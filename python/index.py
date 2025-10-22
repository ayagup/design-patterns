#!/usr/bin/env python3
"""
Design Patterns Collection - Quick Reference Index
Run this script to get an interactive menu of all patterns
"""

PATTERNS = {
    "1. Creational Patterns (9)": {
        "creational/singleton_pattern.py": "Ensures only one instance exists",
        "creational/factory_method_pattern.py": "Creates objects without specifying exact class",
        "creational/abstract_factory_pattern.py": "Families of related objects",
        "creational/builder_pattern.py": "Constructs complex objects step by step",
        "creational/prototype_pattern.py": "Creates objects by cloning",
        "creational/object_pool_pattern.py": "Reuses expensive objects",
        "creational/lazy_initialization_pattern.py": "Delays object creation until needed",
        "creational/dependency_injection_pattern.py": "Injects dependencies externally",
        "creational/multiton_pattern.py": "Manages a map of named instances",
    },
    "2. Structural Patterns (9)": {
        "structural/adapter_pattern.py": "Makes incompatible interfaces work together",
        "structural/bridge_pattern.py": "Separates abstraction from implementation",
        "structural/composite_pattern.py": "Tree structure of objects",
        "structural/decorator_pattern.py": "Adds behavior dynamically",
        "structural/facade_pattern.py": "Simplified interface to complex subsystem",
        "structural/flyweight_pattern.py": "Shares common state to save memory",
        "structural/proxy_pattern.py": "Placeholder for another object",
        "structural/private_class_data_pattern.py": "Restricts accessor/mutator access",
        "structural/extension_object_pattern.py": "Adds functionality without inheritance",
    },
    "3. Behavioral Patterns (14)": {
        "behavioral/chain_of_responsibility_pattern.py": "Passes request along chain",
        "behavioral/command_pattern.py": "Encapsulates requests as objects",
        "behavioral/iterator_pattern.py": "Accesses elements sequentially",
        "behavioral/mediator_pattern.py": "Centralizes complex communications",
        "behavioral/memento_pattern.py": "Captures and restores object state",
        "behavioral/observer_pattern.py": "Notifies dependents of state changes",
        "behavioral/state_pattern.py": "Alters behavior when state changes",
        "behavioral/strategy_pattern.py": "Encapsulates interchangeable algorithms",
        "behavioral/template_method_pattern.py": "Defines skeleton of algorithm",
        "behavioral/visitor_pattern.py": "Separates algorithms from objects",
        "behavioral/null_object_pattern.py": "Provides default behavior",
        "behavioral/interpreter_pattern.py": "Interprets language grammar",
        "behavioral/specification_pattern.py": "Business rules as reusable objects",
        "behavioral/blackboard_pattern.py": "Multiple knowledge sources collaborate",
    },
    "4. Concurrency Patterns (15)": {
        "concurrency/active_object_pattern.py": "Decouples method execution from invocation",
        "concurrency/actor_model_pattern.py": "Message-passing concurrent computation",
        "concurrency/balking_pattern.py": "Executes only if in appropriate state",
        "concurrency/barrier_pattern.py": "Synchronizes multiple threads",
        "concurrency/double_checked_locking_pattern.py": "Reduces locking overhead",
        "concurrency/future_promise_pattern.py": "Placeholder for asynchronous result",
        "concurrency/guarded_suspension_pattern.py": "Waits until condition is met",
        "concurrency/lock_pattern.py": "Mutual exclusion synchronization",
        "concurrency/monitor_object_pattern.py": "Thread-safe object access",
        "concurrency/proactor_pattern.py": "Asynchronous operation completion",
        "concurrency/reactor_pattern.py": "Demultiplexes and dispatches events",
        "concurrency/read_write_lock_pattern.py": "Multiple readers, single writer",
        "concurrency/scheduler_pattern.py": "Schedules thread execution",
        "concurrency/thread_pool_pattern.py": "Reuses threads for multiple tasks",
        "concurrency/thread_specific_storage_pattern.py": "Per-thread data storage",
    },
    "5. Architectural Patterns (15)": {
        "architectural/mvc_pattern.py": "Model-View-Controller",
        "architectural/mvp_pattern.py": "Model-View-Presenter",
        "architectural/mvvm_pattern.py": "Model-View-ViewModel",
        "architectural/layered_pattern.py": "Organizes system into layers",
        "architectural/event_driven_pattern.py": "Event producers and consumers",
        "architectural/pipe_filter_pattern.py": "Data flows through filters",
        "architectural/hexagonal_pattern.py": "Ports & Adapters",
        "architectural/clean_architecture_pattern.py": "Dependency rule and layers",
        "architectural/onion_pattern.py": "Domain-centric layers",
        "architectural/microkernel_pattern.py": "Plugin-based extensibility",
        "architectural/soa_pattern.py": "Service-Oriented Architecture",
        "architectural/cqrs_pattern.py": "Command Query Responsibility Segregation",
        "architectural/event_sourcing_pattern.py": "Stores state changes as events",
        "architectural/broker_pattern.py": "Mediates distributed communication",
        "architectural/space_based_pattern.py": "Distributed in-memory data grid",
    },
    "6. Enterprise Patterns (19)": {
        "enterprise/repository_pattern.py": "Collection-like interface for data",
        "enterprise/unit_of_work_pattern.py": "Maintains list of changes",
        "enterprise/data_mapper_pattern.py": "Maps objects to database",
        "enterprise/active_record_pattern.py": "Domain object + database access",
        "enterprise/dto_pattern.py": "Data Transfer Object",
        "enterprise/service_layer_pattern.py": "Application's boundary",
        "enterprise/domain_model_pattern.py": "Object model of domain",
        "enterprise/transaction_script_pattern.py": "Procedural business logic",
        "enterprise/table_module_pattern.py": "Single instance per table",
        "enterprise/identity_map_pattern.py": "Ensures one object per record",
        "enterprise/lazy_load_pattern.py": "Defers loading until needed",
        "enterprise/front_controller_pattern.py": "Single handler for requests",
        "enterprise/page_controller_pattern.py": "One controller per page",
        "enterprise/application_controller_pattern.py": "Navigation flow control",
        "enterprise/template_view_pattern.py": "Renders with embedded markers",
        "enterprise/transform_view_pattern.py": "Transforms domain to presentation",
        "enterprise/two_step_view_pattern.py": "Two-stage transformation",
        "enterprise/table_data_gateway_pattern.py": "Gateway to database table",
        "enterprise/row_data_gateway_pattern.py": "Gateway to single record",
    },
    "7. Cloud Patterns (20)": {
        "cloud/circuit_breaker_pattern.py": "Prevents cascading failures",
        "cloud/retry_pattern.py": "Retries failed operations",
        "cloud/bulkhead_pattern.py": "Isolates resources",
        "cloud/cache_aside_pattern.py": "Load data on-demand into cache",
        "cloud/throttling_pattern.py": "Controls resource consumption",
        "cloud/ambassador_pattern.py": "Helper services for network requests",
        "cloud/anti_corruption_layer_pattern.py": "Isolates subsystems",
        "cloud/backends_for_frontends_pattern.py": "Separate backends for UIs",
        "cloud/compensating_transaction_pattern.py": "Undoes failed operations",
        "cloud/competing_consumers_pattern.py": "Multiple consumers process messages",
        "cloud/gateway_aggregation_pattern.py": "Aggregates multiple requests",
        "cloud/health_endpoint_monitoring_pattern.py": "Health check endpoints",
        "cloud/leader_election_pattern.py": "Coordinate by electing leader",
        "cloud/materialized_view_pattern.py": "Pre-generated views",
        "cloud/priority_queue_pattern.py": "Prioritizes messages",
        "cloud/queue_based_load_leveling_pattern.py": "Smooths load with queue",
        "cloud/sharding_pattern.py": "Horizontal partitioning",
        "cloud/sidecar_pattern.py": "Deploy helper alongside application",
        "cloud/strangler_fig_pattern.py": "Gradually replace legacy",
        "cloud/valet_key_pattern.py": "Restricted direct access token",
    },
    "8. Microservices Patterns (18)": {
        "microservices/api_gateway_pattern.py": "Single entry point for clients",
        "microservices/service_discovery_pattern.py": "Dynamically discovers services",
        "microservices/saga_pattern.py": "Manages distributed transactions",
        "microservices/database_per_service_pattern.py": "Each service owns database",
        "microservices/shared_database_pattern.py": "Services share same database",
        "microservices/api_composition_pattern.py": "Composes data from multiple services",
        "microservices/aggregator_microservice_pattern.py": "Aggregates multiple services",
        "microservices/chained_microservice_pattern.py": "Services call in sequence",
        "microservices/branch_microservice_pattern.py": "Parallel service invocation",
        "microservices/asynchronous_messaging_pattern.py": "Event-driven communication",
        "microservices/transactional_outbox_pattern.py": "Reliable event publishing",
        "microservices/distributed_tracing_pattern.py": "Traces requests across services",
        "microservices/log_aggregation_pattern.py": "Centralizes logs",
        "microservices/application_metrics_pattern.py": "Instruments services",
        "microservices/audit_logging_pattern.py": "Records user actions",
        "microservices/exception_tracking_pattern.py": "Centralizes exception reporting",
        "microservices/service_mesh_pattern.py": "Infrastructure layer for communication",
        "microservices/externalized_configuration_pattern.py": "Config outside services",
    }
}


