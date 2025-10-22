"""
Builder Pattern
Purpose: Separates the construction of a complex object from its representation
Use Case: Constructing complex objects with many optional parameters
"""

from typing import Optional, List
from dataclasses import dataclass, field


# Example 1: Computer Builder
@dataclass
class Computer:
    cpu: str = ""
    ram: str = ""
    storage: str = ""
    gpu: Optional[str] = None
    motherboard: str = ""
    power_supply: str = ""
    case: str = ""
    cooling: str = "Standard"
    
    def __str__(self) -> str:
        specs = [
            f"CPU: {self.cpu}",
            f"RAM: {self.ram}",
            f"Storage: {self.storage}",
            f"Motherboard: {self.motherboard}",
            f"Power Supply: {self.power_supply}",
            f"Case: {self.case}",
            f"Cooling: {self.cooling}"
        ]
        if self.gpu:
            specs.insert(3, f"GPU: {self.gpu}")
        return "\n".join(specs)


class ComputerBuilder:
    def __init__(self):
        self.computer = Computer()
    
    def set_cpu(self, cpu: str) -> 'ComputerBuilder':
        self.computer.cpu = cpu
        return self
    
    def set_ram(self, ram: str) -> 'ComputerBuilder':
        self.computer.ram = ram
        return self
    
    def set_storage(self, storage: str) -> 'ComputerBuilder':
        self.computer.storage = storage
        return self
    
    def set_gpu(self, gpu: str) -> 'ComputerBuilder':
        self.computer.gpu = gpu
        return self
    
    def set_motherboard(self, motherboard: str) -> 'ComputerBuilder':
        self.computer.motherboard = motherboard
        return self
    
    def set_power_supply(self, psu: str) -> 'ComputerBuilder':
        self.computer.power_supply = psu
        return self
    
    def set_case(self, case: str) -> 'ComputerBuilder':
        self.computer.case = case
        return self
    
    def set_cooling(self, cooling: str) -> 'ComputerBuilder':
        self.computer.cooling = cooling
        return self
    
    def build(self) -> Computer:
        return self.computer


# Example 2: HTTP Request Builder
@dataclass
class HTTPRequest:
    method: str = "GET"
    url: str = ""
    headers: dict = field(default_factory=dict)
    params: dict = field(default_factory=dict)
    body: Optional[str] = None
    timeout: int = 30
    
    def execute(self) -> str:
        result = f"{self.method} {self.url}"
        if self.params:
            param_str = "&".join(f"{k}={v}" for k, v in self.params.items())
            result += f"?{param_str}"
        if self.headers:
            result += f"\nHeaders: {self.headers}"
        if self.body:
            result += f"\nBody: {self.body}"
        result += f"\nTimeout: {self.timeout}s"
        return result


class HTTPRequestBuilder:
    def __init__(self):
        self.request = HTTPRequest()
    
    def method(self, method: str) -> 'HTTPRequestBuilder':
        self.request.method = method
        return self
    
    def url(self, url: str) -> 'HTTPRequestBuilder':
        self.request.url = url
        return self
    
    def header(self, key: str, value: str) -> 'HTTPRequestBuilder':
        self.request.headers[key] = value
        return self
    
    def param(self, key: str, value: str) -> 'HTTPRequestBuilder':
        self.request.params[key] = value
        return self
    
    def body(self, body: str) -> 'HTTPRequestBuilder':
        self.request.body = body
        return self
    
    def timeout(self, seconds: int) -> 'HTTPRequestBuilder':
        self.request.timeout = seconds
        return self
    
    def build(self) -> HTTPRequest:
        return self.request


# Example 3: SQL Query Builder
class SQLQuery:
    def __init__(self):
        self.select_columns: List[str] = []
        self.from_table: str = ""
        self.where_conditions: List[str] = []
        self.order_by_columns: List[str] = []
        self.limit_value: Optional[int] = None
        self.offset_value: Optional[int] = None
    
    def to_sql(self) -> str:
        if not self.from_table:
            raise ValueError("Table name is required")
        
        columns = ", ".join(self.select_columns) if self.select_columns else "*"
        sql = f"SELECT {columns} FROM {self.from_table}"
        
        if self.where_conditions:
            sql += f" WHERE {' AND '.join(self.where_conditions)}"
        
        if self.order_by_columns:
            sql += f" ORDER BY {', '.join(self.order_by_columns)}"
        
        if self.limit_value:
            sql += f" LIMIT {self.limit_value}"
        
        if self.offset_value:
            sql += f" OFFSET {self.offset_value}"
        
        return sql


class SQLQueryBuilder:
    def __init__(self):
        self.query = SQLQuery()
    
    def select(self, *columns: str) -> 'SQLQueryBuilder':
        self.query.select_columns.extend(columns)
        return self
    
    def from_table(self, table: str) -> 'SQLQueryBuilder':
        self.query.from_table = table
        return self
    
    def where(self, condition: str) -> 'SQLQueryBuilder':
        self.query.where_conditions.append(condition)
        return self
    
    def order_by(self, *columns: str) -> 'SQLQueryBuilder':
        self.query.order_by_columns.extend(columns)
        return self
    
    def limit(self, limit: int) -> 'SQLQueryBuilder':
        self.query.limit_value = limit
        return self
    
    def offset(self, offset: int) -> 'SQLQueryBuilder':
        self.query.offset_value = offset
        return self
    
    def build(self) -> SQLQuery:
        return self.query


