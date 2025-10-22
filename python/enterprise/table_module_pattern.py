"""Table Module Pattern - Single instance per table"""
class ProductModule:
    def __init__(self, db):
        self.db = db
    
    def find(self, id):
        return self.db.query(f"SELECT * FROM products WHERE id = {id}")
    
    def find_by_category(self, category):
        return self.db.query(f"SELECT * FROM products WHERE category = '{category}'")
    
    def update_price(self, id, new_price):
        self.db.execute(f"UPDATE products SET price = {new_price} WHERE id = {id}")

class MockDB:
    def query(self, sql):
        print(f"Executing: {sql}")
        return [{"id": 1, "name": "Product", "price": 100}]
    
    def execute(self, sql):
        print(f"Executing: {sql}")

if __name__ == "__main__":
    db = MockDB()
    products = ProductModule(db)
    products.find(1)
    products.update_price(1, 120)
