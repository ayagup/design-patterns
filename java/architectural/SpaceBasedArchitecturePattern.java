package architectural;

import java.util.*;
import java.util.concurrent.*;

/**
 * Space-Based Architecture Pattern
 * ==================================
 * 
 * Intent:
 * Eliminates database as bottleneck by storing application data in-memory
 * across a distributed grid. Processing units scale horizontally, accessing
 * shared memory space instead of centralized database.
 * 
 * Also Known As:
 * - Cloud Architecture Pattern
 * - In-Memory Data Grid Architecture
 * 
 * Motivation:
 * - Handle extreme scaling requirements
 * - Eliminate database bottleneck
 * - Enable near-linear scalability
 * - Minimize database reads/writes
 * 
 * Applicability:
 * - High-volume transaction systems
 * - Real-time data processing
 * - Applications with variable load
 * - Systems requiring elastic scaling
 * 
 * Structure:
 * Processing Units <-> Shared Space (In-Memory Data Grid)
 * Messaging Grid coordinates updates
 * Data Grid replicates across nodes
 * 
 * Key Components:
 * 1. Processing Units: Stateless application instances
 * 2. Virtualized Middleware: Messaging, Data Grid, Processing Grid
 * 3. Data Pumps: Async updates to database
 * 4. Data Writers: Persist data asynchronously
 * 5. Data Readers: Load data on demand
 */

// ============================================================================
// SHARED SPACE (IN-MEMORY DATA GRID)
// ============================================================================

class DataSpace<K, V> {
    private final ConcurrentHashMap<K, V> data = new ConcurrentHashMap<>();
    private final String spaceName;
    
    public DataSpace(String spaceName) {
        this.spaceName = spaceName;
    }
    
    public void write(K key, V value) {
        data.put(key, value);
        System.out.println("[DataSpace: " + spaceName + "] Write: " + key);
    }
    
    public V read(K key) {
        V value = data.get(key);
        System.out.println("[DataSpace: " + spaceName + "] Read: " + key + " -> " + 
                         (value != null ? "found" : "not found"));
        return value;
    }
    
    public V take(K key) {
        V value = data.remove(key);
        System.out.println("[DataSpace: " + spaceName + "] Take (removed): " + key);
        return value;
    }
    
    public Collection<V> readAll() {
        return new ArrayList<>(data.values());
    }
    
    public int size() {
        return data.size();
    }
    
    public void clear() {
        data.clear();
        System.out.println("[DataSpace: " + spaceName + "] Cleared");
    }
}

// ============================================================================
// PROCESSING UNITS
// ============================================================================

class ProcessingUnit {
    private final String unitId;
    private final DataSpace<String, Order> orderSpace;
    private final DataSpace<String, Product> productSpace;
    private volatile boolean running = false;
    
    public ProcessingUnit(String unitId, DataSpace<String, Order> orderSpace,
                         DataSpace<String, Product> productSpace) {
        this.unitId = unitId;
        this.orderSpace = orderSpace;
        this.productSpace = productSpace;
    }
    
    public void start() {
        running = true;
        System.out.println("[ProcessingUnit: " + unitId + "] Started");
    }
    
    public void stop() {
        running = false;
        System.out.println("[ProcessingUnit: " + unitId + "] Stopped");
    }
    
    public String processOrder(String customerId, String productId, int quantity) {
        if (!running) {
            throw new IllegalStateException("Processing unit not running");
        }
        
        System.out.println("[ProcessingUnit: " + unitId + "] Processing order...");
        
        // Read product from shared space
        Product product = productSpace.read(productId);
        if (product == null) {
            System.out.println("[ProcessingUnit: " + unitId + "] Product not found: " + productId);
            return null;
        }
        
        // Create order
        String orderId = "ORD-" + System.currentTimeMillis();
        double amount = product.getPrice() * quantity;
        Order order = new Order(orderId, customerId, productId, quantity, amount);
        
        // Write order to shared space
        orderSpace.write(orderId, order);
        
        System.out.println("[ProcessingUnit: " + unitId + "] Order created: " + orderId + 
                         ", Amount: $" + String.format("%.2f", amount));
        
        return orderId;
    }
    
    public Order getOrder(String orderId) {
        return orderSpace.read(orderId);
    }
}

// ============================================================================
// DATA MODELS
// ============================================================================

class Order {
    private final String orderId;
    private final String customerId;
    private final String productId;
    private final int quantity;
    private final double amount;
    private final long timestamp;
    
    public Order(String orderId, String customerId, String productId, int quantity, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', product='%s', qty=%d, amount=$%.2f}",
                           orderId, customerId, productId, quantity, amount);
    }
}

class Product {
    private final String productId;
    private final String name;
    private final double price;
    
    public Product(String productId, String name, double price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }
    
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=$%.2f}", productId, name, price);
    }
}

