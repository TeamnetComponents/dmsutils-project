package ro.croco.integration.dms.toolkit.utils.strategy.operation.deletedocument;

import org.omg.CORBA.Request;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.RequestIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.InputValidator;

import java.math.BigDecimal;
import java.sql.Connection;
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

    public RequestIdentifier delegatedProcess()throws SQLException{




        return null;
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
