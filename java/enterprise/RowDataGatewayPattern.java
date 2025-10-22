package enterprise;

import java.util.*;

/**
 * ROW DATA GATEWAY PATTERN
 * 
 * An object that acts as a Gateway to a single record in a data source. Each instance
 * contains all the data from one row and provides methods to manipulate that data.
 * Unlike Active Record, it contains only data access logic, not business logic.
 * 
 * Benefits:
 * - One object per database row
 * - Clear mapping between object and row
 * - Separates data access from business logic
 * - Easy to understand and implement
 * - Works well with Table Data Gateway
 * 
 * Use Cases:
 * - Simple CRUD operations on rows
 * - Data access layer in layered architecture
 * - When each row needs independent handling
 * - Transaction Script pattern implementations
 * - Legacy database integration
 */

// Row Data Gateway for User
class UserRowGateway {
    // Database simulation
    private static final Map<Integer, UserRowGateway> database = new HashMap<>();
    private static int nextId = 1;
    
    // Row data
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private boolean active;
    
    // Private constructor for loading from DB
    private UserRowGateway(int id, String username, String email, 
                          String passwordHash, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
    }
    
    // Factory method for new record
    public static UserRowGateway create(String username, String email, String password) {
        System.out.println("  📝 SQL: INSERT INTO users VALUES (...)");
        
        int id = nextId++;
        String passwordHash = "hash_" + password; // Simplified hashing
        UserRowGateway gateway = new UserRowGateway(id, username, email, passwordHash, true);
        database.put(id, gateway);
        
        System.out.println("  ✅ Created user: " + gateway);
        return gateway;
    }
    
    // Finder method
    public static UserRowGateway find(int id) {
        System.out.println("  📝 SQL: SELECT * FROM users WHERE id = " + id);
        
        UserRowGateway gateway = database.get(id);
        if (gateway != null) {
            System.out.println("  ✅ Found user: " + gateway);
        } else {
            System.out.println("  ❌ User not found");
        }
        return gateway;
    }
    
    // Finder by username
    public static UserRowGateway findByUsername(String username) {
        System.out.println("  📝 SQL: SELECT * FROM users WHERE username = '" + username + "'");
        
        return database.values().stream()
            .filter(u -> u.username.equals(username))
            .findFirst()
            .orElse(null);
    }
    
    // Update this row
    public void update() {
        System.out.println("  📝 SQL: UPDATE users SET username='" + username + 
                         "', email='" + email + "', active=" + active + " WHERE id=" + id);
        
        // Update in database (already in-place in HashMap)
        System.out.println("  ✅ Updated user: " + this);
    }
    
    // Delete this row
    public void delete() {
        System.out.println("  📝 SQL: DELETE FROM users WHERE id = " + id);
        database.remove(id);
        System.out.println("  ✅ Deleted user ID: " + id);
    }
    
    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    
    // Setters (for updating fields before calling update())
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setActive(boolean active) { this.active = active; }
    
    public boolean authenticate(String password) {
        String hash = "hash_" + password;
        return passwordHash.equals(hash);
    }
    
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + 
               "', active=" + active + "}";
    }
}

// Row Data Gateway for Order
class OrderRowGateway {
    private static final Map<Integer, OrderRowGateway> database = new HashMap<>();
    private static int nextId = 1000;
    
    private int id;
    private int userId;
    private double totalAmount;
    private String status;
    private long orderDate;
    
    private OrderRowGateway(int id, int userId, double totalAmount, 
                           String status, long orderDate) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
    }
    
    public static OrderRowGateway create(int userId, double totalAmount) {
        System.out.println("  📝 SQL: INSERT INTO orders VALUES (...)");
        
        int id = nextId++;
        long orderDate = System.currentTimeMillis();
        OrderRowGateway gateway = new OrderRowGateway(id, userId, totalAmount, 
                                                      "PENDING", orderDate);
        database.put(id, gateway);
        
        System.out.println("  ✅ Created order: " + gateway);
        return gateway;
    }
    
    public static OrderRowGateway find(int id) {
        System.out.println("  📝 SQL: SELECT * FROM orders WHERE id = " + id);
        return database.get(id);
    }
    
    public static List<OrderRowGateway> findByUserId(int userId) {
        System.out.println("  📝 SQL: SELECT * FROM orders WHERE user_id = " + userId);
        
        List<OrderRowGateway> orders = new ArrayList<>();
        for (OrderRowGateway order : database.values()) {
            if (order.userId == userId) {
                orders.add(order);
            }
        }
        
        System.out.println("  ✅ Found " + orders.size() + " orders");
        return orders;
    }
    
    public void update() {
        System.out.println("  📝 SQL: UPDATE orders SET status='" + status + 
                         "', total_amount=" + totalAmount + " WHERE id=" + id);
        System.out.println("  ✅ Updated order: " + this);
    }
    
    public void delete() {
        System.out.println("  📝 SQL: DELETE FROM orders WHERE id = " + id);
        database.remove(id);
        System.out.println("  ✅ Deleted order ID: " + id);
    }
    
    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public long getOrderDate() { return orderDate; }
    
    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    @Override
    public String toString() {
        return "Order{id=" + id + ", userId=" + userId + ", amount=$" + totalAmount + 
               ", status='" + status + "'}";
    }
}

