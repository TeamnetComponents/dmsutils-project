package ro.croco.integration.dms.toolkit;

import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.lang.StringUtils;
import ro.croco.integration.dms.commons.exceptions.ObjectNotFoundException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.cmis.CmisStoreContextTranslator;
import ro.croco.integration.dms.toolkit.cmis.StoreServiceSessionImpl_Cmis;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * .withStoreServiceName(this.getName())
 * Created by Lucian.Dragomir on 6/23/2014.
 */

public class StoreServiceImpl_Cmis extends StoreServiceImpl_Abstract<StoreServiceSessionImpl_Cmis> {

    private static List<String> cmisStandardProperties;
    private static final String[] EMPTY_ARRAY = new String[0];
    private CmisStoreContextTranslator cmisStoreContextTranslator;

//    private static ThreadLocal<StoreServiceSessionImpl_Cmis> threadSession;

//    static {
//        threadSession = new ThreadLocal<StoreServiceSessionImpl_Cmis>() {
//            @Override
//            public void remove() {
//                super.remove();
//                if (this.get() != null) {
//                    try {
//                        this.get().close();
//                    } catch (Exception e) {
//                    }
//                }
//            }
//        };
//    }


    public StoreServiceImpl_Cmis() {
        super();
    }

    @Override
    public void __init(Properties context) throws IOException {
        super.__init(context);

        if (cmisStandardProperties == null) {
            synchronized (context) {
                if (cmisStandardProperties == null) {
                    cmisStandardProperties = getStandardCmisProperties();
                }
            }
        }

        try {
            cmisStoreContextTranslator = CmisStoreContextTranslator.getInstance(getContext());
        } catch (Exception e) {
            throw new CmisConnectionException("Unable to create context translator instance.", e);
        }
    }

    private StoreServiceSessionImpl_Cmis createSession(StoreContext storeContext) {
        StoreServiceSessionImpl_Cmis storeSession = null;
        //create cmis session
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Session cmisSession;

        Properties completeContext = cmisStoreContextTranslator.translate(getContext(), storeContext);

        //Map<String, String> completeContext = null;
        //completeContext = new HashMap<String, String>();
        //completeContext.putAll(getContext());
        //completeContext = toMap(getContext());
        //completeContext.putAll(toMap(storeContext));

        cmisSession = sessionFactory.createSession(CmisStoreContextTranslator.toMap(completeContext));
        storeSession = new StoreServiceSessionImpl_Cmis(cmisSession);
        return storeSession;
    }

//    private StoreServiceSessionImpl_Cmis openSessionThreaded(StoreContext storeContext) {
//        StoreServiceSessionImpl_Cmis storeSession;
//        if (threadSession.get() == null) {
//            threadSession.set(createSession(prepareStoreContext(storeContext)));
//        }
//        storeSession = threadSession.get();
//        return storeSession;
//    }


    @Override
    protected StoreServiceSessionImpl_Cmis openSession(StoreContext storeContext) {
        return createSession(storeContext);
    }

    @Override
    protected void closeSession(StoreServiceSessionImpl_Cmis storeSession) {
        if (storeSession != null) {
            storeSession.close();
        }
        storeSession = null;
    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreServiceSessionImpl_Cmis storeSession = null;
        boolean existsDoc = false;
        try {
            storeSession = openSession(storeContext);
            existsDoc = existsObject(documentIdentifier, storeSession);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
        return new BooleanResponse(storeContext.getRequestIdentifier(), existsDoc);
    }

    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        DocumentIdentifier documentIdentifier = null;
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);

            Document cmisDocument = null;
            if (documentInfo == null) {
                throw new StoreServiceException("DocumentInfo object must not be null");
            }
            String documentPath = StoreServiceImpl_Abstract.fileUtils.getRootPath();
            if (documentInfo.getParentIdentifier() != null) {
                if (documentInfo.getParentIdentifier().getPath() == null || documentInfo.getParentIdentifier().getPath().isEmpty()) {
                    throw new StoreServiceException("ParentIdentifier must have the path completed.");
                }
                documentPath = documentInfo.getParentIdentifier().getPath();
            }
            String documentNameWithExtension = StoreServiceImpl_Abstract.fileUtils.getFileNameWithExtension(StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentInfo.getName()), documentInfo.getExtension());

            //create folder
            Folder cmisFolder = createFolder(documentPath, null, allowCreatePath, storeSession);

            //create file
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.BASE_TYPE_ID, "cmis:document");
            properties.put(PropertyIds.NAME, StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentNameWithExtension));

            String cmisTypeIdOrName = (documentInfo != null && StringUtils.isNotEmpty(documentInfo.getType())) ? documentInfo.getType() : "cmis:document";
            properties = updateProperties(storeSession, cmisTypeIdOrName, properties, documentInfo.getProperties());


