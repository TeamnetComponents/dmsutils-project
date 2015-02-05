package ro.croco.integration.dms.toolkit;

import ro.croco.integration.dms.commons.FileUtils;

import java.io.InputStream;

/**
 * Interfata StoreService
 * - standardizeaza metodele utilizate mentru managementul de documente indiferent de implementarea
 * aleasa (cmis, jcr sau jms)
 * - standardizeaza procesarea documentelor in regim sincron sau asincron
 */
public interface StoreService extends InitableService {

    public enum PathConfiguration {
        TEMP_UPLOAD("store.service.path.temp.upload"),
        TEMP_DOWNLOAD("store.service.path.temp.download"),
        TEMP_RECYCLE("store.service.path.temp.recycle");
        private final String template;

        PathConfiguration(String template) {
            this.template = template;
        }

        public String getTemplate() {
            return template;
        }

        public String getValue(StoreService storeService) {
            String template = (String) storeService.getContextProperty(this.getTemplate());
            //init map
            return FileUtils.replaceTemporalItems(template);
        }
    }

    public String getPathByConfiguration(PathConfiguration pathConfiguration);

    public void close();

    /**
     * Metoda setStoreMetadata permite setarea unei instante de tip StoreMetadata utilizata pentru calculul medatatelor
     * asociate dosumentelor pentru fiecare tip de repository in parte
     *
     * @param metadataService
     */
    public void setMetadataService(MetadataService metadataService);

    public MetadataService getMetadataService();

    public StoreService getLocalStoreService();

    public ObjectIdentifier moveFrom(StoreContext storeContext, ObjectIdentifier objectIdentifierFrom, StoreService storeServiceFrom, StoreContext storeContextFrom);

    public ObjectIdentifier moveTo(StoreContext storeContext, ObjectIdentifier objectIdentifier, StoreService storeServiceTo, StoreContext storeContextTo);

    public ObjectIdentifier[] moveFrom(StoreContext storeContext, ObjectIdentifier[] objectIdentifierFrom, StoreService storeServiceFrom, StoreContext storeContextFrom);

    public ObjectIdentifier[] moveTo(StoreContext storeContext, ObjectIdentifier[] objectIdentifier, StoreService storeServiceTo, StoreContext storeContextTo);

    /*-----------------------------------------------Document  methods---------------------------------------------------------*/


