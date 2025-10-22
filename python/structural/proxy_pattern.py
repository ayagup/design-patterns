"""
Proxy Pattern - Provides a surrogate or placeholder
"""

from abc import ABC, abstractmethod


class Image(ABC):
    @abstractmethod
    def display(self):
        pass


class RealImage(Image):
    def __init__(self, filename: str):
        self.filename = filename
        self._load_from_disk()
    
    def _load_from_disk(self):
        print(f"Loading image: {self.filename}")
    
    def display(self):
        print(f"Displaying {self.filename}")


class ProxyImage(Image):
    """Virtual Proxy - lazy loading"""
    def __init__(self, filename: str):
        self.filename = filename
        self._real_image = None
    
    def display(self):
        if self._real_image is None:
            self._real_image = RealImage(self.filename)
        self._real_image.display()


class ProtectionProxy:
    """Protection Proxy - access control"""
    def __init__(self, user_role: str):
        self.user_role = user_role
    
    def access_resource(self):
        if self.user_role == "admin":
            print("Access granted")
        else:
            print("Access denied")


if __name__ == "__main__":
    print("=== Proxy Pattern Demo ===\n")
    
    # Virtual Proxy
    print("--- Virtual Proxy ---")
    image = ProxyImage("photo.jpg")
    print("Proxy created (not loaded yet)")
    image.display()  # Loads now
    image.display()  # Uses cached
    
    # Protection Proxy
    print("\n--- Protection Proxy ---")
    admin = ProtectionProxy("admin")
    user = ProtectionProxy("user")
    admin.access_resource()
    user.access_resource()
