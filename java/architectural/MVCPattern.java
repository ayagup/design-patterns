package architectural;

/**
 * Model-View-Controller (MVC) Pattern
 * Separates data, presentation, and control logic.
 */
public class MVCPattern {
    
    // MODEL - Business logic and data
    static class User {
        private String name;
        private String email;
        private int age;
        
        public User(String name, String email, int age) {
            this.name = name;
            this.email = email;
            this.age = age;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
    
    // VIEW - Presentation logic
    interface UserView {
        void displayUserDetails(String name, String email, int age);
        void showMessage(String message);
    }
    
    static class ConsoleUserView implements UserView {
        @Override
        public void displayUserDetails(String name, String email, int age) {
            System.out.println("\n┌─────────────────────────┐");
            System.out.println("│    User Details        │");
            System.out.println("├─────────────────────────┤");
            System.out.println("│ Name:  " + String.format("%-16s", name) + "│");
            System.out.println("│ Email: " + String.format("%-16s", email) + "│");
            System.out.println("│ Age:   " + String.format("%-16d", age) + "│");
            System.out.println("└─────────────────────────┘");
        }
        
        @Override
        public void showMessage(String message) {
            System.out.println("📢 " + message);
        }
    }
    
    // CONTROLLER - Handles user input and updates
    static class UserController {
        private User model;
        private UserView view;
        
        public UserController(User model, UserView view) {
            this.model = model;
            this.view = view;
        }
        
        public void setUserName(String name) {
            model.setName(name);
            view.showMessage("Name updated to: " + name);
        }
        
        public void setUserEmail(String email) {
            if (isValidEmail(email)) {
                model.setEmail(email);
                view.showMessage("Email updated to: " + email);
            } else {
                view.showMessage("❌ Invalid email format!");
            }
        }
        
        public void setUserAge(int age) {
            if (age > 0 && age < 150) {
                model.setAge(age);
                view.showMessage("Age updated to: " + age);
            } else {
                view.showMessage("❌ Invalid age!");
            }
        }
        
        public void displayUser() {
            view.displayUserDetails(
                model.getName(),
                model.getEmail(),
                model.getAge()
            );
        }
        
        private boolean isValidEmail(String email) {
            return email != null && email.contains("@") && email.contains(".");
        }
    }
    
    // Shopping Cart Example
    static class Product {
        private String id;
        private String name;
        private double price;
        
        public Product(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
    }
    
    static class ShoppingCartModel {
        private java.util.List<Product> items = new java.util.ArrayList<>();
        
        public void addProduct(Product product) {
            items.add(product);
        }
        
        public void removeProduct(String productId) {
            items.removeIf(p -> p.getId().equals(productId));
        }
        
        public java.util.List<Product> getItems() {
            return new java.util.ArrayList<>(items);
        }
        
        public double getTotal() {
            return items.stream().mapToDouble(Product::getPrice).sum();
        }
        
        public int getItemCount() {
            return items.size();
        }
    }
    
    static class ShoppingCartView {
        public void displayCart(java.util.List<Product> items, double total) {
            System.out.println("\n🛒 Shopping Cart:");
            System.out.println("─".repeat(50));
            
            if (items.isEmpty()) {
                System.out.println("  (empty)");
            } else {
                for (Product product : items) {
                    System.out.printf("  • %s - $%.2f%n", product.getName(), product.getPrice());
                }
            }
            
            System.out.println("─".repeat(50));
            System.out.printf("  Total: $%.2f (%d items)%n", total, items.size());
        }
        
        public void showMessage(String message) {
            System.out.println("  💬 " + message);
        }
    }
    
    static class ShoppingCartController {
        private ShoppingCartModel model;
        private ShoppingCartView view;
        
        public ShoppingCartController(ShoppingCartModel model, ShoppingCartView view) {
            this.model = model;
            this.view = view;
        }
        
        public void addProduct(Product product) {
            model.addProduct(product);
            view.showMessage("Added: " + product.getName());
            updateView();
        }
        
        public void removeProduct(String productId) {
            Product product = model.getItems().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);
            
            if (product != null) {
                model.removeProduct(productId);
                view.showMessage("Removed: " + product.getName());
                updateView();
            }
        }
        
        public void updateView() {
            view.displayCart(model.getItems(), model.getTotal());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== MVC Pattern Demo ===\n");
        
        // Example 1: User Management
        System.out.println("1. User Management MVC:");
        
        User user = new User("John Doe", "john@example.com", 30);
        UserView view = new ConsoleUserView();
        UserController controller = new UserController(user, view);
        
        controller.displayUser();
        
        System.out.println("\nUpdating user information...");
        controller.setUserName("Jane Smith");
        controller.setUserEmail("jane.smith@example.com");
        controller.setUserAge(28);
        
        controller.displayUser();
        
        System.out.println("\nTrying invalid updates...");
        controller.setUserEmail("invalid-email");
        controller.setUserAge(-5);
        
        System.out.println("\n" + "=".repeat(50));
        
        // Example 2: Shopping Cart
        System.out.println("\n2. Shopping Cart MVC:");
        
        ShoppingCartModel cartModel = new ShoppingCartModel();
        ShoppingCartView cartView = new ShoppingCartView();
        ShoppingCartController cartController = 
            new ShoppingCartController(cartModel, cartView);
        
        cartController.updateView();
        
        System.out.println("\nAdding products...");
        cartController.addProduct(new Product("1", "Laptop", 999.99));
        cartController.addProduct(new Product("2", "Mouse", 29.99));
        cartController.addProduct(new Product("3", "Keyboard", 79.99));
        
        System.out.println("\nRemoving product...");
        cartController.removeProduct("2");
        
        System.out.println("\n--- MVC Components ---");
        System.out.println("📦 MODEL: Business logic and data");
        System.out.println("   - User, ShoppingCartModel");
        System.out.println("   - Contains state and business rules");
        System.out.println();
        System.out.println("👁️  VIEW: Presentation layer");
        System.out.println("   - ConsoleUserView, ShoppingCartView");
        System.out.println("   - Displays data to user");
        System.out.println();
        System.out.println("🎮 CONTROLLER: Input handler");
        System.out.println("   - UserController, ShoppingCartController");
        System.out.println("   - Updates model based on user actions");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Separation of concerns");
        System.out.println("✓ Independent development");
        System.out.println("✓ Multiple views for same model");
        System.out.println("✓ Easier testing");
        System.out.println("✓ Reusable components");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Web applications");
        System.out.println("• Desktop GUIs");
        System.out.println("• Mobile apps");
        System.out.println("• Enterprise applications");
    }
}
