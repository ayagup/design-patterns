package additional;

import java.util.*;
import java.io.*;

/**
 * Execute Around Pattern
 * 
 * Intent: Surrounds an operation with setup and cleanup code,
 * ensuring resources are properly managed automatically.
 * 
 * Motivation:
 * Ensures cleanup code always runs.
 * Reduces boilerplate code.
 * Prevents resource leaks.
 * Centralizes setup/cleanup logic.
 * 
 * Applicability:
 * - Resource management (files, connections)
 * - Transaction handling
 * - Logging/timing operations
 * - Thread synchronization
 */

/**
 * Example 1: File Operations
 * 
 * Automatically handles file opening and closing
 */
@FunctionalInterface
interface FileOperation<T> {
    T execute(BufferedReader reader) throws IOException;
}

class FileOperationExecutor {
    public <T> T executeFileOperation(String filename, FileOperation<T> operation) {
        System.out.println("  [FileOp] Opening file: " + filename);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            T result = operation.execute(reader);
            System.out.println("  [FileOp] Operation completed successfully");
            return result;
        } catch (IOException e) {
            System.out.println("  [FileOp] Error: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            System.out.println("  [FileOp] File closed automatically");
        }
    }
}

/**
 * Example 2: Database Transaction
 * 
 * Wraps operations in transaction with auto-commit/rollback
 */
@FunctionalInterface
interface TransactionCallback<T> {
    T execute() throws Exception;
}

class TransactionExecutor {
    private boolean inTransaction = false;
    
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        System.out.println("  [Transaction] BEGIN");
        inTransaction = true;
        
        try {
            T result = callback.execute();
            System.out.println("  [Transaction] COMMIT");
            return result;
        } catch (Exception e) {
            System.out.println("  [Transaction] ROLLBACK - Error: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            inTransaction = false;
            System.out.println("  [Transaction] END");
        }
    }
    
    public boolean isInTransaction() {
        return inTransaction;
    }
}

/**
 * Example 3: Lock Management
 * 
 * Automatically acquires and releases locks
 */
@FunctionalInterface
interface LockOperation<T> {
    T execute();
}

class LockExecutor {
    private final Object lock = new Object();
    
    public <T> T executeWithLock(LockOperation<T> operation) {
        System.out.println("  [Lock] Acquiring lock...");
        
        synchronized (lock) {
            System.out.println("  [Lock] Lock acquired");
            try {
                T result = operation.execute();
                System.out.println("  [Lock] Operation completed");
                return result;
            } finally {
                System.out.println("  [Lock] Lock released");
            }
        }
    }
}

/**
 * Example 4: Performance Timing
 * 
 * Times operation execution automatically
 */
@FunctionalInterface
interface TimedOperation<T> {
    T execute();
}

class TimingExecutor {
    public <T> T executeWithTiming(String operationName, TimedOperation<T> operation) {
        long startTime = System.nanoTime();
        System.out.println("  [Timer] Starting: " + operationName);
        
        try {
            T result = operation.execute();
            return result;
        } finally {
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            System.out.println("  [Timer] Completed: " + operationName + 
                             " in " + durationMs + "ms");
        }
    }
}

/**
 * Example 5: Logging Wrapper
 * 
 * Adds logging around operations
 */
@FunctionalInterface
interface LoggedOperation<T> {
    T execute() throws Exception;
}

class LoggingExecutor {
    public <T> T executeWithLogging(String operationName, LoggedOperation<T> operation) {
        System.out.println("  [Log] ENTER: " + operationName);
        
        try {
            T result = operation.execute();
            System.out.println("  [Log] SUCCESS: " + operationName + " -> " + result);
            return result;
        } catch (Exception e) {
            System.out.println("  [Log] ERROR: " + operationName + " - " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            System.out.println("  [Log] EXIT: " + operationName);
        }
    }
}

/**
 * Example 6: Connection Pool
 * 
 * Manages connection acquisition and release
 */
interface Connection {
    void execute(String query);
    void close();
}

class MockConnection implements Connection {
    private final int id;
    
    public MockConnection(int id) {
        this.id = id;
    }
    
    @Override
    public void execute(String query) {
        System.out.println("  [Connection-" + id + "] Executing: " + query);
    }
    
    @Override
    public void close() {
        System.out.println("  [Connection-" + id + "] Closed");
    }
}

@FunctionalInterface
interface ConnectionOperation<T> {
    T execute(Connection connection);
}

class ConnectionPool {
    private final Queue<Connection> pool = new LinkedList<>();
    private int nextId = 1;
    
    public ConnectionPool(int size) {
        for (int i = 0; i < size; i++) {
            pool.offer(new MockConnection(nextId++));
        }
        System.out.println("  [Pool] Initialized with " + size + " connections");
    }
    
    public <T> T executeWithConnection(ConnectionOperation<T> operation) {
        Connection conn = pool.poll();
        if (conn == null) {
            System.out.println("  [Pool] No available connections!");
            throw new RuntimeException("Connection pool exhausted");
        }
        
        System.out.println("  [Pool] Connection acquired");
        try {
            return operation.execute(conn);
        } finally {
            pool.offer(conn);
            System.out.println("  [Pool] Connection returned to pool");
        }
    }
    
