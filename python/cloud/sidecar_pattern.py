"""Sidecar - Deploy helper alongside application"""
class Application:
    def process(self, request):
        return f"Processed: {request}"

class SidecarProxy:
    def __init__(self, app):
        self.app = app
    
    def handle_request(self, request):
        # Logging
        print(f"[SIDECAR] Request: {request}")
        
        # Monitoring
        print(f"[SIDECAR] Monitoring metrics")
        
        # Call main application
        result = self.app.process(request)
        
        # Logging
        print(f"[SIDECAR] Response: {result}")
        
        return result

if __name__ == "__main__":
    app = Application()
    sidecar = SidecarProxy(app)
    result = sidecar.handle_request("test")
    print(f"Final result: {result}")
