package enterprise;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Domain Model Pattern
 * 
 * Intent: An object model of the domain that incorporates both behavior
 * and data. Business logic is in the domain objects themselves.
 * 
 * Motivation:
 * Rich domain model with objects that contain both data and behavior.
 * Contrast with anemic domain model (just getters/setters).
 * Business rules live in the domain, not in service layer.
 * 
 * Applicability:
 * - Complex business logic
 * - Domain-driven design (DDD)
 * - When business rules need to be close to data
 * - Rich object-oriented models
 */

/**
 * Example 1: Order Domain Model with Business Logic
 * 
 * Order contains business rules: discounts, validation, totals
 */
class Order {
    private final String orderId;
    private final String customerId;
    private final LocalDate orderDate;
    private final List<OrderLine> lines;
    private OrderStatus status;
    
    public Order(String orderId, String customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = LocalDate.now();
        this.lines = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
    }
    
    // Business logic: Add line with validation
    public void addLine(String productId, int quantity, BigDecimal unitPrice) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify order in " + status + " status");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        lines.add(new OrderLine(productId, quantity, unitPrice));
        System.out.println("Added line: " + productId + " x" + quantity);
    }
    
    // Business logic: Calculate total with discounts
    public BigDecimal calculateTotal() {
        BigDecimal subtotal = lines.stream()
            .map(OrderLine::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discount = calculateDiscount(subtotal);
        return subtotal.subtract(discount);
    }
    
    // Business rule: Volume discount
    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(new BigDecimal("1000")) >= 0) {
            return subtotal.multiply(new BigDecimal("0.10")); // 10% discount
        } else if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
            return subtotal.multiply(new BigDecimal("0.05")); // 5% discount
        }
        return BigDecimal.ZERO;
    }
    
    // Business logic: Submit order with validation
    public void submit() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Order already submitted");
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot submit empty order");
        }
        
        status = OrderStatus.SUBMITTED;
        System.out.println("Order " + orderId + " submitted. Total: $" + calculateTotal());
    }
    
    // Business logic: Cancel order
    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel " + status + " order");
        }
        status = OrderStatus.CANCELLED;
        System.out.println("Order " + orderId + " cancelled");
    }
    
    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
}

class OrderLine {
    private final String productId;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    public OrderLine(String productId, int quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}

enum OrderStatus {
    DRAFT, SUBMITTED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

/**
 * Example 2: Account Domain Model with State Transitions
 * 
 * Bank account with business rules for deposits, withdrawals, overdrafts
 */
class BankAccount {
    private final String accountNumber;
    private final String customerName;
    private BigDecimal balance;
    private final BigDecimal overdraftLimit;
    private AccountType type;
    private boolean frozen;
    
    public BankAccount(String accountNumber, String customerName, 
                      AccountType type, BigDecimal overdraftLimit) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.balance = BigDecimal.ZERO;
        this.overdraftLimit = overdraftLimit;
        this.type = type;
        this.frozen = false;
    }
    
    // Business logic: Deposit with validation
    public void deposit(BigDecimal amount) {
        if (frozen) {
            throw new IllegalStateException("Account is frozen");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        balance = balance.add(amount);
        System.out.println("Deposited $" + amount + ". New balance: $" + balance);
    }
    
    // Business logic: Withdraw with overdraft check
    public void withdraw(BigDecimal amount) {
        if (frozen) {
            throw new IllegalStateException("Account is frozen");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        BigDecimal availableBalance = balance.add(overdraftLimit);
        if (amount.compareTo(availableBalance) > 0) {
            throw new IllegalStateException("Insufficient funds. Available: $" + availableBalance);
        }
        
        balance = balance.subtract(amount);
        
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Warning: Account overdrawn");
        }
        
        System.out.println("Withdrew $" + amount + ". New balance: $" + balance);
    }
    
    // Business rule: Calculate interest based on account type
    public BigDecimal calculateMonthlyInterest() {
        BigDecimal rate = switch (type) {
            case SAVINGS -> new BigDecimal("0.02"); // 2% annual
            case CHECKING -> new BigDecimal("0.005"); // 0.5% annual
            case PREMIUM -> new BigDecimal("0.03"); // 3% annual
        };
        
        return balance.multiply(rate).divide(new BigDecimal("12"), 2, java.math.RoundingMode.HALF_UP);
    }
    
    // Business logic: Apply interest
    public void applyMonthlyInterest() {
        BigDecimal interest = calculateMonthlyInterest();
        balance = balance.add(interest);
        System.out.println("Interest applied: $" + interest + ". New balance: $" + balance);
    }
    
    // Business logic: Freeze account
    public void freeze(String reason) {
        frozen = true;
        System.out.println("Account frozen: " + reason);
    }
    
    public BigDecimal getBalance() { return balance; }
}

enum AccountType {
    CHECKING, SAVINGS, PREMIUM
}

/**
 * Example 3: Rental Domain Model with Complex Business Rules
 * 
 * Movie rental with pricing rules, late fees, loyalty points
 */
class MovieRental {
    private final Movie movie;
    private final LocalDate rentalDate;
    private LocalDate returnDate;
    private final int daysRented;
    
