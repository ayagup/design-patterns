"""Active Object Pattern"""
import threading
import queue

class ActiveObject:
    def __init__(self):
        self._queue = queue.Queue()
        self._thread = threading.Thread(target=self._run)
        self._running = True
        self._thread.start()
    
    def _run(self):
        while self._running:
            try:
                method, args = self._queue.get(timeout=0.1)
                method(*args)
            except queue.Empty:
                pass
    
    def enqueue(self, method, *args):
        self._queue.put((method, args))
    
    def stop(self):
        self._running = False
        self._thread.join()

if __name__ == "__main__":
    import time
    ao = ActiveObject()
    ao.enqueue(print, "Hello from active object")
    time.sleep(0.2)
    ao.stop()
