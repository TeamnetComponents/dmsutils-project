package integration.service.jms;

import domain.UpdateDocumentInfo;
import org.omg.CORBA.REBIND;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import ro.croco.integration.dms.toolkit.*;
import ro.croco.integration.dms.toolkit.jms.JmsMessageStructure;
import ro.croco.integration.dms.toolkit.jms.JmsMessageType;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by hanna.botar on 7/10/2014.
 */
@Service
public class JmsService {

    @Autowired
    @Qualifier("foStoreService")
    StoreService foStoreService;

    @Autowired
    @Qualifier("boStoreService")
    StoreService boStoreService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private static String ALTERNATE_REPOSITORY_PROPERTIES = "alternate.service.properties";
    private static String JCR = "ss-fo";
    private static String CMIS = "ss-bo";

    private static String foContext = System.getProperty("focontext");


    static{
        //FOR local test
        if (foContext==null || "".equals(foContext)){
            foContext = "WPSFO:9081/mediufo";
        }

    }

    private static final String UPDATE_URL = "http://"+foContext+"/integration/updatedocumentupload";


    public Message processMessageStore(Message message) {

        try {
            Object payload = message.getPayload();
            JmsMessageStructure jmsMessageStructure = (JmsMessageStructure) payload;

            Object[] parameters = jmsMessageStructure.getParameters();

            String foPath = ((StoreContext) parameters[0]).getPath();

            DocumentStream downloadedDocumentStream;
            DocumentIdentifier savedDocumentIdentifier = null;

            System.out.println("--------- Downloading from ELO FO ---------------");
            System.out.println(" ------------- with PATH ----------------- " + ((DocumentIdentifier) parameters[2]).getPath() + " ----------");

            downloadedDocumentStream = foStoreService.downloadDocument(StoreContext.builder().build(), (DocumentIdentifier) parameters[2]);
            InputStream inputStream = downloadedDocumentStream.getInputStream();

            System.out.println(" -------------- Document downloaded from ELO FO ------------- ");

            DocumentInfo documentInfo = (DocumentInfo) parameters[1];
            boolean allowCreatePath = (Boolean) parameters[3];
            VersioningType versioningType = (VersioningType) parameters[4];
            StoreContext storeContext = StoreContext.builder().build();

            Properties properties = new Properties();
            properties.putAll(documentInfo.getProperties());
            boStoreService.storeDocument()
            MetadataService.Metadata<DocumentInfo> metadata = boStoreService.getMetadataService().computeDocumentMetadata(
                    properties.getProperty("documentType"),
                    properties.getProperty("documentContext"),
                    boStoreService,
                    storeContext,
                    properties);

            System.out.println(metadata);

            documentInfo = metadata.getInfo();

            System.out.println(" --------------- Storing document in ELO BO ---------------");

            savedDocumentIdentifier = boStoreService.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);

            System.out.println(" ------ Document stored in ELO BO ------------");
            System.out.println(" ----------- with path " + savedDocumentIdentifier.getPath() + " ---------- ");

            StoreContext.COMMUNICATION_TYPE_VALUES communicationType = ((StoreContext) parameters[0]).getCommunicationType();
            String requestIdentifier = ((StoreContext) parameters[0]).getRequestIdentifier();

            System.out.println(" -------------- Communication Type: " + communicationType.name() + " ------------");


            // Call rest for update-ing eloId
            System.out.println(" --------- Calling REST : " + UPDATE_URL + " --------------");

            UpdateDocumentInfo updateDocumentInfo = new UpdateDocumentInfo();

            updateDocumentInfo.setJcrId(foPath);
            updateDocumentInfo.setEloId(savedDocumentIdentifier.getPath());

//            try {
            ResponseEntity response = restTemplate.postForEntity(UPDATE_URL, updateDocumentInfo, String.class);

            System.out.println(" --------- REST response: " + response.getStatusCode().toString() + " ---------------");
//            } catch (ResourceAccessException e) {
//                // Pun inapoi in coada
//                e.printStackTrace();
//                System.out.println(" ----------- Exception in REST call -------------");
//            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return null;

    }

