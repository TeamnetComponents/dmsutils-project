package ro.croco.integration.dms.toolkit;

import org.apache.commons.dbcp.BasicDataSource;
import ro.croco.integration.dms.commons.TemplateEngine;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceNotDefinedException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

/**
 * Created by Lucian.Dragomir on 8/13/2014.
 */
public class MetadataServiceImpl_Db implements MetadataService {

    private static final String METADATA_CONFIGURATION_CONNECTION = "metadata.configuration.connection";
    private static final String METADATA_CONFIGURATION_SCHEMA = "metadata.configuration.schema";
    private static final String METADATA_CONFIGURATION_TEMPLATE_ENGINE = "metadata.configuration.template.engine";

    private static final String CONNECTION_TYPE = "jdbc.${connectionName}.type";
    private static final String CONNECTION_URL = "jdbc.${connectionName}.url";
    private static final String CONNECTION_DRIVER = "jdbc.${connectionName}.driver";
    private static final String CONNECTION_USER = "jdbc.${connectionName}.user";
    private static final String CONNECTION_PASSWORD = "jdbc.${connectionName}.password";
    private static final String CONNECTION_SCHEMA = "jdbc.${connectionName}.schema";
    private static final String CONNECTION_TYPE_LOCAL = "local";
    private static final String CONNECTION_TYPE_JNDI = "jndi";

    private static final String SQL_CONFIGURATION_BY_OBJECT_CODE =
            "             SELECT M.* " + '\n' +
                    "          FROM ${schemaName}.DMSUTILS_METADATA M " + '\n' +
                    "          JOIN ${schemaName}.DMSUTILS_REPOSITORY RD ON ',' || M.REPOSITORY_NAME_DST || ',' LIKE '%,' || RD.REPOSITORY_NAME || ',%' " + '\n' +
                    "          JOIN ${schemaName}.DMSUTILS_REPOSITORY RS ON ',' || M.REPOSITORY_NAME_SRC || ',' LIKE '%,' || RS.REPOSITORY_NAME || ',%' " + '\n' +
                    "     WHERE M.OBJECT_CODE IN ( ?, '*') " + '\n' +
                    "       AND M.OBJECT_TYPE = ? " + '\n' +
                    "       AND M.OBJECT_CONTEXT IN ( ?, '*') " + '\n' +
                    "       AND M.OPERATION_NAME = ? " + '\n' +
                    "       AND RD.STORE_SERVICE_NAME = ? " + '\n' +
                    "       AND RS.STORE_SERVICE_NAME = ? " + '\n' +
                    "     ORDER BY M.OBJECT_PRIORITY";

    private static final String SQL_CONFIGURATION_BY_GROUP_CODE =
            "             SELECT M.* " + '\n' +
                    "          FROM ${schemaName}.DMSUTILS_GROUP G " + '\n' +
                    "          JOIN ${schemaName}.DMSUTILS_REPOSITORY RD ON ',' || G.REPOSITORY_NAME_DST || ',' LIKE '%,' || RD.REPOSITORY_NAME || ',%' " + '\n' +
                    "          JOIN ${schemaName}.DMSUTILS_REPOSITORY RS ON ',' || G.REPOSITORY_NAME_SRC || ',' LIKE '%,' || RS.REPOSITORY_NAME || ',%' " + '\n' +
                    "          JOIN ${schemaName}.DMSUTILS_METADATA M " +
                    "               ON M.OBJECT_CODE = G.OBJECT_CODE AND M.OBJECT_TYPE = G.OBJECT_TYPE " +
                    "              AND M.REPOSITORY_NAME_DST = G.REPOSITORY_NAME_DST AND M.OPERATION_NAME = G.OPERATION_NAME AND M.REPOSITORY_NAME_SRC = G.REPOSITORY_NAME_SRC " + '\n' +
                    "     WHERE G.GROUP_CODE = ? " + '\n' +
                    "       AND RD.STORE_SERVICE_NAME = ? " + '\n' +
                    "       AND (RS.STORE_SERVICE_NAME = ? OR RS.STORE_SERVICE_NAME = '" + MetadataService.STORE_SERVICE_NA + "')" + '\n' +
                    "     ORDER BY M.OBJECT_FOLDER_TEMPLATE, M.OBJECT_NAME_TEMPLATE";

    private Properties context;
    private TemplateEngine template;


    private static class MetadataConfiguration {
        private String objectCode;
        private String objectType;
        private String objectContext;
        private String destinationRepositoryName;
        private String operationName;
        private String sourceRepositoryName;
        private String sourceIdentifierTemplate;
        private String folderTemplate;
        private String nameTemplate;
        private String maskTemplate;
        private String versioningTemplate;
        private String connectionName;
        private String sql;
        private boolean allowCreatePath;

        private MetadataConfiguration() {
            allowCreatePath = false;
        }

