package enterprise;

import java.sql.*;
import java.util.*;

/**
 * DATA MAPPER PATTERN
 * 
 * Separates in-memory objects from database and handles data transfer between them.
 * Acts as a layer between domain objects and the database.
 * 
 * Benefits:
 * - Complete separation of domain logic and database access
 * - Domain objects are not aware of persistence
 * - Easy to test domain objects
 * - Flexible database schema changes
 * - Supports multiple data sources
 * 
 * Use Cases:
 * - Complex domain models
 * - Applications with complex object graphs
 * - Systems requiring database independence
 * - Enterprise applications with rich domain logic
 */

// Domain Model (POJO - no persistence logic)
class User {
    private String id;
    private String username;
    private String email;
    private int age;
    
    public User(String id, String username, String email, int age) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.age = age;
    }
    
    // Domain logic
    public boolean isAdult() {
        return age >= 18;
    }
    
    public String getDisplayName() {
        return username + " (" + email + ")";
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    @Override
    public String toString() {
        return String.format("User[id=%s, username=%s, email=%s, age=%d]",
            id, username, email, age);
    }
}

// Data Mapper interface
interface DataMapper<T> {
    T findById(String id);
    List<T> findAll();
    void insert(T entity);
    void update(T entity);
    void delete(String id);
}

// User Data Mapper (handles all database interactions)
class UserDataMapper implements DataMapper<User> {
    private final Map<String, User> database = new HashMap<>(); // Simulated database
    
    @Override
    public User findById(String id) {
        System.out.println("  [DB] SELECT * FROM users WHERE id = '" + id + "'");
        return database.get(id);
    }
    
    @Override
    public List<User> findAll() {
        System.out.println("  [DB] SELECT * FROM users");
        return new ArrayList<>(database.values());
    }
    
    @Override
    public void insert(User user) {
        System.out.println("  [DB] INSERT INTO users VALUES (" + user.getId() + ", '" + 
            user.getUsername() + "', '" + user.getEmail() + "', " + user.getAge() + ")");
        database.put(user.getId(), user);
    }
    
    @Override
    public void update(User user) {
        System.out.println("  [DB] UPDATE users SET username='" + user.getUsername() + 
            "', email='" + user.getEmail() + "', age=" + user.getAge() + 
            " WHERE id='" + user.getId() + "'");
        database.put(user.getId(), user);
    }
    
    @Override
    public void delete(String id) {
        System.out.println("  [DB] DELETE FROM users WHERE id = '" + id + "'");
        database.remove(id);
    }
    
    // Custom query methods
    public List<User> findByAge(int age) {
        System.out.println("  [DB] SELECT * FROM users WHERE age = " + age);
        List<User> results = new ArrayList<>();
        for (User user : database.values()) {
            if (user.getAge() == age) {
                results.add(user);
            }
        }
        return results;
    }
    
    public List<User> findAdults() {
        System.out.println("  [DB] SELECT * FROM users WHERE age >= 18");
        List<User> results = new ArrayList<>();
        for (User user : database.values()) {
            if (user.isAdult()) {
                results.add(user);
            }
        }
        return results;
    }
}

// Example 2: Product with complex mapping
class Product {
    private String id;
    private String name;
    private double price;
    private String category;
    private List<String> tags;
    
    public Product(String id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.tags = new ArrayList<>();
    }
    
    public void addTag(String tag) {
        tags.add(tag);
    }
    
    public double getPriceWithTax() {
        return price * 1.1; // 10% tax
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCategory() { return category; }
    public List<String> getTags() { return new ArrayList<>(tags); }
    
    @Override
    public String toString() {
        return String.format("Product[id=%s, name=%s, price=$%.2f, category=%s, tags=%s]",
            id, name, price, category, tags);
    }
}

class ProductDataMapper implements DataMapper<Product> {
    private final Map<String, Product> database = new HashMap<>();
    private final Map<String, List<String>> tagsDatabase = new HashMap<>(); // Simulated join table
    
    @Override
    public Product findById(String id) {
        System.out.println("  [DB] SELECT * FROM products WHERE id = '" + id + "'");
        Product product = database.get(id);
        if (product != null) {
            loadTags(product);
        }
        return product;
    }
    
    @Override
    public List<Product> findAll() {
        System.out.println("  [DB] SELECT * FROM products");
        List<Product> products = new ArrayList<>(database.values());
        for (Product product : products) {
            loadTags(product);
        }
        return products;
    }
    
