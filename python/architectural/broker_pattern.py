"""Broker Pattern - Coordinates distributed components"""
class Message:
    def __init__(self, sender, receiver, content):
        self.sender = sender
        self.receiver = receiver
        self.content = content

class Broker:
    def __init__(self):
        self.components = {}
    
    def register(self, name, component):
        self.components[name] = component
    
    def send(self, message):
        if message.receiver in self.components:
            self.components[message.receiver].receive(message)

class Component:
    def __init__(self, name, broker):
        self.name = name
        self.broker = broker
    
    def send(self, receiver, content):
        msg = Message(self.name, receiver, content)
        self.broker.send(msg)
    
    def receive(self, message):
        print(f"{self.name} received from {message.sender}: {message.content}")

if __name__ == "__main__":
    broker = Broker()
    comp1 = Component("Comp1", broker)
    comp2 = Component("Comp2", broker)
    
    broker.register("Comp1", comp1)
    broker.register("Comp2", comp2)
    
    comp1.send("Comp2", "Hello")
