package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

/**
 * Gateway Offloading Pattern
 * 
 * Intent: Offload shared or specialized functionality to a gateway proxy,
 * removing common concerns from individual services.
 * 
 * Also Known As: Edge Service, Gateway Shield
 * 
 * Motivation:
 * Many cross-cutting concerns (authentication, logging, rate limiting, SSL termination)
 * are duplicated across microservices. Gateway Offloading centralizes these concerns
 * at the gateway level, simplifying individual services.
 * 
 * Applicability:
 * - Common functionality duplicated across services
 * - Cross-cutting concerns (auth, logging, monitoring)
 * - SSL/TLS termination needed
 * - Request/response transformation
 * - Rate limiting or throttling required
 * - Caching at entry point
 * 
 * Benefits:
 * - Reduced code duplication
 * - Simplified service implementation
 * - Centralized security and monitoring
 * - Consistent behavior across services
 * - Easier to update cross-cutting concerns
 * - Better performance (SSL termination, caching)
 * 
 * Implementation Considerations:
 * - Gateway becomes more complex
 * - Potential single point of failure
 * - Need proper gateway scaling
 * - Configuration management
 * - Performance overhead at gateway
 */

// Request/Response models
class ServiceRequest {
    private final String requestId;
    private final String path;
    private final String method;
    private final Map<String, String> headers;
    private final String body;
    private final LocalDateTime timestamp;
    
    public ServiceRequest(String requestId, String path, String method, 
                         Map<String, String> headers, String body) {
        this.requestId = requestId;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getRequestId() { return requestId; }
    public String getPath() { return path; }
    public String getMethod() { return method; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    public String getHeader(String name) {
        return headers.get(name);
    }
}

class ServiceResponse {
    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;
    private final long responseTime;
    
    public ServiceResponse(int statusCode, String body, long responseTime) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>();
        this.responseTime = responseTime;
    }
    
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
    public Map<String, String> getHeaders() { return headers; }
    public long getResponseTime() { return responseTime; }
    
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
}

// Backend service interface
interface BackendService {
    ServiceResponse handleRequest(ServiceRequest request);
    String getServiceName();
}

// Example backend service implementations
class UserServiceBackend implements BackendService {
    @Override
    public ServiceResponse handleRequest(ServiceRequest request) {
        // Simulate processing
        try { Thread.sleep(50); } catch (InterruptedException e) { }
        
        String responseBody = String.format("{\"userId\":\"123\",\"name\":\"John Doe\"}");
        return new ServiceResponse(200, responseBody, 50);
    }
    
    @Override
    public String getServiceName() {
        return "UserService";
    }
}

class OrderServiceBackend implements BackendService {
    @Override
    public ServiceResponse handleRequest(ServiceRequest request) {
        try { Thread.sleep(80); } catch (InterruptedException e) { }
        
        String responseBody = String.format("{\"orders\":[{\"id\":\"O1\",\"total\":299.99}]}");
        return new ServiceResponse(200, responseBody, 80);
    }
    
    @Override
    public String getServiceName() {
        return "OrderService";
    }
}

// Example 1: Authentication Offloading
class AuthenticationGateway {
    private final Set<String> validTokens;
    private final BackendService backendService;
    
    public AuthenticationGateway(BackendService backendService) {
        this.backendService = backendService;
        this.validTokens = new HashSet<>(Arrays.asList("token123", "token456", "token789"));
    }
    
    public ServiceResponse handleRequest(ServiceRequest request) {
        System.out.printf("[AuthGateway] Handling %s %s%n", request.getMethod(), request.getPath());
        
        // Offloaded authentication logic
        String authToken = request.getHeader("Authorization");
        if (authToken == null || !validTokens.contains(authToken)) {
            System.out.println("[AuthGateway] Authentication failed");
            return new ServiceResponse(401, "{\"error\":\"Unauthorized\"}", 0);
        }
        
        System.out.println("[AuthGateway] Authentication successful, forwarding to backend");
        return backendService.handleRequest(request);
    }
}

// Example 2: Logging and Monitoring Offloading
class LoggingGateway {
    private final BackendService backendService;
    private final List<String> requestLog;
    private final AtomicInteger requestCounter;
    private final Logger logger;
    
