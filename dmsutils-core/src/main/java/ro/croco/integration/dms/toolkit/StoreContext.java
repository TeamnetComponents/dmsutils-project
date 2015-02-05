package ro.croco.integration.dms.toolkit;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by Lucian.Dragomir on 8/12/2014.
 */
public class StoreContext extends Properties {
    public static final String DMS_PROPERTIES_PREFIX = StoreContext.class.getCanonicalName().toLowerCase() + ".";

    private static String FOLDER_SEPARATOR = ";";

    public static enum AUTHENTICATION_TYPE_VALUES {
        DEFAULT,
        BASIC,
        BASIC_AS,
        AS
    }

    public static enum COMMUNICATION_TYPE_VALUES {
        SYNCHRONOUS,
        ASYNCHRONOUS,
        SYNCHRONOUS_LOCAL
    }

    public static final String AUTHENTICATION_TYPE = DMS_PROPERTIES_PREFIX + "AUTHENTICATION_TYPE".toLowerCase();
    public static final String USER = DMS_PROPERTIES_PREFIX + "USER".toLowerCase();
    public static final String PASSWORD = DMS_PROPERTIES_PREFIX + "PASSWORD".toLowerCase();
    public static final String USER_AS = DMS_PROPERTIES_PREFIX + "USER_AS".toLowerCase();
    public static final String ALLOWED_FOLDERS = DMS_PROPERTIES_PREFIX + "ALLOWED_FOLDERS".toLowerCase();
    public static final String COMMUNICATION_TYPE = DMS_PROPERTIES_PREFIX + "COMMUNICATION_TYPE".toLowerCase();
    public static final String PATH = DMS_PROPERTIES_PREFIX + "PATH".toLowerCase();
    public static final String FORCE_DELETE = DMS_PROPERTIES_PREFIX + "FORCE_DELETE".toLowerCase();
    public static final String REQUEST_IDENTIFIER = DMS_PROPERTIES_PREFIX + "REQUEST_IDENTIFIER".toLowerCase();
    public static final String METADATA_OPERATION = DMS_PROPERTIES_PREFIX + "META_OPERATION".toLowerCase();

    public static final List<String> properties = Arrays.asList(new String[]{
            AUTHENTICATION_TYPE, USER, PASSWORD, USER_AS, ALLOWED_FOLDERS, COMMUNICATION_TYPE,
            PATH, FORCE_DELETE, REQUEST_IDENTIFIER, METADATA_OPERATION});

    public static String generateRequestIdentifier() {
        return String.valueOf(UUID.randomUUID());
    }

    public COMMUNICATION_TYPE_VALUES getCommunicationType() {
        return (COMMUNICATION_TYPE_VALUES) this.get(COMMUNICATION_TYPE);
    }

    public String getRequestIdentifier() {
        //return generateRequestIdentifier();
        return (String) this.get(REQUEST_IDENTIFIER);
    }

    public String getPath() {
        return (String) this.get(PATH);
    }

    public String getMetadataOperation() {
        return this.get(METADATA_OPERATION) == null ? MetadataService.RULE_DEFAULT : (String) this.get(METADATA_OPERATION);
    }

    public AUTHENTICATION_TYPE_VALUES getAuthenticationType() {
        String authenticationType = StringUtils.isNotEmpty((String) this.get(AUTHENTICATION_TYPE)) ? (String) this.get(AUTHENTICATION_TYPE) : AUTHENTICATION_TYPE_VALUES.DEFAULT.name();
        return AUTHENTICATION_TYPE_VALUES.valueOf(authenticationType);
    }

    public static class Builder {
        private StoreContext storeContext;

        public Builder() {
            storeContext = new StoreContext();
        }

        public Builder copy(StoreContext storeContextCopy) {
            storeContext.putAll(storeContextCopy);
            return this;
        }

        public Builder loginDefault() {
            storeContext.put(AUTHENTICATION_TYPE, AUTHENTICATION_TYPE_VALUES.DEFAULT.name());
            storeContext.remove(USER);
            storeContext.remove(PASSWORD);
            storeContext.remove(USER_AS);
            return this;
        }

        public Builder loginBasic(String userName, String userPassword) {
            storeContext.put(AUTHENTICATION_TYPE, AUTHENTICATION_TYPE_VALUES.BASIC.name());
            storeContext.put(USER, userName);
            storeContext.put(PASSWORD, userPassword);
            storeContext.remove(USER_AS);
            return this;
        }

