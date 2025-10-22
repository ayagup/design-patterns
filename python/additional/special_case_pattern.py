"""Special Case Pattern - Subclass for particular cases"""
from abc import ABC, abstractmethod

class Customer(ABC):
    @abstractmethod
    def is_null(self):
        pass
    
    @abstractmethod
    def get_name(self):
        pass
    
    @abstractmethod
    def get_discount(self):
        pass

class RealCustomer(Customer):
    def __init__(self, name, discount=0):
        self.name = name
        self.discount = discount
    
    def is_null(self):
        return False
    
    def get_name(self):
        return self.name
    
    def get_discount(self):
        return self.discount

class NullCustomer(Customer):
    def is_null(self):
        return True
    
    def get_name(self):
        return "Guest"
    
    def get_discount(self):
        return 0

class UnknownCustomer(Customer):
    """Special case for unknown customers"""
    def is_null(self):
        return False
    
    def get_name(self):
        return "Unknown Customer"
    
    def get_discount(self):
        return 0

def calculate_price(customer, base_price):
    discount = customer.get_discount()
    final_price = base_price * (1 - discount)
    print(f"Customer: {customer.get_name()}, Price: ${final_price:.2f}")
    return final_price

if __name__ == "__main__":
    customers = [
        RealCustomer("Alice", 0.1),
        NullCustomer(),
        UnknownCustomer(),
        RealCustomer("Bob", 0.15)
    ]
    
    for customer in customers:
        calculate_price(customer, 100)
