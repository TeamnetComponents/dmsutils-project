package ro.croco.integration.dms.toolkit;

import java.io.Serializable;

/**
 * Created by Lucian.Dragomir on 8/20/2014.
 */
public class RequestIdentifier implements Serializable {
    private String requestId;
    private Exception exception;

    public RequestIdentifier() {
        this(null, null);
    }

    public RequestIdentifier(String requestId) {
        this(requestId, null);
    }

    public RequestIdentifier(String requestId, Exception exception) {
        this.requestId = requestId;
        this.exception = exception;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "RequestIdentifier{" +
                "requestId='" + requestId + '\'' +
                ", exception=" + exception +
                '}';
    }
}
