package integration.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 3/10/2015.
 */
final public class DBRepository {

    public static void updateRequest(Connection connection,String requestTable,String status,String msgId)throws SQLException{
        PreparedStatement statement = StatementPreparator.prepareUpdateRequest(connection,requestTable,status,msgId);
        statement.execute();
        statement.close();
    }

    public static void saveToHistory(Connection connection,String historyTable,String requestTable,String msgId)throws SQLException{
        PreparedStatement statement = StatementPreparator.prepareInsertIntoRequestHistory(connection,historyTable,requestTable,msgId);
        statement.execute();
        statement.close();
    }
}
