"""Ambassador Pattern - Helper services for network requests"""
class Ambassador:
    def __init__(self, target_service):
        self.target_service = target_service
        self.retry_count = 3
    
    def request(self, data):
        for attempt in range(self.retry_count):
            try:
                return self.target_service.call(data)
            except Exception as e:
                if attempt < self.retry_count - 1:
                    print(f"Retry attempt {attempt + 1}")
                    continue
                raise e

class TargetService:
    def call(self, data):
        return f"Response for {data}"

if __name__ == "__main__":
    service = TargetService()
    ambassador = Ambassador(service)
    result = ambassador.request("test")
    print(result)
