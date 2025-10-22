"""
Chain of Responsibility Pattern - Passes requests along a chain
"""

from abc import ABC, abstractmethod
from typing import Optional


class Handler(ABC):
    def __init__(self):
        self._next: Optional[Handler] = None
    
    def set_next(self, handler: 'Handler') -> 'Handler':
        self._next = handler
        return handler
    
    @abstractmethod
    def handle(self, request: str) -> Optional[str]:
        pass


class ConcreteHandler1(Handler):
    def handle(self, request: str) -> Optional[str]:
        if request == "one":
            return f"Handler1: Processing '{request}'"
        elif self._next:
            return self._next.handle(request)
        return None


class ConcreteHandler2(Handler):
    def handle(self, request: str) -> Optional[str]:
        if request == "two":
            return f"Handler2: Processing '{request}'"
        elif self._next:
            return self._next.handle(request)
        return None


class ConcreteHandler3(Handler):
    def handle(self, request: str) -> Optional[str]:
        if request == "three":
            return f"Handler3: Processing '{request}'"
        elif self._next:
            return self._next.handle(request)
        return None


if __name__ == "__main__":
    print("=== Chain of Responsibility Pattern Demo ===\n")
    
    handler1 = ConcreteHandler1()
    handler2 = ConcreteHandler2()
    handler3 = ConcreteHandler3()
    
    handler1.set_next(handler2).set_next(handler3)
    
    for request in ["one", "two", "three", "four"]:
        result = handler1.handle(request)
        if result:
            print(result)
        else:
            print(f"No handler for '{request}'")
