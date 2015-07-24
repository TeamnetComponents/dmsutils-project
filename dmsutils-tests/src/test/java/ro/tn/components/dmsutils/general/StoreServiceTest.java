//package ro.tn.components.dmsutils.general;
//
//import org.apache.commons.io.IOUtils;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import ro.croco.integration.dms.commons.FileUtils;
//import ro.croco.integration.dms.toolkit.*;
//import ro.croco.integration.dms.toolkit.jcr.JcrProperties;
//
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.io.StringWriter;
//import java.nio.charset.Charset;
//import java.util.*;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//public class StoreServiceTest {
//    private static FileUtils fileUtils = new FileUtils("/", "/");
//    private static String CONFIG_CMIS = "cmis";
//    private static String CONFIG_JCR = "jcr";
//    private static String CONFIG_JMS = "jms";
//
//    private static String IMPLEMENTATION_NAME = null;
//    private static StoreContext.COMMUNICATION_TYPE_VALUES IMPLEMENTATION_COMMUNICATION = null;
//
//
//    //    private static final String folderPathName = "/TestStoreServiceImplementations/BaseTestFolder";
//    private static final String folderPathName = "/CR_test";
//    //    private static final String documentNameWithExtension = "testFileName.txt";
//    private static final String documentNameWithExtension = "testCR.txt";
//    private static final String documentContent = "Testing document by storing a file built at runtime";
//    private static Boolean documentExists;
//    private static Boolean folderExists;
//
//
//    public static class TestCombination {
//        private final String implementation;
//        private final StoreContext.COMMUNICATION_TYPE_VALUES communicationType;
//
//        public TestCombination(String implementation, StoreContext.COMMUNICATION_TYPE_VALUES communicationType) {
//            this.implementation = implementation;
//            this.communicationType = communicationType;
//        }
//
//        public String getImplementation() {
//            return implementation;
//        }
//
//        public StoreContext.COMMUNICATION_TYPE_VALUES getCommunicationType() {
//            return communicationType;
//        }
//    }
//
//    //------------------------------------------------------------------------------------------------------------------
//    //UTILITY Methods
//    //------------------------------------------------------------------------------------------------------------------
//
//    public StoreService getService() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
//        JcrProperties jcrProperties = new JcrProperties();
//        //StoreServiceFactory ssf = new StoreServiceFactory(jcrProperties.getProperties());
//        StoreServiceFactory ssf = new StoreServiceFactory(IMPLEMENTATION_NAME);
//        return ssf.getService();
//    }
//
//    public StoreMetadata getMetadata() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
//        StoreMetadataFactory smf = new StoreMetadataFactory("storeMetadata_DB");
//        return smf.getMetadata();
//    }
//
//    public StoreContext getStoreContext(StoreService storeService) {
//        StoreContext storeContext = null;
//        StoreContext.Builder builder = StoreContext.builder();
//        if (storeService instanceof StoreServiceImpl_Jcr) {
//            builder.loginAs("FrontOfficeApp");
//        }
//        if (storeService instanceof StoreServiceImpl_Cmis) {
//            builder.loginAs("$UserNameFromSSO");
//        }
//        if (storeService instanceof StoreServiceImpl_Jms) {
//            builder.loginAs("FrontOfficeApp");
//        }
//
//        builder.communicationType(IMPLEMENTATION_COMMUNICATION);
//
//        storeContext = builder.build();
//        return storeContext;
//    }
//
//    public Map<String, Object> getDocumentProperties(StoreService storeService, StoreContext storeContext) {
//        Map<String, Object> properties = null;
//        if (IMPLEMENTATION_NAME.equals(CONFIG_JCR) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//            properties = new HashMap<String, Object>();
//            properties.put("frontUserName", "gigi");
//            properties.put("documentType", "CerereRambursare");
//            properties.put("documentKey", "3");
//        }
//        if (IMPLEMENTATION_NAME.equals(CONFIG_CMIS) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && !storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//            properties = new HashMap<String, Object>();
//        }
//        return properties;
//    }
//
//    public String getDocumentType(StoreService storeService, StoreContext storeContext) {
//        if (IMPLEMENTATION_NAME.equals(CONFIG_JCR) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//            return StoreServiceImpl_Jcr.JCR_MIX_FILE_ATTRIBUTES;
//        }
//        if (IMPLEMENTATION_NAME.equals(CONFIG_CMIS) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && !storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//            return "cmis:document";
//        }
//        return null;
//    }
//
//    public Map<String, Object> getFolderProperties(StoreService storeService, StoreContext storeContext) {
//        Map<String, Object> properties = null;
//        if (IMPLEMENTATION_NAME.equals(CONFIG_JCR) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//
//        }
//        if (IMPLEMENTATION_NAME.equals(CONFIG_CMIS) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && !storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//
//        }
//        return properties;
//    }
//
//    public String getFolderType(StoreService storeService, StoreContext storeContext) {
//        if (IMPLEMENTATION_NAME.equals(CONFIG_JCR) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//            return ObjectBaseType.FOLDER.name();
//        }
//        if (IMPLEMENTATION_NAME.equals(CONFIG_CMIS) || (IMPLEMENTATION_NAME.equals(CONFIG_JMS) && !storeContext.getCommunicationType().equals(StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL))) {
//            return null;
//        }
//        return null;
//    }
//
//
//    public boolean sameProperties(Map<String, Object> docProperties1, Map<String, Object> docProperties2) {
//        if (docProperties1 == null && docProperties2 == null) return true;
//        if (docProperties1 == null) return false;
//        if (docProperties2 == null) return false;
//        if (docProperties1.size() != docProperties2.size()) return false;
//        for (String propertyName : docProperties1.keySet()) {
//            if (!docProperties2.containsKey(propertyName)) return false;
//            if (!docProperties1.get(propertyName).equals(docProperties2.get(propertyName))) return false;
//        }
//        return true;
//
//    }
//
//    //------------------------------------------------------------------------------------------------------------------
//    //TEST Scenarios
//    //------------------------------------------------------------------------------------------------------------------
//
//
//    //@Test
//    public void test_001_existsDocument() throws Exception {
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(fileUtils.concatenate(folderPathName, fileUtils.getFileBaseName(documentNameWithExtension))).build();
//        BooleanResponse booleanResponse = ss.existsDocument(sc, documentIdentifier);
//        documentExists = booleanResponse.getValue();
//
//        if (documentExists == null)
//            System.out.println("<Unknown - null receiver on asynchonous> document - " + documentIdentifier.getPath());
//        else if (documentExists == true)
//            System.out.println("<Found> document - " + documentIdentifier.getPath());
//        else
//            System.out.println("<Not Found> document - " + documentIdentifier.getPath());
//
//        assert booleanResponse.getRequestId().equals(sc.getRequestIdentifier());
//    }
//
//    //@Test
//    public void test_002_existsFolder() throws Exception {
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        FolderIdentifier folderIdentifier = FolderIdentifier.builder().withPath(folderPathName).build();
//        BooleanResponse booleanResponse = ss.existsFolder(sc, folderIdentifier);
//        folderExists = booleanResponse.getValue();
//
//        if (folderExists == null)
//            System.out.println("<Unknown - null receiver on asynchonous> folder - " + folderIdentifier.getPath());
//        else if (folderExists == true)
//            System.out.println("<Found> folder - " + folderIdentifier.getPath());
//        else
//            System.out.println("<Not Found> folder - " + folderIdentifier.getPath());
//
//        assert booleanResponse.getRequestId().equals(sc.getRequestIdentifier());
//    }
//
//    @Test
//    public void test_003_storeDocument() throws Exception {
//        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(fileUtils.concatenate(folderPathName, fileUtils.getFileBaseName(documentNameWithExtension))).build();
//        Map<String, Object> docProperties = getDocumentProperties(ss, sc);
//        //Map<String, Object> docProperties = new HashMap<String, Object>();
//        String documentType = getDocumentType(ss, sc);
//        DocumentInfo documentInfo = new DocumentInfo(folderPathName, fileUtils.concatenate(folderPathName, documentNameWithExtension), documentType, docProperties);
//        DocumentIdentifier documentIdentifierStored = ss.storeDocument(sc, documentInfo, is, true, VersioningType.MAJOR);
//        if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//            assert documentIdentifierStored.getPath().equals(documentIdentifier.getPath());
//        }
//        assert documentIdentifierStored.getRequestId().equals(sc.getRequestIdentifier());
//    }
//
//    //@Test
//    public void test_004_deleteDocument() throws Exception {
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(fileUtils.concatenate(folderPathName, fileUtils.getFileBaseName(documentNameWithExtension))).build();
//        RequestIdentifier requestIdentifier = ss.deleteDocument(sc, documentIdentifier);
//        assert requestIdentifier.getRequestId().equals(sc.getRequestIdentifier());
//    }
//
//
//    //@Test
//    public void test_005_downloadDocument() throws Exception {
//        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(fileUtils.concatenate(folderPathName, fileUtils.getFileBaseName(documentNameWithExtension))).build();
//        DocumentStream documentStream = ss.downloadDocument(sc, documentIdentifier);
//
//        if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//            StringWriter writer = new StringWriter();
//            IOUtils.copy(documentStream.getInputStream(), writer, "UTF-16");
//            String documentContentDownloaded = writer.toString();
//            if (documentContentDownloaded.indexOf(0) > 0) {
//                documentContentDownloaded = documentContentDownloaded.substring(0, documentContentDownloaded.indexOf(0));
//            }
//            //assert documentStream.getFileName().equals(documentNameWithExtension);
//            //TODO din CMIS trebuie scoasa versiunea din filename:
//            //de ex: cmis intoarce "testFileName_v2.0.txt" si ar trebui sa intoarca testFileName.txt
//            assert documentContentDownloaded.equals(documentContent);
//        }
//        assert documentStream.getRequestId().equals(sc.getRequestIdentifier());
//
//    }
//
//    //@Test
//    public void test_006_getDocumentInfo() throws Exception {
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(fileUtils.concatenate(folderPathName, fileUtils.getFileBaseName(documentNameWithExtension))).build();
//        DocumentInfo documentInfo = ss.getDocumentInfo(sc, documentIdentifier);
//        Map<String, Object> docProperties = getDocumentProperties(ss, sc);
//        String documentType = getDocumentType(ss, sc);
//        System.out.println("DocumentInfo:" + '\n' + documentInfo);
//        assert documentInfo.getRequestId().equals(sc.getRequestIdentifier());
//        if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//            assert documentInfo.getName().equals(fileUtils.getFileBaseName(documentNameWithExtension));
//            assert fileUtils.getFileExtension(documentNameWithExtension).equals(documentInfo.getExtension());
//            assert documentInfo.getParentIdentifier().getPath().equals(folderPathName);
//            assert sameProperties(documentInfo.getProperties(), docProperties);
//            assert documentInfo.getType().equals(documentType);
//        }
//    }
//
//    //@Test
//    public void test_007_getFolderInfo() throws Exception {
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        FolderIdentifier folderIdentifier = FolderIdentifier.builder().withPath(folderPathName).build();
//
//        FolderInfo folderInfo = ss.getFolderInfo(sc, folderIdentifier);
//        assert folderInfo.getRequestId().equals(sc.getRequestIdentifier());
//        if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//            assert folderInfo.getName().equals(fileUtils.getFileBaseName(documentNameWithExtension));
//            assert folderInfo.getParentIdentifier().getPath().equals(folderPathName);
//        }
//    }
//
//
//    //@Test
//    public void test_008_updateDocumentProperties() throws Exception {
//
//    }
//
//    //@Test
//    public void test_009_deleteFolder() throws Exception {
//    }
//
//    //@Test
//    public void test_010_listFolderContent() throws Exception {
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        FolderIdentifier folderIdentifierList = FolderIdentifier.builder().withPath(fileUtils.concatenate(folderPathName, "ListTestFolder")).build();
//        /*
//        * urmatoarea structura se testeaza:
//        *
//        * [Folder1]
//        *   [Folder1_1]
//        *       Document1_1_A  (.txt)
//        *       Document1_1_B  (.pdf)
//        *   Document1_C (.xml)
//        * [Folder2]
//        *   Document2_D (.jpg)
//        * [Folder3]
//        *
//        *
//        * */
//
//        FolderIdentifier folderIdentifier1 = FolderIdentifier.builder().withPath(fileUtils.concatenate(folderIdentifierList.getPath(), "Folder1")).build();
//        FolderInfo folderInfo1 = new FolderInfo(fileUtils.getParentFolderPathName(folderIdentifier1.getPath()), fileUtils.getFolderName(folderIdentifier1.getPath()), getFolderType(ss, sc), getFolderProperties(ss, sc));
//
//        FolderIdentifier folderIdentifier1_1 = FolderIdentifier.builder().withPath(fileUtils.concatenate(fileUtils.concatenate(folderIdentifierList.getPath(), "Folder1"), "Folder1_1")).build();
//        FolderInfo folderInfo1_1 = new FolderInfo(fileUtils.getParentFolderPathName(folderIdentifier1_1.getPath()), fileUtils.getFolderName(folderIdentifier1_1.getPath()), getFolderType(ss, sc), getFolderProperties(ss, sc));
//
//        String document1_1_A = "document1_1_A.txt";
//        DocumentInfo documentInfo1_1_A = new DocumentInfo(folderIdentifier1_1.getPath(), fileUtils.getFileBaseName(document1_1_A), fileUtils.getFileExtension(document1_1_A), getDocumentType(ss, sc), getDocumentProperties(ss, sc));
//
//        String document1_1_B = "document1_1_B.pdf";
//        DocumentInfo documentInfo1_1_B = new DocumentInfo(folderIdentifier1_1.getPath(), fileUtils.getFileBaseName(document1_1_B), fileUtils.getFileExtension(document1_1_B), getDocumentType(ss, sc), getDocumentProperties(ss, sc));
//
//        String document1_C = "document1_C.xml";
//        DocumentInfo documentInfo1_C = new DocumentInfo(folderIdentifier1.getPath(), fileUtils.getFileBaseName(document1_C), fileUtils.getFileExtension(document1_C), getDocumentType(ss, sc), getDocumentProperties(ss, sc));
//
//        FolderIdentifier folderIdentifier2 = FolderIdentifier.builder().withPath(fileUtils.concatenate(folderIdentifierList.getPath(), "Folder2")).build();
//        FolderInfo folderInfo2 = new FolderInfo(fileUtils.getParentFolderPathName(folderIdentifier2.getPath()), fileUtils.getFolderName(folderIdentifier2.getPath()), getFolderType(ss, sc), getFolderProperties(ss, sc));
//
//        String document2_D = "document2_D.jpg";
//        DocumentInfo documentInfo2_D = new DocumentInfo(folderIdentifier2.getPath(), fileUtils.getFileBaseName(document2_D), fileUtils.getFileExtension(document2_D), getDocumentType(ss, sc), getDocumentProperties(ss, sc));
//
//        FolderIdentifier folderIdentifier3 = FolderIdentifier.builder().withPath(fileUtils.concatenate(folderIdentifierList.getPath(), "Folder3")).build();
//        FolderInfo folderInfo3 = new FolderInfo(fileUtils.getParentFolderPathName(folderIdentifier3.getPath()), fileUtils.getFolderName(folderIdentifier3.getPath()), getFolderType(ss, sc), getFolderProperties(ss, sc));
//
//        ss.createFolder(sc, folderInfo1, true);
//        ss.createFolder(sc, folderInfo1_1, true);
//        ss.createFolder(sc, folderInfo2, true);
//        ss.createFolder(sc, folderInfo3, true);
//
//        InputStream is;
//        is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        ss.storeDocument(sc, documentInfo1_1_A, is, false, VersioningType.MAJOR);
//        is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        ss.storeDocument(sc, documentInfo1_1_B, is, false, VersioningType.MAJOR);
//        is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        ss.storeDocument(sc, documentInfo1_C, is, false, VersioningType.MAJOR);
//        is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        ss.storeDocument(sc, documentInfo2_D, is, false, VersioningType.MAJOR);
//
//        ObjectInfoTree tree = ss.listFolderContent(sc, FolderIdentifier.builder().withPath(folderIdentifierList.getPath()).build(), 5, true, ObjectBaseType.FOLDER, ObjectBaseType.DOCUMENT);
//        tree.print();
//    }
//
//
////    @Test
////    public void testStoreMetadata() throws Exception {
////        System.out.println("-------------------------------testStoreMetadata()----------------------------------");
////        StoreService storeService = getService();
////        StoreMetadata storeMetadata = getMetadata();
////        Properties properties = new Properties();
////        StoreMetadata.Metadata<DocumentInfo> metadata = storeMetadata.computeDocumentMetadata("CerereRambursare", storeService, properties);
////        System.out.println(metadata);
////    }
//
//    @Test
//    public void testStoreMetadata() throws Exception {
//        IMPLEMENTATION_NAME = "jcr";
//        IMPLEMENTATION_COMMUNICATION = StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS;
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        StoreMetadata storeMetadata = ss.getStoreMetadata();
//        StoreMetadata.MetadataProperties properties = StoreMetadata.MetadataProperties.builder().withDocumentKey("2").withDocumentName("cerere.pdf").withDocumentType("CerereRambursare").withFrontUserName("gigel.popescu").build();
//
//        //obtinere metadate
//        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        DocumentIdentifier documentIdentifierStored = ss.storeDocument(sc, properties, is);
//        DocumentInfo documentInfo = ss.getDocumentInfo(sc, documentIdentifierStored);
//        System.out.println(documentInfo);
//    }
//
//
//    @Test
//    public void testStoreDocumentJMS() throws Exception {
//        IMPLEMENTATION_NAME = "jms";
//        IMPLEMENTATION_COMMUNICATION = StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS;
//        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode(documentContent).array());
//        StoreService ss = getService();
//        StoreContext sc = getStoreContext(ss);
//        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath(fileUtils.concatenate(folderPathName, fileUtils.getFileBaseName(documentNameWithExtension))).build();
//        Map<String, Object> docProperties = getDocumentProperties(ss, sc);
//        //Map<String, Object> docProperties = new HashMap<String, Object>();
//        String documentType = getDocumentType(ss, sc);
//        DocumentInfo documentInfo = new DocumentInfo(folderPathName, fileUtils.concatenate(folderPathName, documentNameWithExtension), documentType, docProperties);
//        DocumentIdentifier documentIdentifierStored = ss.storeDocument(sc, documentInfo, is, true, VersioningType.MAJOR);
//        if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//            assert documentIdentifierStored.getPath().equals(documentIdentifier.getPath());
//        }
//        assert documentIdentifierStored.getRequestId().equals(sc.getRequestIdentifier());
//}
//
//
//    @Test
//    public void testStoreService() throws Exception {
//        List<TestCombination> toTest = new ArrayList<TestCombination>();
//        //toTest.add(new TestCombination(CONFIG_JCR, StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS));
//        //toTest.add(new TestCombination(CONFIG_CMIS, StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS));
//        //toTest.add(new TestCombination(CONFIG_JMS, StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS_LOCAL));
//        //toTest.add(new TestCombination(CONFIG_JMS, StoreContext.COMMUNICATION_TYPE_VALUES.SYNCHRONOUS));
//        //toTest.add(new TestCombination(CONFIG_JMS, StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS));
//
//        for (TestCombination tc : toTest) {
//
//            IMPLEMENTATION_NAME = tc.getImplementation();
//            IMPLEMENTATION_COMMUNICATION = tc.getCommunicationType();
//
//            System.out.println("****************************************************************************");
//            System.out.println("*                                                                          *");
//            System.out.println("*    TESTING THE IMPLEMENTATION <" + IMPLEMENTATION_NAME + "> WITH <" + IMPLEMENTATION_COMMUNICATION.name() + "> COMMUNICATION");
//            System.out.println("*                                                                          *");
//            System.out.println("****************************************************************************");
//
//            System.out.println("----------------------------------------------");
//            System.out.println("delete document if exists");
//            test_001_existsDocument();
//            if (documentExists != null && documentExists) {
//                test_004_deleteDocument();
//                System.out.println("----------------------------------------------");
//                System.out.println("Document deleted");
//            }
//            System.out.println("----------------------------------------------");
//            System.out.println("delete document if exists");
//            test_001_existsDocument();
//            if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//                assert documentExists == false;
//            } else {
//                assert documentExists == null;
//            }
//            //store document
//            test_003_storeDocument();
//            System.out.println("----------------------------------------------");
//            System.out.println("Document stored");
//            test_001_existsDocument();
//            if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//                assert documentExists == true;
//            } else {
//                assert documentExists == null;
//            }
//
//            //delete document
//            test_004_deleteDocument();
//            System.out.println("----------------------------------------------");
//            System.out.println("Document deleted");
//            test_001_existsDocument();
//            if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//                assert documentExists == false;
//            } else {
//                assert documentExists == null;
//            }
//
//            //store document
//            test_003_storeDocument();
//            System.out.println("----------------------------------------------");
//            System.out.println("Document stored");
//            test_001_existsDocument();
//            if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//                assert documentExists == true;
//            } else {
//                assert documentExists == null;
//            }
//
//            //store document over
//            test_003_storeDocument();
//            System.out.println("----------------------------------------------");
//            System.out.println("Document stored");
//            test_001_existsDocument();
//            if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//                assert documentExists == true;
//            } else {
//                assert documentExists == null;
//            }
//
//            //check document content
//            test_005_downloadDocument();
//            System.out.println("----------------------------------------------");
//            System.out.println("Document downloaded with success.");
//
//            //check document info
//            test_006_getDocumentInfo();
//            System.out.println("----------------------------------------------");
//            System.out.println("Document info retrieved with success.");
//
//            //check if document's folder exists
//            if (!IMPLEMENTATION_COMMUNICATION.equals(StoreContext.COMMUNICATION_TYPE_VALUES.ASYNCHRONOUS)) {
//                System.out.println("----------------------------------------------");
//                System.out.println("check if document's folder exists");
//                test_002_existsFolder();
//                assert folderExists == true;
//                System.out.println("Document's folder exists :) - of course.");
//                System.out.println("----------------------------------------------");
//            }
//            //list a document structure
//            System.out.println("list a folder content");
//            test_010_listFolderContent();
//            System.out.println("----------------------------------------------");
//
//            System.out.println("----------------------------------------------");
//        }
//    }
//}