"""Gateway Aggregation - Aggregates multiple requests"""
class UserService:
    def get_user(self, id):
        return {"id": id, "name": "Alice"}

class OrderService:
    def get_orders(self, user_id):
        return [{"id": 1, "total": 100}]

class RecommendationService:
    def get_recommendations(self, user_id):
        return [{"product": "Book"}]

class AggregationGateway:
    def __init__(self):
        self.user_service = UserService()
        self.order_service = OrderService()
        self.rec_service = RecommendationService()
    
    def get_user_dashboard(self, user_id):
        user = self.user_service.get_user(user_id)
        orders = self.order_service.get_orders(user_id)
        recommendations = self.rec_service.get_recommendations(user_id)
        
        return {
            "user": user,
            "orders": orders,
            "recommendations": recommendations
        }

if __name__ == "__main__":
    gateway = AggregationGateway()
    dashboard = gateway.get_user_dashboard(1)
    print(f"Dashboard: {dashboard}")
