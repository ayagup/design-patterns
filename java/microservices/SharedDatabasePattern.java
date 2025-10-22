package microservices;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared Database Pattern
 * ========================
 * 
 * Intent:
 * Multiple microservices share the same database to enable data sharing,
 * reduce data duplication, and simplify queries that span multiple services.
 * 
 * Also Known As:
 * - Single Database Pattern
 * - Shared Data Store
 * 
 * Motivation:
 * - Enable data sharing across services without API calls
 * - Simplify queries that need data from multiple domains
 * - Reduce data duplication and synchronization overhead
 * - Support transactions that span multiple services
 * 
 * Applicability:
 * - Services are tightly related and owned by same team
 * - Need ACID transactions across multiple services
 * - Query performance is critical
 * - During migration from monolith
 * 
 * Tradeoffs:
 * + Simple data sharing without API calls
 * + Strong consistency guarantees
 * + Efficient queries across domains
 * - Strong coupling between services
 * - Schema changes affect multiple services
 * - Difficult to scale services independently
 * - No clear ownership boundaries
 * 
 * Structure:
 * Service A --> Shared Database <-- Service B
 * Both services access same tables but should own specific tables
 * 
 * Participants:
 * - Service A: Owns specific tables, may read from others
 * - Service B: Owns different tables, may read from others
 * - Shared Database: Single database accessed by all services
 * - Schema Coordinator: Manages schema evolution
 */

// ============================================================================
// SHARED DATABASE (In-Memory Simulation)
// ============================================================================

class SharedDatabase {
    private final Map<Integer, Customer> customers = new ConcurrentHashMap<>();
    private final Map<Integer, Order> orders = new ConcurrentHashMap<>();
    private final Map<Integer, Product> products = new ConcurrentHashMap<>();
    private final Map<Integer, Shipment> shipments = new ConcurrentHashMap<>();
    
    private int customerIdCounter = 1;
    private int orderIdCounter = 100;
    private int productIdCounter = 1000;
    private int shipmentIdCounter = 5000;
    
    public SharedDatabase() {
        // Pre-populate with sample data
        insertCustomer("Alice", "alice@example.com");
        insertCustomer("Bob", "bob@example.com");
        
        insertProduct("Laptop", 999.99, 50);
        insertProduct("Mouse", 29.99, 200);
        insertProduct("Keyboard", 79.99, 150);
    }
    
    // Customer table operations (owned by CustomerService)
    public int insertCustomer(String name, String email) {
        int id = customerIdCounter++;
        customers.put(id, new Customer(id, name, email));
        System.out.println("[DB] INSERT into customers: id=" + id);
        return id;
    }
    
    public Customer selectCustomer(int id) {
        return customers.get(id);
    }
    
    public List<Customer> selectAllCustomers() {
        return new ArrayList<>(customers.values());
    }
    
    // Order table operations (owned by OrderService)
    public int insertOrder(int customerId, int productId, int quantity, double totalAmount) {
        int id = orderIdCounter++;
        orders.put(id, new Order(id, customerId, productId, quantity, totalAmount));
        System.out.println("[DB] INSERT into orders: id=" + id);
        return id;
    }
    
    public Order selectOrder(int id) {
        return orders.get(id);
    }
    
