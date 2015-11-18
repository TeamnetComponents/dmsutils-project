package ro.croco.integration.dms.toolkit.jms;

import java.io.Serializable;
//import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 7/8/2014.
 */
public class JmsMessageStructure implements Serializable {
    private Date date;
    private String method;
    private JmsMessageType type;
    private Object[] parameters;
    private RuntimeException exception;
    private Map<String, String> configuration;

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

    public JmsMessageType getType() {
        return type;
    }

    public void setType(JmsMessageType type) {
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

    /*@Override
    public String toString() {
        return "JmsMessageStructure{" +
                "date=" + date +
                ", method='" + method + '\'' +
                ", type=" + type +
                ", parameters=" + Arrays.toString(parameters) +
                ", exception=" + exception +
                ", configuration=" + configuration +
                '}';
    }*/
}