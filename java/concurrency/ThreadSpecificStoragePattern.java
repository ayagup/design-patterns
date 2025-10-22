package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-Specific Storage Pattern
 * 
 * Intent: Allow multiple threads to use one logical instance of an object
 * while storing separate physical copies per thread to avoid synchronization.
 * 
 * Also Known As: Thread-Local Storage (TLS), Per-Thread Singleton
 * 
 * Motivation:
 * Some data should be isolated per thread to avoid:
 * - Expensive synchronization overhead
 * - Thread contention and blocking
 * - Data races and concurrency bugs
 * 
 * Each thread gets its own copy of the data.
 * 
 * Applicability:
 * - Database connections per thread
 * - Transaction contexts
 * - User authentication/session data
 * - Random number generators
 * - Date formatters (SimpleDateFormat is not thread-safe)
 * - Avoiding parameter passing through call chains
 */

/**
 * Example 1: Basic Thread-Local Storage
 * 
 * Uses Java's ThreadLocal for per-thread data isolation.
 * Each thread has its own instance.
 */
class BasicThreadLocalStorage {
    private static final ThreadLocal<UserContext> userContext = 
        ThreadLocal.withInitial(UserContext::new);
    
    private static final AtomicLong contextCreations = new AtomicLong(0);
    
    static class UserContext {
        private final long threadId;
        private String username;
        private Map<String, Object> attributes;
        
        UserContext() {
            this.threadId = Thread.currentThread().getId();
            this.attributes = new HashMap<>();
            contextCreations.incrementAndGet();
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
        
        public Object getAttribute(String key) {
            return attributes.get(key);
        }
        
        @Override
        public String toString() {
            return "UserContext{thread=" + threadId + 
                   ", username=" + username + 
                   ", attributes=" + attributes.size() + "}";
        }
    }
    
    public static void setUsername(String username) {
        userContext.get().setUsername(username);
    }
    
    public static String getUsername() {
        return userContext.get().getUsername();
    }
    
    public static void setAttribute(String key, Object value) {
        userContext.get().setAttribute(key, value);
    }
    
    public static Object getAttribute(String key) {
        return userContext.get().getAttribute(key);
    }
    
    public static UserContext getCurrentContext() {
        return userContext.get();
    }
    
    public static void clear() {
        userContext.remove();
    }
    
    public static long getContextCreations() {
        return contextCreations.get();
    }
}

/**
 * Example 2: Database Connection Pool with Thread-Local
 * 
 * Each thread maintains its own database connection.
 * Avoids connection contention and synchronization.
 */
class ThreadLocalConnectionPool {
    private final ThreadLocal<Connection> threadConnection;
    private final BlockingQueue<Connection> availableConnections;
    private final Set<Connection> allConnections;
    private final int maxConnections;
    private final AtomicLong connectionsCreated;
    
    static class Connection {
        private final String id;
        private final long threadId;
        private boolean inUse;
        private int queryCount;
        
        Connection(String id) {
            this.id = id;
            this.threadId = Thread.currentThread().getId();
            this.inUse = false;
            this.queryCount = 0;
        }
        
        public void execute(String query) {
            queryCount++;
            // Simulate query execution
            System.out.println("  [Thread " + Thread.currentThread().getId() + 
                             "] Connection " + id + " executing: " + query);
        }
        
        public String getId() { return id; }
        public long getThreadId() { return threadId; }
        public int getQueryCount() { return queryCount; }
        public boolean isInUse() { return inUse; }
        public void setInUse(boolean inUse) { this.inUse = inUse; }
    }
    
    public ThreadLocalConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
        this.availableConnections = new LinkedBlockingQueue<>();
        this.allConnections = ConcurrentHashMap.newKeySet();
        this.connectionsCreated = new AtomicLong(0);
        
        // Thread-local with lazy initialization
        this.threadConnection = ThreadLocal.withInitial(() -> {
            try {
                // Try to reuse available connection
                Connection conn = availableConnections.poll();
                
                if (conn == null && allConnections.size() < maxConnections) {
                    // Create new connection
                    conn = new Connection("conn-" + connectionsCreated.incrementAndGet());
                    allConnections.add(conn);
                    System.out.println("Created new connection: " + conn.getId() + 
                                     " for thread " + Thread.currentThread().getId());
                } else if (conn == null) {
                    // Wait for available connection
                    conn = availableConnections.take();
                }
                
                conn.setInUse(true);
                return conn;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to acquire connection", e);
            }
        });
        
        System.out.println("Thread-Local Connection Pool initialized (max: " + 
                         maxConnections + ")");
    }
    
    public void executeQuery(String query) {
        Connection conn = threadConnection.get();
        conn.execute(query);
    }
    