    /*
    public Message processMessageStoreOLD(Message message) {

        System.out.println(" ----------------- Processing Message for Store");
        System.out.println(" ---------- foContext = " + foContext + " ----------");

        Map<String, Object> headers = message.getHeaders();
        Object payload = message.getPayload();
        JmsMessageStructure jmsMessageStructure = (JmsMessageStructure) payload;

        Object[] parameters = jmsMessageStructure.getParameters();

        String jcrPath = ((StoreContext)parameters[0]).getPath();

        String alternateRepository = jmsMessageStructure.getConfiguration().get(ALTERNATE_REPOSITORY_PROPERTIES);
        DocumentStream downloadedDocumentStream;
        DocumentIdentifier savedDocumentIdentifier = null;
        if (JCR.equals(alternateRepository)) {

            System.out.println("--------- Downloading from JCR ---------------");
            System.out.println(" ------------- with PATH ----------------- " + ((DocumentIdentifier) parameters[2]).getPath() + " ----------");

            // download from jcr and save to cmis
//            downloadedDocumentStream = jcrStoreService.downloadDocument(StoreContext.builder().build(),(DocumentIdentifier) parameters[2]);
//            workaround - download jcr document from FO
            ResponseEntity jcrDocResponse = restTemplate.postForEntity(DOWNLOAD_JCR_URL, jcrPath, byte[].class);
            byte[] docBytes = (byte[]) jcrDocResponse.getBody();

            InputStream inputStream = new ByteArrayInputStream(docBytes);

            System.out.println(" -------------- Document downloaded from JCR ------------- ");

//            String path = ((DocumentIdentifier) parameters[2]).getPath();
            String path = "/CR_test";
//            String fileName = (String) parameters[1];
            DocumentInfo documentInfo = (DocumentInfo) parameters[1];
//            InputStream inputStream = downloadedDocumentStream.getInputStream();
            boolean allowCreatePath = (Boolean) parameters[3];
            VersioningType versioningType = (VersioningType) parameters[4];
            StoreContext storeContext = StoreContext.builder().allowedFolder(path).build();

            System.out.println(" --------------- Storing document in ELO ---------------");
            System.out.println(" ------- with path -------" + path + " ----------");

            savedDocumentIdentifier = boStoreService.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);

            System.out.println(" ------ Document stored in ELO ------------");
            System.out.println(" ----------- with path " + savedDocumentIdentifier.getPath() + " ---------- ");

        } else if (CMIS.equals(alternateRepository)) {
            // download from cmis and save to jcr
            downloadedDocumentStream = boStoreService.downloadDocument(StoreContext.builder().build(),(DocumentIdentifier) parameters[2]);

//            String path = ((DocumentIdentifier) parameters[2]).getPath();
            String path = "/CR_test";
//            String fileName = (String) parameters[1];
            DocumentInfo documentInfo = (DocumentInfo) parameters[1];
            InputStream inputStream = downloadedDocumentStream.getInputStream();
            boolean allowCreatePath = (Boolean) parameters[3];
            VersioningType versioningType = (VersioningType) parameters[4];
            StoreContext storeContext = StoreContext.builder().allowedFolder(path).build();
            savedDocumentIdentifier = foStoreService.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);
        }


        StoreContext.COMMUNICATION_TYPE_VALUES communicationType = ((StoreContext)parameters[0]).getCommunicationType();
        String requestIdentifier = ((StoreContext)parameters[0]).getRequestIdentifier();

        System.out.println(" -------------- Communication Type: " + communicationType.name() + " ------------" );

        if (communicationType.equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
            System.out.println(" ----------- SYNC -------------- ");

            Destination replyToQueue = (Destination) headers.get("jms_replyTo");
            final String uuid = (String) headers.get("jms_correlationId");

            System.out.println(" -------- Sending reply to queue: " + replyToQueue.toString() + " ---------------");
            System.out.println(" ---------- JMS Correlation Id: " + uuid + " ---------------");

            JmsMessageStructure messageStructure = sendReply(jmsMessageStructure, replyToQueue, uuid, savedDocumentIdentifier);

            System.out.println("StoreDocument - Reply message sent to " + replyToQueue.toString());
            return new GenericMessage(messageStructure);
        } else {
            // Call rest for update-ing eloId
            System.out.println(" --------- Calling REST : " + UPDATE_URL + " --------------");

            UpdateDocumentInfo updateDocumentInfo = new UpdateDocumentInfo();

            updateDocumentInfo.setJcrId(jcrPath);
            updateDocumentInfo.setEloId(savedDocumentIdentifier.getPath());
            try {
                ResponseEntity response = restTemplate.postForEntity(UPDATE_URL, updateDocumentInfo, String.class);

                System.out.println(" --------- REST response: " + response.getStatusCode().toString() + " ---------------");
            } catch (ResourceAccessException e) {
                // Pun inapoi in coada
                e.printStackTrace();
                System.out.println(" ----------- Exception in REST call -------------");
            }

            return null;
        }

    }
    */

