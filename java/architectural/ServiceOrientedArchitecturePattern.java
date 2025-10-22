package architectural;

import java.util.*;

/**
 * Service-Oriented Architecture (SOA) Pattern
 * ============================================
 * 
 * Intent:
 * Structures an application as a collection of loosely coupled services that
 * communicate over a network. Services are independent, reusable business
 * functionalities with well-defined interfaces and contracts.
 * 
 * Also Known As:
 * - Service-Based Architecture
 * - SOA
 * 
 * Motivation:
 * - Enable reusability across different applications
 * - Allow services to be developed and deployed independently
 * - Support integration of heterogeneous systems
 * - Enable business agility through service composition
 * 
 * Applicability:
 * - Enterprise application integration
 * - Systems requiring interoperability
 * - Large-scale distributed systems
 * - Legacy system modernization
 * 
 * Structure:
 * Service Consumer -> Service Registry -> Service Provider
 * Service Orchestration/Choreography for complex workflows
 * 
 * Participants:
 * - Service: Business capability with contract
 * - Service Registry: Central directory of services
 * - Service Consumer: Uses services
 * - Service Provider: Implements services
 * - ESB (Enterprise Service Bus): Message routing (optional)
 * 
 * Key Principles:
 * 1. Standardized Service Contract
 * 2. Service Loose Coupling
 * 3. Service Abstraction
 * 4. Service Reusability
 * 5. Service Autonomy
 * 6. Service Statelessness
 * 7. Service Discoverability
 * 8. Service Composability
 */

// ============================================================================
// SERVICE CONTRACTS (INTERFACES)
// ============================================================================

interface Service {
    String getServiceId();
    String getServiceName();
    String getVersion();
    ServiceStatus getStatus();
}

enum ServiceStatus {
    AVAILABLE, BUSY, UNAVAILABLE
}

// Customer Service Interface
interface CustomerService extends Service {
    Customer getCustomer(String customerId);
    String createCustomer(String name, String email);
    boolean updateCustomer(String customerId, String name, String email);
}

// Order Service Interface
interface OrderService extends Service {
    String createOrder(String customerId, List<OrderItem> items);
    Order getOrder(String orderId);
    boolean cancelOrder(String orderId);
    List<Order> getCustomerOrders(String customerId);
}

// Inventory Service Interface
interface InventoryService extends Service {
    boolean checkAvailability(String productId, int quantity);
    boolean reserveStock(String productId, int quantity);
    boolean releaseStock(String productId, int quantity);
    int getStockLevel(String productId);
}

// Payment Service Interface
interface PaymentService extends Service {
    String processPayment(String orderId, double amount, String paymentMethod);
    boolean refundPayment(String transactionId);
    PaymentStatus getPaymentStatus(String transactionId);
}

enum PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}

// Notification Service Interface
interface NotificationService extends Service {
    void sendEmail(String recipient, String subject, String body);
    void sendSMS(String phoneNumber, String message);
    void sendPushNotification(String userId, String message);
}

// ============================================================================
// DATA TRANSFER OBJECTS (DTOs)
// ============================================================================

class Customer {
    private String id;
    private String name;
    private String email;
    
    public Customer(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return String.format("Customer{id='%s', name='%s', email='%s'}", id, name, email);
    }
}

class Order {
    private String id;
    private String customerId;
    private List<OrderItem> items;
    private double totalAmount;
    private String status;
    
    public Order(String id, String customerId, List<OrderItem> items, double totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = "PENDING";
    }
    
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return String.format("Order{id='%s', customerId='%s', items=%d, total=%.2f, status='%s'}",
                           id, customerId, items.size(), totalAmount, status);
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
}

// ============================================================================
// SERVICE REGISTRY
// ============================================================================

class ServiceRegistry {
    private final Map<String, Service> services = new HashMap<>();
    
