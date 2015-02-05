package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 8/24/2014.
 */
public class VersionNotFoundException extends RuntimeException {
    public VersionNotFoundException() {
    }

    public VersionNotFoundException(String message) {
        super(message);
    }

    public VersionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VersionNotFoundException(Throwable cause) {
        super(cause);
    }

//    not supported on jdk1.6
//    public VersionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//    }
}
