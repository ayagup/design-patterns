package additional;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * INTERCEPTOR PATTERN
 * 
 * Intercepts and modifies requests/responses or method calls in a transparent way.
 * Provides hooks for pre-processing and post-processing operations.
 * 
 * Benefits:
 * - Cross-cutting concerns separation
 * - Non-intrusive logging, authentication, validation
 * - Dynamic behavior modification
 * - Chain of responsibility for processing
 * - Clean separation of core and auxiliary logic
 * 
 * Use Cases:
 * - Logging and auditing
 * - Authentication and authorization
 * - Request validation
 * - Performance monitoring
 * - Caching
 * - Transaction management
 */

// Interceptor interface
interface Interceptor {
    void before(InvocationContext context);
    void after(InvocationContext context, Object result);
    void onError(InvocationContext context, Exception exception);
}

// Invocation context
class InvocationContext {
    private final Object target;
    private final String methodName;
    private final Object[] arguments;
    private final Map<String, Object> metadata = new HashMap<>();
    private long startTime;
    
    public InvocationContext(Object target, String methodName, Object[] arguments) {
        this.target = target;
        this.methodName = methodName;
        this.arguments = arguments;
        this.startTime = System.currentTimeMillis();
    }
    
    public Object getTarget() { return target; }
    public String getMethodName() { return methodName; }
    public Object[] getArguments() { return arguments; }
    public long getStartTime() { return startTime; }
    
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}

// Example 1: Logging Interceptor
class LoggingInterceptor implements Interceptor {
    @Override
    public void before(InvocationContext context) {
        System.out.println("[LOG] Before " + context.getMethodName() + 
            " with args: " + Arrays.toString(context.getArguments()));
    }
    
    @Override
    public void after(InvocationContext context, Object result) {
        System.out.println("[LOG] After " + context.getMethodName() + 
            " returned: " + result + " (took " + context.getElapsedTime() + "ms)");
    }
    
    @Override
    public void onError(InvocationContext context, Exception exception) {
        System.out.println("[LOG] Error in " + context.getMethodName() + 
            ": " + exception.getMessage());
    }
}

// Example 2: Authentication Interceptor
class AuthenticationInterceptor implements Interceptor {
    private final Set<String> authenticatedUsers = new HashSet<>();
    
    public void login(String username) {
        authenticatedUsers.add(username);
    }
    
    @Override
    public void before(InvocationContext context) {
        String username = (String) context.getMetadata("username");
        
        if (username == null || !authenticatedUsers.contains(username)) {
            throw new SecurityException("Authentication required for " + context.getMethodName());
        }
        
        System.out.println("[AUTH] User '" + username + "' authenticated");
    }
    
    @Override
    public void after(InvocationContext context, Object result) {
        // No action needed
    }
    
    @Override
    public void onError(InvocationContext context, Exception exception) {
        System.out.println("[AUTH] Security exception: " + exception.getMessage());
    }
}

// Example 3: Performance Monitoring Interceptor
class PerformanceInterceptor implements Interceptor {
    private final Map<String, List<Long>> performanceData = new HashMap<>();
    
    @Override
    public void before(InvocationContext context) {
        context.setMetadata("start", System.nanoTime());
    }
    
    @Override
    public void after(InvocationContext context, Object result) {
        long duration = System.nanoTime() - (Long) context.getMetadata("start");
        
        performanceData.computeIfAbsent(context.getMethodName(), k -> new ArrayList<>())
            .add(duration / 1_000_000); // Convert to milliseconds
        
        System.out.println("[PERF] " + context.getMethodName() + " took " + 
            (duration / 1_000_000) + "ms");
    }
    
    @Override
    public void onError(InvocationContext context, Exception exception) {
        // Still record performance even on error
        after(context, null);
    }
    
    public void printStatistics() {
        System.out.println("\n📊 Performance Statistics:");
        performanceData.forEach((method, times) -> {
            double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
            long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
            
            System.out.printf("  %s: avg=%.2fms, min=%dms, max=%dms, calls=%d%n",
                method, avg, min, max, times.size());
        });
    }
}

