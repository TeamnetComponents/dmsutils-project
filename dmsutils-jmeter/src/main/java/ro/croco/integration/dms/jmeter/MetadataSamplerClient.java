package ro.croco.integration.dms.jmeter;

import ro.croco.integration.dms.toolkit.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Lucian.Dragomir on 12/4/2014.
 */
public class MetadataSamplerClient {


    public static void maindd(String[] args) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
        StoreServiceFactory ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-fo.properties");
        StoreService ss = ssf.getService();
        StoreContext sc = StoreContext.builder().build();

        Properties properties = new Properties();
        properties.put("frontUserName", "gigi");
        properties.put("documentType", "CR-FCR");
        //properties.put("documentContext", "DEFAULT");
        properties.put("documentKey", "2");
        properties.put("documentName", "gigi.pdf");

        MetadataService.Metadata<DocumentInfo> metadata = ss.getMetadataService().computeDocumentMetadata(properties.getProperty("documentType"), properties.getProperty("documentContext"), ss, sc, properties);
        System.out.println(metadata);
    }


    public static void main(String[] args) throws Exception {
        StoreServiceFactory ssf = null;
        //ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-ciprian.properties");
        //ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-domino-dev.properties");
        //ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-fo.properties");
        ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-local.properties");
//        System.out.println(UUID.randomUUID().toString().toUpperCase());
        StoreService ss = ssf.getService();
        StoreContext sc = StoreContext.builder().loginAs("FrontOffice").build();

        System.out.println("----------------------------------");
        System.out.println("createFolder");
        Map<String, Object> documentMetadata = new HashMap<String, Object>();
        //folderProperties.put("ID Beneficiar", Integer.valueOf(100));

        String idDocument = "1466";
        String numarInregistrare = "A111";
        String dataInregistrare = "11.12.2014";
        String tipDocument = "Cerere de inscriere la pensie LV";
        String tipIntocmit = "Electronic";
        String identificator = "1074225634";
        String CNP = "1880925045418";
        String numeSiPrenume = "IRIMIA ALEXANDRU";
        String cui_CIF = null;//
        String denumire = null;
        String observatii = null;
        String termenLegalSolutionare = "30";
        String dataLegalaPentruSolutionare = "10.01.2015";
        String termenPropusPentruSolutionare = null;//rs.getString("termenPropusPentruSolutionare");
        String stareDocument = "Dezarhivat";
        String numarExtern = null;//rs.getString("numarExtern");
        String modintrare = "Electronic";
        String numarRecomandata = null;//rs.getString("numarRecomandata");
        String dataRecomandata = null;//rs.getString("dataRecomandata");
        String alteDateDeIdentificare = null;//rs.getString("alteDateDeIdentificare");
        String responsabil = "Gabriela Gabriela";
        String compartiment = "Registratura generala";
        String tipDocumentExtInt = "Intrare-Extern";

//        if (idDocument != null) documentMetadata.put("ID document", idDocument);
        if (numarInregistrare != null) documentMetadata.put("Numar inregistrare", numarInregistrare);
//        if (dataInregistrare != null) documentMetadata.put("Data inregistrare", dataInregistrare);
        if (tipDocument != null) documentMetadata.put("Tip document", tipDocument);
//        if (tipIntocmit!=null) documentMetadata.put("Intocmit",tipIntocmit);
        if (identificator != null) documentMetadata.put("Identificator", identificator);
//        if (CNP!=null) documentMetadata.put("CNP",CNP);
//        if (numeSiPrenume != null) documentMetadata.put("Nume si prenume", numeSiPrenume);
        if (cui_CIF != null) documentMetadata.put("Cod de indentificare", cui_CIF);
        if (denumire != null) documentMetadata.put("Denumire", denumire);
        if (observatii != null) documentMetadata.put("Observatii", observatii);
//        if (termenLegalSolutionare != null) documentMetadata.put("Termen legal de solutionare", termenLegalSolutionare);
//        if (dataLegalaPentruSolutionare != null)
//            documentMetadata.put("Data legala de solutionare", dataLegalaPentruSolutionare);
        if (termenPropusPentruSolutionare != null)
            documentMetadata.put("Termen propus pentru solutionare", termenPropusPentruSolutionare);
        if (stareDocument != null) documentMetadata.put("Stare document", stareDocument);
        if (numarExtern != null) documentMetadata.put("Numar extern", numarExtern);
        if (modintrare != null) documentMetadata.put("Mod intrare", modintrare);
        if (numarRecomandata != null) documentMetadata.put("Numar recomandata", numarRecomandata);
        if (dataRecomandata != null) documentMetadata.put("Data recomandata", dataRecomandata);
        if (alteDateDeIdentificare != null) documentMetadata.put("Alte date identificare", alteDateDeIdentificare);
//        if (responsabil != null) documentMetadata.put("Responsabil", responsabil);
        if (compartiment != null) documentMetadata.put("Compartiment", compartiment);
//        if (tipDocumentExtInt != null) documentMetadata.put("Tip document INT/EXT", tipDocumentExtInt);
        GregorianCalendar value = new java.util.GregorianCalendar(2014, 12, 16);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();

        documentMetadata.put("Data extern", date);
        System.out.println(value);
        System.out.println(((GregorianCalendar) value).getTime());

        String folderName = "Folder de proba - " + formatter.format(date);
        FolderInfo folderInfo = new FolderInfo("/test/Domino", folderName, "cmis:folder~mask~Document de intrare", documentMetadata);
        FolderIdentifier folderIdentifier = ss.createFolder(sc, folderInfo, true);
        System.out.println("----------------------------------");
//        folderProperties.put("Cod SMIS", "----");
//        folderProperties.put("ID Beneficiar", null);
//        folderProperties.put("Titlu Proiect", "Cel mai tare proiect");
//        folderInfo = new FolderInfo(folderProperties);
//        System.out.println("----------------------------------");
//        System.out.println("updateFolderProperties");
//        FolderIdentifier folderIdentifier1 = ss.updateFolderProperties(sc, folderIdentifier, folderInfo);
//        System.out.println("----------------------------------");
//        System.out.println("renameFolder");
//        FolderIdentifier folderIdentifier2 = ss.renameFolder(sc, folderIdentifier, "new name");
//        System.out.println("----------------------------------");


    }

    public static void mainddd(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {

        //String oldName = "sdf jncas*&qwed_-S";
        //String newName = oldName.replaceAll("[^a-zA-Z0-9\\-]", "_");
        InputStream is = new ByteArrayInputStream(Charset.forName("UTF-16").encode("gigel are mere").array());
        StoreServiceFactory ssf = null;
        ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-ciprian.properties");
        //ssf = new StoreServiceFactory("C:\\__JAVA\\jmeter\\test\\dmsutils\\ss-fo.properties");

        StoreService ss = ssf.getService();
        StoreContext sc = StoreContext.builder().build();
        Map<String, Object> documentProperties = new HashMap<String, Object>();
        //documentProperties.put("documentKey", "23442352354");
        documentProperties.put("elo:property-0", "23442352354");
        documentProperties.put("documentType", "CR-ABCD");
        //documentProperties.put("elo:property-2", "123456789");
        documentProperties.put("2", null);

        Map<String, Object> documentProperties2 = new HashMap<String, Object>();
        documentProperties2.put("elo:property-0", "xxx");
        documentProperties2.put("elo:property-2", "aaa");

        DocumentInfo documentInfo = new DocumentInfo("/JMeter/test2/test3", "upload3.txt", "cmis:document~mask~Document elementar", documentProperties);
        //DocumentInfo documentInfo = new DocumentInfo("/JMeter/test2/test3", "upload3.txt", "Document elementar", documentProperties);

        System.out.println("----------------------------------");
        System.out.println("storeDocument");
        DocumentIdentifier documentIdentifier = ss.storeDocument(sc, documentInfo, is, true, VersioningType.MAJOR);
        System.out.println("----------------------------------");
        System.out.println("updateDocumentProperties");
        DocumentInfo documentInfo1 = new DocumentInfo(documentProperties2);
        DocumentIdentifier documentIdentifier1 = ss.updateDocumentProperties(sc, documentIdentifier, documentInfo1);
        System.out.println("----------------------------------");
        System.out.println("updateDocumentProperties");
        documentProperties2.remove("elo:property-0");
        documentProperties2.put("0", null);
        documentInfo1 = new DocumentInfo(documentProperties2);
        documentIdentifier1 = ss.updateDocumentProperties(sc, documentIdentifier, documentInfo1);
        System.out.println("----------------------------------");
        System.out.println("getDocumentInfo");
        documentInfo1 = ss.getDocumentInfo(sc, documentIdentifier);
        System.out.println("----------------------------------");
        System.out.println(documentInfo1.getProperties());


        System.out.println("----------------------------------");
        System.out.println("createFolder");
        Map<String, Object> folderProperties = new HashMap<String, Object>();
        folderProperties.put("ID Beneficiar", Integer.valueOf(100));
        folderProperties.put("Cod SMIS", "234523");
        FolderInfo folderInfo = new FolderInfo("/JMeter/test2/", "Folder de proba", "cmis:folder~mask~Proiect", folderProperties);
        FolderIdentifier folderIdentifier = ss.createFolder(sc, folderInfo, true);
        System.out.println("----------------------------------");
        folderProperties.put("Cod SMIS", "----");
        folderProperties.put("ID Beneficiar", null);
        folderProperties.put("Titlu Proiect", "Cel mai tare proiect");
        folderInfo = new FolderInfo(folderProperties);
        System.out.println("----------------------------------");
        System.out.println("updateFolderProperties");
        FolderIdentifier folderIdentifier1 = ss.updateFolderProperties(sc, folderIdentifier, folderInfo);
        System.out.println("----------------------------------");
        System.out.println("renameFolder");
        FolderIdentifier folderIdentifier2 = ss.renameFolder(sc, folderIdentifier, "new name");
        System.out.println("----------------------------------");


    }
}
