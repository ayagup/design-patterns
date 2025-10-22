"""Memento Pattern"""
class Memento:
    def __init__(self, state):
        self._state = state
    
    def get_state(self):
        return self._state

class Originator:
    def __init__(self):
        self._state = ""
    
    def set_state(self, state):
        self._state = state
    
    def save(self):
        return Memento(self._state)
    
    def restore(self, memento):
        self._state = memento.get_state()

if __name__ == "__main__":
    orig = Originator()
    orig.set_state("State1")
    saved = orig.save()
    orig.set_state("State2")
    print(f"Current: {orig._state}")
    orig.restore(saved)
    print(f"Restored: {orig._state}")
