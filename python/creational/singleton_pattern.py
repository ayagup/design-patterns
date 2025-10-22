"""
Singleton Pattern
Purpose: Ensures a class has only one instance and provides a global point of access to it
Use Case: Database connections, logging, configuration management
"""

import threading
from typing import Optional


# Example 1: Basic Singleton with __new__
class DatabaseConnection:
    """Thread-safe singleton using __new__"""
    _instance: Optional['DatabaseConnection'] = None
    _lock = threading.Lock()
    
    def __new__(cls):
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super().__new__(cls)
                    cls._instance._initialized = False
        return cls._instance
    
    def __init__(self):
        if not self._initialized:
            self.connection = "Database Connection Established"
            self._initialized = True
    
    def query(self, sql: str) -> str:
        return f"Executing: {sql}"


# Example 2: Singleton with Metaclass
class SingletonMeta(type):
    """Thread-safe singleton metaclass"""
    _instances = {}
    _lock = threading.Lock()
    
    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            with cls._lock:
                if cls not in cls._instances:
                    instance = super().__call__(*args, **kwargs)
                    cls._instances[cls] = instance
        return cls._instances[cls]


class Logger(metaclass=SingletonMeta):
    """Logger as singleton"""
    def __init__(self):
        self.log_file = "app.log"
    
    def log(self, message: str):
        print(f"[LOG] {message}")


# Example 3: Singleton Decorator
def singleton(cls):
    """Singleton decorator"""
    instances = {}
    lock = threading.Lock()
    
    def get_instance(*args, **kwargs):
        if cls not in instances:
            with lock:
                if cls not in instances:
                    instances[cls] = cls(*args, **kwargs)
        return instances[cls]
    
    return get_instance


@singleton
class ConfigurationManager:
    """Configuration manager as singleton"""
    def __init__(self):
        self.settings = {
            'debug': True,
            'max_connections': 100,
            'timeout': 30
        }
    
    def get(self, key: str):
        return self.settings.get(key)
    
    def set(self, key: str, value):
        self.settings[key] = value


# Example 4: Module-level Singleton (Pythonic way)
class _ApplicationState:
    """Private application state class"""
    def __init__(self):
        self.user = None
        self.is_authenticated = False
    
    def login(self, username: str):
        self.user = username
        self.is_authenticated = True
    
    def logout(self):
        self.user = None
        self.is_authenticated = False


# Module-level instance
application_state = _ApplicationState()


# Example 5: Lazy Singleton
class CacheManager:
    """Lazy initialized singleton"""
    _instance = None
    _lock = threading.Lock()
    
    def __new__(cls):
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super().__new__(cls)
        return cls._instance
    
    def __init__(self):
        if not hasattr(self, 'cache'):
            self.cache = {}
    
    def get(self, key: str):
        return self.cache.get(key)
    
    def set(self, key: str, value):
        self.cache[key] = value


def demonstrate_singleton():
    """Demonstrate singleton pattern"""
    print("=== Singleton Pattern Demo ===\n")
    
    # Example 1: Basic Singleton
    print("--- Example 1: Basic Singleton ---")
    db1 = DatabaseConnection()
    db2 = DatabaseConnection()
    print(f"db1 is db2: {db1 is db2}")  # True
    print(f"Query result: {db1.query('SELECT * FROM users')}")
    
    # Example 2: Metaclass Singleton
    print("\n--- Example 2: Metaclass Singleton ---")
    logger1 = Logger()
    logger2 = Logger()
    print(f"logger1 is logger2: {logger1 is logger2}")  # True
    logger1.log("Application started")
    
    # Example 3: Decorator Singleton
    print("\n--- Example 3: Decorator Singleton ---")
    config1 = ConfigurationManager()
    config2 = ConfigurationManager()
    print(f"config1 is config2: {config1 is config2}")  # True
    print(f"Debug mode: {config1.get('debug')}")
    config1.set('debug', False)
    print(f"Debug mode (from config2): {config2.get('debug')}")
    
    # Example 4: Module-level Singleton
    print("\n--- Example 4: Module-level Singleton ---")
    application_state.login("john_doe")
    print(f"User: {application_state.user}")
    print(f"Authenticated: {application_state.is_authenticated}")
    
    # Example 5: Lazy Singleton
    print("\n--- Example 5: Lazy Singleton ---")
    cache1 = CacheManager()
    cache2 = CacheManager()
    print(f"cache1 is cache2: {cache1 is cache2}")  # True
    cache1.set("user:1", {"name": "Alice", "age": 30})
    print(f"Cached data (from cache2): {cache2.get('user:1')}")
    
    print("\n=== Key Concepts ===")
    print("1. Single Instance - Only one instance exists")
    print("2. Global Access - Accessible from anywhere")
    print("3. Lazy Initialization - Created when first needed")
    print("4. Thread Safety - Safe in multi-threaded environment")
    
    print("\n=== Benefits ===")
    print("+ Controlled access to sole instance")
    print("+ Reduced namespace pollution")
    print("+ Permits refinement through subclassing")
    print("+ More flexible than class operations")
    
    print("\n=== Drawbacks ===")
    print("- Global state (can make testing harder)")
    print("- Violates Single Responsibility Principle")
    print("- Can mask poor design")
    print("- Requires special treatment in multi-threaded environment")


if __name__ == "__main__":
    demonstrate_singleton()
