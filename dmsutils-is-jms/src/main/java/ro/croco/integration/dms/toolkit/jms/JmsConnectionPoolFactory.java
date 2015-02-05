package ro.croco.integration.dms.toolkit.jms;

import org.apache.commons.pool.PoolableObjectFactory;
import ro.croco.integration.dms.toolkit.StoreContext;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 7/9/2014.
 */
public class JmsConnectionPoolFactory implements PoolableObjectFactory<JmsConnection> {
    static boolean TRANSACTED_SESSION = true;

    static String CONNECTION_FACTORY = "jms.queue.connection.factory";

    static String PRODUCER_QUEUE = "jms.$communicationType.queue.producer";
    static String PRODUCER_TIMEOUT = "jms.$communicationType.queue.producer.timeout";
    static String PRODUCER_DELIVERY_MODE = "jms.$communicationType.queue.producer.deliveryMode";
    static String PRODUCER_PRIORITY = "jms.$communicationType.queue.producer.priority";
    static String PRODUCER_TIME_TO_LIVE = "jms.$communicationType.queue.producer.timeToLive";

    static String CONSUMER_QUEUE = "jms.$communicationType.queue.consumer";
    static String CONSUMER_TIMEOUT = "jms.$communicationType.queue.consumer.timeout";

    static String SESSION_ACKNOLEDGE = "jms.$communicationType.session.acknoledge";

    //public static String COMMUNICATION_TYPE = "jms.communicationType";

    private Properties properties;
    private StoreContext.COMMUNICATION_TYPE_VALUES communicationType;


    public JmsConnectionPoolFactory(Properties properties, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        this.properties = properties;
        this.communicationType = communicationType;
    }

    /*
    public JmsConnectionPoolFactory(Properties properties) {
        this.properties = properties;
        this.communicationType = (StoreContext.COMMUNICATION_TYPE_VALUES) this.properties.get(COMMUNICATION_TYPE);
    }
    */

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key.replace("$communicationType", this.communicationType.name().toLowerCase()), null);
    }

    public StoreContext.COMMUNICATION_TYPE_VALUES getCommunicationType() {
        return this.communicationType;
    }

    @Override
    public JmsConnection makeObject() throws Exception {
        JmsConnection jmsConnection = new JmsConnection();

        //Look up factory & destination.");
        Properties environment = new Properties();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, this.properties.getProperty(Context.INITIAL_CONTEXT_FACTORY));
        environment.put(Context.PROVIDER_URL, this.properties.getProperty(Context.PROVIDER_URL));
        Context initialContext = new InitialContext(environment);

        //producer setup
        ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup(this.properties.getProperty(CONNECTION_FACTORY));
        Connection connection = null;
        if (this.properties.getProperty(Context.SECURITY_PRINCIPAL) != null && !this.properties.getProperty(Context.SECURITY_PRINCIPAL).isEmpty()) {
            connection = connectionFactory.createConnection(
                    this.properties.getProperty(Context.SECURITY_PRINCIPAL),
                    this.properties.getProperty(Context.SECURITY_CREDENTIALS));
        } else {
            connection = connectionFactory.createConnection();
        }
        connection.start();

        //session
        Session session = connection.createSession(TRANSACTED_SESSION, Integer.parseInt(getProperty(SESSION_ACKNOLEDGE)));

        //producer
        Destination producerDestination = (Destination) initialContext.lookup(getProperty(PRODUCER_QUEUE));
        MessageProducer producer = session.createProducer(producerDestination);
        producer.setDeliveryMode(Integer.parseInt(getProperty(JmsConnectionPoolFactory.PRODUCER_DELIVERY_MODE)));
        producer.setPriority(Integer.parseInt(getProperty(JmsConnectionPoolFactory.PRODUCER_PRIORITY)));
        producer.setTimeToLive(Long.parseLong(getProperty(JmsConnectionPoolFactory.PRODUCER_TIME_TO_LIVE)));


        //consumer
        Destination consumerDestination = null;
        if (getProperty(CONSUMER_QUEUE).isEmpty()) {
            consumerDestination = session.createTemporaryQueue();
        } else {
            consumerDestination = (Destination) initialContext.lookup(getProperty(CONSUMER_QUEUE));
        }

        initialContext.close();

        //set jmsConnection object properties
        jmsConnection.setSession(session);
        jmsConnection.setProducerDestination(producerDestination);
        jmsConnection.setMessageProducer(producer);
        jmsConnection.setConsumerDestination(consumerDestination);

        return jmsConnection;
    }

    @Override
    public void destroyObject(JmsConnection jmsConnection) throws Exception {
        jmsConnection.close();
    }

    @Override
    public boolean validateObject(JmsConnection jmsConnection) {
        return false;
    }

    @Override
    public void activateObject(JmsConnection jmsConnection) throws Exception {
    }

    @Override
    public void passivateObject(JmsConnection jmsConnection) throws Exception {
    }
}
