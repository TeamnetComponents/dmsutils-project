

CREATE TABLE DM_OBJECTS
(
	OBJECT_ID Int AUTONUMBER,
	//BASE_TYPE INT //always document
	OBJ_PATH VARCHAR(3000) NOT NULL,
    OBJ_NAME VARCHAR(255) NOT NULL,
)

CREATE TABLE DM_OBJECT_VERSIONS
(
	VERSION_ID in autonumnber,
	OBJECT_ID Int,   //FK to DM_OBJECTS
	STREAM_ID //FK to DM_STREAMS
	VERSION_LABEL varchar()      1.0, 1.1, 1.2, NULL
	 file_name with extension
)

//file table
CREATE TABLE DM_STREAMS(
	STREAM_ID UUID,
	STREAM_NAME NAME,   // DATE (YYYYMMDD)/UUID

)