    /**
     * Returns a boolean that specifies if the document exists in the repository.
     * The documentIdentifier parameter can contain the path or the identifier for the searched document (one of them is mandatory) and the document version(optional field)
     * The path from the documentIdentifier should not contain the extension of the file but the node name together with the path from the root folder
     *
     * @param documentIdentifier the identifiers to search for a document: Document Path, Document Identifier or both
     * @return a boolean specifying if the document exists or not in the repository
     */
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier);

    /**
     * This method is used for storing a document inside the content repository.
     * Based on the arguments the file will be stored on the specified path, under the node with the file name without extension and using a specified version
     * If no version is specified the repository's default version will be added
     * The document name must contain the extension
     * If the path doesn't exist in the repository, it can be automatically built if the allowCreatePath argument is set to true, otherwise it will retrieve a PathNotFoundException
     *
     * @param documentInfo    additional information related to the document - ocontains at least path, name [and extension]
     * @param inputStream     the file content
     * @param allowCreatePath the option to create or not the path in case it doesn't exist
     * @return the document identifiers of the document: path, identifier and version
     */
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType);


    public DocumentIdentifier storeDocument(StoreContext storeContext, MetadataService.MetadataProperties metadataProperties, InputStream inputStream);


    /**
     * This method retrieves a document from the content repository.
     * It receives as input parameter the path to that file, including the file name(without extension) and retrieves the latest version of the document.
     * the DocumentStream returned contains the input Stream, the name of the file(including extension) and mime type.
     * Throws a PathNotFoundException in case the file was not found at the specified location.
     * The path should be relative to the root folder of the repository.
     *
     * @param documentIdentifier the identifiers to search for a document: Document Path, Document Identifier or both
     * @return a DocumentStream object containing the input Stream, the name of the file(including extension) and mime type
     */
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier);


    /**
     * This method is used for updating the properties of a document.
     * It takes the list of properties and updates them accordingly to the new values sent in the DocumentInfo parameter.
     * This method is NOT updating effectively the latest version of the document
     *
     * @param documentIdentifier the identifiers to search for a document: Document Path, Document Identifier or both
     * @param documentInfo       the information to be updated - the Map with the properties. If a property is null the old value will be erased
     */
    public DocumentIdentifier updateDocumentProperties(StoreContext storeContext, DocumentIdentifier documentIdentifier, DocumentInfo documentInfo);

    /**
     * This method removes a document from a specified path. The folder path will not be deleted, only the document and all the associated versions.
     *
     * @param documentIdentifier the identifiers to search for a document: Document Path, Document Identifier or both
     */
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier);

    /**
     * This method renames a folder from a specified path.
     *
     * @param documentIdentifier the identifiers to search for a documenr: Document Path, Document Identifier
     * @param newDocumentName    the new name of the document
     */
    public DocumentIdentifier renameDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier, String newDocumentName);

    /**
     * This method retrieves the information of a specific document, without its content.
     * All the properties will be retrieved. If no specific version will be provided in the documentIdentifier attribute, the latest version will be retrieved
     *
     * @param documentIdentifier the identifiers to search for a document: Document Path, Document Identifier or both
     * @return the document information including list of properties
     */
    public DocumentInfo getDocumentInfo(StoreContext storeContext, DocumentIdentifier documentIdentifier);


    /*-----------------------------------------------Folder  methods---------------------------------------------------------*/


    /**
     * Returns a boolean that specifies if the folder exists in the repository.
     * The folderIdentifier parameter can contain the path or the identifier for the searched document (one of them is mandatory) and the document version(optional field)
     * The path from the folderIdentifier should not contain the name of some document, only folder path should be provided
     *
     * @param folderIdentifier the identifiers to search for a document: Folder Path, Folder Identifier or both - at least one required
     * @return a boolean specifying if the document exists or not in the repository
     */
    public BooleanResponse existsFolder(StoreContext storeContext, FolderIdentifier folderIdentifier);

    /**
     * This method is used for creating a folder inside the content repository.
     * Based on the arguments, the folder will be stored on the specified path.
     * If the path doesn't exist in the repository, it can be automatically built if the createParentIfNotExists argument is set to true, otherwise it will retrieve a PathNotFoundException
     *
     * @param folderInfo              additional information related to the folder - optional
     * @param createParentIfNotExists the option to create or not the parent path in case it doesn't exist
     * @return the folder identifiers of the document: path and identifier
     */
    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo, boolean createParentIfNotExists);

    /**
     * This method is used for updating the properties of a specific folder
     * It takes the list of properties and updates them accordingly to the new values sent in the FolderInfo parameter.
     * This method is NOT updating effectively the latest version of the document
     *
     * @param folderIdentifier the identifiers to search for a folder: Path, Identifier or both
     * @param folderInfo       the information to be updated - the Map with the properties. If a property is null the old value will be erased
     */
    public FolderIdentifier updateFolderProperties(StoreContext storeContext, FolderIdentifier folderIdentifier, FolderInfo folderInfo);


    /**
     * This method removes a folder from a specified path. The parent folder path will not be deleted, only the folder and all folders/files below it.
     *
     * @param folderIdentifier the identifiers to search for a folder: Folder Path, Folder Identifier
     */
    public RequestIdentifier deleteFolder(StoreContext storeContext, FolderIdentifier folderIdentifier);

    /**
     * This method renames a folder from a specified path.
     *
     * @param folderIdentifier the identifiers to search for a folder: Folder Path, Folder Identifier
     * @param newFolderName    the new name of the document
     */
    public FolderIdentifier renameFolder(StoreContext storeContext, FolderIdentifier folderIdentifier, String newFolderName);

    /**
     * This method retrieves the information of a specific document, without its content.
     * All the properties will be retrieved. If no specific version will be provided in the documentIdentifier attribute, the latest version will be retrieved
     *
     * @param folderIdentifier the identifiers to search for a folder: Folder Path, Folder Identifier
     * @return the folder information including list of properties if any
     */
    public FolderInfo getFolderInfo(StoreContext storeContext, FolderIdentifier folderIdentifier);

    /**
     * This method return a list of object together with their information.
     * Depending on the objectBaseTypes parameter the list can include either FolderInfo, DocumentInfo or both
     *
     * @param folderIdentifier the identifiers to search for a folder: Folder Path, Folder Identifier
     * @param depth            integer specifying the depth to include subfolders content
     * @param includeInfo      boolean specifying if the retrieved array will contain al information or just base info: path, id
     * @param objectBaseTypes  specifies the type/types to be retrieved. Can be FOLDER, DOCUMENT or both
     * @return an array of FOLDER/DOCUMENT info
     */
    public ObjectInfoTree listFolderContent(StoreContext storeContext, FolderIdentifier folderIdentifier, /*String filter,*/ int depth, boolean includeInfo, ObjectBaseType... objectBaseTypes);

    public String toDMSProperty(String propertyName);

    public String toSSProperty(String propertyName);
}
