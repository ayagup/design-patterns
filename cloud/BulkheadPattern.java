package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BULKHEAD PATTERN
 * 
 * Isolates elements of an application into pools so that if one fails,
 * the others will continue to function. Named after ship compartments.
 * 
 * Benefits:
 * - Prevents cascading failures
 * - Isolates critical resources
 * - Improves system resilience
 * - Enables graceful degradation
 * - Protects against resource exhaustion
 * 
 * Use Cases:
 * - Microservices isolating different service calls
 * - Thread pool segregation for different operations
 * - Database connection pool isolation
 * - API rate limiting per client
 * - Resource partitioning in multi-tenant systems
 */

// Bulkhead interface
interface Bulkhead {
    <T> CompletableFuture<T> execute(Callable<T> task);
    boolean isAvailable();
    int getAvailableCapacity();
}

// Thread Pool Bulkhead
class ThreadPoolBulkhead implements Bulkhead {
    private final String name;
    private final ExecutorService executor;
    private final Semaphore semaphore;
    private final int maxConcurrent;
    private final AtomicInteger activeCount = new AtomicInteger(0);
    
    public ThreadPoolBulkhead(String name, int maxConcurrent, int queueCapacity) {
        this.name = name;
        this.maxConcurrent = maxConcurrent;
        this.semaphore = new Semaphore(maxConcurrent);
        this.executor = new ThreadPoolExecutor(
            maxConcurrent,
            maxConcurrent,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    @Override
    public <T> CompletableFuture<T> execute(Callable<T> task) {
        if (!semaphore.tryAcquire()) {
            System.out.println("  ⚠️  [" + name + "] Bulkhead full, rejecting request");
            return CompletableFuture.failedFuture(
                new RuntimeException("Bulkhead capacity exceeded")
            );
        }
        
        return CompletableFuture.supplyAsync(() -> {
            activeCount.incrementAndGet();
            System.out.println("  ✓ [" + name + "] Executing (active: " + activeCount.get() + "/" + maxConcurrent + ")");
            
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                activeCount.decrementAndGet();
                semaphore.release();
            }
        }, executor);
    }
    
    @Override
    public boolean isAvailable() {
        return semaphore.availablePermits() > 0;
    }
    
    @Override
    public int getAvailableCapacity() {
        return semaphore.availablePermits();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Example 1: Service Call Isolation
class PaymentService {
    private final Bulkhead bulkhead;
    
    public PaymentService(Bulkhead bulkhead) {
        this.bulkhead = bulkhead;
    }
    
    public CompletableFuture<String> processPayment(String orderId, double amount) {
        return bulkhead.execute(() -> {
            // Simulate payment processing
            Thread.sleep(1000);
            return "Payment processed: $" + amount + " for order " + orderId;
        });
    }
}

class InventoryService {
    private final Bulkhead bulkhead;
    
    public InventoryService(Bulkhead bulkhead) {
        this.bulkhead = bulkhead;
    }
    
    public CompletableFuture<String> checkInventory(String productId) {
        return bulkhead.execute(() -> {
            // Simulate inventory check
            Thread.sleep(500);
            return "Inventory checked for product: " + productId;
        });
    }
}

class ShippingService {
    private final Bulkhead bulkhead;
    
    public ShippingService(Bulkhead bulkhead) {
        this.bulkhead = bulkhead;
    }
    
    public CompletableFuture<String> scheduleShipping(String orderId) {
        return bulkhead.execute(() -> {
            // Simulate shipping schedule
            Thread.sleep(800);
            return "Shipping scheduled for order: " + orderId;
        });
    }
}

// Example 2: Database Connection Pool Bulkhead
class DatabaseConnectionPool {
    private final String name;
    private final Semaphore connections;
    private final int maxConnections;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    
    public DatabaseConnectionPool(String name, int maxConnections) {
        this.name = name;
        this.maxConnections = maxConnections;
        this.connections = new Semaphore(maxConnections);
    }
    
    public <T> T executeQuery(Callable<T> query) throws Exception {
        if (!connections.tryAcquire(2, TimeUnit.SECONDS)) {
            throw new RuntimeException(name + " connection pool exhausted");
        }
        
        try {
            int active = activeConnections.incrementAndGet();
            System.out.println("  [" + name + "] Query executing (connections: " + active + "/" + maxConnections + ")");
            return query.call();
        } finally {
            activeConnections.decrementAndGet();
            connections.release();
        }
    }
    
    public int getAvailableConnections() {
        return connections.availablePermits();
    }
}

// Example 3: API Rate Limiter Bulkhead
class APIRateLimiterBulkhead {
    private final Map<String, Semaphore> clientLimiters = new ConcurrentHashMap<>();
    private final int requestsPerClient;
    
    public APIRateLimiterBulkhead(int requestsPerClient) {
        this.requestsPerClient = requestsPerClient;
    }
    
    public boolean allowRequest(String clientId) {
        Semaphore limiter = clientLimiters.computeIfAbsent(
            clientId,
            k -> new Semaphore(requestsPerClient)
        );
        
        if (limiter.tryAcquire()) {
            System.out.println("  ✓ Request allowed for client: " + clientId + 
                " (remaining: " + limiter.availablePermits() + ")");
            
            // Release after some time (simulating request completion)
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    limiter.release();
                }
            });
            
            return true;
        } else {
            System.out.println("  ❌ Request denied for client: " + clientId + " (rate limit exceeded)");
            return false;
        }
    }
}