    public LoggingGateway(BackendService backendService) {
        this.backendService = backendService;
        this.requestLog = new CopyOnWriteArrayList<>();
        this.requestCounter = new AtomicInteger(0);
        this.logger = Logger.getLogger(LoggingGateway.class.getName());
    }
    
    public ServiceResponse handleRequest(ServiceRequest request) {
        long startTime = System.currentTimeMillis();
        int requestNum = requestCounter.incrementAndGet();
        
        // Offloaded logging
        logger.info(String.format("[Request #%d] %s %s from client", 
            requestNum, request.getMethod(), request.getPath()));
        
        String logEntry = String.format("[%s] %s %s %s",
            LocalDateTime.now(), request.getRequestId(), request.getMethod(), request.getPath());
        requestLog.add(logEntry);
        
        // Forward to backend
        ServiceResponse response = backendService.handleRequest(request);
        
        // Log response
        long duration = System.currentTimeMillis() - startTime;
        logger.info(String.format("[Response #%d] Status: %d, Duration: %dms",
            requestNum, response.getStatusCode(), duration));
        
        return response;
    }
    
    public List<String> getRequestLog() {
        return new ArrayList<>(requestLog);
    }
    
    public int getTotalRequests() {
        return requestCounter.get();
    }
}

// Example 3: Rate Limiting Offloading
class RateLimitingGateway {
    private final BackendService backendService;
    private final Map<String, RateLimiter> rateLimiters;
    private final int maxRequestsPerMinute;
    
    public RateLimitingGateway(BackendService backendService, int maxRequestsPerMinute) {
        this.backendService = backendService;
        this.rateLimiters = new ConcurrentHashMap<>();
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }
    
    public ServiceResponse handleRequest(ServiceRequest request) {
        String clientId = request.getHeader("X-Client-ID");
        if (clientId == null) {
            clientId = "anonymous";
        }
        
        // Offloaded rate limiting
        RateLimiter limiter = rateLimiters.computeIfAbsent(
            clientId, k -> new RateLimiter(maxRequestsPerMinute));
        
        if (!limiter.allowRequest()) {
            System.out.printf("[RateLimitGateway] Rate limit exceeded for client: %s%n", clientId);
            ServiceResponse response = new ServiceResponse(429, 
                "{\"error\":\"Too many requests\"}", 0);
            response.addHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
            response.addHeader("X-RateLimit-Remaining", "0");
            return response;
        }
        
        System.out.printf("[RateLimitGateway] Request allowed for client: %s (%d remaining)%n",
            clientId, limiter.getRemainingRequests());
        
        ServiceResponse response = backendService.handleRequest(request);
        response.addHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(limiter.getRemainingRequests()));
        
        return response;
    }
    
    private static class RateLimiter {
        private final int maxRequests;
        private final Queue<Long> requestTimes;
        private final long windowMs = 60000; // 1 minute
        
        public RateLimiter(int maxRequests) {
            this.maxRequests = maxRequests;
            this.requestTimes = new ConcurrentLinkedQueue<>();
        }
        
        public synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            
            // Remove old requests outside the window
            while (!requestTimes.isEmpty() && now - requestTimes.peek() > windowMs) {
                requestTimes.poll();
            }
            
            if (requestTimes.size() < maxRequests) {
                requestTimes.offer(now);
                return true;
            }
            
            return false;
        }
        
        public int getRemainingRequests() {
            return maxRequests - requestTimes.size();
        }
    }
}

// Example 4: Caching Offloading
class CachingGateway {
    private final BackendService backendService;
    private final Map<String, CacheEntry> cache;
    private final long cacheTTL;
    
    public CachingGateway(BackendService backendService, long cacheTTLSeconds) {
        this.backendService = backendService;
        this.cache = new ConcurrentHashMap<>();
        this.cacheTTL = cacheTTLSeconds * 1000;
    }
    