def print_index():
    """Print complete pattern index"""
    print("\n" + "="*80)
    print("DESIGN PATTERNS COLLECTION - QUICK REFERENCE INDEX")
    print("="*80 + "\n")
    
    total = 0
    for category, patterns in PATTERNS.items():
        print(f"\n{category}")
        print("-" * 80)
        for path, description in patterns.items():
            print(f"  • {path:<60} {description}")
            total += 1
    
    print("\n" + "="*80)
    print(f"TOTAL: {total} patterns")
    print("="*80 + "\n")


def search_patterns(query):
    """Search patterns by keyword"""
    query = query.lower()
    results = []
    
    for category, patterns in PATTERNS.items():
        for path, description in patterns.items():
            if query in path.lower() or query in description.lower():
                results.append((category, path, description))
    
    return results


def main():
    """Interactive menu"""
    import sys
    
    if len(sys.argv) > 1:
        if sys.argv[1] == "search":
            if len(sys.argv) > 2:
                query = " ".join(sys.argv[2:])
                results = search_patterns(query)
                print(f"\nSearch results for '{query}':")
                print("-" * 80)
                for category, path, description in results:
                    print(f"\n{category}")
                    print(f"  • {path}")
                    print(f"    {description}")
                print(f"\nFound {len(results)} patterns\n")
            else:
                print("Usage: python index.py search <keyword>")
        elif sys.argv[1] == "count":
            total = sum(len(patterns) for patterns in PATTERNS.values())
            print(f"\nTotal patterns: {total}")
            for category, patterns in PATTERNS.items():
                print(f"{category}: {len(patterns)} patterns")
        else:
            print("Usage:")
            print("  python index.py           # Show all patterns")
            print("  python index.py search <keyword>  # Search patterns")
            print("  python index.py count     # Count patterns")
    else:
        print_index()


if __name__ == "__main__":
    main()
