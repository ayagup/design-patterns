"""Plugin Pattern - Extends functionality without modifying core"""
from abc import ABC, abstractmethod

class Plugin(ABC):
    @abstractmethod
    def execute(self, data):
        pass
    
    @abstractmethod
    def get_name(self):
        pass

class LoggingPlugin(Plugin):
    def execute(self, data):
        print(f"[Logging Plugin] Processing: {data}")
        return data
    
    def get_name(self):
        return "logging"

class ValidationPlugin(Plugin):
    def execute(self, data):
        if not data:
            raise ValueError("Data cannot be empty")
        print(f"[Validation Plugin] Data is valid")
        return data
    
    def get_name(self):
        return "validation"

class TransformPlugin(Plugin):
    def execute(self, data):
        transformed = data.upper() if isinstance(data, str) else data
        print(f"[Transform Plugin] Transformed: {transformed}")
        return transformed
    
    def get_name(self):
        return "transform"

class PluginManager:
    def __init__(self):
        self.plugins = {}
    
    def register(self, plugin):
        self.plugins[plugin.get_name()] = plugin
        print(f"Registered plugin: {plugin.get_name()}")
    
    def unregister(self, plugin_name):
        if plugin_name in self.plugins:
            del self.plugins[plugin_name]
    
    def execute_all(self, data):
        result = data
        for plugin in self.plugins.values():
            result = plugin.execute(result)
        return result

if __name__ == "__main__":
    manager = PluginManager()
    
    # Register plugins
    manager.register(LoggingPlugin())
    manager.register(ValidationPlugin())
    manager.register(TransformPlugin())
    
    # Execute all plugins
    result = manager.execute_all("hello world")
    print(f"\nFinal result: {result}")
