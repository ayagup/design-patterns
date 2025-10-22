package additional;

/**
 * CRTP (Curiously Recurring Template Pattern)
 * ==========================================
 * 
 * Intent:
 * Achieve compile-time polymorphism by having a class inherit from a generic
 * base class parameterized by the derived class itself. This pattern enables
 * static polymorphism and can eliminate runtime overhead of virtual methods.
 * 
 * Also Known As:
 * - F-bound polymorphism
 * - Recursive type bound
 * - Self-referential generic
 * 
 * Motivation:
 * - Avoid runtime overhead of virtual method calls
 * - Enforce interface contracts at compile time
 * - Enable method chaining with correct return types
 * - Implement mixins and compile-time interfaces
 * 
 * Applicability:
 * - When you need polymorphic behavior without runtime overhead
 * - When implementing fluent interfaces with inheritance
 * - When creating reusable components that need compile-time type safety
 * - When implementing mathematical operators or comparisons
 * 
 * Structure:
 * Base<T extends Base<T>> ← Derived extends Base<Derived>
 * 
 * Participants:
 * - Base<T>: Generic base class parameterized by derived type
 * - Derived: Concrete class that extends Base<Derived>
 * 
 * Implementation Considerations:
 * 1. Base class must be parameterized by derived type
 * 2. Derived class must extend base with itself as parameter
 * 3. Methods in base return type T for method chaining
 * 4. No runtime type checking or casting needed
 * 5. Type safety enforced at compile time
 */

// Example 1: Fluent Builder with Method Chaining
// The CRTP allows each builder subclass to return its own type
abstract class FluentBuilder<T extends FluentBuilder<T>> {
    protected String name;
    protected String description;
    
    // Returns T (the actual subclass type) instead of FluentBuilder
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
    
    public T withName(String name) {
        this.name = name;
        return self();
    }
    
    public T withDescription(String description) {
        this.description = description;
        return self();
    }
    
    public abstract Object build();
}

// Concrete builder extending with CRTP
class PersonBuilder extends FluentBuilder<PersonBuilder> {
    private int age;
    private String email;
    
    // These methods return PersonBuilder, not FluentBuilder
    public PersonBuilder withAge(int age) {
        this.age = age;
        return self();
    }
    
    public PersonBuilder withEmail(String email) {
        this.email = email;
        return self();
    }
    
    @Override
    public Person build() {
        return new Person(name, description, age, email);
    }
}

class Person {
    private String name;
    private String description;
    private int age;
    private String email;
    
    public Person(String name, String description, int age, String email) {
        this.name = name;
        this.description = description;
        this.age = age;
        this.email = email;
    }
    
    @Override
    public String toString() {
        return String.format("Person{name='%s', age=%d, email='%s', description='%s'}", 
                           name, age, email, description);
    }
}

class CompanyBuilder extends FluentBuilder<CompanyBuilder> {
    private String industry;
    private int employeeCount;
    
    public CompanyBuilder withIndustry(String industry) {
        this.industry = industry;
        return self();
    }
    
    public CompanyBuilder withEmployeeCount(int count) {
        this.employeeCount = count;
        return self();
    }
    
    @Override
    public Company build() {
        return new Company(name, description, industry, employeeCount);
    }
}

class Company {
    private String name;
    private String description;
    private String industry;
    private int employeeCount;
    
    public Company(String name, String description, String industry, int employeeCount) {
        this.name = name;
        this.description = description;
        this.industry = industry;
        this.employeeCount = employeeCount;
    }
    
    @Override
    public String toString() {
        return String.format("Company{name='%s', industry='%s', employees=%d, description='%s'}", 
                           name, industry, employeeCount, description);
    }
}

// Example 2: Comparable with Type Safety
// CRTP ensures you can only compare objects of the same type
abstract class ComparableEntity<T extends ComparableEntity<T>> implements Comparable<T> {
    protected String id;
    protected long timestamp;
    
