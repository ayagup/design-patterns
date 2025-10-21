package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Half-Sync/Half-Async Pattern
 * 
 * Intent: Decouple synchronous I/O from asynchronous I/O to simplify
 * concurrent programming without degrading execution efficiency.
 * 
 * Motivation:
 * Many systems must perform both sync and async tasks.
 * Pattern separates these concerns into different layers:
 * - Async layer: Handles I/O events efficiently
 * - Sync layer: Processes requests with simpler blocking code
 * - Queuing layer: Buffers and communicates between layers
 * 
 * Applicability:
 * - Systems with mix of I/O-bound and CPU-bound tasks
 * - Network servers handling async I/O but sync business logic
 * - Event-driven systems with blocking operations
 * - Separating fast I/O from slow processing
 */

/**
 * Request to be processed
 */
class Request {
    private final String id;
    private final String data;
    private final long receivedAt;
    
    public Request(String id, String data) {
        this.id = id;
        this.data = data;
        this.receivedAt = System.currentTimeMillis();
    }
    
    public String getId() { return id; }
    public String getData() { return data; }
    public long getReceivedAt() { return receivedAt; }
}

/**
 * Example 1: Basic Half-Sync/Half-Async
 * 
 * Async layer receives requests, queues them.
 * Sync layer processes from queue with blocking operations.
 */
class BasicHalfSyncHalfAsync {
    private final BlockingQueue<Request> queue;
    private final ExecutorService asyncLayer;
    private final ExecutorService syncLayer;
    private final AtomicLong requestsReceived;
    private final AtomicLong requestsProcessed;
    private volatile boolean running;
    
    public BasicHalfSyncHalfAsync(int asyncThreads, int syncThreads, int queueCapacity) {
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.asyncLayer = Executors.newFixedThreadPool(asyncThreads);
        this.syncLayer = Executors.newFixedThreadPool(syncThreads);
        this.requestsReceived = new AtomicLong(0);
        this.requestsProcessed = new AtomicLong(0);
        this.running = false;
        
        System.out.println("Half-Sync/Half-Async initialized: " + 
                         asyncThreads + " async, " + syncThreads + " sync threads");
    }
    
