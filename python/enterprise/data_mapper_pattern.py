"""Data Mapper Pattern"""
class User:
    def __init__(self, id, name, email):
        self.id = id
        self.name = name
        self.email = email

class UserMapper:
    def __init__(self):
        self._db = {}
    
    def insert(self, user):
        self._db[user.id] = {'name': user.name, 'email': user.email}
    
    def find(self, id):
        if id in self._db:
            data = self._db[id]
            return User(id, data['name'], data['email'])
        return None

if __name__ == "__main__":
    mapper = UserMapper()
    user = User(1, "Alice", "alice@example.com")
    mapper.insert(user)
    found = mapper.find(1)
    print(f"Found: {found.name}, {found.email}")