    public void register(Service service) {
        services.put(service.getServiceId(), service);
        System.out.println("[Registry] Registered: " + service.getServiceName() + 
                         " (" + service.getServiceId() + ")");
    }
    
    public void unregister(String serviceId) {
        Service removed = services.remove(serviceId);
        if (removed != null) {
            System.out.println("[Registry] Unregistered: " + removed.getServiceName());
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Service> T lookup(String serviceId, Class<T> serviceType) {
        Service service = services.get(serviceId);
        if (service != null && serviceType.isInstance(service)) {
            return (T) service;
        }
        return null;
    }
    
    public List<Service> listServices() {
        return new ArrayList<>(services.values());
    }
}

// ============================================================================
// SERVICE IMPLEMENTATIONS
// ============================================================================

class CustomerServiceImpl implements CustomerService {
    private final Map<String, Customer> customers = new HashMap<>();
    private int customerIdCounter = 1000;
    
    @Override
    public String getServiceId() { return "customer-service"; }
    
    @Override
    public String getServiceName() { return "Customer Service"; }
    
    @Override
    public String getVersion() { return "1.0"; }
    
    @Override
    public ServiceStatus getStatus() { return ServiceStatus.AVAILABLE; }
    
    @Override
    public Customer getCustomer(String customerId) {
        System.out.println("[CustomerService] Getting customer: " + customerId);
        return customers.get(customerId);
    }
    
    @Override
    public String createCustomer(String name, String email) {
        String customerId = "CUST-" + (customerIdCounter++);
        Customer customer = new Customer(customerId, name, email);
        customers.put(customerId, customer);
        System.out.println("[CustomerService] Created: " + customer);
        return customerId;
    }
    
    @Override
    public boolean updateCustomer(String customerId, String name, String email) {
        if (customers.containsKey(customerId)) {
            customers.put(customerId, new Customer(customerId, name, email));
            System.out.println("[CustomerService] Updated: " + customerId);
            return true;
        }
        return false;
    }
}

class OrderServiceImpl implements OrderService {
    private final Map<String, Order> orders = new HashMap<>();
    private int orderIdCounter = 5000;
    
    @Override
    public String getServiceId() { return "order-service"; }
    
    @Override
    public String getServiceName() { return "Order Service"; }
    
    @Override
    public String getVersion() { return "2.0"; }
    
    @Override
    public ServiceStatus getStatus() { return ServiceStatus.AVAILABLE; }
    
    @Override
    public String createOrder(String customerId, List<OrderItem> items) {
        String orderId = "ORD-" + (orderIdCounter++);
        double total = items.stream()
                           .mapToDouble(item -> item.getPrice() * item.getQuantity())
                           .sum();
        
        Order order = new Order(orderId, customerId, items, total);
        orders.put(orderId, order);
        System.out.println("[OrderService] Created: " + order);
        return orderId;
    }
    
    @Override
    public Order getOrder(String orderId) {
        System.out.println("[OrderService] Getting order: " + orderId);
        return orders.get(orderId);
    }
    
    @Override
    public boolean cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order != null && "PENDING".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            System.out.println("[OrderService] Cancelled: " + orderId);
            return true;
        }
        return false;
    }
    
    @Override
    public List<Order> getCustomerOrders(String customerId) {
        List<Order> customerOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getCustomerId().equals(customerId)) {
                customerOrders.add(order);
            }
        }
        return customerOrders;
    }
}

class InventoryServiceImpl implements InventoryService {
    private final Map<String, Integer> inventory = new HashMap<>();
    
    public InventoryServiceImpl() {
        // Initialize with sample inventory
        inventory.put("PROD-001", 100);
        inventory.put("PROD-002", 50);
        inventory.put("PROD-003", 200);
    }
    
    @Override
    public String getServiceId() { return "inventory-service"; }
    
    @Override
    public String getServiceName() { return "Inventory Service"; }
    
    @Override
    public String getVersion() { return "1.5"; }
    
