package architectural;

import java.util.*;

/**
 * Model-View-ViewModel (MVVM) Pattern
 * Separates UI logic from business logic with data binding.
 */
public class MVVMPattern {
    
    // MODEL - Business logic and data
    static class User {
        private String id;
        private String name;
        private String email;
        private int age;
        
        public User(String id, String name, String email, int age) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.age = age;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        @Override
        public String toString() {
            return "User{id='" + id + "', name='" + name + "', email='" + email + 
                   "', age=" + age + "}";
        }
    }
    
    // Observable property for data binding
    static class ObservableProperty<T> {
        private T value;
        private final List<PropertyChangeListener<T>> listeners = new ArrayList<>();
        
        public ObservableProperty(T initialValue) {
            this.value = initialValue;
        }
        
        public T getValue() {
            return value;
        }
        
        public void setValue(T newValue) {
            T oldValue = this.value;
            this.value = newValue;
            notifyListeners(oldValue, newValue);
        }
        
        public void addListener(PropertyChangeListener<T> listener) {
            listeners.add(listener);
        }
        
        private void notifyListeners(T oldValue, T newValue) {
            for (PropertyChangeListener<T> listener : listeners) {
                listener.onPropertyChanged(oldValue, newValue);
            }
        }
    }
    
    interface PropertyChangeListener<T> {
        void onPropertyChanged(T oldValue, T newValue);
    }
    
    // VIEWMODEL - Presentation logic with observable properties
    static class UserViewModel {
        private User model;
        private final ObservableProperty<String> name;
        private final ObservableProperty<String> email;
        private final ObservableProperty<Integer> age;
        private final ObservableProperty<String> statusMessage;
        private final ObservableProperty<Boolean> isValid;
        
        public UserViewModel(User model) {
            this.model = model;
            this.name = new ObservableProperty<>(model.getName());
            this.email = new ObservableProperty<>(model.getEmail());
            this.age = new ObservableProperty<>(model.getAge());
            this.statusMessage = new ObservableProperty<>("");
            this.isValid = new ObservableProperty<>(true);
        }
        
        public ObservableProperty<String> nameProperty() { return name; }
        public ObservableProperty<String> emailProperty() { return email; }
        public ObservableProperty<Integer> ageProperty() { return age; }
        public ObservableProperty<String> statusMessageProperty() { return statusMessage; }
        public ObservableProperty<Boolean> isValidProperty() { return isValid; }
        
        public void updateName(String newName) {
            if (newName == null || newName.trim().isEmpty()) {
                statusMessage.setValue("❌ Name cannot be empty");
                isValid.setValue(false);
                return;
            }
            
            name.setValue(newName);
            model.setName(newName);
            statusMessage.setValue("✅ Name updated successfully");
            isValid.setValue(true);
        }
        
        public void updateEmail(String newEmail) {
            if (!isValidEmail(newEmail)) {
                statusMessage.setValue("❌ Invalid email format");
                isValid.setValue(false);
                return;
            }
            
            email.setValue(newEmail);
            model.setEmail(newEmail);
            statusMessage.setValue("✅ Email updated successfully");
            isValid.setValue(true);
        }
        
        public void updateAge(int newAge) {
            if (newAge < 0 || newAge > 150) {
                statusMessage.setValue("❌ Invalid age");
                isValid.setValue(false);
                return;
            }
            
            age.setValue(newAge);
            model.setAge(newAge);
            statusMessage.setValue("✅ Age updated successfully");
            isValid.setValue(true);
        }
        
        public void saveUser() {
            System.out.println("💾 Saving user: " + model);
            statusMessage.setValue("✅ User saved successfully");
        }
        
        private boolean isValidEmail(String email) {
            return email != null && email.contains("@") && email.contains(".");
        }
    }
    
    // VIEW - UI representation (console-based for demo)
    static class UserView {
        private final UserViewModel viewModel;
        
        public UserView(UserViewModel viewModel) {
            this.viewModel = viewModel;
            bindToViewModel();
        }
        
        private void bindToViewModel() {
            // Data binding: Listen to ViewModel changes and update UI
            viewModel.nameProperty().addListener((oldVal, newVal) -> {
                displayField("Name", newVal);
            });
            
            viewModel.emailProperty().addListener((oldVal, newVal) -> {
                displayField("Email", newVal);
            });
            
            viewModel.ageProperty().addListener((oldVal, newVal) -> {
                displayField("Age", String.valueOf(newVal));
            });
            
            viewModel.statusMessageProperty().addListener((oldVal, newVal) -> {
                displayStatusMessage(newVal);
            });
            
            viewModel.isValidProperty().addListener((oldVal, newVal) -> {
                displayValidationStatus(newVal);
            });
        }
        
        private void displayField(String fieldName, String value) {
            System.out.println("  [UI Updated] " + fieldName + ": " + value);
        }
        
        private void displayStatusMessage(String message) {
            if (!message.isEmpty()) {
                System.out.println("  [Status] " + message);
            }
        }
        
        private void displayValidationStatus(boolean isValid) {
            System.out.println("  [Validation] " + (isValid ? "✅ Valid" : "❌ Invalid"));
        }
        
