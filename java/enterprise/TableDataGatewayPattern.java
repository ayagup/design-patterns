package enterprise;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TABLE DATA GATEWAY PATTERN
 * 
 * An object that acts as a Gateway to a database table. One instance handles all
 * the rows in the table. Contains all the SQL for accessing a single table or view:
 * selects, inserts, updates, and deletes.
 * 
 * Benefits:
 * - Centralizes database access logic for a table
 * - Separates SQL from domain logic
 * - Easier to test (mock the gateway)
 * - Single point for query optimization
 * - Consistent data access patterns
 * 
 * Use Cases:
 * - Simple CRUD operations
 * - Transaction Script pattern implementations
 * - Data access layer in layered architecture
 * - Legacy database integration
 * - Reporting and analytics queries
 */

// Domain Objects (Data Transfer Objects)
class User {
    private final int id;
    private String username;
    private String email;
    private String password;
    
    public User(int id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    
    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}

class Order {
    private final int id;
    private int userId;
    private double totalAmount;
    private String status;
    private long orderDate;
    
    public Order(int id, int userId, double totalAmount, String status, long orderDate) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
    }
    
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public long getOrderDate() { return orderDate; }
    
    public void setStatus(String status) { this.status = status; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    @Override
    public String toString() {
        return "Order{id=" + id + ", userId=" + userId + ", amount=$" + totalAmount + 
               ", status='" + status + "'}";
    }
}

class Product {
    private final int id;
    private String name;
    private double price;
    private int stock;
    
    public Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=$" + price + 
               ", stock=" + stock + "}";
    }
}

// Table Data Gateway for Users Table
class UserGateway {
    private final Map<Integer, User> database = new ConcurrentHashMap<>();
    private int nextId = 1;
    
    public int insert(String username, String email, String password) {
        System.out.println("  📝 SQL: INSERT INTO users VALUES (" + nextId + ", '" + 
            username + "', '" + email + "', '" + password + "')");
        
        int id = nextId++;
        User user = new User(id, username, email, password);
        database.put(id, user);
        
        System.out.println("  ✅ Inserted user with ID: " + id);
        return id;
    }
    
    public User findById(int id) {
        System.out.println("  📝 SQL: SELECT * FROM users WHERE id = " + id);
        User user = database.get(id);
        
        if (user != null) {
            System.out.println("  ✅ Found: " + user);
        } else {
            System.out.println("  ❌ User not found");
        }
        
        return user;
    }
    
    public User findByUsername(String username) {
        System.out.println("  📝 SQL: SELECT * FROM users WHERE username = '" + username + "'");
        
        User user = database.values().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
        
        if (user != null) {
            System.out.println("  ✅ Found: " + user);
        } else {
            System.out.println("  ❌ User not found");
        }
        
        return user;
    }
    
    public List<User> findAll() {
        System.out.println("  📝 SQL: SELECT * FROM users");
        List<User> users = new ArrayList<>(database.values());
        System.out.println("  ✅ Found " + users.size() + " users");
        return users;
    }
    
    public void update(int id, String username, String email) {
        System.out.println("  📝 SQL: UPDATE users SET username='" + username + 
            "', email='" + email + "' WHERE id=" + id);
        
        User user = database.get(id);
        if (user != null) {
            user.setUsername(username);
            user.setEmail(email);
            System.out.println("  ✅ Updated user: " + user);
        } else {
            System.out.println("  ❌ User not found for update");
        }
    }
    
    public void delete(int id) {
        System.out.println("  📝 SQL: DELETE FROM users WHERE id = " + id);
        User removed = database.remove(id);
        
        if (removed != null) {
            System.out.println("  ✅ Deleted user: " + removed);
        } else {
            System.out.println("  ❌ User not found for deletion");
        }
    }
}

// Table Data Gateway for Orders Table
class OrderGateway {
    private final Map<Integer, Order> database = new ConcurrentHashMap<>();
    private int nextId = 1;
    
    public int insert(int userId, double totalAmount, String status) {
        System.out.println("  📝 SQL: INSERT INTO orders VALUES (" + nextId + ", " + 
            userId + ", " + totalAmount + ", '" + status + "', " + System.currentTimeMillis() + ")");
        
        int id = nextId++;
        Order order = new Order(id, userId, totalAmount, status, System.currentTimeMillis());
        database.put(id, order);
        
        System.out.println("  ✅ Inserted order with ID: " + id);
        return id;
    }
    
    public Order findById(int id) {
        System.out.println("  📝 SQL: SELECT * FROM orders WHERE id = " + id);
        Order order = database.get(id);
        
        if (order != null) {
            System.out.println("  ✅ Found: " + order);
        } else {
            System.out.println("  ❌ Order not found");
        }
        
        return order;
    }
    
