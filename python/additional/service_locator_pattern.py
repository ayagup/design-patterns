"""Service Locator - Centralized registry for service lookup"""
class ServiceLocator:
    _services = {}
    
    @classmethod
    def register_service(cls, name, service):
        cls._services[name] = service
        print(f"Service registered: {name}")
    
    @classmethod
    def get_service(cls, name):
        service = cls._services.get(name)
        if service is None:
            raise Exception(f"Service not found: {name}")
        return service

class EmailService:
    def send_email(self, to, message):
        print(f"Sending email to {to}: {message}")

class SMSService:
    def send_sms(self, phone, message):
        print(f"Sending SMS to {phone}: {message}")

class NotificationManager:
    def __init__(self):
        # Use service locator instead of direct dependencies
        pass
    
    def send_notification(self, type, recipient, message):
        if type == "email":
            service = ServiceLocator.get_service("email")
            service.send_email(recipient, message)
        elif type == "sms":
            service = ServiceLocator.get_service("sms")
            service.send_sms(recipient, message)

if __name__ == "__main__":
    # Register services
    ServiceLocator.register_service("email", EmailService())
    ServiceLocator.register_service("sms", SMSService())
    
    # Use services via locator
    manager = NotificationManager()
    manager.send_notification("email", "user@example.com", "Hello via email")
    manager.send_notification("sms", "+1234567890", "Hello via SMS")
