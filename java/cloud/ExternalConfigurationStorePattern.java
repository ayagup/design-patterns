package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * External Configuration Store Pattern
 * 
 * Intent: Move configuration information out of the application deployment package 
 * to a centralized location, enabling configuration changes without redeployment 
 * and allowing configuration sharing across multiple applications.
 * 
 * Also Known As: Centralized Configuration, Configuration Management
 * 
 * Motivation:
 * Hardcoded configuration or configuration files bundled with application require
 * redeployment for changes. External configuration store allows dynamic updates,
 * environment-specific settings, and centralized management across microservices.
 * 
 * Applicability:
 * - Configuration needs to be shared across multiple applications
 * - Need to change configuration without redeployment
 * - Different configurations for different environments (dev, staging, prod)
 * - Sensitive configuration (passwords, API keys) should be external
 * - Microservices architecture with many services
 * - Need audit trail of configuration changes
 * 
 * Benefits:
 * - No redeployment needed for configuration changes
 * - Centralized configuration management
 * - Environment-specific configuration
 * - Configuration versioning and audit trail
 * - Secure storage for sensitive data
 * - Easy rollback of configuration changes
 * 
 * Implementation Considerations:
 * - Configuration store availability (single point of failure)
 * - Caching strategy to reduce latency
 * - Configuration change notifications
 * - Security and access control
 * - Configuration versioning
 * - Fallback to default values
 */

// Configuration entry with metadata
class ConfigEntry {
    private final String key;
    private String value;
    private final String environment;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;
    private final String description;
    private boolean encrypted;
    
    public ConfigEntry(String key, String value, String environment, String description) {
        this.key = key;
        this.value = value;
        this.environment = environment;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1;
        this.encrypted = false;
    }
    
    public void updateValue(String newValue) {
        this.value = newValue;
        this.updatedAt = LocalDateTime.now();
        this.version++;
    }
    
    public String getKey() { return key; }
    public String getValue() { return value; }
    public String getEnvironment() { return environment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public String getDescription() { return description; }
    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }
    
    @Override
    public String toString() {
        return String.format("ConfigEntry[key=%s, value=%s, env=%s, version=%d]",
            key, encrypted ? "***" : value, environment, version);
    }
}

// Configuration change listener
interface ConfigChangeListener {
    void onConfigChanged(String key, String oldValue, String newValue);
}

// Example 1: In-Memory Configuration Store with versioning
class InMemoryConfigStore {
    private final Map<String, ConfigEntry> configs;
    private final Map<String, List<ConfigEntry>> versionHistory;
    private final List<ConfigChangeListener> listeners;
    private final AtomicLong changeCounter;
    
    public InMemoryConfigStore() {
        this.configs = new ConcurrentHashMap<>();
        this.versionHistory = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.changeCounter = new AtomicLong(0);
    }
    
    public void setConfig(String key, String value, String environment, String description) {
        String oldValue = null;
        
        ConfigEntry entry = configs.get(key);
        if (entry != null) {
            oldValue = entry.getValue();
            entry.updateValue(value);
            
            // Store in version history
            versionHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(
                new ConfigEntry(key, oldValue, environment, "Previous version")
            );
        } else {
            entry = new ConfigEntry(key, value, environment, description);
            configs.put(key, entry);
        }
        
        changeCounter.incrementAndGet();
        notifyListeners(key, oldValue, value);
        
        System.out.printf("Config updated: %s = %s (version %d)%n", 
            key, value, entry.getVersion());
    }
    
    public String getConfig(String key) {
        ConfigEntry entry = configs.get(key);
        return entry != null ? entry.getValue() : null;
    }
    
    public String getConfig(String key, String defaultValue) {
        String value = getConfig(key);
        return value != null ? value : defaultValue;
    }
    
    public ConfigEntry getConfigEntry(String key) {
        return configs.get(key);
    }
    
    public List<ConfigEntry> getVersionHistory(String key) {
        return versionHistory.getOrDefault(key, Collections.emptyList());
    }
    
