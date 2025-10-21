package enterprise;

import java.util.*;

/**
 * Unit of Work Pattern
 * Maintains a list of objects affected by a business transaction
 * and coordinates writing changes and resolving concurrency problems.
 */
public class UnitOfWorkPattern {
    
    // Entity base
    static abstract class Entity {
        protected String id;
        protected int version = 0;
        
        public String getId() { return id; }
        public int getVersion() { return version; }
        public void incrementVersion() { version++; }
    }
    
    // Domain Models
    static class User extends Entity {
        private String name;
        private String email;
        
        public User(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        @Override
        public String toString() {
            return "User[id=" + id + ", name=" + name + ", email=" + email + 
                   ", v" + version + "]";
        }
    }
    
    static class Order extends Entity {
        private String userId;
        private double total;
        private String status;
        
        public Order(String id, String userId, double total, String status) {
            this.id = id;
            this.userId = userId;
            this.total = total;
            this.status = status;
        }
        
        public String getUserId() { return userId; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        @Override
        public String toString() {
            return "Order[id=" + id + ", userId=" + userId + ", total=$" + 
                   total + ", status=" + status + ", v" + version + "]";
        }
    }
    
    // Unit of Work implementation
    static class UnitOfWork {
        private final List<Entity> newEntities = new ArrayList<>();
        private final List<Entity> modifiedEntities = new ArrayList<>();
        private final List<Entity> deletedEntities = new ArrayList<>();
        private final Map<String, Entity> identityMap = new HashMap<>();
        
        public void registerNew(Entity entity) {
            if (identityMap.containsKey(entity.getId())) {
                throw new IllegalStateException("Entity already registered: " + entity.getId());
            }
            newEntities.add(entity);
            identityMap.put(entity.getId(), entity);
            System.out.println("📝 Registered NEW: " + entity);
        }
        
        public void registerModified(Entity entity) {
            if (!identityMap.containsKey(entity.getId())) {
                throw new IllegalStateException("Entity not in unit of work: " + entity.getId());
            }
            if (!modifiedEntities.contains(entity) && !newEntities.contains(entity)) {
                modifiedEntities.add(entity);
                System.out.println("📝 Registered MODIFIED: " + entity);
            }
        }
        
        public void registerDeleted(Entity entity) {
            if (newEntities.contains(entity)) {
                newEntities.remove(entity);
            } else {
                if (!deletedEntities.contains(entity)) {
                    deletedEntities.add(entity);
                }
                modifiedEntities.remove(entity);
            }
            identityMap.remove(entity.getId());
            System.out.println("📝 Registered DELETED: " + entity);
        }
        
        public void commit() {
            System.out.println("\n🔄 Committing Unit of Work...");
            
            // Insert new entities
            if (!newEntities.isEmpty()) {
                System.out.println("\n✅ Inserting " + newEntities.size() + " new entities:");
                for (Entity entity : newEntities) {
                    entity.incrementVersion();
                    System.out.println("  INSERT: " + entity);
                }
            }
            
            // Update modified entities
            if (!modifiedEntities.isEmpty()) {
                System.out.println("\n✏️  Updating " + modifiedEntities.size() + " modified entities:");
                for (Entity entity : modifiedEntities) {
                    entity.incrementVersion();
                    System.out.println("  UPDATE: " + entity);
                }
            }
            
            // Delete entities
            if (!deletedEntities.isEmpty()) {
                System.out.println("\n🗑️  Deleting " + deletedEntities.size() + " entities:");
                for (Entity entity : deletedEntities) {
                    System.out.println("  DELETE: " + entity);
                }
            }
            
            // Clear tracking lists
            newEntities.clear();
            modifiedEntities.clear();
            deletedEntities.clear();
            
            System.out.println("\n✅ Transaction committed successfully!");
        }
        
        public void rollback() {
            System.out.println("\n⚠️  Rolling back Unit of Work...");
            newEntities.clear();
            modifiedEntities.clear();
            deletedEntities.clear();
            identityMap.clear();
            System.out.println("✅ Rollback complete");
        }
        
        public void printStatus() {
            System.out.println("\n📊 Unit of Work Status:");
            System.out.println("  New entities: " + newEntities.size());
            System.out.println("  Modified entities: " + modifiedEntities.size());
            System.out.println("  Deleted entities: " + deletedEntities.size());
            System.out.println("  Total tracked: " + identityMap.size());
        }
    }
    
    // Repository with Unit of Work
    static class UserRepository {
        private final UnitOfWork unitOfWork;
        private final Map<String, User> database = new HashMap<>();
        
