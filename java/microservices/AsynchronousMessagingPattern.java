package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Asynchronous Messaging Pattern
 * ================================
 * 
 * Intent:
 * Services communicate through message queues or pub/sub systems
 * asynchronously, enabling loose coupling and better scalability.
 * 
 * Also Known As:
 * - Message Queue Pattern
 * - Event-Driven Messaging
 * - Pub/Sub Pattern
 * 
 * Motivation:
 * - Decouple services (temporal and spatial)
 * - Enable asynchronous processing
 * - Buffer load spikes
 * - Support multiple consumers
 * - Improve reliability with message persistence
 * 
 * Applicability:
 * - Services don't need immediate response
 * - Need to handle variable load
 * - Want loose coupling between services
 * - Support event-driven architecture
 * 
 * Structure:
 * Producer -> Message Broker (Queue/Topic) -> Consumer(s)
 * 
 * Benefits:
 * + Loose coupling
 * + Scalability
 * + Load leveling
 * + Reliability
 * 
 * Drawbacks:
 * - Eventual consistency
 * - Complexity
 * - Message ordering challenges
 * - Duplicate messages possible
 */

// ============================================================================
// MESSAGE DEFINITIONS
// ============================================================================

class OrderPlacedEvent {
    private final String orderId;
    private final String customerId;
    private final double amount;
    private final long timestamp;
    
    public OrderPlacedEvent(String orderId, String customerId, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("OrderPlacedEvent{orderId='%s', customerId='%s', amount=$%.2f}",
                           orderId, customerId, amount);
    }
}

// ============================================================================
// MESSAGE BROKER (In-Memory Simulation)
// ============================================================================

interface MessageHandler<T> {
    void handle(T message);
}

class MessageBrokerSimulator {
    // Queue-based messaging (Point-to-Point)
    private final Map<String, BlockingQueue<Object>> queues = new ConcurrentHashMap<>();
    
    // Topic-based messaging (Publish-Subscribe)
    private final Map<String, List<MessageHandler<Object>>> topics = new ConcurrentHashMap<>();
    
    // Create queue
    public void createQueue(String queueName) {
        queues.putIfAbsent(queueName, new LinkedBlockingQueue<>());
        System.out.println("[Broker] Queue created: " + queueName);
    }
    
    // Send to queue (Point-to-Point)
    public void sendToQueue(String queueName, Object message) {
        BlockingQueue<Object> queue = queues.get(queueName);
        if (queue == null) {
            throw new IllegalArgumentException("Queue not found: " + queueName);
        }
        queue.offer(message);
        System.out.println("[Broker] Message sent to queue '" + queueName + "': " + message);
    }
    
    // Receive from queue (Point-to-Point)
    public Object receiveFromQueue(String queueName, long timeoutMs) throws InterruptedException {
        BlockingQueue<Object> queue = queues.get(queueName);
        if (queue == null) {
            throw new IllegalArgumentException("Queue not found: " + queueName);
        }
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }
    
    // Create topic
    public void createTopic(String topicName) {
        topics.putIfAbsent(topicName, new CopyOnWriteArrayList<>());
        System.out.println("[Broker] Topic created: " + topicName);
    }
    
    // Subscribe to topic (Pub/Sub)
    public void subscribe(String topicName, MessageHandler<Object> handler) {
        List<MessageHandler<Object>> handlers = topics.get(topicName);
        if (handlers == null) {
            throw new IllegalArgumentException("Topic not found: " + topicName);
        }
        handlers.add(handler);
        System.out.println("[Broker] Subscriber added to topic: " + topicName);
    }
    
    // Publish to topic (Pub/Sub)
    public void publish(String topicName, Object message) {
        List<MessageHandler<Object>> handlers = topics.get(topicName);
        if (handlers == null) {
            throw new IllegalArgumentException("Topic not found: " + topicName);
        }
        System.out.println("[Broker] Publishing to topic '" + topicName + "': " + message);
        for (MessageHandler<Object> handler : handlers) {
            // Deliver asynchronously to each subscriber
            CompletableFuture.runAsync(() -> handler.handle(message));
        }
    }
}

// ============================================================================
// PRODUCER SERVICE
// ============================================================================

class OrderServiceProducer {
    private final MessageBrokerSimulator broker;
    
    public OrderServiceProducer(MessageBrokerSimulator broker) {
        this.broker = broker;
    }
    
    public void placeOrder(String orderId, String customerId, double amount) {
        System.out.println("\n[OrderService] Placing order: " + orderId);
        
        // Create event
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, customerId, amount);
        
        // Publish to topic (all interested services will receive)
        broker.publish("orders", event);
        
        System.out.println("[OrderService] Order placed successfully (async)");
    }
}

// ============================================================================
// CONSUMER SERVICES
// ============================================================================

class InventoryServiceConsumer {
    public InventoryServiceConsumer(MessageBrokerSimulator broker) {
        broker.subscribe("orders", message -> {
            if (message instanceof OrderPlacedEvent) {
                handleOrderPlaced((OrderPlacedEvent) message);
            }
        });
    }
    
