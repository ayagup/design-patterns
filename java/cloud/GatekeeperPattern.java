package cloud;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gatekeeper Pattern
 * 
 * Intent: Protect applications and services by using a dedicated host instance
 * that acts as a broker between clients and the application, validates and
 * sanitizes requests, and passes requests to the application.
 * 
 * Also Known As: Security Gateway, Perimeter Defense, DMZ Gateway
 * 
 * Motivation:
 * Exposing application directly to the internet creates security risks:
 * - SQL injection, XSS, and other injection attacks
 * - Malformed requests causing crashes
 * - DDoS and abuse
 * - Sensitive error information leakage
 * 
 * Gatekeeper provides an additional security layer.
 * 
 * Applicability:
 * - Public-facing APIs requiring request validation
 * - Applications with sensitive internal resources
 * - Systems requiring audit logging of all access
 * - Defense in depth security architecture
 * - Compliance requirements for access control
 */

/**
 * Request to be validated by gatekeeper
 */
class IncomingRequest {
    private final String id;
    private final String clientId;
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;
    private final String sourceIp;
    private final Instant timestamp;
    
    public IncomingRequest(String id, String clientId, String method, String path) {
        this.id = id;
        this.clientId = clientId;
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>();
        this.body = "";
        this.sourceIp = "0.0.0.0";
        this.timestamp = Instant.now();
    }
    
    public String getId() { return id; }
    public String getClientId() { return clientId; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
    public String getSourceIp() { return sourceIp; }
    public Instant getTimestamp() { return timestamp; }
}

/**
 * Validation result from gatekeeper
 */
class ValidationResult {
    private final boolean valid;
    private final List<String> violations;
    private final String sanitizedBody;
    
    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.violations = new ArrayList<>();
        this.sanitizedBody = null;
    }
    
    public ValidationResult(boolean valid, String sanitizedBody) {
        this.valid = valid;
        this.violations = new ArrayList<>();
        this.sanitizedBody = sanitizedBody;
    }
    
    public boolean isValid() { return valid; }
    public List<String> getViolations() { return violations; }
    public String getSanitizedBody() { return sanitizedBody; }
    
    public void addViolation(String violation) {
        violations.add(violation);
    }
}

/**
 * Gateway response
 */
class GatewayResponse {
    private final int statusCode;
    private final String message;
    private final Object data;
    
    public GatewayResponse(int statusCode, String message, Object data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }
    
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}

/**
 * Example 1: Basic Gatekeeper
 * 
 * Validates requests before forwarding to protected application.
 * Provides request sanitization and basic security checks.
 */
class BasicGatekeeper {
    private final Set<String> allowedPaths;
    private final Set<String> allowedMethods;
    private final AtomicLong requestCounter;
    private final AtomicLong blockedCounter;
    
    public BasicGatekeeper() {
        this.allowedPaths = new HashSet<>(Arrays.asList(
            "/api/users", "/api/products", "/api/orders"
        ));
        this.allowedMethods = new HashSet<>(Arrays.asList(
            "GET", "POST", "PUT", "DELETE"
        ));
        this.requestCounter = new AtomicLong(0);
        this.blockedCounter = new AtomicLong(0);
    }
    
    public GatewayResponse processRequest(IncomingRequest request) {
        requestCounter.incrementAndGet();
        
        // Validate method
        if (!allowedMethods.contains(request.getMethod())) {
            blockedCounter.incrementAndGet();
            return new GatewayResponse(405, "Method not allowed", null);
        }
        
        // Validate path
        if (!isPathAllowed(request.getPath())) {
            blockedCounter.incrementAndGet();
            return new GatewayResponse(403, "Forbidden path", null);
        }
        
        // Sanitize and validate body
        ValidationResult validation = validateAndSanitize(request);
        if (!validation.isValid()) {
            blockedCounter.incrementAndGet();
            return new GatewayResponse(400, 
                "Invalid request: " + validation.getViolations(), null);
        }
        
        // Forward to protected application
        Object result = forwardToApplication(request, validation.getSanitizedBody());
        return new GatewayResponse(200, "Success", result);
    }
    
