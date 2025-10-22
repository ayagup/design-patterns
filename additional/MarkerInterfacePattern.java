package additional;

import java.util.*;
import java.io.*;

/**
 * Marker Interface Pattern
 * 
 * Intent: Uses empty interfaces to mark or tag classes with specific
 * attributes or capabilities without adding methods.
 * 
 * Motivation:
 * Provides metadata about classes.
 * Enables runtime type checking.
 * Groups classes by capability.
 * No method implementation needed.
 * 
 * Applicability:
 * - Marking classes for special treatment
 * - Runtime type identification
 * - Framework/library categorization
 * - Security/permission marking
 */

/**
 * Example 1: Basic Marker Interfaces
 * 
 * Java's built-in marker interfaces
 */

// Custom marker for cloneable entities
interface Cloneable extends java.lang.Cloneable {
    // Empty marker interface
}

// Custom marker for serializable entities
interface SerializableEntity extends Serializable {
    // Empty marker interface
}

class Product implements SerializableEntity, Cloneable {
    private String name;
    private double price;
    
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
    
    public String getName() { return name; }
    public double getPrice() { return price; }
    
    @Override
    public String toString() {
        return "Product{name='" + name + "', price=" + price + "}";
    }
}

class ProductProcessor {
    public void process(Object obj) {
        System.out.println("  [Processor] Processing: " + obj.getClass().getSimpleName());
        
        if (obj instanceof SerializableEntity) {
            System.out.println("  [Processor] Object is serializable");
        }
        
        if (obj instanceof Cloneable) {
            System.out.println("  [Processor] Object is cloneable");
        }
    }
}

/**
 * Example 2: Permission Markers
 * 
 * Marking classes with permission levels
 */
interface AdminOnly {
    // Marker for admin-only operations
}

interface UserAccessible {
    // Marker for user-accessible operations
}

interface PublicAccess {
    // Marker for publicly accessible operations
}

class AdminReport implements AdminOnly {
    public void generate() {
        System.out.println("  [AdminReport] Generating admin report...");
    }
}

class UserDashboard implements UserAccessible {
    public void display() {
        System.out.println("  [UserDashboard] Displaying user dashboard...");
    }
}

class PublicPage implements PublicAccess {
    public void render() {
        System.out.println("  [PublicPage] Rendering public page...");
    }
}

class SecurityManager {
    public boolean canAccess(Object resource, String userRole) {
        System.out.println("  [Security] Checking access for: " + userRole);
        
        if (resource instanceof AdminOnly) {
            boolean allowed = "ADMIN".equals(userRole);
            System.out.println("  [Security] Admin resource - Access: " + allowed);
            return allowed;
        }
        
        if (resource instanceof UserAccessible) {
            boolean allowed = "ADMIN".equals(userRole) || "USER".equals(userRole);
            System.out.println("  [Security] User resource - Access: " + allowed);
            return allowed;
        }
        
        if (resource instanceof PublicAccess) {
            System.out.println("  [Security] Public resource - Access: true");
            return true;
        }
        
        return false;
    }
}

/**
 * Example 3: Lifecycle Markers
 * 
 * Marking components with lifecycle hooks
 */
interface Initializable {
    // Marker for components that need initialization
}

interface Disposable {
    // Marker for components that need cleanup
}

interface Configurable {
    // Marker for configurable components
}

class DatabaseConnection implements Initializable, Disposable {
    private boolean connected = false;
    
    public void connect() {
        connected = true;
        System.out.println("  [DBConnection] Connected");
    }
    
    public void disconnect() {
        connected = false;
        System.out.println("  [DBConnection] Disconnected");
    }
    
    public boolean isConnected() { return connected; }
}

class CacheService implements Initializable, Disposable, Configurable {
    private boolean initialized = false;
    
    public void initialize() {
        initialized = true;
        System.out.println("  [CacheService] Initialized");
    }
    
    public void cleanup() {
        initialized = false;
        System.out.println("  [CacheService] Cleaned up");
    }
}

