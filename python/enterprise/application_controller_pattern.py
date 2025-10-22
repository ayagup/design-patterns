"""Application Controller - Navigation flow control"""
class ApplicationController:
    def __init__(self):
        self.flows = {}
    
    def register_flow(self, name, steps):
        self.flows[name] = steps
    
    def start_flow(self, name):
        if name not in self.flows:
            return None
        return FlowExecution(self.flows[name])

class FlowExecution:
    def __init__(self, steps):
        self.steps = steps
        self.current_step = 0
    
    def next(self):
        if self.current_step < len(self.steps):
            step = self.steps[self.current_step]
            self.current_step += 1
            return step
        return None

if __name__ == "__main__":
    controller = ApplicationController()
    controller.register_flow("checkout", ["cart", "shipping", "payment", "confirmation"])
    
    flow = controller.start_flow("checkout")
    while True:
        step = flow.next()
        if step is None:
            break
        print(f"Current step: {step}")
