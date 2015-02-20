package ro.croco.integration.dms.toolkit.utils.strategy.operation.existsdocument;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.BooleanResponse;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.context.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.SqlOperationTranslator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public class UnversionedCheckDocument extends CheckDocumentStrategy {

    public UnversionedCheckDocument(StoreServiceSessionImpl_Db session){
        super(session);
    }

    private PreparedStatement prepareDMObjectCheckExistsCmd() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String checkExistsCmd = SqlOperationTranslator.translateCommand("s:DM_OBJECTS(ID),(ID)", SqlOperationTranslator.PREPARED_STATEMENT, dbSchema);
        System.out.println("DmObjectCheckExistsCmd = " + checkExistsCmd);
        return connection.prepareStatement(checkExistsCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public BooleanResponse process(DocumentIdentifier identifier){
        try {
            PreparedStatement dmObjectCheckExistsPS = prepareDMObjectCheckExistsCmd();
            dmObjectCheckExistsPS.setInt(1, new BigDecimal(identifier.getId()).intValue());
            ResultSet resultSet = dmObjectCheckExistsPS.executeQuery();
            BooleanResponse response = new BooleanResponse(!resultSet.last());
            resultSet.close();
            dmObjectCheckExistsPS.close();
            return response;
        }
        catch(Exception ex){
            throw new StoreServiceException(ex);
        }
    }
}