package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 6/23/2014.
 */
public class FolderIdentifier extends ObjectIdentifier {

    public static class Builder {
        private FolderIdentifier object;

        public Builder() {
            object = new FolderIdentifier();
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


        public FolderIdentifier build() {
            return object;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
