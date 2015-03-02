package domain;

import java.io.Serializable;

/**
 * Created by danielpo on 26/08/2014.
 */
public class UpdateDocumentInfo implements Serializable{
    String jcrId;
    String eloId;
    Long documentUploadId;

    public String getJcrId() {
        return jcrId;
    }

    public void setJcrId(String jcrId) {
        this.jcrId = jcrId;
    }

    public String getEloId() {
        return eloId;
    }

    public void setEloId(String eloId) {
        this.eloId = eloId;
    }
}
