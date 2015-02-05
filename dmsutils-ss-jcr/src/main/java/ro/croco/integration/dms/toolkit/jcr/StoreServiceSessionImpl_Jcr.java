package ro.croco.integration.dms.toolkit.jcr;

import org.apache.jackrabbit.core.TransientRepository;
import ro.croco.integration.dms.toolkit.StoreServiceSession;

import javax.jcr.Repository;
import javax.jcr.Session;

/**
 * Created by Lucian.Dragomir on 8/24/2014.
 */
public class StoreServiceSessionImpl_Jcr implements StoreServiceSession {
    Repository jcrRepository = null;
    Session jcrSession = null;


    public StoreServiceSessionImpl_Jcr(Repository jcrRepository, Session jcrSession) {
        this.jcrRepository = jcrRepository;
        this.jcrSession = jcrSession;
    }

    @Override
    public void close() {
        try {
            if (jcrSession != null) {
                if (jcrRepository != null && jcrRepository instanceof TransientRepository) {
                    ((TransientRepository) jcrRepository).loggedOut((org.apache.jackrabbit.core.SessionImpl) jcrSession);
                    ((TransientRepository) jcrRepository).shutdown();
                } else {
                    jcrSession.logout();
                }
            }
        } catch (Exception e) {
            //do nothing
        }
        jcrSession = null;
        jcrRepository = null;
    }

    public Session getJcrSession() {
        return jcrSession;
    }

    public void setJcrSession(Session jcrSession) {
        this.jcrSession = jcrSession;
    }
}
