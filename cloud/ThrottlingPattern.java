package cloud;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Throttling Pattern
 * 
 * Intent: Control the consumption of resources by an application, service, or tenant
 * by limiting the rate at which operations can be performed.
 * 
 * Also Known As: Rate Limiting, Traffic Shaping
 * 
 * Motivation:
 * Cloud applications may experience variable loads. Throttling helps:
 * - Prevent resource exhaustion under high load
 * - Ensure fair resource allocation across tenants
 * - Protect downstream services from overload
 * - Meet SLA commitments by rejecting excess requests
 * - Prevent abuse and DDoS attacks
 * 
 * Applicability:
 * - API rate limiting (requests per second/minute/hour)
 * - Multi-tenant systems with per-tenant quotas
 * - Protecting backend services from overload
 * - Cost control by limiting resource consumption
 * - Compliance with third-party API rate limits
 */

/**
 * Request to be throttled
 */
class ThrottledRequest {
    private final String id;
    private final String clientId;
    private final Instant timestamp;
    private final String operation;
    
    public ThrottledRequest(String id, String clientId, String operation) {
        this.id = id;
        this.clientId = clientId;
        this.timestamp = Instant.now();
        this.operation = operation;
    }
    
    public String getId() { return id; }
    public String getClientId() { return clientId; }
    public Instant getTimestamp() { return timestamp; }
    public String getOperation() { return operation; }
}

/**
 * Response indicating throttling result
 */
class ThrottleResponse {
    private final boolean allowed;
    private final String reason;
    private final long retryAfterMs;
    private final Map<String, Object> metadata;
    
    public ThrottleResponse(boolean allowed, String reason, long retryAfterMs) {
        this.allowed = allowed;
        this.reason = reason;
        this.retryAfterMs = retryAfterMs;
        this.metadata = new HashMap<>();
    }
    
    public boolean isAllowed() { return allowed; }
    public String getReason() { return reason; }
    public long getRetryAfterMs() { return retryAfterMs; }
    public Map<String, Object> getMetadata() { return metadata; }
}

/**
 * Base throttling strategy interface
 */
interface ThrottleStrategy {
    ThrottleResponse allowRequest(ThrottledRequest request);
    void reset();
    Map<String, Object> getMetrics();
}

/**
 * Example 1: Token Bucket Throttling
 * 
 * Maintains a bucket of tokens that refill at a fixed rate.
 * Each request consumes one token. When bucket is empty, requests are rejected.
 * Allows burst traffic up to bucket capacity.
 */
class TokenBucketThrottle implements ThrottleStrategy {
    private final int capacity;
    private final int refillRate; // tokens per second
    private final AtomicInteger tokens;
    private final AtomicLong lastRefillTime;
    private final AtomicLong acceptedCount = new AtomicLong(0);
    private final AtomicLong rejectedCount = new AtomicLong(0);
    
    public TokenBucketThrottle(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicInteger(capacity);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
    }
    
    @Override
    public ThrottleResponse allowRequest(ThrottledRequest request) {
        refillTokens();
        
        if (tokens.get() > 0 && tokens.decrementAndGet() >= 0) {
            acceptedCount.incrementAndGet();
            return new ThrottleResponse(true, "Request allowed", 0);
        }
        
        rejectedCount.incrementAndGet();
        long retryAfter = 1000 / refillRate; // Time until next token
        return new ThrottleResponse(false, "Rate limit exceeded", retryAfter);
    }
    
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long lastRefill = lastRefillTime.get();
        long elapsedMs = now - lastRefill;
        
        if (elapsedMs >= 1000) { // Refill every second
            int tokensToAdd = (int) (elapsedMs / 1000) * refillRate;
            int currentTokens = tokens.get();
            int newTokens = Math.min(capacity, currentTokens + tokensToAdd);
            
            if (tokens.compareAndSet(currentTokens, newTokens)) {
                lastRefillTime.set(now);
            }
        }
    }
    
    @Override
    public void reset() {
        tokens.set(capacity);
        lastRefillTime.set(System.currentTimeMillis());
        acceptedCount.set(0);
        rejectedCount.set(0);
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("currentTokens", tokens.get());
        metrics.put("capacity", capacity);
        metrics.put("refillRate", refillRate);
        metrics.put("acceptedRequests", acceptedCount.get());
        metrics.put("rejectedRequests", rejectedCount.get());
        return metrics;
    }
}

