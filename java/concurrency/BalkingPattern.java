package concurrency;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * BALKING PATTERN
 * 
 * Objects execute an action only when they are in a particular state. If an object is
 * not in the appropriate state, the action is not executed (the object "balks").
 * 
 * Benefits:
 * - Prevents redundant operations
 * - Thread-safe state checking
 * - Avoids blocking when operation can't proceed
 * - Simple error handling (fail fast)
 * - Efficient resource usage
 * 
 * Use Cases:
 * - Resource initialization (initialize only once)
 * - File operations (save only if modified)
 * - Connection management (reconnect only if disconnected)
 * - Cache operations (refresh only if stale)
 * - Background task scheduling
 */

// Example 1: Water Heater with Auto-shutoff
class WaterHeater {
    private enum State { OFF, HEATING, READY }
    
    private State state = State.OFF;
    private int temperature = 20; // Celsius
    private final int targetTemperature = 80;
    private final Lock lock = new ReentrantLock();
    
    public void turnOn() {
        lock.lock();
        try {
            if (state != State.OFF) {
                System.out.println("  ⚠️  Water heater already " + state + " - balking");
                return; // Balk - already on
            }
            
            System.out.println("  🔥 Turning on water heater...");
            state = State.HEATING;
            
            // Simulate heating in background
            new Thread(() -> {
                while (temperature < targetTemperature && state == State.HEATING) {
                    try {
                        Thread.sleep(100);
                        temperature += 5;
                        System.out.println("     Temperature: " + temperature + "°C");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                lock.lock();
                try {
                    if (state == State.HEATING) {
                        state = State.READY;
                        System.out.println("  ✅ Water heater READY at " + temperature + "°C");
                    }
                } finally {
                    lock.unlock();
                }
            }).start();
            
        } finally {
            lock.unlock();
        }
    }
    
    public void turnOff() {
        lock.lock();
        try {
            if (state == State.OFF) {
                System.out.println("  ⚠️  Water heater already OFF - balking");
                return; // Balk - already off
            }
            
            System.out.println("  ❌ Turning off water heater...");
            state = State.OFF;
            
        } finally {
            lock.unlock();
        }
    }
    
    public State getState() {
        lock.lock();
        try {
            return state;
        } finally {
            lock.unlock();
        }
    }
}

// Example 2: Document Auto-saver
class Document {
    private StringBuilder content = new StringBuilder();
    private boolean modified = false;
    private boolean saving = false;
    private final Lock lock = new ReentrantLock();
    private final String filename;
    
    public Document(String filename) {
        this.filename = filename;
    }
    
    public void write(String text) {
        lock.lock();
        try {
            content.append(text);
            modified = true;
            System.out.println("  ✏️  Written: " + text);
        } finally {
            lock.unlock();
        }
    }
    
    public void save() {
        lock.lock();
        try {
            if (!modified) {
                System.out.println("  ⚠️  No changes to save - balking");
                return; // Balk - no changes
            }
            
            if (saving) {
                System.out.println("  ⚠️  Save already in progress - balking");
                return; // Balk - already saving
            }
            
            System.out.println("  💾 Saving document to " + filename + "...");
            saving = true;
            modified = false;
            
            // Simulate file I/O
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    System.out.println("  ✅ Document saved successfully");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.lock();
                    try {
                        saving = false;
                    } finally {
                        lock.unlock();
                    }
                }
            }).start();
            
        } finally {
            lock.unlock();
        }
    }
    
    public String getContent() {
        lock.lock();
        try {
            return content.toString();
        } finally {
            lock.unlock();
        }
    }
}

