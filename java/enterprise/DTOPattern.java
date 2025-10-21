package enterprise;

import java.util.*;

/**
 * Data Transfer Object (DTO) Pattern
 * Transfers data between layers/processes with plain objects.
 */
public class DTOPattern {
    
    // Domain Model (Rich object with business logic)
    static class User {
        private String id;
        private String username;
        private String password; // Should never be exposed
        private String email;
        private Date createdAt;
        private Date lastLogin;
        private boolean active;
        private Set<String> roles;
        
        public User(String id, String username, String password, String email) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.email = email;
            this.createdAt = new Date();
            this.active = true;
            this.roles = new HashSet<>();
        }
        
        // Business logic
        public void login() {
            this.lastLogin = new Date();
            System.out.println("User " + username + " logged in");
        }
        
        public void addRole(String role) {
            roles.add(role);
        }
        
        public boolean hasRole(String role) {
            return roles.contains(role);
        }
        
        // Getters
        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public Date getCreatedAt() { return createdAt; }
        public Date getLastLogin() { return lastLogin; }
        public boolean isActive() { return active; }
        public Set<String> getRoles() { return new HashSet<>(roles); }
    }
    
    // DTO - Simple data container (no business logic)
    static class UserDTO {
        private String id;
        private String username;
        private String email;
        private String createdAt;
        private boolean active;
        
        // Constructor
        public UserDTO(String id, String username, String email, 
                      String createdAt, boolean active) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.createdAt = createdAt;
            this.active = active;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        @Override
        public String toString() {
            return "UserDTO{id='" + id + "', username='" + username + 
                   "', email='" + email + "', createdAt='" + createdAt + 
                   "', active=" + active + "}";
        }
    }
    
    // Mapper to convert between Domain and DTO
    static class UserMapper {
        public static UserDTO toDTO(User user) {
            return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt().toString(),
                user.isActive()
            );
        }
        
        public static List<UserDTO> toDTOList(List<User> users) {
            List<UserDTO> dtos = new ArrayList<>();
            for (User user : users) {
                dtos.add(toDTO(user));
            }
            return dtos;
        }
    }
    
    // Product domain and DTO
    static class Product {
        private String id;
        private String name;
        private String description;
        private double price;
        private int stockQuantity;
        private String internalNotes; // Should not be exposed
        private double costPrice; // Should not be exposed
        
        public Product(String id, String name, String description, double price, 
                      int stockQuantity, double costPrice) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.stockQuantity = stockQuantity;
            this.costPrice = costPrice;
        }
        
        public double calculateProfit() {
            return price - costPrice;
        }
        
        public boolean isInStock() {
            return stockQuantity > 0;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public int getStockQuantity() { return stockQuantity; }
    }
    
    static class ProductDTO {
        private String id;
        private String name;
        private String description;
        private double price;
        private boolean available;
        
        public ProductDTO(String id, String name, String description, 
                         double price, boolean available) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.available = available;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public boolean isAvailable() { return available; }
        
        @Override
        public String toString() {
            return "ProductDTO{id='" + id + "', name='" + name + 
                   "', price=$" + price + ", available=" + available + "}";
        }
    }
    
    static class ProductMapper {
        public static ProductDTO toDTO(Product product) {
            return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isInStock()
            );
        }
    }
    
    // Order with nested DTOs
    static class OrderDTO {
        private String orderId;
        private UserDTO customer;
        private List<ProductDTO> products;
        private double totalAmount;
        private String status;
        
        public OrderDTO(String orderId, UserDTO customer, List<ProductDTO> products, 
                       double totalAmount, String status) {
            this.orderId = orderId;
            this.customer = customer;
            this.products = products;
            this.totalAmount = totalAmount;
            this.status = status;
        }
        
        public String getOrderId() { return orderId; }
        public UserDTO getCustomer() { return customer; }
        public List<ProductDTO> getProducts() { return products; }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        
        @Override
        public String toString() {
            return "OrderDTO{orderId='" + orderId + "', customer=" + 
                   customer.getUsername() + ", products=" + products.size() + 
                   ", total=$" + totalAmount + ", status='" + status + "'}";
        }
    }
    
    // Service layer using DTOs
    static class UserService {
        private List<User> users = new ArrayList<>();
        
        public UserService() {
            // Initialize with some users
            User user1 = new User("1", "alice", "secret123", "alice@example.com");
            user1.addRole("USER");
            users.add(user1);
            
            User user2 = new User("2", "bob", "password456", "bob@example.com");
            user2.addRole("USER");
            user2.addRole("ADMIN");
            users.add(user2);
        }
        
        public List<UserDTO> getAllUsers() {
            System.out.println("📤 Fetching all users as DTOs");
            return UserMapper.toDTOList(users);
        }
        
        public UserDTO getUserById(String id) {
            System.out.println("📤 Fetching user " + id + " as DTO");
            for (User user : users) {
                if (user.getId().equals(id)) {
                    return UserMapper.toDTO(user);
                }
            }
            return null;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Data Transfer Object (DTO) Pattern Demo ===\n");
        
        // 1. Basic DTO conversion
        System.out.println("1. Domain Model to DTO Conversion:");
        User user = new User("U1", "johndoe", "secretpass", "john@example.com");
        user.addRole("USER");
        user.addRole("EDITOR");
        user.login();
        
        System.out.println("\n🏢 Domain Model (internal use):");
        System.out.println("  ID: " + user.getId());
        System.out.println("  Username: " + user.getUsername());
        System.out.println("  Email: " + user.getEmail());
        System.out.println("  Roles: " + user.getRoles());
        System.out.println("  Last Login: " + user.getLastLogin());
        
        UserDTO userDTO = UserMapper.toDTO(user);
        
        System.out.println("\n📦 DTO (for API/transfer):");
        System.out.println("  " + userDTO);
        System.out.println("  ⚠️  Note: Password and roles are NOT exposed!");
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. Product DTO
        System.out.println("\n2. Product DTO (hiding internal data):");
        Product product = new Product("P1", "Laptop", "High-performance laptop", 
                                     999.99, 10, 600.00);
        
        System.out.println("\n🏢 Domain Model (internal):");
        System.out.println("  Name: " + product.getName());
        System.out.println("  Price: $" + product.getPrice());
        System.out.println("  Cost: $600.00 (internal only)");
        System.out.println("  Profit: $" + product.calculateProfit());
        System.out.println("  Stock: " + product.getStockQuantity());
        
        ProductDTO productDTO = ProductMapper.toDTO(product);
        
        System.out.println("\n📦 DTO (for customers):");
        System.out.println("  " + productDTO);
        System.out.println("  ⚠️  Note: Cost price and stock quantity are hidden!");
        
        System.out.println("\n" + "=".repeat(50));
        
        // 3. Service layer with DTOs
        System.out.println("\n3. Service Layer Using DTOs:");
        UserService userService = new UserService();
        
        List<UserDTO> allUsers = userService.getAllUsers();
        System.out.println("\nAll users:");
        for (UserDTO dto : allUsers) {
            System.out.println("  " + dto);
        }
        
        System.out.println();
        UserDTO singleUser = userService.getUserById("1");
        System.out.println("Single user: " + singleUser);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 4. Complex nested DTO
        System.out.println("\n4. Nested DTOs (Order with User and Products):");
        
        UserDTO customer = new UserDTO("C1", "jane", "jane@example.com", 
                                      "2024-01-01", true);
        
        List<ProductDTO> orderProducts = Arrays.asList(
            new ProductDTO("P1", "Laptop", "Gaming laptop", 1299.99, true),
            new ProductDTO("P2", "Mouse", "Wireless mouse", 29.99, true)
        );
        
        OrderDTO order = new OrderDTO("ORD-001", customer, orderProducts, 
                                     1329.98, "SHIPPED");
        
        System.out.println(order);
        System.out.println("\nOrder details:");
        System.out.println("  Customer: " + order.getCustomer().getUsername());
        System.out.println("  Products: " + order.getProducts().size() + " items");
        for (ProductDTO p : order.getProducts()) {
            System.out.println("    - " + p.getName() + " ($" + p.getPrice() + ")");
        }
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Hides internal implementation details");
        System.out.println("✓ Reduces network payload");
        System.out.println("✓ Version compatibility (API evolution)");
        System.out.println("✓ Security (no sensitive data exposure)");
        System.out.println("✓ Decouples layers");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• REST API responses");
        System.out.println("• Inter-service communication");
        System.out.println("• Database to UI layer");
        System.out.println("• Remote procedure calls (RPC)");
        System.out.println("• Serialization boundaries");
    }
}
