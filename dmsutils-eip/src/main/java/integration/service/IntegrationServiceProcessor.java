package integration.service;

import integration.db.DBRepository;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceValidationException;
import ro.croco.integration.dms.toolkit.*;
import ro.croco.integration.dms.toolkit.db.*;
import ro.croco.integration.dms.toolkit.db.ContextProperties;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Lucian.Dragomir on 3/9/2015.
 */
@Service
public class IntegrationServiceProcessor{

    private static final String STORE_DOCUMENT_METHOD = "storeDocument";
    private static final String DOWNLOAD_DOCUMENT_METHOD = "downloadDocument";

    @Autowired
    @Qualifier("ss-local")
    private StoreService ss_local;

    @Autowired
    @Qualifier("ss-final")
    private StoreService ss_final;

    @Autowired
    @Qualifier("syncRequestDataSource")
    private DataSource syncRequestDataSource;

    @Autowired
    @Qualifier("asyncRequestDataSource")
    private DataSource asyncRequestDataSource;

    @Autowired
    @Qualifier("ss-local-context")
    private Properties ss_local_context;

    private void updateRequestWithHistorySave(StoreContext storeContext, String status, String msgId,boolean saveToHistory){
        Connection connection = null;
        try{
            String requestTable = null;
            String historyTable = null;
            if(storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                connection = syncRequestDataSource.getConnection();
                requestTable = QueueConfigurationResolver.getTableName(ss_local_context,ss_local_context.getProperty(ContextProperties.Required.SERVICE_SYNC_REQUEST_QUEUE));
                historyTable = QueueConfigurationResolver.getHistoryTable(ss_local_context,ss_local_context.getProperty(ContextProperties.Required.SERVICE_SYNC_REQUEST_QUEUE));
            }
            else{
                connection = asyncRequestDataSource.getConnection();
                requestTable = QueueConfigurationResolver.getTableName(ss_local_context,ss_local_context.getProperty(ContextProperties.Required.SERVICE_ASYNC_REQUEST_QUEUE));
                historyTable = QueueConfigurationResolver.getHistoryTable(ss_local_context,ss_local_context.getProperty(ContextProperties.Required.SERVICE_ASYNC_REQUEST_QUEUE));
            }
            DBRepository.updateRequest(connection,requestTable,status,msgId);
            System.out.println(requestTable);
            System.out.println(historyTable);
            if(saveToHistory)
                DBRepository.saveToHistory(connection,historyTable,requestTable,msgId);
        }
        catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        finally{
            if(connection != null){
                try{
                    connection.close();
                }
                catch(SQLException e){

                }
            }
        }
    }

    private Map<String, Object> createStoreServiceMessageHeaders(Map<String, Object> inputMessageHeaders) {
        Map<String,Object> messageHeaders = new HashMap<String, Object>();
        //set process headers attributes
        messageHeaders.put("MSG_ID", UUID.randomUUID().toString());
        messageHeaders.put("MSG_CORRELATION_ID",inputMessageHeaders.get("MSG_ID"));
        messageHeaders.put("MSG_DESTINATION", inputMessageHeaders.get("MSG_REPLY_TO"));
        messageHeaders.put("MSG_PRIORITY", inputMessageHeaders.get("MSG_PRIORITY"));
        messageHeaders.put("MSG_EXPIRATION", inputMessageHeaders.get("MSG_EXPIRATION"));
        //TODO - adjust expiration by decreasing it with the processing time in EIP
        //TODO - what if inputMessage Headers is null or it does not contain the REPLY_TO ?
        return messageHeaders;
    }

    private Message<ByteArrayInputStream> packResponseStoreServiceMessage(final StoreServiceMessage storeServiceMessage, final Map<String, Object> headers) {
        Message message = new Message() {
            @Override
            public MessageHeaders getHeaders() {
                return new MessageHeaders(headers);
            }

            @Override
            public ByteArrayInputStream getPayload(){
                byte[] serialized = SerializationUtils.serialize(storeServiceMessage);
                return new ByteArrayInputStream(serialized);
            }
        };
        return message;
    }