// Example 3: Database Connection
class DatabaseConnection {
    private enum ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }
    
    private ConnectionState state = ConnectionState.DISCONNECTED;
    private final Lock lock = new ReentrantLock();
    private final String connectionString;
    
    public DatabaseConnection(String connectionString) {
        this.connectionString = connectionString;
    }
    
    public void connect() {
        lock.lock();
        try {
            if (state == ConnectionState.CONNECTED) {
                System.out.println("  ⚠️  Already connected - balking");
                return; // Balk - already connected
            }
            
            if (state == ConnectionState.CONNECTING) {
                System.out.println("  ⚠️  Connection in progress - balking");
                return; // Balk - connection in progress
            }
            
            System.out.println("  🔌 Connecting to database: " + connectionString);
            state = ConnectionState.CONNECTING;
            
            // Simulate connection establishment
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    lock.lock();
                    try {
                        state = ConnectionState.CONNECTED;
                        System.out.println("  ✅ Database connected");
                    } finally {
                        lock.unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } finally {
            lock.unlock();
        }
    }
    
    public void disconnect() {
        lock.lock();
        try {
            if (state == ConnectionState.DISCONNECTED) {
                System.out.println("  ⚠️  Already disconnected - balking");
                return; // Balk - already disconnected
            }
            
            System.out.println("  🔌 Disconnecting from database...");
            state = ConnectionState.DISCONNECTED;
            System.out.println("  ✅ Database disconnected");
            
        } finally {
            lock.unlock();
        }
    }
    
    public void executeQuery(String query) {
        lock.lock();
        try {
            if (state != ConnectionState.CONNECTED) {
                System.out.println("  ⚠️  Cannot execute query - not connected - balking");
                return; // Balk - not connected
            }
            
            System.out.println("  📊 Executing query: " + query);
            
        } finally {
            lock.unlock();
        }
    }
}

// Example 4: Cache with TTL
class CachedData {
    private String data;
    private long lastRefreshTime = 0;
    private boolean refreshing = false;
    private final long ttlMillis = 3000; // 3 seconds
    private final Lock lock = new ReentrantLock();
    private final String dataSource;
    
    public CachedData(String dataSource) {
        this.dataSource = dataSource;
    }
    
    public String getData() {
        lock.lock();
        try {
            if (isFresh()) {
                System.out.println("  ✅ Cache HIT - data is fresh");
                return data;
            }
            
            if (refreshing) {
                System.out.println("  ⚠️  Refresh in progress - returning stale data");
                return data; // Return stale data while refreshing
            }
            
            System.out.println("  ⚠️  Cache MISS - data is stale");
            refresh();
            return data;
            
        } finally {
            lock.unlock();
        }
    }
    
    public void refresh() {
        lock.lock();
        try {
            if (refreshing) {
                System.out.println("  ⚠️  Refresh already in progress - balking");
                return; // Balk - already refreshing
            }
            
            if (isFresh()) {
                System.out.println("  ⚠️  Data is still fresh - balking");
                return; // Balk - data still fresh
            }
            
            System.out.println("  🔄 Refreshing cache from " + dataSource + "...");
            refreshing = true;
            
            // Simulate data fetch
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    lock.lock();
                    try {
                        data = "Data from " + dataSource + " at " + System.currentTimeMillis();
                        lastRefreshTime = System.currentTimeMillis();
                        refreshing = false;
                        System.out.println("  ✅ Cache refreshed");
                    } finally {
                        lock.unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } finally {
            lock.unlock();
        }
    }
    
    private boolean isFresh() {
        return data != null && 
               (System.currentTimeMillis() - lastRefreshTime) < ttlMillis;
    }
}

// Example 5: Singleton Initializer
class ExpensiveResource {
    private static volatile ExpensiveResource instance;
    private static boolean initializing = false;
    private static final Object initLock = new Object();
    
    private final String resourceData;
    