        public void render() {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║       User Form View        ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║ Name:  " + String.format("%-21s", viewModel.nameProperty().getValue()) + "║");
            System.out.println("║ Email: " + String.format("%-21s", viewModel.emailProperty().getValue()) + "║");
            System.out.println("║ Age:   " + String.format("%-21d", viewModel.ageProperty().getValue()) + "║");
            System.out.println("╚══════════════════════════════╝");
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
    
    static class ShoppingCartViewModel {
        private final List<Product> items = new ArrayList<>();
        private final ObservableProperty<Integer> itemCount;
        private final ObservableProperty<Double> totalPrice;
        private final ObservableProperty<String> cartStatus;
        
        public ShoppingCartViewModel() {
            this.itemCount = new ObservableProperty<>(0);
            this.totalPrice = new ObservableProperty<>(0.0);
            this.cartStatus = new ObservableProperty<>("Cart is empty");
        }
        
        public ObservableProperty<Integer> itemCountProperty() { return itemCount; }
        public ObservableProperty<Double> totalPriceProperty() { return totalPrice; }
        public ObservableProperty<String> cartStatusProperty() { return cartStatus; }
        
        public void addProduct(Product product) {
            items.add(product);
            updateCart();
            cartStatus.setValue("Added: " + product.getName());
        }
        
        public void removeProduct(String productId) {
            items.removeIf(p -> p.getId().equals(productId));
            updateCart();
            cartStatus.setValue("Item removed");
        }
        
        public void clearCart() {
            items.clear();
            updateCart();
            cartStatus.setValue("Cart cleared");
        }
        
        private void updateCart() {
            itemCount.setValue(items.size());
            double total = items.stream().mapToDouble(Product::getPrice).sum();
            totalPrice.setValue(total);
            
            if (items.isEmpty()) {
                cartStatus.setValue("Cart is empty");
            }
        }
    }
    
    static class ShoppingCartView {
        private final ShoppingCartViewModel viewModel;
        
        public ShoppingCartView(ShoppingCartViewModel viewModel) {
            this.viewModel = viewModel;
            bindToViewModel();
        }
        
        private void bindToViewModel() {
            viewModel.itemCountProperty().addListener((oldVal, newVal) -> {
                System.out.println("  [UI] Item count updated: " + newVal);
            });
            
            viewModel.totalPriceProperty().addListener((oldVal, newVal) -> {
                System.out.println("  [UI] Total price updated: $" + String.format("%.2f", newVal));
            });
            
            viewModel.cartStatusProperty().addListener((oldVal, newVal) -> {
                System.out.println("  [UI] Status: " + newVal);
            });
        }
        
        public void render() {
            System.out.println("\n🛒 Shopping Cart:");
            System.out.println("   Items: " + viewModel.itemCountProperty().getValue());
            System.out.println("   Total: $" + String.format("%.2f", viewModel.totalPriceProperty().getValue()));
            System.out.println("   " + viewModel.cartStatusProperty().getValue());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== MVVM Pattern Demo ===\n");
        
        // 1. User Form with MVVM
        System.out.println("1. User Form with Data Binding:");
        User user = new User("U1", "John Doe", "john@example.com", 30);
        UserViewModel userViewModel = new UserViewModel(user);
        UserView userView = new UserView(userViewModel);
        
        userView.render();
        
        System.out.println("\nUpdating name...");
        userViewModel.updateName("Jane Smith");
        
        System.out.println("\nUpdating email...");
        userViewModel.updateEmail("jane.smith@example.com");
        
        System.out.println("\nUpdating age...");
        userViewModel.updateAge(28);
        
        userView.render();
        
        System.out.println("\nAttempting invalid updates...");
        userViewModel.updateEmail("invalid-email");
        userViewModel.updateAge(-5);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. Shopping Cart with MVVM
        System.out.println("\n2. Shopping Cart with Reactive Updates:");
        ShoppingCartViewModel cartViewModel = new ShoppingCartViewModel();
        ShoppingCartView cartView = new ShoppingCartView(cartViewModel);
        
        cartView.render();
        
        System.out.println("\nAdding products...");
        cartViewModel.addProduct(new Product("P1", "Laptop", 999.99));
        cartViewModel.addProduct(new Product("P2", "Mouse", 29.99));
        cartViewModel.addProduct(new Product("P3", "Keyboard", 79.99));
        
        cartView.render();
        
        System.out.println("\nRemoving product...");
        cartViewModel.removeProduct("P2");
        
        cartView.render();
        
        System.out.println("\nClearing cart...");
        cartViewModel.clearCart();
        
        cartView.render();
        
        System.out.println("\n--- MVVM Architecture ---");
        System.out.println("📦 MODEL: Business logic and data");
        System.out.println("   - User, Product");
        System.out.println("   - Pure business objects");
        System.out.println();
        System.out.println("🎨 VIEW: UI representation");
        System.out.println("   - UserView, ShoppingCartView");
        System.out.println("   - Observes ViewModel changes");
        System.out.println("   - No business logic");
        System.out.println();
        System.out.println("🔗 VIEWMODEL: Presentation logic");
        System.out.println("   - UserViewModel, ShoppingCartViewModel");
        System.out.println("   - Observable properties");
        System.out.println("   - Data binding support");
        System.out.println("   - Validation logic");
        
        System.out.println("\n--- Data Binding ---");
        System.out.println("Two-way binding:");
        System.out.println("  View → ViewModel: User actions update ViewModel");
        System.out.println("  ViewModel → View: ViewModel changes auto-update View");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Clear separation of concerns");
        System.out.println("✓ Testable presentation logic");
        System.out.println("✓ Data binding reduces boilerplate");
        System.out.println("✓ UI-independent ViewModels");
        System.out.println("✓ Reactive updates");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• WPF/UWP applications");
        System.out.println("• Android apps (with LiveData)");
        System.out.println("• Web frameworks (Vue, Knockout)");
        System.out.println("• Forms and data entry");
        
        System.out.println("\n--- Comparison ---");
        System.out.println("MVC:  View → Controller → Model");
        System.out.println("MVP:  View ↔ Presenter ↔ Model");
        System.out.println("MVVM: View ⇄ ViewModel ← Model (data binding)");
    }
}
