package additional;

import java.util.*;
import java.lang.reflect.*;

/**
 * Type Tunnel Pattern (Type Token Pattern)
 * 
 * Intent: Preserves type information through Java's generic type system
 * to work around type erasure limitations.
 * 
 * Motivation:
 * Java generics use type erasure at runtime.
 * Need to preserve type information for reflection.
 * Enables type-safe operations on generic types.
 * Allows runtime type checking.
 * 
 * Applicability:
 * - Generic collections with type safety
 * - Dependency injection frameworks
 * - Serialization/deserialization
 * - Type-safe heterogeneous containers
 */

/**
 * Example 1: Type Token Class
 * 
 * Captures and preserves generic type information
 */
abstract class TypeToken<T> {
    private final Type type;
    
    protected TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof ParameterizedType) {
            this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        } else {
            throw new RuntimeException("Missing type parameter");
        }
    }
    
    public Type getType() {
        return type;
    }
    
    @SuppressWarnings("unchecked")
    public Class<T> getRawType() {
        if (type instanceof Class) {
            return (Class<T>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        throw new RuntimeException("Unsupported type");
    }
    
    @Override
    public String toString() {
        return type.getTypeName();
    }
}

/**
 * Example 2: Type-Safe Heterogeneous Container
 * 
 * Store different types with type safety
 */
class TypeSafeMap {
    private final Map<TypeToken<?>, Object> map = new HashMap<>();
    
    public <T> void put(TypeToken<T> type, T value) {
        map.put(type, value);
        System.out.println("  [TypeSafeMap] Stored " + type + " = " + value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(TypeToken<T> type) {
        T value = (T) map.get(type);
        System.out.println("  [TypeSafeMap] Retrieved " + type + " = " + value);
        return value;
    }
    
    public boolean contains(TypeToken<?> type) {
        return map.containsKey(type);
    }
    
    public int size() {
        return map.size();
    }
}

/**
 * Example 3: Generic DAO with Type Token
 * 
 * Type-safe data access object
 */
interface Entity {
    Long getId();
    void setId(Long id);
}

class User implements Entity {
    private Long id;
    private String username;
    
    public User(String username) {
        this.username = username;
    }
    
    @Override
    public Long getId() { return id; }
    
    @Override
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "'}";
    }
}

class Product implements Entity {
    private Long id;
    private String name;
    private double price;
    
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    @Override
    public Long getId() { return id; }
    
    @Override
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
}

class GenericDAO<T extends Entity> {
    private final Class<T> entityClass;
    private final Map<Long, T> storage = new HashMap<>();
    private long nextId = 1L;
    
    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
        System.out.println("  [DAO] Created for type: " + entityClass.getSimpleName());
    }
    
    public void save(T entity) {
        if (entity.getId() == null) {
            entity.setId(nextId++);
        }
        storage.put(entity.getId(), entity);
        System.out.println("  [DAO] Saved: " + entity);
    }
    
    public T findById(Long id) {
        T entity = storage.get(id);
        System.out.println("  [DAO] Found by ID " + id + ": " + entity);
        return entity;
    }
    
    public List<T> findAll() {
        System.out.println("  [DAO] Finding all " + entityClass.getSimpleName() + " entities");
        return new ArrayList<>(storage.values());
    }
    
    public Class<T> getEntityClass() {
        return entityClass;
    }
}

/**
 * Example 4: Type-Safe Event Bus
 * 
 * Event bus with type preservation
 */
class TypedEvent<T> {
    private final TypeToken<T> type;
    private final T data;
    
    public TypedEvent(TypeToken<T> type, T data) {
        this.type = type;
        this.data = data;
    }
    
    public TypeToken<T> getType() { return type; }
    public T getData() { return data; }
}

interface EventHandler<T> {
    void handle(T event);
}

class TypedEventBus {
    private final Map<Type, List<EventHandler<?>>> handlers = new HashMap<>();
    
    public <T> void subscribe(TypeToken<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType.getType(), k -> new ArrayList<>()).add(handler);
        System.out.println("  [EventBus] Subscribed to: " + eventType);
    }
    
    @SuppressWarnings("unchecked")
    public <T> void publish(TypedEvent<T> event) {
        List<EventHandler<?>> eventHandlers = handlers.get(event.getType().getType());
        if (eventHandlers != null) {
            System.out.println("  [EventBus] Publishing event: " + event.getType());
            for (EventHandler<?> handler : eventHandlers) {
                ((EventHandler<T>) handler).handle(event.getData());
            }
        }
    }
}

class UserLoggedInEvent {
    private final String username;
    
    public UserLoggedInEvent(String username) {
        this.username = username;
    }
    
    public String getUsername() { return username; }
}

/**
 * Example 5: Type-Safe Registry
 * 
 * Service registry with type tokens
 */
class ServiceRegistry {
    private final Map<Type, Object> services = new HashMap<>();
    
