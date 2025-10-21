package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;

/**
 * Leader/Followers Pattern
 * 
 * Intent: Provide an efficient concurrency model where multiple threads
 * take turns sharing a set of event sources to detect, demultiplex,
 * dispatch, and process service requests that occur on the event sources.
 * 
 * Motivation:
 * One thread (leader) waits for events. When event arrives:
 * - Leader promotes a follower to become new leader
 * - Original leader processes the event
 * - After processing, thread rejoins follower set
 * 
 * Eliminates need for separate thread pools and event queues.
 * 
 * Applicability:
 * - High-performance network servers
 * - Event-driven systems with many connections
 * - Systems where thread coordination overhead is critical
 * - Real-time systems requiring predictable latency
 */

/**
 * Event to be processed
 */
class Event {
    private final String id;
    private final String type;
    private final String data;
    
    public Event(String id, String type, String data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }
    
    public String getId() { return id; }
    public String getType() { return type; }
    public String getData() { return data; }
}

/**
 * Example 1: Basic Leader/Followers Thread Pool
 * 
 * Thread pool where one thread is leader, others are followers.
 * Leader waits for events, promotes follower when event arrives.
 */
class BasicLeaderFollowers {
    private final int poolSize;
    private final Deque<Thread> followers;
    private final BlockingQueue<Event> eventQueue;
    private final Lock lock;
    private final Condition hasFollowers;
    private Thread leader;
    private volatile boolean running;
    private final AtomicLong eventsProcessed;
    
    public BasicLeaderFollowers(int poolSize) {
        this.poolSize = poolSize;
        this.followers = new ArrayDeque<>();
        this.eventQueue = new LinkedBlockingQueue<>();
        this.lock = new ReentrantLock();
        this.hasFollowers = lock.newCondition();
        this.leader = null;
        this.running = false;
        this.eventsProcessed = new AtomicLong(0);
        
        System.out.println("Basic Leader/Followers initialized with " + 
                         poolSize + " threads");
    }
    
    public void start() {
        running = true;
        
        // Create thread pool
        for (int i = 0; i < poolSize; i++) {
            Thread thread = new Thread(new WorkerRunnable(), "Worker-" + i);
            thread.start();
            
            lock.lock();
            try {
                followers.addLast(thread);
                hasFollowers.signal();
            } finally {
                lock.unlock();
            }
        }
        
        System.out.println("Leader/Followers started");
    }
    
    public void submitEvent(Event event) {
        eventQueue.offer(event);
    }
    
