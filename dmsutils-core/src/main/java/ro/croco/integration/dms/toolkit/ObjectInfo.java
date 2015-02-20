package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.FileUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 6/24/2014.
 */
public class ObjectInfo<T> extends RequestIdentifier implements Serializable {
    static FileUtils fileUtils = new FileUtils("/", "/");

    private T identifier;
    private String name;
    private FolderIdentifier parentIdentifier;
    private String type;
    private Map<String, Object> properties;
    private Date creationDate;
    private Date modificationDate;

    ObjectInfo() {
    }

    public ObjectInfo(Map<String, Object> properties) {
        this(null, null, null, properties);
    }

    public ObjectInfo(String type) {
        this(null, null, type, null);
    }

    public ObjectInfo(String type, Map<String, Object> properties) {
        this(null, null, type, properties);
    }

    public ObjectInfo(String path, String name, String type, Map<String, Object> properties) {
        if (path != null && !path.isEmpty()) {
            this.parentIdentifier = FolderIdentifier.builder().withPath(path).build();
        }
        this.name = name;
        this.type = type;
        this.properties = new HashMap<String, Object>();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }


    public T getIdentifier() {
        return identifier;
    }

    public void setIdentifier(T identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FolderIdentifier getParentIdentifier() {
        return parentIdentifier;
    }

    void setParentIdentifier(FolderIdentifier parentIdentifier) {
        this.parentIdentifier = parentIdentifier;
    }

    public String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Override
    public String toString() {
        return "ObjectInfo{" +
                "identifier=" + identifier +
                ", name='" + name + '\'' +
                ", parentIdentifier=" + parentIdentifier +
                ", type='" + type + '\'' +
                ", properties=" + properties +
                ", creationDate=" + creationDate +
                ", modificationDate=" + modificationDate +
                "} " + super.toString();
    }
}
