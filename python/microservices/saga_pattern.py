"""Saga Pattern"""
class SagaStep:
    def execute(self):
        pass
    
    def compensate(self):
        pass

class OrderStep(SagaStep):
    def execute(self):
        print("Creating order")
        return True
    
    def compensate(self):
        print("Cancelling order")

class PaymentStep(SagaStep):
    def execute(self):
        print("Processing payment")
        return True
    
    def compensate(self):
        print("Refunding payment")

class Saga:
    def __init__(self):
        self.steps = []
    
    def add_step(self, step):
        self.steps.append(step)
    
    def execute(self):
        executed = []
        for step in self.steps:
            if step.execute():
                executed.append(step)
            else:
                for s in reversed(executed):
                    s.compensate()
                return False
        return True

if __name__ == "__main__":
    saga = Saga()
    saga.add_step(OrderStep())
    saga.add_step(PaymentStep())
    saga.execute()
