package architectural;

import java.util.*;
import java.util.concurrent.*;

/**
 * Microkernel Architecture Pattern
 * ==================================
 * 
 * Intent:
 * Provides a minimal core system with extensibility through plugins. The
 * microkernel provides basic services while plugins provide extended
 * functionality that can be added, removed, or updated independently.
 * 
 * Also Known As:
 * - Plugin Architecture
 * - Plug-in Pattern
 * 
 * Motivation:
 * - Separate minimal core functionality from extended features
 * - Enable third-party extensions without core changes
 * - Allow feature updates without system restart
 * - Support product customization per customer
 * 
 * Applicability:
 * - IDE systems (Eclipse, VS Code, IntelliJ)
 * - Web browsers (Chrome extensions, Firefox add-ons)
 * - Content management systems
 * - Game engines with mod support
 * - Operating systems (device drivers as plugins)
 * 
 * Structure:
 * Microkernel (Core) -> Plugin Registry -> Plugins
 * 
 * Participants:
 * - Microkernel: Minimal core system with plugin management
 * - Plugin Registry: Manages plugin lifecycle
 * - Plugin Interface: Contract for plugins
 * - Concrete Plugins: Implement extended functionality
 * 
 * Benefits:
 * - Extensibility: Add features without modifying core
 * - Flexibility: Enable/disable features at runtime
 * - Isolation: Plugin failures don't crash core
 * - Customization: Different configurations for different users
 */

// ============================================================================
// CORE: PLUGIN INTERFACE
// ============================================================================

interface Plugin {
    String getName();
    String getVersion();
    String getDescription();
    
    void initialize(PluginContext context);
    void execute(Map<String, Object> parameters);
    void shutdown();
}

// Plugin context provides services to plugins
class PluginContext {
    private final Map<String, Object> services = new ConcurrentHashMap<>();
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();
    
    public void registerService(String name, Object service) {
        services.put(name, service);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getService(String name) {
        return (T) services.get(name);
    }
    
    public void setConfig(String key, Object value) {
        configuration.put(key, value);
    }
    
    public Object getConfig(String key) {
        return configuration.get(key);
    }
    
    public void log(String message) {
        System.out.println("[Plugin Context] " + message);
    }
}

// ============================================================================
// CORE: MICROKERNEL (PLUGIN REGISTRY)
// ============================================================================

class Microkernel {
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final PluginContext context;
    private final Set<String> enabledPlugins = ConcurrentHashMap.newKeySet();
    
    public Microkernel() {
        this.context = new PluginContext();
        initializeCoreServices();
    }
    
    private void initializeCoreServices() {
        // Core services available to all plugins
        context.registerService("logger", new LoggerService());
        context.registerService("config", new ConfigService());
        System.out.println("[Microkernel] Core services initialized");
    }
    
    public void registerPlugin(Plugin plugin) {
        String name = plugin.getName();
        if (plugins.containsKey(name)) {
            System.out.println("[Microkernel] Plugin already registered: " + name);
            return;
        }
        
        System.out.println("[Microkernel] Registering plugin: " + name + " v" + plugin.getVersion());
        plugins.put(name, plugin);
        
        try {
            plugin.initialize(context);
            enabledPlugins.add(name);
            System.out.println("[Microkernel] Plugin initialized: " + name);
        } catch (Exception e) {
            System.err.println("[Microkernel] Failed to initialize plugin " + name + ": " + e.getMessage());
        }
    }
    
    public void unregisterPlugin(String pluginName) {
        Plugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            System.out.println("[Microkernel] Plugin not found: " + pluginName);
            return;
        }
        
        System.out.println("[Microkernel] Unregistering plugin: " + pluginName);
        
        try {
            plugin.shutdown();
            plugins.remove(pluginName);
            enabledPlugins.remove(pluginName);
            System.out.println("[Microkernel] Plugin unregistered: " + pluginName);
        } catch (Exception e) {
            System.err.println("[Microkernel] Error unregistering plugin " + pluginName + ": " + e.getMessage());
        }
    }
    
    public void executePlugin(String pluginName, Map<String, Object> parameters) {
        if (!enabledPlugins.contains(pluginName)) {
            System.out.println("[Microkernel] Plugin not enabled: " + pluginName);
            return;
        }
        
        Plugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            System.out.println("[Microkernel] Plugin not found: " + pluginName);
            return;
        }
        
        System.out.println("[Microkernel] Executing plugin: " + pluginName);
        
