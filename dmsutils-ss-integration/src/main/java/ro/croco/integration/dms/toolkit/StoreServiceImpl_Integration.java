package ro.croco.integration.dms.toolkit;

import org.apache.commons.lang.StringUtils;
import ro.croco.integration.dms.commons.DeleteOnCloseInputStream;
import ro.croco.integration.dms.commons.FileUtils;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.commons.validation.StoreServicePropValidator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by Lucian.Dragomir on 7/8/2014.
 */
public class StoreServiceImpl_Integration extends StoreServiceImpl_Abstract {
    //private static Logger log = LoggerFactory.getLogger(StoreServiceImpl_Integration.class);
    public static final String SERVICE_INTEGRATION_INSTANCE_CONFIGURATION = "service.integration.instance.configuration";
    private IntegrationService integrationService;

    private StoreServicePropValidator validator = new StoreServicePropValidator(new ContextProperties.Required());

    public StoreServiceImpl_Integration() {
        super();
        setCommunicationTypeSupport(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS, true);
    }

    public IntegrationService getIntegrationService() {
        return integrationService;
    }

    @Override
    public void __init(Properties context) throws IOException {
        super.__init(context);
        validator.validate(context);

        //get parent paths to optimize configuration file detection
        String instanceSource = this.context.getProperty(ServiceFactory_Abstract.INSTANCE_SOURCE);
        String instanceSourceType = this.context.getProperty(ServiceFactory_Abstract.INSTANCE_SOURCE_TYPE);
        if (instanceSourceType == null) {
            instanceSourceType = ServiceFactory_Abstract.ContextType.ANY.name();
        }
        ServiceFactory_Abstract.ContextType contextType = ServiceFactory_Abstract.ContextType.valueOf(instanceSourceType);
        FileUtils fileUtils = FileUtils.proposeFileUtils(instanceSource);


        String[] alternatePaths = null;
        if (StringUtils.isNotEmpty(instanceSource)) {
            String instancePath = fileUtils.getParentFolderPathName(instanceSource);
            if (!instancePath.equals(fileUtils.getRootPath())) {
                alternatePaths = new String[]{instancePath};
            } else {
                alternatePaths = new String[0];
            }
        }

        //get integration service
        String serviceIntegrationConfiguration = (String) this.context.get(SERVICE_INTEGRATION_INSTANCE_CONFIGURATION);
        if (StringUtils.isNotEmpty(serviceIntegrationConfiguration)) {
            IntegrationServiceFactory integrationServiceFactory = new IntegrationServiceFactory(serviceIntegrationConfiguration, contextType, alternatePaths);
            IntegrationService integrationService = null;
            try {
                integrationService = integrationServiceFactory.getInstance();
            } catch (Exception e) {
                throw new StoreServiceException(e);
            }
            this.integrationService = integrationService;
        } else {
            throw new StoreServiceException("No integration service is configured.");
        }
    }

    @Override
    public void close() {
        this.integrationService.close();
        super.close();
    }

    @Override
    protected StoreServiceSession openSession(StoreContext storeContext) {
        throw new StoreServiceException("This method should not be used in an Integration implementation.");
    }

