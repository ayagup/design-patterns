package architectural;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * EVENT SOURCING PATTERN
 * 
 * Stores the state of an application as a sequence of events rather than just
 * the current state. Every change to the application state is captured as an event.
 * 
 * Benefits:
 * - Complete audit trail
 * - Temporal queries (state at any point in time)
 * - Event replay for debugging
 * - Natural fit for event-driven architectures
 * - Simplified concurrency through optimistic locking
 * 
 * Use Cases:
 * - Financial systems (account transactions)
 * - Audit-heavy applications
 * - Collaborative applications
 * - Systems requiring time-travel debugging
 * - Event-driven microservices
 */

// Base Event
abstract class DomainEvent {
    private final String eventId;
    private final String aggregateId;
    private final long timestamp;
    private final long version;
    
    public DomainEvent(String aggregateId, long version) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.timestamp = System.currentTimeMillis();
        this.version = version;
    }
    
    public String getEventId() { return eventId; }
    public String getAggregateId() { return aggregateId; }
    public long getTimestamp() { return timestamp; }
    public long getVersion() { return version; }
    
    public abstract String getEventType();
}

// Event Store
class EventStore {
    private final Map<String, List<DomainEvent>> events = new ConcurrentHashMap<>();
    
    public void saveEvent(DomainEvent event) {
        events.computeIfAbsent(event.getAggregateId(), k -> new ArrayList<>())
            .add(event);
        System.out.println("  [EVENT STORE] Saved: " + event.getEventType() + 
            " v" + event.getVersion() + " for " + event.getAggregateId());
    }
    
    public List<DomainEvent> getEvents(String aggregateId) {
        return new ArrayList<>(events.getOrDefault(aggregateId, Collections.emptyList()));
    }
    
    public List<DomainEvent> getEventsAfterVersion(String aggregateId, long version) {
        return events.getOrDefault(aggregateId, Collections.emptyList())
            .stream()
            .filter(e -> e.getVersion() > version)
            .toList();
    }
    
    public int getEventCount(String aggregateId) {
        return events.getOrDefault(aggregateId, Collections.emptyList()).size();
    }
}

// Example 1: Bank Account Events
class AccountCreatedEvent extends DomainEvent {
    private final String accountNumber;
    private final String owner;
    private final double initialBalance;
    
    public AccountCreatedEvent(String aggregateId, long version, String accountNumber, 
                              String owner, double initialBalance) {
        super(aggregateId, version);
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.initialBalance = initialBalance;
    }
    
    @Override
    public String getEventType() { return "AccountCreated"; }
    
    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    public double getInitialBalance() { return initialBalance; }
}

class MoneyDepositedEvent extends DomainEvent {
    private final double amount;
    private final String description;
    
    public MoneyDepositedEvent(String aggregateId, long version, double amount, String description) {
        super(aggregateId, version);
        this.amount = amount;
        this.description = description;
    }
    
    @Override
    public String getEventType() { return "MoneyDeposited"; }
    
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
}

class MoneyWithdrawnEvent extends DomainEvent {
    private final double amount;
    private final String description;
    
    public MoneyWithdrawnEvent(String aggregateId, long version, double amount, String description) {
        super(aggregateId, version);
        this.amount = amount;
        this.description = description;
    }
    
    @Override
    public String getEventType() { return "MoneyWithdrawn"; }
    
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
}

// Aggregate Root
class BankAccount {
    private String id;
    private String accountNumber;
    private String owner;
    private double balance;
    private long version = 0;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    // For event sourcing
    private BankAccount() {}
    
    // Create new account
    public static BankAccount create(String accountNumber, String owner, double initialBalance) {
        BankAccount account = new BankAccount();
        account.id = UUID.randomUUID().toString();
        
        AccountCreatedEvent event = new AccountCreatedEvent(
            account.id, 0, accountNumber, owner, initialBalance
        );
        
        account.applyEvent(event);
        account.uncommittedEvents.add(event);
        
        return account;
    }
    
    // Reconstruct from events
    public static BankAccount fromEvents(List<DomainEvent> events) {
        BankAccount account = new BankAccount();
        
        for (DomainEvent event : events) {
            account.applyEvent(event);
        }
        
        return account;
    }
    
    public void deposit(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        MoneyDepositedEvent event = new MoneyDepositedEvent(
            id, version + 1, amount, description
        );
        
        applyEvent(event);
        uncommittedEvents.add(event);
    }
    
    public void withdraw(double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        if (balance < amount) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        MoneyWithdrawnEvent event = new MoneyWithdrawnEvent(
            id, version + 1, amount, description
        );
        
        applyEvent(event);
        uncommittedEvents.add(event);
    }
    
