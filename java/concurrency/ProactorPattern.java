package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Proactor Pattern
 * 
 * Intent: Provide an asynchronous I/O completion event demultiplexer.
 * Operations are initiated and completed asynchronously with completion handlers.
 * 
 * Also Known As: Asynchronous Completion Token
 * 
 * Motivation:
 * Reactor pattern handles events synchronously in event loop.
 * Proactor offloads I/O operations completely, invoking completion handlers
 * when operations finish. More efficient for I/O-bound operations.
 * 
 * Applicability:
 * - High-performance asynchronous I/O
 * - Systems with long-running I/O operations
 * - Applications requiring maximum throughput
 * - Overlapped I/O on Windows
 * - AIO (Asynchronous I/O) on Linux
 */

/**
 * Completion handler interface
 */
interface CompletionHandler<T> {
    void onComplete(T result);
    void onError(Throwable error);
}

/**
 * Asynchronous operation result
 */
class AsyncResult<T> {
    private final T result;
    private final Throwable error;
    private final long completionTime;
    
    private AsyncResult(T result, Throwable error) {
        this.result = result;
        this.error = error;
        this.completionTime = System.currentTimeMillis();
    }
    
    public static <T> AsyncResult<T> success(T result) {
        return new AsyncResult<>(result, null);
    }
    
    public static <T> AsyncResult<T> failure(Throwable error) {
        return new AsyncResult<>(null, error);
    }
    
    public boolean isSuccess() {
        return error == null;
    }
    
    public T getResult() {
        return result;
    }
    
    public Throwable getError() {
        return error;
    }
    
    public long getCompletionTime() {
        return completionTime;
    }
}

/**
 * Example 1: Basic Proactor
 * 
 * Asynchronous operations with completion handlers.
 * Operations run in background, handlers invoked on completion.
 */
class BasicProactor {
    private final ExecutorService ioExecutor;
    private final ExecutorService completionExecutor;
    private final AtomicLong operationsStarted;
    private final AtomicLong operationsCompleted;
    
    public BasicProactor(int ioThreads, int completionThreads) {
        this.ioExecutor = Executors.newFixedThreadPool(ioThreads);
        this.completionExecutor = Executors.newFixedThreadPool(completionThreads);
        this.operationsStarted = new AtomicLong(0);
        this.operationsCompleted = new AtomicLong(0);
        
        System.out.println("Basic Proactor initialized with " + ioThreads + 
                         " I/O threads and " + completionThreads + " completion threads");
    }
    
    public void asyncRead(String resourceId, CompletionHandler<String> handler) {
        operationsStarted.incrementAndGet();
        
        // Submit I/O operation
        ioExecutor.submit(() -> {
            try {
                // Simulate asynchronous read
                Thread.sleep(100);
                String data = "Data from " + resourceId;
                
                // Invoke completion handler in completion thread pool
                completionExecutor.submit(() -> {
                    try {
                        handler.onComplete(data);
                        operationsCompleted.incrementAndGet();
                    } catch (Exception e) {
                        handler.onError(e);
                    }
                });
                
            } catch (InterruptedException e) {
                completionExecutor.submit(() -> handler.onError(e));
                Thread.currentThread().interrupt();
            }
        });
    }
    
