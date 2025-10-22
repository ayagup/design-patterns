"""
Abstract Factory Pattern
Purpose: Provides an interface for creating families of related or dependent objects
Use Case: Cross-platform UI toolkits, database abstraction layers
"""

from abc import ABC, abstractmethod
from typing import List


# Example 1: UI Component Factory
class Button(ABC):
    @abstractmethod
    def render(self) -> str:
        pass


class Checkbox(ABC):
    @abstractmethod
    def render(self) -> str:
        pass


class TextField(ABC):
    @abstractmethod
    def render(self) -> str:
        pass


# Windows family
class WindowsButton(Button):
    def render(self) -> str:
        return "Rendering Windows-style button"


class WindowsCheckbox(Checkbox):
    def render(self) -> str:
        return "Rendering Windows-style checkbox"


class WindowsTextField(TextField):
    def render(self) -> str:
        return "Rendering Windows-style text field"


# Mac family
class MacButton(Button):
    def render(self) -> str:
        return "Rendering Mac-style button"


class MacCheckbox(Checkbox):
    def render(self) -> str:
        return "Rendering Mac-style checkbox"


class MacTextField(TextField):
    def render(self) -> str:
        return "Rendering Mac-style text field"


# Linux family
class LinuxButton(Button):
    def render(self) -> str:
        return "Rendering Linux-style button"


class LinuxCheckbox(Checkbox):
    def render(self) -> str:
        return "Rendering Linux-style checkbox"


class LinuxTextField(TextField):
    def render(self) -> str:
        return "Rendering Linux-style text field"


class GUIFactory(ABC):
    """Abstract factory"""
    @abstractmethod
    def create_button(self) -> Button:
        pass
    
    @abstractmethod
    def create_checkbox(self) -> Checkbox:
        pass
    
    @abstractmethod
    def create_text_field(self) -> TextField:
        pass


class WindowsFactory(GUIFactory):
    def create_button(self) -> Button:
        return WindowsButton()
    
    def create_checkbox(self) -> Checkbox:
        return WindowsCheckbox()
    
    def create_text_field(self) -> TextField:
        return WindowsTextField()


class MacFactory(GUIFactory):
    def create_button(self) -> Button:
        return MacButton()
    
    def create_checkbox(self) -> Checkbox:
        return MacCheckbox()
    
    def create_text_field(self) -> TextField:
        return MacTextField()


class LinuxFactory(GUIFactory):
    def create_button(self) -> Button:
        return LinuxButton()
    
    def create_checkbox(self) -> Checkbox:
        return MacCheckbox()
    
    def create_text_field(self) -> TextField:
        return LinuxTextField()


# Example 2: Database Factory
class Connection(ABC):
    @abstractmethod
    def connect(self) -> str:
        pass


class Command(ABC):
    @abstractmethod
    def execute(self, query: str) -> str:
        pass


class Transaction(ABC):
    @abstractmethod
    def begin(self) -> str:
        pass
    
    @abstractmethod
    def commit(self) -> str:
        pass


# MySQL family
class MySQLConnection(Connection):
    def connect(self) -> str:
        return "Connected to MySQL database"


class MySQLCommand(Command):
    def execute(self, query: str) -> str:
        return f"Executing MySQL query: {query}"


class MySQLTransaction(Transaction):
    def begin(self) -> str:
        return "Beginning MySQL transaction"
    
    def commit(self) -> str:
        return "Committing MySQL transaction"


# PostgreSQL family
class PostgreSQLConnection(Connection):
    def connect(self) -> str:
        return "Connected to PostgreSQL database"


class PostgreSQLCommand(Command):
    def execute(self, query: str) -> str:
        return f"Executing PostgreSQL query: {query}"


class PostgreSQLTransaction(Transaction):
    def begin(self) -> str:
        return "Beginning PostgreSQL transaction"
    
    def commit(self) -> str:
        return "Committing PostgreSQL transaction"


class DatabaseFactory(ABC):
    @abstractmethod
    def create_connection(self) -> Connection:
        pass
    
    @abstractmethod
    def create_command(self) -> Command:
        pass
    
    @abstractmethod
    def create_transaction(self) -> Transaction:
        pass


class MySQLFactory(DatabaseFactory):
    def create_connection(self) -> Connection:
        return MySQLConnection()
    
    def create_command(self) -> Command:
        return MySQLCommand()
    
    def create_transaction(self) -> Transaction:
        return MySQLTransaction()


class PostgreSQLFactory(DatabaseFactory):
    def create_connection(self) -> Connection:
        return PostgreSQLConnection()
    
    def create_command(self) -> Command:
        return PostgreSQLCommand()
    
    def create_transaction(self) -> Transaction:
        return PostgreSQLTransaction()