    public void registerListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners(String key, String oldValue, String newValue) {
        for (ConfigChangeListener listener : listeners) {
            try {
                listener.onConfigChanged(key, oldValue, newValue);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    public long getChangeCount() {
        return changeCounter.get();
    }
    
    public Map<String, ConfigEntry> getAllConfigs() {
        return new HashMap<>(configs);
    }
}

// Example 2: Cached Configuration Client
class CachedConfigClient {
    private final InMemoryConfigStore configStore;
    private final Map<String, String> cache;
    private final long cacheTTL;
    private final Map<String, Long> cacheTimestamps;
    private final ScheduledExecutorService scheduler;
    
    public CachedConfigClient(InMemoryConfigStore configStore, long cacheTTLSeconds) {
        this.configStore = configStore;
        this.cache = new ConcurrentHashMap<>();
        this.cacheTimestamps = new ConcurrentHashMap<>();
        this.cacheTTL = cacheTTLSeconds * 1000;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Register for config changes
        configStore.registerListener((key, oldValue, newValue) -> {
            cache.put(key, newValue);
            cacheTimestamps.put(key, System.currentTimeMillis());
            System.out.printf("[CachedClient] Cache updated for key: %s%n", key);
        });
        
        // Schedule cache cleanup
        scheduler.scheduleAtFixedRate(this::cleanExpiredCache, 
            cacheTTL, cacheTTL, TimeUnit.MILLISECONDS);
    }
    
    public String getConfig(String key) {
        Long timestamp = cacheTimestamps.get(key);
        
        // Check if cache is valid
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < cacheTTL) {
            System.out.printf("[CachedClient] Cache hit for: %s%n", key);
            return cache.get(key);
        }
        
        // Cache miss or expired - fetch from store
        System.out.printf("[CachedClient] Cache miss for: %s, fetching from store%n", key);
        String value = configStore.getConfig(key);
        
        if (value != null) {
            cache.put(key, value);
            cacheTimestamps.put(key, System.currentTimeMillis());
        }
        
        return value;
    }
    
    public void invalidateCache(String key) {
        cache.remove(key);
        cacheTimestamps.remove(key);
        System.out.printf("[CachedClient] Cache invalidated for: %s%n", key);
    }
    
    public void invalidateAll() {
        cache.clear();
        cacheTimestamps.clear();
        System.out.println("[CachedClient] All cache invalidated");
    }
    
    private void cleanExpiredCache() {
        long now = System.currentTimeMillis();
        List<String> expiredKeys = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : cacheTimestamps.entrySet()) {
            if (now - entry.getValue() >= cacheTTL) {
                expiredKeys.add(entry.getKey());
            }
        }
        
        for (String key : expiredKeys) {
            cache.remove(key);
            cacheTimestamps.remove(key);
        }
        
        if (!expiredKeys.isEmpty()) {
            System.out.printf("[CachedClient] Cleaned %d expired entries%n", expiredKeys.size());
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}

// Example 3: Environment-Specific Configuration
class EnvironmentConfigManager {
    private final Map<String, InMemoryConfigStore> envStores;
    private String currentEnvironment;
    
    public EnvironmentConfigManager(String defaultEnvironment) {
        this.envStores = new ConcurrentHashMap<>();
        this.currentEnvironment = defaultEnvironment;
        
        // Initialize stores for common environments
        envStores.put("development", new InMemoryConfigStore());
        envStores.put("staging", new InMemoryConfigStore());
        envStores.put("production", new InMemoryConfigStore());
    }
    
    public void setConfig(String key, String value, String environment, String description) {
        InMemoryConfigStore store = envStores.get(environment);
        if (store != null) {
            store.setConfig(key, value, environment, description);
        } else {
            System.err.println("Unknown environment: " + environment);
        }
    }
    
    public String getConfig(String key) {
        InMemoryConfigStore store = envStores.get(currentEnvironment);
        return store != null ? store.getConfig(key) : null;
    }
    
    public String getConfig(String key, String environment) {
        InMemoryConfigStore store = envStores.get(environment);
        return store != null ? store.getConfig(key) : null;
    }
    
    public void switchEnvironment(String environment) {
        if (envStores.containsKey(environment)) {
            this.currentEnvironment = environment;
            System.out.printf("Switched to environment: %s%n", environment);
        } else {
            System.err.println("Unknown environment: " + environment);
        }
    }
    
    public String getCurrentEnvironment() {
        return currentEnvironment;
    }
    
    public void printEnvironmentConfigs(String environment) {
        InMemoryConfigStore store = envStores.get(environment);
        if (store != null) {
            System.out.printf("\n=== Configuration for %s ===%n", environment);
            store.getAllConfigs().values().forEach(System.out::println);
        }
    }
}

// Example 4: Feature Flags Configuration
class FeatureFlagManager {
    private final InMemoryConfigStore configStore;
    private final String flagPrefix = "feature.";
    
    public FeatureFlagManager(InMemoryConfigStore configStore) {
        this.configStore = configStore;
    }
    
    public void enableFeature(String featureName, String environment) {
        String key = flagPrefix + featureName;
        configStore.setConfig(key, "true", environment, "Feature flag: " + featureName);
        System.out.printf("Feature '%s' enabled%n", featureName);
    }
    
    public void disableFeature(String featureName, String environment) {
        String key = flagPrefix + featureName;
        configStore.setConfig(key, "false", environment, "Feature flag: " + featureName);
        System.out.printf("Feature '%s' disabled%n", featureName);
    }
    
    public boolean isFeatureEnabled(String featureName) {
        String key = flagPrefix + featureName;
        String value = configStore.getConfig(key, "false");
        return "true".equalsIgnoreCase(value);
    }
    
    public void setFeaturePercentage(String featureName, int percentage, String environment) {
        String key = flagPrefix + featureName + ".percentage";
        configStore.setConfig(key, String.valueOf(percentage), environment, 
            "Rollout percentage for: " + featureName);
        System.out.printf("Feature '%s' rollout set to %d%%%n", featureName, percentage);
    }
    
    public int getFeaturePercentage(String featureName) {
        String key = flagPrefix + featureName + ".percentage";
        String value = configStore.getConfig(key, "0");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

// Example 5: Application with Dynamic Configuration
class DynamicApplication implements ConfigChangeListener {
    private final String appName;
    private final CachedConfigClient configClient;
    private int maxConnections;
    private int timeout;
    private String apiEndpoint;
    private boolean debugMode;
    
    public DynamicApplication(String appName, CachedConfigClient configClient) {
        this.appName = appName;
        this.configClient = configClient;
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        maxConnections = Integer.parseInt(
            configClient.getConfig("app.maxConnections"));
        timeout = Integer.parseInt(
            configClient.getConfig("app.timeout"));
        apiEndpoint = configClient.getConfig("app.apiEndpoint");
        debugMode = Boolean.parseBoolean(
            configClient.getConfig("app.debugMode"));
        
        System.out.printf("[%s] Configuration loaded: maxConn=%d, timeout=%d, api=%s, debug=%s%n",
            appName, maxConnections, timeout, apiEndpoint, debugMode);
    }
    
    @Override
    public void onConfigChanged(String key, String oldValue, String newValue) {
        System.out.printf("[%s] Config changed: %s: %s -> %s%n", 
            appName, key, oldValue, newValue);
        
        // Reload specific configuration
        switch (key) {
            case "app.maxConnections":
                maxConnections = Integer.parseInt(newValue);
                System.out.printf("[%s] Max connections updated to: %d%n", appName, maxConnections);
                break;
            case "app.timeout":
                timeout = Integer.parseInt(newValue);
                System.out.printf("[%s] Timeout updated to: %d%n", appName, timeout);
                break;
            case "app.apiEndpoint":
                apiEndpoint = newValue;
                System.out.printf("[%s] API endpoint updated to: %s%n", appName, apiEndpoint);
                break;
            case "app.debugMode":
                debugMode = Boolean.parseBoolean(newValue);
                System.out.printf("[%s] Debug mode updated to: %s%n", appName, debugMode);
                break;
        }
    }
    
    public void printCurrentConfig() {
        System.out.printf("\n[%s] Current Configuration:%n", appName);
        System.out.printf("  Max Connections: %d%n", maxConnections);
        System.out.printf("  Timeout: %d ms%n", timeout);
        System.out.printf("  API Endpoint: %s%n", apiEndpoint);
        System.out.printf("  Debug Mode: %s%n", debugMode);
    }
    
    public void simulateWork() {
        if (debugMode) {
            System.out.printf("[%s] DEBUG: Connecting to %s with timeout %d...%n", 
                appName, apiEndpoint, timeout);
        }
        System.out.printf("[%s] Processing with %d max connections%n", 
            appName, maxConnections);
    }
}

// Demonstration
public class ExternalConfigurationStorePattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== External Configuration Store Pattern Demo ===\n");
        
        // Demo 1: Basic configuration store
        System.out.println("--- Demo 1: Basic Configuration Store ---");
        demoBasicConfigStore();
        
        Thread.sleep(1000);
        
        // Demo 2: Cached configuration client
        System.out.println("\n--- Demo 2: Cached Configuration Client ---");
        demoCachedClient();
        
        Thread.sleep(1000);
        
        // Demo 3: Environment-specific configuration
        System.out.println("\n--- Demo 3: Environment-Specific Configuration ---");
        demoEnvironmentConfig();
        
        Thread.sleep(1000);
        
        // Demo 4: Feature flags
        System.out.println("\n--- Demo 4: Feature Flags ---");
        demoFeatureFlags();
        
        Thread.sleep(1000);
        
        // Demo 5: Dynamic application reconfiguration
        System.out.println("\n--- Demo 5: Dynamic Application Configuration ---");
        demoDynamicApplication();
    }
    
    private static void demoBasicConfigStore() {
        InMemoryConfigStore store = new InMemoryConfigStore();
        
        // Set initial configuration
        store.setConfig("database.url", "jdbc:mysql://localhost:3306/mydb", 
            "production", "Database connection URL");
        store.setConfig("database.maxConnections", "100", 
            "production", "Max database connections");
        store.setConfig("cache.ttl", "3600", 
            "production", "Cache TTL in seconds");
        
        // Read configuration
        System.out.println("\nReading configuration:");
        System.out.println("DB URL: " + store.getConfig("database.url"));
        System.out.println("Max Connections: " + store.getConfig("database.maxConnections"));
        System.out.println("Cache TTL: " + store.getConfig("cache.ttl", "600"));
        
        // Update configuration
        System.out.println("\nUpdating configuration:");
        store.setConfig("database.maxConnections", "200", 
            "production", "Increased max connections");
        
        // Check version history
        ConfigEntry entry = store.getConfigEntry("database.maxConnections");
        System.out.println("Current entry: " + entry);
        System.out.println("Version history: " + store.getVersionHistory("database.maxConnections"));
    }
    
    private static void demoCachedClient() throws InterruptedException {
        InMemoryConfigStore store = new InMemoryConfigStore();
        CachedConfigClient client = new CachedConfigClient(store, 2); // 2 second TTL
        
        // Set configuration
        store.setConfig("service.url", "http://api.example.com", "production", "Service URL");
        
        // First access - cache miss
        System.out.println("Value: " + client.getConfig("service.url"));
        
        // Second access - cache hit
        System.out.println("Value: " + client.getConfig("service.url"));
        
        // Update configuration - cache auto-updated via listener
        store.setConfig("service.url", "http://api2.example.com", "production", "Updated service URL");
        
        // Access updated value from cache
        System.out.println("Value: " + client.getConfig("service.url"));
        
        client.shutdown();
    }
    
    private static void demoEnvironmentConfig() {
        EnvironmentConfigManager manager = new EnvironmentConfigManager("development");
        
        // Set config for different environments
        manager.setConfig("api.url", "http://localhost:8080", "development", "Dev API");
        manager.setConfig("api.url", "http://staging.example.com", "staging", "Staging API");
        manager.setConfig("api.url", "http://api.example.com", "production", "Prod API");
        
        manager.setConfig("logging.level", "DEBUG", "development", "Log level");
        manager.setConfig("logging.level", "INFO", "staging", "Log level");
        manager.setConfig("logging.level", "WARN", "production", "Log level");
        
        // Access config from current environment
        System.out.println("\nCurrent environment: " + manager.getCurrentEnvironment());
        System.out.println("API URL: " + manager.getConfig("api.url"));
        System.out.println("Log Level: " + manager.getConfig("logging.level"));
        
        // Switch environment
        manager.switchEnvironment("production");
        System.out.println("\nAPI URL: " + manager.getConfig("api.url"));
        System.out.println("Log Level: " + manager.getConfig("logging.level"));
        
        // Print all configs for an environment
        manager.printEnvironmentConfigs("production");
    }
    
    private static void demoFeatureFlags() {
        InMemoryConfigStore store = new InMemoryConfigStore();
        FeatureFlagManager flagManager = new FeatureFlagManager(store);
        
        // Enable/disable features
        flagManager.enableFeature("newUI", "production");
        flagManager.enableFeature("darkMode", "production");
        flagManager.disableFeature("betaFeature", "production");
        
        // Check feature status
        System.out.println("\nFeature status:");
        System.out.println("New UI enabled: " + flagManager.isFeatureEnabled("newUI"));
        System.out.println("Dark mode enabled: " + flagManager.isFeatureEnabled("darkMode"));
        System.out.println("Beta feature enabled: " + flagManager.isFeatureEnabled("betaFeature"));
        
        // Gradual rollout
        flagManager.setFeaturePercentage("experimentalSearch", 25, "production");
        System.out.println("\nExperimental search rollout: " + 
            flagManager.getFeaturePercentage("experimentalSearch") + "%");
    }
    
    private static void demoDynamicApplication() throws InterruptedException {
        InMemoryConfigStore store = new InMemoryConfigStore();
        CachedConfigClient client = new CachedConfigClient(store, 60);
        
        // Initial configuration
        store.setConfig("app.maxConnections", "50", "production", "Max connections");
        store.setConfig("app.timeout", "5000", "production", "Timeout in ms");
        store.setConfig("app.apiEndpoint", "http://api.example.com", "production", "API endpoint");
        store.setConfig("app.debugMode", "false", "production", "Debug mode");
        
        // Create application
        DynamicApplication app = new DynamicApplication("MyApp", client);
        store.registerListener(app);
        
        app.printCurrentConfig();
        app.simulateWork();
        
        // Update configuration dynamically
        System.out.println("\n--- Updating configuration dynamically ---");
        store.setConfig("app.maxConnections", "100", "production", "Increased connections");
        store.setConfig("app.debugMode", "true", "production", "Enable debug");
        
        Thread.sleep(100); // Allow config changes to propagate
        
        app.printCurrentConfig();
        app.simulateWork();
        
        client.shutdown();
    }
}
