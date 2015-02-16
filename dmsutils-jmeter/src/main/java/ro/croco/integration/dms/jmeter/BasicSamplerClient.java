package ro.croco.integration.dms.jmeter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.toolkit.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 11/13/2014.
 */
public abstract class BasicSamplerClient extends AbstractJavaSamplerClient {
    private static Map<String, StoreService> storeServiceMap = new HashMap<String, StoreService>();
    protected static FileUtils fileUtilsOS = new FileUtils(getRoot(), System.getProperty("file.separator"));
    protected static FileUtils fileUtilsDMS = new FileUtils("/", "/");

    public static String STORE_SERVICE_FILE_NAME = "STORE_SERVICE_FILE_NAME";
    public static String PATH_LOCAL = "PATH_LOCAL";
    public static String PATH_DMS = "PATH_DMS";
    public static String RUN_USER = "RUN_USER";
    public static String RUN_PASSWORD = "RUN_PASSWORD";
    public static String PROCESS_FILE_PATH = "PROCESS_FILE_PATH";

    protected StoreService storeService;

    private static String getRoot() {
        String root = "";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            root = "C:";
        } else {
            root = System.getProperty("file.separator");
        }
        return root;
    }

    public static StoreService getStoreService(String configFileName) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        StoreService storeService = null;
        if (StringUtils.isNotEmpty(configFileName)) {
            storeService = storeServiceMap.get(configFileName);
            if (storeService == null) {
                synchronized (storeServiceMap) {
                    if (storeService == null) {
                        StoreServiceFactory storeServiceFactory = new StoreServiceFactory(configFileName);
                        storeService = storeServiceFactory.getService();
                        storeServiceMap.put(configFileName, storeService);
                    }
                }
            }
        }
        return storeService;
    }


    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        try {
            storeService = BasicSamplerClient.getStoreService(context.getParameter(STORE_SERVICE_FILE_NAME));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }


    public static DocumentIdentifier testUploadDocument(StoreService storeService, StoreContext storeContext, String destinationPathNameRoot, String sourcePathNameRoot, String sourceDocumentPathName, Map<String, Object> documentProperties, String documentType) throws Exception {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(sourceDocumentPathName));
            String sourceDocumentPathNameRelative = fileUtilsOS.convertPathNameWithoutNormalize(sourceDocumentPathName.substring(sourcePathNameRoot.length()), fileUtilsDMS);
            String destinationDocumentPathName = destinationPathNameRoot + sourceDocumentPathNameRelative;
            String destinationPathName = fileUtilsDMS.getParentFolderPathName(destinationDocumentPathName);

            DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(destinationDocumentPathName).build();
            DocumentInfo documentInfo = new DocumentInfo(destinationPathName, destinationDocumentPathName, documentType, documentProperties);
            DocumentIdentifier documentIdentifierStored = storeService.storeDocument(storeContext, documentInfo, inputStream, true, VersioningType.MINOR);
            return documentIdentifierStored;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                //do nothing
            }
        }
    }

    public static DocumentIdentifier testUploadDocument(SampleResult sampleResult, StoreService storeService, StoreContext storeContext, String destinationPathNameRoot, String sourcePathNameRoot, String sourceDocumentPathName, Map<String, Object> documentProperties, String documentType) throws Exception {
        InputStream inputStream = null;
        try {
            byte[] bytes = IOUtils.toByteArray(new FileInputStream(new File(sourceDocumentPathName)));
            inputStream = new ByteArrayInputStream(bytes);

            sampleResult.sampleStart();
            String sourceDocumentPathNameRelative = fileUtilsOS.convertPathNameWithoutNormalize(sourceDocumentPathName.substring(sourcePathNameRoot.length()), fileUtilsDMS);
            String destinationDocumentPathName = destinationPathNameRoot + sourceDocumentPathNameRelative;
            String destinationPathName = fileUtilsDMS.getParentFolderPathName(destinationDocumentPathName);

            DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(destinationDocumentPathName).build();
            DocumentInfo documentInfo = new DocumentInfo(destinationPathName, destinationDocumentPathName, documentType, documentProperties);
            DocumentIdentifier documentIdentifierStored = storeService.storeDocument(storeContext, documentInfo, inputStream, true, VersioningType.MAJOR);

            //set ok info
            sampleResult.setResponseData(documentIdentifierStored.toString(), null);
            sampleResult.setSuccessful(true);
            sampleResult.setResponseCodeOK();
            sampleResult.setResponseMessageOK();

            return documentIdentifierStored;
        } catch (Exception e) {
            //set error info
            sampleResult.setSuccessful(false);
            sampleResult.setResponseCode("500");
            sampleResult.setResponseMessage(e.toString());

            throw e;
        } finally {

            try {
                sampleResult.sampleEnd();
            } catch (Exception e) {
                //do nothing
            }
            try {
                inputStream.close();
            } catch (Exception e) {
                //do nothing
            }

        }
    }


    public static String testDownloadDocument(SampleResult sampleResult, StoreService storeService, StoreContext storeContext, String destinationPathNameRoot, String sourcePathNameRoot, String sourceDocumentPathName) throws Exception {

        String filePartName = sourceDocumentPathName.substring(sourcePathNameRoot.length());

        String sourceDocumentPathNameRelative = fileUtilsDMS.convertPathNameWithoutNormalize(filePartName, fileUtilsOS);
        String destinationDocumentPathName = destinationPathNameRoot + sourceDocumentPathNameRelative;
        String destinationPathName = fileUtilsDMS.getParentFolderPathName(destinationDocumentPathName);

        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(sourceDocumentPathName).build();
        DocumentInfo documentInfo = storeService.getDocumentInfo(storeContext, documentIdentifier);
        DocumentStream documentStream = storeService.downloadDocument(storeContext, documentIdentifier);

        File targetFile = new File(destinationDocumentPathName);
        org.apache.commons.io.FileUtils.copyInputStreamToFile(documentStream.getInputStream(), targetFile);
        return destinationDocumentPathName;
    }


    public static String testDownloadDocument(StoreService storeService, StoreContext storeContext, String destinationPathNameRoot, String sourcePathNameRoot, String sourceDocumentPathName) throws Exception {

        String filePartName = sourceDocumentPathName.substring(sourcePathNameRoot.length());

        String sourceDocumentPathNameRelative = fileUtilsDMS.convertPathNameWithoutNormalize(filePartName, fileUtilsOS);
        String destinationDocumentPathName = destinationPathNameRoot + sourceDocumentPathNameRelative;
        String destinationPathName = fileUtilsDMS.getParentFolderPathName(destinationDocumentPathName);

        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(sourceDocumentPathName).build();
        DocumentInfo documentInfo = storeService.getDocumentInfo(storeContext, documentIdentifier);
        DocumentStream documentStream = storeService.downloadDocument(storeContext, documentIdentifier);

        File targetFile = new File(destinationDocumentPathName);
        org.apache.commons.io.FileUtils.copyInputStreamToFile(documentStream.getInputStream(), targetFile);
        return destinationDocumentPathName;
    }


    public static void main(String[] args) throws Exception {
        StoreService storeService = null;
        //storeService = getStoreService("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-fo.properties");
        //storeService = getStoreService("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-jms.properties");
        //storeService = getStoreService("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-local.properties");
        //storeService = getStoreService("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-sibiac.properties");
        storeService = getStoreService("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-jms-prod.properties");


//        StoreServiceImpl_Integration storeServiceImpl_integration = (StoreServiceImpl_Integration) storeService;
//
//        storeServiceImpl_integration.getIntegrationService().addStoreServiceMessageListener(new StoreServiceMessageListener() {
//            @Override
//            public void onReceive(StoreServiceMessageEvent storeServiceMessageEvent) {
//
//            }
//        });


        StoreContext storeContext = StoreContext.builder()
                .communicationType(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)
                //.loginAs("Mihai.Viscea")
                .build();
        String destinationPathNameRoot = "/JMeter/upload3";
        String sourcePathNameRoot = "C:\\__JAVA\\jmeter\\test\\dmsutils\\upload";
        String sourceDocumentPathName = sourcePathNameRoot + "\\" + "db2.pdf";
        Map<String, Object> documentProperties = null;
        String documentType = null;
        documentType = "cmis:document-mask-23";
        documentType = "cmis:document~mask~Cerere de finantare";
        documentType = "cmis:document~mask~Intrare elementarã";
        documentType = "cmis:document";


        DocumentIdentifier documentIdentifier = testUploadDocument(storeService, storeContext,
                destinationPathNameRoot,
                sourcePathNameRoot,
                sourceDocumentPathName,
                documentProperties, documentType);

        System.out.println(documentIdentifier.toString());

    }


    public static void main2(String[] args) throws Exception {
        StoreService storeService = getStoreService("C:\\__JAVA\\jmeter\\test\\dmsutils\\cmis");
        StoreContext storeContext = StoreContext.builder().build();
        String sourcePathNameRoot = "/JMeter/upload";
        String destinationPathNameRoot = "C:\\__JAVA\\jmeter\\test\\dmsutils\\download";
        String sourceDocumentPathName = sourcePathNameRoot + "/" + "db2";
        Map<String, Object> documentProperties = null;
        String documentType = "cmis:document";

        String filePathName = testDownloadDocument(storeService, storeContext,
                destinationPathNameRoot,
                sourcePathNameRoot,
                sourceDocumentPathName);

    }

    public static void mainr(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        String fileStoreService = "C:\\__JAVA\\jmeter\\test\\dmsutils\\cmis-local";
        //StoreService storeService = getStoreService(fileStoreService);
        StoreContext storeContext = StoreContext.builder().build();
        String sourcePathNameRoot = "/JMeter/upload";
        sourcePathNameRoot = "/Proiecte/Dosar3954";
        String fileList = "C:\\__JAVA\\jmeter\\test\\dmsutils\\fileList.csv";
        JmeterFileUtils.listFileNames(sourcePathNameRoot, fileStoreService, fileList);
    }

}
