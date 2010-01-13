/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Credentials;
import com.webreach.mirth.model.PasswordRequirements;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.util.PasswordRequirementsChecker;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.util.EncryptionException;
import com.webreach.mirth.util.FIPSEncrypter;

public class DefaultUserController extends UserController {
    private Logger logger = Logger.getLogger(this.getClass());
    private FIPSEncrypter encrypter = FIPSEncrypter.getInstance();

    private static DefaultUserController instance = null;

    private DefaultUserController() {

    }

    public static UserController create() {
        synchronized (DefaultUserController.class) {
            if (instance == null) {
                instance = new DefaultUserController();
            }

            return instance;
        }
    }

    public void resetUserStatus() {
        try {
            SqlConfig.getSqlMapClient().update("User.resetUserStatus");
        } catch (SQLException e) {
            logger.error("Could not reset user status.");
        }
    }

    public List<User> getUser(User user) throws ControllerException {
        logger.debug("getting user: " + user);

        try {
            return SqlConfig.getSqlMapClient().queryForList("User.getUser", user);
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void updateUser(User user, String plainTextPassword) throws ControllerException {
        try {

            PasswordRequirements passwordRequirments = ControllerFactory.getFactory().createConfigurationController().getPasswordRequirements();
            Vector<String> responses = PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(plainTextPassword, passwordRequirments);
            if (responses != null) {
                String resString = "";
                for (String response : responses) {
                    resString += response + "\n";
                }
                throw new ControllerException(resString);
            }
            if (user.getId() == null) {
                logger.debug("adding user: " + user);
                SqlConfig.getSqlMapClient().insert("User.insertUser", getUserMap(user, plainTextPassword));
            } else {
                logger.debug("updating user: " + user);
                SqlConfig.getSqlMapClient().update("User.updateUser", getUserMap(user, plainTextPassword));
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeUser(User user) throws ControllerException {
        logger.debug("removing user: " + user);

        try {
            SqlConfig.getSqlMapClient().delete("User.deleteUser", user);

            if (DatabaseUtil.statementExists("User.vacuumPersonTable")) {
                SqlConfig.getSqlMapClient().update("User.vacuumPersonTable");
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public boolean authorizeUser(User user, String plainTextPassword) throws ControllerException {
        try {
            Credentials credentials = (Credentials) SqlConfig.getSqlMapClient().queryForObject("User.getUserCredentials", user);

            if (credentials != null) {
                String checkPasswordHash = encrypter.getHash(plainTextPassword, credentials.getSalt());
                return checkPasswordHash.equals(credentials.getPassword());
            }

            return false;
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void loginUser(User user) throws ControllerException {
        try {
            SqlConfig.getSqlMapClient().update("User.loginUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void logoutUser(User user) throws ControllerException {
        try {
            SqlConfig.getSqlMapClient().update("User.logoutUser", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }

    }

    public boolean isUserLoggedIn(User user) throws ControllerException {
        try {
            return (Boolean) SqlConfig.getSqlMapClient().queryForObject("User.isUserLoggedIn", user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    private Map<String, Object> getUserMap(User user, String plainTextPassword) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        if (user.getId() != null) {
            parameterMap.put("id", user.getId());
        }

        parameterMap.put("username", user.getUsername());
        parameterMap.put("firstName", user.getFirstName());
        parameterMap.put("lastName", user.getLastName());
        parameterMap.put("organization", user.getOrganization());
        parameterMap.put("email", user.getEmail());
        parameterMap.put("phoneNumber", user.getPhoneNumber());
        parameterMap.put("description", user.getDescription());

        // hash the user's password before storing it in the database
        try {
            String salt = encrypter.getSalt();
            parameterMap.put("password", encrypter.getHash(plainTextPassword, salt));
            parameterMap.put("salt", salt);
        } catch (EncryptionException ee) {
            // ignore this
        }

        return parameterMap;
    }

    public Properties getUserPreferences(User user) throws ControllerException {
        try {
            return ConfigurationController.getInstance().getPropertiesForGroup("user." + user.getId());
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

    public void setUserPreference(User user, String name, String value) throws ControllerException {
        try {
            ConfigurationController.getInstance().saveProperty("user." + user.getId(), name, value);
        } catch (Exception e) {
            throw new ControllerException(e);
        }
    }

}
