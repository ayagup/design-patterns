"""Blackboard Pattern"""
class Blackboard:
    def __init__(self):
        self.data = {}
    
    def put(self, key, value):
        self.data[key] = value
    
    def get(self, key):
        return self.data.get(key)

class KnowledgeSource:
    def update(self, blackboard):
        pass

class Solver1(KnowledgeSource):
    def update(self, blackboard):
        if 'input' in blackboard.data:
            blackboard.put('result1', blackboard.get('input') * 2)

if __name__ == "__main__":
    bb = Blackboard()
    bb.put('input', 5)
    solver = Solver1()
    solver.update(bb)
    print(f"Result: {bb.get('result1')}")
