package ru.hse.edu.aaarifkhanov192.supportiveclasses;


import java.util.concurrent.*;

/**
 * Debouncer pattern
 * taken from https://stackoverflow.com/questions/4742210/implementing-debounce-in-java
 * @param <T> type of Key argument to be synchronized
 */
public class Debouncer <T> {

    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<T, TimerTask> delayedMap = new ConcurrentHashMap<T, TimerTask>();
    private final Callback<T> callback;
    private final int interval;

    public Debouncer(Callback<T> c, int interval) {
        this.callback = c;
        this.interval = interval;
    }

    public void call(T key) {
        TimerTask task = new TimerTask(key);

        TimerTask prev;
        try {
            do {
                prev = delayedMap.putIfAbsent(key, task);
                if (prev == null)
                    sched.schedule(task, interval, TimeUnit.MILLISECONDS);
            } while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
        }
        catch (RejectedExecutionException e){
            System.out.println(e.getMessage());
        }
    }

    public void terminate() {
        sched.shutdownNow();
    }

    // The task that wakes up when the wait time elapses
    private class TimerTask implements Runnable {
        private final T key;
        private long dueTime;
        private final Object lock = new Object();

        public TimerTask(T key) {
            this.key = key;
            extend();
        }

        public boolean extend() {
            synchronized (lock) {
                if (dueTime < 0) // Task has been shutdown
                    return false;
                dueTime = System.currentTimeMillis() + interval;
                return true;
            }
        }

        public void run() {
            synchronized (lock) {
                long remaining = dueTime - System.currentTimeMillis();
                if (remaining > 0) { // Re-schedule task
                    sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
                } else { // Mark as terminated and invoke callback
                    dueTime = -1;
                    try {
                        callback.call(key);
                    } finally {
                        delayedMap.remove(key);
                    }
                }
            }
        }
    }
}