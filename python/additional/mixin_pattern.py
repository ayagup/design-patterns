"""Mixin Pattern - Provides methods for use by other classes"""
class TimestampMixin:
    """Adds timestamp functionality"""
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.created_at = None
        self.updated_at = None
    
    def set_created(self):
        from datetime import datetime
        self.created_at = datetime.now()
    
    def set_updated(self):
        from datetime import datetime
        self.updated_at = datetime.now()

class ValidationMixin:
    """Adds validation functionality"""
    def validate(self):
        errors = []
        for field in self.required_fields:
            if not getattr(self, field, None):
                errors.append(f"{field} is required")
        return errors

class SerializationMixin:
    """Adds serialization functionality"""
    def to_dict(self):
        return {
            key: value for key, value in self.__dict__.items()
            if not key.startswith('_')
        }

class User(TimestampMixin, ValidationMixin, SerializationMixin):
    """User class using multiple mixins"""
    required_fields = ['username', 'email']
    
    def __init__(self, username=None, email=None):
        super().__init__()
        self.username = username
        self.email = email
        self.set_created()

if __name__ == "__main__":
    # Create user with mixins
    user = User("alice", "alice@example.com")
    
    # Use timestamp mixin
    print(f"Created at: {user.created_at}")
    
    # Use serialization mixin
    print(f"User dict: {user.to_dict()}")
    
    # Use validation mixin
    invalid_user = User()
    errors = invalid_user.validate()
    print(f"Validation errors: {errors}")
