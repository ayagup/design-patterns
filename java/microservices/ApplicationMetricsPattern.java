package microservices;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Application Metrics Pattern
 * =============================
 * 
 * Intent:
 * Collects, aggregates, and exposes metrics about application health,
 * performance, and business operations for monitoring and alerting.
 * 
 * Also Known As:
 * - Metrics Collection
 * - Application Monitoring
 * - Instrumentation
 * 
 * Motivation:
 * - Monitor application health
 * - Track performance
 * - Measure business KPIs
 * - Enable proactive alerting
 * - Capacity planning
 * 
 * Applicability:
 * - Production microservices
 * - Need visibility into system health
 * - Performance monitoring required
 * - Want to track business metrics
 * 
 * Types of Metrics:
 * - Counter: Monotonically increasing value
 * - Gauge: Current value that can go up/down
 * - Histogram: Distribution of values
 * - Timer: Duration of operations
 * 
 * Benefits:
 * + Proactive monitoring
 * + Performance insights
 * + Business intelligence
 * + Capacity planning
 */

// ============================================================================
// METRIC TYPES
// ============================================================================

class Counter {
    private final AtomicLong value = new AtomicLong(0);
    private final String name;
    
    public Counter(String name) {
        this.name = name;
    }
    
    public void increment() {
        value.incrementAndGet();
    }
    
    public void increment(long delta) {
        value.addAndGet(delta);
    }
    
    public long getValue() {
        return value.get();
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %d", name, getValue());
    }
}

class Gauge {
    private final AtomicReference<Double> value = new AtomicReference<>(0.0);
    private final String name;
    
    public Gauge(String name) {
        this.name = name;
    }
    
    public void setValue(double val) {
        value.set(val);
    }
    
    public double getValue() {
        return value.get();
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %.2f", name, getValue());
    }
}

class Histogram {
    final List<Long> values = new CopyOnWriteArrayList<>();
    private final String name;
    
    public Histogram(String name) {
        this.name = name;
    }
    
    public void record(long value) {
        values.add(value);
    }
    
    public double getAverage() {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }
    
    public long getMin() {
        return values.stream().mapToLong(Long::longValue).min().orElse(0);
    }
    
    public long getMax() {
        return values.stream().mapToLong(Long::longValue).max().orElse(0);
    }
    
    public long getPercentile(double percentile) {
        if (values.isEmpty()) return 0;
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return String.format("%s: count=%d, avg=%.2f, min=%d, max=%d, p95=%d, p99=%d",
            name, values.size(), getAverage(), getMin(), getMax(),
            getPercentile(95), getPercentile(99));
    }
}

// ============================================================================
// METRICS REGISTRY
// ============================================================================

class MetricsRegistry {
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Gauge> gauges = new ConcurrentHashMap<>();
    private final Map<String, Histogram> histograms = new ConcurrentHashMap<>();
    
    public Counter counter(String name) {
        return counters.computeIfAbsent(name, Counter::new);
    }
    
    public Gauge gauge(String name) {
        return gauges.computeIfAbsent(name, Gauge::new);
    }
    
    public Histogram histogram(String name) {
        return histograms.computeIfAbsent(name, Histogram::new);
    }
    
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> all = new HashMap<>();
        counters.forEach((name, counter) -> all.put(name, counter.getValue()));
        gauges.forEach((name, gauge) -> all.put(name, gauge.getValue()));
        histograms.forEach((name, hist) -> all.put(name, Map.of(
            "count", hist.values.size(),
            "avg", hist.getAverage(),
            "min", hist.getMin(),
            "max", hist.getMax(),
            "p95", hist.getPercentile(95),
            "p99", hist.getPercentile(99)
        )));
        return all;
    }
    
    public void printReport() {
        System.out.println("\n=== METRICS REPORT ===\n");
        
        System.out.println("Counters:");
        counters.values().forEach(c -> System.out.println("  " + c));
        
        System.out.println("\nGauges:");
        gauges.values().forEach(g -> System.out.println("  " + g));
        
        System.out.println("\nHistograms:");
        histograms.values().forEach(h -> System.out.println("  " + h));
    }
}

// ============================================================================
// INSTRUMENTED SERVICE
// ============================================================================

class OrderServiceWithMetrics {
    private final MetricsRegistry metrics;
    
    // Metrics
    private final Counter ordersCreated;
    private final Counter ordersFailed;
    private final Gauge activeOrders;
    private final Histogram orderProcessingTime;
    private final Counter totalRevenue;
    
    public OrderServiceWithMetrics(MetricsRegistry metrics) {
        this.metrics = metrics;
        
        // Register metrics
        this.ordersCreated = metrics.counter("orders.created");
        this.ordersFailed = metrics.counter("orders.failed");
        this.activeOrders = metrics.gauge("orders.active");
        this.orderProcessingTime = metrics.histogram("orders.processing.time");
        this.totalRevenue = metrics.counter("revenue.total");
    }
    
