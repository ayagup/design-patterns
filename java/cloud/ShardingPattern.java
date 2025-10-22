package cloud;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Sharding Pattern
 * 
 * Intent: Divide a data store into a set of horizontal partitions (shards).
 * This can improve scalability when storing and accessing large volumes of data.
 * 
 * Also Known As:
 * - Horizontal Partitioning
 * - Data Partitioning
 * - Database Sharding
 * 
 * Motivation:
 * As data grows, a single database server may face:
 * - Storage capacity limitations
 * - Performance bottlenecks
 * - Increased query latency
 * - Difficulty scaling vertically
 * - High costs for powerful single servers
 * 
 * Applicability:
 * - Very large datasets
 * - High throughput requirements
 * - Need for horizontal scalability
 * - Data can be logically partitioned
 * - Geographic distribution requirements
 * 
 * Benefits:
 * - Improved scalability (horizontal)
 * - Better performance (parallel queries)
 * - Reduced contention
 * - Cost-effective (commodity hardware)
 * - Geographic data locality
 * - Fault isolation
 * 
 * Trade-offs:
 * - Increased complexity
 * - Cross-shard queries are expensive
 * - Rebalancing can be difficult
 * - Transactions across shards are complex
 * - Hotspot shards possible
 */

// Data model
class User {
    private final String userId;
    private final String name;
    private final String email;
    private final String region;
    
    public User(String userId, String name, String email, String region) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.region = region;
    }
    
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRegion() { return region; }
    
    @Override
    public String toString() {
        return String.format("User[%s: %s, %s, %s]", userId, name, email, region);
    }
}

// Shard
class Shard<K, V> {
    private final String shardId;
    private final Map<K, V> data;
    private int accessCount;
    
    public Shard(String shardId) {
        this.shardId = shardId;
        this.data = new ConcurrentHashMap<>();
        this.accessCount = 0;
    }
    
    public void put(K key, V value) {
        data.put(key, value);
        accessCount++;
    }
    
    public V get(K key) {
        accessCount++;
        return data.get(key);
    }
    
    public boolean containsKey(K key) {
        return data.containsKey(key);
    }
    
    public Collection<V> getAll() {
        return new ArrayList<>(data.values());
    }
    
    public String getShardId() { return shardId; }
    public int getSize() { return data.size(); }
    public int getAccessCount() { return accessCount; }
    
    @Override
    public String toString() {
        return String.format("Shard[%s: %d items, %d accesses]", shardId, getSize(), accessCount);
    }
}

// Example 1: Hash-Based Sharding
// Distributes data using hash of key
class HashBasedSharding<K, V> {
    private final List<Shard<K, V>> shards;
    private final int shardCount;
    
    public HashBasedSharding(int shardCount) {
        this.shardCount = shardCount;
        this.shards = new ArrayList<>();
        
        for (int i = 0; i < shardCount; i++) {
            shards.add(new Shard<>("shard-" + i));
        }
    }
    
    private int getShardIndex(K key) {
        // Use hash code to determine shard
        int hash = key.hashCode();
        return Math.abs(hash % shardCount);
    }
    
    public void put(K key, V value) {
        int shardIndex = getShardIndex(key);
        Shard<K, V> shard = shards.get(shardIndex);
        shard.put(key, value);
        
        System.out.println(String.format("Stored in %s (hash: %d)", 
            shard.getShardId(), shardIndex));
    }
    
    public V get(K key) {
        int shardIndex = getShardIndex(key);
        Shard<K, V> shard = shards.get(shardIndex);
        V value = shard.get(key);
        
        System.out.println(String.format("Retrieved from %s", shard.getShardId()));
        return value;
    }
    
    public List<V> getAll() {
        List<V> allValues = new ArrayList<>();
        for (Shard<K, V> shard : shards) {
            allValues.addAll(shard.getAll());
        }
        return allValues;
    }
    
    public void printShardDistribution() {
        System.out.println("\nHash-Based Shard Distribution:");
        for (Shard<K, V> shard : shards) {
            System.out.println("  " + shard);
        }
    }
}

// Example 2: Range-Based Sharding
// Partitions data based on key ranges
class RangeBasedSharding {
    private final Map<String, Shard<String, User>> shardMap;
    private final List<ShardRange> ranges;
    
    static class ShardRange {
        final String shardId;
        final String startKey;
        final String endKey;
        
        public ShardRange(String shardId, String startKey, String endKey) {
            this.shardId = shardId;
            this.startKey = startKey;
            this.endKey = endKey;
        }
        