    public int getAvailableConnections() {
        return pool.size();
    }
}

/**
 * Example 7: Resource Cleanup
 * 
 * Generic resource management
 */
@FunctionalInterface
interface ResourceOperation<R, T> {
    T execute(R resource) throws Exception;
}

class ResourceExecutor<R extends AutoCloseable> {
    private final R resource;
    
    public ResourceExecutor(R resource) {
        this.resource = resource;
    }
    
    public <T> T executeWithResource(ResourceOperation<R, T> operation) {
        System.out.println("  [Resource] Acquiring: " + resource.getClass().getSimpleName());
        
        try {
            T result = operation.execute(resource);
            System.out.println("  [Resource] Operation successful");
            return result;
        } catch (Exception e) {
            System.out.println("  [Resource] Operation failed: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                resource.close();
                System.out.println("  [Resource] Closed automatically");
            } catch (Exception e) {
                System.out.println("  [Resource] Error closing: " + e.getMessage());
            }
        }
    }
}

/**
 * Example 8: Context Manager
 * 
 * Sets up and tears down execution context
 */
@FunctionalInterface
interface ContextOperation<T> {
    T execute();
}

class ExecutionContext {
    private final Map<String, Object> variables = new HashMap<>();
    
    public void set(String key, Object value) {
        variables.put(key, value);
    }
    
    public Object get(String key) {
        return variables.get(key);
    }
    
    public void clear() {
        variables.clear();
    }
}

class ContextManager {
    private static final ThreadLocal<ExecutionContext> context = new ThreadLocal<>();
    
    public static <T> T executeInContext(ContextOperation<T> operation) {
        ExecutionContext ctx = new ExecutionContext();
        context.set(ctx);
        
        System.out.println("  [Context] Initialized");
        
        try {
            ctx.set("startTime", System.currentTimeMillis());
            T result = operation.execute();
            System.out.println("  [Context] Operation completed");
            return result;
        } finally {
            context.remove();
            System.out.println("  [Context] Cleaned up");
        }
    }
    
    public static ExecutionContext getContext() {
        return context.get();
    }
}

/**
 * Demonstration of the Execute Around Pattern
 */
public class ExecuteAroundPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Execute Around Pattern Demo ===\n");
        
        // Example 1: File Operations (simulated)
        System.out.println("1. File Operations:");
        System.out.println("  [Demo] Simulating file operation...");
        System.out.println("  [FileOp] Opening file: data.txt");
        System.out.println("  [FileOp] Reading lines...");
        System.out.println("  [FileOp] Operation completed successfully");
        System.out.println("  [FileOp] File closed automatically");
        
        // Example 2: Database Transaction
        System.out.println("\n2. Database Transaction:");
        TransactionExecutor txExecutor = new TransactionExecutor();
        
        String result = txExecutor.executeInTransaction(() -> {
            System.out.println("  [Business] Updating account balance...");
            System.out.println("  [Business] Recording transaction...");
            return "Transaction successful";
        });
        System.out.println("  [Main] Result: " + result);
        
        // Example 3: Lock Management
        System.out.println("\n3. Lock Management:");
        LockExecutor lockExecutor = new LockExecutor();
        
        Integer value = lockExecutor.executeWithLock(() -> {
            System.out.println("  [Critical] Modifying shared resource...");
            return 42;
        });
        System.out.println("  [Main] Value: " + value);
        
        // Example 4: Performance Timing
        System.out.println("\n4. Performance Timing:");
        TimingExecutor timer = new TimingExecutor();
        
        String timedResult = timer.executeWithTiming("Data Processing", () -> {
            try {
                Thread.sleep(100); // Simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Processed 1000 records";
        });
        System.out.println("  [Main] " + timedResult);
        
        // Example 5: Logging Wrapper
        System.out.println("\n5. Logging Wrapper:");
        LoggingExecutor logger = new LoggingExecutor();
        
        Double calculation = logger.executeWithLogging("Calculate Total", () -> {
            double sum = 100.0 + 200.0 + 300.0;
            return sum;
        });
        
        // Example 6: Connection Pool
        System.out.println("\n6. Connection Pool:");
        ConnectionPool pool = new ConnectionPool(2);
        
        String queryResult = pool.executeWithConnection(conn -> {
            conn.execute("SELECT * FROM users");
            return "Query executed";
        });
        System.out.println("  [Main] " + queryResult);
        System.out.println("  [Main] Available connections: " + pool.getAvailableConnections());
        
        // Example 7: Context Manager
        System.out.println("\n7. Context Manager:");
        String contextResult = ContextManager.executeInContext(() -> {
            ExecutionContext ctx = ContextManager.getContext();
            System.out.println("  [Business] Context available: " + (ctx != null));
            System.out.println("  [Business] Performing operation...");
            return "Context operation complete";
        });
        System.out.println("  [Main] " + contextResult);
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Guaranteed cleanup execution");
        System.out.println("✓ Reduces boilerplate code");
        System.out.println("✓ Prevents resource leaks");
        System.out.println("✓ Centralizes setup/teardown");
        System.out.println("✓ Exception-safe operations");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• File I/O operations");
        System.out.println("• Database transactions");
        System.out.println("• Lock management");
        System.out.println("• Connection pooling");
        System.out.println("• Performance monitoring");
    }
}
