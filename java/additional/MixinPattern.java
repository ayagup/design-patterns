package additional;

import java.util.*;
import java.util.function.Consumer;

/**
 * Mixin Pattern
 * 
 * Intent: Adds functionality to classes without using inheritance.
 * Allows composing behavior from multiple sources into a single class.
 * 
 * Motivation:
 * Avoids deep inheritance hierarchies.
 * Enables multiple behavior composition.
 * Promotes code reuse.
 * Flexible feature combination.
 * 
 * Applicability:
 * - Multiple inheritance needed (Java doesn't support)
 * - Composing behaviors from multiple sources
 * - Cross-cutting concerns
 * - Flexible feature sets
 */

/**
 * Example 1: Interface-based Mixins
 * 
 * Using default methods in interfaces
 */
interface Loggable {
    default void log(String message) {
        System.out.println("  [LOG] " + getClass().getSimpleName() + ": " + message);
    }
    
    default void logError(String message) {
        System.out.println("  [ERROR] " + getClass().getSimpleName() + ": " + message);
    }
}

interface Timestamped {
    default long getTimestamp() {
        return System.currentTimeMillis();
    }
    
    default String getFormattedTime() {
        return new Date(getTimestamp()).toString();
    }
}

interface Serializable {
    default String serialize() {
        return "Serialized: " + this.toString();
    }
    
    default void deserialize(String data) {
        System.out.println("  [Deserialize] Processing: " + data);
    }
}

// Class using multiple mixins
class Document implements Loggable, Timestamped, Serializable {
    private final String title;
    private final String content;
    
    public Document(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    public void save() {
        log("Saving document: " + title);
        System.out.println("  [Document] Saved at: " + getFormattedTime());
    }
    
    @Override
    public String toString() {
        return "Document{title='" + title + "', content='" + content + "'}";
    }
}

/**
 * Example 2: Composition-based Mixins
 * 
 * Using composition to add functionality
 */
class ValidationMixin {
    public boolean validateNotNull(Object value, String fieldName) {
        if (value == null) {
            System.out.println("  [Validation] " + fieldName + " is null");
            return false;
        }
        return true;
    }
    
    public boolean validateLength(String value, int minLength, int maxLength) {
        if (value == null || value.length() < minLength || value.length() > maxLength) {
            System.out.println("  [Validation] Length must be between " + 
                             minLength + " and " + maxLength);
            return false;
        }
        return true;
    }
    
    public boolean validateRange(int value, int min, int max) {
        if (value < min || value > max) {
            System.out.println("  [Validation] Value must be between " + min + " and " + max);
            return false;
        }
        return true;
    }
}

class CachingMixin<K, V> {
    private final Map<K, V> cache = new HashMap<>();
    
    public V getFromCache(K key) {
        V value = cache.get(key);
        if (value != null) {
            System.out.println("  [Cache] HIT: " + key);
        } else {
            System.out.println("  [Cache] MISS: " + key);
        }
        return value;
    }
    
    public void putInCache(K key, V value) {
        cache.put(key, value);
        System.out.println("  [Cache] Stored: " + key);
    }
    
    public void clearCache() {
        cache.clear();
        System.out.println("  [Cache] Cleared");
    }
}

// Class using composition mixins
class UserService {
    private final ValidationMixin validator = new ValidationMixin();
    private final CachingMixin<String, String> cache = new CachingMixin<>();
    
    public boolean createUser(String username, String password) {
        // Use validation mixin
        if (!validator.validateNotNull(username, "username")) return false;
        if (!validator.validateLength(username, 3, 20)) return false;
        if (!validator.validateLength(password, 8, 50)) return false;
        
        // Use caching mixin
        cache.putInCache(username, password);
        
        System.out.println("  [UserService] User created: " + username);
        return true;
    }
    
    public String getUser(String username) {
        return cache.getFromCache(username);
    }
}

/**
 * Example 3: Observer Mixin
 * 
 * Adds observer functionality to any class
 */
class ObserverMixin<T> {
    private final List<Consumer<T>> observers = new ArrayList<>();
    
    public void addObserver(Consumer<T> observer) {
        observers.add(observer);
        System.out.println("  [Observer] Added observer");
    }
    
    public void removeObserver(Consumer<T> observer) {
        observers.remove(observer);
        System.out.println("  [Observer] Removed observer");
    }
    
    public void notifyObservers(T data) {
        System.out.println("  [Observer] Notifying " + observers.size() + " observers");
        observers.forEach(observer -> observer.accept(data));
    }
}

class Stock {
    private final ObserverMixin<Double> observerMixin = new ObserverMixin<>();
    private String symbol;
    private double price;
    
    public Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }
    
    public void setPrice(double newPrice) {
        this.price = newPrice;
        System.out.println("  [Stock] " + symbol + " price changed to: $" + price);
        observerMixin.notifyObservers(price);
    }
    
    public void addPriceObserver(Consumer<Double> observer) {
        observerMixin.addObserver(observer);
    }
    
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
}

/**
 * Example 4: Event Emitter Mixin
 * 
 * Adds event emission capability
 */
class EventEmitterMixin {
    private final Map<String, List<Consumer<Object>>> listeners = new HashMap<>();
    
    public void on(String event, Consumer<Object> callback) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(callback);
        System.out.println("  [EventEmitter] Registered listener for: " + event);
    }
    
    public void emit(String event, Object data) {
        List<Consumer<Object>> eventListeners = listeners.get(event);
        if (eventListeners != null) {
            System.out.println("  [EventEmitter] Emitting: " + event);
            eventListeners.forEach(listener -> listener.accept(data));
        }
    }
    
    public void off(String event) {
        listeners.remove(event);
        System.out.println("  [EventEmitter] Removed listeners for: " + event);
    }
}