        public boolean contains(String key) {
            return key.compareTo(startKey) >= 0 && key.compareTo(endKey) < 0;
        }
    }
    
    public RangeBasedSharding() {
        this.shardMap = new HashMap<>();
        this.ranges = new ArrayList<>();
        
        // Define ranges (e.g., A-F, G-M, N-Z)
        addShardRange("shard-1", "A", "G");
        addShardRange("shard-2", "G", "N");
        addShardRange("shard-3", "N", "ZZZ");
    }
    
    private void addShardRange(String shardId, String startKey, String endKey) {
        shardMap.put(shardId, new Shard<>(shardId));
        ranges.add(new ShardRange(shardId, startKey, endKey));
    }
    
    private String getShardId(String key) {
        for (ShardRange range : ranges) {
            if (range.contains(key)) {
                return range.shardId;
            }
        }
        return ranges.get(ranges.size() - 1).shardId;  // Default to last shard
    }
    
    public void put(User user) {
        String key = user.getName().substring(0, 1).toUpperCase();
        String shardId = getShardId(key);
        Shard<String, User> shard = shardMap.get(shardId);
        shard.put(user.getUserId(), user);
        
        System.out.println(String.format("Stored %s in %s (name starts with '%s')", 
            user.getName(), shardId, key));
    }
    
    public User get(String userId, String nameHint) {
        String key = nameHint.substring(0, 1).toUpperCase();
        String shardId = getShardId(key);
        Shard<String, User> shard = shardMap.get(shardId);
        return shard.get(userId);
    }
    
    public List<User> getUsersInRange(String startName, String endName) {
        System.out.println(String.format("\nQuerying range: %s to %s", startName, endName));
        
        List<User> results = new ArrayList<>();
        for (ShardRange range : ranges) {
            // Check if range overlaps with query
            if (range.endKey.compareTo(startName) > 0 && range.startKey.compareTo(endName) < 0) {
                Shard<String, User> shard = shardMap.get(range.shardId);
                System.out.println("  Querying " + range.shardId);
                results.addAll(shard.getAll().stream()
                    .filter(u -> u.getName().compareTo(startName) >= 0 && 
                                u.getName().compareTo(endName) < 0)
                    .collect(Collectors.toList()));
            }
        }
        return results;
    }
    
    public void printShardDistribution() {
        System.out.println("\nRange-Based Shard Distribution:");
        for (ShardRange range : ranges) {
            Shard<String, User> shard = shardMap.get(range.shardId);
            System.out.println(String.format("  %s [%s - %s]: %d users", 
                range.shardId, range.startKey, range.endKey, shard.getSize()));
        }
    }
}

// Example 3: Geographic Sharding
// Partitions data by geographic region
class GeographicSharding {
    private final Map<String, Shard<String, User>> regionShards;
    
    public GeographicSharding() {
        this.regionShards = new HashMap<>();
        
        // Create shards for different regions
        regionShards.put("US-EAST", new Shard<>("us-east-1"));
        regionShards.put("US-WEST", new Shard<>("us-west-1"));
        regionShards.put("EU", new Shard<>("eu-west-1"));
        regionShards.put("ASIA", new Shard<>("ap-south-1"));
    }
    
    public void put(User user) {
        String region = user.getRegion();
        Shard<String, User> shard = regionShards.get(region);
        
        if (shard == null) {
            // Default to US-EAST if region not found
            region = "US-EAST";
            shard = regionShards.get(region);
        }
        
        shard.put(user.getUserId(), user);
        System.out.println(String.format("Stored %s in %s (region: %s)", 
            user.getName(), shard.getShardId(), region));
    }
    
    public User get(String userId, String region) {
        Shard<String, User> shard = regionShards.get(region);
        if (shard == null) return null;
        return shard.get(userId);
    }
    
    public List<User> getUsersByRegion(String region) {
        Shard<String, User> shard = regionShards.get(region);
        if (shard == null) return new ArrayList<>();
        return new ArrayList<>(shard.getAll());
    }
    
    public void printShardDistribution() {
        System.out.println("\nGeographic Shard Distribution:");
        for (Map.Entry<String, Shard<String, User>> entry : regionShards.entrySet()) {
            Shard<String, User> shard = entry.getValue();
            System.out.println(String.format("  %s (%s): %d users", 
                entry.getKey(), shard.getShardId(), shard.getSize()));
        }
    }
}

// Example 4: Consistent Hashing
// Minimizes data movement when adding/removing shards
class ConsistentHashingSharding<K, V> {
    private final TreeMap<Integer, Shard<K, V>> ring;
    private final int virtualNodesPerShard;
    private int shardCounter = 0;
    
