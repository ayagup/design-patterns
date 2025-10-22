"""Table Data Gateway - Gateway to database table"""
class UserGateway:
    def __init__(self, db):
        self.db = db
    
    def find(self, id):
        return self.db.query(f"SELECT * FROM users WHERE id = {id}")
    
    def find_all(self):
        return self.db.query("SELECT * FROM users")
    
    def insert(self, name, email):
        return self.db.execute(f"INSERT INTO users (name, email) VALUES ('{name}', '{email}')")
    
    def update(self, id, name, email):
        return self.db.execute(f"UPDATE users SET name='{name}', email='{email}' WHERE id={id}")
    
    def delete(self, id):
        return self.db.execute(f"DELETE FROM users WHERE id={id}")

class MockDB:
    def query(self, sql):
        print(f"Query: {sql}")
        return [{"id": 1, "name": "Alice", "email": "alice@example.com"}]
    
    def execute(self, sql):
        print(f"Execute: {sql}")
        return True

if __name__ == "__main__":
    db = MockDB()
    gateway = UserGateway(db)
    gateway.find(1)
    gateway.insert("Bob", "bob@example.com")
