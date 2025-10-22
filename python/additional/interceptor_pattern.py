"""
Interceptor Pattern

Intent: Intercepts method calls to add behavior before/after execution.
Used in AOP (Aspect-Oriented Programming), logging, security, caching.
"""

from abc import ABC, abstractmethod
from typing import Any, Callable
from functools import wraps
import time


class Interceptor(ABC):
    """Base interceptor interface"""
    
    @abstractmethod
    def before(self, method_name: str, *args, **kwargs):
        """Called before method execution"""
        pass
    
    @abstractmethod
    def after(self, method_name: str, result: Any):
        """Called after method execution"""
        pass
    
    @abstractmethod
    def on_error(self, method_name: str, error: Exception):
        """Called when method raises exception"""
        pass


class LoggingInterceptor(Interceptor):
    """Logs method calls"""
    
    def before(self, method_name: str, *args, **kwargs):
        print(f"[LOG] Calling {method_name} with args={args}, kwargs={kwargs}")
    
    def after(self, method_name: str, result: Any):
        print(f"[LOG] {method_name} returned: {result}")
    
    def on_error(self, method_name: str, error: Exception):
        print(f"[LOG] {method_name} raised error: {error}")


class TimingInterceptor(Interceptor):
    """Measures method execution time"""
    
    def __init__(self):
        self.start_time = None
    
    def before(self, method_name: str, *args, **kwargs):
        self.start_time = time.time()
    
    def after(self, method_name: str, result: Any):
        elapsed = time.time() - self.start_time
        print(f"[TIMING] {method_name} took {elapsed:.4f} seconds")
    
    def on_error(self, method_name: str, error: Exception):
        elapsed = time.time() - self.start_time
        print(f"[TIMING] {method_name} failed after {elapsed:.4f} seconds")


class SecurityInterceptor(Interceptor):
    """Checks authorization before method execution"""
    
    def __init__(self, allowed_users: set):
        self.allowed_users = allowed_users
        self.current_user = None
    
    def set_user(self, user: str):
        self.current_user = user
    
    def before(self, method_name: str, *args, **kwargs):
        if self.current_user not in self.allowed_users:
            raise PermissionError(f"User {self.current_user} not authorized")
        print(f"[SECURITY] User {self.current_user} authorized for {method_name}")
    
    def after(self, method_name: str, result: Any):
        pass
    
    def on_error(self, method_name: str, error: Exception):
        print(f"[SECURITY] Authorization check failed: {error}")


def intercepted(*interceptors: Interceptor):
    """Decorator to add interceptors to a method"""
    def decorator(func: Callable):
        @wraps(func)
        def wrapper(*args, **kwargs):
            method_name = func.__name__
            
            # Before interceptors
            for interceptor in interceptors:
                interceptor.before(method_name, *args, **kwargs)
            
            try:
                # Execute method
                result = func(*args, **kwargs)
                
                # After interceptors
                for interceptor in interceptors:
                    interceptor.after(method_name, result)
                
                return result
            except Exception as e:
                # Error interceptors
                for interceptor in interceptors:
                    interceptor.on_error(method_name, e)
                raise
        
        return wrapper
    return decorator


class BusinessService:
    """Service with intercepted methods"""
    
    def __init__(self):
        self.logging = LoggingInterceptor()
        self.timing = TimingInterceptor()
        self.security = SecurityInterceptor({'admin', 'user'})
    
    @intercepted(LoggingInterceptor(), TimingInterceptor())
    def process_data(self, data: str) -> str:
        """Process some data"""
        time.sleep(0.1)  # Simulate processing
        return f"Processed: {data}"
    
    def secure_operation(self, action: str) -> str:
        """Manually apply security interceptor"""
        method_name = "secure_operation"
        
        self.security.before(method_name, action)
        try:
            result = f"Executed: {action}"
            self.security.after(method_name, result)
            return result
        except Exception as e:
            self.security.on_error(method_name, e)
            raise


if __name__ == "__main__":
    print("=== Interceptor Pattern Demo ===\n")
    
    # Basic intercepted method
    print("1. Basic Intercepted Method:")
    service = BusinessService()
    result = service.process_data("test data")
    print(f"Result: {result}\n")
    
    # Security interceptor
    print("2. Security Interceptor:")
    service.security.set_user("admin")
    try:
        result = service.secure_operation("delete_records")
        print(f"Result: {result}\n")
    except PermissionError as e:
        print(f"Error: {e}\n")
    
    # Unauthorized access
    print("3. Unauthorized Access:")
    service.security.set_user("guest")
    try:
        result = service.secure_operation("delete_records")
    except PermissionError as e:
        print(f"Access denied: {e}\n")
    
    print("✓ Interceptor pattern enables cross-cutting concerns!")
