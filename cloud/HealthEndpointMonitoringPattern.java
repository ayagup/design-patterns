package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Health Endpoint Monitoring Pattern
 * 
 * Intent: Implement health check endpoints in applications that external tools
 * can access at regular intervals to monitor service availability and performance.
 * 
 * Also Known As: Health Check, Heartbeat, Liveness/Readiness Probe
 * 
 * Motivation:
 * In distributed systems, it's critical to know if services are running properly.
 * Health endpoints allow monitoring systems to detect failures, trigger alerts,
 * and enable auto-healing mechanisms like service restarts or traffic rerouting.
 * 
 * Applicability:
 * - Need to monitor service health in production
 * - Load balancers need to know which instances are healthy
 * - Auto-scaling based on health status
 * - Integration with monitoring systems (Prometheus, Nagios, etc.)
 * - Kubernetes liveness and readiness probes
 * - Circuit breakers need health information
 * 
 * Benefits:
 * - Early detection of failures
 * - Automated recovery (restart unhealthy instances)
 * - Better availability through traffic routing
 * - Visibility into service health
 * - Integration with monitoring dashboards
 * - Support for graceful degradation
 * 
 * Implementation Considerations:
 * - Health check should be lightweight
 * - Don't make external calls in health checks
 * - Consider different health levels (liveness vs readiness)
 * - Cache health status to avoid overhead
 * - Include dependency health
 * - Secure health endpoints if needed
 */

// Health status enumeration
enum HealthStatus {
    HEALTHY,
    DEGRADED,
    UNHEALTHY
}

// Health check result
class HealthCheckResult {
    private final String componentName;
    private final HealthStatus status;
    private final String message;
    private final LocalDateTime timestamp;
    private final long responseTimeMs;
    private final Map<String, Object> details;
    
    public HealthCheckResult(String componentName, HealthStatus status, String message, long responseTimeMs) {
        this.componentName = componentName;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.responseTimeMs = responseTimeMs;
        this.details = new HashMap<>();
    }
    
    public void addDetail(String key, Object value) {
        details.put(key, value);
    }
    
    public String getComponentName() { return componentName; }
    public HealthStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public long getResponseTimeMs() { return responseTimeMs; }
    public Map<String, Object> getDetails() { return details; }
    
    @Override
    public String toString() {
        return String.format("%s: %s - %s (%dms)", 
            componentName, status, message, responseTimeMs);
    }
}

// Health check interface
interface HealthCheck {
    HealthCheckResult check();
    String getName();
}

// Example 1: Database Health Check
class DatabaseHealthCheck implements HealthCheck {
    private final String dbName;
    private final AtomicInteger failureCount;
    private volatile boolean connected;
    
    public DatabaseHealthCheck(String dbName) {
        this.dbName = dbName;
        this.failureCount = new AtomicInteger(0);
        this.connected = true;
    }
    
    @Override
    public HealthCheckResult check() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate database ping
            Thread.sleep(10 + new Random().nextInt(40));
            
            if (connected) {
                failureCount.set(0);
                long responseTime = System.currentTimeMillis() - startTime;
                HealthCheckResult result = new HealthCheckResult(
                    dbName, HealthStatus.HEALTHY, "Database connection OK", responseTime);
                result.addDetail("connectionPool", "5/10 active");
                result.addDetail("queryLatency", responseTime + "ms");
                return result;
            } else {
                int failures = failureCount.incrementAndGet();
                long responseTime = System.currentTimeMillis() - startTime;
                HealthCheckResult result = new HealthCheckResult(
                    dbName, HealthStatus.UNHEALTHY, "Database connection failed", responseTime);
                result.addDetail("consecutiveFailures", failures);
                return result;
            }
            
        } catch (InterruptedException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new HealthCheckResult(dbName, HealthStatus.UNHEALTHY, 
                "Health check interrupted", responseTime);
        }
    }
    
    @Override
    public String getName() {
        return dbName;
    }
    
    public void simulateConnectionLoss() {
        this.connected = false;
    }
    
    public void simulateConnectionRestore() {
        this.connected = true;
    }
}

// Example 2: Memory Health Check
class MemoryHealthCheck implements HealthCheck {
    private final Runtime runtime;
    private final double warningThreshold = 0.8; // 80%
    private final double criticalThreshold = 0.95; // 95%
    
    public MemoryHealthCheck() {
        this.runtime = Runtime.getRuntime();
    }
    
