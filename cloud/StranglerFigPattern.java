package cloud;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Strangler Fig Pattern
 * 
 * Intent: Incrementally migrate a legacy system by gradually replacing specific
 * pieces of functionality with new applications and services. As features from
 * the legacy system are replaced, the new system eventually replaces all the
 * old system's features, strangling the old system and allowing you to decommission it.
 * 
 * Also Known As:
 * - Incremental Migration Pattern
 * - Gradual Replacement Pattern
 * - Strangler Application Pattern
 * 
 * Motivation:
 * Migrating from a legacy system to new architecture is challenging:
 * - Complete rewrites are risky and expensive
 * - Need to maintain business continuity
 * - Difficult to switch everything at once
 * - Unknown unknowns in legacy system
 * - Users need continuous access
 * 
 * Applicability:
 * - Modernizing legacy systems
 * - Incremental migration requirements
 * - Need to maintain service during migration
 * - Risk-averse migration strategy
 * - Monolith to microservices transformation
 * 
 * Benefits:
 * - Low-risk incremental approach
 * - Continuous delivery during migration
 * - Can abort/adjust during migration
 * - Validate new system incrementally
 * - Easier rollback per feature
 * - Spreads cost over time
 * 
 * Trade-offs:
 * - Need to maintain both systems temporarily
 * - Routing complexity
 * - Data synchronization challenges
 * - Longer overall migration timeline
 * - Requires careful planning
 */

// Request representation
class Request {
    private final String id;
    private final String path;
    private final String method;
    private final Map<String, String> params;
    
    public Request(String id, String path, String method) {
        this.id = id;
        this.path = path;
        this.method = method;
        this.params = new HashMap<>();
    }
    
    public String getId() { return id; }
    public String getPath() { return path; }
    public String getMethod() { return method; }
    public Map<String, String> getParams() { return params; }
    
    @Override
    public String toString() {
        return String.format("%s %s (id=%s)", method, path, id);
    }
}

// Response representation
class Response {
    private final String requestId;
    private final int statusCode;
    private final String body;
    private final String source;
    
    public Response(String requestId, int statusCode, String body, String source) {
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.body = body;
        this.source = source;
    }
    
    public String getRequestId() { return requestId; }
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
    public String getSource() { return source; }
    
    @Override
    public String toString() {
        return String.format("[%d] %s (from %s)", statusCode, body, source);
    }
}

// Legacy system
class LegacySystem {
    private final String name;
    
    public LegacySystem(String name) {
        this.name = name;
    }
    
    public Response handleRequest(Request request) {
        System.out.println(String.format("[LEGACY] Processing: %s", request));
        
        // Simulate legacy processing
        String responseBody = String.format("Legacy response for %s", request.getPath());
        return new Response(request.getId(), 200, responseBody, "Legacy System");
    }
    
    public String getName() {
        return name;
    }
}

// New system
class NewSystem {
    private final String name;
    
    public NewSystem(String name) {
        this.name = name;
    }
    
    public Response handleRequest(Request request) {
        System.out.println(String.format("[NEW] Processing: %s", request));
        
        // Simulate new system processing with modern features
        String responseBody = String.format("Modern response for %s (enhanced features)", request.getPath());
        return new Response(request.getId(), 200, responseBody, "New System");
    }
    
    public String getName() {
        return name;
    }
}

// Example 1: Basic Strangler Facade
class StranglerFacade {
    private final LegacySystem legacySystem;
    private final NewSystem newSystem;
    private final Set<String> migratedPaths;
    
    public StranglerFacade(LegacySystem legacySystem, NewSystem newSystem) {
        this.legacySystem = legacySystem;
        this.newSystem = newSystem;
        this.migratedPaths = new HashSet<>();
    }
    
    public void migrateFeature(String path) {
        migratedPaths.add(path);
        System.out.println(String.format("\n[MIGRATION] Feature migrated: %s", path));
    }
    
    public Response routeRequest(Request request) {
        System.out.println(String.format("\n[FACADE] Routing request: %s", request));
        
        if (migratedPaths.contains(request.getPath())) {
            return newSystem.handleRequest(request);
        } else {
            return legacySystem.handleRequest(request);
        }
    }
    
    public void printMigrationStatus() {
        System.out.println("\nMigration Status:");
        System.out.println(String.format("  Migrated features: %d", migratedPaths.size()));
        System.out.println("  Migrated paths: " + migratedPaths);
    }
}

// Example 2: Percentage-Based Traffic Splitting
class TrafficSplittingFacade {
    private final LegacySystem legacySystem;
    private final NewSystem newSystem;
    private final Map<String, Integer> featureMigrationPercentages;
    private final Random random;
    
