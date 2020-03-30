package com.task.roman.connectionmanager.pool.impl;

import com.task.roman.connectionmanager.pool.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@AllArgsConstructor
@Slf4j
public class ConnectionFactoryImpl implements ConnectionFactory {

    private String url;

    private String user;

    private String password;

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnectionHealthy(Connection connection) {
        try {
            // send dummy query to check if the connection is ok
            Statement s = connection.createStatement();
            s.executeQuery("SELECT 1");
            s.close();
            connection.commit();
        } catch (Exception e) {
            log.debug("Connection is not active");
            return false;
        }

        return true;
    }


}