    private boolean isPathAllowed(String path) {
        return allowedPaths.stream().anyMatch(path::startsWith);
    }
    
    private ValidationResult validateAndSanitize(IncomingRequest request) {
        String body = request.getBody();
        
        // Check for SQL injection patterns
        if (body.toLowerCase().contains("drop table") ||
            body.toLowerCase().contains("delete from") ||
            body.toLowerCase().contains("union select")) {
            ValidationResult result = new ValidationResult(false);
            result.addViolation("Potential SQL injection detected");
            return result;
        }
        
        // Check for XSS patterns
        if (body.contains("<script>") || body.contains("javascript:")) {
            ValidationResult result = new ValidationResult(false);
            result.addViolation("Potential XSS attack detected");
            return result;
        }
        
        // Sanitize body (remove dangerous characters)
        String sanitized = body.replaceAll("[<>\"']", "");
        
        return new ValidationResult(true, sanitized);
    }
    
    private Object forwardToApplication(IncomingRequest request, String sanitizedBody) {
        // Simulate forwarding to protected application
        return "Processed: " + request.getMethod() + " " + request.getPath();
    }
    
    public Map<String, Long> getMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("totalRequests", requestCounter.get());
        metrics.put("blockedRequests", blockedCounter.get());
        metrics.put("allowedRequests", requestCounter.get() - blockedCounter.get());
        return metrics;
    }
}

/**
 * Example 2: Rate-Limiting Gatekeeper
 * 
 * Combines security validation with rate limiting.
 * Protects against DDoS and abuse.
 */
class RateLimitingGatekeeper {
    private final Map<String, ClientRateLimit> clientLimits;
    private final int maxRequestsPerMinute;
    
    static class ClientRateLimit {
        private final List<Instant> requestTimestamps;
        private final int maxRequests;
        
        ClientRateLimit(int maxRequests) {
            this.requestTimestamps = new ArrayList<>();
            this.maxRequests = maxRequests;
        }
        
        synchronized boolean allowRequest() {
            Instant now = Instant.now();
            Instant oneMinuteAgo = now.minusSeconds(60);
            
            // Remove old timestamps
            requestTimestamps.removeIf(ts -> ts.isBefore(oneMinuteAgo));
            
            if (requestTimestamps.size() >= maxRequests) {
                return false;
            }
            
            requestTimestamps.add(now);
            return true;
        }
        
        int getCurrentCount() {
            Instant oneMinuteAgo = Instant.now().minusSeconds(60);
            requestTimestamps.removeIf(ts -> ts.isBefore(oneMinuteAgo));
            return requestTimestamps.size();
        }
    }
    
    public RateLimitingGatekeeper(int maxRequestsPerMinute) {
        this.clientLimits = new ConcurrentHashMap<>();
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }
    
    public GatewayResponse processRequest(IncomingRequest request) {
        ClientRateLimit rateLimit = clientLimits.computeIfAbsent(
            request.getClientId(),
            id -> new ClientRateLimit(maxRequestsPerMinute)
        );
        
        if (!rateLimit.allowRequest()) {
            return new GatewayResponse(429, 
                "Rate limit exceeded: " + maxRequestsPerMinute + " requests/minute", 
                null);
        }
        
        // Additional validation
        if (request.getPath().contains("..")) {
            return new GatewayResponse(400, "Path traversal detected", null);
        }
        
        // Forward to application
        return new GatewayResponse(200, "Request processed", 
            "Forwarded: " + request.getPath());
    }
    
    public Map<String, Integer> getRateLimitStatus() {
        Map<String, Integer> status = new HashMap<>();
        clientLimits.forEach((clientId, limit) -> 
            status.put(clientId, limit.getCurrentCount())
        );
        return status;
    }
}

