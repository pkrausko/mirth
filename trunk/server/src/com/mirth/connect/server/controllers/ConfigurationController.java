/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.crypto.SecretKey;

import com.mirth.connect.model.DriverInfo;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.ServerConfiguration;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 */
public abstract class ConfigurationController extends Controller {
    // status codes

    public static final int STATUS_OK = 0;
    public static final int STATUS_UNAVAILABLE = 1;
    public static final int STATUS_ENGINE_STARTING = 2;

    public static ConfigurationController getInstance() {
        return ControllerFactory.getFactory().createConfigurationController();
    }

    /**
     * Checks for an existing certificate to user for secure communication
     * between the server and client. If no certficate exists, this will
     * generate a new one.
     * 
     */
    public abstract void generateKeyPair();

    /**
     * Loads the encryption key from the database and stores it in memory.
     * 
     */
    public abstract void loadEncryptionKey();

    /**
     * Returns the database type (ex. derby)
     * 
     * @return the database type
     */
    public abstract String getDatabaseType();

    /**
     * Returns the server's unique ID
     * 
     * @return the server's unique ID
     */
    public abstract String getServerId();

    public abstract String getServerTimezone(Locale locale);

    /**
     * Returns all of the charset encodings available on the server.
     * 
     * @return a list of charset encoding names
     * @throws ControllerException
     */
    public abstract List<String> getAvaiableCharsetEncodings() throws ControllerException;

    /**
     * Returns the encryption key.
     * 
     * @return a <code>SecretKey</code> object
     */
    public abstract SecretKey getEncryptionKey();

    /**
     * Returns the base directory for the server.
     * 
     * @return the base directory for the server.
     */
    public abstract String getBaseDir();

    /**
     * Returns the conf directory for the server. This is where configuration
     * files and database mapping scripts are stored.
     * 
     * @return the conf directory for the server.
     */
    public abstract String getConfigurationDir();

    /**
     * Returns the app data directory for the server. This is where files
     * generated by the server are stored.
     * 
     * @return the app data directory for the server.
     */
    public abstract String getApplicationDataDir();

    /**
     * Returns all properties from the "core" group.
     * 
     * @return all server properties
     * @throws ControllerException
     */
    public abstract Properties getServerProperties() throws ControllerException;

    /**
     * Sets all properties in the "core" group.
     * 
     * @param properties the properties to set
     * @throws ControllerException
     */
    public abstract void setServerProperties(Properties properties) throws ControllerException;

    /**
     * Generates a new GUID.
     * 
     * @return a new GUID
     */
    public abstract String generateGuid();

    /**
     * A list of database driver metadata specified in the dbdrivers.xml file.
     * 
     * @return a list of database driver metadata
     * @throws ControllerException if the list could not be retrieved or parsed
     */
    public abstract List<DriverInfo> getDatabaseDrivers() throws ControllerException;

    /**
     * Returns the server version (ex. 1.8.2).
     * 
     * @return the server version
     */
    public abstract String getServerVersion();

    /**
     * Returns the current database schema version.
     * 
     * @return the current database schema version.
     */
    public abstract int getSchemaVersion();

    /**
     * Returns the server build date.
     * 
     * @return the server build date.
     */
    public abstract String getBuildDate();

    /**
     * Returns the server configuration, which contains:
     * <ul>
     * <li>Channels</li>
     * <li>Users</li>
     * <li>Alerts</li>
     * <li>Code templates</li>
     * <li>Server properties</li>
     * <li>Scripts</li>
     * </ul>
     *  
     * @return the server configuration
     * @throws ControllerException
     */
    public abstract ServerConfiguration getServerConfiguration() throws ControllerException;

    /**
     * Restores the server configuration.
     * 
     * @param serverConfiguration the server configuration to restore
     * @throws ControllerException if the server configuration could not be restored
     */
    public abstract void setServerConfiguration(ServerConfiguration serverConfiguration) throws ControllerException;

    /**
     * Returns the password requirements specified in the mirth.properties file (ex. min length).
     * 
     * @return the password requriements
     */
    public abstract PasswordRequirements getPasswordRequirements();

    // status

    /**
     * Returns the current status of the server.
     * 
     * @return 0 if the server is running, 1 if it is unavailable, and 2 if it is currently starting.
     */
    public abstract int getStatus();

    /**
     * Returns if the engine is starting.
     * 
     * @return <code>true</code> if the engine is starting, <code>false</code> otherwise
     */
    public abstract boolean isEngineStarting();

    /**
     * Sets the engine starting status.
     * 
     * @param isEngineStarting the starting status of the engine
     */
    public abstract void setEngineStarting(boolean isEngineStarting);

    // properties

    public abstract Properties getPropertiesForGroup(String group);

    public abstract String getProperty(String group, String name);

    public abstract void saveProperty(String group, String name, String property);

    public abstract void removeProperty(String group, String name);
}
