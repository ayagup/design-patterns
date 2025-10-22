"""Gateway Routing - Route requests to services"""
class ServiceV1:
    def handle(self, request):
        return {"version": "v1", "data": f"V1 processed: {request}"}

class ServiceV2:
    def handle(self, request):
        return {"version": "v2", "data": f"V2 processed: {request}"}

class ServiceV3:
    def handle(self, request):
        return {"version": "v3", "data": f"V3 processed: {request}"}

class RoutingGateway:
    def __init__(self):
        self.routes = {}
        self.default_service = None
    
    def register_route(self, pattern, service):
        self.routes[pattern] = service
    
    def set_default(self, service):
        self.default_service = service
    
    def route_request(self, request):
        path = request.get('path', '')
        version = request.get('version')
        
        # Route based on version header
        if version:
            route_key = f"version:{version}"
            if route_key in self.routes:
                return self.routes[route_key].handle(request)
        
        # Route based on path
        for pattern, service in self.routes.items():
            if pattern in path:
                return service.handle(request)
        
        # Default route
        if self.default_service:
            return self.default_service.handle(request)
        
        return {"error": "No route found"}

if __name__ == "__main__":
    gateway = RoutingGateway()
    
    # Register routes
    gateway.register_route("version:v1", ServiceV1())
    gateway.register_route("version:v2", ServiceV2())
    gateway.register_route("/api/v3", ServiceV3())
    gateway.set_default(ServiceV2())
    
    # Route by version header
    result = gateway.route_request({"version": "v1", "data": "test"})
    print(f"Result: {result}")
    
    # Route by path
    result = gateway.route_request({"path": "/api/v3/users", "data": "test"})
    print(f"Result: {result}")
    
    # Default route
    result = gateway.route_request({"data": "test"})
    print(f"Result: {result}")