    @Override
    public ServiceStatus getStatus() { return ServiceStatus.AVAILABLE; }
    
    @Override
    public boolean checkAvailability(String productId, int quantity) {
        int available = inventory.getOrDefault(productId, 0);
        boolean result = available >= quantity;
        System.out.println("[InventoryService] Check availability " + productId + 
                         " (need: " + quantity + ", available: " + available + "): " + result);
        return result;
    }
    
    @Override
    public boolean reserveStock(String productId, int quantity) {
        if (checkAvailability(productId, quantity)) {
            inventory.put(productId, inventory.get(productId) - quantity);
            System.out.println("[InventoryService] Reserved " + quantity + " units of " + productId);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean releaseStock(String productId, int quantity) {
        inventory.put(productId, inventory.getOrDefault(productId, 0) + quantity);
        System.out.println("[InventoryService] Released " + quantity + " units of " + productId);
        return true;
    }
    
    @Override
    public int getStockLevel(String productId) {
        return inventory.getOrDefault(productId, 0);
    }
}

class PaymentServiceImpl implements PaymentService {
    private final Map<String, PaymentStatus> transactions = new HashMap<>();
    private int transactionIdCounter = 7000;
    
    @Override
    public String getServiceId() { return "payment-service"; }
    
    @Override
    public String getServiceName() { return "Payment Service"; }
    
    @Override
    public String getVersion() { return "3.0"; }
    
    @Override
    public ServiceStatus getStatus() { return ServiceStatus.AVAILABLE; }
    
    @Override
    public String processPayment(String orderId, double amount, String paymentMethod) {
        String transactionId = "TXN-" + (transactionIdCounter++);
        transactions.put(transactionId, PaymentStatus.COMPLETED);
        System.out.println("[PaymentService] Processed payment for " + orderId + 
                         ": $" + String.format("%.2f", amount) + 
                         " via " + paymentMethod + " (TXN: " + transactionId + ")");
        return transactionId;
    }
    
    @Override
    public boolean refundPayment(String transactionId) {
        if (transactions.containsKey(transactionId)) {
            transactions.put(transactionId, PaymentStatus.REFUNDED);
            System.out.println("[PaymentService] Refunded transaction: " + transactionId);
            return true;
        }
        return false;
    }
    
    @Override
    public PaymentStatus getPaymentStatus(String transactionId) {
        return transactions.getOrDefault(transactionId, PaymentStatus.FAILED);
    }
}

class NotificationServiceImpl implements NotificationService {
    @Override
    public String getServiceId() { return "notification-service"; }
    
    @Override
    public String getServiceName() { return "Notification Service"; }
    
    @Override
    public String getVersion() { return "1.0"; }
    
    @Override
    public ServiceStatus getStatus() { return ServiceStatus.AVAILABLE; }
    
    @Override
    public void sendEmail(String recipient, String subject, String body) {
        System.out.println("[NotificationService] Email sent to " + recipient);
        System.out.println("  Subject: " + subject);
        System.out.println("  Body: " + body);
    }
    
    @Override
    public void sendSMS(String phoneNumber, String message) {
        System.out.println("[NotificationService] SMS sent to " + phoneNumber + ": " + message);
    }
    
    @Override
    public void sendPushNotification(String userId, String message) {
        System.out.println("[NotificationService] Push notification sent to " + userId + ": " + message);
    }
}

// ============================================================================
// SERVICE ORCHESTRATION (BUSINESS PROCESS)
// ============================================================================

class OrderOrchestrationService {
    private final ServiceRegistry registry;
    
    public OrderOrchestrationService(ServiceRegistry registry) {
        this.registry = registry;
    }
    
    public boolean placeOrder(String customerId, List<OrderItem> items, String paymentMethod) {
        System.out.println("\n[Orchestration] Starting order placement workflow...\n");
        
        // 1. Lookup required services
        CustomerService customerService = registry.lookup("customer-service", CustomerService.class);
        InventoryService inventoryService = registry.lookup("inventory-service", InventoryService.class);
        OrderService orderService = registry.lookup("order-service", OrderService.class);
        PaymentService paymentService = registry.lookup("payment-service", PaymentService.class);
        NotificationService notificationService = registry.lookup("notification-service", NotificationService.class);
        
        // 2. Validate customer
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            System.out.println("[Orchestration] ERROR: Customer not found");
            return false;
        }
        
        // 3. Check inventory
        for (OrderItem item : items) {
            if (!inventoryService.checkAvailability(item.getProductId(), item.getQuantity())) {
                System.out.println("[Orchestration] ERROR: Insufficient inventory for " + item.getProductId());
                return false;
            }
        }
        
        // 4. Reserve inventory
        for (OrderItem item : items) {
            inventoryService.reserveStock(item.getProductId(), item.getQuantity());
        }
        
        // 5. Create order
        String orderId = orderService.createOrder(customerId, items);
        Order order = orderService.getOrder(orderId);
        
        // 6. Process payment
        String transactionId = paymentService.processPayment(orderId, order.getTotalAmount(), paymentMethod);
        
        if (paymentService.getPaymentStatus(transactionId) == PaymentStatus.COMPLETED) {
            // 7. Send confirmation notification
            notificationService.sendEmail(customer.getEmail(), 
                                         "Order Confirmation - " + orderId,
                                         "Your order has been placed successfully. Total: $" + 
                                         String.format("%.2f", order.getTotalAmount()));
            
            System.out.println("\n[Orchestration] Order placement completed successfully!\n");
            return true;
        } else {
            // Rollback: Release inventory
            for (OrderItem item : items) {
                inventoryService.releaseStock(item.getProductId(), item.getQuantity());
            }
            orderService.cancelOrder(orderId);
            System.out.println("\n[Orchestration] Order placement failed - payment declined\n");
            return false;
        }
    }
}

/**
 * Demonstration of Service-Oriented Architecture Pattern
 */
public class ServiceOrientedArchitecturePattern {
    public static void main(String[] args) {
        demonstrateSOA();
    }
    
