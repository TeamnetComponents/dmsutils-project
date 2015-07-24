package ro.croco.integration.dms.toolkit;

import org.apache.commons.lang.NotImplementedException;
import ro.croco.integration.dms.commons.DeleteOnCloseInputStream;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.jms.JmsConnectionPool;
import ro.croco.integration.dms.toolkit.jms.JmsConnectionPoolFactory;
import ro.croco.integration.dms.toolkit.jms.JmsMessageStructure;
import ro.croco.integration.dms.toolkit.jms.JmsMessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by Lucian.Dragomir on 7/8/2014.
 */
public class StoreServiceImpl_Jms extends StoreServiceImpl_Abstract {
    //static Logger log = LoggerFactory.getLogger(StoreServiceImpl_Jms.class);

    private static String ALTERNATE_REPOSITORY_PROPERTIES = "alternate.service.properties";
    //private static String ALTERNATE_REPOSITORY_PATH_UPLOAD = "alternate.service.location.upload";
    //private static String ALTERNATE_REPOSITORY_PATH_DOWNLOAD = "alternate.service.location.download";

    //am scos static in ideea ca toate implementarile de StoreSession vor fi singleton
    //private static Map<String, JmsConnectionPool> jmsSynchronousConnectionPoolMap = new HashMap<String, JmsConnectionPool>();
    //private static Map<String, JmsConnectionPool> jmsAsynchronousConnectionPoolMap = new HashMap<String, JmsConnectionPool>();

    private JmsConnectionPool jmsSynchronousConnectionPool;
    private JmsConnectionPool jmsAsynchronousConnectionPool;


    public StoreServiceImpl_Jms() {
        super();
        setCommunicationTypeSupport(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS, true);
    }

    private JmsConnectionPool getConnectionPool(StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        JmsConnectionPool jmsConnectionPool = null;

        //synchronous pool (with initialization if required)
        if (StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS.equals(communicationType)) {
            if (this.jmsSynchronousConnectionPool == null) {
                synchronized (this) {
                    if (this.jmsSynchronousConnectionPool == null) {
                        JmsConnectionPoolFactory jmsConnectionPoolFactory = new JmsConnectionPoolFactory(StoreServiceImpl_Abstract.toProperties(this.getContext()), communicationType);
                        this.jmsSynchronousConnectionPool = new JmsConnectionPool(jmsConnectionPoolFactory);
                    }
                }
            }
            jmsConnectionPool = this.jmsSynchronousConnectionPool;
        }

        //asynchonous pool (with initialization if required)
        if (StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS.equals(communicationType)) {
            if (this.jmsAsynchronousConnectionPool == null) {
                synchronized (this) {
                    if (this.jmsAsynchronousConnectionPool == null) {
                        JmsConnectionPoolFactory jmsConnectionPoolFactory = new JmsConnectionPoolFactory(StoreServiceImpl_Abstract.toProperties(this.getContext()), communicationType);
                        this.jmsAsynchronousConnectionPool = new JmsConnectionPool(jmsConnectionPoolFactory);
                    }
                }
            }
            jmsConnectionPool = this.jmsAsynchronousConnectionPool;
        }

        //synchronous_local not supported
        if (StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL.equals(communicationType)) {
            throw new StoreServiceException("The JMS store service does not support SYNCHRONOUS_LOCAL communication types");
        }

        return jmsConnectionPool;
    }

    @Override
    public void __init(Properties context) throws IOException {
        super.__init(context);

//        String serviceName = (String) getContextProperty(ALTERNATE_REPOSITORY_PROPERTIES);
//        try {
//            setLocalStoreService((new StoreServiceFactory(serviceName)).getService());
//        } catch (Exception e) {
//            throw new RuntimeException("Unable to create a sore service instance for " + serviceName, e);
//        }

        //if (!jmsSynchronousConnectionPoolMap.containsKey(this.getName())) {
        //    synchronized (jmsSynchronousConnectionPoolMap) {
        //        if (!jmsSynchronousConnectionPoolMap.containsKey(this.getName())) {
        //            JmsConnectionPoolFactory jmsConnectionPoolFactory = new JmsConnectionPoolFactory(context, StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS);
        //            JmsConnectionPool jmsConnectionPool = new JmsConnectionPool(jmsConnectionPoolFactory);
        //            jmsSynchronousConnectionPoolMap.put(this.getName(), jmsConnectionPool);
        //        }
        //    }
        //}
        //if (!jmsAsynchronousConnectionPoolMap.containsKey(this.getName())) {
        //    synchronized (jmsAsynchronousConnectionPoolMap) {
        //        if (!jmsAsynchronousConnectionPoolMap.containsKey(this.getName())) {
        //            JmsConnectionPoolFactory jmsConnectionPoolFactory = new JmsConnectionPoolFactory(context, StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS);
        //            JmsConnectionPool jmsConnectionPool = new JmsConnectionPool(jmsConnectionPoolFactory);
        //            jmsAsynchronousConnectionPoolMap.put(this.getName(), jmsConnectionPool);
        //        }
        //    }
        //}
    }

