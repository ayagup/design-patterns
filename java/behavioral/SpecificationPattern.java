package behavioral;

import java.util.*;

/**
 * Specification Pattern
 * Recombines business logic in a boolean fashion.
 */
public class SpecificationPattern {
    
    // Specification interface
    interface Specification<T> {
        boolean isSatisfiedBy(T item);
        
        default Specification<T> and(Specification<T> other) {
            return item -> this.isSatisfiedBy(item) && other.isSatisfiedBy(item);
        }
        
        default Specification<T> or(Specification<T> other) {
            return item -> this.isSatisfiedBy(item) || other.isSatisfiedBy(item);
        }
        
        default Specification<T> not() {
            return item -> !this.isSatisfiedBy(item);
        }
    }
    
    // Domain model
    static class Product {
        private String name;
        private double price;
        private String category;
        private boolean inStock;
        
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
    
    // Concrete Specifications
    static class PriceSpecification implements Specification<Product> {
        private double minPrice;
        private double maxPrice;
        
        public PriceSpecification(double minPrice, double maxPrice) {
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }
        
        @Override
        public boolean isSatisfiedBy(Product product) {
            return product.getPrice() >= minPrice && product.getPrice() <= maxPrice;
        }
    }
    
    static class CategorySpecification implements Specification<Product> {
        private String category;
        
        public CategorySpecification(String category) {
            this.category = category;
        }
        
        @Override
        public boolean isSatisfiedBy(Product product) {
            return product.getCategory().equalsIgnoreCase(category);
        }
    }
    
    static class InStockSpecification implements Specification<Product> {
        @Override
        public boolean isSatisfiedBy(Product product) {
            return product.isInStock();
        }
    }
    
    // Filter using specifications
    static class ProductFilter {
        public List<Product> filter(List<Product> products, Specification<Product> spec) {
            List<Product> result = new ArrayList<>();
            for (Product product : products) {
                if (spec.isSatisfiedBy(product)) {
                    result.add(product);
                }
            }
            return result;
        }
    }
    
    // User validation example
    static class User {
        private String username;
        private int age;
        private String email;
        
        public User(String username, int age, String email) {
            this.username = username;
            this.age = age;
            this.email = email;
        }
        
        public String getUsername() { return username; }
        public int getAge() { return age; }
        public String getEmail() { return email; }
    }
    
    static class AgeSpecification implements Specification<User> {
        private int minAge;
        
        public AgeSpecification(int minAge) {
            this.minAge = minAge;
        }
        
        @Override
        public boolean isSatisfiedBy(User user) {
            return user.getAge() >= minAge;
        }
    }
    
    static class EmailSpecification implements Specification<User> {
        @Override
        public boolean isSatisfiedBy(User user) {
            return user.getEmail() != null && user.getEmail().contains("@");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Specification Pattern Demo ===\n");
        
        // Product filtering
        System.out.println("1. Product Filtering:");
        List<Product> products = Arrays.asList(
            new Product("Laptop", 999.99, "Electronics", true),
            new Product("Phone", 599.99, "Electronics", false),
            new Product("Desk", 299.99, "Furniture", true),
            new Product("Chair", 149.99, "Furniture", true),
            new Product("Monitor", 399.99, "Electronics", true),
            new Product("Lamp", 49.99, "Furniture", false)
        );
        
        ProductFilter filter = new ProductFilter();
        
        // Single specification
        System.out.println("Electronics:");
        Specification<Product> electronicsSpec = new CategorySpecification("Electronics");
        List<Product> electronics = filter.filter(products, electronicsSpec);
        electronics.forEach(System.out::println);
        
        // Combined specifications (AND)
        System.out.println("\nElectronics in stock and under $500:");
        Specification<Product> affordableInStockElectronics = 
            new CategorySpecification("Electronics")
                .and(new InStockSpecification())
                .and(new PriceSpecification(0, 500));
        List<Product> filtered = filter.filter(products, affordableInStockElectronics);
        filtered.forEach(System.out::println);
        
        // Combined specifications (OR)
        System.out.println("\nExpensive (>$500) OR out of stock:");
        Specification<Product> expensiveOrOutOfStock = 
            new PriceSpecification(500, Double.MAX_VALUE)
                .or(new InStockSpecification().not());
        List<Product> filtered2 = filter.filter(products, expensiveOrOutOfStock);
        filtered2.forEach(System.out::println);
        
        // Complex combination
        System.out.println("\n(Furniture under $200) OR (Electronics in stock):");
        Specification<Product> complexSpec = 
            new CategorySpecification("Furniture")
                .and(new PriceSpecification(0, 200))
                .or(new CategorySpecification("Electronics")
                    .and(new InStockSpecification()));
        List<Product> filtered3 = filter.filter(products, complexSpec);
        filtered3.forEach(System.out::println);
        
        // User validation
        System.out.println("\n\n2. User Validation:");
        User user1 = new User("alice", 25, "alice@example.com");
        User user2 = new User("bob", 16, "bob@example.com");
        User user3 = new User("charlie", 30, "invalid-email");
        
        Specification<User> validUserSpec = 
            new AgeSpecification(18).and(new EmailSpecification());
        
        System.out.println("Valid users (age >= 18 AND valid email):");
        List<User> users = Arrays.asList(user1, user2, user3);
        for (User user : users) {
            if (validUserSpec.isSatisfiedBy(user)) {
                System.out.println("✅ " + user.getUsername() + " is valid");
            } else {
                System.out.println("❌ " + user.getUsername() + " is invalid");
            }
        }
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Composable business rules");
        System.out.println("✓ Reusable specifications");
        System.out.println("✓ Easy to test");
        System.out.println("✓ Expressive and readable");
        System.out.println("✓ Follows Single Responsibility Principle");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Product filtering/searching");
        System.out.println("• Form validation");
        System.out.println("• Business rule evaluation");
        System.out.println("• Query building");
    }
}
