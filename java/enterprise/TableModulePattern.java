package enterprise;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * Table Module Pattern
 * 
 * Intent: A single instance that handles the business logic for all
 * rows in a database table or view.
 * 
 * Motivation:
 * Instead of one object per row (Domain Model/Active Record),
 * use one object to handle all rows of a table.
 * Organizes logic by table rather than by domain object.
 * 
 * Applicability:
 * - Table-oriented database design
 * - When working with RecordSets/DataSets
 * - Business logic is table-centric
 * - .NET DataSet applications
 */

/**
 * Example 1: Product Table Module
 * 
 * Handles all product-related business logic
 */
class ProductTableModule {
    private final Connection connection;
    
    public ProductTableModule(Connection connection) {
        this.connection = connection;
    }
    
    // Business logic: Get products by category
    public ResultSet getProductsByCategory(String category) throws SQLException {
        String sql = "SELECT id, name, category, price, stock FROM products WHERE category = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, category);
        
        System.out.println("  [TableModule] Getting products in category: " + category);
        return stmt.executeQuery();
    }
    
    // Business logic: Calculate discounted price
    public BigDecimal calculateDiscountedPrice(ResultSet rs, BigDecimal discountPercent) throws SQLException {
        BigDecimal price = rs.getBigDecimal("price");
        BigDecimal discount = price.multiply(discountPercent).divide(new BigDecimal("100"));
        return price.subtract(discount);
    }
    
    // Business logic: Check if product is in stock
    public boolean isInStock(ResultSet rs) throws SQLException {
        int stock = rs.getInt("stock");
        return stock > 0;
    }
    
    // Business logic: Update product price with business rules
    public void updatePrice(Long productId, BigDecimal newPrice) throws SQLException {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        
        String sql = "UPDATE products SET price = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newPrice);
            stmt.setLong(2, productId);
            stmt.executeUpdate();
            
            System.out.println("  [TableModule] Updated product " + productId + " price to $" + newPrice);
        }
    }
    
    // Business logic: Restock with validation
    public void restock(Long productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            stmt.executeUpdate();
            
            System.out.println("  [TableModule] Restocked product " + productId + " with " + quantity + " units");
        }
    }
}

/**
 * Example 2: Order Table Module with Complex Logic
 * 
 * Handles orders with calculations and validations
 */
class OrderTableModule {
    private final Connection connection;
    
    public OrderTableModule(Connection connection) {
        this.connection = connection;
    }
    
    // Business logic: Create order
    public Long createOrder(Long customerId, java.sql.Date orderDate) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, order_date, status, total) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, customerId);
            stmt.setDate(2, orderDate);
            stmt.setString(3, "PENDING");
            stmt.setBigDecimal(4, BigDecimal.ZERO);
            stmt.executeUpdate();
            
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long orderId = keys.getLong(1);
                    System.out.println("  [TableModule] Created order: " + orderId);
                    return orderId;
                }
            }
        }
        
        return null;
    }
    
    // Business logic: Add line item
    public void addLineItem(Long orderId, Long productId, int quantity, BigDecimal unitPrice) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, productId);
            stmt.setInt(3, quantity);
            stmt.setBigDecimal(4, unitPrice);
            stmt.executeUpdate();
        }
        
        // Recalculate order total
        recalculateOrderTotal(orderId);
        
        System.out.println("  [TableModule] Added item to order " + orderId);
    }
    
    // Business logic: Calculate order total
    public BigDecimal calculateTotal(Long orderId) throws SQLException {
        String sql = "SELECT SUM(quantity * unit_price) as total FROM order_items WHERE order_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    // Business logic: Recalculate and update total
    private void recalculateOrderTotal(Long orderId) throws SQLException {
        BigDecimal total = calculateTotal(orderId);
        
        String sql = "UPDATE orders SET total = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, total);
            stmt.setLong(2, orderId);
            stmt.executeUpdate();
        }
    }
    
    // Business logic: Get orders by status
    public ResultSet getOrdersByStatus(String status) throws SQLException {
        String sql = "SELECT id, customer_id, order_date, total, status FROM orders WHERE status = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, status);
        
        System.out.println("  [TableModule] Getting orders with status: " + status);
        return stmt.executeQuery();
    }
    
    // Business logic: Apply discount to order
    public void applyDiscount(Long orderId, BigDecimal discountPercent) throws SQLException {
        BigDecimal currentTotal = calculateTotal(orderId);
        BigDecimal discount = currentTotal.multiply(discountPercent).divide(new BigDecimal("100"));
        BigDecimal newTotal = currentTotal.subtract(discount);
        
        String sql = "UPDATE orders SET total = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newTotal);
            stmt.setLong(2, orderId);
            stmt.executeUpdate();
            
            System.out.println("  [TableModule] Applied " + discountPercent + "% discount to order " + orderId);
        }
    }
}

/**
 * Example 3: In-Memory Table Module
 * 
 * Demonstrates pattern without actual database
 */
class InMemoryCustomerTableModule {
    private final Map<Long, CustomerData> customers;
    private long nextId;
    
    static class CustomerData {
        Long id;
        String name;
        String email;
        String tier; // BRONZE, SILVER, GOLD
        BigDecimal totalSpent;
        
