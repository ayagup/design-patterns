"""Clean Architecture - Dependency rule"""
from abc import ABC, abstractmethod

# Entities (innermost layer)
class User:
    def __init__(self, id, name):
        self.id = id
        self.name = name

# Use Cases (application business rules)
class CreateUserUseCase:
    def __init__(self, repository):
        self.repository = repository
    
    def execute(self, name):
        user = User(id=None, name=name)
        return self.repository.save(user)

# Interface Adapters
class UserRepository(ABC):
    @abstractmethod
    def save(self, user):
        pass

class InMemoryUserRepository(UserRepository):
    def __init__(self):
        self.users = []
    
    def save(self, user):
        user.id = len(self.users) + 1
        self.users.append(user)
        return user

if __name__ == "__main__":
    repo = InMemoryUserRepository()
    use_case = CreateUserUseCase(repo)
    user = use_case.execute("Alice")
    print(f"Created user: {user.name} with ID {user.id}")
