package enterprise;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

/**
 * Data Gateway Pattern
 * 
 * Intent: An object that encapsulates access to an external system or
 * resource. All interactions with the external resource go through the gateway.
 * 
 * Motivation:
 * Centralizes access to external data sources.
 * Provides clean API for data access.
 * Can swap implementations (database, web service, file, etc.).
 * Isolates database-specific code.
 * 
 * Applicability:
 * - Need to isolate database code
 * - Working with external APIs
 * - Want to mock data access in tests
 * - Multiple data sources
 */

/**
 * Example 1: Generic Database Gateway
 * 
 * Provides common database operations
 */
interface DataGateway<T> {
    T findById(Long id);
    List<T> findAll();
    void insert(T entity);
    void update(T entity);
    void delete(Long id);
}

class UserGateway implements DataGateway<User> {
    private final Connection connection;
    
    public UserGateway(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public User findById(Long id) {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        
        return null;
    }
    
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email FROM users";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapToUser(rs));
            }
            System.out.println("  [UserGateway] Found " + users.size() + " users");
        } catch (SQLException e) {
            System.err.println("Error finding users: " + e.getMessage());
        }
        
        return users;
    }
    
    @Override
    public void insert(User user) {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            
            System.out.println("  [UserGateway] Inserted user: " + user.getUsername());
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }
    }
    
    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setLong(3, user.getId());
            stmt.executeUpdate();
            
            System.out.println("  [UserGateway] Updated user: " + user.getId());
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
            System.out.println("  [UserGateway] Deleted user: " + id);
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }
    
    private User mapToUser(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        return new User(id, username, email);
    }
}

class User {
    private Long id;
    private String username;
    private String email;
    
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return "User[" + id + "]: " + username + " (" + email + ")";
    }
}

/**
 * Example 2: In-Memory Data Gateway
 * 
 * Gateway implementation using in-memory storage
 */
class InMemoryDataGateway<T> implements DataGateway<T> {
    private final Map<Long, T> storage;
    private final Function<T, Long> idExtractor;
    private long nextId;
    
    public InMemoryDataGateway(Function<T, Long> idExtractor) {
        this.storage = new HashMap<>();
        this.idExtractor = idExtractor;
        this.nextId = 1L;
    }
    
    @Override
    public T findById(Long id) {
        T entity = storage.get(id);
        if (entity != null) {
            System.out.println("  [InMemoryGateway] Found entity with ID: " + id);
        }
        return entity;
    }
    
    @Override
    public List<T> findAll() {
        List<T> all = new ArrayList<>(storage.values());
        System.out.println("  [InMemoryGateway] Found " + all.size() + " entities");
        return all;
    }
    
    @Override
    public void insert(T entity) {
        Long id = nextId++;
        storage.put(id, entity);
        System.out.println("  [InMemoryGateway] Inserted entity with ID: " + id);
    }
    
    @Override
    public void update(T entity) {
        Long id = idExtractor.apply(entity);
        if (storage.containsKey(id)) {
            storage.put(id, entity);
            System.out.println("  [InMemoryGateway] Updated entity: " + id);
        } else {
            System.out.println("  [InMemoryGateway] Entity not found: " + id);
        }
    }
    
    @Override
    public void delete(Long id) {
        storage.remove(id);
        System.out.println("  [InMemoryGateway] Deleted entity: " + id);
    }
}

/**
 * Example 3: REST API Gateway
 * 
 * Gateway that wraps REST API calls
 */
class RestApiGateway {
    private final String baseUrl;
    
    public RestApiGateway(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public ApiResponse get(String endpoint) {
        String url = baseUrl + endpoint;
        System.out.println("  [RestGateway] GET " + url);
        
        // Simulate HTTP GET request
        return new ApiResponse(200, "{\"status\": \"success\"}");
    }
    
    public ApiResponse post(String endpoint, String body) {
        String url = baseUrl + endpoint;
        System.out.println("  [RestGateway] POST " + url);
        System.out.println("  [RestGateway] Body: " + body);
        
        // Simulate HTTP POST request
        return new ApiResponse(201, "{\"id\": 123}");
    }
    
    public ApiResponse put(String endpoint, String body) {
        String url = baseUrl + endpoint;
        System.out.println("  [RestGateway] PUT " + url);
        
        // Simulate HTTP PUT request
        return new ApiResponse(200, "{\"updated\": true}");
    }
    
    public ApiResponse delete(String endpoint) {
        String url = baseUrl + endpoint;
        System.out.println("  [RestGateway] DELETE " + url);
        
        // Simulate HTTP DELETE request
        return new ApiResponse(204, "");
    }
}

class ApiResponse {
    private final int statusCode;
    private final String body;
    
    public ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
    
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
    
    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}

/**
 * Example 4: File System Gateway
 * 
 * Gateway for file operations
 */
class FileSystemGateway {
    private final String basePath;
    
    public FileSystemGateway(String basePath) {
        this.basePath = basePath;
    }
    
    public String readFile(String filename) {
        String path = basePath + "/" + filename;
        System.out.println("  [FileGateway] Reading: " + path);
        
        // Simulate file read
        return "File content from " + filename;
    }
    
    public void writeFile(String filename, String content) {
        String path = basePath + "/" + filename;
        System.out.println("  [FileGateway] Writing to: " + path);
        System.out.println("  [FileGateway] Content length: " + content.length());
    }
    
