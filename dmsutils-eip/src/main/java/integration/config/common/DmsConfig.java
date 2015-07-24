package integration.config.common;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ro.croco.integration.dms.toolkit.*;
import ro.croco.integration.dms.toolkit.jcr.JcrProperties;

import java.io.IOException;

/**
 * Created by hanna.botar on 7/7/2014.
 */
@Configuration
public class DmsConfig implements BaseConfig {

    public DmsConfig(){
    }

////    private static final String cmisConfigFile = "cmis";
////    private static final String jcrConfigFile = "jcr";
//
//    @Bean(name = "cmisStoreService")
//    public StoreService cmisStoreService(){
////        CmisProperties cmisProperties = new CmisProperties();
////        cmisProperties.setUrl("http://sol-w2k8-04:8080/elo-cmis-server/browser");
////        cmisProperties.setRepositoryId("sol-w2k8-04_elo2");
////
////        StoreServiceFactory ssf = new StoreServiceFactory(cmisProperties.getProperties());
////        try {
////            return ssf.getService();
////        } catch (ClassNotFoundException e) {
////            e.printStackTrace();
////        } catch (IllegalAccessException e) {
////            e.printStackTrace();
////        } catch (InstantiationException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        return null;
//    }
//
//    @Bean(name = "jcrStoreService")
//    public StoreService jcrStoreService() {
////        System.out.println("suntem aici2");
////        JcrProperties jcrProperties = new JcrProperties();
//////        jcrProperties.setConnectionType(JcrProperties.ConnectionTypes.CONN_LOCAL);
//////        jcrProperties.setLocalConfigFile("D:/JcrRepo/repository.xml");
////        StoreServiceFactory ssf = new StoreServiceFactory(jcrProperties.getProperties());
////        try {
////            return ssf.getService();
////        } catch (ClassNotFoundException e) {
////            e.printStackTrace();
////        } catch (IllegalAccessException e) {
////            e.printStackTrace();
////        } catch (InstantiationException e) {
////            e.printStackTrace();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        return null;
//    }

    @Bean(name = "boStoreService")
    public StoreService boStoreService() {
        StoreServiceFactory ssf;
        StoreService storeService = null;
        try {
            ssf = new StoreServiceFactory(BO_CMIS);
            String metadataConfig = (String) ssf.getContext().get(METADATA_INSTANCE_CONFIG);
            if (metadataConfig != null && !metadataConfig.isEmpty()) {
                metadataConfig = PROFILE_PATH + "/" + metadataConfig;
                ssf.getContext().put(METADATA_INSTANCE_CONFIG,metadataConfig);
            }
            storeService = ssf.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storeService;
    }

    @Bean(name = "foStoreService")
    public StoreService foStoreService() {
        StoreServiceFactory ssf;
        StoreService storeService = null;
        try {
            ssf = new StoreServiceFactory(FO_CMIS);
            String metadataConfig = (String) ssf.getContext().get(METADATA_INSTANCE_CONFIG);
            if (metadataConfig != null && !metadataConfig.isEmpty()) {
                metadataConfig = PROFILE_PATH + "/" + metadataConfig;
                ssf.getContext().put(METADATA_INSTANCE_CONFIG,metadataConfig);
            }
            storeService = ssf.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return storeService;
    }

}
