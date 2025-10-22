package additional;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * RAII Pattern (Resource Acquisition Is Initialization)
 * ======================================================
 * 
 * Intent:
 * Binds resource lifecycle to object lifetime - resources are acquired in
 * constructor and released in destructor/cleanup. Ensures resources are
 * always properly released, even when exceptions occur.
 * 
 * Also Known As:
 * - Scope-Bound Resource Management
 * - Constructor Acquires, Destructor Releases (CADRe)
 * 
 * Motivation:
 * - Prevent resource leaks (files, locks, connections, memory)
 * - Guarantee cleanup even in exceptional circumstances
 * - Make resource management automatic and foolproof
 * - Reduce boilerplate try-finally blocks
 * 
 * Applicability:
 * - Managing files, network connections, database connections
 * - Managing locks and synchronization primitives
 * - Managing native resources or external system resources
 * - Any scenario where cleanup must be guaranteed
 * 
 * Structure:
 * Resource wrapper with:
 * - Constructor: Acquires resource
 * - AutoCloseable.close(): Releases resource
 * - try-with-resources: Automatic cleanup
 * 
 * Participants:
 * - Resource: The actual resource (file, connection, lock)
 * - RAII Wrapper: Object that manages resource lifecycle
 * 
 * Implementation Considerations:
 * 1. Implement AutoCloseable for try-with-resources support
 * 2. Acquire resource in constructor
 * 3. Release resource in close() method
 * 4. close() should be idempotent (safe to call multiple times)
 * 5. close() should never throw (or handle exceptions internally)
 */

// Example 1: File Handle with RAII
// Automatically opens and closes files
class FileHandle implements AutoCloseable {
    private BufferedReader reader;
    private String filename;
    private boolean closed = false;
    
    // Acquire resource in constructor
    public FileHandle(String filename) throws IOException {
        this.filename = filename;
        this.reader = new BufferedReader(new FileReader(filename));
        System.out.println("File opened: " + filename);
    }
    
    public String readLine() throws IOException {
        if (closed) {
            throw new IllegalStateException("File is closed");
        }
        return reader.readLine();
    }
    
    public List<String> readAllLines() throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }
    
    // Release resource - guaranteed to be called by try-with-resources
    @Override
    public void close() {
        if (!closed) {
            try {
                reader.close();
                System.out.println("File closed: " + filename);
            } catch (IOException e) {
                System.err.println("Error closing file: " + e.getMessage());
            }
            closed = true;
        }
    }
}

// Example 2: Lock Guard (like C++ std::lock_guard)
// Automatically acquires and releases locks
class LockGuard implements AutoCloseable {
    private final Lock lock;
    private boolean locked = false;
    
    // Acquire lock in constructor
    public LockGuard(Lock lock) {
        this.lock = lock;
        lock.lock();
        locked = true;
        System.out.println("Lock acquired");
    }
    
    // Release lock automatically
    @Override
    public void close() {
        if (locked) {
            lock.unlock();
            locked = false;
            System.out.println("Lock released");
        }
    }
}

// Example 3: Database Transaction
// Automatically begins transaction and commits/rolls back
class TransactionScope implements AutoCloseable {
    private final Connection connection;
    private boolean committed = false;
    private boolean rolledBack = false;
    
    // Begin transaction in constructor
    public TransactionScope(Connection connection) {
        this.connection = connection;
        connection.beginTransaction();
        System.out.println("Transaction started");
    }
    
    public void commit() {
        if (!committed && !rolledBack) {
            connection.commit();
            committed = true;
            System.out.println("Transaction committed");
        }
    }
    
    // Rollback if not committed - automatic cleanup
    @Override
    public void close() {
        if (!committed && !rolledBack) {
            connection.rollback();
            rolledBack = true;
            System.out.println("Transaction rolled back (auto cleanup)");
        }
    }
}

// Simple Connection class for demonstration
class Connection {
    private boolean inTransaction = false;
    
