package integration.service;

import domain.UpdateDocumentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
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

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
@Service
public class JdbcService {
    @Autowired
    @Qualifier("jcrStoreService")
    StoreService jcrStoreService;

    @Autowired
    @Qualifier("cmisStoreService")
    StoreService cmisStoreService;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RestTemplate restTemplate;

    private static String ALTERNATE_REPOSITORY_PROPERTIES = "alternate.service.properties";
    private static String JCR = "jcr";
    private static String CMIS = "cmis";

    private static String foContext = System.getProperty("focontext");

    //TODO
    // PREPROD
//    private static final String DOWNLOAD_JCR_URL = "http://WPSFO:9081/fo-cerererambursare/jcrdoc/download";
//    private static final String UPDATE_URL = "http://WPSFO:9081/fo-cerererambursare/integration/updatedocumentupload";
    private static final String DOWNLOAD_JCR_URL = "http://WPSFO:9081/"+foContext+"/jcrdoc/download";
    private static final String UPDATE_URL = "http://WPSFO:9081/"+foContext+"/integration/updatedocumentupload";
    // TST
//    private static final String DOWNLOAD_JCR_URL = "http://WPSFOTST:9080/fo-cerereplata/jcrdoc/download";
//    private static final String UPDATE_URL = "http://WPSFOTST:9080/fo-cerereplata/integration/updatedocumentupload";

    // PROD
//    private static final String DOWNLOAD_JCR_URL = "http://amappfo01:9080/mediufo/jcrdoc/download";
//    private static final String UPDATE_URL = "http://amappfo01:9080/mediufo/integration/updatedocumentupload";
//    private static final String DOWNLOAD_JCR_URL = "http://10.31.254.31/mediufo/jcrdoc/download";
//    private static final String UPDATE_URL = "http://10.31.254.31/mediufo/integration/updatedocumentupload";

    public Message processMessageStore(Message message) {

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

            savedDocumentIdentifier = cmisStoreService.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);

            System.out.println(" ------ Document stored in ELO ------------");
            System.out.println(" ----------- with path " + savedDocumentIdentifier.getPath() + " ---------- ");

        } else if (CMIS.equals(alternateRepository)) {
            // download from cmis and save to jcr
            downloadedDocumentStream = cmisStoreService.downloadDocument(StoreContext.builder().build(),(DocumentIdentifier) parameters[2]);

//            String path = ((DocumentIdentifier) parameters[2]).getPath();
            String path = "/CR_test";
//            String fileName = (String) parameters[1];
            DocumentInfo documentInfo = (DocumentInfo) parameters[1];
            InputStream inputStream = downloadedDocumentStream.getInputStream();
            boolean allowCreatePath = (Boolean) parameters[3];
            VersioningType versioningType = (VersioningType) parameters[4];
            StoreContext storeContext = StoreContext.builder().allowedFolder(path).build();
            savedDocumentIdentifier = jcrStoreService.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);
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
            existDocument = cmisStoreService.existsDocument(StoreContext.builder().build(), documentIdentifier);
        } else if (CMIS.equals(alternateRepository)) {
            // check if document exists in jcr
            existDocument = jcrStoreService.existsDocument(StoreContext.builder().build(), documentIdentifier);
        }
        Destination replyToQueue = (Destination) headers.get("jms_replyTo");
        final String uuid = (String) headers.get("jms_correlationId");

        JmsMessageStructure messageStructure = sendReply(jmsMessageStructure, replyToQueue, uuid, existDocument.getValue());

        System.out.println("ExistsDocument - Reply message sent to " + replyToQueue.toString());
        return new GenericMessage(messageStructure);
    }

    public Message processMessageDownload(Message message) {
        Map<String, Object> headers = message.getHeaders();
        Object payload = message.getPayload();
        JmsMessageStructure jmsMessageStructure = (JmsMessageStructure) payload;

        Object[] parameters = jmsMessageStructure.getParameters();
        DocumentIdentifier documentIdentifier = (DocumentIdentifier) parameters[0];
        documentIdentifier.getPath();

        Destination replyToQueue = (Destination) headers.get("jms_replyTo");
        final String uuid = (String) headers.get("jms_correlationId");

        String alternateRepository = jmsMessageStructure.getConfiguration().get(ALTERNATE_REPOSITORY_PROPERTIES);

        DocumentIdentifier storedDocumentIdentifier = null;
        if (JCR.equals(alternateRepository)) {
            // download from cmis and store in jcr
            DocumentStream documentStream = cmisStoreService.downloadDocument(StoreContext.builder().build(), documentIdentifier);
//            String temporaryFileName = DmsUtils.getFileName(documentIdentifier.getPath()) + "." + DmsUtils.getFileExtension(documentStream.getFileName());

            StoreContext storeContext = StoreContext.builder().allowedFolder("/temporary/download").build();
            storedDocumentIdentifier = jcrStoreService.storeDocument(storeContext,null,documentStream.getInputStream(),true,null);
        } else if (CMIS.equals(alternateRepository)) {
            // download from jcr and store in cmis
            DocumentStream documentStream = jcrStoreService.downloadDocument(StoreContext.builder().build(), documentIdentifier);
//            String temporaryFileName = DmsUtils.getFileName(documentIdentifier.getPath()) + "." + DmsUtils.getFileExtension(documentStream.getFileName());

            StoreContext storeContext = StoreContext.builder().allowedFolder("/temporary/download").build();
            storedDocumentIdentifier = cmisStoreService.storeDocument(storeContext,null,documentStream.getInputStream(),true,VersioningType.NONE);
        }

        JmsMessageStructure messageStructure = sendReply(jmsMessageStructure, replyToQueue, uuid, storedDocumentIdentifier);

        System.out.println("DownloadDocument - Reply message sent to " + replyToQueue.toString());
        return new GenericMessage(messageStructure);
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
