package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Queue-Based Load Leveling Pattern
 * 
 * Intent: Use a queue as a buffer between a task and a service to smooth
 * intermittent heavy loads that may cause the service to fail or the task to timeout.
 * 
 * Also Known As:
 * - Load Smoothing
 * - Queue Buffer
 * - Asynchronous Request-Reply
 * 
 * Motivation:
 * Many cloud applications experience variable workloads. Direct synchronous
 * processing can lead to:
 * - Service overload during traffic spikes
 * - Request timeouts and failures
 * - Poor resource utilization during low traffic
 * - Inability to handle burst traffic
 * 
 * Applicability:
 * - Variable or unpredictable workload patterns
 * - Tasks that can be processed asynchronously
 * - Need to decouple producers from consumers
 * - Requirements to handle traffic spikes gracefully
 * - Background processing scenarios
 * 
 * Benefits:
 * - Smooths load spikes
 * - Improves resilience
 * - Decouples components
 * - Better resource utilization
 * - Enables auto-scaling based on queue depth
 * - Can continue accepting requests even if processing is slow
 * 
 * Trade-offs:
 * - Additional latency (asynchronous processing)
 * - Need to handle message persistence
 * - Complexity in error handling and retries
 * - Requires monitoring queue depth
 */

// Task to be processed
class Task {
    private final String id;
    private final String data;
    private final long submittedAt;
    private final int priority;
    
    public Task(String id, String data, int priority) {
        this.id = id;
        this.data = data;
        this.priority = priority;
        this.submittedAt = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public String getData() { return data; }
    public long getSubmittedAt() { return submittedAt; }
    public int getPriority() { return priority; }
    
    public long getWaitTime() {
        return System.currentTimeMillis() - submittedAt;
    }
    
    @Override
    public String toString() {
        return String.format("Task[%s: %s]", id, data);
    }
}

// Task result
class TaskResult {
    private final String taskId;
    private final boolean success;
    private final String result;
    private final long processingTimeMs;
    
    public TaskResult(String taskId, boolean success, String result, long processingTimeMs) {
        this.taskId = taskId;
        this.success = success;
        this.result = result;
        this.processingTimeMs = processingTimeMs;
    }
    
    public String getTaskId() { return taskId; }
    public boolean isSuccess() { return success; }
    public String getResult() { return result; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    
    @Override
    public String toString() {
        return String.format("Result[%s: %s, %dms]", taskId, success ? "✓" : "✗", processingTimeMs);
    }
}

// Example 1: Basic Queue-Based Load Leveling
class BasicQueueLoadLeveler {
    private final BlockingQueue<Task> taskQueue;
    private final ExecutorService workers;
    private final AtomicInteger processedCount;
    private volatile boolean running = true;
    
    public BasicQueueLoadLeveler(int queueCapacity, int workerCount) {
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = Executors.newFixedThreadPool(workerCount);
        this.processedCount = new AtomicInteger(0);
        
        // Start workers
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i + 1;
            workers.submit(() -> processTasksWorker(workerId));
        }
    }
    
    public boolean submitTask(Task task) {
        boolean accepted = taskQueue.offer(task);
        if (accepted) {
            System.out.println(String.format("[SUBMIT] %s (queue depth: %d)", 
                task, taskQueue.size()));
        } else {
            System.out.println(String.format("[REJECTED] %s (queue full)", task));
        }
        return accepted;
    }
    
