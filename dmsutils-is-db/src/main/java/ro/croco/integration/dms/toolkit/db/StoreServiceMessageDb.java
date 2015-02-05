package ro.croco.integration.dms.toolkit.db;

import ro.croco.integration.dms.toolkit.StoreServiceMessage;
import ro.croco.integration.dms.toolkit.StoreServiceMessageType;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Lucian.Dragomir on 1/17/2015.
 */
public class StoreServiceMessageDb  {
    private StoreServiceMessage storeServiceMessage;

    private String messageID;
    private String messageCorrelationID;
    private String messageDestination;
    private String messageReplyTo;
    private long messageExpiration;
    private int messagePriority;

    public StoreServiceMessageDb() {
        this.storeServiceMessage = null;
        this.messageID = String.valueOf(UUID.randomUUID());
        this.messageCorrelationID = null;
        this.messageDestination = null;
        this.messageReplyTo = null;
        this.messageExpiration = 0;
        this.messagePriority = 4;
    }

    public StoreServiceMessageDb(StoreServiceMessage storeServiceMessage,
                                 String messageID, String messageCorrelationID, String messageDestination, String messageReplyTo,
                                 long messageExpiration, int messagePriority) {
        this.storeServiceMessage = storeServiceMessage;
        this.messageID = messageID;
        this.messageCorrelationID = messageCorrelationID;
        this.messageDestination = messageDestination;
        this.messageReplyTo = messageReplyTo;
        this.messageExpiration = messageExpiration;
        this.messagePriority = messagePriority;
    }

    public StoreServiceMessage getStoreServiceMessage() {
        return storeServiceMessage;
    }

    public void setStoreServiceMessage(StoreServiceMessage storeServiceMessage) {
        this.storeServiceMessage = storeServiceMessage;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessageCorrelationID() {
        return messageCorrelationID;
    }

    public void setMessageCorrelationID(String messageCorrelationID) {
        this.messageCorrelationID = messageCorrelationID;
    }

    public String getMessageDestination() {
        return messageDestination;
    }

    public void setMessageDestination(String messageDestination) {
        this.messageDestination = messageDestination;
    }

    public String getMessageReplyTo() {
        return messageReplyTo;
    }

    public void setMessageReplyTo(String messageReplyTo) {
        this.messageReplyTo = messageReplyTo;
    }

    public long getMessageExpiration() {
        return messageExpiration;
    }

    public void setMessageExpiration(long messageExpiration) {
        this.messageExpiration = messageExpiration;
    }

    public int getMessagePriority() {
        return messagePriority;
    }

    public void setMessagePriority(int messagePriority) {
        this.messagePriority = messagePriority;
    }
}