    public <T> void register(TypeToken<T> type, T service) {
        services.put(type.getType(), service);
        System.out.println("  [Registry] Registered service: " + type);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getService(TypeToken<T> type) {
        T service = (T) services.get(type.getType());
        System.out.println("  [Registry] Retrieved service: " + type);
        return service;
    }
    
    public boolean hasService(TypeToken<?> type) {
        return services.containsKey(type.getType());
    }
}

interface EmailService {
    void sendEmail(String to, String message);
}

class SimpleEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String message) {
        System.out.println("  [Email] Sending to " + to + ": " + message);
    }
}

/**
 * Example 6: Type-Safe Configuration
 * 
 * Configuration with preserved types
 */
class TypedConfiguration {
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, Type> types = new HashMap<>();
    
    public <T> void set(String key, TypeToken<T> type, T value) {
        values.put(key, value);
        types.put(key, type.getType());
        System.out.println("  [Config] Set " + key + " (" + type + ") = " + value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, TypeToken<T> type) {
        Type storedType = types.get(key);
        if (storedType == null) {
            System.out.println("  [Config] Key not found: " + key);
            return null;
        }
        
        if (!storedType.equals(type.getType())) {
            System.out.println("  [Config] Type mismatch for key: " + key);
            return null;
        }
        
        T value = (T) values.get(key);
        System.out.println("  [Config] Get " + key + " (" + type + ") = " + value);
        return value;
    }
}

/**
 * Demonstration of the Type Tunnel Pattern
 */
public class TypeTunnelPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Type Tunnel Pattern Demo ===\n");
        
        // Example 1: Type Token Basics
        System.out.println("1. Type Token Basics:");
        TypeToken<String> stringType = new TypeToken<String>() {};
        TypeToken<List<Integer>> listType = new TypeToken<List<Integer>>() {};
        
        System.out.println("  String type: " + stringType);
        System.out.println("  List<Integer> type: " + listType);
        System.out.println("  Raw type: " + stringType.getRawType().getSimpleName());
        
        // Example 2: Type-Safe Heterogeneous Container
        System.out.println("\n2. Type-Safe Heterogeneous Container:");
        TypeSafeMap map = new TypeSafeMap();
        
        map.put(new TypeToken<String>() {}, "Hello World");
        map.put(new TypeToken<Integer>() {}, 42);
        map.put(new TypeToken<List<String>>() {}, Arrays.asList("a", "b", "c"));
        
        String str = map.get(new TypeToken<String>() {});
        Integer num = map.get(new TypeToken<Integer>() {});
        
        // Example 3: Generic DAO
        System.out.println("\n3. Generic DAO with Type Preservation:");
        GenericDAO<User> userDao = new GenericDAO<>(User.class);
        GenericDAO<Product> productDao = new GenericDAO<>(Product.class);
        
        userDao.save(new User("alice"));
        userDao.save(new User("bob"));
        
        productDao.save(new Product("Laptop", 999.99));
        
        List<User> users = userDao.findAll();
        System.out.println("  Found " + users.size() + " users");
        
        // Example 4: Type-Safe Event Bus
        System.out.println("\n4. Type-Safe Event Bus:");
        TypedEventBus eventBus = new TypedEventBus();
        
        TypeToken<UserLoggedInEvent> loginEventType = new TypeToken<UserLoggedInEvent>() {};
        
        eventBus.subscribe(loginEventType, event -> {
            System.out.println("    [Handler1] User logged in: " + event.getUsername());
        });
        
        eventBus.subscribe(loginEventType, event -> {
            System.out.println("    [Handler2] Sending welcome email to: " + event.getUsername());
        });
        
        eventBus.publish(new TypedEvent<>(loginEventType, new UserLoggedInEvent("alice")));
        
        // Example 5: Service Registry
        System.out.println("\n5. Service Registry:");
        ServiceRegistry registry = new ServiceRegistry();
        
        TypeToken<EmailService> emailServiceType = new TypeToken<EmailService>() {};
        registry.register(emailServiceType, new SimpleEmailService());
        
        EmailService emailService = registry.getService(emailServiceType);
        emailService.sendEmail("bob@example.com", "Hello!");
        
        // Example 6: Typed Configuration
        System.out.println("\n6. Typed Configuration:");
        TypedConfiguration config = new TypedConfiguration();
        
        config.set("app.name", new TypeToken<String>() {}, "MyApp");
        config.set("server.port", new TypeToken<Integer>() {}, 8080);
        config.set("features", new TypeToken<List<String>>() {}, 
                  Arrays.asList("feature1", "feature2"));
        
        String appName = config.get("app.name", new TypeToken<String>() {});
        Integer port = config.get("server.port", new TypeToken<Integer>() {});
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Preserves generic type information");
        System.out.println("✓ Type-safe at compile and runtime");
        System.out.println("✓ Works around type erasure");
        System.out.println("✓ Enables reflection on generics");
        System.out.println("✓ Heterogeneous type-safe containers");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Dependency injection frameworks");
        System.out.println("• Serialization libraries");
        System.out.println("• Type-safe configuration");
        System.out.println("• Generic DAOs");
        System.out.println("• Event systems");
    }
}
