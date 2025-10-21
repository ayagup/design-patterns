package additional;

import java.util.*;
import java.util.function.Predicate;
import java.time.*;

/**
 * Specification Pattern
 * 
 * Intent: Business rules can be recombined by chaining specifications
 * together using boolean logic. Separates the statement of how to match
 * a candidate from the candidate object itself.
 * 
 * Motivation:
 * Encapsulates business rules as reusable objects.
 * Allows complex rules by combining simple ones.
 * Enables runtime rule composition.
 * Improves testability of rules.
 * 
 * Applicability:
 * - Complex business rule validation
 * - Dynamic query building
 * - Filter criteria composition
 * - Rule engines
 */

/**
 * Example 1: Basic Specification
 * 
 * Generic specification interface
 */
interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
    
    default Specification<T> and(Specification<T> other) {
        return new AndSpecification<>(this, other);
    }
    
    default Specification<T> or(Specification<T> other) {
        return new OrSpecification<>(this, other);
    }
    
    default Specification<T> not() {
        return new NotSpecification<>(this);
    }
}

class AndSpecification<T> implements Specification<T> {
    private final Specification<T> left;
    private final Specification<T> right;
    
    public AndSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) && right.isSatisfiedBy(candidate);
    }
}

class OrSpecification<T> implements Specification<T> {
    private final Specification<T> left;
    private final Specification<T> right;
    
    public OrSpecification(Specification<T> left, Specification<T> right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return left.isSatisfiedBy(candidate) || right.isSatisfiedBy(candidate);
    }
}

class NotSpecification<T> implements Specification<T> {
    private final Specification<T> spec;
    
    public NotSpecification(Specification<T> spec) {
        this.spec = spec;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return !spec.isSatisfiedBy(candidate);
    }
}

/**
 * Example 2: Product Specifications
 * 
 * Business rules for product filtering
 */
class Product {
    private final String name;
    private final double price;
    private final String category;
    private final boolean inStock;
    
    public Product(String name, double price, String category, boolean inStock) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.inStock = inStock;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public boolean isInStock() { return inStock; }
    
    @Override
    public String toString() {
        return name + " ($" + price + ", " + category + ", " + 
               (inStock ? "In Stock" : "Out of Stock") + ")";
    }
}

class PriceSpecification implements Specification<Product> {
    private final double minPrice;
    private final double maxPrice;
    
    public PriceSpecification(double minPrice, double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    @Override
    public boolean isSatisfiedBy(Product product) {
        return product.getPrice() >= minPrice && product.getPrice() <= maxPrice;
    }
}

class CategorySpecification implements Specification<Product> {
    private final String category;
    
    public CategorySpecification(String category) {
        this.category = category;
    }
    
    @Override
    public boolean isSatisfiedBy(Product product) {
        return product.getCategory().equals(category);
    }
}

class InStockSpecification implements Specification<Product> {
    @Override
    public boolean isSatisfiedBy(Product product) {
        return product.isInStock();
    }
}

/**
 * Example 3: Customer Specifications
 * 
 * Business rules for customer qualification
 */
class Customer {
    private final String name;
    private final int age;
    private final double creditScore;
    private final boolean isPremium;
    
    public Customer(String name, int age, double creditScore, boolean isPremium) {
        this.name = name;
        this.age = age;
        this.creditScore = creditScore;
        this.isPremium = isPremium;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
    public double getCreditScore() { return creditScore; }
    public boolean isPremium() { return isPremium; }
    
    @Override
    public String toString() {
        return name + " (Age: " + age + ", Credit: " + creditScore + 
               (isPremium ? ", Premium" : "") + ")";
    }
}

class MinimumAgeSpecification implements Specification<Customer> {
    private final int minimumAge;
    
    public MinimumAgeSpecification(int minimumAge) {
        this.minimumAge = minimumAge;
    }
    
    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getAge() >= minimumAge;
    }
}

class MinimumCreditScoreSpecification implements Specification<Customer> {
    private final double minimumScore;
    
    public MinimumCreditScoreSpecification(double minimumScore) {
        this.minimumScore = minimumScore;
    }
    
    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getCreditScore() >= minimumScore;
    }
}

class PremiumCustomerSpecification implements Specification<Customer> {
    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.isPremium();
    }
}

/**
 * Example 4: Invoice Specifications
 * 
 * Business rules for invoice processing
 */
class Invoice {
    private final String number;
    private final double amount;
    private final LocalDate dueDate;
    private final boolean paid;
    
    public Invoice(String number, double amount, LocalDate dueDate, boolean paid) {
        this.number = number;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paid = paid;
    }
    
    public String getNumber() { return number; }
    public double getAmount() { return amount; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isPaid() { return paid; }
    
    @Override
    public String toString() {
        return "Invoice " + number + " ($" + amount + ", Due: " + dueDate + 
               (paid ? ", PAID" : ", UNPAID") + ")";
    }
}

class OverdueSpecification implements Specification<Invoice> {
    @Override
    public boolean isSatisfiedBy(Invoice invoice) {
        return !invoice.isPaid() && invoice.getDueDate().isBefore(LocalDate.now());
    }
}

class LargeAmountSpecification implements Specification<Invoice> {
    private final double threshold;
    
    public LargeAmountSpecification(double threshold) {
        this.threshold = threshold;
    }
    
    @Override
    public boolean isSatisfiedBy(Invoice invoice) {
        return invoice.getAmount() >= threshold;
    }
}

class UnpaidSpecification implements Specification<Invoice> {
    @Override
    public boolean isSatisfiedBy(Invoice invoice) {
        return !invoice.isPaid();
    }
}

/**
 * Example 5: Functional Specification
 * 
 * Using Java 8 Predicate
 */
class FunctionalSpecification<T> implements Specification<T> {
    private final Predicate<T> predicate;
    