    private class WorkerRunnable implements Runnable {
        @Override
        public void run() {
            while (running) {
                // Try to become leader
                lock.lock();
                try {
                    while (running && leader != null) {
                        // Wait as follower
                        hasFollowers.await(100, TimeUnit.MILLISECONDS);
                    }
                    
                    if (!running) break;
                    
                    // Become leader
                    leader = Thread.currentThread();
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    lock.unlock();
                }
                
                // Wait for event (as leader)
                try {
                    Event event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        // Promote follower to leader
                        promoteFollower();
                        
                        // Process event
                        processEvent(event);
                        
                        // Rejoin as follower
                        rejoinAsFollower();
                    } else {
                        // No event, step down
                        lock.lock();
                        try {
                            leader = null;
                            hasFollowers.signal();
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    private void promoteFollower() {
        lock.lock();
        try {
            leader = null;
            hasFollowers.signal();
        } finally {
            lock.unlock();
        }
    }
    
    private void rejoinAsFollower() {
        lock.lock();
        try {
            followers.addLast(Thread.currentThread());
            hasFollowers.signal();
        } finally {
            lock.unlock();
        }
    }
    
    private void processEvent(Event event) {
        try {
            System.out.println("  [" + Thread.currentThread().getName() + 
                             "] Processing: " + event.getId());
            Thread.sleep(100); // Simulate processing
            eventsProcessed.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public long getEventsProcessed() {
        return eventsProcessed.get();
    }
    
    public void shutdown() {
        running = false;
        lock.lock();
        try {
            hasFollowers.signalAll();
        } finally {
            lock.unlock();
        }
        System.out.println("Leader/Followers shut down. Events processed: " + 
                         eventsProcessed.get());
    }
}

/**
 * Example 2: Priority-Based Leader/Followers
 * 
 * Events have priorities. Leader processes high-priority events first.
 */
class PriorityLeaderFollowers {
    private final PriorityBlockingQueue<PriorityEvent> eventQueue;
    private final ExecutorService threadPool;
    private final Semaphore leaderSemaphore;
    private volatile boolean running;
    
    static class PriorityEvent implements Comparable<PriorityEvent> {
        final Event event;
        final int priority;
        
        PriorityEvent(Event event, int priority) {
            this.event = event;
            this.priority = priority;
        }
        
        @Override
        public int compareTo(PriorityEvent other) {
            return Integer.compare(other.priority, this.priority);
        }
    }
    
    public PriorityLeaderFollowers(int poolSize) {
        this.eventQueue = new PriorityBlockingQueue<>();
        this.threadPool = Executors.newFixedThreadPool(poolSize);
        this.leaderSemaphore = new Semaphore(1);
        this.running = false;
        
        System.out.println("Priority Leader/Followers initialized");
    }
    
    public void start() {
        running = true;
        
        for (int i = 0; i < 4; i++) {
            threadPool.submit(() -> {
                while (running) {
                    try {
                        // Try to become leader
                        leaderSemaphore.acquire();
                        
                        // Wait for event
                        PriorityEvent pe = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                        
                        if (pe != null) {
                            // Release leadership immediately
                            leaderSemaphore.release();
                            
                            // Process event
                            System.out.println("  [" + Thread.currentThread().getName() + 
                                             "] Processing priority " + pe.priority + 
                                             ": " + pe.event.getId());
                            Thread.sleep(50);
                        } else {
                            leaderSemaphore.release();
                        }
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        System.out.println("Priority Leader/Followers started");
    }
    
    public void submitEvent(Event event, int priority) {
        eventQueue.offer(new PriorityEvent(event, priority));
    }
    
    public void shutdown() {
        running = false;
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Priority Leader/Followers shut down");
        } catch (InterruptedException e) {
            System.err.println("Shutdown interrupted");
        }
    }
}

/**
 * Example 3: Network Connection Handler with Leader/Followers
 * 
 * Simulates network server where leader accepts connections.
 */
class NetworkLeaderFollowers {
    private final BlockingQueue<Event> connectionQueue;
    private final CyclicBarrier barrier;
    private final AtomicLong connectionsHandled;
    private volatile boolean running;
    private volatile Thread currentLeader;
    
    public NetworkLeaderFollowers(int workerCount) {
        this.connectionQueue = new LinkedBlockingQueue<>();
        this.barrier = new CyclicBarrier(workerCount);
        this.connectionsHandled = new AtomicLong(0);
        this.running = false;
        this.currentLeader = null;
        
        System.out.println("Network Leader/Followers initialized");
    }
    
    public void start(int workerCount) {
        running = true;
        
        for (int i = 0; i < workerCount; i++) {
            new Thread(() -> {
                while (running) {
                    try {
                        // Synchronize to select leader
                        barrier.await(100, TimeUnit.MILLISECONDS);
                        
                        // First thread becomes leader
                        synchronized (this) {
                            if (currentLeader == null) {
                                currentLeader = Thread.currentThread();
                            }
                        }
                        
                        if (currentLeader == Thread.currentThread()) {
                            // Leader waits for connection
                            Event event = connectionQueue.poll(100, TimeUnit.MILLISECONDS);
                            
                            if (event != null) {
                                // Promote new leader
                                currentLeader = null;
                                
                                // Process connection
                                handleConnection(event);
                            } else {
                                currentLeader = null;
                            }
                        }
                        
                    } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                        // Continue
                    }
                }
            }, "NetworkWorker-" + i).start();
        }
        
        System.out.println("Network Leader/Followers started");
    }
    
    public void acceptConnection(Event connection) {
        connectionQueue.offer(connection);
    }
    
    private void handleConnection(Event connection) {
        try {
            System.out.println("  [" + Thread.currentThread().getName() + 
                             "] Handling connection: " + connection.getId());
            Thread.sleep(100);
            connectionsHandled.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public long getConnectionsHandled() {
        return connectionsHandled.get();
    }
    
    public void shutdown() {
        running = false;
        System.out.println("Network Leader/Followers shut down. Connections: " + 
                         connectionsHandled.get());
    }
}

/**
 * Demonstration of the Leader/Followers Pattern
 */
public class LeaderFollowersPattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Leader/Followers Pattern Demo ===\n");
        
        // Example 1: Basic Leader/Followers
        System.out.println("1. Basic Leader/Followers:");
        BasicLeaderFollowers basic = new BasicLeaderFollowers(3);
        basic.start();
        
        for (int i = 1; i <= 5; i++) {
            basic.submitEvent(new Event("event-" + i, "request", "data-" + i));
            Thread.sleep(50);
        }
        
        Thread.sleep(1000);
        basic.shutdown();
        
        // Example 2: Priority-Based Leader/Followers
        System.out.println("\n2. Priority-Based Leader/Followers:");
        PriorityLeaderFollowers priority = new PriorityLeaderFollowers(4);
        priority.start();
        
        priority.submitEvent(new Event("low", "request", "low priority"), 1);
        priority.submitEvent(new Event("high", "request", "high priority"), 10);
        priority.submitEvent(new Event("medium", "request", "medium priority"), 5);
        
        Thread.sleep(500);
        priority.shutdown();
        
        // Example 3: Network Connection Handler
        System.out.println("\n3. Network Leader/Followers:");
        NetworkLeaderFollowers network = new NetworkLeaderFollowers(3);
        network.start(3);
        
        for (int i = 1; i <= 4; i++) {
            network.acceptConnection(new Event("conn-" + i, "connection", "client-" + i));
            Thread.sleep(50);
        }
        
        Thread.sleep(1000);
        network.shutdown();
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Eliminates separate event queues");
        System.out.println("✓ Reduces thread synchronization overhead");
        System.out.println("✓ Better cache locality (leader processes immediately)");
        System.out.println("✓ Predictable performance");
        System.out.println("✓ Used in ACE framework, JAWS web server");
    }
}
