package microservices;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Log Aggregation Pattern
 * =========================
 * 
 * Intent:
 * Centralizes logs from all microservice instances into a single
 * searchable repository for monitoring, debugging, and analysis.
 * 
 * Also Known As:
 * - Centralized Logging
 * - Log Collection
 * 
 * Motivation:
 * - Logs scattered across many service instances
 * - Need unified view for debugging
 * - Correlate logs from different services
 * - Search and analyze across all logs
 * 
 * Applicability:
 * - Multiple microservices
 * - Multiple instances per service
 * - Need centralized log analysis
 * - Debugging distributed systems
 * 
 * Structure:
 * Services -> Log Shipper -> Log Aggregator -> Log Storage -> Search UI
 * 
 * Benefits:
 * + Centralized access
 * + Correlation across services
 * + Powerful search
 * + Historical analysis
 */

// ============================================================================
// LOG ENTRY
// ============================================================================

class LogEntry {
    private final String id;
    private final long timestamp;
    private final String level;
    private final String serviceName;
    private final String instanceId;
    private final String message;
    private final String traceId;
    private final Map<String, String> metadata;
    
    public LogEntry(String serviceName, String instanceId, String level, String message, String traceId) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.level = level;
        this.message = message;
        this.traceId = traceId;
        this.metadata = new HashMap<>();
    }
    
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getServiceName() { return serviceName; }
    public String getInstanceId() { return instanceId; }
    public String getMessage() { return message; }
    public String getTraceId() { return traceId; }
    public Map<String, String> getMetadata() { return metadata; }
    
    @Override
    public String toString() {
        return String.format("[%tT] [%s] [%s:%s] [Trace:%s] %s",
            timestamp, level, serviceName, instanceId, traceId, message);
    }
}

// ============================================================================
// LOG AGGREGATOR (Centralized)
// ============================================================================

class LogAggregator {
    private final List<LogEntry> logs = new CopyOnWriteArrayList<>();
    
    public void ingest(LogEntry logEntry) {
        logs.add(logEntry);
        // In real system, this would write to Elasticsearch, Splunk, etc.
    }
    
    // Search by service
    public List<LogEntry> searchByService(String serviceName) {
        return logs.stream()
            .filter(log -> log.getServiceName().equals(serviceName))
            .collect(Collectors.toList());
    }
    
    // Search by trace ID (correlate across services)
    public List<LogEntry> searchByTraceId(String traceId) {
        return logs.stream()
            .filter(log -> traceId.equals(log.getTraceId()))
            .sorted(Comparator.comparingLong(LogEntry::getTimestamp))
            .collect(Collectors.toList());
    }
    
    // Search by level
    public List<LogEntry> searchByLevel(String level) {
        return logs.stream()
            .filter(log -> log.getLevel().equals(level))
            .collect(Collectors.toList());
    }
    
    // Search by time range
    public List<LogEntry> searchByTimeRange(long startTime, long endTime) {
        return logs.stream()
            .filter(log -> log.getTimestamp() >= startTime && log.getTimestamp() <= endTime)
            .collect(Collectors.toList());
    }
    
    // Full-text search
    public List<LogEntry> search(String query) {
        return logs.stream()
            .filter(log -> log.getMessage().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    public int getTotalLogs() {
        return logs.size();
    }
}

// ============================================================================
// LOGGER (Used by Services)
// ============================================================================

class DistributedLogger {
    private final String serviceName;
    private final String instanceId;
    private final LogAggregator aggregator;
    
    public DistributedLogger(String serviceName, String instanceId, LogAggregator aggregator) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.aggregator = aggregator;
    }
    
    public void info(String message, String traceId) {
        LogEntry entry = new LogEntry(serviceName, instanceId, "INFO", message, traceId);
        aggregator.ingest(entry);
        System.out.println(entry);
    }
    
    public void warn(String message, String traceId) {
        LogEntry entry = new LogEntry(serviceName, instanceId, "WARN", message, traceId);
        aggregator.ingest(entry);
        System.out.println(entry);
    }
    
    public void error(String message, String traceId) {
        LogEntry entry = new LogEntry(serviceName, instanceId, "ERROR", message, traceId);
        entry.addMetadata("severity", "high");
        aggregator.ingest(entry);
        System.out.println(entry);
    }
}

// ============================================================================
// MICROSERVICES WITH CENTRALIZED LOGGING
// ============================================================================

class ServiceA {
    private final DistributedLogger logger;
    
