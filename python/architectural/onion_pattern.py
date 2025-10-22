"""Onion Architecture - Core at center"""
from abc import ABC, abstractmethod

# Core (innermost)
class Entity:
    def __init__(self, id, name):
        self.id = id
        self.name = name

# Service Layer
class IRepository(ABC):
    @abstractmethod
    def add(self, entity):
        pass

class DomainService:
    def __init__(self, repository: IRepository):
        self.repository = repository
    
    def create_entity(self, name):
        entity = Entity(None, name)
        self.repository.add(entity)
        return entity

# Infrastructure (outermost)
class Repository(IRepository):
    def __init__(self):
        self.entities = []
    
    def add(self, entity):
        entity.id = len(self.entities) + 1
        self.entities.append(entity)

if __name__ == "__main__":
    repo = Repository()
    service = DomainService(repo)
    entity = service.create_entity("Test")
    print(f"Created: {entity.name}")
