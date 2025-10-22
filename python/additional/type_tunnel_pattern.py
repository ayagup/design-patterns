"""
Type Tunnel Pattern

Intent: Passes generic type information through non-generic code.
Used in generic programming, type preservation, casting.
"""

from typing import TypeVar, Generic, Type, Any, cast
from abc import ABC


T = TypeVar('T')
U = TypeVar('U')


class TypeTunnel(Generic[T]):
    """Tunnels type information through the system"""
    
    def __init__(self, type_class: Type[T]):
        self.type_class = type_class
    
    def create(self, *args, **kwargs) -> T:
        """Create instance of the tunneled type"""
        return self.type_class(*args, **kwargs)
    
    def cast(self, obj: Any) -> T:
        """Cast object to tunneled type"""
        if isinstance(obj, self.type_class):
            return obj
        raise TypeError(f"Cannot cast {type(obj)} to {self.type_class}")
    
    def get_type(self) -> Type[T]:
        """Get the tunneled type"""
        return self.type_class


class Container(Generic[T]):
    """Generic container that tunnels type info"""
    
    def __init__(self, type_class: Type[T]):
        self.tunnel = TypeTunnel(type_class)
        self.items: list[T] = []
    
    def add(self, item: T):
        """Add item (with type checking)"""
        typed_item = self.tunnel.cast(item)
        self.items.append(typed_item)
    
    def get(self, index: int) -> T:
        """Get item with type preserved"""
        return self.items[index]
    
    def create_and_add(self, *args, **kwargs):
        """Create instance and add to container"""
        item = self.tunnel.create(*args, **kwargs)
        self.add(item)
        return item


class DataMapper(Generic[T]):
    """Maps data while preserving type information"""
    
    def __init__(self, source_type: Type[T]):
        self.source_type = source_type
        self.tunnel = TypeTunnel(source_type)
    
    def from_dict(self, data: dict) -> T:
        """Create typed object from dictionary"""
        return self.tunnel.create(**data)
    
    def to_dict(self, obj: T) -> dict:
        """Convert typed object to dictionary"""
        if hasattr(obj, '__dict__'):
            return obj.__dict__
        raise TypeError(f"Cannot convert {type(obj)} to dict")


class Factory(Generic[T]):
    """Factory that uses type tunnel for creation"""
    
    def __init__(self, product_type: Type[T]):
        self.tunnel = TypeTunnel(product_type)
        self._registry: dict[str, Type[T]] = {}
    
    def register(self, name: str, subtype: Type[T]):
        """Register a subtype"""
        self._registry[name] = subtype
    
    def create(self, name: str, *args, **kwargs) -> T:
        """Create instance by name"""
        if name not in self._registry:
            raise ValueError(f"Unknown type: {name}")
        
        type_class = self._registry[name]
        tunnel = TypeTunnel(type_class)
        return tunnel.create(*args, **kwargs)


# Example domain classes
class Product:
    def __init__(self, name: str, price: float):
        self.name = name
        self.price = price
    
    def __repr__(self):
        return f"Product({self.name}, ${self.price})"


class Book(Product):
    def __init__(self, name: str, price: float, author: str):
        super().__init__(name, price)
        self.author = author
    
    def __repr__(self):
        return f"Book({self.name} by {self.author}, ${self.price})"


class Electronics(Product):
    def __init__(self, name: str, price: float, warranty: int):
        super().__init__(name, price)
        self.warranty = warranty
    
    def __repr__(self):
        return f"Electronics({self.name}, ${self.price}, {self.warranty}y warranty)"


class Person:
    def __init__(self, name: str, age: int):
        self.name = name
        self.age = age
    
    def __repr__(self):
        return f"Person({self.name}, {self.age})"


class Repository(Generic[T]):
    """Generic repository with type tunnel"""
    
    def __init__(self, entity_type: Type[T]):
        self.tunnel = TypeTunnel(entity_type)
        self.storage: dict[int, T] = {}
        self.next_id = 1
    
    def save(self, entity: T) -> int:
        """Save entity and return ID"""
        typed_entity = self.tunnel.cast(entity)
        entity_id = self.next_id
        self.storage[entity_id] = typed_entity
        self.next_id += 1
        return entity_id
    
    def find(self, entity_id: int) -> T:
        """Find entity by ID"""
        return self.storage.get(entity_id)
    
    def get_type(self) -> Type[T]:
        """Get repository type"""
        return self.tunnel.get_type()


if __name__ == "__main__":
    print("=== Type Tunnel Pattern Demo ===\n")
    
    # 1. Type tunnel basics
    print("1. Basic Type Tunnel:")
    tunnel = TypeTunnel(Person)
    person = tunnel.create("Alice", 30)
    print(f"Created: {person}")
    print(f"Type: {tunnel.get_type()}\n")
    
    # 2. Generic container
    print("2. Generic Container:")
    product_container = Container(Product)
    product_container.add(Product("Widget", 9.99))
    product_container.create_and_add("Gadget", 19.99)
    
    for i in range(len(product_container.items)):
        print(f"  {product_container.get(i)}")
    print()
    
    # 3. Data mapper
    print("3. Data Mapper:")
    mapper = DataMapper(Person)
    person_data = {"name": "Bob", "age": 25}
    person = mapper.from_dict(person_data)
    print(f"Mapped: {person}")
    print(f"Back to dict: {mapper.to_dict(person)}\n")
    
    # 4. Factory with type tunnel
    print("4. Factory with Type Tunnel:")
    factory = Factory(Product)
    factory.register("book", Book)
    factory.register("electronics", Electronics)
    
    book = factory.create("book", "Python Guide", 29.99, "John Doe")
    electronics = factory.create("electronics", "Laptop", 999.99, 2)
    
    print(f"Created: {book}")
    print(f"Created: {electronics}\n")
    
    # 5. Generic repository
    print("5. Generic Repository:")
    person_repo = Repository(Person)
    product_repo = Repository(Product)
    
    person_id = person_repo.save(Person("Charlie", 35))
    product_id = product_repo.save(Product("Tool", 14.99))
    
    print(f"Person repo type: {person_repo.get_type()}")
    print(f"Product repo type: {product_repo.get_type()}")
    print(f"Loaded person: {person_repo.find(person_id)}")
    print(f"Loaded product: {product_repo.find(product_id)}")
    
    print("\n✓ Type Tunnel preserves type information through generic code!")
