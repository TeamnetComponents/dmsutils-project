package ro.croco.integration.dms.toolkit.utils.strategy.operation.existsdocument;

import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.BooleanResponse;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
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
public class CheckDocumentStrategy extends DocumentOperationStrategy{

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

    protected DocumentIdentifier identifier;

    private static final String FUNCTION_IDENTIFIER = "[CHECK_DOCUMENT_CALL] ";

    public CheckDocumentStrategy(StoreServiceSessionImpl_Db session){
        super(session);
    }

    private boolean isVersionExistsByPath() throws SQLException{
        String schema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        BigDecimal dmObjectId = null;
        String path = identifier.getPath().split("_")[0];
        String name = identifier.getPath().split("_")[1];

        int lastPathDelimiterIndex = path.lastIndexOf(FileUtils.getFileUtilsDMS().getPathDelimiter());
        if(lastPathDelimiterIndex > 0 && lastPathDelimiterIndex == path.length() - 1)
            path = path.substring(0,lastPathDelimiterIndex);

        dmObjectId = DBRepository.getDmObjectsIdByPathAndName(connection,schema,path,name);

        if(dmObjectId != null)
            return ((identifier.getVersion() != null && !identifier.getVersion().isEmpty()) ? DBRepository.checkExistsDmVersionsByFkDmObjectsAndVersion(connection,schema,dmObjectId,identifier.getVersion()) : true);

        return false;
    }

    public BooleanResponse delegatedProcess() throws SQLException{
        if(isIdentifiedById())
            return new BooleanResponse(DBRepository.checkExistsDmVersionsByIdAndFkDmObjects(connection,(String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA),new BigDecimal(identifier.getId().split("_")[1]),new BigDecimal(identifier.getId().split("_")[0])));
        else return new BooleanResponse(isVersionExistsByPath());
    }

    private boolean isIdentifiedById(){
        return identifier.getId() != null;
    }

    public BooleanResponse process(DocumentIdentifier identifier){
        try{
            this.identifier = identifier;
            validator.validateInputs();
            return delegatedProcess();
        }
        catch(SQLException sqlEx){
            throw new StoreServiceException(sqlEx);
        }
    }
}
