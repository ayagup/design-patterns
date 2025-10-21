package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Backend for Frontend (BFF) Pattern
 * 
 * Intent: Create separate backend services tailored to the needs of specific 
 * frontend applications or user experiences, rather than having a single 
 * general-purpose API.
 * 
 * Also Known As: BFF, Experience API
 * 
 * Motivation:
 * Different client types (web, mobile, IoT) often have different requirements
 * in terms of data format, payload size, and API structure. A single backend
 * API trying to serve all clients becomes complex and inefficient. BFF creates
 * specialized backends for each frontend type.
 * 
 * Applicability:
 * - Multiple client types with different needs (web, mobile, desktop)
 * - Different user experiences requiring different data aggregations
 * - Need to optimize API calls for specific devices
 * - Frontend teams need autonomy to evolve their APIs
 * - Backend services are numerous and need aggregation
 * 
 * Benefits:
 * - Optimized APIs for each client type
 * - Frontend team autonomy
 * - Reduced over-fetching and under-fetching
 * - Simplified frontend code
 * - Better performance for specific devices
 * - Easier to evolve frontend and backend independently
 * 
 * Implementation Considerations:
 * - Code duplication between BFFs
 * - Potential for inconsistency
 * - Additional services to maintain
 * - Need for shared libraries/utilities
 * - Authentication and authorization per BFF
 */

// Domain models representing backend services
class User {
    private String userId;
    private String name;
    private String email;
    private String profileImage;
    private String bio;
    
    public User(String userId, String name, String email, String profileImage, String bio) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.bio = bio;
    }
    
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfileImage() { return profileImage; }
    public String getBio() { return bio; }
}

class Product {
    private String productId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private int stockQuantity;
    private List<String> tags;
    
    public Product(String productId, String name, String description, double price,
                   String imageUrl, String category, int stockQuantity, List<String> tags) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.tags = tags;
    }
    
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
    public int getStockQuantity() { return stockQuantity; }
    public List<String> getTags() { return tags; }
}

class Order {
    private String orderId;
    private String userId;
    private List<String> productIds;
    private double totalAmount;
    private String status;
    private LocalDateTime orderDate;
    
