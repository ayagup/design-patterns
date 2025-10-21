package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Circuit Breaker Pattern
 * Prevents cascading failures by stopping calls to failing services.
 */
public class CircuitBreakerPattern {
    
    // Circuit Breaker States
    enum State {
        CLOSED,   // Normal operation, requests pass through
        OPEN,     // Failure threshold reached, requests fail fast
        HALF_OPEN // Testing if service recovered
    }
    
    // Circuit Breaker implementation
    static class CircuitBreaker {
        private State state = State.CLOSED;
        private int failureCount = 0;
        private int successCount = 0;
        private final int failureThreshold;
        private final int successThreshold;
        private final long timeout;
        private long lastFailureTime = 0;
        
        public CircuitBreaker(int failureThreshold, int successThreshold, long timeout) {
            this.failureThreshold = failureThreshold;
            this.successThreshold = successThreshold;
            this.timeout = timeout;
        }
        
        public <T> T call(Supplier<T> operation, Supplier<T> fallback) {
            if (state == State.OPEN) {
                if (System.currentTimeMillis() - lastFailureTime > timeout) {
                    System.out.println("⚡ Circuit transitioning to HALF_OPEN");
                    state = State.HALF_OPEN;
                    successCount = 0;
                } else {
                    System.out.println("⛔ Circuit is OPEN - failing fast");
                    return fallback.get();
                }
            }
            
            try {
                T result = operation.get();
                onSuccess();
                return result;
            } catch (Exception e) {
                onFailure();
                System.out.println("❌ Operation failed: " + e.getMessage());
                return fallback.get();
            }
        }
        
        private void onSuccess() {
            failureCount = 0;
            
            if (state == State.HALF_OPEN) {
                successCount++;
                System.out.println("✅ Success in HALF_OPEN (" + successCount + 
                                 "/" + successThreshold + ")");
                
                if (successCount >= successThreshold) {
                    System.out.println("🔓 Circuit CLOSED - service recovered");
                    state = State.CLOSED;
                    successCount = 0;
                }
            }
        }
        
        private void onFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
            
            if (state == State.HALF_OPEN) {
                System.out.println("❌ Failure in HALF_OPEN - reopening circuit");
                state = State.OPEN;
                successCount = 0;
            } else if (failureCount >= failureThreshold) {
                System.out.println("🔒 Circuit OPEN - too many failures (" + 
                                 failureCount + ")");
                state = State.OPEN;
            }
        }
        