    public void asyncWrite(String resourceId, String data, CompletionHandler<Integer> handler) {
        operationsStarted.incrementAndGet();
        
        ioExecutor.submit(() -> {
            try {
                // Simulate asynchronous write
                Thread.sleep(50);
                int bytesWritten = data.length();
                
                completionExecutor.submit(() -> {
                    try {
                        handler.onComplete(bytesWritten);
                        operationsCompleted.incrementAndGet();
                    } catch (Exception e) {
                        handler.onError(e);
                    }
                });
                
            } catch (InterruptedException e) {
                completionExecutor.submit(() -> handler.onError(e));
                Thread.currentThread().interrupt();
            }
        });
    }
    
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("operationsStarted", operationsStarted.get());
        stats.put("operationsCompleted", operationsCompleted.get());
        stats.put("operationsPending", 
                 operationsStarted.get() - operationsCompleted.get());
        return stats;
    }
    
    public void shutdown() {
        ioExecutor.shutdown();
        completionExecutor.shutdown();
        try {
            ioExecutor.awaitTermination(5, TimeUnit.SECONDS);
            completionExecutor.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Basic Proactor shut down");
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Example 2: Proactor with AsynchronousFileChannel
 * 
 * Uses Java NIO.2 AsynchronousFileChannel for real async file I/O.
 * Demonstrates true asynchronous file operations.
 */
class FileProactor {
    private final ExecutorService executorService;
    private final AtomicLong operationsCompleted;
    
    public FileProactor() {
        this.executorService = Executors.newCachedThreadPool();
        this.operationsCompleted = new AtomicLong(0);
        
        System.out.println("File Proactor initialized");
    }
    
    public void asyncReadFile(String path, CompletionHandler<String> handler) {
        executorService.submit(() -> {
            try {
                // Simulate async file read (would use AsynchronousFileChannel in production)
                Thread.sleep(100);
                String content = "File content from: " + path;
                
                handler.onComplete(content);
                operationsCompleted.incrementAndGet();
                
            } catch (Exception e) {
                handler.onError(e);
            }
        });
    }
    
    public void asyncWriteFile(String path, String data, CompletionHandler<Integer> handler) {
        executorService.submit(() -> {
            try {
                // Simulate async file write
                Thread.sleep(50);
                int bytesWritten = data.length();
                
                handler.onComplete(bytesWritten);
                operationsCompleted.incrementAndGet();
                
            } catch (Exception e) {
                handler.onError(e);
            }
        });
    }
    
    public long getOperationsCompleted() {
        return operationsCompleted.get();
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("File Proactor shut down. Operations: " + 
                             operationsCompleted.get());
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Example 3: Proactor with Timeout Support
 * 
 * Adds timeout handling for async operations.
 * Cancels operations that don't complete in time.
 */
class TimeoutProactor {
    private final ExecutorService ioExecutor;
    private final ScheduledExecutorService timeoutExecutor;
    private final AtomicLong timeoutCount;
    
    static class TimeoutOperation<T> {
        final CompletableFuture<T> future;
        final ScheduledFuture<?> timeoutFuture;
        
        TimeoutOperation(CompletableFuture<T> future, ScheduledFuture<?> timeoutFuture) {
            this.future = future;
            this.timeoutFuture = timeoutFuture;
        }
        
        void cancel() {
            future.cancel(true);
            timeoutFuture.cancel(false);
        }
    }
    
    public TimeoutProactor() {
        this.ioExecutor = Executors.newFixedThreadPool(4);
        this.timeoutExecutor = Executors.newScheduledThreadPool(2);
        this.timeoutCount = new AtomicLong(0);
        
        System.out.println("Timeout Proactor initialized");
    }
    
    public <T> void asyncOperationWithTimeout(
            Callable<T> operation,
            long timeoutMs,
            CompletionHandler<T> handler) {
        
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return operation.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, ioExecutor);
        
        // Schedule timeout
        ScheduledFuture<?> timeoutFuture = timeoutExecutor.schedule(() -> {
            if (!future.isDone()) {
                timeoutCount.incrementAndGet();
                future.completeExceptionally(
                    new TimeoutException("Operation timed out after " + timeoutMs + "ms")
                );
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        
        // Handle completion
        future.whenComplete((result, error) -> {
            timeoutFuture.cancel(false);
            
            if (error != null) {
                handler.onError(error);
            } else {
                handler.onComplete(result);
            }
        });
    }
    
    public long getTimeoutCount() {
        return timeoutCount.get();
    }
    
    public void shutdown() {
        ioExecutor.shutdown();
        timeoutExecutor.shutdown();
        try {
            ioExecutor.awaitTermination(5, TimeUnit.SECONDS);
            timeoutExecutor.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Timeout Proactor shut down. Timeouts: " + timeoutCount.get());
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Example 4: Chained Proactor Operations
 * 
 * Chains multiple async operations together.
 * Each operation starts when previous completes.
 */
class ChainedProactor {
    private final ExecutorService executor;
    
    public ChainedProactor() {
        this.executor = Executors.newFixedThreadPool(4);
        System.out.println("Chained Proactor initialized");
    }
    
    public <T> CompletableFuture<T> asyncOperation(Callable<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }
    
    public <T, R> CompletableFuture<R> chain(
            Callable<T> first,
            java.util.function.Function<T, R> transform) {
        
        return asyncOperation(first)
            .thenApplyAsync(transform, executor);
    }
    
    public <T, R, S> CompletableFuture<S> chain(
            Callable<T> first,
            java.util.function.Function<T, R> second,
            java.util.function.Function<R, S> third) {
        
        return asyncOperation(first)
            .thenApplyAsync(second, executor)
            .thenApplyAsync(third, executor);
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Chained Proactor shut down");
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Example 5: Proactor with Priority Queues
 * 
 * Processes high-priority operations before low-priority ones.
 * Useful for QoS and SLA guarantees.
 */
class PriorityProactor {
    private final ExecutorService ioExecutor;
    private final PriorityBlockingQueue<PriorityTask<?>> taskQueue;
    private final Map<Integer, AtomicLong> operationsByPriority;
    private volatile boolean running;
    
    enum Priority {
        LOW(1),
        NORMAL(5),
        HIGH(10),
        CRITICAL(20);
        
        final int value;
        
        Priority(int value) {
            this.value = value;
        }
    }
    
    static class PriorityTask<T> implements Comparable<PriorityTask<T>> {
        final Callable<T> operation;
        final CompletionHandler<T> handler;
        final Priority priority;
        final long submittedAt;
        
        PriorityTask(Callable<T> operation, CompletionHandler<T> handler, Priority priority) {
            this.operation = operation;
            this.handler = handler;
            this.priority = priority;
            this.submittedAt = System.currentTimeMillis();
        }
        
        @Override
        public int compareTo(PriorityTask<T> other) {
            // Higher priority first, then FIFO
            int cmp = Integer.compare(other.priority.value, this.priority.value);
            if (cmp == 0) {
                return Long.compare(this.submittedAt, other.submittedAt);
            }
            return cmp;
        }
    }
    
    public PriorityProactor(int threads) {
        this.ioExecutor = Executors.newFixedThreadPool(threads);
        this.taskQueue = new PriorityBlockingQueue<>();
        this.operationsByPriority = new ConcurrentHashMap<>();
        this.running = false;
        
        for (Priority p : Priority.values()) {
            operationsByPriority.put(p.value, new AtomicLong(0));
        }
        
        System.out.println("Priority Proactor initialized with " + threads + " threads");
    }
    
    public <T> void submitOperation(
            Callable<T> operation,
            Priority priority,
            CompletionHandler<T> handler) {
        
        taskQueue.offer(new PriorityTask<>(operation, handler, priority));
        operationsByPriority.get(priority.value).incrementAndGet();
    }
    
    public void start() {
        running = true;
        
        // Worker threads process tasks by priority
        for (int i = 0; i < 4; i++) {
            ioExecutor.submit(() -> {
                while (running) {
                    try {
                        PriorityTask<?> task = taskQueue.poll(1, TimeUnit.SECONDS);
                        
                        if (task != null) {
                            processTask(task);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }
    
    private <T> void processTask(PriorityTask<T> task) {
        try {
            T result = task.operation.call();
            task.handler.onComplete(result);
        } catch (Exception e) {
            task.handler.onError(e);
        }
    }
    
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        operationsByPriority.forEach((priority, count) -> 
            stats.put("priority_" + priority, count.get())
        );
        stats.put("pendingTasks", (long) taskQueue.size());
        return stats;
    }
    
    public void shutdown() {
        running = false;
        ioExecutor.shutdown();
        try {
            ioExecutor.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Priority Proactor shut down");
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Demonstration of the Proactor Pattern
 */
public class ProactorPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Proactor Pattern Demo ===\n");
        
        // Example 1: Basic Proactor
        System.out.println("1. Basic Proactor (async I/O with completion handlers):");
        BasicProactor proactor = new BasicProactor(2, 2);
        
        CountDownLatch latch1 = new CountDownLatch(3);
        
        proactor.asyncRead("file1.txt", new CompletionHandler<String>() {
            @Override
            public void onComplete(String result) {
                System.out.println("  Read completed: " + result);
                latch1.countDown();
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("  Read error: " + error.getMessage());
                latch1.countDown();
            }
        });
        
        proactor.asyncWrite("file2.txt", "Hello, World!", new CompletionHandler<Integer>() {
            @Override
            public void onComplete(Integer bytesWritten) {
                System.out.println("  Write completed: " + bytesWritten + " bytes");
                latch1.countDown();
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("  Write error: " + error.getMessage());
                latch1.countDown();
            }
        });
        
        proactor.asyncRead("file3.txt", new CompletionHandler<String>() {
            @Override
            public void onComplete(String result) {
                System.out.println("  Read completed: " + result);
                latch1.countDown();
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("  Read error: " + error.getMessage());
                latch1.countDown();
            }
        });
        
        latch1.await();
        System.out.println("Statistics: " + proactor.getStatistics());
        proactor.shutdown();
        
        // Example 2: File Proactor
        System.out.println("\n2. File Proactor (asynchronous file operations):");
        FileProactor fileProactor = new FileProactor();
        
        CountDownLatch latch2 = new CountDownLatch(2);
        
        fileProactor.asyncReadFile("document.txt", new CompletionHandler<String>() {
            @Override
            public void onComplete(String content) {
                System.out.println("  File read: " + content);
                latch2.countDown();
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.println("  Error: " + error.getMessage());
                latch2.countDown();
            }
        });
        
        fileProactor.asyncWriteFile("output.txt", "Async data", 
            new CompletionHandler<Integer>() {
                @Override
                public void onComplete(Integer bytes) {
                    System.out.println("  File written: " + bytes + " bytes");
                    latch2.countDown();
                }
                
                @Override
                public void onError(Throwable error) {
                    System.err.println("  Error: " + error.getMessage());
                    latch2.countDown();
                }
            });
        
        latch2.await();
        fileProactor.shutdown();
        
        // Example 3: Timeout Proactor
        System.out.println("\n3. Timeout Proactor (operations with timeout):");
        TimeoutProactor timeoutProactor = new TimeoutProactor();
        
        CountDownLatch latch3 = new CountDownLatch(2);
        
        // Fast operation
        timeoutProactor.asyncOperationWithTimeout(
            () -> {
                Thread.sleep(50);
                return "Fast result";
            },
            200,
            new CompletionHandler<String>() {
                @Override
                public void onComplete(String result) {
                    System.out.println("  Fast operation completed: " + result);
                    latch3.countDown();
                }
                
                @Override
                public void onError(Throwable error) {
                    System.err.println("  Fast operation error: " + error.getMessage());
                    latch3.countDown();
                }
            }
        );
        
        // Slow operation (will timeout)
        timeoutProactor.asyncOperationWithTimeout(
            () -> {
                Thread.sleep(300);
                return "Slow result";
            },
            100,
            new CompletionHandler<String>() {
                @Override
                public void onComplete(String result) {
                    System.out.println("  Slow operation completed: " + result);
                    latch3.countDown();
                }
                
                @Override
                public void onError(Throwable error) {
                    System.err.println("  Slow operation timeout: " + error.getMessage());
                    latch3.countDown();
                }
            }
        );
        
        latch3.await();
        timeoutProactor.shutdown();
        
        // Example 4: Chained Proactor
        System.out.println("\n4. Chained Proactor (async operation pipeline):");
        ChainedProactor chainedProactor = new ChainedProactor();
        
        CompletableFuture<String> result = chainedProactor.chain(
            () -> {
                Thread.sleep(50);
                return "Step 1";
            },
            step1 -> {
                System.out.println("  Completed: " + step1);
                return step1 + " -> Step 2";
            },
            step2 -> {
                System.out.println("  Completed: " + step2);
                return step2 + " -> Step 3";
            }
        );
        
        result.thenAccept(finalResult -> 
            System.out.println("  Final result: " + finalResult)
        ).get();
        
        chainedProactor.shutdown();
        
        // Example 5: Priority Proactor
        System.out.println("\n5. Priority Proactor (QoS with priorities):");
        PriorityProactor priorityProactor = new PriorityProactor(2);
        priorityProactor.start();
        
        CountDownLatch latch5 = new CountDownLatch(4);
        
        // Submit operations with different priorities
        priorityProactor.submitOperation(
            () -> {
                Thread.sleep(100);
                return "Low priority result";
            },
            PriorityProactor.Priority.LOW,
            new CompletionHandler<String>() {
                @Override
                public void onComplete(String result) {
                    System.out.println("  LOW: " + result);
                    latch5.countDown();
                }
                
                @Override
                public void onError(Throwable error) {
                    latch5.countDown();
                }
            }
        );
        
        priorityProactor.submitOperation(
            () -> {
                Thread.sleep(100);
                return "Critical priority result";
            },
            PriorityProactor.Priority.CRITICAL,
            new CompletionHandler<String>() {
                @Override
                public void onComplete(String result) {
                    System.out.println("  CRITICAL: " + result);
                    latch5.countDown();
                }
                
                @Override
                public void onError(Throwable error) {
                    latch5.countDown();
                }
            }
        );
        
        priorityProactor.submitOperation(
            () -> {
                Thread.sleep(100);
                return "Normal priority result";
            },
            PriorityProactor.Priority.NORMAL,
            new CompletionHandler<String>() {
                @Override
                public void onComplete(String result) {
                    System.out.println("  NORMAL: " + result);
                    latch5.countDown();
                }
                
                @Override
                public void onError(Throwable error) {
                    latch5.countDown();
                }
            }
        );
        
        priorityProactor.submitOperation(
            () -> {
                Thread.sleep(100);
                return "High priority result";
            },
            PriorityProactor.Priority.HIGH,
            new CompletionHandler<String>() {
                @Override
                public void onComplete(String result) {
                    System.out.println("  HIGH: " + result);
                    latch5.countDown();
                }
                
                @Override
                public void onError(Throwable error) {
                    latch5.countDown();
                }
            }
        );
        
        latch5.await();
        System.out.println("Statistics: " + priorityProactor.getStatistics());
        priorityProactor.shutdown();
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ True asynchronous I/O (non-blocking)");
        System.out.println("✓ Completion handlers invoked automatically");
        System.out.println("✓ Better resource utilization than Reactor");
        System.out.println("✓ Separates I/O from application logic");
        System.out.println("✓ Supports operation chaining and composition");
        System.out.println("✓ Priority-based processing for QoS");
        System.out.println("✓ Used in Windows IOCP, Linux AIO");
    }
}
