package ro.croco.integration.dms.jmeter;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import ro.croco.integration.dms.toolkit.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Lucian.Dragomir on 11/14/2014.
 */
public class JmeterFileUtils {

    public static List<String> listFileNames(String filePath, StoreService ss) {
        List<String> result = new ArrayList<String>();
        if (ss == null) {
            Collection<File> fileCollection = org.apache.commons.io.FileUtils.listFiles(new File(filePath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : fileCollection) {
                result.add(file.getAbsolutePath());
            }
            fileCollection.clear();
        } else {
            StoreContext storeContext = StoreContext.builder().build();
            FolderIdentifier folderIdentifier = FolderIdentifier.builder().withPath(filePath).build();
            ObjectInfoTree objectInfoTree = ss.listFolderContent(storeContext, folderIdentifier, -1, false, ObjectBaseType.FOLDER, ObjectBaseType.DOCUMENT);
            List<ObjectInfo> objectInfoList = objectInfoTree.listContent();
            for (ObjectInfo objectInfo : objectInfoList) {
                if (objectInfo instanceof DocumentInfo) {
                    result.add(((DocumentInfo) objectInfo).getIdentifier().getPath());
                }
            }
        }
        return result;
    }


    public static void listFileNames(String filePath, String serviceConfigFile, String fileDestination) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        StoreService ss = (StringUtils.isEmpty(serviceConfigFile)) ? null : BasicSamplerClient.getStoreService(serviceConfigFile);
        System.out.println("ss: " + ss);
        List<String> listFileNames = listFileNames(filePath, ss);
        String eol = System.getProperty("line.separator");
        FileWriter fw = new FileWriter(fileDestination, false);
        for (String fileName : listFileNames) {
            fw.write(fileName + eol);
        }
        fw.close();
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        listFileNames("D:\\__KITURI\\IBM DB2\\IBM Data Studio\\DS-V4101-win", null, "D:\\__KITURI\\IBM DB2\\IBM Data Studio\\DS-V4101-win\\out.txt");
        System.out.println(System.getProperty("file.separator"));
    }

}
