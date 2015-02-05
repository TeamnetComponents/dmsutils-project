package ro.croco.integration.dms.toolkit;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 1/4/2015.
 */

public class StoreServiceMessage implements Serializable {
    private Date date;
    private String method;
    private StoreServiceMessageType type;
    private Object[] parameters;
    private RuntimeException exception;
    private Map<String, String> configuration;

    public StoreServiceMessage() {
    }

    public StoreServiceMessage(Date date, String method, StoreServiceMessageType type, Object[] parameters, RuntimeException exception, Map<String, String> configuration) {
        this.date = date;
        this.method = method;
        this.type = type;
        this.parameters = parameters;
        this.exception = exception;
        this.configuration = configuration;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public StoreServiceMessageType getType() {
        return type;
    }

    public void setType(StoreServiceMessageType type) {
        this.type = type;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object... parameters) {
        this.parameters = parameters;
    }

    public RuntimeException getException() {
        return exception;
    }

    public void setException(RuntimeException exception) {
        this.exception = exception;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
}