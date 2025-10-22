"""Money Pattern - Represents monetary values"""
from decimal import Decimal

class Money:
    def __init__(self, amount, currency="USD"):
        self.amount = Decimal(str(amount))
        self.currency = currency
    
    def add(self, other):
        if self.currency != other.currency:
            raise ValueError(f"Cannot add {self.currency} and {other.currency}")
        return Money(self.amount + other.amount, self.currency)
    
    def subtract(self, other):
        if self.currency != other.currency:
            raise ValueError(f"Cannot subtract {other.currency} from {self.currency}")
        return Money(self.amount - other.amount, self.currency)
    
    def multiply(self, factor):
        return Money(self.amount * Decimal(str(factor)), self.currency)
    
    def divide(self, divisor):
        return Money(self.amount / Decimal(str(divisor)), self.currency)
    
    def __eq__(self, other):
        return self.amount == other.amount and self.currency == other.currency
    
    def __str__(self):
        return f"{self.currency} {self.amount:.2f}"
    
    def __repr__(self):
        return f"Money({self.amount}, '{self.currency}')"

if __name__ == "__main__":
    price = Money(19.99, "USD")
    tax = Money(1.50, "USD")
    
    total = price.add(tax)
    print(f"Total: {total}")
    
    # Multiply for quantity
    quantity_price = price.multiply(3)
    print(f"3 items: {quantity_price}")
    
    # Division
    split = total.divide(2)
    print(f"Split between 2: {split}")
