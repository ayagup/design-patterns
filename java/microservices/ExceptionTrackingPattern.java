package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Exception Tracking Pattern
 * ============================
 * 
 * Intent:
 * Centrally captures, aggregates, and tracks exceptions across all
 * microservices for monitoring, alerting, and debugging.
 * 
 * Also Known As:
 * - Error Tracking
 * - Exception Monitoring
 * 
 * Motivation:
 * - Exceptions scattered across services
 * - Need centralized error monitoring
 * - Track error trends and patterns
 * - Enable proactive fixes
 * - Alert on critical errors
 * 
 * Applicability:
 * - Production microservices
 * - Need visibility into errors
 * - Want to track error trends
 * - Debugging distributed systems
 * 
 * Benefits:
 * + Centralized error visibility
 * + Trend analysis
 * + Automatic alerting
 * + Stack trace aggregation
 * + Error grouping and deduplication
 */

// ============================================================================
// EXCEPTION EVENT
// ============================================================================

class ExceptionEvent {
    private final String id;
    private final long timestamp;
    private final String serviceName;
    private final String environment;
    private final String exceptionType;
    private final String message;
    private final List<String> stackTrace;
    private final String userId;
    private final String traceId;
    private final Map<String, String> context;
    private final String severity;
    
    public ExceptionEvent(String serviceName, String environment, Throwable exception,
                         String userId, String traceId, String severity) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.serviceName = serviceName;
        this.environment = environment;
        this.exceptionType = exception.getClass().getSimpleName();
        this.message = exception.getMessage();
        this.stackTrace = extractStackTrace(exception);
        this.userId = userId;
        this.traceId = traceId;
        this.context = new HashMap<>();
        this.severity = severity;
    }
    
    private List<String> extractStackTrace(Throwable exception) {
        List<String> trace = new ArrayList<>();
        for (StackTraceElement element : exception.getStackTrace()) {
            trace.add(element.toString());
            if (trace.size() >= 10) break; // Limit to first 10 lines
        }
        return trace;
    }
    
    public void addContext(String key, String value) {
        context.put(key, value);
    }
    
    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public String getServiceName() { return serviceName; }
    public String getExceptionType() { return exceptionType; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getUserId() { return userId; }
    public String getTraceId() { return traceId; }
    
    @Override
    public String toString() {
        return String.format("[%tT] [%s] [%s] %s: %s (User: %s, Trace: %s)",
            timestamp, severity, serviceName, exceptionType, message, userId, traceId);
    }
    
    public String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString()).append("\n");
        sb.append("  Environment: ").append(environment).append("\n");
        sb.append("  Context: ").append(context).append("\n");
        sb.append("  Stack Trace:\n");
        for (String line : stackTrace) {
            sb.append("    ").append(line).append("\n");
        }
        return sb.toString();
    }
}

// ============================================================================
// EXCEPTION TRACKER (Centralized)
// ============================================================================

class ExceptionTracker {
    private final List<ExceptionEvent> exceptions = new CopyOnWriteArrayList<>();
    private final Map<String, Integer> exceptionCounts = new ConcurrentHashMap<>();
    
    public void track(ExceptionEvent event) {
        exceptions.add(event);
        
        // Count occurrences
        String key = event.getServiceName() + ":" + event.getExceptionType();
        exceptionCounts.merge(key, 1, Integer::sum);
        
        // In production: send to Sentry, Rollbar, Raygun, etc.
        System.out.println("[ExceptionTracker] " + event);
        
        // Alert on critical errors
        if ("CRITICAL".equals(event.getSeverity())) {
            sendAlert(event);
        }
    }
    
    private void sendAlert(ExceptionEvent event) {
        System.out.println("  !! ALERT !! Critical exception: " + event.getMessage());
    }
    
