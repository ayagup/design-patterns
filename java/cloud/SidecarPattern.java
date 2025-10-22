package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sidecar Pattern
 * 
 * Intent: Deploy helper components of an application into a separate process
 * or container to provide isolation and encapsulation. The sidecar shares the
 * same lifecycle as the parent application.
 * 
 * Also Known As:
 * - Sidekick Pattern
 * - Companion Container
 * - Helper Container
 * 
 * Motivation:
 * Applications often need cross-cutting concerns like:
 * - Logging and monitoring
 * - Configuration management
 * - Network proxying
 * - Service discovery
 * - Security/authentication
 * 
 * Embedding these in the main application leads to:
 * - Tight coupling
 * - Language/framework dependencies
 * - Difficult to update independently
 * - Code duplication across services
 * 
 * Applicability:
 * - Microservices architectures
 * - Need to extend functionality without modifying core app
 * - Cross-cutting concerns
 * - Multi-language environments
 * - Legacy application enhancement
 * 
 * Benefits:
 * - Separation of concerns
 * - Independent deployment/updates
 * - Language agnostic
 * - Resource isolation
 * - Reusable across services
 * - Reduces application complexity
 * 
 * Trade-offs:
 * - Additional overhead (separate process)
 * - Inter-process communication complexity
 * - More components to manage
 * - Deployment complexity
 */

// Main application
interface Application {
    void processRequest(String request);
    String getName();
}

// Sidecar interface
interface Sidecar {
    void start();
    void stop();
    String getSidecarType();
}

// Example 1: Logging Sidecar
// Handles all logging concerns separately from main app
class LoggingSidecar implements Sidecar {
    private final Application application;
    private final BlockingQueue<LogEntry> logQueue;
    private final ExecutorService logWriter;
    private volatile boolean running = false;
    
    static class LogEntry {
        final long timestamp;
        final String level;
        final String message;
        final String source;
        
        public LogEntry(String level, String message, String source) {
            this.timestamp = System.currentTimeMillis();
            this.level = level;
            this.message = message;
            this.source = source;
        }
        
        @Override
        public String toString() {
            return String.format("[%tT] [%s] [%s] %s", timestamp, level, source, message);
        }
    }
    
    public LoggingSidecar(Application application) {
        this.application = application;
        this.logQueue = new LinkedBlockingQueue<>();
        this.logWriter = Executors.newSingleThreadExecutor();
    }
    
