package enterprise;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Transaction Script Pattern
 * 
 * Intent: Organizes business logic by procedures where each procedure
 * handles a single request from the presentation layer.
 * 
 * Motivation:
 * Simple procedural approach to business logic.
 * Each transaction is a script that runs in a single procedure.
 * Good for simple business logic without complex domain models.
 * 
 * Applicability:
 * - Simple business logic
 * - CRUD-heavy applications
 * - When domain model would be overkill
 * - Quick prototypes and simple apps
 */

/**
 * Example 1: Order Processing Transaction Scripts
 * 
 * Each business operation is a separate procedure
 */
class OrderService {
    private final Connection connection;
    
    public OrderService(Connection connection) {
        this.connection = connection;
    }
    
    // Transaction script: Create new order
    public Long createOrder(Long customerId, List<OrderItem> items) {
        try {
            connection.setAutoCommit(false);
            
            // Insert order
            String orderSql = "INSERT INTO orders (customer_id, order_date, status) VALUES (?, ?, ?)";
            Long orderId;
            
            try (PreparedStatement stmt = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, customerId);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setString(3, "PENDING");
                stmt.executeUpdate();
                
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getLong(1);
                    } else {
                        throw new SQLException("Failed to get order ID");
                    }
                }
            }
            
            // Insert order items
            String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(itemSql)) {
                for (OrderItem item : items) {
                    stmt.setLong(1, orderId);
                    stmt.setLong(2, item.productId);
                    stmt.setInt(3, item.quantity);
                    stmt.setBigDecimal(4, item.price);
                    stmt.executeUpdate();
                }
            }
            
            connection.commit();
            System.out.println("  [Script] Order created: " + orderId + " with " + items.size() + " items");
            return orderId;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Error creating order: " + e.getMessage());
            return null;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
    
    // Transaction script: Calculate order total
    public BigDecimal calculateOrderTotal(Long orderId) {
        String sql = "SELECT SUM(quantity * price) as total FROM order_items WHERE order_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    System.out.println("  [Script] Order " + orderId + " total: $" + total);
                    return total;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    // Transaction script: Ship order
    public void shipOrder(Long orderId, String trackingNumber) {
        try {
            connection.setAutoCommit(false);
            
            // Update order status
            String orderSql = "UPDATE orders SET status = ?, shipped_date = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(orderSql)) {
                stmt.setString(1, "SHIPPED");
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setLong(3, orderId);
                stmt.executeUpdate();
            }
            
            // Insert shipment record
            String shipSql = "INSERT INTO shipments (order_id, tracking_number, ship_date) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(shipSql)) {
                stmt.setLong(1, orderId);
                stmt.setString(2, trackingNumber);
                stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                stmt.executeUpdate();
            }
            
            connection.commit();
            System.out.println("  [Script] Order " + orderId + " shipped: " + trackingNumber);
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Error shipping order: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
}

class OrderItem {
    Long productId;
    int quantity;
    BigDecimal price;
    
    public OrderItem(Long productId, int quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}

/**
 * Example 2: Account Transfer Transaction Script
 * 
 * Classic money transfer between accounts
 */
class BankingService {
    private final Connection connection;
    
    public BankingService(Connection connection) {
        this.connection = connection;
    }
    
    // Transaction script: Transfer money between accounts
    public boolean transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        try {
            connection.setAutoCommit(false);
            
            // Check source account balance
            String checkSql = "SELECT balance FROM accounts WHERE id = ? FOR UPDATE";
            BigDecimal fromBalance;
            
            try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
                stmt.setLong(1, fromAccountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Source account not found");
                    }
                    fromBalance = rs.getBigDecimal("balance");
                }
            }
            
            if (fromBalance.compareTo(amount) < 0) {
                System.out.println("  [Script] Insufficient funds");
                connection.rollback();
                return false;
            }
            
            // Debit source account
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(debitSql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setLong(2, fromAccountId);
                stmt.executeUpdate();
            }
            
            // Credit destination account
            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(creditSql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setLong(2, toAccountId);
                stmt.executeUpdate();
            }
            
