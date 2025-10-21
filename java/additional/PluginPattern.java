package additional;

import java.util.*;

/**
 * Plugin Pattern
 * Allows adding functionality through plugins without modifying core code.
 */
public class PluginPattern {
    
    // Plugin interface
    interface Plugin {
        String getName();
        String getVersion();
        void initialize();
        void execute();
        void shutdown();
    }
    
    // Base Plugin class
    static abstract class BasePlugin implements Plugin {
        protected final String name;
        protected final String version;
        protected boolean initialized = false;
        
        public BasePlugin(String name, String version) {
            this.name = name;
            this.version = version;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public String getVersion() {
            return version;
        }
        
        @Override
        public void initialize() {
            System.out.println("  🔌 Initializing plugin: " + name + " v" + version);
            initialized = true;
        }
        
        @Override
        public void shutdown() {
            System.out.println("  🔌 Shutting down plugin: " + name);
            initialized = false;
        }
    }
    
    // Concrete Plugins
    static class LoggingPlugin extends BasePlugin {
        private List<String> logs = new ArrayList<>();
        
        public LoggingPlugin() {
            super("Logging Plugin", "1.0");
        }
        
        @Override
        public void execute() {
            String logEntry = "[" + new Date() + "] Log entry created";
            logs.add(logEntry);
            System.out.println("  📝 LoggingPlugin: " + logEntry);
        }
        
        public List<String> getLogs() {
            return new ArrayList<>(logs);
        }
    }
    
    static class CachePlugin extends BasePlugin {
        private Map<String, Object> cache = new HashMap<>();
        
        public CachePlugin() {
            super("Cache Plugin", "2.0");
        }
        
        @Override
        public void execute() {
            // Demonstrate caching
            cache.put("user:123", "User Data");
            System.out.println("  💾 CachePlugin: Cached data (size: " + cache.size() + ")");
        }
        
        public void put(String key, Object value) {
            cache.put(key, value);
        }
        
        public Object get(String key) {
            return cache.get(key);
        }
    }
    
    static class SecurityPlugin extends BasePlugin {
        public SecurityPlugin() {
            super("Security Plugin", "1.5");
        }
        
        @Override
        public void execute() {
            System.out.println("  🔒 SecurityPlugin: Performing security check");
            // Security operations
        }
        
        public boolean authenticate(String token) {
            System.out.println("  🔒 SecurityPlugin: Authenticating token");
            return token != null && token.startsWith("valid_");
        }
    }
    
    static class AnalyticsPlugin extends BasePlugin {
        private int eventCount = 0;
        
        public AnalyticsPlugin() {
            super("Analytics Plugin", "3.0");
        }
        
        @Override
        public void execute() {
            eventCount++;
            System.out.println("  📊 AnalyticsPlugin: Tracking event #" + eventCount);
        }
        
        public int getEventCount() {
            return eventCount;
        }
    }
    
    // Plugin Manager
    static class PluginManager {
        private final Map<String, Plugin> plugins = new LinkedHashMap<>();
        private final List<String> loadOrder = new ArrayList<>();
        
        public void registerPlugin(Plugin plugin) {
            plugins.put(plugin.getName(), plugin);
            loadOrder.add(plugin.getName());
            System.out.println("✅ Registered plugin: " + plugin.getName() + 
                             " v" + plugin.getVersion());
        }
        
        public void loadPlugin(String name) {
            Plugin plugin = plugins.get(name);
            if (plugin != null) {
                plugin.initialize();
            } else {
                System.out.println("❌ Plugin not found: " + name);
            }
        }
        
        public void loadAllPlugins() {
            System.out.println("\n🔄 Loading all plugins...");
            for (String name : loadOrder) {
                loadPlugin(name);
            }
        }
        
        public void executePlugin(String name) {
            Plugin plugin = plugins.get(name);
            if (plugin != null) {
                plugin.execute();
            } else {
                System.out.println("❌ Plugin not found: " + name);
            }
        }
        
        public void executeAllPlugins() {
            System.out.println("\n▶️  Executing all plugins:");
            for (String name : loadOrder) {
                executePlugin(name);
            }
        }
        
        public void unloadPlugin(String name) {
            Plugin plugin = plugins.get(name);
            if (plugin != null) {
                plugin.shutdown();
                plugins.remove(name);
                loadOrder.remove(name);
                System.out.println("✅ Unloaded plugin: " + name);
            }
        }
        
        public void unloadAllPlugins() {
            System.out.println("\n🔄 Unloading all plugins...");
            for (String name : new ArrayList<>(loadOrder)) {
                unloadPlugin(name);
            }
        }
        
        public Plugin getPlugin(String name) {
            return plugins.get(name);
        }
        
        public List<Plugin> getAllPlugins() {
            return new ArrayList<>(plugins.values());
        }
        
