package architectural;

import java.util.*;

/**
 * Onion Architecture Pattern
 * ===========================
 * 
 * Intent:
 * Organizes code into concentric circles where the domain model is at the center,
 * surrounded by domain services, then application services, and finally
 * infrastructure. All dependencies point inward toward the domain.
 * 
 * Also Known As:
 * - Concentric Architecture
 * - Layered Domain-Centric Architecture
 * 
 * Motivation:
 * - Put domain logic at the center (most important)
 * - Protect domain from infrastructure concerns
 * - Dependencies point inward (like Clean Architecture)
 * - Domain is completely independent and testable
 * 
 * Applicability:
 * - Domain-driven design applications
 * - Complex business logic applications
 * - Systems requiring high testability
 * - Long-lived enterprise applications
 * 
 * Structure (from inside out):
 * 1. Domain Model (center) - Pure domain entities and value objects
 * 2. Domain Services - Domain logic that doesn't fit in entities
 * 3. Application Services - Use cases and orchestration
 * 4. Infrastructure - External concerns (DB, UI, APIs)
 * 
 * Key Differences from Other Architectures:
 * - Hexagonal: Similar, but Onion explicitly shows layers as circles
 * - Clean: Very similar, Onion emphasizes domain at center
 * - Layered: Traditional layers depend downward, Onion depends inward
 */

// ============================================================================
// LAYER 1: DOMAIN MODEL (Core - innermost circle)
// ============================================================================

// Value Object - immutable, compared by value
class Money {
    private final double amount;
    private final String currency;
    
    public Money(double amount, String currency) {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        this.amount = amount;
        this.currency = currency;
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount + other.amount, this.currency);
    }
    
    public Money multiply(int quantity) {
        return new Money(this.amount * quantity, this.currency);
    }
    
    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare different currencies");
        }
        return this.amount > other.amount;
    }
    
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    
    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Money)) return false;
        Money other = (Money) obj;
        return this.amount == other.amount && this.currency.equals(other.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}

// Domain Entity - has identity, mutable
class Customer {
    private final String id;
    private String name;
    private String email;
    private CustomerType type;
    
    public Customer(String id, String name, String email, CustomerType type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
    }
    
    public double getDiscountRate() {
        switch (type) {
            case PREMIUM: return 0.15;
            case REGULAR: return 0.05;
            case NEW: return 0.0;
            default: return 0.0;
        }
    }
    
    public void upgradeToPremuim() {
        this.type = CustomerType.PREMIUM;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public CustomerType getType() { return type; }
}

enum CustomerType {
    NEW, REGULAR, PREMIUM
}

// Domain Entity - Aggregate Root
class PurchaseOrder {
    private final String orderId;
    private final Customer customer;
    private final List<OrderLine> lines;
    private OrderStatus status;
    private Money totalAmount;
    
    public PurchaseOrder(String orderId, Customer customer) {
        this.orderId = orderId;
        this.customer = customer;
        this.lines = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
        this.totalAmount = new Money(0.0, "USD");
    }
    
    // Domain logic in entity
    public void addLine(String productId, String productName, int quantity, Money unitPrice) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify order in status: " + status);
        }
        
        OrderLine line = new OrderLine(productId, productName, quantity, unitPrice);
        lines.add(line);
        recalculateTotal();
    }
    
    public void submit() {
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot submit empty order");
        }
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Order already submitted");
        }
        status = OrderStatus.SUBMITTED;
    }
    
    public void approve() {
        if (status != OrderStatus.SUBMITTED) {
            throw new IllegalStateException("Can only approve submitted orders");
        }
        status = OrderStatus.APPROVED;
    }
    
    private void recalculateTotal() {
        Money subtotal = new Money(0.0, "USD");
        for (OrderLine line : lines) {
            subtotal = subtotal.add(line.getLineTotal());
        }
        
        // Apply customer discount
        double discountRate = customer.getDiscountRate();
        double discountedAmount = subtotal.getAmount() * (1 - discountRate);
        this.totalAmount = new Money(discountedAmount, "USD");
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public Customer getCustomer() { return customer; }
    public List<OrderLine> getLines() { return new ArrayList<>(lines); }
    public OrderStatus getStatus() { return status; }
    public Money getTotalAmount() { return totalAmount; }
}

