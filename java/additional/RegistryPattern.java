package additional;

import java.util.*;

/**
 * REGISTRY PATTERN
 * 
 * Provides a well-known object that other objects can use to find common objects
 * and services. Acts as a global point of access for retrieving objects, similar
 * to Service Locator but more general-purpose.
 * 
 * Benefits:
 * - Centralized object lookup
 * - Reduces tight coupling between components
 * - Global access to shared resources
 * - Simplifies object discovery
 * - Can be scoped (global, thread-local, session)
 * 
 * Use Cases:
 * - Accessing global configuration
 * - Retrieving singleton instances
 * - Managing application-wide resources
 * - Plugin registration and discovery
 * - Thread-local storage management
 */

// Simple Registry
class SimpleRegistry<T> {
    private final Map<String, T> registry = new HashMap<>();
    
    public void register(String key, T value) {
        System.out.println("  📝 Registering: " + key);
        registry.put(key, value);
    }
    
    public T get(String key) {
        System.out.println("  🔍 Looking up: " + key);
        T value = registry.get(key);
        if (value == null) {
            throw new IllegalArgumentException("No entry found for key: " + key);
        }
        return value;
    }
    
    public T getOrDefault(String key, T defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }
    
    public boolean contains(String key) {
        return registry.containsKey(key);
    }
    
    public void unregister(String key) {
        System.out.println("  ❌ Unregistering: " + key);
        registry.remove(key);
    }
    
    public Set<String> listKeys() {
        return new HashSet<>(registry.keySet());
    }
}

// Typed Registry with class-based lookup
class TypedRegistry {
    private final Map<Class<?>, Object> registry = new HashMap<>();
    
    public <T> void register(Class<T> type, T instance) {
        System.out.println("  📝 Registering type: " + type.getSimpleName());
        registry.put(type, instance);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        System.out.println("  🔍 Looking up type: " + type.getSimpleName());
        Object instance = registry.get(type);
        if (instance == null) {
            throw new IllegalArgumentException("No instance registered for: " + type);
        }
        return (T) instance;
    }
    
    public boolean contains(Class<?> type) {
        return registry.containsKey(type);
    }
    
    public <T> void unregister(Class<T> type) {
        System.out.println("  ❌ Unregistering type: " + type.getSimpleName());
        registry.remove(type);
    }
}

// Thread-Local Registry
class ThreadLocalRegistry<T> {
    private final ThreadLocal<Map<String, T>> threadLocalMap = 
        ThreadLocal.withInitial(HashMap::new);
    
    public void register(String key, T value) {
        System.out.println("  📝 [Thread " + Thread.currentThread().getId() + 
                         "] Registering: " + key);
        threadLocalMap.get().put(key, value);
    }
    
    public T get(String key) {
        System.out.println("  🔍 [Thread " + Thread.currentThread().getId() + 
                         "] Looking up: " + key);
        return threadLocalMap.get().get(key);
    }
    
    public void clear() {
        System.out.println("  🧹 [Thread " + Thread.currentThread().getId() + 
                         "] Clearing registry");
        threadLocalMap.get().clear();
    }
}

// Application Configuration Registry
class ConfigurationRegistry {
    private static final ConfigurationRegistry instance = new ConfigurationRegistry();
    private final Map<String, String> config = new HashMap<>();
    
    private ConfigurationRegistry() {
        // Initialize with default config
        config.put("app.name", "MyApplication");
        config.put("app.version", "1.0.0");
        config.put("db.host", "localhost");
        config.put("db.port", "5432");
        config.put("cache.enabled", "true");
    }
    
    public static ConfigurationRegistry getInstance() {
        return instance;
    }
    
    public String get(String key) {
        String value = config.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Configuration not found: " + key);
        }
        return value;
    }
    
    public String get(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    
    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }
    
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
    
    public void set(String key, String value) {
        config.put(key, value);
    }
    
    public void printAll() {
        System.out.println("\n  📋 Configuration:");
        config.forEach((k, v) -> System.out.println("    " + k + " = " + v));
    }
}

// Plugin Registry
interface Plugin {
    String getName();
    String getVersion();
    void execute();
}

class PluginRegistry {
    private final Map<String, Plugin> plugins = new HashMap<>();
    
