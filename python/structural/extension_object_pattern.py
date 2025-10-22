"""
Extension Object Pattern - Adds functionality without changing hierarchy
"""

from typing import Dict, Any


class Extension:
    pass


class SerializationExtension(Extension):
    def to_json(self, obj: Any) -> str:
        return f'{{"name": "{obj.name}"}}'


class ValidationExtension(Extension):
    def validate(self, obj: Any) -> bool:
        return hasattr(obj, 'name') and len(obj.name) > 0


class Extensible:
    def __init__(self):
        self._extensions: Dict[type, Extension] = {}
    
    def add_extension(self, extension_type: type, extension: Extension):
        self._extensions[extension_type] = extension
    
    def get_extension(self, extension_type: type) -> Extension:
        return self._extensions.get(extension_type)


class User(Extensible):
    def __init__(self, name: str):
        super().__init__()
        self.name = name


if __name__ == "__main__":
    print("=== Extension Object Pattern Demo ===\n")
    
    user = User("Alice")
    user.add_extension(SerializationExtension, SerializationExtension())
    user.add_extension(ValidationExtension, ValidationExtension())
    
    serializer = user.get_extension(SerializationExtension)
    validator = user.get_extension(ValidationExtension)
    
    print(f"JSON: {serializer.to_json(user)}")
    print(f"Valid: {validator.validate(user)}")