// Example 4: Multi-Tenant Resource Isolation
class TenantResourceManager {
    private final Map<String, ThreadPoolBulkhead> tenantBulkheads = new ConcurrentHashMap<>();
    private final int threadsPerTenant;
    private final int queuePerTenant;
    
    public TenantResourceManager(int threadsPerTenant, int queuePerTenant) {
        this.threadsPerTenant = threadsPerTenant;
        this.queuePerTenant = queuePerTenant;
    }
    
    public CompletableFuture<String> executeForTenant(String tenantId, Callable<String> task) {
        ThreadPoolBulkhead bulkhead = tenantBulkheads.computeIfAbsent(
            tenantId,
            k -> new ThreadPoolBulkhead("Tenant-" + k, threadsPerTenant, queuePerTenant)
        );
        
        return bulkhead.execute(task);
    }
    
    public void shutdown() {
        tenantBulkheads.values().forEach(ThreadPoolBulkhead::shutdown);
    }
}

// Bulkhead Registry
class BulkheadRegistry {
    private final Map<String, Bulkhead> bulkheads = new ConcurrentHashMap<>();
    
    public void register(String name, Bulkhead bulkhead) {
        bulkheads.put(name, bulkhead);
        System.out.println("Registered bulkhead: " + name);
    }
    
    public Bulkhead get(String name) {
        return bulkheads.get(name);
    }
    
    public void printStatus() {
        System.out.println("\n📊 Bulkhead Status:");
        bulkheads.forEach((name, bulkhead) -> {
            System.out.println("  " + name + ": " + 
                (bulkhead.isAvailable() ? "✓ Available" : "⚠️ Full") +
                " (capacity: " + bulkhead.getAvailableCapacity() + ")");
        });
    }
}

