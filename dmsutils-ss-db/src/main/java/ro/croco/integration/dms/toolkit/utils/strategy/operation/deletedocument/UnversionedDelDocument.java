package ro.croco.integration.dms.toolkit.utils.strategy.operation.deletedocument;

import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.RequestIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public class UnversionedDelDocument extends DeleteDocumentStrategy{
    public UnversionedDelDocument(StoreServiceSessionImpl_Db session) {
        super(session);
    }

    @Override
    public RequestIdentifier process(DocumentIdentifier identifier) {
        return null;
    }
}