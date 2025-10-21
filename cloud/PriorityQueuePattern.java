package cloud;

import java.util.*;
import java.util.concurrent.*;

/**
 * Priority Queue Pattern
 * 
 * Intent: Prioritize requests sent to services so that requests with higher
 * priority are received and processed more quickly than requests with lower priority.
 * 
 * Also Known As:
 * - Priority-Based Processing
 * - Weighted Queue
 * - QoS (Quality of Service) Pattern
 * 
 * Motivation:
 * Not all requests are equal in importance:
 * - Premium customers vs free tier users
 * - Critical operations vs background tasks
 * - Real-time updates vs batch processing
 * - Interactive requests vs analytics
 * 
 * Processing all requests in FIFO order can lead to:
 * - Important requests waiting behind low-priority tasks
 * - Poor user experience for premium customers
 * - SLA violations for critical operations
 * 
 * Applicability:
 * - Multi-tenant systems with different service tiers
 * - Mixed workloads (interactive + batch)
 * - Limited processing resources
 * - SLA requirements for different request types
 * - Cost optimization (prioritize revenue-generating requests)
 * 
 * Benefits:
 * - Better resource utilization
 * - Improved user experience for important customers
 * - SLA compliance
 * - Fair resource allocation
 * - Prevents starvation of critical requests
 * 
 * Trade-offs:
 * - Low-priority requests may be delayed
 * - Additional complexity in routing
 * - Need to prevent starvation
 * - Priority assignment logic can be complex
 */

// Request with priority
class PriorityRequest implements Comparable<PriorityRequest> {
    private final String id;
    private final String data;
    private final int priority;  // Higher number = higher priority
    private final long timestamp;
    private final String customerTier;
    
    public PriorityRequest(String id, String data, int priority, String customerTier) {
        this.id = id;
        this.data = data;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
        this.customerTier = customerTier;
    }
    
    public String getId() { return id; }
    public String getData() { return data; }
    public int getPriority() { return priority; }
    public long getTimestamp() { return timestamp; }
    public String getCustomerTier() { return customerTier; }
    
    @Override
    public int compareTo(PriorityRequest other) {
        // Higher priority first
        int priorityCompare = Integer.compare(other.priority, this.priority);
        if (priorityCompare != 0) return priorityCompare;
        
        // If same priority, FIFO (earlier timestamp first)
        return Long.compare(this.timestamp, other.timestamp);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] Priority=%d, Tier=%s, Data='%s'", 
            id, priority, customerTier, data);
    }
}

// Example 1: Basic Priority Queue
class BasicPriorityQueueService {
    private final PriorityBlockingQueue<PriorityRequest> queue;
    private final ExecutorService executor;
    private volatile boolean running = true;
    