// Row Data Gateway for Product
class ProductRowGateway {
    private static final Map<Integer, ProductRowGateway> database = new HashMap<>();
    private static int nextId = 100;
    
    private int id;
    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    
    private ProductRowGateway(int id, String name, String description, 
                             double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    public static ProductRowGateway create(String name, String description, 
                                          double price, int stockQuantity) {
        System.out.println("  📝 SQL: INSERT INTO products VALUES (...)");
        
        int id = nextId++;
        ProductRowGateway gateway = new ProductRowGateway(id, name, description, 
                                                          price, stockQuantity);
        database.put(id, gateway);
        
        System.out.println("  ✅ Created product: " + gateway);
        return gateway;
    }
    
    public static ProductRowGateway find(int id) {
        System.out.println("  📝 SQL: SELECT * FROM products WHERE id = " + id);
        ProductRowGateway product = database.get(id);
        if (product != null) {
            System.out.println("  ✅ Found: " + product);
        }
        return product;
    }
    
    public static List<ProductRowGateway> findInStock() {
        System.out.println("  📝 SQL: SELECT * FROM products WHERE stock_quantity > 0");
        
        List<ProductRowGateway> products = new ArrayList<>();
        for (ProductRowGateway product : database.values()) {
            if (product.stockQuantity > 0) {
                products.add(product);
            }
        }
        
        System.out.println("  ✅ Found " + products.size() + " products in stock");
        return products;
    }
    
    public void update() {
        System.out.println("  📝 SQL: UPDATE products SET name='" + name + 
                         "', price=" + price + ", stock_quantity=" + stockQuantity + 
                         " WHERE id=" + id);
        System.out.println("  ✅ Updated product: " + this);
    }
    
    public void delete() {
        System.out.println("  📝 SQL: DELETE FROM products WHERE id = " + id);
        database.remove(id);
        System.out.println("  ✅ Deleted product ID: " + id);
    }
    
    // Business helper methods
    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }
    
    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        stockQuantity -= quantity;
        System.out.println("  📦 Reduced stock by " + quantity + ", remaining: " + stockQuantity);
    }
    
    public void increaseStock(int quantity) {
        stockQuantity += quantity;
        System.out.println("  📦 Increased stock by " + quantity + ", total: " + stockQuantity);
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    
    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=$" + price + 
               ", stock=" + stockQuantity + "}";
    }
}

// Business Logic using Row Data Gateways
class OrderProcessor {
    public void placeOrder(int userId, int productId, int quantity) {
        System.out.println("\n[ORDER PROCESSOR] Processing order...");
        
        // Load user row
        UserRowGateway user = UserRowGateway.find(userId);
        if (user == null || !user.isActive()) {
            System.out.println("❌ Order failed: Invalid or inactive user");
            return;
        }
        
        // Load product row
        ProductRowGateway product = ProductRowGateway.find(productId);
        if (product == null) {
            System.out.println("❌ Order failed: Product not found");
            return;
        }
        
        // Check stock
        if (!product.hasStock(quantity)) {
            System.out.println("❌ Order failed: Insufficient stock");
            return;
        }
        
        // Calculate total
        double totalAmount = product.getPrice() * quantity;
        
        // Create order row
        OrderRowGateway order = OrderRowGateway.create(userId, totalAmount);
        
        // Reduce product stock
        product.reduceStock(quantity);
        product.update(); // Persist changes
        
        System.out.println("✅ Order placed successfully! Order ID: " + order.getId());
    }
    
    public void completeOrder(int orderId) {
        System.out.println("\n[ORDER PROCESSOR] Completing order " + orderId + "...");
        
        OrderRowGateway order = OrderRowGateway.find(orderId);
        if (order == null) {
            System.out.println("❌ Order not found");
            return;
        }
        
        order.setStatus("COMPLETED");
        order.update();
        
        System.out.println("✅ Order completed");
    }
    
    public void cancelOrder(int orderId) {
        System.out.println("\n[ORDER PROCESSOR] Canceling order " + orderId + "...");
        
        OrderRowGateway order = OrderRowGateway.find(orderId);
        if (order == null) {
            System.out.println("❌ Order not found");
            return;
        }
        
        order.setStatus("CANCELLED");
        order.update();
        
        System.out.println("✅ Order cancelled");
    }
}

