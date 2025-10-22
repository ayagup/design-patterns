"""Log Aggregation - Centralize logs from all services"""
import datetime

class LogAggregator:
    def __init__(self):
        self.logs = []
    
    def log(self, service, level, message):
        entry = {
            "timestamp": datetime.datetime.now(),
            "service": service,
            "level": level,
            "message": message
        }
        self.logs.append(entry)
        print(f"[{entry['timestamp']}] [{service}] [{level}] {message}")
    
    def search(self, service=None, level=None):
        results = self.logs
        if service:
            results = [log for log in results if log['service'] == service]
        if level:
            results = [log for log in results if log['level'] == level]
        return results

class ServiceLogger:
    def __init__(self, service_name, aggregator):
        self.service_name = service_name
        self.aggregator = aggregator
    
    def info(self, message):
        self.aggregator.log(self.service_name, "INFO", message)
    
    def error(self, message):
        self.aggregator.log(self.service_name, "ERROR", message)

if __name__ == "__main__":
    aggregator = LogAggregator()
    
    user_service = ServiceLogger("UserService", aggregator)
    order_service = ServiceLogger("OrderService", aggregator)
    
    user_service.info("User created")
    order_service.info("Order placed")
    order_service.error("Payment failed")
    
    print("\nSearching ERROR logs:")
    errors = aggregator.search(level="ERROR")
    for log in errors:
        print(f"  {log}")
