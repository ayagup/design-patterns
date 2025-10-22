"""Service-Oriented Architecture"""
from abc import ABC, abstractmethod

class Service(ABC):
    @abstractmethod
    def execute(self, request):
        pass

class AuthService(Service):
    def execute(self, request):
        return {"authenticated": True, "user": request["username"]}

class DataService(Service):
    def execute(self, request):
        return {"data": f"Data for {request['user']}"}

class ServiceBus:
    def __init__(self):
        self.services = {}
    
    def register(self, name, service):
        self.services[name] = service
    
    def call(self, service_name, request):
        if service_name in self.services:
            return self.services[service_name].execute(request)

if __name__ == "__main__":
    bus = ServiceBus()
    bus.register("auth", AuthService())
    bus.register("data", DataService())
    
    auth_result = bus.call("auth", {"username": "alice"})
    print(f"Auth: {auth_result}")
    
    data_result = bus.call("data", {"user": "alice"})
    print(f"Data: {data_result}")
