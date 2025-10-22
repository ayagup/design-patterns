package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * Gateway Routing Pattern
 * 
 * Intent: Route requests to different backend services based on request 
 * attributes such as path, headers, or content, using a single entry point.
 * 
 * Also Known As: API Gateway Router, Request Router
 * 
 * Motivation:
 * In microservices architecture, clients need to call different services.
 * Instead of clients knowing about all service endpoints, a gateway routes
 * requests to appropriate services based on routing rules.
 * 
 * Applicability:
 * - Multiple backend services need to be accessed through single endpoint
 * - Service endpoints should be hidden from clients
 * - Need dynamic routing based on request attributes
 * - Want to version APIs without changing client code
 * - Need A/B testing or canary deployments
 * - Service discovery integration required
 * 
 * Benefits:
 * - Single entry point for all services
 * - Simplified client configuration
 * - Dynamic routing without client changes
 * - Support for A/B testing and canary releases
 * - Easy service versioning
 * - Service location transparency
 * 
 * Implementation Considerations:
 * - Gateway as single point of failure
 * - Routing rule complexity
 * - Performance overhead
 * - Need for service discovery
 * - Health check integration
 * - Circuit breaker for failed services
 */

// Request model
class HttpRequest {
    private final String requestId;
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final String body;
    
    public HttpRequest(String requestId, String method, String path,
                      Map<String, String> headers, Map<String, String> queryParams, String body) {
        this.requestId = requestId;
        this.method = method;
        this.path = path;
        this.headers = headers != null ? headers : new HashMap<>();
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
        this.body = body;
    }
    
    public String getRequestId() { return requestId; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public Map<String, String> getQueryParams() { return queryParams; }
    public String getBody() { return body; }
    
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }
}

// Response model
class HttpResponse {
    private final int statusCode;
    private final String body;
    private final String serviceName;
    private final long responseTime;
    
    public HttpResponse(int statusCode, String body, String serviceName, long responseTime) {
        this.statusCode = statusCode;
        this.body = body;
        this.serviceName = serviceName;
        this.responseTime = responseTime;
    }
    
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
    public String getServiceName() { return serviceName; }
    public long getResponseTime() { return responseTime; }
}

// Service endpoint interface
interface ServiceEndpoint {
    HttpResponse handleRequest(HttpRequest request);
    String getServiceName();
    String getVersion();
    boolean isHealthy();
}

// Example service implementations
class UserServiceV1 implements ServiceEndpoint {
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        try { Thread.sleep(50); } catch (InterruptedException e) { }
        String body = "{\"service\":\"UserService\",\"version\":\"v1\",\"data\":\"User data\"}";
        return new HttpResponse(200, body, "UserService-v1", 50);
    }
    
    @Override
    public String getServiceName() { return "UserService"; }
    
    @Override
    public String getVersion() { return "v1"; }
    
    @Override
    public boolean isHealthy() { return true; }
}

class UserServiceV2 implements ServiceEndpoint {
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        try { Thread.sleep(45); } catch (InterruptedException e) { }
        String body = "{\"service\":\"UserService\",\"version\":\"v2\",\"data\":\"Enhanced user data\"}";
        return new HttpResponse(200, body, "UserService-v2", 45);
    }
    
    @Override
    public String getServiceName() { return "UserService"; }
    
    @Override
    public String getVersion() { return "v2"; }
    
    @Override
    public boolean isHealthy() { return true; }
}

class OrderService implements ServiceEndpoint {
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        try { Thread.sleep(80); } catch (InterruptedException e) { }
        String body = "{\"service\":\"OrderService\",\"orders\":[{\"id\":\"O1\"}]}";
        return new HttpResponse(200, body, "OrderService-v1", 80);
    }
    
    @Override
    public String getServiceName() { return "OrderService"; }
    
    @Override
    public String getVersion() { return "v1"; }
    
    @Override
    public boolean isHealthy() { return true; }
}

class PaymentService implements ServiceEndpoint {
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        try { Thread.sleep(100); } catch (InterruptedException e) { }
        String body = "{\"service\":\"PaymentService\",\"status\":\"processed\"}";
        return new HttpResponse(200, body, "PaymentService-v1", 100);
    }
    
    @Override
    public String getServiceName() { return "PaymentService"; }
    
    @Override
    public String getVersion() { return "v1"; }
    
    @Override
    public boolean isHealthy() { return true; }
}

// Routing rule interface
interface RoutingRule {
    boolean matches(HttpRequest request);
    ServiceEndpoint getTargetService();
    String getRuleName();
}

// Example 1: Path-based routing rule
class PathRoutingRule implements RoutingRule {
    private final Pattern pathPattern;
    private final ServiceEndpoint targetService;
    private final String ruleName;
    
