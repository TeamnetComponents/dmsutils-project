package ro.croco.integration.dms.toolkit;

/**
 * Created by Lucian.Dragomir on 8/20/2014.
 */
public class BooleanResponse extends RequestIdentifier {
    private Boolean value;

    public BooleanResponse() {
    }

    public BooleanResponse(Boolean value){
        this.value = value;
    }

    public BooleanResponse(String requestId, Boolean value) {
        super(requestId);
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}
