package ro.croco.integration.dms.toolkit.tests;

import org.junit.Test;
import ro.croco.integration.dms.commons.validation.StoreServicePropValidator;
import ro.croco.integration.dms.toolkit.context.ContextProperties;

import java.util.Properties;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
public class On_StoreServicePropValidator {

    @Test
    public void test_validator(){
        StoreServicePropValidator validator = new StoreServicePropValidator(new ContextProperties.Required());
        Properties context = new Properties();
        context.put("jdbc.type","haha");
        validator.validate(context);
    }
}
