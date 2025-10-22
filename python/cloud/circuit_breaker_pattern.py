"""Circuit Breaker Pattern"""
import time
from enum import Enum

class State(Enum):
    CLOSED = 1
    OPEN = 2
    HALF_OPEN = 3

class CircuitBreaker:
    def __init__(self, threshold=3, timeout=5):
        self.threshold = threshold
        self.timeout = timeout
        self.failures = 0
        self.last_failure_time = None
        self.state = State.CLOSED
    
    def call(self, func):
        if self.state == State.OPEN:
            if time.time() - self.last_failure_time > self.timeout:
                self.state = State.HALF_OPEN
            else:
                raise Exception("Circuit is OPEN")
        
        try:
            result = func()
            if self.state == State.HALF_OPEN:
                self.state = State.CLOSED
                self.failures = 0
            return result
        except Exception as e:
            self.failures += 1
            self.last_failure_time = time.time()
            if self.failures >= self.threshold:
                self.state = State.OPEN
            raise e

if __name__ == "__main__":
    def unreliable_service():
        import random
        if random.random() < 0.5:
            raise Exception("Service failed")
        return "Success"
    
    cb = CircuitBreaker(threshold=2)
    for i in range(5):
        try:
            result = cb.call(unreliable_service)
            print(f"Call {i}: {result}")
        except Exception as e:
            print(f"Call {i}: {e}")