    public void createOrder(String orderId, double amount) {
        long startTime = System.currentTimeMillis();
        
        try {
            System.out.println("[OrderService] Processing order: " + orderId);
            
            // Update active orders gauge
            activeOrders.setValue(activeOrders.getValue() + 1);
            
            // Simulate processing
            simulateWork(50 + (long)(Math.random() * 100));
            
            // Success metrics
            ordersCreated.increment();
            totalRevenue.increment((long)amount);
            
            System.out.println("[OrderService] Order completed: " + orderId);
            
        } catch (Exception e) {
            ordersFailed.increment();
            throw e;
        } finally {
            // Update gauges and histograms
            activeOrders.setValue(activeOrders.getValue() - 1);
            
            long duration = System.currentTimeMillis() - startTime;
            orderProcessingTime.record(duration);
        }
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class InventoryServiceWithMetrics {
    private final MetricsRegistry metrics;
    private final Counter stockChecks;
    private final Counter stockUpdates;
    private final Gauge currentStockLevel;
    
    public InventoryServiceWithMetrics(MetricsRegistry metrics) {
        this.metrics = metrics;
        this.stockChecks = metrics.counter("inventory.checks");
        this.stockUpdates = metrics.counter("inventory.updates");
        this.currentStockLevel = metrics.gauge("inventory.stock.level");
        
        // Initialize stock level
        currentStockLevel.setValue(1000.0);
    }
    
    public void checkStock(String productId) {
        System.out.println("[InventoryService] Checking stock for: " + productId);
        stockChecks.increment();
        simulateWork(20);
    }
    
    public void updateStock(String productId, int delta) {
        System.out.println("[InventoryService] Updating stock for: " + productId + " by " + delta);
        stockUpdates.increment();
        currentStockLevel.setValue(currentStockLevel.getValue() + delta);
        simulateWork(30);
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// ============================================================================
// HEALTH CHECK
// ============================================================================

class HealthCheck {
    private final MetricsRegistry metrics;
    
    public HealthCheck(MetricsRegistry metrics) {
        this.metrics = metrics;
    }
    
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        // Add key metrics to health check
        Counter ordersCreated = metrics.counter("orders.created");
        Counter ordersFailed = metrics.counter("orders.failed");
        
        long total = ordersCreated.getValue() + ordersFailed.getValue();
        double successRate = total > 0 ? (ordersCreated.getValue() * 100.0 / total) : 100.0;
        
        health.put("orders_success_rate", String.format("%.1f%%", successRate));
        health.put("orders_total", total);
        
        // Determine health based on success rate
        if (successRate < 90) {
            health.put("status", "DEGRADED");
        }
        
        return health;
    }
}

/**
 * Demonstration of Application Metrics Pattern
 */
public class ApplicationMetricsPattern {
    public static void main(String[] args) {
        System.out.println("=== Application Metrics Pattern ===\n");
        
        // Create metrics registry
        MetricsRegistry metrics = new MetricsRegistry();
        
        // Create instrumented services
        OrderServiceWithMetrics orderService = new OrderServiceWithMetrics(metrics);
        InventoryServiceWithMetrics inventoryService = new InventoryServiceWithMetrics(metrics);
        HealthCheck healthCheck = new HealthCheck(metrics);
        
        System.out.println("--- Processing Orders ---\n");
        
        // Simulate order processing
        orderService.createOrder("ORD-001", 299.99);
        inventoryService.checkStock("P001");
        inventoryService.updateStock("P001", -1);
        
        orderService.createOrder("ORD-002", 149.99);
        inventoryService.checkStock("P002");
        inventoryService.updateStock("P002", -2);
        
        orderService.createOrder("ORD-003", 499.99);
        inventoryService.checkStock("P003");
        inventoryService.updateStock("P003", -1);
        
        orderService.createOrder("ORD-004", 79.99);
        orderService.createOrder("ORD-005", 199.99);
        
        // Print metrics report
        metrics.printReport();
        
        System.out.println("\n\n--- Health Check ---\n");
        Map<String, Object> health = healthCheck.getHealthStatus();
        health.forEach((key, value) -> System.out.println(key + ": " + value));
        
        System.out.println("\n\n=== Metric Types ===");
        System.out.println("1. Counter - monotonically increasing (orders created, requests)");
        System.out.println("2. Gauge - current value (active connections, queue size)");
        System.out.println("3. Histogram - distribution (latency, request size)");
        System.out.println("4. Timer - duration measurements (operation timing)");
        
        System.out.println("\n=== Key Benefits ===");
        System.out.println("+ Proactive monitoring - detect issues before users");
        System.out.println("+ Performance insights - identify bottlenecks");
        System.out.println("+ Business intelligence - track KPIs");
        System.out.println("+ Capacity planning - understand resource needs");
        
        System.out.println("\n=== Real-World Tools ===");
        System.out.println("- Prometheus + Grafana");
        System.out.println("- Datadog");
        System.out.println("- New Relic");
        System.out.println("- CloudWatch (AWS)");
        System.out.println("- Micrometer (Java)");
    }
}
