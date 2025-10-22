package architectural;

import java.util.*;

/**
 * Hexagonal Architecture Pattern (Ports and Adapters)
 * =====================================================
 * 
 * Intent:
 * Creates a loosely coupled application architecture where the core business
 * logic is isolated from external concerns (UI, database, external services).
 * All interactions go through ports (interfaces) and adapters (implementations).
 * 
 * Also Known As:
 * - Ports and Adapters
 * - Onion Architecture (similar concept)
 * - Clean Architecture (similar concept)
 * 
 * Motivation:
 * - Isolate core business logic from external dependencies
 * - Make application testable without external systems
 * - Allow swapping implementations without changing core logic
 * - Enable multiple adapters for same port (e.g., REST and CLI)
 * 
 * Applicability:
 * - Domain-driven design applications
 * - Applications that need to support multiple interfaces
 * - Systems requiring high testability
 * - Applications with complex business logic
 * 
 * Structure:
 * Domain (Core) <- Ports (interfaces) -> Adapters (implementations)
 * 
 * Participants:
 * - Domain/Core: Pure business logic
 * - Ports: Interfaces defining interactions
 * - Adapters: Concrete implementations (DB, REST, CLI, etc.)
 * 
 * Key Concepts:
 * 1. Primary (Driving) Ports: Called BY external systems TO use the application
 * 2. Secondary (Driven) Ports: Called BY application TO use external systems
 * 3. Adapters: Implement ports to connect external systems
 */

// ============================================================================
// DOMAIN LAYER (Core Business Logic)
// ============================================================================

// Domain model
class Order {
    private String id;
    private String customerId;
    private List<OrderItem> items;
    private OrderStatus status;
    private double totalAmount;
    
    public Order(String id, String customerId) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0.0;
    }
    
    public void addItem(String productId, int quantity, double price) {
        items.add(new OrderItem(productId, quantity, price));
        recalculateTotal();
    }
    
    public void confirm() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot confirm empty order");
        }
        status = OrderStatus.CONFIRMED;
    }
    
    public void ship() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Can only ship confirmed orders");
        }
        status = OrderStatus.SHIPPED;
    }
    
    private void recalculateTotal() {
        totalAmount = items.stream()
                          .mapToDouble(item -> item.getQuantity() * item.getPrice())
                          .sum();
    }
    
    // Getters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public OrderStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
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
}

enum OrderStatus {
    PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

// ============================================================================
// PORTS (Interfaces)
// ============================================================================

// Primary Port: Driving the application (use cases)
interface OrderService {
    String createOrder(String customerId);
    void addItemToOrder(String orderId, String productId, int quantity, double price);
    void confirmOrder(String orderId);
    void shipOrder(String orderId);
    Order getOrder(String orderId);
}

// Secondary Port: Driven by application (dependencies)
interface OrderRepository {
    void save(Order order);
    Order findById(String id);
    List<Order> findByCustomerId(String customerId);
}

// Secondary Port: Notification service
interface NotificationPort {
    void sendOrderConfirmation(String customerId, String orderId, double amount);
    void sendShipmentNotification(String customerId, String orderId);
}

// Secondary Port: Payment service
interface PaymentPort {
    boolean processPayment(String customerId, double amount);
}

// ============================================================================
// DOMAIN SERVICE (Business Logic Implementation)
// ============================================================================

class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final NotificationPort notificationPort;
    private final PaymentPort paymentPort;
    private int orderCounter = 1000;
    
    // Dependencies injected through constructor (dependency inversion)
    public OrderServiceImpl(OrderRepository orderRepository, 
                           NotificationPort notificationPort,
                           PaymentPort paymentPort) {
        this.orderRepository = orderRepository;
        this.notificationPort = notificationPort;
        this.paymentPort = paymentPort;
    }
    
    @Override
    public String createOrder(String customerId) {
        String orderId = "ORD-" + (orderCounter++);
        Order order = new Order(orderId, customerId);
        orderRepository.save(order);
        System.out.println("Order created: " + orderId);
        return orderId;
    }
    