class OrderLine {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;
    
    public OrderLine(String productId, String productName, int quantity, Money unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public Money getLineTotal() {
        return unitPrice.multiply(quantity);
    }
    
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
}

enum OrderStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED, COMPLETED
}

// ============================================================================
// LAYER 2: DOMAIN SERVICES (operate on domain objects)
// ============================================================================

// Domain service - logic that doesn't naturally fit in any entity
class PricingService {
    public Money calculateBulkDiscount(Money basePrice, int quantity) {
        double discountRate;
        if (quantity >= 100) {
            discountRate = 0.20; // 20% discount
        } else if (quantity >= 50) {
            discountRate = 0.10; // 10% discount
        } else if (quantity >= 10) {
            discountRate = 0.05; // 5% discount
        } else {
            discountRate = 0.0;
        }
        
        double discountedAmount = basePrice.getAmount() * (1 - discountRate);
        return new Money(discountedAmount, basePrice.getCurrency());
    }
    
    public Money applySeasonalDiscount(Money price, String season) {
        double rate = season.equals("Holiday") ? 0.15 : 0.0;
        double discounted = price.getAmount() * (1 - rate);
        return new Money(discounted, price.getCurrency());
    }
}

// ============================================================================
// LAYER 3: APPLICATION SERVICES (use cases and orchestration)
// ============================================================================

// Repository interface (defined in application layer, implemented in infrastructure)
interface ICustomerRepository {
    Customer findById(String id);
    void save(Customer customer);
}

interface IOrderRepository {
    PurchaseOrder findById(String orderId);
    void save(PurchaseOrder order);
    String generateOrderId();
}

// Application Service - orchestrates use cases
class OrderApplicationService {
    private final IOrderRepository orderRepository;
    private final ICustomerRepository customerRepository;
    private final PricingService pricingService;
    
    public OrderApplicationService(IOrderRepository orderRepository,
                                   ICustomerRepository customerRepository,
                                   PricingService pricingService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.pricingService = pricingService;
    }
    
    // Use Case: Create Order
    public String createOrder(String customerId) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        
        String orderId = orderRepository.generateOrderId();
        PurchaseOrder order = new PurchaseOrder(orderId, customer);
        
        orderRepository.save(order);
        System.out.println("Order created: " + orderId + " for customer: " + customer.getName());
        
        return orderId;
    }
    
    // Use Case: Add Product to Order
    public void addProductToOrder(String orderId, String productId, String productName, 
                                  int quantity, double unitPrice) {
        PurchaseOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        Money price = new Money(unitPrice, "USD");
        
        // Apply bulk discount (domain service)
        Money discountedPrice = pricingService.calculateBulkDiscount(price, quantity);
        
        order.addLine(productId, productName, quantity, discountedPrice);
        orderRepository.save(order);
        
        System.out.println("Added " + quantity + "x " + productName + " to order " + orderId);
    }
    
    // Use Case: Submit Order for Approval
    public void submitOrder(String orderId) {
        PurchaseOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        order.submit();
        orderRepository.save(order);
        
        System.out.println("Order submitted: " + orderId + 
                         ", Total: " + order.getTotalAmount());
    }
    
    // Use Case: Approve Order
    public void approveOrder(String orderId) {
        PurchaseOrder order = orderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        
        // Check if total exceeds approval limit
        Money approvalLimit = new Money(10000.0, "USD");
        if (order.getTotalAmount().isGreaterThan(approvalLimit)) {
            System.out.println("Order requires senior approval: " + orderId);
            return;
        }
        
        order.approve();
        orderRepository.save(order);
        
        System.out.println("Order approved: " + orderId);
    }
}

// ============================================================================
// LAYER 4: INFRASTRUCTURE (outermost circle)
// ============================================================================

// Infrastructure - Repository implementations
class InMemoryCustomerRepository implements ICustomerRepository {
    private final Map<String, Customer> customers = new HashMap<>();
    
    public InMemoryCustomerRepository() {
        // Seed with test data
        customers.put("C001", new Customer("C001", "Alice Johnson", "alice@example.com", CustomerType.PREMIUM));
        customers.put("C002", new Customer("C002", "Bob Smith", "bob@example.com", CustomerType.REGULAR));
        customers.put("C003", new Customer("C003", "Charlie Brown", "charlie@example.com", CustomerType.NEW));
    }
    
