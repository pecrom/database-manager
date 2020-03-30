package com.task.roman.connectionmanager.health.impl;

import com.task.roman.connectionmanager.pool.ConnectionFactory;
import com.task.roman.connectionmanager.health.HealthCheckerFactory;
import com.task.roman.connectionmanager.health.HealthNotifier;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HealthCheckerFactoryImpl implements HealthCheckerFactory {

    private ConnectionFactory connectionFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public HealthChecker createHealthChecker(HealthNotifier healthNotifier) {
        return new HealthChecker(connectionFactory, healthNotifier);
    }
}
