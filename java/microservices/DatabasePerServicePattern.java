package microservices;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DATABASE PER SERVICE PATTERN
 * 
 * Each microservice has its own private database that cannot be accessed
 * directly by other services. Services communicate via APIs only.
 * 
 * Benefits:
 * - Service independence and autonomy
 * - Prevents tight coupling via database
 * - Each service can use optimal database technology
 * - Easier to scale services independently
 * - Clear ownership boundaries
 * 
 * Use Cases:
 * - Microservices architectures
 * - Polyglot persistence needs
 * - Services with different data access patterns
 * - Systems requiring independent deployment
 * - Domain-driven design implementations
 */

// ============================================
// USER SERVICE with its own database
// ============================================

class UserData {
    private String id;
    private String name;
    private String email;
    private String addressId; // Reference to address service
    
    public UserData(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }
    
    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, email=%s]", id, name, email);
    }
}

class UserDatabase {
    private final Map<String, UserData> users = new ConcurrentHashMap<>();
    
    public UserDatabase() {
        // Initialize data
        users.put("U1", new UserData("U1", "Alice", "alice@example.com"));
        users.put("U2", new UserData("U2", "Bob", "bob@example.com"));
    }
    
    public UserData findById(String id) {
        System.out.println("  [USER DB] Query: SELECT * FROM users WHERE id = '" + id + "'");
        return users.get(id);
    }
    
    public void save(UserData user) {
        System.out.println("  [USER DB] INSERT/UPDATE user: " + user.getId());
        users.put(user.getId(), user);
    }
    
    public List<UserData> findAll() {
        System.out.println("  [USER DB] Query: SELECT * FROM users");
        return new ArrayList<>(users.values());
    }
}

class UserService {
    private final UserDatabase database = new UserDatabase();
    
    public UserData getUser(String userId) {
        System.out.println("[USER SERVICE] Getting user: " + userId);
        return database.findById(userId);
    }
    
    public List<UserData> getAllUsers() {
        System.out.println("[USER SERVICE] Getting all users");
        return database.findAll();
    }
    
    public void updateUser(UserData user) {
        System.out.println("[USER SERVICE] Updating user: " + user.getId());
        database.save(user);
    }
    
    public void linkAddress(String userId, String addressId) {
        System.out.println("[USER SERVICE] Linking address " + addressId + 
            " to user " + userId);
        UserData user = database.findById(userId);
        if (user != null) {
            user.setAddressId(addressId);
            database.save(user);
        }
    }
}

// ============================================
// ORDER SERVICE with its own database
// ============================================

class OrderData {
    private String id;
    private String userId; // Reference to user service
    private String productId; // Reference to product service
    private int quantity;
    private double totalAmount;
    private String status;
    
    public OrderData(String id, String userId, String productId, int quantity, double totalAmount) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
    }
    
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("Order[id=%s, user=%s, product=%s, qty=%d, total=$%.2f, status=%s]",
            id, userId, productId, quantity, totalAmount, status);
    }
}

class OrderDatabase {
    private final Map<String, OrderData> orders = new ConcurrentHashMap<>();
    
    public OrderData findById(String id) {
        System.out.println("  [ORDER DB] Query: SELECT * FROM orders WHERE id = '" + id + "'");
        return orders.get(id);
    }
    
    public void save(OrderData order) {
        System.out.println("  [ORDER DB] INSERT/UPDATE order: " + order.getId());
        orders.put(order.getId(), order);
    }
    
    public List<OrderData> findByUserId(String userId) {
        System.out.println("  [ORDER DB] Query: SELECT * FROM orders WHERE user_id = '" + userId + "'");
        List<OrderData> result = new ArrayList<>();
        for (OrderData order : orders.values()) {
            if (order.getUserId().equals(userId)) {
                result.add(order);
            }
        }
        return result;
    }
    
    public List<OrderData> findAll() {
        System.out.println("  [ORDER DB] Query: SELECT * FROM orders");
        return new ArrayList<>(orders.values());
    }
}

class OrderService {
    private final OrderDatabase database = new OrderDatabase();
    
    public OrderData createOrder(String userId, String productId, int quantity, double totalAmount) {
        System.out.println("[ORDER SERVICE] Creating order");
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        OrderData order = new OrderData(orderId, userId, productId, quantity, totalAmount);
        database.save(order);
        return order;
    }
    
    public OrderData getOrder(String orderId) {
        System.out.println("[ORDER SERVICE] Getting order: " + orderId);
        return database.findById(orderId);
    }
    
