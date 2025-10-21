package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Materialized View Pattern
 * 
 * Intent: Generate prepopulated views over data in one or more data stores when
 * the data isn't ideally formatted for required query operations, improving query
 * performance and simplifying application code.
 * 
 * Also Known As: Precomputed View, Query Cache, CQRS Read Model
 * 
 * Motivation:
 * Querying data from multiple sources or performing complex aggregations can be
 * expensive. Materialized views precompute and store query results, providing
 * fast read access at the cost of eventual consistency.
 * 
 * Applicability:
 * - Complex queries that are expensive to compute
 * - Data from multiple sources needs to be aggregated
 * - Read-heavy workloads where data doesn't change frequently
 * - Denormalization needed for query performance
 * - Different views of same data for different use cases
 * - CQRS pattern implementation (separate read models)
 * 
 * Benefits:
 * - Dramatically improved query performance
 * - Reduced load on source data stores
 * - Simplified query logic
 * - Support for multiple optimized views
 * - Better scalability for reads
 * - Can query across multiple data sources
 * 
 * Implementation Considerations:
 * - Eventual consistency (views may be stale)
 * - Storage overhead (duplicate data)
 * - View refresh strategy (real-time vs periodic)
 * - Complex update logic when source data changes
 * - Need to handle view corruption/rebuild
 * - Staleness acceptable for use case
 */

// Domain models (source data)
class Order {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final LocalDateTime orderDate;
    private final String status;
    
    public Order(String orderId, String customerId, List<OrderItem> items, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.orderDate = LocalDateTime.now();
        this.status = status;
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    
    public double getTotalAmount() {
        return items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
    }
}

class OrderItem {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double price;
    
    public OrderItem(String productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}

class Customer {
    private final String customerId;
    private final String name;
    private final String email;
    private final String tier; // Bronze, Silver, Gold
    
    public Customer(String customerId, String name, String email, String tier) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.tier = tier;
    }
    
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getTier() { return tier; }
}

class Product {
    private final String productId;
    private final String name;
    private final String category;
    private final double price;
    
    public Product(String productId, String name, String category, double price) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
    }
    
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
}

// Example 1: Customer Order Summary View
class CustomerOrderSummaryView {
    private final String customerId;
    private final String customerName;
    private final String customerTier;
    private int totalOrders;
    private double totalSpent;
    private LocalDateTime lastOrderDate;
    private String mostPurchasedCategory;
    private final LocalDateTime viewGeneratedAt;
    
    public CustomerOrderSummaryView(String customerId, String customerName, String customerTier) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerTier = customerTier;
        this.totalOrders = 0;
        this.totalSpent = 0.0;
        this.viewGeneratedAt = LocalDateTime.now();
    }
    
    public void update(int orders, double spent, LocalDateTime lastOrder, String category) {
        this.totalOrders = orders;
        this.totalSpent = spent;
        this.lastOrderDate = lastOrder;
        this.mostPurchasedCategory = category;
    }
    
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerTier() { return customerTier; }
    public int getTotalOrders() { return totalOrders; }
    public double getTotalSpent() { return totalSpent; }
    public LocalDateTime getLastOrderDate() { return lastOrderDate; }
    public String getMostPurchasedCategory() { return mostPurchasedCategory; }
    public LocalDateTime getViewGeneratedAt() { return viewGeneratedAt; }
    
    @Override
    public String toString() {
        return String.format("Customer: %s (%s), Orders: %d, Spent: $%.2f, Category: %s",
            customerName, customerTier, totalOrders, totalSpent, mostPurchasedCategory);
    }
}

// Example 2: Product Sales Analytics View
class ProductSalesView {
    private final String productId;
    private final String productName;
    private final String category;
    private int unitsSold;
    private double revenue;
    private int numberOfOrders;
    private double averageOrderValue;
    
    public ProductSalesView(String productId, String productName, String category) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
    }
    
    public void update(int units, double rev, int orders) {
        this.unitsSold = units;
        this.revenue = rev;
        this.numberOfOrders = orders;
        this.averageOrderValue = orders > 0 ? rev / orders : 0;
    }
    
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public int getUnitsSold() { return unitsSold; }
    public double getRevenue() { return revenue; }
    public int getNumberOfOrders() { return numberOfOrders; }
    public double getAverageOrderValue() { return averageOrderValue; }
    
    @Override
    public String toString() {
        return String.format("Product: %s, Units: %d, Revenue: $%.2f, Orders: %d, AOV: $%.2f",
            productName, unitsSold, revenue, numberOfOrders, averageOrderValue);
    }
}

