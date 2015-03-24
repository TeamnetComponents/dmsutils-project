package ro.croco.integration.dms.toolkit.jms;

import org.apache.commons.pool.impl.GenericObjectPool;
import ro.croco.integration.dms.commons.exceptions.TimeoutException;
import ro.croco.integration.dms.toolkit.StoreContext;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Lucian.Dragomir on 7/9/2014.
 */
public class JmsConnectionPool extends GenericObjectPool<JmsConnection> {

    private static final String MAX_ACTIVE = "jms.$communicationType.connection.pool.maxActive";
    private static final String WHEN_EXHAUSTED_ACTION = "jms.$communicationType.connection.pool.whenExhaustedAction";
    private static final String MAX_WAIT = "jms.$communicationType.connection.pool.maxWait";
    private static final String MIN_IDLE = "jms.$communicationType.connection.pool.minIdle";
    private static final String MAX_IDLE = "jms.$communicationType.connection.pool.maxIdle";
    //private static final String MAX_TOTAL = "jms.$communicationType.connection.pool.maxTotal";
    private static final String TEST_ON_BORROW = "jms.$communicationType.connection.pool.testOnBorrow";
    private static final String TEST_ON_RETURN = "jms.$communicationType.connection.pool.testOnReturn";
    private static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "jms.$communicationType.connection.pool.timeBetweenEvictionRunsMillis";
    private static final String NUM_TESTS_PER_EVICTION_RUN = "jms.$communicationType.connection.pool.numTestsPerEvictionRun";
    private static final String MIN_EVICTABLE_IDLE_TIME_MILLIS = "jms.$communicationType.connection.pool.minEvictableIdleTimeMillis";
    private static final String TEST_WHILE_IDLE = "jms.$communicationType.connection.pool.testWhileIdle";

    private long consumerTimeout;
    private StoreContext.COMMUNICATION_TYPE_VALUES communicationType;

    public JmsConnectionPool(JmsConnectionPoolFactory factory) {
        super(factory);

        this.consumerTimeout = Long.parseLong(getProperty(factory, JmsConnectionPoolFactory.CONSUMER_TIMEOUT));
        this.communicationType = factory.getCommunicationType();

        this.setMaxActive(Integer.parseInt(getProperty(factory, MAX_ACTIVE)));
        this.setWhenExhaustedAction(Byte.parseByte(getProperty(factory, WHEN_EXHAUSTED_ACTION)));
        this.setMaxWait(Long.parseLong(getProperty(factory, MAX_WAIT)));
        this.setMinIdle(Integer.parseInt(getProperty(factory, MIN_IDLE)));
        this.setMaxIdle(Integer.parseInt(getProperty(factory, MAX_IDLE)));
        this.setTestOnBorrow(Boolean.parseBoolean(getProperty(factory, TEST_ON_BORROW)));
        this.setTestOnReturn(Boolean.parseBoolean(getProperty(factory, TEST_ON_RETURN)));
        this.setTimeBetweenEvictionRunsMillis(Long.parseLong(getProperty(factory, TIME_BETWEEN_EVICTION_RUNS_MILLIS)));
        this.setNumTestsPerEvictionRun(Integer.parseInt(getProperty(factory, NUM_TESTS_PER_EVICTION_RUN)));
        this.setMinEvictableIdleTimeMillis(Long.parseLong(getProperty(factory, MIN_EVICTABLE_IDLE_TIME_MILLIS)));
        this.setTestWhileIdle(Boolean.parseBoolean(getProperty(factory, TEST_WHILE_IDLE)));
    }

    public String getProperty(JmsConnectionPoolFactory factory, String propertyName) {
        return factory.getProperties().get(propertyName.replace("$communicationType", factory.getCommunicationType().name().toLowerCase())).toString();
    }

    public StoreContext.COMMUNICATION_TYPE_VALUES getCommunicationType() {
        return communicationType;
    }

    public Serializable sendAndReceiveObject(Serializable serializable) {
        Serializable[] serializables = null;
        serializables = sendAndReceiveObjects(new Serializable[]{serializable}, new String[0]);
        return serializables[0];
    }

