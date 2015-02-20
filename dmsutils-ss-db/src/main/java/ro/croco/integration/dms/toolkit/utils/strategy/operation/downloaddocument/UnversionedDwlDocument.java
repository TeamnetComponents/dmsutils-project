package ro.croco.integration.dms.toolkit.utils.strategy.operation.downloaddocument;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentStream;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.context.ContextProperties;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public class UnversionedDwlDocument extends DownloadDocumentStrategy{

    public UnversionedDwlDocument(StoreServiceSessionImpl_Db session) {
        super(session);
    }

    private PreparedStatement prepareRetrieveStreamCmd() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String retrieveCmd = "select stream_name,mime_type,file_stream from schemaXYZ.dm_object_versions dmov,schemaXYZ.dm_streams dms where dmov.fk_dm_objects = ? and dmov.fk_dm_streams = dms.name order by dms.creation_time desc";
        String schema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        if(schema != null && !schema.equals(""))
            retrieveCmd.replaceAll("schemaXYZ",schema);
        else retrieveCmd.replaceAll("schemaXYZ.","");

        System.out.println("retrieveStreamCmd = " + retrieveCmd);
        return connection.prepareStatement(retrieveCmd, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    public DocumentStream process(DocumentIdentifier identifier) {
        try {
                PreparedStatement retrieveStreamPS = prepareRetrieveStreamCmd();
                retrieveStreamPS.setInt(1,new BigDecimal(identifier.getId()).intValue());
                ResultSet resultSet = retrieveStreamPS.executeQuery();
                resultSet.next();
                DocumentStream documentStream = new DocumentStream(resultSet.getString(1),resultSet.getBinaryStream(2),resultSet.getString(3));
                resultSet.close();
                retrieveStreamPS.close();
                return documentStream;
        }
        catch(Exception ex){
            throw new StoreServiceException(ex);
        }
    }
}
