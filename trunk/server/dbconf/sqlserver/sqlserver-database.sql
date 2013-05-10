CREATE TABLE SCHEMA_INFO (VERSION INTEGER);

CREATE TABLE EVENT (
	ID INTEGER IDENTITY (1, 1) NOT NULL PRIMARY KEY,
	DATE_CREATED DATETIME DEFAULT GETDATE(),
	NAME NVARCHAR(MAX) NOT NULL,
	EVENT_LEVEL NVARCHAR(40) NOT NULL,
	OUTCOME NVARCHAR(40) NOT NULL,
	ATTRIBUTES NVARCHAR(MAX),
	USER_ID INTEGER NOT NULL,
	IP_ADDRESS NVARCHAR(40)
);

CREATE TABLE CHANNEL (
	ID NVARCHAR(36) NOT NULL PRIMARY KEY,
	NAME NVARCHAR(40) NOT NULL,
	NEXT_METADATA_ID INTEGER NOT NULL,
	DESCRIPTION NVARCHAR(MAX),
	IS_ENABLED BIT,
	VERSION NVARCHAR(40),
	REVISION INTEGER,
	LAST_MODIFIED DATETIME DEFAULT GETDATE(),
	SOURCE_CONNECTOR NVARCHAR(MAX),
	DESTINATION_CONNECTORS NVARCHAR(MAX),
	PROPERTIES NVARCHAR(MAX),
	PREPROCESSING_SCRIPT NVARCHAR(MAX),
	POSTPROCESSING_SCRIPT NVARCHAR(MAX),
	DEPLOY_SCRIPT NVARCHAR(MAX),
	SHUTDOWN_SCRIPT NVARCHAR(MAX)
);

CREATE TABLE SCRIPT (
	GROUP_ID NVARCHAR(40) NOT NULL,
	ID NVARCHAR(40) NOT NULL,
	SCRIPT NVARCHAR(MAX),
	PRIMARY KEY(GROUP_ID, ID)
);

CREATE TABLE PERSON (
	ID INTEGER IDENTITY (1, 1) NOT NULL PRIMARY KEY,
	USERNAME NVARCHAR(40) NOT NULL,
	FIRSTNAME NVARCHAR(40),
	LASTNAME NVARCHAR(40),
	ORGANIZATION NVARCHAR(255),
	EMAIL NVARCHAR(255),
	PHONENUMBER NVARCHAR(40),
	DESCRIPTION NVARCHAR(255),
	LAST_LOGIN DATETIME DEFAULT GETDATE(),
	GRACE_PERIOD_START DATETIME DEFAULT NULL,
	LOGGED_IN BIT NOT NULL
);

CREATE TABLE PERSON_PASSWORD (
	PERSON_ID INTEGER NOT NULL,
	PASSWORD NVARCHAR(256) NOT NULL,
	PASSWORD_DATE DATETIME DEFAULT GETDATE()
);

ALTER TABLE PERSON_PASSWORD ADD CONSTRAINT PERSON_ID_PP_FK FOREIGN KEY (PERSON_ID) REFERENCES PERSON (ID) ON DELETE CASCADE;

CREATE TABLE ALERT (
	ID NVARCHAR(36) NOT NULL PRIMARY KEY,
	NAME NVARCHAR(256) NOT NULL UNIQUE,
	ALERT NVARCHAR(MAX) NOT NULL
);

CREATE TABLE CODE_TEMPLATE (
	ID NVARCHAR(255) NOT NULL PRIMARY KEY,
	NAME NVARCHAR(40) NOT NULL,
	CODE_SCOPE NVARCHAR(40) NOT NULL,
	CODE_TYPE NVARCHAR(40) NOT NULL,
	TOOLTIP NVARCHAR(255),
	CODE NVARCHAR(MAX)
);
	
CREATE TABLE CONFIGURATION (
	CATEGORY NVARCHAR(255) NOT NULL,
	NAME NVARCHAR(255) NOT NULL,
	VALUE NVARCHAR(MAX)
);

INSERT INTO PERSON (USERNAME, LOGGED_IN) VALUES('admin', 0);

INSERT INTO PERSON_PASSWORD (PERSON_ID, PASSWORD) VALUES(1, 'YzKZIAnbQ5m+3llggrZvNtf5fg69yX7pAplfYg0Dngn/fESH93OktQ==');

INSERT INTO SCHEMA_INFO (VERSION) VALUES (10);

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'update.url', 'http://updates.mirthcorp.com');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'update.enabled', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'stats.enabled', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'firstlogin', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'server.resetglobalvariables', '1');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'smtp.timeout', '5000');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'smtp.auth', '0');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'smtp.secure', '0');

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'server.maxqueuesize', '0');
