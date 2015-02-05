package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 1/17/2015.
 */
public interface StoreServiceMessageListener extends java.util.EventListener {
    public void onReceive(StoreServiceMessageEvent storeServiceMessageEvent);
}
