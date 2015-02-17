package ro.croco.integration.dms.toolkit.db;

import org.apache.commons.dbcp.BasicDataSource;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceNotDefinedException;
import ro.croco.integration.dms.toolkit.StoreContext;
import ro.croco.integration.dms.toolkit.StoreServiceSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 2/17/2015.
 */
public class StoreServiceSessionImpl_Db implements StoreServiceSession {
    private static final String CONNECTION_TYPE = "jdbc.type";
    private static final String CONNECTION_URL = "jdbc.url";
    private static final String CONNECTION_DRIVER = "jdbc.driver";
    private static final String CONNECTION_USER = "jdbc.user";
    private static final String CONNECTION_PASSWORD = "jdbc.password";
    private static final String CONNECTION_TYPE_LOCAL = "local";
    private static final String CONNECTION_TYPE_JNDI = "jndi";

    private Properties context;
    Connection connection;

    public StoreServiceSessionImpl_Db() {
    }

    public StoreServiceSessionImpl_Db(StoreContext storeContext, Properties context) {
        this.context = context;
    }

    private Connection createConnection() {
        DataSource dataSource = null;
        Connection connection = null;
        String connectionType = (String) this.context.get(CONNECTION_TYPE);
        if (CONNECTION_TYPE_LOCAL.equalsIgnoreCase(connectionType)) {
            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName((String) this.context.get(CONNECTION_DRIVER));
            basicDataSource.setUrl((String) this.context.get(CONNECTION_URL));
            basicDataSource.setUsername((String) this.context.get(CONNECTION_USER));
            basicDataSource.setPassword((String) this.context.get(CONNECTION_PASSWORD));
            dataSource = basicDataSource;
        } else if (CONNECTION_TYPE_JNDI.equalsIgnoreCase(connectionType)) {
            try {
                Context initContext = new InitialContext();
                dataSource = (DataSource) initContext.lookup((String) this.context.get(CONNECTION_URL));
            } catch (NamingException e) {
                e.printStackTrace();
                throw new StoreServiceException(e);
            }
        } else {
            throw new StoreServiceNotDefinedException("The connection is not defined.");
        }

        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StoreServiceException(e);
        } finally {
            dataSource = null;
        }
        return connection;
    }

    @Override
    public void close() {
        if (this.connection != null) {
            try {
                if (!this.connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                // do nothing
            }
        }
        this.connection = null;
    }


    public Connection getConnection() {
        close();
        this.connection = createConnection();
        return this.connection;
    }
}
