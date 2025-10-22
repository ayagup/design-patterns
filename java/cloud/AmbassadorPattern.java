package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AMBASSADOR PATTERN
 * 
 * Creates helper services that send network requests on behalf of a consumer service or application.
 * Acts as a sidecar proxy that handles cross-cutting concerns like monitoring, logging, routing,
 * and circuit breaking.
 * 
 * Benefits:
 * - Offloads common client connectivity tasks
 * - Language-agnostic (ambassador can be in different language than application)
 * - Centralizes network communication logic
 * - Easier testing (mock ambassador)
 * - Handles retries, circuit breaking, monitoring
 * 
 * Use Cases:
 * - Microservices communication
 * - Legacy application modernization
 * - Cross-cutting concerns (logging, monitoring)
 * - Network resilience patterns
 * - Service mesh implementations
 */

// Remote Service Interface
interface RemoteService {
    String call(String request);
}

// Real Remote Service (simulated)
class RealRemoteService implements RemoteService {
    private final String serviceName;
    private final Random random = new Random();
    private final AtomicInteger callCount = new AtomicInteger(0);
    
    public RealRemoteService(String serviceName) {
        this.serviceName = serviceName;
    }
    
    @Override
    public String call(String request) {
        int count = callCount.incrementAndGet();
        
        // Simulate network latency
        try {
            Thread.sleep(random.nextInt(100) + 50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Simulate occasional failures
        if (random.nextInt(10) < 2) { // 20% failure rate
            throw new RuntimeException("Service temporarily unavailable");
        }
        
        return "[" + serviceName + " #" + count + "] Response to: " + request;
    }
}

// Ambassador - Proxy with additional features
class ServiceAmbassador implements RemoteService {
    private final RemoteService realService;
    private final CircuitBreaker circuitBreaker;
    private final RetryPolicy retryPolicy;
    private final RequestLogger logger;
    private final PerformanceMonitor monitor;
    
    public ServiceAmbassador(RemoteService realService) {
        this.realService = realService;
        this.circuitBreaker = new CircuitBreaker();
        this.retryPolicy = new RetryPolicy(3);
        this.logger = new RequestLogger();
        this.monitor = new PerformanceMonitor();
    }
    
    @Override
    public String call(String request) {
        logger.logRequest(request);
        long startTime = System.currentTimeMillis();
        
        try {
            // Check circuit breaker
            if (!circuitBreaker.allowRequest()) {
                String error = "Circuit breaker is OPEN - request blocked";
                logger.logError(request, error);
                throw new RuntimeException(error);
            }
            
            // Execute with retry
            String response = retryPolicy.execute(() -> realService.call(request));
            
            // Record success
            long duration = System.currentTimeMillis() - startTime;
            circuitBreaker.recordSuccess();
            monitor.recordRequest(duration, true);
            logger.logResponse(request, response, duration);
            
            return response;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            circuitBreaker.recordFailure();
            monitor.recordRequest(duration, false);
            logger.logError(request, e.getMessage());
            throw e;
        }
    }
    
    public void printStatistics() {
        System.out.println("\n📊 Ambassador Statistics:");
        monitor.printStats();
        circuitBreaker.printStatus();
    }
}

// Circuit Breaker
class CircuitBreaker {
    private enum State { CLOSED, OPEN, HALF_OPEN }
    
    private State state = State.CLOSED;
    private int failureCount = 0;
    private int successCount = 0;
    private final int failureThreshold = 3;
    private final int successThreshold = 2;
    private long openedTime = 0;
    private final long timeout = 5000; // 5 seconds
    
    public synchronized boolean allowRequest() {
        if (state == State.CLOSED) {
            return true;
        }
        
        if (state == State.OPEN) {
            // Check if timeout has passed
            if (System.currentTimeMillis() - openedTime >= timeout) {
                System.out.println("  🔄 Circuit breaker: OPEN → HALF_OPEN");
                state = State.HALF_OPEN;
                successCount = 0;
                return true;
            }
            return false;
        }
        
        // HALF_OPEN state
        return true;
    }
    
    public synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            successCount++;
            if (successCount >= successThreshold) {
                System.out.println("  ✅ Circuit breaker: HALF_OPEN → CLOSED");
                state = State.CLOSED;
                failureCount = 0;
            }
        } else {
            failureCount = 0;
        }
    }
    
    public synchronized void recordFailure() {
        failureCount++;
        
        if (state == State.CLOSED && failureCount >= failureThreshold) {
            System.out.println("  🚫 Circuit breaker: CLOSED → OPEN");
            state = State.OPEN;
            openedTime = System.currentTimeMillis();
        } else if (state == State.HALF_OPEN) {
            System.out.println("  🚫 Circuit breaker: HALF_OPEN → OPEN");
            state = State.OPEN;
            openedTime = System.currentTimeMillis();
        }
    }
    
    public void printStatus() {
        System.out.println("  🔌 Circuit Breaker State: " + state);
        System.out.println("  ⚠️  Failure Count: " + failureCount);
    }
}

// Retry Policy
class RetryPolicy {
    private final int maxAttempts;
    
    public RetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
    