    public TrafficSplittingFacade(LegacySystem legacySystem, NewSystem newSystem) {
        this.legacySystem = legacySystem;
        this.newSystem = newSystem;
        this.featureMigrationPercentages = new ConcurrentHashMap<>();
        this.random = new Random();
    }
    
    public void setMigrationPercentage(String path, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be 0-100");
        }
        featureMigrationPercentages.put(path, percentage);
        System.out.println(String.format("\n[MIGRATION] %s: %d%% -> New System", path, percentage));
    }
    
    public Response routeRequest(Request request) {
        Integer percentage = featureMigrationPercentages.get(request.getPath());
        
        if (percentage == null) {
            // Not migrated yet - all traffic to legacy
            System.out.println(String.format("[ROUTE] %s -> Legacy (0%%)", request.getPath()));
            return legacySystem.handleRequest(request);
        }
        
        if (percentage >= 100) {
            // Fully migrated - all traffic to new
            System.out.println(String.format("[ROUTE] %s -> New (100%%)", request.getPath()));
            return newSystem.handleRequest(request);
        }
        
        // Partial migration - split traffic
        int roll = random.nextInt(100);
        if (roll < percentage) {
            System.out.println(String.format("[ROUTE] %s -> New (%d%%)", request.getPath(), percentage));
            return newSystem.handleRequest(request);
        } else {
            System.out.println(String.format("[ROUTE] %s -> Legacy (%d%%)", request.getPath(), 100 - percentage));
            return legacySystem.handleRequest(request);
        }
    }
    
    public void printMigrationProgress() {
        System.out.println("\nMigration Progress:");
        featureMigrationPercentages.forEach((path, pct) -> 
            System.out.println(String.format("  %s: %d%%", path, pct)));
    }
}

// Example 3: Feature Flag Based Migration
class FeatureFlagFacade {
    private final LegacySystem legacySystem;
    private final NewSystem newSystem;
    private final Map<String, FeatureFlag> featureFlags;
    
    static class FeatureFlag {
        final String feature;
        boolean enabled;
        Set<String> enabledForUsers;
        
        public FeatureFlag(String feature) {
            this.feature = feature;
            this.enabled = false;
            this.enabledForUsers = new HashSet<>();
        }
    }
    
    public FeatureFlagFacade(LegacySystem legacySystem, NewSystem newSystem) {
        this.legacySystem = legacySystem;
        this.newSystem = newSystem;
        this.featureFlags = new ConcurrentHashMap<>();
    }
    
    public void createFeatureFlag(String feature) {
        featureFlags.put(feature, new FeatureFlag(feature));
        System.out.println(String.format("[FLAG] Created feature flag: %s", feature));
    }
    
    public void enableForUser(String feature, String userId) {
        FeatureFlag flag = featureFlags.get(feature);
        if (flag != null) {
            flag.enabledForUsers.add(userId);
            System.out.println(String.format("[FLAG] Enabled %s for user: %s", feature, userId));
        }
    }
    
    public void enableGlobally(String feature) {
        FeatureFlag flag = featureFlags.get(feature);
        if (flag != null) {
            flag.enabled = true;
            System.out.println(String.format("[FLAG] Enabled %s globally", feature));
        }
    }
    
    public Response routeRequest(Request request, String userId) {
        FeatureFlag flag = featureFlags.get(request.getPath());
        
        if (flag == null || (!flag.enabled && !flag.enabledForUsers.contains(userId))) {
            // Feature flag disabled or user not in beta - use legacy
            System.out.println(String.format("[ROUTE] %s (user=%s) -> Legacy", 
                request.getPath(), userId));
            return legacySystem.handleRequest(request);
        }
        
        // Feature flag enabled - use new system
        System.out.println(String.format("[ROUTE] %s (user=%s) -> New", 
            request.getPath(), userId));
        return newSystem.handleRequest(request);
    }
    
    public void printFeatureFlags() {
        System.out.println("\nFeature Flags:");
        featureFlags.forEach((feature, flag) -> {
            System.out.println(String.format("  %s: global=%s, beta_users=%d", 
                feature, flag.enabled, flag.enabledForUsers.size()));
        });
    }
}

// Example 4: Migration with Monitoring and Rollback
class MonitoredMigrationFacade {
    private final LegacySystem legacySystem;
    private final NewSystem newSystem;
    private final Map<String, MigrationMetrics> metrics;
    private final Map<String, Boolean> migrationStatus;
    
    static class MigrationMetrics {
        int legacyRequests;
        int newRequests;
        int legacyErrors;
        int newErrors;
        long legacyTotalTime;
        long newTotalTime;
        
        public double getNewSystemErrorRate() {
            return newRequests > 0 ? (double) newErrors / newRequests * 100 : 0;
        }
        
        public double getNewSystemAvgTime() {
            return newRequests > 0 ? (double) newTotalTime / newRequests : 0;
        }
    }
    