    @Override
    public HealthCheckResult check() {
        long startTime = System.currentTimeMillis();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usagePercentage = (double) usedMemory / maxMemory;
        
        HealthStatus status;
        String message;
        
        if (usagePercentage >= criticalThreshold) {
            status = HealthStatus.UNHEALTHY;
            message = "Memory usage critical";
        } else if (usagePercentage >= warningThreshold) {
            status = HealthStatus.DEGRADED;
            message = "Memory usage high";
        } else {
            status = HealthStatus.HEALTHY;
            message = "Memory usage normal";
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        HealthCheckResult result = new HealthCheckResult("Memory", status, message, responseTime);
        result.addDetail("usedMemoryMB", usedMemory / (1024 * 1024));
        result.addDetail("maxMemoryMB", maxMemory / (1024 * 1024));
        result.addDetail("usagePercent", String.format("%.1f%%", usagePercentage * 100));
        
        return result;
    }
    
    @Override
    public String getName() {
        return "Memory";
    }
}

// Example 3: Disk Space Health Check
class DiskSpaceHealthCheck implements HealthCheck {
    private final long totalSpace;
    private final AtomicLong usedSpace;
    private final double warningThreshold = 0.85;
    private final double criticalThreshold = 0.95;
    
    public DiskSpaceHealthCheck(long totalSpaceGB) {
        this.totalSpace = totalSpaceGB * 1024 * 1024 * 1024;
        this.usedSpace = new AtomicLong(totalSpaceGB * 1024 * 1024 * 1024 / 2); // Start at 50%
    }
    
    @Override
    public HealthCheckResult check() {
        long startTime = System.currentTimeMillis();
        
        long used = usedSpace.get();
        double usagePercentage = (double) used / totalSpace;
        
        HealthStatus status;
        String message;
        
        if (usagePercentage >= criticalThreshold) {
            status = HealthStatus.UNHEALTHY;
            message = "Disk space critical";
        } else if (usagePercentage >= warningThreshold) {
            status = HealthStatus.DEGRADED;
            message = "Disk space low";
        } else {
            status = HealthStatus.HEALTHY;
            message = "Disk space OK";
        }
        
        long responseTime = System.currentTimeMillis() - startTime;
        HealthCheckResult result = new HealthCheckResult("DiskSpace", status, message, responseTime);
        result.addDetail("usedGB", used / (1024 * 1024 * 1024));
        result.addDetail("totalGB", totalSpace / (1024 * 1024 * 1024));
        result.addDetail("freeGB", (totalSpace - used) / (1024 * 1024 * 1024));
        result.addDetail("usagePercent", String.format("%.1f%%", usagePercentage * 100));
        
        return result;
    }
    
    @Override
    public String getName() {
        return "DiskSpace";
    }
    
    public void simulateUsage(long bytesUsed) {
        usedSpace.addAndGet(bytesUsed);
    }
}

// Example 4: External Service Health Check
class ExternalServiceHealthCheck implements HealthCheck {
    private final String serviceName;
    private final String serviceUrl;
    private final AtomicInteger consecutiveFailures;
    private final int maxFailures = 3;
    private volatile boolean reachable;
    
    public ExternalServiceHealthCheck(String serviceName, String serviceUrl) {
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
        this.consecutiveFailures = new AtomicInteger(0);
        this.reachable = true;
    }
    
    @Override
    public HealthCheckResult check() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Simulate external service call
            Thread.sleep(20 + new Random().nextInt(80));
            
            if (reachable) {
                consecutiveFailures.set(0);
                long responseTime = System.currentTimeMillis() - startTime;
                HealthCheckResult result = new HealthCheckResult(
                    serviceName, HealthStatus.HEALTHY, "Service reachable", responseTime);
                result.addDetail("endpoint", serviceUrl);
                result.addDetail("responseTime", responseTime + "ms");
                return result;
            } else {
                int failures = consecutiveFailures.incrementAndGet();
                long responseTime = System.currentTimeMillis() - startTime;
                
                HealthStatus status = failures >= maxFailures ? 
                    HealthStatus.UNHEALTHY : HealthStatus.DEGRADED;
                
                HealthCheckResult result = new HealthCheckResult(
                    serviceName, status, "Service unreachable", responseTime);
                result.addDetail("endpoint", serviceUrl);
                result.addDetail("consecutiveFailures", failures);
                return result;
            }
            
        } catch (InterruptedException e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new HealthCheckResult(serviceName, HealthStatus.UNHEALTHY,
                "Health check interrupted", responseTime);
        }
    }
    
    @Override
    public String getName() {
        return serviceName;
    }
    
    public void simulateServiceDown() {
        this.reachable = false;
    }
    
    public void simulateServiceUp() {
        this.reachable = true;
    }
}

// Health Endpoint Manager
class HealthEndpoint {
    private final List<HealthCheck> healthChecks;
    private final Map<String, HealthCheckResult> cachedResults;
    private final ScheduledExecutorService scheduler;
    private final long cacheValidityMs = 5000; // 5 seconds
    private final Map<String, Long> lastCheckTime;
    
