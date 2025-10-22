package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Externalized Configuration Pattern
 * ====================================
 * 
 * Intent:
 * Stores configuration outside the service deployment so that it can
 * be changed without redeploying the service.
 * 
 * Also Known As:
 * - Configuration Server Pattern
 * - Dynamic Configuration
 * 
 * Motivation:
 * - Different configs for different environments
 * - Change config without redeploy
 * - Centralized configuration management
 * - Dynamic config refresh
 * - Secrets management
 * 
 * Applicability:
 * - Multiple environments (dev, staging, prod)
 * - Need runtime config changes
 * - Want centralized config
 * - Multiple service instances
 * 
 * Structure:
 * Services -> Config Server -> Config Storage (Git, DB, etc.)
 * 
 * Benefits:
 * + Environment-specific configs
 * + Runtime updates
 * + Centralized management
 * + Version control
 * + Secrets management
 */

// ============================================================================
// CONFIGURATION MODELS
// ============================================================================

class ConfigurationEntry {
    private final String key;
    private String value;
    private final String environment;
    private final String service;
    
    public ConfigurationEntry(String key, String value, String environment, String service) {
        this.key = key;
        this.value = value;
        this.environment = environment;
        this.service = service;
    }
    
    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getEnvironment() { return environment; }
    public String getService() { return service; }
    
    @Override
    public String toString() {
        return String.format("%s.%s.%s = %s", service, environment, key, value);
    }
}

// ============================================================================
// CONFIGURATION SERVER
// ============================================================================

class ConfigurationServer {
    private final Map<String, ConfigurationEntry> configs = new ConcurrentHashMap<>();
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    public ConfigurationServer() {
        // Pre-populate with sample configurations
        seedConfiguration();
    }
    
    private void seedConfiguration() {
        // Database configurations
        addConfig("db.host", "localhost", "dev", "OrderService");
        addConfig("db.host", "prod-db.example.com", "prod", "OrderService");
        addConfig("db.port", "5432", "dev", "OrderService");
        addConfig("db.port", "5432", "prod", "OrderService");
        addConfig("db.maxConnections", "10", "dev", "OrderService");
        addConfig("db.maxConnections", "100", "prod", "OrderService");
        
        // API configurations
        addConfig("api.rateLimit", "100", "dev", "APIGateway");
        addConfig("api.rateLimit", "1000", "prod", "APIGateway");
        addConfig("api.timeout", "5000", "dev", "APIGateway");
        addConfig("api.timeout", "3000", "prod", "APIGateway");
        
        // Feature flags
        addConfig("feature.newCheckout", "true", "dev", "OrderService");
        addConfig("feature.newCheckout", "false", "prod", "OrderService");
        
        System.out.println("[ConfigServer] Configuration seeded");
    }
    
    private void addConfig(String key, String value, String environment, String service) {
        String fullKey = makeKey(service, environment, key);
        configs.put(fullKey, new ConfigurationEntry(key, value, environment, service));
    }
    
    private String makeKey(String service, String environment, String key) {
        return service + ":" + environment + ":" + key;
    }
    
    public String getConfig(String service, String environment, String key) {
        String fullKey = makeKey(service, environment, key);
        ConfigurationEntry entry = configs.get(fullKey);
        return entry != null ? entry.getValue() : null;
    }
    
    public Map<String, String> getAllConfigs(String service, String environment) {
        Map<String, String> result = new HashMap<>();
        String prefix = service + ":" + environment + ":";
        
        configs.forEach((fullKey, entry) -> {
            if (fullKey.startsWith(prefix)) {
                result.put(entry.getKey(), entry.getValue());
            }
        });
        
        return result;
    }
    
    public void updateConfig(String service, String environment, String key, String newValue) {
        String fullKey = makeKey(service, environment, key);
        ConfigurationEntry entry = configs.get(fullKey);
        
        if (entry != null) {
            String oldValue = entry.getValue();
            entry.setValue(newValue);
            System.out.println("[ConfigServer] Updated " + fullKey + ": " + oldValue + " -> " + newValue);
            
            // Notify listeners
            notifyListeners(service, environment, key, oldValue, newValue);
        }
    }
    
    public void registerListener(ConfigChangeListener listener) {
        listeners.add(listener);
    }
    
    private void notifyListeners(String service, String environment, String key, String oldValue, String newValue) {
        for (ConfigChangeListener listener : listeners) {
            listener.onConfigChange(service, environment, key, oldValue, newValue);
        }
    }
    
    public void printAllConfigs() {
        System.out.println("\n=== All Configurations ===");
        configs.values().forEach(entry -> System.out.println("  " + entry));
    }
}

// ============================================================================
// CONFIG CHANGE LISTENER
// ============================================================================

interface ConfigChangeListener {
    void onConfigChange(String service, String environment, String key, String oldValue, String newValue);
}

// ============================================================================
// SERVICE WITH EXTERNALIZED CONFIG
// ============================================================================

class OrderServiceExternalConfig implements ConfigChangeListener {
    private final String serviceName = "OrderService";
    private final String environment;
    private final ConfigurationServer configServer;
    
    // Cached configuration
    private Map<String, String> config;
    
    public OrderServiceExternalConfig(String environment, ConfigurationServer configServer) {
        this.environment = environment;
        this.configServer = configServer;
        
        // Load initial configuration
        refreshConfiguration();
        
        // Register for config changes
        configServer.registerListener(this);
    }
    
    private void refreshConfiguration() {
        config = configServer.getAllConfigs(serviceName, environment);
        System.out.println("[" + serviceName + "] Configuration loaded for environment: " + environment);
        System.out.println("[" + serviceName + "] Active configs: " + config);
    }
    
