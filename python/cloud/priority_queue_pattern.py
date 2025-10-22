"""Priority Queue - Prioritizes messages"""
import heapq

class PriorityQueue:
    def __init__(self):
        self._queue = []
        self._counter = 0
    
    def enqueue(self, item, priority):
        heapq.heappush(self._queue, (priority, self._counter, item))
        self._counter += 1
    
    def dequeue(self):
        if self._queue:
            return heapq.heappop(self._queue)[2]
        return None

if __name__ == "__main__":
    pq = PriorityQueue()
    pq.enqueue("Low priority task", 3)
    pq.enqueue("High priority task", 1)
    pq.enqueue("Medium priority task", 2)
    
    while True:
        task = pq.dequeue()
        if task is None:
            break
        print(f"Processing: {task}")
