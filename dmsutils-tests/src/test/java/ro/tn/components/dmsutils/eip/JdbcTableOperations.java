package ro.tn.components.dmsutils.eip;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;

/**
 * Created by battamir.sugarjav on 3/10/2015.
 */
public class JdbcTableOperations {

    private static BasicDataSource dataSource;

    @BeforeClass
    public static void setupDS(){
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://10.6.35.151;databaseName=DMSUtils");
        dataSource.setUsername("admin_portal");
        dataSource.setPassword("1234$zasX");
    }

    @Test
    public void showRequestTable(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM DM_INTEGRATION_REQUEST");
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            while(resultSet.next()){
                System.out.println("****************************************************");
                for(int i = 1;i <= metadata.getColumnCount();++i){
                    System.out.println(metadata.getColumnLabel(i) + " = " + resultSet.getObject(metadata.getColumnLabel(i)));
                }
                System.out.println("****************************************************");
            }
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void showResponseHistory(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM DM_INTEGRATION_RESPONSE_HIST");
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            while(resultSet.next()){
                System.out.println("****************************************************");
                for(int i = 1;i <= metadata.getColumnCount();++i){
                    System.out.println(metadata.getColumnLabel(i) + " = " + resultSet.getObject(metadata.getColumnLabel(i)));
                }
                System.out.println("****************************************************");
            }
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void restoreRequestRegistry(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE DM_INTEGRATION_REQUEST set MSG_STATUS='INITIAL',MSG_PROCESS_STEP=0");
            statement.execute();
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void showResponseTable(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM DM_INTEGRATION_RESPONSE");
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            while(resultSet.next()){
                System.out.println("****************************************************");
                for(int i = 1;i <= metadata.getColumnCount();++i){
                    System.out.println(metadata.getColumnLabel(i) + " = " + resultSet.getObject(metadata.getColumnLabel(i)));
                }
                System.out.println("****************************************************");
            }
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void deleteRequestTable(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM DM_INTEGRATION_REQUEST");
            statement.execute();
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void deleteResponseHistory(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM DM_INTEGRATION_RESPONSE_HIST");
            statement.execute();
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void deleteResponseTable(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM DM_INTEGRATION_RESPONSE");
            statement.execute();
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void deleteRequestHistoriyTable(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM DM_INTEGRATION_REQUEST_HIST");
            statement.execute();
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void showRequestHistTable(){
        try{
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM DM_INTEGRATION_REQUEST_HIST");
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metadata = resultSet.getMetaData();
            while(resultSet.next()){
                System.out.println("****************************************************");
                for(int i = 1;i <= metadata.getColumnCount();++i){
                    System.out.println(metadata.getColumnLabel(i) + " = " + resultSet.getObject(metadata.getColumnLabel(i)));
                }
                System.out.println("****************************************************");
            }
            connection.close();
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }
}
