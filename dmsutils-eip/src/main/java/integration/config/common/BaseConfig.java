package integration.config.common;

/**
 *
 */
public interface BaseConfig {

    String PROFILE_PATH = "META-INF/"+ (System.getProperty("spring.profiles.active") != null ? System.getProperty("spring.profiles.active") : "dev");
    String BO_CMIS = PROFILE_PATH + "/bo-cmis.properties";
    String FO_CMIS = PROFILE_PATH + "/fo-cmis.properties";
    String METADATA_INSTANCE_CONFIG = "service.metadata.instance.configuration";
    String DEV_JMS_PROPERTIES = "META-INF/dev/jms.properties";
    String TEST_JMS_PROPERTIES = "META-INF/test/jms.properties";
    String PROD_JMS_PROPERTIES = "META-INF/prod/jms.properties";


    String JMS_INITIAL_CONTEXT_FACTORY = "com.ibm.websphere.naming.WsnInitialContextFactory";
    String JMS_PROVIDER_URL = "java.naming.provider.url";
    String JMS_ORB_CLASS = "org.omg.CORBA.ORBClass";
    String JMS_ORB = "com.ibm.CORBA.iiop.ORB";
    String JMS_CONNECTION_FACTORY = "jms.queue.connection.factory";
    String JMS_QUEUE = "jms.queue.producer";

    String REST_USER = "restadmin";
    String REST_PASSWORD = "restadmin";
}
