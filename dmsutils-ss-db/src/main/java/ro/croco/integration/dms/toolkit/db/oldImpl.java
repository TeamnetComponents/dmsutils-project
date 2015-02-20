//package ro.croco.integration.dms.toolkit;
//
//import ro.croco.integration.dms.commons.DeleteOnCloseInputStream;
//import ro.croco.integration.dms.commons.exceptions.ObjectNotFoundException;
//import ro.croco.integration.dms.commons.exceptions.StoreServiceException;
//import ro.croco.integration.dms.commons.exceptions.StoreServiceNotDefinedException;
//import ro.croco.integration.dms.toolkit.jdbc.StoreServiceSessionImpl_Jdbc;
//
//import java.io.*;
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.nio.charset.Charset;
//import java.sql.*;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
///**
//* Created by Lucian.Dragomir on 8/29/2014.
//*/
//public class StoreServiceImpl_Jdbc extends StoreServiceImpl_Abstract<StoreServiceSessionImpl_Jdbc> {
//    private static final String JDBC_SCHEMA = "jdbc.schema";
//    private static final String CONTENT_PERSISTENCE_TYPE = "content.persistence.type";
//    private static final String CONTENT_PERSISTENCE_INTERNAL = "INTERNAL";
//    private static final String CONTENT_PERSISTENCE_EXTERNAL = "EXTERNAL";
//    private static final String CONTENT_PERSISTENCE_EXTERNAL_PATH = "content.persistence.external.path";
//    private static final String CONTENT_PERSISTENCE_TEMPORARY_PATH = "content.persistence.temporary.path";
//    private static final String CONTENT_PERSISTENCE_MEMORY_THRESHOLD = "content.persistence.memory.threshold";
//    private static final String CONTENT_PERSISTENCE_MEMORY_BUFFER = "content.persistence.memory.buffer";
//    private static final String CONTENT_PERSISTENCE_CLEANUP_FREQUENCY = "content.persistence.cleanup.frequency";
//    private static final String CONTENT_PERSISTENCE_CLEANUP_IDLE = "content.persistence.cleanup.idle";
//    private static final String CONTENT_PERSISTENCE_CLEANUP_COUNT = "content.persistence.cleanup.count";
//
//
//    private static final Map<String, Class> TYPES_MAP = new HashMap();
//
//    //private Runnable cleanUp;
//
//    private Thread cleanUpThread;
//    private boolean loopCleanUp;
//    private long loopFrequency;
//
//    static {
//        int i = 0;
//        for (Class clazz : Arrays.asList(String.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Boolean.class, BigDecimal.class, BigInteger.class)) {
//            i = i + 1;
//            TYPES_MAP.put(clazz.getSimpleName().toUpperCase(), clazz);
//            //System.out.println("INSERT INTO DM_PROPERTY_TYPES (ID_PROPERTY_TYPE, NAME) \n" +
//            //"    VALUES(" + i + ", '" + clazz.getSimpleName().toUpperCase() + "'); ");
//        }
//    }
//
////    private final String SQL_PROPERTY_TYPES_INSERT = "INSERT INTO DM_PROPERTY_TYPES (ID_PROPERTY_TYPE, NAME) \n" +
////            "    VALUES(" + i + ", '" + clazz.getSimpleName().toUpperCase() + "')";
//
//
//    //DM_VERSION_PROPERTIES
//    private final String SQL_VERSION_PROPERTIES_WRITE_ALL =
//            "INSERT INTO ${jdbc.schema}.DM_VERSION_PROPERTIES(ID_VERSION, PROPERTY_NAME, ID_PROPERTY_TYPE, PROPERTY_VALUE) " + '\n' +
//                    "  VALUES(?, ?, (SELECT ID_PROPERTY_TYPE FROM ${jdbc.schema}.DM_PROPERTY_TYPES WHERE NAME = ?), ?)";
//
//    private final String SQL_VERSION_PROPERTIES_READ_ALL =
//            "SELECT VP.ID_VERSION, VP.PROPERTY_NAME, PT.NAME AS PROPERTY_TYPE_NAME, VP.PROPERTY_VALUE FROM ${jdbc.schema}.DM_VERSION_PROPERTIES VP " + '\n' +
//                    "  JOIN ${jdbc.schema}.DM_PROPERTY_TYPES PT ON PT.ID_PROPERTY_TYPE = VP.ID_PROPERTY_TYPE " + '\n' +
//                    "  WHERE VP.ID_VERSION = ?";
//
//    private final String SQL_VERSION_PROPERTIES_DELETE_ALL =
//            "DELETE FROM ${jdbc.schema}.DM_VERSION_PROPERTIES VP " + '\n' +
//                    "  WHERE VP.ID_VERSION = ?";
//
//    private final String SQL_VERSION_PROPERTIES_COPY_ALL =
//            "INSERT INTO ${jdbc.schema}.DM_VERSION_PROPERTIES(ID_VERSION, PROPERTY_NAME, ID_PROPERTY_TYPE, PROPERTY_VALUE) " + '\n' +
//                    "  SELECT ? AS ID_VERSION, VP.PROPERTY_NAME, VP.ID_PROPERTY_TYPE, VP.PROPERTY_VALUE FROM ${jdbc.schema}.DM_VERSION_PROPERTIES VP " + '\n' +
//                    "  WHERE VP.ID_VERSION = ?";
//
//    //DM_VERSION_STREAMS
//    private final String SQL_VERSION_STREAMS_WRITE_ONE =
//            "INSERT INTO ${jdbc.schema}.DM_VERSION_STREAMS (ID_VERSION, NAME, MIME_TYPE, TYPE, STREAM_CONTENT, STREAM_REFERENCE, STREAM_SIZE) " + '\n' +
//                    "  VALUES (?, ?, ?, ?, ?, ?, ?)";
//
//    private final String SQL_VERSION_STREAMS_READ_ONE =
//            "SELECT * FROM ${jdbc.schema}.DM_VERSION_STREAMS " + '\n' +
//                    "  WHERE ID_VERSION = ?";
//
//    private final String SQL_VERSION_STREAMS_DELETE_ONE =
//            "DELETE FROM ${jdbc.schema}.DM_VERSION_STREAMS " + '\n' +
//                    "  WHERE ID_VERSION = ?";
//
//    private final String SQL_VERSION_STREAMS_UPDATE_SIZE_ONE =
//            "UPDATE ${jdbc.schema}.DM_VERSION_STREAMS " + '\n' +
//                    "  SET STREAM_SIZE = NVL(?, LENGTH(STREAM_CONTENT))" + '\n' +
//                    "  WHERE ID_VERSION = ?";
//
//    //DM_ORPHAN_STREAMS
//    private final String SQL_ORPHAN_STREAMS_WRITE_ONE =
//            "INSERT INTO ${jdbc.schema}.DM_ORPHAN_STREAMS (STREAM_REFERENCE) " + '\n' +
//                    " VALUES (?)";
//
//    private final String SQL_ORPHAN_STREAMS_REMOVE_ONE =
//            "DELETE FROM ${jdbc.schema}.DM_ORPHAN_STREAMS " + '\n' +
//                    "WHERE ID_ORPHAN_STREAM = ?";
//
//    private final String SQL_ORPHAN_STREAMS_READ_MANY =
//            "SELECT * FROM ${jdbc.schema}.DM_ORPHAN_STREAMS " + '\n' +
//                    "WHERE RETRY_COUNT > 0 " + '\n' +
//                    "  AND RETRY_COUNT > 0 " + '\n' +
//                    "ORDER BY UPDATE_TIME " + '\n' +
//                    "FETCH FIRST ${content.persistence.cleanup.count} ROW ONLY " + '\n' +
//                    "SKIP LOCKED DATA" + '\n' +
//                    "";
//
//    public static class OrphanStreamIdentifier {
//        private long id;
//        private String filePathName;
//
//        public OrphanStreamIdentifier(long id, String filePathName) {
//            this.id = id;
//            this.filePathName = filePathName;
//        }
//
//        public long getId() {
//            return id;
//        }
//
//        public void setId(long id) {
//            this.id = id;
//        }
//
//        public String getFilePathName() {
//            return filePathName;
//        }
//
//        public void setFilePathName(String filePathName) {
//            this.filePathName = filePathName;
//        }
//    }
//
//    private class Evictor implements Runnable {
//        @Override
//        public void run() {
//            while (loopCleanUp && (loopFrequency > 0)) {
//                try {
//                    cleanUpExecution();
//                    Thread.sleep(loopFrequency);
//                } catch (Exception e) {
//                    //do nothing
//                }
//            }
//        }
//    }
//
//
//    public StoreServiceImpl_Jdbc() {
//        super();
//        this.loopCleanUp = true;
//    }
//
//    @Override
//    protected void finalize() throws Throwable {
//        try {
//            this.loopCleanUp = false;
//            this.cleanUpThread = null;
//        } catch (Throwable t) {
//        } finally {
//            super.finalize();
//        }
//    }
//
//    private void cleanUpExecution() {
//        System.out.println("CleanUp");
//    }
//
//
//    @Override
//    public void __init(Properties context) {
//        super.__init(context);
//        this.loopFrequency = Integer.parseInt(getContextProperty(CONTENT_PERSISTENCE_CLEANUP_IDLE));
//        this.cleanUpThread = new Thread(new Evictor());
//        this.cleanUpThread.start();
//    }
//
//    @Override
//    public StoreServiceSessionImpl_Jdbc openSession(StoreContext storeContext) {
//        StoreServiceSessionImpl_Jdbc storeSession = null;
//        storeSession = new StoreServiceSessionImpl_Jdbc(storeContext, this.getContext());
//        return storeSession;
//    }
//
//    @Override
//    protected void closeSession(StoreServiceSessionImpl_Jdbc storeSession) {
//        if (storeSession != null) {
//            storeSession.close();
//        }
//        storeSession = null;
//    }
//
//    @Override
//    protected ObjectInfo[] listFolderContent(StoreServiceSessionImpl_Jdbc storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier, boolean includeInfo, ObjectBaseType... objectBaseTypes) {
//        return new ObjectInfo[0];
//    }
//
//    @Override
//    protected FolderInfo getFolderInfo(StoreServiceSessionImpl_Jdbc storeSession, StoreContext storeContext, FolderIdentifier folderIdentifier) {
//        return null;
//    }
//
//
//    @Override
//    public ObjectIdentifier move(StoreContext storeContext, ObjectIdentifier objectIdentifier, StoreService storeServiceTo, StoreContext storeContextTo, StoreMetadata storeMetadata) {
//        return null;
//    }
//
//    @Override
//    public BooleanResponse existsDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
//        return null;
//    }
//
//    @Override
//    public DocumentIdentifier storeDocument(StoreContext storeContext, DocumentInfo documentInfo, InputStream inputStream, boolean allowCreatePath, VersioningType versioningType) {
//        return null;
//    }
//
//    @Override
//    public DocumentStream downloadDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
//        return null;
//    }
//
//    @Override
//    public RequestIdentifier updateDocumentProperties(StoreContext storeContext, DocumentIdentifier documentIdentifier, DocumentInfo documentInfo, VersioningType versioningType) {
//        return null;
//    }
//
//    @Override
//    public RequestIdentifier deleteDocument(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
//        return null;
//    }
//
//    @Override
//    public DocumentInfo getDocumentInfo(StoreContext storeContext, DocumentIdentifier documentIdentifier) {
//        return null;
//    }
//
//    @Override
//    public BooleanResponse existsFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
//        return null;
//    }
//
//    @Override
//    public FolderIdentifier createFolder(StoreContext storeContext, FolderInfo folderInfo, boolean createParentIfNotExists) {
//        return null;
//    }
//
//    @Override
//    public RequestIdentifier deleteFolder(StoreContext storeContext, FolderIdentifier folderIdentifier) {
//        return null;
//    }
//
//    @Override
//    public FolderInfo getFolderInfo(StoreContext storeContext, FolderIdentifier folderIdentifier) {
//        return null;
//    }
//
//
//    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, SQLException, IOException {
//        StoreServiceFactory ssf = new StoreServiceFactory("jdbc");
//        StoreServiceImpl_Jdbc ss = (StoreServiceImpl_Jdbc) ssf.getService();
//
//        try {
//            StoreContext sc = StoreContext.builder().build();
//            StoreServiceSessionImpl_Jdbc sss = ss.openSession(sc);
//
//            Connection connection = sss.getConnection();
//            Map<String, Object> properties = null;
//            properties = new HashMap<>();
//            properties.put("fileName", "gigi.txt");
//            properties.put("longValue", (long) 1);
//            properties.put("bigDecimalValue", BigDecimal.valueOf(1));
//
//            connection.setAutoCommit(false);
//
//            ss.deleteObjectProperties(sc, connection, (long) 2);
//            connection.commit();
//
//            ss.writeObjectProperties(sc, connection, (long) 2, properties);
//            connection.commit();
//
//            properties = ss.readObjectProperties(sc, connection, (long) 2);
//            connection.commit();
//
//            ss.deleteObjectProperties(sc, connection, (long) 3);
//            ss.copyObjectProperties(sc, connection, (long) 2, (long) 3);
//            connection.commit();
//
//            ss.deleteStream(sc, connection, 2);
//            connection.commit();
//
//            InputStream inputStream = new ByteArrayInputStream(Charset.forName("UTF-16").encode("Acesta este un document text.").array());
//            ss.writeStream(sc, connection, 2, "gigi.txt", "file/txt", inputStream);
//            connection.commit();
//
//            DocumentStream documentStream = ss.readStream(sc, connection, 2);
//
//            connection.close();
//            connection = null;
//            sss.close();
//        } catch (Exception e) {
//
//        } finally {
//            ss = null;
//        }
//
//    }
//
//
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//    //------------------------------------------------------------------------------------------------------------------
//
//    //------------------------------------------------------------------------------------------------------------------
//    private String getWithContext(String stringTemplate) {
//        String stringValue = stringTemplate;
//        for (String propertyName : this.getContext().keySet().toArray(new String[0])) {
//            if (stringValue.indexOf("$" + propertyName) >= 0) {
//                stringValue = stringValue.replace("$" + propertyName, getContextProperty(propertyName));
//            }
//            if (stringValue.indexOf("${" + propertyName + "}") >= 0) {
//                stringValue = stringValue.replace("${" + propertyName + "}", getContextProperty(propertyName));
//            }
//        }
//        return stringValue;
//    }
//
//    //--FILE & STREAM METHODS ------------------------------------------------------------------------------------------
//    private long writeToFile(InputStream inputStream, File outputFile) throws IOException {
//        long fileSize = -1;
//        OutputStream outputStream = null;
//        try {
//            int bufferLength = Integer.parseInt(getContextProperty(CONTENT_PERSISTENCE_MEMORY_BUFFER));
//            outputStream = new FileOutputStream(outputFile);
//            int read = 0;
//            byte[] bytes = new byte[bufferLength];
//            while ((read = inputStream.read(bytes)) != -1) {
//                fileSize += read;
//                outputStream.write(bytes, 0, read);
//            }
//        } catch (IOException e) {
//            throw e;
//        } finally {
//            if (outputStream != null) {
//                try {
//                    outputStream.flush();
//                } catch (IOException e) {
//                    //do nothing
//                }
//                try {
//                    outputStream.close();
//                } catch (IOException e) {
//                    //do nothing
//                }
//                outputStream = null;
//            }
//        }
//        return fileSize;
//    }
//
//
//    private InputStream readFromFile(String filePathName) throws FileNotFoundException {
//        return new FileInputStream(filePathName);
//    }
//
//    private void deleteFile(String filePathName) {
//        File file = new File(filePathName);
//        if (file.exists()) {
//            if (file.delete()) {
//                //ok. deleted
//            } else {
//                //not deleted throw error
//                throw new StoreServiceException("Unable to delete externally stored file.");
//            }
//        } else {
//            //file does not exists ... consider success
//            //throw new ObjectNotFoundException("The requested stream does not exist.");
//        }
//        file = null;
//    }
//
//    private InputStream wrapInputStream(InputStream streamContent, long streamLength) throws IOException {
//        InputStream returnStream = null;
//        if (streamLength < Long.parseLong(getContextProperty(CONTENT_PERSISTENCE_MEMORY_THRESHOLD))) {
//            //return stream bufferd in memory
//            ByteArrayOutputStream memoryStream = null;
//            memoryStream = new ByteArrayOutputStream();
//            try {
//                int bufferLength = Integer.parseInt(getContextProperty(CONTENT_PERSISTENCE_MEMORY_BUFFER));
//                memoryStream = new ByteArrayOutputStream();
//                int read = 0;
//                byte[] bytes = new byte[bufferLength];
//                while ((read = streamContent.read(bytes)) != -1) {
//                    memoryStream.write(bytes, 0, read);
//                }
//                returnStream = new ByteArrayInputStream(memoryStream.toByteArray());
//            } catch (IOException e) {
//                throw e;
//            } finally {
//                if (streamContent != null) {
//                    try {
//                        streamContent.close();
//                    } catch (IOException e) {
//                        //do nothing
//                    }
//                }
//                if (memoryStream != null) {
//                    try {
//                        memoryStream.close();
//                    } catch (IOException e) {
//                    }
//                    memoryStream = null;
//                }
//            }
//        } else {
//            //return stream buffered in a file
//            File tempFile = File.createTempFile(String.valueOf(UUID.randomUUID()), ".tmp");
//
//            writeToFile(streamContent, tempFile);
//            returnStream = new DeleteOnCloseInputStream(tempFile);
//        }
//        return returnStream;
//    }
//
//    //--ORPHAN STREAM---------------------------------------------------------------------------------------------------
//    private long writeOrphanStream(StoreContext sc, Connection connection, String externalFilePathName) throws SQLException {
//        long idOrphanStream = -1;
//        PreparedStatement statement = null;
//        ResultSet resultSet = null;
//        try {
//            String sqlString = getWithContext(SQL_ORPHAN_STREAMS_WRITE_ONE);
//            statement = connection.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
//            statement.setString(1, externalFilePathName);
//            statement.execute();
//            //get the generated key
//            resultSet = statement.getGeneratedKeys();
//            if (resultSet.next()) {
//                idOrphanStream = resultSet.getLong(1);
//            } else {
//                throw new StoreServiceException("The generated key could not be retrieved from database.");
//            }
//
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (resultSet != null) {
//                if (!resultSet.isClosed()) {
//                    resultSet.close();
//                }
//                resultSet = null;
//            }
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//                statement = null;
//            }
//
//        }
//        return idOrphanStream;
//    }
//
//    //add to orphans table on an independent connection (and of course transaction)
//    private long writeOrphanStream(StoreContext storeContext, String externalFilePathName) throws SQLException {
//        long idOrphanStream = -1;
//        StoreServiceSessionImpl_Jdbc storeSession = null;
//        Connection connection = null;
//        try {
//            storeSession = openSession(storeContext);
//            connection = storeSession.getConnection();
//            idOrphanStream = writeOrphanStream(storeContext, connection, externalFilePathName);
//            connection.commit();
//            connection.close();
//            connection = null;
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            if (connection != null) {
//                if (!connection.isClosed()) {
//                    connection.close();
//                }
//                connection = null;
//            }
//        }
//        return idOrphanStream;
//    }
//
//    private void removeOrphanStream(StoreContext sc, Connection connection, long idOrphanStream) throws SQLException {
//        PreparedStatement statement = null;
//        try {
//            String sqlString = getWithContext(SQL_ORPHAN_STREAMS_REMOVE_ONE);
//            statement = connection.prepareStatement(sqlString);
//            statement.setLong(1, idOrphanStream);
//            statement.execute();
//
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//                statement = null;
//            }
//        }
//    }
//
//    //delete orphans table on an independent connection (and of course transaction)
//    private void removeOrphanStream(StoreContext storeContext, long idOrphanStream) throws SQLException {
//        StoreServiceSessionImpl_Jdbc storeSession = null;
//        Connection connection = null;
//        try {
//            storeSession = openSession(storeContext);
//            connection = storeSession.getConnection();
//            removeOrphanStream(storeContext, connection, idOrphanStream);
//            connection.commit();
//            connection.close();
//            connection = null;
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            if (connection != null) {
//                if (!connection.isClosed()) {
//                    connection.close();
//                }
//                connection = null;
//            }
//        }
//    }
//
//    //--STREAM METHODS--------------------------------------------------------------------------------------------------
//    public DocumentStream readStream(StoreContext storeContext, Connection connection, long versionId) throws SQLException, IOException {
//        DocumentStream documentStream = null;
//        PreparedStatement statement = null;
//        ResultSet resultSet = null;
//        String sqlString = null;
//        String fileName = null;
//        String streamType = null;
//        long streamLength = 0;
//        try {
//            //get stream information
//            sqlString = getWithContext(SQL_VERSION_STREAMS_READ_ONE);
//            statement = connection.prepareStatement(sqlString);
//            statement.setLong(1, versionId);
//            resultSet = statement.executeQuery();
//            if (resultSet.next()) {
//                documentStream = new DocumentStream();
//                documentStream.setRequestId(storeContext.getRequestIdentifier());
//                documentStream.setFileName(resultSet.getString("NAME"));
//                documentStream.setMimeType(resultSet.getString("MIME_TYPE"));
//                streamType = resultSet.getString("TYPE");
//                //get stream info based on type(INTERNAL OR EXTERNAL)
//                if (CONTENT_PERSISTENCE_INTERNAL.toUpperCase().equals(streamType.toUpperCase())) {
//                    InputStream streamContent = wrapInputStream(resultSet.getBinaryStream("STREAM_CONTENT"), resultSet.getLong("STREAM_SIZE"));
//                } else if (CONTENT_PERSISTENCE_EXTERNAL.toUpperCase().equals(streamType.toUpperCase())) {
//                    //delete from the external persistence
//                    String rootFolder = getContextProperty(CONTENT_PERSISTENCE_EXTERNAL_PATH);
//                    String streamReference = resultSet.getString("STREAM_REFERENCE");
//                    String externalFilePathName = fileUtils.concatenate(fileUtils.concatenate(rootFolder, fileUtils.getParentFolderPathName(streamReference)), fileUtils.getFileName(streamReference));
//                    documentStream.setInputStream(new FileInputStream(new File(externalFilePathName)));
//                } else {
//                    throw new StoreServiceException("Incorrect content persistence type.");
//                }
//            } else {
//                throw new ObjectNotFoundException("Incorrect content persistence type.");
//            }
//            resultSet.close();
//            resultSet = null;
//            statement.close();
//            statement = null;
//        } catch (SQLException e) {
//            throw e;
//        } catch (IOException e) {
//            throw e;
//        } finally {
//            if (resultSet != null) {
//                if (resultSet.isClosed()) {
//                    try {
//                        resultSet.close();
//                    } catch (Exception e) {
//                        //do nothing
//                    }
//                }
//            }
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//                statement = null;
//            }
//        }
//        return documentStream;
//    }
//
//
//    public OrphanStreamIdentifier deleteStream(StoreContext storeContext, Connection connection, long versionId) throws SQLException {
//        OrphanStreamIdentifier orphanStreamIdentifier = null;
//        long idOrphanStream = -1;
//        PreparedStatement statement = null;
//        ResultSet resultSet = null;
//        String sqlString = null;
//        String streamType = null;
//        String streamReference = null;
//        String rootFolder = null;
//        String externalFilePathName = null;
//        File externalFileName = null;
//        try {
//            //get stream information
//            sqlString = getWithContext(SQL_VERSION_STREAMS_READ_ONE);
//            statement = connection.prepareStatement(sqlString);
//            statement.setLong(1, versionId);
//            resultSet = statement.executeQuery();
//            if (resultSet.next()) {
//                streamType = resultSet.getString("TYPE");
//                streamReference = resultSet.getString("STREAM_REFERENCE");
//            } else {
//                throw new ObjectNotFoundException("The requested stream does not exist.");
//            }
//            resultSet.close();
//            resultSet = null;
//            statement.close();
//            statement = null;
//
//            //add to orphans table on an independent connection (and of course transaction)
//            if (CONTENT_PERSISTENCE_EXTERNAL.toUpperCase().equals(streamType.toUpperCase())) {
//                rootFolder = getContextProperty(CONTENT_PERSISTENCE_EXTERNAL_PATH);
//                externalFilePathName = fileUtils.concatenate(fileUtils.concatenate(rootFolder, fileUtils.getParentFolderPathName(streamReference)), fileUtils.getFileName(streamReference));
//                idOrphanStream = writeOrphanStream(storeContext, externalFilePathName);
//            }
//
//            //delete stream record from the table
//            sqlString = getWithContext(SQL_VERSION_STREAMS_DELETE_ONE);
//            statement = connection.prepareStatement(sqlString);
//            statement.setLong(1, versionId);
//            statement.execute();
//            statement.close();
//            statement = null;
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (resultSet != null) {
//                if (!resultSet.isClosed()) {
//                    try {
//                        resultSet.close();
//                    } catch (Exception e) {
//                        //do nothing
//                    }
//                }
//            }
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//                statement = null;
//            }
//        }
//        if (idOrphanStream != -1) {
//            orphanStreamIdentifier = new OrphanStreamIdentifier(idOrphanStream, externalFilePathName);
//        }
//        return orphanStreamIdentifier;
//    }
//
//    public OrphanStreamIdentifier writeStream(StoreContext storeContext, Connection connection, long versionId, String name, String mimeType, InputStream streamContent) throws SQLException, IOException {
//        OrphanStreamIdentifier orphanStreamIdentifier = null;
//        long idOrphanStream = -1;
//        PreparedStatement statement = null;
//        String streamType = getContextProperty(CONTENT_PERSISTENCE_TYPE);
//        String externalFilePathName = null;
//        InputStream streamDB = null;
//        String streamReference = null;
//        String sqlString = null;
//        long streamLength = -1;
//        if (CONTENT_PERSISTENCE_INTERNAL.toUpperCase().equals(streamType.toUpperCase())) {
//            //save content in database
//            streamDB = streamContent;
//            streamReference = null;
//            streamLength = -1;
//        } else if (CONTENT_PERSISTENCE_EXTERNAL.toUpperCase().equals(streamType.toUpperCase())) {
//            //save content on the shared filesystem
//            streamDB = null;
//            try {
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                String dateFolder = dateFormat.format(Calendar.getInstance().getTime());
//                String rootFolder = getContextProperty(CONTENT_PERSISTENCE_EXTERNAL_PATH);
//                streamReference = fileUtils.concatenate(dateFolder, fileUtils.getFileNameWithExtension(String.valueOf(UUID.randomUUID()), fileUtils.getFileExtension(name)));
//                externalFilePathName = fileUtils.concatenate(fileUtils.concatenate(rootFolder, fileUtils.getParentFolderPathName(streamReference)), fileUtils.getFileName(streamReference));
//
//                //add to orphans table on an independent connection (and of course transaction)
//                idOrphanStream = writeOrphanStream(storeContext, externalFilePathName);
//
//                //write file to filesystem
//                try {
//                    String parentFile = fileUtils.getParentFolderPathName(externalFilePathName);
//                    if (!new File(parentFile).exists()) {
//                        new File(parentFile).mkdirs();
//                    }
//                    File file = new File(externalFilePathName);
//                    streamLength = writeToFile(streamContent, file);
//                } catch (IOException e) {
//                    throw e;
//                } finally {
//                    if (streamContent != null) {
//                        try {
//                            streamContent.close();
//                        } catch (IOException e) {
//                            //do nothing
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                throw e;
//            } finally {
//            }
//        } else {
//            throw new StoreServiceException("Incorrect content persistence type.");
//        }
//
//
//        //insert stream record in the database
//        try {
//            //insert the stream record
//            sqlString = getWithContext(SQL_VERSION_STREAMS_WRITE_ONE);
//            statement = connection.prepareStatement(sqlString /*, Statement.RETURN_GENERATED_KEYS*/);
//            statement.setLong(1, versionId);
//            statement.setString(2, name);
//            statement.setString(3, mimeType);
//            statement.setString(4, streamType);
//            statement.setBinaryStream(5, streamDB);
//            statement.setString(6, streamReference);
//            if (streamLength != -1) {
//                statement.setLong(7, streamLength);
//            } else {
//                statement.setNull(7, Types.BIGINT);
//            }
//            statement.executeUpdate();
//            statement.close();
//
//
//            //set the stream length
//            if (streamLength == -1) {
//                sqlString = getWithContext(SQL_VERSION_STREAMS_UPDATE_SIZE_ONE);
//                statement = connection.prepareStatement(sqlString);
//                if (streamLength != -1) {
//                    statement.setLong(1, streamLength);
//                } else {
//                    statement.setNull(1, Types.BIGINT);
//                }
//                statement.setLong(2, versionId);
//                statement.executeUpdate();
//                statement.close();
//            }
//
//            if (CONTENT_PERSISTENCE_EXTERNAL.toUpperCase().equals(streamType.toUpperCase())) {
//                //remove from orphans table
//                removeOrphanStream(storeContext, connection, idOrphanStream);
//            }
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//            }
//            statement = null;
//        }
//        if (idOrphanStream != -1) {
//            orphanStreamIdentifier = new OrphanStreamIdentifier(idOrphanStream, externalFilePathName);
//        }
//        return orphanStreamIdentifier;
//    }
//
//
//    //OBJECT PROPERTIES METHODS-----------------------------------------------------------------------------------------
//
//    public void deleteObjectProperties(StoreContext storeContext, Connection connection, Long versionId) throws SQLException {
//        PreparedStatement statement = null;
//        try {
//            String sqlString = getWithContext(SQL_VERSION_PROPERTIES_DELETE_ALL);
//            statement = connection.prepareStatement(sqlString);
//            statement.setLong(1, versionId);
//            statement.execute();
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//            }
//            statement = null;
//        }
//    }
//
//    public void writeObjectProperties(StoreContext storeContext, Connection connection, Long versionId, Map<String, Object> properties) throws SQLException {
//        if (properties != null) {
//            PreparedStatement statement = null;
//            try {
//                String sqlString = getWithContext(SQL_VERSION_PROPERTIES_WRITE_ALL);
//                statement = connection.prepareStatement(sqlString);
//                for (String propertyName : properties.keySet()) {
//                    Object propertyValue = properties.get(propertyName);
//                    String propertyType = "STRING";
//                    if (propertyValue != null) {
//                        propertyType = propertyValue.getClass().getCanonicalName();
//                        if (propertyType.indexOf("java.lang.") >= 0) {
//                            propertyType = propertyType.replace("java.lang.", "").toUpperCase();
//                        } else if (propertyType.indexOf("java.math.") >= 0) {
//                            propertyType = propertyType.replace("java.math.", "").toUpperCase();
//                        } else {
//                            propertyType = null;
//                        }
//                    }
//                    statement.setLong(1, versionId);
//                    statement.setString(2, propertyName);
//                    statement.setString(3, propertyType);
//                    statement.setObject(4, propertyValue);
//                    statement.execute();
//                    statement.clearParameters();
//                }
//            } catch (SQLException e) {
//                throw e;
//            } finally {
//                if (statement != null) {
//                    if (!statement.isClosed()) {
//                        statement.close();
//                    }
//                }
//                statement = null;
//            }
//        }
//    }
//
//    public void copyObjectProperties(StoreContext storeContext, Connection connection, Long versionIdOld, Long versionIdNew) throws SQLException {
//        PreparedStatement statement = null;
//        try {
//            String sqlString = getWithContext(SQL_VERSION_PROPERTIES_COPY_ALL);
//            statement = connection.prepareStatement(sqlString);
//            statement.setLong(1, versionIdNew);
//            statement.setLong(2, versionIdOld);
//            statement.execute();
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//            }
//            statement = null;
//        }
//    }
//
//    private Object getStringValueAsObject(Class<?> clazz, String value) {
//        if (value == null) {
//            return null;
//        } else if (clazz.equals(String.class)) {
//            return value;
//        } else if (clazz.equals(Byte.class)) {
//            return Byte.valueOf(value);
//        } else if (clazz.equals(Short.class)) {
//            return Short.valueOf(value);
//        } else if (clazz.equals(Integer.class)) {
//            return Integer.valueOf(value);
//        } else if (clazz.equals(Long.class)) {
//            return Long.valueOf(value);
//        } else if (clazz.equals(Float.class)) {
//            return Float.valueOf(value);
//        } else if (clazz.equals(Double.class)) {
//            return Double.valueOf(value);
//        } else if (clazz.equals(Character.class)) {
//            return (value != null) ? new Character(value.charAt(0)) : null;
//        } else if (clazz.equals(Boolean.class)) {
//            return Boolean.valueOf(value);
//        } else if (clazz.equals(BigDecimal.class)) {
//            return BigDecimal.valueOf(Long.parseLong(value));
//        } else if (clazz.equals(BigInteger.class)) {
//            return BigInteger.valueOf(Long.parseLong(value));
//        }
//        throw new StoreServiceNotDefinedException("No cast is defined for type " + clazz + ".");
//    }
//
//    public Map<String, Object> readObjectProperties(StoreContext storeContext, Connection connection, Long versionId) throws SQLException {
//        Map<String, Object> properties = new HashMap<>();
//        PreparedStatement statement = null;
//        ResultSet resultSet = null;
//        try {
//            String sqlString = getWithContext(SQL_VERSION_PROPERTIES_READ_ALL);
//            statement = connection.prepareStatement(sqlString);
//            statement.setLong(1, versionId);
//            resultSet = statement.executeQuery();
//            String propertyName = null;
//            Class<?> propertyClass = null;
//            Object propertyValue = null;
//            while (resultSet.next()) {
//                propertyName = resultSet.getString("PROPERTY_NAME");
//                propertyClass = TYPES_MAP.get(resultSet.getString("PROPERTY_TYPE_NAME"));
//                propertyValue = getStringValueAsObject(propertyClass, resultSet.getString("PROPERTY_VALUE"));
//                properties.put(propertyName, propertyValue);
//            }
//        } catch (SQLException e) {
//            throw e;
//        } finally {
//            if (resultSet != null) {
//                try {
//                    resultSet.close();
//                } catch (Exception e) {
//                    //do nothing
//                }
//            }
//            if (statement != null) {
//                if (!statement.isClosed()) {
//                    statement.close();
//                }
//            }
//            statement = null;
//        }
//        return properties;
//    }
//
//}
