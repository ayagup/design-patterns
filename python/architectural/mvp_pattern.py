"""MVP Pattern"""
class Model:
    def __init__(self):
        self.data = 0
    
    def increment(self):
        self.data += 1

class View:
    def __init__(self, presenter):
        self.presenter = presenter
    
    def button_clicked(self):
        self.presenter.on_button_click()
    
    def display(self, value):
        print(f"Value: {value}")

class Presenter:
    def __init__(self, model, view):
        self.model = model
        self.view = view
    
    def on_button_click(self):
        self.model.increment()
        self.view.display(self.model.data)

if __name__ == "__main__":
    model = Model()
    presenter = Presenter(model, None)
    view = View(presenter)
    presenter.view = view
    view.button_clicked()
