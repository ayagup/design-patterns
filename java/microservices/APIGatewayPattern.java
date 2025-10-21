package microservices;

import java.util.*;

/**
 * API Gateway Pattern
 * Single entry point for all clients, routing requests to appropriate microservices.
 */
public class APIGatewayPattern {
    
    // Request and Response objects
    static class Request {
        private final String path;
        private final String method;
        private final Map<String, String> headers;
        private final String body;
        
        public Request(String path, String method, Map<String, String> headers, String body) {
            this.path = path;
            this.method = method;
            this.headers = headers;
            this.body = body;
        }
        
        public String getPath() { return path; }
        public String getMethod() { return method; }
        public Map<String, String> getHeaders() { return headers; }
        public String getBody() { return body; }
    }
    
    static class Response {
        private final int statusCode;
        private final String body;
        private final Map<String, String> headers;
        
        public Response(int statusCode, String body) {
            this(statusCode, body, new HashMap<>());
        }
        
        public Response(int statusCode, String body, Map<String, String> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }
        
        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
        
        @Override
        public String toString() {
            return "Response{status=" + statusCode + ", body='" + body + "'}";
        }
    }
    
    // Microservices
    interface Microservice {
        Response handle(Request request);
        String getServiceName();
    }
    
    static class UserService implements Microservice {
        @Override
        public Response handle(Request request) {
            System.out.println("  👤 UserService handling: " + request.getMethod() + 
                             " " + request.getPath());
            
            if (request.getPath().equals("/users") && request.getMethod().equals("GET")) {
                return new Response(200, "{\"users\": [\"Alice\", \"Bob\", \"Charlie\"]}");
            } else if (request.getPath().startsWith("/users/")) {
                String userId = request.getPath().substring(7);
                return new Response(200, "{\"id\": \"" + userId + "\", \"name\": \"User " + userId + "\"}");
            }
            
            return new Response(404, "{\"error\": \"Not found\"}");
        }
        
        @Override
        public String getServiceName() {
            return "UserService";
        }
    }
    
    static class OrderService implements Microservice {
        @Override
        public Response handle(Request request) {
            System.out.println("  🛒 OrderService handling: " + request.getMethod() + 
                             " " + request.getPath());
            
            if (request.getPath().equals("/orders") && request.getMethod().equals("GET")) {
                return new Response(200, "{\"orders\": [{\"id\": \"1\", \"total\": 99.99}]}");
            } else if (request.getPath().equals("/orders") && request.getMethod().equals("POST")) {
                return new Response(201, "{\"orderId\": \"12345\", \"status\": \"created\"}");
            }
            
            return new Response(404, "{\"error\": \"Not found\"}");
        }
        
        @Override
        public String getServiceName() {
            return "OrderService";
        }
    }
    
    static class ProductService implements Microservice {
        @Override
        public Response handle(Request request) {
            System.out.println("  📦 ProductService handling: " + request.getMethod() + 
                             " " + request.getPath());
            
            if (request.getPath().equals("/products") && request.getMethod().equals("GET")) {
                return new Response(200, "{\"products\": [{\"id\": \"P1\", \"name\": \"Laptop\"}]}");
            }
            
            return new Response(404, "{\"error\": \"Not found\"}");
        }
        
        @Override
        public String getServiceName() {
            return "ProductService";
        }
    }
    
    // API Gateway
    static class APIGateway {
        private final Map<String, Microservice> routes = new HashMap<>();
        private final List<RequestInterceptor> interceptors = new ArrayList<>();
        
        public void registerService(String pathPrefix, Microservice service) {
            routes.put(pathPrefix, service);
            System.out.println("✅ Registered " + service.getServiceName() + 
                             " at " + pathPrefix);
        }
        
        public void addInterceptor(RequestInterceptor interceptor) {
            interceptors.add(interceptor);
        }
        
        public Response route(Request request) {
            System.out.println("\n🌐 Gateway received: " + request.getMethod() + 
                             " " + request.getPath());
            
            // Run interceptors
            for (RequestInterceptor interceptor : interceptors) {
                if (!interceptor.preHandle(request)) {
                    return new Response(403, "{\"error\": \"Request blocked by interceptor\"}");
                }
            }
            
            // Find matching service
            for (Map.Entry<String, Microservice> entry : routes.entrySet()) {
                if (request.getPath().startsWith(entry.getKey())) {
                    Response response = entry.getValue().handle(request);
                    
                    // Run post-interceptors
                    for (RequestInterceptor interceptor : interceptors) {
                        interceptor.postHandle(request, response);
                    }
                    
                    return response;
                }
            }
            
            return new Response(404, "{\"error\": \"Service not found\"}");
        }
    }
    
    // Request Interceptor (for cross-cutting concerns)
    interface RequestInterceptor {
        boolean preHandle(Request request);
        void postHandle(Request request, Response response);
    }
    
