package cloud;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Static Content Hosting Pattern
 * 
 * Intent: Deploy static resources to a cloud-based storage service that can
 * deliver them directly to the client, reducing the load on compute instances
 * and often improving performance.
 * 
 * Also Known As:
 * - CDN Pattern
 * - Content Delivery Network Pattern
 * - Static Asset Pattern
 * 
 * Motivation:
 * Web applications often serve static content (images, CSS, JS, videos).
 * Serving from application servers leads to:
 * - Wasted compute resources
 * - Slower delivery to geographically distant users
 * - Higher bandwidth costs
 * - Difficulty scaling for traffic spikes
 * - Complex caching logic
 * 
 * Applicability:
 * - Web applications with static assets
 * - Global user base requiring low latency
 * - High-traffic websites
 * - Media-heavy applications
 * - Cost optimization requirements
 * 
 * Benefits:
 * - Reduced server load
 * - Faster content delivery (edge locations)
 * - Lower costs (storage cheaper than compute)
 * - Better scalability
 * - Built-in caching and CDN
 * - Offload bandwidth from application servers
 * 
 * Trade-offs:
 * - Eventual consistency for updates
 * - Additional infrastructure complexity
 * - CORS configuration needed
 * - Cache invalidation challenges
 * - May need separate deployment process
 */

// Static resource representation
class StaticResource {
    private final String path;
    private final String contentType;
    private final byte[] content;
    private final long size;
    private final String etag;
    private final long lastModified;
    
    public StaticResource(String path, String contentType, String content) {
        this.path = path;
        this.contentType = contentType;
        this.content = content.getBytes();
        this.size = this.content.length;
        this.etag = generateETag(content);
        this.lastModified = System.currentTimeMillis();
    }
    
    private String generateETag(String content) {
        return "\"" + Integer.toHexString(content.hashCode()) + "\"";
    }
    
    public String getPath() { return path; }
    public String getContentType() { return contentType; }
    public byte[] getContent() { return content; }
    public long getSize() { return size; }
    public String getETag() { return etag; }
    public long getLastModified() { return lastModified; }
    
    @Override
    public String toString() {
        return String.format("%s (%s, %d bytes)", path, contentType, size);
    }
}

// Example 1: Basic Static Storage
class StaticStorage {
    private final Map<String, StaticResource> storage;
    private long totalSize;
    
    public StaticStorage() {
        this.storage = new ConcurrentHashMap<>();
        this.totalSize = 0;
    }
    
    public void upload(StaticResource resource) {
        storage.put(resource.getPath(), resource);
        totalSize += resource.getSize();
        System.out.println(String.format("[UPLOAD] %s (%d bytes)", 
            resource.getPath(), resource.getSize()));
    }
    
    public StaticResource get(String path) {
        StaticResource resource = storage.get(path);
        if (resource != null) {
            System.out.println(String.format("[RETRIEVE] %s", path));
        }
        return resource;
    }
    
    public boolean delete(String path) {
        StaticResource removed = storage.remove(path);
        if (removed != null) {
            totalSize -= removed.getSize();
            System.out.println(String.format("[DELETE] %s", path));
            return true;
        }
        return false;
    }
    
    public List<StaticResource> listAll() {
        return new ArrayList<>(storage.values());
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public void printInventory() {
        System.out.println("\nStatic Storage Inventory:");
        System.out.println(String.format("  Total files: %d", storage.size()));
        System.out.println(String.format("  Total size: %d bytes (%.2f KB)", 
            totalSize, totalSize / 1024.0));
        storage.values().forEach(r -> System.out.println("  - " + r));
    }
}

// Example 2: CDN with Edge Locations
class CDNEdgeLocation {
    private final String region;
    private final Map<String, CachedResource> cache;
    private int hitCount;
    private int missCount;
    
    static class CachedResource {
        final StaticResource resource;
        final long cachedAt;
        final long ttl;
        
        public CachedResource(StaticResource resource, long ttl) {
            this.resource = resource;
            this.cachedAt = System.currentTimeMillis();
            this.ttl = ttl;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > ttl;
        }
    }
    
    public CDNEdgeLocation(String region) {
        this.region = region;
        this.cache = new ConcurrentHashMap<>();
        this.hitCount = 0;
        this.missCount = 0;
    }
    
