"""Anti-Corruption Layer - Isolates subsystems"""
class LegacySystem:
    def get_customer_data(self, id):
        return {"cust_id": id, "cust_name": "John", "cust_addr": "123 Main St"}

class AntiCorruptionLayer:
    def __init__(self, legacy):
        self.legacy = legacy
    
    def get_customer(self, id):
        legacy_data = self.legacy.get_customer_data(id)
        # Translate to modern format
        return {
            "id": legacy_data['cust_id'],
            "name": legacy_data['cust_name'],
            "address": legacy_data['cust_addr']
        }

if __name__ == "__main__":
    legacy = LegacySystem()
    acl = AntiCorruptionLayer(legacy)
    customer = acl.get_customer(1)
    print(f"Modern format: {customer}")
