package cloud;

import java.util.concurrent.*;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Competing Consumers Pattern
 * 
 * Intent: Enable multiple concurrent consumers to process messages from the same 
 * message queue in parallel, improving scalability and throughput.
 * 
 * Also Known As: Parallel Processing, Load Distribution
 * 
 * Motivation:
 * When processing messages from a queue, a single consumer may become a bottleneck.
 * The Competing Consumers pattern allows multiple consumers to process messages
 * in parallel, distributing the load and improving throughput.
 * 
 * Applicability:
 * - Messages in queue can be processed independently
 * - Need to scale message processing capacity
 * - Order of processing is not critical (or can be managed)
 * - Need to improve throughput and reduce latency
 * - Processing is time-consuming and can benefit from parallelization
 * 
 * Benefits:
 * - Improved throughput and scalability
 * - Better resource utilization
 * - Reduced processing latency
 * - Fault tolerance (if one consumer fails, others continue)
 * - Dynamic scaling (add/remove consumers as needed)
 * 
 * Implementation Considerations:
 * - Message idempotency (messages may be processed more than once)
 * - Poison message handling
 * - Consumer health monitoring
 * - Load balancing across consumers
 * - Message ordering (if required)
 */

// Message interface
interface Message {
    String getId();
    String getContent();
    int getPriority();
    LocalDateTime getTimestamp();
}

// Generic message implementation
class GenericMessage implements Message {
    private final String id;
    private final String content;
    private final int priority;
    private final LocalDateTime timestamp;
    
    public GenericMessage(String id, String content, int priority) {
        this.id = id;
        this.content = content;
        this.priority = priority;
        this.timestamp = LocalDateTime.now();
    }
    
    @Override
    public String getId() { return id; }
    
    @Override
    public String getContent() { return content; }
    
    @Override
    public int getPriority() { return priority; }
    
    @Override
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("Message[id=%s, content=%s, priority=%d]", 
            id, content, priority);
    }
}

// Message consumer interface
interface MessageConsumer {
    void consume(Message message) throws Exception;
    String getConsumerId();
    boolean isHealthy();
}

// Abstract base consumer with common functionality
abstract class BaseConsumer implements MessageConsumer {
    protected final String consumerId;
    protected final AtomicInteger processedCount;
    protected volatile boolean healthy;
    
    public BaseConsumer(String consumerId) {
        this.consumerId = consumerId;
        this.processedCount = new AtomicInteger(0);
        this.healthy = true;
    }
    
    @Override
    public String getConsumerId() {
        return consumerId;
    }
    
    @Override
    public boolean isHealthy() {
        return healthy;
    }
    
    public int getProcessedCount() {
        return processedCount.get();
    }
    
    protected void logProcessing(Message message) {
        System.out.printf("[%s] Processing: %s%n", consumerId, message);
    }
    
    protected void logCompleted(Message message) {
        System.out.printf("[%s] Completed: %s (Total: %d)%n", 
            consumerId, message.getId(), processedCount.incrementAndGet());
    }
}

// Example 1: Order Processing Consumer
class OrderConsumer extends BaseConsumer {
    private final Random random = new Random();
    
    public OrderConsumer(String consumerId) {
        super(consumerId);
    }
    
    @Override
    public void consume(Message message) throws Exception {
        logProcessing(message);
        
        try {
            // Simulate order validation
            Thread.sleep(100 + random.nextInt(200));
            validateOrder(message);
            
            // Simulate payment processing
            Thread.sleep(100 + random.nextInt(200));
            processPayment(message);
            
            // Simulate inventory update
            Thread.sleep(50 + random.nextInt(100));
            updateInventory(message);
            
            logCompleted(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Order processing interrupted", e);
        }
    }
    
    private void validateOrder(Message message) {
        System.out.printf("  [%s] Validating order: %s%n", consumerId, message.getId());
    }
    
    private void processPayment(Message message) {
        System.out.printf("  [%s] Processing payment: %s%n", consumerId, message.getId());
    }
    
    private void updateInventory(Message message) {
        System.out.printf("  [%s] Updating inventory: %s%n", consumerId, message.getId());
    }
}

// Example 2: Email Notification Consumer
class EmailConsumer extends BaseConsumer {
    private final Random random = new Random();
    
