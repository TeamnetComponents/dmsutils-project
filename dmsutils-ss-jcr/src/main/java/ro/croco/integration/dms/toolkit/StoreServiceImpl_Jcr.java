package ro.croco.integration.dms.toolkit;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.TransientRepository;
import ro.croco.integration.dms.commons.exceptions.ObjectNotFoundException;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.exceptions.VersionNotFoundException;
import ro.croco.integration.dms.toolkit.jcr.JcrRegisterCustomNodetypes;
import ro.croco.integration.dms.toolkit.jcr.StoreServiceSessionImpl_Jcr;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Lucian.Dragomir on 6/23/2014.
 */
public class StoreServiceImpl_Jcr extends StoreServiceImpl_Abstract<StoreServiceSessionImpl_Jcr> {
    private static final String JCR_CONN_TYPE_JNDI = "conn_jndi";
    private static final String JCR_CONN_TYPE_JNDI_REMOTE = "conn_jndi_remote";
    private static final String JCR_CONN_TYPE_RMI = "conn_rmi";
    private static final String JCR_CONN_TYPE_LOCAL = "conn_local";
    private static final String JCR_FILENAME_PROPERTY = "fileName";
    public static final String JCR_MIX_FILE_ATTRIBUTES = "mix:fileAttributes";

    private static List<String> jcrStandardProperties;

//    private static ThreadLocal<StoreServiceSessionImpl_Jcr> sessionThreadLocal = new ThreadLocal<StoreServiceSessionImpl_Jcr>() {
//        @Override
//        public void remove() {
//            try {
//                if (sessionThreadLocal != null) {
//                    sessionThreadLocal.get().close();
//                    sessionThreadLocal.remove();
//                }
//            } catch (Exception e) {
//            }
//        }
//    };


    public StoreServiceImpl_Jcr() {
        super();
    }

    @Override
    public void __init(Properties context) throws IOException {
        super.__init(context);
        if (jcrStandardProperties == null) {
            synchronized (context) {
                if (jcrStandardProperties == null) {
                    jcrStandardProperties = getStandardJCRProperties();
                }
            }
        }
    }

    @Override
    public StoreServiceSessionImpl_Jcr openSession(StoreContext storeContext) {
        StoreServiceSessionImpl_Jcr storeSession = null;
        storeSession = openSession_internal(storeContext);
        //System.out.println("******* OPENED JCR SESSION " + storeSession.toString());
        return storeSession;
    }

    @Override
    public void closeSession(StoreServiceSessionImpl_Jcr storeSession) {
        if (storeSession != null) {
            storeSession.close();
            //System.out.println("******* CLOSED JCR SESSION " + storeSession.toString());
        }
        storeSession = null;
    }