    public ServiceResponse handleRequest(ServiceRequest request) {
        // Only cache GET requests
        if (!"GET".equals(request.getMethod())) {
            System.out.println("[CachingGateway] Non-GET request, bypassing cache");
            return backendService.handleRequest(request);
        }
        
        String cacheKey = request.getPath();
        CacheEntry cached = cache.get(cacheKey);
        
        // Check cache
        if (cached != null && !cached.isExpired()) {
            System.out.printf("[CachingGateway] Cache HIT for: %s%n", cacheKey);
            ServiceResponse response = new ServiceResponse(
                cached.getStatusCode(), cached.getBody(), 0);
            response.addHeader("X-Cache", "HIT");
            return response;
        }
        
        System.out.printf("[CachingGateway] Cache MISS for: %s%n", cacheKey);
        
        // Fetch from backend
        ServiceResponse response = backendService.handleRequest(request);
        
        // Cache successful responses
        if (response.getStatusCode() == 200) {
            cache.put(cacheKey, new CacheEntry(
                response.getStatusCode(), 
                response.getBody(),
                System.currentTimeMillis() + cacheTTL
            ));
            response.addHeader("X-Cache", "MISS");
        }
        
        return response;
    }
    
    public void invalidateCache(String path) {
        cache.remove(path);
        System.out.printf("[CachingGateway] Cache invalidated for: %s%n", path);
    }
    
    private static class CacheEntry {
        private final int statusCode;
        private final String body;
        private final long expiryTime;
        
        public CacheEntry(int statusCode, String body, long expiryTime) {
            this.statusCode = statusCode;
            this.body = body;
            this.expiryTime = expiryTime;
        }
        
        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}

// Example 5: Comprehensive Gateway with Multiple Offloaded Concerns
class ComprehensiveGateway {
    private final BackendService backendService;
    private final Set<String> validTokens;
    private final Map<String, CacheEntry> cache;
    private final Map<String, RateLimiter> rateLimiters;
    private final List<RequestLog> requestLogs;
    private final long cacheTTL = 30000; // 30 seconds
    private final int rateLimit = 10; // per minute
    
    public ComprehensiveGateway(BackendService backendService) {
        this.backendService = backendService;
        this.validTokens = new HashSet<>(Arrays.asList("token123", "token456"));
        this.cache = new ConcurrentHashMap<>();
        this.rateLimiters = new ConcurrentHashMap<>();
        this.requestLogs = new CopyOnWriteArrayList<>();
    }
    
    public ServiceResponse handleRequest(ServiceRequest request) {
        long startTime = System.currentTimeMillis();
        
        System.out.printf("\n[Gateway] Processing %s %s%n", request.getMethod(), request.getPath());
        
        // 1. Authentication Offloading
        String authToken = request.getHeader("Authorization");
        if (authToken == null || !validTokens.contains(authToken)) {
            System.out.println("[Gateway] ❌ Authentication failed");
            logRequest(request, 401, System.currentTimeMillis() - startTime);
            return new ServiceResponse(401, "{\"error\":\"Unauthorized\"}", 0);
        }
        System.out.println("[Gateway] ✓ Authentication passed");
        
        // 2. Rate Limiting Offloading
        String clientId = request.getHeader("X-Client-ID");
        if (clientId == null) clientId = "anonymous";
        
        RateLimiter limiter = rateLimiters.computeIfAbsent(
            clientId, k -> new RateLimiter(rateLimit));
        
        if (!limiter.allowRequest()) {
            System.out.println("[Gateway] ❌ Rate limit exceeded");
            logRequest(request, 429, System.currentTimeMillis() - startTime);
            return new ServiceResponse(429, "{\"error\":\"Too many requests\"}", 0);
        }
        System.out.printf("[Gateway] ✓ Rate limit OK (%d/%d)%n", 
            rateLimit - limiter.getRemainingRequests(), rateLimit);
        
        // 3. Caching Offloading (for GET requests)
        if ("GET".equals(request.getMethod())) {
            CacheEntry cached = cache.get(request.getPath());
            if (cached != null && !cached.isExpired()) {
                System.out.println("[Gateway] ✓ Cache HIT");
                ServiceResponse response = new ServiceResponse(
                    cached.getStatusCode(), cached.getBody(), 0);
                response.addHeader("X-Cache", "HIT");
                logRequest(request, response.getStatusCode(), System.currentTimeMillis() - startTime);
                return response;
            }
            System.out.println("[Gateway] ○ Cache MISS");
        }
        
        // 4. Forward to backend
        System.out.println("[Gateway] → Forwarding to backend: " + backendService.getServiceName());
        ServiceResponse response = backendService.handleRequest(request);
        
        // 5. Cache successful GET responses
        if ("GET".equals(request.getMethod()) && response.getStatusCode() == 200) {
            cache.put(request.getPath(), new CacheEntry(
                response.getStatusCode(), response.getBody(),
                System.currentTimeMillis() + cacheTTL));
            response.addHeader("X-Cache", "MISS");
        }
        
        // 6. Logging Offloading
        long duration = System.currentTimeMillis() - startTime;
        logRequest(request, response.getStatusCode(), duration);
        
        System.out.printf("[Gateway] ✓ Response: %d (Total time: %dms)%n", 
            response.getStatusCode(), duration);
        
        return response;
    }
    
