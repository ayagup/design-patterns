"""Service Mesh - Infrastructure layer for service communication"""
class ServiceProxy:
    def __init__(self, service, circuit_breaker):
        self.service = service
        self.circuit_breaker = circuit_breaker
    
    def call(self, request):
        # Circuit breaker
        if self.circuit_breaker.is_open():
            return {"error": "Circuit open"}
        
        # Retry logic
        try:
            result = self.service.handle(request)
            self.circuit_breaker.record_success()
            return result
        except Exception as e:
            self.circuit_breaker.record_failure()
            raise e

class CircuitBreaker:
    def __init__(self, threshold=3):
        self.threshold = threshold
        self.failures = 0
        self.state = "CLOSED"
    
    def is_open(self):
        return self.state == "OPEN"
    
    def record_success(self):
        self.failures = 0
        self.state = "CLOSED"
    
    def record_failure(self):
        self.failures += 1
        if self.failures >= self.threshold:
            self.state = "OPEN"

class Service:
    def handle(self, request):
        return {"result": f"Processed {request}"}

if __name__ == "__main__":
    service = Service()
    cb = CircuitBreaker()
    proxy = ServiceProxy(service, cb)
    
    result = proxy.call("request")
    print(f"Result: {result}")
