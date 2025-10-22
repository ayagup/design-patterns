"""Throttling Pattern"""
import time

class Throttle:
    def __init__(self, rate_limit, time_window):
        self.rate_limit = rate_limit
        self.time_window = time_window
        self.requests = []
    
    def allow_request(self):
        now = time.time()
        self.requests = [r for r in self.requests if now - r < self.time_window]
        
        if len(self.requests) < self.rate_limit:
            self.requests.append(now)
            return True
        return False

if __name__ == "__main__":
    throttle = Throttle(rate_limit=3, time_window=1)
    
    for i in range(5):
        if throttle.allow_request():
            print(f"Request {i} allowed")
        else:
            print(f"Request {i} throttled")
        time.sleep(0.2)
