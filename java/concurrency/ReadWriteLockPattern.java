package concurrency;

import java.util.concurrent.locks.*;

/**
 * Read-Write Lock Pattern
 * Allows concurrent read access while ensuring exclusive write access.
 */
public class ReadWriteLockPattern {
    
    // Shared cache with read-write lock
    static class Cache {
        private final java.util.Map<String, String> data = new java.util.HashMap<>();
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final Lock readLock = lock.readLock();
        private final Lock writeLock = lock.writeLock();
        
        public String get(String key) {
            readLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + 
                                 " reading key: " + key);
                simulateWork(100);
                return data.get(key);
            } finally {
                readLock.unlock();
            }
        }
        
        public void put(String key, String value) {
            writeLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + 
                                 " writing key: " + key + " = " + value);
                simulateWork(300);
                data.put(key, value);
            } finally {
                writeLock.unlock();
            }
        }
        
        public int size() {
            readLock.lock();
            try {
                return data.size();
            } finally {
                readLock.unlock();
            }
        }
        
        private void simulateWork(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // Document with versioning
    static class Document {
        private String content;
        private int version = 0;
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        
        public Document(String initialContent) {
            this.content = initialContent;
        }
        
        public String read() {
            rwLock.readLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + 
                                 " reading document (v" + version + ")");
                Thread.sleep(200);
                return content;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } finally {
                rwLock.readLock().unlock();
            }
        }
        
        public void write(String newContent) {
            rwLock.writeLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + 
                                 " writing document...");
                Thread.sleep(500);
                content = newContent;
                version++;
                System.out.println(Thread.currentThread().getName() + 
                                 " document updated to v" + version);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.writeLock().unlock();
            }
        }
        
        public int getVersion() {
            rwLock.readLock().lock();
            try {
                return version;
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }
    
    // Statistics counter with read-write lock
    static class Statistics {
        private long totalRequests = 0;
        private long successfulRequests = 0;
        private long failedRequests = 0;
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        
        public void recordSuccess() {
            lock.writeLock().lock();
            try {
                totalRequests++;
                successfulRequests++;
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        public void recordFailure() {
            lock.writeLock().lock();
            try {
                totalRequests++;
                failedRequests++;
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        public long getTotalRequests() {
            lock.readLock().lock();
            try {
                return totalRequests;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        public double getSuccessRate() {
            lock.readLock().lock();
            try {
                if (totalRequests == 0) return 0.0;
                return (double) successfulRequests / totalRequests * 100;
            } finally {
                lock.readLock().unlock();
            }
        }
        
        public void printStats() {
            lock.readLock().lock();
            try {
                System.out.println("\n📊 Statistics:");
                System.out.println("  Total Requests: " + totalRequests);
                System.out.println("  Successful: " + successfulRequests);
                System.out.println("  Failed: " + failedRequests);
                System.out.printf("  Success Rate: %.2f%%%n", getSuccessRate());
            } finally {
                lock.readLock().unlock();
            }
        }
    }
    
    // Comparison: Without Read-Write Lock
    static class NaiveCache {
        private final java.util.Map<String, String> data = new java.util.HashMap<>();
        
        public synchronized String get(String key) {
            System.out.println(Thread.currentThread().getName() + 
                             " (synchronized) reading: " + key);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return data.get(key);
        }
        
        public synchronized void put(String key, String value) {
            System.out.println(Thread.currentThread().getName() + 
                             " (synchronized) writing: " + key);
            data.put(key, value);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Read-Write Lock Pattern Demo ===\n");
        
        // 1. Concurrent cache access
        System.out.println("1. Cache with Read-Write Lock:");
        Cache cache = new Cache();
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");
        
        // Multiple readers
        Thread[] readers = new Thread[5];
        for (int i = 0; i < 5; i++) {
            readers[i] = new Thread(() -> {
                String value = cache.get("user:1");
                System.out.println(Thread.currentThread().getName() + 
                                 " got: " + value);
            }, "Reader-" + i);
        }
        
        // Single writer
        Thread writer = new Thread(() -> {
            cache.put("user:3", "Charlie");
        }, "Writer");
        
        // Start all threads
        for (Thread reader : readers) reader.start();
        Thread.sleep(50);
        writer.start();
        
        for (Thread reader : readers) reader.join();
        writer.join();
        
        System.out.println("Cache size: " + cache.size());
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 2. Document editing
        System.out.println("2. Concurrent Document Access:");
        Document doc = new Document("Initial content");
        
        Thread[] docReaders = new Thread[3];
        for (int i = 0; i < 3; i++) {
            docReaders[i] = new Thread(() -> {
                String content = doc.read();
                System.out.println(Thread.currentThread().getName() + 
                                 " read: " + content);
            }, "DocReader-" + i);
        }
        
        Thread docWriter = new Thread(() -> {
            doc.write("Updated content by " + Thread.currentThread().getName());
        }, "DocWriter");
        
        for (Thread reader : docReaders) reader.start();
        Thread.sleep(100);
        docWriter.start();
        
        for (Thread reader : docReaders) reader.join();
        docWriter.join();
        
        System.out.println("Final version: " + doc.getVersion());
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 3. Statistics tracking
        System.out.println("3. Concurrent Statistics:");
        Statistics stats = new Statistics();
        
        Thread[] statThreads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            statThreads[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    if (Math.random() > 0.3) {
                        stats.recordSuccess();
                    } else {
                        stats.recordFailure();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "StatThread-" + threadId);
        }
        
        for (Thread thread : statThreads) thread.start();
        for (Thread thread : statThreads) thread.join();
        
        stats.printStats();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 4. Performance comparison
        System.out.println("4. Performance Comparison:");
        System.out.println("\n📈 Read-Write Lock Benefits:");
        System.out.println("  • Multiple readers can access simultaneously");
        System.out.println("  • Writers get exclusive access");
        System.out.println("  • Better throughput for read-heavy workloads");
        
        System.out.println("\n📉 Simple synchronized keyword:");
        System.out.println("  • All access is serialized (one at a time)");
        System.out.println("  • Readers block other readers unnecessarily");
        System.out.println("  • Poor performance for concurrent reads");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Concurrent read access");
        System.out.println("✓ Exclusive write access");
        System.out.println("✓ Better scalability for read-heavy loads");
        System.out.println("✓ Fairness policies available");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Caching systems");
        System.out.println("• Configuration management");
        System.out.println("• Document editing");
        System.out.println("• Statistics/metrics collection");
        System.out.println("• Read-heavy data structures");
    }
}
