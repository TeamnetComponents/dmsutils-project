package ro.croco.integration.dms.toolkit.cmis.elo;


import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.cmis.server.elo.commons.EloCmisContextParameter;
import ro.croco.integration.dms.toolkit.StoreContext;
import ro.croco.integration.dms.toolkit.cmis.CmisStoreContextTranslator;

import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 1/3/2015.
 */
public class EloCmisStoreContextTranslator extends CmisStoreContextTranslator {

    public EloCmisStoreContextTranslator() {
        super();
    }

    @Override
    public Properties translate(Properties globalContext, StoreContext storeContext) {
        Properties properties = toProperties(globalContext);
        if (storeContext != null) {
            //identify authentication type
            StoreContext.AUTHENTICATION_TYPE_VALUES authenticationType = storeContext.getAuthenticationType();
            if (authenticationType.equals(StoreContext.AUTHENTICATION_TYPE_VALUES.DEFAULT)) {
                //do nothing
            } else if (authenticationType.equals(StoreContext.AUTHENTICATION_TYPE_VALUES.BASIC)) {
                properties.put(EloCmisContextParameter.AUTHENTICATION_TYPE, StoreContext.AUTHENTICATION_TYPE_VALUES.BASIC.name());
                properties.put(SessionParameter.USER, storeContext.get(StoreContext.USER));
                properties.put(SessionParameter.PASSWORD, storeContext.get(StoreContext.PASSWORD));
            } else if (authenticationType.equals(StoreContext.AUTHENTICATION_TYPE_VALUES.BASIC_AS)) {
                properties.put(EloCmisContextParameter.AUTHENTICATION_TYPE, StoreContext.AUTHENTICATION_TYPE_VALUES.BASIC_AS.name());
                properties.put(SessionParameter.USER, storeContext.get(StoreContext.USER));
                properties.put(SessionParameter.PASSWORD, storeContext.get(StoreContext.PASSWORD));
                properties.put(EloCmisContextParameter.USER_AS, storeContext.get(storeContext.USER_AS));
            } else if (authenticationType.equals(StoreContext.AUTHENTICATION_TYPE_VALUES.AS)) {
                properties.put(EloCmisContextParameter.AUTHENTICATION_TYPE, StoreContext.AUTHENTICATION_TYPE_VALUES.BASIC_AS.name());
                properties.put(EloCmisContextParameter.USER_AS, storeContext.get(storeContext.USER_AS));
            }
        }
        return properties;
    }
}