        private MetadataConfiguration(String objectCode, String objectType, String objectContext, String destinationRepositoryName, String operationName, String sourceRepositoryName, String sourceIdentifierTemplate, String folderTemplate, String nameTemplate, String maskTemplate, String versioningTemplate, String connectionName, String sql, boolean allowCreatePath) {
            this.objectCode = objectCode;
            this.objectType = objectType;
            this.objectContext = objectContext;
            this.destinationRepositoryName = destinationRepositoryName;
            this.operationName = operationName;
            this.sourceRepositoryName = sourceRepositoryName;
            this.sourceIdentifierTemplate = sourceIdentifierTemplate;
            this.folderTemplate = folderTemplate;
            this.nameTemplate = nameTemplate;
            this.maskTemplate = maskTemplate;
            this.versioningTemplate = versioningTemplate;
            this.connectionName = connectionName;
            this.sql = sql;
            this.allowCreatePath = allowCreatePath;
        }

        public String getObjectCode() {
            return objectCode;
        }

        public void setObjectCode(String objectCode) {
            this.objectCode = objectCode;
        }

        public String getObjectType() {
            return objectType;
        }

        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }

        public String getObjectContext() {
            return objectContext;
        }

        public void setObjectContext(String objectContext) {
            this.objectContext = objectContext;
        }

        public String getDestinationRepositoryName() {
            return destinationRepositoryName;
        }

