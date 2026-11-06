package me.hsgamer.topper.fabric.manager;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskManager {
    private static final long MILLIS_PER_TICKS = 50;
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    public Runnable schedule(Runnable runnable, long delayTicks) {
        long finalDelayTicks = Math.max(1, delayTicks);
        long delayMillis = finalDelayTicks * MILLIS_PER_TICKS;
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(runnable, delayMillis, delayMillis, TimeUnit.MILLISECONDS);
        return () -> scheduledFuture.cancel(true);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
