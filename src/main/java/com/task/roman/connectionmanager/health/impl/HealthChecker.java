package com.task.roman.connectionmanager.health.impl;

import com.task.roman.connectionmanager.health.HealthNotifier;
import com.task.roman.connectionmanager.pool.ConnectionFactory;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class HealthChecker extends Thread {

    // how often to check that the database is ready
    private static final int CHECKING_PERIOD_MILLISECONDS = 500;

    // database which should be checked
    @NonNull
    private ConnectionFactory connectionFactory;

    // notifier which is notified when the database is back up and running
    @NonNull
    private HealthNotifier healthNotifier;

    /**
     * Constructor of health checker
     * @param connectionFactory {@link ConnectionFactory} which creates checker connection to database
     * @param notifier {@link HealthNotifier} which is notified when the database is back up and running
     */
    public HealthChecker(ConnectionFactory connectionFactory, HealthNotifier notifier) {
        this.setDaemon(true);
        this.setName(getClass().getName());

        this.connectionFactory = connectionFactory;
        this.healthNotifier = notifier;
    }

    private boolean isNotHealthy() {
        return Try.of(connectionFactory::getConnection).andThenTry(connectionFactory::isConnectionHealthy).isFailure();
    }

    @Override
    public void run() {
        while (isNotHealthy()) {
            log.error("health waiting");
            takeBreak();
        }

        healthNotifier.backUpAndRunning();
    }

    private void takeBreak() {
        try {
            TimeUnit.MILLISECONDS.sleep(CHECKING_PERIOD_MILLISECONDS);
        } catch (InterruptedException e) {
            // left blank
        }
    }

}
