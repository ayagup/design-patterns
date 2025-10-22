"""
Python Design Patterns Generator
This script generates comprehensive Python implementations for all 150 design patterns
"""

import os
from pathlib import Path

# Base directory
BASE_DIR = Path(__file__).parent

# Pattern templates with complete implementations
PATTERN_TEMPLATES = {
    # Remaining Creational Patterns (5-9)
    "prototype_pattern": '''"""
Prototype Pattern
Purpose: Creates new objects by cloning existing ones
Use Case: Object creation is expensive, need to avoid subclassing
"""

import copy
from abc import ABC, abstractmethod
from typing import Dict


class Prototype(ABC):
    @abstractmethod
    def clone(self):
        pass


class Document(Prototype):
    def __init__(self, title: str, content: str, metadata: dict):
        self.title = title
        self.content = content
        self.metadata = metadata
    
    def clone(self):
        # Deep copy for mutable attributes
        return copy.deepcopy(self)
    
    def __str__(self):
        return f"Document(title='{self.title}', metadata={self.metadata})"


class Shape(Prototype):
    def __init__(self, x: int, y: int, color: str):
        self.x = x
        self.y = y
        self.color = color
    
    def clone(self):
        return copy.copy(self)


class Circle(Shape):
    def __init__(self, x: int, y: int, color: str, radius: int):
        super().__init__(x, y, color)
        self.radius = radius
    
    def __str__(self):
        return f"Circle(x={self.x}, y={self.y}, color='{self.color}', radius={self.radius})"


class Rectangle(Shape):
    def __init__(self, x: int, y: int, color: str, width: int, height: int):
        super().__init__(x, y, color)
        self.width = width
        self.height = height
    
    def __str__(self):
        return f"Rectangle(x={self.x}, y={self.y}, color='{self.color}', width={self.width}, height={self.height})"


class PrototypeRegistry:
    """Registry of prototypes for easy cloning"""
    def __init__(self):
        self._prototypes: Dict[str, Prototype] = {}
    
    def register(self, key: str, prototype: Prototype):
        self._prototypes[key] = prototype
    
    def unregister(self, key: str):
        del self._prototypes[key]
    
    def clone(self, key: str) -> Prototype:
        return self._prototypes[key].clone()


if __name__ == "__main__":
    print("=== Prototype Pattern Demo ===\\n")
    
    # Document cloning
    doc1 = Document("Report", "Content here", {"author": "Alice", "version": 1})
    doc2 = doc1.clone()
    doc2.title = "Report Copy"
    doc2.metadata["version"] = 2
    
    print(f"Original: {doc1}")
    print(f"Clone: {doc2}")
    
    # Shape cloning
    circle1 = Circle(10, 20, "red", 5)
    circle2 = circle1.clone()
    circle2.color = "blue"
    
    print(f"\\nOriginal: {circle1}")
    print(f"Clone: {circle2}")
    
    # Registry usage
    registry = PrototypeRegistry()
    registry.register("basic_circle", Circle(0, 0, "black", 10))
    registry.register("basic_rect", Rectangle(0, 0, "white", 50, 30))
    
    new_circle = registry.clone("basic_circle")
    print(f"\\nCloned from registry: {new_circle}")
''',

    "object_pool_pattern": '''"""
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
    print("=== Object Pool Pattern Demo ===\\n")
    
    pool = ObjectPool(DatabaseConnection, max_size=3)
    
    print("Acquiring connections...")
    conn1 = pool.acquire()
    conn2 = pool.acquire()
    print(f"{pool}\\n")
    
    print(f"{conn1.query('SELECT * FROM users')}")
    
    print("\\nReleasing connection...")
    pool.release(conn1)
    print(f"{pool}\\n")
    
    print("Reusing released connection...")
    conn3 = pool.acquire()  # Reuses conn1
    print(f"{pool}")
''',

    "lazy_initialization_pattern": '''"""
Lazy Initialization Pattern
Purpose: Delays object creation until it's needed
Use Case: Resource-intensive objects, improving startup time
"""

from typing import Optional


class LazyLoader:
    """Lazy initialization with property"""
    def __init__(self):
        self._expensive_resource: Optional[str] = None
    
    @property
    def expensive_resource(self) -> str:
        if self._expensive_resource is None:
            print("  [LOADING] Initializing expensive resource...")
            self._expensive_resource = "Expensive Resource Data"
        return self._expensive_resource


class VirtualProxy:
    """Virtual proxy for lazy loading"""
    def __init__(self, filename: str):
        self.filename = filename
        self._real_object: Optional['RealImage'] = None
    
    def display(self):
        if self._real_object is None:
            self._real_object = RealImage(self.filename)
        self._real_object.display()


class RealImage:
    def __init__(self, filename: str):
        self.filename = filename
        self._load_from_disk()
    
    def _load_from_disk(self):
        print(f"  [LOADING] Loading image from {self.filename}")
    
    def display(self):
        print(f"  [DISPLAY] Showing {self.filename}")


class LazyProperty:
    """Descriptor for lazy properties"""
    def __init__(self, function):
        self.function = function
        self.name = function.__name__
    
    def __get__(self, obj, type=None):
        if obj is None:
            return self
        value = self.function(obj)
        setattr(obj, self.name, value)
        return value


class DataProcessor:
    @LazyProperty
    def cache(self):
        print("  [INIT] Creating cache...")
        return {}


if __name__ == "__main__":
    print("=== Lazy Initialization Pattern Demo ===\\n")
    
    # Example 1: Property-based
    print("--- Lazy Loader ---")
    loader = LazyLoader()
    print("Created loader (resource not loaded yet)")
    print(f"Accessing: {loader.expensive_resource}")
    print(f"Accessing again: {loader.expensive_resource}")
    
    # Example 2: Virtual Proxy
    print("\\n--- Virtual Proxy ---")
    image = VirtualProxy("photo.jpg")
    print("Created proxy (image not loaded yet)")
    image.display()
    image.display()
    
    # Example 3: Descriptor
    print("\\n--- Lazy Property Descriptor ---")
    processor = DataProcessor()
    print("Created processor")
    processor.cache["key"] = "value"
    print(f"Cache: {processor.cache}")
''',

    "dependency_injection_pattern": '''"""
Dependency Injection Pattern
Purpose: Provides objects with their dependencies rather than having them construct dependencies
Use Case: Inversion of Control (IoC), testability, loose coupling
"""

from abc import ABC, abstractmethod
from typing import Dict, Callable, Any


# Example 1: Constructor Injection
class Database(ABC):
    @abstractmethod
    def query(self, sql: str) -> str:
        pass


class MySQLDatabase(Database):
    def query(self, sql: str) -> str:
        return f"MySQL: {sql}"


class PostgreSQLDatabase(Database):
    def query(self, sql: str) -> str:
        return f"PostgreSQL: {sql}"


class UserRepository:
    """Constructor injection"""
    def __init__(self, database: Database):
        self.database = database
    
    def find_user(self, user_id: int) -> str:
        return self.database.query(f"SELECT * FROM users WHERE id = {user_id}")


# Example 2: Setter Injection
class Logger(ABC):
    @abstractmethod
    def log(self, message: str):
        pass


class FileLogger(Logger):
    def log(self, message: str):
        print(f"[FILE] {message}")


class ConsoleLogger(Logger):
    def log(self, message: str):
        print(f"[CONSOLE] {message}")


class UserService:
    """Setter injection"""
    def __init__(self):
        self._logger: Logger = ConsoleLogger()  # Default
    
    def set_logger(self, logger: Logger):
        self._logger = logger
    
    def create_user(self, name: str):
        self._logger.log(f"Creating user: {name}")
        return f"User {name} created"


# Example 3: DI Container
class DIContainer:
    def __init__(self):
        self._services: Dict[str, Callable] = {}
        self._singletons: Dict[str, Any] = {}
    
    def register(self, name: str, factory: Callable, singleton: bool = False):
        self._services[name] = (factory, singleton)
    
    def resolve(self, name: str) -> Any:
        if name not in self._services:
            raise ValueError(f"Service '{name}' not registered")
        
        factory, singleton = self._services[name]
        
        if singleton:
            if name not in self._singletons:
                self._singletons[name] = factory(self)
            return self._singletons[name]
        
        return factory(self)


if __name__ == "__main__":
    print("=== Dependency Injection Pattern Demo ===\\n")
    
    # Constructor Injection
    print("--- Constructor Injection ---")
    mysql_repo = UserRepository(MySQLDatabase())
    postgres_repo = UserRepository(PostgreSQLDatabase())
    print(mysql_repo.find_user(1))
    print(postgres_repo.find_user(1))
    
    # Setter Injection
    print("\\n--- Setter Injection ---")
    service = UserService()
    print(service.create_user("Alice"))
    service.set_logger(FileLogger())
    print(service.create_user("Bob"))
    
    # DI Container
    print("\\n--- DI Container ---")
    container = DIContainer()
    container.register("database", lambda c: MySQLDatabase(), singleton=True)
    container.register("logger", lambda c: ConsoleLogger(), singleton=True)
    container.register("user_repo", lambda c: UserRepository(c.resolve("database")))
    
    repo = container.resolve("user_repo")
    print(repo.find_user(42))
''',

    "multiton_pattern": '''"""
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
    print("=== Multiton Pattern Demo ===\\n")
    
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
    print("\\n--- Cache Managers ---")
    cache_us = CacheManager.get_instance("us-east-1")
    cache_eu = CacheManager.get_instance("eu-west-1")
    cache_us2 = CacheManager.get_instance("us-east-1")
    
    print(f"cache_us is cache_us2: {cache_us is cache_us2}")
    print(f"cache_us is cache_eu: {cache_us is cache_eu}")
    
    cache_us.set("user:1", {"name": "Alice"})
    print(f"US Cache: {cache_us2.get('user:1')}")
    print(f"EU Cache: {cache_eu.get('user:1')}")
''',
}


def generate_pattern_file(category: str, pattern_name: str, code: str):
    """Generate a single pattern file"""
    category_dir = BASE_DIR / category
    category_dir.mkdir(exist_ok=True)
    
    file_path = category_dir / f"{pattern_name}.py"
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(code)
    
    print(f"✓ Generated: {category}/{pattern_name}.py")


def main():
    """Generate all remaining creational patterns"""
    print("Generating remaining Creational Patterns...\n")
    
    for pattern_name, code in PATTERN_TEMPLATES.items():
        generate_pattern_file("creational", pattern_name, code)
    
    print(f"\n✓ Generated {len(PATTERN_TEMPLATES)} patterns successfully!")
    print("\nNote: This is a starter generator. Due to the large scope (150 patterns),")
    print("      please run additional generation scripts for other categories.")


if __name__ == "__main__":
    main()
