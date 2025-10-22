package microservices;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Transactional Outbox Pattern
 * ==============================
 * 
 * Intent:
 * Reliably publish events to a message broker by storing them in an outbox
 * table as part of the same database transaction that updates business data.
 * 
 * Also Known As:
 * - Outbox Pattern
 * - Transactional Messaging
 * 
 * Motivation:
 * - Solve dual-write problem (database + message broker)
 * - Ensure atomicity between business logic and event publishing
 * - Guarantee events are published exactly once
 * - Prevent data inconsistency
 * 
 * Applicability:
 * - Need to update database and publish events atomically
 * - Cannot afford lost events
 * - Using event-driven architecture
 * - Need guaranteed event delivery
 * 
 * Structure:
 * 1. Service writes data + event to outbox table (same transaction)
 * 2. Outbox publisher reads outbox table and publishes events
 * 3. Published events marked as sent/deleted
 * 
 * Participants:
 * - Business Service: Performs business logic
 * - Outbox Table: Stores unpublished events
 * - Outbox Publisher: Polls outbox and publishes events
 * - Message Broker: Receives published events
 * 
 * Benefits:
 * + Atomicity (business data and events)
 * + Reliability (no lost events)
 * + Exactly-once delivery guarantee
 * + Simple to implement
 * 
 * Drawbacks:
 * - Polling overhead
 * - Eventual consistency
 * - Need to clean up outbox table
 * - Additional infrastructure
 */

// ============================================================================
// OUTBOX ENTITY
// ============================================================================

class OutboxEvent {
    private final String id;
    private final String aggregateType;
    private final String aggregateId;
    private final String eventType;
    private final String payload;
    private final long createdAt;
    private boolean published;
    
    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload) {
        this.id = UUID.randomUUID().toString();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = System.currentTimeMillis();
        this.published = false;
    }
    
    public String getId() { return id; }
    public String getAggregateType() { return aggregateType; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public long getCreatedAt() { return createdAt; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    
    @Override
    public String toString() {
        return String.format("OutboxEvent{id='%s', type='%s', aggregateId='%s', published=%b}",
                           id, eventType, aggregateId, published);
    }
}

// ============================================================================
// SIMULATED DATABASE
// ============================================================================

class DatabaseSimulator {
    private final Map<String, OrderEntity> orders = new ConcurrentHashMap<>();
    private final Map<String, OutboxEvent> outbox = new ConcurrentHashMap<>();
    
    // Transaction simulation
    public void executeInTransaction(Runnable businessLogic) {
        // In real implementation, this would be a database transaction
        synchronized (this) {
            try {
                businessLogic.run();
                System.out.println("[DB] Transaction committed");
            } catch (Exception e) {
                System.out.println("[DB] Transaction rolled back");
                throw e;
            }
        }
    }
    
    // Business data operations
    public void insertOrder(OrderEntity order) {
        orders.put(order.getId(), order);
        System.out.println("[DB] Inserted order: " + order.getId());
    }
    
    public OrderEntity getOrder(String orderId) {
        return orders.get(orderId);
    }
    
    public void updateOrderStatus(String orderId, String status) {
        OrderEntity order = orders.get(orderId);
        if (order != null) {
            orders.put(orderId, new OrderEntity(orderId, order.getCustomerId(), order.getAmount(), status));
            System.out.println("[DB] Updated order status: " + orderId + " -> " + status);
        }
    }
    
    // Outbox operations
    public void insertOutboxEvent(OutboxEvent event) {
        outbox.put(event.getId(), event);
        System.out.println("[DB] Inserted outbox event: " + event);
    }
    
    public List<OutboxEvent> getUnpublishedEvents() {
        List<OutboxEvent> unpublished = new ArrayList<>();
        for (OutboxEvent event : outbox.values()) {
            if (!event.isPublished()) {
                unpublished.add(event);
            }
        }
        return unpublished;
    }
    
    public void markEventAsPublished(String eventId) {
        OutboxEvent event = outbox.get(eventId);
        if (event != null) {
            event.setPublished(true);
        }
    }
    
    public void deletePublishedEvents() {
        outbox.entrySet().removeIf(entry -> entry.getValue().isPublished());
    }
}

class OrderEntity {
    private final String id;
    private final String customerId;
    private final double amount;
    private final String status;
    
    public OrderEntity(String id, String customerId, double amount, String status) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
    }
    
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    
    @Override
    public String toString() {
        return String.format("Order{id='%s', customerId='%s', amount=$%.2f, status='%s'}",
                           id, customerId, amount, status);
    }
}

// ============================================================================
// BUSINESS SERVICE
// ============================================================================

class OrderServiceWithOutbox {
    private final DatabaseSimulator database;
    
    public OrderServiceWithOutbox(DatabaseSimulator database) {
        this.database = database;
    }
    
