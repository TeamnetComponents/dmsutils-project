package ro.croco.integration.dms.toolkit;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 11/18/2014.
 */
public interface InitableService {
    /**
     * Metoda __init este utilizata intern de clasa de tip factory folosita pentru instantierea serviciului de persistenta
     *
     * @param context - reprezinta tipul repository-ului si parametri specifici de initializare pentru persistenta
     */
    public void __init(Properties context) throws IOException;

    public Properties getContext();

    Object getContextProperty(String propertyName);

    /**
     * This method returns the service name.
     *
     * @return the name of the service as it is configured in the properties file as StoreServiceFactory.SERVICE_NAME property
     */

    public String getName();

 }
