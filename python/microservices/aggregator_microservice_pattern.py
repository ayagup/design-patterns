"""Aggregator Microservice - Aggregates multiple services"""
class ProductService:
    def get_product(self, id):
        return {"id": id, "name": "Product", "price": 100}

class ReviewService:
    def get_reviews(self, product_id):
        return [{"rating": 5, "comment": "Great!"}]

class InventoryService:
    def get_inventory(self, product_id):
        return {"stock": 50}

class ProductAggregator:
    def __init__(self):
        self.product_service = ProductService()
        self.review_service = ReviewService()
        self.inventory_service = InventoryService()
    
    def get_product_details(self, product_id):
        product = self.product_service.get_product(product_id)
        reviews = self.review_service.get_reviews(product_id)
        inventory = self.inventory_service.get_inventory(product_id)
        
        return {
            **product,
            "reviews": reviews,
            "inventory": inventory
        }

if __name__ == "__main__":
    aggregator = ProductAggregator()
    details = aggregator.get_product_details(1)
    print(f"Product details: {details}")
