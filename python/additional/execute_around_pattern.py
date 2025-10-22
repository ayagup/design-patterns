"""
Execute Around Pattern

Intent: Wraps code execution with setup and teardown logic.
Used for resource management, transactions, logging.
"""

from abc import ABC, abstractmethod
from contextlib import contextmanager
from typing import Callable, Any
import time


class ExecuteAround(ABC):
    """Base class for execute around pattern"""
    
    @abstractmethod
    def setup(self):
        """Setup before execution"""
        pass
    
    @abstractmethod
    def teardown(self):
        """Teardown after execution"""
        pass
    
    def execute(self, operation: Callable):
        """Execute operation with setup and teardown"""
        self.setup()
        try:
            return operation()
        finally:
            self.teardown()


class DatabaseConnection(ExecuteAround):
    """Manages database connection lifecycle"""
    
    def __init__(self, db_name: str):
        self.db_name = db_name
        self.connection = None
    
    def setup(self):
        """Open database connection"""
        print(f"Opening connection to {self.db_name}")
        self.connection = f"Connection({self.db_name})"
    
    def teardown(self):
        """Close database connection"""
        print(f"Closing connection to {self.db_name}")
        self.connection = None


class TimedExecution(ExecuteAround):
    """Measures execution time"""
    
    def __init__(self, operation_name: str):
        self.operation_name = operation_name
        self.start_time = None
    
    def setup(self):
        """Start timer"""
        print(f"Starting timer for: {self.operation_name}")
        self.start_time = time.time()
    
    def teardown(self):
        """Stop timer and report"""
        elapsed = time.time() - self.start_time
        print(f"Completed {self.operation_name} in {elapsed:.4f}s")


class Transaction(ExecuteAround):
    """Manages transaction with commit/rollback"""
    
    def __init__(self, name: str):
        self.name = name
        self.active = False
    
    def setup(self):
        """Begin transaction"""
        print(f"BEGIN TRANSACTION: {self.name}")
        self.active = True
    
    def teardown(self):
        """Commit or rollback transaction"""
        if self.active:
            print(f"COMMIT TRANSACTION: {self.name}")
            self.active = False


class ResourceLock(ExecuteAround):
    """Acquires and releases a lock"""
    
    def __init__(self, resource_name: str):
        self.resource_name = resource_name
        self.locked = False
    
    def setup(self):
        """Acquire lock"""
        print(f"Acquiring lock on: {self.resource_name}")
        self.locked = True
    
    def teardown(self):
        """Release lock"""
        print(f"Releasing lock on: {self.resource_name}")
        self.locked = False


# Context manager version (Pythonic way)
@contextmanager
def execute_around_cm(setup_fn: Callable, teardown_fn: Callable):
    """Context manager for execute around pattern"""
    setup_fn()
    try:
        yield
    finally:
        teardown_fn()


class FileHandler:
    """Handles file operations with execute around"""
    
    def __init__(self, filename: str):
        self.filename = filename
        self.file = None
    
    @contextmanager
    def open_file(self, mode: str = 'r'):
        """Context manager for file operations"""
        print(f"Opening file: {self.filename}")
        self.file = open(self.filename, mode)
        try:
            yield self.file
        finally:
            print(f"Closing file: {self.filename}")
            self.file.close()


class LoggingContext:
    """Provides logging context with automatic cleanup"""
    
    def __init__(self, context_name: str):
        self.context_name = context_name
    
    def __enter__(self):
        print(f"[ENTER] {self.context_name}")
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type:
            print(f"[EXIT WITH ERROR] {self.context_name}: {exc_val}")
        else:
            print(f"[EXIT] {self.context_name}")
        return False


def with_transaction(operation: Callable, transaction_name: str = "default"):
    """Decorator for transactional operations"""
    def wrapper(*args, **kwargs):
        print(f"BEGIN TRANSACTION: {transaction_name}")
        try:
            result = operation(*args, **kwargs)
            print(f"COMMIT TRANSACTION: {transaction_name}")
            return result
        except Exception as e:
            print(f"ROLLBACK TRANSACTION: {transaction_name} - Error: {e}")
            raise
    return wrapper


if __name__ == "__main__":
    print("=== Execute Around Pattern Demo ===\n")
    
    # 1. Database connection
    print("1. Database Connection:")
    db = DatabaseConnection("mydb")
    result = db.execute(lambda: print("  Executing query..."))
    print()
    
    # 2. Timed execution
    print("2. Timed Execution:")
    timer = TimedExecution("data_processing")
    timer.execute(lambda: time.sleep(0.1))
    print()
    
    # 3. Transaction
    print("3. Transaction:")
    txn = Transaction("update_records")
    txn.execute(lambda: print("  Updating records..."))
    print()
    
    # 4. Resource lock
    print("4. Resource Lock:")
    lock = ResourceLock("shared_resource")
    lock.execute(lambda: print("  Accessing resource..."))
    print()
    
    # 5. Context manager version
    print("5. Context Manager Version:")
    with execute_around_cm(
        lambda: print("  Setup resources"),
        lambda: print("  Cleanup resources")
    ):
        print("  Doing work...")
    print()
    
    # 6. Logging context
    print("6. Logging Context:")
    with LoggingContext("api_call"):
        print("  Making API call...")
    print()
    
    # 7. Nested contexts
    print("7. Nested Execute Around:")
    with LoggingContext("outer"):
        with LoggingContext("inner"):
            print("  Nested operation")
    
    print("\n✓ Execute Around pattern manages resource lifecycle!")