    public MovieRental(Movie movie, int daysRented) {
        this.movie = movie;
        this.rentalDate = LocalDate.now();
        this.returnDate = null;
        this.daysRented = daysRented;
    }
    
    // Business logic: Calculate rental price based on movie type
    public BigDecimal calculatePrice() {
        BigDecimal basePrice = new BigDecimal("2.00");
        
        return switch (movie.getPriceCode()) {
            case REGULAR -> {
                if (daysRented > 2) {
                    yield basePrice.add(new BigDecimal(daysRented - 2).multiply(new BigDecimal("1.50")));
                }
                yield basePrice;
            }
            case NEW_RELEASE -> new BigDecimal(daysRented).multiply(new BigDecimal("3.00"));
            case CHILDREN -> {
                if (daysRented > 3) {
                    yield basePrice.add(new BigDecimal(daysRented - 3).multiply(new BigDecimal("1.00")));
                }
                yield basePrice;
            }
        };
    }
    
    // Business logic: Calculate loyalty points
    public int calculateLoyaltyPoints() {
        int points = 1;
        if (movie.getPriceCode() == PriceCode.NEW_RELEASE && daysRented > 1) {
            points = 2; // Bonus point for new releases
        }
        return points;
    }
    
    // Business logic: Return movie and calculate late fee
    public BigDecimal returnMovie() {
        returnDate = LocalDate.now();
        long actualDays = ChronoUnit.DAYS.between(rentalDate, returnDate);
        
        if (actualDays > daysRented) {
            long lateDays = actualDays - daysRented;
            BigDecimal lateFee = new BigDecimal(lateDays).multiply(new BigDecimal("5.00"));
            System.out.println("Late fee: $" + lateFee + " (" + lateDays + " days late)");
            return lateFee;
        }
        
        System.out.println("Movie returned on time");
        return BigDecimal.ZERO;
    }
    
    public Movie getMovie() { return movie; }
}

class Movie {
    private final String title;
    private PriceCode priceCode;
    
    public Movie(String title, PriceCode priceCode) {
        this.title = title;
        this.priceCode = priceCode;
    }
    
    public String getTitle() { return title; }
    public PriceCode getPriceCode() { return priceCode; }
}

enum PriceCode {
    REGULAR, NEW_RELEASE, CHILDREN
}

class Customer {
    private final String name;
    private final List<MovieRental> rentals;
    private int loyaltyPoints;
    
    public Customer(String name) {
        this.name = name;
        this.rentals = new ArrayList<>();
        this.loyaltyPoints = 0;
    }
    
    // Business logic: Add rental
    public void addRental(MovieRental rental) {
        rentals.add(rental);
        loyaltyPoints += rental.calculateLoyaltyPoints();
    }
    
    // Business logic: Generate statement
    public String statement() {
        StringBuilder result = new StringBuilder("Rental Record for " + name + "\n");
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (MovieRental rental : rentals) {
            BigDecimal thisAmount = rental.calculatePrice();
            totalAmount = totalAmount.add(thisAmount);
            
            result.append("\t")
                  .append(rental.getMovie().getTitle())
                  .append("\t$")
                  .append(thisAmount)
                  .append("\n");
        }
        
        result.append("Amount owed: $").append(totalAmount).append("\n");
        result.append("Loyalty points earned: ").append(loyaltyPoints);
        
        return result.toString();
    }
}

/**
 * Example 4: Invoice Domain Model with Tax Calculations
 * 
 * Invoice with line items, tax rules, payment tracking
 */
class Invoice {
    private final String invoiceNumber;
    private final LocalDate invoiceDate;
    private final LocalDate dueDate;
    private final List<InvoiceLine> lines;
    private final String taxRegion;
    private BigDecimal amountPaid;
    private InvoiceStatus status;
    
    public Invoice(String invoiceNumber, String taxRegion, int paymentTermDays) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = LocalDate.now();
        this.dueDate = invoiceDate.plusDays(paymentTermDays);
        this.lines = new ArrayList<>();
        this.taxRegion = taxRegion;
        this.amountPaid = BigDecimal.ZERO;
        this.status = InvoiceStatus.DRAFT;
    }
    