        try {
            plugin.execute(parameters);
        } catch (Exception e) {
            System.err.println("[Microkernel] Plugin execution failed for " + pluginName + ": " + e.getMessage());
        }
    }
    
    public void enablePlugin(String pluginName) {
        if (plugins.containsKey(pluginName)) {
            enabledPlugins.add(pluginName);
            System.out.println("[Microkernel] Plugin enabled: " + pluginName);
        }
    }
    
    public void disablePlugin(String pluginName) {
        enabledPlugins.remove(pluginName);
        System.out.println("[Microkernel] Plugin disabled: " + pluginName);
    }
    
    public List<String> listPlugins() {
        return new ArrayList<>(plugins.keySet());
    }
    
    public List<String> listEnabledPlugins() {
        return new ArrayList<>(enabledPlugins);
    }
    
    public Plugin getPlugin(String name) {
        return plugins.get(name);
    }
}

// ============================================================================
// CORE SERVICES
// ============================================================================

class LoggerService {
    public void log(String level, String message) {
        System.out.println("[Logger/" + level + "] " + message);
    }
}

class ConfigService {
    private final Map<String, String> config = new HashMap<>();
    
    public ConfigService() {
        config.put("app.name", "Microkernel System");
        config.put("app.version", "1.0");
    }
    
    public String get(String key) {
        return config.get(key);
    }
    
    public void set(String key, String value) {
        config.put(key, value);
    }
}

// ============================================================================
// EXAMPLE PLUGINS
// ============================================================================

// Plugin 1: Data Export Plugin
class ExportPlugin implements Plugin {
    private PluginContext context;
    
    @Override
    public String getName() { return "Export"; }
    
    @Override
    public String getVersion() { return "1.0.0"; }
    
    @Override
    public String getDescription() { return "Exports data to various formats"; }
    
    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        context.log("Export plugin initialized");
    }
    
    @Override
    public void execute(Map<String, Object> parameters) {
        String format = (String) parameters.get("format");
        Object data = parameters.get("data");
        
        System.out.println("  [Export Plugin] Exporting data to " + format);
        System.out.println("  [Export Plugin] Data: " + data);
        
        LoggerService logger = context.getService("logger");
        logger.log("INFO", "Data exported successfully to " + format);
    }
    
    @Override
    public void shutdown() {
        context.log("Export plugin shutting down");
    }
}

// Plugin 2: Validation Plugin
class ValidationPlugin implements Plugin {
    private PluginContext context;
    
    @Override
    public String getName() { return "Validation"; }
    
    @Override
    public String getVersion() { return "2.1.0"; }
    
    @Override
    public String getDescription() { return "Validates data against rules"; }
    
    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        context.log("Validation plugin initialized");
    }
    
    @Override
    public void execute(Map<String, Object> parameters) {
        String type = (String) parameters.get("type");
        String value = (String) parameters.get("value");
        
        System.out.println("  [Validation Plugin] Validating " + type + ": " + value);
        
        boolean valid = false;
        switch (type) {
            case "email":
                valid = value.contains("@");
                break;
            case "phone":
                valid = value.matches("\\d{10}");
                break;
            case "url":
                valid = value.startsWith("http");
                break;
            default:
                valid = true;
        }
        
        System.out.println("  [Validation Plugin] Result: " + (valid ? "VALID" : "INVALID"));
    }
    
    @Override
    public void shutdown() {
        context.log("Validation plugin shutting down");
    }
}

// Plugin 3: Compression Plugin
class CompressionPlugin implements Plugin {
    private PluginContext context;
    
    @Override
    public String getName() { return "Compression"; }
    
    @Override
    public String getVersion() { return "1.5.0"; }
    
    @Override
    public String getDescription() { return "Compresses and decompresses data"; }
    
    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        context.log("Compression plugin initialized");
    }
    
    @Override
    public void execute(Map<String, Object> parameters) {
        String action = (String) parameters.get("action");
        String data = (String) parameters.get("data");
        
        System.out.println("  [Compression Plugin] Action: " + action);
        System.out.println("  [Compression Plugin] Original size: " + data.length() + " bytes");
        
        if ("compress".equals(action)) {
            int compressedSize = data.length() / 2; // Simulated
            System.out.println("  [Compression Plugin] Compressed size: " + compressedSize + " bytes");
            System.out.println("  [Compression Plugin] Compression ratio: 50%");
        } else {
            System.out.println("  [Compression Plugin] Decompressed successfully");
        }
    }
    
    @Override
    public void shutdown() {
        context.log("Compression plugin shutting down");
    }
}

// Plugin 4: Notification Plugin
class NotificationPlugin implements Plugin {
    private PluginContext context;
    
    @Override
    public String getName() { return "Notification"; }
    
    @Override
    public String getVersion() { return "3.0.0"; }
    
    @Override
    public String getDescription() { return "Sends notifications via multiple channels"; }
    
    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        context.log("Notification plugin initialized");
    }
    
    @Override
    public void execute(Map<String, Object> parameters) {
        String channel = (String) parameters.get("channel");
        String message = (String) parameters.get("message");
        String recipient = (String) parameters.get("recipient");
        
        System.out.println("  [Notification Plugin] Channel: " + channel);
        System.out.println("  [Notification Plugin] Recipient: " + recipient);
        System.out.println("  [Notification Plugin] Message: " + message);
        System.out.println("  [Notification Plugin] Notification sent successfully");
    }
    
    @Override
    public void shutdown() {
        context.log("Notification plugin shutting down");
    }
}