    public List<OrderData> getOrdersByUser(String userId) {
        System.out.println("[ORDER SERVICE] Getting orders for user: " + userId);
        return database.findByUserId(userId);
    }
    
    public void updateOrderStatus(String orderId, String status) {
        System.out.println("[ORDER SERVICE] Updating order status: " + orderId + " -> " + status);
        OrderData order = database.findById(orderId);
        if (order != null) {
            order.setStatus(status);
            database.save(order);
        }
    }
}

// ============================================
// PRODUCT SERVICE with its own database
// ============================================

class ProductData {
    private String id;
    private String name;
    private double price;
    private int stock;
    
    public ProductData(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
    @Override
    public String toString() {
        return String.format("Product[id=%s, name=%s, price=$%.2f, stock=%d]",
            id, name, price, stock);
    }
}

class ProductDatabase {
    private final Map<String, ProductData> products = new ConcurrentHashMap<>();
    
    public ProductDatabase() {
        // Initialize data
        products.put("P1", new ProductData("P1", "Laptop", 999.99, 10));
        products.put("P2", new ProductData("P2", "Mouse", 29.99, 50));
    }
    
    public ProductData findById(String id) {
        System.out.println("  [PRODUCT DB] Query: SELECT * FROM products WHERE id = '" + id + "'");
        return products.get(id);
    }
    
    public void save(ProductData product) {
        System.out.println("  [PRODUCT DB] INSERT/UPDATE product: " + product.getId());
        products.put(product.getId(), product);
    }
    
    public List<ProductData> findAll() {
        System.out.println("  [PRODUCT DB] Query: SELECT * FROM products");
        return new ArrayList<>(products.values());
    }
}

class ProductService {
    private final ProductDatabase database = new ProductDatabase();
    
    public ProductData getProduct(String productId) {
        System.out.println("[PRODUCT SERVICE] Getting product: " + productId);
        return database.findById(productId);
    }
    
    public List<ProductData> getAllProducts() {
        System.out.println("[PRODUCT SERVICE] Getting all products");
        return database.findAll();
    }
    
    public boolean reserveStock(String productId, int quantity) {
        System.out.println("[PRODUCT SERVICE] Reserving stock: " + productId + " x" + quantity);
        ProductData product = database.findById(productId);
        
        if (product == null) {
            System.out.println("[PRODUCT SERVICE] Product not found");
            return false;
        }
        
        if (product.getStock() < quantity) {
            System.out.println("[PRODUCT SERVICE] Insufficient stock");
            return false;
        }
        
        product.setStock(product.getStock() - quantity);
        database.save(product);
        System.out.println("[PRODUCT SERVICE] Stock reserved successfully");
        return true;
    }
}

// ============================================
// ADDRESS SERVICE with its own database
// ============================================

class AddressData {
    private String id;
    private String street;
    private String city;
    private String zipCode;
    
    public AddressData(String id, String street, String city, String zipCode) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
    }
    
    public String getId() { return id; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getZipCode() { return zipCode; }
    
    @Override
    public String toString() {
        return String.format("Address[%s, %s, %s]", street, city, zipCode);
    }
}

class AddressDatabase {
    private final Map<String, AddressData> addresses = new ConcurrentHashMap<>();
    
    public AddressData findById(String id) {
        System.out.println("  [ADDRESS DB] Query: SELECT * FROM addresses WHERE id = '" + id + "'");
        return addresses.get(id);
    }
    
    public void save(AddressData address) {
        System.out.println("  [ADDRESS DB] INSERT/UPDATE address: " + address.getId());
        addresses.put(address.getId(), address);
    }
}

class AddressService {
    private final AddressDatabase database = new AddressDatabase();
    
    public AddressData createAddress(String street, String city, String zipCode) {
        System.out.println("[ADDRESS SERVICE] Creating address");
        String addressId = "ADDR-" + UUID.randomUUID().toString().substring(0, 8);
        AddressData address = new AddressData(addressId, street, city, zipCode);
        database.save(address);
        return address;
    }
    
    public AddressData getAddress(String addressId) {
        System.out.println("[ADDRESS SERVICE] Getting address: " + addressId);
        return database.findById(addressId);
    }
}

// ============================================
// COMPOSITE SERVICE (Orchestration)
// ============================================

class UserProfileDTO {
    private UserData user;
    private AddressData address;
    private List<OrderData> orders;
    
