# Design Patterns in Java

A comprehensive collection of **150+ design patterns** implemented in Java with practical, runnable examples.

## 📚 Table of Contents

### 1. Creational Patterns (9/9) ✅
Design patterns that deal with object creation mechanisms.

- **Singleton** - Ensures a class has only | Category | Complete | Total | Progress |
|----------|----------|-------|----------|
| Creational | 9 | 9 | ████████████ 100% |
| Structural | 9 | 9 | ████████████ 100% |
| Behavioral | 13 | 13 | ████████████ 100% |
| Cloud | 30 | 30 | ████████████ 100% |
| Concurrency | 15 | 15 | ████████████ 100% |
| Enterprise | 19 | 19 | ████████████ 100% |
| Additional | 20 | 20 | ████████████ 100% |
| Architectural | 15 | 15 | ████████████ 100% |
| Microservices | 18 | 18 | ████████████ 100% |
| **TOTAL** | **150** | **150** | ████████████ **100%** |
- **Factory Method** - Creates objects without specifying exact classes
- **Abstract Factory** - Creates families of related objects
- **Builder** - Constructs complex objects step by step
- **Prototype** - Creates objects by cloning existing ones
- **Object Pool** - Reuses expensive-to-create objects
- **Lazy Initialization** - Delays object creation until needed
- **Dependency Injection** - Injects dependencies from external sources
- **Multiton** - Manages named instances of a class

### 2. Structural Patterns (9/9) ✅
Design patterns that deal with object composition and relationships.

- **Adapter** - Converts interface of a class into another
- **Bridge** - Separates abstraction from implementation
- **Composite** - Composes objects into tree structures
- **Decorator** - Adds behavior to objects dynamically
- **Facade** - Provides simplified interface to complex subsystems
- **Flyweight** - Shares common state among multiple objects
- **Proxy** - Provides surrogate or placeholder for another object
- **Private Class Data** - Restricts access to class attributes
- **Extension Object** - Adds functionality to objects dynamically

### 3. Behavioral Patterns (13/13) ✅
Design patterns that deal with object collaboration and responsibility.

- **Chain of Responsibility** - Passes request along chain of handlers
- **Command** - Encapsulates requests as objects
- **Interpreter** - Implements specialized language or expression evaluator
- **Iterator** - Traverses elements of collection without exposing representation
- **Mediator** - Defines simplified communication between classes
- **Memento** - Captures and restores object state
- **Observer** - Notifies multiple objects about state changes
- **State** - Alters object behavior when internal state changes
- **Strategy** - Defines family of interchangeable algorithms
- **Template Method** - Defines skeleton of algorithm in base class
- **Visitor** - Separates algorithms from objects they operate on
- **Null Object** - Provides default behavior for absent objects
- **Specification** - Recombines business logic in boolean fashion
- **Blackboard** - Multiple subsystems collaborate on shared knowledge

### 4. Concurrency Patterns (15/15) ✅
Design patterns for multi-threaded programming.

- **Thread Pool** ✅ - Manages pool of worker threads
- **Future/Promise** ✅ - Represents value available in future
- **Actor Model** ✅ - Message-passing between independent actors
- **Double-Checked Locking** ✅ - Reduces overhead of acquiring locks
- **Read-Write Lock** ✅ - Allows concurrent read access
- **Monitor Object** ✅ - Thread-safe object synchronization with condition variables
- **Active Object** ✅ - Decouples method execution from invocation
- **Balking** ✅ - Executes action only in appropriate state
- **Barrier** ✅ - Waits for multiple threads to reach sync point
- **Guarded Suspension** ✅ - Waits until safe to proceed
- **Reactor** ✅ - Event-driven I/O with event loop
- **Proactor** ✅ - Asynchronous I/O with completion handlers
- **Thread-Specific Storage** ✅ - Per-thread data using ThreadLocal
- **Half-Sync/Half-Async** ✅ - Separates sync and async processing layers
- **Leader/Followers** ✅ - Thread pool with leader election for events

