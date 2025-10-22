package architectural;

import java.util.*;

/**
 * LAYERED ARCHITECTURE PATTERN
 * 
 * Organizes system into layers where each layer provides services to the layer above
 * and uses services from the layer below. Common layers: Presentation, Business, Data.
 * 
 * Benefits:
 * - Clear separation of concerns
 * - Easy to understand and maintain
 * - Layers can be replaced independently
 * - Testability through layer isolation
 * - Reusability of layers
 * 
 * Use Cases:
 * - Enterprise applications
 * - Web applications
 * - Desktop applications
 * - Any application requiring clear separation of concerns
 */

// ============================================
// DATA ACCESS LAYER (Layer 1 - Bottom)
// ============================================

// Entity
class UserEntity {
    private String id;
    private String username;
    private String email;
    private String password;
    
    public UserEntity(String id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setEmail(String email) { this.email = email; }
}

class ProductEntity {
    private String id;
    private String name;
    private double price;
    private int stock;
    
    public ProductEntity(String id, String name, double price, int stock) {
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
}

// Data Access Layer
class UserRepository {
    private final Map<String, UserEntity> database = new HashMap<>();
    
    public UserRepository() {
        // Initialize with sample data
        database.put("U1", new UserEntity("U1", "alice", "alice@example.com", "hash123"));
        database.put("U2", new UserEntity("U2", "bob", "bob@example.com", "hash456"));
    }
    
    public UserEntity findById(String id) {
        System.out.println("  [DATA LAYER] Finding user by ID: " + id);
        return database.get(id);
    }
    
    public UserEntity findByUsername(String username) {
        System.out.println("  [DATA LAYER] Finding user by username: " + username);
        return database.values().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
    
    public void save(UserEntity user) {
        System.out.println("  [DATA LAYER] Saving user: " + user.getId());
        database.put(user.getId(), user);
    }
    
    public List<UserEntity> findAll() {
        System.out.println("  [DATA LAYER] Finding all users");
        return new ArrayList<>(database.values());
    }
}

class ProductRepository {
    private final Map<String, ProductEntity> database = new HashMap<>();
    
    public ProductRepository() {
        // Initialize with sample data
        database.put("P1", new ProductEntity("P1", "Laptop", 999.99, 10));
        database.put("P2", new ProductEntity("P2", "Mouse", 29.99, 50));
        database.put("P3", new ProductEntity("P3", "Keyboard", 79.99, 30));
    }
    
    public ProductEntity findById(String id) {
        System.out.println("  [DATA LAYER] Finding product by ID: " + id);
        return database.get(id);
    }
    
    public void save(ProductEntity product) {
        System.out.println("  [DATA LAYER] Saving product: " + product.getId());
        database.put(product.getId(), product);
    }
    
    public List<ProductEntity> findAll() {
        System.out.println("  [DATA LAYER] Finding all products");
        return new ArrayList<>(database.values());
    }
    
    public List<ProductEntity> findInStock() {
        System.out.println("  [DATA LAYER] Finding in-stock products");
        return database.values().stream()
            .filter(p -> p.getStock() > 0)
            .toList();
    }
}

// ============================================
// BUSINESS LOGIC LAYER (Layer 2 - Middle)
// ============================================

// Business Model
class User {
    private String id;
    private String username;
    private String email;
    
    public User(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return String.format("User[%s, %s, %s]", id, username, email);
    }
}

class Product {
    private String id;
    private String name;
    private double price;
    private int stock;
    
    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    
    public double getPriceWithTax() {
        return price * 1.1; // 10% tax
    }
    
    public boolean isInStock() {
        return stock > 0;
    }
    
    @Override
    public String toString() {
        return String.format("Product[%s, $%.2f, stock:%d]", name, price, stock);
    }
}

// Business Logic Layer - Services
class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User getUserById(String id) {
        System.out.println("  [BUSINESS LAYER] Getting user by ID: " + id);
        UserEntity entity = userRepository.findById(id);
        
        if (entity == null) {
            return null;
        }
        
        // Convert entity to business model (hide password)
        return new User(entity.getId(), entity.getUsername(), entity.getEmail());
    }
    
