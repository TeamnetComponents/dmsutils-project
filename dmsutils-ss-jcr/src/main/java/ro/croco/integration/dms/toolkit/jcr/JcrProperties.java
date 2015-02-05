package ro.croco.integration.dms.toolkit.jcr;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by danielp on 7/9/14.
 */
public class JcrProperties {

    private Properties properties;

    public static enum ConnectionTypes{

        CONN_LOCAL("conn_local"),
        CONN_JNDI("conn_jndi"),
        CONN_RMI("conn_rmi");

        private String value;

        private ConnectionTypes(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }

    }

    public JcrProperties(){
//        System.setMetadataProperty("dms_utils_impl","test-fo");
        ResourceBundle resource = ResourceBundle.getBundle("profiles/jcr");
        properties = new Properties();
        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            properties.put(key, resource.getString(key));
        }
    }

    public void setUsername(String username){
        properties.setProperty("jcr.repo.username", username);
    }

    public void setPassword(String password){
        properties.setProperty("jcr.repo.password", password);
    }

    public void setConnectionType(ConnectionTypes connectionType){
        properties.setProperty("jcr.repo.connection.type", connectionType.getValue());
    }

    public void setLocalConfigFile(String configFilePath){
        properties.setProperty("jcr.repo.local.config.file", configFilePath);
    }

    public void setJNDIName(String jndiName){
        properties.setProperty("jcr.repo.jndi.address", jndiName);
    }

    public void setJNDIRemoteProvidedURL(String providedUrl){
        properties.setProperty("jcr.repo.jndi.remote.provided.url", providedUrl);
    }

    public Properties getProperties() {
        return properties;
    }
}
