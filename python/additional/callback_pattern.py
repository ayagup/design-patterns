"""
Callback Pattern

Intent: Allows passing functions as parameters to be called later.
Used in asynchronous programming, event handling, customization.
"""

from typing import Callable, Any, List
from dataclasses import dataclass
import time


# Simple callback example
def process_data(data: List[int], callback: Callable[[int], int]) -> List[int]:
    """Process data using a callback function"""
    return [callback(item) for item in data]


# Callback with context
@dataclass
class CallbackContext:
    """Context information passed to callbacks"""
    operation: str
    timestamp: float
    data: Any


class EventEmitter:
    """Emits events and calls registered callbacks"""
    
    def __init__(self):
        self._callbacks: dict[str, List[Callable]] = {}
    
    def on(self, event: str, callback: Callable):
        """Register a callback for an event"""
        if event not in self._callbacks:
            self._callbacks[event] = []
        self._callbacks[event].append(callback)
    
    def emit(self, event: str, *args, **kwargs):
        """Emit an event and call all registered callbacks"""
        if event in self._callbacks:
            for callback in self._callbacks[event]:
                callback(*args, **kwargs)
    
    def off(self, event: str, callback: Callable = None):
        """Unregister callback(s) for an event"""
        if event in self._callbacks:
            if callback:
                self._callbacks[event].remove(callback)
            else:
                del self._callbacks[event]


class AsyncTask:
    """Simulates async task with completion callbacks"""
    
    def __init__(self, task_name: str):
        self.task_name = task_name
        self.on_success: Callable = None
        self.on_error: Callable = None
        self.on_complete: Callable = None
    
    def then(self, callback: Callable):
        """Set success callback"""
        self.on_success = callback
        return self
    
    def catch(self, callback: Callable):
        """Set error callback"""
        self.on_error = callback
        return self
    
    def finally_do(self, callback: Callable):
        """Set completion callback (always called)"""
        self.on_complete = callback
        return self
    
    def execute(self, should_fail: bool = False):
        """Execute the task"""
        try:
            print(f"Executing task: {self.task_name}")
            time.sleep(0.1)  # Simulate work
            
            if should_fail:
                raise Exception(f"Task {self.task_name} failed")
            
            result = f"Result of {self.task_name}"
            if self.on_success:
                self.on_success(result)
            
            return result
        except Exception as e:
            if self.on_error:
                self.on_error(e)
            raise
        finally:
            if self.on_complete:
                self.on_complete()


class DataProcessor:
    """Processes data with progress callbacks"""
    
    def process(self, 
                items: List[Any],
                on_item: Callable[[Any], None] = None,
                on_progress: Callable[[int, int], None] = None,
                on_complete: Callable[[List[Any]], None] = None):
        """Process items with callbacks"""
        results = []
        total = len(items)
        
        for i, item in enumerate(items):
            # Process item
            processed = item * 2  # Simple processing
            results.append(processed)
            
            # Item callback
            if on_item:
                on_item(processed)
            
            # Progress callback
            if on_progress:
                on_progress(i + 1, total)
        
        # Completion callback
        if on_complete:
            on_complete(results)
        
        return results


if __name__ == "__main__":
    print("=== Callback Pattern Demo ===\n")
    
    # 1. Simple callback
    print("1. Simple Callback:")
    data = [1, 2, 3, 4, 5]
    doubled = process_data(data, lambda x: x * 2)
    squared = process_data(data, lambda x: x ** 2)
    print(f"Doubled: {doubled}")
    print(f"Squared: {squared}\n")
    
    # 2. Event emitter with callbacks
    print("2. Event Emitter:")
    emitter = EventEmitter()
    
    emitter.on('data', lambda d: print(f"  Handler 1 received: {d}"))
    emitter.on('data', lambda d: print(f"  Handler 2 received: {d}"))
    emitter.on('error', lambda e: print(f"  Error handler: {e}"))
    
    emitter.emit('data', {'value': 42})
    emitter.emit('error', 'Something went wrong')
    print()
    
    # 3. Async task with callbacks
    print("3. Async Task with Callbacks:")
    task = AsyncTask("fetch_data")
    task.then(lambda r: print(f"  Success: {r}")) \
        .catch(lambda e: print(f"  Error: {e}")) \
        .finally_do(lambda: print("  Task completed"))
    
    task.execute()
    print()
    
    # 4. Data processing with progress
    print("4. Data Processing with Progress:")
    processor = DataProcessor()
    
    processor.process(
        items=[1, 2, 3, 4, 5],
        on_item=lambda item: print(f"  Processed item: {item}"),
        on_progress=lambda current, total: print(f"  Progress: {current}/{total}"),
        on_complete=lambda results: print(f"  All done! Results: {results}")
    )
    
    print("\n✓ Callback pattern enables flexible async behavior!")
