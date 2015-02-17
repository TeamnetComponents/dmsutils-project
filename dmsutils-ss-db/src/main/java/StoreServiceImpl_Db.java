import ro.croco.integration.dms.toolkit.*;

import java.io.InputStream;

/**
 * Created by Lucian.Dragomir on 2/17/2015.
 */
public class StoreServiceImpl_Db extends StoreServiceImpl_Abstract {


    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        return super.storeDocument(storeContext, documentInfo, inputStream, allowCreatePath, versioningType);
    }

    @Override
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return super.deleteDocument(storeContext, documentIdentifier);
    }

    @Override
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return super.downloadDocument(storeContext, documentIdentifier);
    }

    @Override
    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo, boolean createParentIfNotExists) {
        return super.createFolder(storeContext, folderInfo, createParentIfNotExists);
    }

    @Override
    public RequestIdentifier deleteFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        return super.deleteFolder(storeContext, folderIdentifier);
    }

    @Override
    public BooleanResponse existsFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        return super.existsFolder(storeContext, folderIdentifier);
    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return super.existsDocument(storeContext, documentIdentifier);
    }

    @Override
    protected StoreServiceSession openSession(StoreContext storeContext) {
        return null;
    }

    @Override
    protected void closeSession(StoreServiceSession storeSession) {

    }

    @Override
    protected ObjectInfo[] listFolderContent(StoreServiceSession storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        return new ObjectInfo[0];
    }

    @Override
    protected FolderInfo getFolderInfo(StoreServiceSession storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier) {
        return null;
    }
}