    @Override
    public void insert(Product product) {
        System.out.println("  [DB] INSERT INTO products VALUES (" + product.getId() + ", '" + 
            product.getName() + "', " + product.getPrice() + ", '" + product.getCategory() + "')");
        database.put(product.getId(), product);
        saveTags(product);
    }
    
    @Override
    public void update(Product product) {
        System.out.println("  [DB] UPDATE products SET name='" + product.getName() + 
            "', price=" + product.getPrice() + ", category='" + product.getCategory() + 
            "' WHERE id='" + product.getId() + "'");
        database.put(product.getId(), product);
        saveTags(product);
    }
    
    @Override
    public void delete(String id) {
        System.out.println("  [DB] DELETE FROM products WHERE id = '" + id + "'");
        database.remove(id);
        tagsDatabase.remove(id);
    }
    
    private void loadTags(Product product) {
        System.out.println("  [DB] SELECT tag FROM product_tags WHERE product_id = '" + product.getId() + "'");
        List<String> tags = tagsDatabase.get(product.getId());
        if (tags != null) {
            for (String tag : tags) {
                product.addTag(tag);
            }
        }
    }
    
    private void saveTags(Product product) {
        System.out.println("  [DB] DELETE FROM product_tags WHERE product_id = '" + product.getId() + "'");
        System.out.println("  [DB] INSERT INTO product_tags (product_id, tag) VALUES ...");
        tagsDatabase.put(product.getId(), new ArrayList<>(product.getTags()));
    }
    
    public List<Product> findByCategory(String category) {
        System.out.println("  [DB] SELECT * FROM products WHERE category = '" + category + "'");
        List<Product> results = new ArrayList<>();
        for (Product product : database.values()) {
            if (product.getCategory().equals(category)) {
                loadTags(product);
                results.add(product);
            }
        }
        return results;
    }
}

// Example 3: Order with relationships
class Order {
    private String id;
    private String userId;
    private double totalAmount;
    private String status;
    private List<OrderItem> items;
    
    public Order(String id, String userId) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.totalAmount = 0.0;
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotal();
    }
    
    private void calculateTotal() {
        totalAmount = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
    
    public void complete() {
        status = "COMPLETED";
    }
    
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    
    @Override
    public String toString() {
        return String.format("Order[id=%s, userId=%s, total=$%.2f, status=%s, items=%d]",
            id, userId, totalAmount, status, items.size());
    }
}

class OrderItem {
    private String productId;
    private int quantity;
    private double price;
    
    public OrderItem(String productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
    
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return String.format("OrderItem[product=%s, qty=%d, price=$%.2f]",
            productId, quantity, price);
    }
}

class OrderDataMapper implements DataMapper<Order> {
    private final Map<String, Order> database = new HashMap<>();
    private final Map<String, List<OrderItem>> itemsDatabase = new HashMap<>();
    
    @Override
    public Order findById(String id) {
        System.out.println("  [DB] SELECT * FROM orders WHERE id = '" + id + "'");
        Order order = database.get(id);
        if (order != null) {
            loadItems(order);
        }
        return order;
    }
    
    @Override
    public List<Order> findAll() {
        System.out.println("  [DB] SELECT * FROM orders");
        List<Order> orders = new ArrayList<>(database.values());
        for (Order order : orders) {
            loadItems(order);
        }
        return orders;
    }
    
    @Override
    public void insert(Order order) {
        System.out.println("  [DB] INSERT INTO orders VALUES (" + order.getId() + ", '" + 
            order.getUserId() + "', " + order.getTotalAmount() + ", '" + order.getStatus() + "')");
        database.put(order.getId(), order);
        saveItems(order);
    }
    
    @Override
    public void update(Order order) {
        System.out.println("  [DB] UPDATE orders SET total=" + order.getTotalAmount() + 
            ", status='" + order.getStatus() + "' WHERE id='" + order.getId() + "'");
        database.put(order.getId(), order);
        saveItems(order);
    }
    
    @Override
    public void delete(String id) {
        System.out.println("  [DB] DELETE FROM orders WHERE id = '" + id + "'");
        database.remove(id);
        itemsDatabase.remove(id);
    }
    
    private void loadItems(Order order) {
        System.out.println("  [DB] SELECT * FROM order_items WHERE order_id = '" + order.getId() + "'");
        List<OrderItem> items = itemsDatabase.get(order.getId());
        if (items != null) {
            for (OrderItem item : items) {
                order.addItem(item);
            }
        }
    }
    