        public UserRepository(UnitOfWork unitOfWork) {
            this.unitOfWork = unitOfWork;
        }
        
        public void add(User user) {
            unitOfWork.registerNew(user);
        }
        
        public User findById(String id) {
            return database.get(id);
        }
        
        public void update(User user) {
            unitOfWork.registerModified(user);
        }
        
        public void delete(User user) {
            unitOfWork.registerDeleted(user);
        }
    }
    
    static class OrderRepository {
        private final UnitOfWork unitOfWork;
        private final Map<String, Order> database = new HashMap<>();
        
        public OrderRepository(UnitOfWork unitOfWork) {
            this.unitOfWork = unitOfWork;
        }
        
        public void add(Order order) {
            unitOfWork.registerNew(order);
        }
        
        public Order findById(String id) {
            return database.get(id);
        }
        
        public void update(Order order) {
            unitOfWork.registerModified(order);
        }
        
        public void delete(Order order) {
            unitOfWork.registerDeleted(order);
        }
    }
    
    // Service using Unit of Work
    static class OrderService {
        private final UserRepository userRepository;
        private final OrderRepository orderRepository;
        private final UnitOfWork unitOfWork;
        
        public OrderService(UserRepository userRepo, OrderRepository orderRepo, 
                          UnitOfWork uow) {
            this.userRepository = userRepo;
            this.orderRepository = orderRepo;
            this.unitOfWork = uow;
        }
        
        public void createOrder(String userId, double amount) {
            try {
                System.out.println("\n🛒 Creating order for user " + userId);
                
                // Create user if new
                User user = new User(userId, "User " + userId, userId + "@example.com");
                userRepository.add(user);
                
                // Create order
                String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
                Order order = new Order(orderId, userId, amount, "PENDING");
                orderRepository.add(order);
                
                // Commit all changes together
                unitOfWork.commit();
                
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                unitOfWork.rollback();
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Unit of Work Pattern Demo ===\n");
        
        // 1. Basic Unit of Work operations
        System.out.println("1. Basic Unit of Work:");
        UnitOfWork uow = new UnitOfWork();
        
        User user1 = new User("U1", "Alice", "alice@example.com");
        User user2 = new User("U2", "Bob", "bob@example.com");
        Order order1 = new Order("O1", "U1", 99.99, "PENDING");
        
        uow.registerNew(user1);
        uow.registerNew(user2);
        uow.registerNew(order1);
        
        uow.printStatus();
        uow.commit();
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. Modifications and Deletions
        System.out.println("\n2. Modifications and Deletions:");
        UnitOfWork uow2 = new UnitOfWork();
        
        User user3 = new User("U3", "Charlie", "charlie@example.com");
        uow2.registerNew(user3);
        
        // Modify the user
        user3.setEmail("charlie.updated@example.com");
        uow2.registerModified(user3);
        
        // Delete user2
        uow2.registerNew(user2); // Re-register for demo
        uow2.registerDeleted(user2);
        
        uow2.printStatus();
        uow2.commit();
        
        System.out.println("\n" + "=".repeat(50));
        
        // 3. Transaction with repositories
        System.out.println("\n3. Transactional Service:");
        UnitOfWork uow3 = new UnitOfWork();
        UserRepository userRepo = new UserRepository(uow3);
        OrderRepository orderRepo = new OrderRepository(uow3);
        OrderService orderService = new OrderService(userRepo, orderRepo, uow3);
        
        orderService.createOrder("U100", 149.99);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 4. Rollback scenario
        System.out.println("\n4. Rollback Scenario:");
        UnitOfWork uow4 = new UnitOfWork();
        
        User user4 = new User("U4", "David", "david@example.com");
        Order order2 = new Order("O2", "U4", 199.99, "PENDING");
        
        uow4.registerNew(user4);
        uow4.registerNew(order2);
        
        uow4.printStatus();
        
        System.out.println("\n⚠️  Simulating error - rolling back...");
        uow4.rollback();
        
        uow4.printStatus();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ All-or-nothing transactions");
        System.out.println("✓ Optimizes database calls (batch operations)");
        System.out.println("✓ Maintains consistency");
        System.out.println("✓ Tracks changes automatically");
        System.out.println("✓ Simplifies transaction management");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Complex business transactions");
        System.out.println("• Multi-entity updates");
        System.out.println("• ORM implementations (Hibernate, EF)");
        System.out.println("• Ensuring data consistency");
    }
}
