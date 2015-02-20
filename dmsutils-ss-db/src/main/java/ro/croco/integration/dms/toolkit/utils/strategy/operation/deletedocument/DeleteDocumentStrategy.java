package ro.croco.integration.dms.toolkit.utils.strategy.operation.deletedocument;

import org.omg.CORBA.Request;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.RequestIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.strategy.operation.DocumentOperationStrategy;

import java.sql.Connection;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public abstract class DeleteDocumentStrategy extends DocumentOperationStrategy{

    public DeleteDocumentStrategy(StoreServiceSessionImpl_Db session){
        super(session);
    }

    protected DocumentIdentifier identifier;

    public abstract RequestIdentifier process(DocumentIdentifier identifier);
}