    @Override
    public void addItemToOrder(String orderId, String productId, int quantity, double price) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        order.addItem(productId, quantity, price);
        orderRepository.save(order);
        System.out.println("Item added to order " + orderId + ": " + productId + " x" + quantity);
    }
    
    @Override
    public void confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        // Process payment
        boolean paymentSuccess = paymentPort.processPayment(
            order.getCustomerId(), 
            order.getTotalAmount()
        );
        
        if (!paymentSuccess) {
            throw new RuntimeException("Payment failed for order: " + orderId);
        }
        
        order.confirm();
        orderRepository.save(order);
        
        // Send notification
        notificationPort.sendOrderConfirmation(
            order.getCustomerId(), 
            orderId, 
            order.getTotalAmount()
        );
        
        System.out.println("Order confirmed: " + orderId);
    }
    
    @Override
    public void shipOrder(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        order.ship();
        orderRepository.save(order);
        
        // Send notification
        notificationPort.sendShipmentNotification(order.getCustomerId(), orderId);
        
        System.out.println("Order shipped: " + orderId);
    }
    
    @Override
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }
}

// ============================================================================
// ADAPTERS (Infrastructure Implementations)
// ============================================================================

// Adapter: In-Memory Repository
class InMemoryOrderRepository implements OrderRepository {
    private Map<String, Order> orders = new HashMap<>();
    
    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
        System.out.println("[In-Memory DB] Saved order: " + order.getId());
    }
    
    @Override
    public Order findById(String id) {
        return orders.get(id);
    }
    
    @Override
    public List<Order> findByCustomerId(String customerId) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getCustomerId().equals(customerId)) {
                result.add(order);
            }
        }
        return result;
    }
}

// Adapter: PostgreSQL Repository (simulated)
class PostgreSQLOrderRepository implements OrderRepository {
    private Map<String, Order> storage = new HashMap<>(); // Simulates database
    
    @Override
    public void save(Order order) {
        storage.put(order.getId(), order);
        System.out.println("[PostgreSQL] INSERT INTO orders VALUES (" + order.getId() + ", ...)");
    }
    
    @Override
    public Order findById(String id) {
        System.out.println("[PostgreSQL] SELECT * FROM orders WHERE id = '" + id + "'");
        return storage.get(id);
    }
    
    @Override
    public List<Order> findByCustomerId(String customerId) {
        System.out.println("[PostgreSQL] SELECT * FROM orders WHERE customer_id = '" + customerId + "'");
        List<Order> result = new ArrayList<>();
        for (Order order : storage.values()) {
            if (order.getCustomerId().equals(customerId)) {
                result.add(order);
            }
        }
        return result;
    }
}

// Adapter: Email Notification
class EmailNotificationAdapter implements NotificationPort {
    @Override
    public void sendOrderConfirmation(String customerId, String orderId, double amount) {
        System.out.println("[Email] To: customer-" + customerId + "@example.com");
        System.out.println("[Email] Subject: Order Confirmation - " + orderId);
        System.out.println("[Email] Your order of $" + String.format("%.2f", amount) + 
                         " has been confirmed!");
    }
    
    @Override
    public void sendShipmentNotification(String customerId, String orderId) {
        System.out.println("[Email] To: customer-" + customerId + "@example.com");
        System.out.println("[Email] Subject: Order Shipped - " + orderId);
        System.out.println("[Email] Your order has been shipped!");
    }
}

// Adapter: SMS Notification
class SMSNotificationAdapter implements NotificationPort {
    @Override
    public void sendOrderConfirmation(String customerId, String orderId, double amount) {
        System.out.println("[SMS] To: +1-555-" + customerId);
        System.out.println("[SMS] Order " + orderId + " confirmed. Total: $" + 
                         String.format("%.2f", amount));
    }
    
    @Override
    public void sendShipmentNotification(String customerId, String orderId) {
        System.out.println("[SMS] To: +1-555-" + customerId);
        System.out.println("[SMS] Order " + orderId + " shipped!");
    }
}

// Adapter: Payment Service
class StripePaymentAdapter implements PaymentPort {
    @Override
    public boolean processPayment(String customerId, double amount) {
        System.out.println("[Stripe] Processing payment for customer " + customerId);
        System.out.println("[Stripe] Amount: $" + String.format("%.2f", amount));
        System.out.println("[Stripe] Payment successful!");
        return true; // Simplified - always succeeds
    }
}

class PayPalPaymentAdapter implements PaymentPort {
    @Override
    public boolean processPayment(String customerId, double amount) {
        System.out.println("[PayPal] Processing payment for customer " + customerId);
        System.out.println("[PayPal] Amount: $" + String.format("%.2f", amount));
        System.out.println("[PayPal] Payment approved!");
        return true; // Simplified - always succeeds
    }
}

// ============================================================================
// PRIMARY ADAPTERS (Driving the Application)
// ============================================================================

// Adapter: REST API Controller
class RESTOrderController {
    private final OrderService orderService;
    
