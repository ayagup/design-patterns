package enterprise;

import java.util.*;

/**
 * Service Layer Pattern
 * Defines application's boundary and encapsulates business operations.
 */
public class ServiceLayerPattern {
    
    // Domain Model
    static class User {
        private String id;
        private String username;
        private String email;
        private boolean active;
        
        public User(String id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.active = true;
        }
        
        public String getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        @Override
        public String toString() {
            return "User{id='" + id + "', username='" + username + 
                   "', email='" + email + "', active=" + active + "}";
        }
    }
    
    static class Order {
        private String id;
        private String userId;
        private List<OrderItem> items;
        private String status;
        private double total;
        
        public Order(String id, String userId) {
            this.id = id;
            this.userId = userId;
            this.items = new ArrayList<>();
            this.status = "PENDING";
            this.total = 0.0;
        }
        
        public String getId() { return id; }
        public String getUserId() { return userId; }
        public List<OrderItem> getItems() { return items; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getTotal() { return total; }
        
        public void addItem(OrderItem item) {
            items.add(item);
            total += item.getPrice() * item.getQuantity();
        }
        
        @Override
        public String toString() {
            return "Order{id='" + id + "', userId='" + userId + 
                   "', items=" + items.size() + ", total=$" + total + 
                   ", status='" + status + "'}";
        }
    }
    
    static class OrderItem {
        private String productId;
        private String productName;
        private double price;
        private int quantity;
        
        public OrderItem(String productId, String productName, double price, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }
    
    // Repository Layer (Data Access)
    static class UserRepository {
        private Map<String, User> users = new HashMap<>();
        
        public void save(User user) {
            users.put(user.getId(), user);
            System.out.println("  [Repository] Saved user: " + user.getId());
        }
        
        public User findById(String id) {
            System.out.println("  [Repository] Finding user: " + id);
            return users.get(id);
        }
        
        public User findByEmail(String email) {
            System.out.println("  [Repository] Finding user by email: " + email);
            return users.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
        }
    }
    
    static class OrderRepository {
        private Map<String, Order> orders = new HashMap<>();
        
        public void save(Order order) {
            orders.put(order.getId(), order);
            System.out.println("  [Repository] Saved order: " + order.getId());
        }
        
        public Order findById(String id) {
            System.out.println("  [Repository] Finding order: " + id);
            return orders.get(id);
        }
        
        public List<Order> findByUserId(String userId) {
            System.out.println("  [Repository] Finding orders for user: " + userId);
            List<Order> result = new ArrayList<>();
            for (Order order : orders.values()) {
                if (order.getUserId().equals(userId)) {
                    result.add(order);
                }
            }
            return result;
        }
    }
    
    // Service Layer (Business Logic)
    static class UserService {
        private final UserRepository userRepository;
        
        public UserService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
        
        public String registerUser(String username, String email) {
            System.out.println("\n[UserService] Registering user: " + username);
            
            // Business validation
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }
            
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format");
            }
            
            // Check if email already exists
            User existing = userRepository.findByEmail(email);
            if (existing != null) {
                throw new IllegalArgumentException("Email already registered");
            }
            
            // Create and save user
            String userId = "U-" + UUID.randomUUID().toString().substring(0, 8);
            User user = new User(userId, username, email);
            userRepository.save(user);
            
            System.out.println("✅ User registered successfully: " + userId);
            return userId;
        }
        
        public User getUserById(String userId) {
            System.out.println("\n[UserService] Getting user: " + userId);
            User user = userRepository.findById(userId);
            
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId);
            }
            
