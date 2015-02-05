package ro.croco.integration.dms.toolkit;

import java.util.Map;

/**
 * Created by Lucian.Dragomir on 6/25/2014.
 */
public class FolderInfo extends ObjectInfo<FolderIdentifier> {

    FolderInfo() {
        super();
    }

    public FolderInfo(Map<String, Object> properties) {
        super(properties);
    }

    public FolderInfo(String type) {
        super(type);
    }

    public FolderInfo(String type, Map<String, Object> properties) {
        super(type, properties);
    }

    public FolderInfo(String path, String name, String type, Map<String, Object> properties) {
        super(path, name, type, properties);
    }

}
