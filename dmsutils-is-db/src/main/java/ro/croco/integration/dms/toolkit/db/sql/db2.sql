-- DROP TABLE DM_INTREGRATION_01;
CREATE TABLE DM_INTREGRATION_01
(
    ID BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 2, INCREMENT BY 1, CACHE 20, NO MINVALUE, NO MAXVALUE, NO CYCLE, NO ORDER),
    MSG_ID VARCHAR(50) NOT NULL,
    MSG_CORRELATION_ID VARCHAR(50),
    MSG_DESTINATION VARCHAR(50) NOT NULL,
    MSG_REPLY_TO VARCHAR(50),
    MSG_EXPIRATION BIGINT BIGINT DEFAULT 0,
    MSG_PRIORITY INTEGER NOT NULL CHECK (MSG_PRIORITY >= 0 AND MSG_PRIORITY <= 9),
    MSG_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MSG_CONTENT VARCHAR(4000),
    PRC_STATUS VARCHAR(50),
    PRC_ID VARCHAR(50),
    PRC_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (ID)
);
-- DROP TABLE DM_INTREGRATION_01_HIS;
CREATE TABLE DM_INTREGRATION_01_HIS
(
    ID BIGINT NOT NULL,
    MSG_ID VARCHAR(50) NOT NULL,
    MSG_CORRELATION_ID VARCHAR(50),
    MSG_DESTINATION VARCHAR(50) NOT NULL,
    MSG_REPLY_TO VARCHAR(50),
    MSG_EXPIRATION BIGINT BIGINT DEFAULT 0,
    MSG_PRIORITY INTEGER NOT NULL CHECK (MSG_PRIORITY >= 0 AND MSG_PRIORITY <= 9),
    MSG_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MSG_CONTENT VARCHAR(4000),
    PRC_STATUS VARCHAR(50),
    PRC_ID VARCHAR(50),
    PRC_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (ID)
);
