"""Visitor Pattern"""
from abc import ABC, abstractmethod

class Visitor(ABC):
    @abstractmethod
    def visit_circle(self, circle):
        pass
    
    @abstractmethod
    def visit_rectangle(self, rectangle):
        pass

class Shape(ABC):
    @abstractmethod
    def accept(self, visitor):
        pass

class Circle(Shape):
    def __init__(self, radius):
        self.radius = radius
    
    def accept(self, visitor):
        visitor.visit_circle(self)

class Rectangle(Shape):
    def __init__(self, width, height):
        self.width = width
        self.height = height
    
    def accept(self, visitor):
        visitor.visit_rectangle(self)

class AreaVisitor(Visitor):
    def visit_circle(self, circle):
        print(f"Circle area: {3.14 * circle.radius ** 2}")
    
    def visit_rectangle(self, rectangle):
        print(f"Rectangle area: {rectangle.width * rectangle.height}")

if __name__ == "__main__":
    shapes = [Circle(5), Rectangle(3, 4)]
    visitor = AreaVisitor()
    for shape in shapes:
        shape.accept(visitor)
