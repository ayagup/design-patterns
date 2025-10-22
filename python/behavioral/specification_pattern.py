"""Specification Pattern"""
from abc import ABC, abstractmethod

class Specification(ABC):
    @abstractmethod
    def is_satisfied_by(self, item):
        pass
    
    def and_(self, other):
        return AndSpecification(self, other)

class AndSpecification(Specification):
    def __init__(self, left, right):
        self.left = left
        self.right = right
    
    def is_satisfied_by(self, item):
        return self.left.is_satisfied_by(item) and self.right.is_satisfied_by(item)

class PriceSpecification(Specification):
    def __init__(self, max_price):
        self.max_price = max_price
    
    def is_satisfied_by(self, item):
        return item['price'] <= self.max_price

if __name__ == "__main__":
    item = {'name': 'Book', 'price': 20}
    spec = PriceSpecification(25)
    print(f"Satisfied: {spec.is_satisfied_by(item)}")
