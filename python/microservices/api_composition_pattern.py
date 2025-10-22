"""API Composition - Composes data from multiple services"""
class UserService:
    def get_user(self, id):
        return {"id": id, "name": "Alice"}

class OrderService:
    def get_orders(self, user_id):
        return [{"id": 1, "total": 100}]

class ProductService:
    def get_product(self, id):
        return {"id": id, "name": "Product"}

class APIComposer:
    def __init__(self):
        self.user_service = UserService()
        self.order_service = OrderService()
        self.product_service = ProductService()
    
    def get_user_with_orders(self, user_id):
        user = self.user_service.get_user(user_id)
        orders = self.order_service.get_orders(user_id)
        
        # Enrich orders with product details
        for order in orders:
            order['product'] = self.product_service.get_product(1)
        
        return {
            "user": user,
            "orders": orders
        }

if __name__ == "__main__":
    composer = APIComposer()
    result = composer.get_user_with_orders(1)
    print(f"Composed result: {result}")
