package ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentInfo;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.context.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.SqlOperationTranslator;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
public class UnversionedStoreDocument extends StoreDocumentStrategy{

    public UnversionedStoreDocument(StoreServiceSessionImpl_Db session){
        super(session);
    }

    private final static String UPDATE = "update";
    private final static String FRESH = "fresh";

    private boolean isUpdate(){
        return documentInfo.getIdentifier() != null;
    }

    private boolean allowCreatePath(){
        return documentInfo.getProperties().get("allowCreatePath").equals(true);
    }

    private PreparedStatement prepareDMObjectsInsertCmd() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String insertDMObject = SqlOperationTranslator.translateCommand("i:DM_OBJECTS(OBJ_PATH,OBJ_NAME)",SqlOperationTranslator.PREPARED_STATEMENT,dbSchema);
        System.out.println("DmObjectsInsertCmd = " + insertDMObject);
        return connection.prepareStatement(insertDMObject,Statement.RETURN_GENERATED_KEYS);
    }

    private PreparedStatement prepareDMStreamsInsertCmd() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String insertDMStreams = SqlOperationTranslator.translateCommand("i:DM_STREAMS(NAME,FILE_STREAM)",SqlOperationTranslator.PREPARED_STATEMENT,dbSchema);
        System.out.println("DmStreamsInsertCmd = " + insertDMStreams);
        return connection.prepareStatement(insertDMStreams);
    }

    private PreparedStatement prepareDMObjectVersionsInsertCmd() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String insertDmObjectVersions = SqlOperationTranslator.translateCommand("i:DM_OBJECT_VERSIONS(FK_DM_OBJECTS,FK_DM_STREAMS,STREAM_NAME,MIME_TYPE)",SqlOperationTranslator.PREPARED_STATEMENT,dbSchema);
        System.out.println("DmObjectVersionsInsertCmd = " + insertDmObjectVersions);
        return connection.prepareStatement(insertDmObjectVersions,Statement.RETURN_GENERATED_KEYS);
    }

    private DocumentIdentifier constructDocumentIdentifier(String id){
        DocumentIdentifier identifier = new DocumentIdentifier();
        identifier.setId(id);

        if (allowCreatePath())
            identifier.setPath(documentInfo.getParentIdentifier().getPath() + "/" + documentInfo.getName());
        System.out.println(identifier);
        return identifier;
    }

    private DocumentIdentifier doFresh() throws Exception{
        System.out.println("Inside 'doFresh'");
        DocumentIdentifier identifier = null;
        BigDecimal objectRowId = null;
        BigDecimal objectVersionsRowId = null;

        {
            PreparedStatement dmObjectsInsertPS = prepareDMObjectsInsertCmd();
            if (allowCreatePath())
                dmObjectsInsertPS.setString(1, documentInfo.getParentIdentifier().getPath());
            else dmObjectsInsertPS.setNull(1, Types.VARCHAR);

            dmObjectsInsertPS.setString(2, documentInfo.getName());
            dmObjectsInsertPS.execute();

            ResultSet generatedKeys = dmObjectsInsertPS.getGeneratedKeys();
            generatedKeys.next();
            objectRowId = new BigDecimal(generatedKeys.getString(1));
            System.out.println(objectRowId);
            generatedKeys.close();
            dmObjectsInsertPS.close();
        }

        {
            PreparedStatement dmStreamsInsertPS = prepareDMStreamsInsertCmd();
            dmStreamsInsertPS.setString(1,(String)documentInfo.getProperties().get("inputStreamNameId"));
            InputStream inputStream = (InputStream)documentInfo.getProperties().get("inputStream");
            dmStreamsInsertPS.setBinaryStream(2,inputStream);
            dmStreamsInsertPS.execute();
            dmStreamsInsertPS.close();
        }

        {
            PreparedStatement dmObjectVersionsInsertPS = prepareDMObjectVersionsInsertCmd();
            dmObjectVersionsInsertPS.setInt(1,objectRowId.intValue());
            dmObjectVersionsInsertPS.setString(2, (String) documentInfo.getProperties().get("inputStreamNameId"));
            dmObjectVersionsInsertPS.setString(3,documentInfo.getName() + "." + documentInfo.getExtension());
            dmObjectVersionsInsertPS.setString(4,(String)documentInfo.getProperties().get("inputStreamMimeType"));
            dmObjectVersionsInsertPS.execute();
            ResultSet generatedKeys = dmObjectVersionsInsertPS.getGeneratedKeys();
            generatedKeys.next();
            objectVersionsRowId = new BigDecimal(generatedKeys.getString(1));
            System.out.println(objectVersionsRowId);
            dmObjectVersionsInsertPS.close();
            connection.commit();
        }

        //identifier = constructDocumentIdentifier(generatedKeys.getString(1));
        return identifier;
    }

    private PreparedStatement prepareDMObjectCheckExistsCmd() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String checkExistsCmd = SqlOperationTranslator.translateCommand("s:DM_OBJECTS(ID),(ID)",SqlOperationTranslator.PREPARED_STATEMENT,dbSchema);
        System.out.println("DmObjectCheckExistsCmd = " + checkExistsCmd);
        return connection.prepareStatement(checkExistsCmd,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
    }

    private boolean isFreshInsertion() throws Exception {
        PreparedStatement dmObjectCheckExistsPS = prepareDMObjectCheckExistsCmd();
        dmObjectCheckExistsPS.setInt(1,new BigDecimal(documentInfo.getIdentifier().getId()).intValue());
        ResultSet resultSet = dmObjectCheckExistsPS.executeQuery();
        boolean retValue = !resultSet.last();
        resultSet.close();
        dmObjectCheckExistsPS.close();
        return retValue;
    }

    private DocumentIdentifier doUpdate() throws Exception{
        DocumentIdentifier identifier = new DocumentIdentifier();
        System.out.println("Inside doUpdate");
        BigDecimal objectVersionsRowId = null;

        {
            PreparedStatement dmStreamsInsertPS = prepareDMStreamsInsertCmd();
            dmStreamsInsertPS.setString(1,(String)documentInfo.getProperties().get("inputStreamNameId"));
            InputStream inputStream = (InputStream)documentInfo.getProperties().get("inputStream");
            dmStreamsInsertPS.setBinaryStream(2,inputStream);
            dmStreamsInsertPS.execute();
            dmStreamsInsertPS.close();
        }

        {
            PreparedStatement dmObjectVersionsInsertPS = prepareDMObjectVersionsInsertCmd();
            dmObjectVersionsInsertPS.setInt(1,new BigDecimal(documentInfo.getIdentifier().getId()).intValue());
            dmObjectVersionsInsertPS.setString(2, (String) documentInfo.getProperties().get("inputStreamNameId"));
            dmObjectVersionsInsertPS.setString(3,documentInfo.getName() + "." + documentInfo.getExtension());
            dmObjectVersionsInsertPS.setString(4,(String)documentInfo.getProperties().get("inputStreamMimeType"));
            dmObjectVersionsInsertPS.execute();
            ResultSet generatedKeys = dmObjectVersionsInsertPS.getGeneratedKeys();
            generatedKeys.next();
            objectVersionsRowId = new BigDecimal(generatedKeys.getString(1));
            System.out.println(objectVersionsRowId);
            dmObjectVersionsInsertPS.close();
            connection.commit();
        }

        //identifier = constructDocumentIdentifier(generatedKeys.getString(1));
        return identifier;
    }

    @Override
    public DocumentIdentifier process(DocumentInfo documentInfo) {
        try {
            this.documentInfo = documentInfo;
            String storeType = isUpdate() ? UPDATE : FRESH;

            if (storeType.equals(UPDATE)) {
                if (isFreshInsertion())
                    return doFresh();
                else return doUpdate();
            }
            if (storeType.equals(FRESH))
                return doFresh();

            throw new StoreServiceException("Not reaching point");
        }
        catch (Exception ex) {
            try{
                connection.rollback();
                throw new StoreServiceException(ex);
            }
            catch (SQLException e) {
                throw new StoreServiceException(e);
            }
        }
    }
}