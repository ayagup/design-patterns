package concurrency;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reactor Pattern
 * 
 * Intent: Handle service requests delivered to an application by one or more inputs.
 * The service handler demultiplexes incoming requests and dispatches them
 * synchronously to associated request handlers.
 * 
 * Also Known As: Dispatcher, Event Demultiplexer
 * 
 * Motivation:
 * Server applications must handle multiple clients concurrently.
 * Traditional thread-per-connection wastes resources.
 * Reactor uses event-driven architecture with non-blocking I/O.
 * 
 * Applicability:
 * - High-performance network servers
 * - Event-driven architectures
 * - Systems handling many concurrent connections
 * - Applications requiring low latency
 * - Non-blocking I/O operations
 */

/**
 * Event types that reactor can handle
 */
enum EventType {
    READ,
    WRITE,
    ACCEPT,
    CONNECT
}

/**
 * Event handler interface
 */
interface EventHandler {
    void handleEvent(SelectionKey key) throws IOException;
    EventType getEventType();
}

/**
 * Example 1: Basic Reactor
 * 
 * Single-threaded reactor with event loop.
 * Handles multiple connections with single thread using NIO.
 */
class BasicReactor implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final Map<SelectionKey, EventHandler> handlers;
    private volatile boolean running;
    private final AtomicLong eventsProcessed;
    
    public BasicReactor(int port) throws IOException {
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.handlers = new ConcurrentHashMap<>();
        this.running = false;
        this.eventsProcessed = new AtomicLong(0);
        
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        
        // Register accept handler
        SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        handlers.put(key, new AcceptHandler(selector, serverChannel));
        
        System.out.println("Basic Reactor initialized on port " + port);
    }
    
    @Override
    public void run() {
        running = true;
        System.out.println("Basic Reactor event loop started");
        
        try {
            while (running) {
                // Wait for events
                selector.select(1000);
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    
                    if (!key.isValid()) {
                        continue;
                    }
                    
                    // Dispatch to handler
                    EventHandler handler = handlers.get(key);
                    if (handler != null) {
                        try {
                            handler.handleEvent(key);
                            eventsProcessed.incrementAndGet();
                        } catch (IOException e) {
                            System.err.println("Error handling event: " + e.getMessage());
                            key.cancel();
                            handlers.remove(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Reactor error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    public void registerHandler(SelectionKey key, EventHandler handler) {
        handlers.put(key, handler);
    }
    
    public void shutdown() {
        running = false;
    }
    
    private void cleanup() {
        try {
            for (SelectionKey key : handlers.keySet()) {
                key.cancel();
            }
            selector.close();
            serverChannel.close();
            System.out.println("Basic Reactor shut down. Events processed: " + 
                             eventsProcessed.get());
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
    
    // Accept handler for new connections
    static class AcceptHandler implements EventHandler {
        private final Selector selector;
        private final ServerSocketChannel serverChannel;
        
        AcceptHandler(Selector selector, ServerSocketChannel serverChannel) {
            this.selector = selector;
            this.serverChannel = serverChannel;
        }
        
        @Override
        public void handleEvent(SelectionKey key) throws IOException {
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                clientChannel.configureBlocking(false);
                SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
                
                // Attach read handler
                BasicReactor reactor = (BasicReactor) key.attachment();
                if (reactor != null) {
                    reactor.registerHandler(clientKey, new ReadHandler(clientChannel));
                }
                
                System.out.println("Accepted new connection");
            }
        }
        
        @Override
        public EventType getEventType() {
            return EventType.ACCEPT;
        }
    }
    
    // Read handler for client data
    static class ReadHandler implements EventHandler {
        private final SocketChannel channel;
        private final ByteBuffer buffer;
        
        ReadHandler(SocketChannel channel) {
            this.channel = channel;
            this.buffer = ByteBuffer.allocate(1024);
        }
        
        @Override
        public void handleEvent(SelectionKey key) throws IOException {
            buffer.clear();
            int bytesRead = channel.read(buffer);
            
            if (bytesRead == -1) {
                // Connection closed
                channel.close();
                key.cancel();
                System.out.println("Connection closed");
                return;
            }
            
            if (bytesRead > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                
                String message = new String(data);
                System.out.println("Received: " + message.trim());
                
                // Echo back (switch to write mode)
                key.interestOps(SelectionKey.OP_WRITE);
                key.attach(ByteBuffer.wrap(("Echo: " + message).getBytes()));
            }
        }
        
        @Override
        public EventType getEventType() {
            return EventType.READ;
        }
    }
}

/**
 * Example 2: Multi-Threaded Reactor
 * 
 * Main reactor thread handles accepts.
 * Worker threads handle I/O operations.
 * Improves scalability on multi-core systems.
 */
class MultiThreadedReactor {
    private final Selector acceptSelector;
    private final ServerSocketChannel serverChannel;
    private final ExecutorService workerPool;
    private volatile boolean running;
    private final AtomicLong connectionsAccepted;
    
    public MultiThreadedReactor(int port, int workerCount) throws IOException {
        this.acceptSelector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.workerPool = Executors.newFixedThreadPool(workerCount);
        this.running = false;
        this.connectionsAccepted = new AtomicLong(0);
        
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.register(acceptSelector, SelectionKey.OP_ACCEPT);
        
        System.out.println("Multi-threaded Reactor initialized on port " + port + 
                         " with " + workerCount + " workers");
    }
    
    public void start() {
        running = true;
        
        // Main reactor thread for accepts
        new Thread(() -> {
            System.out.println("Accept thread started");
            
            try {
                while (running) {
                    acceptSelector.select(1000);
                    
                    Set<SelectionKey> keys = acceptSelector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        
                        if (key.isAcceptable()) {
                            handleAccept();
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Accept thread error: " + e.getMessage());
            }
        }, "Reactor-Accept").start();
    }
    
    private void handleAccept() throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            connectionsAccepted.incrementAndGet();
            
            // Dispatch to worker pool
            workerPool.submit(new WorkerHandler(clientChannel));
            
            System.out.println("Connection accepted, dispatched to worker pool");
        }
    }
    
    public void shutdown() {
        running = false;
        workerPool.shutdown();
        try {
            workerPool.awaitTermination(5, TimeUnit.SECONDS);
            acceptSelector.close();
            serverChannel.close();
            System.out.println("Multi-threaded Reactor shut down. " +
                             "Connections accepted: " + connectionsAccepted.get());
        } catch (IOException | InterruptedException e) {
            System.err.println("Shutdown error: " + e.getMessage());
        }
    }
    
    // Worker handler running in thread pool
    static class WorkerHandler implements Runnable {
        private final SocketChannel channel;
        
        WorkerHandler(SocketChannel channel) {
            this.channel = channel;
        }
        
        @Override
        public void run() {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                
                while (channel.isOpen()) {
                    buffer.clear();
                    int bytesRead = channel.read(buffer);
                    
                    if (bytesRead == -1) {
                        break;
                    }
                    
                    if (bytesRead > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        
                        String message = new String(data);
                        System.out.println("[Worker " + Thread.currentThread().getId() + 
                                         "] Received: " + message.trim());
                        
                        // Echo back
                        String response = "Echo from worker " + 
                                        Thread.currentThread().getId() + ": " + message;
                        channel.write(ByteBuffer.wrap(response.getBytes()));
                    }
                }
                
                channel.close();
            } catch (IOException e) {
                System.err.println("Worker error: " + e.getMessage());
            }
        }
    }
}

/**
 * Example 3: Reactor with Timer Events
 * 
 * Extends reactor to handle scheduled timer events.
 * Useful for timeouts, periodic tasks, and keepalives.
 */
class TimerReactor implements Runnable {
    private final Selector selector;
    private final PriorityQueue<TimerTask> timerQueue;
    private volatile boolean running;
    private final AtomicLong tasksExecuted;
    
    static class TimerTask implements Comparable<TimerTask> {
        final long executeAt;
        final Runnable task;
        final String name;
        
        TimerTask(long executeAt, Runnable task, String name) {
            this.executeAt = executeAt;
            this.task = task;
            this.name = name;
        }
        
        @Override
        public int compareTo(TimerTask other) {
            return Long.compare(this.executeAt, other.executeAt);
        }
    }
    
    public TimerReactor() throws IOException {
        this.selector = Selector.open();
        this.timerQueue = new PriorityQueue<>();
        this.running = false;
        this.tasksExecuted = new AtomicLong(0);
        
        System.out.println("Timer Reactor initialized");
    }
    
    public void scheduleTask(Runnable task, long delayMs, String name) {
        long executeAt = System.currentTimeMillis() + delayMs;
        synchronized (timerQueue) {
            timerQueue.offer(new TimerTask(executeAt, task, name));
        }
        selector.wakeup();
    }
    
    @Override
    public void run() {
        running = true;
        System.out.println("Timer Reactor event loop started");
        
        try {
            while (running) {
                long timeout = getNextTimeout();
                
                if (timeout > 0) {
                    selector.select(timeout);
                } else {
                    selector.selectNow();
                }
                
                // Process timer events
                processTimerEvents();
                
                // Process I/O events
                processIOEvents();
            }
        } catch (IOException e) {
            System.err.println("Timer Reactor error: " + e.getMessage());
        }
    }
    
    private long getNextTimeout() {
        synchronized (timerQueue) {
            TimerTask nextTask = timerQueue.peek();
            if (nextTask == null) {
                return 1000; // Default timeout
            }
            
            long now = System.currentTimeMillis();
            long timeout = nextTask.executeAt - now;
            return Math.max(0, timeout);
        }
    }
    
    private void processTimerEvents() {
        long now = System.currentTimeMillis();
        
        synchronized (timerQueue) {
            while (!timerQueue.isEmpty() && timerQueue.peek().executeAt <= now) {
                TimerTask task = timerQueue.poll();
                
                try {
                    System.out.println("Executing timer task: " + task.name);
                    task.task.run();
                    tasksExecuted.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Timer task error: " + e.getMessage());
                }
            }
        }
    }
    
    private void processIOEvents() {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            
            // Handle I/O event (key would be used in real implementation)
            System.out.println("Processing I/O event");
        }
    }
    
    public void shutdown() {
        running = false;
        try {
            selector.close();
            System.out.println("Timer Reactor shut down. Tasks executed: " + 
                             tasksExecuted.get());
        } catch (IOException e) {
            System.err.println("Shutdown error: " + e.getMessage());
        }
    }
}

/**
 * Example 4: Reactor with Event Priorities
 * 
 * Prioritizes certain events over others.
 * High-priority events are processed first.
 */
class PriorityReactor implements Runnable {
    private final Selector selector;
    private final Map<SelectionKey, Integer> priorities;
    private final PriorityQueue<PriorityEvent> eventQueue;
    private volatile boolean running;
    
    static class PriorityEvent implements Comparable<PriorityEvent> {
        final SelectionKey key;
        final int priority;
        final long timestamp;
        
        PriorityEvent(SelectionKey key, int priority) {
            this.key = key;
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public int compareTo(PriorityEvent other) {
            // Higher priority first, then FIFO
            int cmp = Integer.compare(other.priority, this.priority);
            if (cmp == 0) {
                return Long.compare(this.timestamp, other.timestamp);
            }
            return cmp;
        }
    }
    
    public PriorityReactor() throws IOException {
        this.selector = Selector.open();
        this.priorities = new ConcurrentHashMap<>();
        this.eventQueue = new PriorityQueue<>();
        this.running = false;
        
        System.out.println("Priority Reactor initialized");
    }
    
    public void registerChannel(SelectableChannel channel, int ops, int priority) 
            throws IOException {
        SelectionKey key = channel.register(selector, ops);
        priorities.put(key, priority);
    }
    
    @Override
    public void run() {
        running = true;
        System.out.println("Priority Reactor event loop started");
        
        try {
            while (running) {
                selector.select(1000);
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                
                // Collect events with priorities
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    
                    int priority = priorities.getOrDefault(key, 0);
                    eventQueue.offer(new PriorityEvent(key, priority));
                }
                
                // Process events by priority
                while (!eventQueue.isEmpty()) {
                    PriorityEvent event = eventQueue.poll();
                    
                    if (event.key.isValid()) {
                        System.out.println("Processing event with priority " + 
                                         event.priority);
                        // Handle event based on type
                        handleEvent(event.key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Priority Reactor error: " + e.getMessage());
        }
    }
    
    private void handleEvent(SelectionKey key) {
        // Simulate event handling
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void shutdown() {
        running = false;
        try {
            selector.close();
            System.out.println("Priority Reactor shut down");
        } catch (IOException e) {
            System.err.println("Shutdown error: " + e.getMessage());
        }
    }
}

/**
 * Example 5: Simulated Reactor for Demo
 * 
 * Demonstrates reactor pattern without requiring network setup.
 * Uses simulated events for demonstration purposes.
 */
class SimulatedReactor {
    private final BlockingQueue<SimulatedEvent> eventQueue;
    private final Map<String, EventListener> listeners;
    private volatile boolean running;
    private final ExecutorService executor;
    
    interface EventListener {
        void onEvent(SimulatedEvent event);
    }
    
    static class SimulatedEvent {
        final String type;
        final String data;
        final long timestamp;
        
        SimulatedEvent(String type, String data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public SimulatedReactor() {
        this.eventQueue = new LinkedBlockingQueue<>();
        this.listeners = new ConcurrentHashMap<>();
        this.running = false;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void registerListener(String eventType, EventListener listener) {
        listeners.put(eventType, listener);
        System.out.println("Registered listener for event type: " + eventType);
    }
    
    public void submitEvent(String type, String data) {
        eventQueue.offer(new SimulatedEvent(type, data));
    }
    
    public void start() {
        running = true;
        executor.submit(() -> {
            System.out.println("Simulated Reactor event loop started");
            
            while (running) {
                try {
                    SimulatedEvent event = eventQueue.poll(1, TimeUnit.SECONDS);
                    
                    if (event != null) {
                        EventListener listener = listeners.get(event.type);
                        
                        if (listener != null) {
                            System.out.println("Dispatching " + event.type + 
                                             " event to listener");
                            listener.onEvent(event);
                        } else {
                            System.out.println("No listener for event type: " + 
                                             event.type);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            System.out.println("Simulated Reactor event loop stopped");
        });
    }
    
    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Simulated Reactor shut down");
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Demonstration of the Reactor Pattern
 */
public class ReactorPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Reactor Pattern Demo ===\n");
        
        // Example 1: Basic Reactor (simulated demo - would need actual network in production)
        System.out.println("1. Basic Reactor (single-threaded event loop):");
        System.out.println("- Uses single thread with NIO Selector");
        System.out.println("- Handles multiple connections efficiently");
        System.out.println("- Non-blocking I/O operations");
        System.out.println("(Network demo skipped - would require actual server setup)\n");
        
        // Example 2: Multi-Threaded Reactor
        System.out.println("2. Multi-Threaded Reactor:");
        System.out.println("- Main thread handles accepts");
        System.out.println("- Worker threads handle I/O");
        System.out.println("- Better scalability on multi-core systems");
        System.out.println("(Network demo skipped - would require actual server setup)\n");
        
        // Example 3: Timer Reactor
        System.out.println("3. Reactor with Timer Events:");
        TimerReactor timerReactor = new TimerReactor();
        
        // Schedule some tasks
        timerReactor.scheduleTask(
            () -> System.out.println("  -> Task 1 executed"),
            100,
            "Task-1"
        );
        
        timerReactor.scheduleTask(
            () -> System.out.println("  -> Task 2 executed"),
            200,
            "Task-2"
        );
        
        timerReactor.scheduleTask(
            () -> System.out.println("  -> Periodic keepalive"),
            300,
            "Keepalive"
        );
        
        Thread reactorThread = new Thread(timerReactor);
        reactorThread.start();
        
        Thread.sleep(500);
        timerReactor.shutdown();
        reactorThread.join();
        
        // Example 4: Priority Reactor
        System.out.println("\n4. Reactor with Event Priorities:");
        System.out.println("- High-priority events processed first");
        System.out.println("- Useful for QoS and real-time systems");
        System.out.println("- Prevents head-of-line blocking for critical events");
        System.out.println("(Demo skipped - requires channel setup)\n");
        
        // Example 5: Simulated Reactor
        System.out.println("5. Simulated Reactor (for demonstration):");
        SimulatedReactor simReactor = new SimulatedReactor();
        
        // Register event listeners
        simReactor.registerListener("USER_LOGIN", event -> 
            System.out.println("  -> User logged in: " + event.data)
        );
        
        simReactor.registerListener("DATA_RECEIVED", event -> 
            System.out.println("  -> Data received: " + event.data)
        );
        
        simReactor.registerListener("CONNECTION_CLOSED", event -> 
            System.out.println("  -> Connection closed: " + event.data)
        );
        
        simReactor.start();
        
        // Submit events
        simReactor.submitEvent("USER_LOGIN", "alice@example.com");
        simReactor.submitEvent("DATA_RECEIVED", "payload-123");
        simReactor.submitEvent("CONNECTION_CLOSED", "client-456");
        simReactor.submitEvent("UNKNOWN_EVENT", "test");
        
        Thread.sleep(2000);
        simReactor.shutdown();
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Handles many connections with few threads");
        System.out.println("✓ Non-blocking I/O improves scalability");
        System.out.println("✓ Event-driven architecture");
        System.out.println("✓ Separates application logic from dispatch mechanism");
        System.out.println("✓ Used in high-performance servers (Netty, Node.js)");
        System.out.println("✓ Supports timer events and priorities");
    }
}
