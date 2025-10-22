"""Gateway Offloading - Offload shared functionality"""
import time

class SSLTerminationGateway:
    def terminate_ssl(self, request):
        print("SSL terminated at gateway")
        return request
    
    def add_ssl(self, response):
        print("SSL added for response")
        return response

class AuthenticationGateway:
    def __init__(self):
        self.tokens = {"valid_token": "user123"}
    
    def authenticate(self, request):
        token = request.get('auth_token')
        if token in self.tokens:
            request['user'] = self.tokens[token]
            print(f"User authenticated: {request['user']}")
            return request
        raise Exception("Authentication failed")

class CompressionGateway:
    def compress_response(self, response):
        print("Response compressed")
        response['compressed'] = True
        return response

class BackendService:
    def process(self, request):
        print(f"Backend processing for user: {request.get('user')}")
        return {"data": "result", "user": request.get('user')}

class OffloadingGateway:
    def __init__(self, backend):
        self.backend = backend
        self.ssl_gateway = SSLTerminationGateway()
        self.auth_gateway = AuthenticationGateway()
        self.compression_gateway = CompressionGateway()
    
    def handle_request(self, request):
        try:
            # Offload SSL termination
            request = self.ssl_gateway.terminate_ssl(request)
            
            # Offload authentication
            request = self.auth_gateway.authenticate(request)
            
            # Forward to backend
            response = self.backend.process(request)
            
            # Offload compression
            response = self.compression_gateway.compress_response(response)
            
            # Offload SSL
            response = self.ssl_gateway.add_ssl(response)
            
            return response
        except Exception as e:
            return {"error": str(e)}

if __name__ == "__main__":
    backend = BackendService()
    gateway = OffloadingGateway(backend)
    
    request = {
        "auth_token": "valid_token",
        "data": "request data"
    }
    
    result = gateway.handle_request(request)
    print(f"Final result: {result}")
