package enterprise;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;

/**
 * Lazy Load Pattern
 * 
 * Intent: An object that doesn't contain all the data you need but
 * knows how to get it when needed.
 * 
 * Motivation:
 * Defer loading expensive data until actually needed.
 * Reduce memory consumption and database queries.
 * Load related objects only when accessed.
 * 
 * Approaches:
 * 1. Lazy Initialization - Load on first access
 * 2. Virtual Proxy - Placeholder that loads on first use
 * 3. Value Holder - Wrapper that handles loading
 * 4. Ghost - Object with ID but loads data on first access
 * 
 * Applicability:
 * - Loading data is expensive
 * - Data not always needed
 * - Large object graphs
 * - Database relationships (one-to-many, etc.)
 */

/**
 * Example 1: Lazy Initialization
 * 
 * Data loaded on first access and cached
 */
class Employee {
    private final Long id;
    private String name;
    private Department department; // Loaded lazily
    private boolean departmentLoaded = false;
    
    public Employee(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    
    // Lazy loading
    public Department getDepartment() {
        if (!departmentLoaded) {
            System.out.println("  [LazyLoad] Loading department for employee: " + id);
            department = loadDepartment();
            departmentLoaded = true;
        }
        return department;
    }
    
    private Department loadDepartment() {
        // Simulate database load
        return new Department(100L, "Engineering");
    }
}

class Department {
    private final Long id;
    private final String name;
    
    public Department(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
}

/**
 * Example 2: Virtual Proxy
 * 
 * Proxy object that loads real object on first use
 */
interface OrderItems {
    List<OrderItem> getItems();
    BigDecimal calculateTotal();
}

class RealOrderItems implements OrderItems {
    private final List<OrderItem> items;
    
    public RealOrderItems(Long orderId) {
        System.out.println("  [LazyLoad] Loading order items for order: " + orderId);
        this.items = loadItems(orderId);
    }
    
    private List<OrderItem> loadItems(Long orderId) {
        // Simulate database load
        return Arrays.asList(
            new OrderItem("Item A", new BigDecimal("10.00"), 2),
            new OrderItem("Item B", new BigDecimal("25.00"), 1)
        );
    }
    
    @Override
    public List<OrderItem> getItems() {
        return items;
    }
    
