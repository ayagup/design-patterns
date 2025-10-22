package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * API Composition Pattern
 * ========================
 * 
 * Intent:
 * Implements queries that need data from multiple services by having
 * an API composer invoke multiple services in parallel and combine results.
 * 
 * Also Known As:
 * - Service Aggregation
 * - Backend Aggregation
 * 
 * Motivation:
 * - Each microservice has its own database (Database per Service pattern)
 * - Cannot use JOIN queries across services
 * - Need to retrieve data from multiple services for single API response
 * - Want to minimize latency by calling services in parallel
 * 
 * Applicability:
 * - Implementing queries in microservices architecture
 * - Need to aggregate data from multiple services
 * - Services expose APIs that return needed data
 * - Query result set is not too large
 * 
 * Structure:
 * Client -> API Composer -> [Service A, Service B, Service C]
 * API Composer calls services in parallel and aggregates results
 * 
 * Participants:
 * - API Composer: Orchestrates calls to multiple services
 * - Provider Services: Individual microservices with separate databases
 * - Client: Consumer of aggregated data
 * 
 * Benefits:
 * + Simple and straightforward
 * + Doesn't require new infrastructure
 * + Works well for small data sets
 * 
 * Drawbacks:
 * - Increased latency (multiple service calls)
 * - Reduced availability (fails if any service fails)
 * - Inefficient for large data sets
 * - Cannot handle complex joins effectively
 */

// ============================================================================
// DOMAIN MODELS
// ============================================================================

class UserInfo {
    private final String userId;
    private final String name;
    private final String email;
    
    public UserInfo(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
    
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return String.format("UserInfo{userId='%s', name='%s', email='%s'}", userId, name, email);
    }
}

class OrderInfo {
    private final String orderId;
    private final String userId;
    private final String productId;
    private final int quantity;
    private final double totalAmount;
    
    public OrderInfo(String orderId, String userId, String productId, int quantity, double totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    
    @Override
    public String toString() {
        return String.format("OrderInfo{orderId='%s', userId='%s', productId='%s', quantity=%d, total=$%.2f}",
                           orderId, userId, productId, quantity, totalAmount);
    }
}

class ProductInfo {
    private final String productId;
    private final String name;
    private final double price;
    private final String category;
    
    public ProductInfo(String productId, String name, double price, String category) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.category = category;
    }
    
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    
    @Override
    public String toString() {
        return String.format("ProductInfo{productId='%s', name='%s', price=$%.2f, category='%s'}",
                           productId, name, price, category);
    }
}

class ReviewInfo {
    private final String reviewId;
    private final String productId;
    private final String userId;
    private final int rating;
    private final String comment;
    
    public ReviewInfo(String reviewId, String productId, String userId, int rating, String comment) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }
    
    public String getReviewId() { return reviewId; }
    public String getProductId() { return productId; }
    public String getUserId() { return userId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    
    @Override
    public String toString() {
        return String.format("ReviewInfo{reviewId='%s', productId='%s', rating=%d, comment='%s'}",
                           reviewId, productId, rating, comment);
    }
}

// ============================================================================
// COMPOSED MODELS (Result of API Composition)
// ============================================================================

class OrderDetails {
    private final OrderInfo order;
    private final UserInfo user;
    private final ProductInfo product;
    
    public OrderDetails(OrderInfo order, UserInfo user, ProductInfo product) {
        this.order = order;
        this.user = user;
        this.product = product;
    }
    
    @Override
    public String toString() {
        return String.format("OrderDetails{\n  order=%s,\n  user=%s,\n  product=%s\n}",
                           order, user, product);
    }
}

class ProductWithReviews {
    private final ProductInfo product;
    private final List<ReviewInfo> reviews;
    private final double averageRating;
    
    public ProductWithReviews(ProductInfo product, List<ReviewInfo> reviews) {
        this.product = product;
        this.reviews = reviews;
        this.averageRating = calculateAverageRating(reviews);
    }
    
    private double calculateAverageRating(List<ReviewInfo> reviews) {
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(ReviewInfo::getRating).average().orElse(0.0);
    }
    
    @Override
    public String toString() {
        return String.format("ProductWithReviews{\n  product=%s,\n  reviews=%d,\n  avgRating=%.1f\n}",
                           product, reviews.size(), averageRating);
    }
}

class UserProfile {
    private final UserInfo user;
    private final List<OrderInfo> orders;
    private final List<ReviewInfo> reviews;
    
