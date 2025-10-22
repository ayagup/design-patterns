"""Event-Driven Pattern - Alternative implementation for completeness"""
class Event:
    def __init__(self, type, data):
        self.type = type
        self.data = data
    
    def __repr__(self):
        return f"Event(type={self.type}, data={self.data})"

class EventEmitter:
    def __init__(self):
        self._listeners = {}
    
    def on(self, event_type, handler):
        if event_type not in self._listeners:
            self._listeners[event_type] = []
        self._listeners[event_type].append(handler)
    
    def off(self, event_type, handler):
        if event_type in self._listeners:
            self._listeners[event_type].remove(handler)
    
    def emit(self, event):
        if event.type in self._listeners:
            for handler in self._listeners[event.type]:
                handler(event)

class UserService(EventEmitter):
    def __init__(self):
        super().__init__()
        self.users = {}
    
    def create_user(self, user_id, name):
        self.users[user_id] = {"id": user_id, "name": name}
        self.emit(Event("user.created", {"user_id": user_id, "name": name}))
    
    def delete_user(self, user_id):
        if user_id in self.users:
            user = self.users[user_id]
            del self.users[user_id]
            self.emit(Event("user.deleted", {"user_id": user_id}))

# Event handlers
def send_welcome_email(event):
    print(f"Sending welcome email to user {event.data['user_id']}")

def log_user_activity(event):
    print(f"Logging: {event}")

def cleanup_user_data(event):
    print(f"Cleaning up data for user {event.data['user_id']}")

if __name__ == "__main__":
    user_service = UserService()
    
    # Register event handlers
    user_service.on("user.created", send_welcome_email)
    user_service.on("user.created", log_user_activity)
    user_service.on("user.deleted", cleanup_user_data)
    user_service.on("user.deleted", log_user_activity)
    
    # Trigger events
    print("Creating user:")
    user_service.create_user(1, "Alice")
    
    print("\nDeleting user:")
    user_service.delete_user(1)
