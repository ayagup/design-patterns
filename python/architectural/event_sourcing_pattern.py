"""Event Sourcing - Store state as events"""
class Event:
    def __init__(self, type, data):
        self.type = type
        self.data = data

class EventStore:
    def __init__(self):
        self.events = []
    
    def append(self, event):
        self.events.append(event)
    
    def get_events(self):
        return self.events

class Account:
    def __init__(self, event_store):
        self.event_store = event_store
        self.balance = 0
    
    def deposit(self, amount):
        event = Event("Deposited", {"amount": amount})
        self.event_store.append(event)
        self.balance += amount
    
    def withdraw(self, amount):
        event = Event("Withdrawn", {"amount": amount})
        self.event_store.append(event)
        self.balance -= amount
    
    def replay(self):
        self.balance = 0
        for event in self.event_store.get_events():
            if event.type == "Deposited":
                self.balance += event.data["amount"]
            elif event.type == "Withdrawn":
                self.balance -= event.data["amount"]

if __name__ == "__main__":
    store = EventStore()
    account = Account(store)
    account.deposit(100)
    account.withdraw(30)
    print(f"Balance: {account.balance}")
    
    # Replay events
    account.balance = 0
    account.replay()
    print(f"After replay: {account.balance}")
