package ro.croco.integration.dms.toolkit;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 1/4/2015.
 */
public class IntegrationServiceFactory extends ServiceFactory_Abstract<IntegrationService> {
    public IntegrationServiceFactory(Properties context) {
        super(context);
    }

    public IntegrationServiceFactory(String contextName, String... alternatePaths) throws IOException {
        super(contextName, alternatePaths);
    }

    public IntegrationServiceFactory(String contextName, ContextType contextType, String... alternatePaths) throws IOException {
        super(contextName, contextType, alternatePaths);
    }
}
