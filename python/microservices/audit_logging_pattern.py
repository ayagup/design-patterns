"""Audit Logging - Records user actions"""
import datetime

class AuditLogger:
    def __init__(self):
        self.audit_log = []
    
    def log_action(self, user, action, resource, details=None):
        entry = {
            "timestamp": datetime.datetime.now(),
            "user": user,
            "action": action,
            "resource": resource,
            "details": details
        }
        self.audit_log.append(entry)
        print(f"[AUDIT] {entry}")
    
    def get_audit_trail(self, user=None, resource=None):
        results = self.audit_log
        if user:
            results = [e for e in results if e['user'] == user]
        if resource:
            results = [e for e in results if e['resource'] == resource]
        return results

class UserService:
    def __init__(self, audit_logger):
        self.audit = audit_logger
    
    def create_user(self, admin_user, user_data):
        self.audit.log_action(admin_user, "CREATE", "User", user_data)
        return user_data
    
    def update_user(self, admin_user, user_id, changes):
        self.audit.log_action(admin_user, "UPDATE", f"User:{user_id}", changes)

if __name__ == "__main__":
    audit = AuditLogger()
    service = UserService(audit)
    
    service.create_user("admin", {"name": "Alice", "email": "alice@example.com"})
    service.update_user("admin", 1, {"email": "alice.new@example.com"})
    
    print("\nAudit trail for admin:")
    trail = audit.get_audit_trail(user="admin")
    for entry in trail:
        print(f"  {entry}")