/**
 * Example 2: Sliding Window Throttling
 * 
 * Tracks requests in a sliding time window.
 * More accurate than fixed windows but requires more memory.
 * Prevents burst traffic at window boundaries.
 */
class SlidingWindowThrottle implements ThrottleStrategy {
    private final int maxRequests;
    private final Duration windowSize;
    private final ConcurrentLinkedQueue<Instant> requestTimestamps;
    private final AtomicLong acceptedCount = new AtomicLong(0);
    private final AtomicLong rejectedCount = new AtomicLong(0);
    
    public SlidingWindowThrottle(int maxRequests, Duration windowSize) {
        this.maxRequests = maxRequests;
        this.windowSize = windowSize;
        this.requestTimestamps = new ConcurrentLinkedQueue<>();
    }
    
    @Override
    public ThrottleResponse allowRequest(ThrottledRequest request) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowSize);
        
        // Remove expired timestamps
        while (!requestTimestamps.isEmpty() && 
               requestTimestamps.peek().isBefore(windowStart)) {
            requestTimestamps.poll();
        }
        
        if (requestTimestamps.size() < maxRequests) {
            requestTimestamps.offer(now);
            acceptedCount.incrementAndGet();
            
            ThrottleResponse response = new ThrottleResponse(true, "Request allowed", 0);
            response.getMetadata().put("remainingRequests", maxRequests - requestTimestamps.size());
            return response;
        }
        
        rejectedCount.incrementAndGet();
        
        // Calculate retry after based on oldest request
        Instant oldestRequest = requestTimestamps.peek();
        long retryAfter = oldestRequest != null ? 
            Duration.between(now, oldestRequest.plus(windowSize)).toMillis() : 
            windowSize.toMillis();
        
        return new ThrottleResponse(false, "Rate limit exceeded", retryAfter);
    }
    
    @Override
    public void reset() {
        requestTimestamps.clear();
        acceptedCount.set(0);
        rejectedCount.set(0);
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("currentRequests", requestTimestamps.size());
        metrics.put("maxRequests", maxRequests);
        metrics.put("windowSize", windowSize.toString());
        metrics.put("acceptedRequests", acceptedCount.get());
        metrics.put("rejectedRequests", rejectedCount.get());
        return metrics;
    }
}

/**
 * Example 3: Per-Tenant Throttling
 * 
 * Enforces separate rate limits for each tenant/client.
 * Ensures fair resource allocation in multi-tenant systems.
 * Prevents one tenant from affecting others.
 */
class PerTenantThrottle implements ThrottleStrategy {
    private final Map<String, TokenBucketThrottle> tenantThrottles;
    private final int defaultCapacity;
    private final int defaultRefillRate;
    private final Map<String, TenantQuota> tenantQuotas;
    private final AtomicLong totalAccepted = new AtomicLong(0);
    private final AtomicLong totalRejected = new AtomicLong(0);
    
    static class TenantQuota {
        final int capacity;
        final int refillRate;
        
        TenantQuota(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
        }
    }
    
    public PerTenantThrottle(int defaultCapacity, int defaultRefillRate) {
        this.tenantThrottles = new ConcurrentHashMap<>();
        this.defaultCapacity = defaultCapacity;
        this.defaultRefillRate = defaultRefillRate;
        this.tenantQuotas = new ConcurrentHashMap<>();
    }
    
    public void setTenantQuota(String tenantId, int capacity, int refillRate) {
        tenantQuotas.put(tenantId, new TenantQuota(capacity, refillRate));
        tenantThrottles.remove(tenantId); // Reset throttle with new quota
    }
    
    @Override
    public ThrottleResponse allowRequest(ThrottledRequest request) {
        String tenantId = request.getClientId();
        
        TokenBucketThrottle throttle = tenantThrottles.computeIfAbsent(tenantId, id -> {
            TenantQuota quota = tenantQuotas.get(id);
            if (quota != null) {
                return new TokenBucketThrottle(quota.capacity, quota.refillRate);
            }
            return new TokenBucketThrottle(defaultCapacity, defaultRefillRate);
        });
        
        ThrottleResponse response = throttle.allowRequest(request);
        
        if (response.isAllowed()) {
            totalAccepted.incrementAndGet();
        } else {
            totalRejected.incrementAndGet();
        }
        
        response.getMetadata().put("tenantId", tenantId);
        response.getMetadata().putAll(throttle.getMetrics());
        
        return response;
    }
    
