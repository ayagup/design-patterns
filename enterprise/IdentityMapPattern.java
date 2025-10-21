package enterprise;

import java.math.BigDecimal;
import java.util.*;

/**
 * Identity Map Pattern
 * 
 * Intent: Ensures that each object gets loaded only once by keeping
 * every loaded object in a map. Looks up objects using the map when
 * referring to them.
 * 
 * Motivation:
 * Prevents loading the same database row into multiple objects.
 * Maintains object identity - if you load the same row twice,
 * you get the same object instance.
 * Acts as a cache for database reads.
 * 
 * Applicability:
 * - Need to ensure object identity
 * - Want to avoid redundant database reads
 * - Working with Unit of Work pattern
 * - Need to track loaded objects in a session
 */

/**
 * Example 1: Simple Identity Map
 * 
 * Maps entity IDs to loaded instances
 */
class Person implements Entity {
    private final Long id;
    private String name;
    private String email;
    
    public Person(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    @Override
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    
    @Override
    public String toString() {
        return "Person[" + id + "]: " + name + " (" + email + ")";
    }
}

class SimpleIdentityMap {
    private final Map<Long, Person> map;
    
    public SimpleIdentityMap() {
        this.map = new HashMap<>();
    }
    
    public Person get(Long id) {
        Person person = map.get(id);
        if (person != null) {
            System.out.println("  [IdentityMap] Cache hit for Person ID: " + id);
        }
        return person;
    }
    
    public void put(Person person) {
        map.put(person.getId(), person);
        System.out.println("  [IdentityMap] Cached: " + person);
    }
    
    public void remove(Long id) {
        map.remove(id);
        System.out.println("  [IdentityMap] Removed Person ID: " + id);
    }
    
    public void clear() {
        map.clear();
        System.out.println("  [IdentityMap] Cleared all cached objects");
    }
    
    public int size() {
        return map.size();
    }
}

/**
 * Example 2: Type-Safe Identity Map with Generics
 * 
 * Generic identity map for any entity type
 */
interface Entity {
    Long getId();
}

class Product implements Entity {
    private final Long id;
    private String name;
    private BigDecimal price;
    
    public Product(Long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    @Override
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    
    public void setPrice(BigDecimal price) { this.price = price; }
    
    @Override
    public String toString() {
        return "Product[" + id + "]: " + name + " ($" + price + ")";
    }
}

class GenericIdentityMap<T extends Entity> {
    private final Map<Long, T> map;
    private final String entityType;
    private int hits;
    private int misses;
    
    public GenericIdentityMap(String entityType) {
        this.map = new HashMap<>();
        this.entityType = entityType;
        this.hits = 0;
        this.misses = 0;
    }
    
    public T get(Long id) {
        T entity = map.get(id);
        if (entity != null) {
            hits++;
            System.out.println("  [IdentityMap] Cache HIT for " + entityType + " ID: " + id);
        } else {
            misses++;
            System.out.println("  [IdentityMap] Cache MISS for " + entityType + " ID: " + id);
        }
        return entity;
    }
    
    public void put(T entity) {
        map.put(entity.getId(), entity);
        System.out.println("  [IdentityMap] Cached " + entityType + ": " + entity);
    }
    
    public void remove(Long id) {
        map.remove(id);
        System.out.println("  [IdentityMap] Removed " + entityType + " ID: " + id);
    }
    
    public void clear() {
        map.clear();
        hits = 0;
        misses = 0;
        System.out.println("  [IdentityMap] Cleared " + entityType + " cache");
    }
    
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("size", map.size());
        stats.put("hits", hits);
        stats.put("misses", misses);
        int total = hits + misses;
        stats.put("hitRate", total > 0 ? (hits * 100 / total) : 0);
        return stats;
    }
}

/**
 * Example 3: Identity Map with Repository Integration
 * 
 * Repository pattern using identity map for caching
 */
class Customer implements Entity {
    private final Long id;
    private String name;
    private String email;
    private String tier;
    
    public Customer(Long id, String name, String email, String tier) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.tier = tier;
    }
    
    @Override
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTier() { return tier; }
    
    public void setTier(String tier) { this.tier = tier; }
    
    @Override
    public String toString() {
        return "Customer[" + id + "]: " + name + " (" + tier + ")";
    }
}

class CustomerRepository {
    private final GenericIdentityMap<Customer> identityMap;
    private final Map<Long, Customer> database; // Simulated database
    
    public CustomerRepository() {
        this.identityMap = new GenericIdentityMap<>("Customer");
        this.database = new HashMap<>();
        
        // Initialize some data
        database.put(1L, new Customer(1L, "Alice", "alice@example.com", "GOLD"));
        database.put(2L, new Customer(2L, "Bob", "bob@example.com", "SILVER"));
        database.put(3L, new Customer(3L, "Charlie", "charlie@example.com", "BRONZE"));
    }
    
