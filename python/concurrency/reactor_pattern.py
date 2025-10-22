"""Reactor Pattern - Event-driven demultiplexing"""
import selectors
import socket

class Reactor:
    def __init__(self):
        self.selector = selectors.DefaultSelector()
    
    def register(self, sock, callback):
        self.selector.register(sock, selectors.EVENT_READ, callback)
    
    def run(self):
        while True:
            events = self.selector.select(timeout=1)
            for key, mask in events:
                callback = key.data
                callback(key.fileobj)

if __name__ == "__main__":
    print("Reactor Pattern - Event loop example")
