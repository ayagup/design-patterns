"""Thread Pool Pattern"""
from concurrent.futures import ThreadPoolExecutor
import time

def worker(n):
    print(f"Worker {n} starting")
    time.sleep(0.1)
    print(f"Worker {n} done")
    return n * 2

if __name__ == "__main__":
    with ThreadPoolExecutor(max_workers=3) as executor:
        futures = [executor.submit(worker, i) for i in range(5)]
        for future in futures:
            print(f"Result: {future.result()}")
