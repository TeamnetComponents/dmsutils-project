package integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.messaging.MessageChannel;
import ro.croco.integration.dms.commons.DatabaseUtils;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.toolkit.StoreServiceFactory;
import ro.croco.integration.dms.toolkit.StoreServiceImpl_Cmis;
import ro.croco.integration.dms.toolkit.StoreServiceImpl_Db;
import ro.croco.integration.dms.toolkit.db.ContextProperties;
import ro.croco.integration.dms.toolkit.db.QueueConfigurationResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
@Configuration
@ComponentScan(basePackages = {"integration.service", "integration.config"})
public class JdbcConfig{

    private static final String PROPERTIES_FILE_SUFFIX = ".properties";

    private Properties dbContext;

    private static Properties convertResourceBundleToProperties(ResourceBundle resource) {
        Properties properties = new Properties();
        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            properties.put(key, resource.getString(key));
        }
        return properties;
    }

    private Properties loadConfigFileContext(String contextName){
        FileUtils fileUtils = (contextName.contains(FileUtils.getFileUtilsOS().getPathDelimiter())) ? FileUtils.getFileUtilsOS() : FileUtils.getFileUtilsDMS();
        String pathName = fileUtils.getParentFolderPathName(contextName);
        String contextShortName = fileUtils.getFileName(contextName);
        contextShortName = contextShortName.substring(0,contextShortName.length() - PROPERTIES_FILE_SUFFIX.length());

        List<String> pathList = new ArrayList<String>();
        if(!pathName.equals(fileUtils.getRootPath()))
            pathList.add(pathName);
        else pathList.add(null);

        pathList.add("META-INF");
        pathList.add("WEB-INF/classes");

        Properties context = null;
        for(String path : pathList){
            System.out.println(path);
            try{
                String configurationFilePathName = contextShortName;
                if (path != null)
                    configurationFilePathName = ((path.contains(FileUtils.getFileUtilsOS().getPathDelimiter())) ? FileUtils.getFileUtilsOS() : FileUtils.getFileUtilsDMS()).concatenate(path,contextShortName);
                context = convertResourceBundleToProperties(ResourceBundle.getBundle(configurationFilePathName));
            }
            catch(Exception e){}

            if(context != null)
                return context;
        }

        for(String path : pathList){
            System.out.println(path);
            try{
                String configurationFilePathName = contextShortName;
                if (path != null)
                    configurationFilePathName = ((path.contains(FileUtils.getFileUtilsOS().getPathDelimiter())) ? FileUtils.getFileUtilsOS() : FileUtils.getFileUtilsDMS()).concatenate(path, contextShortName + PROPERTIES_FILE_SUFFIX);
                context = FileUtils.openOsResource(configurationFilePathName);
            }
            catch(IOException e){}

            if(context != null)
                return context;
        }

        if(context == null)
            throw new RuntimeException("Unable to find configuration for context <" + contextShortName + "> .");

        return null;
    }

    public JdbcConfig() throws IOException{
        this.dbContext = loadConfigFileContext("C:\\TeamnetProjects\\DMS-UTILS\\is-db.properties");
    }

    @Bean(name="ss-local")
    public StoreServiceImpl_Db registerSSLocal(){
        try{
            StoreServiceFactory storeServiceFactory = new StoreServiceFactory("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties");
            return (StoreServiceImpl_Db)storeServiceFactory.getService();
        }
        catch(Exception e){
            throw new RuntimeException("Could not initialize ss-local bean.",e);
        }
    }

    @Bean(name="ss-final")
    public StoreServiceImpl_Cmis registerSSCmis(){
        try{
            StoreServiceFactory storeServiceFactory = new StoreServiceFactory("C:\\TeamnetProjects\\DMS-UTILS\\ss-cmis.properties");
            return (StoreServiceImpl_Cmis)storeServiceFactory.getService();
        }
        catch(Exception e){
            throw new RuntimeException("Could not initialize ss-local bean.",e);
        }
    }

    @Bean(name="syncRequestDataSource")
    public DataSource registerSyncRequestDataSource(){
        String connectionName = QueueConfigurationResolver.getConnectionName(this.dbContext,this.dbContext.getProperty(ContextProperties.Required.SERVICE_SYNC_REQUEST_QUEUE));
        return DatabaseUtils.getDataSource(this.dbContext,connectionName);
    }

    @Bean(name="asyncRequestDataSource")
    public DataSource registerAsyncRequestDataSource(){
        String connectionName = QueueConfigurationResolver.getConnectionName(this.dbContext,this.dbContext.getProperty(ContextProperties.Required.SERVICE_ASYNC_REQUEST_QUEUE));
        return DatabaseUtils.getDataSource(this.dbContext,connectionName);
    }

    @Bean(name="syncResponseDataSource")
    public DataSource registerSyncResponseDataSource(){
        String connectionName = QueueConfigurationResolver.getConnectionName(this.dbContext,this.dbContext.getProperty(ContextProperties.Required.SERVICE_SYNC_RESPONSE_QUEUE));
        return DatabaseUtils.getDataSource(this.dbContext,connectionName);
    }

    @Bean(name="asyncResponseDataSource")
    public DataSource registerAsyncResponseDataSource(){
        String connectionName = QueueConfigurationResolver.getConnectionName(this.dbContext,this.dbContext.getProperty(ContextProperties.Required.SERVICE_SYNC_RESPONSE_QUEUE));
        return DatabaseUtils.getDataSource(this.dbContext,connectionName);
    }
}