package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 1/5/2015.
 */
public abstract class IntegrationServiceImpl_Abstract implements IntegrationService {
    private Properties context;
    private List messageListeners = new ArrayList();

    @Override
    public void __init(Properties context) throws IOException {
        this.context = toProperties(context);
    }

    @Override
    public final Properties getContext() {
        return this.context;
    }

    @Override
    public final Object getContextProperty(String propertyName) {
        return this.context.getProperty(propertyName);
    }

    @Override
    public final String getName() {
        return (String) context.get(ServiceFactory_Abstract.INSTANCE_NAME);
    }

    protected static Properties toProperties(Properties properties) {
        Properties context = new Properties();
        context.putAll(properties);
        return context;
    }

    //asynchronous eventing section
    public final synchronized void addStoreServiceMessageListener(StoreServiceMessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public final synchronized void removeStoreServiceMessageListener(StoreServiceMessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    protected synchronized StoreServiceMessageEvent.StoreServiceMessageProcessStatus fireStoreServiceMessageEvent(StoreServiceMessage storeServiceMessage, boolean requireProcessStatus) {
        StoreServiceMessageEvent.StoreServiceMessageProcessStatus storeServiceMessageProcessStatus = StoreServiceMessageEvent.StoreServiceMessageProcessStatus.SUCCESS;
        StoreServiceMessageEvent messageEvent = new StoreServiceMessageEvent(storeServiceMessage);
        Iterator i = messageListeners.iterator();
        while (i.hasNext()) {
            messageEvent.setStoreServiceMessageProcessStatus(null);
            ((StoreServiceMessageListener) i.next()).onReceive(messageEvent);
            if (requireProcessStatus && messageEvent.getStoreServiceMessageProcessStatus() == null) {
                throw new StoreServiceException("The processing status of the event is required.");
            }
            if (StoreServiceMessageEvent.StoreServiceMessageProcessStatus.FAIL.equals(messageEvent.getStoreServiceMessageProcessStatus())) {
                storeServiceMessageProcessStatus = StoreServiceMessageEvent.StoreServiceMessageProcessStatus.FAIL;
                break;
            }
        }
        return storeServiceMessageProcessStatus;
    }
}
