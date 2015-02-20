package ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentInfo;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.SqlOperationTranslator;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */

public abstract class StoreDocumentStrategy extends DocumentOperationStrategy{
    protected DocumentInfo documentInfo;

    public StoreDocumentStrategy(StoreServiceSessionImpl_Db session){
            super(session);
    }

    public abstract DocumentIdentifier process(DocumentInfo documentInfo);

}
