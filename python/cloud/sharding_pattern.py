"""Sharding - Horizontal partitioning"""
class Shard:
    def __init__(self, id):
        self.id = id
        self.data = {}
    
    def store(self, key, value):
        self.data[key] = value
    
    def retrieve(self, key):
        return self.data.get(key)

class ShardingStrategy:
    def get_shard(self, key, num_shards):
        return hash(key) % num_shards

class ShardedDataStore:
    def __init__(self, num_shards):
        self.shards = [Shard(i) for i in range(num_shards)]
        self.strategy = ShardingStrategy()
    
    def store(self, key, value):
        shard_id = self.strategy.get_shard(key, len(self.shards))
        self.shards[shard_id].store(key, value)
        print(f"Stored '{key}' in shard {shard_id}")
    
    def retrieve(self, key):
        shard_id = self.strategy.get_shard(key, len(self.shards))
        return self.shards[shard_id].retrieve(key)

if __name__ == "__main__":
    store = ShardedDataStore(3)
    store.store("user:1", {"name": "Alice"})
    store.store("user:2", {"name": "Bob"})
    store.store("user:3", {"name": "Charlie"})
    
    print(f"Retrieved: {store.retrieve('user:1')}")
