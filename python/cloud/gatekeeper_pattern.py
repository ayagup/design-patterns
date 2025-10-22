"""Gatekeeper - Security validation layer"""
class GatekeeperValidator:
    def __init__(self):
        self.blocked_ips = set()
        self.rate_limits = {}
    
    def validate_request(self, request):
        # Check IP blacklist
        if request.get('ip') in self.blocked_ips:
            return False, "IP blocked"
        
        # Check rate limiting
        ip = request.get('ip')
        if ip in self.rate_limits:
            if self.rate_limits[ip] >= 100:
                return False, "Rate limit exceeded"
            self.rate_limits[ip] += 1
        else:
            self.rate_limits[ip] = 1
        
        # Sanitize input
        if not self.sanitize_input(request.get('data', '')):
            return False, "Invalid input"
        
        return True, "Valid"
    
    def sanitize_input(self, data):
        # Basic sanitization
        dangerous_chars = ['<', '>', 'script', 'DROP', 'DELETE']
        return not any(char in str(data).upper() for char in dangerous_chars)
    
    def block_ip(self, ip):
        self.blocked_ips.add(ip)

class ProtectedApplication:
    def process_request(self, data):
        return f"Processing: {data}"

class Gatekeeper:
    def __init__(self, validator, app):
        self.validator = validator
        self.app = app
    
    def handle_request(self, request):
        valid, message = self.validator.validate_request(request)
        
        if not valid:
            print(f"Request rejected: {message}")
            return None
        
        # Forward to application
        return self.app.process_request(request.get('data'))

if __name__ == "__main__":
    validator = GatekeeperValidator()
    app = ProtectedApplication()
    gatekeeper = Gatekeeper(validator, app)
    
    # Valid request
    result = gatekeeper.handle_request({
        "ip": "192.168.1.1",
        "data": "legitimate data"
    })
    print(f"Result: {result}")
    
    # Malicious request
    result = gatekeeper.handle_request({
        "ip": "192.168.1.2",
        "data": "<script>alert('XSS')</script>"
    })
    print(f"Result: {result}")
