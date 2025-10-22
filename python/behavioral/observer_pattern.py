"""
Observer Pattern - Defines one-to-many dependency
"""

from abc import ABC, abstractmethod
from typing import List


class Observer(ABC):
    @abstractmethod
    def update(self, message: str):
        pass


class Subject:
    def __init__(self):
        self._observers: List[Observer] = []
        self._state = None
    
    def attach(self, observer: Observer):
        self._observers.append(observer)
    
    def detach(self, observer: Observer):
        self._observers.remove(observer)
    
    def notify(self, message: str):
        for observer in self._observers:
            observer.update(message)
    
    def set_state(self, state):
        self._state = state
        self.notify(f"State changed to: {state}")


class ConcreteObserver(Observer):
    def __init__(self, name: str):
        self.name = name
    
    def update(self, message: str):
        print(f"{self.name} received: {message}")


if __name__ == "__main__":
    print("=== Observer Pattern Demo ===\n")
    
    subject = Subject()
    
    observer1 = ConcreteObserver("Observer1")
    observer2 = ConcreteObserver("Observer2")
    
    subject.attach(observer1)
    subject.attach(observer2)
    
    subject.set_state("State A")
    subject.set_state("State B")
