"""
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
    print("=== Lazy Initialization Pattern Demo ===\n")
    
    # Example 1: Property-based
    print("--- Lazy Loader ---")
    loader = LazyLoader()
    print("Created loader (resource not loaded yet)")
    print(f"Accessing: {loader.expensive_resource}")
    print(f"Accessing again: {loader.expensive_resource}")
    
    # Example 2: Virtual Proxy
    print("\n--- Virtual Proxy ---")
    image = VirtualProxy("photo.jpg")
    print("Created proxy (image not loaded yet)")
    image.display()
    image.display()
    
    # Example 3: Descriptor
    print("\n--- Lazy Property Descriptor ---")
    processor = DataProcessor()
    print("Created processor")
    processor.cache["key"] = "value"
    print(f"Cache: {processor.cache}")
