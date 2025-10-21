package cloud;

import java.util.*;

/**
 * Retry Pattern
 * Automatically retries failed operations with configurable strategies.
 */
public class RetryPattern {
    
    // Retry strategy interface
    interface RetryStrategy {
        long getNextDelay(int attemptNumber);
        boolean shouldRetry(int attemptNumber, Exception exception);
    }
    
    // Fixed delay retry
    static class FixedDelayRetry implements RetryStrategy {
        private final int maxAttempts;
        private final long delayMillis;
        
        public FixedDelayRetry(int maxAttempts, long delayMillis) {
            this.maxAttempts = maxAttempts;
            this.delayMillis = delayMillis;
        }
        
        @Override
        public long getNextDelay(int attemptNumber) {
            return delayMillis;
        }
        
        @Override
        public boolean shouldRetry(int attemptNumber, Exception exception) {
            return attemptNumber < maxAttempts;
        }
    }
    
    // Exponential backoff retry
    static class ExponentialBackoffRetry implements RetryStrategy {
        private final int maxAttempts;
        private final long initialDelayMillis;
        private final double multiplier;
        private final long maxDelayMillis;
        
        public ExponentialBackoffRetry(int maxAttempts, long initialDelayMillis, 
                                       double multiplier, long maxDelayMillis) {
            this.maxAttempts = maxAttempts;
            this.initialDelayMillis = initialDelayMillis;
            this.multiplier = multiplier;
            this.maxDelayMillis = maxDelayMillis;
        }
        
        @Override
        public long getNextDelay(int attemptNumber) {
            long delay = (long) (initialDelayMillis * Math.pow(multiplier, attemptNumber - 1));
            return Math.min(delay, maxDelayMillis);
        }
        
        @Override
        public boolean shouldRetry(int attemptNumber, Exception exception) {
            return attemptNumber < maxAttempts;
        }
    }
    
    // Retry executor
    static class RetryExecutor {
        private final RetryStrategy strategy;
        
        public RetryExecutor(RetryStrategy strategy) {
            this.strategy = strategy;
        }
        