    public ComparableEntity(String id) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Default comparison by timestamp, can be overridden
    @Override
    public int compareTo(T other) {
        return Long.compare(this.timestamp, other.timestamp);
    }
    
    public String getId() {
        return id;
    }
}

class Task extends ComparableEntity<Task> {
    private String title;
    private int priority;
    
    public Task(String id, String title, int priority) {
        super(id);
        this.title = title;
        this.priority = priority;
    }
    
    // Override to compare by priority instead
    @Override
    public int compareTo(Task other) {
        return Integer.compare(other.priority, this.priority); // Higher priority first
    }
    
    @Override
    public String toString() {
        return String.format("Task{id='%s', title='%s', priority=%d}", id, title, priority);
    }
}

class Event extends ComparableEntity<Event> {
    private String name;
    private long scheduledTime;
    
    public Event(String id, String name, long scheduledTime) {
        super(id);
        this.name = name;
        this.scheduledTime = scheduledTime;
    }
    
    @Override
    public int compareTo(Event other) {
        return Long.compare(this.scheduledTime, other.scheduledTime);
    }
    
    @Override
    public String toString() {
        return String.format("Event{id='%s', name='%s', scheduledTime=%d}", id, name, scheduledTime);
    }
}

// Example 3: Arithmetic Operations with CRTP
// Enables operator-like methods with correct return types
abstract class Numeric<T extends Numeric<T>> {
    protected double value;
    
    public Numeric(double value) {
        this.value = value;
    }
    
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
    
    // These must be implemented by subclasses to create new instances
    protected abstract T create(double value);
    
    public T add(T other) {
        return create(this.value + other.value);
    }
    
    public T subtract(T other) {
        return create(this.value - other.value);
    }
    
    public T multiply(T other) {
        return create(this.value * other.value);
    }
    
    public T divide(T other) {
        return create(this.value / other.value);
    }
    
    public double getValue() {
        return value;
    }
}

class Distance extends Numeric<Distance> {
    private String unit;
    
    public Distance(double value, String unit) {
        super(value);
        this.unit = unit;
    }
    
    @Override
    protected Distance create(double value) {
        return new Distance(value, this.unit);
    }
    
    @Override
    public String toString() {
        return String.format("%.2f %s", value, unit);
    }
}

class Money extends Numeric<Money> {
    private String currency;
    
    public Money(double amount, String currency) {
        super(amount);
        this.currency = currency;
    }
    
    @Override
    protected Money create(double value) {
        return new Money(value, this.currency);
    }
    
    @Override
    public String toString() {
        return String.format("%.2f %s", value, currency);
    }
}

// Example 4: Cloneable Hierarchy with Covariant Return Types
abstract class CloneableEntity<T extends CloneableEntity<T>> implements Cloneable {
    protected String id;
    
    public CloneableEntity(String id) {
        this.id = id;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    public String getId() {
        return id;
    }
}

class Document extends CloneableEntity<Document> {
    private String content;
    private String author;
    
    public Document(String id, String content, String author) {
        super(id);
        this.content = content;
        this.author = author;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return String.format("Document{id='%s', author='%s', content='%s'}", id, author, content);
    }
}

class Template extends CloneableEntity<Template> {
    private String templateName;
    private String layout;
    
    public Template(String id, String templateName, String layout) {
        super(id);
        this.templateName = templateName;
        this.layout = layout;
    }
    
    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    @Override
    public String toString() {
        return String.format("Template{id='%s', name='%s', layout='%s'}", id, templateName, layout);
    }
}

// Example 5: Equality with Type Safety
abstract class EqualityEntity<T extends EqualityEntity<T>> {
    protected String id;
    
    public EqualityEntity(String id) {
        this.id = id;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        T other = (T) obj;
        return isEqualTo(other);
    }
    
    // Subclasses implement this with compile-time type safety
    protected abstract boolean isEqualTo(T other);
    
    @Override
    public abstract int hashCode();
}

class User extends EqualityEntity<User> {
    private String username;
    private String email;
    