    public UserProfile(UserInfo user, List<OrderInfo> orders, List<ReviewInfo> reviews) {
        this.user = user;
        this.orders = orders;
        this.reviews = reviews;
    }
    
    @Override
    public String toString() {
        return String.format("UserProfile{\n  user=%s,\n  orders=%d,\n  reviews=%d\n}",
                           user, orders.size(), reviews.size());
    }
}

// ============================================================================
// INDIVIDUAL MICROSERVICES (each with own database)
// ============================================================================

class UserService {
    private final Map<String, UserInfo> users = new ConcurrentHashMap<>();
    
    public UserService() {
        users.put("U1", new UserInfo("U1", "Alice", "alice@example.com"));
        users.put("U2", new UserInfo("U2", "Bob", "bob@example.com"));
        users.put("U3", new UserInfo("U3", "Charlie", "charlie@example.com"));
    }
    
    public CompletableFuture<UserInfo> getUser(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[UserService] Fetching user: " + userId);
            simulateNetworkDelay(100);
            return users.get(userId);
        });
    }
    
    private void simulateNetworkDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class OrderServiceForComposition {
    private final Map<String, OrderInfo> orders = new ConcurrentHashMap<>();
    
    public OrderServiceForComposition() {
        orders.put("O1", new OrderInfo("O1", "U1", "P1", 2, 199.98));
        orders.put("O2", new OrderInfo("O2", "U1", "P2", 1, 49.99));
        orders.put("O3", new OrderInfo("O3", "U2", "P3", 3, 149.97));
    }
    
    public CompletableFuture<OrderInfo> getOrder(String orderId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[OrderService] Fetching order: " + orderId);
            simulateNetworkDelay(150);
            return orders.get(orderId);
        });
    }
    
    public CompletableFuture<List<OrderInfo>> getOrdersByUser(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[OrderService] Fetching orders for user: " + userId);
            simulateNetworkDelay(150);
            List<OrderInfo> result = new ArrayList<>();
            for (OrderInfo order : orders.values()) {
                if (order.getUserId().equals(userId)) {
                    result.add(order);
                }
            }
            return result;
        });
    }
    
    private void simulateNetworkDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ProductServiceForComposition {
    private final Map<String, ProductInfo> products = new ConcurrentHashMap<>();
    
    public ProductServiceForComposition() {
        products.put("P1", new ProductInfo("P1", "Laptop", 999.99, "Electronics"));
        products.put("P2", new ProductInfo("P2", "Mouse", 49.99, "Electronics"));
        products.put("P3", new ProductInfo("P3", "Desk", 299.99, "Furniture"));
    }
    
    public CompletableFuture<ProductInfo> getProduct(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[ProductService] Fetching product: " + productId);
            simulateNetworkDelay(120);
            return products.get(productId);
        });
    }
    
    private void simulateNetworkDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ReviewService {
    private final List<ReviewInfo> reviews = new ArrayList<>();
    
    public ReviewService() {
        reviews.add(new ReviewInfo("R1", "P1", "U2", 5, "Excellent laptop!"));
        reviews.add(new ReviewInfo("R2", "P1", "U3", 4, "Good value for money"));
        reviews.add(new ReviewInfo("R3", "P2", "U1", 5, "Perfect mouse"));
    }
    
    public CompletableFuture<List<ReviewInfo>> getReviewsForProduct(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[ReviewService] Fetching reviews for product: " + productId);
            simulateNetworkDelay(100);
            List<ReviewInfo> result = new ArrayList<>();
            for (ReviewInfo review : reviews) {
                if (review.getProductId().equals(productId)) {
                    result.add(review);
                }
            }
            return result;
        });
    }
    
    public CompletableFuture<List<ReviewInfo>> getReviewsByUser(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("[ReviewService] Fetching reviews by user: " + userId);
            simulateNetworkDelay(100);
            List<ReviewInfo> result = new ArrayList<>();
            for (ReviewInfo review : reviews) {
                if (review.getUserId().equals(userId)) {
                    result.add(review);
                }
            }
            return result;
        });
    }
    
    private void simulateNetworkDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// ============================================================================
// API COMPOSER (orchestrates parallel calls and aggregates results)
// ============================================================================

class APIComposer {
    private final UserService userService;
    private final OrderServiceForComposition orderService;
    private final ProductServiceForComposition productService;
    private final ReviewService reviewService;
    
    public APIComposer(UserService userService, OrderServiceForComposition orderService,
                      ProductServiceForComposition productService, ReviewService reviewService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        this.reviewService = reviewService;
    }
    
