package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;

/**
 * Gateway Aggregation Pattern
 * 
 * Intent: Use a gateway to aggregate multiple individual requests into a single 
 * request, reducing chattiness between client and backend services.
 * 
 * Also Known As: API Gateway, Aggregator Gateway
 * 
 * Motivation:
 * In microservices architecture, a single user operation often requires data from
 * multiple services. Making multiple round-trips from client increases latency and
 * complexity. Gateway Aggregation aggregates these calls on the backend side.
 * 
 * Applicability:
 * - Client needs data from multiple backend services
 * - Want to reduce number of client-server round trips
 * - Mobile or low-bandwidth clients need optimized API
 * - Need to combine data from multiple microservices
 * - Want to hide backend complexity from clients
 * 
 * Benefits:
 * - Reduced network latency (fewer round trips)
 * - Simplified client code
 * - Better performance for mobile/remote clients
 * - Centralized aggregation logic
 * - Can parallelize backend calls
 * - Reduces client-server chattiness
 * 
 * Implementation Considerations:
 * - Gateway becomes single point of failure
 * - Increased gateway complexity
 * - Need proper timeout handling
 * - Error handling for partial failures
 * - Caching strategy at gateway level
 */

// Domain models
class UserProfile {
    private String userId;
    private String name;
    private String email;
    private String avatar;
    
    public UserProfile(String userId, String name, String email, String avatar) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
    }
    
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatar() { return avatar; }
    
    @Override
    public String toString() {
        return String.format("UserProfile[id=%s, name=%s]", userId, name);
    }
}

class OrderSummary {
    private String orderId;
    private String userId;
    private double total;
    private String status;
    private LocalDateTime orderDate;
    
    public OrderSummary(String orderId, String userId, double total, String status, LocalDateTime orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.total = total;
        this.status = status;
        this.orderDate = orderDate;
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    
    @Override
    public String toString() {
        return String.format("Order[id=%s, total=%.2f, status=%s]", orderId, total, status);
    }
}

class PaymentInfo {
    private String paymentId;
    private String userId;
    private String method;
    private String last4Digits;
    
    public PaymentInfo(String paymentId, String userId, String method, String last4Digits) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.method = method;
        this.last4Digits = last4Digits;
    }
    
    public String getPaymentId() { return paymentId; }
    public String getUserId() { return userId; }
    public String getMethod() { return method; }
    public String getLast4Digits() { return last4Digits; }
    
    @Override
    public String toString() {
        return String.format("Payment[method=%s, ending in %s]", method, last4Digits);
    }
}

class ShippingAddress {
    private String addressId;
    private String userId;
    private String street;
    private String city;
    private String zipCode;
    
    public ShippingAddress(String addressId, String userId, String street, String city, String zipCode) {
        this.addressId = addressId;
        this.userId = userId;
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
    }
    
    public String getAddressId() { return addressId; }
    public String getUserId() { return userId; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getZipCode() { return zipCode; }
    
    @Override
    public String toString() {
        return String.format("Address[%s, %s, %s]", street, city, zipCode);
    }
}

class Recommendation {
    private String productId;
    private String productName;
    private double score;
    
    public Recommendation(String productId, String productName, double score) {
        this.productId = productId;
        this.productName = productName;
        this.score = score;
    }
    
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getScore() { return score; }
    
