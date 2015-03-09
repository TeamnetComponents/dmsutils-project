package ro.tn.components.dmsutils.ssDb;

import org.junit.Test;
import ro.croco.integration.dms.commons.SqlOperationTranslator;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public class On_SqlOperationTranslator {

    @Test
    public void translate_insert_operation(){
        System.out.println(SqlOperationTranslator.translateCommand("i:DM_OBJECTS(OBJ_PATH,OBJ_NAME)", SqlOperationTranslator.PREPARED_STATEMENT,"schema"));
    }

    @Test
    public void translate_delete_operation() {
        String command = SqlOperationTranslator.translateCommand("d:DM_STREAMS(NAME)", SqlOperationTranslator.PREPARED_STATEMENT,"schema");
        System.out.println(command);
    }

    @Test
    public void translate_select_operation(){
        String command = SqlOperationTranslator.translateCommand("s:DM_OBJECT_VERSIONS(MAX(VERSION_LABEL)),(FK_DM_OBJECTS))",SqlOperationTranslator.PREPARED_STATEMENT,"schema");
        System.out.println(command);
    }
}
