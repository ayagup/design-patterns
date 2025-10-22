package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Service Mesh Pattern
 * ======================
 * 
 * Intent:
 * Provides infrastructure layer for service-to-service communication
 * with features like load balancing, service discovery, encryption,
 * authentication, authorization, and observability.
 * 
 * Also Known As:
 * - Sidecar Proxy Pattern
 * - Service Mesh Infrastructure
 * 
 * Motivation:
 * - Cross-cutting concerns for service communication
 * - Avoid duplicating logic in each service
 * - Centralized traffic management
 * - Security and observability
 * 
 * Applicability:
 * - Large microservices deployments
 * - Need consistent communication policies
 * - Want traffic management
 * - Require strong security
 * 
 * Structure:
 * Service A -> Proxy A -> Proxy B -> Service B
 * Control Plane manages all proxies
 * 
 * Benefits:
 * + Centralized policies
 * + Language-agnostic
 * + Security
 * + Observability
 * + Traffic management
 */

// ============================================================================
// SERVICE PROXY (Sidecar)
// ============================================================================

class ServiceProxy {
    private final String serviceName;
    private final ServiceMeshControlPlane controlPlane;
    
    // Metrics
    private long requestCount = 0;
    private long failureCount = 0;
    
    public ServiceProxy(String serviceName, ServiceMeshControlPlane controlPlane) {
        this.serviceName = serviceName;
        this.controlPlane = controlPlane;
        controlPlane.registerProxy(this);
    }
    
    public String call(String targetService, String request) {
        System.out.println("[" + serviceName + "-Proxy] Intercepting call to " + targetService);
        
        long startTime = System.currentTimeMillis();
        requestCount++;
        
        try {
            // 1. Service Discovery
            System.out.println("[" + serviceName + "-Proxy] Discovering " + targetService);
            ServiceInstance instance = controlPlane.discoverService(targetService);
            if (instance == null) {
                throw new RuntimeException("Service not found: " + targetService);
            }
            
            // 2. Load Balancing
            System.out.println("[" + serviceName + "-Proxy] Routing to: " + instance.getAddress());
            
            // 3. Circuit Breaking
            if (!controlPlane.isServiceHealthy(targetService)) {
                System.out.println("[" + serviceName + "-Proxy] Circuit open for " + targetService + ", using fallback");
                return "FALLBACK_RESPONSE";
            }
            
            // 4. Authentication/Authorization (mTLS simulation)
            System.out.println("[" + serviceName + "-Proxy] Verifying mTLS certificate");
            
            // 5. Retry logic
            int maxRetries = 2;
            Exception lastException = null;
            
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    // 6. Actual call
                    String response = instance.handle(request);
                    
                    // 7. Collect metrics
                    long duration = System.currentTimeMillis() - startTime;
                    controlPlane.recordMetric(serviceName, targetService, duration, true);
                    
                    System.out.println("[" + serviceName + "-Proxy] Call successful (" + duration + "ms)");
                    return response;
                    
                } catch (Exception e) {
                    lastException = e;
                    if (attempt < maxRetries - 1) {
                        System.out.println("[" + serviceName + "-Proxy] Retry " + (attempt + 1) + " after failure");
                        Thread.sleep(100);
                    }
                }
            }
            
            throw lastException;
            
        } catch (Exception e) {
            failureCount++;
            long duration = System.currentTimeMillis() - startTime;
            controlPlane.recordMetric(serviceName, targetService, duration, false);
            System.out.println("[" + serviceName + "-Proxy] Call failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public long getRequestCount() {
        return requestCount;
    }
    
    public long getFailureCount() {
        return failureCount;
    }
}

// ============================================================================
// SERVICE INSTANCE
// ============================================================================

class ServiceInstance {
    private final String serviceName;
    private final String address;
    private boolean healthy = true;
    
    public ServiceInstance(String serviceName, String address) {
        this.serviceName = serviceName;
        this.address = address;
    }
    
    public String handle(String request) {
        // Simulate processing
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        // Simulate occasional failures
        if (Math.random() < 0.1) {
            throw new RuntimeException("Service error");
        }
        
        return serviceName + " processed: " + request;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public boolean isHealthy() {
        return healthy;
    }
    
    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}

// ============================================================================
// CONTROL PLANE
// ============================================================================

class ServiceMeshControlPlane {
    private final Map<String, List<ServiceInstance>> serviceRegistry = new ConcurrentHashMap<>();
    private final Map<String, Boolean> circuitBreakers = new ConcurrentHashMap<>();
    private final List<ServiceProxy> proxies = new CopyOnWriteArrayList<>();
    private final List<String> metrics = new CopyOnWriteArrayList<>();
    
    public void registerService(ServiceInstance instance) {
        serviceRegistry.computeIfAbsent(instance.getServiceName(), k -> new CopyOnWriteArrayList<>())
            .add(instance);
        circuitBreakers.put(instance.getServiceName(), true); // Closed (healthy)
        System.out.println("[ControlPlane] Registered service: " + instance.getServiceName() + " at " + instance.getAddress());
    }
    
