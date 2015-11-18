package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 7/1/2015.
 */
public class ObjectFilterException extends RuntimeException {
    public ObjectFilterException() {
        super();
    }

    public ObjectFilterException(String message) {
        super(message);
    }

    public ObjectFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjectFilterException(Throwable cause) {
        super(cause);
    }

    //    not supported on jdk1.6
    // protected ObjectFilterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    //    super(message, cause, enableSuppression, writableStackTrace);
    //}
}
