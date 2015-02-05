package ro.croco.integration.dms.toolkit.jms;

import javax.jms.*;

/**
 * Created by Lucian.Dragomir on 7/9/2014.
 */
public class JmsConnection {
    private Connection connection;
    private Session session;
    private MessageProducer messageProducer;
    private Destination producerDestination;
    private MessageConsumer messageConsumer;
    private Destination consumerDestination;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public MessageProducer getMessageProducer() {
        return messageProducer;
    }

    public void setMessageProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public Destination getProducerDestination() {
        return producerDestination;
    }

    public void setProducerDestination(Destination producerDestination) {
        this.producerDestination = producerDestination;
    }

    public Destination getConsumerDestination() {
        return consumerDestination;
    }

    public void setConsumerDestination(Destination consumerDestination) {
        this.consumerDestination = consumerDestination;
    }

    public MessageConsumer createConsumer(String filter) throws JMSException {
        closeConsumer();
        this.messageConsumer = session.createConsumer(this.consumerDestination, filter);
        return this.messageConsumer;
    }

    public boolean isConsumerTemporaryQueue() {
        return this.messageConsumer instanceof TemporaryQueue;
    }

    private void closeConsumer() {
        try {
            if (this.messageConsumer != null) {
                this.messageConsumer.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            this.messageConsumer = null;
        }
    }

    private void closeProducer() {
        try {
            if (this.messageProducer != null) {
                this.messageProducer.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            this.messageProducer = null;
        }
    }

    private void closeSession() {
        try {
            if (this.session != null) {
                this.session.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            this.session = null;
        }
    }

    private void closeConnection() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            this.connection = null;
        }
    }

    public void close() {
        closeConsumer();
        closeProducer();
        closeSession();
        closeConnection();
    }
}