    public void releaseConnection() {
        Connection conn = threadConnection.get();
        if (conn != null) {
            conn.setInUse(false);
            availableConnections.offer(conn);
            threadConnection.remove();
        }
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConnections", allConnections.size());
        stats.put("availableConnections", availableConnections.size());
        stats.put("maxConnections", maxConnections);
        
        int totalQueries = allConnections.stream()
            .mapToInt(Connection::getQueryCount)
            .sum();
        stats.put("totalQueries", totalQueries);
        
        return stats;
    }
}

/**
 * Example 3: Thread-Local Transaction Context
 * 
 * Maintains transaction state per thread.
 * Supports nested transactions with save points.
 */
class ThreadLocalTransactionManager {
    private static final ThreadLocal<TransactionContext> transactionContext = 
        new ThreadLocal<>();
    
    private static final AtomicLong transactionIdGenerator = new AtomicLong(0);
    
    static class TransactionContext {
        private final long transactionId;
        private final long threadId;
        private final Deque<String> savePoints;
        private final List<String> operations;
        private TransactionStatus status;
        
        enum TransactionStatus {
            ACTIVE, COMMITTED, ROLLED_BACK
        }
        
        TransactionContext() {
            this.transactionId = transactionIdGenerator.incrementAndGet();
            this.threadId = Thread.currentThread().getId();
            this.savePoints = new ArrayDeque<>();
            this.operations = new ArrayList<>();
            this.status = TransactionStatus.ACTIVE;
        }
        
        public void addOperation(String operation) {
            operations.add(operation);
        }
        
        public void createSavePoint(String name) {
            savePoints.push(name);
        }
        
        public String rollbackToSavePoint() {
            return savePoints.isEmpty() ? null : savePoints.pop();
        }
        
        public long getTransactionId() { return transactionId; }
        public TransactionStatus getStatus() { return status; }
        public void setStatus(TransactionStatus status) { this.status = status; }
        public List<String> getOperations() { return operations; }
        
        @Override
        public String toString() {
            return "Transaction{id=" + transactionId + 
                   ", thread=" + threadId + 
                   ", operations=" + operations.size() + 
                   ", status=" + status + "}";
        }
    }
    
    public static void beginTransaction() {
        if (transactionContext.get() != null) {
            throw new IllegalStateException("Transaction already active");
        }
        
        TransactionContext ctx = new TransactionContext();
        transactionContext.set(ctx);
        System.out.println("  [Thread " + Thread.currentThread().getId() + 
                         "] Transaction started: " + ctx.getTransactionId());
    }
    
    public static void executeOperation(String operation) {
        TransactionContext ctx = transactionContext.get();
        if (ctx == null) {
            throw new IllegalStateException("No active transaction");
        }
        
        ctx.addOperation(operation);
        System.out.println("  [Thread " + Thread.currentThread().getId() + 
                         "] Operation: " + operation);
    }
    
    public static void createSavePoint(String name) {
        TransactionContext ctx = transactionContext.get();
        if (ctx == null) {
            throw new IllegalStateException("No active transaction");
        }
        
        ctx.createSavePoint(name);
        System.out.println("  [Thread " + Thread.currentThread().getId() + 
                         "] Save point created: " + name);
    }
    
    public static void commit() {
        TransactionContext ctx = transactionContext.get();
        if (ctx == null) {
            throw new IllegalStateException("No active transaction");
        }
        
        ctx.setStatus(TransactionContext.TransactionStatus.COMMITTED);
        System.out.println("  [Thread " + Thread.currentThread().getId() + 
                         "] Transaction committed: " + ctx.getTransactionId() + 
                         " (" + ctx.getOperations().size() + " operations)");
        transactionContext.remove();
    }
    
    public static void rollback() {
        TransactionContext ctx = transactionContext.get();
        if (ctx == null) {
            throw new IllegalStateException("No active transaction");
        }
        
        ctx.setStatus(TransactionContext.TransactionStatus.ROLLED_BACK);
        System.out.println("  [Thread " + Thread.currentThread().getId() + 
                         "] Transaction rolled back: " + ctx.getTransactionId());
        transactionContext.remove();
    }
    
    public static TransactionContext getCurrentTransaction() {
        return transactionContext.get();
    }
}

/**
 * Example 4: Thread-Local Random Number Generator
 * 
 * Each thread has its own Random instance to avoid contention.
 * Much faster than using synchronized Random or ThreadLocalRandom.
 */
class ThreadLocalRandom {
    private static final ThreadLocal<Random> random = 
        ThreadLocal.withInitial(() -> new Random(System.nanoTime()));
    
    private static final AtomicLong numbersGenerated = new AtomicLong(0);
    
