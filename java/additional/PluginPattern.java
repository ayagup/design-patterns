package additional;

import java.util.*;
import java.lang.reflect.*;

/**
 * Plugin Pattern
 * 
 * Intent: Allows applications to be extended at runtime by loading
 * external modules (plugins) without recompiling.
 * 
 * Motivation:
 * Enables extensibility without modifying core code.
 * Third-party developers can add features.
 * Load/unload functionality dynamically.
 * Supports modular architecture.
 * 
 * Applicability:
 * - Applications need runtime extensibility
 * - Third-party extensions
 * - Modular architectures
 * - Optional features
 */

/**
 * Example 1: Basic Plugin System
 * 
 * Simple plugin interface and loader
 */
interface Plugin {
    String getName();
    String getVersion();
    void initialize();
    void execute();
    void shutdown();
}

class PluginManager {
    private final Map<String, Plugin> plugins;
    
    public PluginManager() {
        this.plugins = new HashMap<>();
    }
    
    public void registerPlugin(Plugin plugin) {
        plugins.put(plugin.getName(), plugin);
        plugin.initialize();
        System.out.println("  [PluginManager] Registered: " + plugin.getName() + 
                         " v" + plugin.getVersion());
    }
    
    public void unregisterPlugin(String name) {
        Plugin plugin = plugins.remove(name);
        if (plugin != null) {
            plugin.shutdown();
            System.out.println("  [PluginManager] Unregistered: " + name);
        }
    }
    
    public void executePlugin(String name) {
        Plugin plugin = plugins.get(name);
        if (plugin != null) {
            plugin.execute();
        } else {
            System.out.println("  [PluginManager] Plugin not found: " + name);
        }
    }
    
    public List<Plugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
}

class LoggingPlugin implements Plugin {
    @Override
    public String getName() { return "Logging"; }
    
    @Override
    public String getVersion() { return "1.0"; }
    
    @Override
    public void initialize() {
        System.out.println("  [LoggingPlugin] Initializing logging system...");
    }
    
    @Override
    public void execute() {
        System.out.println("  [LoggingPlugin] Logging: Application event occurred");
    }
    
    @Override
    public void shutdown() {
        System.out.println("  [LoggingPlugin] Shutting down logging system");
    }
}

class SecurityPlugin implements Plugin {
    @Override
    public String getName() { return "Security"; }
    
    @Override
    public String getVersion() { return "2.1"; }
    
    @Override
    public void initialize() {
        System.out.println("  [SecurityPlugin] Loading security policies...");
    }
    
    @Override
    public void execute() {
        System.out.println("  [SecurityPlugin] Checking authentication...");
        System.out.println("  [SecurityPlugin] Access granted");
    }
    
    @Override
    public void shutdown() {
        System.out.println("  [SecurityPlugin] Closing security module");
    }
}

/**
 * Example 2: Service Provider Interface (SPI)
 * 
 * Java SPI-style plugin discovery
 */
interface DataProcessor {
    String getFormat();
    void process(String data);
}

class JsonProcessor implements DataProcessor {
    @Override
    public String getFormat() { return "JSON"; }
    
    @Override
    public void process(String data) {
        System.out.println("  [JsonProcessor] Processing JSON: " + data);
        System.out.println("  [JsonProcessor] Parsed successfully");
    }
}

class XmlProcessor implements DataProcessor {
    @Override
    public String getFormat() { return "XML"; }
    
    @Override
    public void process(String data) {
        System.out.println("  [XmlProcessor] Processing XML: " + data);
        System.out.println("  [XmlProcessor] Validated against schema");
    }
}

class DataProcessorRegistry {
    private final Map<String, DataProcessor> processors;
    
    public DataProcessorRegistry() {
        this.processors = new HashMap<>();
    }
    
    public void register(DataProcessor processor) {
        processors.put(processor.getFormat(), processor);
        System.out.println("  [Registry] Registered processor for: " + 
                         processor.getFormat());
    }
    
    public void process(String format, String data) {
        DataProcessor processor = processors.get(format);
        if (processor != null) {
            processor.process(data);
        } else {
            System.out.println("  [Registry] No processor for format: " + format);
        }
    }
}

/**
 * Example 3: Command Plugin System
 * 
 * Plugins as executable commands
 */
interface CommandPlugin {
    String getCommandName();
    String getDescription();
    void execute(String[] args);
}

class HelpCommand implements CommandPlugin {
    @Override
    public String getCommandName() { return "help"; }
    
    @Override
    public String getDescription() { return "Shows help information"; }
    
    @Override
    public void execute(String[] args) {
        System.out.println("  [HelpCommand] Available commands:");
        System.out.println("  - help: Show this help");
        System.out.println("  - status: Check system status");
    }
}

class StatusCommand implements CommandPlugin {
    @Override
    public String getCommandName() { return "status"; }
    
    @Override
    public String getDescription() { return "Shows system status"; }
    
    @Override
    public void execute(String[] args) {
        System.out.println("  [StatusCommand] System Status:");
        System.out.println("  - CPU: 45%");
        System.out.println("  - Memory: 2.3 GB / 8 GB");
        System.out.println("  - Uptime: 24 hours");
    }
}

class CommandRegistry {
    private final Map<String, CommandPlugin> commands;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();
    }
    
    public void registerCommand(CommandPlugin command) {
        commands.put(command.getCommandName(), command);
        System.out.println("  [CommandRegistry] Registered: " + 
                         command.getCommandName() + " - " + command.getDescription());
    }
    
    public void executeCommand(String name, String[] args) {
        CommandPlugin command = commands.get(name);
        if (command != null) {
            command.execute(args);
        } else {
            System.out.println("  [CommandRegistry] Unknown command: " + name);
        }
    }
}

