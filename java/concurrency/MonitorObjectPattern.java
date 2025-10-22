package concurrency;

import java.util.*;
import java.util.concurrent.*;

/**
 * MONITOR OBJECT PATTERN
 * 
 * Synchronizes concurrent method execution to ensure only one method runs at a time.
 * Provides automatic thread-safe access to an object's methods.
 * 
 * Benefits:
 * - Simplified thread synchronization
 * - Prevents race conditions
 * - Encapsulates locking logic
 * - Provides condition variables for waiting
 * - Ensures mutual exclusion
 * 
 * Use Cases:
 * - Shared resource management
 * - Producer-consumer scenarios
 * - Thread-safe collections
 * - State machines with concurrent access
 * - Banking systems with account operations
 */

// Example 1: Bank Account Monitor
class BankAccountMonitor {
    private double balance;
    private final Object lock = new Object();
    
    public BankAccountMonitor(double initialBalance) {
        this.balance = initialBalance;
    }
    
    public void deposit(double amount) {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " depositing $" + amount);
            balance += amount;
            System.out.println("  New balance: $" + balance);
            lock.notifyAll(); // Notify waiting threads
        }
    }
    
    public boolean withdraw(double amount) {
        synchronized (lock) {
            while (balance < amount) {
                System.out.println(Thread.currentThread().getName() + 
                    " waiting to withdraw $" + amount + " (balance: $" + balance + ")");
                try {
                    lock.wait(); // Wait for sufficient funds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            
            System.out.println(Thread.currentThread().getName() + " withdrawing $" + amount);
            balance -= amount;
            System.out.println("  New balance: $" + balance);
            return true;
        }
    }
    
    public double getBalance() {
        synchronized (lock) {
            return balance;
        }
    }
}

// Example 2: Bounded Buffer Monitor (Producer-Consumer)
class BoundedBufferMonitor<T> {
    private final Queue<T> buffer;
    private final int capacity;
    private final Object lock = new Object();
    
    public BoundedBufferMonitor(int capacity) {
        this.capacity = capacity;
        this.buffer = new LinkedList<>();
    }
    
    public void produce(T item) throws InterruptedException {
        synchronized (lock) {
            while (buffer.size() >= capacity) {
                System.out.println(Thread.currentThread().getName() + 
                    " waiting to produce (buffer full: " + buffer.size() + "/" + capacity + ")");
                lock.wait();
            }
            
            buffer.offer(item);
            System.out.println(Thread.currentThread().getName() + 
                " produced: " + item + " (buffer: " + buffer.size() + "/" + capacity + ")");
            lock.notifyAll();
        }
    }
    
    public T consume() throws InterruptedException {
        synchronized (lock) {
            while (buffer.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + 
                    " waiting to consume (buffer empty)");
                lock.wait();
            }
            
            T item = buffer.poll();
            System.out.println(Thread.currentThread().getName() + 
                " consumed: " + item + " (buffer: " + buffer.size() + "/" + capacity + ")");
            lock.notifyAll();
            return item;
        }
    }
    
    public int size() {
        synchronized (lock) {
            return buffer.size();
        }
    }
}

// Example 3: Thread-Safe Counter Monitor
class CounterMonitor {
    private int count = 0;
    private int waitThreshold;
    private final Object lock = new Object();
    
    public CounterMonitor(int waitThreshold) {
        this.waitThreshold = waitThreshold;
    }
    
    public void increment() {
        synchronized (lock) {
            count++;
            System.out.println(Thread.currentThread().getName() + 
                " incremented to " + count);
            
            if (count >= waitThreshold) {
                lock.notifyAll();
            }
        }
    }
    
    public void waitForThreshold() throws InterruptedException {
        synchronized (lock) {
            while (count < waitThreshold) {
                System.out.println(Thread.currentThread().getName() + 
                    " waiting for count to reach " + waitThreshold + 
                    " (current: " + count + ")");
                lock.wait();
            }
            
            System.out.println(Thread.currentThread().getName() + 
                " threshold reached! Count: " + count);
        }
    }
    
    public int getCount() {
        synchronized (lock) {
            return count;
        }
    }
}

// Example 4: Read-Write Monitor with Fairness
class ReadWriteMonitor {
    private int readers = 0;
    private int writers = 0;
    private int writeRequests = 0;
    private final Object lock = new Object();
    
    public void readLock() throws InterruptedException {
        synchronized (lock) {
            while (writers > 0 || writeRequests > 0) {
                System.out.println(Thread.currentThread().getName() + 
                    " waiting for read lock (writers: " + writers + ", write requests: " + writeRequests + ")");
                lock.wait();
            }
            
            readers++;
            System.out.println(Thread.currentThread().getName() + 
                " acquired read lock (readers: " + readers + ")");
        }
    }
    
    public void readUnlock() {
        synchronized (lock) {
            readers--;
            System.out.println(Thread.currentThread().getName() + 
                " released read lock (readers: " + readers + ")");
            lock.notifyAll();
        }
    }
    
    public void writeLock() throws InterruptedException {
        synchronized (lock) {
            writeRequests++;
            
            while (readers > 0 || writers > 0) {
                System.out.println(Thread.currentThread().getName() + 
                    " waiting for write lock (readers: " + readers + ", writers: " + writers + ")");
                lock.wait();
            }
            
            writeRequests--;
            writers++;
            System.out.println(Thread.currentThread().getName() + 
                " acquired write lock");
        }
    }
    