    static class AuthenticationInterceptor implements RequestInterceptor {
        @Override
        public boolean preHandle(Request request) {
            System.out.println("  🔐 AuthenticationInterceptor: Checking authentication");
            String authHeader = request.getHeaders().get("Authorization");
            
            if (authHeader == null || authHeader.isEmpty()) {
                System.out.println("  ❌ No authorization header");
                return false;
            }
            
            System.out.println("  ✅ Authentication passed");
            return true;
        }
        
        @Override
        public void postHandle(Request request, Response response) {
            // Could add security headers here
        }
    }
    
    static class LoggingInterceptor implements RequestInterceptor {
        @Override
        public boolean preHandle(Request request) {
            System.out.println("  📝 LoggingInterceptor: " + request.getMethod() + 
                             " " + request.getPath());
            return true;
        }
        
        @Override
        public void postHandle(Request request, Response response) {
            System.out.println("  📝 Response status: " + response.getStatusCode());
        }
    }
    
    static class RateLimitInterceptor implements RequestInterceptor {
        private final Map<String, Integer> requestCounts = new HashMap<>();
        private final int maxRequests = 5;
        
        @Override
        public boolean preHandle(Request request) {
            String clientId = request.getHeaders().getOrDefault("Client-ID", "anonymous");
            int count = requestCounts.getOrDefault(clientId, 0);
            
            if (count >= maxRequests) {
                System.out.println("  ⛔ RateLimitInterceptor: Rate limit exceeded for " + clientId);
                return false;
            }
            
            requestCounts.put(clientId, count + 1);
            System.out.println("  ✅ RateLimitInterceptor: Request " + (count + 1) + 
                             "/" + maxRequests);
            return true;
        }
        
        @Override
        public void postHandle(Request request, Response response) {}
    }
    
    public static void main(String[] args) {
        System.out.println("=== API Gateway Pattern Demo ===\n");
        
        // Setup microservices
        System.out.println("🔧 Setting up API Gateway and Microservices:");
        APIGateway gateway = new APIGateway();
        
        gateway.registerService("/users", new UserService());
        gateway.registerService("/orders", new OrderService());
        gateway.registerService("/products", new ProductService());
        
        // Add interceptors
        System.out.println("\n🔧 Adding interceptors:");
        gateway.addInterceptor(new LoggingInterceptor());
        gateway.addInterceptor(new AuthenticationInterceptor());
        gateway.addInterceptor(new RateLimitInterceptor());
        
        System.out.println("\n" + "=".repeat(50));
        
        // 1. Successful requests with auth
        System.out.println("\n1. Authenticated Requests:");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");
        headers.put("Client-ID", "client1");
        
        Request req1 = new Request("/users", "GET", headers, "");
        Response resp1 = gateway.route(req1);
        System.out.println("📤 " + resp1);
        
        Request req2 = new Request("/users/42", "GET", headers, "");
        Response resp2 = gateway.route(req2);
        System.out.println("📤 " + resp2);
        
        Request req3 = new Request("/orders", "GET", headers, "");
        Response resp3 = gateway.route(req3);
        System.out.println("📤 " + resp3);
        
        Request req4 = new Request("/products", "GET", headers, "");
        Response resp4 = gateway.route(req4);
        System.out.println("📤 " + resp4);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. Request without authentication
        System.out.println("\n2. Request Without Authentication:");
        Map<String, String> noAuthHeaders = new HashMap<>();
        Request req5 = new Request("/users", "GET", noAuthHeaders, "");
        Response resp5 = gateway.route(req5);
        System.out.println("📤 " + resp5);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 3. Rate limiting
        System.out.println("\n3. Rate Limiting (max 5 requests):");
        for (int i = 1; i <= 7; i++) {
            System.out.println("\nRequest " + i + ":");
            Request req = new Request("/users", "GET", headers, "");
            Response resp = gateway.route(req);
            System.out.println("📤 " + resp);
        }
        
        System.out.println("\n--- API Gateway Responsibilities ---");
        System.out.println("🔀 Routing: Directs requests to appropriate services");
        System.out.println("🔐 Authentication: Verifies user identity");
        System.out.println("🛡️  Authorization: Checks permissions");
        System.out.println("⚡ Rate Limiting: Prevents abuse");
        System.out.println("📝 Logging: Centralized request logging");
        System.out.println("🔄 Load Balancing: Distributes traffic");
        System.out.println("💾 Caching: Reduces backend load");
        System.out.println("🔁 Request/Response transformation");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Single entry point for clients");
        System.out.println("✓ Simplified client code");
        System.out.println("✓ Cross-cutting concerns in one place");
        System.out.println("✓ Protocol translation");
        System.out.println("✓ Service discovery abstraction");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Microservices architecture");
        System.out.println("• Mobile/web backends");
        System.out.println("• Legacy system integration");
        System.out.println("• Multi-channel applications");
        
        System.out.println("\n--- Popular Implementations ---");
        System.out.println("• Kong");
        System.out.println("• Netflix Zuul");
        System.out.println("• Spring Cloud Gateway");
        System.out.println("• Amazon API Gateway");
        System.out.println("• Azure API Management");
    }
}
