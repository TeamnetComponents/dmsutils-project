package ro.croco.integration.dms.toolkit.test;

import org.junit.BeforeClass;
import org.junit.Test;
import ro.croco.integration.dms.toolkit.IntegrationService;
import ro.croco.integration.dms.toolkit.IntegrationServiceFactory;
import ro.croco.integration.dms.toolkit.StoreServiceImpl_Integration;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by battamir.sugarjav on 3/2/2015.
 */
public class On_StoreServiceImpl_Integration {

    public static IntegrationService getStoreServiceImpl(String configFileLocation){
        try {
            IntegrationServiceFactory integrationServiceFactory = new IntegrationServiceFactory(configFileLocation);
            return integrationServiceFactory.getInstance();
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

}
