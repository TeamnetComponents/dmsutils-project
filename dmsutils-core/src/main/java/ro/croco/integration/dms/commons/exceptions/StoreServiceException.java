package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 8/12/2014.
 */
public class StoreServiceException extends RuntimeException {
    public StoreServiceException() {
    }

    public StoreServiceException(String message) {
        super(message);
    }

    public StoreServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreServiceException(Throwable cause) {
        super(cause);
    }

//    not supported on jdk1.6
//    public StoreServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//    }
}
