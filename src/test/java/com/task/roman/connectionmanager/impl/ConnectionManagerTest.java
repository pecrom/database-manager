package com.task.roman.connectionmanager.impl;

import com.task.roman.connectionmanager.pool.impl.ConnectionPool;
import com.task.roman.connectionmanager.health.impl.HealthChecker;
import com.task.roman.connectionmanager.health.HealthCheckerFactory;
import com.task.roman.connectionmanager.pool.exception.UnhealthyConnectionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectionManagerTest {

    @DisplayName("Get connection from master")
    @Test
    void getConnectionFromMaster() throws UnhealthyConnectionException {
        ConnectionPool masterPool = mock(ConnectionPool.class);
        ConnectionPool slavePool = mock(ConnectionPool.class);
        HealthCheckerFactory healthCheckerFactory = mock(HealthCheckerFactory.class);

        Connection masterConnection = mock(Connection.class);

        when(masterPool.getConnection())
                .thenReturn(masterConnection);


        ConnectionManager connectionManager = new ConnectionManager(masterPool, slavePool, healthCheckerFactory);
        assertEquals(masterConnection, connectionManager.getConnection());
    }


    @DisplayName("Get connection from slave")
    @Test
    void getConnectionFromSlave() throws UnhealthyConnectionException {
        ConnectionPool masterPool = mock(ConnectionPool.class);
        ConnectionPool slavePool = mock(ConnectionPool.class);
        HealthChecker healthChecker = mock(HealthChecker.class);
        HealthCheckerFactory healthCheckerFactory = mock(HealthCheckerFactory.class);


        Connection slaveConnection = mock(Connection.class);

        when(masterPool.getConnection())
                .thenThrow(UnhealthyConnectionException.class);

        when(slavePool.getConnection())
                .thenReturn(slaveConnection);

        when(healthCheckerFactory.createHealthChecker(any()))
                .thenReturn(healthChecker);


        ConnectionManager connectionManager = new ConnectionManager(masterPool, slavePool, healthCheckerFactory);
        assertEquals(slaveConnection, connectionManager.getConnection());
    }
}