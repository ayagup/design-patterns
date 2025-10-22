"""CQRS - Command Query Responsibility Segregation"""
# Command side (writes)
class CreateUserCommand:
    def __init__(self, name, email):
        self.name = name
        self.email = email

class CommandHandler:
    def __init__(self):
        self.write_db = []
    
    def handle(self, command):
        user = {"name": command.name, "email": command.email}
        self.write_db.append(user)
        print(f"User created: {user}")

# Query side (reads)
class GetUserQuery:
    def __init__(self, name):
        self.name = name

class QueryHandler:
    def __init__(self):
        self.read_db = []
    
    def handle(self, query):
        for user in self.read_db:
            if user["name"] == query.name:
                return user
        return None

if __name__ == "__main__":
    cmd_handler = CommandHandler()
    cmd_handler.handle(CreateUserCommand("Alice", "alice@example.com"))
