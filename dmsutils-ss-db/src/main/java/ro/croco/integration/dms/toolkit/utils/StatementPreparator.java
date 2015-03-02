package ro.croco.integration.dms.toolkit.utils;

import ro.croco.integration.dms.toolkit.SqlOperationTranslator;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Map;

/**
 * Created by battamir.sugarjav on 2/26/2015.
 */

public class StatementPreparator {

    public static PreparedStatement prepareSelectLastVersionLabelForDmObject(Connection connection,String schema,BigDecimal dmObjectId)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECT_VERSIONS(MAX[VERSION_LABEL],(FK_DM_OBJECTS))",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1,dmObjectId.intValue());
        return statement;
    }



    public static PreparedStatement prepareSelectDmObjectByPathAndName(Connection connection,String schema,String path,String name)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECTS(ID,OBJ_PATH,OBJ_NAME),(OBJ_PATH,OBJ_NAME)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1, path);
        statement.setObject(2, name);
        return statement;
    }

    public static PreparedStatement prepareSelectDmObjectById(Connection connection,String schema,BigDecimal dmObjectsId)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECTS(ID,OBJ_PATH,OBJ_NAME),(ID)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1,dmObjectsId.intValue());
        return statement;
    }

    public static PreparedStatement prepareDeleteDmObjectById(Connection connection,String schema,BigDecimal dmObjectsId)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("d:DM_OBJECTS(ID)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        statement.setObject(1,dmObjectsId.intValue());
        return statement;
    }


    public static PreparedStatement prepareSelectDmObjectByName(Connection connection,String schema,String name)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECTS(ID,OBJ_PATH,OBJ_NAME),(OBJ_NAME)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1, name);
        return statement;
    }

    public static PreparedStatement prepareSelectDmVersionsById(Connection connection,String schema,BigDecimal id)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECT_VERSIONS(ID,FK_DM_OBJECTS,FK_DM_STREAMS,STREAM_NAME,MIME_TYPE,VERSION_LABEL),(ID)", SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        statement.setObject(1,id.intValue());
        return statement;
    }

    public static PreparedStatement prepareSelectDmVersionsByIdAndFkDmObjects(Connection connection,String schema,BigDecimal id,BigDecimal fkDmObjectsId)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECT_VERSIONS(ID,FK_DM_OBJECTS,FK_DM_STREAMS,STREAM_NAME,MIME_TYPE,VERSION_LABEL),(ID,FK_DM_OBJECTS)", SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1,id.intValue());
        statement.setObject(2,fkDmObjectsId.intValue());
        return statement;
    }

    public static PreparedStatement prepareSelectDmVersionsByFkDmObject(Connection connection,String schema,BigDecimal fkDmObjects)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECT_VERSIONS(ID,FK_DM_OBJECTS,FK_DM_STREAMS,STREAM_NAME,MIME_TYPE,VERSION_LABEL),(FK_DM_OBJECTS)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        statement.setObject(1, fkDmObjects.intValue());
        return statement;
    }

    public static PreparedStatement prepareSelectDmVersionsByFkDmObjectAndVersion(Connection connection,String schema,BigDecimal fkDmObjects,String versionLabel)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECT_VERSIONS(ID,FK_DM_OBJECTS,FK_DM_STREAMS,STREAM_NAME,MIME_TYPE,VERSION_LABEL),(FK_DM_OBJECTS,VERSION_LABEL)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        statement.setObject(1, fkDmObjects.intValue());
        statement.setObject(2, versionLabel);
        return statement;
    }

    public static PreparedStatement prepareDeleteDmVersionsByFkDmObjectAndVersion(Connection connection,String schema,BigDecimal fkDmObjects,String versionLabel)throws SQLException{
        String command = SqlOperationTranslator.translateCommand("d:DM_OBJECT_VERSIONS(FK_DM_OBJECTS,VERSION_LABEL)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        statement.setObject(1,fkDmObjects.intValue());
        statement.setObject(2,versionLabel);
        return statement;
    }

    public static PreparedStatement prepareSelectDmStreamsByIdentifier(Connection connection,String schema,Object identifier)throws SQLException{
        String command =  SqlOperationTranslator.translateCommand("s:DM_STREAMS(FILE_STREAM),(NAME)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1,identifier);
        return statement;
    }

    public static PreparedStatement prepareInsertDmVersions(Connection connection,String schema,Map<String,Object> values) throws SQLException{
        String command = SqlOperationTranslator.translateCommand("i:DM_OBJECT_VERSIONS(FK_DM_OBJECTS,FK_DM_STREAMS,STREAM_NAME,MIME_TYPE,VERSION_LABEL)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command, Statement.RETURN_GENERATED_KEYS);
        statement.setObject(1,values.get("FK_DM_OBJECTS"));
        statement.setObject(2,values.get("FK_DM_STREAMS"));
        statement.setObject(3,values.get("STREAM_NAME"));
        statement.setObject(4,values.get("MIME_TYPE"));
        if(values.get("VERSION_LABEL") == null)
            statement.setNull(5,Types.VARCHAR);
        else statement.setObject(5, values.get("VERSION_LABEL"));
        return statement;
    }

    public static PreparedStatement prepareInsertDmStreams(Connection connection,String schema,Map<String,Object> values) throws SQLException{
        String command = SqlOperationTranslator.translateCommand("i:DM_STREAMS(NAME,FILE_STREAM)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        statement.setObject(1,values.get("STREAM_IDENTIFIER"));
        statement.setBinaryStream(2,(InputStream)values.get("FILE_STREAM"));
        return statement;
    }

    public static PreparedStatement prepareInsertDmObjects(Connection connection,String schema,Map<String,Object> values) throws SQLException{
        String command = SqlOperationTranslator.translateCommand("i:DM_OBJECTS(OBJ_PATH,OBJ_NAME)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command,Statement.RETURN_GENERATED_KEYS);
        if(values.get("OBJ_PATH") == null)
            statement.setNull(1, Types.VARCHAR);
        else statement.setObject(1, values.get("OBJ_PATH"));
        statement.setObject(2,values.get("OBJ_NAME"));
        return statement;
    }

    public static PreparedStatement prepareBatchDeleteDmStreamsByIdentifier(Connection connection,String schema) throws SQLException{
        String command = SqlOperationTranslator.translateCommand("d:DM_STREAMS(NAME)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        PreparedStatement statement = connection.prepareStatement(command);
        return statement;
    }

    public static PreparedStatement prepareDeleteDmStreamsByIdentifier(Connection connection,String schema,Object streamIdentifier) throws SQLException{
        String command = SqlOperationTranslator.translateCommand("d:DM_STREAMS(NAME)",SqlOperationTranslator.PREPARED_STATEMENT,schema);
        System.out.println(command);
        PreparedStatement statement = connection.prepareStatement(command,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        statement.setObject(1,streamIdentifier);
        return statement;
    }
}
