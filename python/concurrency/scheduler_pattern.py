"""Scheduler Pattern - Controls thread execution order"""
import heapq
import time
from typing import Callable, Tuple

class Scheduler:
    def __init__(self):
        self._queue = []
    
    def schedule(self, delay: float, task: Callable):
        heapq.heappush(self._queue, (time.time() + delay, task))
    
    def run(self):
        while self._queue:
            run_time, task = heapq.heappop(self._queue)
            wait_time = run_time - time.time()
            if wait_time > 0:
                time.sleep(wait_time)
            task()

if __name__ == "__main__":
    scheduler = Scheduler()
    scheduler.schedule(0.1, lambda: print("Task 1"))
    scheduler.schedule(0.05, lambda: print("Task 2"))
    scheduler.run()
