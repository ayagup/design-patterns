package architectural;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CQRS PATTERN (Command Query Responsibility Segregation)
 * 
 * Separates read and write operations into different models.
 * Commands modify state, queries retrieve state without side effects.
 * 
 * Benefits:
 * - Independent scaling of reads and writes
 * - Optimized data models for queries
 * - Simplified domain logic
 * - Enhanced security
 * - Better performance for complex domains
 * 
 * Use Cases:
 * - High-performance applications with different read/write loads
 * - Complex business logic domains
 * - Event-sourced systems
 * - Microservices architectures
 * - Systems requiring audit trails
 */

// Command side - Writes
interface Command {
    void execute();
}

interface CommandHandler<T extends Command> {
    void handle(T command);
}

// Query side - Reads
interface Query<R> {
    R execute();
}

interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}

// Example 1: E-commerce Product Management

// Write Model (Command Side)
class Product {
    private final String id;
    private String name;
    private double price;
    private int stock;
    private LocalDateTime lastUpdated;
    
    public Product(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void updatePrice(double newPrice) {
        this.price = newPrice;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void updateStock(int quantity) {
        this.stock += quantity;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}

// Commands
class CreateProductCommand implements Command {
    private final String id;
    private final String name;
    private final double price;
    private final int stock;
    
    public CreateProductCommand(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
    
    @Override
    public void execute() {
        System.out.println("Creating product: " + name);
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
}

class UpdatePriceCommand implements Command {
    private final String productId;
    private final double newPrice;
    
    public UpdatePriceCommand(String productId, double newPrice) {
        this.productId = productId;
        this.newPrice = newPrice;
    }
    
    @Override
    public void execute() {
        System.out.println("Updating price for product: " + productId);
    }
    
    public String getProductId() { return productId; }
    public double getNewPrice() { return newPrice; }
}

class UpdateStockCommand implements Command {
    private final String productId;
    private final int quantity;
    
    public UpdateStockCommand(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    @Override
    public void execute() {
        System.out.println("Updating stock for product: " + productId);
    }
    
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}

// Command Handlers
class ProductCommandHandler implements CommandHandler<CreateProductCommand> {
    private final Map<String, Product> writeStore;
    private final ProductReadModelUpdater readModelUpdater;
    
    public ProductCommandHandler(Map<String, Product> writeStore, ProductReadModelUpdater updater) {
        this.writeStore = writeStore;
        this.readModelUpdater = updater;
    }
    
    @Override
    public void handle(CreateProductCommand command) {
        command.execute();
        
        Product product = new Product(
            command.getId(),
            command.getName(),
            command.getPrice(),
            command.getStock()
        );
        
        writeStore.put(command.getId(), product);
        readModelUpdater.onProductCreated(product);
        
        System.out.println("✅ Product created: " + command.getId());
    }
}

class UpdatePriceCommandHandler implements CommandHandler<UpdatePriceCommand> {
    private final Map<String, Product> writeStore;
    private final ProductReadModelUpdater readModelUpdater;
    
    public UpdatePriceCommandHandler(Map<String, Product> writeStore, ProductReadModelUpdater updater) {
        this.writeStore = writeStore;
        this.readModelUpdater = updater;
    }
    
    @Override
    public void handle(UpdatePriceCommand command) {
        command.execute();
        
        Product product = writeStore.get(command.getProductId());
        if (product != null) {
            product.updatePrice(command.getNewPrice());
            readModelUpdater.onProductPriceUpdated(product);
            System.out.println("✅ Price updated for product: " + command.getProductId());
        }
    }
}

// Read Model (Query Side)
class ProductReadModel {
    private String id;
    private String name;
    private double price;
    private int stock;
    private boolean inStock;
    private String priceCategory;
    
    public ProductReadModel(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.inStock = product.getStock() > 0;
        this.priceCategory = categorizePrice(product.getPrice());
    }
    
    private String categorizePrice(double price) {
        if (price < 50) return "Budget";
        if (price < 200) return "Mid-Range";
        return "Premium";
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isInStock() { return inStock; }
    public String getPriceCategory() { return priceCategory; }
    
    @Override
    public String toString() {
        return String.format("%s - $%.2f (%s) - Stock: %d %s",
            name, price, priceCategory, stock, inStock ? "✅" : "❌");
    }
}

// Read Model Updater (Event Handler)
class ProductReadModelUpdater {
    private final Map<String, ProductReadModel> readStore;
    
    public ProductReadModelUpdater(Map<String, ProductReadModel> readStore) {
        this.readStore = readStore;
    }
    
    public void onProductCreated(Product product) {
        readStore.put(product.getId(), new ProductReadModel(product));
        System.out.println("  → Read model updated (product created)");
    }
    
    public void onProductPriceUpdated(Product product) {
        readStore.put(product.getId(), new ProductReadModel(product));
        System.out.println("  → Read model updated (price changed)");
    }
    
    public void onProductStockUpdated(Product product) {
        readStore.put(product.getId(), new ProductReadModel(product));
        System.out.println("  → Read model updated (stock changed)");
    }
}

// Queries
class GetProductByIdQuery implements Query<ProductReadModel> {
    private final String productId;
    private final Map<String, ProductReadModel> readStore;
    
    public GetProductByIdQuery(String productId, Map<String, ProductReadModel> readStore) {
        this.productId = productId;
        this.readStore = readStore;
    }
    
    @Override
    public ProductReadModel execute() {
        return readStore.get(productId);
    }
}

class GetProductsByCategoryQuery implements Query<List<ProductReadModel>> {
    private final String category;
    private final Map<String, ProductReadModel> readStore;
    
    public GetProductsByCategoryQuery(String category, Map<String, ProductReadModel> readStore) {
        this.category = category;
        this.readStore = readStore;
    }
    
    @Override
    public List<ProductReadModel> execute() {
        return readStore.values().stream()
            .filter(p -> p.getPriceCategory().equals(category))
            .collect(Collectors.toList());
    }
}

class GetInStockProductsQuery implements Query<List<ProductReadModel>> {
    private final Map<String, ProductReadModel> readStore;
    
    public GetInStockProductsQuery(Map<String, ProductReadModel> readStore) {
        this.readStore = readStore;
    }
    
    @Override
    public List<ProductReadModel> execute() {
        return readStore.values().stream()
            .filter(ProductReadModel::isInStock)
            .collect(Collectors.toList());
    }
}

// CQRS Mediator/Bus
class CQRSBus {
    private final Map<Class<?>, CommandHandler<?>> commandHandlers = new ConcurrentHashMap<>();
    
    public <T extends Command> void registerCommandHandler(Class<T> commandClass, CommandHandler<T> handler) {
        commandHandlers.put(commandClass, handler);
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Command> void send(T command) {
        CommandHandler<T> handler = (CommandHandler<T>) commandHandlers.get(command.getClass());
        if (handler != null) {
            handler.handle(command);
        } else {
            throw new IllegalArgumentException("No handler registered for: " + command.getClass());
        }
    }
}

// Example 2: Bank Account CQRS
class BankAccount {
    private final String accountId;
    private double balance;
    private List<Transaction> transactions = new ArrayList<>();
    
    public BankAccount(String accountId, double initialBalance) {
        this.accountId = accountId;
        this.balance = initialBalance;
    }
    
    public void deposit(double amount) {
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount, balance));
    }
    
    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            transactions.add(new Transaction("WITHDRAW", amount, balance));
        }
    }
    
    public String getAccountId() { return accountId; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return new ArrayList<>(transactions); }
}

class Transaction {
    final String type;
    final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    
    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("%s: $%.2f (Balance: $%.2f) at %s",
            type, amount, balanceAfter, timestamp);
    }
}

class AccountReadModel {
    private String accountId;
    private double balance;
    private int transactionCount;
    private double totalDeposits;
    private double totalWithdrawals;
    
    public AccountReadModel(BankAccount account) {
        this.accountId = account.getAccountId();
        this.balance = account.getBalance();
        this.transactionCount = account.getTransactions().size();
        
        for (Transaction t : account.getTransactions()) {
            if (t.type.equals("DEPOSIT")) {
                totalDeposits += t.amount;
            } else {
                totalWithdrawals += t.amount;
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("Account %s: Balance=$%.2f, Transactions=%d, Deposits=$%.2f, Withdrawals=$%.2f",
            accountId, balance, transactionCount, totalDeposits, totalWithdrawals);
    }
}

// Demo
public class CQRSPattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     CQRS PATTERN DEMONSTRATION           ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Setup stores
        Map<String, Product> writeStore = new ConcurrentHashMap<>();
        Map<String, ProductReadModel> readStore = new ConcurrentHashMap<>();
        
        ProductReadModelUpdater readModelUpdater = new ProductReadModelUpdater(readStore);
        
        // Setup command handlers
        ProductCommandHandler createHandler = new ProductCommandHandler(writeStore, readModelUpdater);
        UpdatePriceCommandHandler priceHandler = new UpdatePriceCommandHandler(writeStore, readModelUpdater);
        
        // Setup CQRS Bus
        CQRSBus bus = new CQRSBus();
        bus.registerCommandHandler(CreateProductCommand.class, createHandler);
        bus.registerCommandHandler(UpdatePriceCommand.class, priceHandler);
        
        System.out.println("1. CREATING PRODUCTS (Commands)");
        System.out.println("────────────────────────────────");
        bus.send(new CreateProductCommand("P1", "Laptop", 999.99, 10));
        bus.send(new CreateProductCommand("P2", "Mouse", 29.99, 50));
        bus.send(new CreateProductCommand("P3", "Monitor", 299.99, 15));
        bus.send(new CreateProductCommand("P4", "Keyboard", 79.99, 30));
        
        System.out.println("\n2. QUERYING PRODUCTS (Queries)");
        System.out.println("───────────────────────────────");
        
        GetProductByIdQuery query1 = new GetProductByIdQuery("P1", readStore);
        System.out.println("Get Product P1: " + query1.execute());
        
        GetInStockProductsQuery query2 = new GetInStockProductsQuery(readStore);
        System.out.println("\nIn-Stock Products:");
        query2.execute().forEach(p -> System.out.println("  " + p));
        
        GetProductsByCategoryQuery query3 = new GetProductsByCategoryQuery("Premium", readStore);
        System.out.println("\nPremium Products:");
        query3.execute().forEach(p -> System.out.println("  " + p));
        
        System.out.println("\n3. UPDATING PRICES (Commands)");
        System.out.println("──────────────────────────────");
        bus.send(new UpdatePriceCommand("P1", 899.99));
        bus.send(new UpdatePriceCommand("P3", 249.99));
        
        System.out.println("\n4. QUERYING AFTER UPDATES");
        System.out.println("─────────────────────────");
        GetProductsByCategoryQuery query4 = new GetProductsByCategoryQuery("Mid-Range", readStore);
        System.out.println("Mid-Range Products:");
        query4.execute().forEach(p -> System.out.println("  " + p));
        
        System.out.println("\n5. BANK ACCOUNT EXAMPLE");
        System.out.println("───────────────────────");
        BankAccount account = new BankAccount("ACC123", 1000.0);
        account.deposit(500);
        account.withdraw(200);
        account.deposit(1000);
        
        AccountReadModel accountRead = new AccountReadModel(account);
        System.out.println(accountRead);
        
        System.out.println("\n✅ CQRS Pattern demonstration completed!");
        System.out.println("\n📊 Benefits Demonstrated:");
        System.out.println("  • Separated read and write models");
        System.out.println("  • Optimized queries without affecting writes");
        System.out.println("  • Independent scaling capability");
        System.out.println("  • Simplified domain logic");
    }
}