    public UserProfileDTO(UserData user, AddressData address, List<OrderData> orders) {
        this.user = user;
        this.address = address;
        this.orders = orders;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User Profile:\n");
        sb.append("  User: ").append(user).append("\n");
        sb.append("  Address: ").append(address != null ? address : "No address").append("\n");
        sb.append("  Orders: ").append(orders.size()).append("\n");
        for (OrderData order : orders) {
            sb.append("    - ").append(order).append("\n");
        }
        return sb.toString();
    }
}

class CompositeService {
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;
    private final AddressService addressService;
    
    public CompositeService(UserService userService, OrderService orderService,
                          ProductService productService, AddressService addressService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
        this.addressService = addressService;
    }
    
    public UserProfileDTO getUserProfile(String userId) {
        System.out.println("[COMPOSITE SERVICE] Building user profile for: " + userId);
        
        // Call user service
        UserData user = userService.getUser(userId);
        
        if (user == null) {
            return null;
        }
        
        // Call address service if user has address
        AddressData address = null;
        if (user.getAddressId() != null) {
            address = addressService.getAddress(user.getAddressId());
        }
        
        // Call order service
        List<OrderData> orders = orderService.getOrdersByUser(userId);
        
        return new UserProfileDTO(user, address, orders);
    }
    
    public OrderData placeOrder(String userId, String productId, int quantity) {
        System.out.println("[COMPOSITE SERVICE] Placing order");
        
        // Verify user exists
        UserData user = userService.getUser(userId);
        if (user == null) {
            System.out.println("[COMPOSITE SERVICE] User not found");
            return null;
        }
        
        // Get product and check price
        ProductData product = productService.getProduct(productId);
        if (product == null) {
            System.out.println("[COMPOSITE SERVICE] Product not found");
            return null;
        }
        
        // Reserve stock
        if (!productService.reserveStock(productId, quantity)) {
            System.out.println("[COMPOSITE SERVICE] Failed to reserve stock");
            return null;
        }
        
        // Create order
        double totalAmount = product.getPrice() * quantity;
        OrderData order = orderService.createOrder(userId, productId, quantity, totalAmount);
        
        System.out.println("[COMPOSITE SERVICE] Order placed successfully");
        return order;
    }
}

// Demo
public class DatabasePerServicePattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  DATABASE PER SERVICE PATTERN DEMO       ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Initialize services (each with its own database)
        UserService userService = new UserService();
        OrderService orderService = new OrderService();
        ProductService productService = new ProductService();
        AddressService addressService = new AddressService();
        
        CompositeService compositeService = new CompositeService(
            userService, orderService, productService, addressService
        );
        
        System.out.println("🗄️  Each service has its own private database\n");
        
        // Example 1: Direct service access
        System.out.println("1. DIRECT SERVICE ACCESS (Service Isolation)");
        System.out.println("─────────────────────────────────────────────");
        
        UserData user = userService.getUser("U1");
        System.out.println("Retrieved: " + user);
        
        System.out.println();
        ProductData product = productService.getProduct("P1");
        System.out.println("Retrieved: " + product);
        
        // Example 2: Cross-service communication via APIs
        System.out.println("\n2. CROSS-SERVICE COMMUNICATION");
        System.out.println("───────────────────────────────");
        
        // Create address in address service
        AddressData address = addressService.createAddress("123 Main St", "Springfield", "12345");
        System.out.println("Created: " + address);
        
        // Link address to user (via user service API)
        System.out.println();
        userService.linkAddress("U1", address.getId());
        
        // Example 3: Place order (orchestration across services)
        System.out.println("\n3. PLACE ORDER (Multi-Service Orchestration)");
        System.out.println("─────────────────────────────────────────────");
        
        OrderData order = compositeService.placeOrder("U1", "P1", 2);
        if (order != null) {
            System.out.println("Order created: " + order);
        }
        
        // Example 4: Get complete user profile (data from multiple services)
        System.out.println("\n4. GET USER PROFILE (Data Aggregation)");
        System.out.println("───────────────────────────────────────");
        
        UserProfileDTO profile = compositeService.getUserProfile("U1");
        System.out.println(profile);
        
        // Example 5: Demonstrate service independence
        System.out.println("\n5. SERVICE INDEPENDENCE");
        System.out.println("───────────────────────");
        System.out.println("✅ User Service can be deployed independently");
        System.out.println("✅ Order Service can be scaled separately");
        System.out.println("✅ Product Service can use different database technology");
        System.out.println("✅ Address Service can be updated without affecting others");
        
        System.out.println("\n✅ Database per Service Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Each service owns its private database");
        System.out.println("  • Services communicate only via APIs");
        System.out.println("  • Independent deployment and scaling");
        System.out.println("  • Technology diversity (polyglot persistence)");
        System.out.println("  • Clear service boundaries");
    }
}