    public PathRoutingRule(String pathPattern, ServiceEndpoint targetService, String ruleName) {
        this.pathPattern = Pattern.compile(pathPattern);
        this.targetService = targetService;
        this.ruleName = ruleName;
    }
    
    @Override
    public boolean matches(HttpRequest request) {
        return pathPattern.matcher(request.getPath()).matches();
    }
    
    @Override
    public ServiceEndpoint getTargetService() {
        return targetService;
    }
    
    @Override
    public String getRuleName() {
        return ruleName;
    }
}

// Example 2: Header-based routing rule (for A/B testing)
class HeaderRoutingRule implements RoutingRule {
    private final String headerName;
    private final String headerValue;
    private final ServiceEndpoint targetService;
    private final String ruleName;
    
    public HeaderRoutingRule(String headerName, String headerValue, 
                            ServiceEndpoint targetService, String ruleName) {
        this.headerName = headerName;
        this.headerValue = headerValue;
        this.targetService = targetService;
        this.ruleName = ruleName;
    }
    
    @Override
    public boolean matches(HttpRequest request) {
        String value = request.getHeader(headerName);
        return headerValue.equals(value);
    }
    
    @Override
    public ServiceEndpoint getTargetService() {
        return targetService;
    }
    
    @Override
    public String getRuleName() {
        return ruleName;
    }
}

// Example 3: Version-based routing rule
class VersionRoutingRule implements RoutingRule {
    private final String version;
    private final ServiceEndpoint targetService;
    private final String ruleName;
    
    public VersionRoutingRule(String version, ServiceEndpoint targetService, String ruleName) {
        this.version = version;
        this.targetService = targetService;
        this.ruleName = ruleName;
    }
    
    @Override
    public boolean matches(HttpRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("version=" + version);
    }
    
    @Override
    public ServiceEndpoint getTargetService() {
        return targetService;
    }
    
    @Override
    public String getRuleName() {
        return ruleName;
    }
}

// Example 4: Weighted routing rule (for canary deployments)
class WeightedRoutingRule implements RoutingRule {
    private final Pattern pathPattern;
    private final List<WeightedService> services;
    private final Random random;
    private final String ruleName;
    
    public WeightedRoutingRule(String pathPattern, List<WeightedService> services, String ruleName) {
        this.pathPattern = Pattern.compile(pathPattern);
        this.services = services;
        this.random = new Random();
        this.ruleName = ruleName;
    }
    
    @Override
    public boolean matches(HttpRequest request) {
        return pathPattern.matcher(request.getPath()).matches();
    }
    
    @Override
    public ServiceEndpoint getTargetService() {
        int totalWeight = services.stream().mapToInt(WeightedService::getWeight).sum();
        int randomValue = random.nextInt(totalWeight);
        
        int cumulativeWeight = 0;
        for (WeightedService ws : services) {
            cumulativeWeight += ws.getWeight();
            if (randomValue < cumulativeWeight) {
                return ws.getService();
            }
        }
        
        return services.get(0).getService(); // Fallback
    }
    
    @Override
    public String getRuleName() {
        return ruleName;
    }
    
    static class WeightedService {
        private final ServiceEndpoint service;
        private final int weight;
        
        public WeightedService(ServiceEndpoint service, int weight) {
            this.service = service;
            this.weight = weight;
        }
        
        public ServiceEndpoint getService() { return service; }
        public int getWeight() { return weight; }
    }
}

// Gateway Router
class GatewayRouter {
    private final List<RoutingRule> routingRules;
    private final ServiceEndpoint defaultService;
    private final Map<String, Integer> routeStats;
    
    public GatewayRouter(ServiceEndpoint defaultService) {
        this.routingRules = new CopyOnWriteArrayList<>();
        this.defaultService = defaultService;
        this.routeStats = new ConcurrentHashMap<>();
    }
    
    public void addRule(RoutingRule rule) {
        routingRules.add(rule);
    }
    
    public HttpResponse route(HttpRequest request) {
        System.out.printf("\n[Router] Routing request: %s %s%n", 
            request.getMethod(), request.getPath());
        
        // Find matching rule
        for (RoutingRule rule : routingRules) {
            if (rule.matches(request)) {
                ServiceEndpoint service = rule.getTargetService();
                
                System.out.printf("[Router] Matched rule: %s → %s%n", 
                    rule.getRuleName(), service.getServiceName());
                
                // Track statistics
                routeStats.merge(service.getServiceName(), 1, Integer::sum);
                
                // Check service health
                if (!service.isHealthy()) {
                    System.out.println("[Router] Service unhealthy, using default");
                    return defaultService.handleRequest(request);
                }
                
                return service.handleRequest(request);
            }
        }
        
        // No rule matched, use default
        System.out.println("[Router] No rule matched, using default service");
        routeStats.merge("default", 1, Integer::sum);
        return defaultService.handleRequest(request);
    }
    