    void beginTransaction() {
        inTransaction = true;
    }
    
    void commit() {
        inTransaction = false;
    }
    
    void rollback() {
        inTransaction = false;
    }
    
    void execute(String sql) {
        System.out.println("Executing SQL: " + sql);
    }
}

// Example 4: Temporary Directory
// Creates temp directory and deletes it automatically
class TemporaryDirectory implements AutoCloseable {
    private final File directory;
    
    // Create directory in constructor
    public TemporaryDirectory(String prefix) {
        this.directory = new File(System.getProperty("java.io.tmpdir"), 
                                 prefix + "_" + System.currentTimeMillis());
        directory.mkdirs();
        System.out.println("Temporary directory created: " + directory.getAbsolutePath());
    }
    
    public File getDirectory() {
        return directory;
    }
    
    public File createFile(String filename) throws IOException {
        File file = new File(directory, filename);
        file.createNewFile();
        System.out.println("Created file: " + file.getName());
        return file;
    }
    
    // Delete directory and contents automatically
    @Override
    public void close() {
        deleteRecursively(directory);
        System.out.println("Temporary directory deleted");
    }
    
    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}

// Example 5: Timer/Profiler
// Measures execution time automatically
class ScopedTimer implements AutoCloseable {
    private final String operationName;
    private final long startTime;
    
    // Start timer in constructor
    public ScopedTimer(String operationName) {
        this.operationName = operationName;
        this.startTime = System.nanoTime();
        System.out.println("Started: " + operationName);
    }
    
    // Report elapsed time automatically
    @Override
    public void close() {
        long elapsedNanos = System.nanoTime() - startTime;
        double elapsedMillis = elapsedNanos / 1_000_000.0;
        System.out.println("Finished: " + operationName + 
                         " (took " + String.format("%.2f", elapsedMillis) + " ms)");
    }
}

// Example 6: Thread Context Manager
// Sets and restores thread context automatically
class ThreadContextScope implements AutoCloseable {
    private final String contextKey;
    private final Object oldValue;
    private static final ThreadLocal<Map<String, Object>> threadContext = 
        ThreadLocal.withInitial(HashMap::new);
    
    // Save old context and set new value
    public ThreadContextScope(String key, Object value) {
        this.contextKey = key;
        Map<String, Object> context = threadContext.get();
        this.oldValue = context.get(key);
        context.put(key, value);
        System.out.println("Context set: " + key + " = " + value);
    }
    
    public static Object get(String key) {
        return threadContext.get().get(key);
    }
    
    // Restore old context automatically
    @Override
    public void close() {
        Map<String, Object> context = threadContext.get();
        if (oldValue != null) {
            context.put(contextKey, oldValue);
            System.out.println("Context restored: " + contextKey + " = " + oldValue);
        } else {
            context.remove(contextKey);
            System.out.println("Context cleared: " + contextKey);
        }
    }
}

// Example 7: Network Connection Pool
// Acquires connection from pool and returns it automatically
class PooledConnection implements AutoCloseable {
    private final ConnectionPool pool;
    private final NetworkConnection connection;
    
    // Acquire connection from pool
    PooledConnection(ConnectionPool pool, NetworkConnection connection) {
        this.pool = pool;
        this.connection = connection;
        System.out.println("Connection acquired from pool");
    }
    
    public void send(String data) {
        connection.send(data);
    }
    
    public String receive() {
        return connection.receive();
    }
    
    // Return connection to pool automatically
    @Override
    public void close() {
        pool.releaseConnection(connection);
        System.out.println("Connection returned to pool");
    }
}

class ConnectionPool {
    private final Queue<NetworkConnection> availableConnections = new LinkedList<>();
    private final Set<NetworkConnection> activeConnections = new HashSet<>();
    
