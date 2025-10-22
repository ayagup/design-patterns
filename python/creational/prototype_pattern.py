"""
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
    print("=== Prototype Pattern Demo ===\n")
    
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
    
    print(f"\nOriginal: {circle1}")
    print(f"Clone: {circle2}")
    
    # Registry usage
    registry = PrototypeRegistry()
    registry.register("basic_circle", Circle(0, 0, "black", 10))
    registry.register("basic_rect", Rectangle(0, 0, "white", 50, 30))
    
    new_circle = registry.clone("basic_circle")
    print(f"\nCloned from registry: {new_circle}")
