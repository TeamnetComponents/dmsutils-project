package ro.croco.integration.dms.toolkit.db;

/**
 * Created by battamir.sugarjav on 2/26/2015.
 */
public class ContextProperties {

    public static final class Required{
        public static final String SYNCHRONOUS_MESSAGE_WAIT_RESPONSE_TIMEOUT = "service.synchronous.message.waitResponse.timeout";
        public static final String SYNCHRONOUS_MESSAGE_PRIORITY_DEFAULT = "service.synchronous.message.priority.default";
        public static final String SYNCHRONOUS_MESSAGE_WAIT_RESPONSE_ON_ITERATION = "service.synchronous.message.waitResponse.onIteration";

        public final static String SERVICE_SYNC_REQUEST_QUEUE = "service.synchronous.request.queue.name";
        public final static String SERVICE_SYNC_RESPONSE_QUEUE = "service.synchronous.response.queue.name";
        public final static String SERVICE_ASYNC_REQUEST_QUEUE = "service.asynchronous.request.queue.name";
        public final static String SERVICE_ASYNC_RESPONSE_QUEUE = "service.asynchronous.response.queue.name";

        public static final String INSTANCE_NAME="instance.name";
        public static final String ISTANCE_CLASS="instance.class";
        public static final String INSTANCE_CACHE="instance.cache";
    }

    public static final class Optional{

    }
}
