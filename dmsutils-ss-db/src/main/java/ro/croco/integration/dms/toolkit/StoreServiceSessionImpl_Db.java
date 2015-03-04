package ro.croco.integration.dms.toolkit;

import org.apache.commons.dbcp.BasicDataSource;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceNotDefinedException;
import ro.croco.integration.dms.toolkit.utils.ContextProperties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.List;

/**
 * Created by Lucian.Dragomir on 2/17/2015.
 */

public class StoreServiceSessionImpl_Db implements StoreServiceSession{
    public static final String CONNECTION_TYPE_LOCAL = "local";
    public static final String CONNECTION_TYPE_JNDI = "jndi";

    private Properties context;
    private DataSource dataSource;
    private List<Connection> connectionList = new LinkedList<Connection>();

    public Properties getContext() {
        return context;
    }

    public StoreServiceSessionImpl_Db(Properties context){
        this.context = context;
        configureDataSource();
    }

    private void configureDataSource(){
        String connectionType = (String)this.context.get(ContextProperties.Required.CONNECTION_TYPE);
        System.out.println("\nConfiguring dataSource using : ");
        if (CONNECTION_TYPE_LOCAL.equalsIgnoreCase(connectionType)){
            System.out.println("\tLocal dataSource.");
            System.out.println("\t" + this.context.get(ContextProperties.Required.CONNECTION_DRIVER));
            System.out.println("\t" + this.context.get(ContextProperties.Required.CONNECTION_URL));
            System.out.println("\t" + this.context.get(ContextProperties.Required.CONNECTION_USER));
            System.out.println("\t" + this.context.get(ContextProperties.Required.CONNECTION_PASSWORD));

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName((String) this.context.get(ContextProperties.Required.CONNECTION_DRIVER));
            basicDataSource.setUrl((String) this.context.get(ContextProperties.Required.CONNECTION_URL));
            basicDataSource.setUsername((String) this.context.get(ContextProperties.Required.CONNECTION_USER));
            basicDataSource.setPassword((String) this.context.get(ContextProperties.Required.CONNECTION_PASSWORD));
            dataSource = basicDataSource;
        }
        else if(CONNECTION_TYPE_JNDI.equalsIgnoreCase(connectionType)){
            try {
                System.out.println("\tJndi dataSource.");
                System.out.println("\t" + this.context.get(ContextProperties.Required.CONNECTION_URL));
                Context initContext = new InitialContext();
                dataSource = (DataSource) initContext.lookup((String)this.context.get(ContextProperties.Required.CONNECTION_URL));
            }
            catch (NamingException e) {
                e.printStackTrace();
                throw new StoreServiceException(e);
            }
        }
        else throw new StoreServiceNotDefinedException("The connection is not defined.");
    }

    public Connection getConnection(){
        try{
            connectionList.add(dataSource.getConnection());
            return connectionList.get(connectionList.size() - 1);
        }
        catch(SQLException e){
            this.dataSource = null;
            throw new StoreServiceException(e);
        }
    }

    @Override
    public void close(){
        while(connectionList.size() > 0){
            Connection conn = connectionList.get(0);
            if(conn != null){
                try{
                    conn.close();
                }
                catch(SQLException e){

                }
            }
            connectionList.remove(0);
        }
        connectionList = null;
        dataSource = null;
    }
}