// Example 4: Validation Interceptor
class ValidationInterceptor implements Interceptor {
    @Override
    public void before(InvocationContext context) {
        Object[] args = context.getArguments();
        
        // Validate arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                throw new IllegalArgumentException(
                    "Argument " + i + " cannot be null in " + context.getMethodName());
            }
            
            if (args[i] instanceof String && ((String) args[i]).isEmpty()) {
                throw new IllegalArgumentException(
                    "Argument " + i + " cannot be empty in " + context.getMethodName());
            }
            
            if (args[i] instanceof Number && ((Number) args[i]).doubleValue() < 0) {
                throw new IllegalArgumentException(
                    "Argument " + i + " cannot be negative in " + context.getMethodName());
            }
        }
        
        System.out.println("[VALIDATION] Arguments validated for " + context.getMethodName());
    }
    
    @Override
    public void after(InvocationContext context, Object result) {
        // No action needed
    }
    
    @Override
    public void onError(InvocationContext context, Exception exception) {
        System.out.println("[VALIDATION] Validation failed: " + exception.getMessage());
    }
}

// Example 5: Caching Interceptor
class CachingInterceptor implements Interceptor {
    private final Map<String, Object> cache = new HashMap<>();
    
    @Override
    public void before(InvocationContext context) {
        String cacheKey = context.getMethodName() + Arrays.toString(context.getArguments());
        
        if (cache.containsKey(cacheKey)) {
            System.out.println("[CACHE] Cache hit for " + context.getMethodName());
            context.setMetadata("cached", true);
            context.setMetadata("cachedResult", cache.get(cacheKey));
        } else {
            System.out.println("[CACHE] Cache miss for " + context.getMethodName());
            context.setMetadata("cached", false);
        }
        
        context.setMetadata("cacheKey", cacheKey);
    }
    
    @Override
    public void after(InvocationContext context, Object result) {
        if (Boolean.FALSE.equals(context.getMetadata("cached"))) {
            String cacheKey = (String) context.getMetadata("cacheKey");
            cache.put(cacheKey, result);
            System.out.println("[CACHE] Cached result for " + context.getMethodName());
        }
    }
    
    @Override
    public void onError(InvocationContext context, Exception exception) {
        // Don't cache errors
    }
}

// Interceptor Chain
class InterceptorChain {
    private final List<Interceptor> interceptors = new ArrayList<>();
    
    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }
    
    public Object execute(Object target, String methodName, Object[] arguments, 
                         Consumer<Void> actualMethod) {
        InvocationContext context = new InvocationContext(target, methodName, arguments);
        Object result = null;
        
        try {
            // Before interceptors
            for (Interceptor interceptor : interceptors) {
                interceptor.before(context);
            }
            
            // Check if result is cached
            if (context.getMetadata("cached") == Boolean.TRUE) {
                result = context.getMetadata("cachedResult");
            } else {
                // Execute actual method
                actualMethod.accept(null);
                result = context.getMetadata("result");
            }
            
            // After interceptors
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                interceptors.get(i).after(context, result);
            }
            
        } catch (Exception e) {
            // Error interceptors
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                interceptors.get(i).onError(context, e);
            }
            throw new RuntimeException(e);
        }
        
        return result;
    }
}

// Service with interceptors
class PaymentService {
    private final InterceptorChain interceptorChain;
    
    public PaymentService(InterceptorChain interceptorChain) {
        this.interceptorChain = interceptorChain;
    }
    