    public FunctionalSpecification(Predicate<T> predicate) {
        this.predicate = predicate;
    }
    
    @Override
    public boolean isSatisfiedBy(T candidate) {
        return predicate.test(candidate);
    }
    
    public static <T> Specification<T> of(Predicate<T> predicate) {
        return new FunctionalSpecification<>(predicate);
    }
}

/**
 * Example 6: Repository with Specifications
 * 
 * Query objects using specifications
 */
class Repository<T> {
    private final List<T> items;
    
    public Repository(List<T> items) {
        this.items = new ArrayList<>(items);
    }
    
    public List<T> find(Specification<T> spec) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (spec.isSatisfiedBy(item)) {
                result.add(item);
            }
        }
        return result;
    }
}

/**
 * Demonstration of the Specification Pattern
 */
public class SpecificationPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Specification Pattern Demo ===\n");
        
        // Example 1: Product Specifications
        System.out.println("1. Product Filtering:");
        List<Product> products = Arrays.asList(
            new Product("Laptop", 999.99, "Electronics", true),
            new Product("Mouse", 29.99, "Electronics", true),
            new Product("Desk", 299.99, "Furniture", false),
            new Product("Chair", 199.99, "Furniture", true),
            new Product("Monitor", 399.99, "Electronics", true)
        );
        
        Repository<Product> productRepo = new Repository<>(products);
        
        // Electronics in stock and price < $500
        Specification<Product> affordableElectronics = 
            new CategorySpecification("Electronics")
                .and(new InStockSpecification())
                .and(new PriceSpecification(0, 500));
        
        List<Product> filtered = productRepo.find(affordableElectronics);
        System.out.println("  Affordable Electronics in Stock:");
        filtered.forEach(p -> System.out.println("    - " + p));
        
        // Example 2: Customer Qualification
        System.out.println("\n2. Customer Qualification:");
        List<Customer> customers = Arrays.asList(
            new Customer("Alice", 35, 750, true),
            new Customer("Bob", 28, 680, false),
            new Customer("Charlie", 45, 720, true),
            new Customer("Diana", 22, 620, false)
        );
        
        Repository<Customer> customerRepo = new Repository<>(customers);
        
        // Premium customers OR (age >= 30 AND credit >= 700)
        Specification<Customer> qualifiedCustomers = 
            new PremiumCustomerSpecification()
                .or(new MinimumAgeSpecification(30)
                    .and(new MinimumCreditScoreSpecification(700)));
        
        List<Customer> qualified = customerRepo.find(qualifiedCustomers);
        System.out.println("  Qualified Customers:");
        qualified.forEach(c -> System.out.println("    - " + c));
        
        // Example 3: Invoice Processing
        System.out.println("\n3. Invoice Processing:");
        List<Invoice> invoices = Arrays.asList(
            new Invoice("INV-001", 1500, LocalDate.now().minusDays(10), false),
            new Invoice("INV-002", 800, LocalDate.now().plusDays(5), false),
            new Invoice("INV-003", 2000, LocalDate.now().minusDays(5), true),
            new Invoice("INV-004", 1200, LocalDate.now().minusDays(15), false)
        );
        
        Repository<Invoice> invoiceRepo = new Repository<>(invoices);
        
        // Overdue AND large amount (>= $1000)
        Specification<Invoice> criticalInvoices = 
            new OverdueSpecification()
                .and(new LargeAmountSpecification(1000));
        
        List<Invoice> critical = invoiceRepo.find(criticalInvoices);
        System.out.println("  Critical Invoices (Overdue & Large):");
        critical.forEach(inv -> System.out.println("    - " + inv));
        
        // Example 4: Functional Specifications
        System.out.println("\n4. Functional Specifications:");
        
        // Using lambda expressions
        Specification<Product> expensiveProducts = 
            FunctionalSpecification.of(p -> p.getPrice() > 300);
        
        List<Product> expensive = productRepo.find(expensiveProducts);
        System.out.println("  Expensive Products (> $300):");
        expensive.forEach(p -> System.out.println("    - " + p));
        
        // Example 5: Complex Rule Composition
        System.out.println("\n5. Complex Rule Composition:");
        
        // (Electronics OR Furniture) AND (InStock) AND (Price 100-400)
        Specification<Product> complexSpec = 
            new CategorySpecification("Electronics")
                .or(new CategorySpecification("Furniture"))
                .and(new InStockSpecification())
                .and(new PriceSpecification(100, 400));
        
        List<Product> complexFiltered = productRepo.find(complexSpec);
        System.out.println("  Complex Filter Results:");
        complexFiltered.forEach(p -> System.out.println("    - " + p));
        
        // Example 6: Negation
        System.out.println("\n6. Specification Negation:");
        
        // NOT Premium customers
        Specification<Customer> nonPremium = 
            new PremiumCustomerSpecification().not();
        
        List<Customer> nonPremiumCustomers = customerRepo.find(nonPremium);
        System.out.println("  Non-Premium Customers:");
        nonPremiumCustomers.forEach(c -> System.out.println("    - " + c));
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Reusable business rules");
        System.out.println("✓ Composable with boolean logic");
        System.out.println("✓ Testable in isolation");
        System.out.println("✓ Declarative rule definition");
        System.out.println("✓ Runtime rule composition");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Product filtering");
        System.out.println("• Customer qualification");
        System.out.println("• Invoice processing");
        System.out.println("• Access control rules");
        System.out.println("• Dynamic queries");
    }
}
