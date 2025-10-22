"""Asynchronous Messaging - Event-driven communication"""
import queue
import threading

class EventBus:
    def __init__(self):
        self._subscribers = {}
    
    def subscribe(self, event_type, handler):
        if event_type not in self._subscribers:
            self._subscribers[event_type] = []
        self._subscribers[event_type].append(handler)
    
    def publish(self, event_type, data):
        if event_type in self._subscribers:
            for handler in self._subscribers[event_type]:
                threading.Thread(target=handler, args=(data,), daemon=True).start()

class OrderService:
    def __init__(self, bus):
        self.bus = bus
    
    def create_order(self, order_data):
        print(f"Order created: {order_data}")
        self.bus.publish("order_created", order_data)

class EmailService:
    def handle_order_created(self, order_data):
        print(f"Sending confirmation email for order: {order_data}")

class InventoryService:
    def handle_order_created(self, order_data):
        print(f"Updating inventory for order: {order_data}")

if __name__ == "__main__":
    bus = EventBus()
    
    email_service = EmailService()
    inventory_service = InventoryService()
    
    bus.subscribe("order_created", email_service.handle_order_created)
    bus.subscribe("order_created", inventory_service.handle_order_created)
    
    order_service = OrderService(bus)
    order_service.create_order({"id": 1, "items": ["Item1"]})
    
    import time
    time.sleep(0.2)