    private void processTasksWorker(int workerId) {
        while (running) {
            try {
                Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    processTask(workerId, task);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void processTask(int workerId, Task task) {
        long start = System.currentTimeMillis();
        long waitTime = task.getWaitTime();
        
        System.out.println(String.format("[Worker-%d] Processing %s (waited %dms, queue: %d)", 
            workerId, task, waitTime, taskQueue.size()));
        
        // Simulate processing
        try {
            Thread.sleep(100 + new Random().nextInt(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long processingTime = System.currentTimeMillis() - start;
        processedCount.incrementAndGet();
        
        System.out.println(String.format("[Worker-%d] Completed %s (%dms)", 
            workerId, task.getId(), processingTime));
    }
    
    public int getQueueDepth() {
        return taskQueue.size();
    }
    
    public int getProcessedCount() {
        return processedCount.get();
    }
    
    public void shutdown() {
        running = false;
        workers.shutdown();
    }
}

// Example 2: Auto-Scaling Load Leveler
// Automatically adjusts worker count based on queue depth
class AutoScalingLoadLeveler {
    private final BlockingQueue<Task> taskQueue;
    private final List<Future<?>> workers;
    private final ExecutorService executorService;
    private final int minWorkers;
    private final int maxWorkers;
    private final int scaleUpThreshold;
    private final int scaleDownThreshold;
    private volatile boolean running = true;
    private final AtomicInteger activeWorkers;
    
    public AutoScalingLoadLeveler(int queueCapacity, int minWorkers, int maxWorkers) {
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.minWorkers = minWorkers;
        this.maxWorkers = maxWorkers;
        this.scaleUpThreshold = queueCapacity / 2;  // Scale up at 50% capacity
        this.scaleDownThreshold = queueCapacity / 10;  // Scale down at 10% capacity
        this.activeWorkers = new AtomicInteger(0);
        
        // Start with minimum workers
        for (int i = 0; i < minWorkers; i++) {
            addWorker();
        }
        
        // Start auto-scaling monitor
        executorService.submit(this::monitorAndScale);
    }
    
    public boolean submitTask(Task task) {
        return taskQueue.offer(task);
    }
    
    private void addWorker() {
        int workerId = activeWorkers.incrementAndGet();
        Future<?> worker = executorService.submit(() -> processTasksWorker(workerId));
        workers.add(worker);
        System.out.println(String.format("[SCALE UP] Added worker-%d (total: %d)", 
            workerId, activeWorkers.get()));
    }
    
    private void removeWorker() {
        if (workers.size() > minWorkers && !workers.isEmpty()) {
            Future<?> worker = workers.remove(workers.size() - 1);
            worker.cancel(true);
            activeWorkers.decrementAndGet();
            System.out.println(String.format("[SCALE DOWN] Removed worker (total: %d)", 
                activeWorkers.get()));
        }
    }
    
    private void monitorAndScale() {
        while (running) {
            try {
                Thread.sleep(500);
                
                int queueDepth = taskQueue.size();
                int currentWorkers = activeWorkers.get();
                
                // Scale up if queue is getting full
                if (queueDepth > scaleUpThreshold && currentWorkers < maxWorkers) {
                    addWorker();
                }
                // Scale down if queue is mostly empty
                else if (queueDepth < scaleDownThreshold && currentWorkers > minWorkers) {
                    removeWorker();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void processTasksWorker(int workerId) {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    System.out.println(String.format("[Worker-%d] Processing %s (queue: %d, workers: %d)", 
                        workerId, task, taskQueue.size(), activeWorkers.get()));
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void shutdown() {
        running = false;
        executorService.shutdown();
    }
    
    public int getQueueDepth() {
        return taskQueue.size();
    }
    
    public int getActiveWorkers() {
        return activeWorkers.get();
    }
}

// Example 3: Load Leveler with Backpressure
// Provides feedback to producers when system is overloaded
class BackpressureLoadLeveler {
    private final BlockingQueue<Task> taskQueue;
    private final ExecutorService workers;
    private final AtomicInteger rejectedCount;
    private final int warningThreshold;
    private final int criticalThreshold;
    
    public enum LoadLevel {
        NORMAL, WARNING, CRITICAL, OVERLOAD
    }
    
    public BackpressureLoadLeveler(int queueCapacity, int workerCount) {
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = Executors.newFixedThreadPool(workerCount);
        this.rejectedCount = new AtomicInteger(0);
        this.warningThreshold = (int) (queueCapacity * 0.5);
        this.criticalThreshold = (int) (queueCapacity * 0.8);
        
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i + 1;
            workers.submit(() -> processTasksWorker(workerId));
        }
    }
    
    public LoadLevel getCurrentLoadLevel() {
        int queueDepth = taskQueue.size();
        if (queueDepth >= criticalThreshold) {
            return LoadLevel.CRITICAL;
        } else if (queueDepth >= warningThreshold) {
            return LoadLevel.WARNING;
        } else if (queueDepth == taskQueue.remainingCapacity() + queueDepth) {
            return LoadLevel.OVERLOAD;
        }
        return LoadLevel.NORMAL;
    }
    
    public boolean submitTask(Task task) {
        LoadLevel loadLevel = getCurrentLoadLevel();
        
        // Reject low priority tasks during high load
        if (loadLevel == LoadLevel.CRITICAL && task.getPriority() < 5) {
            rejectedCount.incrementAndGet();
            System.out.println(String.format("[BACKPRESSURE] Rejected low-priority %s (load: %s)", 
                task, loadLevel));
            return false;
        }
        
        boolean accepted = taskQueue.offer(task);
        if (accepted) {
            System.out.println(String.format("[SUBMIT] %s (load: %s, queue: %d)", 
                task, loadLevel, taskQueue.size()));
        } else {
            rejectedCount.incrementAndGet();
            System.out.println(String.format("[OVERLOAD] Rejected %s", task));
        }
        
        return accepted;
    }
    
    private void processTasksWorker(int workerId) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    Thread.sleep(80);
                    System.out.println(String.format("[Worker-%d] Completed %s", workerId, task.getId()));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void shutdown() {
        workers.shutdown();
    }
    
    public int getRejectedCount() {
        return rejectedCount.get();
    }
}

// Example 4: Load Leveler with Dead Letter Queue
// Failed tasks go to DLQ for retry or investigation
class DLQLoadLeveler {
    private final BlockingQueue<Task> taskQueue;
    private final BlockingQueue<Task> deadLetterQueue;
    private final ExecutorService workers;
    private final int maxRetries = 3;
    private final Map<String, Integer> retryCount;
    
    public DLQLoadLeveler(int workerCount) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.deadLetterQueue = new LinkedBlockingQueue<>();
        this.workers = Executors.newFixedThreadPool(workerCount);
        this.retryCount = new ConcurrentHashMap<>();
        
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i + 1;
            workers.submit(() -> processTasksWorker(workerId));
        }
    }
    
    public void submitTask(Task task) {
        taskQueue.offer(task);
        System.out.println("[SUBMIT] " + task);
    }
    
    private void processTasksWorker(int workerId) {
        Random random = new Random();
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    // Simulate random failures
                    boolean success = random.nextInt(100) < 70;  // 70% success rate
                    
                    if (success) {
                        System.out.println(String.format("[Worker-%d] ✓ Completed %s", workerId, task.getId()));
                        retryCount.remove(task.getId());
                    } else {
                        int retries = retryCount.getOrDefault(task.getId(), 0) + 1;
                        retryCount.put(task.getId(), retries);
                        
                        if (retries < maxRetries) {
                            taskQueue.offer(task);  // Retry
                            System.out.println(String.format("[Worker-%d] ✗ Failed %s (retry %d/%d)", 
                                workerId, task.getId(), retries, maxRetries));
                        } else {
                            deadLetterQueue.offer(task);  // Move to DLQ
                            System.out.println(String.format("[Worker-%d] ⚠ Moved %s to DLQ (max retries)", 
                                workerId, task.getId()));
                            retryCount.remove(task.getId());
                        }
                    }
                    
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void processDLQ() {
        System.out.println("\n[DLQ] Processing dead letter queue:");
        Task task;
        while ((task = deadLetterQueue.poll()) != null) {
            System.out.println("  - " + task + " (requires manual investigation)");
        }
    }
    
    public int getDLQSize() {
        return deadLetterQueue.size();
    }
    
    public void shutdown() {
        workers.shutdown();
    }
}

// Example 5: Rate-Limited Load Leveler
// Controls processing rate to protect downstream services
class RateLimitedLoadLeveler {
    private final BlockingQueue<Task> taskQueue;
    private final ExecutorService workers;
    private final Semaphore rateLimiter;
    private final ScheduledExecutorService rateLimitRefresh;
    private final int maxRequestsPerSecond;
    
    public RateLimitedLoadLeveler(int workerCount, int maxRequestsPerSecond) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workers = Executors.newFixedThreadPool(workerCount);
        this.maxRequestsPerSecond = maxRequestsPerSecond;
        this.rateLimiter = new Semaphore(maxRequestsPerSecond);
        this.rateLimitRefresh = Executors.newScheduledThreadPool(1);
        
        // Refill permits every second
        rateLimitRefresh.scheduleAtFixedRate(() -> {
            int permits = maxRequestsPerSecond - rateLimiter.availablePermits();
            if (permits > 0) {
                rateLimiter.release(permits);
            }
        }, 1, 1, TimeUnit.SECONDS);
        
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i + 1;
            workers.submit(() -> processTasksWorker(workerId));
        }
    }
    
    public void submitTask(Task task) {
        taskQueue.offer(task);
        System.out.println(String.format("[SUBMIT] %s (queue: %d)", task, taskQueue.size()));
    }
    
    private void processTasksWorker(int workerId) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    // Wait for rate limit permit
                    rateLimiter.acquire();
                    
                    long waitTime = task.getWaitTime();
                    System.out.println(String.format("[Worker-%d] Processing %s (waited %dms, permits: %d/%d)", 
                        workerId, task, waitTime, rateLimiter.availablePermits(), maxRequestsPerSecond));
                    
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void shutdown() {
        workers.shutdown();
        rateLimitRefresh.shutdown();
    }
    
    public int getQueueDepth() {
        return taskQueue.size();
    }
}

// Demo
public class QueueBasedLoadLevelingPattern {
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicLoadLeveling();
        demonstrateAutoScaling();
        demonstrateBackpressure();
        demonstrateDeadLetterQueue();
        demonstrateRateLimiting();
    }
    
    private static void demonstrateBasicLoadLeveling() throws InterruptedException {
        System.out.println("=== Basic Queue-Based Load Leveling ===\n");
        
        BasicQueueLoadLeveler leveler = new BasicQueueLoadLeveler(10, 2);
        
        // Simulate burst traffic
        System.out.println("Submitting burst of 15 tasks...");
        for (int i = 1; i <= 15; i++) {
            leveler.submitTask(new Task("T" + i, "Task " + i, 5));
        }
        
        Thread.sleep(2000);
        System.out.println(String.format("\nProcessed: %d tasks", leveler.getProcessedCount()));
        leveler.shutdown();
    }
    
    private static void demonstrateAutoScaling() throws InterruptedException {
        System.out.println("\n\n=== Auto-Scaling Load Leveler ===\n");
        
        AutoScalingLoadLeveler leveler = new AutoScalingLoadLeveler(20, 2, 5);
        
        // Gradually increase load
        System.out.println("Increasing load...");
        for (int i = 1; i <= 15; i++) {
            leveler.submitTask(new Task("T" + i, "Task " + i, 5));
            Thread.sleep(50);
        }
        
        Thread.sleep(2000);
        leveler.shutdown();
    }
    
    private static void demonstrateBackpressure() throws InterruptedException {
        System.out.println("\n\n=== Load Leveler with Backpressure ===\n");
        
        BackpressureLoadLeveler leveler = new BackpressureLoadLeveler(10, 2);
        
        // Submit mix of priority tasks
        for (int i = 1; i <= 20; i++) {
            int priority = (i % 3 == 0) ? 8 : 3;  // Some high priority
            leveler.submitTask(new Task("T" + i, "Task " + i, priority));
            Thread.sleep(20);
        }
        
        Thread.sleep(1000);
        System.out.println(String.format("\nRejected tasks: %d", leveler.getRejectedCount()));
        leveler.shutdown();
    }
    
    private static void demonstrateDeadLetterQueue() throws InterruptedException {
        System.out.println("\n\n=== Load Leveler with Dead Letter Queue ===\n");
        
        DLQLoadLeveler leveler = new DLQLoadLeveler(3);
        
        for (int i = 1; i <= 10; i++) {
            leveler.submitTask(new Task("T" + i, "Task " + i, 5));
        }
        
        Thread.sleep(3000);
        leveler.processDLQ();
        leveler.shutdown();
    }
    
    private static void demonstrateRateLimiting() throws InterruptedException {
        System.out.println("\n\n=== Rate-Limited Load Leveler ===\n");
        
        RateLimitedLoadLeveler leveler = new RateLimitedLoadLeveler(3, 5);  // Max 5 req/sec
        
        System.out.println("Submitting tasks (rate limited to 5/sec)...");
        for (int i = 1; i <= 15; i++) {
            leveler.submitTask(new Task("T" + i, "Task " + i, 5));
        }
        
        Thread.sleep(4000);
        leveler.shutdown();
    }
}