    @Override
    public Customer findById(String id) {
        return customers.get(id);
    }
    
    @Override
    public void save(Customer customer) {
        customers.put(customer.getId(), customer);
    }
}

class InMemoryOrderRepository implements IOrderRepository {
    private final Map<String, PurchaseOrder> orders = new HashMap<>();
    private int orderCounter = 1000;
    
    @Override
    public PurchaseOrder findById(String orderId) {
        return orders.get(orderId);
    }
    
    @Override
    public void save(PurchaseOrder order) {
        orders.put(order.getOrderId(), order);
    }
    
    @Override
    public String generateOrderId() {
        return "ORD-" + (orderCounter++);
    }
}

// Infrastructure - UI/Controller
class OrderController {
    private final OrderApplicationService orderService;
    
    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }
    
    public void handleCreateOrder(String customerId) {
        System.out.println("[UI] Creating order for customer: " + customerId);
        String orderId = orderService.createOrder(customerId);
        System.out.println("[UI] Order ID: " + orderId);
    }
    
    public void handleAddProduct(String orderId, String productId, String productName, 
                                int quantity, double unitPrice) {
        System.out.println("[UI] Adding product to order " + orderId);
        orderService.addProductToOrder(orderId, productId, productName, quantity, unitPrice);
    }
    
    public void handleSubmitOrder(String orderId) {
        System.out.println("[UI] Submitting order: " + orderId);
        orderService.submitOrder(orderId);
    }
    
    public void handleApproveOrder(String orderId) {
        System.out.println("[UI] Approving order: " + orderId);
        orderService.approveOrder(orderId);
    }
}

/**
 * Demonstration of Onion Architecture Pattern
 */
public class OnionArchitecturePattern {
    public static void main(String[] args) {
        demonstrateOnionArchitecture();
    }
    
    private static void demonstrateOnionArchitecture() {
        System.out.println("=== Onion Architecture: Purchase Order System ===\n");
        
        // Layer 4: Infrastructure - Create repositories
        ICustomerRepository customerRepo = new InMemoryCustomerRepository();
        IOrderRepository orderRepo = new InMemoryOrderRepository();
        
        // Layer 2: Domain Services
        PricingService pricingService = new PricingService();
        
        // Layer 3: Application Services
        OrderApplicationService orderService = new OrderApplicationService(
            orderRepo, customerRepo, pricingService
        );
        
        // Layer 4: Infrastructure - UI Controller
        OrderController controller = new OrderController(orderService);
        
        // Scenario 1: Premium customer with bulk discount
        System.out.println("=== Scenario 1: Premium Customer ===\n");
        controller.handleCreateOrder("C001");
        controller.handleAddProduct("ORD-1000", "P001", "Laptop", 5, 1000.0);
        controller.handleAddProduct("ORD-1000", "P002", "Mouse", 50, 25.0);
        controller.handleSubmitOrder("ORD-1000");
        controller.handleApproveOrder("ORD-1000");
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Scenario 2: Regular customer, small order
        System.out.println("=== Scenario 2: Regular Customer ===\n");
        controller.handleCreateOrder("C002");
        controller.handleAddProduct("ORD-1001", "P003", "Keyboard", 2, 80.0);
        controller.handleSubmitOrder("ORD-1001");
        controller.handleApproveOrder("ORD-1001");
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Scenario 3: New customer, large order requiring approval
        System.out.println("=== Scenario 3: New Customer (Large Order) ===\n");
        controller.handleCreateOrder("C003");
        controller.handleAddProduct("ORD-1002", "P004", "Server", 20, 5000.0);
        controller.handleSubmitOrder("ORD-1002");
        controller.handleApproveOrder("ORD-1002");
        
        System.out.println("\n=== Architecture Layers ===");
        System.out.println("1. Domain Model (center): Customer, PurchaseOrder, OrderLine, Money");
        System.out.println("2. Domain Services: PricingService (bulk discounts)");
        System.out.println("3. Application Services: OrderApplicationService (use cases)");
        System.out.println("4. Infrastructure (outer): Repositories, Controllers");
        System.out.println("\nAll dependencies point INWARD toward the domain!");
    }
}
