package ro.croco.integration.dms.toolkit;

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
    private static final String CONNECTION_SCHEMA = "jdbc.schema";
    private static final String CONNECTION_TYPE_LOCAL = "local";
    private static final String CONNECTION_TYPE_JNDI = "jndi";

    private Properties context;
    private Connection connection;
    private DataSource dataSource;
    private StoreContext storeContext;

    public StoreServiceSessionImpl_Db(Properties context,StoreContext storeContext){
        this.context = context;
        this.storeContext = storeContext;
    }

    public StoreServiceSessionImpl_Db(Properties context,StoreContext storeContext,DataSource dataSource){
        this(context,storeContext);
        this.dataSource = dataSource;
    }

    private void configureDataSource(){
        if(dataSource == null){
            String connectionType = (String)this.context.get(CONNECTION_TYPE);
            System.out.println("\nConfiguring dataSource using : ");

            if (CONNECTION_TYPE_LOCAL.equalsIgnoreCase(connectionType)){
                System.out.println("\tLocal dataSource.");
                System.out.println("\t" + this.context.get(CONNECTION_DRIVER));
                System.out.println("\t" + this.context.get(CONNECTION_URL));
                System.out.println("\t" + this.context.get(CONNECTION_USER));
                System.out.println("\t" + this.context.get(CONNECTION_PASSWORD));

                BasicDataSource basicDataSource = new BasicDataSource();
                basicDataSource.setDriverClassName((String) this.context.get(CONNECTION_DRIVER));
                basicDataSource.setUrl((String) this.context.get(CONNECTION_URL));
                basicDataSource.setUsername((String) this.context.get(CONNECTION_USER));
                basicDataSource.setPassword((String) this.context.get(CONNECTION_PASSWORD));
                dataSource = basicDataSource;
            }
            else if(CONNECTION_TYPE_JNDI.equalsIgnoreCase(connectionType)){
                try {
                    System.out.println("\tJndi dataSource.");
                    System.out.println("\t" + this.context.get(CONNECTION_URL));
                    Context initContext = new InitialContext();
                    dataSource = (DataSource) initContext.lookup((String)this.context.get(CONNECTION_URL));
                }
                catch (NamingException e) {
                    e.printStackTrace();
                    throw new StoreServiceException(e);
                }
            }
            else throw new StoreServiceNotDefinedException("The connection is not defined.");
        }
    }

    public Connection getConnection(){
        try {
            configureDataSource();
            this.connection = dataSource.getConnection();
            return this.connection;
        }
        catch(SQLException e) {
            this.dataSource = null;
            e.printStackTrace();
            throw new StoreServiceException(e);
        }

    }

    @Override
    public void close() {
        if(this.connection != null){
            try {
                if (!this.connection.isClosed()) {
                    connection.close();
                }
            }
            catch (SQLException e) {

            }
            this.connection = null;
        }
    }
}
