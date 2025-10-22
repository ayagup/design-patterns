package architectural;

import java.util.*;
import java.util.concurrent.*;

/**
 * Broker Architecture Pattern
 * ============================
 * 
 * Intent:
 * Decouples clients from servers by introducing an intermediary (broker) that
 * handles communication, routing, transformation, and coordination between
 * distributed components.
 * 
 * Also Known As:
 * - Message Broker Pattern
 * - Mediator for Distributed Systems
 * 
 * Motivation:
 * - Hide complexity of distributed communication
 * - Enable location transparency
 * - Support dynamic binding of services
 * - Provide transformation and routing capabilities
 * 
 * Applicability:
 * - Distributed systems with many interdependent services
 * - Systems requiring message routing and transformation
 * - Applications needing service discovery
 * - Enterprise application integration
 * 
 * Structure:
 * Client -> Broker -> Server
 * Broker handles: discovery, routing, marshalling, error handling
 * 
 * Participants:
 * - Client: Requests services
 * - Server: Provides services
 * - Broker: Intermediary for communication
 * - Client Proxy: Represents broker on client side
 * - Server Proxy: Represents broker on server side
 * 
 * Examples:
 * - CORBA, RMI, Web Services, Message Queues (RabbitMQ, Kafka)
 */

// ============================================================================
// MESSAGE DEFINITIONS
// ============================================================================

class Message {
    private final String id;
    private final String from;
    private final String to;
    private final String operation;
    private final Map<String, Object> payload;
    private final long timestamp;
    
    public Message(String from, String to, String operation, Map<String, Object> payload) {
        this.id = "MSG-" + System.currentTimeMillis() + "-" + Math.random();
        this.from = from;
        this.to = to;
        this.operation = operation;
        this.payload = new HashMap<>(payload);
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getOperation() { return operation; }
    public Map<String, Object> getPayload() { return new HashMap<>(payload); }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("Message{id='%s', from='%s', to='%s', operation='%s'}",
                           id, from, to, operation);
    }
}

class Response {
    private final String messageId;
    private final boolean success;
    private final Object result;
    private final String error;
    
    public Response(String messageId, boolean success, Object result, String error) {
        this.messageId = messageId;
        this.success = success;
        this.result = result;
        this.error = error;
    }
    
    public String getMessageId() { return messageId; }
    public boolean isSuccess() { return success; }
    public Object getResult() { return result; }
    public String getError() { return error; }
    
    public static Response success(String messageId, Object result) {
        return new Response(messageId, true, result, null);
    }
    
    public static Response error(String messageId, String error) {
        return new Response(messageId, false, null, error);
    }
    
    @Override
    public String toString() {
        return String.format("Response{messageId='%s', success=%b, result=%s, error='%s'}",
                           messageId, success, result, error);
    }
}

// ============================================================================
// BROKER
// ============================================================================

class MessageBroker {
    private final Map<String, ServiceHandler> services = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Response>> pendingRequests = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private long messageCount = 0;
    
    public void registerService(String serviceName, ServiceHandler handler) {
        services.put(serviceName, handler);
        System.out.println("[Broker] Service registered: " + serviceName);
    }
    
    public void unregisterService(String serviceName) {
        services.remove(serviceName);
        System.out.println("[Broker] Service unregistered: " + serviceName);
    }
    
    public CompletableFuture<Response> sendRequest(Message message) {
        messageCount++;
        System.out.println("[Broker] Routing message #" + messageCount + ": " + message);
        
        CompletableFuture<Response> future = new CompletableFuture<>();
        pendingRequests.put(message.getId(), future);
        
        // Route message asynchronously
        executorService.submit(() -> {
            try {
                Response response = routeMessage(message);
                future.complete(response);
                pendingRequests.remove(message.getId());
            } catch (Exception e) {
                Response errorResponse = Response.error(message.getId(), e.getMessage());
                future.complete(errorResponse);
                pendingRequests.remove(message.getId());
            }
        });
        
        return future;
    }
    
