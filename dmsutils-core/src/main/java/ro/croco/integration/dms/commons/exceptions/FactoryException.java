package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 11/18/2014.
 */
public class FactoryException extends RuntimeException {

    public FactoryException() {
    }

    public FactoryException(String message) {
        super(message);
    }

    public FactoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public FactoryException(Throwable cause) {
        super(cause);
    }

    //public FactoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    //    super(message, cause, enableSuppression, writableStackTrace);
    //}
}
