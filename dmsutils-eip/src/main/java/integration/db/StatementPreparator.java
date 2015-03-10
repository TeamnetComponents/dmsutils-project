package integration.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by battamir.sugarjav on 3/10/2015.
 */
final public class StatementPreparator {

    public static PreparedStatement prepareUpdateRequest(Connection connection,String requestTable,String status,String msgId)throws SQLException {
        String command = "UPDATE " + requestTable + " set MSG_STATUS=? where MSG_ID=?";
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1,status);
        statement.setObject(2,msgId);
        return statement;
    }

    public static PreparedStatement prepareInsertIntoRequestHistory(Connection connection,String historyTable,String requestTable,String msgId)throws SQLException{
        String command = "INSERT INTO " + historyTable + "(MSG_ID,MSG_DESTINATION,MSG_REPLY_TO,MSG_EXPIRATION,MSG_PRIORITY,MSG_CONTENT,PRC_STATUS,PRC_ID,MSG_STATUS,MSG_PROCESS_STEP) select MSG_ID,MSG_DESTINATION,MSG_REPLY_TO,MSG_EXPIRATION,MSG_PRIORITY,MSG_CONTENT,PRC_STATUS,PRC_ID,MSG_STATUS,MSG_PROCESS_STEP from " + requestTable + " where MSG_ID=?";
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1,msgId);
        return statement;
    }
}
