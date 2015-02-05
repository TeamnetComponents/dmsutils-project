package ro.croco.integration.dms.toolkit;

import org.apache.chemistry.opencmis.commons.SessionParameter;

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
        properties.setProperty(SessionParameter.USER, username);
    }

    public void setPassword(String password){
        properties.setProperty(SessionParameter.PASSWORD, password);
    }

    public void setConnectionType(String connectionType){
        properties.setProperty(SessionParameter.BINDING_TYPE, connectionType);
    }

    public void setUrl(String url){
        properties.setProperty(SessionParameter.BROWSER_URL, url);
    }

    public void setRepositoryId(String repositoryId) {
        properties.setProperty(SessionParameter.REPOSITORY_ID, repositoryId);
    }

    public Properties getProperties() {
        return properties;
    }

}
