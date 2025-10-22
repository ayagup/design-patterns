"""Actor Model - Message-passing concurrency"""
import threading
import queue

class Actor:
    def __init__(self):
        self._mailbox = queue.Queue()
        self._thread = threading.Thread(target=self._run)
        self._running = True
        self._thread.start()
    
    def send(self, message):
        self._mailbox.put(message)
    
    def _run(self):
        while self._running:
            try:
                message = self._mailbox.get(timeout=0.1)
                self.receive(message)
            except queue.Empty:
                pass
    
    def receive(self, message):
        print(f"Received: {message}")
    
    def stop(self):
        self._running = False
        self._thread.join()

if __name__ == "__main__":
    import time
    actor = Actor()
    actor.send("Hello")
    actor.send("World")
    time.sleep(0.2)
    actor.stop()
