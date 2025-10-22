"""
Composite Pattern - Composes objects into tree structures
"""

from abc import ABC, abstractmethod
from typing import List


class Graphic(ABC):
    @abstractmethod
    def draw(self):
        pass


class Circle(Graphic):
    def draw(self):
        print("Drawing a Circle")


class Square(Graphic):
    def draw(self):
        print("Drawing a Square")


class CompositeGraphic(Graphic):
    def __init__(self):
        self.graphics: List[Graphic] = []
    
    def add(self, graphic: Graphic):
        self.graphics.append(graphic)
    
    def remove(self, graphic: Graphic):
        self.graphics.remove(graphic)
    
    def draw(self):
        print("Drawing Composite:")
        for graphic in self.graphics:
            graphic.draw()


if __name__ == "__main__":
    print("=== Composite Pattern Demo ===\n")
    
    # Create primitives
    circle1 = Circle()
    circle2 = Circle()
    square = Square()
    
    # Create composite
    composite = CompositeGraphic()
    composite.add(circle1)
    composite.add(square)
    
    # Create another composite
    composite2 = CompositeGraphic()
    composite2.add(circle2)
    composite2.add(composite)
    
    composite2.draw()
