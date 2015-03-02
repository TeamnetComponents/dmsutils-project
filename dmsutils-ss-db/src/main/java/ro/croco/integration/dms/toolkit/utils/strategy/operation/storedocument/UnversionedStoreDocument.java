package ro.croco.integration.dms.toolkit.utils.strategy.operation.storedocument;

import ro.croco.integration.dms.toolkit.*;
import ro.croco.integration.dms.toolkit.utils.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.DBRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
public class UnversionedStoreDocument extends StoreDocumentStrategy{

    public UnversionedStoreDocument(StoreServiceSessionImpl_Db session){
        super(session);
    }

    @Override
    public DocumentIdentifier delegatedProcess() throws SQLException {
        String schema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        BigDecimal oldDmObjectRowId = DBRepository.getDmObjectsIdByPathAndName(connection,schema,documentInfo.getParentIdentifier().getPath(),documentInfo.getName());
        BigDecimal dmObjectsRowIdOnIdentifier = null;

        if(oldDmObjectRowId != null){
            DBRepository.deleteDmVersionsForObject(connection,schema,oldDmObjectRowId);
            dmObjectsRowIdOnIdentifier = oldDmObjectRowId;
        }
        else dmObjectsRowIdOnIdentifier = DBRepository.createDmObjectsRowWithPathAndName(connection,schema,documentInfo.getParentIdentifier().getPath(),documentInfo.getName());

        String newDmStreamsRowId = DBRepository.createDmStreamsRow(connection, schema, (InputStream)additInfo.get("inputStream"));
        String version = calculateNewVersion(false);
        String nameWithExtension = documentInfo.getName() + (hasExtension() ? "." + documentInfo.getExtension() : "");
        String mimeType = getMIMEType(nameWithExtension);
        BigDecimal newDmVersionsRowId = DBRepository.createDmVersionsRow(connection,schema,nameWithExtension,mimeType,dmObjectsRowIdOnIdentifier,newDmStreamsRowId,version);
        return constructDocumentIdentifier(dmObjectsRowIdOnIdentifier,newDmVersionsRowId,version);
    }
}