"""Revealing Module Pattern - Encapsulates private data with public API"""
def create_counter():
    """Factory function that returns a counter with encapsulated state"""
    
    # Private state
    _count = 0
    _history = []
    
    # Private methods
    def _log(action):
        _history.append(action)
    
    # Public API
    def increment():
        nonlocal _count
        _count += 1
        _log(f"increment: {_count}")
        return _count
    
    def decrement():
        nonlocal _count
        _count -= 1
        _log(f"decrement: {_count}")
        return _count
    
    def get_count():
        return _count
    
    def get_history():
        return _history.copy()
    
    # Reveal only public methods
    return {
        "increment": increment,
        "decrement": decrement,
        "get_count": get_count,
        "get_history": get_history
    }

if __name__ == "__main__":
    counter = create_counter()
    
    counter["increment"]()
    counter["increment"]()
    counter["decrement"]()
    
    print(f"Count: {counter['get_count']()}")
    print(f"History: {counter['get_history']()}")
    
    # Private state is not accessible
    # counter._count would not exist