    @Override
    public void reset() {
        tenantThrottles.values().forEach(ThrottleStrategy::reset);
        totalAccepted.set(0);
        totalRejected.set(0);
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalTenants", tenantThrottles.size());
        metrics.put("totalAccepted", totalAccepted.get());
        metrics.put("totalRejected", totalRejected.get());
        
        Map<String, Map<String, Object>> tenantMetrics = new HashMap<>();
        tenantThrottles.forEach((tenantId, throttle) -> 
            tenantMetrics.put(tenantId, throttle.getMetrics())
        );
        metrics.put("tenantMetrics", tenantMetrics);
        
        return metrics;
    }
}

/**
 * Example 4: Adaptive Throttling
 * 
 * Dynamically adjusts rate limits based on system health.
 * Increases limits when system is healthy, decreases under stress.
 * Uses metrics like CPU, memory, response time to adapt.
 */
class AdaptiveThrottle implements ThrottleStrategy {
    private final int minRate;
    private final int maxRate;
    private volatile int currentRate;
    private final TokenBucketThrottle throttle;
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final ScheduledExecutorService scheduler;
    
    // Simulated health metrics
    private volatile double systemLoad = 0.3; // 0.0 to 1.0
    private volatile double errorRate = 0.0;  // 0.0 to 1.0
    
