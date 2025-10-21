package enterprise;

import java.util.*;

/**
 * Repository Pattern
 * Encapsulates data access logic and provides a collection-like interface.
 */
public class RepositoryPattern {
    
    // Domain Model
    static class User {
        private final String id;
        private String name;
        private String email;
        private int age;
        
        public User(String id, String name, String email, int age) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.age = age;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        @Override
        public String toString() {
            return String.format("User[id=%s, name=%s, email=%s, age=%d]", 
                               id, name, email, age);
        }
    }
    
    // Repository Interface
    interface Repository<T, ID> {
        void save(T entity);
        Optional<T> findById(ID id);
        List<T> findAll();
        void update(T entity);
        void delete(ID id);
        boolean exists(ID id);
    }
    
    // User Repository Interface with custom queries
    interface UserRepository extends Repository<User, String> {
        List<User> findByName(String name);
        List<User> findByAgeGreaterThan(int age);
        Optional<User> findByEmail(String email);
    }
    
    // In-Memory Implementation
    static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> storage = new HashMap<>();
        
        @Override
        public void save(User user) {
            storage.put(user.getId(), user);
            System.out.println("✅ Saved: " + user);
        }
        
        @Override
        public Optional<User> findById(String id) {
            System.out.println("🔍 Finding user by ID: " + id);
            return Optional.ofNullable(storage.get(id));
        }
        
        @Override
        public List<User> findAll() {
            System.out.println("🔍 Finding all users");
            return new ArrayList<>(storage.values());
        }
        
        @Override
        public void update(User user) {
            if (storage.containsKey(user.getId())) {
                storage.put(user.getId(), user);
                System.out.println("✏️  Updated: " + user);
            } else {
                System.out.println("❌ User not found: " + user.getId());
            }
        }
        
        @Override
        public void delete(String id) {
            if (storage.remove(id) != null) {
                System.out.println("🗑️  Deleted user: " + id);
            } else {
                System.out.println("❌ User not found: " + id);
            }
        }
        
        @Override
        public boolean exists(String id) {
            return storage.containsKey(id);
        }
        
        @Override
        public List<User> findByName(String name) {
            System.out.println("🔍 Finding users by name: " + name);
            return storage.values().stream()
                .filter(u -> u.getName().equalsIgnoreCase(name))
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public List<User> findByAgeGreaterThan(int age) {
            System.out.println("🔍 Finding users older than: " + age);
            return storage.values().stream()
                .filter(u -> u.getAge() > age)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public Optional<User> findByEmail(String email) {
            System.out.println("🔍 Finding user by email: " + email);
            return storage.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
        }
    }
    
    // Product domain model
    static class Product {
        private final String id;
        private String name;
        private double price;
        private String category;
        
        public Product(String id, String name, double price, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.category = category;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        
        @Override
        public String toString() {
            return String.format("Product[id=%s, name=%s, price=$%.2f, category=%s]", 
                               id, name, price, category);
        }
    }
    
    // Product Repository
    static class ProductRepository implements Repository<Product, String> {
        private final Map<String, Product> storage = new HashMap<>();
        
        @Override
        public void save(Product product) {
            storage.put(product.getId(), product);
            System.out.println("✅ Saved: " + product);
        }
        
        @Override
        public Optional<Product> findById(String id) {
            return Optional.ofNullable(storage.get(id));
        }
        
        @Override
        public List<Product> findAll() {
            return new ArrayList<>(storage.values());
        }
        
        @Override
        public void update(Product product) {
            storage.put(product.getId(), product);
        }
        
        @Override
        public void delete(String id) {
            storage.remove(id);
        }
        
        @Override
        public boolean exists(String id) {
            return storage.containsKey(id);
        }
        
        public List<Product> findByCategory(String category) {
            System.out.println("🔍 Finding products in category: " + category);
            return storage.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(java.util.stream.Collectors.toList());
        }
        
        public List<Product> findByPriceRange(double min, double max) {
            System.out.println("🔍 Finding products between $" + min + " and $" + max);
            return storage.values().stream()
                .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                .collect(java.util.stream.Collectors.toList());
        }
    }
    
    // Service Layer using Repository
    static class UserService {
        private final UserRepository userRepository;
        
        public UserService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
        
        public void registerUser(String name, String email, int age) {
            String id = UUID.randomUUID().toString().substring(0, 8);
            User user = new User(id, name, email, age);
            userRepository.save(user);
        }
        
        public void updateUserEmail(String userId, String newEmail) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEmail(newEmail);
                userRepository.update(user);
            }
        }
        
        public List<User> getAdultUsers() {
            return userRepository.findByAgeGreaterThan(17);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Repository Pattern Demo ===\n");
        
        // 1. User Repository CRUD operations
        System.out.println("1. User Repository - CRUD Operations:");
        UserRepository userRepo = new InMemoryUserRepository();
        
        // Create
        userRepo.save(new User("1", "Alice", "alice@example.com", 25));
        userRepo.save(new User("2", "Bob", "bob@example.com", 30));
        userRepo.save(new User("3", "Charlie", "charlie@example.com", 35));
        
        System.out.println();
        
        // Read
        Optional<User> user = userRepo.findById("1");
        user.ifPresent(u -> System.out.println("Found: " + u));
        
        System.out.println("\nAll users:");
        userRepo.findAll().forEach(System.out::println);
        
        System.out.println();
        
        // Update
        user.ifPresent(u -> {
            u.setAge(26);
            userRepo.update(u);
        });
        
        System.out.println();
        
        // Delete
        userRepo.delete("2");
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 2. Custom Queries
        System.out.println("2. Custom Query Methods:");
        userRepo.save(new User("4", "Alice", "alice2@example.com", 28));
        
        System.out.println("\nUsers named 'Alice':");
        userRepo.findByName("Alice").forEach(System.out::println);
        
        System.out.println("\nUsers older than 25:");
        userRepo.findByAgeGreaterThan(25).forEach(System.out::println);
        
        System.out.println();
        Optional<User> byEmail = userRepo.findByEmail("charlie@example.com");
        byEmail.ifPresent(u -> System.out.println("Found by email: " + u));
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 3. Product Repository
        System.out.println("3. Product Repository:");
        ProductRepository productRepo = new ProductRepository();
        
        productRepo.save(new Product("P1", "Laptop", 999.99, "Electronics"));
        productRepo.save(new Product("P2", "Mouse", 29.99, "Electronics"));
        productRepo.save(new Product("P3", "Desk", 299.99, "Furniture"));
        productRepo.save(new Product("P4", "Chair", 199.99, "Furniture"));
        
        System.out.println("\nElectronics:");
        productRepo.findByCategory("Electronics").forEach(System.out::println);
        
        System.out.println("\nProducts between $50 and $500:");
        productRepo.findByPriceRange(50, 500).forEach(System.out::println);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 4. Service Layer with Repository
        System.out.println("4. Service Layer using Repository:");
        UserService userService = new UserService(userRepo);
        
        userService.registerUser("David", "david@example.com", 22);
        System.out.println();
        
        System.out.println("Adult users (age > 17):");
        userService.getAdultUsers().forEach(System.out::println);
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Separation of concerns");
        System.out.println("✓ Centralized data access logic");
        System.out.println("✓ Easy to test (mock repositories)");
        System.out.println("✓ Database-agnostic code");
        System.out.println("✓ Consistent API across entities");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Data access layer in applications");
        System.out.println("• Domain-Driven Design (DDD)");
        System.out.println("• Testing with mock data");
        System.out.println("• Switching between data sources");
    }
}
