package additional;

import java.util.*;
import java.util.function.Supplier;

/**
 * Revealing Module Pattern
 * 
 * Intent: Exposes only a selected public API from a module while keeping
 * all other implementation details private. Creates a clear separation
 * between public and private members.
 * 
 * Motivation:
 * Makes public API explicit and obvious.
 * Hides implementation complexity.
 * Controls what's exposed to clients.
 * Improves maintainability.
 * 
 * Applicability:
 * - Clear API definition needed
 * - Hide implementation details
 * - Library/framework development
 * - Complex internal logic
 */

/**
 * Example 1: Calculator Module
 * 
 * Exposes only calculation methods
 */
class CalculatorModule {
    // Private implementation
    private static class Implementation {
        private int operationCount = 0;
        private final List<String> history = new ArrayList<>();
        
        private void recordOperation(String operation) {
            operationCount++;
            history.add(operation);
        }
        
        double performAdd(double a, double b) {
            double result = a + b;
            recordOperation(a + " + " + b + " = " + result);
            return result;
        }
        
        double performSubtract(double a, double b) {
            double result = a - b;
            recordOperation(a + " - " + b + " = " + result);
            return result;
        }
        
        double performMultiply(double a, double b) {
            double result = a * b;
            recordOperation(a + " * " + b + " = " + result);
            return result;
        }
        
        double performDivide(double a, double b) {
            if (b == 0) throw new ArithmeticException("Division by zero");
            double result = a / b;
            recordOperation(a + " / " + b + " = " + result);
            return result;
        }
    }
    
    // Singleton instance
    private static final Implementation impl = new Implementation();
    
    // Public API - revealing only these methods
    public static double add(double a, double b) {
        double result = impl.performAdd(a, b);
        System.out.println("  [Calculator] " + a + " + " + b + " = " + result);
        return result;
    }
    
    public static double subtract(double a, double b) {
        double result = impl.performSubtract(a, b);
        System.out.println("  [Calculator] " + a + " - " + b + " = " + result);
        return result;
    }
    
    public static double multiply(double a, double b) {
        double result = impl.performMultiply(a, b);
        System.out.println("  [Calculator] " + a + " * " + b + " = " + result);
        return result;
    }
    
    public static double divide(double a, double b) {
        double result = impl.performDivide(a, b);
        System.out.println("  [Calculator] " + a + " / " + b + " = " + result);
        return result;
    }
    
    public static int getOperationCount() {
        return impl.operationCount;
    }
    
    // Note: history is NOT exposed - it's private!
}

/**
 * Example 2: User Manager Module
 * 
 * Reveals only user management operations
 */
class UserManagerModule {
    // Public interface - what we reveal
    public interface API {
        boolean createUser(String username, String password);
        boolean authenticate(String username, String password);
        boolean deleteUser(String username);
        int getUserCount();
    }
    
    // Private implementation
    private static class Implementation implements API {
        private final Map<String, UserRecord> users = new HashMap<>();
        
        private static class UserRecord {
            String username;
            String passwordHash;
            long createdAt;
            
            UserRecord(String username, String passwordHash) {
                this.username = username;
                this.passwordHash = passwordHash;
                this.createdAt = System.currentTimeMillis();
            }
        }
        
        private String hashPassword(String password) {
            // Simple hash (not secure - for demo only!)
            return "HASH_" + password.hashCode();
        }
        
        private boolean isValidUsername(String username) {
            return username != null && username.length() >= 3;
        }
        
        @Override
        public boolean createUser(String username, String password) {
            if (!isValidUsername(username)) {
                System.out.println("  [UserManager] Invalid username");
                return false;
            }
            
            if (users.containsKey(username)) {
                System.out.println("  [UserManager] User already exists");
                return false;
            }
            
            String hash = hashPassword(password);
            users.put(username, new UserRecord(username, hash));
            System.out.println("  [UserManager] Created user: " + username);
            return true;
        }
        
        @Override
        public boolean authenticate(String username, String password) {
            UserRecord user = users.get(username);
            if (user == null) {
                System.out.println("  [UserManager] User not found");
                return false;
            }
            
            String hash = hashPassword(password);
            boolean authenticated = user.passwordHash.equals(hash);
            System.out.println("  [UserManager] Authentication: " + authenticated);
            return authenticated;
        }
        