    public StaticResource get(String path, StaticStorage origin, long ttl) {
        CachedResource cached = cache.get(path);
        
        // Cache hit
        if (cached != null && !cached.isExpired()) {
            hitCount++;
            System.out.println(String.format("[%s] Cache HIT: %s", region, path));
            return cached.resource;
        }
        
        // Cache miss - fetch from origin
        missCount++;
        System.out.println(String.format("[%s] Cache MISS: %s (fetching from origin)", region, path));
        
        StaticResource resource = origin.get(path);
        if (resource != null) {
            cache.put(path, new CachedResource(resource, ttl));
        }
        
        return resource;
    }
    
    public void invalidate(String path) {
        cache.remove(path);
        System.out.println(String.format("[%s] Invalidated cache: %s", region, path));
    }
    
    public void invalidateAll() {
        int count = cache.size();
        cache.clear();
        System.out.println(String.format("[%s] Invalidated %d cached items", region, count));
    }
    
    public double getCacheHitRatio() {
        int total = hitCount + missCount;
        return total > 0 ? (double) hitCount / total * 100 : 0;
    }
    
    public void printStats() {
        System.out.println(String.format("[%s] Stats: %d hits, %d misses, %.1f%% hit ratio, %d cached", 
            region, hitCount, missCount, getCacheHitRatio(), cache.size()));
    }
}

class ContentDeliveryNetwork {
    private final StaticStorage origin;
    private final Map<String, CDNEdgeLocation> edgeLocations;
    private final long defaultTTL;
    
    public ContentDeliveryNetwork(StaticStorage origin, long ttlMs) {
        this.origin = origin;
        this.edgeLocations = new HashMap<>();
        this.defaultTTL = ttlMs;
        
        // Create edge locations
        edgeLocations.put("us-east", new CDNEdgeLocation("US-East"));
        edgeLocations.put("us-west", new CDNEdgeLocation("US-West"));
        edgeLocations.put("eu-west", new CDNEdgeLocation("EU-West"));
        edgeLocations.put("ap-south", new CDNEdgeLocation("AP-South"));
    }
    
    public StaticResource getFromEdge(String path, String region) {
        CDNEdgeLocation edge = edgeLocations.get(region);
        if (edge == null) {
            edge = edgeLocations.values().iterator().next(); // Default to first
        }
        
        return edge.get(path, origin, defaultTTL);
    }
    
    public void invalidateCache(String path) {
        System.out.println("\nInvalidating cache for: " + path);
        for (CDNEdgeLocation edge : edgeLocations.values()) {
            edge.invalidate(path);
        }
    }
    
    public void printCDNStats() {
        System.out.println("\nCDN Statistics:");
        for (CDNEdgeLocation edge : edgeLocations.values()) {
            edge.printStats();
        }
    }
}

// Example 3: Versioned Static Assets
class VersionedAssetManager {
    private final StaticStorage storage;
    private final Map<String, String> latestVersions;  // path -> version
    
    public VersionedAssetManager(StaticStorage storage) {
        this.storage = storage;
        this.latestVersions = new ConcurrentHashMap<>();
    }
    
    public String uploadWithVersion(StaticResource resource, String version) {
        String versionedPath = addVersionToPath(resource.getPath(), version);
        
        StaticResource versionedResource = new StaticResource(
            versionedPath,
            resource.getContentType(),
            new String(resource.getContent())
        );
        
        storage.upload(versionedResource);
        latestVersions.put(resource.getPath(), version);
        
        System.out.println(String.format("[VERSION] Uploaded %s as version %s", 
            resource.getPath(), version));
        
        return versionedPath;
    }
    
    private String addVersionToPath(String path, String version) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            return path.substring(0, dotIndex) + "." + version + path.substring(dotIndex);
        }
        return path + "." + version;
    }
    
    public String getLatestVersionPath(String basePath) {
        String version = latestVersions.get(basePath);
        if (version != null) {
            return addVersionToPath(basePath, version);
        }
        return basePath;
    }
    
    public void printVersions() {
        System.out.println("\nVersioned Assets:");
        latestVersions.forEach((path, version) -> 
            System.out.println(String.format("  %s -> v%s", path, version)));
    }
}

// Example 4: Asset Bundler and Minifier
class AssetBundler {
    public StaticResource bundle(String bundlePath, List<StaticResource> resources, String contentType) {
        StringBuilder combined = new StringBuilder();
        
        System.out.println(String.format("\n[BUNDLE] Creating bundle: %s", bundlePath));
        for (StaticResource resource : resources) {
            combined.append(new String(resource.getContent()));
            combined.append("\n");
            System.out.println(String.format("  + %s", resource.getPath()));
        }
        
        StaticResource bundle = new StaticResource(bundlePath, contentType, combined.toString());
        
        long originalSize = resources.stream().mapToLong(StaticResource::getSize).sum();
        System.out.println(String.format("  Bundle created: %d bytes (original: %d bytes)", 
            bundle.getSize(), originalSize));
        
        return bundle;
    }
    
