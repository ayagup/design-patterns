"""Row Data Gateway - Gateway to single record"""
class UserRowGateway:
    def __init__(self, id, name, email, db):
        self.id = id
        self.name = name
        self.email = email
        self.db = db
    
    @classmethod
    def find(cls, id, db):
        data = db.query(f"SELECT * FROM users WHERE id = {id}")
        return cls(data['id'], data['name'], data['email'], db)
    
    def update(self):
        self.db.execute(f"UPDATE users SET name='{self.name}', email='{self.email}' WHERE id={self.id}")
    
    def delete(self):
        self.db.execute(f"DELETE FROM users WHERE id={self.id}")

class MockDB:
    def query(self, sql):
        print(f"Query: {sql}")
        return {"id": 1, "name": "Alice", "email": "alice@example.com"}
    
    def execute(self, sql):
        print(f"Execute: {sql}")

if __name__ == "__main__":
    db = MockDB()
    user = UserRowGateway.find(1, db)
    user.name = "Alice Updated"
    user.update()
