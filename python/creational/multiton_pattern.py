"""
Multiton Pattern
Purpose: Ensures only one instance per key exists
Use Case: Managing multiple named instances, registry pattern
"""

from typing import Dict, Optional
import threading


class Multiton:
    _instances: Dict[str, 'Multiton'] = {}
    _lock = threading.Lock()
    
    def __new__(cls, key: str):
        if key not in cls._instances:
            with cls._lock:
                if key not in cls._instances:
                    instance = super().__new__(cls)
                    cls._instances[key] = instance
        return cls._instances[key]
    
    def __init__(self, key: str):
        if not hasattr(self, '_initialized'):
            self.key = key
            self._initialized = True


class DatabaseConnection(Multiton):
    """Database connection pool with multiton"""
    def __init__(self, key: str):
        super().__init__(key)
        if not hasattr(self, 'connection_string'):
            self.connection_string = f"Connection to {key}"
    
    def query(self, sql: str) -> str:
        return f"[{self.key}] Executing: {sql}"


class CacheManager:
    """Cache manager with multiton pattern"""
    _instances: Dict[str, 'CacheManager'] = {}
    _lock = threading.Lock()
    
    @classmethod
    def get_instance(cls, region: str) -> 'CacheManager':
        if region not in cls._instances:
            with cls._lock:
                if region not in cls._instances:
                    cls._instances[region] = cls(region)
        return cls._instances[region]
    
    def __init__(self, region: str):
        self.region = region
        self.cache: Dict[str, any] = {}
    
    def set(self, key: str, value: any):
        self.cache[key] = value
    
    def get(self, key: str) -> Optional[any]:
        return self.cache.get(key)


if __name__ == "__main__":
    print("=== Multiton Pattern Demo ===\n")
    
    # Database connections
    print("--- Database Connections ---")
    db_primary = DatabaseConnection("primary")
    db_replica = DatabaseConnection("replica")
    db_primary2 = DatabaseConnection("primary")
    
    print(f"db_primary is db_primary2: {db_primary is db_primary2}")
    print(f"db_primary is db_replica: {db_primary is db_replica}")
    print(db_primary.query("SELECT * FROM users"))
    print(db_replica.query("SELECT * FROM users"))
    
    # Cache managers
    print("\n--- Cache Managers ---")
    cache_us = CacheManager.get_instance("us-east-1")
    cache_eu = CacheManager.get_instance("eu-west-1")
    cache_us2 = CacheManager.get_instance("us-east-1")
    
    print(f"cache_us is cache_us2: {cache_us is cache_us2}")
    print(f"cache_us is cache_eu: {cache_us is cache_eu}")
    
    cache_us.set("user:1", {"name": "Alice"})
    print(f"US Cache: {cache_us2.get('user:1')}")
    print(f"EU Cache: {cache_eu.get('user:1')}")
