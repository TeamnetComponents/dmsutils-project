package ro.croco.integration.dms.toolkit;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by Lucian.Dragomir on 7/1/2015.
 */
public class ObjectFilterFactory {

    public ObjectFilter getInstance(String className, Object... parameters) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ObjectFilter instance = null;
        Class clazz = Class.forName(className);
        if (parameters.length == 0) {
            instance = (ObjectFilter) clazz.newInstance();
        } else {
            instance = (ObjectFilter) ConstructorUtils.invokeConstructor(clazz, parameters);
        }
        return instance;
    }
}