    @Override
    public String toString() {
        return String.format("Recommendation[%s, score=%.2f]", productName, score);
    }
}

// Backend microservices (simulated)
class UserService {
    public UserProfile getUserProfile(String userId) {
        simulateLatency(100);
        System.out.println("[UserService] Fetching profile for: " + userId);
        return new UserProfile(userId, "John Doe", "john@example.com", "avatar.jpg");
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class OrderService {
    public List<OrderSummary> getUserOrders(String userId) {
        simulateLatency(150);
        System.out.println("[OrderService] Fetching orders for: " + userId);
        return Arrays.asList(
            new OrderSummary("O1", userId, 299.99, "Delivered", LocalDateTime.now().minusDays(5)),
            new OrderSummary("O2", userId, 149.99, "Shipped", LocalDateTime.now().minusDays(2))
        );
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class PaymentService {
    public List<PaymentInfo> getUserPaymentMethods(String userId) {
        simulateLatency(120);
        System.out.println("[PaymentService] Fetching payment methods for: " + userId);
        return Arrays.asList(
            new PaymentInfo("P1", userId, "Visa", "4242"),
            new PaymentInfo("P2", userId, "MasterCard", "5555")
        );
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ShippingService {
    public List<ShippingAddress> getUserAddresses(String userId) {
        simulateLatency(100);
        System.out.println("[ShippingService] Fetching addresses for: " + userId);
        return Arrays.asList(
            new ShippingAddress("A1", userId, "123 Main St", "New York", "10001"),
            new ShippingAddress("A2", userId, "456 Oak Ave", "Boston", "02101")
        );
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class RecommendationService {
    public List<Recommendation> getUserRecommendations(String userId) {
        simulateLatency(200);
        System.out.println("[RecommendationService] Fetching recommendations for: " + userId);
        return Arrays.asList(
            new Recommendation("P1", "Laptop Pro", 0.95),
            new Recommendation("P2", "Wireless Mouse", 0.87),
            new Recommendation("P3", "USB-C Hub", 0.82)
        );
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// Example 1: Simple API Gateway with Sequential Aggregation
class SimpleAggregationGateway {
    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    
    public SimpleAggregationGateway(UserService userService, OrderService orderService, 
                                    PaymentService paymentService) {
        this.userService = userService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }
    
    public Map<String, Object> getUserDashboard(String userId) {
        System.out.println("\n[SimpleGateway] Aggregating dashboard for: " + userId);
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> dashboard = new HashMap<>();
        
        // Sequential calls (slower)
        UserProfile profile = userService.getUserProfile(userId);
        List<OrderSummary> orders = orderService.getUserOrders(userId);
        List<PaymentInfo> payments = paymentService.getUserPaymentMethods(userId);
        
        dashboard.put("profile", profile);
        dashboard.put("orders", orders);
        dashboard.put("paymentMethods", payments);
        dashboard.put("orderCount", orders.size());
        dashboard.put("hasPaymentMethod", !payments.isEmpty());
        
        long duration = System.currentTimeMillis() - startTime;
        dashboard.put("fetchTime", duration + "ms");
        
        System.out.printf("[SimpleGateway] Dashboard aggregated in %dms%n", duration);
        return dashboard;
    }
}

// Example 2: Parallel Aggregation Gateway (Optimized)
class ParallelAggregationGateway {
    private final UserService userService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final ExecutorService executor;
    
    public ParallelAggregationGateway(UserService userService, OrderService orderService,
                                      PaymentService paymentService, ShippingService shippingService) {
        this.userService = userService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    public Map<String, Object> getUserDashboard(String userId) {
        System.out.println("\n[ParallelGateway] Aggregating dashboard for: " + userId);
        long startTime = System.currentTimeMillis();
        
        // Parallel calls using CompletableFuture
        CompletableFuture<UserProfile> profileFuture = CompletableFuture.supplyAsync(
            () -> userService.getUserProfile(userId), executor);
        
        CompletableFuture<List<OrderSummary>> ordersFuture = CompletableFuture.supplyAsync(
            () -> orderService.getUserOrders(userId), executor);
        
        CompletableFuture<List<PaymentInfo>> paymentsFuture = CompletableFuture.supplyAsync(
            () -> paymentService.getUserPaymentMethods(userId), executor);
        
        CompletableFuture<List<ShippingAddress>> addressesFuture = CompletableFuture.supplyAsync(
            () -> shippingService.getUserAddresses(userId), executor);
        
        // Wait for all to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            profileFuture, ordersFuture, paymentsFuture, addressesFuture);
        
        try {
            allFutures.get(5, TimeUnit.SECONDS);
            
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("profile", profileFuture.get());
            dashboard.put("orders", ordersFuture.get());
            dashboard.put("paymentMethods", paymentsFuture.get());
            dashboard.put("addresses", addressesFuture.get());
            
            // Derived data
            dashboard.put("orderCount", ordersFuture.get().size());
            dashboard.put("hasPaymentMethod", !paymentsFuture.get().isEmpty());
            dashboard.put("hasShippingAddress", !addressesFuture.get().isEmpty());
            
            long duration = System.currentTimeMillis() - startTime;
            dashboard.put("fetchTime", duration + "ms");
            
            System.out.printf("[ParallelGateway] Dashboard aggregated in %dms%n", duration);
            return dashboard;
            
        } catch (TimeoutException e) {
            System.err.println("[ParallelGateway] Timeout aggregating dashboard");
            return Collections.singletonMap("error", "Timeout");
        } catch (Exception e) {
            System.err.println("[ParallelGateway] Error: " + e.getMessage());
            return Collections.singletonMap("error", e.getMessage());
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Example 3: Gateway with Partial Failure Handling
class ResilientAggregationGateway {
    private final UserService userService;
    private final OrderService orderService;
    private final RecommendationService recommendationService;
    private final ExecutorService executor;
    
    public ResilientAggregationGateway(UserService userService, OrderService orderService,
                                       RecommendationService recommendationService) {
        this.userService = userService;
        this.orderService = orderService;
        this.recommendationService = recommendationService;
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    public Map<String, Object> getHomePage(String userId) {
        System.out.println("\n[ResilientGateway] Aggregating home page for: " + userId);
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        List<String> warnings = new CopyOnWriteArrayList<>();
        
        // Critical data - must succeed
        CompletableFuture<UserProfile> profileFuture = CompletableFuture.supplyAsync(
            () -> userService.getUserProfile(userId), executor)
            .orTimeout(2, TimeUnit.SECONDS);
        
        // Important but non-critical - handle failure gracefully
        CompletableFuture<List<OrderSummary>> ordersFuture = CompletableFuture.supplyAsync(
            () -> orderService.getUserOrders(userId), executor)
            .orTimeout(2, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                warnings.add("Orders unavailable");
                return Collections.emptyList();
            });
        
        // Nice-to-have - can fail silently
        CompletableFuture<List<Recommendation>> recommendationsFuture = CompletableFuture.supplyAsync(
            () -> recommendationService.getUserRecommendations(userId), executor)
            .orTimeout(2, TimeUnit.SECONDS)
            .exceptionally(ex -> {
                warnings.add("Recommendations unavailable");
                return Collections.emptyList();
            });
        
        try {
            // Wait for critical data
            UserProfile profile = profileFuture.get();
            response.put("profile", profile);
            
            // Get non-critical data (won't throw on failure due to exceptionally)
            response.put("recentOrders", ordersFuture.get());
            response.put("recommendations", recommendationsFuture.get());
            
            if (!warnings.isEmpty()) {
                response.put("warnings", warnings);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            response.put("fetchTime", duration + "ms");
            
            System.out.printf("[ResilientGateway] Home page aggregated in %dms%n", duration);
            return response;
            
        } catch (Exception e) {
            System.err.println("[ResilientGateway] Critical error: " + e.getMessage());
            return Collections.singletonMap("error", "Unable to load page");
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Example 4: Cached Aggregation Gateway
class CachedAggregationGateway {
    private final UserService userService;
    private final OrderService orderService;
    private final Map<String, CacheEntry> cache;
    private final ExecutorService executor;
    private final long cacheTTL = 60000; // 60 seconds
    
    public CachedAggregationGateway(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
        this.cache = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(5);
    }
    
    public Map<String, Object> getUserSummary(String userId) {
        System.out.println("\n[CachedGateway] Getting user summary for: " + userId);
        
        CacheEntry cached = cache.get(userId);
        if (cached != null && !cached.isExpired()) {
            System.out.println("[CachedGateway] Cache hit!");
            return cached.getData();
        }
        
        System.out.println("[CachedGateway] Cache miss, aggregating data...");
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<UserProfile> profileFuture = CompletableFuture.supplyAsync(
            () -> userService.getUserProfile(userId), executor);
        
        CompletableFuture<List<OrderSummary>> ordersFuture = CompletableFuture.supplyAsync(
            () -> orderService.getUserOrders(userId), executor);
        
        try {
            Map<String, Object> summary = new HashMap<>();
            summary.put("profile", profileFuture.get());
            summary.put("orders", ordersFuture.get());
            
            long duration = System.currentTimeMillis() - startTime;
            summary.put("fetchTime", duration + "ms");
            
            // Cache the result
            cache.put(userId, new CacheEntry(summary, System.currentTimeMillis() + cacheTTL));
            System.out.printf("[CachedGateway] Summary cached for %dms%n", cacheTTL);
            
            return summary;
            
        } catch (Exception e) {
            return Collections.singletonMap("error", e.getMessage());
        }
    }
    
    public void invalidateCache(String userId) {
        cache.remove(userId);
        System.out.println("[CachedGateway] Cache invalidated for: " + userId);
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    private static class CacheEntry {
        private final Map<String, Object> data;
        private final long expiryTime;
        
        public CacheEntry(Map<String, Object> data, long expiryTime) {
            this.data = data;
            this.expiryTime = expiryTime;
        }
        
        public Map<String, Object> getData() {
            return data;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}

// Example 5: Smart Aggregation with Conditional Fetching
class SmartAggregationGateway {
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final ExecutorService executor;
    
    public SmartAggregationGateway(UserService userService, OrderService orderService,
                                   PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.executor = Executors.newFixedThreadPool(5);
    }
    
    public Map<String, Object> getCheckoutPage(String userId, boolean includeRecentOrders) {
        System.out.println("\n[SmartGateway] Aggregating checkout page for: " + userId);
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<?>> futures = new ArrayList<>();
        
        // Always fetch payment methods
        CompletableFuture<List<PaymentInfo>> paymentsFuture = CompletableFuture.supplyAsync(
            () -> paymentService.getUserPaymentMethods(userId), executor);
        futures.add(paymentsFuture);
        
        // Conditionally fetch recent orders
        CompletableFuture<List<OrderSummary>> ordersFuture = null;
        if (includeRecentOrders) {
            ordersFuture = CompletableFuture.supplyAsync(
                () -> orderService.getUserOrders(userId), executor);
            futures.add(ordersFuture);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        try {
            Map<String, Object> checkout = new HashMap<>();
            checkout.put("paymentMethods", paymentsFuture.get());
            
            if (includeRecentOrders && ordersFuture != null) {
                checkout.put("recentOrders", ordersFuture.get());
            }
            
            long duration = System.currentTimeMillis() - startTime;
            checkout.put("fetchTime", duration + "ms");
            
            System.out.printf("[SmartGateway] Checkout page aggregated in %dms%n", duration);
            return checkout;
            
        } catch (Exception e) {
            return Collections.singletonMap("error", e.getMessage());
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Demonstration
public class GatewayAggregationPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Gateway Aggregation Pattern Demo ===\n");
        
        // Initialize backend services
        UserService userService = new UserService();
        OrderService orderService = new OrderService();
        PaymentService paymentService = new PaymentService();
        ShippingService shippingService = new ShippingService();
        RecommendationService recommendationService = new RecommendationService();
        
        // Demo 1: Simple vs Parallel Aggregation
        System.out.println("--- Demo 1: Sequential vs Parallel Aggregation ---");
        demoSequentialVsParallel(userService, orderService, paymentService, shippingService);
        
        Thread.sleep(1000);
        
        // Demo 2: Resilient aggregation with partial failures
        System.out.println("\n--- Demo 2: Resilient Aggregation ---");
        demoResilientAggregation(userService, orderService, recommendationService);
        
        Thread.sleep(1000);
        
        // Demo 3: Cached aggregation
        System.out.println("\n--- Demo 3: Cached Aggregation ---");
        demoCachedAggregation(userService, orderService);
        
        Thread.sleep(1000);
        
        // Demo 4: Smart conditional aggregation
        System.out.println("\n--- Demo 4: Smart Conditional Aggregation ---");
        demoSmartAggregation(userService, orderService, paymentService);
    }
    
    private static void demoSequentialVsParallel(UserService userService, OrderService orderService,
                                                  PaymentService paymentService, ShippingService shippingService) {
        // Sequential
        SimpleAggregationGateway simpleGateway = new SimpleAggregationGateway(
            userService, orderService, paymentService);
        Map<String, Object> result1 = simpleGateway.getUserDashboard("USER123");
        System.out.println("Result: " + result1.get("fetchTime"));
        
        // Parallel
        ParallelAggregationGateway parallelGateway = new ParallelAggregationGateway(
            userService, orderService, paymentService, shippingService);
        Map<String, Object> result2 = parallelGateway.getUserDashboard("USER123");
        System.out.println("Result: " + result2.get("fetchTime"));
        
        parallelGateway.shutdown();
    }
    
    private static void demoResilientAggregation(UserService userService, OrderService orderService,
                                                   RecommendationService recommendationService) {
        ResilientAggregationGateway gateway = new ResilientAggregationGateway(
            userService, orderService, recommendationService);
        
        Map<String, Object> homePage = gateway.getHomePage("USER123");
        System.out.println("Home page keys: " + homePage.keySet());
        if (homePage.containsKey("warnings")) {
            System.out.println("Warnings: " + homePage.get("warnings"));
        }
        
        gateway.shutdown();
    }
    
    private static void demoCachedAggregation(UserService userService, OrderService orderService) {
        CachedAggregationGateway gateway = new CachedAggregationGateway(userService, orderService);
        
        // First call - cache miss
        gateway.getUserSummary("USER123");
        
        // Second call - cache hit
        gateway.getUserSummary("USER123");
        
        // Invalidate and fetch again
        gateway.invalidateCache("USER123");
        gateway.getUserSummary("USER123");
        
        gateway.shutdown();
    }
    
    private static void demoSmartAggregation(UserService userService, OrderService orderService,
                                              PaymentService paymentService) {
        SmartAggregationGateway gateway = new SmartAggregationGateway(
            userService, orderService, paymentService);
        
        // Without recent orders
        Map<String, Object> checkout1 = gateway.getCheckoutPage("USER123", false);
        System.out.println("Keys (without orders): " + checkout1.keySet());
        
        // With recent orders
        Map<String, Object> checkout2 = gateway.getCheckoutPage("USER123", true);
        System.out.println("Keys (with orders): " + checkout2.keySet());
        
        gateway.shutdown();
    }
}
