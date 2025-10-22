"""Service Layer Pattern - Application's boundary"""
class UserService:
    def __init__(self, repository):
        self.repository = repository
    
    def register_user(self, name, email):
        user = {"name": name, "email": email}
        self.repository.save(user)
        return user
    
    def get_user(self, id):
        return self.repository.find(id)

class UserRepository:
    def __init__(self):
        self.users = {}
        self.next_id = 1
    
    def save(self, user):
        user['id'] = self.next_id
        self.users[self.next_id] = user
        self.next_id += 1
    
    def find(self, id):
        return self.users.get(id)

if __name__ == "__main__":
    repo = UserRepository()
    service = UserService(repo)
    user = service.register_user("Alice", "alice@example.com")
    print(f"Registered: {user}")
