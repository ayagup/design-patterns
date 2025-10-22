"""Branch Microservice - Parallel service invocation"""
import concurrent.futures

class EmailService:
    def send(self, user_id):
        return f"Email sent to user {user_id}"

class SMSService:
    def send(self, user_id):
        return f"SMS sent to user {user_id}"

class PushService:
    def send(self, user_id):
        return f"Push notification sent to user {user_id}"

class NotificationBranch:
    def __init__(self):
        self.email = EmailService()
        self.sms = SMSService()
        self.push = PushService()
    
    def send_all(self, user_id):
        with concurrent.futures.ThreadPoolExecutor() as executor:
            futures = {
                "email": executor.submit(self.email.send, user_id),
                "sms": executor.submit(self.sms.send, user_id),
                "push": executor.submit(self.push.send, user_id)
            }
            
            results = {}
            for key, future in futures.items():
                results[key] = future.result()
            
            return results

if __name__ == "__main__":
    notifier = NotificationBranch()
    results = notifier.send_all(1)
    print(f"Results: {results}")