    @Override
    public void onConfigChange(String service, String env, String key, String oldValue, String newValue) {
        if (serviceName.equals(service) && environment.equals(env)) {
            System.out.println("[" + serviceName + "] Config changed: " + key + " = " + newValue);
            refreshConfiguration();
        }
    }
    
    public void processOrder(String orderId) {
        System.out.println("\n[" + serviceName + "] Processing order: " + orderId);
        
        // Use configuration
        String dbHost = config.get("db.host");
        String dbPort = config.get("db.port");
        String maxConnections = config.get("db.maxConnections");
        boolean newCheckoutEnabled = Boolean.parseBoolean(config.getOrDefault("feature.newCheckout", "false"));
        
        System.out.println("[" + serviceName + "] Connecting to database: " + dbHost + ":" + dbPort);
        System.out.println("[" + serviceName + "] Max connections: " + maxConnections);
        System.out.println("[" + serviceName + "] New checkout feature: " + (newCheckoutEnabled ? "ENABLED" : "DISABLED"));
        
        if (newCheckoutEnabled) {
            System.out.println("[" + serviceName + "] Using new checkout flow");
        } else {
            System.out.println("[" + serviceName + "] Using legacy checkout flow");
        }
        
        System.out.println("[" + serviceName + "] Order processed successfully");
    }
}

class APIGatewayExternalConfig implements ConfigChangeListener {
    private final String serviceName = "APIGateway";
    private final String environment;
    private final ConfigurationServer configServer;
    
    private Map<String, String> config;
    
    public APIGatewayExternalConfig(String environment, ConfigurationServer configServer) {
        this.environment = environment;
        this.configServer = configServer;
        
        refreshConfiguration();
        configServer.registerListener(this);
    }
    
    private void refreshConfiguration() {
        config = configServer.getAllConfigs(serviceName, environment);
        System.out.println("[" + serviceName + "] Configuration loaded for environment: " + environment);
        System.out.println("[" + serviceName + "] Active configs: " + config);
    }
    
    @Override
    public void onConfigChange(String service, String env, String key, String oldValue, String newValue) {
        if (serviceName.equals(service) && environment.equals(env)) {
            System.out.println("[" + serviceName + "] Config changed: " + key + " = " + newValue);
            refreshConfiguration();
        }
    }
    
    public void handleRequest() {
        System.out.println("\n[" + serviceName + "] Handling API request");
        
        String rateLimit = config.get("api.rateLimit");
        String timeout = config.get("api.timeout");
        
        System.out.println("[" + serviceName + "] Rate limit: " + rateLimit + " req/sec");
        System.out.println("[" + serviceName + "] Timeout: " + timeout + "ms");
        System.out.println("[" + serviceName + "] Request processed");
    }
}

/**
 * Demonstration of Externalized Configuration Pattern
 */
public class ExternalizedConfigurationPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Externalized Configuration Pattern ===\n");
        
        // Create configuration server
        ConfigurationServer configServer = new ConfigurationServer();
        configServer.printAllConfigs();
        
        System.out.println("\n\n--- Creating Services (DEV Environment) ---\n");
        
        // Services load config from server
        OrderServiceExternalConfig devOrderService = new OrderServiceExternalConfig("dev", configServer);
        APIGatewayExternalConfig devAPIGateway = new APIGatewayExternalConfig("dev", configServer);
        
        System.out.println("\n--- Processing Requests (DEV) ---");
        devOrderService.processOrder("ORD-001");
        devAPIGateway.handleRequest();
        
        System.out.println("\n\n--- Creating Services (PROD Environment) ---\n");
        
        OrderServiceExternalConfig prodOrderService = new OrderServiceExternalConfig("prod", configServer);
        APIGatewayExternalConfig prodAPIGateway = new APIGatewayExternalConfig("prod", configServer);
        
        System.out.println("\n--- Processing Requests (PROD) ---");
        prodOrderService.processOrder("ORD-002");
        prodAPIGateway.handleRequest();
        
        System.out.println("\n\n--- Runtime Configuration Update (No Redeploy!) ---\n");
        
        // Change configuration at runtime
        configServer.updateConfig("OrderService", "dev", "feature.newCheckout", "true");
        
        Thread.sleep(100); // Let listeners process
        
        System.out.println("\n--- Processing Request After Config Change ---");
        devOrderService.processOrder("ORD-003");
        
        System.out.println("\n\n=== Key Benefits ===");
        System.out.println("1. Environment-specific configs (dev vs prod)");
        System.out.println("2. Runtime updates (no redeploy required)");
        System.out.println("3. Centralized management (single source of truth)");
        System.out.println("4. Version control (configs in Git)");
        System.out.println("5. Dynamic refresh (services auto-reload)");
        
        System.out.println("\n=== Common Configuration Types ===");
        System.out.println("- Database connection strings");
        System.out.println("- API endpoints and credentials");
        System.out.println("- Feature flags");
        System.out.println("- Rate limits and timeouts");
        System.out.println("- Circuit breaker thresholds");
        System.out.println("- Logging levels");
        
        System.out.println("\n=== Real-World Tools ===");
        System.out.println("- Spring Cloud Config");
        System.out.println("- Consul (HashiCorp)");
        System.out.println("- etcd");
        System.out.println("- AWS Systems Manager Parameter Store");
        System.out.println("- Azure App Configuration");
        System.out.println("- Google Cloud Secret Manager");
        
        System.out.println("\n\n🎉 CONGRATULATIONS! 🎉");
        System.out.println("You have completed all 150 design patterns!");
    }
}
