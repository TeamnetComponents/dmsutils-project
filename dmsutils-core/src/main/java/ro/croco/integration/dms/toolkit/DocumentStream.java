package ro.croco.integration.dms.toolkit;

import java.io.InputStream;

/**
 * Created by danielp on 7/1/14.
 */
public class DocumentStream extends RequestIdentifier{
    private String fileName;
    private InputStream inputStream;
    private String mimeType;

    public DocumentStream() {
    }

    public DocumentStream(String fileName, InputStream inputStream) {
        this.fileName = fileName;
        this.inputStream = inputStream;
    }

    public DocumentStream(String fileName, InputStream inputStream, String mimeType) {
        this.fileName = fileName;
        this.inputStream = inputStream;
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}