        public void listPlugins() {
            System.out.println("\n📋 Installed Plugins:");
            for (Plugin plugin : plugins.values()) {
                System.out.println("  • " + plugin.getName() + " v" + plugin.getVersion());
            }
        }
    }
    
    // Application using plugins
    static class Application {
        private final PluginManager pluginManager;
        
        public Application() {
            this.pluginManager = new PluginManager();
        }
        
        public void installPlugin(Plugin plugin) {
            pluginManager.registerPlugin(plugin);
        }
        
        public void start() {
            System.out.println("\n🚀 Starting application...");
            pluginManager.loadAllPlugins();
            System.out.println("✅ Application started");
        }
        
        public void processRequest(String request) {
            System.out.println("\n🔹 Processing request: " + request);
            
            // Execute plugins in order
            SecurityPlugin security = (SecurityPlugin) pluginManager.getPlugin("Security Plugin");
            if (security != null && security.authenticate("valid_token")) {
                System.out.println("  ✅ Authentication successful");
            }
            
            CachePlugin cache = (CachePlugin) pluginManager.getPlugin("Cache Plugin");
            if (cache != null) {
                cache.execute();
            }
            
            LoggingPlugin logging = (LoggingPlugin) pluginManager.getPlugin("Logging Plugin");
            if (logging != null) {
                logging.execute();
            }
            
            AnalyticsPlugin analytics = (AnalyticsPlugin) pluginManager.getPlugin("Analytics Plugin");
            if (analytics != null) {
                analytics.execute();
            }
            
            System.out.println("  ✅ Request processed");
        }
        
        public void stop() {
            System.out.println("\n🛑 Stopping application...");
            pluginManager.unloadAllPlugins();
            System.out.println("✅ Application stopped");
        }
        
        public PluginManager getPluginManager() {
            return pluginManager;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Plugin Pattern Demo ===\n");
        
        // Create application
        Application app = new Application();
        
        // Install plugins
        System.out.println("1. Installing Plugins:");
        app.installPlugin(new LoggingPlugin());
        app.installPlugin(new CachePlugin());
        app.installPlugin(new SecurityPlugin());
        app.installPlugin(new AnalyticsPlugin());
        
        app.getPluginManager().listPlugins();
        
        System.out.println("\n" + "=".repeat(50));
        
        // Start application
        System.out.println("\n2. Application Lifecycle:");
        app.start();
        
        System.out.println("\n" + "=".repeat(50));
        
        // Process requests
        System.out.println("\n3. Processing Requests:");
        app.processRequest("GET /api/users");
        app.processRequest("POST /api/orders");
        
        System.out.println("\n" + "=".repeat(50));
        
        // Check plugin status
        System.out.println("\n4. Plugin Status:");
        AnalyticsPlugin analytics = (AnalyticsPlugin) 
            app.getPluginManager().getPlugin("Analytics Plugin");
        if (analytics != null) {
            System.out.println("Total events tracked: " + analytics.getEventCount());
        }
        
        LoggingPlugin logging = (LoggingPlugin) 
            app.getPluginManager().getPlugin("Logging Plugin");
        if (logging != null) {
            System.out.println("Total logs: " + logging.getLogs().size());
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // Stop application
        System.out.println("\n5. Shutdown:");
        app.stop();
        
        System.out.println("\n--- Plugin Architecture ---");
        System.out.println("┌─────────────────────┐");
        System.out.println("│   Core Application  │");
        System.out.println("└──────────┬──────────┘");
        System.out.println("           │");
        System.out.println("┌──────────▼──────────┐");
        System.out.println("│   Plugin Manager    │");
        System.out.println("└──────────┬──────────┘");
        System.out.println("           │");
        System.out.println("     ┌─────┴─────┬─────────┬─────────┐");
        System.out.println("     │           │         │         │");
        System.out.println("┌────▼────┐ ┌────▼───┐ ┌──▼───┐ ┌───▼────┐");
        System.out.println("│ Logging │ │ Cache  │ │Security│Analytics│");
        System.out.println("│ Plugin  │ │ Plugin │ │ Plugin │ Plugin │");
        System.out.println("└─────────┘ └────────┘ └────────┘ └────────┘");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Extensibility without modifying core");
        System.out.println("✓ Dynamic loading/unloading");
        System.out.println("✓ Independent plugin development");
        System.out.println("✓ Feature modularity");
        System.out.println("✓ Hot-swappable components");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• IDE extensions (VS Code, IntelliJ)");
        System.out.println("• Web browsers (Chrome extensions)");
        System.out.println("• Content management systems (WordPress)");
        System.out.println("• Media players (VLC plugins)");
        System.out.println("• Build tools (Maven, Gradle)");
        
        System.out.println("\n--- Real-World Examples ---");
        System.out.println("• Eclipse Plugin Framework");
        System.out.println("• Jenkins Plugins");
        System.out.println("• Minecraft Mods");
        System.out.println("• Photoshop Filters");
    }
}