    @Override
    public void close() {
        super.close();

        if (jmsSynchronousConnectionPool != null) {
            try {
                jmsSynchronousConnectionPool.close();
            } catch (Exception e) {
                e.printStackTrace();
                jmsSynchronousConnectionPool = null;
            }
        }

        if (jmsAsynchronousConnectionPool != null) {
            try {
                jmsAsynchronousConnectionPool.close();
            } catch (Exception e) {
                e.printStackTrace();
                jmsAsynchronousConnectionPool = null;
            }
        }
    }

    @Override
    protected StoreServiceSession openSession(StoreContext storeContext) {
        throw new StoreServiceException("This method should not be used in the JMS implementation.");
    }

    @Override
    protected void closeSession(StoreServiceSession storeSession) {
        throw new StoreServiceException("This method should not be used in the JMS implementation.");
    }

    @Override
    protected ObjectInfo[] listFolderContent(StoreServiceSession storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        throw new StoreServiceException("This method should not be used in the JMS implementation.");
    }


    @Override
    public ObjectIdentifier moveFrom(StoreContext storeContext, ObjectIdentifier objectIdentifierFrom, StoreService storeServiceFrom, StoreContext storeContextFrom) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().moveFrom(preparedStoreContext, objectIdentifierFrom, storeServiceFrom, storeContextFrom);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("moveFrom");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, objectIdentifierFrom, storeServiceFrom.getName(), storeContextFrom);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (ObjectIdentifier) messageStructureResponse.getParameters()[0];
            } else {
                if (objectIdentifierFrom instanceof DocumentIdentifier) {
                    return DocumentIdentifier.builder().withRequestId(storeContext.getRequestIdentifier()).build();
                } else {
                    return FolderIdentifier.builder().withRequestId(storeContext.getRequestIdentifier()).build();
                }
            }
        }
    }

    @Override
    public ObjectIdentifier moveTo(StoreContext storeContext, ObjectIdentifier objectIdentifier, StoreService storeServiceTo, StoreContext storeContextTo) {
        //return super.moveTo(storeContext, objectIdentifier, storeServiceTo, storeContextTo);
        throw new NotImplementedException("Method moveTo not implemented");
    }

    @Override
    public ObjectIdentifier[] moveFrom(StoreContext storeContext, ObjectIdentifier[] objectIdentifierFrom, StoreService storeServiceFrom, StoreContext storeContextFrom) {
        return super.moveFrom(storeContext, objectIdentifierFrom, storeServiceFrom, storeContextFrom);
    }

    @Override
    public ObjectIdentifier[] moveTo(StoreContext storeContext, ObjectIdentifier[] objectIdentifierFrom, StoreService storeServiceTo, StoreContext storeContextTo) {
        return super.moveTo(storeContext, objectIdentifierFrom, storeServiceTo, storeContextTo);
    }


    //    @Override
