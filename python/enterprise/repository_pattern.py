"""Repository Pattern"""
class User:
    def __init__(self, id, name):
        self.id = id
        self.name = name

class UserRepository:
    def __init__(self):
        self._users = {}
    
    def add(self, user):
        self._users[user.id] = user
    
    def get(self, id):
        return self._users.get(id)
    
    def get_all(self):
        return list(self._users.values())

if __name__ == "__main__":
    repo = UserRepository()
    repo.add(User(1, "Alice"))
    repo.add(User(2, "Bob"))
    print(f"User 1: {repo.get(1).name}")
    print(f"All users: {[u.name for u in repo.get_all()]}")
