package ro.croco.integration.dms.toolkit.utils;

import org.apache.commons.lang.StringUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.StoreService;

import java.beans.Statement;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
final public class SqlOperationTranslator {

    public final static int STATEMENT = 0;
    public final static int PREPARED_STATEMENT = 1;
    private final static String INSERT_MARKER = "i:";
    private final static String UPDATE_MARKER = "u:";
    private final static String QUERY_MARKER = "s:";
    private final static String DELETE_MARKER = "d:";

    private static String translatePSInsert(String command,String schema){
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        if(schema != null && !schema.equals(""))
            builder.append(schema + ".");
        builder.append(command.substring(command.indexOf(":") + 1, command.indexOf("(")));
        builder.append(command.substring(command.indexOf("("))).append(" values(");
        int insertValuesCount = StringUtils.countMatches(command.substring(command.indexOf("(")),",");
        for(int i = 0;i < insertValuesCount;i++)
            builder.append("?,");
        builder.append("?)");

        return builder.toString();
    }

    private static String translatePSUpdate(String command,String schema){
        return null;
    }

    private static String translatePSQuery(String command,String schema){
        StringBuilder builder = new StringBuilder("SELECT ");
        builder.append(command.substring(command.indexOf("(") + 1,command.indexOf(")")));
        builder.append(" FROM ");
        if(schema != null && !schema.equals(""))
            builder.append(schema + ".");
        builder.append(command.substring(command.indexOf(":") + 1,command.indexOf("(")));

        if(command.indexOf(",") != -1){
            builder.append(" WHERE ");
            String[] conditionValues = command.substring(command.lastIndexOf("(") + 1,command.lastIndexOf(")")).split(",");
            for(int i = 0;i < conditionValues.length;i++){
                if(conditionValues[i].indexOf("=") != -1)
                    builder.append(conditionValues[i]).append(" AND ");
                else builder.append(conditionValues[i]).append("=? AND ");
            }
            builder.append("1=1");
        }
        return builder.toString();
    }

    private static String translatePSDelete(String command,String schema){
        StringBuilder builder = new StringBuilder("DELETE FROM ");

        if(schema != null && !schema.equals(""))
            builder.append(schema + ".");
        builder.append(command.substring(command.indexOf(":") + 1,command.indexOf("(")));

        String[] conditionValues = command.substring(command.lastIndexOf("(") + 1,command.lastIndexOf(")")).split(",");
        for(int i = 0;i < conditionValues.length;i++){
            if(conditionValues[i].indexOf("=") != -1)
                builder.append(conditionValues[i]).append(" AND ");
            else builder.append(conditionValues[i]).append("=? AND ");
        }
        builder.append("1=1");

        return builder.toString();
    }

    public static String translateCommand(String commnand,int statementType,String schema){
        try {
            String translatedCommand = null;
            if(statementType == PREPARED_STATEMENT){
                if(commnand.startsWith(INSERT_MARKER))
                    translatedCommand = translatePSInsert(commnand,schema);
                if(commnand.startsWith(UPDATE_MARKER))
                    translatedCommand = translatePSUpdate(commnand, schema);
                if(commnand.startsWith(QUERY_MARKER))
                    translatedCommand = translatePSQuery(commnand, schema);
                if(commnand.startsWith(DELETE_MARKER))
                    translatedCommand = translatePSDelete(commnand, schema);
            }

            if(statementType == STATEMENT){

            }

            if(translatedCommand == null)
                throw new StoreServiceException("Targetted function is not implemented.");

            return translatedCommand;
        }
        catch(RuntimeException ex){
            throw new StoreServiceException("Incorrect local formatted sql provided.Please make necessary corrections.",ex);
        }
    }
}