    public ConsistentHashingSharding(int virtualNodesPerShard) {
        this.ring = new TreeMap<>();
        this.virtualNodesPerShard = virtualNodesPerShard;
    }
    
    public void addShard(String shardId) {
        Shard<K, V> shard = new Shard<>(shardId);
        
        // Add virtual nodes
        for (int i = 0; i < virtualNodesPerShard; i++) {
            String virtualNodeKey = shardId + "-vnode-" + i;
            int hash = virtualNodeKey.hashCode();
            ring.put(hash, shard);
        }
        
        shardCounter++;
        System.out.println(String.format("Added shard: %s with %d virtual nodes", 
            shardId, virtualNodesPerShard));
    }
    
    public void removeShard(String shardId) {
        // Remove all virtual nodes for this shard
        ring.entrySet().removeIf(entry -> entry.getValue().getShardId().equals(shardId));
        System.out.println(String.format("Removed shard: %s", shardId));
    }
    
    private Shard<K, V> getShard(K key) {
        if (ring.isEmpty()) return null;
        
        int hash = key.hashCode();
        
        // Find first shard with hash >= key hash
        Map.Entry<Integer, Shard<K, V>> entry = ring.ceilingEntry(hash);
        
        // Wrap around if needed
        if (entry == null) {
            entry = ring.firstEntry();
        }
        
        return entry.getValue();
    }
    
    public void put(K key, V value) {
        Shard<K, V> shard = getShard(key);
        if (shard != null) {
            shard.put(key, value);
            System.out.println(String.format("Stored in %s", shard.getShardId()));
        }
    }
    
    public V get(K key) {
        Shard<K, V> shard = getShard(key);
        if (shard != null) {
            return shard.get(key);
        }
        return null;
    }
    
    public void printDistribution() {
        System.out.println("\nConsistent Hashing Distribution:");
        Map<String, Integer> counts = new HashMap<>();
        
        for (Shard<K, V> shard : ring.values()) {
            counts.put(shard.getShardId(), 
                counts.getOrDefault(shard.getShardId(), 0) + shard.getSize());
        }
        
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println(String.format("  %s: %d items", entry.getKey(), entry.getValue()));
        }
    }
}

// Example 5: Lookup-Based Sharding
// Uses external lookup table to map entities to shards
class LookupBasedSharding {
    private final Map<String, Shard<String, User>> shardMap;
    private final Map<String, String> userToShardMapping;  // userId -> shardId
    
    public LookupBasedSharding() {
        this.shardMap = new HashMap<>();
        this.userToShardMapping = new ConcurrentHashMap<>();
        
        // Create shards
        for (int i = 1; i <= 3; i++) {
            shardMap.put("shard-" + i, new Shard<>("shard-" + i));
        }
    }
    
    public void put(User user, String targetShardId) {
        Shard<String, User> shard = shardMap.get(targetShardId);
        if (shard == null) {
            throw new IllegalArgumentException("Shard not found: " + targetShardId);
        }
        
        shard.put(user.getUserId(), user);
        userToShardMapping.put(user.getUserId(), targetShardId);
        
        System.out.println(String.format("Stored %s in %s (explicit mapping)", 
            user.getName(), targetShardId));
    }
    
    public User get(String userId) {
        String shardId = userToShardMapping.get(userId);
        if (shardId == null) return null;
        
        Shard<String, User> shard = shardMap.get(shardId);
        return shard.get(userId);
    }
    
    public void moveUserToShard(String userId, String newShardId) {
        String oldShardId = userToShardMapping.get(userId);
        if (oldShardId == null) return;
        
        // Get user from old shard
        Shard<String, User> oldShard = shardMap.get(oldShardId);
        User user = oldShard.get(userId);
        
        if (user != null) {
            // Move to new shard
            Shard<String, User> newShard = shardMap.get(newShardId);
            newShard.put(userId, user);
            userToShardMapping.put(userId, newShardId);
            
            System.out.println(String.format("Moved user %s from %s to %s", 
                userId, oldShardId, newShardId));
        }
    }
    
    public void printShardDistribution() {
        System.out.println("\nLookup-Based Shard Distribution:");
        for (Shard<String, User> shard : shardMap.values()) {
            System.out.println("  " + shard);
        }
        System.out.println(String.format("  Total mappings: %d", userToShardMapping.size()));
    }
}

