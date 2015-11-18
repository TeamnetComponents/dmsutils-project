package ro.croco.integration.dms.toolkit;

//import org.apache.chemistry.opencmis.client.api.Tree;
//import org.apache.chemistry.opencmis.client.runtime.util.TreeImpl;

import org.apache.commons.lang.StringUtils;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceNotDefinedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Lucian.Dragomir on 8/23/2014.
 */

public abstract class StoreServiceImpl_Abstract<T extends StoreServiceSession> implements StoreService {
    public static final String SERVICE_STORE_LOCAL_INSTANCE_CONFIGURATION = "service.store.local.instance.configuration";
    public static final String SERVICE_METADATA_INSTANCE_CONFIGURATION = "service.metadata.instance.configuration";
    public static final String SERVICE_STORE_PROPERTY_FORMAT = "store.service.property.format";

    public static enum PropertyFormats {
        DEFAULT,
        ID,
        NAME;

        public static PropertyFormats valueFrom(String value) {
            try {
                return PropertyFormats.valueOf(value.toUpperCase());
            } catch (RuntimeException e) {
                if (StringUtils.isNotEmpty(value)) {
                    throw e;
                }
            }
            return DEFAULT;
        }
    }

    public static final String SERVICE_STORE_PROPERTY_PREFIX_ID = "store.service.property.prefix.id";
    public static final String SERVICE_STORE_PROPERTY_PREFIX_NAME = "store.service.property.prefix.name";
    public static final String SERVICE_STORE_TYPE_PREFIX_ID = "store.service.type.prefix.id";
    public static final String SERVICE_STORE_TYPE_PREFIX_NAME = "store.service.type.prefix.name";

    protected static final FileUtils fileUtils = new FileUtils("/", "/");

    //private Map<String, String> context;
    Properties context;
    private Map<StoreContext.COMMUNICATION_TYPE_VALUES, Boolean> communicationTypeSupport;
    private MetadataService metadataService;
    private StoreService localStoreService;