// ============================================================================
// DATA WRITER (ASYNC PERSISTENCE)
// ============================================================================

class DataWriter {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<String> persistedOrders = Collections.synchronizedList(new ArrayList<>());
    
    public void asyncPersist(Order order) {
        executor.submit(() -> {
            // Simulate async database write
            try {
                Thread.sleep(100);
                persistedOrders.add(order.getOrderId());
                System.out.println("[DataWriter] Persisted to database: " + order.getOrderId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    public int getPersistedCount() {
        return persistedOrders.size();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// ============================================================================
// LOAD BALANCER (DISTRIBUTES WORK)
// ============================================================================

class LoadBalancer {
    private final List<ProcessingUnit> units = new CopyOnWriteArrayList<>();
    private int currentIndex = 0;
    
    public void addUnit(ProcessingUnit unit) {
        units.add(unit);
        System.out.println("[LoadBalancer] Added processing unit, total: " + units.size());
    }
    
    public ProcessingUnit getNextUnit() {
        if (units.isEmpty()) {
            return null;
        }
        ProcessingUnit unit = units.get(currentIndex);
        currentIndex = (currentIndex + 1) % units.size();
        return unit;
    }
    
    public int getUnitCount() {
        return units.size();
    }
}

/**
 * Demonstration of Space-Based Architecture Pattern
 */
public class SpaceBasedArchitecturePattern {
    public static void main(String[] args) throws Exception {
        demonstrateSpaceBasedArchitecture();
    }
    
    private static void demonstrateSpaceBasedArchitecture() throws Exception {
        System.out.println("=== Space-Based Architecture: E-Commerce System ===\n");
        
        // Create shared data spaces (in-memory data grid)
        DataSpace<String, Order> orderSpace = new DataSpace<>("Orders");
        DataSpace<String, Product> productSpace = new DataSpace<>("Products");
        
        // Populate product catalog in shared space
        System.out.println("--- Initializing Product Catalog in Data Grid ---\n");
        productSpace.write("P001", new Product("P001", "Laptop", 999.99));
        productSpace.write("P002", new Product("P002", "Mouse", 29.99));
        productSpace.write("P003", new Product("P003", "Keyboard", 79.99));
        
        // Create processing units (horizontal scaling)
        System.out.println("\n--- Starting Processing Units ---\n");
        ProcessingUnit unit1 = new ProcessingUnit("PU-1", orderSpace, productSpace);
        ProcessingUnit unit2 = new ProcessingUnit("PU-2", orderSpace, productSpace);
        ProcessingUnit unit3 = new ProcessingUnit("PU-3", orderSpace, productSpace);
        
        unit1.start();
        unit2.start();
        unit3.start();
        
        // Create load balancer
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.addUnit(unit1);
        loadBalancer.addUnit(unit2);
        loadBalancer.addUnit(unit3);
        
        // Create data writer for async persistence
        DataWriter dataWriter = new DataWriter();
        
        // Process multiple orders (distributed across processing units)
        System.out.println("\n--- Processing Orders (Load Balanced) ---\n");
        
        String[] customers = {"CUST-001", "CUST-002", "CUST-003", "CUST-004", "CUST-005"};
        String[] products = {"P001", "P002", "P003", "P001", "P002"};
        
        List<String> orderIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            // Get next available processing unit (round-robin)
            ProcessingUnit unit = loadBalancer.getNextUnit();
            
            // Process order
            String orderId = unit.processOrder(customers[i], products[i], 1 + i);
            
            if (orderId != null) {
                orderIds.add(orderId);
                
                // Async persist to database
                Order order = orderSpace.read(orderId);
                dataWriter.asyncPersist(order);
            }
            
            System.out.println();
        }
        
        // Query orders from shared space
        System.out.println("--- Querying Orders from Data Grid ---\n");
        for (String orderId : orderIds) {
            Order order = unit1.getOrder(orderId); // Any unit can access shared space
            System.out.println(order);
        }
        
        // Show statistics
        System.out.println("\n--- Statistics ---");
        System.out.println("Orders in data grid: " + orderSpace.size());
        System.out.println("Products in data grid: " + productSpace.size());
        System.out.println("Active processing units: " + loadBalancer.getUnitCount());
        
        // Wait for async writes to complete
        Thread.sleep(600);
        System.out.println("Orders persisted to database: " + dataWriter.getPersistedCount());
        
        // Cleanup
        unit1.stop();
        unit2.stop();
        unit3.stop();
        dataWriter.shutdown();
        
        System.out.println("\n=== Key Benefits ===");
        System.out.println("1. No database bottleneck - all data in memory");
        System.out.println("2. Horizontal scaling - add more processing units");
        System.out.println("3. High performance - in-memory access");
        System.out.println("4. Async persistence - writes don't block processing");
    }
}
