package ro.croco.integration.dms.toolkit.tests;

import org.junit.Test;
import ro.croco.integration.dms.toolkit.context.ContextProperties;
import ro.croco.integration.dms.toolkit.utils.SqlOperationTranslator;

/**
 * Created by battamir.sugarjav on 2/20/2015.
 */
public class On_SqlOperationTranslator {

    @Test
    public void test_translate_insert_operation(){
        System.out.println(SqlOperationTranslator.translateCommand("i:DM_OBJECTS(OBJ_PATH,OBJ_NAME)", SqlOperationTranslator.PREPARED_STATEMENT,"schema"));
    }

    @Test
    public void test_string_split(){
        System.out.println("abc".split(",").length);
    }
}