    public Order(String orderId, String userId, List<String> productIds, 
                 double totalAmount, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.productIds = productIds;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = LocalDateTime.now();
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public List<String> getProductIds() { return productIds; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public LocalDateTime getOrderDate() { return orderDate; }
}

class Review {
    private String reviewId;
    private String productId;
    private String userId;
    private int rating;
    private String comment;
    
    public Review(String reviewId, String productId, String userId, int rating, String comment) {
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
}

// Backend microservices (simulated)
class UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    public UserService() {
        // Seed data
        users.put("U1", new User("U1", "Alice Johnson", "alice@example.com", 
            "https://img.com/alice.jpg", "Software engineer and tech enthusiast"));
        users.put("U2", new User("U2", "Bob Smith", "bob@example.com", 
            "https://img.com/bob.jpg", "Product manager"));
    }
    
    public User getUser(String userId) {
        simulateLatency(50);
        return users.get(userId);
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ProductService {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    
    public ProductService() {
        products.put("P1", new Product("P1", "Laptop", "High-performance laptop", 1200.0,
            "https://img.com/laptop.jpg", "Electronics", 50, 
            Arrays.asList("tech", "computers", "work")));
        products.put("P2", new Product("P2", "Headphones", "Noise-canceling headphones", 200.0,
            "https://img.com/headphones.jpg", "Electronics", 100,
            Arrays.asList("audio", "music", "accessories")));
    }
    
    public Product getProduct(String productId) {
        simulateLatency(50);
        return products.get(productId);
    }
    
    public List<Product> getProducts(List<String> productIds) {
        simulateLatency(80);
        return productIds.stream()
            .map(products::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public List<Product> getAllProducts() {
        simulateLatency(100);
        return new ArrayList<>(products.values());
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class OrderService {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    
    public OrderService() {
        orders.put("O1", new Order("O1", "U1", Arrays.asList("P1", "P2"), 1400.0, "Shipped"));
        orders.put("O2", new Order("O2", "U1", Arrays.asList("P2"), 200.0, "Delivered"));
    }
    
    public Order getOrder(String orderId) {
        simulateLatency(50);
        return orders.get(orderId);
    }
    
    public List<Order> getUserOrders(String userId) {
        simulateLatency(70);
        return orders.values().stream()
            .filter(o -> o.getUserId().equals(userId))
            .collect(Collectors.toList());
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ReviewService {
    private final Map<String, List<Review>> reviewsByProduct = new ConcurrentHashMap<>();
    
    public ReviewService() {
        reviewsByProduct.put("P1", Arrays.asList(
            new Review("R1", "P1", "U2", 5, "Excellent laptop!"),
            new Review("R2", "P1", "U1", 4, "Great performance")
        ));
    }
    
    public List<Review> getProductReviews(String productId) {
        simulateLatency(60);
        return reviewsByProduct.getOrDefault(productId, Collections.emptyList());
    }
    
    private void simulateLatency(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// Example 1: Web BFF - Rich experience with detailed data
class WebBFF {
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final ExecutorService executor;
    
    public WebBFF(UserService userService, ProductService productService,
                  OrderService orderService, ReviewService reviewService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewService = reviewService;
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    // Rich product details for web - includes everything
    public Map<String, Object> getProductDetails(String productId) {
        System.out.println("[Web BFF] Fetching detailed product information for: " + productId);
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        
        // Fetch product, reviews, and related data in parallel
        CompletableFuture<Product> productFuture = CompletableFuture.supplyAsync(
            () -> productService.getProduct(productId), executor);
        
        CompletableFuture<List<Review>> reviewsFuture = CompletableFuture.supplyAsync(
            () -> reviewService.getProductReviews(productId), executor);
        
        try {
            Product product = productFuture.get();
            List<Review> reviews = reviewsFuture.get();
            
            // Full product details
            response.put("productId", product.getProductId());
            response.put("name", product.getName());
            response.put("description", product.getDescription());
            response.put("price", product.getPrice());
            response.put("imageUrl", product.getImageUrl());
            response.put("category", product.getCategory());
            response.put("stockQuantity", product.getStockQuantity());
            response.put("tags", product.getTags());
            
            // Review details
            double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
            
            response.put("averageRating", avgRating);
            response.put("reviewCount", reviews.size());
            response.put("reviews", reviews.stream().map(r -> {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("rating", r.getRating());
                reviewMap.put("comment", r.getComment());
                reviewMap.put("userId", r.getUserId());
                return reviewMap;
            }).collect(Collectors.toList()));
            
            System.out.println("[Web BFF] Response size: ~" + estimateSize(response) + " bytes");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return response;
    }
    
    // User dashboard with order history
    public Map<String, Object> getUserDashboard(String userId) {
        System.out.println("[Web BFF] Fetching user dashboard for: " + userId);
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        
        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(
            () -> userService.getUser(userId), executor);
        
        CompletableFuture<List<Order>> ordersFuture = CompletableFuture.supplyAsync(
            () -> orderService.getUserOrders(userId), executor);
        
        try {
            User user = userFuture.get();
            List<Order> orders = ordersFuture.get();
            
            // User profile
            response.put("userId", user.getUserId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("profileImage", user.getProfileImage());
            response.put("bio", user.getBio());
            
            // Order history with product details
            List<Map<String, Object>> orderDetails = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("orderId", order.getOrderId());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("status", order.getStatus());
                orderMap.put("orderDate", order.getOrderDate().toString());
                
                // Fetch product details for each order
                List<Product> products = productService.getProducts(order.getProductIds());
                orderMap.put("products", products.stream().map(p -> {
                    Map<String, Object> pMap = new HashMap<>();
                    pMap.put("name", p.getName());
                    pMap.put("imageUrl", p.getImageUrl());
                    pMap.put("price", p.getPrice());
                    return pMap;
                }).collect(Collectors.toList()));
                
                orderDetails.add(orderMap);
            }
            
            response.put("orders", orderDetails);
            response.put("totalOrders", orders.size());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return response;
    }
    
    private int estimateSize(Map<String, Object> map) {
        return map.toString().length() * 2; // Rough estimate
    }
}

// Example 2: Mobile BFF - Optimized for bandwidth and battery
class MobileBFF {
    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    
    public MobileBFF(UserService userService, ProductService productService,
                     OrderService orderService, ReviewService reviewService) {
        this.productService = productService;
        this.orderService = orderService;
        this.reviewService = reviewService;
    }
    
    // Minimal product info for list view
    public Map<String, Object> getProductSummary(String productId) {
        System.out.println("[Mobile BFF] Fetching minimal product info for: " + productId);
        
        Product product = productService.getProduct(productId);
        List<Review> reviews = reviewService.getProductReviews(productId);
        
        Map<String, Object> response = new HashMap<>();
        
        // Only essential fields for mobile
        response.put("id", product.getProductId());
        response.put("name", product.getName());
        response.put("price", product.getPrice());
        response.put("thumbnail", product.getImageUrl()); // Assume thumbnail version
        response.put("inStock", product.getStockQuantity() > 0);
        
        // Simplified rating
        double avgRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        response.put("rating", Math.round(avgRating * 10) / 10.0);
        
        System.out.println("[Mobile BFF] Response size: ~" + estimateSize(response) + " bytes");
        
        return response;
    }
    
    // Paginated, minimal user orders
    public Map<String, Object> getUserOrders(String userId, int page, int pageSize) {
        System.out.println("[Mobile BFF] Fetching paginated orders for: " + userId);
        
        List<Order> allOrders = orderService.getUserOrders(userId);
        
        // Pagination
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allOrders.size());
        List<Order> pageOrders = allOrders.subList(start, end);
        
        Map<String, Object> response = new HashMap<>();
        
        // Minimal order info
        List<Map<String, Object>> orders = pageOrders.stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getOrderId());
            orderMap.put("total", order.getTotalAmount());
            orderMap.put("status", order.getStatus());
            orderMap.put("itemCount", order.getProductIds().size());
            // No product details to save bandwidth
            return orderMap;
        }).collect(Collectors.toList());
        
        response.put("orders", orders);
        response.put("page", page);
        response.put("hasMore", end < allOrders.size());
        
        return response;
    }
    
    private int estimateSize(Map<String, Object> map) {
        return map.toString().length() * 2;
    }
}

// Example 3: IoT/Wearable BFF - Ultra minimal, status only
class IoTBFF {
    private final OrderService orderService;
    
    public IoTBFF(OrderService orderService) {
        this.orderService = orderService;
    }
    
    // Extremely minimal - just order status
    public Map<String, Object> getOrderStatus(String orderId) {
        System.out.println("[IoT BFF] Fetching minimal order status for: " + orderId);
        
        Order order = orderService.getOrder(orderId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getOrderId());
        response.put("status", order.getStatus());
        
        // Status code for easy parsing by IoT devices
        response.put("statusCode", getStatusCode(order.getStatus()));
        
        System.out.println("[IoT BFF] Response size: ~" + estimateSize(response) + " bytes");
        
        return response;
    }
    
    private int getStatusCode(String status) {
        switch (status) {
            case "Pending": return 1;
            case "Shipped": return 2;
            case "Delivered": return 3;
            default: return 0;
        }
    }
    
    private int estimateSize(Map<String, Object> map) {
        return map.toString().length() * 2;
    }
}

// Example 4: Admin Dashboard BFF - Analytics and aggregations
class AdminBFF {
    private final ProductService productService;
    private final ExecutorService executor;
    
    public AdminBFF(UserService userService, ProductService productService,
                    OrderService orderService) {
        this.productService = productService;
        this.executor = Executors.newFixedThreadPool(10);
    }
    
    // Analytics dashboard with aggregated data
    public Map<String, Object> getDashboardAnalytics() {
        System.out.println("[Admin BFF] Fetching dashboard analytics");
        
        Map<String, Object> response = new ConcurrentHashMap<>();
        
        CompletableFuture<List<Product>> productsFuture = CompletableFuture.supplyAsync(
            () -> productService.getAllProducts(), executor);
        
        try {
            List<Product> products = productsFuture.get();
            
            // Product analytics
            response.put("totalProducts", products.size());
            response.put("totalStock", products.stream()
                .mapToInt(Product::getStockQuantity)
                .sum());
            response.put("averagePrice", products.stream()
                .mapToDouble(Product::getPrice)
                .average()
                .orElse(0.0));
            
            // Category breakdown
            Map<String, Long> categoryCount = products.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
            response.put("categoryBreakdown", categoryCount);
            
            // Low stock alerts
            List<String> lowStock = products.stream()
                .filter(p -> p.getStockQuantity() < 20)
                .map(Product::getName)
                .collect(Collectors.toList());
            response.put("lowStockProducts", lowStock);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return response;
    }
}

// Demonstration
public class BackendForFrontendPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Backend for Frontend (BFF) Pattern Demo ===\n");
        
        // Initialize backend services
        UserService userService = new UserService();
        ProductService productService = new ProductService();
        OrderService orderService = new OrderService();
        ReviewService reviewService = new ReviewService();
        
        // Initialize BFFs
        WebBFF webBFF = new WebBFF(userService, productService, orderService, reviewService);
        MobileBFF mobileBFF = new MobileBFF(userService, productService, orderService, reviewService);
        IoTBFF iotBFF = new IoTBFF(orderService);
        AdminBFF adminBFF = new AdminBFF(userService, productService, orderService);
        
        // Demo 1: Web BFF - Rich product details
        System.out.println("--- Demo 1: Web BFF (Rich Experience) ---");
        Map<String, Object> webProduct = webBFF.getProductDetails("P1");
        System.out.println("Web Response: " + webProduct + "\n");
        
        Thread.sleep(500);
        
        // Demo 2: Mobile BFF - Minimal product info
        System.out.println("--- Demo 2: Mobile BFF (Optimized) ---");
        Map<String, Object> mobileProduct = mobileBFF.getProductSummary("P1");
        System.out.println("Mobile Response: " + mobileProduct + "\n");
        
        Thread.sleep(500);
        
        // Demo 3: IoT BFF - Ultra minimal
        System.out.println("--- Demo 3: IoT BFF (Ultra Minimal) ---");
        Map<String, Object> iotStatus = iotBFF.getOrderStatus("O1");
        System.out.println("IoT Response: " + iotStatus + "\n");
        
        Thread.sleep(500);
        
        // Demo 4: Web vs Mobile comparison
        System.out.println("--- Demo 4: Web vs Mobile User Dashboard ---");
        Map<String, Object> webDashboard = webBFF.getUserDashboard("U1");
        System.out.println("Web Dashboard keys: " + webDashboard.keySet());
        
        Map<String, Object> mobileOrders = mobileBFF.getUserOrders("U1", 0, 10);
        System.out.println("Mobile Orders keys: " + mobileOrders.keySet());
        
        Thread.sleep(500);
        
        // Demo 5: Admin BFF
        System.out.println("\n--- Demo 5: Admin BFF (Analytics) ---");
        Map<String, Object> analytics = adminBFF.getDashboardAnalytics();
        System.out.println("Analytics: " + analytics);
    }
}
