package additional;

import java.util.*;
import java.util.function.Consumer;

/**
 * Module Pattern
 * 
 * Intent: Encapsulates code into self-contained modules that expose
 * only a public API while keeping implementation details private.
 * 
 * Motivation:
 * Provides encapsulation in Java.
 * Creates namespace-like structures.
 * Hides implementation details.
 * Promotes single responsibility.
 * 
 * Applicability:
 * - Organizing related functionality
 * - Creating reusable libraries
 * - Hiding implementation
 * - Namespace management
 */

/**
 * Example 1: Simple Module
 * 
 * Basic module with private internals
 */
class MathModule {
    // Private state
    private static final double PI = 3.14159;
    private static int calculationCount = 0;
    
    // Private helper method
    private static void incrementCount() {
        calculationCount++;
    }
    
    // Public API
    public static double circleArea(double radius) {
        incrementCount();
        double area = PI * radius * radius;
        System.out.println("  [MathModule] Circle area: " + area);
        return area;
    }
    
    public static double circleCircumference(double radius) {
        incrementCount();
        double circumference = 2 * PI * radius;
        System.out.println("  [MathModule] Circle circumference: " + circumference);
        return circumference;
    }
    
    public static int getCalculationCount() {
        return calculationCount;
    }
    
    public static void resetCount() {
        calculationCount = 0;
        System.out.println("  [MathModule] Reset count");
    }
}

/**
 * Example 2: Shopping Cart Module
 * 
 * Module with internal state management
 */
class ShoppingCartModule {
    // Private state
    private final List<CartItem> items;
    private double discount;
    
    public ShoppingCartModule() {
        this.items = new ArrayList<>();
        this.discount = 0.0;
    }
    
    // Private helper
    private double calculateSubtotal() {
        return items.stream()
            .mapToDouble(item -> item.price * item.quantity)
            .sum();
    }
    
    // Public API
    public void addItem(String name, double price, int quantity) {
        items.add(new CartItem(name, price, quantity));
        System.out.println("  [Cart] Added: " + quantity + "x " + name);
    }
    
    public void removeItem(String name) {
        items.removeIf(item -> item.name.equals(name));
        System.out.println("  [Cart] Removed: " + name);
    }
    
    public void setDiscount(double percentage) {
        this.discount = percentage;
        System.out.println("  [Cart] Discount set to: " + percentage + "%");
    }
    
    public double getTotal() {
        double subtotal = calculateSubtotal();
        double total = subtotal * (1 - discount / 100);
        System.out.println("  [Cart] Total: $" + String.format("%.2f", total));
        return total;
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    // Private inner class - not accessible outside
    private static class CartItem {
        String name;
        double price;
        int quantity;
        
        CartItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }
}

/**
 * Example 3: Logger Module
 * 
 * Module with configuration and state
 */
class LoggerModule {
    // Private configuration
    private static LogLevel level = LogLevel.INFO;
    private static final List<String> logs = new ArrayList<>();
    private static final int MAX_LOGS = 100;
    
    // Private enum
    private enum LogLevel {
        DEBUG(0), INFO(1), WARN(2), ERROR(3);
        
        final int priority;
        LogLevel(int priority) { this.priority = priority; }
    }
    
    // Private helper
    private static void log(LogLevel logLevel, String message) {
        if (logLevel.priority >= level.priority) {
            String logEntry = "[" + logLevel + "] " + message;
            logs.add(logEntry);
            
            // Keep only last MAX_LOGS entries
            if (logs.size() > MAX_LOGS) {
                logs.remove(0);
            }
            
            System.out.println("  " + logEntry);
        }
    }
    
    // Public API
    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }
    
    public static void info(String message) {
        log(LogLevel.INFO, message);
    }
    