    public Message processMessageExists(Message message) {
        Map<String, Object> headers = message.getHeaders();
        Object payload = message.getPayload();
        JmsMessageStructure jmsMessageStructure = (JmsMessageStructure) payload;

        Object[] parameters = jmsMessageStructure.getParameters();
        DocumentIdentifier documentIdentifier = (DocumentIdentifier) parameters[0];

        String alternateRepository = jmsMessageStructure.getConfiguration().get(ALTERNATE_REPOSITORY_PROPERTIES);
        BooleanResponse existDocument = null;
        if (JCR.equals(alternateRepository)) {
            // check if document exists in cmis
            existDocument = boStoreService.existsDocument(StoreContext.builder().build(), documentIdentifier);
        } else if (CMIS.equals(alternateRepository)) {
            // check if document exists in jcr
            existDocument = foStoreService.existsDocument(StoreContext.builder().build(), documentIdentifier);
        }
        Destination replyToQueue = (Destination) headers.get("jms_replyTo");
        final String uuid = (String) headers.get("jms_correlationId");

        JmsMessageStructure messageStructure = sendReply(jmsMessageStructure, replyToQueue, uuid, existDocument.getValue());

        System.out.println("ExistsDocument - Reply message sent to " + replyToQueue.toString());
        return new GenericMessage(messageStructure);
    }

    public Message processMessageDownload(Message message){
        try {
            Map<String, Object> headers = message.getHeaders();
            Object payload = message.getPayload();
            JmsMessageStructure jmsMessageStructure = (JmsMessageStructure) payload;

            Object[] parameters = jmsMessageStructure.getParameters();
            DocumentIdentifier documentIdentifier = (DocumentIdentifier) parameters[1];
            //documentIdentifier.getPath();

            Destination replyToQueue = (Destination) headers.get("jms_replyTo");
            final String uuid = (String) headers.get("jms_correlationId");

            //"ss-fo"
            String alternateRepository = jmsMessageStructure.getConfiguration().get(ALTERNATE_REPOSITORY_PROPERTIES);

            DocumentIdentifier storedDocumentIdentifier = null;
            if (JCR.equals(alternateRepository)) {
                // download from cmis and store in jcr
                DocumentStream documentStream = boStoreService.downloadDocument(StoreContext.builder().build(), documentIdentifier);
                //            String temporaryFileName = DmsUtils.getFileName(documentIdentifier.getPath()) + "." + DmsUtils.getFileExtension(documentStream.getFileName());

                StoreContext storeContext = StoreContext.builder().allowedFolder("/temporary/download").build();
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setName(documentStream.getFileName());
                documentInfo.setExtension(documentStream.getMimeType());

                storedDocumentIdentifier = foStoreService.storeDocument(storeContext, documentInfo, documentStream.getInputStream(), true, VersioningType.MAJOR);
            } else if (CMIS.equals(alternateRepository)) {
                // download from jcr and store in cmis
                DocumentStream documentStream = foStoreService.downloadDocument(StoreContext.builder().build(), documentIdentifier);
                //            String temporaryFileName = DmsUtils.getFileName(documentIdentifier.getPath()) + "." + DmsUtils.getFileExtension(documentStream.getFileName());

                StoreContext storeContext = StoreContext.builder().allowedFolder("/temporary/download").build();
                storedDocumentIdentifier = boStoreService.storeDocument(storeContext, null, documentStream.getInputStream(), true, VersioningType.NONE);
            }

            JmsMessageStructure messageStructure = sendReply(jmsMessageStructure, replyToQueue, uuid, storedDocumentIdentifier);

            System.out.println("DownloadDocument - Reply message sent to " + replyToQueue.toString());
            return new GenericMessage(messageStructure);
        } catch (Exception e) {
            System.out.println("AK ::: eroare procesare mesaj!!!!!!!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private JmsMessageStructure sendReply(JmsMessageStructure jmsMessageStructure, Destination destination, final String jmsCorrelationId, Object... replyParameters) {
        JmsMessageStructure messageStructure = new JmsMessageStructure();
        messageStructure.setMethod(jmsMessageStructure.getMethod());
        messageStructure.setType(JmsMessageType.RESPONSE);
        messageStructure.setParameters(replyParameters);
        messageStructure.setConfiguration(jmsMessageStructure.getConfiguration());

        final JmsMessageStructure finalMessageStructure = messageStructure;

        jmsTemplate.send(destination, new MessageCreator() {
            public javax.jms.Message createMessage(Session session) throws JMSException {
                ObjectMessage message = session.createObjectMessage();
                message.setObject(finalMessageStructure);
                message.setJMSCorrelationID(jmsCorrelationId);
                return message;
            }
        });
        return messageStructure;
    }

    public Message sendToQueue(Message message) {

        return null;
    }

}