### 5. Architectural Patterns (15/15) ✅
High-level patterns for system architecture.

- **MVC (Model-View-Controller)** ✅ - Separates data, presentation, and control
- **MVVM (Model-View-ViewModel)** ✅ - Data binding oriented with observable properties
- **Event-Driven Architecture** ✅ - Components communicate through events
- **CQRS** ✅ - Command Query Responsibility Segregation
- **Event Sourcing** ✅ - Stores state as sequence of events
- **Layered Architecture** ✅ - Organizes system in layers
- **MVP (Model-View-Presenter)** ✅ - Variant of MVC with passive view
- **Hexagonal (Ports and Adapters)** ✅ - Isolates core logic
- **Clean Architecture** ✅ - Dependency rule enforcement
- **Onion Architecture** ✅ - Concentric dependency layers
- **Pipe and Filter** ✅ - Sequential data processing
- **Microkernel** ✅ - Plugin-based architecture
- **Service-Oriented Architecture** ✅ - Loosely coupled services
- **Space-Based Architecture** ✅ - Distributed in-memory data
- **Broker Pattern** ✅ - Intermediary for service communication

### 6. Enterprise Patterns (19/19) ✅
Patterns for enterprise application development.

- **Repository** ✅ - Encapsulates data access logic
- **Unit of Work** ✅ - Maintains list of objects affected by transaction
- **DTO (Data Transfer Object)** ✅ - Transfers data between layers
- **Service Layer** ✅ - Defines application boundary and business operations
- **Data Mapper** ✅ - Separates domain objects from database
- **Active Record** ✅ - Objects handle their own persistence
- **Table Data Gateway** ✅ - Gateway to database table
- **Row Data Gateway** ✅ - Gateway to single database row
- **Domain Model** ✅ - Rich objects with business logic and behavior
- **Transaction Script** ✅ - Procedural business logic organization
- **Table Module** ✅ - Single instance handles all table rows
- **Identity Map** ✅ - Ensures objects loaded only once per session
- **Lazy Load** ✅ - Defers object loading until needed
- **Front Controller** ✅ - Centralized request handling entry point
- **Application Controller** ✅ - Handles application flow and navigation
- **Page Controller** ✅ - Per-page request handling
- **Data Gateway** ✅ - Encapsulates database access
- **Template View** ✅ - Renders info into HTML with templates
- **Value Object** ✅ - Immutable object compared by value

### 7. Cloud Patterns (30/30) ✅
Patterns for cloud-native applications.

- **Circuit Breaker** ✅ - Prevents calls to failing services
- **Retry** ✅ - Handles transient failures with retry logic
- **Bulkhead** ✅ - Isolates elements to prevent cascade failures
- **Cache-Aside** ✅ - Load data on demand into cache
- **Ambassador** ✅ - Helper services alongside main service
- **Anti-Corruption Layer** ✅ - Facade between modern and legacy
- **Compensating Transaction** ✅ - Undoes failed operations
- **Competing Consumers** ✅ - Multiple concurrent consumers
- **Backend for Frontend (BFF)** ✅ - Separate backend per UI
- **External Configuration Store** ✅ - Externalized config
- **Gateway Aggregation** ✅ - Aggregates multiple requests
- **Gateway Offloading** ✅ - Offloads functionality to gateway
- **Gateway Routing** ✅ - Routes requests to multiple services
- **Health Endpoint Monitoring** ✅ - Exposes health endpoints
- **Leader Election** ✅ - Elects coordinator instance
- **Materialized View** ✅ - Pre-computed views of data
- **Compute Resource Consolidation** ✅ - Consolidates tasks
- **Pipes and Filters** ✅ - Processing pipeline pattern
- **Priority Queue** ✅ - Prioritizes requests by importance
- **Queue-Based Load Leveling** ✅ - Smooths load with queues
- **Scheduler Agent Supervisor** ✅ - Coordinates distributed tasks
- **Sharding** ✅ - Horizontal data partitioning
- **Sidecar** ✅ - Helper container alongside app
- **Static Content Hosting** ✅ - CDN and static asset delivery
- **Strangler Fig** ✅ - Incremental legacy migration
- **Throttling** ✅ - Rate limiting and traffic control
- **Valet Key** ✅ - Restricted direct access tokens
- **Federated Identity** ✅ - External identity providers
- **Gatekeeper** ✅ - Security gateway validation
- **Index Table** ✅ - Secondary indexes for queries
- Scheduler Agent Supervisor - Coordinates distributed actions
- Sharding - Divides data store into partitions
- Sidecar - Co-located helper components
- Static Content Hosting - Static content in cloud storage
- Strangler Fig - Gradually replaces legacy system
- Throttling - Controls resource consumption
- Valet Key - Restricted direct access token

