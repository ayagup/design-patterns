package cloud;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Index Table Pattern
 * 
 * Intent: Create indexes on attributes that are frequently referenced by queries
 * to improve query performance in data stores that don't natively support
 * secondary indexes.
 * 
 * Also Known As: Materialized Index, Secondary Index, Lookup Table
 * 
 * Motivation:
 * NoSQL databases often only support queries by primary key.
 * Searching by other attributes requires full table scans.
 * Index tables provide O(1) or O(log n) lookups for indexed attributes.
 * 
 * Applicability:
 * - NoSQL databases without native secondary indexes
 * - Frequently queried non-primary-key attributes
 * - Multi-criteria queries
 * - Range queries on non-primary attributes
 * - Full-text search requirements
 */

/**
 * Main data entity stored in primary table
 */
class DataRecord {
    private final String id;
    private final String email;
    private final String country;
    private final String category;
    private final int score;
    private final Instant createdAt;
    private final Map<String, Object> additionalData;
    
    public DataRecord(String id, String email, String country, String category, int score) {
        this.id = id;
        this.email = email;
        this.country = country;
        this.category = category;
        this.score = score;
        this.createdAt = Instant.now();
        this.additionalData = new HashMap<>();
    }
    
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getCountry() { return country; }
    public String getCategory() { return category; }
    public int getScore() { return score; }
    public Instant getCreatedAt() { return createdAt; }
    public Map<String, Object> getAdditionalData() { return additionalData; }
    
    @Override
    public String toString() {
        return "DataRecord{id=" + id + ", email=" + email + ", country=" + country + 
               ", category=" + category + ", score=" + score + "}";
    }
}

/**
 * Query criteria for searching
 */
class QueryCriteria {
    private String email;
    private String country;
    private String category;
    private Integer minScore;
    private Integer maxScore;
    
    public QueryCriteria() {}
    
    public QueryCriteria byEmail(String email) {
        this.email = email;
        return this;
    }
    
    public QueryCriteria byCountry(String country) {
        this.country = country;
        return this;
    }
    
    public QueryCriteria byCategory(String category) {
        this.category = category;
        return this;
    }
    
    public QueryCriteria byScoreRange(int min, int max) {
        this.minScore = min;
        this.maxScore = max;
        return this;
    }
    
    public String getEmail() { return email; }
    public String getCountry() { return country; }
    public String getCategory() { return category; }
    public Integer getMinScore() { return minScore; }
    public Integer getMaxScore() { return maxScore; }
}

/**
 * Example 1: Basic Index Table
 * 
 * Maintains separate index tables for frequently queried attributes.
 * Each index maps attribute value to record IDs.
 */
class BasicIndexTable {
    // Primary storage (keyed by ID)
    private final Map<String, DataRecord> primaryTable;
    
    // Secondary indexes
    private final Map<String, Set<String>> emailIndex;      // email -> set of IDs
    private final Map<String, Set<String>> countryIndex;    // country -> set of IDs
    private final Map<String, Set<String>> categoryIndex;   // category -> set of IDs
    
    private long queryCount = 0;
    private long indexHits = 0;
    
    public BasicIndexTable() {
        this.primaryTable = new ConcurrentHashMap<>();
        this.emailIndex = new ConcurrentHashMap<>();
        this.countryIndex = new ConcurrentHashMap<>();
        this.categoryIndex = new ConcurrentHashMap<>();
    }
    
    public void insert(DataRecord record) {
        // Store in primary table
        primaryTable.put(record.getId(), record);
        
        // Update indexes
        emailIndex.computeIfAbsent(record.getEmail(), k -> ConcurrentHashMap.newKeySet())
                  .add(record.getId());
        countryIndex.computeIfAbsent(record.getCountry(), k -> ConcurrentHashMap.newKeySet())
                    .add(record.getId());
        categoryIndex.computeIfAbsent(record.getCategory(), k -> ConcurrentHashMap.newKeySet())
                     .add(record.getId());
    }
    
    public DataRecord getById(String id) {
        return primaryTable.get(id);
    }
    
