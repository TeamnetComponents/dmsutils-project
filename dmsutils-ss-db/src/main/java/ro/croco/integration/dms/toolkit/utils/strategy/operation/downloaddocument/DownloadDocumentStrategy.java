package ro.croco.integration.dms.toolkit.utils.strategy.operation.downloaddocument;

import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.BooleanResponse;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentStream;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.DBRepository;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;
import ro.croco.integration.dms.toolkit.utils.InputValidator;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */

public class DownloadDocumentStrategy extends DocumentOperationStrategy{

    private final class Validator extends InputValidator{
        @Override
        public void validateInputs() throws StoreServiceException {
            if(identifier.getId() == null && identifier.getPath() == null)
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "Neither of identifiers are provided.");
            InputValidator.validateIdentiferId(identifier.getId(),FUNCTION_IDENTIFIER);
            InputValidator.validateIdentifierPath(identifier.getPath(),FUNCTION_IDENTIFIER);
        }
    }

    private Validator validator = new Validator();

    protected DocumentIdentifier identifier;
    private String schema;

    private final static String FUNCTION_IDENTIFIER = "[DOWNLOAD_DOCUMENT_CALL] ";

    public DownloadDocumentStrategy(StoreServiceSessionImpl_Db session){
        super(session);
        this.schema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
    }

    private DocumentStream retrieveStreamByPath() throws SQLException{
        String path = null;
        String name = null;
        int lastPathDelimiterIndex = identifier.getPath().lastIndexOf(FileUtils.getFileUtilsDMS().getPathDelimiter());

        if(lastPathDelimiterIndex == 0){
            path = FileUtils.getFileUtilsDMS().getRootPath();
            name = identifier.getPath().substring(1);
        }
        else{
            path = identifier.getPath().substring(0,lastPathDelimiterIndex);
            name = identifier.getPath().substring(lastPathDelimiterIndex + 1);
        }

        if(lastPathDelimiterIndex > 0 && lastPathDelimiterIndex == path.length() - 1)
            path = path.substring(0,lastPathDelimiterIndex);

        BigDecimal dmObjectId = DBRepository.getDmObjectsIdByPathAndName(connection,schema,path,name);

        if(dmObjectId != null){
            Map<String,Object> data = null;
            if(identifier.getVersion() != null && !identifier.getVersion().isEmpty())
                data = DBRepository.getDmVersionsByFkDmObjectsAndVersionLabel(connection, schema,dmObjectId,identifier.getVersion());
            else data = DBRepository.getLastDmVersionsForDmObject(connection, schema, dmObjectId);

            if(data != null)
                return DBRepository.getDocumenStreamByIdProvidedOnRest(connection,(String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA),data.get("FK_DM_STREAMS"),(String)data.get("STREAM_NAME"),(String)data.get("MIME_TYPE"));
        }
        return null;
    }

    private boolean isIdentifiedById(){
        return identifier.getId() != null;
    }

    private DocumentStream retrieveStreamById()throws SQLException{
        boolean isPairCorrect = DBRepository.checkExistsDmVersionsByIdAndFkDmObjects(connection,(String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA),new BigDecimal(identifier.getId().split("_")[1]),new BigDecimal(identifier.getId().split("_")[0]));
        if(isPairCorrect)
            return DBRepository.getDocumentStreamByDmVersionsId(connection,(String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA),new BigDecimal(identifier.getId().split("_")[1]));
        return null;
    }

    public DocumentStream delegatedProcess()throws SQLException{
        if(isIdentifiedById())
            return retrieveStreamById();
        else return retrieveStreamByPath();
    }

    public DocumentStream process(DocumentIdentifier identifier){
        try{
            this.identifier = identifier;
            validator.validateInputs();
            return delegatedProcess();
        }
        catch(SQLException sqlEx){
            throw new StoreServiceException(sqlEx);
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
}