    public HealthEndpoint() {
        this.healthChecks = new CopyOnWriteArrayList<>();
        this.cachedResults = new ConcurrentHashMap<>();
        this.lastCheckTime = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    public void registerHealthCheck(HealthCheck healthCheck) {
        healthChecks.add(healthCheck);
        System.out.println("[HealthEndpoint] Registered: " + healthCheck.getName());
    }
    
    public void startPeriodicChecks(long intervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            for (HealthCheck check : healthChecks) {
                try {
                    HealthCheckResult result = check.check();
                    cachedResults.put(check.getName(), result);
                    lastCheckTime.put(check.getName(), System.currentTimeMillis());
                } catch (Exception e) {
                    System.err.println("Health check failed: " + check.getName() + " - " + e.getMessage());
                }
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
        
        System.out.println("[HealthEndpoint] Started periodic checks (every " + intervalSeconds + "s)");
    }
    
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        List<HealthCheckResult> results = new ArrayList<>();
        HealthStatus overallStatus = HealthStatus.HEALTHY;
        
        for (HealthCheck check : healthChecks) {
            HealthCheckResult result = getCachedOrFreshResult(check);
            results.add(result);
            
            // Determine overall status
            if (result.getStatus() == HealthStatus.UNHEALTHY) {
                overallStatus = HealthStatus.UNHEALTHY;
            } else if (result.getStatus() == HealthStatus.DEGRADED && overallStatus != HealthStatus.UNHEALTHY) {
                overallStatus = HealthStatus.DEGRADED;
            }
        }
        
        health.put("status", overallStatus.toString());
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("checks", results);
        
        return health;
    }
    
    public Map<String, Object> getLiveness() {
        // Liveness - is the application running?
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", LocalDateTime.now().toString());
        return liveness;
    }
    
    public Map<String, Object> getReadiness() {
        // Readiness - is the application ready to serve requests?
        Map<String, Object> readiness = new HashMap<>();
        List<HealthCheckResult> criticalChecks = new ArrayList<>();
        HealthStatus overallStatus = HealthStatus.HEALTHY;
        
        for (HealthCheck check : healthChecks) {
            HealthCheckResult result = getCachedOrFreshResult(check);
            criticalChecks.add(result);
            
            if (result.getStatus() == HealthStatus.UNHEALTHY) {
                overallStatus = HealthStatus.UNHEALTHY;
            }
        }
        
        readiness.put("status", overallStatus == HealthStatus.HEALTHY ? "READY" : "NOT_READY");
        readiness.put("timestamp", LocalDateTime.now().toString());
        readiness.put("checks", criticalChecks);
        
        return readiness;
    }
    
    private HealthCheckResult getCachedOrFreshResult(HealthCheck check) {
        Long lastCheck = lastCheckTime.get(check.getName());
        HealthCheckResult cached = cachedResults.get(check.getName());
        
        // Use cache if valid
        if (cached != null && lastCheck != null && 
            (System.currentTimeMillis() - lastCheck) < cacheValidityMs) {
            return cached;
        }
        
        // Otherwise perform fresh check
        HealthCheckResult result = check.check();
        cachedResults.put(check.getName(), result);
        lastCheckTime.put(check.getName(), System.currentTimeMillis());
        return result;
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
    
    public void printHealth() {
        Map<String, Object> health = getHealth();
        System.out.println("\n=== Health Status ===");
        System.out.println("Overall Status: " + health.get("status"));
        System.out.println("Timestamp: " + health.get("timestamp"));
        System.out.println("\nComponent Health:");
        
        @SuppressWarnings("unchecked")
        List<HealthCheckResult> checks = (List<HealthCheckResult>) health.get("checks");
        for (HealthCheckResult result : checks) {
            System.out.println("  " + result);
            if (!result.getDetails().isEmpty()) {
                result.getDetails().forEach((k, v) -> 
                    System.out.println("    - " + k + ": " + v));
            }
        }
    }
}

// Example 5: Application with Health Monitoring
class MonitoredApplication {
    private final String appName;
    private final HealthEndpoint healthEndpoint;
    private final DatabaseHealthCheck dbCheck;
    private final ExternalServiceHealthCheck apiCheck;
    
    public MonitoredApplication(String appName) {
        this.appName = appName;
        this.healthEndpoint = new HealthEndpoint();
        
        // Register health checks
        this.dbCheck = new DatabaseHealthCheck("PostgreSQL");
        this.apiCheck = new ExternalServiceHealthCheck("PaymentAPI", "https://api.payment.com");
        
        healthEndpoint.registerHealthCheck(dbCheck);
        healthEndpoint.registerHealthCheck(new MemoryHealthCheck());
        healthEndpoint.registerHealthCheck(new DiskSpaceHealthCheck(100)); // 100GB
        healthEndpoint.registerHealthCheck(apiCheck);
    }
    
    public void start() {
        System.out.println("[" + appName + "] Application starting...");
        healthEndpoint.startPeriodicChecks(3); // Check every 3 seconds
        System.out.println("[" + appName + "] Application started");
    }
    
    public void stop() {
        System.out.println("[" + appName + "] Application stopping...");
        healthEndpoint.shutdown();
        System.out.println("[" + appName + "] Application stopped");
    }
    
    public HealthEndpoint getHealthEndpoint() {
        return healthEndpoint;
    }
    
    public void simulateDatabaseFailure() {
        System.out.println("\n[SIMULATION] Database connection lost!");
        dbCheck.simulateConnectionLoss();
    }
    
    public void simulateDatabaseRecovery() {
        System.out.println("\n[SIMULATION] Database connection restored!");
        dbCheck.simulateConnectionRestore();
    }
    
    public void simulateExternalServiceFailure() {
        System.out.println("\n[SIMULATION] External API down!");
        apiCheck.simulateServiceDown();
    }
    
    public void simulateExternalServiceRecovery() {
        System.out.println("\n[SIMULATION] External API recovered!");
        apiCheck.simulateServiceUp();
    }
}

// Demonstration
public class HealthEndpointMonitoringPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Health Endpoint Monitoring Pattern Demo ===\n");
        
        // Demo 1: Basic health checks
        System.out.println("--- Demo 1: Basic Health Checks ---");
        demoBasicHealthChecks();
        
        Thread.sleep(1000);
        
        // Demo 2: Health endpoint with multiple checks
        System.out.println("\n--- Demo 2: Health Endpoint ---");
        demoHealthEndpoint();
        
        Thread.sleep(1000);
        
        // Demo 3: Liveness vs Readiness
        System.out.println("\n--- Demo 3: Liveness vs Readiness Probes ---");
        demoLivenessReadiness();
        
        Thread.sleep(1000);
        
        // Demo 4: Monitored application with failures
        System.out.println("\n--- Demo 4: Application Health Monitoring ---");
        demoMonitoredApplication();
    }
    