        CustomerData(Long id, String name, String email, String tier) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.tier = tier;
            this.totalSpent = BigDecimal.ZERO;
        }
    }
    
    public InMemoryCustomerTableModule() {
        this.customers = new HashMap<>();
        this.nextId = 1L;
    }
    
    // Business logic: Create customer
    public Long createCustomer(String name, String email) {
        Long id = nextId++;
        customers.put(id, new CustomerData(id, name, email, "BRONZE"));
        
        System.out.println("  [TableModule] Created customer: " + id + " - " + name);
        return id;
    }
    
    // Business logic: Get customers by tier
    public List<CustomerData> getCustomersByTier(String tier) {
        List<CustomerData> result = new ArrayList<>();
        
        for (CustomerData customer : customers.values()) {
            if (customer.tier.equals(tier)) {
                result.add(customer);
            }
        }
        
        System.out.println("  [TableModule] Found " + result.size() + " " + tier + " customers");
        return result;
    }
    
    // Business logic: Record purchase and upgrade tier if needed
    public void recordPurchase(Long customerId, BigDecimal amount) {
        CustomerData customer = customers.get(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        customer.totalSpent = customer.totalSpent.add(amount);
        
        // Business rule: Upgrade tier based on total spent
        String oldTier = customer.tier;
        if (customer.totalSpent.compareTo(new BigDecimal("10000")) >= 0) {
            customer.tier = "GOLD";
        } else if (customer.totalSpent.compareTo(new BigDecimal("5000")) >= 0) {
            customer.tier = "SILVER";
        } else {
            customer.tier = "BRONZE";
        }
        
        if (!oldTier.equals(customer.tier)) {
            System.out.println("  [TableModule] Customer " + customerId + " upgraded to " + customer.tier);
        }
        
        System.out.println("  [TableModule] Recorded purchase for customer " + customerId + ": $" + amount);
    }
    
    // Business logic: Calculate discount based on tier
    public BigDecimal calculateDiscount(Long customerId, BigDecimal purchaseAmount) {
        CustomerData customer = customers.get(customerId);
        if (customer == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountPercent = switch (customer.tier) {
            case "GOLD" -> new BigDecimal("0.15"); // 15%
            case "SILVER" -> new BigDecimal("0.10"); // 10%
            case "BRONZE" -> new BigDecimal("0.05"); // 5%
            default -> BigDecimal.ZERO;
        };
        
        BigDecimal discount = purchaseAmount.multiply(discountPercent);
        System.out.println("  [TableModule] " + customer.tier + " discount: $" + discount);
        return discount;
    }
    
    // Business logic: Get total spent by all customers
    public BigDecimal getTotalRevenue() {
        BigDecimal total = customers.values().stream()
            .map(c -> c.totalSpent)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        System.out.println("  [TableModule] Total revenue: $" + total);
        return total;
    }
}

/**
 * Demonstration of the Table Module Pattern
 */
public class TableModulePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Table Module Pattern Demo ===\n");
        
        // Example 1: Product Table Module
        System.out.println("1. Product Table Module (simulated):");
        System.out.println("  [Simulated] ProductTableModule would handle:");
        System.out.println("  - getProductsByCategory(): Query products");
        System.out.println("  - calculateDiscountedPrice(): Apply discount logic");
        System.out.println("  - updatePrice(): Update with validation");
        System.out.println("  - restock(): Add inventory");
        
        // Example 2: Order Table Module
        System.out.println("\n2. Order Table Module (simulated):");
        System.out.println("  [Simulated] OrderTableModule would handle:");
        System.out.println("  - createOrder(): Insert order record");
        System.out.println("  - addLineItem(): Add items and recalculate");
        System.out.println("  - calculateTotal(): Sum line items");
        System.out.println("  - applyDiscount(): Update total with discount");
        
        // Example 3: In-Memory Customer Table Module (working demo)
        System.out.println("\n3. In-Memory Customer Table Module:");
        InMemoryCustomerTableModule customerModule = new InMemoryCustomerTableModule();
        
        Long cust1 = customerModule.createCustomer("Alice", "alice@example.com");
        Long cust2 = customerModule.createCustomer("Bob", "bob@example.com");
        
        customerModule.recordPurchase(cust1, new BigDecimal("1000"));
        customerModule.calculateDiscount(cust1, new BigDecimal("100"));
        
        customerModule.recordPurchase(cust1, new BigDecimal("5000"));
        customerModule.calculateDiscount(cust1, new BigDecimal("100"));
        
        customerModule.recordPurchase(cust1, new BigDecimal("5000"));
        customerModule.calculateDiscount(cust1, new BigDecimal("100"));
        
        List<InMemoryCustomerTableModule.CustomerData> goldCustomers = 
            customerModule.getCustomersByTier("GOLD");
        
        customerModule.getTotalRevenue();
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Organizes logic by table");
        System.out.println("✓ Works well with RecordSets/DataSets");
        System.out.println("✓ Less object creation overhead");
        System.out.println("✓ Good for table-centric designs");
        System.out.println("✓ Common in .NET with DataSets");
        
        System.out.println("\n=== vs Domain Model ===");
        System.out.println("• Table Module: One instance handles all rows");
        System.out.println("• Domain Model: One object per row");
        System.out.println("• Table Module better for simple, table-centric logic");
        System.out.println("• Domain Model better for complex object interactions");
    }
}
