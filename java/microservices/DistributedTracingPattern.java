package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Distributed Tracing Pattern
 * =============================
 * 
 * Intent:
 * Traces requests as they flow through multiple microservices to understand
 * system behavior, identify bottlenecks, and debug issues.
 * 
 * Also Known As:
 * - Request Tracing
 * - Distributed Context Propagation
 * 
 * Motivation:
 * - Single request spans multiple services
 * - Need to correlate logs and metrics
 * - Debug latency and errors
 * - Understand service dependencies
 * 
 * Applicability:
 * - Microservices architecture
 * - Need to track requests across services
 * - Performance analysis required
 * - Debugging distributed systems
 * 
 * Structure:
 * Trace ID (unique per request) propagated across all services
 * Each service creates Spans (unit of work) under the Trace
 * 
 * Participants:
 * - Trace: Represents entire request journey
 * - Span: Represents single operation in a service
 * - Trace Context: Propagated between services
 * - Tracer: Creates and manages spans
 * 
 * Benefits:
 * + Visibility into distributed systems
 * + Performance analysis
 * + Root cause analysis
 * + Service dependency mapping
 */

// ============================================================================
// TRACE CONTEXT
// ============================================================================

class TraceContext {
    private final String traceId;
    private final String parentSpanId;
    
    public TraceContext(String traceId, String parentSpanId) {
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
    }
    
    public String getTraceId() { return traceId; }
    public String getParentSpanId() { return parentSpanId; }
    
    @Override
    public String toString() {
        return String.format("TraceContext{traceId='%s', parentSpanId='%s'}", traceId, parentSpanId);
    }
}

// ============================================================================
// SPAN
// ============================================================================

class Span {
    private final String spanId;
    private final String traceId;
    private final String parentSpanId;
    private final String serviceName;
    private final String operationName;
    private final long startTime;
    private long endTime;
    private final Map<String, String> tags = new HashMap<>();
    private final List<String> logs = new ArrayList<>();
    
    public Span(String traceId, String parentSpanId, String serviceName, String operationName) {
        this.spanId = UUID.randomUUID().toString().substring(0, 8);
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.serviceName = serviceName;
        this.operationName = operationName;
        this.startTime = System.currentTimeMillis();
    }
    
    public void addTag(String key, String value) {
        tags.put(key, value);
    }
    
    public void log(String message) {
        logs.add(System.currentTimeMillis() + ": " + message);
    }
    
    public void finish() {
        this.endTime = System.currentTimeMillis();
        System.out.println(this);
    }
    
    public String getSpanId() { return spanId; }
    public String getTraceId() { return traceId; }
    public long getDuration() { return endTime - startTime; }
    
    @Override
    public String toString() {
        return String.format(
            "  [SPAN] %s | Trace: %s | Parent: %s | Service: %s | Operation: %s | Duration: %dms | Tags: %s",
            spanId, traceId, parentSpanId != null ? parentSpanId : "root",
            serviceName, operationName, getDuration(), tags
        );
    }
}

// ============================================================================
// TRACER
// ============================================================================

class Tracer {
    // Made public for testing/demonstration purposes
    public static final List<Span> allSpans = new CopyOnWriteArrayList<>();
    
    public static Span startSpan(String serviceName, String operationName, TraceContext context) {
        String traceId = context != null ? context.getTraceId() : UUID.randomUUID().toString().substring(0, 8);
        String parentSpanId = context != null ? context.getParentSpanId() : null;
        
        Span span = new Span(traceId, parentSpanId, serviceName, operationName);
        allSpans.add(span);
        
        System.out.println("[TRACE] Started span: " + span.getSpanId() + " in " + serviceName);
        return span;
    }
    
    public static TraceContext extractContext(Span span) {
        return new TraceContext(span.getTraceId(), span.getSpanId());
    }
    
    public static List<Span> getTrace(String traceId) {
        List<Span> trace = new ArrayList<>();
        for (Span span : allSpans) {
            if (span.getTraceId().equals(traceId)) {
                trace.add(span);
            }
        }
        return trace;
    }
    
    public static void printTrace(String traceId) {
        System.out.println("\n=== DISTRIBUTED TRACE: " + traceId + " ===");
        List<Span> spans = getTrace(traceId);
        long totalDuration = spans.stream().mapToLong(Span::getDuration).sum();
        System.out.println("Total spans: " + spans.size());
        System.out.println("Accumulated duration: " + totalDuration + "ms");
        System.out.println("\nSpan Timeline:");
        for (Span span : spans) {
            System.out.println(span);
        }
    }
}