    public String execute(Callable<String> operation) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxAttempts) {
            attempt++;
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    System.out.println("  🔄 Retry attempt " + attempt + "/" + maxAttempts);
                    try {
                        Thread.sleep(100 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed after " + maxAttempts + " attempts", lastException);
    }
}

// Request Logger
class RequestLogger {
    private final AtomicInteger requestCount = new AtomicInteger(0);
    
    public void logRequest(String request) {
        int count = requestCount.incrementAndGet();
        System.out.println("\n📤 [Request #" + count + "] " + request);
    }
    
    public void logResponse(String request, String response, long duration) {
        System.out.println("📥 [Response] " + response + " (" + duration + "ms)");
    }
    
    public void logError(String request, String error) {
        System.out.println("❌ [Error] " + error);
    }
}

// Performance Monitor
class PerformanceMonitor {
    private final List<Long> responseTimes = new CopyOnWriteArrayList<>();
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    
    public void recordRequest(long duration, boolean success) {
        responseTimes.add(duration);
        if (success) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
    }
    
    public void printStats() {
        int total = successCount.get() + failureCount.get();
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        System.out.println("  📈 Total Requests: " + total);
        System.out.println("  ✅ Successful: " + successCount.get());
        System.out.println("  ❌ Failed: " + failureCount.get());
        System.out.println("  ⏱️  Avg Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
    }
}

// Client Application
class ClientApplication {
    private final RemoteService service;
    
    public ClientApplication(RemoteService service) {
        this.service = service;
    }
    
    public void makeRequest(String request) {
        try {
            String response = service.call(request);
            System.out.println("✅ Application received: " + response);
        } catch (Exception e) {
            System.out.println("❌ Application error: " + e.getMessage());
        }
    }
}

// Demo
public class AmbassadorPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   AMBASSADOR PATTERN DEMONSTRATION   ║");
        System.out.println("╚══════════════════════════════════════╝");
        
        // Example 1: Without Ambassador (direct calls)
        System.out.println("\n1. WITHOUT AMBASSADOR (Direct Calls)");
        System.out.println("─────────────────────────────────────");
        
        RemoteService directService = new RealRemoteService("DirectAPI");
        ClientApplication directClient = new ClientApplication(directService);
        
        for (int i = 1; i <= 3; i++) {
            System.out.println("\n[Direct Call " + i + "]");
            try {
                directClient.makeRequest("Direct request " + i);
            } catch (Exception e) {
                System.out.println("❌ Failed: " + e.getMessage());
            }
        }
        
        // Example 2: With Ambassador (proxy with features)
        System.out.println("\n\n2. WITH AMBASSADOR (Enhanced Communication)");
        System.out.println("────────────────────────────────────────────");
        
        RemoteService remoteService = new RealRemoteService("UserAPI");
        ServiceAmbassador ambassador = new ServiceAmbassador(remoteService);
        ClientApplication client = new ClientApplication(ambassador);
        
        // Make successful requests
        for (int i = 1; i <= 5; i++) {
            client.makeRequest("Get user " + i);
            Thread.sleep(100);
        }
        
        // Example 3: Circuit Breaker in Action
        System.out.println("\n\n3. CIRCUIT BREAKER DEMONSTRATION");
        System.out.println("─────────────────────────────────────");
        
        RemoteService flakyService = new RealRemoteService("FlakyAPI");
        ServiceAmbassador flakyAmbassador = new ServiceAmbassador(flakyService);
        ClientApplication flakyClient = new ClientApplication(flakyAmbassador);
        
        System.out.println("\n[Forcing failures to trigger circuit breaker]");
        
        for (int i = 1; i <= 10; i++) {
            flakyClient.makeRequest("Request " + i);
            Thread.sleep(100);
        }
        
        // Example 4: Statistics
        System.out.println("\n\n4. PERFORMANCE STATISTICS");
        System.out.println("─────────────────────────");
        
        ambassador.printStatistics();
        flakyAmbassador.printStatistics();
        
        // Example 5: Multiple Services with Ambassadors
        System.out.println("\n\n5. MULTIPLE SERVICES (Microservices Architecture)");
        System.out.println("──────────────────────────────────────────────────");
        
        // User Service
        RemoteService userService = new RealRemoteService("UserService");
        ServiceAmbassador userAmbassador = new ServiceAmbassador(userService);
        
        // Order Service
        RemoteService orderService = new RealRemoteService("OrderService");
        ServiceAmbassador orderAmbassador = new ServiceAmbassador(orderService);
        
        // Product Service
        RemoteService productService = new RealRemoteService("ProductService");
        ServiceAmbassador productAmbassador = new ServiceAmbassador(productService);
        
        System.out.println("\n[Calling multiple services through ambassadors]");
        
        ClientApplication multiClient = new ClientApplication(userAmbassador);
        multiClient.makeRequest("Get user profile");
        
        ClientApplication orderClient = new ClientApplication(orderAmbassador);
        orderClient.makeRequest("Get user orders");
        
        ClientApplication productClient = new ClientApplication(productAmbassador);
        productClient.makeRequest("Get product catalog");
        
        System.out.println("\n\n✅ Ambassador Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Automatic retry with exponential backoff");
        System.out.println("  • Circuit breaker prevents cascade failures");
        System.out.println("  • Request/response logging for debugging");
        System.out.println("  • Performance monitoring and metrics");
        System.out.println("  • Centralized cross-cutting concerns");
        
        System.out.println("\n🏗️  Common Use Cases:");
        System.out.println("  • Microservices sidecar proxies");
        System.out.println("  • Service mesh implementations (Istio, Linkerd)");
        System.out.println("  • Legacy system integration");
        System.out.println("  • Cloud-native applications");
    }
}
