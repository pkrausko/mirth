/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;

public class DatabaseDispatcherQuery implements DatabaseDispatcherDelegate {
    private DatabaseDispatcher connector;
    private Map<Long, BasicDataSource> dataSources = new ConcurrentHashMap<Long, BasicDataSource>();

    public DatabaseDispatcherQuery(DatabaseDispatcher connector) {
        this.connector = connector;
    }

    @Override
    public void deploy() throws ConnectorTaskException {}

    @Override
    public void undeploy() throws ConnectorTaskException {}

    @Override
    public void start() throws ConnectorTaskException {}

    @Override
    public void stop() throws ConnectorTaskException {
        Throwable firstThrowable = null;

        for (BasicDataSource dataSource : dataSources.values()) {
            if (dataSource != null && !dataSource.isClosed()) {
                try {
                    dataSource.close();
                } catch (Throwable t) {
                    if (firstThrowable == null) {
                        firstThrowable = t;
                    }
                }
            }
        }

        if (firstThrowable != null) {
            throw new ConnectorTaskException("Failed to close data source", firstThrowable);
        }

        dataSources.clear();
    }

    @Override
    public void halt() throws ConnectorTaskException {
        stop();
    }

    @Override
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException {
        // send the message and retry (once) if the database connection fails
        return send(connectorProperties, true);
    }

    private Response send(DatabaseDispatcherProperties connectorProperties, boolean retryOnConnectionFailure) throws DatabaseDispatcherException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection(connectorProperties);
            statement = connection.prepareStatement(connectorProperties.getQuery());
            int i = 1;

            for (Object param : connectorProperties.getParameters()) {
                statement.setObject(i++, param);
            }

            /*
             * We do not use Statement.executeUpdate() here because it could prevent users from
             * executing a stored procedure. Executing a stored procedure in Postgres (and possibly
             * other databases) is done via SELECT myprocedure(), which breaks executeUpdate() since
             * it returns a result, even if the procedure itself returns void.
             */
            statement.execute();
            int numRows = statement.getUpdateCount();
            String responseData = null;
            String responseMessageStatus = null;

            if (numRows == -1) {
                responseMessageStatus = "Database write success";
            } else {
                responseMessageStatus = "Database write success, " + numRows + " rows updated";
            }

            return new Response(Status.SENT, responseData, responseMessageStatus);
        } catch (SQLException e) {
            if (connection != null && !JdbcUtils.isValidConnection(connection)) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    throw new DatabaseDispatcherException("Failed to close an invalid database connection", e1);
                }
            }

            try {
                if (retryOnConnectionFailure && (connection == null || connection.isClosed())) {
                    // retry sending on a new connection and pass retryOnConnectionFailure = false this time, so that we only retry once
                    return send(connectorProperties, false);
                }
            } catch (SQLException e1) {
                // if connection.isClosed() threw a SQLException, ignore it and refer to the original exception
            }

            throw new DatabaseDispatcherException("Failed to write to database", e);
        } finally {
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Get a database connection based on the given connector properties.
     */
    private Connection getConnection(DatabaseDispatcherProperties connectorProperties) throws SQLException {
        long dispatcherId = connector.getDispatcherId();
        BasicDataSource dataSource = dataSources.get(dispatcherId);

        /*
         * If we have an existing connection pool and it is based on the same
         * driver/username/password/url that is set in the given connector properties, then re-use
         * the pool. Otherwise, close it and create a new pool since the connection settings have
         * changed.
         */

        if (dataSource != null && !dataSource.isClosed()) {
            if (connectorProperties.getDriver().equals(dataSource.getDriverClassName()) && connectorProperties.getUsername().equals(dataSource.getUsername()) && connectorProperties.getPassword().equals(dataSource.getPassword()) && connectorProperties.getUrl().equals(dataSource.getUrl())) {
                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(true);
                return connection;
            }

            try {
                dataSource.close();
            } catch (SQLException e) {
            }
        }

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(connectorProperties.getDriver());
        dataSource.setUsername(connectorProperties.getUsername());
        dataSource.setPassword(connectorProperties.getPassword());
        dataSource.setUrl(connectorProperties.getUrl());

        /*
         * MIRTH-3570: As of DBCP version 2.0, test-on-borrow is enabled by default in
         * BasicDataSource and is automatically done using the JDBC 4.0 isValid method. We don't
         * want to do this for two reasons: 1) Our SQL Server (JTDS) driver doesn't support JDBC
         * 4.0, so it would be necessary to provide a validation query and 2) DBCP's test-on-borrow
         * happens on every single borrow, which could slow performance (unlike HikariCP which tests
         * at most once a second).
         */
        dataSource.setTestOnBorrow(false);

        dataSources.put(dispatcherId, dataSource);

        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }
}