    // Create order with outbox pattern
    public void createOrder(String orderId, String customerId, double amount) {
        System.out.println("\n[OrderService] Creating order: " + orderId);
        
        database.executeInTransaction(() -> {
            // 1. Insert business data
            OrderEntity order = new OrderEntity(orderId, customerId, amount, "PENDING");
            database.insertOrder(order);
            
            // 2. Insert event into outbox (same transaction!)
            String payload = String.format("{\"orderId\":\"%s\",\"customerId\":\"%s\",\"amount\":%.2f}",
                                         orderId, customerId, amount);
            OutboxEvent event = new OutboxEvent("Order", orderId, "OrderCreated", payload);
            database.insertOutboxEvent(event);
        });
        
        System.out.println("[OrderService] Order created (event in outbox)");
    }
    
    // Update order with outbox pattern
    public void confirmOrder(String orderId) {
        System.out.println("\n[OrderService] Confirming order: " + orderId);
        
        database.executeInTransaction(() -> {
            // 1. Update business data
            database.updateOrderStatus(orderId, "CONFIRMED");
            
            // 2. Insert event into outbox (same transaction!)
            String payload = String.format("{\"orderId\":\"%s\"}", orderId);
            OutboxEvent event = new OutboxEvent("Order", orderId, "OrderConfirmed", payload);
            database.insertOutboxEvent(event);
        });
        
        System.out.println("[OrderService] Order confirmed (event in outbox)");
    }
}

// ============================================================================
// OUTBOX PUBLISHER
// ============================================================================

interface MessagePublisher {
    void publish(String eventType, String payload);
}

class OutboxPublisher {
    private final DatabaseSimulator database;
    private final MessagePublisher messagePublisher;
    private volatile boolean running = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public OutboxPublisher(DatabaseSimulator database, MessagePublisher messagePublisher) {
        this.database = database;
        this.messagePublisher = messagePublisher;
    }
    
    public void start() {
        System.out.println("[OutboxPublisher] Starting...");
        scheduler.scheduleAtFixedRate(this::pollAndPublish, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    private void pollAndPublish() {
        if (!running) return;
        
        // Get unpublished events
        List<OutboxEvent> events = database.getUnpublishedEvents();
        
        if (!events.isEmpty()) {
            System.out.println("\n[OutboxPublisher] Found " + events.size() + " unpublished events");
        }
        
        for (OutboxEvent event : events) {
            try {
                // Publish to message broker
                System.out.println("[OutboxPublisher] Publishing: " + event);
                messagePublisher.publish(event.getEventType(), event.getPayload());
                
                // Mark as published
                database.markEventAsPublished(event.getId());
                System.out.println("[OutboxPublisher] Event published successfully");
                
            } catch (Exception e) {
                System.out.println("[OutboxPublisher] Failed to publish event (will retry): " + e.getMessage());
            }
        }
        
        // Cleanup old published events (optional)
        database.deletePublishedEvents();
    }
    
    public void stop() {
        System.out.println("[OutboxPublisher] Stopping...");
        running = false;
        scheduler.shutdown();
    }
}

// ============================================================================
// MESSAGE BROKER SIMULATION
// ============================================================================

class MessageBrokerMock implements MessagePublisher {
    @Override
    public void publish(String eventType, String payload) {
        System.out.println("  [MessageBroker] Received event - Type: " + eventType);
        System.out.println("  [MessageBroker] Payload: " + payload);
        
        // Simulate publishing to subscribers
        simulateDelay(10);
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

/**
 * Demonstration of Transactional Outbox Pattern
 */
public class TransactionalOutboxPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Transactional Outbox Pattern ===\n");
        
        // Setup
        DatabaseSimulator database = new DatabaseSimulator();
        MessageBrokerMock messageBroker = new MessageBrokerMock();
        OutboxPublisher outboxPublisher = new OutboxPublisher(database, messageBroker);
        OrderServiceWithOutbox orderService = new OrderServiceWithOutbox(database);
        
        // Start outbox publisher (polls every 100ms)
        outboxPublisher.start();
        
        System.out.println("--- Creating Orders ---");
        
        // Create orders (writes to both business table and outbox in same transaction)
        orderService.createOrder("ORD-001", "CUST-100", 299.99);
        Thread.sleep(150); // Wait for publisher to process
        
        orderService.createOrder("ORD-002", "CUST-101", 149.99);
        Thread.sleep(150);
        
        // Confirm order (another transaction with event)
        orderService.confirmOrder("ORD-001");
        Thread.sleep(150);
        
        // Stop publisher
        outboxPublisher.stop();
        Thread.sleep(100);
        
        System.out.println("\n\n=== Key Benefits ===");
        System.out.println("1. Atomicity - business data and event in same transaction");
        System.out.println("2. Reliability - events guaranteed to be published");
        System.out.println("3. No dual-write problem - single database transaction");
        System.out.println("4. Exactly-once guarantee - events published once and only once");
        
        System.out.println("\n=== How It Works ===");
        System.out.println("1. Service writes data + event to outbox (single transaction)");
        System.out.println("2. Outbox publisher polls outbox table");
        System.out.println("3. Publisher publishes events to message broker");
        System.out.println("4. Published events marked as sent/deleted");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("- Event-driven microservices");
        System.out.println("- CQRS with event sourcing");
        System.out.println("- Saga pattern coordination");
        System.out.println("- Audit logging");
        System.out.println("- Data synchronization");
    }
}
