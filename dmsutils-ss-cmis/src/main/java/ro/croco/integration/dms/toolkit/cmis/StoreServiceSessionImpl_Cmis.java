package ro.croco.integration.dms.toolkit.cmis;

import org.apache.chemistry.opencmis.client.api.Session;
import ro.croco.integration.dms.toolkit.StoreServiceSession;

/**
 * Created by Lucian.Dragomir on 8/24/2014.
 */
public class StoreServiceSessionImpl_Cmis implements StoreServiceSession {

    Session cmisSession = null;

    public StoreServiceSessionImpl_Cmis(Session cmisSession) {
        this.cmisSession = cmisSession;
    }

    @Override
    public void close() {
        try {
            if (cmisSession != null) {
                cmisSession.clear();
            }
        } catch (Exception e) {
            //do nothing
        }
        cmisSession = null;
    }

    public Session getCmisSession() {
        return cmisSession;
    }

    public void setCmisSession(Session cmisSession) {
        this.cmisSession = cmisSession;
    }
}
