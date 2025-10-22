package enterprise;

import java.util.*;

/**
 * ACTIVE RECORD PATTERN
 * 
 * An object that wraps a row in a database table, encapsulates database access,
 * and adds domain logic on that data. Each Active Record instance corresponds
 * to a single row in the database.
 * 
 * Benefits:
 * - Simple and intuitive for CRUD operations
 * - Objects handle their own persistence
 * - Easy to understand for simple domains
 * - Less code for basic scenarios
 * - Direct object-to-database mapping
 * 
 * Use Cases:
 * - Simple domain models
 * - CRUD-heavy applications
 * - Rapid prototyping
 * - Applications with straightforward persistence needs
 * - Small to medium-sized projects
 */

// Base Active Record class
abstract class ActiveRecord {
    protected boolean isNew = true;
    
    public abstract void save();
    public abstract void delete();
    public abstract void load(String id);
    
    protected void markAsExisting() {
        isNew = false;
    }
    
    protected boolean isNew() {
        return isNew;
    }
}

// Example 1: User Active Record
class User extends ActiveRecord {
    private String id;
    private String username;
    private String email;
    private int age;
    
    // Simulated database
    private static final Map<String, User> database = new HashMap<>();
    
    public User() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }
    
    public User(String username, String email, int age) {
        this();
        this.username = username;
        this.email = email;
        this.age = age;
    }
    
    @Override
    public void save() {
        if (isNew()) {
            System.out.println("  [DB] INSERT INTO users (id, username, email, age) VALUES ('" +
                id + "', '" + username + "', '" + email + "', " + age + ")");
            database.put(id, this);
            markAsExisting();
        } else {
            System.out.println("  [DB] UPDATE users SET username='" + username +
                "', email='" + email + "', age=" + age + " WHERE id='" + id + "'");
            database.put(id, this);
        }
    }
    
    @Override
    public void delete() {
        System.out.println("  [DB] DELETE FROM users WHERE id='" + id + "'");
        database.remove(id);
    }
    
    @Override
    public void load(String id) {
        System.out.println("  [DB] SELECT * FROM users WHERE id='" + id + "'");
        User user = database.get(id);
        if (user != null) {
            this.id = user.id;
            this.username = user.username;
            this.email = user.email;
            this.age = user.age;
            markAsExisting();
        }
    }
    
    // Static finder methods
    public static User find(String id) {
        System.out.println("  [DB] SELECT * FROM users WHERE id='" + id + "'");
        User user = database.get(id);
        if (user != null) {
            user.markAsExisting();
        }
        return user;
    }
    
    public static List<User> findAll() {
        System.out.println("  [DB] SELECT * FROM users");
        return new ArrayList<>(database.values());
    }
    
    public static List<User> findByAge(int age) {
        System.out.println("  [DB] SELECT * FROM users WHERE age=" + age);
        List<User> results = new ArrayList<>();
        for (User user : database.values()) {
            if (user.age == age) {
                results.add(user);
            }
        }
        return results;
    }
    
    public static User findByUsername(String username) {
        System.out.println("  [DB] SELECT * FROM users WHERE username='" + username + "'");
        for (User user : database.values()) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    // Domain logic
    public boolean isAdult() {
        return age >= 18;
    }
    
    public void updateEmail(String newEmail) {
        this.email = newEmail;
        save(); // Automatic persistence
    }
    
    // Getters and setters
    public String getId() { return id; }
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

// Example 2: Product Active Record with Associations
class Product extends ActiveRecord {
    private String id;
    private String name;
    private double price;
    private int stock;
    private String categoryId;
    
    private static final Map<String, Product> database = new HashMap<>();
    
    public Product() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }
    
    public Product(String name, double price, int stock, String categoryId) {
        this();
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
    }
    
    @Override
    public void save() {
        if (isNew()) {
            System.out.println("  [DB] INSERT INTO products VALUES ('" + id + "', '" +
                name + "', " + price + ", " + stock + ", '" + categoryId + "')");
            database.put(id, this);
            markAsExisting();
        } else {
            System.out.println("  [DB] UPDATE products SET name='" + name +
                "', price=" + price + ", stock=" + stock + ", category_id='" +
                categoryId + "' WHERE id='" + id + "'");
            database.put(id, this);
        }
    }
    
    @Override
    public void delete() {
        System.out.println("  [DB] DELETE FROM products WHERE id='" + id + "'");
        database.remove(id);
    }
    
    @Override
    public void load(String id) {
        System.out.println("  [DB] SELECT * FROM products WHERE id='" + id + "'");
        Product product = database.get(id);
        if (product != null) {
            this.id = product.id;
            this.name = product.name;
            this.price = product.price;
            this.stock = product.stock;
            this.categoryId = product.categoryId;
            markAsExisting();
        }
    }
    
    // Static finder methods
    public static Product find(String id) {
        Product product = database.get(id);
        if (product != null) {
            product.markAsExisting();
        }
        return product;
    }
    
    public static List<Product> findAll() {
        System.out.println("  [DB] SELECT * FROM products");
        return new ArrayList<>(database.values());
    }
    
    public static List<Product> findByCategory(String categoryId) {
        System.out.println("  [DB] SELECT * FROM products WHERE category_id='" + categoryId + "'");
        List<Product> results = new ArrayList<>();
        for (Product product : database.values()) {
            if (product.categoryId.equals(categoryId)) {
                results.add(product);
            }
        }
        return results;
    }
    
    public static List<Product> findInStock() {
        System.out.println("  [DB] SELECT * FROM products WHERE stock > 0");
        List<Product> results = new ArrayList<>();
        for (Product product : database.values()) {
            if (product.stock > 0) {
                results.add(product);
            }
        }
        return results;
    }
    
    // Domain logic
    public boolean isInStock() {
        return stock > 0;
    }
    
    public boolean decreaseStock(int quantity) {
        if (stock >= quantity) {
            stock -= quantity;
            save();
            return true;
        }
        return false;
    }
    
    public void increaseStock(int quantity) {
        stock += quantity;
        save();
    }
    
    public void updatePrice(double newPrice) {
        this.price = newPrice;
        save();
    }
    
    public double getPriceWithTax() {
        return price * 1.1; // 10% tax
    }
    
    // Association method
    public Category getCategory() {
        return Category.find(categoryId);
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategoryId() { return categoryId; }
    
    @Override
    public String toString() {
        return String.format("Product[id=%s, name=%s, price=$%.2f, stock=%d, category=%s]",
            id, name, price, stock, categoryId);
    }
}

// Example 3: Category Active Record
class Category extends ActiveRecord {
    private String id;
    private String name;
    private String description;
    
    private static final Map<String, Category> database = new HashMap<>();
    
    public Category() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }
    
    public Category(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    @Override
    public void save() {
        if (isNew()) {
            System.out.println("  [DB] INSERT INTO categories VALUES ('" + id +
                "', '" + name + "', '" + description + "')");
            database.put(id, this);
            markAsExisting();
        } else {
            System.out.println("  [DB] UPDATE categories SET name='" + name +
                "', description='" + description + "' WHERE id='" + id + "'");
            database.put(id, this);
        }
    }
    
    @Override
    public void delete() {
        System.out.println("  [DB] DELETE FROM categories WHERE id='" + id + "'");
        database.remove(id);
    }
    
    @Override
    public void load(String id) {
        Category category = database.get(id);
        if (category != null) {
            this.id = category.id;
            this.name = category.name;
            this.description = category.description;
            markAsExisting();
        }
    }
    
    public static Category find(String id) {
        Category category = database.get(id);
        if (category != null) {
            category.markAsExisting();
        }
        return category;
    }
    
    public static List<Category> findAll() {
        System.out.println("  [DB] SELECT * FROM categories");
        return new ArrayList<>(database.values());
    }
    
    // Association method
    public List<Product> getProducts() {
        return Product.findByCategory(id);
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    
    @Override
    public String toString() {
        return String.format("Category[id=%s, name=%s, description=%s]",
            id, name, description);
    }
}

// Demo
public class ActiveRecordPattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   ACTIVE RECORD PATTERN DEMONSTRATION    ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Example 1: User CRUD Operations
        System.out.println("1. USER ACTIVE RECORD - CRUD OPERATIONS");
        System.out.println("────────────────────────────────────────");
        
        System.out.println("Creating users:");
        User alice = new User("alice", "alice@example.com", 25);
        alice.save();
        
        User bob = new User("bob", "bob@example.com", 17);
        bob.save();
        
        User charlie = new User("charlie", "charlie@example.com", 30);
        charlie.save();
        
        System.out.println("\nFinding user by ID:");
        User found = User.find(alice.getId());
        System.out.println("Found: " + found);
        System.out.println("Is adult: " + found.isAdult());
        
        System.out.println("\nUpdating user:");
        alice.setAge(26);
        alice.updateEmail("alice.new@example.com");
        
        System.out.println("\nFinding all users:");
        List<User> allUsers = User.findAll();
        allUsers.forEach(u -> System.out.println("  " + u));
        
        System.out.println("\nFinding by username:");
        User foundByName = User.findByUsername("bob");
        System.out.println("Found: " + foundByName);
        
        // Example 2: Product with Category Association
        System.out.println("\n2. PRODUCT WITH CATEGORY ASSOCIATION");
        System.out.println("────────────────────────────────────");
        
        System.out.println("Creating categories:");
        Category electronics = new Category("Electronics", "Electronic devices");
        electronics.save();
        
        Category furniture = new Category("Furniture", "Home and office furniture");
        furniture.save();
        
        System.out.println("\nCreating products:");
        Product laptop = new Product("Laptop", 999.99, 10, electronics.getId());
        laptop.save();
        
        Product phone = new Product("Smartphone", 699.99, 25, electronics.getId());
        phone.save();
        
        Product desk = new Product("Standing Desk", 399.99, 5, furniture.getId());
        desk.save();
        
        System.out.println("\nFinding products by category:");
        List<Product> electronicProducts = Product.findByCategory(electronics.getId());
        electronicProducts.forEach(p -> System.out.println("  " + p));
        
        System.out.println("\nUsing association:");
        System.out.println("Laptop category: " + laptop.getCategory());
        System.out.println("Electronics products: " + electronics.getProducts().size());
        
        // Example 3: Business Logic with Persistence
        System.out.println("\n3. BUSINESS LOGIC WITH AUTO-PERSISTENCE");
        System.out.println("────────────────────────────────────────");
        
        System.out.println("Current laptop stock: " + laptop.getStock());
        System.out.println("Decreasing stock by 3:");
        boolean success = laptop.decreaseStock(3);
        System.out.println("Success: " + success);
        System.out.println("New stock: " + laptop.getStock());
        
        System.out.println("\nUpdating price:");
        laptop.updatePrice(899.99);
        System.out.println("Price with tax: $" + String.format("%.2f", laptop.getPriceWithTax()));
        
        System.out.println("\nFinding in-stock products:");
        List<Product> inStock = Product.findInStock();
        inStock.forEach(p -> System.out.println("  " + p));
        
        // Example 4: Deleting Records
        System.out.println("\n4. DELETING RECORDS");
        System.out.println("───────────────────");
        
        System.out.println("Deleting bob:");
        bob.delete();
        
        System.out.println("\nRemaining users:");
        User.findAll().forEach(u -> System.out.println("  " + u));
        
        System.out.println("\n✅ Active Record Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Simple CRUD operations");
        System.out.println("  • Objects handle their own persistence");
        System.out.println("  • Static finder methods for querying");
        System.out.println("  • Domain logic integrated with persistence");
        System.out.println("  • Associations between records");
    }
}