    private static void demoBasicHealthChecks() {
        DatabaseHealthCheck dbCheck = new DatabaseHealthCheck("MySQL");
        MemoryHealthCheck memCheck = new MemoryHealthCheck();
        
        System.out.println("Database: " + dbCheck.check());
        System.out.println("Memory: " + memCheck.check());
        
        // Simulate failure
        dbCheck.simulateConnectionLoss();
        System.out.println("\nAfter connection loss:");
        System.out.println("Database: " + dbCheck.check());
    }
    
    private static void demoHealthEndpoint() {
        HealthEndpoint endpoint = new HealthEndpoint();
        
        endpoint.registerHealthCheck(new DatabaseHealthCheck("PostgreSQL"));
        endpoint.registerHealthCheck(new MemoryHealthCheck());
        endpoint.registerHealthCheck(new DiskSpaceHealthCheck(100));
        
        endpoint.printHealth();
        
        endpoint.shutdown();
    }
    
    private static void demoLivenessReadiness() {
        HealthEndpoint endpoint = new HealthEndpoint();
        
        DatabaseHealthCheck dbCheck = new DatabaseHealthCheck("Redis");
        endpoint.registerHealthCheck(dbCheck);
        endpoint.registerHealthCheck(new MemoryHealthCheck());
        
        System.out.println("Liveness Probe: " + endpoint.getLiveness());
        System.out.println("Readiness Probe: " + endpoint.getReadiness());
        
        // Simulate DB failure
        System.out.println("\n[After DB failure]");
        dbCheck.simulateConnectionLoss();
        
        System.out.println("Liveness Probe: " + endpoint.getLiveness()); // Still alive
        System.out.println("Readiness Probe: " + endpoint.getReadiness()); // Not ready
        
        endpoint.shutdown();
    }
    
    private static void demoMonitoredApplication() throws InterruptedException {
        MonitoredApplication app = new MonitoredApplication("ECommerceApp");
        app.start();
        
        // Initial healthy state
        Thread.sleep(1000);
        app.getHealthEndpoint().printHealth();
        
        // Simulate database failure
        Thread.sleep(2000);
        app.simulateDatabaseFailure();
        Thread.sleep(4000);
        app.getHealthEndpoint().printHealth();
        
        // Simulate recovery
        Thread.sleep(2000);
        app.simulateDatabaseRecovery();
        Thread.sleep(4000);
        app.getHealthEndpoint().printHealth();
        
        // Simulate external service failure
        Thread.sleep(2000);
        app.simulateExternalServiceFailure();
        Thread.sleep(4000);
        app.getHealthEndpoint().printHealth();
        
        app.stop();
    }
}