//            //get type of the object
//            String cmisTypeId = null;
//
//            ObjectType objectType = getTypeDefinition(storeSession, cmisTypeIdOrName);
//            cmisTypeId = objectType.getId();
//            properties.put(PropertyIds.OBJECT_TYPE_ID, cmisTypeId);
//
//
//            if (documentInfo != null) {
//                if (documentInfo.getProperties() != null) {
//                    for (String key : documentInfo.getProperties().keySet()) {
//                        //fix to translate from specific cmis property format and dmsutils
//                        String cmisPropertyId = null;
//                        PropertyDefinition propertyDefinition = getPropertyDefinition(objectType, key);
//                        cmisPropertyId = propertyDefinition.getId();
//                        properties.put(cmisPropertyId, String.valueOf(documentInfo.getProperties().get(key)));
//                    }
//                }
//            }

            //send document content
            ContentStreamImpl contentStreamImpl = new ContentStreamImpl();
            contentStreamImpl.setFileName(documentNameWithExtension);
            //contentStreamImpl.setLength(length);
            contentStreamImpl.setMimeType(StoreServiceImpl_Abstract.fileUtils.getMimeType(documentNameWithExtension));
            contentStreamImpl.setStream(inputStream);
            cmisDocument = cmisFolder.createDocument(properties, (ContentStream) contentStreamImpl, VersioningState.valueOf(versioningType.name()));

            documentIdentifier = toDocumentIdentifier(cmisDocument, this);
            documentIdentifier.setRequestId(storeContext.getRequestIdentifier());
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
        return documentIdentifier;
    }

    @Override
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        DocumentStream documentStream = null;
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Document document = (Document) getObject(documentIdentifier, storeSession);
            if (!"null".equals(document.getContentStreamId())) {
                documentStream = new DocumentStream(document.getContentStream().getFileName(), document.getContentStream().getStream(), document.getContentStream().getMimeType());
            } else {
                //no content was saved with this document; might be a keywording template file (ELO mask)
                documentStream = new DocumentStream(document.getContentStreamFileName(), null, document.getContentStreamMimeType());
            }
            documentStream.setRequestId(storeContext.getRequestIdentifier());
            return documentStream;
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
    }


    private Map<String, Object> updateProperties(StoreServiceSessionImpl_Cmis storeSession, String cmisTypeIdOrName, Map<String, Object> cmisProperties, Map<String, Object> dmsPoperties) {
        //get type of the object
        String cmisTypeId = null;
        String cmisBaseTypeId = null;
        ObjectType objectType = getTypeDefinition(storeSession, cmisTypeIdOrName);
        cmisTypeId = objectType.getId();
        cmisProperties.put(PropertyIds.OBJECT_TYPE_ID, cmisTypeId);
        cmisBaseTypeId = objectType.isBaseType() ? objectType.getBaseTypeId().value() : objectType.getBaseType().getId();

        cmisProperties.put(PropertyIds.BASE_TYPE_ID, cmisBaseTypeId);
        if (dmsPoperties != null) {
            for (String key : dmsPoperties.keySet()) {
                //fix to translate from specific cmis property format and dmsutils
                String cmisPropertyId = null;
                PropertyDefinition propertyDefinition = getPropertyDefinition(objectType, key);
                cmisPropertyId = propertyDefinition.getId();
                cmisProperties.put(cmisPropertyId, dmsPoperties.get(key));
            }
        }
        return cmisProperties;
    }


    private CmisObject updateObjectProperties(StoreContext storeContext, ObjectIdentifier objectIdentifier, ObjectInfo objectInfo) {
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            CmisObject cmisObject = getObject(objectIdentifier, storeSession);
            Map<String, Object> cmisProperties = updateProperties(storeSession, cmisObject.getType().getId(), new HashMap<String, Object>(), objectInfo.getProperties());
            return cmisObject.updateProperties(cmisProperties);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
    }

    //TODO
    @Override
    public DocumentIdentifier updateDocumentProperties(StoreContext storeContext, DocumentIdentifier documentIdentifier, DocumentInfo documentInfo) {
        CmisObject cmisObject = updateObjectProperties(storeContext, documentIdentifier, documentInfo);
        return toDocumentIdentifier((Document) cmisObject, this);
    }


    @Override
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Document document = (Document) getObject(documentIdentifier, storeSession);
            if (document != null) {
                document.delete(documentIdentifier.getVersion() == null);
                return new RequestIdentifier(storeContext.getRequestIdentifier());
            } else {
                throw new ObjectNotFoundException("Document identifier not found.");
            }
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
    }

    @Override
    public DocumentIdentifier renameDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier, String newDocumentName) {
        return null;
    }

    @Override
    public DocumentInfo getDocumentInfo(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        ObjectInfo objectInfo = null;
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Document document = (Document) getObject(documentIdentifier, storeSession);
            objectInfo = toObjectInfo(document, true, this);
            objectInfo.setRequestId(storeContext.getRequestIdentifier());
            return (DocumentInfo) objectInfo;
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
    }

    @Override
    public BooleanResponse existsFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            return new BooleanResponse(storeContext.getRequestIdentifier(), existsObject(folderIdentifier, storeSession));
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
    }

    @Override
    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo,
                                         boolean createParentIfNotExists) {
        FolderIdentifier folderIdentifier = null;
        StoreServiceSessionImpl_Cmis storeSession = null;
        Folder cmisFolder = null;
        Map<String, Object> properties = new HashMap<String, Object>();
        try {
            storeSession = openSession(storeContext);

            String folderPath = StoreServiceImpl_Abstract.fileUtils.getRootPath();
            if (folderInfo.getParentIdentifier() != null) {
                if (folderInfo.getParentIdentifier().getPath() == null || folderInfo.getParentIdentifier().getPath().isEmpty()) {
                    throw new StoreServiceException("ParentIdentifier must have the path completed.");
                }
                folderPath = folderInfo.getParentIdentifier().getPath();
            }
            String folderName = StoreServiceImpl_Abstract.fileUtils.getFolderName(folderInfo.getName());
            String folderPathName = StoreServiceImpl_Abstract.fileUtils.concatenate(folderPath, folderName);


            properties.put(PropertyIds.BASE_TYPE_ID, "cmis:folder");


            //get type of the object
            String cmisTypeId = null;
            String cmisTypeIdOrName = (folderInfo != null && StringUtils.isNotEmpty(folderInfo.getType())) ? folderInfo.getType() : "cmis:folder";
            ObjectType objectType = getTypeDefinition(storeSession, cmisTypeIdOrName);
            cmisTypeId = objectType.getId();
            properties.put(PropertyIds.OBJECT_TYPE_ID, cmisTypeId);


            properties = updateProperties(storeSession, cmisTypeIdOrName, properties, folderInfo.getProperties());

            cmisFolder = createFolder(folderPathName, properties, createParentIfNotExists, storeSession);
            folderIdentifier = toFolderIdentifier(cmisFolder, this);
            folderIdentifier.setRequestId(storeContext.getRequestIdentifier());
        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
        return folderIdentifier;
    }

    @Override
    public FolderIdentifier updateFolderProperties(StoreContext storeContext, FolderIdentifier folderIdentifier, FolderInfo folderInfo) {
        CmisObject cmisObject = updateObjectProperties(storeContext, folderIdentifier, folderInfo);
        return toFolderIdentifier((Folder) cmisObject, this);
    }

    @Override
    public RequestIdentifier deleteFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Folder cmisFolder = (Folder) getObject(folderIdentifier, storeSession);
            cmisFolder.delete(true);
            return new RequestIdentifier(storeContext.getRequestIdentifier());
        } catch (RuntimeException e) {
            throw new StoreServiceException(e);
        } finally {
            closeSession(storeSession);
        }
    }

    @Override
    public FolderIdentifier renameFolder(StoreContext storeContext, FolderIdentifier folderIdentifier, String newFolderName) {
        FolderIdentifier folderIdentifierReturn = null;
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Folder cmisFolder = (Folder) getObject(folderIdentifier, storeSession);

            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(PropertyIds.NAME, newFolderName);
            cmisFolder = (Folder) cmisFolder.updateProperties(properties);
            folderIdentifierReturn = toFolderIdentifier(cmisFolder, this);
            folderIdentifierReturn.setRequestId(storeContext.getRequestIdentifier());
        } catch (RuntimeException e) {
            throw new StoreServiceException(e);
        } finally {
            closeSession(storeSession);
        }
        return folderIdentifierReturn;
    }

    @Override
    protected FolderInfo getFolderInfo(StoreServiceSessionImpl_Cmis storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier) {
        ObjectInfo objectInfo = null;
        Folder folder = (Folder) getObject(folderIdentifier, storeSession);
        objectInfo = toObjectInfo(folder, true, this);
        objectInfo.setRequestId(storeContext.getRequestIdentifier());
        return (FolderInfo) objectInfo;
    }

    @Override
    public FolderInfo getFolderInfo(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        ObjectInfo objectInfo = null;
        StoreServiceSessionImpl_Cmis storeSession = null;
        try {
            storeSession = openSession(storeContext);
            objectInfo = getFolderInfo(storeSession, storeContext, folderIdentifier);
//            Folder folder = (Folder) getObject(folderIdentifier, storeSession);
//            objectInfo = toObjectInfo(folder, true, this);
//            objectInfo.setRequestId(storeContext.getRequestIdentifier());

        } catch (RuntimeException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
        return (FolderInfo) objectInfo;
    }

    private String getFolderPath(Folder folder) {
        String path = folder.getPropertyValue(PropertyIds.PATH);
        if (StringUtils.isEmpty(path)) {
            List<String> folderPaths = folder.getPaths();
            if (folderPaths.size() > 0) {
                path = folderPaths.get(0);
            }
        }
        return path;
    }

    private ObjectInfo[] listFolderContent(StoreServiceSessionImpl_Cmis storeSession, FolderIdentifier folderIdentifier, boolean includeInfo, int skipCount, int maxItemsPerPage, ObjectBaseType... objectBaseTypes) {
        List<ObjectInfo> objectInfoList = new ArrayList<ObjectInfo>();
        if (maxItemsPerPage <= 0) {
            maxItemsPerPage = Integer.MAX_VALUE;
        }
        if (skipCount < 0) {
            skipCount = 0;
        }
        List<ObjectBaseType> objectBaseTypeList = Arrays.asList(objectBaseTypes);

        Folder folder = (Folder) getObject(folderIdentifier, storeSession);
        OperationContext operationContext = storeSession.getCmisSession().createOperationContext();
        operationContext.setMaxItemsPerPage(maxItemsPerPage);
        //System.out.println(folder.getPath());
        ItemIterable<CmisObject> children = folder.getChildren(operationContext);
        ItemIterable<CmisObject> page = children.skipTo(skipCount).getPage();

        //System.out.println("-------------------------------------------");
        Iterator<CmisObject> pageItems = page.iterator();
        while (pageItems.hasNext()) {
            CmisObject cmisObject = pageItems.next();
            if (cmisObject != null) {
                //System.out.println(cmisObject.getName());
                if (
                        (objectBaseTypeList.contains(ObjectBaseType.FOLDER) && cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_FOLDER))
                                ||
                                (objectBaseTypeList.contains(ObjectBaseType.DOCUMENT) && cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT))
                        ) {
                    String parentPath = getFolderPath(folder);
                    ObjectInfo objectInfo = toObjectInfo(cmisObject, includeInfo, this, parentPath);
                    if (objectInfo.getParentIdentifier() == null) {
                        System.out.println(objectInfo.getParentIdentifier());
                    }
                    System.out.println(objectInfo.getParentIdentifier().getPath() + "/" + objectInfo.getName());
                    objectInfoList.add(objectInfo);
                }
            }
        }
        //System.out.println("-------------------------------------------");
        ObjectInfo[] objectInfos = new ObjectInfo[0];
        return objectInfoList.toArray(objectInfos);
    }

    @Override
    protected ObjectInfo[] listFolderContent(StoreServiceSessionImpl_Cmis storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        int maxItemsPerPage = Integer.MAX_VALUE;
        int skipCount = 0;
        return listFolderContent(storeSession, folderIdentifier, includeInfo, skipCount, maxItemsPerPage, objectBaseTypes);
    }

