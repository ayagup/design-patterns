"""Barrier Pattern"""
import threading

def worker(barrier, worker_id):
    print(f"Worker {worker_id} working")
    barrier.wait()
    print(f"Worker {worker_id} passed barrier")

if __name__ == "__main__":
    barrier = threading.Barrier(3)
    threads = [threading.Thread(target=worker, args=(barrier, i)) for i in range(3)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()
