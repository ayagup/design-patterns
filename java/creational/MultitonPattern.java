package creational;

import java.util.HashMap;
import java.util.Map;

/**
 * Multiton Pattern
 * Ensures only one instance per key exists.
 */
public class MultitonPattern {
    
    // Multiton class
    static class DatabaseConnection {
        private static final Map<String, DatabaseConnection> instances = new HashMap<>();
        private final String database;
        
        private DatabaseConnection(String database) {
            this.database = database;
            System.out.println("Creating connection to database: " + database);
        }
        
        public static synchronized DatabaseConnection getInstance(String key) {
            if (!instances.containsKey(key)) {
                instances.put(key, new DatabaseConnection(key));
            } else {
                System.out.println("Reusing existing connection to: " + key);
            }
            return instances.get(key);
        }
        
        public void executeQuery(String query) {
            System.out.println("Executing on " + database + ": " + query);
        }
        
        public static void printAllInstances() {
            System.out.println("\nTotal instances: " + instances.size());
            instances.keySet().forEach(key -> 
                System.out.println("  - " + key)
            );
        }
    }
    
    // Another Multiton example - Logger
    static class Logger {
        private static final Map<String, Logger> instances = new HashMap<>();
        private final String name;
        
        private Logger(String name) {
            this.name = name;
        }
        
        public static synchronized Logger getInstance(String name) {
            return instances.computeIfAbsent(name, Logger::new);
        }
        
        public void log(String message) {
            System.out.println("[" + name + "] " + message);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Multiton Pattern Demo ===\n");
        
        System.out.println("1. Database Connection Multiton:");
        
        // Get instances for different databases
        DatabaseConnection mysql1 = DatabaseConnection.getInstance("MySQL");
        mysql1.executeQuery("SELECT * FROM users");
        
        DatabaseConnection postgres1 = DatabaseConnection.getInstance("PostgreSQL");
        postgres1.executeQuery("SELECT * FROM products");
        
        DatabaseConnection mongo1 = DatabaseConnection.getInstance("MongoDB");
        mongo1.executeQuery("db.orders.find()");
        
        // Try to get instances again - should reuse existing
        System.out.println("\nRequesting existing instances:");
        DatabaseConnection mysql2 = DatabaseConnection.getInstance("MySQL");
        DatabaseConnection postgres2 = DatabaseConnection.getInstance("PostgreSQL");
        
        // Verify same instances
        System.out.println("\nVerifying instances:");
        System.out.println("mysql1 == mysql2: " + (mysql1 == mysql2));
        System.out.println("postgres1 == postgres2: " + (postgres1 == postgres2));
        
        DatabaseConnection.printAllInstances();
        
        // Logger example
        System.out.println("\n\n2. Logger Multiton:");
        Logger appLogger = Logger.getInstance("Application");
        Logger dbLogger = Logger.getInstance("Database");
        Logger secLogger = Logger.getInstance("Security");
        
        appLogger.log("Application started");
        dbLogger.log("Connected to database");
        secLogger.log("User authenticated");
        appLogger.log("Processing request");
        dbLogger.log("Query executed");
        
        // Reuse logger
        Logger appLogger2 = Logger.getInstance("Application");
        System.out.println("\nappLogger == appLogger2: " + (appLogger == appLogger2));
        
        System.out.println("\n--- Key Differences from Singleton ---");
        System.out.println("Singleton: One instance per class");
        System.out.println("Multiton: One instance per key per class");
    }
}