        public void setDestinationRepositoryName(String destinationRepositoryName) {
            this.destinationRepositoryName = destinationRepositoryName;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public String getSourceRepositoryName() {
            return sourceRepositoryName;
        }

        public void setSourceRepositoryName(String sourceRepositoryName) {
            this.sourceRepositoryName = sourceRepositoryName;
        }

        public String getSourceIdentifierTemplate() {
            return sourceIdentifierTemplate;
        }

        public void setSourceIdentifierTemplate(String sourceIdentifierTemplate) {
            this.sourceIdentifierTemplate = sourceIdentifierTemplate;
        }

        public String getFolderTemplate() {
            return folderTemplate;
        }

        public void setFolderTemplate(String folderTemplate) {
            this.folderTemplate = folderTemplate;
        }

        public String getNameTemplate() {
            return nameTemplate;
        }

        public void setNameTemplate(String nameTemplate) {
            this.nameTemplate = nameTemplate;
        }

        public String getMaskTemplate() {
            return maskTemplate;
        }

        public void setMaskTemplate(String maskTemplate) {
            this.maskTemplate = maskTemplate;
        }

        public String getVersioningTemplate() {
            return versioningTemplate;
        }

        public void setVersioningTemplate(String versioningTemplate) {
            this.versioningTemplate = versioningTemplate;
        }

        public String getConnectionName() {
            return connectionName;
        }

        public void setConnectionName(String connectionName) {
            this.connectionName = connectionName;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public boolean isAllowCreatePath() {
            return allowCreatePath;
        }

        public void setAllowCreatePath(boolean allowCreatePath) {
            this.allowCreatePath = allowCreatePath;
        }
    }

    public MetadataServiceImpl_Db() {
    }

    @Override
    public void __init(Properties context) {
        this.context = context;
        this.template = TemplateEngine.getInstance(TemplateEngine.TemplateEngineType.fromValue((String) getContextProperty(METADATA_CONFIGURATION_TEMPLATE_ENGINE)));
    }

    @Override
    public Properties getContext() {
        return this.context;
    }

    @Override
    public Object getContextProperty(String propertyName) {
        return context.get(propertyName);
    }

    @Override
    public String getName() {
        return (String) context.get(StoreServiceFactory.INSTANCE_NAME);
    }

    @Override
    public List<String> getDocumentsSupportedForStore(StoreService storeServiceDestination, StoreContext storeContextDestination) {
        return null;
    }

    @Override
    public Metadata<DocumentInfo> computeDocumentMetadata(StoreService storeServiceDestination, StoreContext storeContextDestination, MetadataProperties metadataProperties) {
        String documentCode;
        String documentContext;
        documentCode = (String) metadataProperties.getMetadataProperty(MetadataPropertySpecial.Code).getValue();
        documentContext = (String) metadataProperties.getMetadataProperty(MetadataPropertySpecial.Context).getValue();
        return computeDocumentMetadata(documentCode, documentContext, storeServiceDestination, storeContextDestination, metadataProperties.getAsPproperties());
    }


    @Override
    public Metadata<DocumentInfo> computeDocumentMetadata(String documentCode, String documentContext, StoreService storeServiceDestination, StoreContext storeContextDestination, Properties properties) {
        Metadata<DocumentInfo> metadata;
        String operationName = storeContextDestination.getMetadataOperation().equals(MetadataService.RULE_DEFAULT) ? MetadataService.RULE_STORE_DEFAULT : storeContextDestination.getMetadataOperation();
        MetadataConfiguration metadataConfiguration = getConfigurationByObjectCode(documentCode, documentContext, ObjectBaseType.DOCUMENT, operationName, storeServiceDestination.getName(), MetadataService.STORE_SERVICE_NA);
        metadata = (Metadata<DocumentInfo>) computeMetadata(metadataConfiguration, properties);
        return metadata;


        // calculate object properties
        //MetadataProperties objectProperties = calculateObjectProperties(metadataConfiguration.getConnectionName(), metadataConfiguration.getSql(), properties);
//        //calculate values based on properties
//        String path = getValueFromTemplate(metadataConfiguration.getFolderTemplate(), objectProperties);
//        String nameWithExtension = getValueFromTemplate(metadataConfiguration.getNameTemplate(), objectProperties);
//        if (properties.containsKey("documentName")) {
//            //overwrite extension with the real extension filename
//            nameWithExtension = fileUtils.getFileNameWithExtension(fileUtils.getFileBaseName(nameWithExtension), fileUtils.getFileExtension(properties.getProperty("documentName")));
//        }
//        String mask = getValueFromTemplate(metadataConfiguration.getMaskTemplate(), objectProperties);
//        DocumentInfo documentInfo = new DocumentInfo(path, fileUtils.getFileBaseName(nameWithExtension), fileUtils.getFileExtension(nameWithExtension), mask, objectProperties.getAsMap(true));
//        metadata.setInfo(documentInfo);
//        metadata.setVersioningType(metadataConfiguration.getVersioning());
//        return metadata;
    }

    @Override
    public Metadata<DocumentInfo> computeDocumentMetadata(DocumentIdentifier documentIdentifierSource, StoreService storeServiceSource, StoreContext storeContextSource, StoreService storeServiceDestination, StoreContext storeContextDestination) {
        Metadata<DocumentInfo> metadata = new Metadata<DocumentInfo>();


        //Metadata<DocumentInfo> metadata = new Metadata<DocumentInfo>();
//        DocumentInfo documentInfo = storeServiceSource.getDocumentInfo(storeContextSource, documentIdentifierSource);
//        MetadataConfiguration metadataConfiguration = getConfigurationByObjectCode(documentInfo.getType(), ObjectBaseType.DOCUMENT, "MOVE", storeServiceDestination.getName(), storeServiceSource.getName());
//        return (Metadata<FolderInfo>) computeMetadata(metadataConfiguration, properties);
        return metadata;
    }

    @Override
    public Metadata<FolderInfo> computeFolderMetadata(StoreService storeServiceDestination, StoreContext storeContextDestination, MetadataProperties metadataProperties) {
        String folderCode;
        String folderContext;
        folderCode = (String) metadataProperties.getMetadataProperty(MetadataPropertySpecial.Code).getValue();
        folderContext = (String) metadataProperties.getMetadataProperty(MetadataPropertySpecial.Context).getValue();
        return computeFolderMetadata(folderCode, folderContext, storeServiceDestination, storeContextDestination, metadataProperties.getAsPproperties());
    }

    @Override
    public Metadata<FolderInfo> computeFolderMetadata(String folderCode, String folderContext, StoreService storeServiceDestination, StoreContext storeContextDestination, Properties properties) {
//        Metadata<FolderInfo> metadata = new Metadata<FolderInfo>();
        String operationName = storeContextDestination.getMetadataOperation().equals(MetadataService.RULE_DEFAULT) ? MetadataService.RULE_STORE_DEFAULT : storeContextDestination.getMetadataOperation();
        MetadataConfiguration metadataConfiguration = getConfigurationByObjectCode(folderCode, folderContext, ObjectBaseType.FOLDER, operationName, storeServiceDestination.getName(), MetadataService.STORE_SERVICE_NA);
        return (Metadata<FolderInfo>) computeMetadata(metadataConfiguration, properties);
//        // calculate object properties
//        MetadataProperties objectProperties = calculateObjectProperties(metadataConfiguration.getConnectionName(), metadataConfiguration.getSql(), properties);
//        //calculate values based on properties
//        String path = getValueFromTemplate(metadataConfiguration.getFolderTemplate(), objectProperties);
//        String name = getValueFromTemplate(metadataConfiguration.getNameTemplate(), objectProperties);
//        String mask = getValueFromTemplate(metadataConfiguration.getMaskTemplate(), objectProperties);
//        FolderInfo folderInfo = new FolderInfo(path, fileUtils.getFileBaseName(name), mask, objectProperties.getAsMap(true));
//        metadata.setInfo(folderInfo);
//        metadata.setVersioningType(metadataConfiguration.getVersioning());
//        return metadata;
    }

    @Override
    public List<Metadata<? extends ObjectInfo>> computeGroupMetadata(String groupCode, StoreService storeServiceDestination, StoreContext storeContextDestination, Properties properties) {
        List<Metadata<? extends ObjectInfo>> metadataList = new ArrayList<Metadata<? extends ObjectInfo>>();
        List<MetadataConfiguration> metadataConfigurationList = getConfigurationByGroupCode(groupCode, storeServiceDestination.getName(), MetadataService.STORE_SERVICE_NA);
        for (MetadataConfiguration metadataConfiguration : metadataConfigurationList) {
            metadataList.add(computeMetadata(metadataConfiguration, properties));
        }
        return metadataList;
    }

    //connection detail helper methods
    private String getConnectionType(String connectionName) {
        return (String) this.getContextProperty(template.getValueFromTemplate(CONNECTION_TYPE, "connectionName", connectionName));
    }

    private String getConnectionUrl(String connectionName) {
        return (String) this.getContextProperty(template.getValueFromTemplate(CONNECTION_URL, "connectionName", connectionName));
    }

    private String getConnectionDriver(String connectionName) {
        return (String) this.getContextProperty(template.getValueFromTemplate(CONNECTION_DRIVER, "connectionName", connectionName));
    }

    private String getConnectionUser(String connectionName) {
        return (String) this.getContextProperty(template.getValueFromTemplate(CONNECTION_USER, "connectionName", connectionName));
    }

    private String getConnectionPassword(String connectionName) {
        return (String) this.getContextProperty(template.getValueFromTemplate(CONNECTION_PASSWORD, "connectionName", connectionName));
    }

    private String getConnectionSchema(String connectionName) {
        return (String) this.getContextProperty(template.getValueFromTemplate(CONNECTION_SCHEMA, "connectionName", connectionName));
    }

    private Connection getConnection(String connectionName) {
        DataSource dataSource = null;
        Connection connection = null;
        String connectionType = getConnectionType(connectionName);
        if (CONNECTION_TYPE_LOCAL.equalsIgnoreCase(connectionType)) {
            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(getConnectionDriver(connectionName));
            basicDataSource.setUrl(getConnectionUrl(connectionName));
            basicDataSource.setUsername(getConnectionUser(connectionName));
            basicDataSource.setPassword(getConnectionPassword(connectionName));
            String connectionSchema = getConnectionSchema(connectionName);
            if (connectionSchema != null && !connectionSchema.isEmpty()) {
                basicDataSource.addConnectionProperty("currentSchema", connectionSchema);
            }
            dataSource = basicDataSource;
        } else if (CONNECTION_TYPE_JNDI.equalsIgnoreCase(connectionType)) {
            try {
                Context initContext = new InitialContext();
                dataSource = (DataSource) initContext.lookup(getConnectionUrl(connectionName));
            } catch (NamingException e) {
                e.printStackTrace();
                throw new StoreServiceException(e);
            }
        } else {
            throw new StoreServiceNotDefinedException("The connection name " + connectionName + " is not defined.");
        }

        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreServiceException(e);
        } finally {
            dataSource = null;
        }
        return connection;
    }

