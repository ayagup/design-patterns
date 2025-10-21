package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * CACHE-ASIDE PATTERN (Lazy Loading)
 * 
 * Application code directly manages cache and data store. On cache miss,
 * the application loads data from the store and updates the cache.
 * 
 * Benefits:
 * - Application controls caching strategy
 * - Only requested data is cached
 * - Cache failures don't block data access
 * - Simple to implement
 * - Flexible cache invalidation
 * 
 * Use Cases:
 * - Read-heavy workloads
 * - Database query caching
 * - API response caching
 * - Expensive computation results
 * - Reference data caching
 */

// Cache interface
interface Cache<K, V> {
    V get(K key);
    void put(K key, V value);
    void invalidate(K key);
    void clear();
    boolean contains(K key);
}

// Simple in-memory cache implementation
class InMemoryCache<K, V> implements Cache<K, V> {
    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;
    
    public InMemoryCache(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }
    
    @Override
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        
        if (entry == null) {
            return null;
        }
        
        if (entry.isExpired()) {
            System.out.println("  [CACHE] Entry expired for key: " + key);
            cache.remove(key);
            return null;
        }
        
        System.out.println("  [CACHE] Hit for key: " + key);
        return entry.value;
    }
    
    @Override
    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis() + ttlMillis));
        System.out.println("  [CACHE] Stored key: " + key);
    }
    
    @Override
    public void invalidate(K key) {
        cache.remove(key);
        System.out.println("  [CACHE] Invalidated key: " + key);
    }
    
    @Override
    public void clear() {
        cache.clear();
        System.out.println("  [CACHE] Cleared all entries");
    }
    
    @Override
    public boolean contains(K key) {
        CacheEntry<V> entry = cache.get(key);
        return entry != null && !entry.isExpired();
    }
    
    public int size() {
        return cache.size();
    }
    
    private static class CacheEntry<V> {
        final V value;
        final long expiryTime;
        
        CacheEntry(V value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}

// Example 1: Product Repository with Cache-Aside
class Product {
    private final String id;
    private final String name;
    private final double price;
    private final String category;
    
    public Product(String id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    
    @Override
    public String toString() {
        return String.format("Product[id=%s, name=%s, price=$%.2f, category=%s]",
            id, name, price, category);
    }
}

class ProductRepository {
    private final Map<String, Product> database = new ConcurrentHashMap<>();
    private final Cache<String, Product> cache;
    
    public ProductRepository(Cache<String, Product> cache) {
        this.cache = cache;
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        database.put("P1", new Product("P1", "Laptop", 999.99, "Electronics"));
        database.put("P2", new Product("P2", "Phone", 699.99, "Electronics"));
        database.put("P3", new Product("P3", "Desk", 399.99, "Furniture"));
    }
    
    public Product findById(String id) {
        // Try cache first (Cache-Aside pattern)
        Product product = cache.get(id);
        
        if (product != null) {
            return product;
        }
        
        // Cache miss - load from database
        System.out.println("  [CACHE] Miss for key: " + id);
        System.out.println("  [DB] Loading from database: " + id);
        
        product = database.get(id);
        
        if (product != null) {
            // Update cache
            cache.put(id, product);
        }
        
        return product;
    }
    
    public void update(Product product) {
        System.out.println("  [DB] Updating product: " + product.getId());
        database.put(product.getId(), product);
        
        // Invalidate cache to maintain consistency
        cache.invalidate(product.getId());
    }
    
    public void delete(String id) {
        System.out.println("  [DB] Deleting product: " + id);
        database.remove(id);
        cache.invalidate(id);
    }
}

// Example 2: User Service with Cache-Aside
class User {
    private final String id;
    private String name;
    private String email;
    
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, email=%s]", id, name, email);
    }
}

class UserService {
    private final Map<String, User> database = new ConcurrentHashMap<>();
    private final Cache<String, User> cache;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    public UserService(Cache<String, User> cache) {
        this.cache = cache;
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        database.put("U1", new User("U1", "Alice", "alice@example.com"));
        database.put("U2", new User("U2", "Bob", "bob@example.com"));
        database.put("U3", new User("U3", "Charlie", "charlie@example.com"));
    }
    
