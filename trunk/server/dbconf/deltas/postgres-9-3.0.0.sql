DROP TABLE TEMPLATE

DROP SEQUENCE MESSAGE_SEQUENCE CASCADE

ALTER TABLE CHANNEL_STATISTICS RENAME TO OLD_CHANNEL_STATISTICS

ALTER TABLE ATTACHMENT RENAME TO OLD_ATTACHMENT

ALTER TABLE MESSAGE RENAME TO OLD_MESSAGE

ALTER TABLE ALERT RENAME TO OLD_ALERT

ALTER TABLE CHANNEL_ALERT RENAME TO OLD_CHANNEL_ALERT

ALTER TABLE ALERT_EMAIL RENAME TO OLD_ALERT_EMAIL

ALTER TABLE CHANNEL RENAME TO OLD_CHANNEL

ALTER TABLE CODE_TEMPLATE RENAME TO OLD_CODE_TEMPLATE

CREATE TABLE CHANNEL (
	ID CHAR(36) NOT NULL PRIMARY KEY,
	NAME VARCHAR(40) NOT NULL,
	REVISION INTEGER,
	CHANNEL TEXT
)

CREATE TABLE ALERT (
	ID CHAR(36) NOT NULL PRIMARY KEY,
	NAME VARCHAR(255) NOT NULL UNIQUE,
	ALERT TEXT NOT NULL
)

CREATE TABLE CODE_TEMPLATE (
	ID VARCHAR(255) NOT NULL PRIMARY KEY,
	CODE_TEMPLATE TEXT
)

UPDATE SCRIPT SET GROUP_ID = 'Global' WHERE GROUP_ID = 'GLOBAL'

DELETE FROM CONFIGURATION WHERE CATEGORY = 'core' AND NAME = 'server.maxqueuesize'

INSERT INTO CONFIGURATION (CATEGORY, NAME, VALUE) VALUES ('core', 'server.queuebuffersize', '1000')