    public Message storeDocument(Message<StoreServiceMessage> message){
        StoreServiceMessage inputMessage = message.getPayload();
        if (inputMessage.getMethod().equals(STORE_DOCUMENT_METHOD)){
            System.out.println("Storing Document");

            StoreServiceMessage processMessage = new StoreServiceMessage();
            DocumentIdentifier documentIdentifier = null;
            Map<String, Object> processHeaders = null;
            StoreContext storeContext = null;
            try{
                //process headers
                processHeaders = createStoreServiceMessageHeaders(message.getHeaders());
                //process message
                storeContext = (StoreContext)inputMessage.getParameters()[0];
                DocumentInfo documentInfo = (DocumentInfo)inputMessage.getParameters()[1];
                DocumentIdentifier temporaryDocumentIdentifier = (DocumentIdentifier) inputMessage.getParameters()[2];
                boolean allowCreatePath = (Boolean) inputMessage.getParameters()[3];
                VersioningType versioningType = (VersioningType)inputMessage.getParameters()[4];

                //get file content from the temporary service
                DocumentStream documentStream = ss_local.downloadDocument(new StoreContext().builder().build(),temporaryDocumentIdentifier);
                //put the stream into the final location using final service
                documentIdentifier = ss_final.storeDocument(storeContext, documentInfo, documentStream.getInputStream(), allowCreatePath, versioningType);
                updateRequestWithHistorySave(storeContext,"SUCCESS",message.getHeaders().get("MSG_ID").toString(),true);
            }
            catch(RuntimeException e){
                processMessage.setException(e);
                try{
                    updateRequestWithHistorySave(storeContext,"ERROR", message.getHeaders().get("MSG_ID").toString(),false);
                }
                catch(RuntimeException ex){

                }
            }
            //set process message attributes
            processMessage.setDate(new Date());
            processMessage.setMethod(STORE_DOCUMENT_METHOD);
            processMessage.setParameters(documentIdentifier);
            processMessage.setType(StoreServiceMessageType.RESPONSE);
            return packResponseStoreServiceMessage(processMessage,processHeaders);
        }
        else throw new StoreServiceValidationException("Incorrect method sent to " + STORE_DOCUMENT_METHOD + " method.");
    }

    public Message downloadDocument(Message<StoreServiceMessage> message) {
        StoreServiceMessage inputMessage = message.getPayload();
        if(inputMessage.getMethod().equals(DOWNLOAD_DOCUMENT_METHOD)){
            System.out.println("Downloading document");
            StoreServiceMessage processMessage = new StoreServiceMessage();
            DocumentIdentifier temporaryDocumentIdentifier = null;
            Map<String, Object> processHeaders = null;
            StoreContext storeContext = null;
            try {
                //process headers
                processHeaders = createStoreServiceMessageHeaders(message.getHeaders());
                //process message
                storeContext = (StoreContext)inputMessage.getParameters()[0];
                DocumentIdentifier documentIdentifier = (DocumentIdentifier)inputMessage.getParameters()[1];

                //get documentStream content from the final service
                DocumentStream documentStream = ss_final.downloadDocument(new StoreContext().builder().build(), documentIdentifier);
                //put the stream into the temporary location using temporary service
                String temporaryPathDownload = FileUtils.replaceTemporalItems(ss_local.getPathByConfiguration(StoreService.PathConfiguration.TEMP_DOWNLOAD));
                DocumentInfo documentInfo = new DocumentInfo(temporaryPathDownload, documentStream.getFileName(), "", null);
                boolean allowCreatePath = true;
                VersioningType versioningType = VersioningType.MAJOR;

                temporaryDocumentIdentifier = ss_local.storeDocument(storeContext, documentInfo, documentStream.getInputStream(),allowCreatePath,versioningType);
                updateRequestWithHistorySave(storeContext,"SUCCESS",message.getHeaders().get("MSG_ID").toString(),true);
            }
            catch(RuntimeException e){
                processMessage.setException(e);
                try{
                    updateRequestWithHistorySave(storeContext,"ERROR", message.getHeaders().get("MSG_ID").toString(),false);
                }
                catch(RuntimeException ex){

                }
            }
            //set process message attributes
            processMessage.setMethod(DOWNLOAD_DOCUMENT_METHOD);
            processMessage.setDate(new Date());
            processMessage.setParameters(temporaryDocumentIdentifier);
            processMessage.setType(StoreServiceMessageType.RESPONSE);

            return packResponseStoreServiceMessage(processMessage, processHeaders);
        }
        else throw new StoreServiceValidationException("Incorrect method sent to " + DOWNLOAD_DOCUMENT_METHOD + " method.");
    }
}