    public void registerProxy(ServiceProxy proxy) {
        proxies.add(proxy);
        System.out.println("[ControlPlane] Registered proxy for: " + proxy.getServiceName());
    }
    
    public ServiceInstance discoverService(String serviceName) {
        List<ServiceInstance> instances = serviceRegistry.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            return null;
        }
        
        // Simple round-robin load balancing
        return instances.get(0);
    }
    
    public boolean isServiceHealthy(String serviceName) {
        return circuitBreakers.getOrDefault(serviceName, false);
    }
    
    public void recordMetric(String source, String target, long duration, boolean success) {
        String metric = String.format("[Metric] %s -> %s: %dms, success=%b", source, target, duration, success);
        metrics.add(metric);
    }
    
    public void printTopology() {
        System.out.println("\n=== Service Mesh Topology ===");
        System.out.println("Registered Services:");
        serviceRegistry.forEach((name, instances) -> {
            System.out.println("  " + name + ": " + instances.size() + " instance(s)");
            instances.forEach(inst -> System.out.println("    - " + inst.getAddress()));
        });
        
        System.out.println("\nRegistered Proxies:");
        proxies.forEach(proxy -> {
            System.out.println("  " + proxy.getServiceName() + 
                " (requests: " + proxy.getRequestCount() + 
                ", failures: " + proxy.getFailureCount() + ")");
        });
    }
    
    public void printMetrics() {
        System.out.println("\n=== Service Mesh Metrics ===");
        metrics.forEach(System.out::println);
    }
}

// ============================================================================
// BUSINESS SERVICES (Behind Proxies)
// ============================================================================

class OrderServiceMesh {
    private final ServiceProxy proxy;
    
    public OrderServiceMesh(ServiceProxy proxy) {
        this.proxy = proxy;
    }
    
    public void createOrder(String orderId) {
        System.out.println("\n[OrderService] Creating order: " + orderId);
        
        // Calls to other services go through proxy
        String userInfo = proxy.call("UserService", "getUser:123");
        System.out.println("[OrderService] Received: " + userInfo);
        
        String productInfo = proxy.call("ProductService", "getProduct:456");
        System.out.println("[OrderService] Received: " + productInfo);
        
        System.out.println("[OrderService] Order created successfully");
    }
}

/**
 * Demonstration of Service Mesh Pattern
 */
public class ServiceMeshPattern {
    public static void main(String[] args) {
        System.out.println("=== Service Mesh Pattern ===\n");
        
        // Create control plane
        ServiceMeshControlPlane controlPlane = new ServiceMeshControlPlane();
        
        System.out.println("--- Setting Up Service Mesh ---\n");
        
        // Register service instances with control plane
        controlPlane.registerService(new ServiceInstance("UserService", "10.0.1.10:8080"));
        controlPlane.registerService(new ServiceInstance("ProductService", "10.0.1.20:8080"));
        controlPlane.registerService(new ServiceInstance("OrderService", "10.0.1.30:8080"));
        
        // Create proxies (sidecars) for each service
        ServiceProxy orderProxy = new ServiceProxy("OrderService", controlPlane);
        ServiceProxy userProxy = new ServiceProxy("UserService", controlPlane);
        ServiceProxy productProxy = new ServiceProxy("ProductService", controlPlane);
        
        controlPlane.printTopology();
        
        System.out.println("\n--- Processing Requests Through Mesh ---");
        
        // Business service uses proxy for all outbound calls
        OrderServiceMesh orderService = new OrderServiceMesh(orderProxy);
        
        try {
            orderService.createOrder("ORD-001");
        } catch (Exception e) {
            System.out.println("Order creation failed: " + e.getMessage());
        }
        
        try {
            orderService.createOrder("ORD-002");
        } catch (Exception e) {
            System.out.println("Order creation failed: " + e.getMessage());
        }
        
        // Show collected metrics and topology
        controlPlane.printMetrics();
        controlPlane.printTopology();
        
        System.out.println("\n\n=== Service Mesh Features ===");
        System.out.println("1. Service Discovery - automatic service location");
        System.out.println("2. Load Balancing - distributes traffic");
        System.out.println("3. Circuit Breaking - prevents cascading failures");
        System.out.println("4. Retry Logic - automatic retries on failure");
        System.out.println("5. mTLS - encrypted service-to-service communication");
        System.out.println("6. Observability - metrics and tracing");
        System.out.println("7. Traffic Management - canary deployments, A/B testing");
        
        System.out.println("\n=== Benefits ===");
        System.out.println("+ Language-agnostic - works with any language");
        System.out.println("+ Centralized policies - configured in one place");
        System.out.println("+ Security - mTLS, authorization, authentication");
        System.out.println("+ Observability - automatic metrics and traces");
        System.out.println("+ Resilience - retries, timeouts, circuit breakers");
        
        System.out.println("\n=== Real-World Implementations ===");
        System.out.println("- Istio (most popular)");
        System.out.println("- Linkerd (CNCF)");
        System.out.println("- Consul Connect (HashiCorp)");
        System.out.println("- AWS App Mesh");
    }
}
