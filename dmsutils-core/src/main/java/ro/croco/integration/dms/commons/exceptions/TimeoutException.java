package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 7/8/2014.
 */
public class TimeoutException extends RuntimeException{
    public TimeoutException() {
        super();
    }

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeoutException(Throwable cause) {
        super(cause);
    }

//    not supported on jdk 1.6
//    protected TimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//    }
}
