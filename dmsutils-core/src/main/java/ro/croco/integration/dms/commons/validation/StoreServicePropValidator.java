package ro.croco.integration.dms.commons.validation;

import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceValidationException;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Created by battamir.sugarjav on 2/19/2015.
 */
public class StoreServicePropValidator {

    private Object toInspectClassObj;
    private Properties context;

    public StoreServicePropValidator(Object toInspectClassObj){
        this.toInspectClassObj = toInspectClassObj;
    }

    private boolean propertyNotSetOrExist(String property){
        return !context.containsKey(property) || context.get(property).equals("");
    }

    public void validate(Properties context){
        try {
            this.context = context;
            Field[] requiredProperties = toInspectClassObj.getClass().getDeclaredFields();
            System.out.println();
            for (Field property : requiredProperties) {
                if (propertyNotSetOrExist((String) property.get(toInspectClassObj)))
                    throw new StoreServiceValidationException("Property '" + property.get(toInspectClassObj) + "' is missing from file.Please make sure you provide it");
                System.out.println("\tMandatory Property '" + property.get(toInspectClassObj) + "' has been found - OK");
            }
        }
        catch(IllegalAccessException ex){
            throw new StoreServiceException(ex);
        }
    }
}
