package com.task.roman;

import com.task.roman.connectionmanager.impl.ConnectionManager;
import com.task.roman.connectionmanager.pool.ConnectionFactory;
import com.task.roman.connectionmanager.pool.impl.ConnectionPool;
import com.task.roman.connectionmanager.health.HealthCheckerFactory;
import com.task.roman.connectionmanager.pool.impl.ConnectionFactoryImpl;
import com.task.roman.connectionmanager.health.impl.HealthCheckerFactoryImpl;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static ConnectionPool createPool(String url, String user, String password, int connectionCount) {
        ConnectionFactory connectionFactory = new ConnectionFactoryImpl(url, user, password);
        return ConnectionPool.newPool().connectionFactory(connectionFactory).connections(connectionCount).init();
    }

    private static HealthCheckerFactory createHealthCheckerFactory(String url, String user, String password) {
        ConnectionFactory healthConnectionFactory = new ConnectionFactoryImpl(url, user, password);

        return new HealthCheckerFactoryImpl(healthConnectionFactory);
    }

    public static void main(String[] args) {
        ConnectionPool masterConnectionPool = createPool("jdbc:postgresql://localhost:5435/master-database", "master", "master", 10);
        ConnectionPool slaveConnectionPool = createPool("jdbc:postgresql://localhost:5436/slave-database", "slave", "slave", 10);
        HealthCheckerFactory healthCheckerFactory =createHealthCheckerFactory("jdbc:postgresql://localhost:5435/master-database", "master", "master");

        ConnectionManager connectionManager = new ConnectionManager(masterConnectionPool, slaveConnectionPool, healthCheckerFactory);
        testConnectionManager(connectionManager);
    }

    private static void sleepForAWhile() {
        try {
            TimeUnit.SECONDS.sleep((int)(Math.random() * 10));
        } catch (InterruptedException e) {
            // blank
        }
    }

    private static void testConnectionManager(ConnectionManager connectionManager) {

        ExecutorService executor = Executors.newFixedThreadPool(3);

        for (int numberOfThreads = 0; numberOfThreads < 3; numberOfThreads++) {

            executor.execute(() -> {
                do {
                    Connection connection = connectionManager.getConnection();
                    sleepForAWhile();
                    connectionManager.releaseConnection(connection);
                    sleepForAWhile();
                } while(true);
            });
        }


        while (!executor.isTerminated()) {

        }
        System.out.println("\nFinished all threads");
    }
}