    public void registerPlugin(Plugin plugin) {
        System.out.println("  🔌 Registering plugin: " + plugin.getName() + 
                         " v" + plugin.getVersion());
        plugins.put(plugin.getName(), plugin);
    }
    
    public Plugin getPlugin(String name) {
        Plugin plugin = plugins.get(name);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + name);
        }
        return plugin;
    }
    
    public List<Plugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    public void executePlugin(String name) {
        System.out.println("\n  ▶️  Executing plugin: " + name);
        Plugin plugin = getPlugin(name);
        plugin.execute();
    }
    
    public void listPlugins() {
        System.out.println("\n  📦 Available plugins:");
        plugins.values().forEach(p -> 
            System.out.println("    • " + p.getName() + " v" + p.getVersion())
        );
    }
}

// Sample Plugins
class LoggingPlugin implements Plugin {
    @Override
    public String getName() { return "Logger"; }
    
    @Override
    public String getVersion() { return "1.2.0"; }
    
    @Override
    public void execute() {
        System.out.println("    📝 Logging plugin: Writing logs...");
    }
}

class CachePlugin implements Plugin {
    @Override
    public String getName() { return "Cache"; }
    
    @Override
    public String getVersion() { return "2.0.1"; }
    
    @Override
    public void execute() {
        System.out.println("    💾 Cache plugin: Managing cache...");
    }
}

class SecurityPlugin implements Plugin {
    @Override
    public String getName() { return "Security"; }
    
    @Override
    public String getVersion() { return "3.1.0"; }
    
    @Override
    public void execute() {
        System.out.println("    🔒 Security plugin: Validating credentials...");
    }
}

// Service Registry
interface Service {
    void start();
    void stop();
}

class ServiceRegistry {
    private final Map<String, Service> services = new HashMap<>();
    private final Set<String> runningServices = new HashSet<>();
    
    public void register(String name, Service service) {
        System.out.println("  📝 Registering service: " + name);
        services.put(name, service);
    }
    
    public Service get(String name) {
        Service service = services.get(name);
        if (service == null) {
            throw new IllegalArgumentException("Service not found: " + name);
        }
        return service;
    }
    
    public void start(String name) {
        System.out.println("\n  ▶️  Starting service: " + name);
        Service service = get(name);
        service.start();
        runningServices.add(name);
    }
    
    public void stop(String name) {
        System.out.println("\n  ⏹️  Stopping service: " + name);
        Service service = get(name);
        service.stop();
        runningServices.remove(name);
    }
    
    public void stopAll() {
        System.out.println("\n  ⏹️  Stopping all services...");
        new ArrayList<>(runningServices).forEach(this::stop);
    }
    
    public boolean isRunning(String name) {
        return runningServices.contains(name);
    }
}

class DatabaseService implements Service {
    @Override
    public void start() {
        System.out.println("    💾 Database service started");
    }
    
    @Override
    public void stop() {
        System.out.println("    💾 Database service stopped");
    }
}

class WebServerService implements Service {
    @Override
    public void start() {
        System.out.println("    🌐 Web server started on port 8080");
    }
    
    @Override
    public void stop() {
        System.out.println("    🌐 Web server stopped");
    }
}

class MessagingService implements Service {
    @Override
    public void start() {
        System.out.println("    📨 Messaging service connected");
    }
    
    @Override
    public void stop() {
        System.out.println("    📨 Messaging service disconnected");
    }
}

