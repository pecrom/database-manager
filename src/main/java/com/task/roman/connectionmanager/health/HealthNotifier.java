package com.task.roman.connectionmanager.health;

@FunctionalInterface
public interface HealthNotifier {

    /**
     * Notify that the database is back up and ready to accept connections
     */
    void backUpAndRunning();
}
