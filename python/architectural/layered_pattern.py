"""Layered Architecture Pattern"""
class DataLayer:
    def get_data(self):
        return {"id": 1, "name": "Alice"}

class BusinessLayer:
    def __init__(self, data_layer):
        self.data_layer = data_layer
    
    def process(self):
        data = self.data_layer.get_data()
        data['processed'] = True
        return data

class PresentationLayer:
    def __init__(self, business_layer):
        self.business_layer = business_layer
    
    def display(self):
        data = self.business_layer.process()
        print(f"Display: {data}")

if __name__ == "__main__":
    data = DataLayer()
    business = BusinessLayer(data)
    presentation = PresentationLayer(business)
    presentation.display()
