"""Monitor Object Pattern - Synchronizes concurrent method execution"""
import threading

class MonitorObject:
    def __init__(self):
        self._lock = threading.Lock()
        self._value = 0
    
    def increment(self):
        with self._lock:
            self._value += 1
            print(f"Incremented to {self._value}")
    
    def get_value(self):
        with self._lock:
            return self._value

if __name__ == "__main__":
    monitor = MonitorObject()
    threads = [threading.Thread(target=monitor.increment) for _ in range(5)]
    for t in threads: t.start()
    for t in threads: t.join()
    print(f"Final value: {monitor.get_value()}")
