"""
Dependency Injection Pattern
Purpose: Provides objects with their dependencies rather than having them construct dependencies
Use Case: Inversion of Control (IoC), testability, loose coupling
"""

from abc import ABC, abstractmethod
from typing import Dict, Callable, Any


# Example 1: Constructor Injection
class Database(ABC):
    @abstractmethod
    def query(self, sql: str) -> str:
        pass


class MySQLDatabase(Database):
    def query(self, sql: str) -> str:
        return f"MySQL: {sql}"


class PostgreSQLDatabase(Database):
    def query(self, sql: str) -> str:
        return f"PostgreSQL: {sql}"


class UserRepository:
    """Constructor injection"""
    def __init__(self, database: Database):
        self.database = database
    
    def find_user(self, user_id: int) -> str:
        return self.database.query(f"SELECT * FROM users WHERE id = {user_id}")


# Example 2: Setter Injection
class Logger(ABC):
    @abstractmethod
    def log(self, message: str):
        pass


class FileLogger(Logger):
    def log(self, message: str):
        print(f"[FILE] {message}")


class ConsoleLogger(Logger):
    def log(self, message: str):
        print(f"[CONSOLE] {message}")


class UserService:
    """Setter injection"""
    def __init__(self):
        self._logger: Logger = ConsoleLogger()  # Default
    
    def set_logger(self, logger: Logger):
        self._logger = logger
    
    def create_user(self, name: str):
        self._logger.log(f"Creating user: {name}")
        return f"User {name} created"


# Example 3: DI Container
class DIContainer:
    def __init__(self):
        self._services: Dict[str, Callable] = {}
        self._singletons: Dict[str, Any] = {}
    
    def register(self, name: str, factory: Callable, singleton: bool = False):
        self._services[name] = (factory, singleton)
    
    def resolve(self, name: str) -> Any:
        if name not in self._services:
            raise ValueError(f"Service '{name}' not registered")
        
        factory, singleton = self._services[name]
        
        if singleton:
            if name not in self._singletons:
                self._singletons[name] = factory(self)
            return self._singletons[name]
        
        return factory(self)


if __name__ == "__main__":
    print("=== Dependency Injection Pattern Demo ===\n")
    
    # Constructor Injection
    print("--- Constructor Injection ---")
    mysql_repo = UserRepository(MySQLDatabase())
    postgres_repo = UserRepository(PostgreSQLDatabase())
    print(mysql_repo.find_user(1))
    print(postgres_repo.find_user(1))
    
    # Setter Injection
    print("\n--- Setter Injection ---")
    service = UserService()
    print(service.create_user("Alice"))
    service.set_logger(FileLogger())
    print(service.create_user("Bob"))
    
    # DI Container
    print("\n--- DI Container ---")
    container = DIContainer()
    container.register("database", lambda c: MySQLDatabase(), singleton=True)
    container.register("logger", lambda c: ConsoleLogger(), singleton=True)
    container.register("user_repo", lambda c: UserRepository(c.resolve("database")))
    
    repo = container.resolve("user_repo")
    print(repo.find_user(42))
