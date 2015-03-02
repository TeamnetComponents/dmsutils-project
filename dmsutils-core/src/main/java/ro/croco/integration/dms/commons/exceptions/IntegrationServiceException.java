package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by battamir.sugarjav on 3/2/2015.
 */
public class IntegrationServiceException extends RuntimeException {
    public IntegrationServiceException() {
    }

    public IntegrationServiceException(String message) {
        super(message);
    }

    public IntegrationServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrationServiceException(Throwable cause) {
        super(cause);
    }
}
