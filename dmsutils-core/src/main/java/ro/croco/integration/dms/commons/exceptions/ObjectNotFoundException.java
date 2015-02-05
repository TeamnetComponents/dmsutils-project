package ro.croco.integration.dms.commons.exceptions;

/**
 * Created by danielp on 7/1/14.
 */
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException() { super(); }
    public ObjectNotFoundException(String s) { super(s); }
    public ObjectNotFoundException(String s, Throwable throwable) { super(s, throwable); }
    public ObjectNotFoundException(Throwable throwable) { super(throwable); }
}
