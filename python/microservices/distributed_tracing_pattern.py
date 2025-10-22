"""Distributed Tracing - Trace requests across services"""
import uuid
import time

class Span:
    def __init__(self, trace_id, span_id, operation):
        self.trace_id = trace_id
        self.span_id = span_id
        self.operation = operation
        self.start_time = time.time()
        self.end_time = None
    
    def finish(self):
        self.end_time = time.time()
        print(f"[TRACE {self.trace_id}] {self.operation}: {(self.end_time - self.start_time) * 1000:.2f}ms")

class TracingContext:
    def __init__(self):
        self.trace_id = str(uuid.uuid4())[:8]
    
    def start_span(self, operation):
        span_id = str(uuid.uuid4())[:8]
        return Span(self.trace_id, span_id, operation)

class ServiceA:
    def handle_request(self, context):
        span = context.start_span("ServiceA.handle_request")
        time.sleep(0.01)
        
        # Call ServiceB
        service_b = ServiceB()
        service_b.process(context)
        
        span.finish()

class ServiceB:
    def process(self, context):
        span = context.start_span("ServiceB.process")
        time.sleep(0.01)
        span.finish()

if __name__ == "__main__":
    context = TracingContext()
    print(f"Starting trace: {context.trace_id}\n")
    
    service_a = ServiceA()
    service_a.handle_request(context)
