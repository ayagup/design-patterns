"""Marker Interface - Empty interface to mark classes"""
from abc import ABC

class Serializable(ABC):
    """Marker interface - indicates class can be serialized"""
    pass

class Cloneable(ABC):
    """Marker interface - indicates class can be cloned"""
    pass

class User(Serializable, Cloneable):
    def __init__(self, name, email):
        self.name = name
        self.email = email

class Product(Serializable):
    def __init__(self, name, price):
        self.name = name
        self.price = price

def serialize(obj):
    """Only serialize objects marked as Serializable"""
    if not isinstance(obj, Serializable):
        raise TypeError(f"{type(obj).__name__} is not serializable")
    
    return {
        key: value for key, value in obj.__dict__.items()
        if not key.startswith('_')
    }

def clone(obj):
    """Only clone objects marked as Cloneable"""
    if not isinstance(obj, Cloneable):
        raise TypeError(f"{type(obj).__name__} is not cloneable")
    
    import copy
    return copy.deepcopy(obj)

if __name__ == "__main__":
    user = User("Alice", "alice@example.com")
    product = Product("Laptop", 999)
    
    # Serialize both
    print(f"User serialized: {serialize(user)}")
    print(f"Product serialized: {serialize(product)}")
    
    # Clone user (works)
    user_clone = clone(user)
    print(f"User cloned: {user_clone.name}")
    
    # Clone product (fails - not Cloneable)
    try:
        product_clone = clone(product)
    except TypeError as e:
        print(f"Error: {e}")