    public ConnectionPool(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            availableConnections.offer(new NetworkConnection("conn-" + i));
        }
    }
    
    public PooledConnection acquire() {
        NetworkConnection conn = availableConnections.poll();
        if (conn == null) {
            throw new IllegalStateException("No connections available");
        }
        activeConnections.add(conn);
        return new PooledConnection(this, conn);
    }
    
    void releaseConnection(NetworkConnection conn) {
        activeConnections.remove(conn);
        availableConnections.offer(conn);
    }
}

class NetworkConnection {
    private String id;
    
    NetworkConnection(String id) {
        this.id = id;
    }
    
    void send(String data) {
        System.out.println("[" + id + "] Sending: " + data);
    }
    
    String receive() {
        return "Response from " + id;
    }
}

// Example 8: State Saver/Restorer
// Saves state and restores it automatically
class StateSaver<T> implements AutoCloseable {
    private final StateHolder<T> holder;
    private final T savedState;
    
    // Save current state
    public StateSaver(StateHolder<T> holder) {
        this.holder = holder;
        this.savedState = holder.getState();
        System.out.println("State saved: " + savedState);
    }
    
    // Restore state automatically
    @Override
    public void close() {
        holder.setState(savedState);
        System.out.println("State restored: " + savedState);
    }
}

class StateHolder<T> {
    private T state;
    
    public StateHolder(T initialState) {
        this.state = initialState;
    }
    
    public T getState() {
        return state;
    }
    
    public void setState(T state) {
        this.state = state;
    }
}

// Example 9: Resource Bundle
// Manages multiple resources as a single unit
class ResourceBundle implements AutoCloseable {
    private final List<AutoCloseable> resources = new ArrayList<>();
    private final String bundleName;
    
    public ResourceBundle(String bundleName) {
        this.bundleName = bundleName;
        System.out.println("Resource bundle created: " + bundleName);
    }
    
    public void addResource(AutoCloseable resource) {
        resources.add(resource);
    }
    
    // Close all resources in reverse order
    @Override
    public void close() {
        System.out.println("Closing resource bundle: " + bundleName);
        
        // Close in reverse order (LIFO)
        for (int i = resources.size() - 1; i >= 0; i--) {
            try {
                resources.get(i).close();
            } catch (Exception e) {
                System.err.println("Error closing resource: " + e.getMessage());
            }
        }
    }
}

/**
 * Demonstration of RAII Pattern
 */
public class RAIIPattern {
    public static void main(String[] args) {
        demonstrateFileHandle();
        demonstrateLockGuard();
        demonstrateTransaction();
        demonstrateTemporaryDirectory();
        demonstrateScopedTimer();
        demonstrateThreadContext();
        demonstrateConnectionPool();
        demonstrateStateSaver();
        demonstrateResourceBundle();
    }
    
    private static void demonstrateFileHandle() {
        System.out.println("=== File Handle (RAII) ===\n");
        
        // Create a temporary file for demonstration
        try {
            File tempFile = File.createTempFile("demo", ".txt");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write("Line 1\n");
                writer.write("Line 2\n");
                writer.write("Line 3\n");
            }
            
            // File automatically closed even if exception occurs
            try (FileHandle file = new FileHandle(tempFile.getAbsolutePath())) {
                List<String> lines = file.readAllLines();
                System.out.println("Read " + lines.size() + " lines");
            } // File automatically closed here
            
            tempFile.delete();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void demonstrateLockGuard() {
        System.out.println("=== Lock Guard (RAII) ===\n");
        
        Lock lock = new ReentrantLock();
        
        // Lock automatically acquired and released
        try (LockGuard guard = new LockGuard(lock)) {
            System.out.println("Critical section - lock is held");
            // Do work while holding lock
        } // Lock automatically released here, even if exception occurs
        
        System.out.println();
    }
    
    private static void demonstrateTransaction() {
        System.out.println("=== Transaction Scope (RAII) ===\n");
        
        Connection conn = new Connection();
        
        // Example 1: Successful commit
        try (TransactionScope tx = new TransactionScope(conn)) {
            conn.execute("INSERT INTO users VALUES (...)");
            conn.execute("UPDATE accounts SET ...");
            tx.commit();
        } // No rollback - transaction was committed
        
        System.out.println();
        
        // Example 2: Automatic rollback on exception
        try (TransactionScope tx = new TransactionScope(conn)) {
            conn.execute("INSERT INTO orders VALUES (...)");
            
            if (Math.random() > 0.5) {
                throw new RuntimeException("Simulated error");
            }
            
            tx.commit();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.getMessage());
        } // Transaction automatically rolled back
        
        System.out.println();
    }
    