            return user;
        }
        
        public void deactivateUser(String userId) {
            System.out.println("\n[UserService] Deactivating user: " + userId);
            User user = getUserById(userId);
            user.setActive(false);
            userRepository.save(user);
            System.out.println("✅ User deactivated");
        }
        
        private boolean isValidEmail(String email) {
            return email != null && email.contains("@") && email.contains(".");
        }
    }
    
    static class OrderService {
        private final OrderRepository orderRepository;
        private final UserRepository userRepository;
        
        public OrderService(OrderRepository orderRepository, UserRepository userRepository) {
            this.orderRepository = orderRepository;
            this.userRepository = userRepository;
        }
        
        public String createOrder(String userId, List<OrderItem> items) {
            System.out.println("\n[OrderService] Creating order for user: " + userId);
            
            // Business validation
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found: " + userId);
            }
            
            if (!user.isActive()) {
                throw new IllegalArgumentException("User account is deactivated");
            }
            
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Order must have at least one item");
            }
            
            // Create order
            String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
            Order order = new Order(orderId, userId);
            
            for (OrderItem item : items) {
                order.addItem(item);
            }
            
            orderRepository.save(order);
            System.out.println("✅ Order created: " + orderId + " (Total: $" + order.getTotal() + ")");
            
            return orderId;
        }
        
        public void processOrder(String orderId) {
            System.out.println("\n[OrderService] Processing order: " + orderId);
            
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            
            if (!order.getStatus().equals("PENDING")) {
                throw new IllegalStateException("Order already processed");
            }
            
            // Business logic for processing
            order.setStatus("PROCESSING");
            orderRepository.save(order);
            
            System.out.println("✅ Order processing initiated");
        }
        
        public void completeOrder(String orderId) {
            System.out.println("\n[OrderService] Completing order: " + orderId);
            
            Order order = orderRepository.findById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("Order not found: " + orderId);
            }
            
            order.setStatus("COMPLETED");
            orderRepository.save(order);
            
            System.out.println("✅ Order completed");
        }
        
        public List<Order> getUserOrders(String userId) {
            System.out.println("\n[OrderService] Getting orders for user: " + userId);
            return orderRepository.findByUserId(userId);
        }
        
        public double getUserTotalSpent(String userId) {
            System.out.println("\n[OrderService] Calculating total spent for user: " + userId);
            List<Order> orders = orderRepository.findByUserId(userId);
            
            double total = orders.stream()
                .filter(o -> o.getStatus().equals("COMPLETED"))
                .mapToDouble(Order::getTotal)
                .sum();
            
            System.out.println("  Total spent: $" + total);
            return total;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Service Layer Pattern Demo ===\n");
        
        // Setup
        UserRepository userRepo = new UserRepository();
        OrderRepository orderRepo = new OrderRepository();
        
        UserService userService = new UserService(userRepo);
        OrderService orderService = new OrderService(orderRepo, userRepo);
        
        // 1. User Registration
        System.out.println("1. User Registration:");
        String userId1 = userService.registerUser("alice", "alice@example.com");
        String userId2 = userService.registerUser("bob", "bob@example.com");
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. Create Orders
        System.out.println("\n2. Creating Orders:");
        
        List<OrderItem> items1 = Arrays.asList(
            new OrderItem("P1", "Laptop", 999.99, 1),
            new OrderItem("P2", "Mouse", 29.99, 2)
        );
        String orderId1 = orderService.createOrder(userId1, items1);
        
        List<OrderItem> items2 = Arrays.asList(
            new OrderItem("P3", "Keyboard", 79.99, 1)
        );
        String orderId2 = orderService.createOrder(userId2, items2);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 3. Process Orders
        System.out.println("\n3. Processing Orders:");
        orderService.processOrder(orderId1);
        orderService.completeOrder(orderId1);
        
        orderService.processOrder(orderId2);
        orderService.completeOrder(orderId2);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 4. Get User Orders
        System.out.println("\n4. Retrieving User Orders:");
        List<Order> aliceOrders = orderService.getUserOrders(userId1);
        System.out.println("Alice's orders: " + aliceOrders.size());
        for (Order order : aliceOrders) {
            System.out.println("  " + order);
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // 5. Calculate Total Spent
        System.out.println("\n5. User Statistics:");
        double aliceTotal = orderService.getUserTotalSpent(userId1);
        System.out.println("Alice total spent: $" + aliceTotal);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 6. Error Handling
        System.out.println("\n6. Business Rule Validation:");
        try {
            userService.registerUser("charlie", "invalid-email");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Validation failed: " + e.getMessage());
        }
        
        try {
            userService.registerUser("alice2", "alice@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Business rule violated: " + e.getMessage());
        }
        
        System.out.println("\n--- Service Layer Architecture ---");
        System.out.println("┌─────────────────────────┐");
        System.out.println("│   Presentation Layer   │ (Controllers, Views)");
        System.out.println("└───────────┬─────────────┘");
        System.out.println("            │");
        System.out.println("┌───────────▼─────────────┐");
        System.out.println("│    Service Layer       │ (Business Logic)");
        System.out.println("│ - UserService          │");
        System.out.println("│ - OrderService         │");
        System.out.println("└───────────┬─────────────┘");
        System.out.println("            │");
        System.out.println("┌───────────▼─────────────┐");
        System.out.println("│   Repository Layer     │ (Data Access)");
        System.out.println("│ - UserRepository       │");
        System.out.println("│ - OrderRepository      │");
        System.out.println("└─────────────────────────┘");
        
        System.out.println("\n--- Service Layer Responsibilities ---");
        System.out.println("✓ Business logic encapsulation");
        System.out.println("✓ Transaction management");
        System.out.println("✓ Business rule validation");
        System.out.println("✓ Coordination of multiple repositories");
        System.out.println("✓ Application boundary definition");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Clear separation of concerns");
        System.out.println("✓ Reusable business logic");
        System.out.println("✓ Easy to test");
        System.out.println("✓ Consistent transaction boundaries");
        System.out.println("✓ Reduces code duplication");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Enterprise applications");
        System.out.println("• Multi-layered architectures");
        System.out.println("• Domain-Driven Design");
        System.out.println("• RESTful API backends");
    }
}