    // Query methods
    public List<ExceptionEvent> findByService(String serviceName) {
        List<ExceptionEvent> results = new ArrayList<>();
        for (ExceptionEvent event : exceptions) {
            if (serviceName.equals(event.getServiceName())) {
                results.add(event);
            }
        }
        return results;
    }
    
    public List<ExceptionEvent> findBySeverity(String severity) {
        List<ExceptionEvent> results = new ArrayList<>();
        for (ExceptionEvent event : exceptions) {
            if (severity.equals(event.getSeverity())) {
                results.add(event);
            }
        }
        return results;
    }
    
    public List<ExceptionEvent> findByUser(String userId) {
        List<ExceptionEvent> results = new ArrayList<>();
        for (ExceptionEvent event : exceptions) {
            if (userId.equals(event.getUserId())) {
                results.add(event);
            }
        }
        return results;
    }
    
    public List<ExceptionEvent> findByTrace(String traceId) {
        List<ExceptionEvent> results = new ArrayList<>();
        for (ExceptionEvent event : exceptions) {
            if (traceId.equals(event.getTraceId())) {
                results.add(event);
            }
        }
        return results;
    }
    
    public Map<String, Integer> getExceptionCounts() {
        return new HashMap<>(exceptionCounts);
    }
    
    public int getTotalExceptions() {
        return exceptions.size();
    }
}

// ============================================================================
// SERVICES WITH EXCEPTION TRACKING
// ============================================================================

class PaymentProcessingService {
    private final ExceptionTracker tracker;
    private final String serviceName = "PaymentService";
    
    public PaymentProcessingService(ExceptionTracker tracker) {
        this.tracker = tracker;
    }
    
    public void processPayment(String userId, double amount, String traceId) {
        System.out.println("\n[PaymentService] Processing payment: $" + amount);
        
        try {
            if (amount > 10000) {
                throw new IllegalArgumentException("Amount exceeds limit");
            }
            
            if (Math.random() < 0.2) { // Simulate 20% failure rate
                throw new RuntimeException("Payment gateway timeout");
            }
            
            System.out.println("[PaymentService] Payment successful");
            
        } catch (Exception e) {
            // Track exception
            ExceptionEvent event = new ExceptionEvent(serviceName, "production", e, userId, traceId, "HIGH");
            event.addContext("amount", String.valueOf(amount));
            event.addContext("paymentMethod", "CreditCard");
            tracker.track(event);
            
            throw e; // Re-throw after tracking
        }
    }
}

class UserAuthenticationService {
    private final ExceptionTracker tracker;
    private final String serviceName = "AuthService";
    
    public UserAuthenticationService(ExceptionTracker tracker) {
        this.tracker = tracker;
    }
    
    public void authenticate(String userId, String password, String traceId) {
        System.out.println("\n[AuthService] Authenticating user: " + userId);
        
        try {
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty");
            }
            
            if (password.equals("wrongpassword")) {
                throw new SecurityException("Invalid credentials");
            }
            
            System.out.println("[AuthService] Authentication successful");
            
        } catch (SecurityException e) {
            // Track security exception (CRITICAL)
            ExceptionEvent event = new ExceptionEvent(serviceName, "production", e, userId, traceId, "CRITICAL");
            event.addContext("attemptedUser", userId);
            event.addContext("ipAddress", "192.168.1.100");
            tracker.track(event);
            
            throw e;
        } catch (Exception e) {
            // Track other exceptions
            ExceptionEvent event = new ExceptionEvent(serviceName, "production", e, userId, traceId, "MEDIUM");
            tracker.track(event);
            
            throw e;
        }
    }
}

class DataProcessingService {
    private final ExceptionTracker tracker;
    private final String serviceName = "DataService";
    
    public DataProcessingService(ExceptionTracker tracker) {
        this.tracker = tracker;
    }
    
