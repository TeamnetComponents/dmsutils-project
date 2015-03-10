package ro.croco.integration.dms.toolkit.utils;

import org.apache.commons.compress.utils.IOUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.DocumentStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by battamir.sugarjav on 2/27/2015.
 */
public class DBRepository {

    //getDmObjectsIdByPath
    public static BigDecimal getDmObjectsIdByName(Connection connection,String schema,String name) throws SQLException {
        PreparedStatement statement = StatementPreparator.prepareSelectDmObjectByName(connection,schema,name);
        ResultSet resultSet = statement.executeQuery();
        BigDecimal dmObjectsRowId = null;

        if(resultSet.next())
            dmObjectsRowId = new BigDecimal(resultSet.getString(1));

        resultSet.close();
        statement.close();
        return dmObjectsRowId;
    }

    public static DocumentStream getDocumentStreamByDmVersionsId(Connection connection,String schema,BigDecimal dmVersionsId) throws SQLException{
        String command = "SELECT FILE_STREAM,STREAM_NAME,MIME_TYPE FROM TEMP_SCHEMA.DM_STREAMS DS,TEMP_SCHEMA.DM_OBJECT_VERSIONS DOV WHERE DS.NAME=DOV.FK_DM_STREAMS AND DOV.ID=?";
        if(schema != null && !schema.isEmpty())
            command = command.replaceAll("TEMP_SCHEMA",schema);
        else command = command.replaceAll("TEMP_SCHEMA.","");

        PreparedStatement statement = connection.prepareStatement(command);
        statement.setInt(1,dmVersionsId.intValue());

        ResultSet resultSet = statement.executeQuery();
        DocumentStream documentStream = null;

        if(resultSet.next()){
            documentStream = new DocumentStream();

            byte[] array = new byte[0];
            try {
                array = IOUtils.toByteArray(resultSet.getBinaryStream(1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(array.length);
            documentStream.setInputStream(new ByteArrayInputStream(array));

            System.out.println(documentStream.getInputStream());

            documentStream.setFileName(resultSet.getString(2));
            documentStream.setMimeType(resultSet.getString(3));
        }
        resultSet.close();
        statement.close();
        return documentStream;
    }

    public static DocumentStream getDocumenStreamByIdProvidedOnRest(Connection connection,String schema,Object streamIdentifier,String streamName,String mimeType)throws SQLException{
        PreparedStatement statement = StatementPreparator.prepareSelectDmStreamsByIdentifier(connection,schema,streamIdentifier);

        ResultSet resultSet = statement.executeQuery();
        DocumentStream documentStream = null;

        if(resultSet.next()){
            documentStream = new DocumentStream();
            try {
                byte[] array = IOUtils.toByteArray(resultSet.getBinaryStream(1));
                System.out.println(array.length);
                documentStream.setInputStream(new ByteArrayInputStream(array));

                System.out.println(documentStream.getInputStream());
            } catch (IOException ioEx) {
                throw new StoreServiceException(ioEx);
            }
            documentStream.setFileName(streamName);
            documentStream.setMimeType(mimeType);
        }

        resultSet.close();
        statement.close();
        return documentStream;
    }

    //getDmObjectsIdByPath
    public static BigDecimal getDmObjectsIdByPathAndName(Connection connection,String schema,String path,String name) throws SQLException {
        PreparedStatement statement = StatementPreparator.prepareSelectDmObjectByPathAndName(connection,schema,path,name);
        ResultSet resultSet = statement.executeQuery();
        BigDecimal dmObjectsRowId = null;

        if(resultSet.next())
            dmObjectsRowId = new BigDecimal(resultSet.getString(1));

        resultSet.close();
        statement.close();
        return dmObjectsRowId;
    }

    public static String getLastVersionLabelForDmObject(Connection connection,String schema,BigDecimal dmObjectId)throws SQLException{
        String lastDmVersionsCmd = "SELECT MAX(VERSION_LABEL) FROM TEMP_SCHEMA.DM_OBJECT_VERSIONS WHERE FK_DM_OBJECTS=?";
        if(schema != null && !schema.isEmpty())
            lastDmVersionsCmd = lastDmVersionsCmd.replaceAll("TEMP_SCHEMA",schema);
        else lastDmVersionsCmd = lastDmVersionsCmd.replaceAll("TEMP_SCHEMA.","");

        PreparedStatement statement = connection.prepareStatement(lastDmVersionsCmd);
        statement.setObject(1,dmObjectId.intValue());
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String lastVersion = resultSet.getString(1);
        resultSet.close();
        statement.close();
        return lastVersion;
    }

    public static void deleteLastVersionAndStreamForDmObject(Connection connection,String schema,BigDecimal dmObjectId)throws SQLException{
        String lastDmVersionsCmd = "SELECT FK_DM_STREAMS FROM TEMP_SCHEMA.DM_OBJECT_VERSIONS WHERE VERSION_LABEL=(SELECT MAX(VERSION_LABEL) FROM TEMP_SCHEMA.DM_OBJECT_VERSIONS WHERE FK_DM_OBJECTS=?)";
        if(schema != null && !schema.isEmpty())
            lastDmVersionsCmd = lastDmVersionsCmd.replaceAll("TEMP_SCHEMA",schema);
        else lastDmVersionsCmd = lastDmVersionsCmd.replaceAll("TEMP_SCHEMA.","");

        PreparedStatement statement = connection.prepareStatement(lastDmVersionsCmd,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        statement.setObject(1,dmObjectId.intValue());
        statement.setObject(2,dmObjectId.intValue());
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()){
            Object streamIdentifier = resultSet.getObject(1);
            resultSet.deleteRow();
            PreparedStatement dStatement = StatementPreparator.prepareDeleteDmStreamsByIdentifier(connection,schema,streamIdentifier);
            dStatement.execute();
            dStatement.close();
        }
        resultSet.close();
        statement.close();
    }

    public static Map<String,Object> getLastDmVersionsForDmObject(Connection connection,String schema,BigDecimal dmObjectId)throws SQLException{
        Map<String,Object> retData = null;

        String dmVersionsQueryCmd = "SELECT FK_DM_STREAMS,STREAM_NAME,MIME_TYPE FROM TEMP_SCHEMA.DM_OBJECT_VERSIONS WHERE ID=(SELECT MAX(ID) FROM TEMP_SCHEMA.DM_OBJECT_VERSIONS WHERE FK_DM_OBJECTS=?)";
        if(schema != null && !schema.isEmpty())
            dmVersionsQueryCmd = dmVersionsQueryCmd.replaceAll("TEMP_SCHEMA",schema);
        else dmVersionsQueryCmd = dmVersionsQueryCmd.replaceAll("TEMP_SCHEMA.","");

        PreparedStatement statement = connection.prepareStatement(dmVersionsQueryCmd);
        statement.setInt(1,dmObjectId.intValue());

        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()){
            retData = new HashMap<String, Object>();
            retData.put("FK_DM_STREAMS",resultSet.getObject(1));
            retData.put("STREAM_NAME",resultSet.getString(2));
            retData.put("MIME_TYPE",resultSet.getString(3));
        }
        resultSet.close();
        statement.close();
        return retData;
    }

    public static boolean checkExistsDmVersionsByIdAndFkDmObjects(Connection connection,String schema,BigDecimal dmVersionsId,BigDecimal fkDmObjectsId)throws SQLException{
        PreparedStatement statement = StatementPreparator.prepareSelectDmVersionsByIdAndFkDmObjects(connection,schema,dmVersionsId,fkDmObjectsId);
        ResultSet resultSet = statement.executeQuery();
        boolean exists = false;
        if(resultSet.next())
            exists = true;
        resultSet.close();
        statement.close();
        return exists;
    }

    public static boolean checkExistsDmVersionsByFkDmObjectsAndVersion(Connection connection,String schema,BigDecimal dmObjectId,String version)throws SQLException{
        BigDecimal dmVersionsId = null;
        PreparedStatement statement = StatementPreparator.prepareSelectDmVersionsByFkDmObjectAndVersion(connection,schema,dmObjectId,version);

        ResultSet resultSet = statement.executeQuery();
        boolean result = false;
        if(resultSet.next())
            return true;

        resultSet.close();
        statement.close();
        return result;
    }



    public static Map<String,Object> getDmVersionsByFkDmObjectsAndVersionLabel(Connection connection,String schema,BigDecimal dmOBjectsId,String version)throws SQLException{
        Map<String,Object> retData = null;
        PreparedStatement statement = StatementPreparator.prepareSelectDmVersionsByFkDmObjectAndVersion(connection,schema,dmOBjectsId,version);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()){
            retData = new HashMap<String, Object>();
            retData.put("FK_DM_STREAMS",resultSet.getObject(1));
            retData.put("STREAM_NAME",resultSet.getString(2));
            retData.put("MIME_TYPE",resultSet.getString(3));
        }
        resultSet.close();
        statement.close();
        return retData;
    }

    //deleteDmVersionsForObject
    public static void deleteDmVersionsForObject(Connection connection,String schema,BigDecimal dmObjectRowId) throws SQLException{
        PreparedStatement sStatement = StatementPreparator.prepareSelectDmVersionsByFkDmObject(connection,schema,dmObjectRowId);
        PreparedStatement dStatement = StatementPreparator.prepareBatchDeleteDmStreamsByIdentifier(connection,schema);

        ResultSet resultSet = sStatement.executeQuery();
        while(resultSet.next()){
            dStatement.setObject(1,resultSet.getObject(3));
            dStatement.addBatch();
            resultSet.deleteRow();
        }
        resultSet.close();
        sStatement.close();
        dStatement.executeBatch();
        dStatement.close();
    }

    public static void deleteDmObjectsById(Connection connection,String schema,BigDecimal dmObjectsRowId)throws SQLException{
        PreparedStatement statement = StatementPreparator.prepareDeleteDmObjectById(connection,schema,dmObjectsRowId);
        statement.execute();
        statement.close();
    }

    public static void deleteDmVersionsAndDmStreamsById(Connection connection, String schema, BigDecimal dmVersionsRowId)throws SQLException{
        PreparedStatement sStatement = StatementPreparator.prepareSelectDmVersionsById(connection, schema,dmVersionsRowId);
        ResultSet resultSet = sStatement.executeQuery();
        if(resultSet.next()){
            Object streamIdentifier = resultSet.getObject(3);
            resultSet.deleteRow();
            PreparedStatement dStatement = StatementPreparator.prepareDeleteDmStreamsByIdentifier(connection,schema,streamIdentifier);
            dStatement.execute();
            dStatement.close();
        }
        resultSet.close();
        sStatement.close();
    }

    public static void deleteDmVersionsAndDmStreamsByFkDmOBjectsAndVersionLabel(Connection connection,String schema,BigDecimal dmObjectsId,String version)throws SQLException{
        PreparedStatement statement = StatementPreparator.prepareSelectDmVersionsByFkDmObjectAndVersion(connection, schema, dmObjectsId, version);
        ResultSet resultSet = statement.executeQuery();
        if(resultSet.next()){
            Object streamIdentifier = resultSet.getObject(3);
            resultSet.deleteRow();
            PreparedStatement dStatement = StatementPreparator.prepareDeleteDmStreamsByIdentifier(connection,schema,streamIdentifier);
            dStatement.execute();
            dStatement.close();
        }
        resultSet.close();
        statement.close();
    }

    //createDmObjectsRow
    public static BigDecimal createDmObjectsRowWithPathAndName(Connection connection,String schema,String path,String name) throws SQLException{
        Map<String,Object> insertValues = new HashMap<String,Object>();
        insertValues.put("OBJ_NAME",name);
        insertValues.put("OBJ_PATH",path);

        PreparedStatement statement = StatementPreparator.prepareInsertDmObjects(connection,schema,insertValues);
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        generatedKeys.next();
        String generatedKey = generatedKeys.getString(1);
        generatedKeys.close();
        statement.close();
        return new BigDecimal(generatedKey);
    }

    //createDmObjectsRow
    public static BigDecimal createDmObjectsRowWithName(Connection connection,String schema,String name) throws SQLException{
        Map<String,Object> insertValues = new HashMap<String,Object>();
        insertValues.put("OBJ_NAME",name);

        PreparedStatement statement = StatementPreparator.prepareInsertDmObjects(connection,schema,insertValues);
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        generatedKeys.next();
        String generatedKey = generatedKeys.getString(1);
        generatedKeys.close();
        statement.close();
        return new BigDecimal(generatedKey);
    }

    private static String getGeneratedStreamId(){
        return UUID.randomUUID().toString();
    }

    //createDmStreamsRow
    public static String createDmStreamsRow(Connection connection,String schema,InputStream inputStream)throws SQLException{
        String streamIdentifier = getGeneratedStreamId();
        Map<String,Object> insertValues = new HashMap<String,Object>();
        insertValues.put("STREAM_IDENTIFIER",streamIdentifier);
        insertValues.put("FILE_STREAM",inputStream);

        PreparedStatement statement = StatementPreparator.prepareInsertDmStreams(connection,schema,insertValues);
        statement.execute();
        statement.close();
        return streamIdentifier;
    }

    //createDmVersionsRow
    public static BigDecimal createDmVersionsRow(Connection connection,String schema,String nameWithExtension,String mimeType,BigDecimal newDmObjectsRowId,String newDmStreamsRowId,String version) throws SQLException{
        Map<String,Object> insertValues = new HashMap<String, Object>();
        insertValues.put("FK_DM_OBJECTS",newDmObjectsRowId);
        insertValues.put("FK_DM_STREAMS",newDmStreamsRowId);
        insertValues.put("STREAM_NAME",nameWithExtension);
        insertValues.put("MIME_TYPE",mimeType);
        insertValues.put("VERSION_LABEL",version);

        PreparedStatement statement = StatementPreparator.prepareInsertDmVersions(connection,schema,insertValues);
        statement.execute();

        ResultSet generatedKeys = statement.getGeneratedKeys();
        generatedKeys.next();
        String generatedKey = generatedKeys.getString(1);
        generatedKeys.close();
        statement.close();
        return new BigDecimal(generatedKey);
    }
}
