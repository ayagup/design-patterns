"""Registry Pattern - Well-known object for service lookup"""
class ServiceRegistry:
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance.services = {}
        return cls._instance
    
    def register(self, name, service):
        self.services[name] = service
        print(f"Registered service: {name}")
    
    def get(self, name):
        return self.services.get(name)
    
    def unregister(self, name):
        if name in self.services:
            del self.services[name]
            print(f"Unregistered service: {name}")

class DatabaseService:
    def query(self, sql):
        return f"Query result for: {sql}"

class LoggingService:
    def log(self, message):
        print(f"[LOG] {message}")

if __name__ == "__main__":
    registry = ServiceRegistry()
    
    # Register services
    registry.register("database", DatabaseService())
    registry.register("logger", LoggingService())
    
    # Use services from registry
    db = registry.get("database")
    logger = registry.get("logger")
    
    result = db.query("SELECT * FROM users")
    logger.log(f"Database query executed: {result}")