    public StaticResource minify(StaticResource resource) {
        String content = new String(resource.getContent());
        
        // Simple minification: remove comments and extra whitespace
        String minified = content
            .replaceAll("/\\*.*?\\*/", "")  // Remove /* */ comments
            .replaceAll("//.*?\\n", "\n")   // Remove // comments
            .replaceAll("\\s+", " ")        // Collapse whitespace
            .trim();
        
        StaticResource minifiedResource = new StaticResource(
            resource.getPath().replace(".js", ".min.js").replace(".css", ".min.css"),
            resource.getContentType(),
            minified
        );
        
        double reduction = (1 - (double)minifiedResource.getSize() / resource.getSize()) * 100;
        System.out.println(String.format("[MINIFY] %s: %d -> %d bytes (%.1f%% reduction)", 
            resource.getPath(), resource.getSize(), minifiedResource.getSize(), reduction));
        
        return minifiedResource;
    }
}

// Example 5: Smart Cache with Conditional Requests
class SmartStaticContentServer {
    private final StaticStorage storage;
    private final Map<String, RequestStats> stats;
    
    static class RequestStats {
        int requests;
        int notModifiedResponses;
        long bandwidth;
        
        public void recordRequest(boolean notModified, long size) {
            requests++;
            if (notModified) {
                notModifiedResponses++;
            } else {
                bandwidth += size;
            }
        }
    }
    
    public SmartStaticContentServer(StaticStorage storage) {
        this.storage = storage;
        this.stats = new ConcurrentHashMap<>();
    }
    
    public Response serve(String path, String ifNoneMatch, Long ifModifiedSince) {
        StaticResource resource = storage.get(path);
        
        if (resource == null) {
            return new Response(404, "Not Found", null);
        }
        
        RequestStats resourceStats = stats.computeIfAbsent(path, k -> new RequestStats());
        
        // Check If-None-Match (ETag)
        if (ifNoneMatch != null && ifNoneMatch.equals(resource.getETag())) {
            resourceStats.recordRequest(true, 0);
            System.out.println(String.format("[304] Not Modified: %s (ETag match)", path));
            return new Response(304, "Not Modified", null);
        }
        
        // Check If-Modified-Since
        if (ifModifiedSince != null && ifModifiedSince >= resource.getLastModified()) {
            resourceStats.recordRequest(true, 0);
            System.out.println(String.format("[304] Not Modified: %s (time match)", path));
            return new Response(304, "Not Modified", null);
        }
        
        // Serve full content
        resourceStats.recordRequest(false, resource.getSize());
        System.out.println(String.format("[200] Serving: %s (%d bytes)", path, resource.getSize()));
        
        return new Response(200, "OK", resource);
    }
    
    static class Response {
        final int statusCode;
        final String statusMessage;
        final StaticResource resource;
        
        public Response(int statusCode, String statusMessage, StaticResource resource) {
            this.statusCode = statusCode;
            this.statusMessage = statusMessage;
            this.resource = resource;
        }
    }
    
    public void printStats() {
        System.out.println("\nServer Statistics:");
        stats.forEach((path, stat) -> {
            double notModifiedRatio = (double) stat.notModifiedResponses / stat.requests * 100;
            System.out.println(String.format("  %s: %d requests, %d not-modified (%.1f%%), %d bytes served",
                path, stat.requests, stat.notModifiedResponses, notModifiedRatio, stat.bandwidth));
        });
    }
}

