"""Exception Tracking - Centralizes exception reporting"""
import datetime
import traceback

class ExceptionTracker:
    def __init__(self):
        self.exceptions = []
    
    def track(self, service, exception, context=None):
        entry = {
            "timestamp": datetime.datetime.now(),
            "service": service,
            "type": type(exception).__name__,
            "message": str(exception),
            "traceback": traceback.format_exc(),
            "context": context
        }
        self.exceptions.append(entry)
        print(f"[EXCEPTION] {service}: {exception}")
    
    def get_exceptions(self, service=None):
        if service:
            return [e for e in self.exceptions if e['service'] == service]
        return self.exceptions

class ServiceWithTracking:
    def __init__(self, tracker):
        self.tracker = tracker
    
    def risky_operation(self):
        try:
            # Simulate error
            raise ValueError("Something went wrong")
        except Exception as e:
            self.tracker.track("MyService", e, {"operation": "risky_operation"})

if __name__ == "__main__":
    tracker = ExceptionTracker()
    service = ServiceWithTracking(tracker)
    
    service.risky_operation()
    
    print("\nAll exceptions:")
    for exc in tracker.get_exceptions():
        print(f"  {exc['service']}: {exc['type']} - {exc['message']}")
