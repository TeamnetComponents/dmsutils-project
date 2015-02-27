package ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument;

import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentInfo;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.VersioningType;
import ro.croco.integration.dms.toolkit.utils.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.DBRepository;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;
import ro.croco.integration.dms.toolkit.utils.InputValidator;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */

public abstract class StoreDocumentStrategy extends DocumentOperationStrategy{

    private static final String FUNCTION_IDENTIFIER = "[STORE_DOCUMENT_CALL] ";

    private final class Validator extends InputValidator{
        @Override
        public void validateInputs() throws StoreServiceException {
            if(documentInfo == null)
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "DocumentInfo or additional info is not provided.");

            if(additInfo.get("inputStream") == null)
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "Document stream is empty.Please provide it.");

            if(documentInfo.getName() == null || documentInfo.getName().equals(""))
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "Document does not have a name associeted.");
        }
    }

    public StoreDocumentStrategy(StoreServiceSessionImpl_Db session){
            super(session);
    }

    protected DocumentInfo documentInfo;
    protected Map<String,Object> additInfo;



    protected boolean hasPathSpecified(){
        return documentInfo.getParentIdentifier() != null && !documentInfo.getParentIdentifier().getPath().isEmpty();
    }

    public abstract DocumentIdentifier delegatedProcess() throws SQLException;

    private Validator validator = new Validator();

    public DocumentIdentifier process(DocumentInfo documentInfo,Map<String,Object> additInfo) {
        try {
            this.additInfo = additInfo;
            this.documentInfo = documentInfo;
            validator.validateInputs();
            DocumentIdentifier newDocumentIdentifer = delegatedProcess();
            connection.commit();
            return newDocumentIdentifer;
        }
        catch (SQLException sqlEx) {
            try{
                connection.rollback();
                throw new StoreServiceException(sqlEx);
            }
            catch (SQLException rollBackEx) {
                throw new StoreServiceException(rollBackEx);
            }
        }
    }

    protected DocumentIdentifier constructDocumentIdentifier(BigDecimal dmObjectsId,BigDecimal dmVersionsId,String version){
        DocumentIdentifier identifier = new DocumentIdentifier();
        identifier.setId(dmObjectsId + "_" + dmVersionsId);

        if(hasPathSpecified())
            identifier.setPath(documentInfo.getParentIdentifier().getPath() + "_" + documentInfo.getName());
        else identifier.setPath(documentInfo.getName());

        identifier.setVersion(version);

        return identifier;
    }

    protected String getMIMEType(String fileNameWithExtension){
        return FileUtils.getFileUtilsDMS().getMimeType(fileNameWithExtension);
    }

    protected String calculateNewVersion(boolean calculateBasedOnPrev) throws SQLException{
        VersioningType versioningType = (VersioningType)additInfo.get("versioningType");

        if(versioningType.equals(VersioningType.NONE))
            return null;

        if(calculateBasedOnPrev){
            String schema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
            BigDecimal oldDmObjectsRowId = hasPathSpecified() ? DBRepository.getDmObjectsIdByPathAndName(connection, schema, documentInfo.getParentIdentifier().getPath(), documentInfo.getName()) :
                                                                DBRepository.getDmObjectsIdByName(connection,schema,documentInfo.getName());

            return VersioningType.getNextVersion(DBRepository.getLastVersionLabelForDmObject(connection, schema, oldDmObjectsRowId),versioningType);
        }
        return VersioningType.getNextVersion(null,versioningType);
    }

    protected boolean hasExtension(){
        return documentInfo.getExtension() != null && !documentInfo.getExtension().equals("");
    }
}