    private void applyEvent(DomainEvent event) {
        if (event instanceof AccountCreatedEvent e) {
            this.id = e.getAggregateId();
            this.accountNumber = e.getAccountNumber();
            this.owner = e.getOwner();
            this.balance = e.getInitialBalance();
            this.version = e.getVersion();
        } else if (event instanceof MoneyDepositedEvent e) {
            this.balance += e.getAmount();
            this.version = e.getVersion();
        } else if (event instanceof MoneyWithdrawnEvent e) {
            this.balance -= e.getAmount();
            this.version = e.getVersion();
        }
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }
    
    // Getters
    public String getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    public double getBalance() { return balance; }
    public long getVersion() { return version; }
    
    @Override
    public String toString() {
        return String.format("BankAccount[number=%s, owner=%s, balance=$%.2f, version=%d]",
            accountNumber, owner, balance, version);
    }
}

// Repository with Event Sourcing
class BankAccountRepository {
    private final EventStore eventStore;
    
    public BankAccountRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    public void save(BankAccount account) {
        for (DomainEvent event : account.getUncommittedEvents()) {
            eventStore.saveEvent(event);
        }
        account.clearUncommittedEvents();
    }
    
    public BankAccount findById(String id) {
        List<DomainEvent> events = eventStore.getEvents(id);
        if (events.isEmpty()) {
            return null;
        }
        return BankAccount.fromEvents(events);
    }
    
    public BankAccount findByIdAtVersion(String id, long version) {
        List<DomainEvent> events = eventStore.getEvents(id).stream()
            .filter(e -> e.getVersion() <= version)
            .toList();
        
        if (events.isEmpty()) {
            return null;
        }
        
        return BankAccount.fromEvents(events);
    }
}

// Example 2: Shopping Cart Events
class CartCreatedEvent extends DomainEvent {
    private final String userId;
    
    public CartCreatedEvent(String aggregateId, long version, String userId) {
        super(aggregateId, version);
        this.userId = userId;
    }
    
    @Override
    public String getEventType() { return "CartCreated"; }
    
    public String getUserId() { return userId; }
}

class ItemAddedToCartEvent extends DomainEvent {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double price;
    
    public ItemAddedToCartEvent(String aggregateId, long version, String productId, 
                               String productName, int quantity, double price) {
        super(aggregateId, version);
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
    
    @Override
    public String getEventType() { return "ItemAddedToCart"; }
    
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}

class ItemRemovedFromCartEvent extends DomainEvent {
    private final String productId;
    
    public ItemRemovedFromCartEvent(String aggregateId, long version, String productId) {
        super(aggregateId, version);
        this.productId = productId;
    }
    
    @Override
    public String getEventType() { return "ItemRemovedFromCart"; }
    
    public String getProductId() { return productId; }
}

class ShoppingCart {
    private String id;
    private String userId;
    private Map<String, CartItem> items = new HashMap<>();
    private long version = 0;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    private ShoppingCart() {}
    
    public static ShoppingCart create(String userId) {
        ShoppingCart cart = new ShoppingCart();
        cart.id = UUID.randomUUID().toString();
        
        CartCreatedEvent event = new CartCreatedEvent(cart.id, 0, userId);
        cart.applyEvent(event);
        cart.uncommittedEvents.add(event);
        
        return cart;
    }
    
    public static ShoppingCart fromEvents(List<DomainEvent> events) {
        ShoppingCart cart = new ShoppingCart();
        for (DomainEvent event : events) {
            cart.applyEvent(event);
        }
        return cart;
    }
    
    public void addItem(String productId, String productName, int quantity, double price) {
        ItemAddedToCartEvent event = new ItemAddedToCartEvent(
            id, version + 1, productId, productName, quantity, price
        );
        
        applyEvent(event);
        uncommittedEvents.add(event);
    }
    
    public void removeItem(String productId) {
        if (!items.containsKey(productId)) {
            throw new IllegalArgumentException("Item not in cart");
        }
        
        ItemRemovedFromCartEvent event = new ItemRemovedFromCartEvent(
            id, version + 1, productId
        );
        
        applyEvent(event);
        uncommittedEvents.add(event);
    }
    
    private void applyEvent(DomainEvent event) {
        if (event instanceof CartCreatedEvent e) {
            this.id = e.getAggregateId();
            this.userId = e.getUserId();
            this.version = e.getVersion();
        } else if (event instanceof ItemAddedToCartEvent e) {
            CartItem item = items.get(e.getProductId());
            if (item != null) {
                item.quantity += e.getQuantity();
            } else {
                items.put(e.getProductId(), new CartItem(e.getProductId(), 
                    e.getProductName(), e.getQuantity(), e.getPrice()));
            }
            this.version = e.getVersion();
        } else if (event instanceof ItemRemovedFromCartEvent e) {
            items.remove(e.getProductId());
            this.version = e.getVersion();
        }
    }
    