    public RESTOrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    public void handleCreateOrder(String customerId) {
        System.out.println("[REST API] POST /orders");
        String orderId = orderService.createOrder(customerId);
        System.out.println("[REST API] Response: { \"orderId\": \"" + orderId + "\" }");
    }
    
    public void handleAddItem(String orderId, String productId, int quantity, double price) {
        System.out.println("[REST API] POST /orders/" + orderId + "/items");
        orderService.addItemToOrder(orderId, productId, quantity, price);
        System.out.println("[REST API] Response: { \"status\": \"success\" }");
    }
    
    public void handleConfirmOrder(String orderId) {
        System.out.println("[REST API] POST /orders/" + orderId + "/confirm");
        orderService.confirmOrder(orderId);
        System.out.println("[REST API] Response: { \"status\": \"confirmed\" }");
    }
}

// Adapter: CLI Interface
class CLIOrderInterface {
    private final OrderService orderService;
    
    public CLIOrderInterface(OrderService orderService) {
        this.orderService = orderService;
    }
    
    public void processCommand(String command) {
        String[] parts = command.split(" ");
        
        System.out.println("[CLI] $ " + command);
        
        switch (parts[0]) {
            case "create":
                String orderId = orderService.createOrder(parts[1]);
                System.out.println("[CLI] Created order: " + orderId);
                break;
            case "add":
                orderService.addItemToOrder(parts[1], parts[2], 
                                          Integer.parseInt(parts[3]), 
                                          Double.parseDouble(parts[4]));
                System.out.println("[CLI] Item added");
                break;
            case "confirm":
                orderService.confirmOrder(parts[1]);
                System.out.println("[CLI] Order confirmed");
                break;
            default:
                System.out.println("[CLI] Unknown command");
        }
    }
}

/**
 * Demonstration of Hexagonal Architecture Pattern
 */
public class HexagonalArchitecturePattern {
    public static void main(String[] args) {
        demonstrateWithInMemoryAndEmail();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateWithPostgreSQLAndSMS();
        System.out.println("\n" + "=".repeat(60) + "\n");
        demonstrateMultipleInterfaces();
    }
    
    private static void demonstrateWithInMemoryAndEmail() {
        System.out.println("=== Hexagonal Architecture: In-Memory DB + Email ===\n");
        
        // Assemble adapters
        OrderRepository repository = new InMemoryOrderRepository();
        NotificationPort notifier = new EmailNotificationAdapter();
        PaymentPort payment = new StripePaymentAdapter();
        
        // Create core service (business logic)
        OrderService orderService = new OrderServiceImpl(repository, notifier, payment);
        
        // Use REST adapter
        RESTOrderController restController = new RESTOrderController(orderService);
        
        restController.handleCreateOrder("CUST-001");
        restController.handleAddItem("ORD-1000", "PROD-123", 2, 29.99);
        restController.handleAddItem("ORD-1000", "PROD-456", 1, 49.99);
        restController.handleConfirmOrder("ORD-1000");
    }
    
    private static void demonstrateWithPostgreSQLAndSMS() {
        System.out.println("=== Hexagonal Architecture: PostgreSQL + SMS + PayPal ===\n");
        
        // Different adapters - SAME business logic!
        OrderRepository repository = new PostgreSQLOrderRepository();
        NotificationPort notifier = new SMSNotificationAdapter();
        PaymentPort payment = new PayPalPaymentAdapter();
        
        OrderService orderService = new OrderServiceImpl(repository, notifier, payment);
        RESTOrderController restController = new RESTOrderController(orderService);
        
        restController.handleCreateOrder("CUST-002");
        restController.handleAddItem("ORD-1001", "PROD-789", 5, 9.99);
        restController.handleConfirmOrder("ORD-1001");
    }
    
    private static void demonstrateMultipleInterfaces() {
        System.out.println("=== Hexagonal Architecture: Multiple Interfaces ===\n");
        
        OrderRepository repository = new InMemoryOrderRepository();
        NotificationPort notifier = new EmailNotificationAdapter();
        PaymentPort payment = new StripePaymentAdapter();
        
        OrderService orderService = new OrderServiceImpl(repository, notifier, payment);
        
        // Same service, different interfaces!
        RESTOrderController restController = new RESTOrderController(orderService);
        CLIOrderInterface cliInterface = new CLIOrderInterface(orderService);
        
        System.out.println("--- Using REST Interface ---");
        restController.handleCreateOrder("CUST-003");
        
        System.out.println("\n--- Using CLI Interface ---");
        cliInterface.processCommand("add ORD-1002 PROD-111 3 15.99");
        cliInterface.processCommand("confirm ORD-1002");
    }
}