    public void writeUnlock() {
        synchronized (lock) {
            writers--;
            System.out.println(Thread.currentThread().getName() + 
                " released write lock");
            lock.notifyAll();
        }
    }
}

// Example 5: Connection Pool Monitor
class ConnectionPoolMonitor {
    private final Queue<String> availableConnections;
    private final Set<String> activeConnections;
    private final int maxConnections;
    private final Object lock = new Object();
    
    public ConnectionPoolMonitor(int maxConnections) {
        this.maxConnections = maxConnections;
        this.availableConnections = new LinkedList<>();
        this.activeConnections = new HashSet<>();
        
        // Initialize pool
        for (int i = 1; i <= maxConnections; i++) {
            availableConnections.offer("Connection-" + i);
        }
    }
    
    public String acquire() throws InterruptedException {
        synchronized (lock) {
            while (availableConnections.isEmpty()) {
                System.out.println(Thread.currentThread().getName() + 
                    " waiting for connection (active: " + activeConnections.size() + "/" + maxConnections + ")");
                lock.wait();
            }
            
            String connection = availableConnections.poll();
            activeConnections.add(connection);
            System.out.println(Thread.currentThread().getName() + 
                " acquired " + connection + 
                " (available: " + availableConnections.size() + ", active: " + activeConnections.size() + ")");
            return connection;
        }
    }
    
    public void release(String connection) {
        synchronized (lock) {
            if (activeConnections.remove(connection)) {
                availableConnections.offer(connection);
                System.out.println(Thread.currentThread().getName() + 
                    " released " + connection + 
                    " (available: " + availableConnections.size() + ", active: " + activeConnections.size() + ")");
                lock.notify();
            }
        }
    }
    
    public int getAvailableCount() {
        synchronized (lock) {
            return availableConnections.size();
        }
    }
}

// Demo
public class MonitorObjectPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   MONITOR OBJECT PATTERN DEMONSTRATION   ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Example 1: Bank Account
        System.out.println("1. BANK ACCOUNT MONITOR");
        System.out.println("───────────────────────");
        BankAccountMonitor account = new BankAccountMonitor(100.0);
        
        Thread depositor = new Thread(() -> {
            try {
                Thread.sleep(1000);
                account.deposit(200.0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Depositor");
        
        Thread withdrawer = new Thread(() -> {
            account.withdraw(250.0);
        }, "Withdrawer");
        
        withdrawer.start();
        depositor.start();
        
        withdrawer.join();
        depositor.join();
        
        System.out.println("Final balance: $" + account.getBalance());
        
        // Example 2: Producer-Consumer
        System.out.println("\n2. BOUNDED BUFFER MONITOR (Producer-Consumer)");
        System.out.println("──────────────────────────────────────────────");
        BoundedBufferMonitor<String> buffer = new BoundedBufferMonitor<>(3);
        
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buffer.produce("Item-" + i);
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");
        
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buffer.consume();
                    Thread.sleep(700);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");
        
        producer.start();
        consumer.start();
        
        producer.join();
        consumer.join();
        
        // Example 3: Counter with Threshold
        System.out.println("\n3. COUNTER MONITOR WITH THRESHOLD");
        System.out.println("──────────────────────────────────");
        CounterMonitor counter = new CounterMonitor(5);
        
        Thread waiter = new Thread(() -> {
            try {
                counter.waitForThreshold();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Waiter");
        
        waiter.start();
        Thread.sleep(500);
        
        for (int i = 1; i <= 5; i++) {
            final int num = i;
            new Thread(() -> {
                try {
                    Thread.sleep(num * 200);
                    counter.increment();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Incrementer-" + i).start();
        }
        
        waiter.join();
        
        // Example 4: Read-Write Monitor
        System.out.println("\n4. READ-WRITE MONITOR");
        System.out.println("─────────────────────");
        ReadWriteMonitor rwMonitor = new ReadWriteMonitor();
        
        // Start readers
        for (int i = 1; i <= 3; i++) {
            final int readerId = i;
            new Thread(() -> {
                try {
                    rwMonitor.readLock();
                    Thread.sleep(500);
                    rwMonitor.readUnlock();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Reader-" + readerId).start();
        }
        
        Thread.sleep(200);
        
        // Start writer
        Thread writer = new Thread(() -> {
            try {
                rwMonitor.writeLock();
                Thread.sleep(300);
                rwMonitor.writeUnlock();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Writer");
        
        writer.start();
        writer.join();
        
        // Example 5: Connection Pool
        System.out.println("\n5. CONNECTION POOL MONITOR");
        System.out.println("──────────────────────────");
        ConnectionPoolMonitor pool = new ConnectionPoolMonitor(2);
        
        List<Thread> clients = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            final int clientId = i;
            Thread client = new Thread(() -> {
                try {
                    String conn = pool.acquire();
                    Thread.sleep(1000);
                    pool.release(conn);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Client-" + clientId);
            clients.add(client);
            client.start();
        }
        
        for (Thread client : clients) {
            client.join();
        }
        
        System.out.println("\n✅ Monitor Object Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Automatic thread-safe access to shared objects");
        System.out.println("  • Condition-based waiting and notification");
        System.out.println("  • Encapsulated synchronization logic");
        System.out.println("  • Prevention of race conditions");
    }
}
