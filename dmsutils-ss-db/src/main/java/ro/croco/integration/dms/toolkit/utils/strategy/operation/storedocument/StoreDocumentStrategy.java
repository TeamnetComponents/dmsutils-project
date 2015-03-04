package ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument;

import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.*;
import ro.croco.integration.dms.toolkit.utils.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.DBRepository;
import ro.croco.integration.dms.toolkit.utils.InputValidator;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */

public abstract class StoreDocumentStrategy extends DocumentOperationStrategy {

    private static final String FUNCTION_IDENTIFIER = "[STORE_DOCUMENT_CALL] ";

    private final class Validator extends InputValidator {
        @Override
        public void validateInputs() throws StoreServiceException {
            if (documentInfo == null)
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "DocumentInfo or additional info is not provided.");

            if (additInfo.get("inputStream") == null)
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "Document stream is empty.Please provide it.");

            if (documentInfo.getName() == null || documentInfo.getName().equals(""))
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "Document does not have a name associeted.");

            if (documentInfo.getParentIdentifier() != null && (documentInfo.getParentIdentifier().getPath() == null || documentInfo.getParentIdentifier().getPath().isEmpty()))
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "Document has not path specified in FolderIdentifier != null.");
        }
    }

    public StoreDocumentStrategy(StoreServiceSessionImpl_Db session) {
        super(session);
    }

    protected DocumentInfo documentInfo;
    protected Map<String, Object> additInfo;

//    protected boolean hasPathSpecified(){
//        return documentInfo.getParentIdentifier() != null &&  documentInfo.getParentIdentifier().getPath() != null && !documentInfo.getParentIdentifier().getPath().isEmpty();
//    }

    public abstract DocumentIdentifier delegatedProcess() throws SQLException;

    private Validator validator = new Validator();

    private void calculatePathIfNecessary() {
        if (documentInfo.getParentIdentifier() == null) {
            documentInfo.setParentIdentifier(FolderIdentifier.builder().withPath(FileUtils.getFileUtilsDMS().getRootPath()).build());
        } else if (!documentInfo.getParentIdentifier().getPath().equals(FileUtils.getFileUtilsDMS().getRootPath())) {
            String path = documentInfo.getParentIdentifier().getPath();
            int lastPathDelimiter = path.lastIndexOf(FileUtils.getFileUtilsDMS().getPathDelimiter());
            if (lastPathDelimiter != -1 && lastPathDelimiter == path.length() - 1)
                documentInfo.getParentIdentifier().setPath(path.substring(0, lastPathDelimiter));
        }
    }

    public DocumentIdentifier process(DocumentInfo documentInfo, Map<String, Object> additInfo) {
        try {
            this.additInfo = additInfo;
            this.documentInfo = documentInfo;
            validator.validateInputs();
            calculatePathIfNecessary();
            DocumentIdentifier newDocumentIdentifer = delegatedProcess();
            connection.commit();
            return newDocumentIdentifer;
        }
        catch (SQLException sqlEx){
            try {
                connection.rollback();
                throw new StoreServiceException(sqlEx);
            } catch (SQLException rollBackEx) {
                throw new StoreServiceException(rollBackEx);
            }
        }
        finally{
            if(connection != null){
                try{
                    connection.close();
                }
                catch (SQLException connCloseEx){

                }
                connection = null;
            }
        }
    }

    protected DocumentIdentifier constructDocumentIdentifier(BigDecimal dmObjectsId, BigDecimal dmVersionsId, String version) {

//        DocumentIdentifier identifier = new DocumentIdentifier();
//        identifier.setId(dmObjectsId + "_" + dmVersionsId);
//        identifier.setPath(documentInfo.getParentIdentifier().getPath() + "_" + documentInfo.getName());
//        identifier.setVersion(version);/
//        return identifier;

        return StoreServiceImpl_Db.constructDocumentIdentifier(
                dmObjectsId + "_" + dmVersionsId,
                (documentInfo.getParentIdentifier().getPath().length() == FileUtils.getFileUtilsDMS().getRootPath().length() ? "" : FileUtils.getFileUtilsDMS().getPathDelimiter()) + documentInfo.getName(),
                version,((Properties)additInfo.get("context")).getProperty(ContextProperties.Required.INSTANCE_NAME));
    }

    protected String getMIMEType(String fileNameWithExtension) {
        return FileUtils.getFileUtilsDMS().getMimeType(fileNameWithExtension);
    }

    protected String calculateNewVersion(boolean calculateBasedOnPrev) throws SQLException {
        VersioningType versioningType = (VersioningType) additInfo.get("versioningType");

        if (versioningType.equals(VersioningType.NONE))
            return null;

        if (calculateBasedOnPrev) {
            String schema = (String) session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
            BigDecimal oldDmObjectsRowId = DBRepository.getDmObjectsIdByPathAndName(connection, schema, documentInfo.getParentIdentifier().getPath(), documentInfo.getName());
            String previousVersions = DBRepository.getLastVersionLabelForDmObject(connection, schema, oldDmObjectsRowId);
            System.out.println(previousVersions);
            return VersioningType.getNextVersion(previousVersions, versioningType);
        }
        return VersioningType.getNextVersion(null, versioningType);
    }

    protected boolean hasExtension() {
        return documentInfo.getExtension() != null && !documentInfo.getExtension().equals("");
    }
}
