package me.hsgamer.topper.fabric.manager;

import me.hsgamer.topper.agent.core.Agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskManager {
    private static final long MILLIS_PER_TICKS = 50;
    private final List<Task> syncTasks = Collections.synchronizedList(new ArrayList<>());
    private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);

    public void onTick() {
        Iterator<Task> taskIterator = syncTasks.iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.isCancelled()) {
                taskIterator.remove();
                continue;
            }
            task.run();
        }
    }

    public Agent createTaskAgent(Runnable runnable, boolean async, long delayTicks) {
        long finalDelayTicks = Math.max(1, delayTicks);
        if (async) {
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
        } else {
            return new Agent() {
                private Task task;

                @Override
                public void start() {
                    task = new Task(runnable, finalDelayTicks);
                    syncTasks.add(task);
                }

                @Override
                public void stop() {
                    if (task != null) {
                        task.cancel();
                    }
                    task = null;
                }
            };
        }
    }

    private static final class Task {
        private final Runnable runnable;
        private final long delayTicks;
        private long nextTime;
        private boolean cancel;

        Task(Runnable runnable, long delayTicks) {
            this.runnable = runnable;
            this.delayTicks = delayTicks;
            scheduleTime();
        }

        boolean canRun() {
            return System.currentTimeMillis() >= nextTime;
        }

        void scheduleTime() {
            nextTime = System.currentTimeMillis() + (delayTicks * MILLIS_PER_TICKS);
        }

        boolean isCancelled() {
            return cancel;
        }

        void cancel() {
            cancel = true;
        }

        void run() {
            if (canRun()) {
                runnable.run();
                scheduleTime();
            }
        }
    }
}
