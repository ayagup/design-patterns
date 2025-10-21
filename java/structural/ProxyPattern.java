package structural;

/**
 * Proxy Pattern
 * Provides a surrogate or placeholder for another object.
 */
public class ProxyPattern {
    
    // Subject interface
    interface Image {
        void display();
    }
    
    // Real Subject
    static class RealImage implements Image {
        private String filename;
        
        public RealImage(String filename) {
            this.filename = filename;
            loadFromDisk();
        }
        
        private void loadFromDisk() {
            System.out.println("Loading image from disk: " + filename);
            // Simulate expensive operation
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void display() {
            System.out.println("Displaying image: " + filename);
        }
    }
    
    // Virtual Proxy - delays creation until needed
    static class ProxyImage implements Image {
        private String filename;
        private RealImage realImage;
        
        public ProxyImage(String filename) {
            this.filename = filename;
        }
        
        @Override
        public void display() {
            if (realImage == null) {
                realImage = new RealImage(filename);
            }
            realImage.display();
        }
    }
    
    // Protection Proxy example
    interface BankAccount {
        void deposit(double amount);
        void withdraw(double amount);
        double getBalance();
    }
    
    static class RealBankAccount implements BankAccount {
        private double balance = 1000.0;
        
        @Override
        public void deposit(double amount) {
            balance += amount;
            System.out.println("Deposited: $" + amount + ", New balance: $" + balance);
        }
        
        @Override
        public void withdraw(double amount) {
            if (balance >= amount) {
                balance -= amount;
                System.out.println("Withdrew: $" + amount + ", New balance: $" + balance);
            } else {
                System.out.println("Insufficient funds!");
            }
        }
        
        @Override
        public double getBalance() {
            return balance;
        }
    }
    
    static class ProtectedBankAccount implements BankAccount {
        private RealBankAccount realAccount;
        private String userRole;
        
        public ProtectedBankAccount(String userRole) {
            this.realAccount = new RealBankAccount();
            this.userRole = userRole;
        }
        
        @Override
        public void deposit(double amount) {
            if ("ADMIN".equals(userRole) || "USER".equals(userRole)) {
                realAccount.deposit(amount);
            } else {
                System.out.println("Access denied: No permission to deposit");
            }
        }
        
        @Override
        public void withdraw(double amount) {
            if ("ADMIN".equals(userRole)) {
                realAccount.withdraw(amount);
            } else {
                System.out.println("Access denied: Only ADMIN can withdraw");
            }
        }
        
        @Override
        public double getBalance() {
            if ("ADMIN".equals(userRole) || "USER".equals(userRole)) {
                return realAccount.getBalance();
            } else {
                System.out.println("Access denied: No permission to view balance");
                return 0;
            }
        }
    }
    
    // Logging Proxy example
    interface DatabaseExecutor {
        void executeQuery(String query);
    }
    
    static class RealDatabaseExecutor implements DatabaseExecutor {
        @Override
        public void executeQuery(String query) {
            System.out.println("Executing query: " + query);
        }
    }
    
    static class LoggingDatabaseProxy implements DatabaseExecutor {
        private RealDatabaseExecutor executor;
        
        public LoggingDatabaseProxy() {
            this.executor = new RealDatabaseExecutor();
        }
        
        @Override
        public void executeQuery(String query) {
            System.out.println("[LOG] Query execution started at " + 
                             new java.util.Date());
            long startTime = System.currentTimeMillis();
            
            executor.executeQuery(query);
            
            long endTime = System.currentTimeMillis();
            System.out.println("[LOG] Query execution completed in " + 
                             (endTime - startTime) + "ms");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Proxy Pattern Demo ===\n");
        
        // Virtual Proxy - lazy loading
        System.out.println("1. Virtual Proxy (Lazy Loading):");
        System.out.println("Creating proxy images...");
        Image image1 = new ProxyImage("photo1.jpg");
        Image image2 = new ProxyImage("photo2.jpg");
        System.out.println("Proxies created (images not loaded yet)\n");
        
        System.out.println("Displaying image1 first time:");
        image1.display();
        
        System.out.println("\nDisplaying image1 again:");
        image1.display();
        
        System.out.println("\nDisplaying image2:");
        image2.display();
        
        // Protection Proxy
        System.out.println("\n\n2. Protection Proxy (Access Control):");
        
        System.out.println("Admin user:");
        BankAccount adminAccount = new ProtectedBankAccount("ADMIN");
        adminAccount.deposit(500);
        adminAccount.withdraw(200);
        System.out.println("Balance: $" + adminAccount.getBalance());
        
        System.out.println("\nRegular user:");
        BankAccount userAccount = new ProtectedBankAccount("USER");
        userAccount.deposit(300);
        userAccount.withdraw(100);
        System.out.println("Balance: $" + userAccount.getBalance());
        
        System.out.println("\nGuest user:");
        BankAccount guestAccount = new ProtectedBankAccount("GUEST");
        guestAccount.deposit(200);
        guestAccount.getBalance();
        
        // Logging Proxy
        System.out.println("\n\n3. Logging Proxy:");
        DatabaseExecutor db = new LoggingDatabaseProxy();
        db.executeQuery("SELECT * FROM users WHERE id = 1");
        
        System.out.println("\n--- Proxy Types ---");
        System.out.println("• Virtual Proxy: Lazy initialization");
        System.out.println("• Protection Proxy: Access control");
        System.out.println("• Remote Proxy: Represents remote object");
        System.out.println("• Smart Proxy: Additional functionality");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Controls access to the real object");
        System.out.println("✓ Can add functionality without changing real object");
        System.out.println("✓ Supports lazy initialization");
    }
}