    public static void warn(String message) {
        log(LogLevel.WARN, message);
    }
    
    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }
    
    public static void setLevel(String levelName) {
        try {
            level = LogLevel.valueOf(levelName.toUpperCase());
            System.out.println("  [Logger] Level set to: " + level);
        } catch (IllegalArgumentException e) {
            System.out.println("  [Logger] Invalid level: " + levelName);
        }
    }
    
    public static List<String> getLogs() {
        return new ArrayList<>(logs);
    }
    
    public static void clearLogs() {
        logs.clear();
        System.out.println("  [Logger] Logs cleared");
    }
}

/**
 * Example 4: Event Bus Module
 * 
 * Module for event handling
 */
class EventBusModule {
    // Private state
    private final Map<String, List<Consumer<Object>>> listeners;
    private final List<String> eventHistory;
    
    public EventBusModule() {
        this.listeners = new HashMap<>();
        this.eventHistory = new ArrayList<>();
    }
    
    // Private helper
    private void recordEvent(String eventName) {
        eventHistory.add(eventName);
    }
    
    // Public API
    public void on(String eventName, Consumer<Object> callback) {
        listeners.computeIfAbsent(eventName, k -> new ArrayList<>()).add(callback);
        System.out.println("  [EventBus] Registered listener for: " + eventName);
    }
    
    public void emit(String eventName, Object data) {
        recordEvent(eventName);
        System.out.println("  [EventBus] Emitting: " + eventName);
        
        List<Consumer<Object>> eventListeners = listeners.get(eventName);
        if (eventListeners != null) {
            eventListeners.forEach(listener -> listener.accept(data));
        }
    }
    
    public void off(String eventName) {
        listeners.remove(eventName);
        System.out.println("  [EventBus] Removed listeners for: " + eventName);
    }
    
    public int getListenerCount(String eventName) {
        List<Consumer<Object>> eventListeners = listeners.get(eventName);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    public List<String> getEventHistory() {
        return new ArrayList<>(eventHistory);
    }
}

/**
 * Example 5: Cache Module
 * 
 * Module with TTL and size limits
 */
class CacheModule<K, V> {
    // Private state
    private final Map<K, CacheEntry<V>> cache;
    private final int maxSize;
    private final long ttlMillis;
    private int hits = 0;
    private int misses = 0;
    
    public CacheModule(int maxSize, long ttlMillis) {
        this.cache = new LinkedHashMap<>();
        this.maxSize = maxSize;
        this.ttlMillis = ttlMillis;
    }
    
    // Private helper classes
    private static class CacheEntry<V> {
        V value;
        long timestamp;
        
        CacheEntry(V value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired(long ttl) {
            return System.currentTimeMillis() - timestamp > ttl;
        }
    }
    
    // Private helper methods
    private void evictOldest() {
        if (!cache.isEmpty()) {
            K firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
        }
    }
    
    private void cleanExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(ttlMillis));
    }
    
    // Public API
    public void put(K key, V value) {
        cleanExpired();
        
        if (cache.size() >= maxSize && !cache.containsKey(key)) {
            evictOldest();
        }
        
        cache.put(key, new CacheEntry<>(value));
        System.out.println("  [Cache] Put: " + key);
    }
    
    public V get(K key) {
        cleanExpired();
        
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && !entry.isExpired(ttlMillis)) {
            hits++;
            System.out.println("  [Cache] HIT: " + key);
            return entry.value;
        }
        
        misses++;
        System.out.println("  [Cache] MISS: " + key);
        return null;
    }
    
    public void clear() {
        cache.clear();
        System.out.println("  [Cache] Cleared");
    }
    
    public int size() {
        cleanExpired();
        return cache.size();
    }
    
    public double getHitRate() {
        int total = hits + misses;
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    public void printStats() {
        System.out.println("  [Cache] Stats - Hits: " + hits + ", Misses: " + misses + 
                         ", Hit Rate: " + String.format("%.2f%%", getHitRate() * 100));
    }
}

/**
 * Example 6: Configuration Module
 * 
 * Centralized configuration management
 */
