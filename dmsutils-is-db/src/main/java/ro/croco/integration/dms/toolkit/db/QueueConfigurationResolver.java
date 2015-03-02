package ro.croco.integration.dms.toolkit.db;

import ro.croco.integration.dms.commons.TemplateEngine;
import ro.croco.integration.dms.commons.exceptions.IntegrationServiceException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;


import java.util.Properties;

/**
 * Created by battamir.sugarjav on 2/26/2015.
 */

public class QueueConfigurationResolver {

    private final static TemplateEngine templateEngine = TemplateEngine.getInstance();
    private final static String QUEUE_DEFINITION_NAME = "queueName";
    private final static String QUEUE_DEFINITION_CONNECTION = "queue." + templateEngine.escapeVariable(QUEUE_DEFINITION_NAME) + ".connection.jdbc";
    private final static String QUEUE_DEFINITION_TABLE = "queue." + templateEngine.escapeVariable(QUEUE_DEFINITION_NAME) + ".table.name";
    private final static String QUEUE_DEFINITION_SEQUENCE = "queue." + templateEngine.escapeVariable(QUEUE_DEFINITION_NAME) + ".table.sequence";
    private final static String QUEUE_DEFINITION_HISTORY = "queue." + templateEngine.escapeVariable(QUEUE_DEFINITION_NAME) + ".table.history.name";

    private static void verifyExistance(Object evaluated)throws StoreServiceException{
        if(evaluated == null)
            throw new IntegrationServiceException("[QueueConfigurationResolver] Context does not contain required property.");
    }

    public static String getConnectionName(Properties context,String queue)throws IntegrationServiceException{
        Object connectionName = context.get(templateEngine.getValueFromTemplate(QUEUE_DEFINITION_CONNECTION, QUEUE_DEFINITION_NAME,queue));
        verifyExistance(connectionName);
        return (String)connectionName;
    }

    public static String getHistoryTable(Properties context,String queue)throws IntegrationServiceException{
        Object historyTable =  context.get(templateEngine.getValueFromTemplate(QUEUE_DEFINITION_HISTORY,QUEUE_DEFINITION_NAME,queue));
        verifyExistance(historyTable);
        return (String) historyTable;
    }
    public static String getTableName(Properties context,String queue)throws IntegrationServiceException{
        Object tableName =  context.get(templateEngine.getValueFromTemplate(QUEUE_DEFINITION_TABLE, QUEUE_DEFINITION_NAME,queue));
        verifyExistance(tableName);
        return (String) tableName;
    }

    public static String getTableSequenceName(Properties context,String queue){
        Object tableSeqName = context.get(templateEngine.getValueFromTemplate(QUEUE_DEFINITION_SEQUENCE, QUEUE_DEFINITION_NAME,queue));
        verifyExistance(tableSeqName);
        return (String)tableSeqName;
    }
}
