package ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument;

import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentInfo;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;

import java.util.Map;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
public class VersionedStoreDocument extends StoreDocumentStrategy {

    public VersionedStoreDocument(StoreServiceSessionImpl_Db session){
        super(session);
    }

    @Override
    public DocumentIdentifier process(DocumentInfo documentInfo) {
        return null;
    }
}
