package integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.Payload;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import ro.croco.integration.dms.toolkit.*;
import ro.croco.integration.dms.toolkit.jms.JmsMessageStructure;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by hanna.botar on 7/7/2014.
 */
@Service
public class JcrService implements DmsService {

    @Autowired
    @Qualifier("jcrStoreService")
    StoreService jcrStoreService;


    @Override
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return jcrStoreService.downloadDocument(storeContext, documentIdentifier);
    }

    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        return jcrStoreService.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);
    }

    public DocumentStream getDocument(@Payload Object payload) {
        System.out.println("JcrService.getDocument called");
//        System.out.println(((TestDocument)payload).getId());
        if (payload instanceof DocumentIdentifier) {
//            return downloadDocument((DocumentIdentifier) payload);
            return jcrStoreService.downloadDocument(StoreContext.builder().build(),(DocumentIdentifier) payload);
        }
//        return null;
        return new DocumentStream();
    }


    public Message getJcrDocument(Message message) {
        Map<String, Object> headers = message.getHeaders();
        Object payload = message.getPayload();
        JmsMessageStructure jmsMessageStructure = (JmsMessageStructure) payload;

        Object[] parameters = jmsMessageStructure.getParameters();

        DocumentStream jcrDocumentStream = jcrStoreService.downloadDocument(StoreContext.builder().build(), (DocumentIdentifier) parameters[3]);


        Message replyMessage = new GenericMessage(jcrDocumentStream,headers);
        return replyMessage;
    }
}