    public ServiceA(String instanceId, LogAggregator aggregator) {
        this.logger = new DistributedLogger("ServiceA", instanceId, aggregator);
    }
    
    public void processRequest(String traceId) {
        logger.info("Received request", traceId);
        simulateWork(30);
        logger.info("Request processed successfully", traceId);
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ServiceB {
    private final DistributedLogger logger;
    
    public ServiceB(String instanceId, LogAggregator aggregator) {
        this.logger = new DistributedLogger("ServiceB", instanceId, aggregator);
    }
    
    public void processRequest(String traceId) {
        logger.info("Starting database query", traceId);
        simulateWork(50);
        logger.warn("Query took longer than expected", traceId);
        logger.info("Database query completed", traceId);
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ServiceC {
    private final DistributedLogger logger;
    
    public ServiceC(String instanceId, LogAggregator aggregator) {
        this.logger = new DistributedLogger("ServiceC", instanceId, aggregator);
    }
    
    public void processRequest(String traceId) {
        logger.info("Calling external API", traceId);
        simulateWork(40);
        logger.error("External API timeout", traceId);
        logger.info("Retrying with fallback", traceId);
    }
    
    private void simulateWork(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

/**
 * Demonstration of Log Aggregation Pattern
 */
public class LogAggregationPattern {
    public static void main(String[] args) {
        System.out.println("=== Log Aggregation Pattern ===\n");
        
        // Centralized log aggregator
        LogAggregator aggregator = new LogAggregator();
        
        // Multiple service instances (each logs to central aggregator)
        ServiceA serviceA1 = new ServiceA("A-1", aggregator);
        ServiceA serviceA2 = new ServiceA("A-2", aggregator);
        ServiceB serviceB1 = new ServiceB("B-1", aggregator);
        ServiceC serviceC1 = new ServiceC("C-1", aggregator);
        
        System.out.println("--- Services Processing Requests ---\n");
        
        // Trace 1
        String trace1 = "TRACE-001";
        serviceA1.processRequest(trace1);
        serviceB1.processRequest(trace1);
        serviceC1.processRequest(trace1);
        
        System.out.println();
        
        // Trace 2 (different instance of ServiceA)
        String trace2 = "TRACE-002";
        serviceA2.processRequest(trace2);
        serviceB1.processRequest(trace2);
        
        System.out.println("\n\n--- Querying Aggregated Logs ---\n");
        
        System.out.println("1. All logs from ServiceA:");
        List<LogEntry> serviceALogs = aggregator.searchByService("ServiceA");
        serviceALogs.forEach(System.out::println);
        
        System.out.println("\n2. All logs for TRACE-001 (across all services):");
        List<LogEntry> trace1Logs = aggregator.searchByTraceId(trace1);
        trace1Logs.forEach(System.out::println);
        
        System.out.println("\n3. All ERROR level logs:");
        List<LogEntry> errorLogs = aggregator.searchByLevel("ERROR");
        errorLogs.forEach(System.out::println);
        
        System.out.println("\n4. Search for 'timeout':");
        List<LogEntry> timeoutLogs = aggregator.search("timeout");
        timeoutLogs.forEach(System.out::println);
        
        System.out.println("\n\n=== Statistics ===");
        System.out.println("Total logs aggregated: " + aggregator.getTotalLogs());
        System.out.println("ServiceA logs: " + aggregator.searchByService("ServiceA").size());
        System.out.println("ServiceB logs: " + aggregator.searchByService("ServiceB").size());
        System.out.println("ServiceC logs: " + aggregator.searchByService("ServiceC").size());
        System.out.println("ERROR logs: " + aggregator.searchByLevel("ERROR").size());
        
        System.out.println("\n=== Key Benefits ===");
        System.out.println("1. Centralized - all logs in one place");
        System.out.println("2. Correlation - trace requests across services");
        System.out.println("3. Searchable - find logs by various criteria");
        System.out.println("4. Scalable - handles logs from many instances");
        
        System.out.println("\n=== Real-World Tools ===");
        System.out.println("- ELK Stack (Elasticsearch, Logstash, Kibana)");
        System.out.println("- Splunk");
        System.out.println("- Graylog");
        System.out.println("- CloudWatch Logs (AWS)");
        System.out.println("- Stackdriver (Google Cloud)");
    }
}
