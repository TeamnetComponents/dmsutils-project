package ro.croco.integration.dms.toolkit.cmis;

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Created by hanna.botar on 7/9/2014.
 */
public class CmisProperties {

    private Properties properties;

    public CmisProperties() {
        ResourceBundle resource = ResourceBundle.getBundle("profiles/cmis");
        properties = new Properties();
        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            properties.put(key, resource.getString(key));
        }
    }

    public void setUsername(String username){
        properties.setProperty("org.apache.chemistry.opencmis.user", username);
    }

    public void setPassword(String password){
        properties.setProperty("org.apache.chemistry.opencmis.password", password);
    }

    public void setConnectionType(String connectionType){
        properties.setProperty("org.apache.chemistry.opencmis.binding.spi.type", connectionType);
    }

    public void setUrl(String url){
        properties.setProperty("org.apache.chemistry.opencmis.binding.browser.url", url);
    }

    public void setRepositoryId(String repositoryId) {
        properties.setProperty("org.apache.chemistry.opencmis.session.repository.id", repositoryId);
    }

    public Properties getProperties() {
        return properties;
    }

}