/**
 * Example 4: Reflective Plugin Loader
 * 
 * Loads plugins using reflection
 */
interface PluginComponent {
    void onLoad();
    void onUnload();
}

class ReflectivePluginLoader {
    public PluginComponent loadPlugin(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            
            if (!PluginComponent.class.isAssignableFrom(clazz)) {
                System.out.println("  [Loader] Class does not implement PluginComponent");
                return null;
            }
            
            PluginComponent plugin = (PluginComponent) clazz.getDeclaredConstructor().newInstance();
            plugin.onLoad();
            
            System.out.println("  [Loader] Loaded plugin: " + className);
            return plugin;
            
        } catch (Exception e) {
            System.out.println("  [Loader] Failed to load: " + e.getMessage());
            return null;
        }
    }
}

class SamplePlugin implements PluginComponent {
    @Override
    public void onLoad() {
        System.out.println("  [SamplePlugin] Plugin loaded successfully");
    }
    
    @Override
    public void onUnload() {
        System.out.println("  [SamplePlugin] Plugin unloaded");
    }
}

/**
 * Example 5: Event-Based Plugin System
 * 
 * Plugins respond to application events
 */
interface EventListener {
    String getEventType();
    void onEvent(Event event);
}

class Event {
    private final String type;
    private final Map<String, Object> data;
    
    public Event(String type) {
        this.type = type;
        this.data = new HashMap<>();
    }
    
    public String getType() { return type; }
    
    public void setData(String key, Object value) {
        data.put(key, value);
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
}

class UserLoginListener implements EventListener {
    @Override
    public String getEventType() { return "user.login"; }
    
    @Override
    public void onEvent(Event event) {
        String username = (String) event.getData("username");
        System.out.println("  [UserLoginListener] User logged in: " + username);
        System.out.println("  [UserLoginListener] Updating last login time");
    }
}

class AuditLogListener implements EventListener {
    @Override
    public String getEventType() { return "user.login"; }
    
    @Override
    public void onEvent(Event event) {
        String username = (String) event.getData("username");
        System.out.println("  [AuditLogListener] Recording audit: " + username + " logged in");
    }
}

class EventBus {
    private final Map<String, List<EventListener>> listeners;
    
    public EventBus() {
        this.listeners = new HashMap<>();
    }
    
    public void registerListener(EventListener listener) {
        listeners.computeIfAbsent(listener.getEventType(), k -> new ArrayList<>())
                 .add(listener);
        System.out.println("  [EventBus] Registered listener for: " + 
                         listener.getEventType());
    }
    
    public void publishEvent(Event event) {
        List<EventListener> eventListeners = listeners.get(event.getType());
        if (eventListeners != null) {
            System.out.println("  [EventBus] Publishing event: " + event.getType());
            for (EventListener listener : eventListeners) {
                listener.onEvent(event);
            }
        }
    }
}

/**
 * Demonstration of the Plugin Pattern
 */
public class PluginPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Plugin Pattern Demo ===\n");
        
        // Example 1: Basic Plugin System
        System.out.println("1. Basic Plugin System:");
        PluginManager manager = new PluginManager();
        
        manager.registerPlugin(new LoggingPlugin());
        manager.registerPlugin(new SecurityPlugin());
        
        manager.executePlugin("Logging");
        manager.executePlugin("Security");
        
        manager.unregisterPlugin("Logging");
        
        // Example 2: Data Processor Registry
        System.out.println("\n2. Service Provider Interface:");
        DataProcessorRegistry registry = new DataProcessorRegistry();
        
        registry.register(new JsonProcessor());
        registry.register(new XmlProcessor());
        
        registry.process("JSON", "{\"name\": \"Alice\"}");
        registry.process("XML", "<user><name>Bob</name></user>");
        
        // Example 3: Command Plugin System
        System.out.println("\n3. Command Plugin System:");
        CommandRegistry commandRegistry = new CommandRegistry();
        
        commandRegistry.registerCommand(new HelpCommand());
        commandRegistry.registerCommand(new StatusCommand());
        
        commandRegistry.executeCommand("help", new String[]{});
        commandRegistry.executeCommand("status", new String[]{});
        
        // Example 4: Reflective Plugin Loader
        System.out.println("\n4. Reflective Plugin Loader:");
        ReflectivePluginLoader loader = new ReflectivePluginLoader();
        
        PluginComponent plugin = loader.loadPlugin("additional.SamplePlugin");
        if (plugin != null) {
            plugin.onUnload();
        }
        
        // Example 5: Event-Based Plugin System
        System.out.println("\n5. Event-Based Plugin System:");
        EventBus eventBus = new EventBus();
        
        eventBus.registerListener(new UserLoginListener());
        eventBus.registerListener(new AuditLogListener());
        
        Event loginEvent = new Event("user.login");
        loginEvent.setData("username", "alice");
        eventBus.publishEvent(loginEvent);
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Runtime extensibility");
        System.out.println("✓ Third-party extensions");
        System.out.println("✓ No core code changes");
        System.out.println("✓ Modular architecture");
        System.out.println("✓ Optional features");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• IDE plugins (VS Code, IntelliJ)");
        System.out.println("• Browser extensions");
        System.out.println("• Application modules");
        System.out.println("• Service providers");
        System.out.println("• Theme systems");
    }
}