    public User(String id, String username, String email) {
        super(id);
        this.username = username;
        this.email = email;
    }
    
    @Override
    protected boolean isEqualTo(User other) {
        return id.equals(other.id) && username.equals(other.username);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode() * 31 + username.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s', email='%s'}", id, username, email);
    }
}

class Product extends EqualityEntity<Product> {
    private String sku;
    private String name;
    
    public Product(String id, String sku, String name) {
        super(id);
        this.sku = sku;
        this.name = name;
    }
    
    @Override
    protected boolean isEqualTo(Product other) {
        return sku.equals(other.sku);
    }
    
    @Override
    public int hashCode() {
        return sku.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Product{id='%s', sku='%s', name='%s'}", id, sku, name);
    }
}

// Example 6: Visitor Pattern with CRTP for Compile-Time Type Safety
abstract class Visitable<T extends Visitable<T>> {
    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }
    
    public abstract <R> R accept(Visitor<R> visitor);
}

interface Visitor<R> {
    R visitShape(Shape shape);
    R visitCircle(Circle circle);
    R visitRectangle(Rectangle rectangle);
}

abstract class Shape extends Visitable<Shape> {
    protected String color;
    
    public Shape(String color) {
        this.color = color;
    }
    
    public String getColor() {
        return color;
    }
}

class Circle extends Shape {
    private double radius;
    
    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }
    
    public double getRadius() {
        return radius;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitCircle(this);
    }
}

class Rectangle extends Shape {
    private double width;
    private double height;
    
    public Rectangle(String color, double width, double height) {
        super(color);
        this.width = width;
        this.height = height;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
    
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitRectangle(this);
    }
}

class AreaCalculator implements Visitor<Double> {
    @Override
    public Double visitShape(Shape shape) {
        return 0.0;
    }
    
    @Override
    public Double visitCircle(Circle circle) {
        return Math.PI * circle.getRadius() * circle.getRadius();
    }
    
    @Override
    public Double visitRectangle(Rectangle rectangle) {
        return rectangle.getWidth() * rectangle.getHeight();
    }
}

class ShapeRenderer implements Visitor<String> {
    @Override
    public String visitShape(Shape shape) {
        return "Unknown shape in " + shape.getColor();
    }
    
    @Override
    public String visitCircle(Circle circle) {
        return String.format("Drawing %s circle with radius %.2f", circle.getColor(), circle.getRadius());
    }
    
    @Override
    public String visitRectangle(Rectangle rectangle) {
        return String.format("Drawing %s rectangle %.2fx%.2f", 
                           rectangle.getColor(), rectangle.getWidth(), rectangle.getHeight());
    }
}

/**
 * Demonstration of CRTP Pattern
 */
public class CRTPPattern {
    public static void main(String[] args) {
        demonstrateFluentBuilders();
        demonstrateComparableEntities();
        demonstrateNumericOperations();
        demonstrateCloneableHierarchy();
        demonstrateEqualityEntities();
        demonstrateVisitorPattern();
    }
    
    private static void demonstrateFluentBuilders() {
        System.out.println("=== Fluent Builders with CRTP ===\n");
        
        // PersonBuilder returns PersonBuilder, allowing access to withAge and withEmail
        Person person = new PersonBuilder()
                .withName("Alice Johnson")
                .withDescription("Software Engineer")
                .withAge(28)
                .withEmail("alice@example.com")
                .build();
        
        System.out.println(person);
        
        // CompanyBuilder returns CompanyBuilder, allowing access to withIndustry
        Company company = new CompanyBuilder()
                .withName("TechCorp")
                .withDescription("Leading technology company")
                .withIndustry("Software")
                .withEmployeeCount(500)
                .build();
        
        System.out.println(company);
        System.out.println();
    }
    