    private static void demonstrateTemporaryDirectory() {
        System.out.println("=== Temporary Directory (RAII) ===\n");
        
        try (TemporaryDirectory tempDir = new TemporaryDirectory("myapp")) {
            tempDir.createFile("data.txt");
            tempDir.createFile("config.ini");
            
            System.out.println("Working with temporary files...");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } // Directory and all files automatically deleted
        
        System.out.println();
    }
    
    private static void demonstrateScopedTimer() {
        System.out.println("=== Scoped Timer (RAII) ===\n");
        
        try (ScopedTimer timer = new ScopedTimer("Database Query")) {
            // Simulate some work
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } // Elapsed time automatically reported
        
        System.out.println();
        
        try (ScopedTimer timer = new ScopedTimer("Image Processing")) {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    private static void demonstrateThreadContext() {
        System.out.println("=== Thread Context (RAII) ===\n");
        
        try (ThreadContextScope ctx = new ThreadContextScope("userId", "12345")) {
            System.out.println("Current userId: " + ThreadContextScope.get("userId"));
            
            // Nested context
            try (ThreadContextScope ctx2 = new ThreadContextScope("userId", "67890")) {
                System.out.println("Current userId: " + ThreadContextScope.get("userId"));
            } // Inner context restored
            
            System.out.println("Current userId: " + ThreadContextScope.get("userId"));
        } // Outer context restored
        
        System.out.println();
    }
    
    private static void demonstrateConnectionPool() {
        System.out.println("=== Connection Pool (RAII) ===\n");
        
        ConnectionPool pool = new ConnectionPool(3);
        
        try (PooledConnection conn = pool.acquire()) {
            conn.send("Hello");
            String response = conn.receive();
            System.out.println("Received: " + response);
        } // Connection automatically returned to pool
        
        System.out.println();
    }
    
    private static void demonstrateStateSaver() {
        System.out.println("=== State Saver (RAII) ===\n");
        
        StateHolder<String> holder = new StateHolder<>("Initial");
        System.out.println("State: " + holder.getState());
        
        try (StateSaver<String> saver = new StateSaver<>(holder)) {
            holder.setState("Modified");
            System.out.println("State: " + holder.getState());
            
            // Nested state saving
            try (StateSaver<String> saver2 = new StateSaver<>(holder)) {
                holder.setState("Further Modified");
                System.out.println("State: " + holder.getState());
            } // State restored to "Modified"
            
            System.out.println("State: " + holder.getState());
        } // State restored to "Initial"
        
        System.out.println("State: " + holder.getState());
        System.out.println();
    }
    
    private static void demonstrateResourceBundle() {
        System.out.println("=== Resource Bundle (RAII) ===\n");
        
        Lock lock = new ReentrantLock();
        Connection conn = new Connection();
        
        try (ResourceBundle bundle = new ResourceBundle("MyOperation")) {
            bundle.addResource(new LockGuard(lock));
            bundle.addResource(new TransactionScope(conn));
            bundle.addResource(new ScopedTimer("Bundle Operation"));
            
            System.out.println("All resources acquired, performing operation...");
            Thread.sleep(50);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } // All resources closed in reverse order
        
        System.out.println();
    }
}
