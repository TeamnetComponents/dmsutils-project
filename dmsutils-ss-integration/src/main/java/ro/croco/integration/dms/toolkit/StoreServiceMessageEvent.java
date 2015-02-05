package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 1/17/2015.
 */
public class StoreServiceMessageEvent extends java.util.EventObject {
    public static enum StoreServiceMessageProcessStatus {
        SUCCESS,
        FAIL
    }

    private StoreServiceMessageProcessStatus storeServiceMessageProcessStatus;
    private boolean requireProcessStatus;

    public StoreServiceMessageEvent(StoreServiceMessage storeServiceMessage) {
        this(storeServiceMessage, false);
    }

    public StoreServiceMessageEvent(StoreServiceMessage storeServiceMessage, boolean requireProcessStatus) {
        super(storeServiceMessage);
        storeServiceMessageProcessStatus = null;
        requireProcessStatus = false;
    }


    @Override
    public StoreServiceMessage getSource() {
        return (StoreServiceMessage) super.getSource();
    }

    public StoreServiceMessageProcessStatus getStoreServiceMessageProcessStatus() {
        return storeServiceMessageProcessStatus;
    }

    public void setStoreServiceMessageProcessStatus(StoreServiceMessageProcessStatus storeServiceMessageProcessStatus) {
        this.storeServiceMessageProcessStatus = storeServiceMessageProcessStatus;
    }

    public boolean isRequireProcessStatus() {
        return requireProcessStatus;
    }
}
