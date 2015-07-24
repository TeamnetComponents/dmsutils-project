package integration.service.common;

import ro.croco.integration.dms.toolkit.*;

import java.io.InputStream;

/**
 * Created by hanna.botar on 7/7/2014.
 */
public interface DmsService {

    DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier);

    DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType);

}
