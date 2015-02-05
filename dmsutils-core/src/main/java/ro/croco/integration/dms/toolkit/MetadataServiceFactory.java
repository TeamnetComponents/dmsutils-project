package ro.croco.integration.dms.toolkit;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 8/21/2014.
 */
public class MetadataServiceFactory extends ServiceFactory_Abstract<MetadataService> {
    //public static String METADATA_CLASS = "metadata.class";

    public MetadataServiceFactory(Properties context) {
       super(context);
    }

    public MetadataServiceFactory(String contextName, String... alternatePaths) throws IOException {
        super(contextName, alternatePaths);
    }

    public MetadataServiceFactory(String contextName, ContextType contextType, String... alternatePaths) throws IOException {
        super(contextName, contextType, alternatePaths);
    }

    /*
    * @deprecated The getInstance() method should be used instead of this method.
    */
    public MetadataService getMetadata() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        return getInstance();
    }
}
