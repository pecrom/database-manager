package com.task.roman.connectionmanager.health;

import com.task.roman.connectionmanager.health.impl.HealthChecker;

public interface HealthCheckerFactory {

    /**
     * Create new health checker
     * @param healthNotifier notifier which is notified when the database is bak up and running
     * @return {@link HealthChecker} new health checker
     */
    HealthChecker createHealthChecker(HealthNotifier healthNotifier);
}
