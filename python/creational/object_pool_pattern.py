"""
Object Pool Pattern
Purpose: Reuses objects that are expensive to create
Use Case: Database connections, thread pools, network connections
"""

from typing import Generic, TypeVar, List, Optional
import threading
import time


T = TypeVar('T')


class ObjectPool(Generic[T]):
    def __init__(self, factory, max_size: int = 10):
        self._factory = factory
        self._max_size = max_size
        self._available: List[T] = []
        self._in_use: List[T] = []
        self._lock = threading.Lock()
    
    def acquire(self) -> Optional[T]:
        with self._lock:
            if self._available:
                obj = self._available.pop()
                self._in_use.append(obj)
                return obj
            elif len(self._in_use) < self._max_size:
                obj = self._factory()
                self._in_use.append(obj)
                return obj
            return None
    
    def release(self, obj: T):
        with self._lock:
            if obj in self._in_use:
                self._in_use.remove(obj)
                self._available.append(obj)
    
    def __str__(self):
        return f"Pool(available={len(self._available)}, in_use={len(self._in_use)})"


class DatabaseConnection:
    _counter = 0
    
    def __init__(self):
        DatabaseConnection._counter += 1
        self.id = DatabaseConnection._counter
        print(f"  [CREATED] Connection #{self.id}")
    
    def query(self, sql: str):
        return f"Connection #{self.id} executed: {sql}"
    
    def __str__(self):
        return f"Connection #{self.id}"


if __name__ == "__main__":
    print("=== Object Pool Pattern Demo ===\n")
    
    pool = ObjectPool(DatabaseConnection, max_size=3)
    
    print("Acquiring connections...")
    conn1 = pool.acquire()
    conn2 = pool.acquire()
    print(f"{pool}\n")
    
    print(f"{conn1.query('SELECT * FROM users')}")
    
    print("\nReleasing connection...")
    pool.release(conn1)
    print(f"{pool}\n")
    
    print("Reusing released connection...")
    conn3 = pool.acquire()  # Reuses conn1
    print(f"{pool}")
