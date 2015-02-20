package ro.croco.integration.dms.toolkit.utils.strategy.operation.downloaddocument;

import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentStream;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public class UnversionedDwlDocument extends DownloadDocumentStrategy{

    public UnversionedDwlDocument(StoreServiceSessionImpl_Db session) {
        super(session);
    }

    @Override
    public DocumentStream process(DocumentIdentifier identifier) {
        return null;
    }
}
