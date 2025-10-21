package concurrency;

import java.util.*;
import java.util.concurrent.*;

/**
 * Thread Pool Pattern
 * Manages a pool of worker threads to execute tasks efficiently.
 */
public class ThreadPoolPattern {
    
    // Simple Thread Pool Implementation
    static class SimpleThreadPool {
        private final BlockingQueue<Runnable> taskQueue;
        private final List<WorkerThread> threads;
        private volatile boolean isStopped = false;
        
        public SimpleThreadPool(int poolSize, int queueSize) {
            taskQueue = new LinkedBlockingQueue<>(queueSize);
            threads = new ArrayList<>();
            
            for (int i = 0; i < poolSize; i++) {
                WorkerThread thread = new WorkerThread(taskQueue, "Worker-" + i);
                threads.add(thread);
                thread.start();
            }
        }
        
        public void execute(Runnable task) throws InterruptedException {
            if (isStopped) {
                throw new IllegalStateException("Thread pool is stopped");
            }
            taskQueue.put(task);
        }
        
        public void shutdown() {
            isStopped = true;
            for (WorkerThread thread : threads) {
                thread.stopThread();
            }
        }
        
        public void awaitTermination() throws InterruptedException {
            for (WorkerThread thread : threads) {
                thread.join();
            }
        }
        
        private class WorkerThread extends Thread {
            private final BlockingQueue<Runnable> queue;
            private volatile boolean stopped = false;
            
            public WorkerThread(BlockingQueue<Runnable> queue, String name) {
                super(name);
                this.queue = queue;
            }
            
            @Override
            public void run() {
                while (!stopped) {
                    try {
                        Runnable task = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (task != null) {
                            System.out.println(Thread.currentThread().getName() + " executing task");
                            task.run();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                System.out.println(Thread.currentThread().getName() + " stopped");
            }
            
            public void stopThread() {
                stopped = true;
                this.interrupt();
            }
        }
    }
    
    // Using Java's ExecutorService
    static class TaskExecutor {
        public static void demonstrateFixedThreadPool() {
            System.out.println("=== Fixed Thread Pool ===");
            ExecutorService executor = Executors.newFixedThreadPool(3);
            
            for (int i = 1; i <= 6; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " started by " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Task " + taskId + " completed");
                });
            }
            
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public static void demonstrateCachedThreadPool() {
            System.out.println("\n=== Cached Thread Pool ===");
            ExecutorService executor = Executors.newCachedThreadPool();
            
            for (int i = 1; i <= 10; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " by " + 
                                     Thread.currentThread().getName());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        public static void demonstrateScheduledThreadPool() {
            System.out.println("\n=== Scheduled Thread Pool ===");
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
            
            // Schedule task with delay
            executor.schedule(() -> {
                System.out.println("Task executed after 1 second delay");
            }, 1, TimeUnit.SECONDS);
            
            // Schedule periodic task
            ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
                System.out.println("Periodic task at " + System.currentTimeMillis());
            }, 0, 2, TimeUnit.SECONDS);
            
            // Let it run for a bit
            try {
                Thread.sleep(7000);
                future.cancel(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            executor.shutdown();
        }
    }
    
    // Custom ThreadPoolExecutor with monitoring
    static class MonitoredThreadPool {
        private final ThreadPoolExecutor executor;
        
        public MonitoredThreadPool(int coreSize, int maxSize) {
            executor = new ThreadPoolExecutor(
                coreSize, maxSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }
        
        public void execute(Runnable task) {
            executor.execute(task);
        }
        
        public void printStats() {
            System.out.println("\n--- Thread Pool Stats ---");
            System.out.println("Active threads: " + executor.getActiveCount());
            System.out.println("Pool size: " + executor.getPoolSize());
            System.out.println("Core pool size: " + executor.getCorePoolSize());
            System.out.println("Largest pool size: " + executor.getLargestPoolSize());
            System.out.println("Task count: " + executor.getTaskCount());
            System.out.println("Completed tasks: " + executor.getCompletedTaskCount());
        }
        
        public void shutdown() {
            executor.shutdown();
        }
        
        public void awaitTermination() throws InterruptedException {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread Pool Pattern Demo ===\n");
        
        // 1. Simple custom thread pool
        System.out.println("1. Custom Thread Pool:");
        SimpleThreadPool pool = new SimpleThreadPool(3, 10);
        
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            pool.execute(() -> {
                System.out.println("  Task " + taskId + " processing...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        Thread.sleep(3000);
        pool.shutdown();
        pool.awaitTermination();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 2. Java's ExecutorService variants
        TaskExecutor.demonstrateFixedThreadPool();
        Thread.sleep(1000);
        TaskExecutor.demonstrateCachedThreadPool();
        Thread.sleep(1000);
        TaskExecutor.demonstrateScheduledThreadPool();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 3. Monitored thread pool
        System.out.println("3. Monitored Thread Pool:");
        MonitoredThreadPool monitoredPool = new MonitoredThreadPool(2, 4);
        
        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            monitoredPool.execute(() -> {
                System.out.println("Task " + taskId + " started");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        Thread.sleep(2000);
        monitoredPool.printStats();
        
        monitoredPool.shutdown();
        monitoredPool.awaitTermination();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Reuses threads efficiently");
        System.out.println("✓ Limits resource consumption");
        System.out.println("✓ Better performance than creating threads per task");
        System.out.println("✓ Built-in queue management");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Web server request handling");
        System.out.println("• Background task processing");
        System.out.println("• Parallel computation");
        System.out.println("• Database connection pooling");
    }
}