// ============================================================================
// MICROSERVICES WITH TRACING
// ============================================================================

class APIGatewayService {
    public void handleRequest(String requestId) {
        // Start root span (no parent context)
        Span span = Tracer.startSpan("APIGateway", "handleRequest", null);
        span.addTag("requestId", requestId);
        span.log("Request received");
        
        try {
            simulateWork(20);
            
            // Extract context to pass to downstream service
            TraceContext context = Tracer.extractContext(span);
            
            // Call downstream services
            span.log("Calling UserService");
            UserServiceTraced userService = new UserServiceTraced();
            userService.getUser("U123", context);
            
            span.log("Calling OrderService");
            OrderServiceTraced orderService = new OrderServiceTraced();
            orderService.getOrders("U123", context);
            
            span.log("Request completed");
        } finally {
            span.finish();
        }
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class UserServiceTraced {
    public void getUser(String userId, TraceContext context) {
        // Continue trace with parent context
        Span span = Tracer.startSpan("UserService", "getUser", context);
        span.addTag("userId", userId);
        span.log("Fetching user from database");
        
        try {
            simulateWork(50);
            
            // Call another service
            TraceContext childContext = Tracer.extractContext(span);
            ProfileService profileService = new ProfileService();
            profileService.getProfile(userId, childContext);
            
            span.log("User data retrieved");
        } finally {
            span.finish();
        }
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ProfileService {
    public void getProfile(String userId, TraceContext context) {
        Span span = Tracer.startSpan("ProfileService", "getProfile", context);
        span.addTag("userId", userId);
        span.log("Fetching profile");
        
        try {
            simulateWork(30);
            span.log("Profile retrieved");
        } finally {
            span.finish();
        }
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class OrderServiceTraced {
    public void getOrders(String userId, TraceContext context) {
        Span span = Tracer.startSpan("OrderService", "getOrders", context);
        span.addTag("userId", userId);
        span.log("Querying orders");
        
        try {
            simulateWork(40);
            
            // Call payment service
            TraceContext childContext = Tracer.extractContext(span);
            PaymentServiceTraced paymentService = new PaymentServiceTraced();
            paymentService.getPaymentInfo("O456", childContext);
            
            span.log("Orders retrieved");
        } finally {
            span.finish();
        }
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class PaymentServiceTraced {
    public void getPaymentInfo(String orderId, TraceContext context) {
        Span span = Tracer.startSpan("PaymentService", "getPaymentInfo", context);
        span.addTag("orderId", orderId);
        span.log("Fetching payment info");
        
        try {
            simulateWork(25);
            span.log("Payment info retrieved");
        } finally {
            span.finish();
        }
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

/**
 * Demonstration of Distributed Tracing Pattern
 */
public class DistributedTracingPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Distributed Tracing Pattern ===\n");
        
        System.out.println("--- Processing Request with Distributed Tracing ---\n");
        
        APIGatewayService gateway = new APIGatewayService();
        gateway.handleRequest("REQ-001");
        
        // Get and print the complete trace
        Thread.sleep(100); // Ensure all spans finished
        
        // Find the trace ID from the first span (root span)
        Span rootSpan = Tracer.allSpans.get(0);
        Tracer.printTrace(rootSpan.getTraceId());
        
        System.out.println("\n\n=== Key Concepts ===");
        System.out.println("1. Trace ID - unique identifier for entire request");
        System.out.println("2. Span ID - unique identifier for each operation");
        System.out.println("3. Parent Span ID - links spans in hierarchy");
        System.out.println("4. Tags - metadata about the operation");
        System.out.println("5. Logs - events that occurred during span");
        
        System.out.println("\n=== Benefits ===");
        System.out.println("+ Visibility - see entire request flow");
        System.out.println("+ Performance - identify slow services");
        System.out.println("+ Debugging - trace errors to source");
        System.out.println("+ Dependencies - understand service relationships");
        
        System.out.println("\n=== Implementation Tools ===");
        System.out.println("- Jaeger (CNCF project)");
        System.out.println("- Zipkin");
        System.out.println("- AWS X-Ray");
        System.out.println("- OpenTelemetry (standard)");
    }
}