    private ExpensiveResource() {
        System.out.println("  🏗️  Initializing expensive resource...");
        try {
            Thread.sleep(1000); // Simulate expensive initialization
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.resourceData = "Initialized at " + System.currentTimeMillis();
        System.out.println("  ✅ Resource initialized");
    }
    
    public static ExpensiveResource getInstance() {
        if (instance != null) {
            System.out.println("  ✅ Returning existing instance - balking initialization");
            return instance; // Balk - already initialized
        }
        
        synchronized (initLock) {
            if (initializing) {
                System.out.println("  ⚠️  Initialization in progress - balking");
                return null; // Balk - initialization in progress
            }
            
            if (instance != null) {
                return instance; // Double-check
            }
            
            initializing = true;
        }
        
        try {
            instance = new ExpensiveResource();
            return instance;
        } finally {
            synchronized (initLock) {
                initializing = false;
            }
        }
    }
    
    public String getData() {
        return resourceData;
    }
}

// Demo
public class BalkingPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   BALKING PATTERN DEMONSTRATION      ║");
        System.out.println("╚══════════════════════════════════════╝");
        
        // Example 1: Water Heater
        System.out.println("\n1. WATER HEATER (State-based Balking)");
        System.out.println("──────────────────────────────────────");
        
        WaterHeater heater = new WaterHeater();
        heater.turnOn();
        Thread.sleep(200);
        heater.turnOn(); // Should balk - already heating
        Thread.sleep(2000);
        heater.turnOn(); // Should balk - already ready
        heater.turnOff();
        heater.turnOff(); // Should balk - already off
        
        // Example 2: Document Auto-saver
        System.out.println("\n\n2. DOCUMENT AUTO-SAVER (Modification-based Balking)");
        System.out.println("────────────────────────────────────────────────────");
        
        Document doc = new Document("report.txt");
        doc.save(); // Should balk - no changes
        doc.write("Hello ");
        doc.write("World!");
        doc.save(); // Should save
        Thread.sleep(100);
        doc.save(); // Should balk - no new changes
        doc.save(); // Should balk - save in progress
        Thread.sleep(600);
        doc.write(" More content.");
        doc.save(); // Should save new changes
        
        // Example 3: Database Connection
        System.out.println("\n\n3. DATABASE CONNECTION (Connection State Balking)");
        System.out.println("──────────────────────────────────────────────────");
        
        DatabaseConnection db = new DatabaseConnection("jdbc:mysql://localhost/mydb");
        db.executeQuery("SELECT * FROM users"); // Should balk - not connected
        db.connect();
        Thread.sleep(200);
        db.connect(); // Should balk - connection in progress
        Thread.sleep(1000);
        db.executeQuery("SELECT * FROM users"); // Should execute
        db.connect(); // Should balk - already connected
        db.disconnect();
        db.disconnect(); // Should balk - already disconnected
        
        // Example 4: Cache with TTL
        System.out.println("\n\n4. CACHE WITH TTL (Freshness-based Balking)");
        System.out.println("────────────────────────────────────────────");
        
        CachedData cache = new CachedData("RemoteAPI");
        cache.refresh(); // First refresh
        Thread.sleep(600);
        cache.refresh(); // Should balk - data still fresh
        cache.getData(); // Cache hit
        Thread.sleep(3500);
        cache.getData(); // Cache miss - auto refresh
        
        // Example 5: Singleton Initializer
        System.out.println("\n\n5. SINGLETON INITIALIZER (Initialization Balking)");
        System.out.println("──────────────────────────────────────────────────");
        
        // Multiple threads trying to initialize
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 1; i <= 5; i++) {
            final int threadId = i;
            executor.submit(() -> {
                System.out.println("\n[Thread " + threadId + "] Requesting instance...");
                ExpensiveResource resource = ExpensiveResource.getInstance();
                if (resource != null) {
                    System.out.println("[Thread " + threadId + "] Got resource: " + 
                        resource.getData());
                } else {
                    System.out.println("[Thread " + threadId + "] Balked - initialization in progress");
                }
            });
            Thread.sleep(100);
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\n\n✅ Balking Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Prevents redundant operations");
        System.out.println("  • Fails fast when operation can't proceed");
        System.out.println("  • Thread-safe state checking");
        System.out.println("  • Efficient resource usage");
        System.out.println("  • Simple implementation");
        
        System.out.println("\n🆚 Compare with:");
        System.out.println("  • Guarded Suspension: Blocks until condition is met");
        System.out.println("  • Balking: Returns immediately if condition not met");
    }
}