    public void start() {
        running = true;
        
        // Start sync layer workers
        for (int i = 0; i < 4; i++) {
            syncLayer.submit(() -> {
                while (running) {
                    try {
                        Request request = queue.poll(1, TimeUnit.SECONDS);
                        if (request != null) {
                            processSynchronously(request);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }
    
    public void submitRequestAsync(Request request) {
        asyncLayer.submit(() -> {
            try {
                requestsReceived.incrementAndGet();
                // Async processing (fast, non-blocking)
                System.out.println("  [Async] Received: " + request.getId());
                
                // Queue for sync processing
                queue.put(request);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    private void processSynchronously(Request request) {
        // Sync processing (blocking, business logic)
        try {
            System.out.println("  [Sync] Processing: " + request.getId());
            Thread.sleep(100); // Simulate blocking operation
            requestsProcessed.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("requestsReceived", requestsReceived.get());
        stats.put("requestsProcessed", requestsProcessed.get());
        stats.put("queueSize", (long) queue.size());
        return stats;
    }
    
    public void shutdown() {
        running = false;
        asyncLayer.shutdown();
        syncLayer.shutdown();
        try {
            asyncLayer.awaitTermination(5, TimeUnit.SECONDS);
            syncLayer.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Half-Sync/Half-Async shut down");
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Example 2: Priority-Based Half-Sync/Half-Async
 * 
 * Uses priority queue to process high-priority requests first.
 */
class PriorityHalfSyncHalfAsync {
    private final PriorityBlockingQueue<PriorityRequest> queue;
    private final ExecutorService asyncLayer;
    private final ExecutorService syncLayer;
    private volatile boolean running;
    
    static class PriorityRequest implements Comparable<PriorityRequest> {
        final Request request;
        final int priority;
        
        PriorityRequest(Request request, int priority) {
            this.request = request;
            this.priority = priority;
        }
        
        @Override
        public int compareTo(PriorityRequest other) {
            return Integer.compare(other.priority, this.priority);
        }
    }
    
    public PriorityHalfSyncHalfAsync() {
        this.queue = new PriorityBlockingQueue<>();
        this.asyncLayer = Executors.newFixedThreadPool(2);
        this.syncLayer = Executors.newFixedThreadPool(3);
        this.running = false;
        
        System.out.println("Priority Half-Sync/Half-Async initialized");
    }
    
    public void start() {
        running = true;
        
        for (int i = 0; i < 3; i++) {
            syncLayer.submit(() -> {
                while (running) {
                    try {
                        PriorityRequest pr = queue.poll(1, TimeUnit.SECONDS);
                        if (pr != null) {
                            System.out.println("  [Sync] Processing priority " + 
                                             pr.priority + ": " + pr.request.getId());
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }
    
    public void submitRequest(Request request, int priority) {
        asyncLayer.submit(() -> {
            System.out.println("  [Async] Received priority " + priority + ": " + 
                             request.getId());
            queue.offer(new PriorityRequest(request, priority));
        });
    }
    
    public void shutdown() {
        running = false;
        asyncLayer.shutdown();
        syncLayer.shutdown();
        try {
            asyncLayer.awaitTermination(5, TimeUnit.SECONDS);
            syncLayer.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Priority Half-Sync/Half-Async shut down");
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Example 3: Network Server with Half-Sync/Half-Async
 * 
 * Async layer handles network I/O.
 * Sync layer processes business logic.
 */
class NetworkServerHalfSyncHalfAsync {
    private final BlockingQueue<Request> requestQueue;
    private final ExecutorService ioThreads;
    private final ExecutorService workerThreads;
    private final AtomicLong connectionsHandled;
    private volatile boolean running;
    
    public NetworkServerHalfSyncHalfAsync() {
        this.requestQueue = new LinkedBlockingQueue<>(100);
        this.ioThreads = Executors.newFixedThreadPool(2);
        this.workerThreads = Executors.newFixedThreadPool(4);
        this.connectionsHandled = new AtomicLong(0);
        this.running = false;
        
        System.out.println("Network Server Half-Sync/Half-Async initialized");
    }
    
    public void start() {
        running = true;
        
        // Worker threads (sync layer)
        for (int i = 0; i < 4; i++) {
            workerThreads.submit(() -> {
                while (running) {
                    try {
                        Request request = requestQueue.poll(1, TimeUnit.SECONDS);
                        if (request != null) {
                            handleBusinessLogic(request);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        System.out.println("Network server started");
    }
    
    public void acceptConnection(Request request) {
        // Async I/O layer
        ioThreads.submit(() -> {
            try {
                connectionsHandled.incrementAndGet();
                System.out.println("  [I/O] Connection: " + request.getId());
                
                // Queue for worker threads
                requestQueue.put(request);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    private void handleBusinessLogic(Request request) {
        // Sync processing layer
        try {
            System.out.println("  [Worker] Processing: " + request.getId());
            Thread.sleep(150); // Simulate database access, etc.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public long getConnectionsHandled() {
        return connectionsHandled.get();
    }
    
    public void shutdown() {
        running = false;
        ioThreads.shutdown();
        workerThreads.shutdown();
        try {
            ioThreads.awaitTermination(5, TimeUnit.SECONDS);
            workerThreads.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Network server shut down. Connections: " + 
                             connectionsHandled.get());
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Demonstration of the Half-Sync/Half-Async Pattern
 */
public class HalfSyncHalfAsyncPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Half-Sync/Half-Async Pattern Demo ===\n");
        
        // Example 1: Basic Half-Sync/Half-Async
        System.out.println("1. Basic Half-Sync/Half-Async:");
        BasicHalfSyncHalfAsync basic = new BasicHalfSyncHalfAsync(2, 4, 10);
        basic.start();
        
        for (int i = 1; i <= 5; i++) {
            basic.submitRequestAsync(new Request("req-" + i, "data-" + i));
        }
        
        Thread.sleep(1000);
        System.out.println("Statistics: " + basic.getStatistics());
        basic.shutdown();
        
        // Example 2: Priority-Based
        System.out.println("\n2. Priority-Based Half-Sync/Half-Async:");
        PriorityHalfSyncHalfAsync priority = new PriorityHalfSyncHalfAsync();
        priority.start();
        
        priority.submitRequest(new Request("req-low", "low priority"), 1);
        priority.submitRequest(new Request("req-high", "high priority"), 10);
        priority.submitRequest(new Request("req-medium", "medium priority"), 5);
        
        Thread.sleep(500);
        priority.shutdown();
        
        // Example 3: Network Server
        System.out.println("\n3. Network Server Half-Sync/Half-Async:");
        NetworkServerHalfSyncHalfAsync server = new NetworkServerHalfSyncHalfAsync();
        server.start();
        
        for (int i = 1; i <= 3; i++) {
            server.acceptConnection(new Request("conn-" + i, "request data"));
        }
        
        Thread.sleep(1000);
        server.shutdown();
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Separates async I/O from sync processing");
        System.out.println("✓ Simplifies programming model");
        System.out.println("✓ Improves throughput and responsiveness");
        System.out.println("✓ Better resource utilization");
        System.out.println("✓ Used in ACE framework, JAWS web server");
    }
}
