package ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument;

import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.utils.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.DBRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
public class VersionedStoreDocument extends StoreDocumentStrategy {

    public VersionedStoreDocument(StoreServiceSessionImpl_Db session){
        super(session);
    }

    @Override
    public DocumentIdentifier delegatedProcess() throws SQLException{
        String schema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        BigDecimal oldDmObjectRowId = hasPathSpecified() ? DBRepository.getDmObjectsIdByPathAndName(connection,schema,documentInfo.getParentIdentifier().getPath(),documentInfo.getName()) :
                                                           DBRepository.getDmObjectsIdByName(connection,schema,documentInfo.getName());
        BigDecimal dmObjectsRowIdOnIdentifier = null;
        boolean calculateBasedOnPrev = false;

        if(oldDmObjectRowId != null){
            dmObjectsRowIdOnIdentifier = oldDmObjectRowId;
            calculateBasedOnPrev = true;
        }
        else dmObjectsRowIdOnIdentifier = hasPathSpecified() ? DBRepository.createDmObjectsRowWithPathAndName(connection,schema,documentInfo.getParentIdentifier().getPath(),documentInfo.getName()) :
                                                               DBRepository.createDmObjectsRowWithName(connection,schema,documentInfo.getName());

        String newDmStreamsRowId = DBRepository.createDmStreamsRow(connection,schema,(InputStream)additInfo.get("inputStream"));
        String version = calculateNewVersion(calculateBasedOnPrev);

        String nameWithExtension = documentInfo.getName() + (hasExtension() ? "." + documentInfo.getExtension() : "");
        String mimeType = getMIMEType(nameWithExtension);
        BigDecimal newDmVersionsRowId = DBRepository.createDmVersionsRow(connection,schema,nameWithExtension,mimeType,dmObjectsRowIdOnIdentifier,newDmStreamsRowId,version);
        return constructDocumentIdentifier(dmObjectsRowIdOnIdentifier,newDmVersionsRowId,version);
    }
}
