package architectural;

import java.util.*;

/**
 * Event-Driven Architecture Pattern
 * Components communicate through events in a loosely coupled manner.
 */
public class EventDrivenPattern {
    
    // Event interface
    interface Event {
        String getEventType();
        long getTimestamp();
    }
    
    // Base Event class
    static abstract class BaseEvent implements Event {
        private final String eventType;
        private final long timestamp;
        
        public BaseEvent(String eventType) {
            this.eventType = eventType;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public String getEventType() {
            return eventType;
        }
        
        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    // Concrete Events
    static class UserRegisteredEvent extends BaseEvent {
        private final String userId;
        private final String email;
        
        public UserRegisteredEvent(String userId, String email) {
            super("USER_REGISTERED");
            this.userId = userId;
            this.email = email;
        }
        
        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        
        @Override
        public String toString() {
            return "UserRegisteredEvent{userId='" + userId + "', email='" + email + "'}";
        }
    }
    
    static class OrderPlacedEvent extends BaseEvent {
        private final String orderId;
        private final String userId;
        private final double amount;
        
        public OrderPlacedEvent(String orderId, String userId, double amount) {
            super("ORDER_PLACED");
            this.orderId = orderId;
            this.userId = userId;
            this.amount = amount;
        }
        
        public String getOrderId() { return orderId; }
        public String getUserId() { return userId; }
        public double getAmount() { return amount; }
        
        @Override
        public String toString() {
            return "OrderPlacedEvent{orderId='" + orderId + "', amount=$" + amount + "}";
        }
    }
    
    static class PaymentProcessedEvent extends BaseEvent {
        private final String paymentId;
        private final String orderId;
        private final boolean success;
        
        public PaymentProcessedEvent(String paymentId, String orderId, boolean success) {
            super("PAYMENT_PROCESSED");
            this.paymentId = paymentId;
            this.orderId = orderId;
            this.success = success;
        }
        
        public String getPaymentId() { return paymentId; }
        public String getOrderId() { return orderId; }
        public boolean isSuccess() { return success; }
        
        @Override
        public String toString() {
            return "PaymentProcessedEvent{paymentId='" + paymentId + 
                   "', success=" + success + "}";
        }
    }
    
    // Event Handler interface
    interface EventHandler<T extends Event> {
        void handle(T event);
        String getEventType();
    }
    
    // Event Bus (Mediator for events)
    static class EventBus {
        private final Map<String, List<EventHandler<? extends Event>>> handlers = new HashMap<>();
        private final List<Event> eventHistory = new ArrayList<>();
        
        public <T extends Event> void subscribe(String eventType, EventHandler<T> handler) {
            handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
            System.out.println("📝 Subscribed " + handler.getClass().getSimpleName() + 
                             " to " + eventType);
        }
        
        public void publish(Event event) {
            System.out.println("\n📢 Publishing: " + event);
            eventHistory.add(event);
            
            List<EventHandler<? extends Event>> eventHandlers = 
                handlers.getOrDefault(event.getEventType(), Collections.emptyList());
            
            for (EventHandler handler : eventHandlers) {
                try {
                    handler.handle(event);
                } catch (Exception e) {
                    System.err.println("❌ Error handling event: " + e.getMessage());
                }
            }
        }
        
        public List<Event> getEventHistory() {
            return new ArrayList<>(eventHistory);
        }
        
        public void printEventHistory() {
            System.out.println("\n📜 Event History:");
            for (Event event : eventHistory) {
                System.out.println("  " + new Date(event.getTimestamp()) + " - " + event);
            }
        }
    }
    
    // Event Handlers (Subscribers)
    static class EmailNotificationHandler implements EventHandler<UserRegisteredEvent> {
        @Override
        public void handle(UserRegisteredEvent event) {
            System.out.println("  📧 EmailNotificationHandler: Sending welcome email to " + 
                             event.getEmail());
        }
        
        @Override
        public String getEventType() {
            return "USER_REGISTERED";
        }
    }
    
    static class UserAnalyticsHandler implements EventHandler<UserRegisteredEvent> {
        @Override
        public void handle(UserRegisteredEvent event) {
            System.out.println("  📊 UserAnalyticsHandler: Recording user registration for " + 
                             event.getUserId());
        }
        
        @Override
        public String getEventType() {
            return "USER_REGISTERED";
        }
    }
    
    static class InventoryHandler implements EventHandler<OrderPlacedEvent> {
        @Override
        public void handle(OrderPlacedEvent event) {
            System.out.println("  📦 InventoryHandler: Reserving items for order " + 
                             event.getOrderId());
        }
        
        @Override
        public String getEventType() {
            return "ORDER_PLACED";
        }
    }
    
    static class ShippingHandler implements EventHandler<PaymentProcessedEvent> {
        @Override
        public void handle(PaymentProcessedEvent event) {
            if (event.isSuccess()) {
                System.out.println("  🚚 ShippingHandler: Initiating shipment for order " + 
                                 event.getOrderId());
            } else {
                System.out.println("  ⚠️  ShippingHandler: Payment failed, not shipping order " + 
                                 event.getOrderId());
            }
        }
        
        @Override
        public String getEventType() {
            return "PAYMENT_PROCESSED";
        }
    }
    
    static class NotificationHandler implements EventHandler<PaymentProcessedEvent> {
        @Override
        public void handle(PaymentProcessedEvent event) {
            if (event.isSuccess()) {
                System.out.println("  📧 NotificationHandler: Sending payment confirmation");
            } else {
                System.out.println("  📧 NotificationHandler: Sending payment failure notice");
            }
        }
        
        @Override
        public String getEventType() {
            return "PAYMENT_PROCESSED";
        }
    }
    
    // Service that publishes events
    static class UserService {
        private final EventBus eventBus;
        
        public UserService(EventBus eventBus) {
            this.eventBus = eventBus;
        }
        
        public void registerUser(String userId, String email) {
            System.out.println("\n🔹 UserService: Registering user " + userId);
            // Business logic here...
            
            // Publish event
            eventBus.publish(new UserRegisteredEvent(userId, email));
        }
    }
    
    static class OrderService {
        private final EventBus eventBus;
        
        public OrderService(EventBus eventBus) {
            this.eventBus = eventBus;
        }
        
        public void placeOrder(String orderId, String userId, double amount) {
            System.out.println("\n🔹 OrderService: Placing order " + orderId);
            // Business logic here...
            
            // Publish event
            eventBus.publish(new OrderPlacedEvent(orderId, userId, amount));
        }
    }
    
    static class PaymentService {
        private final EventBus eventBus;
        
        public PaymentService(EventBus eventBus) {
            this.eventBus = eventBus;
        }
        
        public void processPayment(String paymentId, String orderId, boolean success) {
            System.out.println("\n🔹 PaymentService: Processing payment " + paymentId);
            // Business logic here...
            
            // Publish event
            eventBus.publish(new PaymentProcessedEvent(paymentId, orderId, success));
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Event-Driven Architecture Demo ===\n");
        
        // Setup Event Bus
        EventBus eventBus = new EventBus();
        
        // Register event handlers (subscribers)
        System.out.println("🔧 Setting up event handlers:");
        eventBus.subscribe("USER_REGISTERED", new EmailNotificationHandler());
        eventBus.subscribe("USER_REGISTERED", new UserAnalyticsHandler());
        eventBus.subscribe("ORDER_PLACED", new InventoryHandler());
        eventBus.subscribe("PAYMENT_PROCESSED", new ShippingHandler());
        eventBus.subscribe("PAYMENT_PROCESSED", new NotificationHandler());
        
        System.out.println("\n" + "=".repeat(50));
        
        // Create services
        UserService userService = new UserService(eventBus);
        OrderService orderService = new OrderService(eventBus);
        PaymentService paymentService = new PaymentService(eventBus);
        
        // Scenario 1: User Registration
        System.out.println("\n1. User Registration Flow:");
        userService.registerUser("U123", "alice@example.com");
        
        System.out.println("\n" + "=".repeat(50));
        
        // Scenario 2: Order Placement
        System.out.println("\n2. Order Placement Flow:");
        orderService.placeOrder("ORD-456", "U123", 299.99);
        
        System.out.println("\n" + "=".repeat(50));
        
        // Scenario 3: Successful Payment
        System.out.println("\n3. Successful Payment Flow:");
        paymentService.processPayment("PAY-789", "ORD-456", true);
        
        System.out.println("\n" + "=".repeat(50));
        
        // Scenario 4: Failed Payment
        System.out.println("\n4. Failed Payment Flow:");
        paymentService.processPayment("PAY-790", "ORD-457", false);
        
        // Show event history
        eventBus.printEventHistory();
        
        System.out.println("\n--- Event-Driven Architecture Components ---");
        System.out.println("📢 Event Publishers:");
        System.out.println("   - UserService, OrderService, PaymentService");
        System.out.println("   - Generate events when actions occur");
        System.out.println();
        System.out.println("📡 Event Bus:");
        System.out.println("   - Routes events to subscribers");
        System.out.println("   - Decouples publishers from subscribers");
        System.out.println();
        System.out.println("👂 Event Subscribers (Handlers):");
        System.out.println("   - EmailHandler, AnalyticsHandler, etc.");
        System.out.println("   - React to events asynchronously");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Loose coupling between components");
        System.out.println("✓ Easy to add new event handlers");
        System.out.println("✓ Scalable architecture");
        System.out.println("✓ Supports async processing");
        System.out.println("✓ Event sourcing capability");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Microservices communication");
        System.out.println("• Real-time systems");
        System.out.println("• IoT applications");
        System.out.println("• GUI applications");
        System.out.println("• Complex workflow orchestration");
        
        System.out.println("\n--- Real-World Examples ---");
        System.out.println("• Apache Kafka");
        System.out.println("• RabbitMQ / AMQP");
        System.out.println("• AWS EventBridge");
        System.out.println("• Spring Events");
        System.out.println("• Node.js EventEmitter");
    }
}
