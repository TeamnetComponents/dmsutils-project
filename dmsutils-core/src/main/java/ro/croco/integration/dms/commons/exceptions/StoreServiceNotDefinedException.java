package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 8/12/2014.
 */
public class StoreServiceNotDefinedException extends RuntimeException {
    public StoreServiceNotDefinedException() {
    }

    public StoreServiceNotDefinedException(String message) {
        super(message);
    }

    public StoreServiceNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreServiceNotDefinedException(Throwable cause) {
        super(cause);
    }

//    not supported on jdk1.6
//    public StoreServiceNotDefinedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//    }
}
