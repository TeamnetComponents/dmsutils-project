package ro.croco.integration.dms.commons;


import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.*;

import java.io.*;

/**
 * Created by Lucian.Dragomir on 7/10/2014.
 */

public class DeleteOnCloseInputStream extends InputStream {
    private InputStream inputStream;

    //working with store service
    private StoreService storeService;
    private StoreContext storeContext;
    private ObjectIdentifier objectIdentifier;

    //working with file
    private File file;

    public DeleteOnCloseInputStream(InputStream inputStream, StoreService storeService, StoreContext storeContext, ObjectIdentifier objectIdentifier) {
        this.inputStream = inputStream;
        this.storeService = storeService;
        this.storeContext = storeContext;
        this.objectIdentifier = objectIdentifier;
    }

    public DeleteOnCloseInputStream(File file) throws FileNotFoundException {
        this.file = file;
        this.inputStream = new FileInputStream(this.file);
        this.file.deleteOnExit();
    }

    public DeleteOnCloseInputStream(String filePathName) throws FileNotFoundException {
        this(new File(filePathName));
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        deleteObject();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int hashCode() {
        return inputStream.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return inputStream.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        deleteObject();
    }

    private void deleteObject() {
        try {
            if (objectIdentifier != null) {
                if (objectIdentifier instanceof DocumentIdentifier) {
                    storeService.deleteDocument(storeContext, (ro.croco.integration.dms.toolkit.DocumentIdentifier) objectIdentifier);
                }
                if (objectIdentifier instanceof FolderIdentifier) {
                    storeService.deleteFolder(storeContext, (ro.croco.integration.dms.toolkit.FolderIdentifier) objectIdentifier);
                }
                objectIdentifier = null;
            }
            storeService = null;

            if (file != null) {
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            //at least tried
        }
    }

    public static DocumentStream wrap(DocumentStream documentStream, StoreService storeService, StoreContext storeContext, ObjectIdentifier objectIdentifier) {
        DocumentStream documentStreamResult = new DocumentStream();
        documentStreamResult.setFileName(documentStream.getFileName());
        documentStreamResult.setMimeType(documentStream.getMimeType());
        documentStreamResult.setInputStream(new DeleteOnCloseInputStream(documentStream.getInputStream(), storeService, storeContext, objectIdentifier));
        return documentStreamResult;
    }

    public static DocumentStream wrap(String fileName, String mimeType, String fileContentPathName) {
        DocumentStream documentStreamResult = new DocumentStream();
        documentStreamResult.setFileName(fileName);
        documentStreamResult.setMimeType(mimeType);
        try {
            documentStreamResult.setInputStream(new DeleteOnCloseInputStream(fileContentPathName));
        } catch (FileNotFoundException e) {
            throw new StoreServiceException(e);
        }
        return documentStreamResult;
    }
}