package me.hsgamer.topper.fabric.manager;

import me.hsgamer.topper.agent.core.Agent;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskManager {
    private static final long MILLIS_PER_TICKS = 50;
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    public Agent createTaskAgent(Runnable runnable, long delayTicks) {
        long finalDelayTicks = Math.max(1, delayTicks);
        long delayMillis = finalDelayTicks * MILLIS_PER_TICKS;
        return new Agent() {
            private ScheduledFuture<?> scheduledFuture;

            @Override
            public void start() {
                scheduledFuture = scheduler.scheduleAtFixedRate(runnable, delayMillis, delayMillis, TimeUnit.MILLISECONDS);
            }

            @Override
            public void stop() {
                if (scheduledFuture != null) {
                    scheduledFuture.cancel(true);
                }
            }
        };
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
