package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * GUARDED SUSPENSION PATTERN
 * 
 * Suspends execution of a method until a guard condition becomes true. Unlike the
 * Balking pattern which returns immediately, Guarded Suspension waits (blocks) until
 * the condition is satisfied.
 * 
 * Benefits:
 * - Thread waits for condition instead of busy waiting
 * - Efficient resource usage (no CPU spinning)
 * - Clean synchronization semantics
 * - Built-in support via wait/notify or Condition
 * - Thread-safe state transitions
 * 
 * Use Cases:
 * - Producer-consumer queues
 * - Resource pools
 * - Request queues
 * - State-dependent operations
 * - Thread coordination
 */

// Example 1: Request Queue with Guarded Suspension
class RequestQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int maxSize;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();
    
    public RequestQueue(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public void enqueue(T request) throws InterruptedException {
        lock.lock();
        try {
            // Wait until queue is not full (guarded suspension)
            while (queue.size() >= maxSize) {
                System.out.println("  ⏳ [" + Thread.currentThread().getName() + 
                                 "] Queue full, waiting...");
                notFull.await();
            }
            
            queue.offer(request);
            System.out.println("  ✅ [" + Thread.currentThread().getName() + 
                             "] Enqueued: " + request + " (size: " + queue.size() + ")");
            
            // Signal that queue is not empty
            notEmpty.signal();
            
        } finally {
            lock.unlock();
        }
    }
    
    public T dequeue() throws InterruptedException {
        lock.lock();
        try {
            // Wait until queue is not empty (guarded suspension)
            while (queue.isEmpty()) {
                System.out.println("  ⏳ [" + Thread.currentThread().getName() + 
                                 "] Queue empty, waiting...");
                notEmpty.await();
            }
            
            T request = queue.poll();
            System.out.println("  ✅ [" + Thread.currentThread().getName() + 
                             "] Dequeued: " + request + " (size: " + queue.size() + ")");
            
            // Signal that queue is not full
            notFull.signal();
            
            return request;
            
        } finally {
            lock.unlock();
        }
    }
    
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}

// Example 2: Connection Pool
class ConnectionPool {
    private final Queue<Connection> availableConnections = new LinkedList<>();
    private final int maxConnections;
    private int totalConnections = 0;
    private final Lock lock = new ReentrantLock();
    private final Condition connectionAvailable = lock.newCondition();
    
    public ConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public Connection acquire() throws InterruptedException {
        lock.lock();
        try {
            // Wait until a connection is available (guarded suspension)
            while (availableConnections.isEmpty() && totalConnections >= maxConnections) {
                System.out.println("  ⏳ [" + Thread.currentThread().getName() + 
                                 "] No connections available, waiting...");
                connectionAvailable.await();
            }
            
            Connection conn;
            if (!availableConnections.isEmpty()) {
                conn = availableConnections.poll();
                System.out.println("  ✅ [" + Thread.currentThread().getName() + 
                                 "] Reusing connection: " + conn.getId());
            } else {
                conn = new Connection(++totalConnections);
                System.out.println("  🆕 [" + Thread.currentThread().getName() + 
                                 "] Created new connection: " + conn.getId());
            }
            
            return conn;
            
        } finally {
            lock.unlock();
        }
    }
    
    public void release(Connection connection) {
        lock.lock();
        try {
            availableConnections.offer(connection);
            System.out.println("  ↩️  [" + Thread.currentThread().getName() + 
                             "] Released connection: " + connection.getId() + 
                             " (available: " + availableConnections.size() + ")");
            
            // Signal that a connection is available
            connectionAvailable.signal();
            
        } finally {
            lock.unlock();
        }
    }
    
    static class Connection {
        private final int id;
        
        public Connection(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
        
        public void use() throws InterruptedException {
            Thread.sleep(100);
        }
    }
}

// Example 3: State-Dependent Service
class Document {
    private enum State { DRAFT, SUBMITTED, APPROVED, REJECTED }
    
    private State state = State.DRAFT;
    private final Lock lock = new ReentrantLock();
    private final Condition approved = lock.newCondition();
    private final Condition submitted = lock.newCondition();
    
    public void submit() {
        lock.lock();
        try {
            if (state != State.DRAFT) {
                System.out.println("  ⚠️  Document already submitted");
                return;
            }
            
            state = State.SUBMITTED;
            System.out.println("  📝 Document submitted");
            
            // Signal waiting approvers
            submitted.signal();
            
        } finally {
            lock.unlock();
        }
    }
    
    public void approve() throws InterruptedException {
        lock.lock();
        try {
            // Wait until document is submitted (guarded suspension)
            while (state != State.SUBMITTED) {
                System.out.println("  ⏳ Waiting for document to be submitted...");
                submitted.await();
            }
            
            state = State.APPROVED;
            System.out.println("  ✅ Document approved");
            
            // Signal waiting processors
            approved.signalAll();
            
        } finally {
            lock.unlock();
        }
    }
    
    public void process() throws InterruptedException {
        lock.lock();
        try {
            // Wait until document is approved (guarded suspension)
            while (state != State.APPROVED) {
                System.out.println("  ⏳ [" + Thread.currentThread().getName() + 
                                 "] Waiting for approval...");
                approved.await();
            }
            
            System.out.println("  ⚙️  [" + Thread.currentThread().getName() + 
                             "] Processing approved document");
            
        } finally {
            lock.unlock();
        }
    }
}

// Example 4: Bounded Counter
class BoundedCounter {
    private int count = 0;
    private final int min;
    private final int max;
    private final Lock lock = new ReentrantLock();
    private final Condition notAtMax = lock.newCondition();
    private final Condition notAtMin = lock.newCondition();
    
    public BoundedCounter(int min, int max) {
        this.min = min;
        this.max = max;
        this.count = min;
    }
    
