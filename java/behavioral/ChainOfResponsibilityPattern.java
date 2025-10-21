package behavioral;

/**
 * Chain of Responsibility Pattern
 * Passes requests along a chain of handlers.
 */
public class ChainOfResponsibilityPattern {
    
    // Handler interface
    abstract static class SupportHandler {
        protected SupportHandler nextHandler;
        
        public void setNextHandler(SupportHandler handler) {
            this.nextHandler = handler;
        }
        
        public abstract void handleRequest(String issue, int priority);
    }
    
    // Concrete Handlers
    static class Level1Support extends SupportHandler {
        @Override
        public void handleRequest(String issue, int priority) {
            if (priority <= 1) {
                System.out.println("Level 1 Support: Handling issue - " + issue);
            } else if (nextHandler != null) {
                System.out.println("Level 1 Support: Escalating to Level 2");
                nextHandler.handleRequest(issue, priority);
            }
        }
    }
    
    static class Level2Support extends SupportHandler {
        @Override
        public void handleRequest(String issue, int priority) {
            if (priority <= 2) {
                System.out.println("Level 2 Support: Handling issue - " + issue);
            } else if (nextHandler != null) {
                System.out.println("Level 2 Support: Escalating to Level 3");
                nextHandler.handleRequest(issue, priority);
            }
        }
    }
    
    static class Level3Support extends SupportHandler {
        @Override
        public void handleRequest(String issue, int priority) {
            System.out.println("Level 3 Support: Handling critical issue - " + issue);
        }
    }
    
    // Authentication chain example
    abstract static class AuthenticationHandler {
        protected AuthenticationHandler next;
        
        public void setNext(AuthenticationHandler handler) {
            this.next = handler;
        }
        
        public abstract boolean authenticate(String username, String password);
    }
    
    static class UsernamePasswordValidator extends AuthenticationHandler {
        @Override
        public boolean authenticate(String username, String password) {
            if (username == null || username.isEmpty() || 
                password == null || password.isEmpty()) {
                System.out.println("❌ Username or password is empty");
                return false;
            }
            System.out.println("✓ Username and password format valid");
            return next != null ? next.authenticate(username, password) : true;
        }
    }
    
    static class DatabaseValidator extends AuthenticationHandler {
        @Override
        public boolean authenticate(String username, String password) {
            // Simulate database check
            if ("admin".equals(username) && "admin123".equals(password)) {
                System.out.println("✓ Credentials verified in database");
                return next != null ? next.authenticate(username, password) : true;
            }
            System.out.println("❌ Invalid credentials");
            return false;
        }
    }
    
    static class RoleValidator extends AuthenticationHandler {
        @Override
        public boolean authenticate(String username, String password) {
            System.out.println("✓ User role validated");
            return next != null ? next.authenticate(username, password) : true;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Chain of Responsibility Pattern Demo ===\n");
        
        // Support ticket example
        System.out.println("1. Support Ticket System:");
        SupportHandler level1 = new Level1Support();
        SupportHandler level2 = new Level2Support();
        SupportHandler level3 = new Level3Support();
        
        level1.setNextHandler(level2);
        level2.setNextHandler(level3);
        
        level1.handleRequest("Password reset", 1);
        System.out.println();
        level1.handleRequest("Software bug", 2);
        System.out.println();
        level1.handleRequest("System crash", 3);
        
        // Authentication chain
        System.out.println("\n\n2. Authentication Chain:");
        AuthenticationHandler formatValidator = new UsernamePasswordValidator();
        AuthenticationHandler dbValidator = new DatabaseValidator();
        AuthenticationHandler roleValidator = new RoleValidator();
        
        formatValidator.setNext(dbValidator);
        dbValidator.setNext(roleValidator);
        
        System.out.println("Authentication attempt 1 (valid):");
        boolean result1 = formatValidator.authenticate("admin", "admin123");
        System.out.println("Result: " + (result1 ? "Success" : "Failed"));
        
        System.out.println("\nAuthentication attempt 2 (invalid):");
        boolean result2 = formatValidator.authenticate("user", "wrong");
        System.out.println("Result: " + (result2 ? "Success" : "Failed"));
        
        System.out.println("\nAuthentication attempt 3 (empty):");
        boolean result3 = formatValidator.authenticate("", "");
        System.out.println("Result: " + (result3 ? "Success" : "Failed"));
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Decouples sender and receiver");
        System.out.println("✓ Flexible chain configuration");
        System.out.println("✓ Single Responsibility Principle");
        System.out.println("✓ Easy to add new handlers");
    }
}