    public User authenticateUser(String username, String password) {
        System.out.println("  [BUSINESS LAYER] Authenticating user: " + username);
        UserEntity entity = userRepository.findByUsername(username);
        
        if (entity != null && entity.getPassword().equals(password)) {
            System.out.println("  [BUSINESS LAYER] Authentication successful");
            return new User(entity.getId(), entity.getUsername(), entity.getEmail());
        }
        
        System.out.println("  [BUSINESS LAYER] Authentication failed");
        return null;
    }
    
    public List<User> getAllUsers() {
        System.out.println("  [BUSINESS LAYER] Getting all users");
        return userRepository.findAll().stream()
            .map(e -> new User(e.getId(), e.getUsername(), e.getEmail()))
            .toList();
    }
    
    public boolean updateEmail(String userId, String newEmail) {
        System.out.println("  [BUSINESS LAYER] Updating email for user: " + userId);
        
        // Business validation
        if (!newEmail.contains("@")) {
            System.out.println("  [BUSINESS LAYER] Invalid email format");
            return false;
        }
        
        UserEntity entity = userRepository.findById(userId);
        if (entity != null) {
            entity.setEmail(newEmail);
            userRepository.save(entity);
            return true;
        }
        
        return false;
    }
}

class ProductService {
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Product getProductById(String id) {
        System.out.println("  [BUSINESS LAYER] Getting product by ID: " + id);
        ProductEntity entity = productRepository.findById(id);
        
        if (entity == null) {
            return null;
        }
        
        return new Product(entity.getId(), entity.getName(), 
            entity.getPrice(), entity.getStock());
    }
    
    public List<Product> getAllProducts() {
        System.out.println("  [BUSINESS LAYER] Getting all products");
        return productRepository.findAll().stream()
            .map(e -> new Product(e.getId(), e.getName(), e.getPrice(), e.getStock()))
            .toList();
    }
    
    public List<Product> getAvailableProducts() {
        System.out.println("  [BUSINESS LAYER] Getting available products");
        return productRepository.findInStock().stream()
            .map(e -> new Product(e.getId(), e.getName(), e.getPrice(), e.getStock()))
            .toList();
    }
    
    public boolean purchaseProduct(String productId, int quantity) {
        System.out.println("  [BUSINESS LAYER] Processing purchase: " + 
            productId + " x" + quantity);
        
        ProductEntity entity = productRepository.findById(productId);
        
        if (entity == null) {
            System.out.println("  [BUSINESS LAYER] Product not found");
            return false;
        }
        
        if (entity.getStock() < quantity) {
            System.out.println("  [BUSINESS LAYER] Insufficient stock");
            return false;
        }
        
        // Update stock
        entity.setStock(entity.getStock() - quantity);
        productRepository.save(entity);
        
        System.out.println("  [BUSINESS LAYER] Purchase successful");
        return true;
    }
}

class OrderService {
    private final ProductService productService;
    private final UserService userService;
    
    public OrderService(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }
    
    public boolean placeOrder(String userId, String productId, int quantity) {
        System.out.println("  [BUSINESS LAYER] Placing order for user: " + userId);
        
        // Validate user
        User user = userService.getUserById(userId);
        if (user == null) {
            System.out.println("  [BUSINESS LAYER] User not found");
            return false;
        }
        
        // Validate product
        Product product = productService.getProductById(productId);
        if (product == null) {
            System.out.println("  [BUSINESS LAYER] Product not found");
            return false;
        }
        
        // Business logic: Check if product is available
        if (!product.isInStock()) {
            System.out.println("  [BUSINESS LAYER] Product out of stock");
            return false;
        }
        
        // Calculate total
        double total = product.getPriceWithTax() * quantity;
        System.out.println("  [BUSINESS LAYER] Order total: $" + 
            String.format("%.2f", total));
        
        // Process purchase
        return productService.purchaseProduct(productId, quantity);
    }
}

// ============================================
// PRESENTATION LAYER (Layer 3 - Top)
// ============================================

class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    public void displayUser(String userId) {
        System.out.println("  [PRESENTATION LAYER] Displaying user");
        User user = userService.getUserById(userId);
        
        if (user != null) {
            System.out.println("    User Details:");
            System.out.println("    - ID: " + user.getId());
            System.out.println("    - Username: " + user.getUsername());
            System.out.println("    - Email: " + user.getEmail());
        } else {
            System.out.println("    User not found");
        }
    }
    
    public void displayAllUsers() {
        System.out.println("  [PRESENTATION LAYER] Displaying all users");
        List<User> users = userService.getAllUsers();
        
        System.out.println("    Total users: " + users.size());
        users.forEach(u -> System.out.println("    - " + u));
    }
    