### 8. Microservices Patterns (18/18) ✅
Patterns specific to microservices architecture.

- **API Gateway** ✅ - Single entry point for clients
- **Service Discovery** ✅ - Automatic detection of service instances with health checking
- **Saga** ✅ - Distributed transaction management with compensating transactions
- **Database per Service** ✅ - Each service owns its database
- **Shared Database** ✅ - Multiple services share database
- **API Composition** ✅ - Composes data from multiple services
- **Aggregator Microservice** ✅ - Invokes multiple services
- **Chained Microservice** ✅ - Services call each other
- **Branch Microservice** ✅ - Parallel service invocation
- **Asynchronous Messaging** ✅ - Event-driven communication
- **Transactional Outbox** ✅ - Reliably publish events
- **Distributed Tracing** ✅ - Traces requests across services
- **Log Aggregation** ✅ - Centralizes logs from all services
- **Application Metrics** ✅ - Monitors application health
- **Audit Logging** ✅ - Records user activities
- **Exception Tracking** ✅ - Centralized exception handling
- **Service Mesh** ✅ - Infrastructure layer for service communication
- **Externalized Configuration** ✅ - Config outside service
- Consumer-Driven Contract - Consumer defines contract

### 8. Additional Patterns 20/20 ✅
Other useful design patterns.

- **Plugin** ✅ - Adds features via plugins with dynamic loading
- **Callback** ✅ - Function passed as parameter to be called back later
- **Specification** ✅ - Encapsulates business rules as composable objects
- **Value Object** ✅ - Immutable object compared by value
- **Interceptor** ✅ - Intercepts and modifies requests/responses
- **Service Locator** ✅ - Registry for obtaining services
- **Registry** ✅ - Well-known object for finding services
- **Money** ✅ - Represents monetary values
- **Special Case** ✅ - Subclass for special cases (Null Object)
- **Service Stub** ✅ - Testing substitutes for services
- **Module Pattern** ✅ - Encapsulates code into modules
- **Revealing Module** ✅ - Exposes public API from module
- **Mixin** ✅ - Adds functionality to classes without inheritance
- **Twin** ✅ - Allows modeling same entity in two systems
- **Marker Interface** ✅ - Empty interface to mark classes
- **Execute Around** ✅ - Surrounds operation with setup/cleanup
- **Type Tunnel** ✅ - Preserves type information through generics
- **CRTP** ✅ - Static polymorphism via self-referencing generics
- **Pimpl** ✅ - Hides implementation behind pointer/reference
- **RAII** ✅ - Binds resource lifecycle to object lifetime
- Scope Guard - Executes action on scope exit
- Policy-Based Design - Behavior via template parameters

## 🚀 Getting Started

### Prerequisites
- Java JDK 8 or higher
- No external dependencies required

### Running the Examples

Each pattern is self-contained in its own file with a `main` method:

```bash
# Compile
javac java/creational/SingletonPattern.java

# Run
java creational.SingletonPattern
```

### Project Structure

