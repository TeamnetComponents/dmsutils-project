package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.exceptions.StoreServiceNotDefinedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 6/23/2014.
 */
public class StoreServiceFactory extends ServiceFactory_Abstract<StoreService> {


    private static final Map<String, StoreService> storeServiceMap = new HashMap<String, StoreService>();

    public StoreServiceFactory(Properties context) {
        super(context);
    }

    public StoreServiceFactory(String contextName, String... alternatePaths) throws IOException {
        super(contextName, alternatePaths);
    }

    public StoreServiceFactory(String contextName, ContextType contextType, String... alternatePaths) throws IOException {
        super(contextName, contextType, alternatePaths);
    }

    /*
         * @deprecated The getInstance() method should be used instead of this method.
         */
    public StoreService getService() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        return getInstance();
    }

    //------------------------------------------------------------------------------------------------------------------
    public static StoreService retrieveService(ObjectIdentifier objectIdentifier, StoreService defaultStoreService) {
        if (objectIdentifier == null || objectIdentifier.getStoreServiceName() == null) {
            return defaultStoreService;
        }

        if (objectIdentifier.getStoreServiceName().isEmpty()) {
            return defaultStoreService;
        }

        if (objectIdentifier.getStoreServiceName().equals(defaultStoreService.getName())) {
            return defaultStoreService;
        }

        StoreService storeService = (StoreService) retrieveInstance(StoreService.class.getCanonicalName(), objectIdentifier.getStoreServiceName());
        if (storeService == null) {
            throw new StoreServiceNotDefinedException("The service with name " + objectIdentifier.getStoreServiceName() + " is not defined.");
        }
        return storeService;
    }

}
