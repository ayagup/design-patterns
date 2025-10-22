"""Event-Driven Architecture Pattern"""
class EventBus:
    def __init__(self):
        self._handlers = {}
    
    def subscribe(self, event_type, handler):
        if event_type not in self._handlers:
            self._handlers[event_type] = []
        self._handlers[event_type].append(handler)
    
    def publish(self, event_type, data):
        if event_type in self._handlers:
            for handler in self._handlers[event_type]:
                handler(data)

def user_created_handler(data):
    print(f"User created: {data}")

if __name__ == "__main__":
    bus = EventBus()
    bus.subscribe("user_created", user_created_handler)
    bus.publish("user_created", {"name": "Alice"})