//    public ObjectIdentifier move(StoreContext storeContext, ObjectIdentifier objectIdentifier, StoreService storeServiceTo, StoreContext storeContextTo, StoreMetadata storeMetadata) {
//        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
//        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
//            return getLocalStoreService().move(preparedStoreContext, objectIdentifier, storeServiceTo, storeContextTo, storeMetadata);
//        } else {
//            JmsMessageStructure messageStructure = new JmsMessageStructure();
//            messageStructure.setMethod("existsDocument");
//            messageStructure.setType(JmsMessageType.REQUEST);
//            messageStructure.setParameters(preparedStoreContext, objectIdentifier, storeServiceTo, storeContextTo, storeMetadata);
//            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());
//
//            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
//                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
//                if (messageStructureResponse.getException() != null) {
//                    throw (messageStructureResponse.getException());
//                }
//                return (ObjectIdentifier) messageStructureResponse.getParameters()[0];
//            } else {
//                ObjectIdentifier objectIdentifierResponse = null;
//                if (objectIdentifier instanceof DocumentIdentifier) {
//                    objectIdentifierResponse = new DocumentIdentifier();
//                } else if (objectIdentifier instanceof FolderIdentifier) {
//                    objectIdentifierResponse = new FolderIdentifier();
//                }
//                objectIdentifierResponse.setRequestId(storeContext.getRequestIdentifier());
//                return objectIdentifierResponse;
//            }
//        }
//    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().existsDocument(preparedStoreContext, documentIdentifier);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("existsDocument");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (BooleanResponse) messageStructureResponse.getParameters()[0];
            } else {
                BooleanResponse booleanResponse = new BooleanResponse();
                booleanResponse.setRequestId(storeContext.getRequestIdentifier());
                return booleanResponse;
            }
        }
    }


    @Override
    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
        //System.out.println("--------------- START Prepare context --------------");

        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        //System.out.println("--------------- FINISH Prepare context --------------");
        //System.out.println("-------------------- FINISH Prepare context: " + preparedStoreContext.getCommunicationType().name() + " ----------");
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().storeDocument(preparedStoreContext, documentInfo, inputStream, allowCreatePath, versioningType);
        } else {
            if (documentInfo == null) {
                throw new StoreServiceException("DocumentInfo object must not be null");
            }
            String documentPath = StoreServiceImpl_Abstract.fileUtils.getRootPath(); //+"upload/";
            if (documentInfo.getParentIdentifier() != null) {
                if (documentInfo.getParentIdentifier().getPath() == null || documentInfo.getParentIdentifier().getPath().isEmpty()) {
                    throw new StoreServiceException("ParentIdentifier must have the path completed.");
                }
                documentPath = documentInfo.getParentIdentifier().getPath();
            }
            String documentNameWithExtension = StoreServiceImpl_Abstract.fileUtils.getFileNameWithExtension(StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentInfo.getName()), documentInfo.getExtension());


            String temporaryFilePath = getLocalStoreService().getPathByConfiguration(PathConfiguration.TEMP_UPLOAD);
            String temporaryFileName = String.valueOf(UUID.randomUUID()) + "_" + documentNameWithExtension;
            DocumentInfo temporaryDocumentInfo = new DocumentInfo(temporaryFilePath, StoreServiceImpl_Abstract.fileUtils.getFileBaseName(temporaryFileName), StoreServiceImpl_Abstract.fileUtils.getFileExtension(temporaryFileName), null, null);
            DocumentIdentifier temporaryDocumentIdentifier = getLocalStoreService().storeDocument(preparedStoreContext, temporaryDocumentInfo, inputStream, true, VersioningType.NONE);

            //DocumentIdentifier temporaryDocumentIdentifier = new DocumentIdentifier();
            //temporaryDocumentIdentifier.setPath(temporaryFilePath + "/" + temporaryFileName);

            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("storeDocument");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentInfo, temporaryDocumentIdentifier, allowCreatePath, versioningType);

            //System.out.println(" ------------- START send msg to JMS queue --------------");
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());
            //System.out.println(" ------------- FINISH send msg to JMS queue --------------");

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (DocumentIdentifier) messageStructureResponse.getParameters()[0];
            } else {
                DocumentIdentifier documentIdentifier = new DocumentIdentifier();
                documentIdentifier.setRequestId(preparedStoreContext.getRequestIdentifier());
                return documentIdentifier;
            }
        }
    }

    @Override
    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().downloadDocument(preparedStoreContext, documentIdentifier);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("downloadDocument");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                DocumentIdentifier temporaryDocumentIdentifier = (DocumentIdentifier) messageStructureResponse.getParameters()[0];
                //TODO AK preparedStoreContext e ok de transmis aici?
                DocumentStream documentStream = getLocalStoreService().downloadDocument(preparedStoreContext, temporaryDocumentIdentifier);
                return DeleteOnCloseInputStream.wrap(documentStream, this.getLocalStoreService(), storeContext, temporaryDocumentIdentifier);
            } else {
                DocumentStream documentStream = new DocumentStream();
                documentStream.setRequestId(preparedStoreContext.getRequestIdentifier());
                return documentStream;
            }
        }
    }

    @Override
    public DocumentIdentifier updateDocumentProperties(StoreContext storeContext, DocumentIdentifier documentIdentifier, DocumentInfo documentInfo) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().updateDocumentProperties(preparedStoreContext, documentIdentifier, documentInfo);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("updateDocumentProperties");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier, documentInfo);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (DocumentIdentifier) messageStructureResponse.getParameters()[0];
            }
            return DocumentIdentifier.builder().withRequestId(storeContext.getRequestIdentifier()).build();
        }
    }

    @Override
    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().deleteDocument(preparedStoreContext, documentIdentifier);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("deleteDocument");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (RequestIdentifier) messageStructureResponse.getParameters()[0];
            }
            return new RequestIdentifier(storeContext.getRequestIdentifier());
        }
    }

    @Override
    public DocumentIdentifier renameDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier, String newDocumentName) {
        return null;
    }

    @Override
    public DocumentInfo getDocumentInfo(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().getDocumentInfo(preparedStoreContext, documentIdentifier);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("getDocumentInfo");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (DocumentInfo) messageStructureResponse.getParameters()[0];
            } else {
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setRequestId(preparedStoreContext.getRequestIdentifier());
                return documentInfo;
            }
        }
    }

    @Override
    public BooleanResponse existsFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().existsFolder(preparedStoreContext, folderIdentifier);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("existsFolder");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (BooleanResponse) messageStructureResponse.getParameters()[0];
            }
            return new BooleanResponse(storeContext.getRequestIdentifier(), null);
        }
    }

    @Override
    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo, boolean createParentIfNotExists) {

        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().createFolder(preparedStoreContext, folderInfo, createParentIfNotExists);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("createFolder");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderInfo, createParentIfNotExists);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (FolderIdentifier) messageStructureResponse.getParameters()[0];
            } else {
                FolderIdentifier folderIdentifier = new FolderIdentifier();
                folderIdentifier.setRequestId(preparedStoreContext.getRequestIdentifier());
                return folderIdentifier;
            }
        }
    }

    @Override
    public RequestIdentifier deleteFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().deleteFolder(preparedStoreContext, folderIdentifier);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("deleteFolder");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (RequestIdentifier) messageStructureResponse.getParameters()[0];
            }
            return new RequestIdentifier(storeContext.getRequestIdentifier());
        }
    }

    @Override
    public FolderIdentifier renameFolder(StoreContext storeContext, FolderIdentifier folderIdentifier, String newFolderName) {
        return null;
    }

    @Override
    protected FolderInfo getFolderInfo(StoreServiceSession storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier) {
        throw new StoreServiceException("This method should not be used in the JMS implementation.");
    }

    @Override
    public FolderInfo getFolderInfo(StoreContext storeContext, FolderIdentifier folderIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().getFolderInfo(preparedStoreContext, folderIdentifier);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("getFolderInfo");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (FolderInfo) messageStructureResponse.getParameters()[0];
            } else {
                FolderInfo folderInfo = new FolderInfo();
                folderInfo.setRequestId(preparedStoreContext.getRequestIdentifier());
                return folderInfo;
            }
        }
    }


    @Override
    public ObjectInfoTree listFolderContent(StoreContext storeContext, FolderIdentifier folderIdentifier, /*String filter,*/ int depth, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().listFolderContent(preparedStoreContext, folderIdentifier, depth, includeInfo, objectBaseTypes);
        } else {
            JmsMessageStructure messageStructure = new JmsMessageStructure();
            messageStructure.setMethod("listFolderContent");
            messageStructure.setType(JmsMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier, depth, includeInfo, objectBaseTypes);
            Serializable response = sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                JmsMessageStructure messageStructureResponse = (JmsMessageStructure) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (ObjectInfoTree) messageStructureResponse.getParameters()[0];
            }

            ObjectInfoTree objectInfoTree = new ObjectInfoTree();
            objectInfoTree.setRequestId(storeContext.getRequestIdentifier());
            return objectInfoTree;
        }
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {
        StoreServiceFactory ssf = new StoreServiceFactory("jms");
        StoreService storeService = ssf.getService();
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath("/tmp/gigi.txt").build();
        storeService.downloadDocument(null, documentIdentifier);
    }

    private JmsMessageStructure[] sendAndReceive(JmsMessageStructure[] messageStructures, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        Map<String, String> configuration = new HashMap<String, String>();
        //configuration.put(ALTERNATE_REPOSITORY_PROPERTIES, (String) getContextProperty(ALTERNATE_REPOSITORY_PROPERTIES));
        configuration.put(ALTERNATE_REPOSITORY_PROPERTIES, getLocalStoreService().getName());
        for (int index = 0; index < messageStructures.length; index++) {
            messageStructures[index].setConfiguration(configuration);
        }
        JmsConnectionPool jmsConnectionPool = getConnectionPool(communicationType);
        Serializable[] arrSerializable = jmsConnectionPool.sendAndReceiveObjects(messageStructures);
        JmsMessageStructure[] jmsMessageStructures = new JmsMessageStructure[arrSerializable.length];
        for (int index = 0; index < arrSerializable.length; index++) {
            jmsMessageStructures[index] = (JmsMessageStructure) arrSerializable[index];
        }
        return jmsMessageStructures;
    }

    private JmsMessageStructure sendAndReceive(JmsMessageStructure messageStructure, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
        JmsMessageStructure[] messageStructureResponses = sendAndReceive(new JmsMessageStructure[]{messageStructure}, communicationType);
        return messageStructureResponses[0];
    }