class ChatRoom {
    private final EventEmitterMixin events = new EventEmitterMixin();
    private final String name;
    
    public ChatRoom(String name) {
        this.name = name;
    }
    
    public void sendMessage(String user, String message) {
        System.out.println("  [ChatRoom] " + user + ": " + message);
        events.emit("message", user + ": " + message);
    }
    
    public void userJoined(String user) {
        System.out.println("  [ChatRoom] " + user + " joined");
        events.emit("userJoined", user);
    }
    
    public void onMessage(Consumer<Object> callback) {
        events.on("message", callback);
    }
    
    public void onUserJoined(Consumer<Object> callback) {
        events.on("userJoined", callback);
    }
}

/**
 * Example 5: Fluent Interface Mixin
 * 
 * Adds builder-style methods
 */
class FluentMixin<T> {
    private final T target;
    
    public FluentMixin(T target) {
        this.target = target;
    }
    
    public T apply(Consumer<T> action) {
        action.accept(target);
        return target;
    }
    
    public T applyIf(boolean condition, Consumer<T> action) {
        if (condition) {
            action.accept(target);
        }
        return target;
    }
}

class QueryBuilder {
    private final StringBuilder sql = new StringBuilder();
    private final FluentMixin<QueryBuilder> fluent = new FluentMixin<>(this);
    
    public QueryBuilder select(String... columns) {
        sql.append("SELECT ").append(String.join(", ", columns));
        return this;
    }
    
    public QueryBuilder from(String table) {
        sql.append(" FROM ").append(table);
        return this;
    }
    
    public QueryBuilder where(String condition) {
        sql.append(" WHERE ").append(condition);
        return this;
    }
    
    public QueryBuilder orderBy(String column) {
        sql.append(" ORDER BY ").append(column);
        return this;
    }
    
    public QueryBuilder apply(Consumer<QueryBuilder> action) {
        return fluent.apply(action);
    }
    
    public QueryBuilder applyIf(boolean condition, Consumer<QueryBuilder> action) {
        return fluent.applyIf(condition, action);
    }
    
    public String build() {
        String query = sql.toString();
        System.out.println("  [QueryBuilder] Built query: " + query);
        return query;
    }
}

/**
 * Example 6: Retry Mixin
 * 
 * Adds retry logic to operations
 */
class RetryMixin {
    private int maxRetries = 3;
    private long retryDelay = 100;
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    public <T> T retry(java.util.function.Supplier<T> operation) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            attempt++;
            try {
                System.out.println("  [Retry] Attempt " + attempt + "/" + maxRetries);
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                System.out.println("  [Retry] Failed: " + e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        System.out.println("  [Retry] All attempts failed");
        throw new RuntimeException("Operation failed after " + maxRetries + " attempts", lastException);
    }
}

class NetworkClient {
    private final RetryMixin retry = new RetryMixin();
    private int callCount = 0;
    
    public NetworkClient() {
        retry.setMaxRetries(3);
        retry.setRetryDelay(50);
    }
    
    public String fetchData(String url) {
        return retry.retry(() -> {
            callCount++;
            if (callCount < 3) {
                throw new RuntimeException("Network error");
            }
            return "Data from " + url;
        });
    }
}

/**
 * Demonstration of the Mixin Pattern
 */
public class MixinPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Mixin Pattern Demo ===\n");
        
        // Example 1: Interface-based Mixins
        System.out.println("1. Interface-based Mixins:");
        Document doc = new Document("Report", "Annual report content");
        doc.save();
        System.out.println("  Serialized: " + doc.serialize());
        
        // Example 2: Composition-based Mixins
        System.out.println("\n2. Composition-based Mixins:");
        UserService userService = new UserService();
        userService.createUser("alice", "password123");
        userService.createUser("ab", "short"); // Validation fails
        
        // Example 3: Observer Mixin
        System.out.println("\n3. Observer Mixin:");
        Stock stock = new Stock("AAPL", 150.00);
        stock.addPriceObserver(price -> 
            System.out.println("    [Observer1] Price alert: $" + price));
        stock.addPriceObserver(price -> 
            System.out.println("    [Observer2] Logging price: $" + price));
        stock.setPrice(155.50);
        
        // Example 4: Event Emitter Mixin
        System.out.println("\n4. Event Emitter Mixin:");
        ChatRoom chatRoom = new ChatRoom("General");
        chatRoom.onMessage(msg -> System.out.println("    [Logger] " + msg));
        chatRoom.onUserJoined(user -> System.out.println("    [Greeter] Welcome " + user + "!"));
        
        chatRoom.userJoined("Alice");
        chatRoom.sendMessage("Alice", "Hello everyone!");
        
        // Example 5: Fluent Interface Mixin
        System.out.println("\n5. Fluent Interface Mixin:");
        QueryBuilder query = new QueryBuilder();
        query.select("id", "name", "email")
             .from("users")
             .applyIf(true, q -> q.where("active = 1"))
             .orderBy("name")
             .build();
        
        // Example 6: Retry Mixin
        System.out.println("\n6. Retry Mixin:");
        NetworkClient client = new NetworkClient();
        try {
            String data = client.fetchData("https://api.example.com/data");
            System.out.println("  Success: " + data);
        } catch (RuntimeException e) {
            System.out.println("  Failed to fetch data");
        }
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Avoids inheritance complexity");
        System.out.println("✓ Composes multiple behaviors");
        System.out.println("✓ Promotes code reuse");
        System.out.println("✓ Flexible feature combination");
        System.out.println("✓ Cross-cutting concerns");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Adding logging/validation");
        System.out.println("• Event handling");
        System.out.println("• Caching capabilities");
        System.out.println("• Observer functionality");
        System.out.println("• Retry logic");
    }
}
