package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.validation.StoreServicePropValidator;
import ro.croco.integration.dms.toolkit.context.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.deletedocument.UnversionedDelDocument;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.deletedocument.VersionedDelDocument;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.downloaddocument.DownloadDocumentStrategy;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.existsdocument.CheckDocumentStrategy;
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

    private static final String FUNCTION_IDENTIFIER = "[StoreServiceImpl_Db] ";

   @Override
   public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        Map<String,Object> additInfo = new HashMap<String,Object>();
        additInfo.put("inputStream",inputStream);
        additInfo.put("allowCreatePath",allowCreatePath);
        additInfo.put("versioningType",versioningType);

        if(versioningType.equals(VersioningType.NONE))
            return new UnversionedStoreDocument(this.openSession(storeContext)).process(documentInfo,additInfo);

        if(versioningType.equals(VersioningType.MAJOR) || versioningType.equals(VersioningType.MINOR))
            return new VersionedStoreDocument(this.openSession(storeContext)).process(documentInfo,additInfo);

        throw new StoreServiceException(FUNCTION_IDENTIFIER + "Should not have reached this point.Nu tratati toate cazurile de versionare pe functia 'storeDocument'.");
    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return new CheckDocumentStrategy(this.openSession(storeContext)).process(documentIdentifier);
    }

    @Override
    public DocumentStream downloadDocument(StoreContext storeContext,DocumentIdentifier documentIdentifier) {
        return new DownloadDocumentStrategy(this.openSession(storeContext)).process(documentIdentifier);
    }

    @Override
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        return null;
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