    public boolean deleteFile(String filename) {
        String path = basePath + "/" + filename;
        System.out.println("  [FileGateway] Deleting: " + path);
        return true;
    }
    
    public List<String> listFiles() {
        System.out.println("  [FileGateway] Listing files in: " + basePath);
        
        // Simulate directory listing
        return Arrays.asList("file1.txt", "file2.txt", "file3.txt");
    }
}

/**
 * Example 5: Cache-Enabled Gateway
 * 
 * Gateway with caching layer
 */
class CachedDataGateway<T> implements DataGateway<T> {
    private final DataGateway<T> delegate;
    private final Map<Long, T> cache;
    private final long cacheTtlMillis;
    private final Map<Long, Long> cacheTimestamps;
    
    public CachedDataGateway(DataGateway<T> delegate, long cacheTtlMillis) {
        this.delegate = delegate;
        this.cache = new HashMap<>();
        this.cacheTimestamps = new HashMap<>();
        this.cacheTtlMillis = cacheTtlMillis;
    }
    
    @Override
    public T findById(Long id) {
        // Check cache
        if (cache.containsKey(id)) {
            Long timestamp = cacheTimestamps.get(id);
            if (System.currentTimeMillis() - timestamp < cacheTtlMillis) {
                System.out.println("  [CachedGateway] Cache HIT for ID: " + id);
                return cache.get(id);
            } else {
                System.out.println("  [CachedGateway] Cache EXPIRED for ID: " + id);
                cache.remove(id);
                cacheTimestamps.remove(id);
            }
        }
        
        // Cache miss - fetch from delegate
        System.out.println("  [CachedGateway] Cache MISS for ID: " + id);
        T entity = delegate.findById(id);
        
        if (entity != null) {
            cache.put(id, entity);
            cacheTimestamps.put(id, System.currentTimeMillis());
        }
        
        return entity;
    }
    
    @Override
    public List<T> findAll() {
        return delegate.findAll();
    }
    
    @Override
    public void insert(T entity) {
        delegate.insert(entity);
    }
    
    @Override
    public void update(T entity) {
        delegate.update(entity);
        // Invalidate cache (or update it)
        cache.clear();
        cacheTimestamps.clear();
        System.out.println("  [CachedGateway] Cache invalidated");
    }
    
    @Override
    public void delete(Long id) {
        delegate.delete(id);
        cache.remove(id);
        cacheTimestamps.remove(id);
        System.out.println("  [CachedGateway] Removed from cache: " + id);
    }
}

/**
 * Demonstration of the Data Gateway Pattern
 */
public class DataGatewayPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Data Gateway Pattern Demo ===\n");
        
        // Example 1: Database Gateway (simulated)
        System.out.println("1. Database Gateway (simulated):");
        System.out.println("  [Simulated] UserGateway would handle:");
        System.out.println("  - findById(): SELECT query");
        System.out.println("  - findAll(): SELECT all rows");
        System.out.println("  - insert(): INSERT statement");
        System.out.println("  - update(): UPDATE statement");
        System.out.println("  - delete(): DELETE statement");
        
        // Example 2: In-Memory Gateway
        System.out.println("\n2. In-Memory Data Gateway:");
        InMemoryDataGateway<User> inMemoryGateway = 
            new InMemoryDataGateway<>(User::getId);
        
        User user1 = new User(null, "alice", "alice@example.com");
        inMemoryGateway.insert(user1);
        
        User user2 = new User(null, "bob", "bob@example.com");
        inMemoryGateway.insert(user2);
        
        List<User> allUsers = inMemoryGateway.findAll();
        
        // Example 3: REST API Gateway
        System.out.println("\n3. REST API Gateway:");
        RestApiGateway restGateway = new RestApiGateway("https://api.example.com");
        
        ApiResponse getResp = restGateway.get("/users/123");
        System.out.println("  Response: " + getResp.getStatusCode() + " - " + getResp.getBody());
        
        ApiResponse postResp = restGateway.post("/users", "{\"name\": \"Charlie\"}");
        System.out.println("  Response: " + postResp.getStatusCode() + " - " + postResp.getBody());
        
        // Example 4: File System Gateway
        System.out.println("\n4. File System Gateway:");
        FileSystemGateway fileGateway = new FileSystemGateway("/data");
        
        String content = fileGateway.readFile("config.json");
        fileGateway.writeFile("output.txt", "Hello, World!");
        List<String> files = fileGateway.listFiles();
        System.out.println("  Files: " + files);
        
        // Example 5: Cached Gateway
        System.out.println("\n5. Cached Data Gateway:");
        InMemoryDataGateway<User> baseGateway = new InMemoryDataGateway<>(User::getId);
        User user3 = new User(1L, "dave", "dave@example.com");
        baseGateway.insert(user3);
        
        CachedDataGateway<User> cachedGateway = 
            new CachedDataGateway<>(baseGateway, 5000); // 5 second TTL
        
        cachedGateway.findById(1L); // Cache miss
        cachedGateway.findById(1L); // Cache hit
        cachedGateway.findById(1L); // Cache hit
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Centralizes data access");
        System.out.println("✓ Isolates external dependencies");
        System.out.println("✓ Easy to mock for testing");
        System.out.println("✓ Can swap implementations");
        System.out.println("✓ Consistent API across data sources");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Database access layer");
        System.out.println("• REST API clients");
        System.out.println("• File system operations");
        System.out.println("• Message queue interfaces");
        System.out.println("• Cache management");
    }
}
