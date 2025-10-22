"""Hexagonal Architecture (Ports and Adapters)"""
from abc import ABC, abstractmethod

# Port (interface)
class Repository(ABC):
    @abstractmethod
    def save(self, data):
        pass

# Domain (core logic)
class Service:
    def __init__(self, repository: Repository):
        self.repository = repository
    
    def process(self, data):
        print(f"Processing {data}")
        self.repository.save(data)

# Adapter (implementation)
class DatabaseRepository(Repository):
    def save(self, data):
        print(f"Saving to database: {data}")

class FileRepository(Repository):
    def save(self, data):
        print(f"Saving to file: {data}")

if __name__ == "__main__":
    service = Service(DatabaseRepository())
    service.process("Data1")
    
    service = Service(FileRepository())
    service.process("Data2")