// Demo
public class RegistryPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   REGISTRY PATTERN DEMONSTRATION     ║");
        System.out.println("╚══════════════════════════════════════╝");
        
        // Example 1: Simple Registry
        System.out.println("\n1. SIMPLE REGISTRY (Key-Value Storage)");
        System.out.println("───────────────────────────────────────");
        
        SimpleRegistry<String> stringRegistry = new SimpleRegistry<>();
        stringRegistry.register("api.key", "abc123xyz");
        stringRegistry.register("api.url", "https://api.example.com");
        stringRegistry.register("timeout", "30000");
        
        System.out.println("\n  Retrieving values:");
        String apiKey = stringRegistry.get("api.key");
        System.out.println("  ✅ API Key: " + apiKey);
        
        String apiUrl = stringRegistry.get("api.url");
        System.out.println("  ✅ API URL: " + apiUrl);
        
        // Example 2: Typed Registry
        System.out.println("\n\n2. TYPED REGISTRY (Class-Based Lookup)");
        System.out.println("───────────────────────────────────────");
        
        TypedRegistry typedRegistry = new TypedRegistry();
        typedRegistry.register(StringBuilder.class, new StringBuilder("Hello"));
        typedRegistry.register(Random.class, new Random());
        typedRegistry.register(Date.class, new Date());
        
        System.out.println("\n  Retrieving by type:");
        StringBuilder sb = typedRegistry.get(StringBuilder.class);
        System.out.println("  ✅ StringBuilder: " + sb);
        
        Random random = typedRegistry.get(Random.class);
        System.out.println("  ✅ Random number: " + random.nextInt(100));
        
        // Example 3: Configuration Registry
        System.out.println("\n\n3. CONFIGURATION REGISTRY (Application Config)");
        System.out.println("────────────────────────────────────────────────");
        
        ConfigurationRegistry config = ConfigurationRegistry.getInstance();
        config.printAll();
        
        System.out.println("\n  Using configuration:");
        System.out.println("  App: " + config.get("app.name") + " v" + config.get("app.version"));
        System.out.println("  Database: " + config.get("db.host") + ":" + config.getInt("db.port"));
        System.out.println("  Cache enabled: " + config.getBoolean("cache.enabled"));
        
        // Example 4: Plugin Registry
        System.out.println("\n\n4. PLUGIN REGISTRY (Dynamic Plugin System)");
        System.out.println("───────────────────────────────────────────");
        
        PluginRegistry pluginRegistry = new PluginRegistry();
        pluginRegistry.registerPlugin(new LoggingPlugin());
        pluginRegistry.registerPlugin(new CachePlugin());
        pluginRegistry.registerPlugin(new SecurityPlugin());
        
        pluginRegistry.listPlugins();
        
        System.out.println("\n  Executing plugins:");
        pluginRegistry.executePlugin("Logger");
        pluginRegistry.executePlugin("Security");
        pluginRegistry.executePlugin("Cache");
        
        // Example 5: Service Registry
        System.out.println("\n\n5. SERVICE REGISTRY (Service Lifecycle Management)");
        System.out.println("────────────────────────────────────────────────────");
        
        ServiceRegistry serviceRegistry = new ServiceRegistry();
        serviceRegistry.register("database", new DatabaseService());
        serviceRegistry.register("webserver", new WebServerService());
        serviceRegistry.register("messaging", new MessagingService());
        
        System.out.println("\n  Starting services:");
        serviceRegistry.start("database");
        serviceRegistry.start("webserver");
        serviceRegistry.start("messaging");
        
        System.out.println("\n  Service status:");
        System.out.println("    Database running: " + serviceRegistry.isRunning("database"));
        System.out.println("    Webserver running: " + serviceRegistry.isRunning("webserver"));
        
        System.out.println("\n  Stopping services:");
        serviceRegistry.stopAll();
        
        // Example 6: Thread-Local Registry
        System.out.println("\n\n6. THREAD-LOCAL REGISTRY (Per-Thread Storage)");
        System.out.println("───────────────────────────────────────────────");
        
        ThreadLocalRegistry<String> threadLocalReg = new ThreadLocalRegistry<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            threadLocalReg.register("user.name", "Alice");
            threadLocalReg.register("request.id", "REQ-001");
            System.out.println("  ✅ Thread 1 user: " + threadLocalReg.get("user.name"));
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            threadLocalReg.register("user.name", "Bob");
            threadLocalReg.register("request.id", "REQ-002");
            System.out.println("  ✅ Thread 2 user: " + threadLocalReg.get("user.name"));
        });
        
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        
        System.out.println("\n✅ Registry Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Centralized object lookup");
        System.out.println("  • Global access to shared resources");
        System.out.println("  • Reduces coupling between components");
        System.out.println("  • Supports multiple scopes (global, thread-local)");
        System.out.println("  • Simplifies object discovery");
        
        System.out.println("\n🆚 Registry vs Service Locator:");
        System.out.println("  • Registry: General-purpose object storage and retrieval");
        System.out.println("  • Service Locator: Specifically for service discovery");
        System.out.println("  • Registry is broader, Service Locator is more focused");
        
        System.out.println("\n⚠️  Note: Use dependency injection when possible");
        System.out.println("   Registry/Service Locator can hide dependencies");
    }
}