    // Compose order details from 3 services
    public CompletableFuture<OrderDetails> getOrderDetails(String orderId) {
        System.out.println("[APIComposer] Composing order details for: " + orderId);
        long startTime = System.currentTimeMillis();
        
        return orderService.getOrder(orderId)
            .thenCompose(order -> {
                if (order == null) {
                    return CompletableFuture.completedFuture(null);
                }
                
                // Call user and product services in parallel
                CompletableFuture<UserInfo> userFuture = userService.getUser(order.getUserId());
                CompletableFuture<ProductInfo> productFuture = productService.getProduct(order.getProductId());
                
                return CompletableFuture.allOf(userFuture, productFuture)
                    .thenApply(v -> {
                        long endTime = System.currentTimeMillis();
                        System.out.println("[APIComposer] Composed order details in " + (endTime - startTime) + "ms");
                        return new OrderDetails(order, userFuture.join(), productFuture.join());
                    });
            });
    }
    
    // Compose product with reviews from 2 services
    public CompletableFuture<ProductWithReviews> getProductWithReviews(String productId) {
        System.out.println("[APIComposer] Composing product with reviews for: " + productId);
        long startTime = System.currentTimeMillis();
        
        // Call product and review services in parallel
        CompletableFuture<ProductInfo> productFuture = productService.getProduct(productId);
        CompletableFuture<List<ReviewInfo>> reviewsFuture = reviewService.getReviewsForProduct(productId);
        
        return CompletableFuture.allOf(productFuture, reviewsFuture)
            .thenApply(v -> {
                long endTime = System.currentTimeMillis();
                System.out.println("[APIComposer] Composed product with reviews in " + (endTime - startTime) + "ms");
                return new ProductWithReviews(productFuture.join(), reviewsFuture.join());
            });
    }
    
    // Compose user profile from 3 services
    public CompletableFuture<UserProfile> getUserProfile(String userId) {
        System.out.println("[APIComposer] Composing user profile for: " + userId);
        long startTime = System.currentTimeMillis();
        
        // Call all 3 services in parallel
        CompletableFuture<UserInfo> userFuture = userService.getUser(userId);
        CompletableFuture<List<OrderInfo>> ordersFuture = orderService.getOrdersByUser(userId);
        CompletableFuture<List<ReviewInfo>> reviewsFuture = reviewService.getReviewsByUser(userId);
        
        return CompletableFuture.allOf(userFuture, ordersFuture, reviewsFuture)
            .thenApply(v -> {
                long endTime = System.currentTimeMillis();
                System.out.println("[APIComposer] Composed user profile in " + (endTime - startTime) + "ms");
                return new UserProfile(userFuture.join(), ordersFuture.join(), reviewsFuture.join());
            });
    }
}

/**
 * Demonstration of API Composition Pattern
 */
public class APICompositionPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("=== API Composition Pattern ===\n");
        
        // Individual microservices (each with own database)
        UserService userService = new UserService();
        OrderServiceForComposition orderService = new OrderServiceForComposition();
        ProductServiceForComposition productService = new ProductServiceForComposition();
        ReviewService reviewService = new ReviewService();
        
        // API Composer
        APIComposer composer = new APIComposer(userService, orderService, productService, reviewService);
        
        System.out.println("--- Composing Order Details (3 services) ---\n");
        OrderDetails orderDetails = composer.getOrderDetails("O1").get();
        System.out.println("\nResult:\n" + orderDetails);
        
        System.out.println("\n--- Composing Product with Reviews (2 services) ---\n");
        ProductWithReviews productWithReviews = composer.getProductWithReviews("P1").get();
        System.out.println("\nResult:\n" + productWithReviews);
        
        System.out.println("\n--- Composing User Profile (3 services) ---\n");
        UserProfile userProfile = composer.getUserProfile("U1").get();
        System.out.println("\nResult:\n" + userProfile);
        
        System.out.println("\n=== Key Points ===");
        System.out.println("1. Parallel calls - services invoked concurrently for speed");
        System.out.println("2. Data aggregation - combines results from multiple sources");
        System.out.println("3. Reduced latency - parallelization minimizes total time");
        System.out.println("4. Simple approach - no complex infrastructure needed");
        
        System.out.println("\n=== When to Use ===");
        System.out.println("- Database per Service architecture");
        System.out.println("- Need to aggregate data from multiple services");
        System.out.println("- Result set is reasonably small");
        System.out.println("- Services provide needed data via APIs");
    }
}