    public Customer findById(Long id) {
        // Check identity map first
        Customer customer = identityMap.get(id);
        
        if (customer == null) {
            // Load from database
            customer = loadFromDatabase(id);
            if (customer != null) {
                identityMap.put(customer);
            }
        }
        
        return customer;
    }
    
    private Customer loadFromDatabase(Long id) {
        System.out.println("  [Repository] Loading from database: Customer ID " + id);
        Customer customer = database.get(id);
        return customer != null ? 
            new Customer(customer.getId(), customer.getName(), customer.getEmail(), customer.getTier()) : 
            null;
    }
    
    public void save(Customer customer) {
        database.put(customer.getId(), customer);
        identityMap.put(customer);
        System.out.println("  [Repository] Saved: " + customer);
    }
    
    public void clearCache() {
        identityMap.clear();
    }
    
    public Map<String, Integer> getCacheStatistics() {
        return identityMap.getStatistics();
    }
}

/**
 * Example 4: Multi-Type Identity Map (Session-Level)
 * 
 * Single identity map managing multiple entity types
 */
class SessionIdentityMap {
    private final Map<Class<?>, Map<Long, Entity>> maps;
    
    public SessionIdentityMap() {
        this.maps = new HashMap<>();
    }
    
    public <T extends Entity> T get(Class<T> type, Long id) {
        Map<Long, Entity> map = maps.get(type);
        if (map == null) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        T entity = (T) map.get(id);
        
        if (entity != null) {
            System.out.println("  [Session] Cache hit: " + type.getSimpleName() + " ID " + id);
        }
        
        return entity;
    }
    
    public <T extends Entity> void put(T entity) {
        Class<?> type = entity.getClass();
        Map<Long, Entity> map = maps.computeIfAbsent(type, k -> new HashMap<>());
        map.put(entity.getId(), entity);
        
        System.out.println("  [Session] Cached: " + entity);
    }
    
    public <T extends Entity> void remove(Class<T> type, Long id) {
        Map<Long, Entity> map = maps.get(type);
        if (map != null) {
            map.remove(id);
            System.out.println("  [Session] Removed: " + type.getSimpleName() + " ID " + id);
        }
    }
    
    public void clear() {
        maps.clear();
        System.out.println("  [Session] Cleared all cached entities");
    }
    
    public int getTotalSize() {
        return maps.values().stream().mapToInt(Map::size).sum();
    }
}

/**
 * Demonstration of the Identity Map Pattern
 */
public class IdentityMapPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Identity Map Pattern Demo ===\n");
        
        // Example 1: Simple Identity Map
        System.out.println("1. Simple Identity Map:");
        SimpleIdentityMap personMap = new SimpleIdentityMap();
        
        Person p1 = new Person(1L, "Alice", "alice@example.com");
        personMap.put(p1);
        
        Person retrieved1 = personMap.get(1L);
        Person retrieved2 = personMap.get(1L);
        
        System.out.println("Same instance? " + (retrieved1 == retrieved2));
        System.out.println("Cache size: " + personMap.size());
        
        // Example 2: Generic Identity Map
        System.out.println("\n2. Generic Identity Map:");
        GenericIdentityMap<Product> productMap = new GenericIdentityMap<>("Product");
        
        Product prod1 = new Product(1L, "Laptop", new BigDecimal("999.99"));
        productMap.put(prod1);
        
        productMap.get(1L); // Hit
        productMap.get(2L); // Miss
        productMap.get(1L); // Hit
        
        System.out.println("Statistics: " + productMap.getStatistics());
        
        // Example 3: Repository with Identity Map
        System.out.println("\n3. Repository with Identity Map:");
        CustomerRepository repository = new CustomerRepository();
        
        Customer c1 = repository.findById(1L); // DB load + cache
        Customer c2 = repository.findById(1L); // Cache hit
        Customer c3 = repository.findById(1L); // Cache hit
        
        System.out.println("Same instance? " + (c1 == c2) + " and " + (c2 == c3));
        System.out.println("Cache stats: " + repository.getCacheStatistics());
        
        // Example 4: Session-Level Identity Map
        System.out.println("\n4. Session-Level Identity Map:");
        SessionIdentityMap session = new SessionIdentityMap();
        
        Person person = new Person(1L, "Bob", "bob@example.com");
        Product product = new Product(2L, "Phone", new BigDecimal("599.99"));
        
        session.put(person);
        session.put(product);
        
        Person retrievedPerson = session.get(Person.class, 1L);
        Product retrievedProduct = session.get(Product.class, 2L);
        
        System.out.println("Total cached entities: " + session.getTotalSize());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Ensures object identity (same row = same instance)");
        System.out.println("✓ Prevents duplicate loading");
        System.out.println("✓ Acts as cache for database reads");
        System.out.println("✓ Improves performance");
        System.out.println("✓ Essential for Unit of Work pattern");
        
        System.out.println("\n=== Considerations ===");
        System.out.println("• Need to clear map when transaction ends");
        System.out.println("• Memory usage grows with loaded objects");
        System.out.println("• Must handle concurrent access in multi-threaded environments");
    }
}
