"""Competing Consumers - Multiple consumers process messages"""
import threading
import queue
import time

class MessageQueue:
    def __init__(self):
        self.queue = queue.Queue()
    
    def send(self, message):
        self.queue.put(message)
    
    def receive(self):
        return self.queue.get()

class Consumer:
    def __init__(self, id, message_queue):
        self.id = id
        self.queue = message_queue
    
    def start(self):
        threading.Thread(target=self._consume, daemon=True).start()
    
    def _consume(self):
        while True:
            try:
                message = self.queue.receive()
                print(f"Consumer {self.id} processing: {message}")
                time.sleep(0.1)
            except:
                break

if __name__ == "__main__":
    mq = MessageQueue()
    
    # Start consumers
    for i in range(3):
        Consumer(i, mq).start()
    
    # Send messages
    for i in range(10):
        mq.send(f"Message {i}")
    
    time.sleep(2)