/**
 * Example 3: Authentication Gatekeeper
 * 
 * Validates authentication tokens before allowing access.
 * Separates authentication concerns from application logic.
 */
class AuthenticationGatekeeper {
    private final Map<String, TokenInfo> validTokens;
    private final Set<String> revokedTokens;
    
    static class TokenInfo {
        String token;
        String userId;
        Set<String> roles;
        Instant expiresAt;
        
        TokenInfo(String token, String userId, Set<String> roles, Instant expiresAt) {
            this.token = token;
            this.userId = userId;
            this.roles = roles;
            this.expiresAt = expiresAt;
        }
        
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
    
    public AuthenticationGatekeeper() {
        this.validTokens = new ConcurrentHashMap<>();
        this.revokedTokens = ConcurrentHashMap.newKeySet();
        
        // Pre-populate with test tokens
        initializeTokens();
    }
    
    private void initializeTokens() {
        validTokens.put("token-admin-123", new TokenInfo(
            "token-admin-123",
            "admin",
            Set.of("ADMIN", "USER"),
            Instant.now().plusSeconds(3600)
        ));
        
        validTokens.put("token-user-456", new TokenInfo(
            "token-user-456",
            "user1",
            Set.of("USER"),
            Instant.now().plusSeconds(3600)
        ));
    }
    
    public GatewayResponse processRequest(IncomingRequest request) {
        // Extract token from header
        String authHeader = request.getHeaders().get("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new GatewayResponse(401, "Missing or invalid authorization header", null);
        }
        
        String token = authHeader.substring(7);
        
        // Check if token is revoked
        if (revokedTokens.contains(token)) {
            return new GatewayResponse(401, "Token has been revoked", null);
        }
        
        // Validate token
        TokenInfo tokenInfo = validTokens.get(token);
        if (tokenInfo == null) {
            return new GatewayResponse(401, "Invalid token", null);
        }
        
        if (!tokenInfo.isValid()) {
            return new GatewayResponse(401, "Token expired", null);
        }
        
        // Check authorization for path
        if (request.getPath().startsWith("/api/admin") && 
            !tokenInfo.roles.contains("ADMIN")) {
            return new GatewayResponse(403, "Insufficient permissions", null);
        }
        
        // Forward with user context
        return new GatewayResponse(200, "Authenticated", 
            "User: " + tokenInfo.userId + " accessing " + request.getPath());
    }
    
    public void revokeToken(String token) {
        revokedTokens.add(token);
    }
}

/**
 * Example 4: Multi-Layer Gatekeeper
 * 
 * Applies multiple validation layers in sequence.
 * Each layer can reject the request independently.
 */
class MultiLayerGatekeeper {
    private final List<ValidationLayer> layers;
    private final AtomicLong requestsProcessed;
    private final Map<String, AtomicLong> rejectionsByLayer;
    
    interface ValidationLayer {
        String getName();
        ValidationResult validate(IncomingRequest request);
    }
    
    public MultiLayerGatekeeper() {
        this.layers = new ArrayList<>();
        this.requestsProcessed = new AtomicLong(0);
        this.rejectionsByLayer = new ConcurrentHashMap<>();
        
        // Add default layers
        addLayer(new IPWhitelistLayer());
        addLayer(new RequestSizeLayer());
        addLayer(new ContentTypeLayer());
        addLayer(new SecurityPatternLayer());
    }
    
    public void addLayer(ValidationLayer layer) {
        layers.add(layer);
        rejectionsByLayer.put(layer.getName(), new AtomicLong(0));
    }
    
    public GatewayResponse processRequest(IncomingRequest request) {
        requestsProcessed.incrementAndGet();
        
        // Apply each validation layer
        for (ValidationLayer layer : layers) {
            ValidationResult result = layer.validate(request);
            
            if (!result.isValid()) {
                rejectionsByLayer.get(layer.getName()).incrementAndGet();
                return new GatewayResponse(400, 
                    "Rejected by " + layer.getName() + ": " + result.getViolations(),
                    null);
            }
        }
        
        // All layers passed
        return new GatewayResponse(200, "Request validated and forwarded", 
            "Processed: " + request.getPath());
    }
    