        @Override
        public boolean deleteUser(String username) {
            UserRecord removed = users.remove(username);
            if (removed != null) {
                System.out.println("  [UserManager] Deleted user: " + username);
                return true;
            }
            return false;
        }
        
        @Override
        public int getUserCount() {
            return users.size();
        }
    }
    
    // Revealed API - singleton instance
    private static final API instance = new Implementation();
    
    public static boolean createUser(String username, String password) {
        return instance.createUser(username, password);
    }
    
    public static boolean authenticate(String username, String password) {
        return instance.authenticate(username, password);
    }
    
    public static boolean deleteUser(String username) {
        return instance.deleteUser(username);
    }
    
    public static int getUserCount() {
        return instance.getUserCount();
    }
}

/**
 * Example 3: Shopping Cart Module
 * 
 * Reveals cart operations, hides internal storage
 */
class RevealingCartModule {
    // Private implementation
    private final List<Item> items = new ArrayList<>();
    private double taxRate = 0.08;
    
    private static class Item {
        String name;
        double price;
        int quantity;
        
        Item(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        
        double getSubtotal() {
            return price * quantity;
        }
    }
    
    private double calculateSubtotal() {
        return items.stream().mapToDouble(Item::getSubtotal).sum();
    }
    
    private double calculateTax(double subtotal) {
        return subtotal * taxRate;
    }
    
    private Item findItem(String name) {
        return items.stream()
            .filter(item -> item.name.equals(name))
            .findFirst()
            .orElse(null);
    }
    
    // Public API - only these are revealed
    public void addItem(String name, double price, int quantity) {
        Item existing = findItem(name);
        if (existing != null) {
            existing.quantity += quantity;
            System.out.println("  [Cart] Updated quantity for: " + name);
        } else {
            items.add(new Item(name, price, quantity));
            System.out.println("  [Cart] Added: " + name);
        }
    }
    
    public boolean removeItem(String name) {
        boolean removed = items.removeIf(item -> item.name.equals(name));
        if (removed) {
            System.out.println("  [Cart] Removed: " + name);
        }
        return removed;
    }
    
    public double getTotal() {
        double subtotal = calculateSubtotal();
        double tax = calculateTax(subtotal);
        double total = subtotal + tax;
        System.out.println("  [Cart] Subtotal: $" + String.format("%.2f", subtotal));
        System.out.println("  [Cart] Tax: $" + String.format("%.2f", tax));
        System.out.println("  [Cart] Total: $" + String.format("%.2f", total));
        return total;
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    public void clear() {
        items.clear();
        System.out.println("  [Cart] Cleared");
    }
}

/**
 * Example 4: Data Store Module
 * 
 * Reveals CRUD operations, hides storage
 */
class DataStoreModule<T> {
    // Private state
    private final Map<String, T> storage = new HashMap<>();
    private final List<ChangeListener<T>> listeners = new ArrayList<>();
    
    private interface ChangeListener<T> {
        void onChange(String key, T value);
    }
    
    private void notifyListeners(String key, T value) {
        listeners.forEach(listener -> listener.onChange(key, value));
    }
    
    private boolean validateKey(String key) {
        return key != null && !key.isEmpty();
    }
    
    // Revealed API
    public void save(String key, T value) {
        if (!validateKey(key)) {
            System.out.println("  [DataStore] Invalid key");
            return;
        }
        
        storage.put(key, value);
        notifyListeners(key, value);
        System.out.println("  [DataStore] Saved: " + key);
    }
    
    public T get(String key) {
        T value = storage.get(key);
        System.out.println("  [DataStore] Retrieved: " + key + 
                         (value != null ? " (found)" : " (not found)"));
        return value;
    }
    
    public boolean delete(String key) {
        T removed = storage.remove(key);
        if (removed != null) {
            System.out.println("  [DataStore] Deleted: " + key);
            return true;
        }
        return false;
    }
    
    public Set<String> keys() {
        return new HashSet<>(storage.keySet());
    }
    
