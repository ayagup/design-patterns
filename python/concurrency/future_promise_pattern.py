"""Future/Promise Pattern"""
from concurrent.futures import Future, ThreadPoolExecutor
import time

def compute(n):
    time.sleep(0.1)
    return n * n

if __name__ == "__main__":
    executor = ThreadPoolExecutor()
    future = executor.submit(compute, 5)
    print("Computing...")
    result = future.result()
    print(f"Result: {result}")
    executor.shutdown()