    public static int nextInt(int bound) {
        numbersGenerated.incrementAndGet();
        return random.get().nextInt(bound);
    }
    
    public static double nextDouble() {
        numbersGenerated.incrementAndGet();
        return random.get().nextDouble();
    }
    
    public static long nextLong() {
        numbersGenerated.incrementAndGet();
        return random.get().nextLong();
    }
    
    public static boolean nextBoolean() {
        numbersGenerated.incrementAndGet();
        return random.get().nextBoolean();
    }
    
    public static long getNumbersGenerated() {
        return numbersGenerated.get();
    }
}

/**
 * Example 5: Thread-Local Request Context for Web Applications
 * 
 * Stores request-scoped data without passing through method parameters.
 * Common in web frameworks for tracking request ID, user, locale, etc.
 */
class ThreadLocalRequestContext {
    private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();
    
    static class RequestContext {
        private final String requestId;
        private final long startTime;
        private String userId;
        private Locale locale;
        private final Map<String, Object> attributes;
        private final List<String> breadcrumbs;
        
        RequestContext(String requestId) {
            this.requestId = requestId;
            this.startTime = System.currentTimeMillis();
            this.attributes = new HashMap<>();
            this.breadcrumbs = new ArrayList<>();
            this.locale = Locale.getDefault();
        }
        
        public String getRequestId() { return requestId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserId() { return userId; }
        public void setLocale(Locale locale) { this.locale = locale; }
        public Locale getLocale() { return locale; }
        
        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
        
        public Object getAttribute(String key) {
            return attributes.get(key);
        }
        
        public void addBreadcrumb(String component) {
            breadcrumbs.add(component);
        }
        
        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }
        
        @Override
        public String toString() {
            return "RequestContext{id=" + requestId + 
                   ", userId=" + userId + 
                   ", locale=" + locale + 
                   ", duration=" + getDuration() + "ms" +
                   ", breadcrumbs=" + breadcrumbs + "}";
        }
    }
    
    public static void initializeRequest(String requestId) {
        context.set(new RequestContext(requestId));
    }
    
    public static void setUserId(String userId) {
        RequestContext ctx = context.get();
        if (ctx != null) {
            ctx.setUserId(userId);
        }
    }
    
    public static String getUserId() {
        RequestContext ctx = context.get();
        return ctx != null ? ctx.getUserId() : null;
    }
    
    public static void setLocale(Locale locale) {
        RequestContext ctx = context.get();
        if (ctx != null) {
            ctx.setLocale(locale);
        }
    }
    
    public static Locale getLocale() {
        RequestContext ctx = context.get();
        return ctx != null ? ctx.getLocale() : Locale.getDefault();
    }
    
    public static void setAttribute(String key, Object value) {
        RequestContext ctx = context.get();
        if (ctx != null) {
            ctx.setAttribute(key, value);
        }
    }
    
    public static Object getAttribute(String key) {
        RequestContext ctx = context.get();
        return ctx != null ? ctx.getAttribute(key) : null;
    }
    
    public static void addBreadcrumb(String component) {
        RequestContext ctx = context.get();
        if (ctx != null) {
            ctx.addBreadcrumb(component);
        }
    }
    
    public static RequestContext getCurrentRequest() {
        return context.get();
    }
    
    public static void clear() {
        RequestContext ctx = context.get();
        if (ctx != null) {
            System.out.println("  Request completed: " + ctx);
        }
        context.remove();
    }
}

/**
 * Demonstration of the Thread-Specific Storage Pattern
 */
public class ThreadSpecificStoragePattern {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Thread-Specific Storage Pattern Demo ===\n");
        
        // Example 1: Basic Thread-Local Storage
        System.out.println("1. Basic Thread-Local Storage (user context):");
        
        ExecutorService executor1 = Executors.newFixedThreadPool(3);
        CountDownLatch latch1 = new CountDownLatch(3);
        
        for (int i = 1; i <= 3; i++) {
            final String username = "user" + i;
            executor1.submit(() -> {
                BasicThreadLocalStorage.setUsername(username);
                BasicThreadLocalStorage.setAttribute("role", "developer");
                
                System.out.println("  " + BasicThreadLocalStorage.getCurrentContext());
                
                latch1.countDown();
            });
        }
        
        latch1.await();
        System.out.println("Total contexts created: " + 
            BasicThreadLocalStorage.getContextCreations());
        executor1.shutdown();
        
        // Example 2: Database Connection Pool
        System.out.println("\n2. Thread-Local Connection Pool:");
        ThreadLocalConnectionPool pool = new ThreadLocalConnectionPool(2);
        