    private StoreServiceSessionImpl_Jcr openSession_internal(StoreContext storeContext) {
        StoreServiceSessionImpl_Jcr storeSession = null;
        Date startDate = new Date();
        Repository repository = null;
        Session session = null;

        String username = (String) getContextProperty("jcr.repo.username");
        String password = (String) getContextProperty("jcr.repo.password");
        String jcrConnType = (String) getContextProperty("jcr.repo.connection.type");
        try {
            if (JCR_CONN_TYPE_JNDI.equals(jcrConnType)) {
                String repoJndi = (String) getContextProperty("jcr.repo.jndi.address");
                Context ctx;
                try {
                    ctx = new InitialContext();
                    repository = (Repository) ctx.lookup(repoJndi);
                    session = repository.login(new SimpleCredentials(username, password.toCharArray()), "default");
                    storeSession = new StoreServiceSessionImpl_Jcr(null, session);
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            } else if (JCR_CONN_TYPE_JNDI_REMOTE.equals(jcrConnType)) {
                String repoJndi = (String) getContextProperty("jcr.repo.jndi.address");
                Context ctx;
                try {
                    Properties env1 = new Properties();
                    env1.put(Context.INITIAL_CONTEXT_FACTORY, getContextProperty("jcr.repo.jndi.remote.initial.context.factory"));
                    env1.put(Context.PROVIDER_URL, getContextProperty("jcr.repo.jndi.remote.provided.url"));
                    ctx = new InitialContext(env1);
                    repository = (Repository) ctx.lookup(repoJndi);
                    session = repository.login(new SimpleCredentials(username, password.toCharArray()), "default");
                    storeSession = new StoreServiceSessionImpl_Jcr(null, session);
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            } else if (JCR_CONN_TYPE_RMI.equals(jcrConnType)) {
                String repoURL = (String) getContextProperty("jcr.repo.rmi.address");
                repository = JcrUtils.getRepository(repoURL);
                session = repository.login(new SimpleCredentials(username, password.toCharArray()), "default");
                storeSession = new StoreServiceSessionImpl_Jcr(null, session);
            } else if (JCR_CONN_TYPE_LOCAL.equals(jcrConnType)) {
                String repoConfigFilePath = (String) getContextProperty("jcr.repo.local.config.file");
                //File repoConfig = new File(repoConfigFilePath);
                repository = new TransientRepository(repoConfigFilePath, StoreServiceImpl_Abstract.fileUtils.getParentFolderPathName(repoConfigFilePath));
                session = repository.login(new SimpleCredentials(username, password.toCharArray()), "default");
                storeSession = new StoreServiceSessionImpl_Jcr(repository, session);
            }
        } catch (RepositoryException e) {
            throw new StoreServiceException(e);
        }
        if (session == null)
            throw new StoreServiceException("An error was encountered while trying to retrieve a JCR Repository Session! Please check the openSession functionality and the connection Type ");
        Date endDate = new Date();
        return storeSession;
    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreServiceSessionImpl_Jcr storeSession = null;
        boolean existsDoc = false;
        try {
            storeSession = openSession(storeContext);
            Node node = getObject(documentIdentifier, storeSession, false);
            if (ObjectBaseType.DOCUMENT.equals(getObjectBaseType(node))) {
                existsDoc = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new StoreServiceException("There was a RepositoryEception in the existsDocument method!!!", e);
        } finally {
            closeSession(storeSession);
        }
        return new BooleanResponse(storeContext.getRequestIdentifier(), existsDoc);
    }

    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        DocumentIdentifier documentIdentifier = null;
        StoreServiceSessionImpl_Jcr storeSession = null;
        try {
            storeSession = openSession(storeContext);
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

            Node root = storeSession.getJcrSession().getRootNode();
            //log.info("Returning or creating the path:" + documentPath);
            Node currentNode = returnNode(documentPath, root, allowCreatePath);
            if (currentNode == null) {
                //log.error("Error while retrieving the node needed for adding the JCR nt:file node");
                throw new StoreServiceException("Error while retrieving the node needed for adding the JCR nt:file node");
            }
            Node fileNode = null;
            Node resNode = null;
            try {
                fileNode = currentNode.getNode(StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentNameWithExtension));
                resNode = fileNode.getNode(JcrConstants.JCR_CONTENT);
                storeSession.getJcrSession().getWorkspace().getVersionManager().checkout(resNode.getPath());
            } catch (javax.jcr.PathNotFoundException e) {
                fileNode = currentNode.addNode(StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentNameWithExtension), JcrConstants.NT_FILE);
                resNode = fileNode.addNode(JcrConstants.JCR_CONTENT, JcrConstants.NT_RESOURCE);
                resNode.addMixin(JcrConstants.MIX_VERSIONABLE);
            }

            //set document mask
            String documentType = JCR_MIX_FILE_ATTRIBUTES;
            if (documentInfo.getType() != null && !documentInfo.getType().isEmpty()) {
                documentType = documentInfo.getType();
            }


            ValueFactory valueFactory = storeSession.getJcrSession().getValueFactory();
            Binary contentValue = valueFactory.createBinary(inputStream);
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(lastModified.getTimeInMillis());
            //Adding the "file" type node with the name of the file
            //log.info("-------------Adding the file " + documentNameWithExtension);
            //sets the properties
            resNode.setProperty(JcrConstants.JCR_MIMETYPE, StoreServiceImpl_Abstract.fileUtils.getMimeType(documentNameWithExtension));
            resNode.setProperty(JcrConstants.JCR_DATA, contentValue);
            resNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);

            JcrRegisterCustomNodetypes.addNtResourceMixinType(storeSession.getJcrSession());

            resNode.addMixin(documentType);
            resNode.setProperty(JCR_FILENAME_PROPERTY, documentNameWithExtension);

            //set the rest of provided properties
            if (documentInfo.getProperties() != null) {
                for (String propertyName : documentInfo.getProperties().keySet()) {
                    if (documentInfo.getProperties().get(propertyName) == null || documentInfo.getProperties().get(propertyName) instanceof String) {
                        String propertyValueString = (String) documentInfo.getProperties().get(propertyName);
                        resNode.setProperty(propertyName, propertyValueString);
                    } else {
                        throw new StoreServiceException("Properties of type " + documentInfo.getProperties().get(propertyName).getClass().getCanonicalName() + " not supported.");
                    }
                }
            }


            storeSession.getJcrSession().save();
            storeSession.getJcrSession().getWorkspace().getVersionManager().checkin(resNode.getPath());
            Version version = retrieveLatestVersion(DocumentIdentifier.builder().withPath(StoreServiceImpl_Abstract.fileUtils.concatenate(documentPath, StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentNameWithExtension))).build(), storeSession);
            documentIdentifier = DocumentIdentifier.builder().withPath(StoreServiceImpl_Abstract.fileUtils.concatenate(documentPath, StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentNameWithExtension))).withId(resNode.getIdentifier()).withVersion(version.getName()).build();
            documentIdentifier.setRequestId(storeContext.getRequestIdentifier());

        } catch (RepositoryException e) {
            throw new StoreServiceException("Unable to store File due to Repository Exception", e);
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
        StoreServiceSessionImpl_Jcr storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Node fileNode = getObject(documentIdentifier, storeSession, true);
            if (ObjectBaseType.DOCUMENT.equals(getObjectBaseType(fileNode))) {
                InputStream inputStream = null;
                String fileName = null;
                String mimeType = null;
                Node jcrContent = fileNode.getNode(JcrConstants.JCR_CONTENT);
                inputStream = jcrContent.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();
                fileName = jcrContent.getProperty(JCR_FILENAME_PROPERTY).getString();
                mimeType = jcrContent.getProperty(JcrConstants.JCR_MIMETYPE).getString();
                documentStream = new DocumentStream(fileName, inputStream, mimeType);
                documentStream.setRequestId(storeContext.getRequestIdentifier());
            } else {
                throw new ObjectNotFoundException("The file was not found using the specified identifier.");
            }
        } catch (RepositoryException e) {
            //log.error("Unable to download File due to Repository Exception", e);
            throw new StoreServiceException("Unable to download File due to Repository Exception", e);
        } catch (RuntimeException e) {
            //log.error("Unable to download File due to Repository Exception", e);
            throw e;
        } finally {
            closeSession(storeSession);
        }
        return documentStream;
    }


    @Override
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        RequestIdentifier requestIdentifier = null;
        StoreServiceSessionImpl_Jcr storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Node node = getObject(documentIdentifier, storeSession, true);
            if (ObjectBaseType.DOCUMENT.equals(getObjectBaseType(node))) {
                VersionManager versionManager = null;
                VersionHistory history = null;
                versionManager = storeSession.getJcrSession().getWorkspace().getVersionManager();
                Node jcrContentNode = null;
                if (node != null)
                    jcrContentNode = node.getNode(JcrConstants.JCR_CONTENT);
                history = versionManager.getVersionHistory(jcrContentNode.getPath());
                node.remove();
                storeSession.getJcrSession().save();
                deleteAllVersions(history);
                storeSession.getJcrSession().save();
                requestIdentifier = new RequestIdentifier(storeContext.getRequestIdentifier());
            } else {
                throw new ObjectNotFoundException("The file was not found using the specified identifier.");
            }

        } catch (RepositoryException e) {
            throw new StoreServiceException("There was a RepositoryEception in the existsFolder method!!!", e);
        } catch (StoreServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new StoreServiceException(e);
        } finally {
            closeSession(storeSession);
        }
        return requestIdentifier;
    }

    private void deleteAllVersions(VersionHistory history) {
        try {
            VersionIterator versionIterator = history.getAllVersions();
            while (versionIterator.hasNext()) {
                String versionName = versionIterator.nextVersion().getName();
                if (!versionName.equals(JcrConstants.JCR_ROOTVERSION))
                    history.removeVersion(versionName);
                //version.remove();
            }
            //history.remove();
        } catch (javax.jcr.PathNotFoundException pathException) {
            throw new ObjectNotFoundException("the file was not found at the specified path", pathException);
        } catch (RepositoryException e) {
            throw new StoreServiceException(e);
        }
    }

    private Version retrieveLatestVersion(DocumentIdentifier documentIdentifier, StoreServiceSessionImpl_Jcr storeSession) throws RepositoryException {
        VersionManager versionManager = null;
        VersionHistory history = null;
        versionManager = storeSession.getJcrSession().getWorkspace().getVersionManager();
        Node node = null;
        String path = documentIdentifier.getPath(); // the path to the file node that contains the versioned node
        node = storeSession.getJcrSession().getNode(path);
        Node jcrContentNode = null;
        if (node != null)
            jcrContentNode = node.getNode(JcrConstants.JCR_CONTENT);
        history = versionManager.getVersionHistory(jcrContentNode.getPath());
        VersionIterator versionIterator = history.getAllVersions();
        Version version = null;
        while (versionIterator.hasNext()) {
            version = versionIterator.nextVersion();
            if (!versionIterator.hasNext())
                return version;
        }
        return version;
    }


    private ObjectInfo toObjectInfo(Node objectNode, boolean includeInfo, StoreService storeService, StoreContext storeContext) throws RepositoryException {
        ObjectInfo objectInfo = null;
        FolderInfo folderInfo;
        DocumentInfo documentInfo;

        //set object identifier
        String name = objectNode.getName();
        if (ObjectBaseType.FOLDER.equals(getObjectBaseType(objectNode))) {
            folderInfo = new FolderInfo();
            FolderIdentifier folderIdentifier = toFolderIdentifier(objectNode, storeService, storeContext);
            folderInfo.setIdentifier(folderIdentifier);
            folderInfo.setName(name);
            //folderInfo.setParentIdentifier(toFolderIdentifier(objectNode.getParent(), storeService, storeContext));
            folderInfo.setParentIdentifier(FolderIdentifier.builder().withRequestId(storeContext.getRequestIdentifier()).withStoreServiceName(storeService != null ? storeService.getName() : null).withPath(StoreServiceImpl_Abstract.fileUtils.getParentFolderPathName(folderIdentifier.getPath())).build());
            folderInfo.setType(ObjectBaseType.FOLDER.name());

            if (includeInfo) {
                //set properties

                //set creation and modification date
                folderInfo.setCreationDate(null);
                folderInfo.setModificationDate(null);
            }
            objectInfo = folderInfo;
        } else if (ObjectBaseType.DOCUMENT.equals(getObjectBaseType(objectNode))) {
            documentInfo = new DocumentInfo();
            Node jcrContent = objectNode.getNode(JcrConstants.JCR_CONTENT);
            String fileName = jcrContent.getProperty(JCR_FILENAME_PROPERTY).getString();
            DocumentIdentifier documentIdentifier = toDocumentIdentifier(objectNode, storeService, storeContext);
            documentInfo.setIdentifier(documentIdentifier);
            documentInfo.setExtension(StoreServiceImpl_Abstract.fileUtils.getFileExtension(fileName));
            documentInfo.setParentIdentifier(FolderIdentifier.builder().withRequestId(storeContext.getRequestIdentifier()).withStoreServiceName(storeService != null ? storeService.getName() : null).withPath(StoreServiceImpl_Abstract.fileUtils.getParentFolderPathName(documentIdentifier.getPath())).build());
            documentInfo.setName(name);
            documentInfo.setType(JCR_MIX_FILE_ATTRIBUTES);
            for (NodeType nodeType : jcrContent.getMixinNodeTypes()) {
                if (!jcrStandardProperties.contains(nodeType.getName())) {
                    documentInfo.setType(nodeType.getName());
                }
            }
            if (includeInfo) {
                //set properties
                documentInfo.setProperties(new HashMap<String, Object>());
                PropertyIterator propertyIterator = jcrContent.getProperties();
                while (propertyIterator.hasNext()) {
                    Property p = propertyIterator.nextProperty();
                    if (!jcrStandardProperties.contains(p.getName())) {
                        if (p.getType() == PropertyType.STRING) {
                            documentInfo.getProperties().put(p.getName(), p.getString());
                        } else if (p.getType() == PropertyType.BINARY) {
                            documentInfo.getProperties().put(p.getName(), p.getBinary());
                        } else if (p.getType() == PropertyType.LONG) {
                            documentInfo.getProperties().put(p.getName(), p.getLong());
                        } else if (p.getType() == PropertyType.DOUBLE) {
                            documentInfo.getProperties().put(p.getName(), p.getDouble());
                        } else if (p.getType() == PropertyType.DATE) {
                            documentInfo.getProperties().put(p.getName(), p.getDate());
                        } else if (p.getType() == PropertyType.BOOLEAN) {
                            documentInfo.getProperties().put(p.getName(), p.getBoolean());
                        } else if (p.getType() == PropertyType.NAME) {
                            documentInfo.getProperties().put(p.getName(), p.getName());
                        } else if (p.getType() == PropertyType.PATH) {
                            documentInfo.getProperties().put(p.getName(), p.getPath());
                        } else if (p.getType() == PropertyType.REFERENCE) {
                            //DO NOTHING
                        } else if (p.getType() == PropertyType.WEAKREFERENCE) {
                            //DO NOTHING
                        } else if (p.getType() == PropertyType.URI) {
                            //DO NOTHING
                        } else if (p.getType() == PropertyType.DECIMAL) {
                            documentInfo.getProperties().put(p.getName(), p.getDecimal());
                        }
                    }
                }
                //set creation and modification date
                documentInfo.setCreationDate(jcrContent.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate().getTime());
                documentInfo.setModificationDate(jcrContent.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate().getTime());
                //set versions
                //TODO - include versions
//                if (cmisObject.getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT)) {
//                    List<DocumentIdentifier> documentIdentifierList = new ArrayList<DocumentIdentifier>();
//                    for (Document document : ((Document) cmisObject).getAllVersions()) {
//                        DocumentIdentifier documentIdentifier = toDocumentIdentifier(document, storeService);
//                        documentIdentifierList.add(documentIdentifier);
//                    }
//                    //TODO - dc. nu avre versions trebuie sa adaug un self identifier on the array
//                    DocumentIdentifier[] versions = new DocumentIdentifier[0];
//                    ((DocumentInfo) objectInfo).setDocumentIdentifierVersions(documentIdentifierList.toArray(versions));
//                }
            }
            objectInfo = documentInfo;
        }
        objectInfo.setRequestId(storeContext.getRequestIdentifier());
        return objectInfo;
    }

    @Override
    public DocumentInfo getDocumentInfo(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        DocumentInfo documentInfo = null;
        StoreServiceSessionImpl_Jcr storeSession = null;
        try {
            storeSession = openSession(storeContext);
            Node node = getObject(documentIdentifier, storeSession, true);
            documentInfo = (DocumentInfo) toObjectInfo(node, true, this, storeContext);
        } catch (RepositoryException e) {
            //log.error("Unable to download File due to Repository Exception", e);
            throw new StoreServiceException("Unable to download File due to Repository Exception", e);
        } catch (StoreServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new StoreServiceException(e);
        } finally {
            closeSession(storeSession);
        }
        return documentInfo;
    }