// Demo
public class BulkheadPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║      BULKHEAD PATTERN DEMONSTRATION      ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Example 1: Service Isolation
        System.out.println("1. SERVICE ISOLATION WITH BULKHEADS");
        System.out.println("───────────────────────────────────");
        
        BulkheadRegistry registry = new BulkheadRegistry();
        
        Bulkhead paymentBulkhead = new ThreadPoolBulkhead("Payment", 2, 5);
        Bulkhead inventoryBulkhead = new ThreadPoolBulkhead("Inventory", 3, 5);
        Bulkhead shippingBulkhead = new ThreadPoolBulkhead("Shipping", 2, 5);
        
        registry.register("Payment", paymentBulkhead);
        registry.register("Inventory", inventoryBulkhead);
        registry.register("Shipping", shippingBulkhead);
        
        PaymentService paymentService = new PaymentService(paymentBulkhead);
        InventoryService inventoryService = new InventoryService(inventoryBulkhead);
        ShippingService shippingService = new ShippingService(shippingBulkhead);
        
        System.out.println("\nSimulating concurrent requests:");
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        // Send multiple requests to each service
        for (int i = 1; i <= 5; i++) {
            futures.add(paymentService.processPayment("ORD-" + i, 100.0 * i));
            futures.add(inventoryService.checkInventory("PROD-" + i));
            futures.add(shippingService.scheduleShipping("ORD-" + i));
        }
        
        Thread.sleep(500);
        registry.printStatus();
        
        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        System.out.println("\n✅ All requests completed");
        
        // Example 2: Database Connection Pool Isolation
        System.out.println("\n2. DATABASE CONNECTION POOL BULKHEADS");
        System.out.println("─────────────────────────────────────");
        
        DatabaseConnectionPool readsPool = new DatabaseConnectionPool("Reads", 10);
        DatabaseConnectionPool writesPool = new DatabaseConnectionPool("Writes", 3);
        
        System.out.println("Executing read queries:");
        for (int i = 1; i <= 5; i++) {
            final int queryId = i;
            new Thread(() -> {
                try {
                    readsPool.executeQuery(() -> {
                        Thread.sleep(300);
                        return "Read query " + queryId + " completed";
                    });
                } catch (Exception e) {
                    System.out.println("  ❌ Read query " + queryId + " failed: " + e.getMessage());
                }
            }).start();
        }
        
        System.out.println("\nExecuting write queries:");
        for (int i = 1; i <= 5; i++) {
            final int queryId = i;
            new Thread(() -> {
                try {
                    writesPool.executeQuery(() -> {
                        Thread.sleep(500);
                        return "Write query " + queryId + " completed";
                    });
                } catch (Exception e) {
                    System.out.println("  ❌ Write query " + queryId + " failed: " + e.getMessage());
                }
            }).start();
        }
        
        Thread.sleep(2000);
        
        // Example 3: API Rate Limiting
        System.out.println("\n3. API RATE LIMITING BULKHEAD");
        System.out.println("──────────────────────────────");
        
        APIRateLimiterBulkhead rateLimiter = new APIRateLimiterBulkhead(3);
        
        System.out.println("Client A making requests:");
        for (int i = 1; i <= 5; i++) {
            rateLimiter.allowRequest("ClientA");
        }
        
        System.out.println("\nClient B making requests:");
        for (int i = 1; i <= 3; i++) {
            rateLimiter.allowRequest("ClientB");
        }
        
        // Example 4: Multi-Tenant Isolation
        System.out.println("\n4. MULTI-TENANT RESOURCE ISOLATION");
        System.out.println("───────────────────────────────────");
        
        TenantResourceManager tenantManager = new TenantResourceManager(2, 3);
        
        System.out.println("Tenant1 requests:");
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            tenantManager.executeForTenant("Tenant1", () -> {
                Thread.sleep(500);
                return "Tenant1 task " + taskId + " completed";
            }).thenAccept(result -> System.out.println("  " + result));
        }
        
        System.out.println("\nTenant2 requests:");
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            tenantManager.executeForTenant("Tenant2", () -> {
                Thread.sleep(500);
                return "Tenant2 task " + taskId + " completed";
            }).thenAccept(result -> System.out.println("  " + result));
        }
        
        Thread.sleep(2000);
        
        // Cleanup
        ((ThreadPoolBulkhead) paymentBulkhead).shutdown();
        ((ThreadPoolBulkhead) inventoryBulkhead).shutdown();
        ((ThreadPoolBulkhead) shippingBulkhead).shutdown();
        tenantManager.shutdown();
        
        System.out.println("\n✅ Bulkhead Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Service call isolation prevents cascading failures");
        System.out.println("  • Database connection pools protect resources");
        System.out.println("  • Rate limiting per client ensures fairness");
        System.out.println("  • Multi-tenant isolation prevents resource starvation");
    }
}
