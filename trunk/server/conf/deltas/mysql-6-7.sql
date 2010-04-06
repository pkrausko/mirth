DROP TABLE CONFIGURATION;

CREATE TABLE CONFIGURATION
	(CATEGORY VARCHAR(255) NOT NULL,
	NAME VARCHAR(255) NOT NULL,
	VALUE LONGTEXT NOT NULL) ENGINE=InnoDB;
	
DROP TABLE PREFERENCES;

ALTER TABLE SCRIPT ADD COLUMN GROUP_ID VARCHAR(255);

UPDATE SCRIPT SET GROUP_ID = 'GLOBAL' WHERE ID = 'Deploy' OR ID = 'Shutdown' OR ID = 'Preprocessor' OR ID = 'Postprocessor';

ALTER TABLE SCRIPT DROP PRIMARY KEY;

ALTER TABLE SCRIPT ADD CONSTRAINT PRIMARY KEY (GROUP_ID, ID);

DELETE FROM TEMPLATE;

ALTER TABLE TEMPLATE ADD COLUMN GROUP_ID VARCHAR(255) NOT NULL;

ALTER TABLE TEMPLATE ADD CONSTRAINT PRIMARY KEY (GROUP_ID, ID);