    public MonitoredMigrationFacade(LegacySystem legacySystem, NewSystem newSystem) {
        this.legacySystem = legacySystem;
        this.newSystem = newSystem;
        this.metrics = new ConcurrentHashMap<>();
        this.migrationStatus = new ConcurrentHashMap<>();
    }
    
    public void migrateFeature(String path) {
        migrationStatus.put(path, true);
        metrics.putIfAbsent(path, new MigrationMetrics());
        System.out.println(String.format("\n[MIGRATION] Started migration: %s", path));
    }
    
    public void rollbackFeature(String path) {
        migrationStatus.put(path, false);
        System.out.println(String.format("\n[ROLLBACK] Rolled back: %s", path));
    }
    
    public Response routeRequest(Request request) {
        String path = request.getPath();
        MigrationMetrics pathMetrics = metrics.computeIfAbsent(path, k -> new MigrationMetrics());
        
        if (migrationStatus.getOrDefault(path, false)) {
            // Route to new system
            long start = System.currentTimeMillis();
            Response response = newSystem.handleRequest(request);
            long duration = System.currentTimeMillis() - start;
            
            pathMetrics.newRequests++;
            pathMetrics.newTotalTime += duration;
            if (response.getStatusCode() >= 500) {
                pathMetrics.newErrors++;
            }
            
            return response;
        } else {
            // Route to legacy system
            long start = System.currentTimeMillis();
            Response response = legacySystem.handleRequest(request);
            long duration = System.currentTimeMillis() - start;
            
            pathMetrics.legacyRequests++;
            pathMetrics.legacyTotalTime += duration;
            if (response.getStatusCode() >= 500) {
                pathMetrics.legacyErrors++;
            }
            
            return response;
        }
    }
    
    public void printMetrics() {
        System.out.println("\nMigration Metrics:");
        metrics.forEach((path, m) -> {
            System.out.println(String.format("  %s:", path));
            System.out.println(String.format("    Legacy: %d requests, %d errors, %.1fms avg", 
                m.legacyRequests, m.legacyErrors, 
                m.legacyRequests > 0 ? (double)m.legacyTotalTime / m.legacyRequests : 0));
            System.out.println(String.format("    New: %d requests, %d errors (%.1f%%), %.1fms avg", 
                m.newRequests, m.newErrors, m.getNewSystemErrorRate(), m.getNewSystemAvgTime()));
        });
    }
}

// Example 5: Migration Orchestrator
class MigrationOrchestrator {
    private final StranglerFacade facade;
    private final List<MigrationStep> migrationPlan;
    private int currentStep;
    
    static class MigrationStep {
        final String feature;
        final String description;
        boolean completed;
        
        public MigrationStep(String feature, String description) {
            this.feature = feature;
            this.description = description;
            this.completed = false;
        }
    }
    
    public MigrationOrchestrator(StranglerFacade facade) {
        this.facade = facade;
        this.migrationPlan = new ArrayList<>();
        this.currentStep = 0;
    }
    
    public void addMigrationStep(String feature, String description) {
        migrationPlan.add(new MigrationStep(feature, description));
    }
    
    public boolean executeNextStep() {
        if (currentStep >= migrationPlan.size()) {
            System.out.println("\n[ORCHESTRATOR] Migration complete!");
            return false;
        }
        
        MigrationStep step = migrationPlan.get(currentStep);
        System.out.println(String.format("\n[ORCHESTRATOR] Step %d/%d: %s", 
            currentStep + 1, migrationPlan.size(), step.description));
        
        facade.migrateFeature(step.feature);
        step.completed = true;
        currentStep++;
        
        return true;
    }
    
    public void printMigrationPlan() {
        System.out.println("\nMigration Plan:");
        for (int i = 0; i < migrationPlan.size(); i++) {
            MigrationStep step = migrationPlan.get(i);
            String status = step.completed ? "✓" : (i == currentStep ? "→" : " ");
            System.out.println(String.format("  %s Step %d: %s (%s)", 
                status, i + 1, step.description, step.feature));
        }
    }
}

// Demo
public class StranglerFigPattern {
    public static void main(String[] args) {
        demonstrateBasicStrangler();
        demonstrateTrafficSplitting();
        demonstrateFeatureFlags();
        demonstrateMonitoredMigration();
        demonstrateMigrationOrchestration();
    }
    
    private static void demonstrateBasicStrangler() {
        System.out.println("=== Basic Strangler Fig Pattern ===\n");
        
        LegacySystem legacy = new LegacySystem("Legacy Monolith");
        NewSystem newSys = new NewSystem("Microservices");
        StranglerFacade facade = new StranglerFacade(legacy, newSys);
        
        // Initial state - all requests to legacy
        facade.routeRequest(new Request("R1", "/users", "GET"));
        facade.routeRequest(new Request("R2", "/orders", "POST"));
        
        // Migrate /users to new system
        facade.migrateFeature("/users");
        
        // Now /users goes to new system
        facade.routeRequest(new Request("R3", "/users", "GET"));
        facade.routeRequest(new Request("R4", "/orders", "POST"));
        
        facade.printMigrationStatus();
    }
    
