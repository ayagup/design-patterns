"""
Strategy Pattern - Defines family of algorithms
"""

from abc import ABC, abstractmethod


class Strategy(ABC):
    @abstractmethod
    def execute(self, a: int, b: int) -> int:
        pass


class AddStrategy(Strategy):
    def execute(self, a: int, b: int) -> int:
        return a + b


class SubtractStrategy(Strategy):
    def execute(self, a: int, b: int) -> int:
        return a - b


class MultiplyStrategy(Strategy):
    def execute(self, a: int, b: int) -> int:
        return a * b


class Context:
    def __init__(self, strategy: Strategy):
        self._strategy = strategy
    
    def set_strategy(self, strategy: Strategy):
        self._strategy = strategy
    
    def execute_strategy(self, a: int, b: int) -> int:
        return self._strategy.execute(a, b)


if __name__ == "__main__":
    print("=== Strategy Pattern Demo ===\n")
    
    context = Context(AddStrategy())
    print(f"10 + 5 = {context.execute_strategy(10, 5)}")
    
    context.set_strategy(SubtractStrategy())
    print(f"10 - 5 = {context.execute_strategy(10, 5)}")
    
    context.set_strategy(MultiplyStrategy())
    print(f"10 * 5 = {context.execute_strategy(10, 5)}")