    public CompletableFuture<User> findByIdAsync(String id) {
        return CompletableFuture.supplyAsync(() -> {
            // Check cache
            User user = cache.get(id);
            
            if (user != null) {
                return user;
            }
            
            // Load from database
            System.out.println("  [CACHE] Miss for key: " + id);
            System.out.println("  [DB] Loading from database: " + id);
            
            try {
                Thread.sleep(100); // Simulate database latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            user = database.get(id);
            
            if (user != null) {
                cache.put(id, user);
            }
            
            return user;
        }, executor);
    }
    
    public void update(User user) {
        System.out.println("  [DB] Updating user: " + user.getId());
        database.put(user.getId(), user);
        cache.invalidate(user.getId());
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Example 3: Query Result Cache
class QueryResult {
    private final String query;
    private final List<String> results;
    private final long executionTime;
    
    public QueryResult(String query, List<String> results, long executionTime) {
        this.query = query;
        this.results = results;
        this.executionTime = executionTime;
    }
    
    public String getQuery() { return query; }
    public List<String> getResults() { return new ArrayList<>(results); }
    public long getExecutionTime() { return executionTime; }
    
    @Override
    public String toString() {
        return String.format("QueryResult[query='%s', results=%d, time=%dms]",
            query, results.size(), executionTime);
    }
}

class DatabaseQueryService {
    private final Cache<String, QueryResult> queryCache;
    
    public DatabaseQueryService(Cache<String, QueryResult> queryCache) {
        this.queryCache = queryCache;
    }
    
    public QueryResult executeQuery(String query) {
        // Check cache
        QueryResult cached = queryCache.get(query);
        
        if (cached != null) {
            return cached;
        }
        
        // Execute query
        System.out.println("  [CACHE] Miss for query: " + query);
        System.out.println("  [DB] Executing query: " + query);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Thread.sleep(200); // Simulate query execution
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulated results
        List<String> results = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            results.add("Result-" + i);
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        QueryResult result = new QueryResult(query, results, executionTime);
        
        // Cache the result
        queryCache.put(query, result);
        
        return result;
    }
    
    public void invalidateQuery(String query) {
        queryCache.invalidate(query);
    }
}

// Example 4: Write-Through Cache (comparison)
class WriteThroughCache<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();
    private final Map<K, V> database;
    
    public WriteThroughCache(Map<K, V> database) {
        this.database = database;
    }
    
    public V get(K key) {
        // Always try cache first
        V value = cache.get(key);
        
        if (value != null) {
            System.out.println("  [WRITE-THROUGH] Cache hit: " + key);
            return value;
        }
        
        // Load from database
        System.out.println("  [WRITE-THROUGH] Cache miss: " + key);
        value = database.get(key);
        
        if (value != null) {
            cache.put(key, value);
        }
        
        return value;
    }
    
    public void put(K key, V value) {
        // Write to database first
        System.out.println("  [WRITE-THROUGH] Writing to database: " + key);
        database.put(key, value);
        
        // Then update cache
        System.out.println("  [WRITE-THROUGH] Updating cache: " + key);
        cache.put(key, value);
    }
}

// Demo
public class CacheAsidePattern {
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     CACHE-ASIDE PATTERN DEMONSTRATION    ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Example 1: Product Repository
        System.out.println("1. PRODUCT REPOSITORY WITH CACHE-ASIDE");
        System.out.println("───────────────────────────────────────");
        
        Cache<String, Product> productCache = new InMemoryCache<>(5000);
        ProductRepository productRepo = new ProductRepository(productCache);
        
        System.out.println("First access (cache miss):");
        Product p1 = productRepo.findById("P1");
        System.out.println("Retrieved: " + p1);
        
        System.out.println("\nSecond access (cache hit):");
        Product p1Again = productRepo.findById("P1");
        System.out.println("Retrieved: " + p1Again);
        
        System.out.println("\nUpdating product:");
        Product updated = new Product("P1", "Gaming Laptop", 1299.99, "Electronics");
        productRepo.update(updated);
        
        System.out.println("\nAccess after update (cache invalidated):");
        Product p1Updated = productRepo.findById("P1");
        System.out.println("Retrieved: " + p1Updated);
        
        // Example 2: Async User Service
        System.out.println("\n2. ASYNC USER SERVICE WITH CACHE-ASIDE");
        System.out.println("───────────────────────────────────────");
        
        Cache<String, User> userCache = new InMemoryCache<>(5000);
        UserService userService = new UserService(userCache);
        
        System.out.println("Concurrent async requests:");
        List<CompletableFuture<User>> futures = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            futures.add(userService.findByIdAsync("U1"));
            futures.add(userService.findByIdAsync("U2"));
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        
        System.out.println("\nAll requests completed");
        
        // Example 3: Query Result Cache
        System.out.println("\n3. QUERY RESULT CACHE");
        System.out.println("─────────────────────");
        
        Cache<String, QueryResult> queryCache = new InMemoryCache<>(10000);
        DatabaseQueryService queryService = new DatabaseQueryService(queryCache);
        
        String query = "SELECT * FROM products WHERE category = 'Electronics'";
        
        System.out.println("First query execution:");
        QueryResult result1 = queryService.executeQuery(query);
        System.out.println("Result: " + result1);
        
        System.out.println("\nSecond query execution (cached):");
        long start = System.currentTimeMillis();
        QueryResult result2 = queryService.executeQuery(query);
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("Result: " + result2);
        System.out.println("Time: " + elapsed + "ms (vs " + result1.getExecutionTime() + "ms)");
        
        // Example 4: Cache Expiration
        System.out.println("\n4. CACHE EXPIRATION (TTL)");
        System.out.println("──────────────────────────");
        
        Cache<String, String> shortTtlCache = new InMemoryCache<>(1000); // 1 second TTL
        
        shortTtlCache.put("key1", "value1");
        System.out.println("Stored: key1 -> value1");
        
        System.out.println("\nImmediate retrieval:");
        System.out.println("Retrieved: " + shortTtlCache.get("key1"));
        
        System.out.println("\nWaiting 1.5 seconds...");
        Thread.sleep(1500);
        
        System.out.println("Retrieval after expiration:");
        String expired = shortTtlCache.get("key1");
        System.out.println("Retrieved: " + (expired == null ? "null (expired)" : expired));
        
        // Example 5: Write-Through vs Cache-Aside
        System.out.println("\n5. WRITE-THROUGH vs CACHE-ASIDE");
        System.out.println("────────────────────────────────");
        
        Map<String, String> database = new ConcurrentHashMap<>();
        WriteThroughCache<String, String> writeThrough = new WriteThroughCache<>(database);
        
        System.out.println("Write-Through Cache PUT:");
        writeThrough.put("data1", "value1");
        
        System.out.println("\nWrite-Through Cache GET:");
        writeThrough.get("data1");
        
        userService.shutdown();
        
        System.out.println("\n✅ Cache-Aside Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Reduced database load through caching");
        System.out.println("  • Application controls caching strategy");
        System.out.println("  • Only requested data is cached (lazy loading)");
        System.out.println("  • Cache invalidation on updates");
        System.out.println("  • TTL-based expiration for freshness");
    }
}
