package ro.croco.integration.dms.toolkit;


import ro.croco.integration.dms.toolkit.db.StoreServiceSessionImpl_Db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 2/17/2015.
 */
public class StoreServiceImpl_Db extends StoreServiceImpl_Abstract<StoreServiceSessionImpl_Db> {
    private static final String JDBC_SCHEMA = "jdbc.schema";


    @Override
    public void __init(Properties context) throws IOException {
        super.__init(context);
    }

    @Override
    public StoreServiceSessionImpl_Db openSession(StoreContext storeContext) {
        StoreServiceSessionImpl_Db storeSession = null;
        storeSession = new StoreServiceSessionImpl_Db(storeContext, this.getContext());
        return storeSession;
    }

    @Override
    protected void closeSession(StoreServiceSessionImpl_Db storeSession) {
        if (storeSession != null) {
            storeSession.close();
        }
        storeSession = null;
    }

    @Override
    protected ObjectInfo[] listFolderContent(StoreServiceSessionImpl_Db storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        return new ObjectInfo[0];
    }

    @Override
    protected FolderInfo getFolderInfo(StoreServiceSessionImpl_Db storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier) {
        return null;
    }
}
