package additional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SERVICE LOCATOR PATTERN
 * 
 * Provides a centralized registry that manages service instances and provides
 * them to clients on demand, decoupling service consumers from service implementations.
 * 
 * Benefits:
 * - Centralized service management
 * - Reduced coupling between clients and services
 * - Lazy service initialization
 * - Service caching for performance
 * - Dynamic service registration
 * 
 * Use Cases:
 * - Service discovery in distributed systems
 * - Plugin architectures
 * - Dependency management
 * - Testing (mock service replacement)
 * - Legacy system integration
 */

// Service interface
interface Service {
    String getName();
    void execute();
}

// Service Locator
class ServiceLocator {
    private static final Map<String, Service> services = new ConcurrentHashMap<>();
    private static final ServiceCache cache = new ServiceCache();
    
    public static Service getService(String serviceName) {
        System.out.println("  [SERVICE LOCATOR] Looking up service: " + serviceName);
        
        // Check cache first
        Service service = cache.getService(serviceName);
        
        if (service != null) {
            System.out.println("  [SERVICE LOCATOR] Found in cache");
            return service;
        }
        
        // Lookup from registry
        service = lookupService(serviceName);
        
        if (service != null) {
            cache.addService(service);
        }
        
        return service;
    }
    
    private static Service lookupService(String serviceName) {
        System.out.println("  [SERVICE LOCATOR] Loading from registry");
        Service service = services.get(serviceName);
        
        if (service == null) {
            throw new RuntimeException("Service not found: " + serviceName);
        }
        
        return service;
    }
    
    public static void registerService(String name, Service service) {
        System.out.println("  [SERVICE LOCATOR] Registering service: " + name);
        services.put(name, service);
    }
    
    public static void unregisterService(String name) {
        System.out.println("  [SERVICE LOCATOR] Unregistering service: " + name);
        services.remove(name);
        cache.removeService(name);
    }
    
    public static void clearCache() {
        cache.clear();
    }
}

// Service Cache
class ServiceCache {
    private final Map<String, Service> cache = new ConcurrentHashMap<>();
    
    public Service getService(String serviceName) {
        return cache.get(serviceName);
    }
    
    public void addService(Service service) {
        System.out.println("  [CACHE] Caching service: " + service.getName());
        cache.put(service.getName(), service);
    }
    
    public void removeService(String serviceName) {
        cache.remove(serviceName);
    }
    
    public void clear() {
        System.out.println("  [CACHE] Clearing all cached services");
        cache.clear();
    }
}

// Example 1: Email Service
class EmailService implements Service {
    @Override
    public String getName() {
        return "EmailService";
    }
    
    @Override
    public void execute() {
        System.out.println("    📧 Sending email...");
    }
    
    public void sendEmail(String to, String subject, String body) {
        System.out.println("    📧 Email sent to: " + to);
        System.out.println("       Subject: " + subject);
        System.out.println("       Body: " + body);
    }
}

// Example 2: Logging Service
class LoggingService implements Service {
    @Override
    public String getName() {
        return "LoggingService";
    }
    
    @Override
    public void execute() {
        System.out.println("    📝 Logging active...");
    }
    
    public void log(String level, String message) {
        System.out.println("    📝 [" + level + "] " + message);
    }
}

// Example 3: Payment Service
class PaymentService implements Service {
    @Override
    public String getName() {
        return "PaymentService";
    }
    
    @Override
    public void execute() {
        System.out.println("    💳 Processing payment...");
    }
    
    public boolean processPayment(String orderId, double amount) {
        System.out.println("    💳 Payment processed: Order " + orderId + " - $" + amount);
        return true;
    }
}

// Example 4: Authentication Service
class AuthenticationService implements Service {
    private final Map<String, String> users = new HashMap<>();
    
    public AuthenticationService() {
        users.put("alice", "password123");
        users.put("bob", "password456");
    }
    
    @Override
    public String getName() {
        return "AuthenticationService";
    }
    
    @Override
    public void execute() {
        System.out.println("    🔐 Authentication service active...");
    }
    
    public boolean authenticate(String username, String password) {
        String storedPassword = users.get(username);
        boolean authenticated = storedPassword != null && storedPassword.equals(password);
        
        System.out.println("    🔐 Authentication " + 
            (authenticated ? "successful" : "failed") + " for: " + username);
        
        return authenticated;
    }
}

// Example 5: Database Service
class DatabaseService implements Service {
    @Override
    public String getName() {
        return "DatabaseService";
    }
    
    @Override
    public void execute() {
        System.out.println("    💾 Database connection active...");
    }
    
    public void query(String sql) {
        System.out.println("    💾 Executing query: " + sql);
    }
}

