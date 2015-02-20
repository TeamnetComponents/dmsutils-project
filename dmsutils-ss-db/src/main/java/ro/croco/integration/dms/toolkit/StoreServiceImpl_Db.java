package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.validation.StoreServicePropValidator;
import ro.croco.integration.dms.toolkit.context.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument.StoreDocumentStrategy;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument.UnversionedStoreDocument;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument.VersionedStoreDocument;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Lucian.Dragomir on 2/17/2015.
 */
public class StoreServiceImpl_Db extends StoreServiceImpl_Abstract<StoreServiceSessionImpl_Db> {

    private StoreServicePropValidator validator = new StoreServicePropValidator(new ContextProperties.Required());

    @Override
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {

        return null;
    }

    @Override
    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo, boolean createParentIfNotExists) {
        return super.createFolder(storeContext, folderInfo, createParentIfNotExists);
    }

    private void storeDocumentUnversioned(){

    }

    private void fillStoreDocDocumentInfo(DocumentInfo documentInfo,InputStream inputStream,boolean allowCreatePath){
        if(documentInfo.getProperties() == null)
            documentInfo.setProperties(new HashMap<String, Object>());

        documentInfo.getProperties().put("inputStream",inputStream);
        documentInfo.getProperties().put("allowCreatePath",allowCreatePath);
    }

    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        fillStoreDocDocumentInfo(documentInfo,inputStream,allowCreatePath);
        StoreDocumentStrategy processor = null;

        if(versioningType.equals(VersioningType.NONE)){
            processor = new UnversionedStoreDocument(this.openSession(storeContext));
        }
        else
        if(versioningType.equals(VersioningType.MAJOR) || versioningType.equals(VersioningType.MINOR)){
            processor = new VersionedStoreDocument(this.openSession(storeContext));
        }
        else throw new StoreServiceException("Nu tratati toate cazurile de versionare pe functia 'storeDocument'");

        return processor.process(documentInfo);
    }

    @Override
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return super.deleteDocument(storeContext, documentIdentifier);
    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return super.existsDocument(storeContext, documentIdentifier);
    }

    @Override
    public void __init(Properties context) throws IOException {
        super.__init(context);
        validator.validate(context);
    }

    @Override
    public StoreServiceSessionImpl_Db openSession(StoreContext storeContext) {
        return new StoreServiceSessionImpl_Db(this.getContext(),storeContext);
    }

    @Override
    protected void closeSession(StoreServiceSessionImpl_Db storeSession) {
        if (storeSession != null)
            storeSession.close();
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