        public Builder loginBasicAs(String userName, String userPassword, String userNameAs) {
            storeContext.put(AUTHENTICATION_TYPE, AUTHENTICATION_TYPE_VALUES.BASIC_AS.name());
            storeContext.put(USER, userName);
            storeContext.put(PASSWORD, userPassword);
            storeContext.put(USER_AS, userNameAs);
            return this;
        }

        public Builder loginAs(String userNameAs) {
            storeContext.put(AUTHENTICATION_TYPE, AUTHENTICATION_TYPE_VALUES.AS.name());
            storeContext.remove(USER);
            storeContext.remove(PASSWORD);
            storeContext.put(USER_AS, userNameAs);
            return this;
        }

        /* Pentru a transmite informatiile in serverul cmis ca headere pe request,
           trebuie sa prefixam proprietatile cu ELO_CMIS_SERVER_PREFIX
         */
//        public Builder loginEloCmisAs(String userNameAs) {
//            storeContext.put(ELO_CMIS_SERVER_PREFIX + AUTHENTICATION_TYPE, AUTHENTICATION_TYPE_VALUES.AS);
//            storeContext.put(ELO_CMIS_SERVER_PREFIX + USER_AS, userNameAs);
//            return this;
//        }

//        public Builder loginToken(byte[] token) {
//            storeContext.put(AUTHENTICATION_TYPE, AUTHENTICATION_TYPE_VALUES.TOKEN);
//            storeContext.put(TOKEN, token);
//            return this;
//        }
//
//        public Builder loginCertificate(String certificate) {
//            storeContext.put(AUTHENTICATION_TYPE, AUTHENTICATION_TYPE_VALUES.CERTIFICATE);
//            storeContext.put(CERTIFICATE, certificate);
//            return this;
//        }

        public Builder allowedFolder(String folderPath) {
            if (!storeContext.containsKey(ALLOWED_FOLDERS)) {
                storeContext.put(ALLOWED_FOLDERS, folderPath);
            } else {
                storeContext.put(ALLOWED_FOLDERS, storeContext.get(ALLOWED_FOLDERS) + FOLDER_SEPARATOR + folderPath);
            }
            return this;
        }

        public Builder communicationType(COMMUNICATION_TYPE_VALUES communicationType) {
            storeContext.put(COMMUNICATION_TYPE, communicationType);
            return this;
        }

        private Builder requestIdentifier() {
            return requestIdentifier(StoreContext.generateRequestIdentifier());
        }

        private Builder requestIdentifier(String requestIdentifier) {
            storeContext.put(REQUEST_IDENTIFIER, requestIdentifier);
            return this;
        }

        public Builder withPath(String path) {
            storeContext.put(PATH, path);
            return this;
        }

        public Builder metadataOperation(String metadataOperation) {
            storeContext.put(METADATA_OPERATION, metadataOperation);
            return this;
        }


        public StoreContext build() {
            if (!storeContext.containsKey(AUTHENTICATION_TYPE)) {
                this.loginDefault();
            }
            if (!storeContext.containsKey(COMMUNICATION_TYPE)) {
                storeContext.put(COMMUNICATION_TYPE, COMMUNICATION_TYPE_VALUES.SYNCHRONOUS);
            }
            if (!storeContext.containsKey(REQUEST_IDENTIFIER)) {
                this.requestIdentifier();
            }
            return storeContext;
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

//    @Override
//    public synchronized boolean containsKey(Object key) {
//        return super.containsKey(key) || super.containsKey(DMS_PROPERTIES_PREFIX + key);
//    }
//
//    @Override
//    public synchronized Object put(Object key, Object value) {
//        if ((key instanceof String) && properties.contains(key)) {
//            super.remove(key);
//            key = DMS_PROPERTIES_PREFIX + key;
//        }
//        return super.put(key, value);
//    }
//
//    @Override
//    public synchronized Object get(Object key) {
//        if ((key instanceof String) && properties.contains(key)) {
//            key = DMS_PROPERTIES_PREFIX + key;
//        }
//        return super.get(key);
//    }
//
//    @Override
//    public synchronized Object remove(Object key) {
//        if (super.containsKey(key)) {
//            return super.remove(key);
//        }
//        if (super.containsKey(DMS_PROPERTIES_PREFIX + key)) {
//            return super.remove(DMS_PROPERTIES_PREFIX + key);
//        }
//
//        return null;
//    }
//
//    @Override
//    public String getProperty(String key) {
//        if (properties.contains(key)) {
//            key = DMS_PROPERTIES_PREFIX + key;
//        }
//        return super.getProperty(key);
//    }

}