    private static void demonstrateTrafficSplitting() {
        System.out.println("\n\n=== Traffic Splitting Migration ===\n");
        
        LegacySystem legacy = new LegacySystem("Legacy System");
        NewSystem newSys = new NewSystem("New System");
        TrafficSplittingFacade facade = new TrafficSplittingFacade(legacy, newSys);
        
        // Start with 25% traffic to new system
        facade.setMigrationPercentage("/api/products", 25);
        
        System.out.println("\nSending requests:");
        for (int i = 1; i <= 8; i++) {
            facade.routeRequest(new Request("R" + i, "/api/products", "GET"));
        }
        
        // Increase to 75%
        facade.setMigrationPercentage("/api/products", 75);
        
        System.out.println("\nAfter increasing to 75%:");
        for (int i = 9; i <= 12; i++) {
            facade.routeRequest(new Request("R" + i, "/api/products", "GET"));
        }
        
        facade.printMigrationProgress();
    }
    
    private static void demonstrateFeatureFlags() {
        System.out.println("\n\n=== Feature Flag Based Migration ===\n");
        
        LegacySystem legacy = new LegacySystem("Legacy System");
        NewSystem newSys = new NewSystem("New System");
        FeatureFlagFacade facade = new FeatureFlagFacade(legacy, newSys);
        
        // Create feature flag
        facade.createFeatureFlag("/api/search");
        
        // Enable for beta users
        facade.enableForUser("/api/search", "alice");
        facade.enableForUser("/api/search", "bob");
        
        System.out.println("\nRequests from different users:");
        facade.routeRequest(new Request("R1", "/api/search", "GET"), "alice");  // Beta user
        facade.routeRequest(new Request("R2", "/api/search", "GET"), "charlie"); // Regular user
        
        // Enable globally
        facade.enableGlobally("/api/search");
        
        System.out.println("\nAfter global enablement:");
        facade.routeRequest(new Request("R3", "/api/search", "GET"), "charlie");
        
        facade.printFeatureFlags();
    }
    
    private static void demonstrateMonitoredMigration() {
        System.out.println("\n\n=== Monitored Migration with Rollback ===\n");
        
        LegacySystem legacy = new LegacySystem("Legacy System");
        NewSystem newSys = new NewSystem("New System");
        MonitoredMigrationFacade facade = new MonitoredMigrationFacade(legacy, newSys);
        
        // Send requests to legacy
        System.out.println("Before migration:");
        for (int i = 1; i <= 3; i++) {
            facade.routeRequest(new Request("R" + i, "/api/checkout", "POST"));
        }
        
        // Migrate feature
        facade.migrateFeature("/api/checkout");
        
        // Send requests to new system
        System.out.println("\nAfter migration:");
        for (int i = 4; i <= 6; i++) {
            facade.routeRequest(new Request("R" + i, "/api/checkout", "POST"));
        }
        
        facade.printMetrics();
        
        // Simulated rollback due to issues
        facade.rollbackFeature("/api/checkout");
        
        System.out.println("\nAfter rollback:");
        facade.routeRequest(new Request("R7", "/api/checkout", "POST"));
    }
    
    private static void demonstrateMigrationOrchestration() {
        System.out.println("\n\n=== Migration Orchestration ===\n");
        
        LegacySystem legacy = new LegacySystem("Legacy Monolith");
        NewSystem newSys = new NewSystem("Microservices Platform");
        StranglerFacade facade = new StranglerFacade(legacy, newSys);
        
        MigrationOrchestrator orchestrator = new MigrationOrchestrator(facade);
        
        // Define migration plan
        orchestrator.addMigrationStep("/api/users", "Migrate user service");
        orchestrator.addMigrationStep("/api/products", "Migrate product catalog");
        orchestrator.addMigrationStep("/api/orders", "Migrate order processing");
        orchestrator.addMigrationStep("/api/payments", "Migrate payment gateway");
        
        orchestrator.printMigrationPlan();
        
        // Execute migration steps
        System.out.println("\nExecuting migration:");
        orchestrator.executeNextStep();
        orchestrator.executeNextStep();
        
        orchestrator.printMigrationPlan();
        
        // Test routing
        System.out.println("\nTesting routing:");
        facade.routeRequest(new Request("R1", "/api/users", "GET"));      // Migrated
        facade.routeRequest(new Request("R2", "/api/products", "GET"));   // Migrated
        facade.routeRequest(new Request("R3", "/api/orders", "POST"));    // Not yet migrated
    }
}
