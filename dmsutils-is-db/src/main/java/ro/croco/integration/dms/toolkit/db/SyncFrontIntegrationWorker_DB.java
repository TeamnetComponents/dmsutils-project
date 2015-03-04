package ro.croco.integration.dms.toolkit.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.exceptions.TimeoutException;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by battamir.sugarjav on 2/26/2015.
 */

public class SyncFrontIntegrationWorker_DB {

    private Properties context;
    private StoreServiceMessageDb dbMessage;

    public SyncFrontIntegrationWorker_DB(Properties context){
        this.context = context;
    }

    public void send(StoreServiceMessageDb dbMessage,Connection connection,String requestDbSchema){
        try{
            connection.setAutoCommit(false);
            PreparedStatement statement = StatementPreparator.FrontSide.prepareInsertRequest(connection,requestDbSchema,dbMessage.getMessageDestination(),translateMsgToInsertionMap(dbMessage));
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            generatedKeys.close();
            statement.close();
            connection.commit();
            this.dbMessage = dbMessage;
        }
        catch(SQLException sqlEx){
            try{
                connection.rollback();
            }
            catch(SQLException rollBackEx) {
                throw new StoreServiceException(rollBackEx);
            }
            throw new StoreServiceException(sqlEx);
        }
        finally{
            if(connection != null){
                try{
                    connection.close();
                }
                catch(SQLException connCloseEx){

                }
                connection = null;
            }
        }
    }

    public StoreServiceMessageDb receive(Connection connection,String responseDbSchema,String historyTableName,Long waitResponseTime,Long waitOnIterationTime){
        try{
            connection.setAutoCommit(false);
            Long cummulatedTime = 0L;
            StoreServiceMessageDb response = null;
            PreparedStatement statement = StatementPreparator.FrontSide.prepareSelectResponse(connection,responseDbSchema,dbMessage.getMessageReplyTo(),translateMsgToSelectionResponseMap(this.dbMessage));
            ResultSet resultSet = null;

            while(cummulatedTime <= waitResponseTime){
                Long iterationStartTime = System.currentTimeMillis();
                resultSet = statement.executeQuery();
                if(resultSet.next()){
                    System.out.println("Response found at time : " + cummulatedTime);
                    response = translateJdbcResponseToSSMsg(resultSet);
                    resultSet.deleteRow();
                    this.saveResponseIntoHistory(response,connection,responseDbSchema,historyTableName);
                    resultSet.close();
                    statement.close();
                    break;
                }
                Thread.sleep(waitOnIterationTime);
                cummulatedTime += (System.currentTimeMillis() - iterationStartTime);
                System.out.println("Time slept until current iteration : " + cummulatedTime);
            }
            if(response != null){
                connection.commit();
                return response;
            }
            throw new TimeoutException("Could not get a proper response within time interval specified.");
        }
        catch(SQLException sqlEx){
            try{
                sqlEx.printStackTrace();
                connection.rollback();
            }
            catch(SQLException rollBackEx){
                throw new StoreServiceException(rollBackEx);
            }
            throw new StoreServiceException(sqlEx);
        }
        catch (InterruptedException interruptedEx) {
            throw new StoreServiceException(interruptedEx);
        }
        finally{
            if(connection != null){
                try{
                    connection.close();
                }
                catch(SQLException connCloseEx){

                }
                connection = null;
            }
        }
    }

    private Map<String,Object> translateMsgToInsertionMap(StoreServiceMessageDb dbMessage){
        Map<String,Object> insertValues = new HashMap<String,Object>();
        insertValues.put("MSG_ID",dbMessage.getMessageID());
        insertValues.put("MSG_DESTINATION",dbMessage.getMessageDestination());
        insertValues.put("MSG_CORRELATION_ID", dbMessage.getMessageCorrelationID());
        insertValues.put("MSG_REPLY_TO",dbMessage.getMessageReplyTo());
        insertValues.put("MSG_EXPIRATION",dbMessage.getMessageExpiration());
        insertValues.put("MSG_PRIORITY",dbMessage.getMessagePriority());
        insertValues.put("MSG_CONTENT",dbMessage.getStoreServiceMessage());
        insertValues.put("PRC_STATUS",null);
        insertValues.put("PRC_ID",null);
        return insertValues;
    }

    private Map<String,Object> translateMsgToSelectionResponseMap(StoreServiceMessageDb dbMessage){
        Map<String,Object> selectByValues = new HashMap<String, Object>();
        selectByValues.put("MSG_ID",dbMessage.getMessageID());
        selectByValues.put("MSG_CORRELATION_ID",dbMessage.getMessageCorrelationID());
        return selectByValues;
    }

    private StoreServiceMessageDb translateJdbcResponseToSSMsg(ResultSet resultSet)throws SQLException{
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            StoreServiceMessageDb message = new StoreServiceMessageDb();
            message.setMessageID(resultSet.getString(1));
            message.setMessageCorrelationID(resultSet.getString(2));
            message.setMessageDestination(resultSet.getString(3));
            message.setMessageReplyTo(resultSet.getString(4));
            message.setMessageExpiration(resultSet.getLong(5));
            message.setMessagePriority(resultSet.getInt(6));
            message.setStoreServiceMessage(objectMapper.readValue(resultSet.getString(7),StoreServiceMessage.class));
            return message;
        }
        catch (IOException ioEx) {
            throw new StoreServiceException(ioEx);
        }
    }

    private void saveResponseIntoHistory(StoreServiceMessageDb dbMessage,Connection connection,String responseDbSchema,String historyTableName)throws SQLException{
        PreparedStatement statement = StatementPreparator.FrontSide.prepareInsertRequest(connection,responseDbSchema,historyTableName,translateMsgToInsertionMap(dbMessage));
        statement.execute();
    }
}