// Example 3: Daily Sales Summary View
class DailySalesSummaryView {
    private final String date;
    private int totalOrders;
    private double totalRevenue;
    private int uniqueCustomers;
    private Map<String, Integer> ordersByStatus;
    private Map<String, Double> revenueByCategory;
    
    public DailySalesSummaryView(String date) {
        this.date = date;
        this.ordersByStatus = new HashMap<>();
        this.revenueByCategory = new HashMap<>();
    }
    
    public void update(int orders, double revenue, int customers,
                      Map<String, Integer> statusCounts, Map<String, Double> categoryRevenue) {
        this.totalOrders = orders;
        this.totalRevenue = revenue;
        this.uniqueCustomers = customers;
        this.ordersByStatus = new HashMap<>(statusCounts);
        this.revenueByCategory = new HashMap<>(categoryRevenue);
    }
    
    public String getDate() { return date; }
    public int getTotalOrders() { return totalOrders; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getUniqueCustomers() { return uniqueCustomers; }
    public Map<String, Integer> getOrdersByStatus() { return ordersByStatus; }
    public Map<String, Double> getRevenueByCategory() { return revenueByCategory; }
    
    @Override
    public String toString() {
        return String.format("Date: %s, Orders: %d, Revenue: $%.2f, Customers: %d",
            date, totalOrders, totalRevenue, uniqueCustomers);
    }
}

// Materialized View Manager
class MaterializedViewManager {
    private final Map<String, CustomerOrderSummaryView> customerViews;
    private final Map<String, ProductSalesView> productViews;
    private final Map<String, DailySalesSummaryView> dailyViews;
    
    private final Map<String, Order> orderStore;
    private final Map<String, Customer> customerStore;
    private final Map<String, Product> productStore;
    
    private final ScheduledExecutorService refreshScheduler;
    
    public MaterializedViewManager() {
        this.customerViews = new ConcurrentHashMap<>();
        this.productViews = new ConcurrentHashMap<>();
        this.dailyViews = new ConcurrentHashMap<>();
        
        this.orderStore = new ConcurrentHashMap<>();
        this.customerStore = new ConcurrentHashMap<>();
        this.productStore = new ConcurrentHashMap<>();
        
        this.refreshScheduler = Executors.newScheduledThreadPool(1);
    }
    
    // Add source data
    public void addCustomer(Customer customer) {
        customerStore.put(customer.getCustomerId(), customer);
        System.out.println("[ViewManager] Customer added: " + customer.getName());
    }
    
    public void addProduct(Product product) {
        productStore.put(product.getProductId(), product);
        System.out.println("[ViewManager] Product added: " + product.getName());
    }
    
    public void addOrder(Order order) {
        orderStore.put(order.getOrderId(), order);
        System.out.println("[ViewManager] Order added: " + order.getOrderId());
        
        // Trigger incremental view update
        updateViewsForNewOrder(order);
    }
    
    // Incremental update when new order arrives
    private void updateViewsForNewOrder(Order order) {
        System.out.println("[ViewManager] Incrementally updating views for order: " + order.getOrderId());
        
        // Update customer view
        refreshCustomerView(order.getCustomerId());
        
        // Update product views
        for (OrderItem item : order.getItems()) {
            refreshProductView(item.getProductId());
        }
        
        // Update daily view
        String dateKey = order.getOrderDate().toLocalDate().toString();
        refreshDailyView(dateKey);
    }
    
