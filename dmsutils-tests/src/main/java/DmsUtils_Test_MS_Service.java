import ro.croco.integration.dms.toolkit.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by Lucian.Dragomir on 11/16/2015.
 */
public class DmsUtils_Test_MS_Service {

    private static String metadataServiceFileLocation = "C:\\__JAVA\\jmeter\\test\\dmsutils\\SIRMES_DEV\\sirmes-ms.properties";
    private static String storeServiceFileLocation = "C:\\__JAVA\\jmeter\\test\\dmsutils\\SIRMES_DEV\\sirmes-ss.properties";

    public static MetadataService getMetadataServiceImpl(String configFileLocation) throws IOException {
        MetadataService metadataService = null;
        try {
            MetadataServiceFactory metadataServiceFactory = null;
            metadataServiceFactory = new MetadataServiceFactory(configFileLocation);
            metadataService = metadataServiceFactory.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metadataService;
    }

    public static StoreService getStoreServiceImpl(String configFileLocation) {
        try {
            StoreServiceFactory storeServiceFactory = new StoreServiceFactory(configFileLocation);
            return storeServiceFactory.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {

        //instantiate DMS services
        MetadataService metadataService = getMetadataServiceImpl(metadataServiceFileLocation);
        StoreService storeService = getStoreServiceImpl(storeServiceFileLocation);

        //compute metadata for a document
        StoreService storeServiceDestination = storeService;
        StoreContext storeContextDestination = StoreContext.builder().metadataOperation("STORE").build();
        MetadataService.MetadataProperties metadataProperties = MetadataService.MetadataProperties.builder()
                .withCode("DOC_JUST_INCIDENT")
                .withContext("DEFAULT")
                .withName("GigiDocumentName")
                .withExtension("txt")
                .withProperty("IdIncident", "3",true)
                .build();

        MetadataService.Metadata documentMetadata = metadataService.computeDocumentMetadata(storeServiceDestination, storeContextDestination, metadataProperties);
        System.out.println(documentMetadata);

        InputStream byteArrayInputStream = new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8));
        DocumentIdentifier documentIdentifier = storeService.storeDocument(storeContextDestination,
                (DocumentInfo) documentMetadata.getInfo(),
                byteArrayInputStream, true, documentMetadata.getVersioningType());


//        MetadataService.Metadata<DocumentInfo> metadata = storeService.getMetadataService().computeDocumentMetadata(properties.getProperty("documentType"), properties.getProperty("documentContext"), storeService, sc, properties);
//        System.out.println(metadata);
//        DocumentInfo documentInfo = new DocumentInfo();
//        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\testfile.txt"));
//        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
//        DocumentIdentifier documentIdentifier = null;

    }
}
