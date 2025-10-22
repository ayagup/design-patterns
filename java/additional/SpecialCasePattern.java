package additional;

import java.util.*;

/**
 * Special Case Pattern (Null Object Pattern)
 * 
 * Intent: Provides a special subclass to handle special cases, eliminating
 * the need for null checks and special conditional logic.
 * 
 * Motivation:
 * Eliminates null checks throughout code.
 * Provides default behavior for special cases.
 * Simplifies client code.
 * Reduces conditional complexity.
 * 
 * Applicability:
 * - Many null checks in code
 * - Default behavior for missing objects
 * - Special handling for edge cases
 * - Eliminating conditional logic
 */

/**
 * Example 1: Null Object Pattern
 * 
 * Basic null object implementation
 */
interface Customer {
    String getName();
    String getEmail();
    boolean isNull();
}

class RealCustomer implements Customer {
    private final String name;
    private final String email;
    
    public RealCustomer(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    @Override
    public String getName() { return name; }
    
    @Override
    public String getEmail() { return email; }
    
    @Override
    public boolean isNull() { return false; }
}

class NullCustomer implements Customer {
    @Override
    public String getName() { return "Guest"; }
    
    @Override
    public String getEmail() { return "no-reply@example.com"; }
    
    @Override
    public boolean isNull() { return true; }
}

class CustomerRepository {
    private final Map<String, Customer> customers = new HashMap<>();
    
    public CustomerRepository() {
        customers.put("alice", new RealCustomer("Alice", "alice@example.com"));
        customers.put("bob", new RealCustomer("Bob", "bob@example.com"));
    }
    
    public Customer getCustomer(String id) {
        Customer customer = customers.get(id);
        return customer != null ? customer : new NullCustomer();
    }
}

/**
 * Example 2: Missing User Special Case
 * 
 * Handles missing user scenario
 */
interface User {
    String getUsername();
    String getRole();
    boolean hasPermission(String permission);
    void log(String message);
}

class RealUser implements User {
    private final String username;
    private final String role;
    private final Set<String> permissions;
    
    public RealUser(String username, String role, Set<String> permissions) {
        this.username = username;
        this.role = role;
        this.permissions = permissions;
    }
    
    @Override
    public String getUsername() { return username; }
    
    @Override
    public String getRole() { return role; }
    
    @Override
    public boolean hasPermission(String permission) {
        boolean has = permissions.contains(permission);
        System.out.println("  [RealUser] " + username + " permission '" + permission + "': " + has);
        return has;
    }
    
    @Override
    public void log(String message) {
        System.out.println("  [Log] " + username + ": " + message);
    }
}

class AnonymousUser implements User {
    @Override
    public String getUsername() { return "anonymous"; }
    
    @Override
    public String getRole() { return "guest"; }
    
    @Override
    public boolean hasPermission(String permission) {
        System.out.println("  [AnonymousUser] No permissions granted");
        return false;
    }
    
    @Override
    public void log(String message) {
        // Anonymous users don't log
    }
}

/**
 * Example 3: Discount Special Cases
 * 
 * Different discount strategies including null discount
 */
interface Discount {
    double apply(double price);
    String getDescription();
}

class PercentageDiscount implements Discount {
    private final double percentage;
    
    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }
    
    @Override
    public double apply(double price) {
        double discounted = price * (1 - percentage / 100);
        System.out.println("  [PercentageDiscount] " + percentage + "% off: $" + 
                         String.format("%.2f", price) + " -> $" + String.format("%.2f", discounted));
        return discounted;
    }
    
    @Override
    public String getDescription() {
        return percentage + "% discount";
    }
}

class FixedAmountDiscount implements Discount {
    private final double amount;
    
    public FixedAmountDiscount(double amount) {
        this.amount = amount;
    }
    
    @Override
    public double apply(double price) {
        double discounted = Math.max(0, price - amount);
        System.out.println("  [FixedAmountDiscount] $" + amount + " off: $" + 
                         String.format("%.2f", price) + " -> $" + String.format("%.2f", discounted));
        return discounted;
    }
    
    @Override
    public String getDescription() {
        return "$" + amount + " discount";
    }
}

class NoDiscount implements Discount {
    @Override
    public double apply(double price) {
        System.out.println("  [NoDiscount] No discount applied: $" + String.format("%.2f", price));
        return price;
    }
    
    @Override
    public String getDescription() {
        return "No discount";
    }
}

/**
 * Example 4: Logger Special Cases
 * 
 * Different logging behaviors including null logger
 */
interface Logger {
    void info(String message);
    void error(String message);
    void debug(String message);
}

class ConsoleLogger implements Logger {
    @Override
    public void info(String message) {
        System.out.println("  [INFO] " + message);
    }
    
    @Override
    public void error(String message) {
        System.out.println("  [ERROR] " + message);
    }
    
    @Override
    public void debug(String message) {
        System.out.println("  [DEBUG] " + message);
    }
}

class NullLogger implements Logger {
    @Override
    public void info(String message) {
        // Do nothing
    }
    
    @Override
    public void error(String message) {
        // Do nothing
    }
    
    @Override
    public void debug(String message) {
        // Do nothing
    }
}

class Application {
    private final Logger logger;
    
    public Application(Logger logger) {
        this.logger = logger;
    }
    
    public void run() {
        logger.info("Application started");
        logger.debug("Initializing components");
        // No null checks needed!
        logger.info("Application running");
    }
}

