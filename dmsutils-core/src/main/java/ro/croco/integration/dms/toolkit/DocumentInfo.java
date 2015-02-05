package ro.croco.integration.dms.toolkit;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 6/25/2014.
 */
public class DocumentInfo extends ObjectInfo<DocumentIdentifier> {
    private DocumentIdentifier[] documentIdentifierVersions;
    private String extension;

    DocumentInfo() {
        super();
    }

    public DocumentInfo(Map<String, Object> properties) {
        super(properties);
    }

    public DocumentInfo(String type) {
        super(type);
    }

    public DocumentInfo(String type, Map<String, Object> properties) {
        super(type, properties);
    }

    public DocumentInfo(String filePathNameWithExtension, String type, Map<String, Object> properties) {
        super(fileUtils.getParentFolderPathName(filePathNameWithExtension), fileUtils.getFileBaseName(filePathNameWithExtension), type, properties);
        this.extension = fileUtils.getFileExtension(filePathNameWithExtension);
    }

    public DocumentInfo(String path, String name, String extension, String type, Map<String, Object> properties) {
        super(path, name, type, properties);
        this.extension = extension;
    }

    public DocumentInfo(String filePath, String fileNameWithExtension, String type, Map<String, Object> properties) {
        super(filePath, fileUtils.getFileBaseName(fileNameWithExtension), type, properties);
        this.extension = fileUtils.getFileExtension(fileNameWithExtension);
    }

    public DocumentIdentifier[] getDocumentIdentifierVersions() {
        return documentIdentifierVersions;
    }

    void setDocumentIdentifierVersions(DocumentIdentifier[] documentIdentifierVersions) {
        this.documentIdentifierVersions = documentIdentifierVersions;
    }

    public String getExtension() {
        return extension;
    }

    void setExtension(String extension) {
        this.extension = extension;
    }


    @Override
    public String toString() {
        return "DocumentInfo{" +
                "documentIdentifierVersions=" + Arrays.toString(documentIdentifierVersions) +
                ", extension='" + extension + '\'' +
                "} " + super.toString();
    }
}
