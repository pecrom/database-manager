package com.task.roman.connectionmanager.pool;

import com.task.roman.connectionmanager.pool.exception.UnhealthyConnectionException;
import com.task.roman.connectionmanager.pool.impl.ConnectionPool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectionPoolTest {

    @DisplayName("Successfully get connection")
    @Test
    void getConnection() throws UnhealthyConnectionException, SQLException {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        Connection connection = mock(Connection.class);

        when(connectionFactory.isConnectionHealthy(connection))
                .thenReturn(true);

        when(connectionFactory.getConnection())
                .thenReturn(connection);


        ConnectionPool connectionPool = ConnectionPool.newPool().connectionFactory(connectionFactory).connections(1).init();

        assertEquals(connection, connectionPool.getConnection());
    }

    @DisplayName("UnhealthyConnectionException thrown when connection is not healthy")
    @Test
    void getConnectionWithException() throws SQLException {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        Connection connection = mock(Connection.class);

        when(connectionFactory.isConnectionHealthy(connection))
                .thenReturn(false);

        when(connectionFactory.getConnection())
                .thenReturn(connection);


        ConnectionPool connectionPool = ConnectionPool.newPool().connectionFactory(connectionFactory).connections(1).init();
        assertThrows(UnhealthyConnectionException.class, connectionPool::getConnection);
    }

    @DisplayName("Check that there is not more available connections")
    @Test
    void checkNotAvailableConnections() throws SQLException, UnhealthyConnectionException {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        Connection connection = mock(Connection.class);

        when(connectionFactory.isConnectionHealthy(connection))
                .thenReturn(true);

        when(connectionFactory.getConnection())
                .thenReturn(connection);


        ConnectionPool connectionPool = ConnectionPool.newPool().connectionFactory(connectionFactory).connections(1).init();
        connectionPool.getConnection();

        assertTrue(connectionPool.hasNotAvailable());
    }
}