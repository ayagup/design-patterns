"""Domain Model Pattern - Object model of domain"""
class Money:
    def __init__(self, amount, currency="USD"):
        self.amount = amount
        self.currency = currency
    
    def add(self, other):
        if self.currency != other.currency:
            raise ValueError("Currency mismatch")
        return Money(self.amount + other.amount, self.currency)

class Product:
    def __init__(self, name, price):
        self.name = name
        self.price = price

class OrderLine:
    def __init__(self, product, quantity):
        self.product = product
        self.quantity = quantity
    
    def total(self):
        return Money(self.product.price.amount * self.quantity, self.product.price.currency)

class Order:
    def __init__(self):
        self.lines = []
    
    def add_line(self, product, quantity):
        self.lines.append(OrderLine(product, quantity))
    
    def total(self):
        result = Money(0)
        for line in self.lines:
            result = result.add(line.total())
        return result

if __name__ == "__main__":
    product = Product("Book", Money(20))
    order = Order()
    order.add_line(product, 2)
    print(f"Order total: ${order.total().amount}")