    public List<Order> selectOrdersByCustomer(int customerId) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.customerId == customerId) {
                result.add(order);
            }
        }
        return result;
    }
    
    // Product table operations (owned by CatalogService)
    public int insertProduct(String name, double price, int stock) {
        int id = productIdCounter++;
        products.put(id, new Product(id, name, price, stock));
        System.out.println("[DB] INSERT into products: id=" + id);
        return id;
    }
    
    public Product selectProduct(int id) {
        return products.get(id);
    }
    
    public void updateProductStock(int id, int newStock) {
        Product product = products.get(id);
        if (product != null) {
            products.put(id, new Product(id, product.name, product.price, newStock));
            System.out.println("[DB] UPDATE products SET stock=" + newStock + " WHERE id=" + id);
        }
    }
    
    // Shipment table operations (owned by ShippingService)
    public int insertShipment(int orderId, String address, String status) {
        int id = shipmentIdCounter++;
        shipments.put(id, new Shipment(id, orderId, address, status));
        System.out.println("[DB] INSERT into shipments: id=" + id);
        return id;
    }
    
    public Shipment selectShipment(int id) {
        return shipments.get(id);
    }
    
    public Shipment selectShipmentByOrderId(int orderId) {
        for (Shipment shipment : shipments.values()) {
            if (shipment.orderId == orderId) {
                return shipment;
            }
        }
        return null;
    }
    
    // Cross-service query (demonstrates benefit of shared database)
    public OrderWithDetails selectOrderWithDetails(int orderId) {
        Order order = orders.get(orderId);
        if (order == null) return null;
        
        Customer customer = customers.get(order.customerId);
        Product product = products.get(order.productId);
        Shipment shipment = selectShipmentByOrderId(orderId);
        
        System.out.println("[DB] Complex JOIN query across 4 tables");
        return new OrderWithDetails(order, customer, product, shipment);
    }
}

// Domain objects
class Customer {
    final int id;
    final String name;
    final String email;
    
    Customer(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s', email='%s'}", id, name, email);
    }
}

class Order {
    final int id;
    final int customerId;
    final int productId;
    final int quantity;
    final double totalAmount;
    
    Order(int id, int customerId, int productId, int quantity, double totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
    }
    
    @Override
    public String toString() {
        return String.format("Order{id=%d, customerId=%d, productId=%d, quantity=%d, total=$%.2f}",
                           id, customerId, productId, quantity, totalAmount);
    }
}

class Product {
    final int id;
    final String name;
    final double price;
    final int stock;
    
    Product(int id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    
    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=$%.2f, stock=%d}",
                           id, name, price, stock);
    }
}

class Shipment {
    final int id;
    final int orderId;
    final String address;
    final String status;
    
    Shipment(int id, int orderId, String address, String status) {
        this.id = id;
        this.orderId = orderId;
        this.address = address;
        this.status = status;
    }
    
    @Override
    public String toString() {
        return String.format("Shipment{id=%d, orderId=%d, address='%s', status='%s'}",
                           id, orderId, address, status);
    }
}

class OrderWithDetails {
    final Order order;
    final Customer customer;
    final Product product;
    final Shipment shipment;
    
    OrderWithDetails(Order order, Customer customer, Product product, Shipment shipment) {
        this.order = order;
        this.customer = customer;
        this.product = product;
        this.shipment = shipment;
    }
    
    @Override
    public String toString() {
        return String.format("OrderDetails{\n  order=%s,\n  customer=%s,\n  product=%s,\n  shipment=%s\n}",
                           order, customer, product, shipment);
    }
}

// ============================================================================
// MICROSERVICES (each owns specific tables but can read from others)
// ============================================================================

class CustomerService {
    private final SharedDatabase db;
    
    CustomerService(SharedDatabase db) {
        this.db = db;
    }
    
    public int createCustomer(String name, String email) {
        System.out.println("[CustomerService] Creating customer: " + name);
        return db.insertCustomer(name, email);
    }
    
    public Customer getCustomer(int id) {
        System.out.println("[CustomerService] Fetching customer: " + id);
        return db.selectCustomer(id);
    }
    
    public List<Customer> getAllCustomers() {
        System.out.println("[CustomerService] Fetching all customers");
        return db.selectAllCustomers();
    }
}

class OrderService {
    private final SharedDatabase db;
    
    OrderService(SharedDatabase db) {
        this.db = db;
    }
    
