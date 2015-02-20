package ro.croco.integration.dms.toolkit.utils.strategy.operation.existsdocument;

import ro.croco.integration.dms.toolkit.BooleanResponse;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */

public class VersionedCheckDocument extends CheckDocumentStrategy{

    public VersionedCheckDocument(StoreServiceSessionImpl_Db session){
        super(session);
    }

    @Override
    public BooleanResponse process(DocumentIdentifier identifier) {
        return null;
    }
}
