package me.hsgamer.topper.fabric.manager;

import me.hsgamer.topper.agent.core.Agent;
import me.hsgamer.topper.fabric.TopperFabric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TaskManager {
    private final TopperFabric mod;
    private final List<Task> syncTasks = Collections.synchronizedList(new ArrayList<>());

    public TaskManager(TopperFabric mod) {
        this.mod = mod;
    }

    public void onTick() {
        Iterator<Task> taskIterator = syncTasks.iterator();
        while (taskIterator.hasNext()) {
            Task task = taskIterator.next();
            if (task.isCancelled()) {
                taskIterator.remove();
                continue;
            }
            if (task.canRun()) {
                task.run();
                task.scheduleTime();
            }
        }
    }

    public Agent createTaskAgent(Runnable runnable, boolean async, long delayTicks) {
        return new Agent() {
            private Task task;

            @Override
            public void start() {
                task = new Task(runnable, Math.max(1, delayTicks));
                if (async) {

                } else {
                    syncTasks.add(task);
                }
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

    private static final class Task {
        private static final long MILLIS_PER_TICKS = 50;
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
            runnable.run();
        }
    }
}
