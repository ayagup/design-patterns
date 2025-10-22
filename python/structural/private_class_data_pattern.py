"""
Private Class Data Pattern - Restricts access to class data
"""

class Circle:
    """Immutable circle after construction"""
    def __init__(self, radius: float, color: str, origin: tuple):
        self.__radius = radius
        self.__color = color
        self.__origin = origin
    
    @property
    def radius(self) -> float:
        return self.__radius
    
    @property
    def color(self) -> str:
        return self.__color
    
    @property
    def origin(self) -> tuple:
        return self.__origin
    
    def area(self) -> float:
        return 3.14159 * self.__radius ** 2


if __name__ == "__main__":
    print("=== Private Class Data Pattern Demo ===\n")
    
    circle = Circle(5.0, "red", (0, 0))
    print(f"Radius: {circle.radius}")
    print(f"Color: {circle.color}")
    print(f"Area: {circle.area()}")
    
    # This would fail: circle.radius = 10
    print("\nData is protected from modification")