class ComponentManager {
    private final List<Object> components = new ArrayList<>();
    
    public void register(Object component) {
        components.add(component);
        System.out.println("  [Manager] Registered: " + component.getClass().getSimpleName());
        
        if (component instanceof Configurable) {
            System.out.println("  [Manager] Component needs configuration");
        }
    }
    
    public void initializeAll() {
        System.out.println("  [Manager] Initializing all components...");
        for (Object component : components) {
            if (component instanceof Initializable) {
                System.out.println("  [Manager] Initializing: " + component.getClass().getSimpleName());
            }
        }
    }
    
    public void disposeAll() {
        System.out.println("  [Manager] Disposing all components...");
        for (Object component : components) {
            if (component instanceof Disposable) {
                System.out.println("  [Manager] Disposing: " + component.getClass().getSimpleName());
            }
        }
    }
}

/**
 * Example 4: Validation Markers
 * 
 * Marking entities for validation
 */
interface Validatable {
    // Marker for entities that need validation
}

interface Auditable {
    // Marker for entities that need audit logging
}

interface Versionable {
    // Marker for entities with versioning
}

class Order implements Validatable, Auditable {
    private String orderId;
    private double amount;
    
    public Order(String orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }
    
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    
    @Override
    public String toString() {
        return "Order{id='" + orderId + "', amount=" + amount + "}";
    }
}

class Customer implements Validatable, Versionable {
    private String name;
    private String email;
    
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return "Customer{name='" + name + "', email='" + email + "'}";
    }
}

class EntityProcessor {
    public void process(Object entity) {
        System.out.println("  [Processor] Processing: " + entity);
        
        if (entity instanceof Validatable) {
            System.out.println("  [Processor] Running validation...");
        }
        
        if (entity instanceof Auditable) {
            System.out.println("  [Processor] Logging audit entry...");
        }
        
        if (entity instanceof Versionable) {
            System.out.println("  [Processor] Checking version...");
        }
    }
}

/**
 * Example 5: Event Markers
 * 
 * Marking events by type
 */
interface CriticalEvent {
    // Marker for critical events
}

interface WarningEvent {
    // Marker for warning events
}

interface InfoEvent {
    // Marker for informational events
}

class SystemFailure implements CriticalEvent {
    private final String message;
    
    public SystemFailure(String message) {
        this.message = message;
    }
    
    public String getMessage() { return message; }
}

class LowMemoryWarning implements WarningEvent {
    private final long availableMemory;
    
    public LowMemoryWarning(long availableMemory) {
        this.availableMemory = availableMemory;
    }
    
    public long getAvailableMemory() { return availableMemory; }
}

class UserLogin implements InfoEvent {
    private final String username;
    
    public UserLogin(String username) {
        this.username = username;
    }
    
    public String getUsername() { return username; }
}

class EventHandler {
    public void handle(Object event) {
        if (event instanceof CriticalEvent) {
            System.out.println("  [CRITICAL] Immediate action required!");
            if (event instanceof SystemFailure) {
                System.out.println("  [CRITICAL] System failure: " + ((SystemFailure) event).getMessage());
            }
        }
        
        if (event instanceof WarningEvent) {
            System.out.println("  [WARNING] Potential issue detected");
            if (event instanceof LowMemoryWarning) {
                System.out.println("  [WARNING] Memory: " + ((LowMemoryWarning) event).getAvailableMemory() + " bytes");
            }
        }
        
        if (event instanceof InfoEvent) {
            System.out.println("  [INFO] Event logged");
            if (event instanceof UserLogin) {
                System.out.println("  [INFO] User: " + ((UserLogin) event).getUsername());
            }
        }
    }
}

/**
 * Example 6: Feature Markers
 * 
 * Marking classes with feature flags
 */
interface BetaFeature {
    // Marker for beta features
}

interface ExperimentalFeature {
    // Marker for experimental features
}

interface DeprecatedFeature {
    // Marker for deprecated features
}

