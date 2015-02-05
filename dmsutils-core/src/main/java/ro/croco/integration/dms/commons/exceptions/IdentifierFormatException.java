package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 11/12/2014.
 */
public class IdentifierFormatException extends RuntimeException {
    public IdentifierFormatException() {
        super();
    }

    public IdentifierFormatException(String message) {
        super(message);
    }

    public IdentifierFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdentifierFormatException(Throwable cause) {
        super(cause);
    }

    //protected IdentifierFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    //    super(message, cause, enableSuppression, writableStackTrace);
    //}
}
