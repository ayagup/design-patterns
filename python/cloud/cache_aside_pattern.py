"""Cache-Aside Pattern"""
class CacheAside:
    def __init__(self):
        self.cache = {}
    
    def get(self, key, fetch_func):
        if key in self.cache:
            print(f"Cache hit for {key}")
            return self.cache[key]
        
        print(f"Cache miss for {key}")
        value = fetch_func(key)
        self.cache[key] = value
        return value
    
    def invalidate(self, key):
        if key in self.cache:
            del self.cache[key]

def fetch_from_db(key):
    return f"Data for {key}"

if __name__ == "__main__":
    cache = CacheAside()
    print(cache.get("user:1", fetch_from_db))
    print(cache.get("user:1", fetch_from_db))