    private static void demonstrateComparableEntities() {
        System.out.println("=== Comparable Entities with Type Safety ===\n");
        
        Task task1 = new Task("T1", "Fix bug", 5);
        Task task2 = new Task("T2", "Write tests", 3);
        Task task3 = new Task("T3", "Deploy", 10);
        
        System.out.println("Tasks (unsorted):");
        System.out.println(task1);
        System.out.println(task2);
        System.out.println(task3);
        
        System.out.println("\nComparing tasks by priority:");
        System.out.println("task1 vs task2: " + task1.compareTo(task2));
        System.out.println("task3 vs task1: " + task3.compareTo(task1));
        
        Event event1 = new Event("E1", "Meeting", System.currentTimeMillis() + 3600000);
        Event event2 = new Event("E2", "Lunch", System.currentTimeMillis() + 1800000);
        
        System.out.println("\nComparing events by scheduled time:");
        System.out.println("event1 vs event2: " + event1.compareTo(event2));
        System.out.println();
    }
    
    private static void demonstrateNumericOperations() {
        System.out.println("=== Numeric Operations with CRTP ===\n");
        
        Distance d1 = new Distance(100, "meters");
        Distance d2 = new Distance(50, "meters");
        
        System.out.println("Distance 1: " + d1);
        System.out.println("Distance 2: " + d2);
        System.out.println("Sum: " + d1.add(d2));
        System.out.println("Difference: " + d1.subtract(d2));
        
        Money m1 = new Money(100.50, "USD");
        Money m2 = new Money(25.75, "USD");
        
        System.out.println("\nMoney 1: " + m1);
        System.out.println("Money 2: " + m2);
        System.out.println("Total: " + m1.add(m2));
        System.out.println("Change: " + m1.subtract(m2));
        System.out.println();
    }
    
    private static void demonstrateCloneableHierarchy() {
        System.out.println("=== Cloneable Hierarchy with Covariant Returns ===\n");
        
        Document original = new Document("D1", "Original content", "Bob");
        System.out.println("Original: " + original);
        
        // Clone returns Document, not CloneableEntity
        Document copy = original.clone();
        copy.setContent("Modified content");
        
        System.out.println("Copy: " + copy);
        System.out.println("Original unchanged: " + original);
        
        Template template = new Template("T1", "Blog Post", "Title + Content + Footer");
        Template templateCopy = template.clone();
        templateCopy.setLayout("Title + Sidebar + Content");
        
        System.out.println("\nOriginal template: " + template);
        System.out.println("Modified copy: " + templateCopy);
        System.out.println();
    }
    
    private static void demonstrateEqualityEntities() {
        System.out.println("=== Equality with Type Safety ===\n");
        
        User user1 = new User("U1", "alice", "alice@example.com");
        User user2 = new User("U1", "alice", "alice.new@example.com");
        User user3 = new User("U2", "bob", "bob@example.com");
        
        System.out.println("user1: " + user1);
        System.out.println("user2: " + user2);
        System.out.println("user3: " + user3);
        System.out.println("\nuser1.equals(user2): " + user1.equals(user2)); // true (same id & username)
        System.out.println("user1.equals(user3): " + user1.equals(user3)); // false
        
        Product p1 = new Product("P1", "SKU-123", "Widget");
        Product p2 = new Product("P2", "SKU-123", "Widget Pro");
        
        System.out.println("\nProduct 1: " + p1);
        System.out.println("Product 2: " + p2);
        System.out.println("p1.equals(p2): " + p1.equals(p2)); // true (same SKU)
        System.out.println();
    }
    
    private static void demonstrateVisitorPattern() {
        System.out.println("=== Visitor Pattern with CRTP ===\n");
        
        Circle circle = new Circle("red", 5.0);
        Rectangle rectangle = new Rectangle("blue", 10.0, 20.0);
        
        AreaCalculator areaCalc = new AreaCalculator();
        ShapeRenderer renderer = new ShapeRenderer();
        
        System.out.println("Circle area: " + circle.accept(areaCalc));
        System.out.println("Rectangle area: " + rectangle.accept(areaCalc));
        
        System.out.println("\n" + circle.accept(renderer));
        System.out.println(rectangle.accept(renderer));
    }
}
