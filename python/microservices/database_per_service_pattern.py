"""Database per Service - Each service owns database"""
class UserServiceDB:
    def __init__(self):
        self.users = {}
    
    def save_user(self, user):
        self.users[user['id']] = user
    
    def get_user(self, id):
        return self.users.get(id)

class OrderServiceDB:
    def __init__(self):
        self.orders = {}
    
    def save_order(self, order):
        self.orders[order['id']] = order
    
    def get_order(self, id):
        return self.orders.get(id)

class UserService:
    def __init__(self):
        self.db = UserServiceDB()
    
    def create_user(self, name, email):
        user = {"id": 1, "name": name, "email": email}
        self.db.save_user(user)
        return user

class OrderService:
    def __init__(self):
        self.db = OrderServiceDB()
    
    def create_order(self, user_id, items):
        order = {"id": 1, "user_id": user_id, "items": items}
        self.db.save_order(order)
        return order

if __name__ == "__main__":
    user_service = UserService()
    order_service = OrderService()
    
    user = user_service.create_user("Alice", "alice@example.com")
    order = order_service.create_order(user['id'], ["Item1"])
    
    print(f"User: {user}")
    print(f"Order: {order}")
