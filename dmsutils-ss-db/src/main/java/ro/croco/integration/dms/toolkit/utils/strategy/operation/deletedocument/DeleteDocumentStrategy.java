package ro.croco.integration.dms.toolkit.utils.strategy.operation.deletedocument;

import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.RequestIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.DBRepository;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;
import ro.croco.integration.dms.toolkit.utils.InputValidator;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */

public class DeleteDocumentStrategy extends DocumentOperationStrategy{

    private static final String FUNCTION_IDENTIFIER = "[DELETE_DOCUMENT_CALL] ";

    public DeleteDocumentStrategy(StoreServiceSessionImpl_Db session){
        super(session);
    }

    protected DocumentIdentifier identifier;

    private boolean isIdentifiedById(){
        return identifier.getId() != null;
    }

    private final class Validator extends InputValidator{
        @Override
        public void validateInputs() throws StoreServiceException{
            if(identifier.getId() == null && identifier.getPath() == null)
                throw new StoreServiceException(FUNCTION_IDENTIFIER + "Neither of identifiers are provided.");
            InputValidator.validateIdentiferId(identifier.getId(),FUNCTION_IDENTIFIER);
            InputValidator.validateIdentifierPath(identifier.getPath(),FUNCTION_IDENTIFIER);
        }
    }

    private Validator validator = new Validator();

    private RequestIdentifier deleteDocumentById()throws SQLException{
        BigDecimal dmObjectsId = new BigDecimal(identifier.getId().split("_")[0]);
        BigDecimal dmVersionsId = new BigDecimal(identifier.getId().split("_")[1]);
        String schema = (String) session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        DBRepository.deleteDmVersionsAndDmStreamsById(connection, schema,dmVersionsId);
        try{
            DBRepository.deleteDmObjectsById(connection,schema,dmObjectsId);
        }
        catch(SQLException deleteFkFirstEx){}

        return new RequestIdentifier();
    }

    private RequestIdentifier deleteDocumentByPath()throws SQLException{
        String schema = (String) session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String path = identifier.getPath().split("_")[0];
        String name = identifier.getPath().split("_")[1];

        int lastPathDelimiterIndex = path.lastIndexOf(FileUtils.getFileUtilsDMS().getPathDelimiter());
        if(lastPathDelimiterIndex > 0 && lastPathDelimiterIndex == path.length() - 1)
            path = path.substring(0,lastPathDelimiterIndex);

        BigDecimal dmObjectId = DBRepository.getDmObjectsIdByPathAndName(connection,schema,path,name);

        if(dmObjectId != null){
            if(identifier.getVersion() != null && !identifier.getVersion().isEmpty())
                DBRepository.deleteDmVersionsAndDmStreamsByFkDmOBjectsAndVersionLabel(connection,schema,dmObjectId,identifier.getVersion());
            else DBRepository.deleteLastVersionAndStreamForDmObject(connection,schema,dmObjectId);
            try{
                DBRepository.deleteDmObjectsById(connection,schema,dmObjectId);
            }
            catch(SQLException deleteFkFirstEx){}
        }
        return new RequestIdentifier();
    }

    public RequestIdentifier delegatedProcess()throws SQLException{
        if(isIdentifiedById())
            return deleteDocumentById();
        else return deleteDocumentByPath();
    }


    public RequestIdentifier process(DocumentIdentifier identifier){
        try{
            this.identifier = identifier;
            validator.validateInputs();
            RequestIdentifier requestIdentifier = delegatedProcess();
            connection.commit();
            return requestIdentifier;
        }
        catch(SQLException sqlEx){
            try{
                connection.rollback();
                throw new StoreServiceException(sqlEx);
            }
            catch(SQLException rollbackEx){
                throw new StoreServiceException(rollbackEx);
            }
        }
    }
}