            // Record transaction
            String txnSql = "INSERT INTO transactions (from_account, to_account, amount, txn_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(txnSql)) {
                stmt.setLong(1, fromAccountId);
                stmt.setLong(2, toAccountId);
                stmt.setBigDecimal(3, amount);
                stmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
                stmt.executeUpdate();
            }
            
            connection.commit();
            System.out.println("  [Script] Transferred $" + amount + " from " + fromAccountId + " to " + toAccountId);
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            System.err.println("Transfer failed: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
    
    // Transaction script: Get account balance
    public BigDecimal getBalance(Long accountId) {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    System.out.println("  [Script] Account " + accountId + " balance: $" + balance);
                    return balance;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting balance: " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
}

/**
 * Example 3: In-Memory Transaction Scripts
 * 
 * Demonstrates pattern without actual database
 */
class InMemoryOrderService {
    private final Map<Long, SimulatedOrder> orders;
    private final Map<Long, BigDecimal> inventory;
    private long nextOrderId;
    
    public InMemoryOrderService() {
        this.orders = new HashMap<>();
        this.inventory = new HashMap<>();
        this.nextOrderId = 1L;
        
        // Initialize some inventory
        inventory.put(1L, new BigDecimal("100"));
        inventory.put(2L, new BigDecimal("50"));
    }
    
    // Transaction script: Place order
    public Long placeOrder(Long customerId, List<OrderItem> items) {
        // Validate inventory
        for (OrderItem item : items) {
            BigDecimal available = inventory.getOrDefault(item.productId, BigDecimal.ZERO);
            if (available.compareTo(new BigDecimal(item.quantity)) < 0) {
                System.out.println("  [Script] Insufficient inventory for product: " + item.productId);
                return null;
            }
        }
        
        // Deduct inventory
        for (OrderItem item : items) {
            BigDecimal current = inventory.get(item.productId);
            inventory.put(item.productId, current.subtract(new BigDecimal(item.quantity)));
        }
        
        // Create order
        Long orderId = nextOrderId++;
        BigDecimal total = items.stream()
            .map(item -> item.price.multiply(new BigDecimal(item.quantity)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        orders.put(orderId, new SimulatedOrder(orderId, customerId, total, "PLACED"));
        
        System.out.println("  [Script] Order placed: " + orderId + " for customer " + customerId + ", total: $" + total);
        return orderId;
    }
    
    // Transaction script: Cancel order
    public boolean cancelOrder(Long orderId) {
        SimulatedOrder order = orders.get(orderId);
        if (order == null) {
            System.out.println("  [Script] Order not found: " + orderId);
            return false;
        }
        
        if ("SHIPPED".equals(order.status)) {
            System.out.println("  [Script] Cannot cancel shipped order");
            return false;
        }
        
        order.status = "CANCELLED";
        System.out.println("  [Script] Order cancelled: " + orderId);
        return true;
    }
    
    // Transaction script: Get order status
    public String getOrderStatus(Long orderId) {
        SimulatedOrder order = orders.get(orderId);
        if (order == null) {
            return "NOT_FOUND";
        }
        
        System.out.println("  [Script] Order " + orderId + " status: " + order.status);
        return order.status;
    }
}

class SimulatedOrder {
    Long orderId;
    Long customerId;
    BigDecimal total;
    String status;
    
    public SimulatedOrder(Long orderId, Long customerId, BigDecimal total, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.total = total;
        this.status = status;
    }
}

/**
 * Demonstration of the Transaction Script Pattern
 */
public class TransactionScriptPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Transaction Script Pattern Demo ===\n");
        
        // Example 1: Simulated Order Service
        System.out.println("1. Order Transaction Scripts (simulated):");
        System.out.println("  [Simulated] OrderService would handle:");
        System.out.println("  - createOrder(): Insert order + items in transaction");
        System.out.println("  - calculateOrderTotal(): Sum order items");
        System.out.println("  - shipOrder(): Update status + create shipment");
        
        // Example 2: Simulated Banking Service
        System.out.println("\n2. Banking Transaction Scripts (simulated):");
        System.out.println("  [Simulated] BankingService would handle:");
        System.out.println("  - transferMoney(): Debit + credit + record transaction");
        System.out.println("  - getBalance(): Query account balance");
        System.out.println("  - All in single database transaction");
        
        // Example 3: In-Memory Order Service (working demo)
        System.out.println("\n3. In-Memory Order Service:");
        InMemoryOrderService service = new InMemoryOrderService();
        
        List<OrderItem> items = Arrays.asList(
            new OrderItem(1L, 5, new BigDecimal("10.00")),
            new OrderItem(2L, 2, new BigDecimal("25.00"))
        );
        
        Long orderId = service.placeOrder(100L, items);
        
        if (orderId != null) {
            service.getOrderStatus(orderId);
            service.cancelOrder(orderId);
            service.getOrderStatus(orderId);
        }
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Simple and straightforward");
        System.out.println("✓ Good for simple business logic");
        System.out.println("✓ Easy to understand and test");
        System.out.println("✓ Less overhead than domain model");
        System.out.println("✓ Ideal for CRUD applications");
        
        System.out.println("\n=== When to Use ===");
        System.out.println("• Simple business logic");
        System.out.println("• Few business rules");
        System.out.println("• CRUD-heavy applications");
        System.out.println("• Rapid prototyping");
    }
}