    public List<DataRecord> queryByEmail(String email) {
        queryCount++;
        Set<String> ids = emailIndex.get(email);
        
        if (ids == null) {
            return Collections.emptyList();
        }
        
        indexHits++;
        return ids.stream()
                  .map(primaryTable::get)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    public List<DataRecord> queryByCountry(String country) {
        queryCount++;
        Set<String> ids = countryIndex.get(country);
        
        if (ids == null) {
            return Collections.emptyList();
        }
        
        indexHits++;
        return ids.stream()
                  .map(primaryTable::get)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    public List<DataRecord> queryByCategory(String category) {
        queryCount++;
        Set<String> ids = categoryIndex.get(category);
        
        if (ids == null) {
            return Collections.emptyList();
        }
        
        indexHits++;
        return ids.stream()
                  .map(primaryTable::get)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    public void delete(String id) {
        DataRecord record = primaryTable.remove(id);
        if (record != null) {
            // Remove from indexes
            removeFromIndex(emailIndex, record.getEmail(), id);
            removeFromIndex(countryIndex, record.getCountry(), id);
            removeFromIndex(categoryIndex, record.getCategory(), id);
        }
    }
    
    private void removeFromIndex(Map<String, Set<String>> index, String key, String id) {
        Set<String> ids = index.get(key);
        if (ids != null) {
            ids.remove(id);
            if (ids.isEmpty()) {
                index.remove(key);
            }
        }
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", primaryTable.size());
        stats.put("emailIndexSize", emailIndex.size());
        stats.put("countryIndexSize", countryIndex.size());
        stats.put("categoryIndexSize", categoryIndex.size());
        stats.put("queryCount", queryCount);
        stats.put("indexHitRate", queryCount > 0 ? (indexHits * 100.0 / queryCount) : 0);
        return stats;
    }
}

/**
 * Example 2: Composite Index Table
 * 
 * Supports queries on multiple attributes simultaneously.
 * Creates composite keys for common query patterns.
 */
class CompositeIndexTable {
    private final Map<String, DataRecord> primaryTable;
    
    // Composite indexes (combine two attributes)
    private final Map<String, Set<String>> countryCategoryIndex;  // "US:Electronics" -> IDs
    private final Map<String, Set<String>> categoryScoreIndex;    // "Electronics:50-100" -> IDs
    
    public CompositeIndexTable() {
        this.primaryTable = new ConcurrentHashMap<>();
        this.countryCategoryIndex = new ConcurrentHashMap<>();
        this.categoryScoreIndex = new ConcurrentHashMap<>();
    }
    
    public void insert(DataRecord record) {
        primaryTable.put(record.getId(), record);
        
        // Build composite indexes
        String countryCategory = record.getCountry() + ":" + record.getCategory();
        countryCategoryIndex.computeIfAbsent(countryCategory, k -> ConcurrentHashMap.newKeySet())
                           .add(record.getId());
        
        String scoreRange = getScoreRange(record.getScore());
        String categoryScore = record.getCategory() + ":" + scoreRange;
        categoryScoreIndex.computeIfAbsent(categoryScore, k -> ConcurrentHashMap.newKeySet())
                         .add(record.getId());
    }
    
    private String getScoreRange(int score) {
        if (score < 25) return "0-25";
        if (score < 50) return "25-50";
        if (score < 75) return "50-75";
        return "75-100";
    }
    
    public List<DataRecord> queryByCountryAndCategory(String country, String category) {
        String compositeKey = country + ":" + category;
        Set<String> ids = countryCategoryIndex.get(compositeKey);
        
        if (ids == null) {
            return Collections.emptyList();
        }
        
        return ids.stream()
                  .map(primaryTable::get)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    public List<DataRecord> queryByCategoryAndScoreRange(String category, String scoreRange) {
        String compositeKey = category + ":" + scoreRange;
        Set<String> ids = categoryScoreIndex.get(compositeKey);
        
        if (ids == null) {
            return Collections.emptyList();
        }
        
        return ids.stream()
                  .map(primaryTable::get)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    public int getRecordCount() {
        return primaryTable.size();
    }
}

/**
 * Example 3: Range Index Table
 * 
 * Supports range queries on numeric or date fields.
 * Uses sorted structures for efficient range scans.
 */
class RangeIndexTable {
    private final Map<String, DataRecord> primaryTable;
    private final NavigableMap<Integer, Set<String>> scoreIndex;     // score -> IDs
    private final NavigableMap<Long, Set<String>> timestampIndex;   // timestamp -> IDs
    
    public RangeIndexTable() {
        this.primaryTable = new ConcurrentHashMap<>();
        this.scoreIndex = new TreeMap<>();
        this.timestampIndex = new TreeMap<>();
    }
    
    public void insert(DataRecord record) {
        primaryTable.put(record.getId(), record);
        
        // Add to score index
        scoreIndex.computeIfAbsent(record.getScore(), k -> new HashSet<>())
                  .add(record.getId());
        
        // Add to timestamp index
        long timestamp = record.getCreatedAt().toEpochMilli();
        timestampIndex.computeIfAbsent(timestamp, k -> new HashSet<>())
                     .add(record.getId());
    }
    
    public List<DataRecord> queryByScoreRange(int minScore, int maxScore) {
        Set<String> resultIds = new HashSet<>();
        
        // Get all entries in range
        NavigableMap<Integer, Set<String>> rangeMap = scoreIndex.subMap(
            minScore, true, maxScore, true);
        
        rangeMap.values().forEach(resultIds::addAll);
        
        return resultIds.stream()
                       .map(primaryTable::get)
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }
    
    public List<DataRecord> queryByTimeRange(Instant start, Instant end) {
        Set<String> resultIds = new HashSet<>();
        
        NavigableMap<Long, Set<String>> rangeMap = timestampIndex.subMap(
            start.toEpochMilli(), true, end.toEpochMilli(), true);
        
        rangeMap.values().forEach(resultIds::addAll);
        
        return resultIds.stream()
                       .map(primaryTable::get)
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }
    
    public List<DataRecord> queryTopScores(int limit) {
        Set<String> resultIds = new LinkedHashSet<>();
        
        // Iterate from highest to lowest scores
        NavigableMap<Integer, Set<String>> descendingScores = scoreIndex.descendingMap();
        
        for (Set<String> ids : descendingScores.values()) {
            resultIds.addAll(ids);
            if (resultIds.size() >= limit) {
                break;
            }
        }
        
        return resultIds.stream()
                       .limit(limit)
                       .map(primaryTable::get)
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }
}

/**
 * Example 4: Inverted Index for Full-Text Search
 * 
 * Tokenizes text fields and creates inverted index.
 * Supports keyword search across text fields.
 */
class InvertedIndexTable {
    private final Map<String, DataRecord> primaryTable;
    private final Map<String, Set<String>> invertedIndex;  // word -> set of IDs
    
    public InvertedIndexTable() {
        this.primaryTable = new ConcurrentHashMap<>();
        this.invertedIndex = new ConcurrentHashMap<>();
    }
    
    public void insert(DataRecord record) {
        primaryTable.put(record.getId(), record);
        
        // Index searchable fields
        indexText(record.getId(), record.getEmail());
        indexText(record.getId(), record.getCountry());
        indexText(record.getId(), record.getCategory());
        
        // Index additional data if present
        record.getAdditionalData().values().forEach(value -> {
            if (value instanceof String) {
                indexText(record.getId(), (String) value);
            }
        });
    }
    
    private void indexText(String recordId, String text) {
        if (text == null) return;
        
        // Tokenize and normalize
        String[] tokens = text.toLowerCase()
                             .replaceAll("[^a-z0-9\\s]", "")
                             .split("\\s+");
        
        for (String token : tokens) {
            if (token.length() >= 2) {  // Ignore single characters
                invertedIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                           .add(recordId);
            }
        }
    }
    
    public List<DataRecord> search(String keyword) {
        String normalizedKeyword = keyword.toLowerCase();
        Set<String> ids = invertedIndex.get(normalizedKeyword);
        
        if (ids == null) {
            return Collections.emptyList();
        }
        
        return ids.stream()
                  .map(primaryTable::get)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    public List<DataRecord> searchMultiple(String... keywords) {
        if (keywords.length == 0) {
            return Collections.emptyList();
        }
        
        // Find intersection of all keyword matches (AND operation)
        Set<String> resultIds = null;
        
        for (String keyword : keywords) {
            String normalized = keyword.toLowerCase();
            Set<String> ids = invertedIndex.get(normalized);
            
            if (ids == null) {
                return Collections.emptyList();  // No results if any keyword missing
            }
            
            if (resultIds == null) {
                resultIds = new HashSet<>(ids);
            } else {
                resultIds.retainAll(ids);  // Intersection
            }
        }
        
        if (resultIds == null) {
            return Collections.emptyList();
        }
        
        return resultIds.stream()
                       .map(primaryTable::get)
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }
    
    public int getIndexSize() {
        return invertedIndex.size();
    }
}

/**
 * Example 5: Materialized View Index
 * 
 * Pre-computes and caches common query results.
 * Trades storage for query performance.
 */
class MaterializedViewIndex {
    private final Map<String, DataRecord> primaryTable;
    
    // Materialized views (pre-computed query results)
    private final Map<String, List<DataRecord>> countryViews;
    private final Map<String, List<DataRecord>> categoryViews;
    private final Map<String, Integer> countryRecordCounts;
    private final Map<String, Double> categoryAverageScores;
    
    private volatile boolean viewsStale = true;
    
    public MaterializedViewIndex() {
        this.primaryTable = new ConcurrentHashMap<>();
        this.countryViews = new ConcurrentHashMap<>();
        this.categoryViews = new ConcurrentHashMap<>();
        this.countryRecordCounts = new ConcurrentHashMap<>();
        this.categoryAverageScores = new ConcurrentHashMap<>();
    }
    
    public void insert(DataRecord record) {
        primaryTable.put(record.getId(), record);
        viewsStale = true;  // Mark views as needing refresh
    }
    
    public void refreshMaterializedViews() {
        System.out.println("Refreshing materialized views...");
        
        // Clear existing views
        countryViews.clear();
        categoryViews.clear();
        countryRecordCounts.clear();
        categoryAverageScores.clear();
        
        // Rebuild views by scanning primary table
        for (DataRecord record : primaryTable.values()) {
            // Country view
            countryViews.computeIfAbsent(record.getCountry(), k -> new ArrayList<>())
                       .add(record);
            
            // Category view
            categoryViews.computeIfAbsent(record.getCategory(), k -> new ArrayList<>())
                        .add(record);
        }
        
        // Compute aggregations
        countryViews.forEach((country, records) -> 
            countryRecordCounts.put(country, records.size())
        );
        
        categoryViews.forEach((category, records) -> {
            double avgScore = records.stream()
                                    .mapToInt(DataRecord::getScore)
                                    .average()
                                    .orElse(0.0);
            categoryAverageScores.put(category, avgScore);
        });
        
        viewsStale = false;
        System.out.println("Views refreshed successfully");
    }
    
    public List<DataRecord> getByCountry(String country) {
        if (viewsStale) {
            refreshMaterializedViews();
        }
        
        return countryViews.getOrDefault(country, Collections.emptyList());
    }
    
    public List<DataRecord> getByCategory(String category) {
        if (viewsStale) {
            refreshMaterializedViews();
        }
        
        return categoryViews.getOrDefault(category, Collections.emptyList());
    }
    
    public Integer getCountryRecordCount(String country) {
        if (viewsStale) {
            refreshMaterializedViews();
        }
        
        return countryRecordCounts.getOrDefault(country, 0);
    }
    
    public Double getCategoryAverageScore(String category) {
        if (viewsStale) {
            refreshMaterializedViews();
        }
        
        return categoryAverageScores.getOrDefault(category, 0.0);
    }
    
    public Map<String, Object> getViewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", primaryTable.size());
        stats.put("countryViewsCount", countryViews.size());
        stats.put("categoryViewsCount", categoryViews.size());
        stats.put("viewsStale", viewsStale);
        return stats;
    }
}

/**
 * Demonstration of the Index Table Pattern
 */
public class IndexTablePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Index Table Pattern Demo ===\n");
        
        // Example 1: Basic Index Table
        System.out.println("1. Basic Index Table (secondary indexes):");
        BasicIndexTable basicIndex = new BasicIndexTable();
        
        basicIndex.insert(new DataRecord("1", "alice@us.com", "US", "Electronics", 85));
        basicIndex.insert(new DataRecord("2", "bob@uk.com", "UK", "Books", 72));
        basicIndex.insert(new DataRecord("3", "carol@us.com", "US", "Books", 91));
        basicIndex.insert(new DataRecord("4", "dave@uk.com", "UK", "Electronics", 68));
        
        System.out.println("Query by email (alice@us.com):");
        basicIndex.queryByEmail("alice@us.com").forEach(System.out::println);
        
        System.out.println("\nQuery by country (US):");
        basicIndex.queryByCountry("US").forEach(System.out::println);
        
        System.out.println("\nQuery by category (Electronics):");
        basicIndex.queryByCategory("Electronics").forEach(System.out::println);
        
        System.out.println("\nStatistics: " + basicIndex.getStatistics());
        
        // Example 2: Composite Index Table
        System.out.println("\n2. Composite Index Table (multi-attribute queries):");
        CompositeIndexTable compositeIndex = new CompositeIndexTable();
        
        compositeIndex.insert(new DataRecord("1", "alice@us.com", "US", "Electronics", 85));
        compositeIndex.insert(new DataRecord("2", "bob@uk.com", "UK", "Electronics", 72));
        compositeIndex.insert(new DataRecord("3", "carol@us.com", "US", "Books", 45));
        compositeIndex.insert(new DataRecord("4", "dave@uk.com", "UK", "Books", 91));
        
        System.out.println("Query by country=US AND category=Electronics:");
        compositeIndex.queryByCountryAndCategory("US", "Electronics")
                     .forEach(System.out::println);
        
        System.out.println("\nQuery by category=Books AND scoreRange=75-100:");
        compositeIndex.queryByCategoryAndScoreRange("Books", "75-100")
                     .forEach(System.out::println);
        
        System.out.println("\nTotal records: " + compositeIndex.getRecordCount());
        
        // Example 3: Range Index Table
        System.out.println("\n3. Range Index Table (numeric/time range queries):");
        RangeIndexTable rangeIndex = new RangeIndexTable();
        
        rangeIndex.insert(new DataRecord("1", "alice@us.com", "US", "Electronics", 45));
        rangeIndex.insert(new DataRecord("2", "bob@uk.com", "UK", "Books", 72));
        rangeIndex.insert(new DataRecord("3", "carol@us.com", "US", "Clothing", 85));
        rangeIndex.insert(new DataRecord("4", "dave@uk.com", "UK", "Electronics", 91));
        rangeIndex.insert(new DataRecord("5", "eve@us.com", "US", "Books", 58));
        
        System.out.println("Query by score range 70-90:");
        rangeIndex.queryByScoreRange(70, 90).forEach(System.out::println);
        
        System.out.println("\nQuery top 3 scores:");
        rangeIndex.queryTopScores(3).forEach(System.out::println);
        
        // Example 4: Inverted Index for Full-Text Search
        System.out.println("\n4. Inverted Index Table (full-text search):");
        InvertedIndexTable invertedIndex = new InvertedIndexTable();
        
        DataRecord rec1 = new DataRecord("1", "alice@example.com", "US", "Electronics", 85);
        rec1.getAdditionalData().put("description", "High quality electronics");
        
        DataRecord rec2 = new DataRecord("2", "bob@example.com", "UK", "Books", 72);
        rec2.getAdditionalData().put("description", "Classic literature books");
        
        DataRecord rec3 = new DataRecord("3", "carol@example.com", "US", "Electronics", 91);
        rec3.getAdditionalData().put("description", "Premium electronics devices");
        
        invertedIndex.insert(rec1);
        invertedIndex.insert(rec2);
        invertedIndex.insert(rec3);
        
        System.out.println("Search for keyword 'electronics':");
        invertedIndex.search("electronics").forEach(System.out::println);
        
        System.out.println("\nSearch for keywords 'premium' AND 'electronics':");
        invertedIndex.searchMultiple("premium", "electronics").forEach(System.out::println);
        
        System.out.println("\nInverted index size: " + invertedIndex.getIndexSize() + " unique terms");
        
        // Example 5: Materialized View Index
        System.out.println("\n5. Materialized View Index (pre-computed aggregations):");
        MaterializedViewIndex materializedIndex = new MaterializedViewIndex();
        
        materializedIndex.insert(new DataRecord("1", "alice@us.com", "US", "Electronics", 85));
        materializedIndex.insert(new DataRecord("2", "bob@us.com", "US", "Electronics", 75));
        materializedIndex.insert(new DataRecord("3", "carol@uk.com", "UK", "Books", 90));
        materializedIndex.insert(new DataRecord("4", "dave@uk.com", "UK", "Books", 80));
        
        System.out.println("Records in US (from materialized view):");
        materializedIndex.getByCountry("US").forEach(System.out::println);
        
        System.out.println("\nUS record count: " + 
            materializedIndex.getCountryRecordCount("US"));
        System.out.println("Electronics average score: " + 
            materializedIndex.getCategoryAverageScore("Electronics"));
        System.out.println("Books average score: " + 
            materializedIndex.getCategoryAverageScore("Books"));
        
        System.out.println("\nView statistics: " + materializedIndex.getViewStatistics());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Fast lookups on non-primary-key attributes");
        System.out.println("✓ Supports complex multi-attribute queries");
        System.out.println("✓ Efficient range queries on numeric/date fields");
        System.out.println("✓ Full-text search with inverted indexes");
        System.out.println("✓ Pre-computed aggregations with materialized views");
        System.out.println("✓ Avoids full table scans in NoSQL databases");
        System.out.println("✓ Trades storage space for query performance");
    }
}
