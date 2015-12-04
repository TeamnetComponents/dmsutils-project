import ro.croco.integration.dms.commons.DatabaseUtils;
import ro.croco.integration.dms.toolkit.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 11/16/2015.
 */
public class DmsUtils_Test_MS_Service {

    //private static String metadataServiceFileLocation = "C:\\__JAVA\\jmeter\\test\\dmsutils\\SIRMES_DEV\\sirmes-ms.properties";
    //private static String storeServiceFileLocation = "C:\\__JAVA\\jmeter\\test\\dmsutils\\SIRMES_DEV\\sirmes-ss.properties";
    private static String metadataServiceFileLocation = "C:\\__JAVA\\jmeter\\test\\dmsutils\\SIRMES_DEV\\sirmes-ms2.properties";
    private static String storeServiceFileLocation = "C:\\__JAVA\\jmeter\\test\\dmsutils\\SIRMES_DEV\\sirmes-ss2.properties";

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
        MetadataServiceImpl_Db metadataService = (MetadataServiceImpl_Db) getMetadataServiceImpl(metadataServiceFileLocation);
        StoreService storeService = getStoreServiceImpl(storeServiceFileLocation);

        Properties context = new Properties();

        context.setProperty("connection.jdbc.SirmesDB.type", "local");
        context.setProperty("connection.jdbc.SirmesDB.url", "jdbc:oracle:thin:@10.16.40.114:1521:sirmes");
        context.setProperty("connection.jdbc.SirmesDB.driver", "oracle.jdbc.driver.OracleDriver");
        context.setProperty("connection.jdbc.SirmesDB.user", "sirmes");
        context.setProperty("connection.jdbc.SirmesDB.password", "sirmestest123$");
        context.setProperty("connection.jdbc.SirmesDB.schema", "sirmes");

        Connection appConnection = null;
        appConnection =  DatabaseUtils.getConnection(context, "SirmesDB");

        metadataService.setConnection("SirmesDB", appConnection);

        //compute metadata for a document
        StoreService storeServiceDestination = storeService;
        StoreContext storeContextDestination = StoreContext.builder().metadataOperation("STORE").build();
        MetadataService.MetadataProperties metadataProperties = MetadataService.MetadataProperties.builder()
                .withCode("DOC_JUST_INCIDENT")
                .withContext("DEFAULT")
                .withName("gigi234")
                .withExtension("txt")
                .withProperty("IdIncident", "222", true)
                .withProperty("Tip document", "test")
                .withProperty("Data emiterii document", "06/11/2015")
                .withProperty("Emitent document", "test2")
                .withProperty("Numar document", 123)
                .withProperty("Utilizator", "portal")
                .build();

        MetadataService.Metadata documentMetadata = metadataService.computeDocumentMetadata(storeServiceDestination, storeContextDestination, metadataProperties);
        System.out.println(documentMetadata);
        //documentMetadata.setAllowCreatePath(true);

        InputStream byteArrayInputStream = new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8));
        DocumentIdentifier documentIdentifier = storeService.storeDocument(storeContextDestination,
                (DocumentInfo) documentMetadata.getInfo(),
                byteArrayInputStream, documentMetadata.isAllowCreatePath(), documentMetadata.getVersioningType());


        MetadataService.MetadataProperties folderProperties = MetadataService.MetadataProperties.builder()
                .withCode("DOSAR_INCIDENT")
                .withContext("DEFAULT")
                .withProperty("IdIncident", "952", true)
                .build();


        System.out.println("-------------------------------------------------");
        MetadataService.Metadata<FolderInfo> folderMetadata = metadataService.computeFolderMetadata(storeServiceDestination, storeContextDestination, folderProperties);
        System.out.println(folderMetadata);
        System.out.println("-------------------------------------------------");

        FolderIdentifier folderIdentifier = (FolderIdentifier) folderMetadata.getInfo().getIdentifier();
        System.out.println(folderIdentifier);

        boolean includeInfo = true;
        ObjectInfoTree objectInfoTree = storeService.listFolderContent(storeContextDestination, folderIdentifier, 1, includeInfo, ObjectBaseType.DOCUMENT);
        List<ObjectInfo> objectInfos = objectInfoTree.listContent();
        System.out.println("-------------------------------------------------");
        for (ObjectInfo objectInfo : objectInfos) {
            System.out.println(objectInfo.getIdentifier());
            System.out.println(objectInfo);
        }
        System.out.println("-------------------------------------------------");


//        MetadataService.Metadata<DocumentInfo> metadata = storeService.getMetadataService().computeDocumentMetadata(properties.getProperty("documentType"), properties.getProperty("documentContext"), storeService, sc, properties);
//        System.out.println(metadata);
//        DocumentInfo documentInfo = new DocumentInfo();
//        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\testfile.txt"));
//        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
//        DocumentIdentifier documentIdentifier = null;

    }
}
