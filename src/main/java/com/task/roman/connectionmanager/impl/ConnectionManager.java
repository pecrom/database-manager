package com.task.roman.connectionmanager.impl;

import com.task.roman.connectionmanager.pool.impl.ConnectionPool;
import com.task.roman.connectionmanager.health.impl.HealthChecker;
import com.task.roman.connectionmanager.health.HealthCheckerFactory;
import com.task.roman.connectionmanager.health.HealthNotifier;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;

@RequiredArgsConstructor
@Log4j2
public final class ConnectionManager implements HealthNotifier {

    // flag that the master database should be used
    private volatile boolean masterActive = true;

    // connection pools
    @NonNull
    private ConnectionPool masterPool, slavePool;

    // health checker
    @NonNull
    private HealthCheckerFactory healthCheckerFactory;


    // switch to slave, because master is down
    private void switchToSlave(Throwable ex) {
        log.debug("Switching to slave");
        masterActive = false;

        // start checking if the master is back up and ready to accept connections
        HealthChecker healthChecker = healthCheckerFactory.createHealthChecker(this);
        healthChecker.start();
    }

    // check if master is up and wait for the connection to master
    private void isMasterWait() {
        while(masterActive && masterPool.hasNotAvailable()) {
            waitForConnection();
        }
    }

    // check if master is down and wait for the connection to slave
    private void isSlaveWait() {
        while(!masterActive && slavePool.hasNotAvailable()) {
            waitForConnection();
        }
    }

    private void waitForConnection() {
        try {
            wait();
        } catch (InterruptedException e) {
            // left blank
        }
    }

    /**
     * Get connection from connection pool. First try to acquire connection to master db, if not available, then return
     * connection to slave db.
     *
     * @return {@link Connection} connection from connection pool
     */
    public synchronized Connection getConnection() {
        isMasterWait();
        isSlaveWait();

        Connection connection;

        if (masterActive) {
            log.debug("Getting connectiom from MASTER");
            connection = Try.of(masterPool::getConnection)
                            .onFailure(this::switchToSlave)
                            .getOrElseTry(slavePool::getConnection);
        } else {
            log.debug("Getting connection from SLAVE");
            connection = Try.of(slavePool::getConnection).get();
        }

        return connection;
    }

    /**
     * Release connection
     *
     * @param  connection to be released
     */
    public synchronized void releaseConnection(Connection connection) {
        masterPool.releaseConnection(connection);
        slavePool.releaseConnection(connection);
        notify();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void backUpAndRunning() {
        log.debug("Master is back up and running");
        masterPool.reconnect();
        masterActive = true;
    }
}
