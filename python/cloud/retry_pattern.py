"""Retry Pattern"""
import time

def retry(max_attempts=3, delay=1):
    def decorator(func):
        def wrapper(*args, **kwargs):
            for attempt in range(max_attempts):
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    if attempt < max_attempts - 1:
                        print(f"Attempt {attempt + 1} failed, retrying...")
                        time.sleep(delay)
                    else:
                        raise e
        return wrapper
    return decorator

@retry(max_attempts=3, delay=0.1)
def unreliable_function():
    import random
    if random.random() < 0.7:
        raise Exception("Failed")
    return "Success"

if __name__ == "__main__":
    try:
        result = unreliable_function()
        print(f"Result: {result}")
    except Exception as e:
        print(f"All attempts failed: {e}")