    // Layer 1: IP Whitelist
    static class IPWhitelistLayer implements ValidationLayer {
        private final Set<String> allowedIps;
        
        IPWhitelistLayer() {
            this.allowedIps = Set.of("192.168.1.100", "10.0.0.1", "0.0.0.0");
        }
        
        @Override
        public String getName() { return "IP Whitelist"; }
        
        @Override
        public ValidationResult validate(IncomingRequest request) {
            if (!allowedIps.contains(request.getSourceIp())) {
                ValidationResult result = new ValidationResult(false);
                result.addViolation("IP not whitelisted: " + request.getSourceIp());
                return result;
            }
            return new ValidationResult(true);
        }
    }
    
    // Layer 2: Request Size
    static class RequestSizeLayer implements ValidationLayer {
        private static final int MAX_BODY_SIZE = 1024 * 1024; // 1MB
        
        @Override
        public String getName() { return "Request Size"; }
        
        @Override
        public ValidationResult validate(IncomingRequest request) {
            if (request.getBody().length() > MAX_BODY_SIZE) {
                ValidationResult result = new ValidationResult(false);
                result.addViolation("Request body too large: " + 
                    request.getBody().length() + " > " + MAX_BODY_SIZE);
                return result;
            }
            return new ValidationResult(true);
        }
    }
    
    // Layer 3: Content Type
    static class ContentTypeLayer implements ValidationLayer {
        private final Set<String> allowedContentTypes;
        
        ContentTypeLayer() {
            this.allowedContentTypes = Set.of(
                "application/json", "application/xml", "text/plain"
            );
        }
        
        @Override
        public String getName() { return "Content Type"; }
        
        @Override
        public ValidationResult validate(IncomingRequest request) {
            String contentType = request.getHeaders().get("Content-Type");
            if (contentType != null && !allowedContentTypes.contains(contentType)) {
                ValidationResult result = new ValidationResult(false);
                result.addViolation("Content type not allowed: " + contentType);
                return result;
            }
            return new ValidationResult(true);
        }
    }
    
    // Layer 4: Security Patterns
    static class SecurityPatternLayer implements ValidationLayer {
        @Override
        public String getName() { return "Security Patterns"; }
        
        @Override
        public ValidationResult validate(IncomingRequest request) {
            String body = request.getBody().toLowerCase();
            
            if (body.contains("eval(") || body.contains("exec(")) {
                ValidationResult result = new ValidationResult(false);
                result.addViolation("Code injection pattern detected");
                return result;
            }
            
            if (body.contains("../") || body.contains("..\\")) {
                ValidationResult result = new ValidationResult(false);
                result.addViolation("Path traversal pattern detected");
                return result;
            }
            
            return new ValidationResult(true);
        }
    }
    
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", requestsProcessed.get());
        
        Map<String, Long> rejections = new HashMap<>();
        rejectionsByLayer.forEach((layer, count) -> 
            rejections.put(layer, count.get())
        );
        metrics.put("rejectionsByLayer", rejections);
        
        return metrics;
    }
}

/**
 * Example 5: Circuit Breaker Gatekeeper
 * 
 * Protects backend by failing fast when it's unhealthy.
 * Prevents cascading failures.
 */
class CircuitBreakerGatekeeper {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    
    private volatile State state;
    private final int failureThreshold;
    private final long timeoutMs;
    private int failureCount;
    private Instant lastFailureTime;
    private final AtomicLong successCount;
    private final AtomicLong circuitOpenCount;
    
    public CircuitBreakerGatekeeper(int failureThreshold, long timeoutMs) {
        this.state = State.CLOSED;
        this.failureThreshold = failureThreshold;
        this.timeoutMs = timeoutMs;
        this.failureCount = 0;
        this.successCount = new AtomicLong(0);
        this.circuitOpenCount = new AtomicLong(0);
    }
    