// Client using Service Locator
class UserController {
    public void createUser(String username, String email) {
        System.out.println("\n[USER CONTROLLER] Creating user: " + username);
        
        // Get logging service
        LoggingService logger = (LoggingService) ServiceLocator.getService("LoggingService");
        logger.log("INFO", "Creating user: " + username);
        
        // Get database service
        DatabaseService db = (DatabaseService) ServiceLocator.getService("DatabaseService");
        db.query("INSERT INTO users VALUES ('" + username + "', '" + email + "')");
        
        // Get email service
        EmailService emailService = (EmailService) ServiceLocator.getService("EmailService");
        emailService.sendEmail(email, "Welcome!", "Welcome to our service, " + username);
        
        logger.log("INFO", "User created successfully");
    }
}

class OrderController {
    public void placeOrder(String username, String orderId, double amount) {
        System.out.println("\n[ORDER CONTROLLER] Placing order: " + orderId);
        
        // Get authentication service
        AuthenticationService auth = (AuthenticationService) 
            ServiceLocator.getService("AuthenticationService");
        
        if (!auth.authenticate(username, "password123")) {
            System.out.println("Order failed: Authentication failed");
            return;
        }
        
        // Get logging service
        LoggingService logger = (LoggingService) ServiceLocator.getService("LoggingService");
        logger.log("INFO", "Processing order: " + orderId);
        
        // Get payment service
        PaymentService payment = (PaymentService) ServiceLocator.getService("PaymentService");
        boolean paid = payment.processPayment(orderId, amount);
        
        if (paid) {
            // Get database service
            DatabaseService db = (DatabaseService) ServiceLocator.getService("DatabaseService");
            db.query("INSERT INTO orders VALUES ('" + orderId + "', '" + username + "', " + amount + ")");
            
            logger.log("INFO", "Order completed successfully");
        }
    }
}

// Initial Context (Service Registry Initializer)
class InitialContext {
    public static void initializeServices() {
        System.out.println("🔧 Initializing services...");
        
        ServiceLocator.registerService("EmailService", new EmailService());
        ServiceLocator.registerService("LoggingService", new LoggingService());
        ServiceLocator.registerService("PaymentService", new PaymentService());
        ServiceLocator.registerService("AuthenticationService", new AuthenticationService());
        ServiceLocator.registerService("DatabaseService", new DatabaseService());
        
        System.out.println("✅ All services registered\n");
    }
}

// Demo
public class ServiceLocatorPattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   SERVICE LOCATOR PATTERN DEMONSTRATION  ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Initialize services
        InitialContext.initializeServices();
        
        // Example 1: Direct service lookup
        System.out.println("1. DIRECT SERVICE LOOKUP");
        System.out.println("────────────────────────");
        
        Service emailService = ServiceLocator.getService("EmailService");
        emailService.execute();
        
        System.out.println();
        Service loggingService = ServiceLocator.getService("LoggingService");
        loggingService.execute();
        
        // Example 2: Service caching
        System.out.println("\n2. SERVICE CACHING");
        System.out.println("──────────────────");
        
        System.out.println("First lookup:");
        ServiceLocator.getService("PaymentService");
        
        System.out.println("\nSecond lookup (from cache):");
        ServiceLocator.getService("PaymentService");
        
        // Example 3: Use services in controllers
        System.out.println("\n3. USER CONTROLLER (Multiple Service Usage)");
        System.out.println("────────────────────────────────────────────");
        
        UserController userController = new UserController();
        userController.createUser("alice", "alice@example.com");
        
        // Example 4: Order controller
        System.out.println("\n4. ORDER CONTROLLER (Service Composition)");
        System.out.println("──────────────────────────────────────────");
        
        OrderController orderController = new OrderController();
        orderController.placeOrder("alice", "ORD-001", 150.00);
        
        // Example 5: Dynamic service replacement
        System.out.println("\n5. DYNAMIC SERVICE REPLACEMENT");
        System.out.println("───────────────────────────────");
        
        System.out.println("Unregistering email service:");
        ServiceLocator.unregisterService("EmailService");
        
        System.out.println("\nRegistering mock email service:");
        Service mockEmail = new Service() {
            @Override
            public String getName() {
                return "EmailService";
            }
            
            @Override
            public void execute() {
                System.out.println("    📧 [MOCK] Email service (no actual sending)");
            }
        };
        ServiceLocator.registerService("EmailService", mockEmail);
        
        System.out.println("\nUsing mock service:");
        Service replacedService = ServiceLocator.getService("EmailService");
        replacedService.execute();
        
        // Example 6: Cache management
        System.out.println("\n6. CACHE MANAGEMENT");
        System.out.println("───────────────────");
        
        System.out.println("Clearing cache:");
        ServiceLocator.clearCache();
        
        System.out.println("\nLookup after cache clear:");
        ServiceLocator.getService("LoggingService");
        
        System.out.println("\n✅ Service Locator Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Centralized service registry");
        System.out.println("  • Service caching for performance");
        System.out.println("  • Loose coupling between clients and services");
        System.out.println("  • Dynamic service registration/replacement");
        System.out.println("  • Easy testing with mock services");
        
        System.out.println("\n⚠️  Note: Dependency Injection is often preferred over Service Locator");
        System.out.println("   because it makes dependencies explicit.");
    }
}
