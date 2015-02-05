package ro.croco.integration.dms.toolkit.jms;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.IntegrationServiceImpl_Abstract;
import ro.croco.integration.dms.toolkit.StoreContext;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 1/4/2015.
 */
public class IntegrationServiceImpl_Jms extends IntegrationServiceImpl_Abstract {
    private Properties properties;
    private JmsConnectionPool jmsSynchronousConnectionPool;
    private JmsConnectionPool jmsAsynchronousConnectionPool;

    @Override
    public void __init(Properties context) throws IOException {
        super.__init(context);
    }

    @Override
    public void close() {
        if (jmsSynchronousConnectionPool != null) {
            try {
                jmsSynchronousConnectionPool.close();
            } catch (Exception e) {
                e.printStackTrace();
                jmsSynchronousConnectionPool = null;
            }
        }

        if (jmsAsynchronousConnectionPool != null) {
            try {
                jmsAsynchronousConnectionPool.close();
            } catch (Exception e) {
                e.printStackTrace();
                jmsAsynchronousConnectionPool = null;
            }
        }
    }


    private JmsConnectionPool getConnectionPool(StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        JmsConnectionPool jmsConnectionPool = null;

        //synchronous pool (with initialization if required)
        if (StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS.equals(communicationType)) {
            if (this.jmsSynchronousConnectionPool == null) {
                synchronized (this) {
                    if (this.jmsSynchronousConnectionPool == null) {
                        JmsConnectionPoolFactory jmsConnectionPoolFactory = new JmsConnectionPoolFactory(IntegrationServiceImpl_Abstract.toProperties(this.getContext()), communicationType);
                        this.jmsSynchronousConnectionPool = new JmsConnectionPool(jmsConnectionPoolFactory);
                    }
                }
            }
            jmsConnectionPool = this.jmsSynchronousConnectionPool;
        }

        //asynchonous pool (with initialization if required)
        if (StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS.equals(communicationType)) {
            if (this.jmsAsynchronousConnectionPool == null) {
                synchronized (this) {
                    if (this.jmsAsynchronousConnectionPool == null) {
                        JmsConnectionPoolFactory jmsConnectionPoolFactory = new JmsConnectionPoolFactory(IntegrationServiceImpl_Abstract.toProperties(this.getContext()), communicationType);
                        this.jmsAsynchronousConnectionPool = new JmsConnectionPool(jmsConnectionPoolFactory);
                    }
                }
            }
            jmsConnectionPool = this.jmsAsynchronousConnectionPool;
        }

        //synchronous_local not supported
        if (StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL.equals(communicationType)) {
            throw new StoreServiceException("The JMS store service does not support SYNCHRONOUS_LOCAL communication types");
        }

        return jmsConnectionPool;
    }

    @Override
    public StoreServiceMessage[] sendAndReceive(StoreServiceMessage[] messageStructures, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        JmsConnectionPool jmsConnectionPool = getConnectionPool(communicationType);
        Serializable[] arrSerializable = jmsConnectionPool.sendAndReceiveObjects(messageStructures);
        StoreServiceMessage[] jmsMessageStructures = new StoreServiceMessage[arrSerializable.length];
        for (int index = 0; index < arrSerializable.length; index++) {
            jmsMessageStructures[index] = (StoreServiceMessage) arrSerializable[index];
        }
        return jmsMessageStructures;
    }

    @Override
    public StoreServiceMessage sendAndReceive(StoreServiceMessage messageStructure, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        StoreServiceMessage[] messageStructureResponses = sendAndReceive(new StoreServiceMessage[]{messageStructure}, communicationType);
        return messageStructureResponses[0];
    }
}