    @Override
    public BigDecimal calculateTotal() {
        return items.stream()
            .map(item -> item.price.multiply(new BigDecimal(item.quantity)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

class LazyOrderItems implements OrderItems {
    private final Long orderId;
    private RealOrderItems real;
    
    public LazyOrderItems(Long orderId) {
        this.orderId = orderId;
    }
    
    private RealOrderItems getReal() {
        if (real == null) {
            real = new RealOrderItems(orderId);
        }
        return real;
    }
    
    @Override
    public List<OrderItem> getItems() {
        return getReal().getItems();
    }
    
    @Override
    public BigDecimal calculateTotal() {
        return getReal().calculateTotal();
    }
}

class OrderItem {
    String name;
    BigDecimal price;
    int quantity;
    
    public OrderItem(String name, BigDecimal price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}

class Order {
    private final Long id;
    private final String customerName;
    private final OrderItems items; // Virtual proxy
    
    public Order(Long id, String customerName) {
        this.id = id;
        this.customerName = customerName;
        this.items = new LazyOrderItems(id); // Proxy, not real items yet
    }
    
    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public OrderItems getItems() { return items; }
}

/**
 * Example 3: Value Holder
 * 
 * Generic holder that loads value on demand
 */
class ValueHolder<T> {
    private T value;
    private final Supplier<T> loader;
    private boolean loaded = false;
    
    public ValueHolder(Supplier<T> loader) {
        this.loader = loader;
    }
    
    public T getValue() {
        if (!loaded) {
            System.out.println("  [ValueHolder] Loading value...");
            value = loader.get();
            loaded = true;
        }
        return value;
    }
    
    public boolean isLoaded() {
        return loaded;
    }
}

class Product {
    private final Long id;
    private final String name;
    private final ValueHolder<String> description;
    private final ValueHolder<List<Review>> reviews;
    
    public Product(Long id, String name) {
        this.id = id;
        this.name = name;
        
        // Lazy load description
        this.description = new ValueHolder<>(() -> {
            System.out.println("  [LazyLoad] Loading description for product: " + id);
            return "This is a detailed description of " + name;
        });
        
        // Lazy load reviews
        this.reviews = new ValueHolder<>(() -> {
            System.out.println("  [LazyLoad] Loading reviews for product: " + id);
            return Arrays.asList(
                new Review("Great product!", 5),
                new Review("Good value", 4)
            );
        });
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    
    public String getDescription() {
        return description.getValue();
    }
    
    public List<Review> getReviews() {
        return reviews.getValue();
    }
}

class Review {
    String comment;
    int rating;
    
    public Review(String comment, int rating) {
        this.comment = comment;
        this.rating = rating;
    }
    
    @Override
    public String toString() {
        return rating + " stars: " + comment;
    }
}

/**
 * Example 4: Ghost Pattern
 * 
 * Object with just ID, loads full data on first property access
 */
class Customer {
    private final Long id;
    private String name;
    private String email;
    private String address;
    private boolean isGhost = true;
    
    // Ghost constructor (only ID)
    public Customer(Long id) {
        this.id = id;
    }
    
    // Full constructor
    public Customer(Long id, String name, String email, String address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.isGhost = false;
    }
    
    private void load() {
        if (isGhost) {
            System.out.println("  [Ghost] Loading full customer data for ID: " + id);
            // Simulate database load
            this.name = "Customer " + id;
            this.email = "customer" + id + "@example.com";
            this.address = "123 Main St";
            this.isGhost = false;
        }
    }
    
    public Long getId() { return id; }
    
    public String getName() {
        load();
        return name;
    }
    
    public String getEmail() {
        load();
        return email;
    }
    
    public String getAddress() {
        load();
        return address;
    }
    
    public boolean isGhost() {
        return isGhost;
    }
}

/**
 * Example 5: Lazy Collection Loading
 * 
 * Collection that loads items on first access
 */
class LazyList<T> extends AbstractList<T> {
    private List<T> list;
    private final Supplier<List<T>> loader;
    
    public LazyList(Supplier<List<T>> loader) {
        this.loader = loader;
    }
    
    private void ensureLoaded() {
        if (list == null) {
            System.out.println("  [LazyList] Loading collection...");
            list = loader.get();
        }
    }
    
    @Override
    public T get(int index) {
        ensureLoaded();
        return list.get(index);
    }
    
    @Override
    public int size() {
        ensureLoaded();
        return list.size();
    }
}

class Author {
    private final Long id;
    private final String name;
    private final LazyList<Book> books;
    
    public Author(Long id, String name) {
        this.id = id;
        this.name = name;
        
        // Books loaded only when accessed
        this.books = new LazyList<>(() -> {
            System.out.println("  [LazyLoad] Loading books for author: " + name);
            return Arrays.asList(
                new Book("Book 1", id),
                new Book("Book 2", id),
                new Book("Book 3", id)
            );
        });
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Book> getBooks() { return books; }
}

class Book {
    String title;
    Long authorId;
    
    public Book(String title, Long authorId) {
        this.title = title;
        this.authorId = authorId;
    }
    
    @Override
    public String toString() {
        return title;
    }
}

/**
 * Demonstration of the Lazy Load Pattern
 */
public class LazyLoadPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Lazy Load Pattern Demo ===\n");
        
        // Example 1: Lazy Initialization
        System.out.println("1. Lazy Initialization:");
        Employee emp = new Employee(1L, "Alice");
        System.out.println("Employee created (department not loaded yet)");
        System.out.println("Accessing department...");
        Department dept = emp.getDepartment();
        System.out.println("Department: " + dept.getName());
        System.out.println("Accessing department again (cached)...");
        dept = emp.getDepartment();
        
        // Example 2: Virtual Proxy
        System.out.println("\n2. Virtual Proxy:");
        Order order = new Order(100L, "Bob");
        System.out.println("Order created (items not loaded yet)");
        System.out.println("Calculating total...");
        BigDecimal total = order.getItems().calculateTotal();
        System.out.println("Total: $" + total);
        
        // Example 3: Value Holder
        System.out.println("\n3. Value Holder:");
        Product product = new Product(1L, "Laptop");
        System.out.println("Product created (description and reviews not loaded)");
        System.out.println("Getting description...");
        String desc = product.getDescription();
        System.out.println("Getting reviews...");
        List<Review> reviews = product.getReviews();
        System.out.println("Reviews: " + reviews);
        
        // Example 4: Ghost Pattern
        System.out.println("\n4. Ghost Pattern:");
        Customer customer = new Customer(42L);
        System.out.println("Ghost customer created with ID only");
        System.out.println("Is ghost? " + customer.isGhost());
        System.out.println("Accessing name...");
        String name = customer.getName();
        System.out.println("Name: " + name);
        System.out.println("Is ghost? " + customer.isGhost());
        
        // Example 5: Lazy Collection
        System.out.println("\n5. Lazy Collection:");
        Author author = new Author(1L, "J.K. Rowling");
        System.out.println("Author created (books not loaded)");
        System.out.println("Getting books...");
        List<Book> books = author.getBooks();
        System.out.println("Book count: " + books.size());
        System.out.println("Books: " + books);
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Reduces memory consumption");
        System.out.println("✓ Improves initial load time");
        System.out.println("✓ Loads data only when needed");
        System.out.println("✓ Transparent to client code");
        System.out.println("✓ Essential for large object graphs");
        
        System.out.println("\n=== Considerations ===");
        System.out.println("• May cause additional database queries");
        System.out.println("• N+1 query problem if not careful");
        System.out.println("• Thread safety with lazy loading");
        System.out.println("• Debugging can be harder");
    }
}