    private Connection getConfigurationConnection() {
        return getConnection((String) this.context.get(METADATA_CONFIGURATION_CONNECTION));
    }

    private MetadataProperties calculateObjectProperties(String connectionName, String sqlProperties, Properties properties) {
        return calculateObjectProperties(connectionName, sqlProperties, new HashMap<String, Object>((Map) properties));
    }


    private MetadataProperties calculateObjectProperties(String connectionName, String sqlProperties, Map<String, Object> properties) {
        MetadataProperties metadataProperties = new MetadataProperties();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            //if new properties must be calculated from DB
            if (connectionName != null && !connectionName.isEmpty() && sqlProperties != null && !sqlProperties.isEmpty()) {
                connection = getConnection(connectionName);
                String SQL = template.getValueFromTemplate(sqlProperties, properties);
                System.out.println(SQL);
                statement = connection.createStatement();
                resultSet = statement.executeQuery(SQL);
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                if (resultSet.next()) {
                    //multiple property rows having PROPERTY_NAME, PROPERTY_TYPE, PROPERTY_VALUE, PROPERTY_VISIBLE
                    if (
                            (resultSetMetaData.getColumnCount() >= 4) &&
                                    (
                                            resultSetMetaData.getColumnLabel(1).equalsIgnoreCase("P_NAME") &&
                                                    resultSetMetaData.getColumnLabel(2).equalsIgnoreCase("P_TYPE") &&
                                                    resultSetMetaData.getColumnLabel(3).equalsIgnoreCase("P_VALUE") &&
                                                    resultSetMetaData.getColumnLabel(4).equalsIgnoreCase("P_VISIBLE")
                                    )
                            ) {
                        do {
                            String name = resultSet.getString(1);
                            String type = resultSet.getString(2);
                            Object value = null;
                            switch (type) {
                                case "BigDecimal":
                                    value = resultSet.getBigDecimal(3);
                                    break;
                                case "Boolean":
                                    value = resultSet.getBoolean(3);
                                    break;
                                case "Byte":
                                    value = resultSet.getByte(3);
                                    break;
                                case "Int":
                                    value = resultSet.getInt(3);
                                    break;
                                case "Double":
                                    value = resultSet.getDouble(3);
                                    break;
                                case "Date":
                                    value = resultSet.getDate(3);
                                    break;
                                case "Long":
                                    value = resultSet.getLong(3);
                                    break;
                                default:
                                    value = resultSet.getString(3);
                            }
                            boolean visible = resultSet.getBoolean(4);
                            MetadataProperty metadataProperty = new MetadataProperty(name, value, visible);
                            metadataProperties.setMetadataProperty(metadataProperty);
                        }
                        while (resultSet.next());
                    } else {
                        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                            metadataProperties.setMetadataProperty(new MetadataProperty(resultSetMetaData.getColumnLabel(i), resultSet.getObject(i)));
                        }
                    }
                }
                resultSet.close();
                resultSet = null;
            } else {
                for (Object propertyName : properties.keySet()) {
                    metadataProperties.setMetadataProperty(new MetadataProperty((String) propertyName, properties.get((String) propertyName), false));
                }
            }
        } catch (Exception ex) {
            throw new StoreServiceException(ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
            }
        }
        return metadataProperties;
    }

    private Metadata<? extends ObjectInfo> computeMetadata(MetadataConfiguration metadataConfiguration, Properties properties) {
        Metadata<? extends ObjectInfo> metadata = null;
        String nameWithExtension = null;
        // calculate object properties
        MetadataProperties objectProperties = calculateObjectProperties(metadataConfiguration.getConnectionName(), metadataConfiguration.getSql(), properties);
        //System.out.println(objectProperties);

        //calculate values based on properties
        String path = template.getValueFromTemplate(metadataConfiguration.getFolderTemplate(), objectProperties);
        String name = template.getValueFromTemplate(metadataConfiguration.getNameTemplate(), objectProperties);
        String mask = template.getValueFromTemplate(metadataConfiguration.getMaskTemplate(), objectProperties);
        String sourceIdentifier = template.getValueFromTemplate(metadataConfiguration.getSourceIdentifierTemplate(), objectProperties);
        String versioning = template.getValueFromTemplate(metadataConfiguration.getVersioningTemplate(), objectProperties);
        Boolean allowCreatePath = metadataConfiguration.isAllowCreatePath(); //template.getValueFromTemplate(metadataConfiguration.getFolderTemplate(), objectProperties);

        VersioningType versioningType = VersioningType.NONE;
        if (versioning != null && !versioning.isEmpty()) {
            versioningType = VersioningType.fromValue(versioning);
        }

        if (metadataConfiguration.getObjectType().equals(ObjectBaseType.FOLDER.name())) {
            FolderInfo folderInfo = new FolderInfo(path, MetadataService.fileUtils.getFileBaseName(name), mask, objectProperties.getAsMap(true));
            Metadata<FolderInfo> medatadaFolder = new Metadata<FolderInfo>();
            medatadaFolder.setInfo(folderInfo);
            medatadaFolder.setVersioningType(versioningType);
            medatadaFolder.setAllowCreatePath(allowCreatePath);
            metadata = medatadaFolder;
        } else if (metadataConfiguration.getObjectType().equals(ObjectBaseType.DOCUMENT.name())) {
            if (properties.containsKey(MetadataPropertySpecial.Name.toString())) {
                //overwrite extension with the real extension filename
                name = MetadataService.fileUtils.getFileNameWithExtension(MetadataService.fileUtils.getFileBaseName(name), MetadataService.fileUtils.getFileExtension(properties.getProperty(MetadataPropertySpecial.Name.toString())));
            }
            if (properties.containsKey(MetadataPropertySpecial.Extension.toString())) {
                name = MetadataService.fileUtils.getFileNameWithExtension(MetadataService.fileUtils.getFileBaseName(name), properties.getProperty(MetadataPropertySpecial.Extension.toString()));
            }

            DocumentInfo documentInfo = new DocumentInfo(path, MetadataService.fileUtils.getFileBaseName(name), MetadataService.fileUtils.getFileExtension(name), mask, objectProperties.getAsMap(true));
            Metadata<DocumentInfo> medatadaDocument = new Metadata<DocumentInfo>();
            medatadaDocument.setInfo(documentInfo);
            medatadaDocument.setVersioningType(versioningType);
            medatadaDocument.setAllowCreatePath(allowCreatePath);
            metadata = medatadaDocument;
        } else {
            throw new StoreServiceNotDefinedException("The provided object type is not defined.");
        }
        if (sourceIdentifier != null) {
            throw new StoreServiceNotDefinedException("The source identifier is not allowed in group definitions");
        } else {
            metadata.setSourceIdentifier(null);
        }

        return metadata;
    }


    private MetadataConfiguration getConfigurationByObjectCode(String objectCode, String objectContext, ObjectBaseType objectBaseType, String operationName, String destinationStoreServiceName, String sourceStoreServiceName) {
        MetadataConfiguration metadataConfiguration = null;
        Connection connection = null;
        CallableStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConfigurationConnection();
            String SQL = template.getValueFromTemplate(SQL_CONFIGURATION_BY_OBJECT_CODE, "schemaName", this.context.getProperty(METADATA_CONFIGURATION_SCHEMA));
            //System.out.println(SQL);
            statement = connection.prepareCall(SQL);
            statement.setString(1, objectCode);
            statement.setString(2, objectBaseType.name());
            statement.setString(3, objectContext);
            statement.setString(4, operationName);
            statement.setString(5, destinationStoreServiceName);
            statement.setString(6, sourceStoreServiceName);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                metadataConfiguration = new MetadataConfiguration(
                        resultSet.getString("OBJECT_CODE"),
                        resultSet.getString("OBJECT_TYPE"),
                        resultSet.getString("OBJECT_CONTEXT"),
                        resultSet.getString("REPOSITORY_NAME_DST"),
                        resultSet.getString("OPERATION_NAME"),
                        resultSet.getString("REPOSITORY_NAME_SRC"),
                        resultSet.getString("OBJECT_IDENTIFIER_TEMPLATE_SRC"),
                        resultSet.getString("OBJECT_FOLDER_TEMPLATE"),
                        resultSet.getString("OBJECT_NAME_TEMPLATE"),
                        resultSet.getString("OBJECT_MASK"),
                        resultSet.getString("OBJECT_VERSIONING"),
                        resultSet.getString("OBJECT_PROPERTIES_CONNECTION"),
                        resultSet.getString("OBJECT_PROPERTIES_SQL"),
                        resultSet.getBoolean("OBJECT_CREATE_PATH")
                );
            } else {
                throw new StoreServiceNotDefinedException(
                        String.format("There is no metadata configuration for the requested combination (%s-%s-%s-%s-%s-%s).",
                                objectCode, objectContext, objectBaseType, operationName, destinationStoreServiceName, sourceStoreServiceName
                        ));
            }
        } catch (Exception ex) {
            throw new StoreServiceException(ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
            }
        }
        return metadataConfiguration;
    }


    private List<MetadataConfiguration> getConfigurationByGroupCode(String groupCode, String destinationStoreServiceName, String sourceStoreServiceName) {
        List<MetadataConfiguration> metadataConfigurationList = new ArrayList<MetadataConfiguration>();
        MetadataConfiguration metadataConfiguration = null;
        Connection connection = null;
        CallableStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConfigurationConnection();
            String SQL = template.getValueFromTemplate(SQL_CONFIGURATION_BY_GROUP_CODE, "schemaName", this.context.getProperty(METADATA_CONFIGURATION_SCHEMA));
            System.out.println(SQL);
            statement = connection.prepareCall(SQL);
            statement.setString(1, groupCode);
            statement.setString(2, destinationStoreServiceName);
            statement.setString(3, sourceStoreServiceName);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                metadataConfiguration = new MetadataConfiguration(
                        resultSet.getString("OBJECT_CODE"),
                        resultSet.getString("OBJECT_TYPE"),
                        resultSet.getString("OBJECT_CONTEXT"),
                        resultSet.getString("REPOSITORY_NAME_DST"),
                        resultSet.getString("OPERATION_NAME"),
                        resultSet.getString("REPOSITORY_NAME_SRC"),
                        resultSet.getString("OBJECT_IDENTIFIER_TEMPLATE_SRC"),
                        resultSet.getString("OBJECT_FOLDER_TEMPLATE"),
                        resultSet.getString("OBJECT_NAME_TEMPLATE"),
                        resultSet.getString("OBJECT_MASK"),
                        resultSet.getString("OBJECT_VERSIONING"),
                        resultSet.getString("OBJECT_PROPERTIES_CONNECTION"),
                        resultSet.getString("OBJECT_PROPERTIES_SQL"),
                        resultSet.getBoolean("OBJECT_CREATE_PATH")
                );
                metadataConfigurationList.add(metadataConfiguration);
                metadataConfiguration = null;
            }
        } catch (Exception ex) {
            throw new StoreServiceException(ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) try {
                connection.close();
            } catch (SQLException e) {
            }
        }
        return metadataConfigurationList;
    }

