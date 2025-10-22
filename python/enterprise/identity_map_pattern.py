"""Identity Map Pattern - Ensures one object per record"""
class IdentityMap:
    def __init__(self):
        self._map = {}
    
    def get(self, key):
        return self._map.get(key)
    
    def put(self, key, obj):
        self._map[key] = obj
    
    def has(self, key):
        return key in self._map

class Session:
    def __init__(self):
        self.identity_map = IdentityMap()
        self.db = MockDB()
    
    def get(self, cls, id):
        key = f"{cls.__name__}:{id}"
        if self.identity_map.has(key):
            print(f"Retrieved from identity map: {key}")
            return self.identity_map.get(key)
        
        # Load from database
        data = self.db.load(cls, id)
        obj = cls(**data)
        self.identity_map.put(key, obj)
        return obj

class User:
    def __init__(self, id, name):
        self.id = id
        self.name = name

class MockDB:
    def load(self, cls, id):
        print(f"Loading from database: {cls.__name__} #{id}")
        return {"id": id, "name": "Alice"}

if __name__ == "__main__":
    session = Session()
    user1 = session.get(User, 1)
    user2 = session.get(User, 1)  # From identity map
    print(f"Same object: {user1 is user2}")
