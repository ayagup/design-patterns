"""
State Pattern - Allows object to alter behavior when state changes
"""

from abc import ABC, abstractmethod


class State(ABC):
    @abstractmethod
    def handle(self, context: 'Context'):
        pass


class ConcreteStateA(State):
    def handle(self, context: 'Context'):
        print("State A: Transitioning to State B")
        context.state = ConcreteStateB()


class ConcreteStateB(State):
    def handle(self, context: 'Context'):
        print("State B: Transitioning to State A")
        context.state = ConcreteStateA()


class Context:
    def __init__(self, state: State):
        self.state = state
    
    def request(self):
        self.state.handle(self)


if __name__ == "__main__":
    print("=== State Pattern Demo ===\n")
    
    context = Context(ConcreteStateA())
    context.request()
    context.request()
    context.request()
