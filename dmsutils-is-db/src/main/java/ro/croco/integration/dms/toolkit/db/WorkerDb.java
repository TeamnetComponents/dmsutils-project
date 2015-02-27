//package ro.croco.integration.dms.toolkit.db;
//
//import com.sun.xml.internal.bind.v2.model.core.ID;
//import org.apache.commons.lang.StringUtils;
//import ro.croco.integration.dms.commons.DatabaseUtils;
//import ro.croco.integration.dms.commons.TemplateEngine;
//import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
//
//import java.sql.*;
//import java.util.Properties;
//import java.util.UUID;
//
//import static ro.croco.integration.dms.commons.DatabaseUtils.*;
//
///**
//* Created by Lucian.Dragomir on 1/17/2015.
//*/
//public class WorkerDb {
//    private static final TemplateEngine templateEngine = TemplateEngine.getInstance();
//    private static final String QUEUE_NAME = "queueName";
//    private static final String QUEUE_CONNECTION = "queue." + templateEngine.escapeVariable(QUEUE_NAME) + ".connection.jdbc";
//    private static final String QUEUE_TABLE = "queue." + templateEngine.escapeVariable(QUEUE_NAME) + ".table.name";
//    private static final String QUEUE_SEQUENCE = "queue." + templateEngine.escapeVariable(QUEUE_NAME) + ".table.sequence";
//
//
//    private final String SQL_SEND_WITH_SEQ =
//            "INSERT INTO " + templateEngine.escapeVariable(SCHEMA_NAME) + "." + templateEngine.escapeVariable(TABLE_NAME) +
//                    "(ID, MSG_ID, MSG_CORRELATION_ID, MSG_DESTINATION, MSG_REPLY_TO, MSG_EXPIRATION, MSG_PRIORITY, MSG_CONTENT" + '\n' +
//                    ", PRC_STATUS, PRC_ID" + '\n' +
//                    ") " + '\n' +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//    private final String SQL_SEND_WITHOUT_SEQ =
//            "INSERT INTO " + templateEngine.escapeVariable(SCHEMA_NAME) + "." + templateEngine.escapeVariable(TABLE_NAME) +
//                    "(MSG_ID, MSG_CORRELATION_ID, MSG_DESTINATION, MSG_REPLY_TO, MSG_EXPIRATION, MSG_PRIORITY, MSG_CONTENT" + '\n' +
//                    ", PRC_STATUS, PRC_ID" + '\n' +
//                    ") " + '\n' +
//                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//
//    private Properties context;
//    private Connection connection;
//
//    public WorkerDb(Properties context) {
//        this.context = context;
//    }
//
//    private String getConnectionName(String queueName) {
//        return (String) context.get(templateEngine.getValueFromTemplate(QUEUE_CONNECTION, QUEUE_NAME, queueName));
//    }
//
//    private String getTableName(String queueName) {
//        return (String) context.get(templateEngine.getValueFromTemplate(QUEUE_TABLE, QUEUE_NAME, queueName));
//    }
//
//    private String getTableSequenceName(String queueName) {
//        return (String) context.get(templateEngine.getValueFromTemplate(QUEUE_SEQUENCE, QUEUE_NAME, queueName));
//    }
//
//
//    public String sendMessage(Connection connection, StoreServiceMessageDb storeServiceMessage) throws SQLException {
//        long sequenceValue = -1;
//        String messageID = (StringUtils.isNotEmpty(storeServiceMessage.getMessageID())) ? storeServiceMessage.getMessageID() : String.valueOf(UUID.randomUUID());
//        String messageDestination = storeServiceMessage.getMessageDestination();
//        String messageCorrelationID = storeServiceMessage.getMessageCorrelationID();
//        String messageReplyTo = storeServiceMessage.getMessageReplyTo();
//        long messageExpiration = (storeServiceMessage.getMessageExpiration() < 0) ? 0 : storeServiceMessage.getMessageExpiration();
//        int messagePriority = (storeServiceMessage.getMessageExpiration() < 0) ? 4 : storeServiceMessage.getMessagePriority();
//
//
//        String connectionName = getConnectionName(messageDestination);
//        String tableSchema = getConnectionSchema(context, connectionName);
//        String tableName = getTableName(messageDestination);
//        String tableSequence = getTableSequenceName(messageDestination);
//        String sqlString;
//
//        PreparedStatement statement;
//        ResultSet resultSet = null;
//
//        //get the sequence value (if explicit sequence)
//        if (StringUtils.isNotEmpty(tableSequence)) {
//            sequenceValue = DatabaseUtils.getSequenceNextVal(connection, tableSequence);
//        }
//
//        //prepare insert statement
//        if (StringUtils.isNotEmpty(tableSequence)) {
//            sequenceValue = DatabaseUtils.getSequenceNextVal(connection, tableSequence);
//            sqlString = templateEngine.getValueFromTemplate(SQL_SEND_WITH_SEQ, SCHEMA_NAME, tableSchema, TABLE_NAME, tableName);
//            statement = connection.prepareStatement(sqlString /*, Statement.RETURN_GENERATED_KEYS*/);
//        } else {
//            sqlString = templateEngine.getValueFromTemplate(SQL_SEND_WITHOUT_SEQ, SCHEMA_NAME, tableSchema, TABLE_NAME, tableName);
//            statement = connection.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
//        }
//
//        //send parameters
//        int index = 1;
//        if (StringUtils.isNotEmpty(tableSequence)) {
//            statement.setLong(index++, sequenceValue);
//        }
//        statement.setString(index++, messageID);
//        statement.setString(index++, messageCorrelationID);
//        statement.setString(index++, messageDestination);
//        statement.setString(index++, messageReplyTo);
//        statement.setLong(index++, messageExpiration);
//        statement.setInt(index++, messagePriority);
//        statement.setString(index++, storeServiceMessage.getStoreServiceMessage().);
//
//
//        MSG_ID, MSG_CORRELATION_ID, MSG_DESTINATION, MSG_REPLY_TO, MSG_EXPIRATION, MSG_PRIORITY, MSG_CONTENT " + '\n' +
//        ", PRC_STATUS, PRC_ID
//
//
//        statement.setString(index++, externalFilePathName);
//        statement.execute();
//
//        //get the sequence value (if implicit sequence)
//        if (StringUtils.isEmpty(tableSequence)) {
//            resultSet = statement.getGeneratedKeys();
//            if (resultSet.next()) {
//                sequenceValue = resultSet.getLong(1);
//            } else {
//                throw new StoreServiceException("The generated key could not be retrieved from database.");
//            }
//        }
//
//
//        PreparedStatement statement = null;
//
//        Connection
//
//        CREATE TABLE DM_INTREGRATION_01
//                (
//                        ID BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY(START WITH 2, INCREMENT BY 1, CACHE 20, NO MINVALUE, NO MAXVALUE, NO CYCLE, NO ORDER),
//                        MSG_ID VARCHAR(50)NOT NULL,
//                        MSG_CORRELATION_ID VARCHAR(50),
//                        MSG_DESTINATION VARCHAR(50)NOT NULL,
//                        MSG_REPLY_TO VARCHAR(50),
//                        MSG_EXPIRATION BIGINT BIGINT DEFAULT 0,
//                        MSG_PRIORITY INTEGER NOT NULL CHECK(MSG_PRIORITY >= 0AND MSG_PRIORITY <= 9),
//                        MSG_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                        MSG_CONTENT VARCHAR(4000),
//                        PRC_STATUS VARCHAR(50),
//                        PRC_ID VARCHAR(50),
//                        PRC_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//                        PRIMARY KEY(ID)
//                );
//        return messageID;
//    }
//
//    public StoreServiceMessageDb getMessage(String queueName, String messageCorrelationID) {
//        StoreServiceMessageDb storeServiceMessage = null;
//
//        return storeServiceMessage;
//    }
//
//}
//
///*
//    DROP TABLE DMSUTILS_METADATA;
//    CREATE TABLE DMSUTILS_INTEGRATION_01
//    (
//
//    OBJECT_CODE VARCHAR(255) NOT NULL, -- will be the mask name of the object
//    OBJECT_TYPE VARCHAR(50) NOT NULL CHECK (OBJECT_TYPE IN ('FOLDER', 'DOCUMENT')), -- can be 'FOLDER' or 'DOCUMENT'
//    OBJECT_CONTEXT VARCHAR(255) NOT NULL, -- will be the context in wich the document is uploaded
//    REPOSITORY_NAME_DST VARCHAR(100) NOT NULL,
//    OPERATION_NAME VARCHAR(255) NOT NULL, -- can be STORE OR MOVE
//    REPOSITORY_NAME_SRC VARCHAR(100) NOT NULL,
//    OBJECT_IDENTIFIER_TEMPLATE_SRC VARCHAR(1000),
//    OBJECT_FOLDER_TEMPLATE VARCHAR(1000) NULL,
//    OBJECT_NAME_TEMPLATE VARCHAR(255) NULL,
//    OBJECT_MASK VARCHAR(255) NULL,
//    OBJECT_VERSIONING VARCHAR(50) DEFAULT 'NONE' CHECK (OBJECT_VERSIONING IN ('NONE', 'MINOR', 'MAJOR')),  --can be
//    OBJECT_PROPERTIES_CONNECTION VARCHAR(50),
//    OBJECT_PROPERTIES_SQL VARCHAR(4000),
//    OBJECT_CREATE_PATH INT NOT NULL CHECK (OBJECT_CREATE_PATH IN (0, 1)),
//    OBJECT_PRIORITY INT DEFAULT 1000,
//    PRIMARY KEY (OBJECT_CODE, OBJECT_TYPE, OBJECT_CONTEXT, REPOSITORY_NAME_DST, OPERATION_NAME, REPOSITORY_NAME_SRC)
//    );
//*/
