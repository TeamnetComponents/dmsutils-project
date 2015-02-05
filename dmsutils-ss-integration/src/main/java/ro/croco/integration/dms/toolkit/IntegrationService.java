package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 1/4/2015.
 */
public interface IntegrationService extends InitableService {

    public void close();

    public StoreServiceMessage[] sendAndReceive(StoreServiceMessage[] messageStructures, StoreContext.COMMUNICATION_TYPE_VALUES communicationType);

    public StoreServiceMessage sendAndReceive(StoreServiceMessage messageStructure, StoreContext.COMMUNICATION_TYPE_VALUES communicationType);

    public void addStoreServiceMessageListener(StoreServiceMessageListener messageListener);

    public void removeStoreServiceMessageListener(StoreServiceMessageListener messageListener);
}
