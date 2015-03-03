package integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.croco.integration.dms.toolkit.CmisProperties;
import ro.croco.integration.dms.toolkit.StoreService;
import ro.croco.integration.dms.toolkit.StoreServiceFactory;
import ro.croco.integration.dms.toolkit.jcr.JcrProperties;

import java.io.IOException;

/**
 * Created by hanna.botar on 7/7/2014.
 */
@Configuration
public class DmsConfig {

    public DmsConfig(){
    }

//    private static final String cmisConfigFile = "cmis";
//    private static final String jcrConfigFile = "jcr";

    @Bean(name = "cmisStoreService")
    public StoreService cmisStoreService(){
//        CmisProperties cmisProperties = new CmisProperties();
//        cmisProperties.setUrl("http://sol-w2k8-04:8080/elo-cmis-server/browser");
//        cmisProperties.setRepositoryId("sol-w2k8-04_elo2");
//
//        StoreServiceFactory ssf = new StoreServiceFactory(cmisProperties.getProperties());
//        try {
//            return ssf.getService();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    @Bean(name = "jcrStoreService")
    public StoreService jcrStoreService() {
//        System.out.println("suntem aici2");
//        JcrProperties jcrProperties = new JcrProperties();
////        jcrProperties.setConnectionType(JcrProperties.ConnectionTypes.CONN_LOCAL);
////        jcrProperties.setLocalConfigFile("D:/JcrRepo/repository.xml");
//        StoreServiceFactory ssf = new StoreServiceFactory(jcrProperties.getProperties());
//        try {
//            return ssf.getService();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return null;
    }

}
