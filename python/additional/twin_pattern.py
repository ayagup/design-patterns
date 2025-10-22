"""Twin Pattern - Allows modeling of multiple inheritance"""
class DrawingTwin:
    """One twin handles drawing"""
    def __init__(self, logic_twin=None):
        self.logic_twin = logic_twin
    
    def draw(self):
        if self.logic_twin:
            data = self.logic_twin.get_data()
            print(f"Drawing: {data}")
    
    def set_logic_twin(self, logic_twin):
        self.logic_twin = logic_twin

class LogicTwin:
    """Other twin handles logic"""
    def __init__(self, drawing_twin=None):
        self.drawing_twin = drawing_twin
        self.data = "Default data"
    
    def process(self, data):
        self.data = data
        print(f"Processing: {data}")
        if self.drawing_twin:
            self.drawing_twin.draw()
    
    def get_data(self):
        return self.data
    
    def set_drawing_twin(self, drawing_twin):
        self.drawing_twin = drawing_twin

class GameObject:
    """Combines both twins"""
    def __init__(self, name):
        self.name = name
        self.logic = LogicTwin()
        self.drawing = DrawingTwin()
        
        # Link twins
        self.logic.set_drawing_twin(self.drawing)
        self.drawing.set_logic_twin(self.logic)
    
    def update(self, data):
        self.logic.process(data)

if __name__ == "__main__":
    game_object = GameObject("Player")
    game_object.update("Player position: (10, 20)")