    @Override
    protected void closeSession(StoreServiceSession storeSession) {
        throw new StoreServiceException("This method should not be used in an Integration implementation.");
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("moveFrom");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, objectIdentifierFrom, storeServiceFrom.getName(), storeContextFrom);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
        throw new StoreServiceException("The method moveTo is not implemented");
    }

    @Override
    public ObjectIdentifier[] moveFrom(StoreContext storeContext, ObjectIdentifier[] objectIdentifierFrom, StoreService storeServiceFrom, StoreContext storeContextFrom) {
        return super.moveFrom(storeContext, objectIdentifierFrom, storeServiceFrom, storeContextFrom);
    }

    @Override
    public ObjectIdentifier[] moveTo(StoreContext storeContext, ObjectIdentifier[] objectIdentifierFrom, StoreService storeServiceTo, StoreContext storeContextTo) {
        return super.moveTo(storeContext, objectIdentifierFrom, storeServiceTo, storeContextTo);
    }

    @Override
    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
        StoreContext preparedStoreContext = prepareStoreContext(storeContext);
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)) {
            return getLocalStoreService().existsDocument(preparedStoreContext, documentIdentifier);
        } else {
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("existsDocument");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext,documentIdentifier);
            Serializable response = integrationService.sendAndReceive(messageStructure,storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
        if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL)){
            return getLocalStoreService().storeDocument(preparedStoreContext, documentInfo, inputStream, allowCreatePath, versioningType);
        } else {
            if(documentInfo == null){
                throw new StoreServiceException("DocumentInfo object must not be null");
            }
            String documentPath = StoreServiceImpl_Abstract.fileUtils.getRootPath(); //+"upload/";
            if (documentInfo.getParentIdentifier() != null) {
                if (documentInfo.getParentIdentifier().getPath() == null || documentInfo.getParentIdentifier().getPath().isEmpty()){
                    throw new StoreServiceException("ParentIdentifier must have the path completed.");
                }
                documentPath = documentInfo.getParentIdentifier().getPath();
            }
            String documentNameWithExtension = StoreServiceImpl_Abstract.fileUtils.getFileNameWithExtension(StoreServiceImpl_Abstract.fileUtils.getFileBaseName(documentInfo.getName()), documentInfo.getExtension());
            String temporaryFilePath = getLocalStoreService().getPathByConfiguration(PathConfiguration.TEMP_UPLOAD);
            String temporaryFileName = String.valueOf(UUID.randomUUID()) + "_" + documentNameWithExtension;
            DocumentInfo temporaryDocumentInfo = new DocumentInfo(temporaryFilePath, StoreServiceImpl_Abstract.fileUtils.getFileBaseName(temporaryFileName), StoreServiceImpl_Abstract.fileUtils.getFileExtension(temporaryFileName), null, null);
            DocumentIdentifier temporaryDocumentIdentifier = getLocalStoreService().storeDocument(preparedStoreContext,temporaryDocumentInfo,inputStream,true,VersioningType.NONE);

            //DocumentIdentifier temporaryDocumentIdentifier = new DocumentIdentifier();
            //temporaryDocumentIdentifier.setPath(temporaryFilePath + "/" + temporaryFileName);

            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("storeDocument");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentInfo, temporaryDocumentIdentifier, allowCreatePath, versioningType);

            //System.out.println(" ------------- START send msg to JMS queue --------------");
            Serializable response = integrationService.sendAndReceive(messageStructure,storeContext.getCommunicationType());
            //System.out.println(" ------------- FINISH send msg to JMS queue --------------");

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage)response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                return (DocumentIdentifier)messageStructureResponse.getParameters()[0];
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("downloadDocument");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
                if (messageStructureResponse.getException() != null) {
                    throw (messageStructureResponse.getException());
                }
                DocumentIdentifier temporaryDocumentIdentifier = (DocumentIdentifier) messageStructureResponse.getParameters()[0];
                DocumentStream documentStream = getLocalStoreService().downloadDocument(null, temporaryDocumentIdentifier);
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("updateDocumentProperties");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier, documentInfo);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("deleteDocument");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("getDocumentInfo");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, documentIdentifier);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("existsFolder");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("createFolder");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderInfo, createParentIfNotExists);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("deleteFolder");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("getFolderInfo");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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
            StoreServiceMessage messageStructure = new StoreServiceMessage();
            messageStructure.setMethod("listFolderContent");
            messageStructure.setType(StoreServiceMessageType.REQUEST);
            messageStructure.setParameters(preparedStoreContext, folderIdentifier, depth, includeInfo, objectBaseTypes);
            Serializable response = integrationService.sendAndReceive(messageStructure, storeContext.getCommunicationType());

            if (storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS)) {
                StoreServiceMessage messageStructureResponse = (StoreServiceMessage) response;
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

        StoreServiceImpl_Integration ssi = null;
        ssi.getIntegrationService().addStoreServiceMessageListener(new StoreServiceMessageListener() {
            @Override
            public void onReceive(StoreServiceMessageEvent storeServiceMessageEvent) {

            }
        });
    }


}