    private void logRequest(ServiceRequest request, int statusCode, long duration) {
        requestLogs.add(new RequestLog(
            request.getRequestId(),
            request.getMethod(),
            request.getPath(),
            statusCode,
            duration,
            LocalDateTime.now()
        ));
    }
    
    public List<RequestLog> getRequestLogs() {
        return new ArrayList<>(requestLogs);
    }
    
    public void printStatistics() {
        System.out.println("\n=== Gateway Statistics ===");
        System.out.println("Total requests: " + requestLogs.size());
        
        long avgDuration = requestLogs.stream()
            .mapToLong(RequestLog::getDuration)
            .sum() / Math.max(1, requestLogs.size());
        System.out.println("Average duration: " + avgDuration + "ms");
        
        Map<Integer, Long> statusCounts = new HashMap<>();
        for (RequestLog log : requestLogs) {
            statusCounts.merge(log.getStatusCode(), 1L, Long::sum);
        }
        System.out.println("Status codes: " + statusCounts);
    }
    
    private static class CacheEntry {
        private final int statusCode;
        private final String body;
        private final long expiryTime;
        
        public CacheEntry(int statusCode, String body, long expiryTime) {
            this.statusCode = statusCode;
            this.body = body;
            this.expiryTime = expiryTime;
        }
        
        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
    
    private static class RateLimiter {
        private final int maxRequests;
        private final Queue<Long> requestTimes = new ConcurrentLinkedQueue<>();
        private final long windowMs = 60000;
        
        public RateLimiter(int maxRequests) {
            this.maxRequests = maxRequests;
        }
        
        public synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            while (!requestTimes.isEmpty() && now - requestTimes.peek() > windowMs) {
                requestTimes.poll();
            }
            if (requestTimes.size() < maxRequests) {
                requestTimes.offer(now);
                return true;
            }
            return false;
        }
        
        public int getRemainingRequests() {
            return maxRequests - requestTimes.size();
        }
    }
}

class RequestLog {
    private final String requestId;
    private final String method;
    private final String path;
    private final int statusCode;
    private final long duration;
    private final LocalDateTime timestamp;
    
    public RequestLog(String requestId, String method, String path, 
                     int statusCode, long duration, LocalDateTime timestamp) {
        this.requestId = requestId;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.duration = duration;
        this.timestamp = timestamp;
    }
    
    public String getRequestId() { return requestId; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public int getStatusCode() { return statusCode; }
    public long getDuration() { return duration; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s %s -> %d (%dms)",
            timestamp, method, path, statusCode, duration);
    }
}

// Demonstration
public class GatewayOffloadingPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Gateway Offloading Pattern Demo ===\n");
        
        BackendService userService = new UserServiceBackend();
        BackendService orderService = new OrderServiceBackend();
        
        // Demo 1: Authentication offloading
        System.out.println("--- Demo 1: Authentication Offloading ---");
        demoAuthenticationOffloading(userService);
        
        Thread.sleep(500);
        
        // Demo 2: Logging offloading
        System.out.println("\n--- Demo 2: Logging Offloading ---");
        demoLoggingOffloading(userService);
        