    public double getTotal() {
        return items.values().stream()
            .mapToDouble(item -> item.price * item.quantity)
            .sum();
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }
    
    public String getId() { return id; }
    public Collection<CartItem> getItems() { return items.values(); }
    
    static class CartItem {
        String productId;
        String productName;
        int quantity;
        double price;
        
        CartItem(String productId, String productName, int quantity, double price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
        
        @Override
        public String toString() {
            return String.format("%s x%d @ $%.2f", productName, quantity, price);
        }
    }
}

// Demo
public class EventSourcingPattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   EVENT SOURCING PATTERN DEMONSTRATION   ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        EventStore eventStore = new EventStore();
        BankAccountRepository repository = new BankAccountRepository(eventStore);
        
        // Example 1: Bank Account Event Sourcing
        System.out.println("1. BANK ACCOUNT EVENT SOURCING");
        System.out.println("───────────────────────────────");
        
        System.out.println("Creating account:");
        BankAccount account = BankAccount.create("ACC-001", "Alice", 1000.0);
        repository.save(account);
        System.out.println(account);
        
        System.out.println("\nPerforming transactions:");
        account.deposit(500.0, "Salary");
        account.withdraw(200.0, "Groceries");
        account.deposit(300.0, "Freelance payment");
        account.withdraw(100.0, "Utilities");
        repository.save(account);
        
        System.out.println("Current state: " + account);
        System.out.println("Event count: " + eventStore.getEventCount(account.getId()));
        
        // Example 2: Reconstructing from Events
        System.out.println("\n2. RECONSTRUCTING STATE FROM EVENTS");
        System.out.println("───────────────────────────────────────");
        
        System.out.println("Loading account from event store:");
        BankAccount reconstructed = repository.findById(account.getId());
        System.out.println("Reconstructed: " + reconstructed);
        
        // Example 3: Temporal Queries
        System.out.println("\n3. TEMPORAL QUERIES (Time Travel)");
        System.out.println("──────────────────────────────────");
        
        System.out.println("Account state at different versions:");
        for (long v = 0; v <= account.getVersion(); v++) {
            BankAccount atVersion = repository.findByIdAtVersion(account.getId(), v);
            System.out.println("  Version " + v + ": Balance = $" + 
                String.format("%.2f", atVersion.getBalance()));
        }
        
        // Example 4: Event History
        System.out.println("\n4. EVENT HISTORY (Audit Trail)");
        System.out.println("───────────────────────────────");
        
        List<DomainEvent> history = eventStore.getEvents(account.getId());
        System.out.println("Complete event history:");
        for (DomainEvent event : history) {
            System.out.print("  v" + event.getVersion() + ": " + event.getEventType());
            
            if (event instanceof MoneyDepositedEvent e) {
                System.out.println(" - $" + e.getAmount() + " (" + e.getDescription() + ")");
            } else if (event instanceof MoneyWithdrawnEvent e) {
                System.out.println(" - $" + e.getAmount() + " (" + e.getDescription() + ")");
            } else if (event instanceof AccountCreatedEvent e) {
                System.out.println(" - Initial: $" + e.getInitialBalance());
            } else {
                System.out.println();
            }
        }
        
        // Example 5: Shopping Cart Event Sourcing
        System.out.println("\n5. SHOPPING CART EVENT SOURCING");
        System.out.println("────────────────────────────────");
        
        System.out.println("Creating cart:");
        ShoppingCart cart = ShoppingCart.create("user123");
        
        System.out.println("\nAdding items:");
        cart.addItem("P1", "Laptop", 1, 999.99);
        cart.addItem("P2", "Mouse", 2, 29.99);
        cart.addItem("P3", "Keyboard", 1, 79.99);
        
        System.out.println("\nCart contents:");
        cart.getItems().forEach(item -> System.out.println("  " + item));
        System.out.println("Total: $" + String.format("%.2f", cart.getTotal()));
        
        System.out.println("\nRemoving item:");
        cart.removeItem("P2");
        
        System.out.println("\nUpdated cart:");
        cart.getItems().forEach(item -> System.out.println("  " + item));
        System.out.println("Total: $" + String.format("%.2f", cart.getTotal()));
        
        System.out.println("\nSaving cart events:");
        for (DomainEvent event : cart.getUncommittedEvents()) {
            eventStore.saveEvent(event);
        }
        
        System.out.println("\n✅ Event Sourcing Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Complete audit trail of all changes");
        System.out.println("  • Ability to reconstruct state from events");
        System.out.println("  • Temporal queries (state at any point in time)");
        System.out.println("  • Event replay for debugging");
        System.out.println("  • Natural fit for event-driven architectures");
    }
}
