package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by Lucian.Dragomir on 12/4/2015.
 */
public class TemplateEngineValueException extends RuntimeException {
    public TemplateEngineValueException() {
        super();
    }

    public TemplateEngineValueException(String message) {
        super(message);
    }

    public TemplateEngineValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateEngineValueException(Throwable cause) {
        super(cause);
    }

    protected TemplateEngineValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
