package ro.croco.integration.dms.toolkit;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.IdentifierFormatException;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Lucian.Dragomir on 6/23/2014.
 */
public abstract class ObjectIdentifier extends RequestIdentifier implements Serializable {

    //    public static final String PATH_DELIMITER = "/";
    //    public static final String IDENTIFIER_DELIMITER = "<~||~>";
    //    public static final String IDENTIFIER_DELIMITER_ESCAPED = StringEscapeUtils.escapeJava(IDENTIFIER_DELIMITER);
    private static ObjectMapper OBJECT_MAPPER = null;

    private String storeServiceName;

    private String id;
    private String path;

    ObjectIdentifier() {
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    private static ObjectMapper getObjectMapper() {
        if (OBJECT_MAPPER == null) {
            synchronized (ObjectIdentifier.class) {
                if (OBJECT_MAPPER == null) {
                    OBJECT_MAPPER = createObjectMapper();
                }
            }
        }
        return OBJECT_MAPPER;
    }

//    public static Map<String, String> splitItems(String identifier, String itemNames, String defaultItemName) {
//        Map<String, String> itemMap = new HashMap<String, String>();
//        if (identifier.contains(IDENTIFIER_DELIMITER)) {
//            String[] arrItemValues = identifier.split(IDENTIFIER_DELIMITER_ESCAPED);
//            String[] arrItemNames = itemNames.split(IDENTIFIER_DELIMITER_ESCAPED);
//            if (arrItemNames.length != arrItemValues.length) {
//                throw new IdentifierFormatException("Unable to match identifier: " + identifier + " to format: " + itemNames);
//            }
//            for (int index = 0; index < arrItemValues.length; index++) {
//                itemMap.put(arrItemNames[index], arrItemValues[index]);
//            }
//        } else {
//            itemMap.put(defaultItemName, identifier);
//        }
//        return itemMap;
//    }

    protected ObjectIdentifier(String identifier) {
        if (identifier.startsWith("{")) {
            //we assume the object is serialized as json
            try {
                //a copy constructor using jackson
                getObjectMapper().readerForUpdating(this).readValue(identifier);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (identifier.startsWith(FileUtils.getFileUtilsDMS().getPathDelimiter())) {
            //we assume it is a path information
            setPath(identifier);
        } else {
            //we assume it is an id information
            setId(identifier);
        }
    }

    public String getStoreServiceName() {
        return storeServiceName;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setStoreServiceName(String storeServiceName) {
        this.storeServiceName = storeServiceName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIdentifier() {
        try {
            return getObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IdentifierFormatException(e);
        }
    }

    public ObjectBaseType getType() {
        return ObjectBaseType.getByName(this.getClass().getCanonicalName());
    }


    @Override
    public String toString() {
        return "ObjectIdentifier{" +
                "storeServiceName='" + storeServiceName + '\'' +
                ", id='" + id + '\'' +
                ", path='" + path + '\'' +
                "} " + super.toString();
    }


    public static void main(String[] args) {
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withId("1234567890").withPath("/JMeter/upload").withRequestId("142-124-2424-435346-6jkv-9043").withStoreServiceName("ss-fo").build();
        String string = documentIdentifier.getIdentifier();
        System.out.println(documentIdentifier.hashCode());
        System.out.println(documentIdentifier);
        System.out.println(string);

        DocumentIdentifier documentIdentifier1 = new DocumentIdentifier(string);
        String string1 = documentIdentifier1.getIdentifier();
        System.out.println(documentIdentifier1.hashCode());
        System.out.println(documentIdentifier1);
        System.out.println(string1);
    }
}