// Demo
public class StaticContentHostingPattern {
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicStaticStorage();
        demonstrateCDN();
        demonstrateVersionedAssets();
        demonstrateAssetBundling();
        demonstrateSmartCaching();
    }
    
    private static void demonstrateBasicStaticStorage() {
        System.out.println("=== Basic Static Storage ===\n");
        
        StaticStorage storage = new StaticStorage();
        
        // Upload static files
        storage.upload(new StaticResource("/css/main.css", "text/css", 
            "body { margin: 0; padding: 0; }"));
        storage.upload(new StaticResource("/js/app.js", "application/javascript", 
            "console.log('Hello World');"));
        storage.upload(new StaticResource("/images/logo.png", "image/png", 
            "PNG_IMAGE_DATA_HERE"));
        
        storage.printInventory();
        
        // Retrieve file
        System.out.println();
        StaticResource css = storage.get("/css/main.css");
        System.out.println("Retrieved: " + css);
    }
    
    private static void demonstrateCDN() throws InterruptedException {
        System.out.println("\n\n=== Content Delivery Network ===\n");
        
        StaticStorage origin = new StaticStorage();
        origin.upload(new StaticResource("/js/app.js", "application/javascript", 
            "console.log('Application');"));
        origin.upload(new StaticResource("/css/style.css", "text/css", 
            "body { font-family: Arial; }"));
        
        ContentDeliveryNetwork cdn = new ContentDeliveryNetwork(origin, TimeUnit.SECONDS.toMillis(5));
        
        // Requests from different regions
        System.out.println("User requests from different regions:\n");
        cdn.getFromEdge("/js/app.js", "us-east");
        cdn.getFromEdge("/js/app.js", "us-east");  // Cache hit
        cdn.getFromEdge("/js/app.js", "eu-west");  // Different edge
        cdn.getFromEdge("/css/style.css", "us-east");
        cdn.getFromEdge("/css/style.css", "ap-south");
        
        cdn.printCDNStats();
        
        // Update file and invalidate cache
        origin.upload(new StaticResource("/js/app.js", "application/javascript", 
            "console.log('Updated Application');"));
        cdn.invalidateCache("/js/app.js");
        
        System.out.println("\nAfter cache invalidation:");
        cdn.getFromEdge("/js/app.js", "us-east");  // Will fetch new version
        
        cdn.printCDNStats();
    }
    
    private static void demonstrateVersionedAssets() {
        System.out.println("\n\n=== Versioned Assets ===\n");
        
        StaticStorage storage = new StaticStorage();
        VersionedAssetManager versionManager = new VersionedAssetManager(storage);
        
        // Upload different versions
        StaticResource appJs = new StaticResource("/js/app.js", "application/javascript", 
            "console.log('v1');");
        
        versionManager.uploadWithVersion(appJs, "1.0.0");
        
        // Update to v2
        StaticResource appJsV2 = new StaticResource("/js/app.js", "application/javascript", 
            "console.log('v2 with new features');");
        versionManager.uploadWithVersion(appJsV2, "2.0.0");
        
        // Update to v3
        StaticResource appJsV3 = new StaticResource("/js/app.js", "application/javascript", 
            "console.log('v3 with bug fixes');");
        versionManager.uploadWithVersion(appJsV3, "3.0.0");
        
        versionManager.printVersions();
        
        String latestPath = versionManager.getLatestVersionPath("/js/app.js");
        System.out.println("\nLatest version path: " + latestPath);
    }
    
    private static void demonstrateAssetBundling() {
        System.out.println("\n\n=== Asset Bundling and Minification ===\n");
        
        AssetBundler bundler = new AssetBundler();
        
        // Create individual JS files
        List<StaticResource> jsFiles = Arrays.asList(
            new StaticResource("/js/utils.js", "application/javascript", 
                "function utils() { /* Utility functions */ return true; }"),
            new StaticResource("/js/api.js", "application/javascript", 
                "function callAPI() { // API calls\n  return fetch('/api'); }"),
            new StaticResource("/js/ui.js", "application/javascript", 
                "function updateUI() { /* Update UI */ console.log('UI updated'); }")
        );
        
        // Bundle files
        StaticResource bundle = bundler.bundle("/js/bundle.js", jsFiles, "application/javascript");
        
        // Minify bundle
        StaticResource minified = bundler.minify(bundle);
        
        System.out.println(String.format("\nFinal: bundle.min.js (%d bytes)", minified.getSize()));
    }
    
    private static void demonstrateSmartCaching() {
        System.out.println("\n\n=== Smart Caching with Conditional Requests ===\n");
        
        StaticStorage storage = new StaticStorage();
        storage.upload(new StaticResource("/js/app.js", "application/javascript", 
            "console.log('Application');"));
        
        SmartStaticContentServer server = new SmartStaticContentServer(storage);
        
        // First request - full content
        System.out.println("Request 1: Initial request");
        SmartStaticContentServer.Response response1 = server.serve("/js/app.js", null, null);
        
        if (response1.resource != null) {
            String etag = response1.resource.getETag();
            long lastModified = response1.resource.getLastModified();
            
            // Subsequent requests with caching headers
            System.out.println("\nRequest 2: With ETag");
            server.serve("/js/app.js", etag, null);
            
            System.out.println("\nRequest 3: With If-Modified-Since");
            server.serve("/js/app.js", null, lastModified);
            
            System.out.println("\nRequest 4: Without caching headers");
            server.serve("/js/app.js", null, null);
        }
        
        server.printStats();
    }
}
