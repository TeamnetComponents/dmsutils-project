package integration.config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import ro.croco.integration.dms.toolkit.db.ContextProperties;
import ro.croco.integration.dms.toolkit.db.QueueConfigurationResolver;

import java.util.Properties;

/**
 * Created by battamir.sugarjav on 3/6/2015.
 */
public class ContextPlaceHolderConfigurer extends PropertyPlaceholderConfigurer {
    @Override
    protected String resolvePlaceholder(String placeholder,Properties context){

        if(placeholder.equals(JdbcContextPlaceHolders.SYNC_REQUEST_QUEUE_TABLE))
            return QueueConfigurationResolver.getTableName(context,context.getProperty(ContextProperties.Required.SERVICE_SYNC_REQUEST_QUEUE));

        if(placeholder.equals(JdbcContextPlaceHolders.ASYNC_REQUEST_QUEUE_TABLE))
            return QueueConfigurationResolver.getTableName(context,context.getProperty(ContextProperties.Required.SERVICE_ASYNC_REQUEST_QUEUE));

        if(placeholder.equals(JdbcContextPlaceHolders.SYNC_QUEUE)){
            return context.getProperty(ContextProperties.Required.SERVICE_SYNC_REQUEST_QUEUE);
        }

        if(placeholder.equals(JdbcContextPlaceHolders.ASYNC_QUEUE))
            return context.getProperty(ContextProperties.Required.SERVICE_ASYNC_REQUEST_QUEUE);

        if(placeholder.equals(JdbcContextPlaceHolders.SYNC_RESPONSE_QUEUE_TABLE))
            return QueueConfigurationResolver.getTableName(context,context.getProperty(ContextProperties.Required.SERVICE_SYNC_RESPONSE_QUEUE));

        if(placeholder.equals(JdbcContextPlaceHolders.ASYNC_RESPONSE_QUEUE_TABLE))
            return QueueConfigurationResolver.getTableName(context,context.getProperty(ContextProperties.Required.SERVICE_ASYNC_RESPONSE_QUEUE));

        throw new RuntimeException("Extra placeHolders found inside context xml file.");
    }
}