    public List<Order> findByUserId(int userId) {
        System.out.println("  📝 SQL: SELECT * FROM orders WHERE user_id = " + userId);
        
        List<Order> orders = new ArrayList<>();
        for (Order order : database.values()) {
            if (order.getUserId() == userId) {
                orders.add(order);
            }
        }
        
        System.out.println("  ✅ Found " + orders.size() + " orders for user " + userId);
        return orders;
    }
    
    public List<Order> findByStatus(String status) {
        System.out.println("  📝 SQL: SELECT * FROM orders WHERE status = '" + status + "'");
        
        List<Order> orders = new ArrayList<>();
        for (Order order : database.values()) {
            if (order.getStatus().equals(status)) {
                orders.add(order);
            }
        }
        
        System.out.println("  ✅ Found " + orders.size() + " " + status + " orders");
        return orders;
    }
    
    public void updateStatus(int id, String newStatus) {
        System.out.println("  📝 SQL: UPDATE orders SET status='" + newStatus + "' WHERE id=" + id);
        
        Order order = database.get(id);
        if (order != null) {
            order.setStatus(newStatus);
            System.out.println("  ✅ Updated order status: " + order);
        } else {
            System.out.println("  ❌ Order not found for update");
        }
    }
    
    public double getTotalRevenue() {
        System.out.println("  📝 SQL: SELECT SUM(total_amount) FROM orders WHERE status='COMPLETED'");
        
        double total = database.values().stream()
            .filter(o -> o.getStatus().equals("COMPLETED"))
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        System.out.println("  ✅ Total revenue: $" + total);
        return total;
    }
}

// Table Data Gateway for Products Table
class ProductGateway {
    private final Map<Integer, Product> database = new ConcurrentHashMap<>();
    private int nextId = 1;
    
    public int insert(String name, double price, int stock) {
        System.out.println("  📝 SQL: INSERT INTO products VALUES (" + nextId + ", '" + 
            name + "', " + price + ", " + stock + ")");
        
        int id = nextId++;
        Product product = new Product(id, name, price, stock);
        database.put(id, product);
        
        System.out.println("  ✅ Inserted product with ID: " + id);
        return id;
    }
    
    public Product findById(int id) {
        System.out.println("  📝 SQL: SELECT * FROM products WHERE id = " + id);
        Product product = database.get(id);
        
        if (product != null) {
            System.out.println("  ✅ Found: " + product);
        } else {
            System.out.println("  ❌ Product not found");
        }
        
        return product;
    }
    
    public List<Product> findInStock() {
        System.out.println("  📝 SQL: SELECT * FROM products WHERE stock > 0");
        
        List<Product> products = new ArrayList<>();
        for (Product product : database.values()) {
            if (product.getStock() > 0) {
                products.add(product);
            }
        }
        
        System.out.println("  ✅ Found " + products.size() + " products in stock");
        return products;
    }
    
    public void updateStock(int id, int newStock) {
        System.out.println("  📝 SQL: UPDATE products SET stock=" + newStock + " WHERE id=" + id);
        
        Product product = database.get(id);
        if (product != null) {
            product.setStock(newStock);
            System.out.println("  ✅ Updated product stock: " + product);
        } else {
            System.out.println("  ❌ Product not found for update");
        }
    }
    
    public void updatePrice(int id, double newPrice) {
        System.out.println("  📝 SQL: UPDATE products SET price=" + newPrice + " WHERE id=" + id);
        
        Product product = database.get(id);
        if (product != null) {
            product.setPrice(newPrice);
            System.out.println("  ✅ Updated product price: " + product);
        } else {
            System.out.println("  ❌ Product not found for update");
        }
    }
}

// Business Logic using Table Data Gateways
class OrderService {
    private final UserGateway userGateway;
    private final OrderGateway orderGateway;
    private final ProductGateway productGateway;
    
    public OrderService(UserGateway userGateway, OrderGateway orderGateway, 
                       ProductGateway productGateway) {
        this.userGateway = userGateway;
        this.orderGateway = orderGateway;
        this.productGateway = productGateway;
    }
    
    public void placeOrder(int userId, int productId, int quantity) {
        System.out.println("\n[ORDER SERVICE] Placing order...");
        
        // Verify user exists
        User user = userGateway.findById(userId);
        if (user == null) {
            System.out.println("❌ Order failed: User not found");
            return;
        }
        
        // Verify product exists and has stock
        Product product = productGateway.findById(productId);
        if (product == null) {
            System.out.println("❌ Order failed: Product not found");
            return;
        }
        
        if (product.getStock() < quantity) {
            System.out.println("❌ Order failed: Insufficient stock");
            return;
        }
        
        // Calculate total
        double totalAmount = product.getPrice() * quantity;
        
        // Create order
        int orderId = orderGateway.insert(userId, totalAmount, "PENDING");
        
        // Update product stock
        productGateway.updateStock(productId, product.getStock() - quantity);
        
        System.out.println("✅ Order placed successfully! Order ID: " + orderId);
    }
    
