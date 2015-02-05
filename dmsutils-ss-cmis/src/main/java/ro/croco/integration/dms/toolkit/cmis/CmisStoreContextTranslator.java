package ro.croco.integration.dms.toolkit.cmis;

import ro.croco.integration.dms.toolkit.StoreContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 1/3/2015.
 */
public class CmisStoreContextTranslator {
    public static final String DMS_STORE_CONTEXT_TRANSLATOR = StoreContext.DMS_PROPERTIES_PREFIX + "translator.classname";

    public static CmisStoreContextTranslator getInstance(Properties properties) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        CmisStoreContextTranslator instance;
        String className = CmisStoreContextTranslator.class.getCanonicalName();
        if (properties.containsKey(DMS_STORE_CONTEXT_TRANSLATOR)) {
            className = properties.getProperty(DMS_STORE_CONTEXT_TRANSLATOR);
        }
        instance = (CmisStoreContextTranslator) Class.forName(className).newInstance();
        return instance;
    }

    public CmisStoreContextTranslator() {
    }

    public Properties translate(Properties globalContext, StoreContext storeContext) {
        Properties completeContext = null;
        completeContext = toProperties(globalContext);
        completeContext.putAll(storeContext);
        return completeContext;
    }

//    public Map<String, String> getExtendedRequestPropertyMap() {
//        return null;
//    }


    public static Map<String, String> toMap(Properties properties) {
        Map context = new HashMap();
        if (properties != null) {
            for (Object propertyKey : properties.keySet()) {
                context.put(propertyKey, properties.get(propertyKey));
            }
        }
        return context;
    }

    public static Map<String, String> toMap(Map<String, String> properties) {
        Map context = new HashMap();
        if (properties != null) {
            for (Object propertyKey : properties.keySet()) {
                context.put(propertyKey, properties.get(propertyKey));
            }
        }
        return context;
    }

    public static Properties toProperties(Map properties) {
        Properties context = new Properties();
        if (properties != null) {
            context.putAll(properties);
        }
        return context;
    }

    public static Properties toProperties(Properties properties) {
        Properties context = new Properties();
        if (properties != null) {
            context.putAll(properties);
        }
        return context;
    }

}