    public EmailConsumer(String consumerId) {
        super(consumerId);
    }
    
    @Override
    public void consume(Message message) throws Exception {
        logProcessing(message);
        
        try {
            // Simulate email composition
            Thread.sleep(50 + random.nextInt(100));
            composeEmail(message);
            
            // Simulate sending email
            Thread.sleep(100 + random.nextInt(150));
            sendEmail(message);
            
            logCompleted(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Email processing interrupted", e);
        }
    }
    
    private void composeEmail(Message message) {
        System.out.printf("  [%s] Composing email for: %s%n", consumerId, message.getId());
    }
    
    private void sendEmail(Message message) {
        System.out.printf("  [%s] Sending email: %s%n", consumerId, message.getId());
    }
}

// Example 3: Image Processing Consumer
class ImageConsumer extends BaseConsumer {
    private final Random random = new Random();
    
    public ImageConsumer(String consumerId) {
        super(consumerId);
    }
    
    @Override
    public void consume(Message message) throws Exception {
        logProcessing(message);
        
        try {
            // Simulate image processing tasks
            Thread.sleep(200 + random.nextInt(300));
            resizeImage(message);
            
            Thread.sleep(150 + random.nextInt(200));
            applyFilters(message);
            
            Thread.sleep(100 + random.nextInt(150));
            generateThumbnail(message);
            
            logCompleted(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Image processing interrupted", e);
        }
    }
    
    private void resizeImage(Message message) {
        System.out.printf("  [%s] Resizing image: %s%n", consumerId, message.getId());
    }
    
    private void applyFilters(Message message) {
        System.out.printf("  [%s] Applying filters: %s%n", consumerId, message.getId());
    }
    
    private void generateThumbnail(Message message) {
        System.out.printf("  [%s] Generating thumbnail: %s%n", consumerId, message.getId());
    }
}

// Message queue with competing consumers support
class MessageQueue {
    private final BlockingQueue<Message> queue;
    private final List<ConsumerWorker> workers;
    private final ExecutorService executorService;
    private final AtomicInteger messageCount;
    private volatile boolean running;
    
    public MessageQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.workers = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.messageCount = new AtomicInteger(0);
        this.running = false;
    }
    
    public void addConsumer(MessageConsumer consumer) {
        ConsumerWorker worker = new ConsumerWorker(consumer, queue);
        workers.add(worker);
        if (running) {
            executorService.submit(worker);
        }
    }
    
    public void start() {
        running = true;
        for (ConsumerWorker worker : workers) {
            executorService.submit(worker);
        }
        System.out.println("MessageQueue started with " + workers.size() + " consumers");
    }
    
    public void stop() {
        running = false;
        for (ConsumerWorker worker : workers) {
            worker.stop();
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("MessageQueue stopped");
    }
    
    public boolean enqueue(Message message) throws InterruptedException {
        boolean added = queue.offer(message, 1, TimeUnit.SECONDS);
        if (added) {
            messageCount.incrementAndGet();
            System.out.printf("Enqueued: %s (Queue size: %d)%n", 
                message.getId(), queue.size());
        }
        return added;
    }
    
    public int getQueueSize() {
        return queue.size();
    }
    
    public int getTotalMessagesProcessed() {
        return messageCount.get() - queue.size();
    }
    
    public void printStatistics() {
        System.out.println("\n=== Queue Statistics ===");
        System.out.println("Total messages enqueued: " + messageCount.get());
        System.out.println("Messages in queue: " + queue.size());
        System.out.println("Messages processed: " + getTotalMessagesProcessed());
        
        System.out.println("\n=== Consumer Statistics ===");
        for (ConsumerWorker worker : workers) {
            MessageConsumer consumer = worker.getConsumer();
            if (consumer instanceof BaseConsumer) {
                BaseConsumer baseConsumer = (BaseConsumer) consumer;
                System.out.printf("%s: Processed=%d, Healthy=%s%n",
                    consumer.getConsumerId(),
                    baseConsumer.getProcessedCount(),
                    consumer.isHealthy());
            }
        }
    }
}

// Worker thread that continuously processes messages
class ConsumerWorker implements Runnable {
    private final MessageConsumer consumer;
    private final BlockingQueue<Message> queue;
    private volatile boolean running;
    private final AtomicInteger failureCount;
    private static final int MAX_FAILURES = 3;
    