    public String processPayment(String userId, double amount) {
        return (String) interceptorChain.execute(this, "processPayment", 
            new Object[]{userId, amount}, 
            v -> {
                try {
                    // Simulate processing time
                    Thread.sleep((long) (Math.random() * 100));
                    
                    InvocationContext ctx = new InvocationContext(this, "processPayment", 
                        new Object[]{userId, amount});
                    String result = "Payment of $" + amount + " processed for user " + userId;
                    ctx.setMetadata("result", result);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
    }
    
    public String refundPayment(String userId, double amount) {
        return (String) interceptorChain.execute(this, "refundPayment", 
            new Object[]{userId, amount}, 
            v -> {
                try {
                    Thread.sleep((long) (Math.random() * 100));
                    
                    InvocationContext ctx = new InvocationContext(this, "refundPayment", 
                        new Object[]{userId, amount});
                    String result = "Refund of $" + amount + " processed for user " + userId;
                    ctx.setMetadata("result", result);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
    }
}

// Demo
public class InterceptorPattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    INTERCEPTOR PATTERN DEMONSTRATION     ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Setup interceptor chain
        InterceptorChain chain = new InterceptorChain();
        
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
        AuthenticationInterceptor authInterceptor = new AuthenticationInterceptor();
        PerformanceInterceptor perfInterceptor = new PerformanceInterceptor();
        ValidationInterceptor validationInterceptor = new ValidationInterceptor();
        CachingInterceptor cachingInterceptor = new CachingInterceptor();
        
        chain.addInterceptor(loggingInterceptor);
        chain.addInterceptor(authInterceptor);
        chain.addInterceptor(validationInterceptor);
        chain.addInterceptor(cachingInterceptor);
        chain.addInterceptor(perfInterceptor);
        
        // Authenticate user
        authInterceptor.login("alice");
        
        // Create service
        PaymentService paymentService = new PaymentService(chain);
        
        // Example 1: Successful operation
        System.out.println("1. SUCCESSFUL PAYMENT WITH INTERCEPTORS");
        System.out.println("────────────────────────────────────────");
        
        InvocationContext ctx1 = new InvocationContext(paymentService, "processPayment", 
            new Object[]{"alice", 100.0});
        ctx1.setMetadata("username", "alice");
        
        try {
            paymentService.processPayment("alice", 100.0);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Example 2: Cached call
        System.out.println("\n2. CACHED PAYMENT (Same parameters)");
        System.out.println("────────────────────────────────────");
        
        try {
            paymentService.processPayment("alice", 100.0);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Example 3: Validation failure
        System.out.println("\n3. VALIDATION FAILURE (Negative amount)");
        System.out.println("────────────────────────────────────────");
        
        try {
            paymentService.processPayment("alice", -50.0);
        } catch (Exception e) {
            System.out.println("Error caught: " + e.getMessage());
        }
        
        // Example 4: Authentication failure
        System.out.println("\n4. AUTHENTICATION FAILURE");
        System.out.println("─────────────────────────");
        
        InvocationContext ctx4 = new InvocationContext(paymentService, "processPayment", 
            new Object[]{"bob", 200.0});
        ctx4.setMetadata("username", "bob"); // bob is not authenticated
        
        try {
            paymentService.processPayment("bob", 200.0);
        } catch (Exception e) {
            System.out.println("Error caught: " + e.getMessage());
        }
        
        // Example 5: Multiple operations for performance stats
        System.out.println("\n5. MULTIPLE OPERATIONS FOR PERFORMANCE");
        System.out.println("───────────────────────────────────────");
        
        for (int i = 1; i <= 3; i++) {
            InvocationContext ctx = new InvocationContext(paymentService, "refundPayment", 
                new Object[]{"alice", i * 50.0});
            ctx.setMetadata("username", "alice");
            
            try {
                paymentService.refundPayment("alice", i * 50.0);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        // Print performance statistics
        perfInterceptor.printStatistics();
        
        System.out.println("\n✅ Interceptor Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Logging: Automatic request/response logging");
        System.out.println("  • Authentication: Transparent security checks");
        System.out.println("  • Validation: Automatic argument validation");
        System.out.println("  • Caching: Transparent result caching");
        System.out.println("  • Performance: Automatic performance monitoring");
    }
}
