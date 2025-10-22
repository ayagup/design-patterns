"""
Bridge Pattern - Separates abstraction from implementation
"""

from abc import ABC, abstractmethod


class Renderer(ABC):
    @abstractmethod
    def render_circle(self, radius: float):
        pass


class VectorRenderer(Renderer):
    def render_circle(self, radius: float):
        print(f"Drawing a circle of radius {radius} as vectors")


class RasterRenderer(Renderer):
    def render_circle(self, radius: float):
        print(f"Drawing a circle of radius {radius} as pixels")


class Shape:
    def __init__(self, renderer: Renderer):
        self.renderer = renderer


class Circle(Shape):
    def __init__(self, renderer: Renderer, radius: float):
        super().__init__(renderer)
        self.radius = radius
    
    def draw(self):
        self.renderer.render_circle(self.radius)
    
    def resize(self, factor: float):
        self.radius *= factor


if __name__ == "__main__":
    print("=== Bridge Pattern Demo ===\n")
    
    vector = VectorRenderer()
    raster = RasterRenderer()
    
    circle1 = Circle(vector, 5)
    circle2 = Circle(raster, 5)
    
    circle1.draw()
    circle2.draw()
