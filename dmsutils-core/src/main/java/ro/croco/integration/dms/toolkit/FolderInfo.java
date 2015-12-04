package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.FileUtils;

import java.util.Arrays;
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
        String fullPathName = FileUtils.getFileUtilsDMS().concatenate(path, name);
        FolderIdentifier folderIdentifier = FolderIdentifier.builder().withPath(fullPathName).build();
        this.setIdentifier(folderIdentifier);
    }

    @Override
    public String toString() {
        return "FolderInfo{} " + super.toString();
    }
}