    public ConsumerWorker(MessageConsumer consumer, BlockingQueue<Message> queue) {
        this.consumer = consumer;
        this.queue = queue;
        this.running = true;
        this.failureCount = new AtomicInteger(0);
    }
    
    @Override
    public void run() {
        System.out.printf("Consumer %s started%n", consumer.getConsumerId());
        
        while (running && consumer.isHealthy()) {
            try {
                Message message = queue.poll(500, TimeUnit.MILLISECONDS);
                if (message != null) {
                    processMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.printf("Consumer %s stopped%n", consumer.getConsumerId());
    }
    
    private void processMessage(Message message) {
        try {
            consumer.consume(message);
            failureCount.set(0); // Reset failure count on success
        } catch (Exception e) {
            int failures = failureCount.incrementAndGet();
            System.err.printf("ERROR [%s] Failed to process %s (Failure count: %d): %s%n",
                consumer.getConsumerId(), message.getId(), failures, e.getMessage());
            
            if (failures >= MAX_FAILURES) {
                System.err.printf("ERROR [%s] Max failures reached, marking unhealthy%n",
                    consumer.getConsumerId());
                if (consumer instanceof BaseConsumer) {
                    ((BaseConsumer) consumer).healthy = false;
                }
            } else {
                // Re-queue message for retry (in production, use dead letter queue)
                try {
                    queue.offer(message, 100, TimeUnit.MILLISECONDS);
                    System.out.printf("Re-queued message %s for retry%n", message.getId());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    public void stop() {
        running = false;
    }
    
    public MessageConsumer getConsumer() {
        return consumer;
    }
}

// Example 4: Priority-based message processing
class PriorityMessageQueue {
    private final PriorityBlockingQueue<Message> queue;
    private final List<ConsumerWorker> workers;
    private final ExecutorService executorService;
    
    public PriorityMessageQueue() {
        // Higher priority messages processed first
        this.queue = new PriorityBlockingQueue<>(100, 
            (m1, m2) -> Integer.compare(m2.getPriority(), m1.getPriority()));
        this.workers = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
    }
    
    public void addConsumer(MessageConsumer consumer) {
        ConsumerWorker worker = new ConsumerWorker(consumer, queue);
        workers.add(worker);
        executorService.submit(worker);
    }
    
    public void enqueue(Message message) {
        queue.offer(message);
        System.out.printf("Enqueued priority message: %s (Priority: %d)%n", 
            message.getId(), message.getPriority());
    }
    
    public void shutdown() {
        workers.forEach(ConsumerWorker::stop);
        executorService.shutdown();
    }
}

// Example 5: Load balancer with consumer health monitoring
class LoadBalancedMessageQueue {
    private final BlockingQueue<Message> queue;
    private final List<ConsumerWorker> workers;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService executorService;
    
    public LoadBalancedMessageQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.workers = new CopyOnWriteArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.executorService = Executors.newCachedThreadPool();
    }
    
    public void addConsumer(MessageConsumer consumer) {
        ConsumerWorker worker = new ConsumerWorker(consumer, queue);
        workers.add(worker);
        executorService.submit(worker);
    }
    
    public void startHealthMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n--- Health Check ---");
            int healthyCount = 0;
            for (ConsumerWorker worker : workers) {
                MessageConsumer consumer = worker.getConsumer();
                boolean healthy = consumer.isHealthy();
                System.out.printf("%s: %s%n", 
                    consumer.getConsumerId(), 
                    healthy ? "HEALTHY" : "UNHEALTHY");
                if (healthy) healthyCount++;
            }
            System.out.printf("Healthy consumers: %d/%d%n", healthyCount, workers.size());
            
            // In production, could auto-scale here
            if (healthyCount < workers.size() / 2) {
                System.out.println("WARNING: Less than 50% consumers healthy!");
            }
        }, 2, 2, TimeUnit.SECONDS);
    }
    
    public void enqueue(Message message) throws InterruptedException {
        queue.put(message);
    }
    
    public void shutdown() {
        scheduler.shutdown();
        workers.forEach(ConsumerWorker::stop);
        executorService.shutdown();
    }
}

// Demonstration
public class CompetingConsumersPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Competing Consumers Pattern Demo ===\n");
        
        // Demo 1: Order processing with competing consumers
        System.out.println("--- Demo 1: Order Processing ---");
        demoOrderProcessing();
        
        Thread.sleep(2000);
        
        // Demo 2: Email notifications with multiple consumers
        System.out.println("\n--- Demo 2: Email Notifications ---");
        demoEmailNotifications();
        
        Thread.sleep(2000);
        
        // Demo 3: Image processing with competing consumers
        System.out.println("\n--- Demo 3: Image Processing ---");
        demoImageProcessing();
        
        Thread.sleep(2000);
        
        // Demo 4: Priority-based message processing
        System.out.println("\n--- Demo 4: Priority Message Processing ---");
        demoPriorityProcessing();
    }
    
    private static void demoOrderProcessing() throws InterruptedException {
        MessageQueue queue = new MessageQueue(50);
        
        // Add three competing order consumers
        queue.addConsumer(new OrderConsumer("OrderConsumer-1"));
        queue.addConsumer(new OrderConsumer("OrderConsumer-2"));
        queue.addConsumer(new OrderConsumer("OrderConsumer-3"));
        
        queue.start();
        
        // Enqueue orders
        for (int i = 1; i <= 10; i++) {
            Message order = new GenericMessage(
                "ORDER-" + i,
                "Order for customer " + i,
                1
            );
            queue.enqueue(order);
            Thread.sleep(50); // Simulate orders arriving over time
        }
        
        // Wait for processing
        Thread.sleep(3000);
        queue.stop();
        queue.printStatistics();
    }
    
    private static void demoEmailNotifications() throws InterruptedException {
        MessageQueue queue = new MessageQueue(50);
        
        // Add four competing email consumers
        queue.addConsumer(new EmailConsumer("EmailConsumer-1"));
        queue.addConsumer(new EmailConsumer("EmailConsumer-2"));
        queue.addConsumer(new EmailConsumer("EmailConsumer-3"));
        queue.addConsumer(new EmailConsumer("EmailConsumer-4"));
        
        queue.start();
        
        // Enqueue email notifications
        for (int i = 1; i <= 15; i++) {
            Message email = new GenericMessage(
                "EMAIL-" + i,
                "Welcome email for user " + i,
                1
            );
            queue.enqueue(email);
            Thread.sleep(30);
        }
        
        Thread.sleep(2500);
        queue.stop();
        queue.printStatistics();
    }
    
    private static void demoImageProcessing() throws InterruptedException {
        MessageQueue queue = new MessageQueue(30);
        
        // Add two competing image consumers (fewer due to resource intensity)
        queue.addConsumer(new ImageConsumer("ImageConsumer-1"));
        queue.addConsumer(new ImageConsumer("ImageConsumer-2"));
        
        queue.start();
        
        // Enqueue image processing tasks
        for (int i = 1; i <= 8; i++) {
            Message image = new GenericMessage(
                "IMG-" + i,
                "Process image " + i + ".jpg",
                1
            );
            queue.enqueue(image);
            Thread.sleep(100);
        }
        
        Thread.sleep(4000);
        queue.stop();
        queue.printStatistics();
    }
    
    private static void demoPriorityProcessing() throws InterruptedException {
        PriorityMessageQueue queue = new PriorityMessageQueue();
        
        // Add consumers
        queue.addConsumer(new OrderConsumer("PriorityConsumer-1"));
        queue.addConsumer(new OrderConsumer("PriorityConsumer-2"));
        
        // Enqueue messages with different priorities
        queue.enqueue(new GenericMessage("MSG-1", "Low priority", 1));
        queue.enqueue(new GenericMessage("MSG-2", "High priority", 10));
        queue.enqueue(new GenericMessage("MSG-3", "Medium priority", 5));
        queue.enqueue(new GenericMessage("MSG-4", "Critical", 20));
        queue.enqueue(new GenericMessage("MSG-5", "Low priority", 2));
        
        Thread.sleep(3000);
        queue.shutdown();
        System.out.println("Priority processing completed");
    }
}