# Example 4: Pizza Builder with Director
@dataclass
class Pizza:
    size: str = ""
    crust: str = ""
    sauce: str = ""
    cheese: str = ""
    toppings: List[str] = field(default_factory=list)
    
    def __str__(self) -> str:
        result = f"{self.size} Pizza with {self.crust} crust"
        result += f"\nSauce: {self.sauce}"
        result += f"\nCheese: {self.cheese}"
        if self.toppings:
            result += f"\nToppings: {', '.join(self.toppings)}"
        return result


class PizzaBuilder:
    def __init__(self):
        self.pizza = Pizza()
    
    def set_size(self, size: str) -> 'PizzaBuilder':
        self.pizza.size = size
        return self
    
    def set_crust(self, crust: str) -> 'PizzaBuilder':
        self.pizza.crust = crust
        return self
    
    def set_sauce(self, sauce: str) -> 'PizzaBuilder':
        self.pizza.sauce = sauce
        return self
    
    def set_cheese(self, cheese: str) -> 'PizzaBuilder':
        self.pizza.cheese = cheese
        return self
    
    def add_topping(self, topping: str) -> 'PizzaBuilder':
        self.pizza.toppings.append(topping)
        return self
    
    def build(self) -> Pizza:
        return self.pizza


class PizzaDirector:
    """Director that knows how to build specific pizza types"""
    @staticmethod
    def make_margherita(builder: PizzaBuilder) -> Pizza:
        return (builder
                .set_size("Medium")
                .set_crust("Thin")
                .set_sauce("Tomato")
                .set_cheese("Mozzarella")
                .add_topping("Basil")
                .build())
    
    @staticmethod
    def make_pepperoni(builder: PizzaBuilder) -> Pizza:
        return (builder
                .set_size("Large")
                .set_crust("Regular")
                .set_sauce("Tomato")
                .set_cheese("Mozzarella")
                .add_topping("Pepperoni")
                .add_topping("Oregano")
                .build())
    
    @staticmethod
    def make_veggie(builder: PizzaBuilder) -> Pizza:
        return (builder
                .set_size("Medium")
                .set_crust("Whole Wheat")
                .set_sauce("Pesto")
                .set_cheese("Feta")
                .add_topping("Bell Peppers")
                .add_topping("Onions")
                .add_topping("Mushrooms")
                .add_topping("Olives")
                .build())


def demonstrate_builder():
    """Demonstrate builder pattern"""
    print("=== Builder Pattern Demo ===\n")
    
    # Example 1: Computer Builder
    print("--- Example 1: Gaming Computer ---")
    gaming_pc = (ComputerBuilder()
                 .set_cpu("Intel i9-13900K")
                 .set_ram("32GB DDR5")
                 .set_storage("2TB NVMe SSD")
                 .set_gpu("NVIDIA RTX 4090")
                 .set_motherboard("ASUS ROG")
                 .set_power_supply("1000W")
                 .set_case("NZXT H710")
                 .set_cooling("Liquid Cooling")
                 .build())
    print(gaming_pc)
    
    print("\n--- Office Computer ---")
    office_pc = (ComputerBuilder()
                 .set_cpu("Intel i5-13400")
                 .set_ram("16GB DDR4")
                 .set_storage("512GB SSD")
                 .set_motherboard("MSI B660")
                 .set_power_supply("500W")
                 .set_case("Fractal Design")
                 .build())
    print(office_pc)
    
    # Example 2: HTTP Request Builder
    print("\n--- Example 2: HTTP Request ---")
    request = (HTTPRequestBuilder()
               .method("POST")
               .url("https://api.example.com/users")
               .header("Content-Type", "application/json")
               .header("Authorization", "Bearer token123")
               .body('{"name": "John", "email": "john@example.com"}')
               .timeout(60)
               .build())
    print(request.execute())
    
    # Example 3: SQL Query Builder
    print("\n--- Example 3: SQL Query ---")
    query = (SQLQueryBuilder()
             .select("id", "name", "email")
             .from_table("users")
             .where("age > 18")
             .where("active = true")
             .order_by("name", "id")
             .limit(10)
             .offset(20)
             .build())
    print(query.to_sql())
    
    # Example 4: Pizza with Director
    print("\n--- Example 4: Pizza Orders ---")
    director = PizzaDirector()
    
    margherita = director.make_margherita(PizzaBuilder())
    print("Margherita Pizza:")
    print(margherita)
    
    print("\nPepperoni Pizza:")
    pepperoni = director.make_pepperoni(PizzaBuilder())
    print(pepperoni)
    
    print("\nVeggie Pizza:")
    veggie = director.make_veggie(PizzaBuilder())
    print(veggie)
    
    print("\n=== Key Concepts ===")
    print("1. Builder - Constructs complex objects step by step")
    print("2. Fluent Interface - Method chaining for readability")
    print("3. Director - Knows how to build specific configurations")
    print("4. Product - Complex object being built")
    
    print("\n=== Benefits ===")
    print("+ Constructs objects step by step")
    print("+ Reuse same construction code for different representations")
    print("+ Isolates complex construction code")
    print("+ Single Responsibility Principle")
    
    print("\n=== When to Use ===")
    print("• Object has many optional parameters")
    print("• Want to avoid telescoping constructors")
    print("• Need to create different representations")
    print("• Construction process must allow different representations")


if __name__ == "__main__":
    demonstrate_builder()
