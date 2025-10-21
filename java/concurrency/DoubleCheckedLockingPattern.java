package concurrency;

import java.util.concurrent.locks.*;

/**
 * Double-Checked Locking Pattern
 * Reduces overhead of acquiring a lock by first testing without locking.
 */
public class DoubleCheckedLockingPattern {
    
    // Lazy-initialized singleton with double-checked locking
    static class Singleton {
        // volatile ensures visibility across threads
        private static volatile Singleton instance;
        private String data;
        
        private Singleton() {
            // Simulate expensive initialization
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.data = "Initialized at " + System.currentTimeMillis();
            System.out.println("Singleton instance created: " + data);
        }
        
        public static Singleton getInstance() {
            // First check (no locking)
            if (instance == null) {
                synchronized (Singleton.class) {
                    // Second check (with locking)
                    if (instance == null) {
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
        
        public String getData() {
            return data;
        }
    }
    
    // Resource Manager with double-checked locking
    static class ResourceManager {
        private volatile boolean initialized = false;
        private final Object lock = new Object();
        private String resource;
        
        public void ensureInitialized() {
            if (!initialized) {
                synchronized (lock) {
                    if (!initialized) {
                        System.out.println("Initializing resource...");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        resource = "Resource loaded";
                        initialized = true;
                        System.out.println("Resource initialization complete");
                    }
                }
            }
        }
        
        public String getResource() {
            ensureInitialized();
            return resource;
        }
    }
    
    // Configuration loader with double-checked locking
    static class ConfigurationLoader {
        private static volatile ConfigurationLoader instance;
        private volatile java.util.Map<String, String> config;
        private final Object configLock = new Object();
        
        private ConfigurationLoader() {}
        
        public static ConfigurationLoader getInstance() {
            if (instance == null) {
                synchronized (ConfigurationLoader.class) {
                    if (instance == null) {
                        instance = new ConfigurationLoader();
                    }
                }
            }
            return instance;
        }
        
        public java.util.Map<String, String> getConfig() {
            // Double-checked locking for config initialization
            if (config == null) {
                synchronized (configLock) {
                    if (config == null) {
                        System.out.println("Loading configuration...");
                        config = new java.util.HashMap<>();
                        config.put("db.host", "localhost");
                        config.put("db.port", "5432");
                        config.put("cache.enabled", "true");
                        System.out.println("Configuration loaded");
                    }
                }
            }
            return config;
        }
    }
    
    // Connection Pool with double-checked locking
    static class ConnectionPool {
        private static volatile ConnectionPool instance;
        private volatile java.util.List<String> connections;
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        
        private ConnectionPool() {}
        
        public static ConnectionPool getInstance() {
            if (instance == null) {
                synchronized (ConnectionPool.class) {
                    if (instance == null) {
                        instance = new ConnectionPool();
                    }
                }
            }
            return instance;
        }
        
        public void initializeConnections(int size) {
            // Read lock first (cheaper)
            rwLock.readLock().lock();
            try {
                if (connections != null) {
                    return; // Already initialized
                }
            } finally {
                rwLock.readLock().unlock();
            }
            
            // Upgrade to write lock
            rwLock.writeLock().lock();
            try {
                // Double check after acquiring write lock
                if (connections == null) {
                    System.out.println("Initializing " + size + " connections...");
                    connections = new java.util.ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        connections.add("Connection-" + i);
                    }
                    System.out.println("Connections initialized");
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        
        public java.util.List<String> getConnections() {
            rwLock.readLock().lock();
            try {
                return connections != null ? new java.util.ArrayList<>(connections) : null;
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Double-Checked Locking Pattern Demo ===\n");
        
        // 1. Singleton with multiple threads
        System.out.println("1. Thread-Safe Singleton:");
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                System.out.println("Thread " + threadId + " requesting instance...");
                Singleton singleton = Singleton.getInstance();
                System.out.println("Thread " + threadId + " got instance: " + 
                                 singleton.getData());
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 2. Resource Manager
        System.out.println("2. Resource Manager:");
        ResourceManager manager = new ResourceManager();
        
        Thread[] resourceThreads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            resourceThreads[i] = new Thread(() -> {
                System.out.println("Thread " + threadId + " accessing resource...");
                String resource = manager.getResource();
                System.out.println("Thread " + threadId + " got: " + resource);
            });
            resourceThreads[i].start();
        }
        
        for (Thread thread : resourceThreads) {
            thread.join();
        }
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 3. Configuration Loader
        System.out.println("3. Configuration Loader:");
        ConfigurationLoader loader = ConfigurationLoader.getInstance();
        
        Thread[] configThreads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            configThreads[i] = new Thread(() -> {
                System.out.println("Thread " + threadId + " loading config...");
                java.util.Map<String, String> config = loader.getConfig();
                System.out.println("Thread " + threadId + " got config: " + config);
            });
            configThreads[i].start();
        }
        
        for (Thread thread : configThreads) {
            thread.join();
        }
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 4. Connection Pool
        System.out.println("4. Connection Pool:");
        ConnectionPool pool = ConnectionPool.getInstance();
        
        Thread[] poolThreads = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int threadId = i;
            poolThreads[i] = new Thread(() -> {
                System.out.println("Thread " + threadId + " initializing pool...");
                pool.initializeConnections(5);
                java.util.List<String> connections = pool.getConnections();
                System.out.println("Thread " + threadId + " sees " + 
                                 connections.size() + " connections");
            });
            poolThreads[i].start();
        }
        
        for (Thread thread : poolThreads) {
            thread.join();
        }
        
        System.out.println("\n--- Why Use Double-Checked Locking? ---");
        System.out.println("❌ Without DCL: Every access requires locking (expensive)");
        System.out.println("✅ With DCL: Only first access requires locking");
        System.out.println("   - First check: No lock (fast path)");
        System.out.println("   - Second check: With lock (safe initialization)");
        System.out.println("   - Subsequent accesses: No locking needed");
        
        System.out.println("\n--- Critical Points ---");
        System.out.println("⚠️  Must use 'volatile' keyword");
        System.out.println("   - Ensures visibility across threads");
        System.out.println("   - Prevents instruction reordering");
        System.out.println("⚠️  Java 5+ required for proper volatile semantics");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Lazy initialization");
        System.out.println("✓ Thread-safe");
        System.out.println("✓ Low synchronization overhead");
        System.out.println("✓ Good performance");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Singleton pattern");
        System.out.println("• Lazy resource initialization");
        System.out.println("• Configuration loading");
        System.out.println("• Connection pool setup");
    }
}
