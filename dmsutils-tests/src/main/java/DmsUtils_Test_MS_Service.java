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

    public static void listFolder(StoreService storeService, StoreContext storeContextDestination, FolderIdentifier folderIdentifier) {
        boolean includeInfo = true;
        System.out.println("-------------------------------------------------");
        System.out.println("Listing folder content for " + folderIdentifier.toString());
        try {
            ObjectInfoTree objectInfoTree = storeService.listFolderContent(storeContextDestination, folderIdentifier, 1, includeInfo, ObjectBaseType.DOCUMENT);
            List<ObjectInfo> objectInfos = objectInfoTree.listContent();
            for (ObjectInfo objectInfo : objectInfos) {
                System.out.println(objectInfo.getIdentifier());
                System.out.println(objectInfo);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        System.out.println("-------------------------------------------------");
    }

    public static void main(String[] args) throws IOException {

        String exemplu = "INCIDENT";
        exemplu = "CAZ";
        exemplu = "REGISTRATURA";

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
        appConnection = DatabaseUtils.getConnection(context, "SirmesDB");

        metadataService.setConnection("SirmesDB", appConnection);

        StoreService storeServiceDestination = storeService;
        StoreContext storeContextDestination = StoreContext.builder().metadataOperation("STORE").build();


        //INCIDENT
        if (exemplu.equalsIgnoreCase("INCIDENT")) {
            String idIncident = "1356";

            //getting folder info
            MetadataService.MetadataProperties folderProperties = MetadataService.MetadataProperties.builder()
                    .withCode("DOSAR_INCIDENT")
                    .withContext("DEFAULT")
                    .withProperty("IdIncident", idIncident, true)
                    .build();


            System.out.println("-------------------------------------------------");
            MetadataService.Metadata<FolderInfo> folderMetadata = metadataService.computeFolderMetadata(storeServiceDestination, storeContextDestination, folderProperties);
            System.out.println(folderMetadata);
            System.out.println("-------------------------------------------------");
            FolderIdentifier folderIdentifier = (FolderIdentifier) folderMetadata.getInfo().getIdentifier();

            //list folder before upload
            listFolder(storeService, storeContextDestination, folderIdentifier);

            //upload document
            MetadataService.MetadataProperties metadataProperties = MetadataService.MetadataProperties.builder()
                    .withCode("DOC_JUST_INCIDENT")
                    .withContext("DEFAULT")
                    .withName("doc_test_incident")
                    .withExtension("txt")
                    .withProperty("IdIncident", idIncident, true)
                    .withProperty("Tip document", "Tip document - test")
                    .withProperty("Data emiterii document", "06/11/2015")
                    .withProperty("Emitent document", "Emitent document test")
                    .withProperty("Numar document", 1241234)
                    .withProperty("Utilizator", "portal")
                    .build();

            MetadataService.Metadata documentMetadata = metadataService.computeDocumentMetadata(storeServiceDestination, storeContextDestination, metadataProperties);
            System.out.println(documentMetadata);

            InputStream byteArrayInputStream = new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8));
            DocumentIdentifier documentIdentifier = storeService.storeDocument(storeContextDestination,
                    (DocumentInfo) documentMetadata.getInfo(),
                    byteArrayInputStream, documentMetadata.isAllowCreatePath(), documentMetadata.getVersioningType());


            //list folder after upload
            listFolder(storeService, storeContextDestination, folderIdentifier);

            //delete document
            //storeService.deleteDocument(storeContextDestination, documentIdentifier);

        }

        //CAZ
        if (exemplu.equalsIgnoreCase("CAZ")) {
            String idCaz = "954";

            //getting folder info
            MetadataService.MetadataProperties folderProperties = MetadataService.MetadataProperties.builder()
                    .withCode("DOSAR_CAZ")
                    .withContext("DEFAULT")
                    .withProperty("IdCaz", idCaz, true)
                    .build();


            System.out.println("-------------------------------------------------");
            MetadataService.Metadata<FolderInfo> folderMetadata = metadataService.computeFolderMetadata(storeServiceDestination, storeContextDestination, folderProperties);
            System.out.println(folderMetadata);
            System.out.println("-------------------------------------------------");
            FolderIdentifier folderIdentifier = (FolderIdentifier) folderMetadata.getInfo().getIdentifier();

            //list folder before upload
            listFolder(storeService, storeContextDestination, folderIdentifier);

            //upload document
            MetadataService.MetadataProperties metadataProperties = MetadataService.MetadataProperties.builder()
                    .withCode("DOC_JUST_CAZ")
                    .withContext("DEFAULT")
                    .withName("doc_test_caz")
                    .withExtension("txt")
                    .withProperty("IdCaz", idCaz, true)
                    .withProperty("Tip document", "Tip document - test")
                    .withProperty("Data emiterii document", "06/11/2015")
                    .withProperty("Emitent document", "Emitent document test")
                    .withProperty("Numar document", 1241234)
                    .withProperty("Utilizator", "portal")
                    .build();

            MetadataService.Metadata documentMetadata = metadataService.computeDocumentMetadata(storeServiceDestination, storeContextDestination, metadataProperties);
            System.out.println(documentMetadata);

            InputStream byteArrayInputStream = new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8));
            DocumentIdentifier documentIdentifier = storeService.storeDocument(storeContextDestination,
                    (DocumentInfo) documentMetadata.getInfo(),
                    byteArrayInputStream, documentMetadata.isAllowCreatePath(), documentMetadata.getVersioningType());


            //list folder after upload
            listFolder(storeService, storeContextDestination, folderIdentifier);

            //delete document
            //storeService.deleteDocument(storeContextDestination, documentIdentifier);
        }


        //REGISTRATURA
        if (exemplu.equalsIgnoreCase("REGISTRATURA")) {
            String idRie = "244";
            String type = "OUT";

            //getting folder info
            MetadataService.MetadataProperties folderProperties = MetadataService.MetadataProperties.builder()
                    .withCode("DOSAR_REGISTRATURA_" + type)
                    .withContext("DEFAULT")
                    .withProperty("IdRie", idRie, true)
                    .build();


            System.out.println("-------------------------------------------------");
            MetadataService.Metadata<FolderInfo> folderMetadata = metadataService.computeFolderMetadata(storeServiceDestination, storeContextDestination, folderProperties);
            System.out.println(folderMetadata);
            System.out.println("-------------------------------------------------");
            FolderIdentifier folderIdentifier = (FolderIdentifier) folderMetadata.getInfo().getIdentifier();

            //list folder before upload
            listFolder(storeService, storeContextDestination, folderIdentifier);

            //upload document
            MetadataService.MetadataProperties metadataProperties = MetadataService.MetadataProperties.builder()
                    .withCode("DOC_REGISTRATURA_" + type)
                    .withContext("DEFAULT")
                    .withName("doc_test_registratura")
                    .withExtension("txt")
                    .withProperty("IdRie", idRie, true)
                    .withProperty("Tip document", "Tip document - test")
                    .withProperty("Data emiterii document", "06/11/2015")
                    .withProperty("Emitent document", "Emitent document test")
                    .withProperty("Numar document", 1241234)
                    .withProperty("Utilizator", "portal")
                    .build();

            MetadataService.Metadata documentMetadata = metadataService.computeDocumentMetadata(storeServiceDestination, storeContextDestination, metadataProperties);
            System.out.println(documentMetadata);

            InputStream byteArrayInputStream = new ByteArrayInputStream("ABC".getBytes(StandardCharsets.UTF_8));
            DocumentIdentifier documentIdentifier = storeService.storeDocument(storeContextDestination,
                    (DocumentInfo) documentMetadata.getInfo(),
                    byteArrayInputStream, documentMetadata.isAllowCreatePath(), documentMetadata.getVersioningType());


            //list folder after upload
            listFolder(storeService, storeContextDestination, folderIdentifier);

            //delete document
            //storeService.deleteDocument(storeContextDestination, documentIdentifier);
        }


//        MetadataService.Metadata<DocumentInfo> metadata = storeService.getMetadataService().computeDocumentMetadata(properties.getProperty("documentType"), properties.getProperty("documentContext"), storeService, sc, properties);
//        System.out.println(metadata);
//        DocumentInfo documentInfo = new DocumentInfo();
//        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\testfile.txt"));
//        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
//        DocumentIdentifier documentIdentifier = null;

    }
}
