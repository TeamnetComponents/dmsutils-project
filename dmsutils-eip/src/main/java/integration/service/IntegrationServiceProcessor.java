package integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceValidationException;
import ro.croco.integration.dms.toolkit.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 3/9/2015.
 */
@Service
public class IntegrationServiceProcessor {

    @Autowired
    @Qualifier("ss-local")
    StoreService ss_local;

    @Autowired
    @Qualifier("ss-final")
    StoreService ss_final;

    private Map<String, Object> createStoreServiceMessageHeaders(Map<String, Object> inputMessageHeaders) {
        Map<String, Object> messageHeaders = new HashMap<String, Object>();
        //set process headers attributes
        messageHeaders.put("MSG_CORRELATION_ID", inputMessageHeaders.get("MSG_ID"));
        messageHeaders.put("MSG_DESTINATION", inputMessageHeaders.get("MSG_REPLY_TO"));
        messageHeaders.put("MSG_PRIORITY", inputMessageHeaders.get("MSG_PRIORITY"));
        messageHeaders.put("MSG_EXPIRATION", inputMessageHeaders.get("MSG_EXPIRATION"));

        //TODO - adjust expiration by decreasing it with the processing time in EIP
        //TODO - what if inputMessage Headers is null or it does not contain the REPLY_TO ?

        //return messageHeaders
        return messageHeaders;
    }

    private Message packStoreServiceMessage(final StoreServiceMessage storeServiceMessage, final Map<String, Object> headers) {
        Message message = new Message() {
            @Override
            public MessageHeaders getHeaders() {
                return new MessageHeaders(headers);
            }

            @Override
            public Object getPayload() {
                return storeServiceMessage;
            }
        };
        return message;
    }

    public Message storeDocument(Message<StoreServiceMessage> message) {
        System.out.println("Storing Document");
        String methodName = "storeDocument";
        StoreServiceMessage processMessage = new StoreServiceMessage();
        Map<String, Object> processHeaders = new HashMap<String, Object>();

        DocumentIdentifier documentIdentifier = null;
        try {
            //process headers
            processHeaders = createStoreServiceMessageHeaders(message.getHeaders());

            //process message
            StoreServiceMessage inputMessage = message.getPayload();
            if (inputMessage.getMethod().equals(methodName)) {
                System.out.println(inputMessage.getParameters().getClass().getName());
                System.out.println(inputMessage.getParameters()[0]);
                StoreContext storeContext = (StoreContext)inputMessage.getParameters()[0];
                DocumentInfo documentInfo = (DocumentInfo)inputMessage.getParameters()[1];
                DocumentIdentifier temporaryDocumentIdentifier = (DocumentIdentifier) inputMessage.getParameters()[2];
                boolean allowCreatePath = (Boolean) inputMessage.getParameters()[3];
                VersioningType versioningType = (VersioningType) inputMessage.getParameters()[4];

                //get file content from the temporary service
                DocumentStream documentStream = ss_local.downloadDocument(new StoreContext().builder().build(), temporaryDocumentIdentifier);

                //put the stream into the final location using final service
                documentIdentifier = ss_final.storeDocument(storeContext, documentInfo, documentStream.getInputStream(), allowCreatePath, versioningType);
            } else {
                throw new StoreServiceValidationException("Incorrect method sent to " + methodName + " method.");
            }
        } catch (RuntimeException e) {
            processMessage.setException(e);
        }

        //set process message attributes
        processMessage.setMethod(methodName);
        processMessage.setDate(new Date());
        processMessage.setParameters(documentIdentifier);
        processMessage.setType(StoreServiceMessageType.RESPONSE);

        return packStoreServiceMessage(processMessage, processHeaders);
    }


    public Message downloadDocument(Message<StoreServiceMessage> message) {
        System.out.println("Downloading document");
        String methodName = "downloadDocument";
        StoreServiceMessage processMessage = new StoreServiceMessage();
        Map<String, Object> processHeaders = new HashMap<String, Object>();

        DocumentIdentifier temporaryDocumentIdentifier = null;
        try {
            //process headers
            processHeaders = createStoreServiceMessageHeaders(message.getHeaders());

            //process message
            StoreServiceMessage inputMessage = message.getPayload();
            if (inputMessage.getMethod().equals(methodName)) {
                StoreContext storeContext = (StoreContext) inputMessage.getParameters()[0];
                DocumentIdentifier documentIdentifier = (DocumentIdentifier) inputMessage.getParameters()[1];

                //get file content from the final service
                DocumentStream documentStream = ss_final.downloadDocument(new StoreContext().builder().build(), documentIdentifier);


                //put the stream into the temporary location using temporary service
                String temporaryPathDownload = FileUtils.replaceTemporalItems(ss_local.getPathByConfiguration(StoreService.PathConfiguration.TEMP_DOWNLOAD));
                DocumentInfo documentInfo = new DocumentInfo(temporaryPathDownload, documentStream.getFileName(), "", null);

                boolean allowCreatePath = true;
                VersioningType versioningType = VersioningType.MAJOR;

                temporaryDocumentIdentifier = ss_local.storeDocument(storeContext, documentInfo, documentStream.getInputStream(), allowCreatePath, versioningType);

            } else {
                throw new StoreServiceValidationException("Incorrect method sent to " + methodName + " method.");
            }
        } catch (RuntimeException e) {
            processMessage.setException(e);
        }

        //set process message attributes
        processMessage.setMethod(methodName);
        processMessage.setDate(new Date());
        processMessage.setParameters(temporaryDocumentIdentifier);
        processMessage.setType(StoreServiceMessageType.RESPONSE);

        return packStoreServiceMessage(processMessage, processHeaders);
    }
}
