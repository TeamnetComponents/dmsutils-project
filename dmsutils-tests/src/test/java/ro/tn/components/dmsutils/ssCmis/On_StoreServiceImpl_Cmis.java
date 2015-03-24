package ro.tn.components.dmsutils.ssCmis;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.croco.integration.dms.toolkit.*;

import java.io.*;
import java.util.Properties;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by battamir.sugarjav on 3/2/2015.
 */
public class On_StoreServiceImpl_Cmis {

    public static StoreService getStoreServiceImpl(String configFileLocation) {
        try {
            StoreServiceFactory storeServiceFactory = new StoreServiceFactory(configFileLocation);
            return storeServiceFactory.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String configFileLocation = null;
    private static StoreService storeService = null;

    @BeforeClass
    public static void getStoreServiceDb() {
        configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\cmis.properties";
        storeService = getStoreServiceImpl(configFileLocation);
    }

    @Test
    public void check_correct_instantiation() {
        assertNotNull(storeService);
    }

    @Test
    public void store_document() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo("/dir1/dir2/test.txt", null, null);
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-cmis_SIAMC.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        DocumentIdentifier documentIdentifier = null;
        Assert.assertNotNull(documentIdentifier = storeService.storeDocument(StoreContext.builder().build(), documentInfo, byteArrayInputStream, true, VersioningType.MAJOR));
        System.out.println(documentIdentifier);
    }


    @Test
    public void store_document2() throws IOException {
//        DocumentInfo documentInfo = new DocumentInfo("/dir1/dir2/test.txt", null, null);
        DocumentInfo documentInfo = new DocumentInfo();
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\testfile.txt"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        DocumentIdentifier documentIdentifier = null;

        Properties properties = new Properties();
        properties.put("frontUserName", "gigi");
        properties.put("documentType", "CR-FCR");
        properties.put("documentContext", "DEFAULT");
        properties.put("documentKey", "330");
        properties.put("documentName", "gigi.pdf");
        properties.put("METADATA.OBJECT_CODE", "CR-FCR");

        StoreContext sc = StoreContext.builder().build();

        MetadataService.Metadata<DocumentInfo> metadata = storeService.getMetadataService().computeDocumentMetadata(properties.getProperty("documentType"), properties.getProperty("documentContext"), storeService, sc, properties);
        System.out.println(metadata);

        Assert.assertNotNull(documentIdentifier = storeService.storeDocument(sc, documentInfo, byteArrayInputStream, true, VersioningType.MAJOR));
        System.out.println(documentIdentifier);
    }

    @Test
    public void download_document() {
        //316-215
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath("/dir1/dir2/test").build();
        documentIdentifier = DocumentIdentifier.builder().withId("316-215").build();

        DocumentStream documentStream = storeService.downloadDocument(StoreContext.builder().build(), documentIdentifier);
        Assert.assertNotNull(documentStream);
    }
}