class NewSearchEngine implements BetaFeature {
    public void search(String query) {
        System.out.println("  [NewSearch] Searching for: " + query);
    }
}

class AIRecommendations implements ExperimentalFeature {
    public void recommend() {
        System.out.println("  [AI] Generating recommendations...");
    }
}

class OldDataExporter implements DeprecatedFeature {
    public void export() {
        System.out.println("  [OldExporter] Exporting data (deprecated)...");
    }
}

class FeatureManager {
    private boolean enableBeta = false;
    private boolean enableExperimental = false;
    
    public void setEnableBeta(boolean enable) {
        this.enableBeta = enable;
    }
    
    public void setEnableExperimental(boolean enable) {
        this.enableExperimental = enable;
    }
    
    public boolean isEnabled(Object feature) {
        if (feature instanceof DeprecatedFeature) {
            System.out.println("  [FeatureManager] Deprecated feature - use with caution");
            return true; // Still allowed but warned
        }
        
        if (feature instanceof BetaFeature) {
            System.out.println("  [FeatureManager] Beta feature - enabled: " + enableBeta);
            return enableBeta;
        }
        
        if (feature instanceof ExperimentalFeature) {
            System.out.println("  [FeatureManager] Experimental feature - enabled: " + enableExperimental);
            return enableExperimental;
        }
        
        return true; // Regular features always enabled
    }
}

/**
 * Demonstration of the Marker Interface Pattern
 */
public class MarkerInterfacePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Marker Interface Pattern Demo ===\n");
        
        // Example 1: Basic Markers
        System.out.println("1. Basic Marker Interfaces:");
        Product product = new Product("Laptop", 999.99);
        ProductProcessor processor = new ProductProcessor();
        processor.process(product);
        
        // Example 2: Permission Markers
        System.out.println("\n2. Permission Markers:");
        SecurityManager security = new SecurityManager();
        
        AdminReport adminReport = new AdminReport();
        security.canAccess(adminReport, "USER");
        security.canAccess(adminReport, "ADMIN");
        
        PublicPage publicPage = new PublicPage();
        security.canAccess(publicPage, "GUEST");
        
        // Example 3: Lifecycle Markers
        System.out.println("\n3. Lifecycle Markers:");
        ComponentManager manager = new ComponentManager();
        
        manager.register(new DatabaseConnection());
        manager.register(new CacheService());
        
        manager.initializeAll();
        manager.disposeAll();
        
        // Example 4: Validation Markers
        System.out.println("\n4. Validation Markers:");
        EntityProcessor entityProcessor = new EntityProcessor();
        
        Order order = new Order("ORD-123", 250.50);
        entityProcessor.process(order);
        
        Customer customer = new Customer("Alice", "alice@example.com");
        entityProcessor.process(customer);
        
        // Example 5: Event Markers
        System.out.println("\n5. Event Markers:");
        EventHandler eventHandler = new EventHandler();
        
        eventHandler.handle(new SystemFailure("Database connection lost"));
        eventHandler.handle(new LowMemoryWarning(1024000));
        eventHandler.handle(new UserLogin("bob"));
        
        // Example 6: Feature Markers
        System.out.println("\n6. Feature Markers:");
        FeatureManager features = new FeatureManager();
        
        NewSearchEngine search = new NewSearchEngine();
        features.isEnabled(search); // Beta disabled by default
        
        features.setEnableBeta(true);
        if (features.isEnabled(search)) {
            search.search("marker interface pattern");
        }
        
        AIRecommendations ai = new AIRecommendations();
        features.setEnableExperimental(true);
        if (features.isEnabled(ai)) {
            ai.recommend();
        }
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ No method implementation needed");
        System.out.println("✓ Runtime type checking");
        System.out.println("✓ Metadata about classes");
        System.out.println("✓ Groups related classes");
        System.out.println("✓ Framework integration");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Serialization (Serializable)");
        System.out.println("• Cloning (Cloneable)");
        System.out.println("• Security permissions");
        System.out.println("• Lifecycle management");
        System.out.println("• Feature flags");
    }
}