    public int size() {
        return storage.size();
    }
}

/**
 * Example 5: Counter Module
 * 
 * Reveals increment/decrement, hides value storage
 */
class CounterModule {
    // Private builder for creating counter instances
    private static class CounterImpl {
        private int value;
        private final int min;
        private final int max;
        private final int step;
        
        CounterImpl(int initial, int min, int max, int step) {
            this.value = initial;
            this.min = min;
            this.max = max;
            this.step = step;
        }
        
        int increment() {
            if (value + step <= max) {
                value += step;
            }
            return value;
        }
        
        int decrement() {
            if (value - step >= min) {
                value -= step;
            }
            return value;
        }
        
        void reset(int newValue) {
            if (newValue >= min && newValue <= max) {
                value = newValue;
            }
        }
    }
    
    // Public factory method
    public static Counter create(int initial) {
        return new Counter(new CounterImpl(initial, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    }
    
    public static Counter create(int initial, int min, int max, int step) {
        return new Counter(new CounterImpl(initial, min, max, step));
    }
    
    // Public wrapper class - reveals only these methods
    public static class Counter {
        private final CounterImpl impl;
        
        private Counter(CounterImpl impl) {
            this.impl = impl;
        }
        
        public int increment() {
            int newValue = impl.increment();
            System.out.println("  [Counter] Incremented to: " + newValue);
            return newValue;
        }
        
        public int decrement() {
            int newValue = impl.decrement();
            System.out.println("  [Counter] Decremented to: " + newValue);
            return newValue;
        }
        
        public int getValue() {
            return impl.value;
        }
        
        public void reset(int value) {
            impl.reset(value);
            System.out.println("  [Counter] Reset to: " + impl.value);
        }
    }
}

/**
 * Demonstration of the Revealing Module Pattern
 */
public class RevealingModulePattern {
    
    public static void main(String[] args) {
        System.out.println("=== Revealing Module Pattern Demo ===\n");
        
        // Example 1: Calculator Module
        System.out.println("1. Calculator Module:");
        CalculatorModule.add(10, 5);
        CalculatorModule.multiply(3, 7);
        CalculatorModule.divide(20, 4);
        System.out.println("  Operations performed: " + CalculatorModule.getOperationCount());
        // Note: Cannot access history - it's private!
        
        // Example 2: User Manager Module
        System.out.println("\n2. User Manager Module:");
        UserManagerModule.createUser("alice", "password123");
        UserManagerModule.createUser("bob", "secret456");
        UserManagerModule.authenticate("alice", "password123");
        UserManagerModule.authenticate("alice", "wrongpassword");
        System.out.println("  Total users: " + UserManagerModule.getUserCount());
        
        // Example 3: Shopping Cart Module
        System.out.println("\n3. Shopping Cart Module:");
        RevealingCartModule cart = new RevealingCartModule();
        cart.addItem("Laptop", 999.99, 1);
        cart.addItem("Mouse", 29.99, 2);
        cart.getTotal();
        
        // Example 4: Data Store Module
        System.out.println("\n4. Data Store Module:");
        DataStoreModule<String> store = new DataStoreModule<>();
        store.save("user:1", "Alice");
        store.save("user:2", "Bob");
        store.get("user:1");
        System.out.println("  Keys in store: " + store.keys());
        
        // Example 5: Counter Module
        System.out.println("\n5. Counter Module:");
        CounterModule.Counter counter1 = CounterModule.create(0);
        counter1.increment();
        counter1.increment();
        counter1.decrement();
        
        CounterModule.Counter counter2 = CounterModule.create(10, 0, 100, 5);
        counter2.increment();
        counter2.increment();
        System.out.println("  Counter value: " + counter2.getValue());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Explicit public API");
        System.out.println("✓ Implementation hiding");
        System.out.println("✓ Clear separation of concerns");
        System.out.println("✓ Easy to understand interface");
        System.out.println("✓ Improved maintainability");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Library/framework APIs");
        System.out.println("• Service modules");
        System.out.println("• Utility classes");
        System.out.println("• Data management");
        System.out.println("• Complex internal logic");
    }
}
