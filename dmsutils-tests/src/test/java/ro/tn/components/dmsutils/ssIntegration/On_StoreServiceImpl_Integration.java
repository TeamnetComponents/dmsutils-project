package ro.tn.components.dmsutils.ssIntegration;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ro.croco.integration.dms.toolkit.*;

import java.io.*;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by battamir.sugarjav on 3/2/2015.
 */
public class On_StoreServiceImpl_Integration {

    public static StoreService getStoreServiceImpl(String configFileLocation){
        try {
            IntegrationServiceFactory integrationServiceFactory = new IntegrationServiceFactory(configFileLocation);
            return (StoreServiceImpl_Integration) integrationServiceFactory.getInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String configFileLocation = null;
    private static StoreServiceImpl_Integration dbIntegrationService = null;

    @BeforeClass
    public static void getStoreServiceDb(){
        configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\ss-integration.properties";
        dbIntegrationService = (StoreServiceImpl_Integration)getStoreServiceImpl(configFileLocation);
    }

    @Test
    public void check_correct_instantiation(){
        assertNotNull(dbIntegrationService);
    }

    @Test
    public void store_document() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo("/dir1/dir2/test.txt",null,null);
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        Assert.assertNotNull(dbIntegrationService.storeDocument(StoreContext.builder().build(), documentInfo, byteArrayInputStream,true, VersioningType.MAJOR));
    }

    @Test
    public void download_document(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath("/dir1/dir2/test").build();
        DocumentStream documentStream = dbIntegrationService.downloadDocument(StoreContext.builder().build(),documentIdentifier);
        Assert.assertNotNull(documentStream);
    }
}