// Demo
public class ShardingPattern {
    public static void main(String[] args) {
        demonstrateHashBasedSharding();
        demonstrateRangeBasedSharding();
        demonstrateGeographicSharding();
        demonstrateConsistentHashing();
        demonstrateLookupBasedSharding();
    }
    
    private static void demonstrateHashBasedSharding() {
        System.out.println("=== Hash-Based Sharding ===\n");
        
        HashBasedSharding<String, User> sharding = new HashBasedSharding<>(4);
        
        // Store users
        sharding.put("U001", new User("U001", "Alice", "alice@example.com", "US-EAST"));
        sharding.put("U002", new User("U002", "Bob", "bob@example.com", "US-WEST"));
        sharding.put("U003", new User("U003", "Charlie", "charlie@example.com", "EU"));
        sharding.put("U004", new User("U004", "Diana", "diana@example.com", "ASIA"));
        sharding.put("U005", new User("U005", "Eve", "eve@example.com", "US-EAST"));
        
        sharding.printShardDistribution();
    }
    
    private static void demonstrateRangeBasedSharding() {
        System.out.println("\n\n=== Range-Based Sharding ===\n");
        
        RangeBasedSharding sharding = new RangeBasedSharding();
        
        sharding.put(new User("U001", "Alice", "alice@example.com", "US-EAST"));
        sharding.put(new User("U002", "Bob", "bob@example.com", "US-WEST"));
        sharding.put(new User("U003", "Charlie", "charlie@example.com", "EU"));
        sharding.put(new User("U004", "Mike", "mike@example.com", "ASIA"));
        sharding.put(new User("U005", "Zoe", "zoe@example.com", "US-EAST"));
        
        sharding.printShardDistribution();
        
        // Range query
        List<User> users = sharding.getUsersInRange("A", "D");
        System.out.println(String.format("Found %d users in range A-D", users.size()));
    }
    
    private static void demonstrateGeographicSharding() {
        System.out.println("\n\n=== Geographic Sharding ===\n");
        
        GeographicSharding sharding = new GeographicSharding();
        
        sharding.put(new User("U001", "Alice", "alice@example.com", "US-EAST"));
        sharding.put(new User("U002", "Bob", "bob@example.com", "US-WEST"));
        sharding.put(new User("U003", "Charlie", "charlie@example.com", "EU"));
        sharding.put(new User("U004", "Diana", "diana@example.com", "ASIA"));
        sharding.put(new User("U005", "Eve", "eve@example.com", "US-EAST"));
        
        sharding.printShardDistribution();
        
        System.out.println("\nUS-EAST users:");
        List<User> usEastUsers = sharding.getUsersByRegion("US-EAST");
        usEastUsers.forEach(u -> System.out.println("  " + u.getName()));
    }
    
    private static void demonstrateConsistentHashing() {
        System.out.println("\n\n=== Consistent Hashing ===\n");
        
        ConsistentHashingSharding<String, User> sharding = new ConsistentHashingSharding<>(3);
        
        // Add initial shards
        sharding.addShard("shard-1");
        sharding.addShard("shard-2");
        sharding.addShard("shard-3");
        
        // Store data
        System.out.println("\nStoring users:");
        for (int i = 1; i <= 10; i++) {
            String userId = "U" + String.format("%03d", i);
            sharding.put(userId, new User(userId, "User" + i, "user" + i + "@example.com", "US"));
        }
        
        sharding.printDistribution();
        
        // Add new shard
        System.out.println("\nAdding new shard:");
        sharding.addShard("shard-4");
        
        // Store more data (will be distributed including new shard)
        for (int i = 11; i <= 15; i++) {
            String userId = "U" + String.format("%03d", i);
            sharding.put(userId, new User(userId, "User" + i, "user" + i + "@example.com", "US"));
        }
        
        sharding.printDistribution();
    }
    
    private static void demonstrateLookupBasedSharding() {
        System.out.println("\n\n=== Lookup-Based Sharding ===\n");
        
        LookupBasedSharding sharding = new LookupBasedSharding();
        
        // Explicitly assign users to shards
        sharding.put(new User("U001", "Alice", "alice@example.com", "US-EAST"), "shard-1");
        sharding.put(new User("U002", "Bob", "bob@example.com", "US-WEST"), "shard-1");
        sharding.put(new User("U003", "Charlie", "charlie@example.com", "EU"), "shard-2");
        
        sharding.printShardDistribution();
        
        // Move user to different shard
        System.out.println("\nRebalancing:");
        sharding.moveUserToShard("U002", "shard-3");
        
        sharding.printShardDistribution();
    }
}