    public void increment() throws InterruptedException {
        lock.lock();
        try {
            // Wait until count is below max (guarded suspension)
            while (count >= max) {
                System.out.println("  ⏳ [" + Thread.currentThread().getName() + 
                                 "] Count at max (" + max + "), waiting...");
                notAtMax.await();
            }
            
            count++;
            System.out.println("  ⬆️  [" + Thread.currentThread().getName() + 
                             "] Incremented to: " + count);
            
            // Signal that count is not at min
            notAtMin.signal();
            
        } finally {
            lock.unlock();
        }
    }
    
    public void decrement() throws InterruptedException {
        lock.lock();
        try {
            // Wait until count is above min (guarded suspension)
            while (count <= min) {
                System.out.println("  ⏳ [" + Thread.currentThread().getName() + 
                                 "] Count at min (" + min + "), waiting...");
                notAtMin.await();
            }
            
            count--;
            System.out.println("  ⬇️  [" + Thread.currentThread().getName() + 
                             "] Decremented to: " + count);
            
            // Signal that count is not at max
            notAtMax.signal();
            
        } finally {
            lock.unlock();
        }
    }
    
    public int get() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}

// Example 5: Simple Guarded Object using wait/notify
class GuardedObject {
    private String data;
    
    public synchronized void setData(String data) {
        System.out.println("  📝 Setting data: " + data);
        this.data = data;
        
        // Notify waiting threads
        notifyAll();
    }
    
    public synchronized String getData() throws InterruptedException {
        // Wait until data is available (guarded suspension)
        while (data == null) {
            System.out.println("  ⏳ [" + Thread.currentThread().getName() + 
                             "] Data not ready, waiting...");
            wait();
        }
        
        System.out.println("  ✅ [" + Thread.currentThread().getName() + 
                         "] Got data: " + data);
        return data;
    }
}

// Demo
public class GuardedSuspensionPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║   GUARDED SUSPENSION PATTERN DEMONSTRATION    ║");
        System.out.println("╚═══════════════════════════════════════════════╝");
        
        // Example 1: Request Queue
        System.out.println("\n1. REQUEST QUEUE (Producer-Consumer)");
        System.out.println("────────────────────────────────────");
        
        RequestQueue<String> queue = new RequestQueue<>(3);
        
        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    queue.enqueue("Request-" + i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(500); // Start late to let queue fill
                for (int i = 1; i <= 5; i++) {
                    queue.dequeue();
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");
        
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        
        // Example 2: Connection Pool
        System.out.println("\n\n2. CONNECTION POOL (Resource Management)");
        System.out.println("────────────────────────────────────────");
        
        ConnectionPool pool = new ConnectionPool(2);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        for (int i = 1; i <= 4; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    ConnectionPool.Connection conn = pool.acquire();
                    System.out.println("  🔧 [Thread-" + taskId + "] Using connection");
                    conn.use();
                    pool.release(conn);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Example 3: Document State Machine
        System.out.println("\n\n3. DOCUMENT STATE MACHINE (State-Dependent Operations)");
        System.out.println("───────────────────────────────────────────────────────");
        
        Document document = new Document();
        
        // Processor threads waiting for approval
        Thread processor1 = new Thread(() -> {
            try {
                document.process();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Processor-1");
        
        Thread processor2 = new Thread(() -> {
            try {
                document.process();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Processor-2");
        
        // Approver thread
        Thread approver = new Thread(() -> {
            try {
                Thread.sleep(500);
                document.approve();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Approver");
        
        processor1.start();
        processor2.start();
        approver.start();
        
        Thread.sleep(200);
        document.submit();
        
        processor1.join();
        processor2.join();
        approver.join();
        
        // Example 4: Bounded Counter
        System.out.println("\n\n4. BOUNDED COUNTER (Range Constraints)");
        System.out.println("───────────────────────────────────────");
        
        BoundedCounter counter = new BoundedCounter(0, 5);
        
        Thread incrementer = new Thread(() -> {
            try {
                for (int i = 0; i < 8; i++) {
                    counter.increment();
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Incrementer");
        
        Thread decrementer = new Thread(() -> {
            try {
                Thread.sleep(300);
                for (int i = 0; i < 8; i++) {
                    counter.decrement();
                    Thread.sleep(150);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Decrementer");
        
        incrementer.start();
        decrementer.start();
        incrementer.join();
        decrementer.join();
        
        System.out.println("  📊 Final count: " + counter.get());
        
        // Example 5: Simple Guarded Object
        System.out.println("\n\n5. SIMPLE GUARDED OBJECT (wait/notify)");
        System.out.println("───────────────────────────────────────");
        
        GuardedObject guardedObj = new GuardedObject();
        
        Thread getter = new Thread(() -> {
            try {
                guardedObj.getData();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Getter");
        
        Thread setter = new Thread(() -> {
            try {
                Thread.sleep(500);
                guardedObj.setData("Important Data");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Setter");
        
        getter.start();
        setter.start();
        getter.join();
        setter.join();
        
        System.out.println("\n\n✅ Guarded Suspension Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Threads wait efficiently (no busy waiting)");
        System.out.println("  • Clean synchronization semantics");
        System.out.println("  • Thread-safe state-dependent operations");
        System.out.println("  • CPU-efficient (threads sleep while waiting)");
        System.out.println("  • Built-in support in Java (wait/notify, Condition)");
        
        System.out.println("\n🆚 Guarded Suspension vs Balking:");
        System.out.println("  • Guarded Suspension: WAITS until condition is true");
        System.out.println("  • Balking: RETURNS immediately if condition is false");
        System.out.println("  • Use Guarded Suspension when operation must eventually execute");
        System.out.println("  • Use Balking when operation is optional");
    }
}
