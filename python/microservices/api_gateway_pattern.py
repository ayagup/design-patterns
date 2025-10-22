"""API Gateway Pattern"""
class UserService:
    def get_user(self, id):
        return {"id": id, "name": "Alice"}

class OrderService:
    def get_orders(self, user_id):
        return [{"id": 1, "total": 100}]

class APIGateway:
    def __init__(self):
        self.user_service = UserService()
        self.order_service = OrderService()
    
    def get_user_details(self, user_id):
        user = self.user_service.get_user(user_id)
        orders = self.order_service.get_orders(user_id)
        return {**user, "orders": orders}

if __name__ == "__main__":
    gateway = APIGateway()
    details = gateway.get_user_details(1)
    print(f"User details: {details}")