        public State getState() {
            return state;
        }
    }
    
    // Simulated remote service
    static class RemoteService {
        private boolean isHealthy = true;
        private int callCount = 0;
        
        public String call() {
            callCount++;
            System.out.println("🌐 Calling remote service (call #" + callCount + ")");
            
            if (!isHealthy) {
                throw new RuntimeException("Service is down");
            }
            
            // Simulate occasional failures
            if (Math.random() > 0.7) {
                throw new RuntimeException("Random failure");
            }
            
            return "Success: Data from remote service";
        }
        
        public void setHealthy(boolean healthy) {
            this.isHealthy = healthy;
            System.out.println("\n💊 Service health set to: " + 
                             (healthy ? "HEALTHY" : "UNHEALTHY"));
        }
    }
    
    // Service with Circuit Breaker
    static class ProtectedService {
        private final RemoteService remoteService;
        private final CircuitBreaker circuitBreaker;
        
        public ProtectedService(RemoteService service) {
            this.remoteService = service;
            this.circuitBreaker = new CircuitBreaker(
                3,    // failure threshold
                2,    // success threshold
                5000  // timeout (5 seconds)
            );
        }
        
        public String getData() {
            return circuitBreaker.call(
                () -> remoteService.call(),
                () -> {
                    System.out.println("💾 Using cached/fallback data");
                    return "Fallback: Cached data";
                }
            );
        }
        
        public State getCircuitState() {
            return circuitBreaker.getState();
        }
    }
    
    // HTTP Client with Circuit Breaker example
    static class HttpClient {
        private final CircuitBreaker circuitBreaker;
        
        public HttpClient() {
            this.circuitBreaker = new CircuitBreaker(5, 3, 10000);
        }
        
        public String get(String url) {
            return circuitBreaker.call(
                () -> makeRequest(url),
                () -> "Error: Service unavailable"
            );
        }
        
        private String makeRequest(String url) {
            System.out.println("📡 GET " + url);
            // Simulate network call
            if (Math.random() > 0.5) {
                throw new RuntimeException("Network timeout");
            }
            return "Response from " + url;
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Circuit Breaker Pattern Demo ===\n");
        
        // 1. Basic Circuit Breaker behavior
        System.out.println("1. Normal Operation → Open → Half-Open → Closed:");
        RemoteService service = new RemoteService();
        ProtectedService protectedService = new ProtectedService(service);
        
        // Normal operation (CLOSED)
        System.out.println("\n--- Phase 1: Normal Operation (CLOSED) ---");
        for (int i = 0; i < 3; i++) {
            String result = protectedService.getData();
            System.out.println("Result: " + result);
            System.out.println("Circuit state: " + protectedService.getCircuitState());
            System.out.println();
            Thread.sleep(500);
        }
        
        // Service goes down
        System.out.println("--- Phase 2: Service Fails (OPEN) ---");
        service.setHealthy(false);
        
        for (int i = 0; i < 5; i++) {
            String result = protectedService.getData();
            System.out.println("Result: " + result);
            System.out.println("Circuit state: " + protectedService.getCircuitState());
            System.out.println();
            Thread.sleep(500);
        }
        
        // Wait for timeout
        System.out.println("--- Phase 3: Waiting for timeout... ---");
        Thread.sleep(5000);
        
        // Service recovers
        System.out.println("--- Phase 4: Service Recovers (HALF_OPEN → CLOSED) ---");
        service.setHealthy(true);
        
        for (int i = 0; i < 4; i++) {
            String result = protectedService.getData();
            System.out.println("Result: " + result);
            System.out.println("Circuit state: " + protectedService.getCircuitState());
            System.out.println();
            Thread.sleep(500);
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. HTTP Client example
        System.out.println("\n2. HTTP Client with Circuit Breaker:");
        HttpClient client = new HttpClient();
        
        for (int i = 0; i < 8; i++) {
            System.out.println("\nRequest " + (i + 1) + ":");
            String response = client.get("https://api.example.com/data");
            System.out.println("Response: " + response);
            Thread.sleep(300);
        }
        
        System.out.println("\n--- Circuit Breaker States ---");
        System.out.println("🔓 CLOSED: Normal operation");
        System.out.println("   - All requests pass through");
        System.out.println("   - Failures are counted");
        
        System.out.println("\n🔒 OPEN: Service unavailable");
        System.out.println("   - Requests fail immediately");
        System.out.println("   - No calls to failing service");
        System.out.println("   - Returns fallback response");
        
        System.out.println("\n⚡ HALF_OPEN: Testing recovery");
        System.out.println("   - Limited requests allowed");
        System.out.println("   - Success → CLOSED");
        System.out.println("   - Failure → OPEN");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Prevents cascading failures");
        System.out.println("✓ Fails fast when service is down");
        System.out.println("✓ Automatic recovery detection");
        System.out.println("✓ Protects downstream services");
        System.out.println("✓ Improves system resilience");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Microservices communication");
        System.out.println("• External API calls");
        System.out.println("• Database connections");
        System.out.println("• Remote service invocations");
        System.out.println("• Distributed systems");
        
        System.out.println("\n--- Real-World Implementations ---");
        System.out.println("• Netflix Hystrix (retired)");
        System.out.println("• Resilience4j");
        System.out.println("• Spring Cloud Circuit Breaker");
        System.out.println("• Polly (.NET)");
    }
}