//JmsConnectionPool jmsConnectionPool = null;
//if (communicationType == StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS) {
//    jmsConnectionPool = jmsSynchronousConnectionPoolMap.get(this.getName());
//} else if (communicationType == StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS) {
//    jmsConnectionPool = jmsAsynchronousConnectionPoolMap.get(this.getName());
//} else {
//    throw new StoreServiceException("The JMS store service does not support SYNCHRONOUS_LOCAL communication type");
//    }


//    private JmsMessageStructure sendAndReceive(JmsMessageStructure messageStructure) {
//        JmsMessageStructure jmsMessageStructureResponse = null;
//        JmsConnectionPool jmsConnectionPool = jmsSynchronousConnectionPoolMap.get(this.getName());
//        JmsConnection jmsConnection = null;
//        try {
//            jmsConnection = jmsConnectionPool.borrowObject();
//        } catch (Exception e) {
//            throw new RuntimeException("Unable to get jmsConnection from connection pool", e);
//        }
//
//        //send the message
//        String uuid = String.valueOf(UUID.randomUUID());
//        try {
//            Message requestMessage = null;
//            requestMessage = jmsConnection.getSession().createObjectMessage(messageStructure);
//            requestMessage.setJMSReplyTo(jmsConnection.getConsumerDestination());
//            requestMessage.setJMSCorrelationID(uuid);
//            jmsConnection.getMessageProducer().send(requestMessage);
//        } catch (JMSException e) {
//            try {
//                jmsConnectionPool.invalidateObject(jmsConnection);
//
//            } catch (Exception e1) {
//            }
//            try {
//                jmsConnectionPool.returnObject(jmsConnection);
//            } catch (Exception e1) {
//            }
//            throw new RuntimeException("Unable to create and send a message request", e);
//        }
//
//        //wait for response
//        Message responseMessage = null;
//        try {
//            MessageConsumer messageConsumer = jmsConnection.createConsumer(jmsConnection.isConsumerTemporaryQueue() ? null : "JMSCorrelationID='" + uuid + "'");
//            do {
//                responseMessage = messageConsumer.receive(Long.parseLong(this.context.get(COMMAND_TIMEOUT_RECEIVE)));
//            } while ((responseMessage != null) && (!responseMessage.getJMSCorrelationID().equals(uuid)));
//            if (responseMessage == null) {
//                throw new TimeoutException("Timeout executing store service " + messageStructure.getMethod() + " method.");
//            }
//            jmsMessageStructureResponse = (JmsMessageStructure) ((ObjectMessage) responseMessage).getObject();
//        } catch (JMSException e) {
//            try {
//                jmsConnectionPool.invalidateObject(jmsConnection);
//
//            } catch (Exception e1) {
//            }
//            try {
//                jmsConnectionPool.returnObject(jmsConnection);
//            } catch (Exception e1) {
//            }
//            throw new RuntimeException("Unable to retrieve the message response", e);
//        }
//        return jmsMessageStructureResponse;
//    }

//------------------------------------------------------------------------------------------------------------------

}
