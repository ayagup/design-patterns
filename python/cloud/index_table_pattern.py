"""Index Table - Create indexes for efficient queries"""
class IndexTable:
    def __init__(self):
        self.primary_data = {}
        self.indexes = {}
    
    def create_index(self, index_name, key_extractor):
        self.indexes[index_name] = {
            "key_extractor": key_extractor,
            "index": {}
        }
    
    def insert(self, id, data):
        self.primary_data[id] = data
        
        # Update all indexes
        for index_name, index_info in self.indexes.items():
            key = index_info["key_extractor"](data)
            if key not in index_info["index"]:
                index_info["index"][key] = []
            index_info["index"][key].append(id)
    
    def query_by_index(self, index_name, key):
        if index_name not in self.indexes:
            return []
        
        ids = self.indexes[index_name]["index"].get(key, [])
        return [self.primary_data[id] for id in ids]
    
    def query_by_id(self, id):
        return self.primary_data.get(id)

if __name__ == "__main__":
    # Create table with indexes
    users = IndexTable()
    
    # Create indexes
    users.create_index("by_email", lambda user: user['email'])
    users.create_index("by_city", lambda user: user['city'])
    
    # Insert data
    users.insert(1, {"name": "Alice", "email": "alice@example.com", "city": "NYC"})
    users.insert(2, {"name": "Bob", "email": "bob@example.com", "city": "LA"})
    users.insert(3, {"name": "Charlie", "email": "charlie@example.com", "city": "NYC"})
    
    # Query by index
    print("Users in NYC:")
    print(users.query_by_index("by_city", "NYC"))
    
    print("\nUser by email:")
    print(users.query_by_index("by_email", "bob@example.com"))
