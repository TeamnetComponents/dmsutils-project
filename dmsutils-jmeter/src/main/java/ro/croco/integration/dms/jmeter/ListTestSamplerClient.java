package ro.croco.integration.dms.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Created by Lucian.Dragomir on 11/18/2014.
 */
public class ListTestSamplerClient extends BasicSamplerClient {
    private static String PATH_TO_DIR = "PATH_TO_DIR";
    private static String FILE_DESTINATION = "FILE_DESTINATION";

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(STORE_SERVICE_FILE_NAME, "");
        defaultParameters.addArgument(PATH_TO_DIR, "");
        defaultParameters.addArgument(FILE_DESTINATION, "");
        return defaultParameters;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        String filePath = context.getParameter(PATH_TO_DIR);
        String fileDest = context.getParameter(FILE_DESTINATION);
        String storeServiceFile = context.getParameter(STORE_SERVICE_FILE_NAME);

        SampleResult sampleResult = new SampleResult();
        sampleResult.sampleStart(); // start stopwatch
        try {
            JmeterFileUtils.listFileNames(filePath, storeServiceFile, fileDest);
            sampleResult.setResponseData(fileDest, null);
            sampleResult.setSuccessful(true);
            sampleResult.setResponseCodeOK();
            sampleResult.setResponseMessageOK();
        } catch (Exception e) {
            sampleResult.setSuccessful(false);
            sampleResult.setResponseCode("500");
            sampleResult.setResponseMessage(e.toString());
        } finally {
            sampleResult.sampleEnd();
        }
        return sampleResult;
    }

}