    public Map<String, Integer> getRouteStatistics() {
        return new HashMap<>(routeStats);
    }
    
    public void printStatistics() {
        System.out.println("\n=== Routing Statistics ===");
        routeStats.forEach((service, count) -> 
            System.out.printf("%s: %d requests%n", service, count));
    }
}

// Example 5: Service Registry for dynamic routing
class ServiceRegistry {
    private final Map<String, List<ServiceEndpoint>> servicesByName;
    private final Map<String, ServiceEndpoint> servicesById;
    
    public ServiceRegistry() {
        this.servicesByName = new ConcurrentHashMap<>();
        this.servicesById = new ConcurrentHashMap<>();
    }
    
    public void registerService(String serviceId, ServiceEndpoint service) {
        servicesById.put(serviceId, service);
        servicesByName.computeIfAbsent(service.getServiceName(), k -> new CopyOnWriteArrayList<>())
                     .add(service);
        System.out.printf("[Registry] Registered: %s (ID: %s, Version: %s)%n",
            service.getServiceName(), serviceId, service.getVersion());
    }
    
    public void deregisterService(String serviceId) {
        ServiceEndpoint service = servicesById.remove(serviceId);
        if (service != null) {
            List<ServiceEndpoint> instances = servicesByName.get(service.getServiceName());
            if (instances != null) {
                instances.remove(service);
            }
            System.out.printf("[Registry] Deregistered: %s%n", serviceId);
        }
    }
    
    public ServiceEndpoint getServiceById(String serviceId) {
        return servicesById.get(serviceId);
    }
    
    public List<ServiceEndpoint> getServicesByName(String serviceName) {
        return servicesByName.getOrDefault(serviceName, Collections.emptyList());
    }
    
    public ServiceEndpoint getHealthyService(String serviceName) {
        List<ServiceEndpoint> instances = getServicesByName(serviceName);
        return instances.stream()
            .filter(ServiceEndpoint::isHealthy)
            .findFirst()
            .orElse(null);
    }
    
    public void printRegistry() {
        System.out.println("\n=== Service Registry ===");
        servicesByName.forEach((name, instances) -> {
            System.out.printf("%s: %d instance(s)%n", name, instances.size());
            instances.forEach(svc -> System.out.printf("  - Version: %s, Healthy: %s%n",
                svc.getVersion(), svc.isHealthy()));
        });
    }
}

// Dynamic Gateway with Service Discovery
class DynamicGatewayRouter {
    private final ServiceRegistry registry;
    private final Map<String, String> pathToServiceMap;
    
    public DynamicGatewayRouter(ServiceRegistry registry) {
        this.registry = registry;
        this.pathToServiceMap = new ConcurrentHashMap<>();
    }
    
    public void mapPathToService(String pathPattern, String serviceName) {
        pathToServiceMap.put(pathPattern, serviceName);
        System.out.printf("[DynamicRouter] Mapped %s → %s%n", pathPattern, serviceName);
    }
    
    public HttpResponse route(HttpRequest request) {
        System.out.printf("\n[DynamicRouter] Routing: %s %s%n", 
            request.getMethod(), request.getPath());
        
        // Find service based on path
        for (Map.Entry<String, String> entry : pathToServiceMap.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey());
            if (pattern.matcher(request.getPath()).matches()) {
                String serviceName = entry.getValue();
                ServiceEndpoint service = registry.getHealthyService(serviceName);
                
                if (service != null) {
                    System.out.printf("[DynamicRouter] Routing to: %s (version %s)%n",
                        service.getServiceName(), service.getVersion());
                    return service.handleRequest(request);
                } else {
                    System.out.printf("[DynamicRouter] No healthy instance of %s found%n", serviceName);
                    return new HttpResponse(503, "{\"error\":\"Service unavailable\"}", 
                        "gateway", 0);
                }
            }
        }
        
        System.out.println("[DynamicRouter] No route found");
        return new HttpResponse(404, "{\"error\":\"Not found\"}", "gateway", 0);
    }
}

