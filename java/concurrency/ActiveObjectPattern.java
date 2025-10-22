package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ACTIVE OBJECT PATTERN
 * 
 * Decouples method execution from method invocation to enhance concurrency
 * and simplify synchronized access to objects. Methods run in their own thread.
 * 
 * Benefits:
 * - Decouples method invocation from execution
 * - Simplifies concurrent programming
 * - Transparent asynchronous execution
 * - Request queuing and scheduling
 * - Enhanced system responsiveness
 * 
 * Use Cases:
 * - GUI event handling
 * - Server request processing
 * - Asynchronous I/O operations
 * - Background task execution
 * - Message-driven architectures
 */

// Active Object interface
interface ActiveObject {
    void start();
    void stop();
    boolean isRunning();
}

// Method Request (Command pattern for active object)
interface MethodRequest<T> {
    T execute();
    String getName();
}

// Scheduler (manages execution)
class Scheduler {
    private final BlockingQueue<MethodRequest<?>> requestQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;
    
    public void start() {
        executor.submit(() -> {
            while (running) {
                try {
                    MethodRequest<?> request = requestQueue.take();
                    System.out.println("  [SCHEDULER] Executing: " + request.getName());
                    request.execute();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    public <T> Future<T> enqueue(MethodRequest<T> request) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        MethodRequest<T> wrappedRequest = new MethodRequest<T>() {
            @Override
            public T execute() {
                try {
                    T result = request.execute();
                    future.complete(result);
                    return result;
                } catch (Exception e) {
                    future.completeExceptionally(e);
                    throw e;
                }
            }
            
            @Override
            public String getName() {
                return request.getName();
            }
        };
        
        requestQueue.offer(wrappedRequest);
        return future;
    }
    
    public void stop() {
        running = false;
        executor.shutdown();
    }
}

// Example 1: Bank Account Active Object
class BankAccountActiveObject implements ActiveObject {
    private double balance;
    private final Scheduler scheduler = new Scheduler();
    private volatile boolean running = false;
    
    public BankAccountActiveObject(double initialBalance) {
        this.balance = initialBalance;
    }
    
    @Override
    public void start() {
        running = true;
        scheduler.start();
        System.out.println("✅ Bank Account Active Object started");
    }
    
    @Override
    public void stop() {
        running = false;
        scheduler.stop();
        System.out.println("🛑 Bank Account Active Object stopped");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    // Asynchronous deposit
    public Future<Double> deposit(double amount) {
        MethodRequest<Double> request = new MethodRequest<Double>() {
            @Override
            public Double execute() {
                balance += amount;
                System.out.println(Thread.currentThread().getName() + 
                    " - Deposited $" + amount + ", new balance: $" + balance);
                return balance;
            }
            
            @Override
            public String getName() {
                return "deposit($" + amount + ")";
            }
        };
        
        return scheduler.enqueue(request);
    }
    
    // Asynchronous withdrawal
    public Future<Double> withdraw(double amount) {
        MethodRequest<Double> request = new MethodRequest<Double>() {
            @Override
            public Double execute() {
                if (balance >= amount) {
                    balance -= amount;
                    System.out.println(Thread.currentThread().getName() + 
                        " - Withdrew $" + amount + ", new balance: $" + balance);
                    return balance;
                } else {
                    System.out.println(Thread.currentThread().getName() + 
                        " - Insufficient funds for withdrawal of $" + amount);
                    return balance;
                }
            }
            
            @Override
            public String getName() {
                return "withdraw($" + amount + ")";
            }
        };
        
        return scheduler.enqueue(request);
    }
    
    // Asynchronous balance query
    public Future<Double> getBalance() {
        MethodRequest<Double> request = new MethodRequest<Double>() {
            @Override
            public Double execute() {
                System.out.println(Thread.currentThread().getName() + 
                    " - Balance query: $" + balance);
                return balance;
            }
            
            @Override
            public String getName() {
                return "getBalance()";
            }
        };
        
        return scheduler.enqueue(request);
    }
}

// Example 2: Logger Active Object
class LoggerActiveObject implements ActiveObject {
    private final Scheduler scheduler = new Scheduler();
    private volatile boolean running = false;
    private final List<String> logHistory = new ArrayList<>();
    
    @Override
    public void start() {
        running = true;
        scheduler.start();
        System.out.println("✅ Logger Active Object started");
    }
    
    @Override
    public void stop() {
        running = false;
        scheduler.stop();
        System.out.println("🛑 Logger Active Object stopped");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    public Future<Void> log(String level, String message) {
        MethodRequest<Void> request = new MethodRequest<Void>() {
            @Override
            public Void execute() {
                String logEntry = String.format("[%s] %s - %s",
                    level, new java.util.Date(), message);
                logHistory.add(logEntry);
                System.out.println(logEntry);
                return null;
            }
            
            @Override
            public String getName() {
                return "log(" + level + ")";
            }
        };
        
        return scheduler.enqueue(request);
    }
    
    public Future<List<String>> getLogHistory() {
        MethodRequest<List<String>> request = new MethodRequest<List<String>>() {
            @Override
            public List<String> execute() {
                return new ArrayList<>(logHistory);
            }
            
            @Override
            public String getName() {
                return "getLogHistory()";
            }
        };
        
        return scheduler.enqueue(request);
    }
    
    // Convenience methods
    public Future<Void> info(String message) {
        return log("INFO", message);
    }
    
    public Future<Void> warn(String message) {
        return log("WARN", message);
    }
    
    public Future<Void> error(String message) {
        return log("ERROR", message);
    }
}

// Example 3: Task Processor Active Object
class TaskProcessorActiveObject implements ActiveObject {
    private final Scheduler scheduler = new Scheduler();
    private volatile boolean running = false;
    private final Map<String, String> taskResults = new ConcurrentHashMap<>();
    private final AtomicInteger taskCounter = new AtomicInteger(0);
    
    @Override
    public void start() {
        running = true;
        scheduler.start();
        System.out.println("✅ Task Processor Active Object started");
    }
    
    @Override
    public void stop() {
        running = false;
        scheduler.stop();
        System.out.println("🛑 Task Processor Active Object stopped");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    public Future<String> processTask(String taskData) {
        String taskId = "TASK-" + taskCounter.incrementAndGet();
        
        MethodRequest<String> request = new MethodRequest<String>() {
            @Override
            public String execute() {
                System.out.println(Thread.currentThread().getName() + 
                    " - Processing task: " + taskId);
                
                try {
                    // Simulate processing
                    Thread.sleep((long) (Math.random() * 500));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                String result = "Processed: " + taskData;
                taskResults.put(taskId, result);
                
                System.out.println(Thread.currentThread().getName() + 
                    " - Completed task: " + taskId);
                
                return taskId;
            }
            
            @Override
            public String getName() {
                return "processTask(" + taskId + ")";
            }
        };
        
        return scheduler.enqueue(request);
    }
    
    public Future<String> getTaskResult(String taskId) {
        MethodRequest<String> request = new MethodRequest<String>() {
            @Override
            public String execute() {
                return taskResults.get(taskId);
            }
            
            @Override
            public String getName() {
                return "getTaskResult(" + taskId + ")";
            }
        };
        
        return scheduler.enqueue(request);
    }
}

// Example 4: Counter Active Object (for comparison)
class CounterActiveObject implements ActiveObject {
    private int count = 0;
    private final Scheduler scheduler = new Scheduler();
    private volatile boolean running = false;
    
    @Override
    public void start() {
        running = true;
        scheduler.start();
        System.out.println("✅ Counter Active Object started");
    }
    
    @Override
    public void stop() {
        running = false;
        scheduler.stop();
        System.out.println("🛑 Counter Active Object stopped");
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    public Future<Integer> increment() {
        MethodRequest<Integer> request = new MethodRequest<Integer>() {
            @Override
            public Integer execute() {
                count++;
                System.out.println(Thread.currentThread().getName() + 
                    " - Incremented to: " + count);
                return count;
            }
            
            @Override
            public String getName() {
                return "increment()";
            }
        };
        
        return scheduler.enqueue(request);
    }
    
    public Future<Integer> decrement() {
        MethodRequest<Integer> request = new MethodRequest<Integer>() {
            @Override
            public Integer execute() {
                count--;
                System.out.println(Thread.currentThread().getName() + 
                    " - Decremented to: " + count);
                return count;
            }
            
            @Override
            public String getName() {
                return "decrement()";
            }
        };
        
        return scheduler.enqueue(request);
    }
    
    public Future<Integer> getCount() {
        MethodRequest<Integer> request = new MethodRequest<Integer>() {
            @Override
            public Integer execute() {
                return count;
            }
            
            @Override
            public String getName() {
                return "getCount()";
            }
        };
        
        return scheduler.enqueue(request);
    }
}

// Demo
public class ActiveObjectPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    ACTIVE OBJECT PATTERN DEMONSTRATION   ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Example 1: Bank Account Active Object
        System.out.println("1. BANK ACCOUNT ACTIVE OBJECT");
        System.out.println("──────────────────────────────");
        
        BankAccountActiveObject account = new BankAccountActiveObject(1000.0);
        account.start();
        
        // Asynchronous operations
        Future<Double> deposit1 = account.deposit(500.0);
        Future<Double> deposit2 = account.deposit(300.0);
        Future<Double> withdraw1 = account.withdraw(200.0);
        Future<Double> balance1 = account.getBalance();
        
        // Wait for results
        System.out.println("Final balance: $" + balance1.get());
        
        account.stop();
        
        // Example 2: Logger Active Object
        System.out.println("\n2. LOGGER ACTIVE OBJECT");
        System.out.println("───────────────────────");
        
        LoggerActiveObject logger = new LoggerActiveObject();
        logger.start();
        
        // Concurrent logging
        logger.info("Application started");
        logger.warn("High memory usage detected");
        logger.error("Connection timeout");
        logger.info("Processing request");
        
        Thread.sleep(500);
        
        Future<List<String>> history = logger.getLogHistory();
        System.out.println("\nLog history entries: " + history.get().size());
        
        logger.stop();
        
        // Example 3: Task Processor Active Object
        System.out.println("\n3. TASK PROCESSOR ACTIVE OBJECT");
        System.out.println("────────────────────────────────");
        
        TaskProcessorActiveObject processor = new TaskProcessorActiveObject();
        processor.start();
        
        // Submit multiple tasks
        List<Future<String>> taskFutures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Future<String> taskFuture = processor.processTask("Data-" + i);
            taskFutures.add(taskFuture);
        }
        
        // Wait for all tasks
        System.out.println("\nTask IDs:");
        for (Future<String> future : taskFutures) {
            System.out.println("  " + future.get());
        }
        
        processor.stop();
        
        // Example 4: Counter Active Object
        System.out.println("\n4. COUNTER ACTIVE OBJECT (Concurrent Operations)");
        System.out.println("─────────────────────────────────────────────────");
        
        CounterActiveObject counter = new CounterActiveObject();
        counter.start();
        
        // Multiple threads trying to increment/decrement
        List<Future<Integer>> counterFutures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            counterFutures.add(counter.increment());
        }
        
        for (int i = 0; i < 5; i++) {
            counterFutures.add(counter.decrement());
        }
        
        // Wait for all operations
        for (Future<Integer> future : counterFutures) {
            future.get();
        }
        
        Future<Integer> finalCount = counter.getCount();
        System.out.println("Final count: " + finalCount.get());
        
        counter.stop();
        
        System.out.println("\n✅ Active Object Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Decoupled method invocation from execution");
        System.out.println("  • Transparent asynchronous execution");
        System.out.println("  • Simplified concurrent access");
        System.out.println("  • Request queuing and ordering");
        System.out.println("  • No explicit locking required");
    }
}
