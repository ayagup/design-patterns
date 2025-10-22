"""Data Transfer Object Pattern"""
from dataclasses import dataclass

@dataclass
class UserDTO:
    id: int
    name: str
    email: str

class UserService:
    def get_user(self, id):
        # Simulate getting from database
        return UserDTO(id=id, name="Alice", email="alice@example.com")

if __name__ == "__main__":
    service = UserService()
    dto = service.get_user(1)
    print(f"User: {dto.name}, {dto.email}")
