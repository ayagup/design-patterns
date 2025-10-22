"""Service Discovery Pattern"""
class ServiceRegistry:
    def __init__(self):
        self._services = {}
    
    def register(self, name, address):
        self._services[name] = address
        print(f"Registered {name} at {address}")
    
    def discover(self, name):
        return self._services.get(name)

if __name__ == "__main__":
    registry = ServiceRegistry()
    registry.register("user-service", "http://localhost:8001")
    registry.register("order-service", "http://localhost:8002")
    
    address = registry.discover("user-service")
    print(f"Found user-service at: {address}")