//    private DocumentInfo[] getAlDocumentVersions(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
//        Session session = openSession(storeContext);
//        Node node = null;
//        String path = documentIdentifier.getPath(); // the path to the file node that contains the versioned node
//        try {
//
//            VersionManager versionManager = null;
//            VersionHistory history = null;
//            versionManager = session.getWorkspace().getVersionManager();
//            history = versionManager.getVersionHistory(documentIdentifier.getPath() + PATH_DELIMITER + JcrConstants.JCR_CONTENT);
//            VersionIterator versionIterator = history.getAllVersions();
//            System.out.println(versionIterator.getSize());
//
//            while (versionIterator.hasNext()) {
//                Version version = versionIterator.nextVersion();
//                if (!version.getName().equals(JcrConstants.JCR_ROOTVERSION)) {
//                    //                node = (Node) version;
//                    node = version.getFrozenNode();
//
//                    System.out.println(version.getName());
//                    //                Node node = version.getNode(documentIdentifier.getPath());
//    /*                PropertyIterator propertyIterator = version.getFrozenNode().getProperties();
//                    while (propertyIterator.hasNext()) {
//                        Property p = propertyIterator.nextProperty();
//                        if (!p.getName().equals(JcrConstants.JCR_MIXINTYPES) && !p.getName().equals(JcrConstants.JCR_PREDECESSORS))
//                            System.out.println(p.getName() + " - " + p.getValue().toString());
//                        else {
//                            Value[] values = p.getValues();
//                        }
//
//                    }*/
//                    //Node jcrContent = node.getNode(JcrConstants.JCR_CONTENT);
//                    String documentName = node.getMetadataProperty(JCR_FILENAME_PROPERTY).getString();
//                    InputStream inputStream = node.getMetadataProperty("jcr:data").getBinary().getStream();
//                    OutputStream outputStream = null;
//
//                    try {
//                        // write the inputStream to a FileOutputStream
//                        outputStream =
//                                new FileOutputStream(new File("D:\\WORK\\CROCO\\JackRabbit\\outputTestFolder\\" + documentName));
//
//                        int read = 0;
//                        byte[] bytes = new byte[1024];
//
//                        while ((read = inputStream.read(bytes)) != -1) {
//                            outputStream.write(bytes, 0, read);
//                        }
//
//                        System.out.println("Done!");
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        if (inputStream != null) {
//                            try {
//                                inputStream.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        if (outputStream != null) {
//                            try {
//                                // outputStream.flush();
//                                outputStream.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                    }
//
//
//                    //                Node jcrContent = fNode.getNode("jcr:content");
//                    //                System.out.println(jcrContent.getMetadataProperty("jcr:lastModified").getDate());
//                    System.out.println("--------------------------------------------------------------------------------------");
//                }
//            }
//        } catch (RepositoryException e) {
//            throw new RuntimeException("runtime Exception at getDocInfo", e);
//        } finally {
//            closeSession(session);
//        }
//
//
//        return null;
//    }

    @Override
    public BooleanResponse existsFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        StoreServiceSessionImpl_Jcr storeSession = null;
        boolean existsFolder = false;
        try {
            storeSession = openSession(storeContext);
            Node node = getObject(folderIdentifier, storeSession, false);
            if (ObjectBaseType.FOLDER.equals(getObjectBaseType(node))) {
                existsFolder = true;
            }
        } catch (Exception e) {
            throw new StoreServiceException("There was a RepositoryEception in the existsFolder method!!!", e);
        } finally {
            closeSession(storeSession);
        }
        return new BooleanResponse(storeContext.getRequestIdentifier(), existsFolder);
    }

    @Override
    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo, boolean createParentIfNotExists) {
        FolderIdentifier folderIdentifier = null;
        StoreServiceSessionImpl_Jcr storeSession = null;
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

            Node root = storeSession.getJcrSession().getRootNode();
            Node parentFolderNode = returnNode(folderPath, root, createParentIfNotExists);
            Node nodeToCreate = null;
            if (parentFolderNode.hasNode(folderName))
                nodeToCreate = parentFolderNode.getNode(folderName);
            else
                nodeToCreate = parentFolderNode.addNode(folderName, JcrConstants.NT_FOLDER);
            folderIdentifier = FolderIdentifier.builder().withPath(nodeToCreate.getPath()).build();
            folderIdentifier.setRequestId(storeContext.getRequestIdentifier());
            storeSession.getJcrSession().save();
        } catch (RepositoryException e) {
            //log.error("Unable to create folder!", e);
            throw new StoreServiceException("Unable to create folder!", e);
        } finally {
            closeSession(storeSession);
        }
        return folderIdentifier;
    }

    @Override
    public RequestIdentifier deleteFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        StoreServiceSessionImpl_Jcr storeSession = null;
        try {
            storeSession = openSession(storeContext);
            if (1 == 1) {
                throw new StoreServiceException("deleteFolder is not implemented.");
            }
        } catch (StoreServiceException e) {
            throw e;
        } finally {
            closeSession(storeSession);
        }
        return new RequestIdentifier(storeContext.getRequestIdentifier());
    }

    @Override
    public FolderIdentifier renameFolder(StoreContext storeContext, FolderIdentifier folderIdentifier, String newFolderName) {
        return null;
    }

    @Override
    protected FolderInfo getFolderInfo(StoreServiceSessionImpl_Jcr storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier) {
        FolderInfo folderInfo = null;
        try {
            Node node = getObject(folderIdentifier, storeSession, true);
            folderInfo = (FolderInfo) toObjectInfo(node, true, this, storeContext);
        } catch (RepositoryException e) {
            throw new StoreServiceException(e);
        }
        return folderInfo;
    }

    @Override
    public FolderInfo getFolderInfo(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        FolderInfo folderInfo = null;
        StoreServiceSessionImpl_Jcr storeSession = null;
        try {
            storeSession = openSession(storeContext);
            //Node node = getObject(folderIdentifier, storeSession, true);
            //folderInfo = (FolderInfo) toObjectInfo(node, true, this, storeContext);
            folderInfo = getFolderInfo(storeSession, storeContext, folderIdentifier);

        } catch (StoreServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new StoreServiceException(e);
        } finally {
            closeSession(storeSession);
        }
        return folderInfo;
    }

    @Override
    protected ObjectInfo[] listFolderContent(StoreServiceSessionImpl_Jcr storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        ObjectInfo[] objectInfoArray = null;

        try {
            List<ObjectInfo> objectInfoList = new ArrayList<ObjectInfo>();
            List<ObjectBaseType> objectBaseTypeList = Arrays.asList(objectBaseTypes);
            Node root = storeSession.getJcrSession().getRootNode();
            Node pathNode = returnNode(folderIdentifier.getPath(), root, false);
            NodeIterator nodeIterator = pathNode.getNodes();
            while (nodeIterator.hasNext()) {
                ObjectInfo objInfo = null;
                Node tempNode = nodeIterator.nextNode();
                if (tempNode.getPrimaryNodeType().isNodeType(NodeType.NT_FILE) && objectBaseTypeList.contains(ObjectBaseType.DOCUMENT)) {
                    //objInfo = new DocumentInfo();
                    //objInfo.setIdentifier(DocumentIdentifier.builder().withPath(fileUtils.concatenate(folderIdentifier.getPath(), tempNode.getName())).build());
                    objInfo = toObjectInfo(tempNode, includeInfo, this, storeContext);
                } else if (tempNode.getPrimaryNodeType().isNodeType(NodeType.NT_FOLDER) && objectBaseTypeList.contains(ObjectBaseType.FOLDER)) {
                    //objInfo = new FolderInfo();
                    //objInfo.setIdentifier(FolderIdentifier.builder().withPath(fileUtils.concatenate(folderIdentifier.getPath(), tempNode.getName())).build());
                    objInfo = toObjectInfo(tempNode, includeInfo, this, storeContext);
                }
                if (objInfo != null) {
                    objectInfoList.add(objInfo);
                    //System.out.println(objInfo.getIdentifier());
                }
            }
            objectInfoArray = objectInfoList.toArray(new ObjectInfo[0]);
        } catch (RepositoryException e) {
            throw new StoreServiceException("Unable to listFolderContent due to Repository Exception", e);
        } finally {
            //not required to close session / it is done in the default implementation in "public ObjectInfoTree StoreServiceImpl_Abstract.listFolderContent"
        }
        return objectInfoArray;
    }

    /**
     * This method return a node from a given path.
     * If that path doesn't exist it can be automatically created if the allowCreatePath variable is set to true, otherwise a PathNotFoundException will be thrown
     *
     * @param path            the path where the node should be created
     * @param rootNode        the root Node of the repository (in most cases "/")
     * @param allowCreatePath boolean variable that specifies if the path should or should not be created in the method (if it doesn't exist)
     * @return the Node created at the specified path
     */

    private Node returnNode(String path, Node rootNode, boolean allowCreatePath) {
        if (StringUtils.isEmpty(path))
            throw new ObjectNotFoundException("The path is empty");
        if (path.equals(StoreServiceImpl_Abstract.fileUtils.getRootPath()))
            return rootNode;

        //if allowCreatePath is false it returns the Node from the specified path
        if (!allowCreatePath)
            try {
                if (path.startsWith(StoreServiceImpl_Abstract.fileUtils.getRootPath()))
                    path = path.substring(1, path.length());
                return rootNode.getNode(path);
            } catch (javax.jcr.PathNotFoundException e) {
                //log.error("Path not found", e);
                throw new ObjectNotFoundException("The specified path was not found", e);
            } catch (RepositoryException e1) {
                //log.error("Repository Exception on return node with allowCreatePath false", e1);
                return null;
            }
        //starts building the path and creating the nodes
        Node tempNode = null;
        try {
            String[] pathItems = path.split(StoreServiceImpl_Abstract.fileUtils.getPathDelimiter());

            for (String pathItem : pathItems) {
                if (pathItem.length() > 0) {
                    try {
                        tempNode = rootNode.getNode(pathItem);
                    } catch (javax.jcr.PathNotFoundException e) {
                        //if the node cannot be found, it will be created
                        tempNode = rootNode.addNode(pathItem, JcrConstants.NT_FOLDER);
                    }
                    rootNode = tempNode;
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return tempNode;
    }

    public void testNodeDefinitionTemplate() throws Exception {

//        NodeTypeManager nodeTypeManager = NodeTypeManager
    }

    private static List<String> getStandardJCRProperties() {
        List<String> jcrProperties = new ArrayList<String>();
        Field[] declaredFields = JcrConstants.class.getDeclaredFields();
        String jcrPropertyPrefix = "";
        for (Field field : declaredFields) {
            if (Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())) {
                String fieldValue = null;
                try {
                    fieldValue = String.valueOf(field.get(null));
                } catch (IllegalAccessException e) {

                }
                if (fieldValue != null && fieldValue.startsWith(jcrPropertyPrefix)) {
                    jcrProperties.add(fieldValue);
                }
            }
        }
        jcrProperties.add("jcr:createdBy");
        jcrProperties.add("jcr:lastModifiedBy");
        jcrProperties.add(JCR_FILENAME_PROPERTY);

        return jcrProperties;
    }

//    public static void main(String[] args) {
//        getStandardJCRProperties();
//    }


    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    //JCR UTILITY METHODS
    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    private ObjectBaseType getObjectBaseType(Node node) {
        ObjectBaseType objectBaseType = null;
        try {
            if (node != null) {
                if (node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FOLDER)) {
                    objectBaseType = ObjectBaseType.FOLDER;
                } else if (node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FILE)) {
                    objectBaseType = ObjectBaseType.DOCUMENT;
                } else if (node.getPrimaryNodeType().getName().equals(JcrConstants.NT_BASE)) {
                    objectBaseType = ObjectBaseType.FOLDER;
                }
            }
        } catch (RepositoryException e) {
            throw new StoreServiceException(e);
        }
        return objectBaseType;
    }

    private Node getObject(ObjectIdentifier objectIdentifier, StoreServiceSessionImpl_Jcr storeSession, boolean throwWhenNotFound) {
        Node node = null;
        //momentan implementam search-ul numai dupa path
        boolean supportSearchByIdentifier = false;
        boolean supportSearchByVersion = false;

        try {
            //search by identifier
            if (supportSearchByIdentifier && objectIdentifier.getId() != null) {
                node = storeSession.getJcrSession().getNodeByIdentifier(objectIdentifier.getId());
            }
            //search by path [and version if applicable]
            else if (objectIdentifier.getPath() != null) {
                node = storeSession.getJcrSession().getNode(objectIdentifier.getPath());
                //in case of searching a document
                if (objectIdentifier instanceof DocumentIdentifier) {
                    String version = ((DocumentIdentifier) objectIdentifier).getVersion();
                    if (supportSearchByVersion && (version != null)) {
                        if (supportSearchByVersion) {
                            throw new StoreServiceException("The search with version is not supported.");
                        }

                        //when implemented the code will search for the node with the appropriate version label
                        //TODO - CODE TO SEARCH THE RIGHT VERSION
                        //when implemented the code will search for the node with the appropriate version label

                        throw new VersionNotFoundException("Requested version does not exist.");
                    }
                }
                //in case of searching a folder
                else if (objectIdentifier instanceof FolderIdentifier) {
                    //node is already set
                }
            }
            //throw errors because search criteria were not provided
            else {
                throw new StoreServiceException("At least one of id or path must be non null when searching a jcr object");
            }
        } catch (ItemNotFoundException e) {
            node = null;
            if (throwWhenNotFound) {
                throw new ObjectNotFoundException(e);
            }
        } catch (PathNotFoundException e) {
            node = null;
            if (throwWhenNotFound) {
                throw new ObjectNotFoundException(e);
            }
        } catch (VersionNotFoundException e) {
            node = null;
            if (throwWhenNotFound) {
                throw new ObjectNotFoundException(e);
            }
        } catch (RepositoryException e) {
            node = null;
            throw new StoreServiceException(e);
        }
        return node;
    }

    private boolean existsObject(ObjectIdentifier objectIdentifier, StoreServiceSessionImpl_Jcr storeSession) {
        return (getObject(objectIdentifier, storeSession, false) != null);
    }

    private static FolderIdentifier toFolderIdentifier(Node folderNode, StoreService storeService, StoreContext storeContext) {
        FolderIdentifier folderIdentifier = null;
        try {
            folderIdentifier = FolderIdentifier.builder().withRequestId(storeContext.getRequestIdentifier()).withStoreServiceName(storeService != null ? storeService.getName() : null).withId(folderNode.getIdentifier()).withPath(folderNode.getPath()).build();
            folderIdentifier.setRequestId(storeContext.getRequestIdentifier());
        } catch (RepositoryException e) {
            throw new StoreServiceException(e);
        }
        return folderIdentifier;
    }

    private static DocumentIdentifier toDocumentIdentifier(Node documentNode, StoreService storeService, StoreContext storeContext) {
        DocumentIdentifier documentIdentifier = null;
        try {
            Node jcrContent = documentNode.getNode(JcrConstants.JCR_CONTENT);
            String fileName = jcrContent.getProperty(JCR_FILENAME_PROPERTY).getString();
            DocumentIdentifier.Builder documentIdentifierBuilder = DocumentIdentifier.builder();
            documentIdentifierBuilder.withRequestId(storeContext.getRequestIdentifier());
            documentIdentifierBuilder.withStoreServiceName(storeService != null ? storeService.getName() : null);
            documentIdentifierBuilder.withId(documentNode.getIdentifier());
            documentIdentifierBuilder.withPath(documentNode.getPath());
            //TODO - implement support for retrieving file version
            documentIdentifierBuilder.withVersion(null);
            documentIdentifier = documentIdentifierBuilder.build();
        } catch (RepositoryException e) {
            throw new StoreServiceException(e);
        }
        return documentIdentifier;
    }

}
