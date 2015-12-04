package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.FileUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 6/25/2014.
 */
public class DocumentInfo extends ObjectInfo<DocumentIdentifier> {
    private DocumentIdentifier[] documentIdentifierVersions;
    private String extension;

    public DocumentInfo() {
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

    public DocumentInfo(String path, String name, String extension, String type, Map<String, Object> properties) {
        super(path, name, type, properties);
        this.extension = extension;
        String fullPathName = FileUtils.getFileUtilsDMS().concatenate(path, name);
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(fullPathName).build();
        this.setIdentifier(documentIdentifier);
    }

    public DocumentInfo(String filePathNameWithExtension, String type, Map<String, Object> properties) {
        this(
            fileUtils.getParentFolderPathName(filePathNameWithExtension),
            fileUtils.getFileBaseName(filePathNameWithExtension),
            fileUtils.getFileExtension(filePathNameWithExtension),
            type,
            properties
        );
    }

    public DocumentInfo(String filePath, String fileNameWithExtension, String type, Map<String, Object> properties) {
        this(
            filePath,
                fileUtils.getFileBaseName(fileNameWithExtension),
                fileUtils.getFileExtension(fileNameWithExtension),
                type,
                properties
        );

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

    public void setExtension(String extension) {
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