    public void processData(String userId, String data, String traceId) {
        System.out.println("\n[DataService] Processing data...");
        
        try {
            if (data == null) {
                throw new NullPointerException("Data cannot be null");
            }
            
            if (Math.random() < 0.3) { // Simulate 30% failure
                throw new RuntimeException("Database connection failed");
            }
            
            System.out.println("[DataService] Data processed successfully");
            
        } catch (Exception e) {
            // Track exception
            ExceptionEvent event = new ExceptionEvent(serviceName, "production", e, userId, traceId, "HIGH");
            event.addContext("dataSize", data != null ? String.valueOf(data.length()) : "null");
            tracker.track(event);
            
            throw e;
        }
    }
}

/**
 * Demonstration of Exception Tracking Pattern
 */
public class ExceptionTrackingPattern {
    public static void main(String[] args) {
        System.out.println("=== Exception Tracking Pattern ===\n");
        
        // Create centralized exception tracker
        ExceptionTracker tracker = new ExceptionTracker();
        
        // Create services with exception tracking
        PaymentProcessingService paymentService = new PaymentProcessingService(tracker);
        UserAuthenticationService authService = new UserAuthenticationService(tracker);
        DataProcessingService dataService = new DataProcessingService(tracker);
        
        System.out.println("--- Processing Requests (Some Will Fail) ---");
        
        // Simulate various operations with some failures
        String traceId1 = "TRACE-001";
        try {
            paymentService.processPayment("USER-123", 500.0, traceId1);
        } catch (Exception e) {
            // Exception already tracked
        }
        
        try {
            authService.authenticate("USER-456", "wrongpassword", traceId1);
        } catch (Exception e) {
            // Exception already tracked
        }
        
        String traceId2 = "TRACE-002";
        try {
            paymentService.processPayment("USER-789", 15000.0, traceId2); // Exceeds limit
        } catch (Exception e) {
            // Exception already tracked
        }
        
        try {
            dataService.processData("USER-789", "sample data", traceId2);
        } catch (Exception e) {
            // Exception already tracked
        }
        
        String traceId3 = "TRACE-003";
        try {
            dataService.processData("USER-123", null, traceId3); // Null data
        } catch (Exception e) {
            // Exception already tracked
        }
        
        try {
            authService.authenticate("USER-123", "", traceId3); // Empty password
        } catch (Exception e) {
            // Exception already tracked
        }
        
        System.out.println("\n\n--- Exception Analysis ---\n");
        
        System.out.println("1. All exceptions from PaymentService:");
        List<ExceptionEvent> paymentExceptions = tracker.findByService("PaymentService");
        paymentExceptions.forEach(e -> System.out.println("  " + e));
        
        System.out.println("\n2. All CRITICAL exceptions:");
        List<ExceptionEvent> criticalExceptions = tracker.findBySeverity("CRITICAL");
        criticalExceptions.forEach(e -> System.out.println("  " + e.getDetailedReport()));
        
        System.out.println("\n3. All exceptions in TRACE-002:");
        List<ExceptionEvent> trace2Exceptions = tracker.findByTrace("TRACE-002");
        trace2Exceptions.forEach(e -> System.out.println("  " + e));
        
        System.out.println("\n\n=== Exception Statistics ===");
        System.out.println("Total exceptions: " + tracker.getTotalExceptions());
        System.out.println("\nExceptions by service and type:");
        tracker.getExceptionCounts().forEach((key, count) -> 
            System.out.println("  " + key + ": " + count));
        
        System.out.println("\n=== Key Benefits ===");
        System.out.println("1. Centralized - all exceptions in one place");
        System.out.println("2. Context - captures user, trace, and metadata");
        System.out.println("3. Alerting - automatic alerts on critical errors");
        System.out.println("4. Trends - track error patterns over time");
        System.out.println("5. Debugging - full stack traces and context");
        
        System.out.println("\n=== Real-World Tools ===");
        System.out.println("- Sentry");
        System.out.println("- Rollbar");
        System.out.println("- Raygun");
        System.out.println("- Bugsnag");
        System.out.println("- Airbrake");
    }
}