    public Serializable[] sendAndReceiveObjects(Serializable[] arrSerializable) {
        return sendAndReceiveObjects(arrSerializable, new String[0]);
    }

    public Serializable[] sendAndReceiveObjects(Serializable[] arrSerializable, String[] arrCorelationId) {
        // do parameter validation
//        if (arrSerializable == null || arrCorelationId == null || arrSerializable.length == 0 || arrCorelationId.length == 0 || arrSerializable.length != arrCorelationId.length) {
//            throw new RuntimeException("The serializable and correlationId arrays must be non null and must have the same size.");
//        }
        if (getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS) && arrSerializable.length > 1) {
            throw new RuntimeException("Jms synchronous operations are supported only for singular items.");
        }

        Serializable[] arrSerializableResponse = new Serializable[arrSerializable.length];
        JmsConnection jmsConnection = null;
        Serializable serializable = null;
        String corelationId = null;
        int messageIndex = 0;
        Serializable serializableResponse = null;
        try {
            //System.out.println(" ---------- START GenericObjectPool.borrowObject ------------");
            jmsConnection = this.borrowObject();
            //System.out.println(" ---------- END GenericObjectPool.borrowObject ------------");
        } catch (Exception e) {
            throw new RuntimeException("Unable to get jmsConnection from the connection pool", e);
        }

        for (messageIndex = 0; messageIndex < arrSerializable.length; messageIndex++) {
            serializable = arrSerializable[messageIndex];
            corelationId = null;
            if ((arrCorelationId != null) && (messageIndex < arrCorelationId.length)) {
                corelationId = arrCorelationId[messageIndex];
            }
            serializableResponse = null;

            //send the message
            if (corelationId == null || corelationId.isEmpty()) {
                corelationId = String.valueOf(UUID.randomUUID());
            }
            try {
                Message requestMessage = null;
                requestMessage = jmsConnection.getSession().createObjectMessage(serializable);
                requestMessage.setJMSReplyTo(jmsConnection.getConsumerDestination());
                requestMessage.setJMSCorrelationID(corelationId);
                //System.out.println(" ------------ SEND MSG ------------------");
                jmsConnection.getMessageProducer().send(requestMessage);

                //last message from array commit the batch
                if ((messageIndex == arrSerializable.length - 1)) {
                    jmsConnection.getSession().commit();
                }
                //System.out.println(" ------------ SEND MSG - FINISHED ------------------");
            } catch (JMSException e) {
                try {
                    jmsConnection.getSession().rollback();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    this.invalidateObject(jmsConnection);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    this.returnObject(jmsConnection);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                throw new RuntimeException("Unable to create and send a message request", e);
            }

            //wait for response
            if (getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                Message responseMessage = null;
                try {
                    MessageConsumer messageConsumer = jmsConnection.createConsumer(jmsConnection.isConsumerTemporaryQueue() ? null : "JMSCorrelationID='" + corelationId + "'");
                    do {
                        responseMessage = messageConsumer.receive(consumerTimeout);
                    }
                    while ((responseMessage != null) && (!responseMessage.getJMSCorrelationID().equals(corelationId)));
                    if (responseMessage == null) {
                        throw new TimeoutException("Timeout receiving message response.");
                    }
                    serializableResponse = ((ObjectMessage) responseMessage).getObject();
                    jmsConnection.getSession().commit();
                    arrSerializableResponse[messageIndex] = serializableResponse;
                } catch (JMSException e) {
                    try {
                        jmsConnection.getSession().rollback();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    try {
                        this.invalidateObject(jmsConnection);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    throw new RuntimeException("Unable to retrieve the message response", e);
                }
            } else if (getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
                JmsMessageStructure jmsMessageStructure = new JmsMessageStructure();
                jmsMessageStructure.setParameters(corelationId);
                arrSerializableResponse[messageIndex] = jmsMessageStructure;
            }
        }

        try {
            this.returnObject(jmsConnection);
        } catch (Exception e) {
            throw new RuntimeException("Unable to return the jmsConnection to the connection pool", e);
        }
        return arrSerializableResponse;
    }
}