    // Refresh customer view
    public void refreshCustomerView(String customerId) {
        Customer customer = customerStore.get(customerId);
        if (customer == null) return;
        
        List<Order> customerOrders = orderStore.values().stream()
            .filter(o -> o.getCustomerId().equals(customerId))
            .collect(Collectors.toList());
        
        CustomerOrderSummaryView view = customerViews.computeIfAbsent(customerId,
            id -> new CustomerOrderSummaryView(id, customer.getName(), customer.getTier()));
        
        int totalOrders = customerOrders.size();
        double totalSpent = customerOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        LocalDateTime lastOrder = customerOrders.stream()
            .map(Order::getOrderDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        // Find most purchased category
        Map<String, Long> categoryCounts = customerOrders.stream()
            .flatMap(order -> order.getItems().stream())
            .map(item -> {
                Product p = productStore.get(item.getProductId());
                return p != null ? p.getCategory() : "Unknown";
            })
            .collect(Collectors.groupingBy(cat -> cat, Collectors.counting()));
        
        String mostPurchased = categoryCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
        
        view.update(totalOrders, totalSpent, lastOrder, mostPurchased);
        System.out.println("[ViewManager] Refreshed customer view: " + customerId);
    }
    
    // Refresh product view
    public void refreshProductView(String productId) {
        Product product = productStore.get(productId);
        if (product == null) return;
        
        ProductSalesView view = productViews.computeIfAbsent(productId,
            id -> new ProductSalesView(id, product.getName(), product.getCategory()));
        
        int unitsSold = 0;
        double revenue = 0;
        Set<String> orderIds = new HashSet<>();
        
        for (Order order : orderStore.values()) {
            for (OrderItem item : order.getItems()) {
                if (item.getProductId().equals(productId)) {
                    unitsSold += item.getQuantity();
                    revenue += item.getPrice() * item.getQuantity();
                    orderIds.add(order.getOrderId());
                }
            }
        }
        
        view.update(unitsSold, revenue, orderIds.size());
        System.out.println("[ViewManager] Refreshed product view: " + productId);
    }
    
    // Refresh daily summary view
    public void refreshDailyView(String dateKey) {
        DailySalesSummaryView view = dailyViews.computeIfAbsent(dateKey,
            date -> new DailySalesSummaryView(date));
        
        List<Order> dayOrders = orderStore.values().stream()
            .filter(o -> o.getOrderDate().toLocalDate().toString().equals(dateKey))
            .collect(Collectors.toList());
        
        int totalOrders = dayOrders.size();
        double totalRevenue = dayOrders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        Set<String> uniqueCustomers = dayOrders.stream()
            .map(Order::getCustomerId)
            .collect(Collectors.toSet());
        
        Map<String, Integer> statusCounts = dayOrders.stream()
            .collect(Collectors.groupingBy(Order::getStatus,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        
        Map<String, Double> categoryRevenue = new HashMap<>();
        for (Order order : dayOrders) {
            for (OrderItem item : order.getItems()) {
                Product product = productStore.get(item.getProductId());
                if (product != null) {
                    categoryRevenue.merge(product.getCategory(),
                        item.getPrice() * item.getQuantity(), Double::sum);
                }
            }
        }
        
        view.update(totalOrders, totalRevenue, uniqueCustomers.size(), statusCounts, categoryRevenue);
        System.out.println("[ViewManager] Refreshed daily view: " + dateKey);
    }
    
    // Refresh all views (full rebuild)
    public void refreshAllViews() {
        System.out.println("\n[ViewManager] Starting full view refresh...");
        long startTime = System.currentTimeMillis();
        
        customerStore.keySet().forEach(this::refreshCustomerView);
        productStore.keySet().forEach(this::refreshProductView);
        
        Set<String> dates = orderStore.values().stream()
            .map(o -> o.getOrderDate().toLocalDate().toString())
            .collect(Collectors.toSet());
        dates.forEach(this::refreshDailyView);
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("[ViewManager] Full refresh completed in %dms%n", duration);
    }
    
    // Schedule periodic refresh
    public void schedulePeriodicRefresh(long intervalSeconds) {
        refreshScheduler.scheduleAtFixedRate(
            this::refreshAllViews,
            intervalSeconds,
            intervalSeconds,
            TimeUnit.SECONDS
        );
        System.out.printf("[ViewManager] Scheduled periodic refresh every %ds%n", intervalSeconds);
    }
    
    // Query methods
    public CustomerOrderSummaryView getCustomerSummary(String customerId) {
        return customerViews.get(customerId);
    }
    
    public ProductSalesView getProductSales(String productId) {
        return productViews.get(productId);
    }
    
    public DailySalesSummaryView getDailySummary(String date) {
        return dailyViews.get(date);
    }
    
    public List<CustomerOrderSummaryView> getTopCustomers(int limit) {
        return customerViews.values().stream()
            .sorted((v1, v2) -> Double.compare(v2.getTotalSpent(), v1.getTotalSpent()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<ProductSalesView> getTopProducts(int limit) {
        return productViews.values().stream()
            .sorted((v1, v2) -> Double.compare(v2.getRevenue(), v1.getRevenue()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public void printStatistics() {
        System.out.println("\n=== Materialized View Statistics ===");
        System.out.println("Customer views: " + customerViews.size());
        System.out.println("Product views: " + productViews.size());
        System.out.println("Daily views: " + dailyViews.size());
        System.out.println("Source orders: " + orderStore.size());
    }
    
    public void shutdown() {
        refreshScheduler.shutdown();
    }
}

// Demonstration
public class MaterializedViewPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Materialized View Pattern Demo ===\n");
        
        MaterializedViewManager viewManager = new MaterializedViewManager();
        
        // Setup: Add customers and products
        viewManager.addCustomer(new Customer("C1", "Alice Johnson", "alice@example.com", "Gold"));
        viewManager.addCustomer(new Customer("C2", "Bob Smith", "bob@example.com", "Silver"));
        viewManager.addCustomer(new Customer("C3", "Charlie Brown", "charlie@example.com", "Bronze"));
        
        viewManager.addProduct(new Product("P1", "Laptop", "Electronics", 1200.00));
        viewManager.addProduct(new Product("P2", "Mouse", "Electronics", 25.00));
        viewManager.addProduct(new Product("P3", "Desk Chair", "Furniture", 300.00));
        viewManager.addProduct(new Product("P4", "Monitor", "Electronics", 400.00));
        
        // Demo 1: Incremental view updates
        System.out.println("--- Demo 1: Incremental View Updates ---");
        demoIncrementalUpdates(viewManager);
        
        Thread.sleep(500);
        
        // Demo 2: Query materialized views
        System.out.println("\n--- Demo 2: Query Materialized Views ---");
        demoQueryViews(viewManager);
        
        Thread.sleep(500);
        
        // Demo 3: Top customers and products
        System.out.println("\n--- Demo 3: Analytics from Views ---");
        demoAnalytics(viewManager);
        
        Thread.sleep(500);
        
        // Demo 4: Full refresh
        System.out.println("\n--- Demo 4: Full View Refresh ---");
        demoFullRefresh(viewManager);
        
        viewManager.shutdown();
    }
    
    private static void demoIncrementalUpdates(MaterializedViewManager viewManager) throws InterruptedException {
        // Add orders one by one - views update incrementally
        viewManager.addOrder(new Order("O1", "C1",
            Arrays.asList(
                new OrderItem("P1", "Laptop", 1, 1200.00),
                new OrderItem("P2", "Mouse", 2, 25.00)
            ), "Completed"));
        
        Thread.sleep(100);
        
        viewManager.addOrder(new Order("O2", "C2",
            Arrays.asList(
                new OrderItem("P3", "Desk Chair", 1, 300.00)
            ), "Shipped"));
        
        Thread.sleep(100);
        
        viewManager.addOrder(new Order("O3", "C1",
            Arrays.asList(
                new OrderItem("P4", "Monitor", 1, 400.00),
                new OrderItem("P2", "Mouse", 1, 25.00)
            ), "Completed"));
        
        viewManager.printStatistics();
    }
    
    private static void demoQueryViews(MaterializedViewManager viewManager) {
        // Query customer summary
        CustomerOrderSummaryView customerView = viewManager.getCustomerSummary("C1");
        System.out.println("\nCustomer C1 Summary:");
        System.out.println(customerView);
        
        // Query product sales
        ProductSalesView productView = viewManager.getProductSales("P2");
        System.out.println("\nProduct P2 Sales:");
        System.out.println(productView);
        
        // Query daily summary
        String today = LocalDateTime.now().toLocalDate().toString();
        DailySalesSummaryView dailyView = viewManager.getDailySummary(today);
        if (dailyView != null) {
            System.out.println("\nToday's Summary:");
            System.out.println(dailyView);
            System.out.println("Orders by status: " + dailyView.getOrdersByStatus());
            System.out.println("Revenue by category: " + dailyView.getRevenueByCategory());
        }
    }
    
    private static void demoAnalytics(MaterializedViewManager viewManager) {
        System.out.println("\nTop 3 Customers:");
        viewManager.getTopCustomers(3).forEach(System.out::println);
        
        System.out.println("\nTop 3 Products:");
        viewManager.getTopProducts(3).forEach(System.out::println);
    }
    
    private static void demoFullRefresh(MaterializedViewManager viewManager) {
        // Add more orders
        viewManager.addOrder(new Order("O4", "C3",
            Arrays.asList(
                new OrderItem("P1", "Laptop", 1, 1200.00)
            ), "Processing"));
        
        viewManager.addOrder(new Order("O5", "C2",
            Arrays.asList(
                new OrderItem("P2", "Mouse", 3, 25.00),
                new OrderItem("P4", "Monitor", 1, 400.00)
            ), "Completed"));
        
        // Perform full refresh
        viewManager.refreshAllViews();
        
        viewManager.printStatistics();
        
        System.out.println("\nUpdated Top Customers:");
        viewManager.getTopCustomers(3).forEach(System.out::println);
    }
}
