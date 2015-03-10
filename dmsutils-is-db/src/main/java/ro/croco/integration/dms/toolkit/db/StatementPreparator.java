package ro.croco.integration.dms.toolkit.db;

import org.apache.commons.lang.SerializationUtils;
import ro.croco.integration.dms.commons.SqlOperationTranslator;
import ro.croco.integration.dms.toolkit.StoreService;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 2/26/2015.
 */
public class StatementPreparator{

    public static class FrontSide{
        public static PreparedStatement prepareInsertRequest(Connection connection,String schema,String table,Map<String,Object> values) throws SQLException{
            String command = SqlOperationTranslator.translateCommand("i:" + table + "(MSG_ID,MSG_CORRELATION_ID,MSG_DESTINATION,MSG_REPLY_TO,MSG_EXPIRATION,MSG_PRIORITY,MSG_CONTENT,PRC_STATUS,PRC_ID)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
            System.out.println(command);
            PreparedStatement statement = connection.prepareStatement(command,Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1,values.get("MSG_ID"));
            statement.setObject(2,values.get("MSG_CORRELATION_ID"));
            statement.setObject(3,values.get("MSG_DESTINATION"));
            statement.setObject(4,values.get("MSG_REPLY_TO"));
            statement.setObject(5,values.get("MSG_EXPIRATION"));
            statement.setObject(6,values.get("MSG_PRIORITY"));

//            try{
//                ObjectMapper objectMapper = new ObjectMapper();
//                statement.setString(7,objectMapper.writeValueAsString(values.get("MSG_CONTENT")));
//            }
//            catch (JsonProcessingException jacksonEx) {
//                throw new StoreServiceException(jacksonEx);
//            }
            //statement.setBytes(7, SerializationUtils.serialize((StoreServiceMessage) values.get("MSG_CONTENT")));

            byte[] serialized = SerializationUtils.serialize((StoreServiceMessage) values.get("MSG_CONTENT"));
            InputStream stream = new ByteArrayInputStream(serialized);
            statement.setBinaryStream(7,stream);

            if(values.get("PRC_STATUS") == null)
                statement.setNull(8,Types.VARCHAR);
            else statement.setObject(8,values.get("PRC_STATUS"));

            if(values.get("PRC_ID") == null)
                statement.setNull(9,Types.VARCHAR);
            else statement.setObject(9, values.get("PRC_ID"));
            return statement;
        }

        public static PreparedStatement prepareSelectResponse(Connection connection,String schema,String table,Map<String,Object> values) throws SQLException{
            String command = SqlOperationTranslator.translateCommand("s:" + table + "(MSG_ID,MSG_CORRELATION_ID,MSG_DESTINATION,MSG_REPLY_TO,MSG_EXPIRATION,MSG_PRIORITY,MSG_CONTENT,PRC_STATUS,PRC_ID),(MSG_CORRELATION_ID,MSG_DESTINATION)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
            System.out.println(command);
            PreparedStatement statement = connection.prepareStatement(command,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            statement.setObject(1,values.get("MSG_CORRELATION_ID"));
            statement.setObject(2,values.get("MSG_DESTINATION"));
            return statement;
        }
    }

    public static class BackSide{

    }
}
