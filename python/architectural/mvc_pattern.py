"""MVC Pattern"""
class Model:
    def __init__(self):
        self.data = []
    
    def add(self, item):
        self.data.append(item)
    
    def get_all(self):
        return self.data

class View:
    def display(self, data):
        print("Items:", ", ".join(data))

class Controller:
    def __init__(self, model, view):
        self.model = model
        self.view = view
    
    def add_item(self, item):
        self.model.add(item)
        self.view.display(self.model.get_all())

if __name__ == "__main__":
    model = Model()
    view = View()
    controller = Controller(model, view)
    controller.add_item("Item1")
    controller.add_item("Item2")
