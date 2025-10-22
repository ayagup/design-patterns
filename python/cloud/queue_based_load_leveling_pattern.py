"""Queue-Based Load Leveling - Smooths load with queue"""
import queue
import threading
import time

class LoadLevelingQueue:
    def __init__(self, max_workers=2):
        self.queue = queue.Queue()
        self.workers = []
        for i in range(max_workers):
            t = threading.Thread(target=self._worker, daemon=True)
            t.start()
            self.workers.append(t)
    
    def submit(self, task):
        self.queue.put(task)
        print(f"Task submitted: {task}")
    
    def _worker(self):
        while True:
            task = self.queue.get()
            print(f"Processing: {task}")
            time.sleep(0.5)  # Simulate work
            self.queue.task_done()

if __name__ == "__main__":
    leveler = LoadLevelingQueue()
    
    # Submit burst of tasks
    for i in range(10):
        leveler.submit(f"Task{i}")
    
    leveler.queue.join()
    print("All tasks completed")
