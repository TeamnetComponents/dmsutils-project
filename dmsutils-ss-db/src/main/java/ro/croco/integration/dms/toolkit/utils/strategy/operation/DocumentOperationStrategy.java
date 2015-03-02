package ro.croco.integration.dms.toolkit.utils.strategy.operation;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public abstract class DocumentOperationStrategy {

    protected StoreServiceSessionImpl_Db session;
    protected Connection connection;

    public DocumentOperationStrategy(StoreServiceSessionImpl_Db session){
        try{
            this.session = session;
            this.connection = session.getConnection();
            this.connection.setAutoCommit(false);
        }
        catch(SQLException ex){
            throw new StoreServiceException(ex);
        }
    }
}