    public synchronized GatewayResponse processRequest(IncomingRequest request) {
        // Check if circuit should transition from OPEN to HALF_OPEN
        if (state == State.OPEN && lastFailureTime != null) {
            long elapsed = System.currentTimeMillis() - lastFailureTime.toEpochMilli();
            if (elapsed >= timeoutMs) {
                state = State.HALF_OPEN;
                System.out.println("Circuit breaker transitioning to HALF_OPEN");
            }
        }
        
        // Reject immediately if circuit is open
        if (state == State.OPEN) {
            circuitOpenCount.incrementAndGet();
            return new GatewayResponse(503, 
                "Service unavailable - circuit breaker is OPEN", null);
        }
        
        // Try to forward request
        try {
            // Simulate backend call
            if (simulateBackendCall(request)) {
                onSuccess();
                return new GatewayResponse(200, "Request processed", 
                    "Forwarded: " + request.getPath());
            } else {
                onFailure();
                return new GatewayResponse(500, "Backend error", null);
            }
        } catch (Exception e) {
            onFailure();
            return new GatewayResponse(500, "Backend exception: " + e.getMessage(), null);
        }
    }
    
    private boolean simulateBackendCall(IncomingRequest request) {
        // Simulate occasional failures
        return !request.getPath().contains("fail");
    }
    
    private synchronized void onSuccess() {
        successCount.incrementAndGet();
        failureCount = 0;
        
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            System.out.println("Circuit breaker closed after successful request");
        }
    }
    
    private synchronized void onFailure() {
        failureCount++;
        lastFailureTime = Instant.now();
        
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
            System.out.println("Circuit breaker opened after " + failureCount + " failures");
        }
    }
    
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("state", state.toString());
        metrics.put("successCount", successCount.get());
        metrics.put("failureCount", failureCount);
        metrics.put("circuitOpenRejections", circuitOpenCount.get());
        return metrics;
    }
}

/**
 * Demonstration of the Gatekeeper Pattern
 */
public class GatekeeperPattern {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Gatekeeper Pattern Demo ===\n");
        
        // Example 1: Basic Gatekeeper
        System.out.println("1. Basic Gatekeeper (validation & sanitization):");
        BasicGatekeeper basic = new BasicGatekeeper();
        
        IncomingRequest validRequest = new IncomingRequest(
            "req-1", "client-1", "GET", "/api/users");
        GatewayResponse response1 = basic.processRequest(validRequest);
        System.out.println("Valid request: " + response1.getStatusCode() + " - " + 
            response1.getMessage());
        
        IncomingRequest sqlInjection = new IncomingRequest(
            "req-2", "client-1", "POST", "/api/users");
        GatewayResponse response2 = basic.processRequest(sqlInjection);
        System.out.println("SQL injection attempt: " + response2.getStatusCode() + " - " + 
            response2.getMessage());
        
        IncomingRequest invalidPath = new IncomingRequest(
            "req-3", "client-1", "GET", "/internal/config");
        GatewayResponse response3 = basic.processRequest(invalidPath);
        System.out.println("Invalid path: " + response3.getStatusCode() + " - " + 
            response3.getMessage());
        
        System.out.println("Metrics: " + basic.getMetrics());
        
        // Example 2: Rate-Limiting Gatekeeper
        System.out.println("\n2. Rate-Limiting Gatekeeper (DDoS protection):");
        RateLimitingGatekeeper rateLimiter = new RateLimitingGatekeeper(3); // 3 req/min
        
        for (int i = 1; i <= 5; i++) {
            IncomingRequest req = new IncomingRequest(
                "req-" + i, "client-2", "GET", "/api/products");
            GatewayResponse response = rateLimiter.processRequest(req);
            System.out.println("Request " + i + ": " + response.getStatusCode() + " - " + 
                response.getMessage());
        }
        
        System.out.println("Rate limit status: " + rateLimiter.getRateLimitStatus());
        
