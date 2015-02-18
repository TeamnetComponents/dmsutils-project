package ro.croco.integration.dms.toolkit.tests;

import org.junit.Test;
import ro.croco.integration.dms.toolkit.StoreService;
import ro.croco.integration.dms.toolkit.StoreServiceFactory;
import ro.croco.integration.dms.toolkit.StoreServiceImpl_Db;

import static org.junit.Assert.assertNotNull;

/**
 * Created by battamir.sugarjav on 2/18/2015.
 */
public class Tests_on_StoreServiceImpl_Db {

    private StoreService getStoreServiceImpl(String configFileLocation){
        try {
            StoreServiceFactory storeServiceFactory = new StoreServiceFactory(configFileLocation);
            return storeServiceFactory.getService();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void retrieve_local_store_service_db_test(){
        String configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties";
        StoreServiceImpl_Db dbStoreService = (StoreServiceImpl_Db)this.getStoreServiceImpl(configFileLocation);

        assertNotNull(dbStoreService.openSession(null).getConnection());
        assertNotNull(dbStoreService);
    }
}