//
//    private String getValueFromTemplate(String template, Properties properties) {
//        return getValueFromTemplate(template, new VelocityContext(properties));
//    }
//
//    private String getValueFromTemplate(String template, Map<String, Object> properties) {
//        return getValueFromTemplate(template, new VelocityContext(properties));
//    }
//
//    private String getValueFromTemplate(String template, MetadataProperties properties) {
//
//        return getValueFromTemplate(template, new VelocityContext(properties.getAsMap()));
//    }
//
//    private String getValueFromTemplate(String template, VelocityContext properties) {
//        //template = "args = #foreach ($arg in $args) $arg #end";
//        //avem buba la Velocity ca nu suporta variabile formate decat din
//        //alphabetic (a .. z, A .. Z)
//        //numeric (0 .. 9)
//        //hyphen ("-")
//        //underscore ("_")
//        if (template == null) {
//            return null;
//        }
//        properties = fixVelocityContext(properties);
//        StringWriter writer = new StringWriter();
//
//        //TODO - trebuie verificat ca este threadsafe
//        synchronized (this) {
//            Velocity.evaluate(properties, writer, "LOG", template);
//        }
//        return String.valueOf(writer.getBuffer());
//    }
//
//    private VelocityContext fixVelocityContext(VelocityContext properties) {
//        for (Object propertyName : properties.getKeys()) {
//            String propertyNameCurrent = (String) propertyName;
//            String propertyNameChecked = propertyNameCurrent.replaceAll("[^a-zA-Z0-9\\-]", "_");
//            if (!propertyNameChecked.equals(propertyNameCurrent)) {
//                properties.put(propertyNameChecked, properties.get(propertyNameCurrent));
//                properties.remove(propertyNameCurrent);
//            }
//        }
//        return properties;
//    }

    public static void mainbkp(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {

        //String oldName = "sdf jncas*&qwed_-S";
        //String newName = oldName.replaceAll("[^a-zA-Z0-9\\-]", "_");
        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode("gigel are mere").array());
        StoreServiceFactory ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-fo.properties");
        //StoreServiceFactory ssf = new StoreServiceFactory("ss-fo.properties"); //META-INF

        StoreService ss = ssf.getService();
        StoreContext sc = StoreContext.builder().build();
        Map<String, Object> documentProperties = new HashMap<String, Object>();
        documentProperties.put("documentKey", "23442352354");
        DocumentInfo documentInfo = new DocumentInfo("/JMeter/test", "upload.txt", "cmis:document~mask~Document elementar", documentProperties);

        DocumentIdentifier documentIdentifier = ss.storeDocument(sc, documentInfo, is, true, VersioningType.MAJOR);

    }


    /*
    public static void main(String[] args) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        StoreServiceFactory ssf = new StoreServiceFactory("C:\\TeamnetProjects\\DMS-UTILS\\cmis.properties");
        StoreService ss = ssf.getService();
        StoreContext sc = StoreContext.builder().build();

        MetadataProperties metadataProperties = MetadataProperties.builder().withCode("dfsd").build().getAsPproperties();
        Properties properties = new Properties();
        properties.put("frontUserName", "gigi");
        properties.put("documentType", "CR-FCR");
        properties.put("documentContext", "DEFAULT");
        properties.put("documentKey", "330");
        properties.put("documentName", "gigi.pdf");
        properties.put("METADATA.OBJECT_CODE", "CR-FCR");

        Metadata<DocumentInfo> metadata = ss.getMetadataService().computeDocumentMetadata(properties.getProperty("documentType"), properties.getProperty("documentContext"), ss, sc, properties);
        System.out.println(metadata);

    }

    public static void mainx(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {

        MetadataProperties mp = MetadataProperties.builder()
                .withDocumentType("CR_LISTA_VERIFICARE")
                .withDocumentKey("ceva, o cheie")
                .withVersioningType(VersioningType.MINOR)
                .withDocumentName("real name.pdf")
                        //.withDocumentContext()
                        //.withDocumentExtension()
                .withVersioningType(VersioningType.MAJOR)
                        //.withFrontUserName("frontUserName")
                .build();
        //DocumentIdentifier documentIdentifier = ss.storeDocument(sc, mp, is);


        Properties properties = new Properties();
        properties.put("frontUserName", "gigi");
        properties.put("documentType", "CR-FCR");
        properties.put("documentContext", "DEFAULT");
        properties.put("documentKey", "2");
        properties.put("documentName", "gigi.pdf");
        //Metadata<DocumentInfo> metadata = ss.getMetadataService().computeDocumentMetadata(properties.getProperty("documentType"), properties.getProperty("documentContext"), ss, sc, properties);
        //System.out.println(metadata);


        StoreServiceFactory ssf2 = new StoreServiceFactory("cmis");
        StoreService ss2 = ssf2.getService();
        StoreContext sc2 = StoreContext.builder().build();
        Properties projectProperties = new Properties();
        projectProperties.put("ID Proiect", 1);
        List<Metadata<?>> metadataList = ss2.getMetadataService().computeGroupMetadata("STRUCTURA_PROIECT", ss2, sc2, projectProperties);
        for (Metadata metadataItem : metadataList) {
            System.out.println(metadataItem);
            ss2.createFolder(sc2, (FolderInfo) metadataItem.getInfo(), metadataItem.isAllowCreatePath());
        }
    }
    */
}
/*
# script for creating the required tables

-- DROP TABLE DMSUTILS_REPOSITORY;
-- SELECT * FROM DMSUTILS_REPOSITORY;
CREATE TABLE DMSUTILS_REPOSITORY
(
    STORE_SERVICE_NAME VARCHAR(100) NOT NULL,
    REPOSITORY_NAME VARCHAR(100) NOT NULL,
    PRIMARY KEY (STORE_SERVICE_NAME)
);

--DELETE FROM DMSUTILS_REPOSITORY;
INSERT INTO DMSUTILS_REPOSITORY (STORE_SERVICE_NAME, REPOSITORY_NAME)
    VALUES ('N/A', 'N/A');
INSERT INTO DMSUTILS_REPOSITORY (STORE_SERVICE_NAME, REPOSITORY_NAME)
    VALUES ('ss-fo', 'ELO-FO');
INSERT INTO DMSUTILS_REPOSITORY (STORE_SERVICE_NAME, REPOSITORY_NAME)
    VALUES ('ss-bo', 'ELO-BO');
INSERT INTO DMSUTILS_REPOSITORY (STORE_SERVICE_NAME, REPOSITORY_NAME)
    VALUES ('ss-fo-jms', 'ELO-BO');
COMMIT;

--DROP TABLE DMSUTILS_METADATA;
CREATE TABLE DMSUTILS_METADATA
(
    OBJECT_CODE VARCHAR(255) NOT NULL, -- will be the mask name of the object
    OBJECT_TYPE VARCHAR(50) NOT NULL CHECK (OBJECT_TYPE IN ('FOLDER', 'DOCUMENT')), -- can be 'FOLDER' or 'DOCUMENT'
    OBJECT_CONTEXT VARCHAR(255) NOT NULL, -- will be the context in wich the document is uploaded
    REPOSITORY_NAME_DST VARCHAR(100) NOT NULL,
    OPERATION_NAME VARCHAR(255) NOT NULL, -- can be STORE OR MOVE
    REPOSITORY_NAME_SRC VARCHAR(100) NOT NULL,
    OBJECT_IDENTIFIER_TEMPLATE_SRC VARCHAR(1000),
    OBJECT_FOLDER_TEMPLATE VARCHAR(1000) NULL,
    OBJECT_NAME_TEMPLATE VARCHAR(255) NULL,
    OBJECT_MASK VARCHAR(255) NULL,
    OBJECT_VERSIONING VARCHAR(50) DEFAULT 'NONE' CHECK (OBJECT_VERSIONING IN ('NONE', 'MINOR', 'MAJOR')),  --can be
    OBJECT_PROPERTIES_CONNECTION VARCHAR(50),
    OBJECT_PROPERTIES_SQL VARCHAR(4000),
    OBJECT_CREATE_PATH INT NOT NULL CHECK (OBJECT_CREATE_PATH IN (0, 1)),
    OBJECT_PRIORITY INT DEFAULT 1000,
    PRIMARY KEY (OBJECT_CODE, OBJECT_TYPE, OBJECT_CONTEXT, REPOSITORY_NAME_DST, OPERATION_NAME, REPOSITORY_NAME_SRC)
);


//
//INSERT INTO DMSUTILS_METADATA  (OBJECT_CODE, OBJECT_TYPE, REPOSITORY_NAME_DST, OPERATION_NAME, REPOSITORY_NAME_SRC, OBJECT_FOLDER_TEMPLATE, OBJECT_NAME_TEMPLATE, OBJECT_MASK, OBJECT_VERSIONING, OBJECT_PROPERTIES_CONNECTION, OBJECT_PROPERTIES_SQL, OBJECT_PRIORITY)
//    VALUES('CerereRambursare', 'DOCUMENT', 'JCR', 'STORE', 'N/A', '/Proiecte/Dosar_${TEMP_PROJ_CODE}/Cerere Rambursare/', 'CerereRambursare_${RR_ID}', 'mix:fileAttributes', 'NONE', 'BackOfficeDB', NULL, 1000);
//INSERT INTO DMSUTILS_METADATA  (OBJECT_CODE, OBJECT_TYPE, REPOSITORY_NAME_DST, OPERATION_NAME, REPOSITORY_NAME_SRC, OBJECT_FOLDER_TEMPLATE, OBJECT_NAME_TEMPLATE, OBJECT_MASK, OBJECT_VERSIONING, OBJECT_PROPERTIES_CONNECTION, OBJECT_PROPERTIES_SQL, OBJECT_PRIORITY)
//    VALUES('CerereRambursare', 'DOCUMENT', 'ELO', 'STORE', 'N/A', '/Proiecte/Dosar$SMIS/Cerere Rambursare/', 'CerereRambursare', 'cmis:CerereRambursare', 'NONE', 'jndi/FrontOfficeDatabase', 'SELECT smisCode AS "SMIS", ''$'' ', 1000);
//COMMIT;



-- DROP TABLE DMSUTILS_GROUP;
-- SELECT * FROM DMSUTILS_GROUP;
CREATE TABLE DMSUTILS_GROUP
(
    GROUP_CODE VARCHAR(100) NOT NULL,
    OBJECT_CODE VARCHAR(255) NOT NULL, -- will be the mask name of the object
    OBJECT_TYPE VARCHAR(50) NOT NULL CHECK (OBJECT_TYPE IN ('FOLDER', 'DOCUMENT')), -- can be 'FOLDER' or 'DOCUMENT'
    OBJECT_CONTEXT VARCHAR(255) NOT NULL, -- will be the context in wich the document is uploaded
    REPOSITORY_NAME_DST VARCHAR(100) NOT NULL,
    OPERATION_NAME VARCHAR(255) NOT NULL, -- can be STORE OR MOVE
    REPOSITORY_NAME_SRC VARCHAR(100) NOT NULL,
    PRIMARY KEY (GROUP_CODE, OBJECT_CODE, OBJECT_TYPE, OBJECT_CONTEXT, REPOSITORY_NAME_DST, REPOSITORY_NAME_SRC)
);

ALTER TABLE DMSUTILS_GROUP ADD CONSTRAINT DMSUTILS_GROUP_FK01 FOREIGN KEY (OBJECT_CODE, OBJECT_TYPE, REPOSITORY_NAME_DST, OPERATION_NAME, REPOSITORY_NAME_SRC)
  REFERENCES DMSUTILS_METADATA (OBJECT_CODE, OBJECT_TYPE, REPOSITORY_NAME_DST, OPERATION_NAME, REPOSITORY_NAME_SRC)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
    ENFORCED
    ENABLE QUERY OPTIMIZATION;

--conventie orice parametru care incepe cu "_" va fi considerat HIDDEN (nu va fi trimis ca proprietate pe document)

--special metadata

SELECT
      P.TEMP_PROJ_CODE AS "_TEMP_PROJ_CODE",
      P.SMIS_CODE AS "_SMIS_CODE",
      RR.ID AS "_RR_ID",
      RR.REQUEST_DATE AS "_RR_REQUEST_DATE",
      '${METADATA.OBJECT_CODE}' AS "_METADATA.OBJECT_CODE",
      '${frontUserName}' AS "frontUserName",
      '${documentType}' AS "documentType",
      '${documentKey}' AS "documentKey"
FROM MEDIU_BO_IMPORT.REIMBURSEMENT_REQUEST RR
JOIN MEDIU_BO_IMPORT.PROJECT P ON P.ID = RR.FK_PROJECT
WHERE RR.ID = ${documentKey}



---SPECIAL PLACEHOLDERS
    $ORIGINAL_FILENAME
    $ORIGINAL_PATH


 */