    public void completeOrder(int orderId) {
        System.out.println("\n[ORDER SERVICE] Completing order " + orderId + "...");
        orderGateway.updateStatus(orderId, "COMPLETED");
    }
}

// Demo
public class TableDataGatewayPattern {
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║   TABLE DATA GATEWAY PATTERN DEMONSTRATION    ║");
        System.out.println("╚═══════════════════════════════════════════════╝");
        
        // Initialize gateways
        UserGateway userGateway = new UserGateway();
        OrderGateway orderGateway = new OrderGateway();
        ProductGateway productGateway = new ProductGateway();
        
        // Example 1: User CRUD operations
        System.out.println("\n1. USER GATEWAY (CRUD Operations)");
        System.out.println("──────────────────────────────────");
        
        int userId1 = userGateway.insert("alice", "alice@example.com", "pass123");
        int userId2 = userGateway.insert("bob", "bob@example.com", "pass456");
        
        System.out.println("\nFind by ID:");
        userGateway.findById(userId1);
        
        System.out.println("\nFind by username:");
        userGateway.findByUsername("alice");
        
        System.out.println("\nUpdate user:");
        userGateway.update(userId1, "alice_updated", "alice.new@example.com");
        
        System.out.println("\nFind all users:");
        List<User> allUsers = userGateway.findAll();
        allUsers.forEach(System.out::println);
        
        // Example 2: Product operations
        System.out.println("\n\n2. PRODUCT GATEWAY (Inventory Management)");
        System.out.println("───────────────────────────────────────────");
        
        int productId1 = productGateway.insert("Laptop", 999.99, 10);
        int productId2 = productGateway.insert("Mouse", 29.99, 50);
        int productId3 = productGateway.insert("Keyboard", 79.99, 0);
        
        System.out.println("\nFind products in stock:");
        List<Product> inStock = productGateway.findInStock();
        inStock.forEach(System.out::println);
        
        System.out.println("\nUpdate price:");
        productGateway.updatePrice(productId1, 899.99);
        
        // Example 3: Order operations
        System.out.println("\n\n3. ORDER GATEWAY (Order Management)");
        System.out.println("────────────────────────────────────");
        
        int orderId1 = orderGateway.insert(userId1, 999.99, "PENDING");
        int orderId2 = orderGateway.insert(userId1, 29.99, "PENDING");
        int orderId3 = orderGateway.insert(userId2, 79.99, "COMPLETED");
        
        System.out.println("\nFind orders by user:");
        List<Order> userOrders = orderGateway.findByUserId(userId1);
        userOrders.forEach(System.out::println);
        
        System.out.println("\nFind pending orders:");
        List<Order> pendingOrders = orderGateway.findByStatus("PENDING");
        pendingOrders.forEach(System.out::println);
        
        System.out.println("\nUpdate order status:");
        orderGateway.updateStatus(orderId1, "COMPLETED");
        
        // Example 4: Business logic using gateways
        System.out.println("\n\n4. ORDER SERVICE (Business Logic with Gateways)");
        System.out.println("─────────────────────────────────────────────────");
        
        OrderService orderService = new OrderService(userGateway, orderGateway, productGateway);
        orderService.placeOrder(userId1, productId2, 2);
        orderService.placeOrder(userId2, productId1, 1);
        
        // Example 5: Aggregate queries
        System.out.println("\n\n5. AGGREGATE QUERIES (Reporting)");
        System.out.println("─────────────────────────────────");
        
        double totalRevenue = orderGateway.getTotalRevenue();
        System.out.println("\n💰 Total Revenue from Completed Orders: $" + totalRevenue);
        
        System.out.println("\n✅ Table Data Gateway Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Centralized SQL for each table");
        System.out.println("  • Clear separation of data access logic");
        System.out.println("  • Easy to test and mock");
        System.out.println("  • Simple CRUD operations");
        System.out.println("  • Works well with Transaction Script pattern");
        
        System.out.println("\n🆚 Compare with:");
        System.out.println("  • Row Data Gateway: One instance per row");
        System.out.println("  • Table Data Gateway: One instance per table (this pattern)");
        System.out.println("  • Active Record: Domain object + data access");
        System.out.println("  • Data Mapper: Separate domain objects from persistence");
    }
}
