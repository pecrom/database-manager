package com.task.roman.connectionmanager.pool;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionFactory {

    /**
     * Get database connection
     * @return new {@link Connection}
     */
    Connection getConnection() throws SQLException;

    /**
     * Check if connection is healthy
     * @param connection {@link Connection} to be checked that it is healthy
     * @return true / false
     */
    boolean isConnectionHealthy(Connection connection);
}