    public void addLine(String description, BigDecimal amount, boolean taxable) {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify finalized invoice");
        }
        lines.add(new InvoiceLine(description, amount, taxable));
    }
    
    // Business logic: Calculate tax based on region
    private BigDecimal calculateTax() {
        BigDecimal taxableAmount = lines.stream()
            .filter(InvoiceLine::isTaxable)
            .map(InvoiceLine::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxRate = switch (taxRegion) {
            case "US-CA" -> new BigDecimal("0.0725"); // California
            case "US-NY" -> new BigDecimal("0.08"); // New York
            case "US-TX" -> new BigDecimal("0.0625"); // Texas
            default -> BigDecimal.ZERO;
        };
        
        return taxableAmount.multiply(taxRate);
    }
    
    // Business logic: Calculate total
    public BigDecimal calculateTotal() {
        BigDecimal subtotal = lines.stream()
            .map(InvoiceLine::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal tax = calculateTax();
        return subtotal.add(tax);
    }
    
    // Business logic: Finalize invoice
    public void finalize() {
        if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot finalize empty invoice");
        }
        status = InvoiceStatus.SENT;
        System.out.println("Invoice " + invoiceNumber + " finalized. Total: $" + calculateTotal());
    }
    
    // Business logic: Record payment
    public void recordPayment(BigDecimal amount) {
        if (status == InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Cannot pay draft invoice");
        }
        
        amountPaid = amountPaid.add(amount);
        
        if (amountPaid.compareTo(calculateTotal()) >= 0) {
            status = InvoiceStatus.PAID;
            System.out.println("Invoice fully paid");
        } else {
            status = InvoiceStatus.PARTIAL_PAYMENT;
            System.out.println("Partial payment received. Remaining: $" + 
                             calculateTotal().subtract(amountPaid));
        }
    }
    
    // Business logic: Check if overdue
    public boolean isOverdue() {
        return status != InvoiceStatus.PAID && LocalDate.now().isAfter(dueDate);
    }
}

class InvoiceLine {
    private final String description;
    private final BigDecimal amount;
    private final boolean taxable;
    
    public InvoiceLine(String description, BigDecimal amount, boolean taxable) {
        this.description = description;
        this.amount = amount;
        this.taxable = taxable;
    }
    
    public BigDecimal getAmount() { return amount; }
    public boolean isTaxable() { return taxable; }
}

enum InvoiceStatus {
    DRAFT, SENT, PARTIAL_PAYMENT, PAID, OVERDUE
}

/**
 * Demonstration of the Domain Model Pattern
 */
public class DomainModelPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Domain Model Pattern Demo ===\n");
        
        // Example 1: Order with business logic
        System.out.println("1. Order Domain Model:");
        Order order = new Order("ORD-001", "CUST-123");
        order.addLine("PROD-1", 5, new BigDecimal("100.00"));
        order.addLine("PROD-2", 2, new BigDecimal("150.00"));
        System.out.println("Total: $" + order.calculateTotal());
        order.submit();
        
        // Example 2: Bank account with state
        System.out.println("\n2. Bank Account Domain Model:");
        BankAccount account = new BankAccount("ACC-001", "John Doe", 
            AccountType.SAVINGS, new BigDecimal("500"));
        account.deposit(new BigDecimal("1000"));
        account.withdraw(new BigDecimal("200"));
        account.applyMonthlyInterest();
        
        // Example 3: Movie rental
        System.out.println("\n3. Movie Rental Domain Model:");
        Customer customer = new Customer("Alice");
        customer.addRental(new MovieRental(
            new Movie("The Matrix", PriceCode.NEW_RELEASE), 3));
        customer.addRental(new MovieRental(
            new Movie("Toy Story", PriceCode.CHILDREN), 2));
        System.out.println(customer.statement());
        
        // Example 4: Invoice
        System.out.println("\n4. Invoice Domain Model:");
        Invoice invoice = new Invoice("INV-001", "US-CA", 30);
        invoice.addLine("Consulting services", new BigDecimal("1000"), true);
        invoice.addLine("Travel expenses", new BigDecimal("200"), false);
        invoice.finalize();
        invoice.recordPayment(new BigDecimal("500"));
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Business logic encapsulated in domain objects");
        System.out.println("✓ Rich object model (not anemic)");
        System.out.println("✓ Business rules close to data");
        System.out.println("✓ Easier to test and maintain");
        System.out.println("✓ Core pattern in Domain-Driven Design");
    }
}
