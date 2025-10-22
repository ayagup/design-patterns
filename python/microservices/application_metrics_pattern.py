"""Application Metrics - Instruments services to gather metrics"""
import time
from collections import defaultdict

class MetricsCollector:
    def __init__(self):
        self.counters = defaultdict(int)
        self.gauges = {}
        self.histograms = defaultdict(list)
    
    def increment_counter(self, name, value=1):
        self.counters[name] += value
    
    def set_gauge(self, name, value):
        self.gauges[name] = value
    
    def record_histogram(self, name, value):
        self.histograms[name].append(value)
    
    def get_metrics(self):
        return {
            "counters": dict(self.counters),
            "gauges": self.gauges,
            "histograms": {k: {
                "count": len(v),
                "avg": sum(v) / len(v) if v else 0
            } for k, v in self.histograms.items()}
        }

class ServiceWithMetrics:
    def __init__(self, metrics):
        self.metrics = metrics
    
    def process_request(self):
        start = time.time()
        
        self.metrics.increment_counter("requests_total")
        
        # Simulate processing
        time.sleep(0.01)
        
        duration = (time.time() - start) * 1000
        self.metrics.record_histogram("request_duration_ms", duration)
        
        self.metrics.set_gauge("active_requests", 5)

if __name__ == "__main__":
    metrics = MetricsCollector()
    service = ServiceWithMetrics(metrics)
    
    for _ in range(10):
        service.process_request()
    
    print("Metrics:")
    import json
    print(json.dumps(metrics.get_metrics(), indent=2))