    public AdaptiveThrottle(int minRate, int maxRate, int initialRate) {
        this.minRate = minRate;
        this.maxRate = maxRate;
        this.currentRate = initialRate;
        this.throttle = new TokenBucketThrottle(initialRate, initialRate);
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Adjust rate every 5 seconds based on system health
        scheduler.scheduleAtFixedRate(this::adjustRate, 5, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public ThrottleResponse allowRequest(ThrottledRequest request) {
        ThrottleResponse response = throttle.allowRequest(request);
        
        if (response.isAllowed()) {
            successCount.incrementAndGet();
        } else {
            errorCount.incrementAndGet();
        }
        
        response.getMetadata().put("currentRate", currentRate);
        response.getMetadata().put("systemLoad", systemLoad);
        
        return response;
    }
    
    private void adjustRate() {
        long total = successCount.get() + errorCount.get();
        if (total > 0) {
            errorRate = errorCount.get() / (double) total;
        }
        
        // Decrease rate if system is stressed
        if (systemLoad > 0.8 || errorRate > 0.1) {
            currentRate = Math.max(minRate, (int) (currentRate * 0.9));
        }
        // Increase rate if system is healthy
        else if (systemLoad < 0.5 && errorRate < 0.01) {
            currentRate = Math.min(maxRate, (int) (currentRate * 1.1));
        }
        
        // Update throttle with new rate
        throttle.reset();
        System.out.println("Adaptive throttle adjusted rate to: " + currentRate + 
                         " (load: " + systemLoad + ", errors: " + errorRate + ")");
    }
    
    public void setSystemLoad(double load) {
        this.systemLoad = Math.max(0.0, Math.min(1.0, load));
    }
    
    @Override
    public void reset() {
        throttle.reset();
        successCount.set(0);
        errorCount.set(0);
        currentRate = (minRate + maxRate) / 2;
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = throttle.getMetrics();
        metrics.put("currentRate", currentRate);
        metrics.put("minRate", minRate);
        metrics.put("maxRate", maxRate);
        metrics.put("systemLoad", systemLoad);
        metrics.put("errorRate", errorRate);
        metrics.put("successCount", successCount.get());
        metrics.put("errorCount", errorCount.get());
        return metrics;
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}

/**
 * Example 5: Distributed Throttling with Redis
 * 
 * Coordinates rate limiting across multiple service instances.
 * Uses centralized counter (simulated here with shared data).
 * Essential for horizontally scaled services.
 */
class DistributedThrottle implements ThrottleStrategy {
    private final String throttleKey;
    private final int maxRequests;
    private final Duration windowSize;
    
    // Simulated distributed store (would be Redis/Memcached in production)
    private static final ConcurrentHashMap<String, RequestCounter> distributedStore = 
        new ConcurrentHashMap<>();
    
    static class RequestCounter {
        private final AtomicInteger count;
        private final AtomicLong windowStart;
        
        RequestCounter() {
            this.count = new AtomicInteger(0);
            this.windowStart = new AtomicLong(System.currentTimeMillis());
        }
        
        int getCount() { return count.get(); }
        long getWindowStart() { return windowStart.get(); }
        
        boolean incrementIfAllowed(int max, long currentWindow) {
            // Reset if window expired
            if (windowStart.get() != currentWindow) {
                count.set(0);
                windowStart.set(currentWindow);
            }
            
            int current = count.get();
            if (current >= max) {
                return false;
            }
            
            return count.compareAndSet(current, current + 1);
        }
    }
    
    public DistributedThrottle(String throttleKey, int maxRequests, Duration windowSize) {
        this.throttleKey = throttleKey;
        this.maxRequests = maxRequests;
        this.windowSize = windowSize;
    }
    
    @Override
    public ThrottleResponse allowRequest(ThrottledRequest request) {
        RequestCounter counter = distributedStore.computeIfAbsent(
            throttleKey, 
            k -> new RequestCounter()
        );
        
        long currentWindow = System.currentTimeMillis() / windowSize.toMillis();
        
        if (counter.incrementIfAllowed(maxRequests, currentWindow)) {
            ThrottleResponse response = new ThrottleResponse(true, "Request allowed", 0);
            response.getMetadata().put("requestCount", counter.getCount());
            response.getMetadata().put("maxRequests", maxRequests);
            response.getMetadata().put("distributedKey", throttleKey);
            return response;
        }
        
        long retryAfter = windowSize.toMillis() - 
            (System.currentTimeMillis() % windowSize.toMillis());
        
        ThrottleResponse response = new ThrottleResponse(false, "Rate limit exceeded", retryAfter);
        response.getMetadata().put("requestCount", counter.getCount());
        response.getMetadata().put("maxRequests", maxRequests);
        
        return response;
    }
    
    @Override
    public void reset() {
        distributedStore.remove(throttleKey);
    }
    
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        RequestCounter counter = distributedStore.get(throttleKey);
        
        if (counter != null) {
            metrics.put("currentCount", counter.getCount());
            metrics.put("maxRequests", maxRequests);
            metrics.put("windowSize", windowSize.toString());
            metrics.put("distributedKey", throttleKey);
        }
        
        return metrics;
    }
}

/**
 * Demonstration of the Throttling Pattern
 */
public class ThrottlingPattern {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Throttling Pattern Demo ===\n");
        
        // Example 1: Token Bucket Throttling
        System.out.println("1. Token Bucket Throttling (burst support):");
        TokenBucketThrottle tokenBucket = new TokenBucketThrottle(5, 2); // 5 capacity, 2/sec refill
        
        // Send burst of requests
        for (int i = 1; i <= 8; i++) {
            ThrottledRequest req = new ThrottledRequest("req-" + i, "client-1", "GET /api/data");
            ThrottleResponse response = tokenBucket.allowRequest(req);
            
            System.out.println("Request " + i + ": " + 
                (response.isAllowed() ? "ALLOWED" : "REJECTED - " + response.getReason()));
        }
        
        System.out.println("Metrics: " + tokenBucket.getMetrics());
        System.out.println("\nWaiting 2 seconds for token refill...");
        Thread.sleep(2000);
        
        ThrottleResponse afterRefill = tokenBucket.allowRequest(
            new ThrottledRequest("req-9", "client-1", "GET /api/data")
        );
        System.out.println("After refill: " + 
            (afterRefill.isAllowed() ? "ALLOWED" : "REJECTED"));
        System.out.println("Updated metrics: " + tokenBucket.getMetrics());
        
        // Example 2: Sliding Window Throttling
        System.out.println("\n2. Sliding Window Throttling (precise time window):");
        SlidingWindowThrottle slidingWindow = new SlidingWindowThrottle(3, Duration.ofSeconds(5));
        
        for (int i = 1; i <= 5; i++) {
            ThrottledRequest req = new ThrottledRequest("req-" + i, "client-2", "POST /api/data");
            ThrottleResponse response = slidingWindow.allowRequest(req);
            
            System.out.println("Request " + i + ": " + 
                (response.isAllowed() ? "ALLOWED" : "REJECTED - retry after " + 
                 response.getRetryAfterMs() + "ms") +
                " | Metadata: " + response.getMetadata());
            
            Thread.sleep(500); // Small delay between requests
        }
        
        System.out.println("Metrics: " + slidingWindow.getMetrics());
        
        // Example 3: Per-Tenant Throttling
        System.out.println("\n3. Per-Tenant Throttling (fair resource allocation):");
        PerTenantThrottle perTenant = new PerTenantThrottle(3, 1); // Default: 3 capacity, 1/sec
        
        // Premium tenant gets higher quota
        perTenant.setTenantQuota("premium-tenant", 10, 5);
        
        // Test multiple tenants
        String[] tenants = {"free-tenant", "premium-tenant", "free-tenant", "premium-tenant"};
        for (int i = 0; i < 8; i++) {
            String tenant = tenants[i % tenants.length];
            ThrottledRequest req = new ThrottledRequest("req-" + i, tenant, "GET /api/resource");
            ThrottleResponse response = perTenant.allowRequest(req);
            
            System.out.println(tenant + " request " + i + ": " + 
                (response.isAllowed() ? "ALLOWED" : "REJECTED"));
        }
        
        System.out.println("\nPer-tenant metrics: " + perTenant.getMetrics());
        
        // Example 4: Adaptive Throttling
        System.out.println("\n4. Adaptive Throttling (dynamic rate adjustment):");
        AdaptiveThrottle adaptive = new AdaptiveThrottle(5, 50, 20); // min: 5, max: 50, initial: 20
        
        System.out.println("Starting with rate: 20 requests/sec");
        
        // Simulate healthy system
        adaptive.setSystemLoad(0.3);
        for (int i = 0; i < 3; i++) {
            ThrottledRequest req = new ThrottledRequest("req-" + i, "client-4", "GET /api/data");
            adaptive.allowRequest(req);
        }
        
        Thread.sleep(6000); // Wait for rate adjustment
        System.out.println("After healthy period: " + adaptive.getMetrics().get("currentRate"));
        
        // Simulate stressed system
        adaptive.setSystemLoad(0.9);
        Thread.sleep(6000); // Wait for rate adjustment
        System.out.println("After stress period: " + adaptive.getMetrics().get("currentRate"));
        
        adaptive.shutdown();
        
        // Example 5: Distributed Throttling
        System.out.println("\n5. Distributed Throttling (multiple instances):");
        
        // Simulate multiple service instances sharing rate limit
        DistributedThrottle instance1 = new DistributedThrottle("api-key-123", 5, Duration.ofSeconds(10));
        DistributedThrottle instance2 = new DistributedThrottle("api-key-123", 5, Duration.ofSeconds(10));
        
        System.out.println("Two service instances sharing rate limit of 5 requests per 10 seconds:");
        
        for (int i = 1; i <= 7; i++) {
            // Alternate between instances
            DistributedThrottle instance = (i % 2 == 0) ? instance1 : instance2;
            String instanceName = (i % 2 == 0) ? "Instance-1" : "Instance-2";
            
            ThrottledRequest req = new ThrottledRequest("req-" + i, "api-key-123", "GET /api/data");
            ThrottleResponse response = instance.allowRequest(req);
            
            System.out.println(instanceName + " - Request " + i + ": " + 
                (response.isAllowed() ? "ALLOWED" : "REJECTED") +
                " | Count: " + response.getMetadata().get("requestCount"));
        }
        
        System.out.println("\nDistributed metrics: " + instance1.getMetrics());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Prevents resource exhaustion under load");
        System.out.println("✓ Fair allocation across tenants");
        System.out.println("✓ Protects downstream services");
        System.out.println("✓ Supports various algorithms (token bucket, sliding window)");
        System.out.println("✓ Adapts to system health dynamically");
        System.out.println("✓ Works across distributed instances");
    }
}