// Demonstration
public class GatewayRoutingPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Gateway Routing Pattern Demo ===\n");
        
        // Initialize services
        ServiceEndpoint userV1 = new UserServiceV1();
        ServiceEndpoint userV2 = new UserServiceV2();
        ServiceEndpoint orderService = new OrderService();
        ServiceEndpoint paymentService = new PaymentService();
        
        // Demo 1: Path-based routing
        System.out.println("--- Demo 1: Path-Based Routing ---");
        demoPathBasedRouting(userV1, orderService, paymentService);
        
        Thread.sleep(500);
        
        // Demo 2: Header-based routing (A/B testing)
        System.out.println("\n--- Demo 2: Header-Based Routing (A/B Testing) ---");
        demoHeaderBasedRouting(userV1, userV2);
        
        Thread.sleep(500);
        
        // Demo 3: Weighted routing (Canary deployment)
        System.out.println("\n--- Demo 3: Weighted Routing (Canary Deployment) ---");
        demoWeightedRouting(userV1, userV2);
        
        Thread.sleep(500);
        
        // Demo 4: Service registry with dynamic routing
        System.out.println("\n--- Demo 4: Service Registry & Dynamic Routing ---");
        demoServiceRegistry(userV1, userV2, orderService);
    }
    
    private static void demoPathBasedRouting(ServiceEndpoint userService, 
                                             ServiceEndpoint orderService,
                                             ServiceEndpoint paymentService) {
        GatewayRouter router = new GatewayRouter(userService);
        
        // Configure routing rules
        router.addRule(new PathRoutingRule("/api/users/.*", userService, "User Service Route"));
        router.addRule(new PathRoutingRule("/api/orders/.*", orderService, "Order Service Route"));
        router.addRule(new PathRoutingRule("/api/payments/.*", paymentService, "Payment Service Route"));
        
        // Test requests
        router.route(new HttpRequest("R1", "GET", "/api/users/123", null, null, null));
        router.route(new HttpRequest("R2", "GET", "/api/orders/456", null, null, null));
        router.route(new HttpRequest("R3", "POST", "/api/payments/pay", null, null, null));
        router.route(new HttpRequest("R4", "GET", "/api/unknown", null, null, null));
        
        router.printStatistics();
    }
    
    private static void demoHeaderBasedRouting(ServiceEndpoint userV1, ServiceEndpoint userV2) {
        GatewayRouter router = new GatewayRouter(userV1);
        
        // Route to v2 if beta header present
        router.addRule(new HeaderRoutingRule("X-Beta-User", "true", userV2, "Beta Users → v2"));
        router.addRule(new PathRoutingRule("/api/users/.*", userV1, "Default Users → v1"));
        
        // Regular user
        Map<String, String> headers1 = new HashMap<>();
        HttpRequest req1 = new HttpRequest("R1", "GET", "/api/users/123", headers1, null, null);
        router.route(req1);
        
        // Beta user
        Map<String, String> headers2 = new HashMap<>();
        headers2.put("X-Beta-User", "true");
        HttpRequest req2 = new HttpRequest("R2", "GET", "/api/users/123", headers2, null, null);
        router.route(req2);
        
        router.printStatistics();
    }
    
    private static void demoWeightedRouting(ServiceEndpoint userV1, ServiceEndpoint userV2) {
        GatewayRouter router = new GatewayRouter(userV1);
        
        // 90% traffic to v1, 10% to v2 (canary)
        List<WeightedRoutingRule.WeightedService> weights = Arrays.asList(
            new WeightedRoutingRule.WeightedService(userV1, 90),
            new WeightedRoutingRule.WeightedService(userV2, 10)
        );
        router.addRule(new WeightedRoutingRule("/api/users/.*", weights, "Canary Deployment"));
        
        // Simulate 10 requests
        for (int i = 1; i <= 10; i++) {
            HttpRequest req = new HttpRequest("R" + i, "GET", "/api/users/123", null, null, null);
            router.route(req);
        }
        
        router.printStatistics();
    }
    
    private static void demoServiceRegistry(ServiceEndpoint userV1, ServiceEndpoint userV2,
                                            ServiceEndpoint orderService) {
        ServiceRegistry registry = new ServiceRegistry();
        
        // Register services
        registry.registerService("user-v1-instance1", userV1);
        registry.registerService("user-v2-instance1", userV2);
        registry.registerService("order-v1-instance1", orderService);
        
        registry.printRegistry();
        
        // Create dynamic router
        DynamicGatewayRouter router = new DynamicGatewayRouter(registry);
        router.mapPathToService("/api/users/.*", "UserService");
        router.mapPathToService("/api/orders/.*", "OrderService");
        
        // Route requests
        router.route(new HttpRequest("R1", "GET", "/api/users/123", null, null, null));
        router.route(new HttpRequest("R2", "GET", "/api/orders/456", null, null, null));
        
        // Deregister a service
        registry.deregisterService("user-v1-instance1");
        registry.printRegistry();
        
        // Route again (should use v2 now)
        router.route(new HttpRequest("R3", "GET", "/api/users/789", null, null, null));
    }
}
