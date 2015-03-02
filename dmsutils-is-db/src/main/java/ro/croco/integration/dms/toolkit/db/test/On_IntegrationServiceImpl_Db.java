package ro.croco.integration.dms.toolkit.db.test;

import org.junit.BeforeClass;
import org.junit.Test;
import ro.croco.integration.dms.toolkit.IntegrationService;
import ro.croco.integration.dms.toolkit.IntegrationServiceFactory;
import ro.croco.integration.dms.toolkit.StoreContext;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;
import ro.croco.integration.dms.toolkit.db.IntegrationServiceImpl_Db;

import static org.junit.Assert.assertNotNull;

/**
 * Created by battamir.sugarjav on 3/2/2015.
 */
public class On_IntegrationServiceImpl_Db {

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
    private static IntegrationServiceImpl_Db dbIntegrationService = null;

    @BeforeClass
    public static void getStoreServiceDb(){
        configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\is-db.properties";
        dbIntegrationService = (IntegrationServiceImpl_Db)getStoreServiceImpl(configFileLocation);
    }

    @Test
    public void check_correct_instantiation(){
        assertNotNull(dbIntegrationService);
    }

    @Test
    public void send_and_receive(){
        dbIntegrationService.sendAndReceive(new StoreServiceMessage(), StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS);
    }
}
