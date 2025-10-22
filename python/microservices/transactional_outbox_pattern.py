"""Transactional Outbox - Reliable event publishing"""
class OutboxTable:
    def __init__(self):
        self.events = []
    
    def insert(self, event):
        self.events.append({"id": len(self.events) + 1, "event": event, "published": False})
    
    def get_unpublished(self):
        return [e for e in self.events if not e["published"]]
    
    def mark_published(self, event_id):
        for e in self.events:
            if e["id"] == event_id:
                e["published"] = True

class OrderService:
    def __init__(self, outbox):
        self.outbox = outbox
    
    def create_order(self, order_data):
        # Save order in database (transaction begins)
        print(f"Saving order: {order_data}")
        
        # Save event to outbox table (same transaction)
        event = {"type": "OrderCreated", "data": order_data}
        self.outbox.insert(event)
        
        # Transaction commits
        print("Order and event saved atomically")

class EventPublisher:
    def __init__(self, outbox):
        self.outbox = outbox
    
    def publish_events(self):
        events = self.outbox.get_unpublished()
        for event in events:
            print(f"Publishing event: {event['event']}")
            # Publish to message broker
            self.outbox.mark_published(event['id'])

if __name__ == "__main__":
    outbox = OutboxTable()
    order_service = OrderService(outbox)
    publisher = EventPublisher(outbox)
    
    order_service.create_order({"id": 1, "items": ["Item1"]})
    publisher.publish_events()
