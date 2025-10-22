"""
Decorator Pattern - Adds new functionality dynamically
"""

from abc import ABC, abstractmethod


class Coffee(ABC):
    @abstractmethod
    def cost(self) -> float:
        pass
    
    @abstractmethod
    def description(self) -> str:
        pass


class SimpleCoffee(Coffee):
    def cost(self) -> float:
        return 2.0
    
    def description(self) -> str:
        return "Simple coffee"


class CoffeeDecorator(Coffee):
    def __init__(self, coffee: Coffee):
        self._coffee = coffee
    
    def cost(self) -> float:
        return self._coffee.cost()
    
    def description(self) -> str:
        return self._coffee.description()


class Milk(CoffeeDecorator):
    def cost(self) -> float:
        return self._coffee.cost() + 0.5
    
    def description(self) -> str:
        return self._coffee.description() + ", milk"


class Sugar(CoffeeDecorator):
    def cost(self) -> float:
        return self._coffee.cost() + 0.2
    
    def description(self) -> str:
        return self._coffee.description() + ", sugar"


class Whip(CoffeeDecorator):
    def cost(self) -> float:
        return self._coffee.cost() + 0.7
    
    def description(self) -> str:
        return self._coffee.description() + ", whip"


if __name__ == "__main__":
    print("=== Decorator Pattern Demo ===\n")
    
    # Simple coffee
    coffee = SimpleCoffee()
    print(f"{coffee.description()}: ${coffee.cost()}")
    
    # Coffee with milk
    coffee_with_milk = Milk(SimpleCoffee())
    print(f"{coffee_with_milk.description()}: ${coffee_with_milk.cost()}")
    
    # Coffee with milk and sugar
    coffee_deluxe = Sugar(Milk(SimpleCoffee()))
    print(f"{coffee_deluxe.description()}: ${coffee_deluxe.cost()}")
    
    # Full loaded
    coffee_supreme = Whip(Sugar(Milk(SimpleCoffee())))
    print(f"{coffee_supreme.description()}: ${coffee_supreme.cost()}")