# Example 3: Furniture Factory
class Chair(ABC):
    @abstractmethod
    def sit_on(self) -> str:
        pass


class Sofa(ABC):
    @abstractmethod
    def lie_on(self) -> str:
        pass


class Table(ABC):
    @abstractmethod
    def put_on(self) -> str:
        pass


# Modern furniture
class ModernChair(Chair):
    def sit_on(self) -> str:
        return "Sitting on a modern chair"


class ModernSofa(Sofa):
    def lie_on(self) -> str:
        return "Lying on a modern sofa"


class ModernTable(Table):
    def put_on(self) -> str:
        return "Putting things on a modern table"


# Victorian furniture
class VictorianChair(Chair):
    def sit_on(self) -> str:
        return "Sitting on a Victorian chair"


class VictorianSofa(Sofa):
    def lie_on(self) -> str:
        return "Lying on a Victorian sofa"


class VictorianTable(Table):
    def put_on(self) -> str:
        return "Putting things on a Victorian table"


class FurnitureFactory(ABC):
    @abstractmethod
    def create_chair(self) -> Chair:
        pass
    
    @abstractmethod
    def create_sofa(self) -> Sofa:
        pass
    
    @abstractmethod
    def create_table(self) -> Table:
        pass


class ModernFurnitureFactory(FurnitureFactory):
    def create_chair(self) -> Chair:
        return ModernChair()
    
    def create_sofa(self) -> Sofa:
        return ModernSofa()
    
    def create_table(self) -> Table:
        return ModernTable()


class VictorianFurnitureFactory(FurnitureFactory):
    def create_chair(self) -> Chair:
        return VictorianChair()
    
    def create_sofa(self) -> Sofa:
        return VictorianSofa()
    
    def create_table(self) -> Table:
        return VictorianTable()


# Client code
class Application:
    def __init__(self, factory: GUIFactory):
        self.factory = factory
    
    def create_ui(self) -> List[str]:
        button = self.factory.create_button()
        checkbox = self.factory.create_checkbox()
        text_field = self.factory.create_text_field()
        
        return [
            button.render(),
            checkbox.render(),
            text_field.render()
        ]


def demonstrate_abstract_factory():
    """Demonstrate abstract factory pattern"""
    print("=== Abstract Factory Pattern Demo ===\n")
    
    # Example 1: UI Components
    print("--- Example 1: Cross-Platform UI ---")
    for factory_class, platform in [(WindowsFactory, "Windows"), 
                                     (MacFactory, "Mac"),
                                     (LinuxFactory, "Linux")]:
        factory = factory_class()
        app = Application(factory)
        print(f"\n{platform} UI:")
        for component in app.create_ui():
            print(f"  {component}")
    
    # Example 2: Database Operations
    print("\n--- Example 2: Database Operations ---")
    for db_factory, db_name in [(MySQLFactory(), "MySQL"),
                                  (PostgreSQLFactory(), "PostgreSQL")]:
        print(f"\n{db_name}:")
        conn = db_factory.create_connection()
        cmd = db_factory.create_command()
        txn = db_factory.create_transaction()
        
        print(f"  {conn.connect()}")
        print(f"  {txn.begin()}")
        print(f"  {cmd.execute('SELECT * FROM users')}")
        print(f"  {txn.commit()}")
    
    # Example 3: Furniture Store
    print("\n--- Example 3: Furniture Store ---")
    for furniture_factory, style in [(ModernFurnitureFactory(), "Modern"),
                                      (VictorianFurnitureFactory(), "Victorian")]:
        print(f"\n{style} Furniture:")
        chair = furniture_factory.create_chair()
        sofa = furniture_factory.create_sofa()
        table = furniture_factory.create_table()
        
        print(f"  {chair.sit_on()}")
        print(f"  {sofa.lie_on()}")
        print(f"  {table.put_on()}")
    
    print("\n=== Key Concepts ===")
    print("1. Abstract Factory - Interface for creating product families")
    print("2. Concrete Factories - Implement creation of specific products")
    print("3. Product Families - Related products that work together")
    print("4. Product Variants - Different implementations of same product")
    
    print("\n=== Benefits ===")
    print("+ Ensures product compatibility")
    print("+ Isolates concrete classes")
    print("+ Easy to exchange product families")
    print("+ Promotes consistency among products")
    
    print("\n=== When to Use ===")
    print("• System needs to be independent of product creation")
    print("• System configured with multiple product families")
    print("• Product family designed to be used together")
    print("• Want to reveal only interfaces, not implementations")


if __name__ == "__main__":
    demonstrate_abstract_factory()
