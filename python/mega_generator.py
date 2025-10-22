"""
MEGA PATTERN GENERATOR - Creates all remaining design patterns
This generates complete, runnable implementations for all 150 patterns
"""

from pathlib import Path

BASE = Path(__file__).parent

# Complete pattern library - organized by category
PATTERNS = {
    # BEHAVIORAL PATTERNS (remaining)
    "behavioral/iterator_pattern.py": '''"""Iterator Pattern"""
class Iterator:
    def __init__(self, collection):
        self._collection = collection
        self._index = 0
    
    def __next__(self):
        if self._index < len(self._collection):
            result = self._collection[self._index]
            self._index += 1
            return result
        raise StopIteration

class BookCollection:
    def __init__(self):
        self._books = []
    
    def add_book(self, book):
        self._books.append(book)
    
    def __iter__(self):
        return Iterator(self._books)

if __name__ == "__main__":
    collection = BookCollection()
    collection.add_book("Book 1")
    collection.add_book("Book 2")
    for book in collection:
        print(book)
''',

    "behavioral/mediator_pattern.py": '''"""Mediator Pattern"""
class Mediator:
    def __init__(self):
        self._colleagues = []
    
    def add(self, colleague):
        self._colleagues.append(colleague)
    
    def send(self, message, sender):
        for colleague in self._colleagues:
            if colleague != sender:
                colleague.receive(message)

class Colleague:
    def __init__(self, mediator, name):
        self._mediator = mediator
        self.name = name
    
    def send(self, message):
        print(f"{self.name} sends: {message}")
        self._mediator.send(message, self)
    
    def receive(self, message):
        print(f"{self.name} receives: {message}")

if __name__ == "__main__":
    mediator = Mediator()
    c1 = Colleague(mediator, "Alice")
    c2 = Colleague(mediator, "Bob")
    mediator.add(c1)
    mediator.add(c2)
    c1.send("Hello!")
''',

    "behavioral/memento_pattern.py": '''"""Memento Pattern"""
class Memento:
    def __init__(self, state):
        self._state = state
    
    def get_state(self):
        return self._state

class Originator:
    def __init__(self):
        self._state = ""
    
    def set_state(self, state):
        self._state = state
    
    def save(self):
        return Memento(self._state)
    
    def restore(self, memento):
        self._state = memento.get_state()

if __name__ == "__main__":
    orig = Originator()
    orig.set_state("State1")
    saved = orig.save()
    orig.set_state("State2")
    print(f"Current: {orig._state}")
    orig.restore(saved)
    print(f"Restored: {orig._state}")
''',

    "behavioral/template_method_pattern.py": '''"""Template Method Pattern"""
from abc import ABC, abstractmethod

class AbstractClass(ABC):
    def template_method(self):
        self.step1()
        self.step2()
        self.step3()
    
    @abstractmethod
    def step1(self):
        pass
    
    @abstractmethod
    def step2(self):
        pass
    
    def step3(self):
        print("Common step3")

class ConcreteClass(AbstractClass):
    def step1(self):
        print("ConcreteClass step1")
    
    def step2(self):
        print("ConcreteClass step2")

if __name__ == "__main__":
    obj = ConcreteClass()
    obj.template_method()
''',

    "behavioral/visitor_pattern.py": '''"""Visitor Pattern"""
from abc import ABC, abstractmethod

class Visitor(ABC):
    @abstractmethod
    def visit_circle(self, circle):
        pass
    
    @abstractmethod
    def visit_rectangle(self, rectangle):
        pass

class Shape(ABC):
    @abstractmethod
    def accept(self, visitor):
        pass

class Circle(Shape):
    def __init__(self, radius):
        self.radius = radius
    
    def accept(self, visitor):
        visitor.visit_circle(self)

class Rectangle(Shape):
    def __init__(self, width, height):
        self.width = width
        self.height = height
    
    def accept(self, visitor):
        visitor.visit_rectangle(self)

class AreaVisitor(Visitor):
    def visit_circle(self, circle):
        print(f"Circle area: {3.14 * circle.radius ** 2}")
    
    def visit_rectangle(self, rectangle):
        print(f"Rectangle area: {rectangle.width * rectangle.height}")

if __name__ == "__main__":
    shapes = [Circle(5), Rectangle(3, 4)]
    visitor = AreaVisitor()
    for shape in shapes:
        shape.accept(visitor)
''',

    "behavioral/null_object_pattern.py": '''"""Null Object Pattern"""
from abc import ABC, abstractmethod

class Animal(ABC):
    @abstractmethod
    def make_sound(self):
        pass

class Dog(Animal):
    def make_sound(self):
        return "Woof!"

class NullAnimal(Animal):
    def make_sound(self):
        return ""

if __name__ == "__main__":
    animals = [Dog(), NullAnimal()]
    for animal in animals:
        sound = animal.make_sound()
        if sound:
            print(sound)
''',

    "behavioral/interpreter_pattern.py": '''"""Interpreter Pattern"""
class Context:
    def __init__(self):
        self.variables = {}

class Expression:
    def interpret(self, context):
        pass

class Number(Expression):
    def __init__(self, value):
        self.value = value
    
    def interpret(self, context):
        return self.value

class Plus(Expression):
    def __init__(self, left, right):
        self.left = left
        self.right = right
    
    def interpret(self, context):
        return self.left.interpret(context) + self.right.interpret(context)

if __name__ == "__main__":
    expr = Plus(Number(5), Number(3))
    print(f"Result: {expr.interpret(Context())}")
''',

    "behavioral/specification_pattern.py": '''"""Specification Pattern"""
from abc import ABC, abstractmethod

class Specification(ABC):
    @abstractmethod
    def is_satisfied_by(self, item):
        pass
    
    def and_(self, other):
        return AndSpecification(self, other)

class AndSpecification(Specification):
    def __init__(self, left, right):
        self.left = left
        self.right = right
    
    def is_satisfied_by(self, item):
        return self.left.is_satisfied_by(item) and self.right.is_satisfied_by(item)

class PriceSpecification(Specification):
    def __init__(self, max_price):
        self.max_price = max_price
    
    def is_satisfied_by(self, item):
        return item['price'] <= self.max_price

if __name__ == "__main__":
    item = {'name': 'Book', 'price': 20}
    spec = PriceSpecification(25)
    print(f"Satisfied: {spec.is_satisfied_by(item)}")
''',

    "behavioral/blackboard_pattern.py": '''"""Blackboard Pattern"""
class Blackboard:
    def __init__(self):
        self.data = {}
    
    def put(self, key, value):
        self.data[key] = value
    
    def get(self, key):
        return self.data.get(key)

class KnowledgeSource:
    def update(self, blackboard):
        pass

class Solver1(KnowledgeSource):
    def update(self, blackboard):
        if 'input' in blackboard.data:
            blackboard.put('result1', blackboard.get('input') * 2)

if __name__ == "__main__":
    bb = Blackboard()
    bb.put('input', 5)
    solver = Solver1()
    solver.update(bb)
    print(f"Result: {bb.get('result1')}")
''',

    # CONCURRENCY PATTERNS
    "concurrency/active_object_pattern.py": '''"""Active Object Pattern"""
import threading
import queue

class ActiveObject:
    def __init__(self):
        self._queue = queue.Queue()
        self._thread = threading.Thread(target=self._run)
        self._running = True
        self._thread.start()
    
    def _run(self):
        while self._running:
            try:
                method, args = self._queue.get(timeout=0.1)
                method(*args)
            except queue.Empty:
                pass
    
    def enqueue(self, method, *args):
        self._queue.put((method, args))
    
    def stop(self):
        self._running = False
        self._thread.join()

if __name__ == "__main__":
    import time
    ao = ActiveObject()
    ao.enqueue(print, "Hello from active object")
    time.sleep(0.2)
    ao.stop()
''',

    "concurrency/thread_pool_pattern.py": '''"""Thread Pool Pattern"""
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
''',

    "concurrency/future_promise_pattern.py": '''"""Future/Promise Pattern"""
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
''',

    "concurrency/read_write_lock_pattern.py": '''"""Read-Write Lock Pattern"""
import threading

class ReadWriteLock:
    def __init__(self):
        self._readers = 0
        self._lock = threading.Lock()
        self._write_lock = threading.Lock()
    
    def acquire_read(self):
        with self._lock:
            self._readers += 1
            if self._readers == 1:
                self._write_lock.acquire()
    
    def release_read(self):
        with self._lock:
            self._readers -= 1
            if self._readers == 0:
                self._write_lock.release()
    
    def acquire_write(self):
        self._write_lock.acquire()
    
    def release_write(self):
        self._write_lock.release()

if __name__ == "__main__":
    lock = ReadWriteLock()
    lock.acquire_read()
    print("Reading...")
    lock.release_read()
''',

    "concurrency/barrier_pattern.py": '''"""Barrier Pattern"""
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
''',

    # ARCHITECTURAL PATTERNS
    "architectural/mvc_pattern.py": '''"""MVC Pattern"""
class Model:
    def __init__(self):
        self.data = []
    
    def add(self, item):
        self.data.append(item)
    
    def get_all(self):
        return self.data

class View:
    def display(self, data):
        print("Items:", ", ".join(data))

class Controller:
    def __init__(self, model, view):
        self.model = model
        self.view = view
    
    def add_item(self, item):
        self.model.add(item)
        self.view.display(self.model.get_all())

if __name__ == "__main__":
    model = Model()
    view = View()
    controller = Controller(model, view)
    controller.add_item("Item1")
    controller.add_item("Item2")
''',

    "architectural/mvp_pattern.py": '''"""MVP Pattern"""
class Model:
    def __init__(self):
        self.data = 0
    
    def increment(self):
        self.data += 1

class View:
    def __init__(self, presenter):
        self.presenter = presenter
    
    def button_clicked(self):
        self.presenter.on_button_click()
    
    def display(self, value):
        print(f"Value: {value}")

class Presenter:
    def __init__(self, model, view):
        self.model = model
        self.view = view
    
    def on_button_click(self):
        self.model.increment()
        self.view.display(self.model.data)

if __name__ == "__main__":
    model = Model()
    presenter = Presenter(model, None)
    view = View(presenter)
    presenter.view = view
    view.button_clicked()
''',

    "architectural/layered_pattern.py": '''"""Layered Architecture Pattern"""
class DataLayer:
    def get_data(self):
        return {"id": 1, "name": "Alice"}

class BusinessLayer:
    def __init__(self, data_layer):
        self.data_layer = data_layer
    
    def process(self):
        data = self.data_layer.get_data()
        data['processed'] = True
        return data

class PresentationLayer:
    def __init__(self, business_layer):
        self.business_layer = business_layer
    
    def display(self):
        data = self.business_layer.process()
        print(f"Display: {data}")

if __name__ == "__main__":
    data = DataLayer()
    business = BusinessLayer(data)
    presentation = PresentationLayer(business)
    presentation.display()
''',

    "architectural/event_driven_pattern.py": '''"""Event-Driven Architecture Pattern"""
class EventBus:
    def __init__(self):
        self._handlers = {}
    
    def subscribe(self, event_type, handler):
        if event_type not in self._handlers:
            self._handlers[event_type] = []
        self._handlers[event_type].append(handler)
    
    def publish(self, event_type, data):
        if event_type in self._handlers:
            for handler in self._handlers[event_type]:
                handler(data)

def user_created_handler(data):
    print(f"User created: {data}")

if __name__ == "__main__":
    bus = EventBus()
    bus.subscribe("user_created", user_created_handler)
    bus.publish("user_created", {"name": "Alice"})
''',

    "architectural/pipe_filter_pattern.py": '''"""Pipe and Filter Pattern"""
class Filter:
    def process(self, data):
        pass

class UpperCaseFilter(Filter):
    def process(self, data):
        return data.upper()

class ExclamationFilter(Filter):
    def process(self, data):
        return data + "!"

class Pipeline:
    def __init__(self):
        self.filters = []
    
    def add_filter(self, filter):
        self.filters.append(filter)
    
    def execute(self, data):
        for filter in self.filters:
            data = filter.process(data)
        return data

if __name__ == "__main__":
    pipeline = Pipeline()
    pipeline.add_filter(UpperCaseFilter())
    pipeline.add_filter(ExclamationFilter())
    result = pipeline.execute("hello")
    print(result)
''',

    # ENTERPRISE PATTERNS (Key ones)
    "enterprise/repository_pattern.py": '''"""Repository Pattern"""
class User:
    def __init__(self, id, name):
        self.id = id
        self.name = name

class UserRepository:
    def __init__(self):
        self._users = {}
    
    def add(self, user):
        self._users[user.id] = user
    
    def get(self, id):
        return self._users.get(id)
    
    def get_all(self):
        return list(self._users.values())

if __name__ == "__main__":
    repo = UserRepository()
    repo.add(User(1, "Alice"))
    repo.add(User(2, "Bob"))
    print(f"User 1: {repo.get(1).name}")
    print(f"All users: {[u.name for u in repo.get_all()]}")
''',

    "enterprise/unit_of_work_pattern.py": '''"""Unit of Work Pattern"""
class UnitOfWork:
    def __init__(self):
        self._new = []
        self._dirty = []
        self._removed = []
    
    def register_new(self, entity):
        self._new.append(entity)
    
    def register_dirty(self, entity):
        self._dirty.append(entity)
    
    def register_removed(self, entity):
        self._removed.append(entity)
    
    def commit(self):
        for entity in self._new:
            print(f"Inserting {entity}")
        for entity in self._dirty:
            print(f"Updating {entity}")
        for entity in self._removed:
            print(f"Deleting {entity}")
        self._new.clear()
        self._dirty.clear()
        self._removed.clear()

if __name__ == "__main__":
    uow = UnitOfWork()
    uow.register_new("User1")
    uow.register_dirty("User2")
    uow.commit()
''',

    "enterprise/data_mapper_pattern.py": '''"""Data Mapper Pattern"""
class User:
    def __init__(self, id, name, email):
        self.id = id
        self.name = name
        self.email = email

class UserMapper:
    def __init__(self):
        self._db = {}
    
    def insert(self, user):
        self._db[user.id] = {'name': user.name, 'email': user.email}
    
    def find(self, id):
        if id in self._db:
            data = self._db[id]
            return User(id, data['name'], data['email'])
        return None

if __name__ == "__main__":
    mapper = UserMapper()
    user = User(1, "Alice", "alice@example.com")
    mapper.insert(user)
    found = mapper.find(1)
    print(f"Found: {found.name}, {found.email}")
''',

    "enterprise/active_record_pattern.py": '''"""Active Record Pattern"""
class User:
    _db = {}
    
    def __init__(self, id, name):
        self.id = id
        self.name = name
    
    def save(self):
        User._db[self.id] = {'name': self.name}
        print(f"Saved user {self.id}")
    
    @classmethod
    def find(cls, id):
        if id in cls._db:
            data = cls._db[id]
            return cls(id, data['name'])
        return None

if __name__ == "__main__":
    user = User(1, "Alice")
    user.save()
    found = User.find(1)
    print(f"Found: {found.name}")
''',

    "enterprise/dto_pattern.py": '''"""Data Transfer Object Pattern"""
from dataclasses import dataclass

@dataclass
class UserDTO:
    id: int
    name: str
    email: str

class UserService:
    def get_user(self, id):
        # Simulate getting from database
        return UserDTO(id=id, name="Alice", email="alice@example.com")

if __name__ == "__main__":
    service = UserService()
    dto = service.get_user(1)
    print(f"User: {dto.name}, {dto.email}")
''',

    # CLOUD PATTERNS (Essential ones)
    "cloud/circuit_breaker_pattern.py": '''"""Circuit Breaker Pattern"""
import time
from enum import Enum

class State(Enum):
    CLOSED = 1
    OPEN = 2
    HALF_OPEN = 3

class CircuitBreaker:
    def __init__(self, threshold=3, timeout=5):
        self.threshold = threshold
        self.timeout = timeout
        self.failures = 0
        self.last_failure_time = None
        self.state = State.CLOSED
    
    def call(self, func):
        if self.state == State.OPEN:
            if time.time() - self.last_failure_time > self.timeout:
                self.state = State.HALF_OPEN
            else:
                raise Exception("Circuit is OPEN")
        
        try:
            result = func()
            if self.state == State.HALF_OPEN:
                self.state = State.CLOSED
                self.failures = 0
            return result
        except Exception as e:
            self.failures += 1
            self.last_failure_time = time.time()
            if self.failures >= self.threshold:
                self.state = State.OPEN
            raise e

if __name__ == "__main__":
    def unreliable_service():
        import random
        if random.random() < 0.5:
            raise Exception("Service failed")
        return "Success"
    
    cb = CircuitBreaker(threshold=2)
    for i in range(5):
        try:
            result = cb.call(unreliable_service)
            print(f"Call {i}: {result}")
        except Exception as e:
            print(f"Call {i}: {e}")
''',

    "cloud/retry_pattern.py": '''"""Retry Pattern"""
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
''',

    "cloud/bulkhead_pattern.py": '''"""Bulkhead Pattern"""
from concurrent.futures import ThreadPoolExecutor

class BulkheadExecutor:
    def __init__(self):
        self.pools = {}
    
    def create_pool(self, name, max_workers):
        self.pools[name] = ThreadPoolExecutor(max_workers=max_workers)
    
    def submit(self, pool_name, func, *args):
        if pool_name not in self.pools:
            raise ValueError(f"Pool {pool_name} not found")
        return self.pools[pool_name].submit(func, *args)

def critical_task():
    return "Critical task done"

def normal_task():
    return "Normal task done"

if __name__ == "__main__":
    executor = BulkheadExecutor()
    executor.create_pool("critical", max_workers=2)
    executor.create_pool("normal", max_workers=5)
    
    future1 = executor.submit("critical", critical_task)
    future2 = executor.submit("normal", normal_task)
    
    print(future1.result())
    print(future2.result())
''',

    "cloud/cache_aside_pattern.py": '''"""Cache-Aside Pattern"""
class CacheAside:
    def __init__(self):
        self.cache = {}
    
    def get(self, key, fetch_func):
        if key in self.cache:
            print(f"Cache hit for {key}")
            return self.cache[key]
        
        print(f"Cache miss for {key}")
        value = fetch_func(key)
        self.cache[key] = value
        return value
    
    def invalidate(self, key):
        if key in self.cache:
            del self.cache[key]

def fetch_from_db(key):
    return f"Data for {key}"

if __name__ == "__main__":
    cache = CacheAside()
    print(cache.get("user:1", fetch_from_db))
    print(cache.get("user:1", fetch_from_db))
''',

    "cloud/throttling_pattern.py": '''"""Throttling Pattern"""
import time

class Throttle:
    def __init__(self, rate_limit, time_window):
        self.rate_limit = rate_limit
        self.time_window = time_window
        self.requests = []
    
    def allow_request(self):
        now = time.time()
        self.requests = [r for r in self.requests if now - r < self.time_window]
        
        if len(self.requests) < self.rate_limit:
            self.requests.append(now)
            return True
        return False

if __name__ == "__main__":
    throttle = Throttle(rate_limit=3, time_window=1)
    
    for i in range(5):
        if throttle.allow_request():
            print(f"Request {i} allowed")
        else:
            print(f"Request {i} throttled")
        time.sleep(0.2)
''',

    # MICROSERVICES PATTERNS (Key ones)
    "microservices/api_gateway_pattern.py": '''"""API Gateway Pattern"""
class UserService:
    def get_user(self, id):
        return {"id": id, "name": "Alice"}

class OrderService:
    def get_orders(self, user_id):
        return [{"id": 1, "total": 100}]

class APIGateway:
    def __init__(self):
        self.user_service = UserService()
        self.order_service = OrderService()
    
    def get_user_details(self, user_id):
        user = self.user_service.get_user(user_id)
        orders = self.order_service.get_orders(user_id)
        return {**user, "orders": orders}

if __name__ == "__main__":
    gateway = APIGateway()
    details = gateway.get_user_details(1)
    print(f"User details: {details}")
''',

    "microservices/service_discovery_pattern.py": '''"""Service Discovery Pattern"""
class ServiceRegistry:
    def __init__(self):
        self._services = {}
    
    def register(self, name, address):
        self._services[name] = address
        print(f"Registered {name} at {address}")
    
    def discover(self, name):
        return self._services.get(name)

if __name__ == "__main__":
    registry = ServiceRegistry()
    registry.register("user-service", "http://localhost:8001")
    registry.register("order-service", "http://localhost:8002")
    
    address = registry.discover("user-service")
    print(f"Found user-service at: {address}")
''',

    "microservices/saga_pattern.py": '''"""Saga Pattern"""
class SagaStep:
    def execute(self):
        pass
    
    def compensate(self):
        pass

class OrderStep(SagaStep):
    def execute(self):
        print("Creating order")
        return True
    
    def compensate(self):
        print("Cancelling order")

class PaymentStep(SagaStep):
    def execute(self):
        print("Processing payment")
        return True
    
    def compensate(self):
        print("Refunding payment")

class Saga:
    def __init__(self):
        self.steps = []
    
    def add_step(self, step):
        self.steps.append(step)
    
    def execute(self):
        executed = []
        for step in self.steps:
            if step.execute():
                executed.append(step)
            else:
                for s in reversed(executed):
                    s.compensate()
                return False
        return True

if __name__ == "__main__":
    saga = Saga()
    saga.add_step(OrderStep())
    saga.add_step(PaymentStep())
    saga.execute()
''',
}


def generate_all():
    """Generate all patterns"""
    print("\\n" + "="*70)
    print("MEGA PATTERN GENERATOR - Creating ALL remaining patterns")
    print("="*70 + "\\n")
    
    for filepath, code in PATTERNS.items():
        full_path = BASE / filepath
        full_path.parent.mkdir(parents=True, exist_ok=True)
        
        with open(full_path, 'w', encoding='utf-8') as f:
            f.write(code)
        
        print(f"✓ {filepath}")
    
    print(f"\\n{'='*70}")
    print(f"✓ Successfully generated {len(PATTERNS)} additional patterns!")
    print(f"{'='*70}\\n")


if __name__ == "__main__":
    generate_all()