/**
 * Example 5: Collection Special Cases
 * 
 * Empty collection as special case
 */
interface ProductList {
    void add(Product product);
    List<Product> getProducts();
    double getTotalPrice();
    boolean isEmpty();
}

class StandardProductList implements ProductList {
    private final List<Product> products;
    
    public StandardProductList() {
        this.products = new ArrayList<>();
    }
    
    @Override
    public void add(Product product) {
        products.add(product);
        System.out.println("  [ProductList] Added: " + product.getName());
    }
    
    @Override
    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }
    
    @Override
    public double getTotalPrice() {
        double total = products.stream().mapToDouble(Product::getPrice).sum();
        System.out.println("  [ProductList] Total: $" + String.format("%.2f", total));
        return total;
    }
    
    @Override
    public boolean isEmpty() {
        return products.isEmpty();
    }
}

class EmptyProductList implements ProductList {
    @Override
    public void add(Product product) {
        // Cannot add to empty list
        System.out.println("  [EmptyProductList] Cannot add products");
    }
    
    @Override
    public List<Product> getProducts() {
        return Collections.emptyList();
    }
    
    @Override
    public double getTotalPrice() {
        System.out.println("  [EmptyProductList] Total: $0.00");
        return 0.0;
    }
    
    @Override
    public boolean isEmpty() {
        return true;
    }
}

class Product {
    private final String name;
    private final double price;
    
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
}

/**
 * Example 6: Payment Method Special Cases
 * 
 * Different payment methods including unpaid
 */
interface PaymentMethod {
    boolean process(double amount);
    String getReceipt();
}

class CreditCardPayment implements PaymentMethod {
    private final String cardNumber;
    
    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    @Override
    public boolean process(double amount) {
        System.out.println("  [CreditCard] Processing $" + String.format("%.2f", amount) + 
                         " on card " + cardNumber);
        return true;
    }
    
    @Override
    public String getReceipt() {
        return "Paid by Credit Card ending in " + cardNumber.substring(cardNumber.length() - 4);
    }
}

class UnpaidPayment implements PaymentMethod {
    @Override
    public boolean process(double amount) {
        System.out.println("  [Unpaid] Order placed without payment: $" + String.format("%.2f", amount));
        return false;
    }
    
    @Override
    public String getReceipt() {
        return "Payment pending";
    }
}

/**
 * Demonstration of the Special Case Pattern
 */
public class SpecialCasePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Special Case Pattern Demo ===\n");
        
        // Example 1: Null Customer
        System.out.println("1. Null Object Pattern:");
        CustomerRepository repo = new CustomerRepository();
        
        Customer alice = repo.getCustomer("alice");
        System.out.println("  Customer: " + alice.getName() + " (" + alice.getEmail() + ")");
        
        Customer unknown = repo.getCustomer("unknown");
        System.out.println("  Customer: " + unknown.getName() + " (" + unknown.getEmail() + ")");
        System.out.println("  Is null object: " + unknown.isNull());
        
        // Example 2: Anonymous User
        System.out.println("\n2. Missing User Special Case:");
        User admin = new RealUser("admin", "administrator", 
            new HashSet<>(Arrays.asList("read", "write", "delete")));
        User anonymous = new AnonymousUser();
        
        admin.hasPermission("write");
        anonymous.hasPermission("write");
        
        admin.log("Performed admin action");
        anonymous.log("This won't be logged");
        
        // Example 3: Discount Special Cases
        System.out.println("\n3. Discount Special Cases:");
        Discount percentDiscount = new PercentageDiscount(20);
        Discount fixedDiscount = new FixedAmountDiscount(15);
        Discount noDiscount = new NoDiscount();
        
        double price = 100.0;
        percentDiscount.apply(price);
        fixedDiscount.apply(price);
        noDiscount.apply(price);
        
        // Example 4: Null Logger
        System.out.println("\n4. Logger Special Cases:");
        System.out.println("  With Console Logger:");
        Application app1 = new Application(new ConsoleLogger());
        app1.run();
        
        System.out.println("\n  With Null Logger (no output):");
        Application app2 = new Application(new NullLogger());
        app2.run();
        System.out.println("  [Main] Application ran without logging");
        
        // Example 5: Empty Collection
        System.out.println("\n5. Collection Special Cases:");
        ProductList normalList = new StandardProductList();
        normalList.add(new Product("Laptop", 999.99));
        normalList.add(new Product("Mouse", 29.99));
        normalList.getTotalPrice();
        
        ProductList emptyList = new EmptyProductList();
        emptyList.add(new Product("Keyboard", 79.99)); // Won't add
        emptyList.getTotalPrice();
        
        // Example 6: Payment Method Special Cases
        System.out.println("\n6. Payment Method Special Cases:");
        PaymentMethod creditCard = new CreditCardPayment("4532-1234-5678-9010");
        PaymentMethod unpaid = new UnpaidPayment();
        
        creditCard.process(150.00);
        System.out.println("  Receipt: " + creditCard.getReceipt());
        
        unpaid.process(150.00);
        System.out.println("  Receipt: " + unpaid.getReceipt());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Eliminates null checks");
        System.out.println("✓ Provides default behavior");
        System.out.println("✓ Simplifies client code");
        System.out.println("✓ Reduces conditional logic");
        System.out.println("✓ Polymorphic handling");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Null object pattern");
        System.out.println("• Default configurations");
        System.out.println("• Missing data handling");
        System.out.println("• Guest/anonymous users");
        System.out.println("• Empty collections");
    }
}
