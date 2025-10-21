package behavioral;

/**
 * Null Object Pattern
 * Provides a default object to avoid null checks.
 */
public class NullObjectPattern {
    
    // Abstract class
    abstract static class Animal {
        public abstract void makeSound();
        public abstract String getName();
    }
    
    // Real objects
    static class Dog extends Animal {
        @Override
        public void makeSound() {
            System.out.println("🐕 Woof! Woof!");
        }
        
        @Override
        public String getName() {
            return "Dog";
        }
    }
    
    static class Cat extends Animal {
        @Override
        public void makeSound() {
            System.out.println("🐱 Meow!");
        }
        
        @Override
        public String getName() {
            return "Cat";
        }
    }
    
    // Null Object
    static class NullAnimal extends Animal {
        @Override
        public void makeSound() {
            // Do nothing
        }
        
        @Override
        public String getName() {
            return "No animal";
        }
    }
    
    // Factory
    static class AnimalFactory {
        public static Animal getAnimal(String type) {
            if ("dog".equalsIgnoreCase(type)) {
                return new Dog();
            } else if ("cat".equalsIgnoreCase(type)) {
                return new Cat();
            }
            return new NullAnimal();
        }
    }
    
    // Customer example
    interface Customer {
        String getName();
        boolean isNull();
        void displayInfo();
    }
    
    static class RealCustomer implements Customer {
        private String name;
        
        public RealCustomer(String name) {
            this.name = name;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public boolean isNull() {
            return false;
        }
        
        @Override
        public void displayInfo() {
            System.out.println("👤 Customer: " + name);
        }
    }
    
    static class NullCustomer implements Customer {
        @Override
        public String getName() {
            return "Guest";
        }
        
        @Override
        public boolean isNull() {
            return true;
        }
        
        @Override
        public void displayInfo() {
            System.out.println("👻 No customer information available");
        }
    }
    
    static class CustomerFactory {
        private static final String[] customers = {"Alice", "Bob", "Charlie"};
        
        public static Customer getCustomer(String name) {
            for (String customer : customers) {
                if (customer.equalsIgnoreCase(name)) {
                    return new RealCustomer(name);
                }
            }
            return new NullCustomer();
        }
    }
    
    // Logger example
    interface Logger {
        void log(String message);
    }
    
    static class ConsoleLogger implements Logger {
        @Override
        public void log(String message) {
            System.out.println("[LOG] " + message);
        }
    }
    
    static class NullLogger implements Logger {
        @Override
        public void log(String message) {
            // Do nothing - silent logger
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Null Object Pattern Demo ===\n");
        
        // Animal example
        System.out.println("1. Animal Sounds (Without Null Checks):");
        Animal animal1 = AnimalFactory.getAnimal("dog");
        Animal animal2 = AnimalFactory.getAnimal("cat");
        Animal animal3 = AnimalFactory.getAnimal("bird");
        
        // No null checks needed!
        System.out.println(animal1.getName() + ":");
        animal1.makeSound();
        
        System.out.println(animal2.getName() + ":");
        animal2.makeSound();
        
        System.out.println(animal3.getName() + ":");
        animal3.makeSound(); // Null object does nothing gracefully
        
        // Customer example
        System.out.println("\n2. Customer Lookup:");
        Customer customer1 = CustomerFactory.getCustomer("Alice");
        Customer customer2 = CustomerFactory.getCustomer("Bob");
        Customer customer3 = CustomerFactory.getCustomer("David");
        
        customer1.displayInfo();
        customer2.displayInfo();
        customer3.displayInfo(); // Null customer
        
        System.out.println("\nProcessing orders:");
        processOrder(customer1);
        processOrder(customer3);
        
        // Logger example
        System.out.println("\n3. Logger Example:");
        Logger activeLogger = new ConsoleLogger();
        Logger inactiveLogger = new NullLogger();
        
        System.out.println("With active logger:");
        activeLogger.log("Application started");
        activeLogger.log("User logged in");
        
        System.out.println("\nWith inactive logger (silent):");
        inactiveLogger.log("This message won't appear");
        inactiveLogger.log("Neither will this one");
        System.out.println("(No logs displayed above)");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Eliminates null checks");
        System.out.println("✓ Provides default behavior");
        System.out.println("✓ Reduces conditional complexity");
        System.out.println("✓ Follows polymorphism principles");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Avoiding NullPointerExceptions");
        System.out.println("• Providing default behaviors");
        System.out.println("• Simplifying client code");
        System.out.println("• Optional features that do nothing when disabled");
    }
    
    static void processOrder(Customer customer) {
        if (customer.isNull()) {
            System.out.println("⚠️  Cannot process order for " + customer.getName());
        } else {
            System.out.println("✅ Processing order for " + customer.getName());
        }
    }
}