//    @Override
//    protected ObjectInfo[] listFolderContent(StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
//        int maxItemsPerPage = Integer.MAX_VALUE;
//        int skipCount = 0;
//        Session session = null;
//        try {
//            session = openSession(storeContext);
//            ObjectInfo[] objectInfos = listFolderContent(session, folderIdentifier, includeInfo, skipCount, maxItemsPerPage, objectBaseTypes);
//            return objectInfos;
//        } catch (RuntimeException e) {
//            throw e;
//        } finally {
//            closeSession(session);
//        }
//    }
//
//
//    private Tree<ObjectInfo> listFolderContentTree(StoreContext storeContext, FolderIdentifier folderIdentifier, int depth, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
//        List<ObjectBaseType> objectBaseTypeList = Arrays.asList(objectBaseTypes);
//        FolderInfo folderInfo = getFolderInfo(storeContext, folderIdentifier);
//        ObjectInfo[] objectInfos = listFolderContent(storeContext, folderIdentifier, includeInfo, ObjectBaseType.FOLDER, ObjectBaseType.DOCUMENT);
//        List<Tree<ObjectInfo>> children = new ArrayList<Tree<ObjectInfo>>();
//        for (ObjectInfo objectInfo : objectInfos) {
//            if (objectInfo instanceof FolderInfo) {
//                if (objectBaseTypeList.contains(ObjectBaseType.FOLDER)) {
//                    Tree<ObjectInfo> child = new TreeImpl(objectInfo, null);
//                    children.add(child);
//                }
//                if (depth > 1) {
//                    children.add(listFolderContentTree(storeContext, (FolderIdentifier) objectInfo.getIdentifier(), depth - 1, includeInfo, objectBaseTypes));
//                }
//            } else if (objectInfo instanceof DocumentInfo) {
//                Tree<ObjectInfo> child = new TreeImpl(objectInfo, null);
//                children.add(child);
//            }
//        }
//        return new TreeImpl(folderInfo, children);
//    }
//
//    @Override
//    public ObjectInfoTree listFolderContent(StoreContext storeContext, FolderIdentifier folderIdentifier, /*String filter,*/ int depth, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
//        ObjectInfoTree objectInfoTree = new ObjectInfoTree();
//        objectInfoTree.setContent(listFolderContentTree(storeContext, folderIdentifier, depth, includeInfo, objectBaseTypes));
//        objectInfoTree.setRequestId(storeContext.getRequestIdentifier());
//        return objectInfoTree;
//    }

    private Folder createFolder(String folderPathName, Map<String, Object> folderProperties, boolean recursive, StoreServiceSessionImpl_Cmis storeSession) {
        Folder cmisFolder = null;
        cmisFolder = (Folder) getObject(FolderIdentifier.builder().withPath(folderPathName).build(), storeSession);
        if (cmisFolder == null) {
            Folder cmisFolderParent = null;
            String folderShortName = StoreServiceImpl_Abstract.fileUtils.getFolderName(folderPathName);
            String folderParentPath = StoreServiceImpl_Abstract.fileUtils.getParentFolderPathName(folderPathName);
            cmisFolderParent = (Folder) getObject(FolderIdentifier.builder().withPath(folderParentPath).build(), storeSession);
            if (cmisFolderParent == null) {
                if (recursive) {
                    cmisFolderParent = createFolder(folderParentPath, null, recursive, storeSession);
                } else {
                    throw new ObjectNotFoundException("The parent folder does not exists");
                }
            }

            //create requested folder
            if (folderProperties == null) {
                folderProperties = new HashMap<String, Object>();
                //TODO - ar trebui extrase utilizand storeMetadata pentru a crea folderele pe mastile reale (nu pe default-uri)
            }
            if (!folderProperties.containsKey(PropertyIds.OBJECT_TYPE_ID)) {
                folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            }
            folderProperties.put(PropertyIds.NAME, folderShortName);
            cmisFolder = cmisFolderParent.createFolder(folderProperties);
            cmisFolder = (Folder) storeSession.getCmisSession().getObjectByPath(folderPathName);
        }
        return cmisFolder;
    }


    private boolean matchTypeDefinition(ObjectType objectType, String cmisTypeIdOrName) {
        if (objectType == null || StringUtils.isEmpty(cmisTypeIdOrName)) {
            return false;
        }
        return (cmisTypeIdOrName.equals(objectType.getId()) || cmisTypeIdOrName.equals(objectType.getDisplayName()));
    }

    private ObjectType findTypeDefinition(List<Tree<ObjectType>> treeList, String cmisTypeIdOrName) {
        if (treeList == null) {
            return null;
        }
        for (Tree<ObjectType> tree : treeList) {
            if (matchTypeDefinition(tree.getItem(), cmisTypeIdOrName)) {
                return tree.getItem();
            }
            ObjectType objectType = findTypeDefinition(tree.getChildren(), cmisTypeIdOrName);
            if (objectType != null) {
                return objectType;
            }
        }
        return null;
    }

    private ObjectType getTypeDefinition(StoreServiceSessionImpl_Cmis storeSession, String cmisTypeIdOrName) {
        ObjectType objectType = null;
        if (StringUtils.isEmpty(cmisTypeIdOrName)) {
            throw new CmisObjectNotFoundException("Unable to find type <" + cmisTypeIdOrName + ">");
        }
        try {
            objectType = storeSession.getCmisSession().getTypeDefinition(cmisTypeIdOrName);
        } catch (CmisObjectNotFoundException e) {
            //fallback to search by typeName
        }
        if (objectType == null) {
            List<Tree<ObjectType>> typeDescendants = storeSession.getCmisSession().getTypeDescendants(null, -1, false);
            objectType = findTypeDefinition(typeDescendants, cmisTypeIdOrName);
            if (objectType != null) {
                objectType = storeSession.getCmisSession().getTypeDefinition(objectType.getId());
            }
        }
        if (objectType == null) {
            throw new CmisObjectNotFoundException("Unable to find type <" + cmisTypeIdOrName + ">");
        }
        return objectType;
    }

    private boolean matchPropertyDefinition(PropertyDefinition propertyDefinition, String cmisPropertyIdOrName) {
        if (propertyDefinition == null || StringUtils.isEmpty(cmisPropertyIdOrName)) {
            return false;
        }
        return (cmisPropertyIdOrName.equals(propertyDefinition.getId()) ||
                toSSProperty(cmisPropertyIdOrName).equals(propertyDefinition.getId()) ||
                cmisPropertyIdOrName.equals(propertyDefinition.getDisplayName()) ||
                toSSProperty(cmisPropertyIdOrName).equals(propertyDefinition.getDisplayName())
        );
    }

    private PropertyDefinition getPropertyDefinition(ObjectType objectType, String cmisPropertyIdOrName) {
        PropertyDefinition propertyDefinition = null;
        if (StringUtils.isEmpty(cmisPropertyIdOrName)) {
            throw new CmisObjectNotFoundException("Unable to find property <" + cmisPropertyIdOrName + ">");
        }
        if (objectType.getPropertyDefinitions() != null) {
            propertyDefinition = objectType.getPropertyDefinitions().get(cmisPropertyIdOrName);
        }
        if (propertyDefinition == null) {
            for (PropertyDefinition propertyDefinitionSearch : objectType.getPropertyDefinitions().values()) {
                if (matchPropertyDefinition(propertyDefinitionSearch, cmisPropertyIdOrName)) {
                    propertyDefinition = propertyDefinitionSearch;
                    break;
                }
            }
        }
        if (propertyDefinition == null) {
            throw new CmisObjectNotFoundException("Unable to find property <" + cmisPropertyIdOrName + "> for type <" + objectType.getId() + ">");
        }
        return propertyDefinition;
    }

    private CmisObject getObject(ObjectIdentifier objectIdentifier, StoreServiceSessionImpl_Cmis storeSession) {
        try {
            if (objectIdentifier.getId() != null) {
                return storeSession.getCmisSession().getObject(objectIdentifier.getId());
            } else if (objectIdentifier.getPath() != null) {
                CmisObject cmisObject = storeSession.getCmisSession().getObjectByPath(objectIdentifier.getPath());
                if (objectIdentifier instanceof DocumentIdentifier) {
                    String version = ((DocumentIdentifier) objectIdentifier).getVersion();
                    if (version != null) {
                        for (Document document : ((Document) cmisObject).getAllVersions()) {
                            if (document.getVersionLabel().equalsIgnoreCase(version)) {
                                return document;
                            }
                        }
                    } else {
                        return cmisObject;
                    }
                    throw new CmisVersioningException("Requested version does not exists");
                }
                if (objectIdentifier instanceof FolderIdentifier) {
                    return cmisObject;
                }
            }
        } catch (CmisObjectNotFoundException e) {
            return null;
        }
        throw new CmisConstraintException("At least one of id or path must be non null when searching a cmis object");
    }

    private boolean existsObject(ObjectIdentifier objectIdentifier, StoreServiceSessionImpl_Cmis storeSession) {
        return getObject(objectIdentifier, storeSession) != null;
    }

    private static FolderIdentifier toFolderIdentifier(Folder folder, StoreService storeService) {
        return toFolderIdentifier(folder, storeService, EMPTY_ARRAY);
    }

    private static FolderIdentifier toFolderIdentifier(Folder folder, StoreService storeService, String... parentPaths) {
        FolderIdentifier folderIdentifier = null;
        FolderIdentifier.Builder folderIdentifierBuilder = FolderIdentifier.builder();
        folderIdentifierBuilder.withStoreServiceName(storeService != null ? storeService.getName() : null);
        folderIdentifierBuilder.withId(folder.getId());
        if ((parentPaths != null) && (parentPaths.length > 0)) {
            folderIdentifierBuilder.withPath(fileUtils.concatenate(parentPaths[0], folder.getName()));
        } else {
            String path = folder.getPropertyValue(PropertyIds.PATH);
            if (StringUtils.isNotEmpty(path)) {
                folderIdentifierBuilder.withPath(path);
            } else {
                List<String> folderPaths = folder.getPaths();
                //System.out.println("size: " + documentPaths.size(
                if (folderPaths.size() > 0) {
                    folderIdentifierBuilder.withPath(folderPaths.get(0));
                }
            }
        }
        folderIdentifier = folderIdentifierBuilder.build();
        return folderIdentifier;
    }

    private static DocumentIdentifier toDocumentIdentifier(Document document, StoreService storeService) {
        return toDocumentIdentifier(document, storeService, EMPTY_ARRAY);
    }

    private static DocumentIdentifier toDocumentIdentifier(Document document, StoreService storeService, String... parentPaths) {
        DocumentIdentifier documentIdentifier = null;
        DocumentIdentifier.Builder documentIdentifierBuilder = DocumentIdentifier.builder();
        documentIdentifierBuilder.withStoreServiceName(storeService != null ? storeService.getName() : null);
        documentIdentifierBuilder.withId(document.getId());

        if ((parentPaths != null) && (parentPaths.length > 0)) {
            documentIdentifierBuilder.withPath(fileUtils.concatenate(parentPaths[0], document.getName()));
        } else {
            String path = document.getPropertyValue(PropertyIds.PATH);
            if (StringUtils.isNotEmpty(path)) {
                documentIdentifierBuilder.withPath(path);
            } else {
                //document.getProperty()
                List<String> documentPaths = document.getPaths();
                //System.out.println("size: " + documentPaths.size(
                if (documentPaths.size() > 0) {
                    documentIdentifierBuilder.withPath(documentPaths.get(0));
                }
            }
        }
        documentIdentifierBuilder.withVersion(document.getVersionLabel());
        documentIdentifier = documentIdentifierBuilder.build();
        return documentIdentifier;
    }

    private static ObjectInfo toObjectInfo(CmisObject cmisObject, boolean includeInfo, StoreService storeService) {
        return toObjectInfo(cmisObject, includeInfo, storeService, EMPTY_ARRAY);
    }

    private static ObjectInfo toObjectInfo(CmisObject cmisObject, boolean includeInfo, StoreService storeService, String... parentPaths) {
        ObjectInfo objectInfo = null;
        //set object identifier
        if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_FOLDER)) {
            objectInfo = new FolderInfo();
            objectInfo.setIdentifier(toFolderIdentifier((Folder) cmisObject, storeService, parentPaths));
            objectInfo.setName(cmisObject.getName());
            objectInfo.setParentIdentifier(toFolderIdentifier(((Folder) cmisObject).getFolderParent(), storeService));
            //objectInfo.setType(ObjectBaseType.FOLDER.name());
        } else if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
            objectInfo = new DocumentInfo();
            DocumentIdentifier documentIdentifier = toDocumentIdentifier((Document) cmisObject, storeService, parentPaths);
            objectInfo.setIdentifier(documentIdentifier);
            objectInfo.setName(cmisObject.getName());
            if ((documentIdentifier.getPath() != null) && (!documentIdentifier.getPath().isEmpty())) {
                objectInfo.setParentIdentifier(FolderIdentifier.builder().withPath(StoreServiceImpl_Abstract.fileUtils.getParentFolderPathName(documentIdentifier.getPath())).build());
            }
            //objectInfo.setType(ObjectBaseType.DOCUMENT.name());
            //objectInfo.setType(cmisObject.getType().getId());
            //set extension
            Document cmisDocument = (Document) cmisObject;
            String cmisContentStreamId = ((Document) cmisObject).getContentStreamId();
            String cmisContentStreamFileName = cmisDocument.getContentStreamFileName();
            if (StringUtils.isNotEmpty(cmisContentStreamFileName)) {
                ((DocumentInfo) objectInfo).setExtension(StoreServiceImpl_Abstract.fileUtils.getFileExtension(cmisContentStreamFileName));
            }
