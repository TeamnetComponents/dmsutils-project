package ro.croco.integration.dms.toolkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 6/24/2014.
 */
public enum ObjectBaseType {
    FOLDER(FolderIdentifier.class.getCanonicalName()),
    DOCUMENT(DocumentIdentifier.class.getCanonicalName());

    private static final Map<String, ObjectBaseType> lookup = new HashMap<String, ObjectBaseType>();

    static {
        for (ObjectBaseType objectBaseType : ObjectBaseType.values())
            lookup.put(objectBaseType.name, objectBaseType);
    }

    private String name;

    ObjectBaseType(String name) {
        this.name = name;
    }

    public static ObjectBaseType getByName(String name) {
        return lookup.get(name);
    }
}
