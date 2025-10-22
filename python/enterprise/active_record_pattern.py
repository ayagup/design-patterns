"""Active Record Pattern"""
class User:
    _db = {}
    
    def __init__(self, id, name):
        self.id = id
        self.name = name
    
    def save(self):
        User._db[self.id] = {'name': self.name}
        print(f"Saved user {self.id}")
    
    @classmethod
    def find(cls, id):
        if id in cls._db:
            data = cls._db[id]
            return cls(id, data['name'])
        return None

if __name__ == "__main__":
    user = User(1, "Alice")
    user.save()
    found = User.find(1)
    print(f"Found: {found.name}")
