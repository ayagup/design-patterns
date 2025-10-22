"""Lock Pattern - Mutual exclusion"""
import threading

class Resource:
    def __init__(self):
        self._lock = threading.Lock()
        self._data = []
    
    def add(self, item):
        with self._lock:
            self._data.append(item)
            print(f"Added {item}")

if __name__ == "__main__":
    resource = Resource()
    threads = [threading.Thread(target=resource.add, args=(i,)) for i in range(5)]
    for t in threads: t.start()
    for t in threads: t.join()
