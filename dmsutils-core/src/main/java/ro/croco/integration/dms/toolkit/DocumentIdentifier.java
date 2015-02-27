package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 6/23/2014.
 */
public class DocumentIdentifier extends ObjectIdentifier {
    private String version;

    public DocumentIdentifier() {
        super();
    }

    public DocumentIdentifier(String identifier) {
        super(identifier);
    }

    public static class Builder {
        private DocumentIdentifier object;

        public Builder() {
            object = new DocumentIdentifier();
        }

        public Builder withRequestId(String requestId) {
            object.setRequestId(requestId);
            return this;
        }

        public Builder withStoreServiceName(String storeServiceName) {
            object.setStoreServiceName(storeServiceName);
            return this;
        }

        public Builder withId(String id) {
            object.setId(id);
            return this;
        }

        public Builder withPath(String path) {
            object.setPath(path);
            return this;
        }

        public Builder withVersion(String version) {
            object.setVersion(version);
            return this;
        }

        public DocumentIdentifier build() {
            return object;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "DocumentIdentifier{" +
                "version='" + version + '\'' +
                "} " + super.toString();
    }
}