        // Example 3: Authentication Gatekeeper
        System.out.println("\n3. Authentication Gatekeeper (token validation):");
        AuthenticationGatekeeper auth = new AuthenticationGatekeeper();
        
        IncomingRequest authRequest = new IncomingRequest(
            "req-1", "client-3", "GET", "/api/users");
        authRequest.getHeaders().put("Authorization", "Bearer token-user-456");
        GatewayResponse authResponse = auth.processRequest(authRequest);
        System.out.println("Authenticated request: " + authResponse.getStatusCode() + 
            " - " + authResponse.getData());
        
        IncomingRequest adminRequest = new IncomingRequest(
            "req-2", "client-3", "GET", "/api/admin/settings");
        adminRequest.getHeaders().put("Authorization", "Bearer token-user-456");
        GatewayResponse adminResponse = auth.processRequest(adminRequest);
        System.out.println("Admin access (insufficient perms): " + 
            adminResponse.getStatusCode() + " - " + adminResponse.getMessage());
        
        IncomingRequest noAuthRequest = new IncomingRequest(
            "req-3", "client-3", "GET", "/api/users");
        GatewayResponse noAuthResponse = auth.processRequest(noAuthRequest);
        System.out.println("No auth header: " + noAuthResponse.getStatusCode() + 
            " - " + noAuthResponse.getMessage());
        
        // Example 4: Multi-Layer Gatekeeper
        System.out.println("\n4. Multi-Layer Gatekeeper (defense in depth):");
        MultiLayerGatekeeper multiLayer = new MultiLayerGatekeeper();
        
        IncomingRequest layerTest1 = new IncomingRequest(
            "req-1", "client-4", "POST", "/api/data");
        layerTest1.getHeaders().put("Content-Type", "application/json");
        GatewayResponse layerResponse1 = multiLayer.processRequest(layerTest1);
        System.out.println("All layers passed: " + layerResponse1.getStatusCode() + 
            " - " + layerResponse1.getMessage());
        
        IncomingRequest layerTest2 = new IncomingRequest(
            "req-2", "client-4", "POST", "/api/data");
        layerTest2.getHeaders().put("Content-Type", "application/javascript");
        GatewayResponse layerResponse2 = multiLayer.processRequest(layerTest2);
        System.out.println("Invalid content type: " + layerResponse2.getStatusCode() + 
            " - " + layerResponse2.getMessage());
        
        System.out.println("Multi-layer metrics: " + multiLayer.getMetrics());
        
        // Example 5: Circuit Breaker Gatekeeper
        System.out.println("\n5. Circuit Breaker Gatekeeper (backend protection):");
        CircuitBreakerGatekeeper circuitBreaker = new CircuitBreakerGatekeeper(3, 5000);
        
        // Successful requests
        for (int i = 1; i <= 2; i++) {
            IncomingRequest req = new IncomingRequest(
                "req-" + i, "client-5", "GET", "/api/success");
            GatewayResponse response = circuitBreaker.processRequest(req);
            System.out.println("Success request " + i + ": " + response.getStatusCode());
        }
        
        // Trigger failures to open circuit
        System.out.println("\nTriggering failures...");
        for (int i = 1; i <= 4; i++) {
            IncomingRequest req = new IncomingRequest(
                "req-fail-" + i, "client-5", "GET", "/api/fail");
            GatewayResponse response = circuitBreaker.processRequest(req);
            System.out.println("Failure request " + i + ": " + response.getStatusCode() + 
                " - " + response.getMessage());
        }
        
        System.out.println("\nCircuit breaker metrics: " + circuitBreaker.getMetrics());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Additional security layer (defense in depth)");
        System.out.println("✓ Request validation and sanitization");
        System.out.println("✓ Rate limiting and DDoS protection");
        System.out.println("✓ Authentication and authorization enforcement");
        System.out.println("✓ Circuit breaker for backend protection");
        System.out.println("✓ Audit logging and monitoring");
        System.out.println("✓ Separation of security concerns from application logic");
    }
}