    private void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("  [InventoryService] Processing order: " + event.getOrderId());
        simulateDelay(50);
        System.out.println("  [InventoryService] Inventory updated for order: " + event.getOrderId());
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class PaymentServiceConsumer {
    public PaymentServiceConsumer(MessageBrokerSimulator broker) {
        broker.subscribe("orders", message -> {
            if (message instanceof OrderPlacedEvent) {
                handleOrderPlaced((OrderPlacedEvent) message);
            }
        });
    }
    
    private void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("  [PaymentService] Processing payment: $" + event.getAmount());
        simulateDelay(100);
        System.out.println("  [PaymentService] Payment processed for order: " + event.getOrderId());
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class NotificationServiceConsumer {
    public NotificationServiceConsumer(MessageBrokerSimulator broker) {
        broker.subscribe("orders", message -> {
            if (message instanceof OrderPlacedEvent) {
                handleOrderPlaced((OrderPlacedEvent) message);
            }
        });
    }
    
    private void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("  [NotificationService] Sending notification to customer: " + event.getCustomerId());
        simulateDelay(30);
        System.out.println("  [NotificationService] Notification sent for order: " + event.getOrderId());
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class AnalyticsServiceConsumer {
    public AnalyticsServiceConsumer(MessageBrokerSimulator broker) {
        broker.subscribe("orders", message -> {
            if (message instanceof OrderPlacedEvent) {
                handleOrderPlaced((OrderPlacedEvent) message);
            }
        });
    }
    
    private void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("  [AnalyticsService] Recording metrics for order: " + event.getOrderId());
        simulateDelay(20);
        System.out.println("  [AnalyticsService] Metrics recorded");
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// ============================================================================
// QUEUE-BASED PROCESSING (Work Queue Pattern)
// ============================================================================

class TaskProducer {
    private final MessageBrokerSimulator broker;
    
    public TaskProducer(MessageBrokerSimulator broker) {
        this.broker = broker;
    }
    
    public void submitTask(String taskId) {
        System.out.println("[TaskProducer] Submitting task: " + taskId);
        broker.sendToQueue("tasks", taskId);
    }
}

class TaskWorker {
    private final String workerId;
    private final MessageBrokerSimulator broker;
    private volatile boolean running = true;
    
    public TaskWorker(String workerId, MessageBrokerSimulator broker) {
        this.workerId = workerId;
        this.broker = broker;
    }
    
    public void start() {
        CompletableFuture.runAsync(() -> {
            System.out.println("[" + workerId + "] Worker started");
            while (running) {
                try {
                    Object task = broker.receiveFromQueue("tasks", 100);
                    if (task != null) {
                        processTask((String) task);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("[" + workerId + "] Worker stopped");
        });
    }
    
    private void processTask(String taskId) {
        System.out.println("  [" + workerId + "] Processing task: " + taskId);
        simulateDelay(50);
        System.out.println("  [" + workerId + "] Task completed: " + taskId);
    }
    
    public void stop() {
        running = false;
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

/**
 * Demonstration of Asynchronous Messaging Pattern
 */
public class AsynchronousMessagingPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Asynchronous Messaging Pattern ===\n");
        
        MessageBrokerSimulator broker = new MessageBrokerSimulator();
        
        System.out.println("--- Scenario 1: Pub/Sub (Event-Driven) ---\n");
        
        // Create topic
        broker.createTopic("orders");
        
        // Register consumers (subscribe before publishing)
        new InventoryServiceConsumer(broker);
        new PaymentServiceConsumer(broker);
        new NotificationServiceConsumer(broker);
        new AnalyticsServiceConsumer(broker);
        
        // Producer publishes events
        OrderServiceProducer orderService = new OrderServiceProducer(broker);
        orderService.placeOrder("ORD-001", "CUST-100", 299.99);
        
        Thread.sleep(200); // Wait for async processing
        
        orderService.placeOrder("ORD-002", "CUST-101", 149.99);
        
        Thread.sleep(200);
        
        System.out.println("\n\n--- Scenario 2: Queue-Based (Work Distribution) ---\n");
        
        // Create queue
        broker.createQueue("tasks");
        
        // Start workers (competing consumers)
        TaskWorker worker1 = new TaskWorker("Worker-1", broker);
        TaskWorker worker2 = new TaskWorker("Worker-2", broker);
        TaskWorker worker3 = new TaskWorker("Worker-3", broker);
        
        worker1.start();
        worker2.start();
        worker3.start();
        
        Thread.sleep(100); // Let workers start
        
        // Submit tasks
        TaskProducer producer = new TaskProducer(broker);
        for (int i = 1; i <= 6; i++) {
            producer.submitTask("TASK-" + i);
        }
        
        Thread.sleep(500); // Wait for processing
        
        worker1.stop();
        worker2.stop();
        worker3.stop();
        
        System.out.println("\n\n=== Key Points ===");
        System.out.println("1. Asynchronous - producer doesn't wait for consumers");
        System.out.println("2. Loose coupling - services don't know about each other");
        System.out.println("3. Pub/Sub - one event, multiple subscribers");
        System.out.println("4. Queue - competing consumers (load distribution)");
        System.out.println("5. Scalability - add more consumers easily");
        
        System.out.println("\n=== Benefits ===");
        System.out.println("+ Temporal decoupling (services don't need to be available simultaneously)");
        System.out.println("+ Load leveling (queue buffers during spikes)");
        System.out.println("+ Scalability (add consumers to handle more load)");
        System.out.println("+ Reliability (messages can be persisted)");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("- Event notifications");
        System.out.println("- Background job processing");
        System.out.println("- Decoupling microservices");
        System.out.println("- Load leveling");
    }
}