        ExecutorService executor2 = Executors.newFixedThreadPool(3);
        CountDownLatch latch2 = new CountDownLatch(3);
        
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            executor2.submit(() -> {
                pool.executeQuery("SELECT * FROM users WHERE id = " + taskId);
                pool.executeQuery("UPDATE users SET last_login = NOW() WHERE id = " + taskId);
                pool.releaseConnection();
                latch2.countDown();
            });
        }
        
        latch2.await();
        System.out.println("Pool statistics: " + pool.getStatistics());
        executor2.shutdown();
        
        // Example 3: Transaction Management
        System.out.println("\n3. Thread-Local Transaction Context:");
        
        ExecutorService executor3 = Executors.newFixedThreadPool(2);
        CountDownLatch latch3 = new CountDownLatch(2);
        
        // Transaction 1: Successful commit
        executor3.submit(() -> {
            ThreadLocalTransactionManager.beginTransaction();
            ThreadLocalTransactionManager.executeOperation("INSERT INTO accounts VALUES (1, 100)");
            ThreadLocalTransactionManager.executeOperation("INSERT INTO accounts VALUES (2, 200)");
            ThreadLocalTransactionManager.createSavePoint("sp1");
            ThreadLocalTransactionManager.executeOperation("UPDATE accounts SET balance = 150 WHERE id = 1");
            ThreadLocalTransactionManager.commit();
            latch3.countDown();
        });
        
        // Transaction 2: Rollback
        executor3.submit(() -> {
            ThreadLocalTransactionManager.beginTransaction();
            ThreadLocalTransactionManager.executeOperation("DELETE FROM accounts WHERE id = 3");
            ThreadLocalTransactionManager.rollback();
            latch3.countDown();
        });
        
        latch3.await();
        executor3.shutdown();
        
        // Example 4: Thread-Local Random
        System.out.println("\n4. Thread-Local Random Number Generator:");
        
        ExecutorService executor4 = Executors.newFixedThreadPool(4);
        CountDownLatch latch4 = new CountDownLatch(4);
        
        for (int i = 1; i <= 4; i++) {
            executor4.submit(() -> {
                int random1 = ThreadLocalRandom.nextInt(100);
                double random2 = ThreadLocalRandom.nextDouble();
                
                System.out.println("  [Thread " + Thread.currentThread().getId() + 
                                 "] Random numbers: " + random1 + ", " + 
                                 String.format("%.4f", random2));
                latch4.countDown();
            });
        }
        
        latch4.await();
        System.out.println("Total numbers generated: " + 
            ThreadLocalRandom.getNumbersGenerated());
        executor4.shutdown();
        
        // Example 5: Request Context
        System.out.println("\n5. Thread-Local Request Context (web framework):");
        
        ExecutorService executor5 = Executors.newFixedThreadPool(2);
        CountDownLatch latch5 = new CountDownLatch(2);
        
        // Request 1
        executor5.submit(() -> {
            ThreadLocalRequestContext.initializeRequest("req-001");
            ThreadLocalRequestContext.setUserId("alice");
            ThreadLocalRequestContext.setLocale(Locale.US);
            ThreadLocalRequestContext.addBreadcrumb("LoginController");
            
            // Simulate processing through layers
            processRequest();
            
            ThreadLocalRequestContext.clear();
            latch5.countDown();
        });
        
        // Request 2
        executor5.submit(() -> {
            ThreadLocalRequestContext.initializeRequest("req-002");
            ThreadLocalRequestContext.setUserId("bob");
            ThreadLocalRequestContext.setLocale(Locale.FRANCE);
            ThreadLocalRequestContext.addBreadcrumb("ProfileController");
            
            processRequest();
            
            ThreadLocalRequestContext.clear();
            latch5.countDown();
        });
        
        latch5.await();
        executor5.shutdown();
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Avoids expensive synchronization");
        System.out.println("✓ Each thread has isolated data");
        System.out.println("✓ No thread contention or blocking");
        System.out.println("✓ Simplifies code (no parameter passing)");
        System.out.println("✓ Used in web frameworks, ORMs, logging");
        System.out.println("✓ Better performance than shared mutable state");
        
        System.out.println("\n=== Pattern Caveats ===");
        System.out.println("⚠ Must call remove() to prevent memory leaks (thread pools)");
        System.out.println("⚠ Not suitable for shared state across threads");
        System.out.println("⚠ Can increase memory usage");
    }
    
    // Helper method for request processing demo
    private static void processRequest() {
        ThreadLocalRequestContext.addBreadcrumb("ServiceLayer");
        
        String userId = ThreadLocalRequestContext.getUserId();
        Locale locale = ThreadLocalRequestContext.getLocale();
        
        System.out.println("  Processing request for user: " + userId + 
                         " (locale: " + locale + ")");
        
        ThreadLocalRequestContext.addBreadcrumb("RepositoryLayer");
    }
}
