package ro.croco.integration.dms.toolkit.utils.strategy.operation.downloaddocument;

import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentStream;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;

import java.sql.Connection;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public abstract class DownloadDocumentStrategy extends DocumentOperationStrategy{

    protected DocumentIdentifier identifier;

    protected DownloadDocumentStrategy(StoreServiceSessionImpl_Db session){
        super(session);
    }

    public abstract DocumentStream process(DocumentIdentifier identifier);
}