    //------------------------------------------------------------------------------------------------------------------
    //STATIC METHODS----------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public final String toDMSProperty(String propertyName) {
        String convertedProperty = propertyName;
        String propertyPrefixId = (String) getContextProperty(SERVICE_STORE_PROPERTY_PREFIX_ID);
        String propertyPrefixName = (String) getContextProperty(SERVICE_STORE_PROPERTY_PREFIX_NAME);
        if (propertyName == null) {
            return convertedProperty;
        }
        if (StringUtils.isNotEmpty(propertyPrefixId)) {
            if (convertedProperty.startsWith(propertyPrefixId)) {
                convertedProperty = convertedProperty.substring(propertyPrefixId.length());
            }
        }
        if (StringUtils.isNotEmpty(propertyPrefixName)) {
            if (convertedProperty.startsWith(propertyPrefixName)) {
                convertedProperty = convertedProperty.substring(propertyPrefixName.length());
            }
        }
        return convertedProperty;
    }

    @Override
    public final String toSSProperty(String propertyName) {
        String convertedProperty = propertyName;
        String propertyPrefixId = (String) getContextProperty(SERVICE_STORE_PROPERTY_PREFIX_ID);
        String propertyPrefixName = (String) getContextProperty(SERVICE_STORE_PROPERTY_PREFIX_NAME);
        if (propertyName == null) {
            return convertedProperty;
        }
        boolean isNumeric = true;
        try {
            Integer.parseInt(propertyName);
        } catch (Exception e) {
            isNumeric = false;
        }
        if ((isNumeric) && StringUtils.isNotEmpty(propertyPrefixId)) {
            if ((!convertedProperty.startsWith(propertyPrefixId)) && (!convertedProperty.startsWith(propertyPrefixName))) {
                convertedProperty = propertyPrefixId + convertedProperty;
            }
        }
        if ((!isNumeric) && StringUtils.isNotEmpty(propertyPrefixName)) {
            if ((!convertedProperty.startsWith(propertyPrefixId)) && (!convertedProperty.startsWith(propertyPrefixName))) {
                convertedProperty = propertyPrefixName + convertedProperty;
            }
        }
        return convertedProperty;
    }


    protected static Map<String, String> toMap(Properties properties) {
        Map context = new HashMap();
        for (Object propertyKey : properties.keySet()) {
            context.put(propertyKey, properties.get(propertyKey));
        }
        return context;
    }

    protected static Map<String, String> toMap(Map<String, String> properties) {
        Map context = new HashMap();
        for (Object propertyKey : properties.keySet()) {
            context.put(propertyKey, properties.get(propertyKey));
        }
        return context;
    }

    protected static Properties toProperties(Map properties) {
        Properties context = new Properties();
        context.putAll(properties);
        return context;
    }

    protected static Properties toProperties(Properties properties) {
        Properties context = new Properties();
        context.putAll(properties);
        return context;
    }

    private static ObjectIdentifier moveObject(ObjectIdentifier objectIdentifier, StoreService storeServiceFrom, StoreContext storeContextFrom, StoreService storeServiceTo, StoreContext storeContextTo, MetadataService metadataService) {
        ObjectIdentifier objectIdentifierReturn = null;
        ObjectInfo objectInfo = null;
        MetadataService.Metadata objectMetadata = null;
        if (objectIdentifier instanceof DocumentIdentifier) {
            objectInfo = storeServiceFrom.getDocumentInfo(storeContextFrom, (DocumentIdentifier) objectIdentifier);
            objectMetadata = metadataService.computeDocumentMetadata((DocumentIdentifier) objectIdentifier, storeServiceFrom, storeContextFrom, storeServiceTo, storeContextTo);
            DocumentStream documentStream = storeServiceFrom.downloadDocument(storeContextFrom, (DocumentIdentifier) objectIdentifier);
            objectIdentifierReturn = storeServiceTo.storeDocument(storeContextTo, (DocumentInfo) objectMetadata.getInfo(), documentStream.getInputStream(), true, objectMetadata.getVersioningType());
            storeServiceFrom.deleteDocument(storeContextFrom, (DocumentIdentifier) objectIdentifier);
        } else if (objectIdentifier instanceof FolderIdentifier) {
            throw new StoreServiceException("Move operation is currently not supported for folders.");
        }
        return objectIdentifierReturn;
    }

    public static ObjectIdentifier moveObject(StoreService storeServiceFrom, StoreContext storeContextFrom, ObjectIdentifier objectIdentifierFrom,
                                              StoreService storeServiceTo, StoreContext storeContextTo, ObjectInfo objectInfoTo,
                                              boolean allowCreatePath, VersioningType versioningType) {
        if (objectIdentifierFrom == null) {
            throw new StoreServiceException("The objectIdentifierFrom must be not null on the moveObject operation.");
        }
        if (objectInfoTo != null) {
            if (((objectIdentifierFrom instanceof DocumentIdentifier) && !(objectInfoTo instanceof DocumentInfo)) ||
                    ((objectIdentifierFrom instanceof FolderIdentifier) && !(objectInfoTo instanceof FolderInfo))) {
                throw new StoreServiceException("The source and destionation objects must have the same type.");
            }
        }
        ObjectIdentifier objectIdentifierReturn = null;
        ObjectInfo objectInfoFrom = null;
        if (objectIdentifierFrom instanceof DocumentIdentifier) {
            objectInfoFrom = storeServiceFrom.getDocumentInfo(storeContextFrom, (DocumentIdentifier) objectIdentifierFrom);
            DocumentStream documentStream = storeServiceFrom.downloadDocument(storeContextFrom, (DocumentIdentifier) objectIdentifierFrom);
            if (objectInfoTo == null) {
                objectInfoTo = objectInfoFrom;
            } else {
                if (objectInfoTo.getProperties() == null) {
                    objectInfoTo.setProperties(objectInfoFrom.getProperties());
                }
                if (StringUtils.isEmpty(objectInfoTo.getType())) {
                    objectInfoTo.setType(objectInfoFrom.getType());
                }
                if (StringUtils.isEmpty(objectInfoTo.getName())) {
                    objectInfoTo.setName(objectInfoFrom.getName());
                }
                if (objectInfoTo.getParentIdentifier() == null) {
                    objectInfoTo.setParentIdentifier(objectInfoFrom.getParentIdentifier());
                }
            }
            objectIdentifierReturn = storeServiceTo.storeDocument(storeContextTo, (DocumentInfo) objectInfoTo, documentStream.getInputStream(), allowCreatePath, versioningType);
            storeServiceFrom.deleteDocument(storeContextFrom, (DocumentIdentifier) objectIdentifierFrom);
        } else if (objectIdentifierFrom instanceof FolderIdentifier) {
            throw new StoreServiceException("Move operation is currently not supported for folders.");
        }
        return objectIdentifierReturn;
    }


    //------------------------------------------------------------------------------------------------------------------
    //INSTANCE METHODS--------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    public StoreServiceImpl_Abstract() {
        communicationTypeSupport = new HashMap<StoreContext.COMMUNICATION_TYPE_VALUES, Boolean>();
        setCommunicationTypeSupport(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS, true);
    }

    protected abstract T openSession(StoreContext storeContext);

    protected abstract void closeSession(T storeSession);


    public boolean getCommunicationTypeSupport(StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        return (communicationTypeSupport.get(communicationType) == null) ? false : communicationTypeSupport.get(communicationType);
    }

    protected void setCommunicationTypeSupport(StoreContext.COMMUNICATION_TYPE_VALUES communicationType, boolean value) {
        communicationTypeSupport.put(communicationType, value);
    }

    public Properties getContext() {
        return this.context;
    }

    @Override
    public Object getContextProperty(String propertyName) {
        return this.context.get(propertyName);
    }

    protected void validateStoreContext(StoreContext storeContext) {
        if (!getCommunicationTypeSupport(storeContext.getCommunicationType())) {
            throw new StoreServiceException("This store service does not support " + storeContext.getCommunicationType().name() + " communication type");
        }
    }

    protected StoreContext prepareStoreContext(StoreContext storeContext, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        validateStoreContext(storeContext);
        // TODO
        StoreContext preparedStoreContext = (StoreContext) storeContext.clone();
        switch (communicationType) {
            case SYNCHRONOUS_LOCAL:
                preparedStoreContext.put(StoreContext.COMMUNICATION_TYPE, StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS);
                break;
            case SYNCHRONOUS:
                preparedStoreContext.put(StoreContext.COMMUNICATION_TYPE, StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS);
                break;
            case ASYNCHRONOUS:
                preparedStoreContext.put(StoreContext.COMMUNICATION_TYPE, StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS);
                break;
        }
        return preparedStoreContext;
    }

    protected StoreContext prepareStoreContext(StoreContext storeContext) {
        return prepareStoreContext(storeContext, storeContext.getCommunicationType());
    }

    @Override
    public void __init(Properties context) throws IOException {
        //this.context = toMap(context);
        this.context = toProperties(context);

        //get parent paths to optimize configuration file detection
        String instanceSource = this.context.getProperty(ServiceFactory_Abstract.INSTANCE_SOURCE);
        String instanceSourceType = this.context.getProperty(ServiceFactory_Abstract.INSTANCE_SOURCE_TYPE);
        if (instanceSourceType == null) {
            instanceSourceType = ServiceFactory_Abstract.ContextType.ANY.name();
        }
        ServiceFactory_Abstract.ContextType contextType = ServiceFactory_Abstract.ContextType.valueOf(instanceSourceType);
        FileUtils fileUtils = FileUtils.proposeFileUtils(instanceSource);


        String[] alternatePaths = null;
        if (StringUtils.isNotEmpty(instanceSource)) {
            String instancePath = fileUtils.getParentFolderPathName(instanceSource);
            if (!instancePath.equals(fileUtils.getRootPath())) {
                alternatePaths = new String[]{instancePath};
            } else {
                alternatePaths = new String[0];
            }
        }

        //get local store service if available
        String serviceName = (String) getContextProperty(SERVICE_STORE_LOCAL_INSTANCE_CONFIGURATION);
        if (StringUtils.isNotEmpty(serviceName)) {
            try {
                setLocalStoreService((new StoreServiceFactory(serviceName, contextType, alternatePaths)).getService());
            } catch (Exception e) {
                throw new RuntimeException("Unable to create a store service instance for " + serviceName, e);
            }
        }

        //get metadata service if available
        String serviceMetadata = (String) this.context.get(SERVICE_METADATA_INSTANCE_CONFIGURATION);
        if (StringUtils.isNotEmpty(serviceMetadata)) {
            MetadataServiceFactory metadataServiceFactory = new MetadataServiceFactory(serviceMetadata, contextType, alternatePaths);
            MetadataService metadataService = null;
            try {
                metadataService = metadataServiceFactory.getMetadata();
            } catch (Exception e) {
                throw new StoreServiceException(e);
            }
            setMetadataService(metadataService);
        }
    }

    @Override
    public void close() {
        //this method can be overridden in subclasses
    }

    @Override
    public final void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public final MetadataService getMetadataService() {
        return metadataService;
    }

    @Override
    public StoreService getLocalStoreService() {
        return this.localStoreService;
    }

    @Override
    public void setLocalStoreService(StoreService localStoreService) {
        this.localStoreService = localStoreService;
        setCommunicationTypeSupport(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL, (localStoreService != null));
    }

    @Override
    public String getName() {
        return (String) context.get(StoreServiceFactory.INSTANCE_NAME);
    }

    @Override
    public ObjectIdentifier moveFrom(StoreContext storeContext, ObjectIdentifier objectIdentifierFrom, StoreService storeServiceFrom, StoreContext storeContextFrom) {
        if (getMetadataService() == null) {
            throw new StoreServiceNotDefinedException("No instance of storeMetadata is associated with this service.");
        }
        return moveObject(objectIdentifierFrom, storeServiceFrom, storeContextFrom, this, storeContext, getMetadataService());
    }

    @Override
    public ObjectIdentifier moveTo(StoreContext storeContext, ObjectIdentifier objectIdentifier, StoreService storeServiceTo, StoreContext storeContextTo) {
        if (getMetadataService() == null) {
            throw new StoreServiceNotDefinedException("No instance of storeMetadata is associated with this service.");
        }
        return moveObject(objectIdentifier, this, storeContext, storeServiceTo, storeContextTo, getMetadataService());
    }

    @Override
    public ObjectIdentifier[] moveFrom(StoreContext storeContext, ObjectIdentifier[] objectIdentifierFrom, StoreService storeServiceFrom, StoreContext storeContextFrom) {
        ObjectIdentifier[] objectIdentifiers = new ObjectIdentifier[objectIdentifierFrom.length];
        for (int index = 0; index < objectIdentifierFrom.length; index++) {
            ObjectIdentifier objectIdentifier = null;
            try {
                objectIdentifier = moveFrom(storeContext, objectIdentifierFrom[index], storeServiceFrom, storeContextFrom);
            } catch (Exception e) {
                objectIdentifier = objectIdentifierFrom[index] instanceof DocumentIdentifier ? new DocumentIdentifier() : new FolderIdentifier();
                objectIdentifier.setRequestId(storeContext.getRequestIdentifier());
                objectIdentifier.setException(e);
            }
            objectIdentifiers[index] = objectIdentifier;
        }
        return objectIdentifiers;
    }

    @Override
    public ObjectIdentifier[] moveTo(StoreContext storeContext, ObjectIdentifier[] objectIdentifierFrom, StoreService storeServiceTo, StoreContext storeContextTo) {
        ObjectIdentifier[] objectIdentifiers = new ObjectIdentifier[objectIdentifierFrom.length];
        for (int index = 0; index < objectIdentifierFrom.length; index++) {
            ObjectIdentifier objectIdentifier = null;
            try {
                objectIdentifier = moveTo(storeContext, objectIdentifierFrom[index], storeServiceTo, storeContextTo);
            } catch (Exception e) {
                objectIdentifier = objectIdentifierFrom[index] instanceof DocumentIdentifier ? new DocumentIdentifier() : new FolderIdentifier();
                objectIdentifier.setRequestId(storeContext.getRequestIdentifier());
                objectIdentifier.setException(e);
            }
            objectIdentifiers[index] = objectIdentifier;
        }
        return objectIdentifiers;
    }

    @Override
    public final DocumentIdentifier storeDocument(StoreContext storeContext, MetadataService.MetadataProperties metadataProperties, InputStream inputStream) {
        if (getMetadataService() == null) {
            throw new StoreServiceNotDefinedException("No instance of storeMetadata is associated with this service.");
        }
        String documentCode = (String) metadataProperties.getMetadataProperty(MetadataService.MetadataPropertySpecial.Code).getValue();
        String documentContext = (String) metadataProperties.getMetadataProperty(MetadataService.MetadataPropertySpecial.Context).getValue();

        MetadataService.Metadata<DocumentInfo> metadata = metadataService.computeDocumentMetadata(documentCode, documentContext, this, storeContext, metadataProperties.getAsPproperties());
        return this.storeDocument(storeContext, metadata.getInfo(), inputStream, metadata.isAllowCreatePath(), metadata.getVersioningType());
    }

    @Override
    public FolderIdentifier updateFolderProperties(StoreContext storeContext, FolderIdentifier folerIdentifier, FolderInfo folderInfo) {
        throw new StoreServiceException("The method updateFolderProperties is not implemented");
    }

    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        throw new StoreServiceException("The method storeDocument is not implemented");
    }

    @Override
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        throw new StoreServiceException("The method deleteDocument is not implemented");
    }

    @Override
    public DocumentInfo getDocumentInfo(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        throw new StoreServiceException("The method renameDocument is not implemented");
    }

    @Override
    public FolderIdentifier renameFolder(StoreContext storeContext, FolderIdentifier folderIdentifier, String newFolderName) {
        throw new StoreServiceException("The method renameFolder is not implemented");
    }

    @Override
    public FolderInfo getFolderInfo(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        throw new StoreServiceException("The method getFolderInfo is not implemented");
    }

    @Override
    public RequestIdentifier deleteFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        throw new StoreServiceException("The method deleteFolder is not implemented");
    }

    @Override
    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo, boolean createParentIfNotExists) {
        throw new StoreServiceException("The method createFolder is not implemented");
    }

    @Override
    public BooleanResponse existsFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        throw new StoreServiceException("The method existsFolder is not implemented");
    }

    @Override
    public DocumentIdentifier renameDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier, String newDocumentName) {
        throw new StoreServiceException("The method renameDocument is not implemented");
    }

    @Override
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        throw new StoreServiceException("The method downloadDocument is not implemented");
    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        throw new StoreServiceException("The method existsDocument is not implemented");
    }

    @Override
    public DocumentIdentifier updateDocumentProperties(StoreContext storeContext, DocumentIdentifier documentIdentifier, DocumentInfo documentInfo) {
        throw new StoreServiceException("The method updateDocumentProperties is not implemented");
    }

    protected abstract ObjectInfo[] listFolderContent(T storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes);

    protected abstract FolderInfo getFolderInfo(T storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier);

    private final Tree<ObjectInfo> listFolderContentTree(T storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, int depth, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        List<ObjectBaseType> objectBaseTypeList = Arrays.asList(objectBaseTypes);
        FolderInfo folderInfo = getFolderInfo(storeSession, storeContext, folderIdentifier);
        List<Tree<ObjectInfo>> children = new ArrayList<Tree<ObjectInfo>>();
        if (depth > 0 || depth == -1) {
            // depth =  0 iese din recursivitate
            // depth = -1 listeaza tot pana la frunze
            // depth >  0 listeaza pana la adancimea specificata
            int nextDepth = (depth == -1) ? depth : depth - 1;
            ObjectInfo[] objectInfos = listFolderContent(storeSession, storeContext, folderIdentifier, includeInfo, ObjectBaseType.FOLDER, ObjectBaseType.DOCUMENT);
            for (ObjectInfo objectInfo : objectInfos) {
                if (objectInfo instanceof FolderInfo) {
                    if (objectBaseTypeList.contains(ObjectBaseType.FOLDER)) {
                        children.add(listFolderContentTree(storeSession, storeContext, (FolderIdentifier) objectInfo.getIdentifier(), nextDepth, includeInfo, objectBaseTypes));
                    }
                } else if (objectInfo instanceof DocumentInfo) {
                    if (objectBaseTypeList.contains(ObjectBaseType.DOCUMENT)) {
                        Tree<ObjectInfo> child = new TreeImpl(objectInfo, null);
                        children.add(child);
                    }
                }
            }
        }
        return new TreeImpl(folderInfo, children);
    }

    @Override
    public ObjectInfoTree listFolderContent(StoreContext storeContext, FolderIdentifier folderIdentifier, int depth, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        ObjectInfoTree objectInfoTree = null;
        T storeSession = null;
        try {
            storeSession = openSession(storeContext);
            objectInfoTree = new ObjectInfoTree();
            objectInfoTree.setContent(listFolderContentTree(storeSession, storeContext, folderIdentifier, depth, includeInfo, objectBaseTypes));
            objectInfoTree.setRequestId(storeContext.getRequestIdentifier());
        } catch (StoreServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new StoreServiceException(e);
        } finally {
            closeSession(storeSession);
        }
        return objectInfoTree;
    }

    @Override
    public String getPathByConfiguration(PathConfiguration pathConfiguration) {
        return pathConfiguration.getValue(this);
    }
}
