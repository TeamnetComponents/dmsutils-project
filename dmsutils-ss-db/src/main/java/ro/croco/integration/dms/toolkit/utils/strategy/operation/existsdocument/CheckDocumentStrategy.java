package ro.croco.integration.dms.toolkit.utils.strategy.operation.existsdocument;

import ro.croco.integration.dms.toolkit.BooleanResponse;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;

import java.sql.Connection;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public abstract class CheckDocumentStrategy extends DocumentOperationStrategy{

    public CheckDocumentStrategy(StoreServiceSessionImpl_Db session){
        super(session);
    }

    public abstract BooleanResponse process(DocumentIdentifier identifier);

}
