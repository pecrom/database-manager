package com.task.roman.connectionmanager.pool.impl;

import com.task.roman.connectionmanager.pool.ConnectionFactory;
import com.task.roman.connectionmanager.pool.exception.UnhealthyConnectionException;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class ConnectionPool {

    // all connections
    private List<Connection> pool;

    // connections which are available
    private Queue<Connection> availableConnections;

    // number of connections
    private int connectionsCount;

    @NonNull
    private ConnectionFactory connectionFactory;

    @Builder(buildMethodName = "init", builderMethodName = "newPool")
    private ConnectionPool(ConnectionFactory connectionFactory, int connections) {
        this.connectionFactory = connectionFactory;
        this.connectionsCount = connections;
        init();
    }

    // initialize
    private void init() {
        pool = new ArrayList<>(connectionsCount);
        availableConnections = new LinkedBlockingDeque<>(connectionsCount);
        reconnect();
    }


    /**
     * Get available connection
     * @return {@link Connection} available connection
     * @throws UnhealthyConnectionException thrown when the database is not running
     */
    public Connection getConnection() throws UnhealthyConnectionException {
        Connection availableConnection = availableConnections.poll();

        log.debug("available connection: " + availableConnection);

        if (availableConnection == null || isNotHealthy(availableConnection)) {
            throw new UnhealthyConnectionException();
        }

        return availableConnection;
    }

    /**
     * Return connection back to pool
     * @param connection
     */
    public void releaseConnection(Connection connection) {
        if (pool.contains(connection)) {
            availableConnections.add(connection);
            log.debug("releasing connection: " + connection);
        }
    }

    // check if the connection is ok by sending simple query
    private boolean isNotHealthy(Connection connection) {
        return !connectionFactory.isConnectionHealthy(connection);
    }

    // creates connection
    private Connection createConnection() {
        Connection connection = null;

        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);
        } catch (Exception e) {
            log.error("Can not create connection");
        }

        return connection;
    }

    // close connection
    private void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            // left blank
        }
    }

    /**
     * Reconnects
     */
    public void reconnect() {
        pool.stream().filter(Objects::nonNull).forEach(this::closeConnection);
        pool.clear();
        availableConnections.clear();

        Connection newConnection;

        // init new connections
        for (int connectionIdx = 0; connectionIdx < connectionsCount; connectionIdx++) {
            newConnection = createConnection();

            if (newConnection != null) {
                pool.add(newConnection);
                availableConnections.add(newConnection);
            }

        }
    }

    /**
     * Check if there is any available connection
     * @return true / false
     */
    public boolean hasNotAvailable() {
        return availableConnections.isEmpty() && !pool.isEmpty();
    }
}