    private Response routeMessage(Message message) {
        String serviceName = message.getTo();
        ServiceHandler handler = services.get(serviceName);
        
        if (handler == null) {
            System.out.println("[Broker] Service not found: " + serviceName);
            return Response.error(message.getId(), "Service not found: " + serviceName);
        }
        
        System.out.println("[Broker] Dispatching to service: " + serviceName);
        
        try {
            Object result = handler.handle(message.getOperation(), message.getPayload());
            System.out.println("[Broker] Service response received from: " + serviceName);
            return Response.success(message.getId(), result);
        } catch (Exception e) {
            System.out.println("[Broker] Service error from " + serviceName + ": " + e.getMessage());
            return Response.error(message.getId(), e.getMessage());
        }
    }
    
    public Set<String> getAvailableServices() {
        return new HashSet<>(services.keySet());
    }
    
    public long getMessageCount() {
        return messageCount;
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}

// ============================================================================
// SERVICE HANDLER INTERFACE
// ============================================================================

interface ServiceHandler {
    Object handle(String operation, Map<String, Object> payload) throws Exception;
}

// ============================================================================
// SERVICE IMPLEMENTATIONS
// ============================================================================

class UserService implements ServiceHandler {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private int userIdCounter = 1000;
    
    public UserService() {
        // Pre-populate with sample data
        users.put("U1000", new User("U1000", "Alice", "alice@example.com"));
        users.put("U1001", new User("U1001", "Bob", "bob@example.com"));
    }
    
    @Override
    public Object handle(String operation, Map<String, Object> payload) throws Exception {
        System.out.println("  [UserService] Handling operation: " + operation);
        
        switch (operation) {
            case "getUser":
                String userId = (String) payload.get("userId");
                User user = users.get(userId);
                if (user == null) throw new Exception("User not found");
                return user;
                
            case "createUser":
                String name = (String) payload.get("name");
                String email = (String) payload.get("email");
                String newUserId = "U" + (userIdCounter++);
                User newUser = new User(newUserId, name, email);
                users.put(newUserId, newUser);
                return newUser;
                
            case "listUsers":
                return new ArrayList<>(users.values());
                
            default:
                throw new Exception("Unknown operation: " + operation);
        }
    }
    
    static class User {
        private final String id;
        private final String name;
        private final String email;
        
        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        @Override
        public String toString() {
            return String.format("User{id='%s', name='%s', email='%s'}", id, name, email);
        }
    }
}

class ProductService implements ServiceHandler {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    
    public ProductService() {
        // Pre-populate
        products.put("P001", new Product("P001", "Laptop", 999.99));
        products.put("P002", new Product("P002", "Mouse", 29.99));
        products.put("P003", new Product("P003", "Keyboard", 79.99));
    }
    
    @Override
    public Object handle(String operation, Map<String, Object> payload) throws Exception {
        System.out.println("  [ProductService] Handling operation: " + operation);
        
        switch (operation) {
            case "getProduct":
                String productId = (String) payload.get("productId");
                Product product = products.get(productId);
                if (product == null) throw new Exception("Product not found");
                return product;
                
            case "listProducts":
                return new ArrayList<>(products.values());
                
            case "updatePrice":
                String id = (String) payload.get("productId");
                Double newPrice = (Double) payload.get("price");
                Product p = products.get(id);
                if (p == null) throw new Exception("Product not found");
                Product updated = new Product(p.id, p.name, newPrice);
                products.put(id, updated);
                return updated;
                
            default:
                throw new Exception("Unknown operation: " + operation);
        }
    }
    
    static class Product {
        private final String id;
        private final String name;
        private final double price;
        
        public Product(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        
        @Override
        public String toString() {
            return String.format("Product{id='%s', name='%s', price=$%.2f}", id, name, price);
        }
    }
}

class OrderService implements ServiceHandler {
    private final Map<String, OrderRecord> orders = new ConcurrentHashMap<>();
    private int orderIdCounter = 5000;
    
    @Override
    public Object handle(String operation, Map<String, Object> payload) throws Exception {
        System.out.println("  [OrderService] Handling operation: " + operation);
        
        switch (operation) {
            case "createOrder":
                String userId = (String) payload.get("userId");
                String productId = (String) payload.get("productId");
                Integer quantity = (Integer) payload.get("quantity");
                
                String orderId = "ORD-" + (orderIdCounter++);
                OrderRecord order = new OrderRecord(orderId, userId, productId, quantity);
                orders.put(orderId, order);
                return order;
                
            case "getOrder":
                String id = (String) payload.get("orderId");
                OrderRecord o = orders.get(id);
                if (o == null) throw new Exception("Order not found");
                return o;
                
            case "listOrders":
                return new ArrayList<>(orders.values());
                
            default:
                throw new Exception("Unknown operation: " + operation);
        }
    }
    