    private void saveItems(Order order) {
        System.out.println("  [DB] DELETE FROM order_items WHERE order_id = '" + order.getId() + "'");
        System.out.println("  [DB] INSERT INTO order_items (order_id, product_id, quantity, price) VALUES ...");
        itemsDatabase.put(order.getId(), new ArrayList<>(order.getItems()));
    }
    
    public List<Order> findByUserId(String userId) {
        System.out.println("  [DB] SELECT * FROM orders WHERE user_id = '" + userId + "'");
        List<Order> results = new ArrayList<>();
        for (Order order : database.values()) {
            if (order.getUserId().equals(userId)) {
                loadItems(order);
                results.add(order);
            }
        }
        return results;
    }
}

// Demo
public class DataMapperPattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    DATA MAPPER PATTERN DEMONSTRATION     ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Example 1: User Data Mapper
        System.out.println("1. USER DATA MAPPER");
        System.out.println("───────────────────");
        UserDataMapper userMapper = new UserDataMapper();
        
        System.out.println("Creating users:");
        User user1 = new User("U1", "alice", "alice@example.com", 25);
        User user2 = new User("U2", "bob", "bob@example.com", 17);
        User user3 = new User("U3", "charlie", "charlie@example.com", 30);
        
        userMapper.insert(user1);
        userMapper.insert(user2);
        userMapper.insert(user3);
        
        System.out.println("\nFinding user by ID:");
        User found = userMapper.findById("U1");
        System.out.println("Found: " + found);
        System.out.println("Display name: " + found.getDisplayName());
        System.out.println("Is adult: " + found.isAdult());
        
        System.out.println("\nFinding all adults:");
        List<User> adults = userMapper.findAdults();
        adults.forEach(u -> System.out.println("  " + u));
        
        System.out.println("\nUpdating user:");
        user1.setAge(26);
        userMapper.update(user1);
        
        // Example 2: Product Data Mapper
        System.out.println("\n2. PRODUCT DATA MAPPER (with tags)");
        System.out.println("───────────────────────────────────");
        ProductDataMapper productMapper = new ProductDataMapper();
        
        System.out.println("Creating products:");
        Product laptop = new Product("P1", "Laptop", 999.99, "Electronics");
        laptop.addTag("portable");
        laptop.addTag("work");
        productMapper.insert(laptop);
        
        Product phone = new Product("P2", "Smartphone", 699.99, "Electronics");
        phone.addTag("portable");
        phone.addTag("communication");
        productMapper.insert(phone);
        
        Product desk = new Product("P3", "Standing Desk", 399.99, "Furniture");
        desk.addTag("office");
        desk.addTag("ergonomic");
        productMapper.insert(desk);
        
        System.out.println("\nFinding products by category:");
        List<Product> electronics = productMapper.findByCategory("Electronics");
        electronics.forEach(p -> System.out.println("  " + p));
        
        System.out.println("\nProduct with tax:");
        Product foundProduct = productMapper.findById("P1");
        System.out.println("Base price: $" + foundProduct.getPrice());
        System.out.println("Price with tax: $" + String.format("%.2f", foundProduct.getPriceWithTax()));
        
        // Example 3: Order Data Mapper
        System.out.println("\n3. ORDER DATA MAPPER (with relationships)");
        System.out.println("──────────────────────────────────────────");
        OrderDataMapper orderMapper = new OrderDataMapper();
        
        System.out.println("Creating order:");
        Order order = new Order("O1", "U1");
        order.addItem(new OrderItem("P1", 1, 999.99));
        order.addItem(new OrderItem("P2", 2, 699.99));
        orderMapper.insert(order);
        
        System.out.println("\nFinding order by ID:");
        Order foundOrder = orderMapper.findById("O1");
        System.out.println("Order: " + foundOrder);
        System.out.println("Items:");
        foundOrder.getItems().forEach(item -> System.out.println("  " + item));
        
        System.out.println("\nCompleting order:");
        foundOrder.complete();
        orderMapper.update(foundOrder);
        
        System.out.println("\nFinding orders by user:");
        List<Order> userOrders = orderMapper.findByUserId("U1");
        userOrders.forEach(o -> System.out.println("  " + o));
        
        System.out.println("\n✅ Data Mapper Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Complete separation of domain and database logic");
        System.out.println("  • Domain objects are pure POJOs");
        System.out.println("  • Complex object graphs handled by mapper");
        System.out.println("  • Easy to test domain logic independently");
    }
}