```
design-patterns/
├── java/
│   ├── creational/          # Creational patterns
│   ├── structural/          # Structural patterns
│   ├── behavioral/          # Behavioral patterns
│   ├── concurrency/         # Concurrency patterns
│   ├── architectural/       # Architectural patterns
│   ├── enterprise/          # Enterprise patterns
│   ├── cloud/              # Cloud patterns
│   ├── microservices/      # Microservices patterns
│   └── additional/         # Additional patterns
├── python/                 # (Future: Python implementations)
└── README.md
```

## 📖 Pattern Categories Explained

### **Creational Patterns**
Focus on object creation mechanisms, providing flexibility in what, how, when, and who creates objects. They help make a system independent of how its objects are created, composed, and represented.

### **Structural Patterns**
Deal with object composition and typically identify simple ways to realize relationships between different objects. They help ensure that when one part of a system changes, the entire structure doesn't need to change.

### **Behavioral Patterns**
Concerned with algorithms and the assignment of responsibilities between objects. They describe patterns of communication between objects and focus on how objects collaborate.

### **Concurrency Patterns**
Address multi-threading, parallel processing, and concurrent execution challenges. Essential for building responsive, scalable applications.

### **Architectural Patterns**
Define the overall structure and organization of software systems at a high level. They guide the entire application architecture.

### **Enterprise Patterns**
Tackle common problems in enterprise application development, especially around data access, business logic, and presentation layers.

### **Cloud Patterns**
Address challenges specific to cloud computing: scalability, resilience, resource management, and distributed systems.

### **Microservices Patterns**
Solve problems specific to microservices architecture: service communication, data management, deployment, and observability.

## 💡 How to Use This Repository

1. **Learning**: Study patterns in order (Creational → Structural → Behavioral)
2. **Reference**: Use as quick reference when solving design problems
3. **Practice**: Modify examples to match your use cases
4. **Interview Prep**: Review implementations before technical interviews

## 🎯 Pattern Selection Guide

### When to use which pattern?

**Single object creation**: Singleton, Factory Method  
**Multiple related objects**: Abstract Factory, Builder  
**Add behavior dynamically**: Decorator, Strategy  
**Simplify complex interface**: Facade, Adapter  
**Manage object relationships**: Mediator, Observer  
**Handle state changes**: State, Strategy  
**Process collections**: Iterator, Visitor  
**Async operations**: Future/Promise, Async  
**Concurrent access**: Thread Pool, Monitor Object  

## 🏗️ Code Quality

- ✅ All examples are **runnable** and **self-contained**
- ✅ Follows **Java best practices** and conventions
- ✅ Includes **multiple examples** per pattern
- ✅ **Well-commented** with clear explanations
- ✅ Demonstrates **real-world use cases**
- ✅ Uses **modern Java features** (Java 8+)

## 📚 Additional Resources

- **Gang of Four (GoF)**: "Design Patterns: Elements of Reusable Object-Oriented Software"
- **Martin Fowler**: "Patterns of Enterprise Application Architecture"
- **Microsoft Docs**: "Cloud Design Patterns"
- **Microservices.io**: Microservices patterns reference

## 🤝 Contributing

This is a comprehensive educational resource. Patterns are implemented to demonstrate core concepts clearly and practically.

## 📝 License

Educational use - Free to use for learning and reference.

## ⭐ Progress Tracker

| Category | Complete | Total | Progress |
|----------|----------|-------|----------|
| Creational | 9 | 9 | ████████████ 100% |
| Structural | 9 | 9 | ████████████ 100% |
| Behavioral | 13 | 13 | ████████████ 100% |
| Concurrency | 6 | 15 | █████░░░░░░░ 40% |
| Architectural | 5 | 15 | █████░░░░░░░ 33% |
| Enterprise | 6 | 19 | █████░░░░░░░ 32% |
| Cloud | 4 | 30 | ███░░░░░░░░░ 13% |
| Microservices | 3 | 19 | ███░░░░░░░░░ 16% |
| Additional | 3 | 20 | ███░░░░░░░░░ 15% |
| **TOTAL** | **58** | **150** | ██████░░░░░░ **39%** |

---

**Last Updated**: 2024
**Status**: 🚧 Work in Progress - Adding patterns daily
