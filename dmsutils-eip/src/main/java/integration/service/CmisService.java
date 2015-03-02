package integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.annotation.Payload;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import ro.croco.integration.dms.toolkit.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanna.botar on 7/7/2014.
 */
@Service
public class CmisService implements DmsService {

    @Autowired
    @Qualifier("cmisStoreService")
    StoreService cmisStoreService;


    @Override
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return cmisStoreService.downloadDocument(storeContext,documentIdentifier);
    }

    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        return cmisStoreService.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);
    }

    public DocumentIdentifier saveDocument(@Payload Object payload) {
        System.out.println("CmisService.saveDocument called");

        if (payload instanceof DocumentStream) {
            DocumentStream ds = (DocumentStream) payload;
            Map<String, Object> docProperties = new HashMap<String, Object>();
            StoreContext storeContext = StoreContext.builder().allowedFolder("/TestFolder/TestProject").build();
            String fileName = ds.getFileName();
            return cmisStoreService.storeDocument( storeContext, new DocumentInfo(docProperties), ds.getInputStream(), true, VersioningType.MAJOR);
        }
        return null;
    }

    public Message saveCmisDocument(Message message) {
        Map<String, Object> headers = message.getHeaders();
        Object payload = message.getPayload();

        DocumentIdentifier cmisDocumentIdentifier = null;

        Message replyMessage = new GenericMessage(cmisDocumentIdentifier,headers);
        return replyMessage;
    }


   /* private static String configFile = "cmis";

    public StoreService getService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        StoreServiceFactory ssf = new StoreServiceFactory(configFile);
        return ssf.getService();
    }

    @Override
    public DocumentStream downloadDocument(DocumentIdentifier documentIdentifier) {
        DocumentStream documentStream = null;
        try {
            StoreService storeService = getService();
            documentStream = storeService.downloadDocument(documentIdentifier);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return documentStream;
    }

    @Override
    public DocumentIdentifier storeDocument(String documentPath, String documentName, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        DocumentIdentifier documentIdentifier = null;
        try {
            StoreService storeService = getService();
            documentIdentifier = storeService.storeDocument(documentPath, documentName, documentInfo, inputStream, allowCreatePath, versioningType);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return documentIdentifier;
    }*/

}
