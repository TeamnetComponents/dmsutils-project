package ro.croco.integration.dms.jmeter;

import org.apache.commons.lang.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import ro.croco.integration.dms.toolkit.DocumentIdentifier;
import ro.croco.integration.dms.toolkit.DocumentInfo;
import ro.croco.integration.dms.toolkit.StoreContext;
import ro.croco.integration.dms.toolkit.VersioningType;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by Lucian.Dragomir on 11/13/2014.
 */
public class DocumentUploadSamplerClient extends BasicSamplerClient {
    private static String RUN_FILE_NAME = "RUN_FILE_NAME";
    private static String RUN_USER = "RUN_USER";
    private static String RUN_PASSWORD = "RUN_PASSWORD";

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(STORE_SERVICE_FILE_NAME, "");
        defaultParameters.addArgument(PATH_LOCAL, "");
        defaultParameters.addArgument(PATH_DMS, "");
        defaultParameters.addArgument(RUN_USER, "");
        defaultParameters.addArgument(RUN_PASSWORD, "");
        defaultParameters.addArgument(PROCESS_FILE_PATH, "");
        return defaultParameters;
    }


    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult sampleResult = new SampleResult();

        //start counters
        //sampleResult.sampleStart();

        //init variables
        StoreContext storeContext = null;
        DocumentInfo documentInfo = null;
        InputStream inputStream = null;
        VersioningType versioningType = VersioningType.MAJOR;
        boolean allowCreatePath = true;
        DocumentIdentifier documentIdentifier = null;
        Map<String, Object> documentProperties = null;
        String documentType = "cmis:document";

        String pathLocal = context.getParameter(PATH_LOCAL);
        String pathDMS = context.getParameter(PATH_DMS);
        String user = context.getParameter(RUN_USER);
        String password = context.getParameter(RUN_PASSWORD);
        String processFilePath = context.getParameter(PROCESS_FILE_PATH);

        try {
            StoreContext.Builder storeContextBuilder = StoreContext.builder();
            if (StringUtils.isNotEmpty(user)) {
                storeContextBuilder.loginBasic(user, password);
            }
            storeContext = storeContextBuilder.build();
            documentIdentifier = testUploadDocument(sampleResult, storeService, storeContext, pathDMS, pathLocal, processFilePath, documentProperties, documentType);
//            documentIdentifier = testUploadDocument(storeService, storeContext, pathDMS, pathLocal, processFilePath, documentProperties, documentType);
//            sampleResult.setResponseData(documentIdentifier.toString(), null);
//            sampleResult.setSuccessful(true);
//            sampleResult.setResponseCodeOK();
//            sampleResult.setResponseMessageOK();
        } catch (Exception e) {
//            sampleResult.setSuccessful(false);
//            sampleResult.setResponseCode("500");
//            sampleResult.setResponseMessage(e.toString());
        } finally {
            //sampleResult.sampleEnd();
        }
        return sampleResult;
    }
}