        public <T> T execute(java.util.function.Supplier<T> operation) throws Exception {
            int attempt = 0;
            Exception lastException = null;
            
            while (true) {
                attempt++;
                
                try {
                    System.out.println("🔄 Attempt " + attempt);
                    T result = operation.get();
                    if (attempt > 1) {
                        System.out.println("✅ Success after " + attempt + " attempts");
                    }
                    return result;
                    
                } catch (Exception e) {
                    lastException = e;
                    System.out.println("❌ Attempt " + attempt + " failed: " + e.getMessage());
                    
                    if (!strategy.shouldRetry(attempt, e)) {
                        System.out.println("⛔ Max retries reached");
                        throw e;
                    }
                    
                    long delay = strategy.getNextDelay(attempt);
                    System.out.println("⏳ Waiting " + delay + "ms before retry...");
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
    }
    
    // Simulated unreliable service
    static class UnreliableService {
        private int callCount = 0;
        private final int failUntilAttempt;
        
        public UnreliableService(int failUntilAttempt) {
            this.failUntilAttempt = failUntilAttempt;
        }
        
        public String fetchData() {
            callCount++;
            
            if (callCount < failUntilAttempt) {
                throw new RuntimeException("Service temporarily unavailable");
            }
            
            return "Data successfully retrieved on attempt " + callCount;
        }
        
        public void reset() {
            callCount = 0;
        }
    }
    
    // HTTP client with retry
    static class ResilientHttpClient {
        private final RetryExecutor retryExecutor;
        
        public ResilientHttpClient(RetryStrategy strategy) {
            this.retryExecutor = new RetryExecutor(strategy);
        }
        
        public String get(String url) throws Exception {
            return retryExecutor.execute(() -> {
                System.out.println("  📡 GET " + url);
                
                // Simulate network issues
                if (Math.random() > 0.5) {
                    throw new RuntimeException("Connection timeout");
                }
                
                return "Response from " + url;
            });
        }
    }
    
    // Database connection with retry
    static class DatabaseConnection {
        private int connectionAttempts = 0;
        
        public String query(String sql) {
            connectionAttempts++;
            
            if (connectionAttempts < 3) {
                throw new RuntimeException("Database connection failed");
            }
            
            return "Query result for: " + sql;
        }
        
        public void reset() {
            connectionAttempts = 0;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Retry Pattern Demo ===\n");
        
        // 1. Fixed delay retry
        System.out.println("1. Fixed Delay Retry Strategy:");
        System.out.println("   (Same delay between retries)");
        System.out.println();
        
        RetryStrategy fixedDelay = new FixedDelayRetry(4, 1000);
        RetryExecutor fixedRetryExecutor = new RetryExecutor(fixedDelay);
        
        UnreliableService service1 = new UnreliableService(3);
        
        try {
            String result = fixedRetryExecutor.execute(() -> service1.fetchData());
            System.out.println("📦 Result: " + result);
        } catch (Exception e) {
            System.out.println("❌ Final failure: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. Exponential backoff retry
        System.out.println("\n2. Exponential Backoff Strategy:");
        System.out.println("   (Increasing delay: 500ms, 1000ms, 2000ms, 4000ms)");
        System.out.println();
        
        RetryStrategy exponentialBackoff = new ExponentialBackoffRetry(
            5,      // max attempts
            500,    // initial delay
            2.0,    // multiplier
            10000   // max delay
        );
        RetryExecutor expRetryExecutor = new RetryExecutor(exponentialBackoff);
        
        UnreliableService service2 = new UnreliableService(4);
        
        try {
            String result = expRetryExecutor.execute(() -> service2.fetchData());
            System.out.println("📦 Result: " + result);
        } catch (Exception e) {
            System.out.println("❌ Final failure: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // 3. Database connection retry
        System.out.println("\n3. Database Connection with Retry:");
        DatabaseConnection db = new DatabaseConnection();
        RetryStrategy dbRetry = new FixedDelayRetry(5, 500);
        RetryExecutor dbRetryExecutor = new RetryExecutor(dbRetry);
        
        try {
            String result = dbRetryExecutor.execute(() -> 
                db.query("SELECT * FROM users"));
            System.out.println("📦 Result: " + result);
        } catch (Exception e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // 4. HTTP client with exponential backoff
        System.out.println("\n4. HTTP Client with Exponential Backoff:");
        RetryStrategy httpRetry = new ExponentialBackoffRetry(3, 1000, 2.0, 8000);
        ResilientHttpClient httpClient = new ResilientHttpClient(httpRetry);
        
        try {
            String response = httpClient.get("https://api.example.com/data");
            System.out.println("📦 Response: " + response);
        } catch (Exception e) {
            System.out.println("❌ Failed after retries: " + e.getMessage());
        }
        
        System.out.println("\n--- Retry Strategies ---");
        System.out.println("1️⃣  Fixed Delay:");
        System.out.println("   • Constant delay between retries");
        System.out.println("   • Simple and predictable");
        System.out.println("   • Use for: Quick recovery scenarios");
        
        System.out.println("\n2️⃣  Exponential Backoff:");
        System.out.println("   • Increasing delay (exponential growth)");
        System.out.println("   • Reduces load on failing service");
        System.out.println("   • Use for: Network calls, APIs, databases");
        
        System.out.println("\n3️⃣  Exponential Backoff + Jitter:");
        System.out.println("   • Adds randomness to prevent thundering herd");
        System.out.println("   • Distributes retry attempts");
        System.out.println("   • Use for: High-concurrency systems");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Handles transient failures");
        System.out.println("✓ Improves reliability");
        System.out.println("✓ Automatic recovery");
        System.out.println("✓ Reduces manual intervention");
        System.out.println("✓ Better user experience");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Network requests");
        System.out.println("• Database connections");
        System.out.println("• External API calls");
        System.out.println("• Microservice communication");
        System.out.println("• Distributed transactions");
        
        System.out.println("\n--- Best Practices ---");
        System.out.println("⚡ Use with Circuit Breaker");
        System.out.println("⏱️  Set max retry limits");
        System.out.println("📊 Log retry attempts");
        System.out.println("🎯 Only retry transient errors");
        System.out.println("🔄 Consider idempotency");
    }
}