    public BasicPriorityQueueService(int workerCount) {
        this.queue = new PriorityBlockingQueue<>();
        this.executor = Executors.newFixedThreadPool(workerCount);
        
        // Start workers
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i + 1;
            executor.submit(() -> processRequests(workerId));
        }
    }
    
    public void submitRequest(PriorityRequest request) {
        queue.offer(request);
        System.out.println("Submitted: " + request);
    }
    
    private void processRequests(int workerId) {
        while (running) {
            try {
                PriorityRequest request = queue.poll(100, TimeUnit.MILLISECONDS);
                if (request != null) {
                    processRequest(workerId, request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void processRequest(int workerId, PriorityRequest request) {
        long waitTime = System.currentTimeMillis() - request.getTimestamp();
        System.out.println(String.format("Worker-%d processing: %s (waited %dms)", 
            workerId, request, waitTime));
        
        // Simulate processing
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void shutdown() {
        running = false;
        executor.shutdown();
    }
    
    public int getQueueSize() {
        return queue.size();
    }
}

// Example 2: Multi-Level Priority Queue
// Separate queues for different priority levels
class MultiLevelPriorityQueue {
    private final Map<Integer, BlockingQueue<PriorityRequest>> levelQueues;
    private final List<Integer> priorityLevels;
    private final ExecutorService executor;
    private volatile boolean running = true;
    
    public MultiLevelPriorityQueue(int workerCount) {
        this.levelQueues = new ConcurrentHashMap<>();
        this.priorityLevels = Arrays.asList(10, 5, 1);  // Critical, High, Normal
        
        // Create queue for each level
        for (int level : priorityLevels) {
            levelQueues.put(level, new LinkedBlockingQueue<>());
        }
        
        this.executor = Executors.newFixedThreadPool(workerCount);
        
        // Start workers
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i + 1;
            executor.submit(() -> processRequests(workerId));
        }
    }
    
    public void submitRequest(PriorityRequest request) {
        int level = findPriorityLevel(request.getPriority());
        levelQueues.get(level).offer(request);
        System.out.println(String.format("Submitted to level %d: %s", level, request));
    }
    
    private int findPriorityLevel(int priority) {
        // Map priority to closest level
        for (int level : priorityLevels) {
            if (priority >= level) return level;
        }
        return priorityLevels.get(priorityLevels.size() - 1);
    }
    
    private void processRequests(int workerId) {
        while (running) {
            PriorityRequest request = null;
            
            // Check queues in priority order
            for (int level : priorityLevels) {
                request = levelQueues.get(level).poll();
                if (request != null) {
                    processRequest(workerId, request, level);
                    break;
                }
            }
            
            if (request == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    private void processRequest(int workerId, PriorityRequest request, int level) {
        long waitTime = System.currentTimeMillis() - request.getTimestamp();
        System.out.println(String.format("Worker-%d [Level %d] processing: %s (waited %dms)", 
            workerId, level, request, waitTime));
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void printQueueStatus() {
        System.out.println("\nQueue Status:");
        for (int level : priorityLevels) {
            System.out.println(String.format("  Level %d: %d requests", 
                level, levelQueues.get(level).size()));
        }
    }
    
    public void shutdown() {
        running = false;
        executor.shutdown();
    }
}

// Example 3: Dynamic Priority Assignment
// Priority based on customer tier and request type
class DynamicPriorityQueueService {
    private final PriorityBlockingQueue<PriorityRequest> queue;
    private final Map<String, Integer> tierPriorities;
    private final Map<String, Integer> requestTypePriorities;
    
    public DynamicPriorityQueueService() {
        this.queue = new PriorityBlockingQueue<>();
        
        // Tier priorities
        this.tierPriorities = new HashMap<>();
        tierPriorities.put("ENTERPRISE", 100);
        tierPriorities.put("PREMIUM", 50);
        tierPriorities.put("STANDARD", 20);
        tierPriorities.put("FREE", 5);
        
        // Request type priorities
        this.requestTypePriorities = new HashMap<>();
        requestTypePriorities.put("CRITICAL", 100);
        requestTypePriorities.put("URGENT", 50);
        requestTypePriorities.put("NORMAL", 20);
        requestTypePriorities.put("BACKGROUND", 5);
    }
    
    public void submitRequest(String id, String data, String tier, String requestType) {
        int priority = calculatePriority(tier, requestType);
        PriorityRequest request = new PriorityRequest(id, data, priority, tier);
        queue.offer(request);
        
        System.out.println(String.format("Submitted [%s + %s = Priority %d]: %s", 
            tier, requestType, priority, request));
    }
    
    private int calculatePriority(String tier, String requestType) {
        int tierPriority = tierPriorities.getOrDefault(tier, 1);
        int typePriority = requestTypePriorities.getOrDefault(requestType, 1);
        
        // Combine: tier weight 60%, type weight 40%
        return (int) (tierPriority * 0.6 + typePriority * 0.4);
    }
    
    public PriorityRequest getNextRequest() {
        return queue.poll();
    }
    
    public void processAll() {
        System.out.println("\nProcessing all requests in priority order:");
        PriorityRequest request;
        int count = 1;
        while ((request = queue.poll()) != null) {
            System.out.println(String.format("  %d. Processing: %s", count++, request));
        }
    }
}

// Example 4: Priority Queue with Aging
// Prevents starvation by increasing priority over time
class AgingPriorityQueue {
    private final PriorityBlockingQueue<AgingRequest> queue;
    private final ScheduledExecutorService ageScheduler;
    private final long agingIntervalMs = 1000;  // Age every second
    private final int agingIncrement = 1;
    
    static class AgingRequest implements Comparable<AgingRequest> {
        private final PriorityRequest baseRequest;
        private int currentPriority;
        private int ageBonus;
        
        public AgingRequest(PriorityRequest request) {
            this.baseRequest = request;
            this.currentPriority = request.getPriority();
            this.ageBonus = 0;
        }
        
        public void age(int increment) {
            ageBonus += increment;
            currentPriority = baseRequest.getPriority() + ageBonus;
        }
        
        public PriorityRequest getBaseRequest() { return baseRequest; }
        public int getCurrentPriority() { return currentPriority; }
        public int getAgeBonus() { return ageBonus; }
        
        @Override
        public int compareTo(AgingRequest other) {
            int priorityCompare = Integer.compare(other.currentPriority, this.currentPriority);
            if (priorityCompare != 0) return priorityCompare;
            return Long.compare(this.baseRequest.getTimestamp(), other.baseRequest.getTimestamp());
        }
        
        @Override
        public String toString() {
            return String.format("%s (current priority: %d, age bonus: +%d)", 
                baseRequest, currentPriority, ageBonus);
        }
    }
    
    public AgingPriorityQueue() {
        this.queue = new PriorityBlockingQueue<>();
        this.ageScheduler = Executors.newScheduledThreadPool(1);
        
        // Periodically age all requests
        ageScheduler.scheduleAtFixedRate(this::ageAllRequests, 
            agingIntervalMs, agingIntervalMs, TimeUnit.MILLISECONDS);
    }
    
    public void submitRequest(PriorityRequest request) {
        AgingRequest agingRequest = new AgingRequest(request);
        queue.offer(agingRequest);
        System.out.println("Submitted with aging: " + request);
    }
    
    private void ageAllRequests() {
        List<AgingRequest> requests = new ArrayList<>();
        queue.drainTo(requests);
        
        for (AgingRequest request : requests) {
            request.age(agingIncrement);
        }
        
        queue.addAll(requests);
    }
    
    public AgingRequest getNextRequest() {
        return queue.poll();
    }
    
    public void processNext() {
        AgingRequest request = queue.poll();
        if (request != null) {
            long waitTime = System.currentTimeMillis() - request.getBaseRequest().getTimestamp();
            System.out.println(String.format("Processing: %s (waited %dms)", request, waitTime));
        }
    }
    
    public void shutdown() {
        ageScheduler.shutdown();
    }
    
    public int getQueueSize() {
        return queue.size();
    }
}

// Example 5: Fair Priority Queue with Quotas
// Ensures each tier gets minimum processing quota
class FairPriorityQueue {
    private final Map<String, BlockingQueue<PriorityRequest>> tierQueues;
    private final Map<String, Integer> tierQuotas;  // Requests per cycle
    private final Map<String, Integer> processedCounts;
    private final ExecutorService executor;
    private volatile boolean running = true;
    
    public FairPriorityQueue(int workerCount) {
        this.tierQueues = new ConcurrentHashMap<>();
        this.tierQuotas = new HashMap<>();
        this.processedCounts = new ConcurrentHashMap<>();
        
        // Configure tiers with quotas (requests per cycle)
        String[] tiers = {"ENTERPRISE", "PREMIUM", "STANDARD", "FREE"};
        int[] quotas = {10, 5, 3, 1};  // Enterprise gets 10, Free gets 1
        
        for (int i = 0; i < tiers.length; i++) {
            tierQueues.put(tiers[i], new LinkedBlockingQueue<>());
            tierQuotas.put(tiers[i], quotas[i]);
            processedCounts.put(tiers[i], 0);
        }
        
        this.executor = Executors.newFixedThreadPool(workerCount);
        
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i + 1;
            executor.submit(() -> processRequests(workerId));
        }
    }
    
    public void submitRequest(PriorityRequest request) {
        String tier = request.getCustomerTier();
        tierQueues.get(tier).offer(request);
        System.out.println(String.format("Submitted to %s queue: %s", tier, request));
    }
    
    private void processRequests(int workerId) {
        while (running) {
            boolean processed = false;
            
            // Process each tier according to quota
            for (Map.Entry<String, Integer> entry : tierQuotas.entrySet()) {
                String tier = entry.getKey();
                int quota = entry.getValue();
                int processedThisCycle = processedCounts.get(tier);
                
                if (processedThisCycle < quota) {
                    PriorityRequest request = tierQueues.get(tier).poll();
                    if (request != null) {
                        processRequest(workerId, request, tier);
                        processedCounts.put(tier, processedThisCycle + 1);
                        processed = true;
                        break;
                    }
                }
            }
            
            // Reset counts when all quotas filled or no requests
            if (!processed) {
                boolean allQuotasFilled = true;
                for (Map.Entry<String, Integer> entry : tierQuotas.entrySet()) {
                    if (processedCounts.get(entry.getKey()) < entry.getValue() && 
                        !tierQueues.get(entry.getKey()).isEmpty()) {
                        allQuotasFilled = false;
                        break;
                    }
                }
                
                if (allQuotasFilled) {
                    processedCounts.replaceAll((k, v) -> 0);
                }
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    
    private void processRequest(int workerId, PriorityRequest request, String tier) {
        long waitTime = System.currentTimeMillis() - request.getTimestamp();
        System.out.println(String.format("Worker-%d [%s] processing: %s (waited %dms)", 
            workerId, tier, request, waitTime));
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void printQueueStatus() {
        System.out.println("\nFair Queue Status:");
        for (String tier : tierQuotas.keySet()) {
            System.out.println(String.format("  %s: %d pending (quota: %d/cycle, processed: %d)", 
                tier, tierQueues.get(tier).size(), tierQuotas.get(tier), processedCounts.get(tier)));
        }
    }
    
    public void shutdown() {
        running = false;
        executor.shutdown();
    }
}

// Demo
public class PriorityQueuePattern {
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicPriorityQueue();
        demonstrateMultiLevelQueue();
        demonstrateDynamicPriority();
        demonstrateAgingQueue();
        demonstrateFairQueue();
    }
    
    private static void demonstrateBasicPriorityQueue() throws InterruptedException {
        System.out.println("=== Basic Priority Queue ===\n");
        
        BasicPriorityQueueService service = new BasicPriorityQueueService(2);
        
        // Submit requests with different priorities
        service.submitRequest(new PriorityRequest("R1", "Low priority task", 1, "FREE"));
        service.submitRequest(new PriorityRequest("R2", "High priority task", 10, "ENTERPRISE"));
        service.submitRequest(new PriorityRequest("R3", "Medium priority task", 5, "PREMIUM"));
        service.submitRequest(new PriorityRequest("R4", "Another high priority", 10, "ENTERPRISE"));
        service.submitRequest(new PriorityRequest("R5", "Normal task", 3, "STANDARD"));
        
        Thread.sleep(1000);
        service.shutdown();
    }
    
    private static void demonstrateMultiLevelQueue() throws InterruptedException {
        System.out.println("\n\n=== Multi-Level Priority Queue ===\n");
        
        MultiLevelPriorityQueue queue = new MultiLevelPriorityQueue(2);
        
        queue.submitRequest(new PriorityRequest("R1", "Normal", 1, "STANDARD"));
        queue.submitRequest(new PriorityRequest("R2", "Critical", 10, "ENTERPRISE"));
        queue.submitRequest(new PriorityRequest("R3", "High", 5, "PREMIUM"));
        queue.submitRequest(new PriorityRequest("R4", "Normal", 2, "STANDARD"));
        
        Thread.sleep(500);
        queue.printQueueStatus();
        Thread.sleep(500);
        queue.shutdown();
    }
    
    private static void demonstrateDynamicPriority() {
        System.out.println("\n\n=== Dynamic Priority Assignment ===\n");
        
        DynamicPriorityQueueService service = new DynamicPriorityQueueService();
        
        service.submitRequest("R1", "Task 1", "FREE", "NORMAL");
        service.submitRequest("R2", "Task 2", "ENTERPRISE", "CRITICAL");
        service.submitRequest("R3", "Task 3", "PREMIUM", "URGENT");
        service.submitRequest("R4", "Task 4", "STANDARD", "BACKGROUND");
        service.submitRequest("R5", "Task 5", "ENTERPRISE", "NORMAL");
        
        service.processAll();
    }
    
    private static void demonstrateAgingQueue() throws InterruptedException {
        System.out.println("\n\n=== Priority Queue with Aging ===\n");
        
        AgingPriorityQueue queue = new AgingPriorityQueue();
        
        // Submit low priority request first
        queue.submitRequest(new PriorityRequest("R1", "Old low priority", 1, "FREE"));
        
        Thread.sleep(1500);  // Wait for aging
        
        // Submit high priority requests
        queue.submitRequest(new PriorityRequest("R2", "New high priority", 10, "ENTERPRISE"));
        queue.submitRequest(new PriorityRequest("R3", "New medium priority", 5, "PREMIUM"));
        
        System.out.println("\nProcessing (notice R1 got age boost):");
        for (int i = 0; i < 3; i++) {
            queue.processNext();
            Thread.sleep(100);
        }
        
        queue.shutdown();
    }
    
    private static void demonstrateFairQueue() throws InterruptedException {
        System.out.println("\n\n=== Fair Priority Queue with Quotas ===\n");
        
        FairPriorityQueue queue = new FairPriorityQueue(3);
        
        // Submit many requests from different tiers
        for (int i = 1; i <= 5; i++) {
            queue.submitRequest(new PriorityRequest("E" + i, "Enterprise task", 10, "ENTERPRISE"));
            queue.submitRequest(new PriorityRequest("F" + i, "Free task", 1, "FREE"));
        }
        
        Thread.sleep(1000);
        queue.printQueueStatus();
        Thread.sleep(1000);
        queue.shutdown();
    }
}
