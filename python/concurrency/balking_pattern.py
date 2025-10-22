"""Balking Pattern - Only executes if object is in correct state"""
class Service:
    def __init__(self):
        self.is_ready = False
    
    def initialize(self):
        self.is_ready = True
    
    def execute(self):
        if not self.is_ready:
            print("Service not ready, balking")
            return None
        print("Executing service")
        return "Result"

if __name__ == "__main__":
    service = Service()
    service.execute()  # Balks
    service.initialize()
    service.execute()  # Executes
