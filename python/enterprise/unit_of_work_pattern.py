"""Unit of Work Pattern"""
class UnitOfWork:
    def __init__(self):
        self._new = []
        self._dirty = []
        self._removed = []
    
    def register_new(self, entity):
        self._new.append(entity)
    
    def register_dirty(self, entity):
        self._dirty.append(entity)
    
    def register_removed(self, entity):
        self._removed.append(entity)
    
    def commit(self):
        for entity in self._new:
            print(f"Inserting {entity}")
        for entity in self._dirty:
            print(f"Updating {entity}")
        for entity in self._removed:
            print(f"Deleting {entity}")
        self._new.clear()
        self._dirty.clear()
        self._removed.clear()

if __name__ == "__main__":
    uow = UnitOfWork()
    uow.register_new("User1")
    uow.register_dirty("User2")
    uow.commit()
