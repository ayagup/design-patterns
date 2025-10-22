"""MVVM Pattern - Model-View-ViewModel"""
class Model:
    def __init__(self):
        self.data = "Initial Data"

class ViewModel:
    def __init__(self, model):
        self.model = model
        self._observers = []
    
    def get_data(self):
        return self.model.data
    
    def set_data(self, value):
        self.model.data = value
        self.notify_observers()
    
    def add_observer(self, observer):
        self._observers.append(observer)
    
    def notify_observers(self):
        for observer in self._observers:
            observer.update(self.get_data())

class View:
    def __init__(self, viewmodel):
        self.viewmodel = viewmodel
        self.viewmodel.add_observer(self)
    
    def update(self, data):
        print(f"View updated with: {data}")

if __name__ == "__main__":
    model = Model()
    viewmodel = ViewModel(model)
    view = View(viewmodel)
    viewmodel.set_data("New Data")
