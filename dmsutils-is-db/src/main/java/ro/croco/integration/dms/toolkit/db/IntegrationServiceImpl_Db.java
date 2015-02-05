package ro.croco.integration.dms.toolkit.db;
import ro.croco.integration.dms.toolkit.IntegrationServiceImpl_Abstract;
import ro.croco.integration.dms.toolkit.StoreContext;
import ro.croco.integration.dms.toolkit.StoreServiceMessage;

/**
 * Created by Lucian.Dragomir on 1/17/2015.
 */
public class IntegrationServiceImpl_Db extends IntegrationServiceImpl_Abstract {

    @Override
    public void close() {

    }

    @Override
    public StoreServiceMessage[] sendAndReceive(StoreServiceMessage[] messageStructures, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        return new StoreServiceMessage[0];
    }

    @Override
    public StoreServiceMessage sendAndReceive(StoreServiceMessage messageStructure, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        return null;
    }
}
