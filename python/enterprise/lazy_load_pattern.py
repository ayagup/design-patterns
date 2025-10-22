"""Lazy Load Pattern - Defer loading until needed"""
class LazyProperty:
    def __init__(self, func):
        self.func = func
        self.name = func.__name__
    
    def __get__(self, obj, owner):
        if obj is None:
            return self
        value = self.func(obj)
        setattr(obj, self.name, value)
        return value

class Order:
    def __init__(self, id):
        self.id = id
        self._customer = None
    
    @LazyProperty
    def customer(self):
        print(f"Loading customer for order {self.id}")
        return Customer(1, "Alice")

class Customer:
    def __init__(self, id, name):
        self.id = id
        self.name = name

if __name__ == "__main__":
    order = Order(1)
    print("Order created")
    print(f"Customer: {order.customer.name}")  # Loads here
    print(f"Customer again: {order.customer.name}")  # Cached
