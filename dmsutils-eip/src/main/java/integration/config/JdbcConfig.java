package integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ro.croco.integration.dms.commons.DatabaseUtils;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.toolkit.db.ContextProperties;
import ro.croco.integration.dms.toolkit.db.QueueConfigurationResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Razvan.Ionescu on 3/2/2015.
 */
@Configuration
@ComponentScan(basePackages = {"ro.croco.integration.service", "ro.croco.integration.config"})
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
        contextShortName = contextShortName.substring(0, contextShortName.length() - PROPERTIES_FILE_SUFFIX.length());

        List<String> pathList = new ArrayList<String>();
        if(!pathName.equals(fileUtils.getRootPath()))
            pathList.add(pathName);
        else pathList.add(null);

        pathList.add("META-INF");
        pathList.add("WEB-INF/classes");

        Properties context = null;
        for(String path : pathList){
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

    @Bean
    public JdbcTemplate jdbcTemplate(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        return jdbcTemplate;
    }

    @Bean(name="syncRequestDataSource")
    public DataSource registerSnycRequestDataSource(){
        return DatabaseUtils.getDataSource(this.dbContext,QueueConfigurationResolver.getConnectionName(this.dbContext,ContextProperties.Required.SERVICE_SYNC_REQUEST_QUEUE));
    }

    @Bean(name="syncResponseDataSource")
    public DataSource registerSyncResponseDataSource(){
        return DatabaseUtils.getDataSource(this.dbContext,QueueConfigurationResolver.getConnectionName(this.dbContext,ContextProperties.Required.SERVICE_SYNC_RESPONSE_QUEUE));
    }
}