//            try {
//                if ((cmisContentStreamId != null) && (!cmisContentStreamId.equalsIgnoreCase("null"))) {
//                    if ((cmisDocument.getContentStream() != null) && (cmisDocument.getContentStream().getFileName() != null)) {
//                        ((DocumentInfo) objectInfo).setExtension(StoreServiceImpl_Abstract.fileUtils.getFileExtension(cmisDocument.getContentStream().getFileName()));
//                    }
//                    cmisDocument.getContentStream()
//                }
//            } catch (Exception e) {
//                //unable to get the ContentStream
//                //((Document) cmisObject).getContentStreamId();
//            }

        }

        objectInfo.setType(cmisObject.getType().getId());

        if (includeInfo) {

            //set properties
            Map<String, Object> properties = new HashMap<String, Object>();
            List<Property<?>> cmisProperties = cmisObject.getProperties();
            Iterator<Property<?>> iterator = cmisProperties.iterator();
            while (iterator.hasNext()) {
                Property<?> propertyData = iterator.next();
                //do not add cmis standard properties
                if (!cmisStandardProperties.contains(propertyData.getId())) {
                    PropertyFormats propertyFormat = PropertyFormats.valueFrom((String) storeService.getContextProperty(SERVICE_STORE_PROPERTY_FORMAT));
                    String propertyKey = null;
                    if (propertyFormat.equals(PropertyFormats.ID)) {
                        propertyKey = storeService.toDMSProperty(propertyData.getId());
                    } else if (propertyFormat.equals(PropertyFormats.NAME)) {
                        propertyKey = storeService.toDMSProperty(propertyData.getDisplayName());
                    } else {
                        propertyKey = propertyData.getId();
                    }
                    properties.put(propertyKey, propertyData.getValue());
                }
            }
            objectInfo.setProperties(properties);

            //set creation and modification date
            if (cmisObject.getLastModificationDate() != null) {
                objectInfo.setModificationDate(cmisObject.getLastModificationDate().getGregorianChange());
            }
            if (cmisObject.getCreationDate() != null) {
                objectInfo.setCreationDate(cmisObject.getCreationDate().getGregorianChange());
            }

            //set versions
            if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
                List<DocumentIdentifier> documentIdentifierList = new ArrayList<DocumentIdentifier>();
                for (Document document : ((Document) cmisObject).getAllVersions()) {
                    DocumentIdentifier documentIdentifier = toDocumentIdentifier(document, storeService);
                    documentIdentifierList.add(documentIdentifier);
                }
                //TODO - dc. nu avre versions trebuie sa adaug un self identifier on the array
                DocumentIdentifier[] versions = new DocumentIdentifier[0];
                ((DocumentInfo) objectInfo).setDocumentIdentifierVersions(documentIdentifierList.toArray(versions));
            }
        }
        return objectInfo;
    }

    private static List<String> getStandardCmisProperties() {
        List<String> cmisProperties = new ArrayList<String>();
        Field[] declaredFields = PropertyIds.class.getDeclaredFields();
        String cmisPropertyPrefix = "cmis:";
        for (Field field : declaredFields) {
            if (Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())) {
                String fieldValue = null;
                try {
                    fieldValue = String.valueOf(field.get(null));
                } catch (IllegalAccessException e) {

                }
                if (fieldValue != null && fieldValue.startsWith(cmisPropertyPrefix)) {
                    cmisProperties.add(fieldValue);
                }
            }
        }
        return cmisProperties;
    }
}
