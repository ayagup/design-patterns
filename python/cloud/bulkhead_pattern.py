"""Bulkhead Pattern"""
from concurrent.futures import ThreadPoolExecutor

class BulkheadExecutor:
    def __init__(self):
        self.pools = {}
    
    def create_pool(self, name, max_workers):
        self.pools[name] = ThreadPoolExecutor(max_workers=max_workers)
    
    def submit(self, pool_name, func, *args):
        if pool_name not in self.pools:
            raise ValueError(f"Pool {pool_name} not found")
        return self.pools[pool_name].submit(func, *args)

def critical_task():
    return "Critical task done"

def normal_task():
    return "Normal task done"

if __name__ == "__main__":
    executor = BulkheadExecutor()
    executor.create_pool("critical", max_workers=2)
    executor.create_pool("normal", max_workers=5)
    
    future1 = executor.submit("critical", critical_task)
    future2 = executor.submit("normal", normal_task)
    
    print(future1.result())
    print(future2.result())
