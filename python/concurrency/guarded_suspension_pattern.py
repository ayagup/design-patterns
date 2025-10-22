"""Guarded Suspension - Waits for condition"""
import threading
import time

class GuardedQueue:
    def __init__(self):
        self._queue = []
        self._lock = threading.Lock()
        self._condition = threading.Condition(self._lock)
    
    def put(self, item):
        with self._condition:
            self._queue.append(item)
            self._condition.notify()
    
    def get(self):
        with self._condition:
            while not self._queue:
                self._condition.wait()
            return self._queue.pop(0)

if __name__ == "__main__":
    queue = GuardedQueue()
    threading.Thread(target=lambda: (time.sleep(0.1), queue.put("item"))).start()
    print(f"Got: {queue.get()}")