// Plugin 5: Analytics Plugin
class AnalyticsPlugin implements Plugin {
    private PluginContext context;
    private int eventCount = 0;
    
    @Override
    public String getName() { return "Analytics"; }
    
    @Override
    public String getVersion() { return "1.2.0"; }
    
    @Override
    public String getDescription() { return "Tracks and analyzes user events"; }
    
    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        context.log("Analytics plugin initialized");
    }
    
    @Override
    public void execute(Map<String, Object> parameters) {
        String event = (String) parameters.get("event");
        String userId = (String) parameters.get("userId");
        
        eventCount++;
        
        System.out.println("  [Analytics Plugin] Event: " + event);
        System.out.println("  [Analytics Plugin] User: " + userId);
        System.out.println("  [Analytics Plugin] Total events tracked: " + eventCount);
    }
    
    @Override
    public void shutdown() {
        System.out.println("  [Analytics Plugin] Final event count: " + eventCount);
        context.log("Analytics plugin shutting down");
    }
}

/**
 * Demonstration of Microkernel Architecture Pattern
 */
public class MicrokernelPattern {
    public static void main(String[] args) {
        demonstrateMicrokernel();
    }
    
    private static void demonstrateMicrokernel() {
        System.out.println("=== Microkernel Architecture: Plugin System ===\n");
        
        // Create microkernel
        Microkernel kernel = new Microkernel();
        
        System.out.println("\n--- Registering Plugins ---\n");
        
        // Register plugins
        kernel.registerPlugin(new ExportPlugin());
        kernel.registerPlugin(new ValidationPlugin());
        kernel.registerPlugin(new CompressionPlugin());
        kernel.registerPlugin(new NotificationPlugin());
        kernel.registerPlugin(new AnalyticsPlugin());
        
        System.out.println("\n--- Available Plugins ---");
        System.out.println("Total plugins: " + kernel.listPlugins().size());
        for (String pluginName : kernel.listPlugins()) {
            Plugin plugin = kernel.getPlugin(pluginName);
            System.out.println("  - " + pluginName + " v" + plugin.getVersion() + 
                             ": " + plugin.getDescription());
        }
        
        System.out.println("\n--- Executing Plugins ---\n");
        
        // Execute Export plugin
        Map<String, Object> exportParams = new HashMap<>();
        exportParams.put("format", "JSON");
        exportParams.put("data", "{name: 'John', age: 30}");
        kernel.executePlugin("Export", exportParams);
        
        System.out.println();
        
        // Execute Validation plugin
        Map<String, Object> validationParams = new HashMap<>();
        validationParams.put("type", "email");
        validationParams.put("value", "user@example.com");
        kernel.executePlugin("Validation", validationParams);
        
        System.out.println();
        
        // Execute Compression plugin
        Map<String, Object> compressionParams = new HashMap<>();
        compressionParams.put("action", "compress");
        compressionParams.put("data", "This is a long string that needs compression to save space");
        kernel.executePlugin("Compression", compressionParams);
        
        System.out.println();
        
        // Execute Notification plugin
        Map<String, Object> notificationParams = new HashMap<>();
        notificationParams.put("channel", "email");
        notificationParams.put("recipient", "admin@example.com");
        notificationParams.put("message", "System maintenance scheduled");
        kernel.executePlugin("Notification", notificationParams);
        
        System.out.println();
        
        // Execute Analytics plugin
        Map<String, Object> analyticsParams = new HashMap<>();
        analyticsParams.put("event", "user_login");
        analyticsParams.put("userId", "USER-123");
        kernel.executePlugin("Analytics", analyticsParams);
        
        System.out.println("\n--- Plugin Management ---\n");
        
        // Disable a plugin
        kernel.disablePlugin("Compression");
        
        // Try to execute disabled plugin
        kernel.executePlugin("Compression", compressionParams);
        
        // Re-enable plugin
        kernel.enablePlugin("Compression");
        System.out.println();
        kernel.executePlugin("Compression", compressionParams);
        
        System.out.println("\n--- Unregistering Plugins ---\n");
        
        // Unregister a plugin
        kernel.unregisterPlugin("Compression");
        
        // Try to execute unregistered plugin
        System.out.println();
        kernel.executePlugin("Compression", compressionParams);
        
        System.out.println("\n--- Final Plugin List ---");
        System.out.println("Remaining plugins: " + kernel.listPlugins().size());
        for (String pluginName : kernel.listPlugins()) {
            System.out.println("  - " + pluginName);
        }
    }
}
