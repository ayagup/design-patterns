"""Valet Key - Restricted direct access token"""
import time

class ValetKey:
    def __init__(self, resource, permissions, expiry):
        self.resource = resource
        self.permissions = permissions
        self.expiry = expiry
    
    def is_valid(self):
        return time.time() < self.expiry
    
    def can(self, permission):
        return permission in self.permissions

class StorageService:
    def generate_valet_key(self, resource, permissions, duration=3600):
        expiry = time.time() + duration
        key = ValetKey(resource, permissions, expiry)
        return key
    
    def access_resource(self, key, permission):
        if not key.is_valid():
            return "Key expired"
        if not key.can(permission):
            return "Permission denied"
        return f"Accessing {key.resource} with {permission}"

if __name__ == "__main__":
    storage = StorageService()
    key = storage.generate_valet_key("file.pdf", ["read"], duration=3600)
    
    print(storage.access_resource(key, "read"))
    print(storage.access_resource(key, "write"))
