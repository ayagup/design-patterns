"""Mediator Pattern"""
class Mediator:
    def __init__(self):
        self._colleagues = []
    
    def add(self, colleague):
        self._colleagues.append(colleague)
    
    def send(self, message, sender):
        for colleague in self._colleagues:
            if colleague != sender:
                colleague.receive(message)

class Colleague:
    def __init__(self, mediator, name):
        self._mediator = mediator
        self.name = name
    
    def send(self, message):
        print(f"{self.name} sends: {message}")
        self._mediator.send(message, self)
    
    def receive(self, message):
        print(f"{self.name} receives: {message}")

if __name__ == "__main__":
    mediator = Mediator()
    c1 = Colleague(mediator, "Alice")
    c2 = Colleague(mediator, "Bob")
    mediator.add(c1)
    mediator.add(c2)
    c1.send("Hello!")