    public int createOrder(int customerId, int productId, int quantity) {
        System.out.println("[OrderService] Creating order");
        
        // Read from other services' tables (coupling!)
        Customer customer = db.selectCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        Product product = db.selectProduct(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        
        if (product.stock < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        
        // Update product stock (modifying another service's table - tight coupling!)
        db.updateProductStock(productId, product.stock - quantity);
        
        double total = product.price * quantity;
        return db.insertOrder(customerId, productId, quantity, total);
    }
    
    public Order getOrder(int id) {
        System.out.println("[OrderService] Fetching order: " + id);
        return db.selectOrder(id);
    }
    
    public List<Order> getCustomerOrders(int customerId) {
        System.out.println("[OrderService] Fetching orders for customer: " + customerId);
        return db.selectOrdersByCustomer(customerId);
    }
    
    // Benefit: Can do complex cross-service query efficiently
    public OrderWithDetails getOrderWithDetails(int orderId) {
        System.out.println("[OrderService] Fetching order with full details");
        return db.selectOrderWithDetails(orderId);
    }
}

class CatalogService {
    private final SharedDatabase db;
    
    CatalogService(SharedDatabase db) {
        this.db = db;
    }
    
    public int addProduct(String name, double price, int stock) {
        System.out.println("[CatalogService] Adding product: " + name);
        return db.insertProduct(name, price, stock);
    }
    
    public Product getProduct(int id) {
        System.out.println("[CatalogService] Fetching product: " + id);
        return db.selectProduct(id);
    }
}

class ShippingService {
    private final SharedDatabase db;
    
    ShippingService(SharedDatabase db) {
        this.db = db;
    }
    
    public int createShipment(int orderId, String address) {
        System.out.println("[ShippingService] Creating shipment for order: " + orderId);
        
        // Read from OrderService's table (coupling!)
        Order order = db.selectOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        
        return db.insertShipment(orderId, address, "PENDING");
    }
    
    public Shipment getShipment(int id) {
        System.out.println("[ShippingService] Fetching shipment: " + id);
        return db.selectShipment(id);
    }
    
    public Shipment getShipmentByOrderId(int orderId) {
        System.out.println("[ShippingService] Fetching shipment for order: " + orderId);
        return db.selectShipmentByOrderId(orderId);
    }
}

/**
 * Demonstration of Shared Database Pattern
 */
public class SharedDatabasePattern {
    public static void main(String[] args) {
        System.out.println("=== Shared Database Pattern ===\n");
        
        // Single shared database
        SharedDatabase database = new SharedDatabase();
        
        // Multiple services sharing same database
        CustomerService customerService = new CustomerService(database);
        OrderService orderService = new OrderService(database);
        CatalogService catalogService = new CatalogService(database);
        ShippingService shippingService = new ShippingService(database);
        
        System.out.println("--- Creating New Customer ---\n");
        int newCustomerId = customerService.createCustomer("Charlie", "charlie@example.com");
        
        System.out.println("\n--- Creating Order (reads from multiple tables) ---\n");
        int orderId = orderService.createOrder(1, 1000, 2);
        System.out.println("Created order: " + orderId);
        
        System.out.println("\n--- Creating Shipment (reads from Order table) ---\n");
        int shipmentId = shippingService.createShipment(orderId, "123 Main St");
        System.out.println("Created shipment: " + shipmentId);
        
        System.out.println("\n--- Efficient Cross-Service Query (JOIN across 4 tables) ---\n");
        OrderWithDetails details = orderService.getOrderWithDetails(orderId);
        System.out.println(details);
        
        System.out.println("\n=== Benefits ===");
        System.out.println("1. Simple data sharing - no API calls needed");
        System.out.println("2. Efficient queries - JOIN across multiple services");
        System.out.println("3. ACID transactions - strong consistency");
        System.out.println("4. Easy during migration from monolith");
        
        System.out.println("\n=== Drawbacks ===");
        System.out.println("1. Tight coupling - services depend on shared schema");
        System.out.println("2. Schema evolution - changes affect multiple services");
        System.out.println("3. No clear ownership - who owns which tables?");
        System.out.println("4. Scaling challenges - can't scale services independently");
        System.out.println("5. Team coordination - need to coordinate schema changes");
        
        System.out.println("\n=== When to Use ===");
        System.out.println("- Transitioning from monolith to microservices");
        System.out.println("- Services are tightly related and owned by same team");
        System.out.println("- Need strong consistency guarantees");
        System.out.println("- Query performance is critical");
        
        System.out.println("\n=== Recommended Alternative ===");
        System.out.println("Database per Service pattern for loose coupling!");
    }
}
