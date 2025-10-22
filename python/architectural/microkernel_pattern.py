"""Microkernel (Plugin Architecture)"""
class PluginInterface:
    def execute(self):
        pass

class CoreSystem:
    def __init__(self):
        self.plugins = {}
    
    def register_plugin(self, name, plugin):
        self.plugins[name] = plugin
        print(f"Registered plugin: {name}")
    
    def execute_plugin(self, name):
        if name in self.plugins:
            self.plugins[name].execute()

class PluginA(PluginInterface):
    def execute(self):
        print("Executing Plugin A")

class PluginB(PluginInterface):
    def execute(self):
        print("Executing Plugin B")

if __name__ == "__main__":
    core = CoreSystem()
    core.register_plugin("A", PluginA())
    core.register_plugin("B", PluginB())
    core.execute_plugin("A")
    core.execute_plugin("B")