    static class OrderRecord {
        private final String orderId;
        private final String userId;
        private final String productId;
        private final int quantity;
        
        public OrderRecord(String orderId, String userId, String productId, int quantity) {
            this.orderId = orderId;
            this.userId = userId;
            this.productId = productId;
            this.quantity = quantity;
        }
        
        @Override
        public String toString() {
            return String.format("Order{id='%s', userId='%s', productId='%s', quantity=%d}",
                               orderId, userId, productId, quantity);
        }
    }
}

/**
 * Demonstration of Broker Architecture Pattern
 */
public class BrokerPattern {
    public static void main(String[] args) throws Exception {
        demonstrateBroker();
    }
    
    private static void demonstrateBroker() throws Exception {
        System.out.println("=== Broker Architecture: Distributed Services ===\n");
        
        // Create broker
        MessageBroker broker = new MessageBroker();
        
        System.out.println("--- Registering Services with Broker ---\n");
        
        // Register services
        broker.registerService("UserService", new UserService());
        broker.registerService("ProductService", new ProductService());
        broker.registerService("OrderService", new OrderService());
        
        System.out.println("\n--- Available Services ---");
        System.out.println(broker.getAvailableServices());
        
        System.out.println("\n--- Client Requests via Broker ---\n");
        
        // Client 1: Get user
        Map<String, Object> getUserPayload = new HashMap<>();
        getUserPayload.put("userId", "U1000");
        Message msg1 = new Message("Client1", "UserService", "getUser", getUserPayload);
        
        CompletableFuture<Response> future1 = broker.sendRequest(msg1);
        Response response1 = future1.get();
        System.out.println("[Client1] Response: " + response1);
        if (response1.isSuccess()) {
            System.out.println("[Client1] User data: " + response1.getResult());
        }
        
        System.out.println();
        
        // Client 2: Create order
        Map<String, Object> createOrderPayload = new HashMap<>();
        createOrderPayload.put("userId", "U1000");
        createOrderPayload.put("productId", "P001");
        createOrderPayload.put("quantity", 2);
        Message msg2 = new Message("Client2", "OrderService", "createOrder", createOrderPayload);
        
        CompletableFuture<Response> future2 = broker.sendRequest(msg2);
        Response response2 = future2.get();
        System.out.println("[Client2] Response: " + response2);
        if (response2.isSuccess()) {
            System.out.println("[Client2] Order created: " + response2.getResult());
        }
        
        System.out.println();
        
        // Client 3: Update product price
        Map<String, Object> updatePricePayload = new HashMap<>();
        updatePricePayload.put("productId", "P002");
        updatePricePayload.put("price", 24.99);
        Message msg3 = new Message("Client3", "ProductService", "updatePrice", updatePricePayload);
        
        CompletableFuture<Response> future3 = broker.sendRequest(msg3);
        Response response3 = future3.get();
        System.out.println("[Client3] Response: " + response3);
        if (response3.isSuccess()) {
            System.out.println("[Client3] Updated product: " + response3.getResult());
        }
        
        System.out.println();
        
        // Error handling: Request non-existent service
        Map<String, Object> emptyPayload = new HashMap<>();
        Message msg4 = new Message("Client4", "NonExistentService", "someOp", emptyPayload);
        
        CompletableFuture<Response> future4 = broker.sendRequest(msg4);
        Response response4 = future4.get();
        System.out.println("[Client4] Response: " + response4);
        if (!response4.isSuccess()) {
            System.out.println("[Client4] Error: " + response4.getError());
        }
        
        System.out.println("\n--- Statistics ---");
        System.out.println("Total messages routed: " + broker.getMessageCount());
        System.out.println("Active services: " + broker.getAvailableServices().size());
        
        // Cleanup
        broker.shutdown();
        
        System.out.println("\n=== Key Benefits ===");
        System.out.println("1. Location transparency - clients don't know server locations");
        System.out.println("2. Dynamic service discovery - services registered at runtime");
        System.out.println("3. Decoupling - clients and servers independent");
        System.out.println("4. Flexibility - easy to add/remove services");
    }
}
