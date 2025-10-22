"""Publisher-Subscriber Pattern - Asynchronous messaging"""
class Topic:
    def __init__(self, name):
        self.name = name
        self.subscribers = []
    
    def subscribe(self, subscriber):
        self.subscribers.append(subscriber)
        print(f"{subscriber.name} subscribed to {self.name}")
    
    def unsubscribe(self, subscriber):
        self.subscribers.remove(subscriber)
        print(f"{subscriber.name} unsubscribed from {self.name}")
    
    def publish(self, message):
        print(f"\n[Topic: {self.name}] Publishing: {message}")
        for subscriber in self.subscribers:
            subscriber.receive(message)

class Publisher:
    def __init__(self, name):
        self.name = name
    
    def publish(self, topic, message):
        print(f"[Publisher: {self.name}] Publishing to {topic.name}")
        topic.publish(message)

class Subscriber:
    def __init__(self, name):
        self.name = name
        self.received_messages = []
    
    def receive(self, message):
        self.received_messages.append(message)
        print(f"  [{self.name}] Received: {message}")

if __name__ == "__main__":
    # Create topics
    news_topic = Topic("News")
    sports_topic = Topic("Sports")
    
    # Create publishers
    news_publisher = Publisher("NewsAgency")
    sports_publisher = Publisher("SportsNetwork")
    
    # Create subscribers
    alice = Subscriber("Alice")
    bob = Subscriber("Bob")
    charlie = Subscriber("Charlie")
    
    # Subscribe to topics
    news_topic.subscribe(alice)
    news_topic.subscribe(bob)
    sports_topic.subscribe(bob)
    sports_topic.subscribe(charlie)
    
    # Publish messages
    news_publisher.publish(news_topic, "Breaking: New discovery!")
    sports_publisher.publish(sports_topic, "Team wins championship!")
    
    print(f"\nAlice received: {alice.received_messages}")
    print(f"Bob received: {bob.received_messages}")
    print(f"Charlie received: {charlie.received_messages}")
