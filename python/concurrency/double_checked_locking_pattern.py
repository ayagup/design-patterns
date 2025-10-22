"""Double-Checked Locking - Lazy initialization with threading"""
import threading

class Singleton:
    _instance = None
    _lock = threading.Lock()
    
    @classmethod
    def get_instance(cls):
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = cls()
        return cls._instance

if __name__ == "__main__":
    s1 = Singleton.get_instance()
    s2 = Singleton.get_instance()
    print(f"Same instance: {s1 is s2}")
