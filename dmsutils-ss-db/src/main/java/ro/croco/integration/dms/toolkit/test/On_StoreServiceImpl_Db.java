package ro.croco.integration.dms.toolkit.test;

import org.apache.commons.compress.utils.IOUtils;

import org.junit.BeforeClass;
import org.junit.Test;
import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
import ro.croco.integration.dms.toolkit.*;



import java.io.*;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by battamir.sugarjav on 2/18/2015.
 */
public class On_StoreServiceImpl_Db {

    public static StoreService getStoreServiceImpl(String configFileLocation){
        try {
            StoreServiceFactory storeServiceFactory = new StoreServiceFactory(configFileLocation);
            return storeServiceFactory.getService();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String configFileLocation = null;
    private static StoreServiceImpl_Db dbStoreService = null;

    @BeforeClass
    public static void getStoreServiceDb(){
        configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties";
        dbStoreService = (StoreServiceImpl_Db)getStoreServiceImpl(configFileLocation);
    }

    @Test//init
    public void retrieve_local_store_service_db(){
        assertNotNull(dbStoreService);
    }

    @Test//connection
    public void retrieve_local_store_service_db_connection(){
        assertNotNull(dbStoreService.openSession(null).getConnection());
    }

    @Test//STORE_DOCUMENT
    public void store_document_type_unversioned_with_path() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo("dir1/dir2/textasdasdasd.txt",null,null);
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        assertNotNull(dbStoreService.storeDocument(null,documentInfo,byteArrayInputStream,false, VersioningType.NONE));
    }

    @Test//STORE_DOCUMENT
    public void store_document_type_unversioned_no_path() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo("text.txt",null,null);
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        assertNotNull(dbStoreService.storeDocument(null,documentInfo,byteArrayInputStream,false, VersioningType.NONE));
    }

    @Test//STORE_DOCUMENT
    public void store_document_type_versioned_MAJOR_with_path() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo("/dir1/dir2/text_v.txt",null,null);
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        assertNotNull(dbStoreService.storeDocument(null,documentInfo,byteArrayInputStream,false, VersioningType.MAJOR));
    }

    @Test//STORE_DOCUMENT
    public void store_document_type_versioned_MINOR_with_path() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo("dir1/dir2/textasdasda.txt",null,null);
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        assertNotNull(dbStoreService.storeDocument(null,documentInfo,byteArrayInputStream,false, VersioningType.MINOR));
    }

    @Test(expected= StoreServiceException.class)//STORE_DOCUMENT
    public void store_document_bad_document_info() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo("text_vMINOR_123.txt",null,null);//empty/type/inputstream=null/name=null
        documentInfo.setParentIdentifier(new FolderIdentifier());
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));
        dbStoreService.storeDocument(null,documentInfo,byteArrayInputStream,false, VersioningType.MINOR);
    }

    @Test//ExistsDocument
    public void exists_document_check_by_id(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withId("35_16").build();
        assertTrue(dbStoreService.existsDocument(null,documentIdentifier).getValue());
    }

    @Test//ExistsDocument
     public void exists_document_check_by_id_precedence_over_path(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withId("35_16").withPath("/dir1/dir2/_text").build();
        assertTrue(dbStoreService.existsDocument(null,documentIdentifier).getValue());
    }

    @Test//ExistsDocument
    public void exists_document_check_by_path(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath("/dir1/dir2/_text").build();
        assertTrue(dbStoreService.existsDocument(null,documentIdentifier).getValue());
    }

    @Test//DownloadDocument
    public void download_document_by_id(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withId("34_14").build();
        DocumentStream documentStream = dbStoreService.downloadDocument(null,documentIdentifier);
        assertNotNull(documentStream);
        System.out.println();
        System.out.println(documentStream.getFileName());
        System.out.println(documentStream.getMimeType());
        System.out.println(documentStream.getInputStream());
    }

    @Test//DownloadDocument
    public void download_document_by_id_precedence_over_path(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withId("34_14").withPath("/dir1/dir22123/_text").build();
        DocumentStream documentStream = dbStoreService.downloadDocument(null,documentIdentifier);
        assertNotNull(documentStream);
        System.out.println();
        System.out.println(documentStream.getFileName());
        System.out.println(documentStream.getMimeType());
        System.out.println(documentStream.getInputStream());
    }

    @Test//DownloadDocument
    public void download_document_by_path(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath("/dir1/dir2_text").build();
        DocumentStream documentStream = dbStoreService.downloadDocument(null,documentIdentifier);
        assertNotNull(documentStream);
        System.out.println();
        System.out.println(documentStream.getFileName());
        System.out.println(documentStream.getMimeType());
        System.out.println(documentStream.getInputStream());
    }

    @Test//DeleteDocument
    public void delete_document_by_id(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withId("36_18").build();
        assertNotNull(dbStoreService.deleteDocument(null,documentIdentifier));
    }

    @Test//DeleteDocument
    public void delete_document_by_path(){
        DocumentIdentifier documentIdentifier = DocumentIdentifier.builder().withPath("dir1/dir2/_textasdasda").withVersion("0.1").build();
        assertNotNull(dbStoreService.deleteDocument(null,documentIdentifier));
    }
}