// Demo
public class RowDataGatewayPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   ROW DATA GATEWAY PATTERN DEMONSTRATION       ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        
        // Example 1: User Row Gateway
        System.out.println("\n1. USER ROW GATEWAY (CRUD on Single Row)");
        System.out.println("─────────────────────────────────────────");
        
        UserRowGateway user1 = UserRowGateway.create("alice", "alice@example.com", "pass123");
        UserRowGateway user2 = UserRowGateway.create("bob", "bob@example.com", "pass456");
        
        System.out.println("\nFind by ID:");
        UserRowGateway foundUser = UserRowGateway.find(user1.getId());
        
        System.out.println("\nAuthenticate:");
        boolean auth = foundUser.authenticate("pass123");
        System.out.println("  Authentication: " + (auth ? "✅ Success" : "❌ Failed"));
        
        System.out.println("\nUpdate user:");
        foundUser.setEmail("alice.updated@example.com");
        foundUser.update();
        
        // Example 2: Product Row Gateway
        System.out.println("\n\n2. PRODUCT ROW GATEWAY (Inventory Management)");
        System.out.println("───────────────────────────────────────────────");
        
        ProductRowGateway product1 = ProductRowGateway.create(
            "Laptop", "High-performance laptop", 1299.99, 50
        );
        ProductRowGateway product2 = ProductRowGateway.create(
            "Mouse", "Wireless mouse", 29.99, 200
        );
        ProductRowGateway product3 = ProductRowGateway.create(
            "Keyboard", "Mechanical keyboard", 89.99, 0
        );
        
        System.out.println("\nFind products in stock:");
        List<ProductRowGateway> inStock = ProductRowGateway.findInStock();
        inStock.forEach(System.out::println);
        
        System.out.println("\nUpdate product:");
        product1.setPrice(1199.99);
        product1.update();
        
        // Example 3: Order Row Gateway
        System.out.println("\n\n3. ORDER ROW GATEWAY (Order Operations)");
        System.out.println("────────────────────────────────────────");
        
        OrderRowGateway order1 = OrderRowGateway.create(user1.getId(), 1299.99);
        OrderRowGateway order2 = OrderRowGateway.create(user1.getId(), 29.99);
        OrderRowGateway order3 = OrderRowGateway.create(user2.getId(), 89.99);
        
        System.out.println("\nFind orders by user:");
        List<OrderRowGateway> userOrders = OrderRowGateway.findByUserId(user1.getId());
        System.out.println("  User " + user1.getUsername() + " has " + userOrders.size() + " orders:");
        userOrders.forEach(o -> System.out.println("    " + o));
        
        System.out.println("\nUpdate order status:");
        order1.setStatus("COMPLETED");
        order1.update();
        
        // Example 4: Business Logic with Row Data Gateways
        System.out.println("\n\n4. ORDER PROCESSOR (Business Logic with Gateways)");
        System.out.println("───────────────────────────────────────────────────");
        
        OrderProcessor processor = new OrderProcessor();
        processor.placeOrder(user1.getId(), product2.getId(), 2);
        processor.placeOrder(user2.getId(), product1.getId(), 1);
        
        System.out.println("\nAttempt to order out-of-stock product:");
        processor.placeOrder(user1.getId(), product3.getId(), 1);
        
        // Example 5: Order lifecycle
        System.out.println("\n\n5. ORDER LIFECYCLE (Status Changes)");
        System.out.println("────────────────────────────────────");
        
        OrderRowGateway newOrder = OrderRowGateway.create(user1.getId(), 89.99);
        System.out.println("  Initial status: " + newOrder.getStatus());
        
        processor.completeOrder(newOrder.getId());
        
        OrderRowGateway anotherOrder = OrderRowGateway.create(user2.getId(), 29.99);
        processor.cancelOrder(anotherOrder.getId());
        
        // Example 6: Delete operations
        System.out.println("\n\n6. DELETE OPERATIONS");
        System.out.println("────────────────────");
        
        System.out.println("\nDelete user:");
        UserRowGateway userToDelete = UserRowGateway.create("temp", "temp@example.com", "pass");
        userToDelete.delete();
        
        System.out.println("\nAttempt to find deleted user:");
        UserRowGateway deletedUser = UserRowGateway.find(userToDelete.getId());
        System.out.println("  Result: " + (deletedUser == null ? "Not found (as expected)" : "Found"));
        
        System.out.println("\n\n✅ Row Data Gateway Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • One object per database row");
        System.out.println("  • Clear object-row mapping");
        System.out.println("  • Each instance handles its own persistence");
        System.out.println("  • Simple and intuitive");
        System.out.println("  • Good for simple CRUD operations");
        
        System.out.println("\n🆚 Compare with:");
        System.out.println("  • Table Data Gateway: One instance handles ALL rows");
        System.out.println("  • Row Data Gateway: One instance per row (this pattern)");
        System.out.println("  • Active Record: Row Gateway + Business Logic");
        System.out.println("  • Data Mapper: Separate domain objects from persistence");
    }
}
