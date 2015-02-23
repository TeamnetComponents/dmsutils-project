package ro.croco.integration.dms.toolkit.utils.strategy.operation.deletedocument;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.RequestIdentifier;
import ro.croco.integration.dms.toolkit.StoreServiceSessionImpl_Db;
import ro.croco.integration.dms.toolkit.context.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.SqlOperationTranslator;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public class UnversionedDelDocument extends DeleteDocumentStrategy{
    public UnversionedDelDocument(StoreServiceSessionImpl_Db session) {
        super(session);
    }

    private PreparedStatement prepareRetrieveVersions() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String retrieveVersionsCmd = SqlOperationTranslator.translateCommand("s:DM_OBJECT_VERSIONS(FK_DM_STREAMS),(FK_DM_OBJECTS)",SqlOperationTranslator.PREPARED_STATEMENT,dbSchema);
        System.out.println("DmVersionsRetrieveByFkDmObjectsCmd = " + retrieveVersionsCmd);
        return connection.prepareStatement(retrieveVersionsCmd,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
    }

    private PreparedStatement prepareDeleteStream() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String deleteStreamCmd = SqlOperationTranslator.translateCommand("d:DM_STREAMS(NAME)",SqlOperationTranslator.PREPARED_STATEMENT,dbSchema);
        System.out.println("DmStreamsDeleteStreamCmd = " + deleteStreamCmd);
        return connection.prepareStatement(deleteStreamCmd);
    }

    private PreparedStatement prepareDeleteObject() throws Exception{
        String dbSchema = (String)session.getContext().get(ContextProperties.Optional.CONNECTION_SCHEMA);
        String deleteObjectCmd = SqlOperationTranslator.translateCommand("d:DM_OBJECTS(ID)",SqlOperationTranslator.PREPARED_STATEMENT,dbSchema);
        System.out.println("DmObjectsDeleteObjectCmd = " + deleteObjectCmd);
        return connection.prepareStatement(deleteObjectCmd);
    }

    @Override
    public RequestIdentifier process(DocumentIdentifier identifier){
        try{
            PreparedStatement retrieveVersionsPS = this.prepareRetrieveVersions();
            PreparedStatement deleteStreamPS = this.prepareDeleteStream();
            PreparedStatement deleteObjectPS = this.prepareDeleteObject();

            retrieveVersionsPS.setInt(1,new BigDecimal(identifier.getId()).intValue());
            ResultSet resultSet = retrieveVersionsPS.executeQuery();

            while(resultSet.next()){
                deleteStreamPS.setObject(1,resultSet.getObject(1));
                deleteStreamPS.addBatch();
                resultSet.deleteRow();
            }

            deleteStreamPS.executeBatch();
            deleteObjectPS.setInt(1,new BigDecimal(identifier.getId()).intValue());
            deleteObjectPS.execute();
            resultSet.close();
            retrieveVersionsPS.close();
            deleteStreamPS.close();
            deleteObjectPS.close();

            return null;
        }
        catch(Exception ex){
            try{
                connection.rollback();
                throw new StoreServiceException(ex);
            }
            catch (SQLException rollbackEx){
                throw new StoreServiceException(rollbackEx);
            }
        }
    }
}