package ro.croco.integration.dms.toolkit.utils;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
final public class ContextProperties {

    public static final class Required{
        public static final String CONNECTION_TYPE = "jdbc.type";
        public static final String CONNECTION_URL = "jdbc.url";
        public static final String CONNECTION_DRIVER = "jdbc.driver";
        public static final String CONNECTION_USER = "jdbc.user";
        public static final String CONNECTION_PASSWORD = "jdbc.password";
        public static final String INSTANCE_NAME="instance.name";
        public static final String ISTANCE_CLASS="instance.class";
        public static final String INSTANCE_CACHE="instance.cache";
    }

    public static final class Optional{
        public static final String CONNECTION_SCHEMA = "jdbc.schema";
    }

}
