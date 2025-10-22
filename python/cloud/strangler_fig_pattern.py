"""Strangler Fig - Gradually replace legacy system"""
class LegacySystem:
    def process_order(self, order):
        return f"Legacy: Processed {order}"

class NewSystem:
    def process_order(self, order):
        return f"New: Processed {order}"

class StranglerFacade:
    def __init__(self):
        self.legacy = LegacySystem()
        self.new_system = NewSystem()
        self.migrated_features = set()
    
    def migrate_feature(self, feature):
        self.migrated_features.add(feature)
    
    def process_order(self, order):
        if "feature1" in self.migrated_features:
            return self.new_system.process_order(order)
        return self.legacy.process_order(order)

if __name__ == "__main__":
    facade = StranglerFacade()
    
    print(facade.process_order("Order1"))  # Legacy
    
    facade.migrate_feature("feature1")
    print(facade.process_order("Order2"))  # New system
