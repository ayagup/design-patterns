# Comprehensive Design Patterns Reference

This document provides an exhaustive list of software design patterns, organized by category.

## Table of Contents
- [Creational Patterns](#creational-patterns)
- [Structural Patterns](#structural-patterns)
- [Behavioral Patterns](#behavioral-patterns)
- [Concurrency Patterns](#concurrency-patterns)
- [Architectural Patterns](#architectural-patterns)
- [Enterprise Patterns](#enterprise-patterns)
- [Cloud Patterns](#cloud-patterns)
- [Microservices Patterns](#microservices-patterns)

---

## Creational Patterns

Creational patterns deal with object creation mechanisms, trying to create objects in a manner suitable to the situation.

### 1. **Singleton**
- **Purpose**: Ensures a class has only one instance and provides a global point of access to it
- **Use Case**: Database connections, logging, configuration management
- **Key Features**: Private constructor, static instance, lazy/eager initialization

### 2. **Factory Method**
- **Purpose**: Defines an interface for creating objects but lets subclasses decide which class to instantiate
- **Use Case**: Creating UI components, document types, database connections
- **Key Features**: Encapsulates object creation, promotes loose coupling

### 3. **Abstract Factory**
- **Purpose**: Provides an interface for creating families of related or dependent objects
- **Use Case**: Cross-platform UI toolkits, database abstraction layers
- **Key Features**: Factory of factories, ensures product compatibility

### 4. **Builder**
- **Purpose**: Separates the construction of a complex object from its representation
- **Use Case**: Constructing complex objects with many optional parameters
- **Key Features**: Step-by-step construction, fluent interface, immutability

### 5. **Prototype**
- **Purpose**: Creates new objects by cloning existing ones
- **Use Case**: Object creation is expensive, need to avoid subclassing
- **Key Features**: Cloning, deep vs shallow copy, registry of prototypes

### 6. **Object Pool**
- **Purpose**: Reuses objects that are expensive to create
- **Use Case**: Database connections, thread pools, network connections
- **Key Features**: Recycling, resource management, performance optimization

### 7. **Lazy Initialization**
- **Purpose**: Delays object creation until it's needed
- **Use Case**: Resource-intensive objects, improving startup time
- **Key Features**: On-demand creation, memory optimization

### 8. **Dependency Injection**
- **Purpose**: Provides objects with their dependencies rather than having them construct dependencies
- **Use Case**: Inversion of Control (IoC), testability, loose coupling
- **Key Features**: Constructor injection, setter injection, interface injection

### 9. **Multiton**
- **Purpose**: Ensures only one instance per key exists
- **Use Case**: Managing multiple named instances, registry pattern
- **Key Features**: Map of instances, keyed access

---

## Structural Patterns

Structural patterns deal with object composition, creating relationships between objects to form larger structures.

### 10. **Adapter (Wrapper)**
- **Purpose**: Allows incompatible interfaces to work together
- **Use Case**: Legacy code integration, third-party library integration
- **Key Features**: Interface conversion, class adapter vs object adapter

### 11. **Bridge**
- **Purpose**: Separates abstraction from implementation so both can vary independently
- **Use Case**: Cross-platform applications, device drivers
- **Key Features**: Decoupling, multiple orthogonal hierarchies

### 12. **Composite**
- **Purpose**: Composes objects into tree structures to represent part-whole hierarchies
- **Use Case**: File systems, UI component hierarchies, organization structures
- **Key Features**: Uniform treatment, recursive composition

### 13. **Decorator**
- **Purpose**: Adds new functionality to objects dynamically without altering their structure
- **Use Case**: Adding responsibilities to objects, I/O streams, UI components
- **Key Features**: Wrapping, flexible alternative to subclassing

### 14. **Facade**
- **Purpose**: Provides a simplified interface to a complex subsystem
- **Use Case**: Library wrappers, API simplification, subsystem access
- **Key Features**: Simplification, decoupling, unified interface

### 15. **Flyweight**
- **Purpose**: Shares objects to support large numbers of fine-grained objects efficiently
- **Use Case**: Character rendering, particle systems, caching
- **Key Features**: Intrinsic vs extrinsic state, memory optimization

### 16. **Proxy**
- **Purpose**: Provides a surrogate or placeholder for another object
- **Use Case**: Lazy loading, access control, logging, remote objects
- **Key Features**: Virtual proxy, protection proxy, remote proxy, smart reference

### 17. **Private Class Data**
- **Purpose**: Restricts accessor/mutator access to class data
- **Use Case**: Encapsulation, immutability
- **Key Features**: Data hiding, controlled access

### 18. **Extension Object**
- **Purpose**: Adds functionality to a hierarchy without changing the hierarchy
- **Use Case**: Plugin systems, extensible frameworks
- **Key Features**: Dynamic extension, open for extension

---

## Behavioral Patterns

Behavioral patterns are concerned with algorithms and the assignment of responsibilities between objects.

### 19. **Chain of Responsibility**
- **Purpose**: Passes requests along a chain of handlers
- **Use Case**: Event handling, logging levels, middleware
- **Key Features**: Decoupling sender and receiver, dynamic chain

### 20. **Command**
- **Purpose**: Encapsulates a request as an object
- **Use Case**: Undo/redo, macros, queuing operations, logging
- **Key Features**: Parameterization, queuing, reversibility

### 21. **Interpreter**
- **Purpose**: Defines a grammar and interpreter for a language
- **Use Case**: SQL parsing, expression evaluation, configuration languages
- **Key Features**: Grammar representation, recursive evaluation

### 22. **Iterator**
- **Purpose**: Provides a way to access elements of a collection sequentially
- **Use Case**: Collection traversal, database cursors
- **Key Features**: Encapsulation of traversal, multiple iterators

### 23. **Mediator**
- **Purpose**: Defines an object that encapsulates how objects interact
- **Use Case**: UI dialogs, chat rooms, air traffic control
- **Key Features**: Loose coupling, centralized control, reduced dependencies

### 24. **Memento**
- **Purpose**: Captures and restores an object's internal state
- **Use Case**: Undo mechanisms, snapshots, checkpoints
- **Key Features**: Encapsulation, state preservation

### 25. **Observer (Publish-Subscribe)**
- **Purpose**: Defines a one-to-many dependency between objects
- **Use Case**: Event systems, MVC, data binding, notifications
- **Key Features**: Loose coupling, automatic updates, push/pull models

### 26. **State**
- **Purpose**: Allows an object to alter its behavior when its internal state changes
- **Use Case**: State machines, workflow engines, connection states
- **Key Features**: State encapsulation, state transitions

### 27. **Strategy**
- **Purpose**: Defines a family of algorithms and makes them interchangeable
- **Use Case**: Sorting algorithms, payment methods, compression algorithms
- **Key Features**: Algorithm encapsulation, runtime selection

### 28. **Template Method**
- **Purpose**: Defines the skeleton of an algorithm, deferring some steps to subclasses
- **Use Case**: Frameworks, workflows, game engines
- **Key Features**: Code reuse, inversion of control, hooks

### 29. **Visitor**
- **Purpose**: Separates an algorithm from the object structure it operates on
- **Use Case**: Compilers (AST traversal), file system operations, reporting
- **Key Features**: Double dispatch, open for extension

### 30. **Null Object**
- **Purpose**: Provides a default object to avoid null checks
- **Use Case**: Default behaviors, eliminating null checks
- **Key Features**: Polymorphism, default implementation

### 31. **Specification**
- **Purpose**: Recombines business logic in a boolean fashion
- **Use Case**: Filtering, validation, querying
- **Key Features**: Composable rules, reusable logic

### 32. **Blackboard**
- **Purpose**: Multiple specialized subsystems contribute to a solution
- **Use Case**: AI systems, speech recognition, image processing
- **Key Features**: Shared knowledge base, collaborative problem-solving

---

## Concurrency Patterns

Concurrency patterns deal with multi-threaded programming paradigms.

### 33. **Active Object**
- **Purpose**: Decouples method execution from method invocation
- **Use Case**: Asynchronous method calls, concurrent programming
- **Key Features**: Scheduling, thread safety, message queue

### 34. **Balking**
- **Purpose**: Executes an action only when the object is in a particular state
- **Use Case**: State-dependent operations, resource locking
- **Key Features**: Conditional execution, state checking

### 35. **Barrier**
- **Purpose**: Waits for multiple threads to reach a common point
- **Use Case**: Parallel algorithms, phased computation
- **Key Features**: Synchronization point, thread coordination

### 36. **Double-Checked Locking**
- **Purpose**: Reduces locking overhead for lazy initialization
- **Use Case**: Singleton pattern, expensive initialization
- **Key Features**: Performance optimization, thread safety

### 37. **Guarded Suspension**
- **Purpose**: Manages operations that require both a lock and a condition
- **Use Case**: Producer-consumer, resource availability
- **Key Features**: Conditional waiting, thread coordination

### 38. **Monitor Object**
- **Purpose**: Synchronizes concurrent method execution
- **Use Case**: Thread-safe objects, mutual exclusion
- **Key Features**: Synchronized methods, condition variables

### 39. **Reactor**
- **Purpose**: Handles service requests delivered concurrently
- **Use Case**: Event-driven servers, I/O multiplexing
- **Key Features**: Event loop, non-blocking I/O, demultiplexing

### 40. **Read-Write Lock**
- **Purpose**: Allows concurrent read access while maintaining exclusive write access
- **Use Case**: Caching, shared resources, databases
- **Key Features**: Multiple readers, single writer, performance

### 41. **Scheduler**
- **Purpose**: Controls the order of thread execution
- **Use Case**: Task scheduling, thread pools, job queues
- **Key Features**: Priority management, resource allocation

### 42. **Thread Pool**
- **Purpose**: Maintains multiple threads waiting for tasks
- **Use Case**: Server applications, parallel processing
- **Key Features**: Resource management, performance, reusability

### 43. **Thread-Specific Storage**
- **Purpose**: Stores data specific to each thread
- **Use Case**: Per-thread context, avoiding shared state
- **Key Features**: Thread-local variables, isolation

### 44. **Proactor**
- **Purpose**: Asynchronous operation completion handling
- **Use Case**: Asynchronous I/O, high-performance servers
- **Key Features**: Completion events, asynchronous operations

### 45. **Lock**
- **Purpose**: Provides exclusive access to a shared resource
- **Use Case**: Critical sections, mutual exclusion
- **Key Features**: Blocking, ownership, reentrancy

### 46. **Future/Promise**
- **Purpose**: Represents a value that will be available in the future
- **Use Case**: Asynchronous computations, parallel tasks
- **Key Features**: Non-blocking, chaining, error handling

### 47. **Actor Model**
- **Purpose**: Concurrent computation with actors that communicate via messages
- **Use Case**: Distributed systems, concurrent applications
- **Key Features**: Message passing, isolation, scalability

---

## Architectural Patterns

Architectural patterns define the overall structure of software systems.

### 48. **Model-View-Controller (MVC)**
- **Purpose**: Separates application into three interconnected components
- **Use Case**: Web applications, desktop applications, UI frameworks
- **Key Features**: Separation of concerns, multiple views, testability

### 49. **Model-View-Presenter (MVP)**
- **Purpose**: Derivative of MVC with presenter handling UI logic
- **Use Case**: Complex UI applications, testable UI
- **Key Features**: Passive view, presenter logic, testability

### 50. **Model-View-ViewModel (MVVM)**
- **Purpose**: Separates UI from business logic using data binding
- **Use Case**: WPF, Angular, modern UI frameworks
- **Key Features**: Data binding, declarative UI, separation

### 51. **Layered Architecture**
- **Purpose**: Organizes system into layers with specific responsibilities
- **Use Case**: Enterprise applications, web applications
- **Key Features**: Separation of concerns, dependency rules, abstraction

### 52. **Hexagonal Architecture (Ports and Adapters)**
- **Purpose**: Isolates core logic from external concerns
- **Use Case**: Domain-driven design, testable systems
- **Key Features**: Ports, adapters, dependency inversion

### 53. **Clean Architecture**
- **Purpose**: Keeps business rules independent of frameworks and UI
- **Use Case**: Enterprise applications, maintainable systems
- **Key Features**: Dependency rule, layers, independence

### 54. **Onion Architecture**
- **Purpose**: Layers depend inward, domain at the center
- **Use Case**: Domain-driven design, enterprise applications
- **Key Features**: Core independence, dependency inversion

### 55. **Pipe and Filter**
- **Purpose**: Processes data through a series of processing components
- **Use Case**: Data processing pipelines, compilers, Unix commands
- **Key Features**: Reusable components, sequential processing

### 56. **Microkernel (Plugin Architecture)**
- **Purpose**: Core system with pluggable extensions
- **Use Case**: Application frameworks, product lines, extensible systems
- **Key Features**: Core system, plugins, extensibility

### 57. **Event-Driven Architecture**
- **Purpose**: Components communicate through events
- **Use Case**: Real-time systems, distributed systems, UI applications
- **Key Features**: Loose coupling, asynchronous, scalability

### 58. **Service-Oriented Architecture (SOA)**
- **Purpose**: Services communicate over a network
- **Use Case**: Enterprise integration, distributed systems
- **Key Features**: Interoperability, reusability, loose coupling

### 59. **Space-Based Architecture**
- **Purpose**: Minimizes database bottlenecks using in-memory data grids
- **Use Case**: High-volume applications, scalable systems
- **Key Features**: Processing units, virtualized middleware, scalability

### 60. **CQRS (Command Query Responsibility Segregation)**
- **Purpose**: Separates read and write operations
- **Use Case**: High-performance applications, event sourcing
- **Key Features**: Read/write separation, scalability, optimization

### 61. **Event Sourcing**
- **Purpose**: Stores state changes as a sequence of events
- **Use Case**: Audit trails, temporal queries, distributed systems
- **Key Features**: Event log, replay capability, audit

### 62. **Broker Pattern**
- **Purpose**: Coordinates communication between distributed components
- **Use Case**: Distributed systems, middleware, message-oriented systems
- **Key Features**: Decoupling, transparency, coordination

---

## Enterprise Patterns

Enterprise patterns address concerns in large-scale business applications.

### 63. **Repository**
- **Purpose**: Mediates between domain and data mapping layers
- **Use Case**: Data access abstraction, domain-driven design
- **Key Features**: Collection-like interface, persistence abstraction

### 64. **Unit of Work**
- **Purpose**: Maintains a list of objects affected by a business transaction
- **Use Case**: Database operations, transaction management
- **Key Features**: Transaction boundary, change tracking

### 65. **Data Mapper**
- **Purpose**: Transfers data between objects and database
- **Use Case**: ORM implementations, persistence layer
- **Key Features**: Separation, mapping, independence

### 66. **Active Record**
- **Purpose**: Object carries both data and behavior
- **Use Case**: Simple CRUD applications, RAD frameworks
- **Key Features**: Self-persistence, simplicity

### 67. **Table Data Gateway**
- **Purpose**: Gateway to a database table
- **Use Case**: Data access layer, simple queries
- **Key Features**: Table-centric, SQL encapsulation

### 68. **Row Data Gateway**
- **Purpose**: Object acts as gateway to a single record
- **Use Case**: Record-based operations, data access
- **Key Features**: Row-centric, CRUD operations

### 69. **Data Transfer Object (DTO)**
- **Purpose**: Carries data between processes
- **Use Case**: Remote calls, layer communication, serialization
- **Key Features**: Serializable, no behavior, transfer optimization

### 70. **Service Layer**
- **Purpose**: Defines application's boundary and operations
- **Use Case**: Application services, use cases, transaction boundaries
- **Key Features**: Business operations, transaction management

### 71. **Domain Model**
- **Purpose**: Object model of the domain
- **Use Case**: Complex business logic, domain-driven design
- **Key Features**: Rich objects, business rules, behavior

### 72. **Transaction Script**
- **Purpose**: Organizes business logic by procedures
- **Use Case**: Simple business logic, procedural approach
- **Key Features**: Procedural, simple, straightforward

### 73. **Table Module**
- **Purpose**: Single instance handles all rows in a table
- **Use Case**: Table-based operations, record sets
- **Key Features**: Table-centric, stateless

### 74. **Identity Map**
- **Purpose**: Ensures each object is loaded only once
- **Use Case**: ORM, caching, identity management
- **Key Features**: Object identity, caching, performance

### 75. **Lazy Load**
- **Purpose**: Defers loading of an object until needed
- **Use Case**: Performance optimization, ORM
- **Key Features**: On-demand loading, proxy, value holder

### 76. **Front Controller**
- **Purpose**: Single handler for all requests
- **Use Case**: Web applications, request routing
- **Key Features**: Centralized control, request handling

### 77. **Application Controller**
- **Purpose**: Centralizes retrieval and invocation of request-processing components
- **Use Case**: Complex navigation, workflow
- **Key Features**: Flow control, navigation logic

### 78. **Page Controller**
- **Purpose**: Object handles a request for a specific page
- **Use Case**: Simple web applications, page-based logic
- **Key Features**: Page-specific, simple routing

### 79. **Template View**
- **Purpose**: Renders information into HTML by embedding markers
- **Use Case**: Web page generation, templating
- **Key Features**: Markup embedding, separation of concerns

### 80. **Transform View**
- **Purpose**: Transforms domain data to HTML
- **Use Case**: XML/XSLT transformation, data presentation
- **Key Features**: Transformation, data formatting

### 81. **Two-Step View**
- **Purpose**: Transforms domain data to logical presentation then to HTML
- **Use Case**: Consistent look and feel, multi-format output
- **Key Features**: Two-stage transformation, flexibility

---

## Cloud Patterns

Cloud patterns address challenges in cloud-native applications.

### 82. **Ambassador**
- **Purpose**: Creates helper services that send network requests
- **Use Case**: Monitoring, logging, routing, circuit breaking
- **Key Features**: Proxy, language-agnostic, offloading

### 83. **Anti-Corruption Layer**
- **Purpose**: Isolates different subsystems
- **Use Case**: Legacy integration, bounded contexts
- **Key Features**: Translation, isolation, migration

### 84. **Backends for Frontends (BFF)**
- **Purpose**: Separate backend services for different frontends
- **Use Case**: Mobile vs web, client-specific APIs
- **Key Features**: Client optimization, separation

### 85. **Bulkhead**
- **Purpose**: Isolates critical resources
- **Use Case**: Resource isolation, fault tolerance
- **Key Features**: Isolation, resilience, resource pools

### 86. **Circuit Breaker**
- **Purpose**: Prevents cascading failures
- **Use Case**: Remote service calls, fault tolerance
- **Key Features**: Failure detection, fallback, recovery

### 87. **Compensating Transaction**
- **Purpose**: Undoes work performed by a series of steps
- **Use Case**: Distributed transactions, eventual consistency
- **Key Features**: Rollback, compensation logic

### 88. **Competing Consumers**
- **Purpose**: Multiple concurrent consumers process messages
- **Use Case**: Message processing, load distribution
- **Key Features**: Parallel processing, scalability

### 89. **Compute Resource Consolidation**
- **Purpose**: Consolidates multiple tasks into a single compute unit
- **Use Case**: Cost optimization, resource utilization
- **Key Features**: Consolidation, efficiency

### 90. **Event Sourcing** (see #61)
- **Purpose**: Stores state as events
- **Use Case**: Audit, temporal queries, event-driven
- **Key Features**: Event store, replay, audit trail

### 91. **External Configuration Store**
- **Purpose**: Externalizes configuration
- **Use Case**: Configuration management, environment-specific settings
- **Key Features**: Centralization, dynamic updates

### 92. **Federated Identity**
- **Purpose**: Delegates authentication to an external provider
- **Use Case**: Single sign-on, identity management
- **Key Features**: Third-party authentication, trust

### 93. **Gatekeeper**
- **Purpose**: Protects applications using a dedicated host
- **Use Case**: Security, request validation, sanitization
- **Key Features**: Security layer, validation

### 94. **Gateway Aggregation**
- **Purpose**: Aggregates multiple service requests
- **Use Case**: Reducing client calls, data composition
- **Key Features**: Aggregation, reduction

### 95. **Gateway Offloading**
- **Purpose**: Offloads shared functionality to a gateway
- **Use Case**: SSL termination, caching, authentication
- **Key Features**: Centralization, offloading

### 96. **Gateway Routing**
- **Purpose**: Routes requests to multiple services
- **Use Case**: API gateway, request routing
- **Key Features**: Routing, single entry point

### 97. **Health Endpoint Monitoring**
- **Purpose**: Exposes health check endpoints
- **Use Case**: Monitoring, load balancing, orchestration
- **Key Features**: Health checks, availability monitoring

### 98. **Index Table**
- **Purpose**: Creates indexes for efficient queries
- **Use Case**: Query optimization, data access
- **Key Features**: Secondary indexes, performance

### 99. **Leader Election**
- **Purpose**: Coordinates actions by electing a leader
- **Use Case**: Distributed systems, coordination
- **Key Features**: Coordination, single leader

### 100. **Materialized View**
- **Purpose**: Pre-generates views over data
- **Use Case**: Query performance, reporting
- **Key Features**: Pre-computation, denormalization

### 101. **Priority Queue**
- **Purpose**: Prioritizes requests sent to services
- **Use Case**: Message processing, task scheduling
- **Key Features**: Prioritization, ordering

### 102. **Publisher-Subscriber** (see #25)
- **Purpose**: Asynchronous messaging between publishers and subscribers
- **Use Case**: Event notification, decoupling
- **Key Features**: Decoupling, broadcast, topics

### 103. **Queue-Based Load Leveling**
- **Purpose**: Uses a queue to smooth load
- **Use Case**: Load management, burst handling
- **Key Features**: Buffering, decoupling, smoothing

### 104. **Retry**
- **Purpose**: Retries failed operations
- **Use Case**: Transient failures, resilience
- **Key Features**: Automatic retry, exponential backoff

### 105. **Scheduler Agent Supervisor**
- **Purpose**: Coordinates distributed actions
- **Use Case**: Distributed workflows, orchestration
- **Key Features**: Coordination, monitoring, recovery

### 106. **Sharding**
- **Purpose**: Divides data into horizontal partitions
- **Use Case**: Scalability, performance, large datasets
- **Key Features**: Horizontal partitioning, distribution

### 107. **Sidecar**
- **Purpose**: Deploys helper components alongside applications
- **Use Case**: Logging, monitoring, configuration
- **Key Features**: Co-deployment, isolation, reusability

### 108. **Static Content Hosting**
- **Purpose**: Delivers static content efficiently
- **Use Case**: CDN, web hosting, performance
- **Key Features**: Separation, CDN, caching

### 109. **Strangler Fig**
- **Purpose**: Incrementally migrates legacy systems
- **Use Case**: System migration, legacy modernization
- **Key Features**: Incremental replacement, facade

### 110. **Throttling**
- **Purpose**: Controls resource consumption
- **Use Case**: Rate limiting, resource protection
- **Key Features**: Rate limiting, quotas, protection

### 111. **Valet Key**
- **Purpose**: Provides restricted direct access to resources
- **Use Case**: Direct client access, security
- **Key Features**: Time-limited tokens, restricted access

---

## Microservices Patterns

Microservices patterns address challenges in distributed microservices architectures.

### 112. **API Gateway**
- **Purpose**: Single entry point for all clients
- **Use Case**: Request routing, composition, protocol translation
- **Key Features**: Routing, aggregation, security

### 113. **Service Registry and Discovery**
- **Purpose**: Tracks available service instances
- **Use Case**: Dynamic service locations, load balancing
- **Key Features**: Registration, discovery, health checks

### 114. **Saga**
- **Purpose**: Manages distributed transactions
- **Use Case**: Cross-service transactions, eventual consistency
- **Key Features**: Choreography, orchestration, compensation

### 115. **Database per Service**
- **Purpose**: Each service has its own database
- **Use Case**: Service autonomy, polyglot persistence
- **Key Features**: Data isolation, independence

### 116. **Shared Database**
- **Purpose**: Multiple services share a database
- **Use Case**: Simple integration, legacy systems
- **Key Features**: Tight coupling, simplicity (anti-pattern in pure microservices)

### 117. **API Composition**
- **Purpose**: Implements queries by invoking multiple services
- **Use Case**: Cross-service queries, data composition
- **Key Features**: Composition, aggregation

### 118. **Aggregator**
- **Purpose**: Collects data from multiple services
- **Use Case**: Data aggregation, composite queries
- **Key Features**: Collection, composition, single response

### 119. **Chained Microservice**
- **Purpose**: Services call each other synchronously
- **Use Case**: Sequential processing, pipelines
- **Key Features**: Synchronous chains, dependency

### 120. **Branch Microservice**
- **Purpose**: Processes requests in parallel branches
- **Use Case**: Parallel processing, independent operations
- **Key Features**: Parallel calls, aggregation

### 121. **Asynchronous Messaging**
- **Purpose**: Services communicate via messages
- **Use Case**: Event-driven, loose coupling
- **Key Features**: Asynchronous, message brokers, queues

### 122. **Transactional Outbox**
- **Purpose**: Reliably publishes events
- **Use Case**: Event publishing, consistency
- **Key Features**: Database table, polling/transaction log tailing

### 123. **Event-Driven Architecture** (see #57)
- **Purpose**: Services react to events
- **Use Case**: Real-time processing, loose coupling
- **Key Features**: Events, pub/sub, asynchronous

### 124. **Distributed Tracing**
- **Purpose**: Traces requests across services
- **Use Case**: Monitoring, debugging, performance
- **Key Features**: Correlation IDs, trace context, visualization

### 125. **Log Aggregation**
- **Purpose**: Centralizes logs from all services
- **Use Case**: Monitoring, debugging, analysis
- **Key Features**: Centralization, searchability, analysis

### 126. **Application Metrics**
- **Purpose**: Instruments services to gather metrics
- **Use Case**: Monitoring, alerting, performance
- **Key Features**: Metrics collection, dashboards, alerting

### 127. **Audit Logging**
- **Purpose**: Records user actions
- **Use Case**: Compliance, security, debugging
- **Key Features**: User actions, immutability, retention

### 128. **Exception Tracking**
- **Purpose**: Centralizes exception reporting
- **Use Case**: Error monitoring, debugging
- **Key Features**: Error aggregation, alerting, analysis

### 129. **Service Mesh**
- **Purpose**: Infrastructure layer for service communication
- **Use Case**: Traffic management, security, observability
- **Key Features**: Sidecar proxies, control plane, policies

### 130. **Backend for Frontend (BFF)** (see #84)
- **Purpose**: Separate backends for different clients
- **Use Case**: Client-specific APIs, optimization
- **Key Features**: Client tailoring, separation

---

## Additional Patterns

### 131. **Registry**
- **Purpose**: Well-known object that others can use to find services
- **Use Case**: Service location, dependency injection containers
- **Key Features**: Service lookup, registration

### 132. **Money**
- **Purpose**: Represents monetary values
- **Use Case**: Financial calculations, currency handling
- **Key Features**: Precision, currency, immutability

### 133. **Special Case** (Null Object variant)
- **Purpose**: Subclass that provides special behavior for particular cases
- **Use Case**: Default behaviors, null handling
- **Key Features**: Polymorphism, special handling

### 134. **Plugin**
- **Purpose**: Extends functionality without modifying core
- **Use Case**: Extensible applications, third-party extensions
- **Key Features**: Dynamic loading, extension points

### 135. **Service Stub**
- **Purpose**: Removes dependence on problematic services during testing
- **Use Case**: Testing, development, integration
- **Key Features**: Test doubles, simulation

### 136. **Service Locator**
- **Purpose**: Centralized registry for service lookup
- **Use Case**: Dependency resolution, service discovery
- **Key Features**: Registry, lookup, decoupling

### 137. **Module**
- **Purpose**: Groups related code into a single unit
- **Use Case**: Code organization, namespace management
- **Key Features**: Encapsulation, namespace, organization

### 138. **Revealing Module**
- **Purpose**: Encapsulates private data with public API
- **Use Case**: JavaScript patterns, encapsulation
- **Key Features**: Closures, privacy, public interface

### 139. **Mixin**
- **Purpose**: Provides methods for use by other classes without inheritance
- **Use Case**: Code reuse, trait composition
- **Key Features**: Horizontal reuse, composition

### 140. **Twin**
- **Purpose**: Allows modeling of multiple inheritance
- **Use Case**: Simulating multiple inheritance
- **Key Features**: Paired classes, delegation

### 141. **Marker Interface**
- **Purpose**: Empty interface to mark classes
- **Use Case**: Metadata, type identification
- **Key Features**: Tagging, no methods, type checking

### 142. **Interceptor**
- **Purpose**: Intercepts method calls
- **Use Case**: AOP, logging, security, caching
- **Key Features**: Cross-cutting concerns, interception

### 143. **Callback**
- **Purpose**: Passes executable code as an argument
- **Use Case**: Event handlers, asynchronous operations
- **Key Features**: Function pointers, event handling

### 144. **Execute Around**
- **Purpose**: Executes code before and after an operation
- **Use Case**: Resource management, transactions
- **Key Features**: Setup/teardown, resource management

### 145. **Type Tunnel**
- **Purpose**: Passes types through APIs that don't support generics
- **Use Case**: Legacy APIs, type preservation
- **Key Features**: Type information, workaround

### 146. **Curiously Recurring Template Pattern (CRTP)**
- **Purpose**: Class inherits from template instantiation of itself
- **Use Case**: Static polymorphism, compile-time optimization
- **Key Features**: Template metaprogramming, performance

### 147. **Pimpl (Pointer to Implementation)**
- **Purpose**: Hides implementation details
- **Use Case**: ABI stability, compilation firewall
- **Key Features**: Opaque pointer, encapsulation

### 148. **Resource Acquisition Is Initialization (RAII)**
- **Purpose**: Ties resource lifetime to object lifetime
- **Use Case**: Resource management, exception safety
- **Key Features**: Constructor/destructor, automatic cleanup

### 149. **Scope Guard**
- **Purpose**: Ensures cleanup code executes
- **Use Case**: Exception safety, resource cleanup
- **Key Features**: RAII, cleanup actions

### 150. **Policy-Based Design**
- **Purpose**: Uses templates to configure class behavior
- **Use Case**: Generic programming, compile-time configuration
- **Key Features**: Templates, policies, flexibility

---

## Anti-Patterns

While not design patterns per se, it's important to recognize common anti-patterns to avoid:

### Common Anti-Patterns
- **God Object**: Object that knows too much or does too much
- **Spaghetti Code**: Tangled control structures
- **Lava Flow**: Dead code that's never removed
- **Golden Hammer**: Using a familiar solution for everything
- **Copy-Paste Programming**: Code duplication instead of abstraction
- **Hard Coding**: Embedding data directly in code
- **Magic Numbers**: Unnamed numerical constants
- **Big Ball of Mud**: System with no recognizable structure
- **Circular Dependencies**: Modules that depend on each other
- **Premature Optimization**: Optimizing before understanding the problem
- **Cargo Cult Programming**: Using patterns without understanding
- **Not Invented Here**: Rejecting external solutions
- **Reinventing the Wheel**: Creating solutions that already exist
- **Analysis Paralysis**: Over-analyzing without taking action
- **Death March**: Project doomed from the start
- **Vendor Lock-In**: Excessive dependence on a vendor
- **Inner-Platform Effect**: Creating a poor copy of another system

---

## Pattern Selection Guidelines

### When to Use Design Patterns
1. **Proven Solutions**: Apply when facing common, recurring problems
2. **Communication**: Use as a vocabulary with your team
3. **Flexibility**: When you need adaptable, maintainable code
4. **Refactoring**: To improve existing code structure

### When NOT to Use Design Patterns
1. **Over-Engineering**: Don't add complexity for simple problems
2. **Premature Application**: Don't force patterns where they don't fit
3. **Learning Phase**: Don't use patterns you don't understand
4. **Performance Critical**: Some patterns add overhead

### Pattern Selection Criteria
- **Problem Domain**: What problem are you solving?
- **Constraints**: Performance, scalability, maintainability
- **Team Experience**: Team familiarity with the pattern
- **Project Phase**: Current stage of development
- **Trade-offs**: Understand costs and benefits

---

## Pattern Relationships

### Patterns That Work Well Together
- **Factory + Singleton**: Create a single factory instance
- **Strategy + Factory**: Select strategies via factory
- **Observer + Mediator**: Centralized event management
- **Composite + Visitor**: Process complex structures
- **Decorator + Factory**: Create decorated objects
- **Command + Memento**: Implement undo with state saving
- **Iterator + Composite**: Traverse composite structures
- **Proxy + Singleton**: Single proxy instance
- **Abstract Factory + Bridge**: Create platform-specific implementations
- **Builder + Composite**: Build complex composite structures

### Patterns That May Conflict
- **Singleton + Testing**: Hard to unit test
- **Active Record + Repository**: Different data access philosophies
- **Service Locator + Dependency Injection**: Different dependency resolution
- **Shared Database + Microservices**: Violates service independence

---

## Modern Pattern Trends

### Reactive Patterns
- **Reactive Streams**: Asynchronous stream processing
- **Backpressure**: Flow control in streams
- **Reactive Extensions (Rx)**: Observable sequences

### Serverless Patterns
- **Function as a Service (FaaS)**: Stateless function execution
- **Event-Driven Functions**: Functions triggered by events
- **Backend as a Service (BaaS)**: Third-party backend services

### Container Patterns
- **Sidecar Container**: Helper container in same pod
- **Ambassador Container**: Proxy for external services
- **Adapter Container**: Standardizes interfaces
- **Init Container**: Initialization before main container

### DevOps Patterns
- **Infrastructure as Code**: Declarative infrastructure
- **Immutable Infrastructure**: Replace rather than update
- **Blue-Green Deployment**: Two identical environments
- **Canary Release**: Gradual rollout to subset of users
- **Feature Toggles**: Enable/disable features at runtime

---

## Resources and Further Reading

### Classic Books
- **"Design Patterns: Elements of Reusable Object-Oriented Software"** - Gang of Four (GoF)
- **"Pattern-Oriented Software Architecture"** series - Buschmann et al.
- **"Enterprise Integration Patterns"** - Hohpe & Woolf
- **"Patterns of Enterprise Application Architecture"** - Martin Fowler
- **"Head First Design Patterns"** - Freeman & Freeman
- **"Domain-Driven Design"** - Eric Evans
- **"Implementing Domain-Driven Design"** - Vaughn Vernon
- **"Building Microservices"** - Sam Newman
- **"Cloud Native Patterns"** - Cornelia Davis
- **"Release It!"** - Michael Nygard

### Online Resources
- **Refactoring.Guru**: Visual pattern explanations
- **SourceMaking**: Design patterns and anti-patterns
- **Martin Fowler's Blog**: Enterprise patterns and architecture
- **Microsoft Azure Architecture Center**: Cloud patterns
- **AWS Well-Architected Framework**: Cloud design principles
- **Microservices.io**: Microservices patterns
- **reactive.io**: Reactive patterns

### Pattern Catalogs
- **Gang of Four Patterns**: Original 23 patterns
- **POSA Patterns**: Pattern-Oriented Software Architecture
- **Enterprise Patterns**: Fowler's enterprise patterns
- **Cloud Design Patterns**: Microsoft's cloud patterns
- **Microservices Patterns**: Chris Richardson's catalog
- **EIP Patterns**: Enterprise Integration Patterns

---

## Conclusion

Design patterns are powerful tools in a developer's arsenal, but they should be applied judiciously. Understanding when and why to use a pattern is as important as knowing how to implement it. 

**Key Takeaways:**
1. Patterns are proven solutions to recurring problems
2. They provide a common vocabulary for developers
3. Not every problem needs a pattern
4. Understand the trade-offs before applying
5. Patterns evolve with technology and practices
6. Combine patterns thoughtfully
7. Context matters - adapt patterns to your needs

Remember: **"A design pattern is a description or template for how to solve a problem that can be used in many different situations."** Use them as guidelines, not rigid rules.

---

**Document Version**: 1.0  
**Last Updated**: October 21, 2025  
**Total Patterns Documented**: 150+