    @Override
    public void start() {
        running = true;
        System.out.println("[Sidecar] Logging sidecar started for " + application.getName());
        
        logWriter.submit(() -> {
            while (running) {
                try {
                    LogEntry entry = logQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        writeLog(entry);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    @Override
    public void stop() {
        running = false;
        logWriter.shutdown();
        System.out.println("[Sidecar] Logging sidecar stopped");
    }
    
    @Override
    public String getSidecarType() {
        return "Logging";
    }
    
    public void log(String level, String message) {
        logQueue.offer(new LogEntry(level, message, application.getName()));
    }
    
    private void writeLog(LogEntry entry) {
        // In real scenario, would write to file, send to log aggregator, etc.
        System.out.println("[LOG] " + entry);
    }
}

// Example 2: Metrics Sidecar
// Collects and exposes metrics from the application
class MetricsSidecar implements Sidecar {
    private final Application application;
    private final Map<String, AtomicInteger> metrics;
    private final ScheduledExecutorService scheduler;
    
    public MetricsSidecar(Application application) {
        this.application = application;
        this.metrics = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    @Override
    public void start() {
        System.out.println("[Sidecar] Metrics sidecar started for " + application.getName());
        
        // Periodically report metrics
        scheduler.scheduleAtFixedRate(this::reportMetrics, 2, 2, TimeUnit.SECONDS);
    }
    
    @Override
    public void stop() {
        scheduler.shutdown();
        System.out.println("[Sidecar] Metrics sidecar stopped");
    }
    
    @Override
    public String getSidecarType() {
        return "Metrics";
    }
    
    public void incrementCounter(String metricName) {
        metrics.computeIfAbsent(metricName, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    public void recordValue(String metricName, int value) {
        metrics.computeIfAbsent(metricName, k -> new AtomicInteger(0)).set(value);
    }
    
    private void reportMetrics() {
        if (!metrics.isEmpty()) {
            System.out.println("\n[METRICS] " + application.getName() + " metrics:");
            metrics.forEach((name, value) -> 
                System.out.println("  " + name + ": " + value.get()));
        }
    }
    
    public Map<String, Integer> getMetrics() {
        Map<String, Integer> snapshot = new HashMap<>();
        metrics.forEach((k, v) -> snapshot.put(k, v.get()));
        return snapshot;
    }
}

// Example 3: Configuration Sidecar
// Manages configuration updates without restarting the app
class ConfigurationSidecar implements Sidecar {
    private final Application application;
    private final Map<String, String> config;
    private final List<ConfigChangeListener> listeners;
    private final ScheduledExecutorService configPoller;
    
    interface ConfigChangeListener {
        void onConfigChanged(String key, String oldValue, String newValue);
    }
    
    public ConfigurationSidecar(Application application) {
        this.application = application;
        this.config = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.configPoller = Executors.newScheduledThreadPool(1);
        
        // Initialize default config
        config.put("max_connections", "100");
        config.put("timeout_ms", "5000");
        config.put("retry_count", "3");
    }
    
    @Override
    public void start() {
        System.out.println("[Sidecar] Configuration sidecar started for " + application.getName());
        
        // Poll for config changes (in real scenario, would watch external config store)
        configPoller.scheduleAtFixedRate(this::checkForConfigUpdates, 3, 3, TimeUnit.SECONDS);
    }
    
    @Override
    public void stop() {
        configPoller.shutdown();
        System.out.println("[Sidecar] Configuration sidecar stopped");
    }
    
    @Override
    public String getSidecarType() {
        return "Configuration";
    }
    
    public String getConfig(String key) {
        return config.get(key);
    }
    
    public void updateConfig(String key, String value) {
        String oldValue = config.put(key, value);
        System.out.println(String.format("[CONFIG] Updated %s: %s -> %s", key, oldValue, value));
        
        // Notify listeners
        for (ConfigChangeListener listener : listeners) {
            listener.onConfigChanged(key, oldValue, value);
        }
    }
    
    public void addConfigListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }
    
    private void checkForConfigUpdates() {
        // Simulated config check - in reality would poll external config service
        System.out.println("[CONFIG] Checking for configuration updates...");
    }
    
    public Map<String, String> getAllConfig() {
        return new HashMap<>(config);
    }
}

// Example 4: Proxy Sidecar
// Handles network communication, retries, circuit breaking
class ProxySidecar implements Sidecar {
    private final Application application;
    private final Map<String, ServiceEndpoint> serviceRegistry;
    private final AtomicInteger requestCount;
    
    static class ServiceEndpoint {
        final String name;
        final String url;
        boolean healthy;
        
        public ServiceEndpoint(String name, String url) {
            this.name = name;
            this.url = url;
            this.healthy = true;
        }
    }
    
    public ProxySidecar(Application application) {
        this.application = application;
        this.serviceRegistry = new ConcurrentHashMap<>();
        this.requestCount = new AtomicInteger(0);
        
        // Register some services
        registerService("user-service", "http://users.example.com");
        registerService("order-service", "http://orders.example.com");
        registerService("payment-service", "http://payments.example.com");
    }
    
    @Override
    public void start() {
        System.out.println("[Sidecar] Proxy sidecar started for " + application.getName());
    }
    
    @Override
    public void stop() {
        System.out.println("[Sidecar] Proxy sidecar stopped");
    }
    
    @Override
    public String getSidecarType() {
        return "Proxy";
    }
    
    public void registerService(String name, String url) {
        serviceRegistry.put(name, new ServiceEndpoint(name, url));
    }
    
    public String callService(String serviceName, String request) {
        ServiceEndpoint endpoint = serviceRegistry.get(serviceName);
        if (endpoint == null) {
            return "Error: Service not found";
        }
        
        if (!endpoint.healthy) {
            return "Error: Service unhealthy";
        }
        
        requestCount.incrementAndGet();
        
        // Simulate HTTP call with retry logic
        System.out.println(String.format("[PROXY] %s -> %s: %s", 
            application.getName(), endpoint.url, request));
        
        // In real scenario: handle retries, circuit breaking, timeouts
        return "Response from " + serviceName;
    }
    
    public int getRequestCount() {
        return requestCount.get();
    }
}

// Example 5: Security Sidecar
// Handles authentication, authorization, encryption
class SecuritySidecar implements Sidecar {
    private final Application application;
    private final Map<String, AuthToken> tokenStore;
    private final Set<String> authorizedUsers;
    
    static class AuthToken {
        final String token;
        final String userId;
        final long expiresAt;
        
        public AuthToken(String userId) {
            this.token = UUID.randomUUID().toString();
            this.userId = userId;
            this.expiresAt = System.currentTimeMillis() + 3600000; // 1 hour
        }
        
        public boolean isValid() {
            return System.currentTimeMillis() < expiresAt;
        }
    }
    
    public SecuritySidecar(Application application) {
        this.application = application;
        this.tokenStore = new ConcurrentHashMap<>();
        this.authorizedUsers = ConcurrentHashMap.newKeySet();
        
        // Add some authorized users
        authorizedUsers.add("alice");
        authorizedUsers.add("bob");
        authorizedUsers.add("charlie");
    }
    
    @Override
    public void start() {
        System.out.println("[Sidecar] Security sidecar started for " + application.getName());
    }
    
    @Override
    public void stop() {
        System.out.println("[Sidecar] Security sidecar stopped");
    }
    
    @Override
    public String getSidecarType() {
        return "Security";
    }
    
    public String authenticate(String userId, String password) {
        // Simplified authentication
        if (authorizedUsers.contains(userId)) {
            AuthToken token = new AuthToken(userId);
            tokenStore.put(token.token, token);
            System.out.println(String.format("[SECURITY] Authenticated user: %s", userId));
            return token.token;
        }
        
        System.out.println(String.format("[SECURITY] Authentication failed for: %s", userId));
        return null;
    }
    
    public boolean authorize(String token) {
        AuthToken authToken = tokenStore.get(token);
        
        if (authToken == null) {
            System.out.println("[SECURITY] Invalid token");
            return false;
        }
        
        if (!authToken.isValid()) {
            System.out.println("[SECURITY] Expired token");
            tokenStore.remove(token);
            return false;
        }
        
        System.out.println(String.format("[SECURITY] Authorized: %s", authToken.userId));
        return true;
    }
    
    public String encryptData(String data) {
        // Simplified encryption (in reality, use proper crypto)
        String encrypted = Base64.getEncoder().encodeToString(data.getBytes());
        System.out.println("[SECURITY] Data encrypted");
        return encrypted;
    }
    
    public String decryptData(String encryptedData) {
        // Simplified decryption
        String decrypted = new String(Base64.getDecoder().decode(encryptedData));
        System.out.println("[SECURITY] Data decrypted");
        return decrypted;
    }
}

// Application with sidecars
class ApplicationWithSidecars implements Application {
    private final String name;
    private final List<Sidecar> sidecars;
    private LoggingSidecar loggingSidecar;
    private MetricsSidecar metricsSidecar;
    private ConfigurationSidecar configSidecar;
    
    public ApplicationWithSidecars(String name) {
        this.name = name;
        this.sidecars = new ArrayList<>();
    }
    
    public void attachLoggingSidecar() {
        this.loggingSidecar = new LoggingSidecar(this);
        sidecars.add(loggingSidecar);
    }
    
    public void attachMetricsSidecar() {
        this.metricsSidecar = new MetricsSidecar(this);
        sidecars.add(metricsSidecar);
    }
    
    public void attachConfigSidecar() {
        this.configSidecar = new ConfigurationSidecar(this);
        sidecars.add(configSidecar);
    }
    
    public void startAll() {
        System.out.println("\n=== Starting Application: " + name + " ===");
        for (Sidecar sidecar : sidecars) {
            sidecar.start();
        }
        System.out.println("Application started with " + sidecars.size() + " sidecars\n");
    }
    
    public void stopAll() {
        System.out.println("\n=== Stopping Application: " + name + " ===");
        for (Sidecar sidecar : sidecars) {
            sidecar.stop();
        }
    }
    
    @Override
    public void processRequest(String request) {
        if (loggingSidecar != null) {
            loggingSidecar.log("INFO", "Processing request: " + request);
        }
        
        if (metricsSidecar != null) {
            metricsSidecar.incrementCounter("requests_processed");
        }
        
        // Simulate processing
        System.out.println("[APP] " + name + " processing: " + request);
        
        if (loggingSidecar != null) {
            loggingSidecar.log("INFO", "Request completed");
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public LoggingSidecar getLoggingSidecar() {
        return loggingSidecar;
    }
    
    public MetricsSidecar getMetricsSidecar() {
        return metricsSidecar;
    }
    
    public ConfigurationSidecar getConfigSidecar() {
        return configSidecar;
    }
}

// Demo
public class SidecarPattern {
    public static void main(String[] args) throws InterruptedException {
        demonstrateLoggingSidecar();
        demonstrateMetricsSidecar();
        demonstrateConfigurationSidecar();
        demonstrateProxySidecar();
        demonstrateSecuritySidecar();
        demonstrateMultipleSidecars();
    }
    
    private static void demonstrateLoggingSidecar() throws InterruptedException {
        System.out.println("=== Logging Sidecar Demo ===\n");
        
        Application app = new Application() {
            public void processRequest(String request) {
                System.out.println("[APP] Processing: " + request);
            }
            public String getName() { return "OrderService"; }
        };
        
        LoggingSidecar loggingSidecar = new LoggingSidecar(app);
        loggingSidecar.start();
        
        loggingSidecar.log("INFO", "Service started");
        loggingSidecar.log("DEBUG", "Processing order #123");
        loggingSidecar.log("WARN", "Low inventory for item XYZ");
        loggingSidecar.log("ERROR", "Payment failed for order #124");
        
        Thread.sleep(500);
        loggingSidecar.stop();
    }
    
    private static void demonstrateMetricsSidecar() throws InterruptedException {
        System.out.println("\n\n=== Metrics Sidecar Demo ===\n");
        
        Application app = new Application() {
            public void processRequest(String request) {}
            public String getName() { return "UserService"; }
        };
        
        MetricsSidecar metricsSidecar = new MetricsSidecar(app);
        metricsSidecar.start();
        
        // Simulate activity
        for (int i = 0; i < 10; i++) {
            metricsSidecar.incrementCounter("requests_total");
            metricsSidecar.incrementCounter("requests_success");
        }
        
        metricsSidecar.incrementCounter("requests_total");
        metricsSidecar.incrementCounter("requests_failed");
        
        metricsSidecar.recordValue("active_connections", 42);
        
        Thread.sleep(2500);
        metricsSidecar.stop();
    }
    
    private static void demonstrateConfigurationSidecar() throws InterruptedException {
        System.out.println("\n\n=== Configuration Sidecar Demo ===\n");
        
        Application app = new Application() {
            public void processRequest(String request) {}
            public String getName() { return "PaymentService"; }
        };
        
        ConfigurationSidecar configSidecar = new ConfigurationSidecar(app);
        
        // Listen for config changes
        configSidecar.addConfigListener((key, oldValue, newValue) -> {
            System.out.println(String.format("[APP] Detected config change: %s", key));
        });
        
        configSidecar.start();
        
        System.out.println("\nCurrent config: " + configSidecar.getAllConfig());
        
        Thread.sleep(1000);
        
        // Update configuration
        configSidecar.updateConfig("max_connections", "200");
        configSidecar.updateConfig("timeout_ms", "10000");
        
        Thread.sleep(3500);
        configSidecar.stop();
    }
    
    private static void demonstrateProxySidecar() {
        System.out.println("\n\n=== Proxy Sidecar Demo ===\n");
        
        Application app = new Application() {
            public void processRequest(String request) {}
            public String getName() { return "APIGateway"; }
        };
        
        ProxySidecar proxySidecar = new ProxySidecar(app);
        proxySidecar.start();
        
        // Make service calls through proxy
        proxySidecar.callService("user-service", "GET /users/123");
        proxySidecar.callService("order-service", "POST /orders");
        proxySidecar.callService("payment-service", "POST /payments");
        
        System.out.println("\nTotal proxied requests: " + proxySidecar.getRequestCount());
        
        proxySidecar.stop();
    }
    
    private static void demonstrateSecuritySidecar() {
        System.out.println("\n\n=== Security Sidecar Demo ===\n");
        
        Application app = new Application() {
            public void processRequest(String request) {}
            public String getName() { return "DataService"; }
        };
        
        SecuritySidecar securitySidecar = new SecuritySidecar(app);
        securitySidecar.start();
        
        // Authenticate users
        String token1 = securitySidecar.authenticate("alice", "password123");
        securitySidecar.authenticate("eve", "hacker");  // Will fail
        
        // Authorize requests
        if (token1 != null) {
            System.out.println("\nAttempting to access protected resource:");
            securitySidecar.authorize(token1);
        }
        
        // Encrypt/decrypt data
        System.out.println("\nEncryption demo:");
        String sensitiveData = "credit_card=1234-5678-9012-3456";
        String encrypted = securitySidecar.encryptData(sensitiveData);
        System.out.println("Encrypted: " + encrypted.substring(0, 20) + "...");
        String decrypted = securitySidecar.decryptData(encrypted);
        System.out.println("Decrypted: " + decrypted);
        
        securitySidecar.stop();
    }
    
    private static void demonstrateMultipleSidecars() throws InterruptedException {
        System.out.println("\n\n=== Multiple Sidecars Demo ===\n");
        
        ApplicationWithSidecars app = new ApplicationWithSidecars("ECommerceService");
        
        // Attach multiple sidecars
        app.attachLoggingSidecar();
        app.attachMetricsSidecar();
        app.attachConfigSidecar();
        
        app.startAll();
        
        // Process requests
        app.processRequest("Create order #501");
        Thread.sleep(200);
        app.processRequest("Process payment #502");
        Thread.sleep(200);
        app.processRequest("Send confirmation #503");
        
        Thread.sleep(2000);
        
        // Update config through sidecar
        app.getConfigSidecar().updateConfig("feature_flag_express_shipping", "true");
        
        Thread.sleep(1000);
        
        app.stopAll();
    }
}