class ConfigModule {
    // Private state
    private static final Map<String, String> config = new HashMap<>();
    private static final Map<String, String> defaults = new HashMap<>();
    
    static {
        // Initialize defaults
        defaults.put("app.name", "MyApp");
        defaults.put("app.version", "1.0.0");
        defaults.put("server.port", "8080");
    }
    
    // Private helper
    private static String getOrDefault(String key) {
        return config.getOrDefault(key, defaults.get(key));
    }
    
    // Public API
    public static void set(String key, String value) {
        config.put(key, value);
        System.out.println("  [Config] Set " + key + " = " + value);
    }
    
    public static String get(String key) {
        String value = getOrDefault(key);
        System.out.println("  [Config] Get " + key + " = " + value);
        return value;
    }
    
    public static int getInt(String key) {
        String value = getOrDefault(key);
        return value != null ? Integer.parseInt(value) : 0;
    }
    
    public static boolean getBoolean(String key) {
        String value = getOrDefault(key);
        return Boolean.parseBoolean(value);
    }
    
    public static void reset() {
        config.clear();
        System.out.println("  [Config] Reset to defaults");
    }
    
    public static Map<String, String> getAll() {
        Map<String, String> all = new HashMap<>(defaults);
        all.putAll(config);
        return all;
    }
}

/**
 * Demonstration of the Module Pattern
 */
public class ModulePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Module Pattern Demo ===\n");
        
        // Example 1: Math Module
        System.out.println("1. Simple Module:");
        MathModule.circleArea(5.0);
        MathModule.circleCircumference(5.0);
        System.out.println("  Total calculations: " + MathModule.getCalculationCount());
        MathModule.resetCount();
        
        // Example 2: Shopping Cart Module
        System.out.println("\n2. Shopping Cart Module:");
        ShoppingCartModule cart = new ShoppingCartModule();
        cart.addItem("Laptop", 999.99, 1);
        cart.addItem("Mouse", 29.99, 2);
        cart.setDiscount(10);
        cart.getTotal();
        System.out.println("  Items in cart: " + cart.getItemCount());
        
        // Example 3: Logger Module
        System.out.println("\n3. Logger Module:");
        LoggerModule.setLevel("INFO");
        LoggerModule.debug("This won't show");
        LoggerModule.info("Application started");
        LoggerModule.warn("Low memory");
        LoggerModule.error("Connection failed");
        System.out.println("  Total logs: " + LoggerModule.getLogs().size());
        
        // Example 4: Event Bus Module
        System.out.println("\n4. Event Bus Module:");
        EventBusModule eventBus = new EventBusModule();
        
        eventBus.on("user.login", data -> 
            System.out.println("    [Listener1] User logged in: " + data));
        eventBus.on("user.login", data -> 
            System.out.println("    [Listener2] Send welcome email"));
        
        eventBus.emit("user.login", "alice");
        System.out.println("  Event history: " + eventBus.getEventHistory());
        
        // Example 5: Cache Module
        System.out.println("\n5. Cache Module:");
        CacheModule<String, String> cache = new CacheModule<>(3, 5000);
        
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.get("key1"); // Hit
        cache.get("key3"); // Miss
        cache.printStats();
        
        // Example 6: Configuration Module
        System.out.println("\n6. Configuration Module:");
        ConfigModule.get("app.name");
        ConfigModule.set("server.port", "9000");
        ConfigModule.get("server.port");
        System.out.println("  Port as int: " + ConfigModule.getInt("server.port"));
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Encapsulation of related functionality");
        System.out.println("✓ Hides implementation details");
        System.out.println("✓ Clear public API");
        System.out.println("✓ Namespace management");
        System.out.println("✓ Single responsibility");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Utility libraries");
        System.out.println("• Service modules");
        System.out.println("• Configuration management");
        System.out.println("• Event handling");
        System.out.println("• Caching systems");
    }
}
