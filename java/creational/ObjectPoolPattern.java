package creational;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Object Pool Pattern
 * Reuses objects that are expensive to create.
 */
public class ObjectPoolPattern {
    
    // Pooled object
    static class DatabaseConnection {
        private static int counter = 0;
        private final int id;
        private boolean inUse;
        
        public DatabaseConnection() {
            this.id = ++counter;
            System.out.println("Creating new connection #" + id);
        }
        
        public void connect() {
            System.out.println("Connection #" + id + " - Executing query...");
        }
        
        public void disconnect() {
            System.out.println("Connection #" + id + " - Query completed");
        }
        
        public boolean isInUse() {
            return inUse;
        }
        
        public void setInUse(boolean inUse) {
            this.inUse = inUse;
        }
        
        public int getId() {
            return id;
        }
    }
    
    // Object Pool
    static class ConnectionPool {
        private final int maxPoolSize;
        private final ConcurrentLinkedQueue<DatabaseConnection> availableConnections;
        private final ConcurrentLinkedQueue<DatabaseConnection> inUseConnections;
        
        public ConnectionPool(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            this.availableConnections = new ConcurrentLinkedQueue<>();
            this.inUseConnections = new ConcurrentLinkedQueue<>();
            
            // Pre-populate pool
            for (int i = 0; i < maxPoolSize / 2; i++) {
                availableConnections.add(new DatabaseConnection());
            }
        }
        
        public synchronized DatabaseConnection acquireConnection() {
            DatabaseConnection connection = availableConnections.poll();
            
            if (connection == null) {
                if (inUseConnections.size() < maxPoolSize) {
                    connection = new DatabaseConnection();
                } else {
                    System.out.println("⚠️ Pool exhausted! Waiting for available connection...");
                    return null;
                }
            }
            
            connection.setInUse(true);
            inUseConnections.add(connection);
            System.out.println("✓ Acquired connection #" + connection.getId() + 
                             " (Available: " + availableConnections.size() + 
                             ", In use: " + inUseConnections.size() + ")");
            return connection;
        }
        
        public synchronized void releaseConnection(DatabaseConnection connection) {
            if (connection != null && inUseConnections.remove(connection)) {
                connection.setInUse(false);
                availableConnections.add(connection);
                System.out.println("✓ Released connection #" + connection.getId() + 
                                 " (Available: " + availableConnections.size() + 
                                 ", In use: " + inUseConnections.size() + ")");
            }
        }
        
        public void shutdown() {
            availableConnections.clear();
            inUseConnections.clear();
            System.out.println("Pool shutdown complete");
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Object Pool Pattern Demo ===\n");
        
        ConnectionPool pool = new ConnectionPool(3);
        
        System.out.println("\n--- Acquiring connections ---");
        DatabaseConnection conn1 = pool.acquireConnection();
        if (conn1 != null) conn1.connect();
        
        DatabaseConnection conn2 = pool.acquireConnection();
        if (conn2 != null) conn2.connect();
        
        DatabaseConnection conn3 = pool.acquireConnection();
        if (conn3 != null) conn3.connect();
        
        // Try to acquire when pool is exhausted
        System.out.println("\n--- Pool exhausted scenario ---");
        DatabaseConnection conn4 = pool.acquireConnection();
        
        // Release and reuse
        System.out.println("\n--- Releasing and reusing connections ---");
        if (conn1 != null) {
            conn1.disconnect();
            pool.releaseConnection(conn1);
        }
        
        DatabaseConnection conn5 = pool.acquireConnection();
        if (conn5 != null) {
            conn5.connect();
            System.out.println("Reused connection #" + conn5.getId());
        }
        
        // Cleanup
        System.out.println("\n--- Cleanup ---");
        if (conn2 != null) pool.releaseConnection(conn2);
        if (conn3 != null) pool.releaseConnection(conn3);
        if (conn5 != null) pool.releaseConnection(conn5);
        
        pool.shutdown();
    }
}
