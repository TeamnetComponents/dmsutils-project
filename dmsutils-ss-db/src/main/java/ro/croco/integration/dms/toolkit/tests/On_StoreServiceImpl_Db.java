package ro.croco.integration.dms.toolkit.tests;

import org.apache.commons.compress.utils.IOUtils;

import ro.croco.integration.dms.toolkit.*;



import java.io.*;
import java.util.Date;
import java.util.HashMap;



/**
 * Created by battamir.sugarjav on 2/18/2015.
 */
public class On_StoreServiceImpl_Db {

    private StoreService getStoreServiceImpl(String configFileLocation){
        try {
            StoreServiceFactory storeServiceFactory = new StoreServiceFactory(configFileLocation);
            return storeServiceFactory.getService();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void retrieve_local_store_service_db_test(){
        String configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties";
        StoreServiceImpl_Db dbStoreService = (StoreServiceImpl_Db)this.getStoreServiceImpl(configFileLocation);

        assertNotNull(dbStoreService.openSession(null).getConnection());
        assertNotNull(dbStoreService);
    }

    @Test
    public void store_document_type_fresh_unversioned() throws IOException {
        String configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties";
        StoreServiceImpl_Db dbStoreService = (StoreServiceImpl_Db)this.getStoreServiceImpl(configFileLocation);
        DocumentInfo documentInfo = new DocumentInfo("/dir1/dir2/text.txt",null,null);
        documentInfo.setExtension(".extensie");
        documentInfo.setProperties(new HashMap<String, Object>());
        documentInfo.getProperties().put("inputStreamNameId", new Date().toString().replaceAll(" ", "").replaceAll(":", ""));
        documentInfo.getProperties().put("inputStreamMimeType","application/text");
        InputStream fileInputStream = new FileInputStream(new File("C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties"));
        InputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(fileInputStream));

        assertNotNull(dbStoreService.storeDocument(null, documentInfo, byteArrayInputStream,false, VersioningType.NONE));
    }

    @Test
    public void store_document_type_update_unversioned(){
        String configFileLocation = "C:\\TeamnetProjects\\DMS-UTILS\\ss-fo-db.properties";
        StoreServiceImpl_Db dbStoreService = (StoreServiceImpl_Db)this.getStoreServiceImpl(configFileLocation);
        DocumentInfo documentInfo = new DocumentInfo("/dir1/dir2/text.txt",null,null);
        DocumentIdentifier identifier = new DocumentIdentifier();
        identifier.setId("29");
        documentInfo.setIdentifier(identifier);
        assertNotNull(dbStoreService.storeDocument(null, documentInfo,null,false, VersioningType.NONE));
    }
}

