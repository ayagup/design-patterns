"""Thread-Specific Storage - Per-thread data"""
import threading

thread_local = threading.local()

def worker(name):
    thread_local.name = name
    print(f"Thread {thread_local.name} running")

if __name__ == "__main__":
    threads = [threading.Thread(target=worker, args=(f"T{i}",)) for i in range(3)]
    for t in threads: t.start()
    for t in threads: t.join()