    public void login(String username, String password) {
        System.out.println("  [PRESENTATION LAYER] Processing login");
        User user = userService.authenticateUser(username, password);
        
        if (user != null) {
            System.out.println("    ✅ Login successful! Welcome, " + user.getUsername());
        } else {
            System.out.println("    ❌ Login failed! Invalid credentials");
        }
    }
}

class ProductController {
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    public void displayProduct(String productId) {
        System.out.println("  [PRESENTATION LAYER] Displaying product");
        Product product = productService.getProductById(productId);
        
        if (product != null) {
            System.out.println("    Product Details:");
            System.out.println("    - Name: " + product.getName());
            System.out.println("    - Price: $" + String.format("%.2f", product.getPrice()));
            System.out.println("    - Price with tax: $" + 
                String.format("%.2f", product.getPriceWithTax()));
            System.out.println("    - Stock: " + product.getStock());
            System.out.println("    - Status: " + 
                (product.isInStock() ? "✅ In Stock" : "❌ Out of Stock"));
        } else {
            System.out.println("    Product not found");
        }
    }
    
    public void displayAvailableProducts() {
        System.out.println("  [PRESENTATION LAYER] Displaying available products");
        List<Product> products = productService.getAvailableProducts();
        
        System.out.println("    Available products:");
        products.forEach(p -> System.out.println("    - " + p));
    }
}

class OrderController {
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    public void processOrder(String userId, String productId, int quantity) {
        System.out.println("  [PRESENTATION LAYER] Processing order request");
        System.out.println("    User: " + userId);
        System.out.println("    Product: " + productId);
        System.out.println("    Quantity: " + quantity);
        
        boolean success = orderService.placeOrder(userId, productId, quantity);
        
        if (success) {
            System.out.println("    ✅ Order placed successfully!");
        } else {
            System.out.println("    ❌ Order failed!");
        }
    }
}

// Demo
public class LayeredArchitecturePattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║  LAYERED ARCHITECTURE PATTERN DEMO       ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Initialize layers from bottom to top
        System.out.println("🏗️  Initializing 3-Layer Architecture...\n");
        
        // Layer 1: Data Access Layer
        UserRepository userRepo = new UserRepository();
        ProductRepository productRepo = new ProductRepository();
        
        // Layer 2: Business Logic Layer
        UserService userService = new UserService(userRepo);
        ProductService productService = new ProductService(productRepo);
        OrderService orderService = new OrderService(productService, userService);
        
        // Layer 3: Presentation Layer
        UserController userController = new UserController(userService);
        ProductController productController = new ProductController(productService);
        OrderController orderController = new OrderController(orderService);
        
        // Example 1: User operations through all layers
        System.out.println("1. USER OPERATIONS (Through All Layers)");
        System.out.println("────────────────────────────────────────");
        userController.displayUser("U1");
        
        System.out.println("\n");
        userController.login("alice", "hash123");
        
        System.out.println("\n");
        userController.login("alice", "wrongpassword");
        
        // Example 2: Product operations
        System.out.println("\n2. PRODUCT OPERATIONS");
        System.out.println("─────────────────────");
        productController.displayProduct("P1");
        
        System.out.println("\n");
        productController.displayAvailableProducts();
        
        // Example 3: Order processing (uses multiple services)
        System.out.println("\n3. ORDER PROCESSING (Cross-Layer Interaction)");
        System.out.println("──────────────────────────────────────────────");
        orderController.processOrder("U1", "P1", 2);
        
        System.out.println("\n");
        orderController.processOrder("U2", "P2", 100); // Should fail - insufficient stock
        
        System.out.println("\n✅ Layered Architecture Pattern demonstration completed!");
        System.out.println("\n📊 Architecture Summary:");
        System.out.println("  Layer 3 (Presentation): Controllers - User interaction");
        System.out.println("  Layer 2 (Business): Services - Business logic");
        System.out.println("  Layer 1 (Data): Repositories - Data access");
        System.out.println("\n💡 Benefits Demonstrated:");
        System.out.println("  • Clear separation of concerns");
        System.out.println("  • Each layer has distinct responsibility");
        System.out.println("  • Easy to test layers independently");
        System.out.println("  • Layers can be replaced without affecting others");
    }
}