        Thread.sleep(500);
        
        // Demo 3: Rate limiting offloading
        System.out.println("\n--- Demo 3: Rate Limiting Offloading ---");
        demoRateLimitingOffloading(orderService);
        
        Thread.sleep(500);
        
        // Demo 4: Caching offloading
        System.out.println("\n--- Demo 4: Caching Offloading ---");
        demoCachingOffloading(userService);
        
        Thread.sleep(500);
        
        // Demo 5: Comprehensive gateway
        System.out.println("\n--- Demo 5: Comprehensive Gateway ---");
        demoComprehensiveGateway(userService);
    }
    
    private static void demoAuthenticationOffloading(BackendService service) {
        AuthenticationGateway gateway = new AuthenticationGateway(service);
        
        // Valid token
        Map<String, String> headers1 = new HashMap<>();
        headers1.put("Authorization", "token123");
        ServiceRequest req1 = new ServiceRequest("REQ1", "/users/123", "GET", headers1, null);
        gateway.handleRequest(req1);
        
        // Invalid token
        Map<String, String> headers2 = new HashMap<>();
        headers2.put("Authorization", "invalid");
        ServiceRequest req2 = new ServiceRequest("REQ2", "/users/123", "GET", headers2, null);
        gateway.handleRequest(req2);
    }
    
    private static void demoLoggingOffloading(BackendService service) {
        LoggingGateway gateway = new LoggingGateway(service);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "token123");
        
        for (int i = 1; i <= 3; i++) {
            ServiceRequest req = new ServiceRequest("REQ" + i, "/users/" + i, "GET", headers, null);
            gateway.handleRequest(req);
        }
        
        System.out.println("\nRequest log:");
        gateway.getRequestLog().forEach(System.out::println);
        System.out.println("Total requests: " + gateway.getTotalRequests());
    }
    
    private static void demoRateLimitingOffloading(BackendService service) {
        RateLimitingGateway gateway = new RateLimitingGateway(service, 3); // 3 requests per minute
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Client-ID", "client1");
        
        // Send 5 requests (last 2 should be rate limited)
        for (int i = 1; i <= 5; i++) {
            ServiceRequest req = new ServiceRequest("REQ" + i, "/orders", "GET", headers, null);
            ServiceResponse resp = gateway.handleRequest(req);
            System.out.println("Response status: " + resp.getStatusCode());
        }
    }
    
    private static void demoCachingOffloading(BackendService service) {
        CachingGateway gateway = new CachingGateway(service, 60); // 60 second TTL
        
        Map<String, String> headers = new HashMap<>();
        ServiceRequest req = new ServiceRequest("REQ1", "/users/123", "GET", headers, null);
        
        // First call - cache miss
        gateway.handleRequest(req);
        
        // Second call - cache hit
        gateway.handleRequest(req);
        
        // Third call - cache hit
        gateway.handleRequest(req);
    }
    
    private static void demoComprehensiveGateway(BackendService service) {
        ComprehensiveGateway gateway = new ComprehensiveGateway(service);
        
        // Request 1: Valid, should succeed
        Map<String, String> headers1 = new HashMap<>();
        headers1.put("Authorization", "token123");
        headers1.put("X-Client-ID", "client1");
        ServiceRequest req1 = new ServiceRequest("REQ1", "/users/123", "GET", headers1, null);
        gateway.handleRequest(req1);
        
        // Request 2: Same path, should hit cache
        ServiceRequest req2 = new ServiceRequest("REQ2", "/users/123", "GET", headers1, null);
        gateway.handleRequest(req2);
        
        // Request 3: Different path
        ServiceRequest req3 = new ServiceRequest("REQ3", "/users/456", "GET", headers1, null);
        gateway.handleRequest(req3);
        
        // Request 4: No auth, should fail
        Map<String, String> headers2 = new HashMap<>();
        ServiceRequest req4 = new ServiceRequest("REQ4", "/users/789", "GET", headers2, null);
        gateway.handleRequest(req4);
        
        // Print statistics
        gateway.printStatistics();
        
        System.out.println("\nDetailed logs:");
        gateway.getRequestLogs().forEach(System.out::println);
    }
}