    private static void demonstrateSOA() {
        System.out.println("=== Service-Oriented Architecture: E-Commerce System ===\n");
        
        // Create service registry
        ServiceRegistry registry = new ServiceRegistry();
        
        System.out.println("--- Registering Services ---\n");
        
        // Register services
        registry.register(new CustomerServiceImpl());
        registry.register(new OrderServiceImpl());
        registry.register(new InventoryServiceImpl());
        registry.register(new PaymentServiceImpl());
        registry.register(new NotificationServiceImpl());
        
        System.out.println("\n--- Available Services ---");
        for (Service service : registry.listServices()) {
            System.out.println("  - " + service.getServiceName() + " v" + service.getVersion() + 
                             " [" + service.getStatus() + "]");
        }
        
        // Create customer
        System.out.println("\n--- Creating Customer ---\n");
        CustomerService customerService = registry.lookup("customer-service", CustomerService.class);
        String customerId = customerService.createCustomer("Alice Johnson", "alice@example.com");
        
        // Create order orchestration service
        OrderOrchestrationService orchestration = new OrderOrchestrationService(registry);
        
        // Place order
        System.out.println("\n" + "=".repeat(70));
        List<OrderItem> items = Arrays.asList(
            new OrderItem("PROD-001", 2, 29.99),
            new OrderItem("PROD-002", 1, 49.99)
        );
        
        orchestration.placeOrder(customerId, items, "credit_card");
        
        // Check inventory levels
        System.out.println("--- Final Inventory Levels ---\n");
        InventoryService inventoryService = registry.lookup("inventory-service", InventoryService.class);
        System.out.println("PROD-001: " + inventoryService.getStockLevel("PROD-001") + " units");
        System.out.println("PROD-002: " + inventoryService.getStockLevel("PROD-002") + " units");
    }
}
