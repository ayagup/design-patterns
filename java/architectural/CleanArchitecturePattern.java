package architectural;

import java.util.*;

/**
 * Clean Architecture Pattern
 * ===========================
 * 
 * Intent:
 * Organize software into concentric circles of dependencies, where inner circles
 * contain business logic and outer circles contain implementation details. The
 * Dependency Rule: source code dependencies must point only inward.
 * 
 * Also Known As:
 * - Uncle Bob's Clean Architecture
 * - Screaming Architecture
 * 
 * Motivation:
 * - Independent of frameworks
 * - Testable without UI, database, or external dependencies
 * - Independent of UI (can swap UI easily)
 * - Independent of database (can swap DB easily)
 * - Business rules don't know anything about outside world
 * 
 * Applicability:
 * - Large enterprise applications
 * - Systems requiring long-term maintainability
 * - Applications with complex business rules
 * - Systems that need framework independence
 * 
 * Structure (from inside out):
 * 1. Entities: Enterprise business rules
 * 2. Use Cases: Application business rules
 * 3. Interface Adapters: Controllers, Presenters, Gateways
 * 4. Frameworks & Drivers: UI, DB, Web, Devices
 * 
 * Key Principle:
 * Dependencies point INWARD. Inner layers never depend on outer layers.
 */

// ============================================================================
// LAYER 1: ENTITIES (Enterprise Business Rules)
// ============================================================================

class Product {
    private final String id;
    private String name;
    private double price;
    private int stockQuantity;
    
    public Product(String id, String name, double price, int stockQuantity) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock cannot be negative");
        
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    public boolean isAvailable(int requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }
    
    public void reduceStock(int quantity) {
        if (!isAvailable(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        stockQuantity -= quantity;
    }
    
    public void increaseStock(int quantity) {
        stockQuantity += quantity;
    }
    
    public double calculateTotalPrice(int quantity) {
        return price * quantity;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
}

class ShoppingCart {
    private final String customerId;
    private final Map<String, CartItem> items = new HashMap<>();
    
    public ShoppingCart(String customerId) {
        this.customerId = customerId;
    }
    
    public void addItem(Product product, int quantity) {
        if (!product.isAvailable(quantity)) {
            throw new IllegalStateException("Product not available in requested quantity");
        }
        
        CartItem existing = items.get(product.getId());
        if (existing != null) {
            items.put(product.getId(), new CartItem(
                product.getId(), 
                product.getName(), 
                product.getPrice(), 
                existing.quantity + quantity
            ));
        } else {
            items.put(product.getId(), new CartItem(
                product.getId(), 
                product.getName(), 
                product.getPrice(), 
                quantity
            ));
        }
    }
    
    public void removeItem(String productId) {
        items.remove(productId);
    }
    
    public double calculateTotal() {
        return items.values().stream()
                   .mapToDouble(item -> item.price * item.quantity)
                   .sum();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public Collection<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    static class CartItem {
        private final String productId;
        private final String productName;
        private final double price;
        private final int quantity;
        
        public CartItem(String productId, String productName, double price, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }
}

// ============================================================================
// LAYER 2: USE CASES (Application Business Rules)
// ============================================================================

// Input/Output boundaries (Data Transfer Objects)
class AddToCartRequest {
    public final String customerId;
    public final String productId;
    public final int quantity;
    
    public AddToCartRequest(String customerId, String productId, int quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
    }
}

class AddToCartResponse {
    public final boolean success;
    public final String message;
    public final double cartTotal;
    
    public AddToCartResponse(boolean success, String message, double cartTotal) {
        this.success = success;
        this.message = message;
        this.cartTotal = cartTotal;
    }
}

class CheckoutRequest {
    public final String customerId;
    public final String paymentMethod;
    
    public CheckoutRequest(String customerId, String paymentMethod) {
        this.customerId = customerId;
        this.paymentMethod = paymentMethod;
    }
}

class CheckoutResponse {
    public final boolean success;
    public final String orderId;
    public final double totalAmount;
    public final String message;
    
    public CheckoutResponse(boolean success, String orderId, double totalAmount, String message) {
        this.success = success;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.message = message;
    }
}

// Repository interfaces (defined in use case layer, implemented in outer layer)
interface ProductGateway {
    Product findById(String id);
    void save(Product product);
}

interface CartGateway {
    ShoppingCart findByCustomerId(String customerId);
    void save(ShoppingCart cart);
}

// Use Case: Add Product to Cart
class AddToCartUseCase {
    private final ProductGateway productGateway;
    private final CartGateway cartGateway;
    
    public AddToCartUseCase(ProductGateway productGateway, CartGateway cartGateway) {
        this.productGateway = productGateway;
        this.cartGateway = cartGateway;
    }
    
    public AddToCartResponse execute(AddToCartRequest request) {
        try {
            // Fetch product
            Product product = productGateway.findById(request.productId);
            if (product == null) {
                return new AddToCartResponse(false, "Product not found", 0.0);
            }
            
            // Check availability
            if (!product.isAvailable(request.quantity)) {
                return new AddToCartResponse(false, "Insufficient stock", 0.0);
            }
            
            // Get or create cart
            ShoppingCart cart = cartGateway.findByCustomerId(request.customerId);
            if (cart == null) {
                cart = new ShoppingCart(request.customerId);
            }
            
            // Add to cart
            cart.addItem(product, request.quantity);
            cartGateway.save(cart);
            
            double total = cart.calculateTotal();
            return new AddToCartResponse(true, "Product added to cart", total);
            
        } catch (Exception e) {
            return new AddToCartResponse(false, "Error: " + e.getMessage(), 0.0);
        }
    }
}

// Use Case: Checkout
class CheckoutUseCase {
    private final CartGateway cartGateway;
    private final ProductGateway productGateway;
    private int orderCounter = 1000;
    
    public CheckoutUseCase(CartGateway cartGateway, ProductGateway productGateway) {
        this.cartGateway = cartGateway;
        this.productGateway = productGateway;
    }
    
    public CheckoutResponse execute(CheckoutRequest request) {
        try {
            // Get cart
            ShoppingCart cart = cartGateway.findByCustomerId(request.customerId);
            if (cart == null || cart.isEmpty()) {
                return new CheckoutResponse(false, null, 0.0, "Cart is empty");
            }
            
            // Reduce stock for all items
            for (ShoppingCart.CartItem item : cart.getItems()) {
                Product product = productGateway.findById(item.getProductId());
                product.reduceStock(item.getQuantity());
                productGateway.save(product);
            }
            
            double total = cart.calculateTotal();
            String orderId = "ORD-" + (orderCounter++);
            
            // Clear cart (checkout complete)
            cart = new ShoppingCart(request.customerId);
            cartGateway.save(cart);
            
            return new CheckoutResponse(true, orderId, total, "Order placed successfully");
            
        } catch (Exception e) {
            return new CheckoutResponse(false, null, 0.0, "Checkout failed: " + e.getMessage());
        }
    }
}

// ============================================================================
// LAYER 3: INTERFACE ADAPTERS (Controllers, Presenters, Gateways)
// ============================================================================

// Gateway implementations
class InMemoryProductGateway implements ProductGateway {
    private Map<String, Product> products = new HashMap<>();
    
    public InMemoryProductGateway() {
        // Seed with sample data
        products.put("P001", new Product("P001", "Laptop", 999.99, 10));
        products.put("P002", new Product("P002", "Mouse", 29.99, 50));
        products.put("P003", new Product("P003", "Keyboard", 79.99, 30));
    }
    
    @Override
    public Product findById(String id) {
        return products.get(id);
    }
    
    @Override
    public void save(Product product) {
        products.put(product.getId(), product);
    }
}

class InMemoryCartGateway implements CartGateway {
    private Map<String, ShoppingCart> carts = new HashMap<>();
    
    @Override
    public ShoppingCart findByCustomerId(String customerId) {
        return carts.get(customerId);
    }
    
    @Override
    public void save(ShoppingCart cart) {
        carts.put(cart.getCustomerId(), cart);
    }
}

// Controller (accepts web requests, calls use cases)
class ShoppingController {
    private final AddToCartUseCase addToCartUseCase;
    private final CheckoutUseCase checkoutUseCase;
    
    public ShoppingController(AddToCartUseCase addToCartUseCase, 
                             CheckoutUseCase checkoutUseCase) {
        this.addToCartUseCase = addToCartUseCase;
        this.checkoutUseCase = checkoutUseCase;
    }
    
    public void handleAddToCart(String customerId, String productId, int quantity) {
        System.out.println("[Controller] POST /cart/add");
        System.out.println("[Controller] Body: {customerId: " + customerId + 
                         ", productId: " + productId + ", quantity: " + quantity + "}");
        
        AddToCartRequest request = new AddToCartRequest(customerId, productId, quantity);
        AddToCartResponse response = addToCartUseCase.execute(request);
        
        presentAddToCartResponse(response);
    }
    
    public void handleCheckout(String customerId, String paymentMethod) {
        System.out.println("\n[Controller] POST /checkout");
        System.out.println("[Controller] Body: {customerId: " + customerId + 
                         ", paymentMethod: " + paymentMethod + "}");
        
        CheckoutRequest request = new CheckoutRequest(customerId, paymentMethod);
        CheckoutResponse response = checkoutUseCase.execute(request);
        
        presentCheckoutResponse(response);
    }
    
    // Presenter logic
    private void presentAddToCartResponse(AddToCartResponse response) {
        if (response.success) {
            System.out.println("[Controller] Response 200: {");
            System.out.println("  \"message\": \"" + response.message + "\",");
            System.out.println("  \"cartTotal\": " + String.format("%.2f", response.cartTotal));
            System.out.println("}");
        } else {
            System.out.println("[Controller] Response 400: {");
            System.out.println("  \"error\": \"" + response.message + "\"");
            System.out.println("}");
        }
    }
    
    private void presentCheckoutResponse(CheckoutResponse response) {
        if (response.success) {
            System.out.println("[Controller] Response 200: {");
            System.out.println("  \"orderId\": \"" + response.orderId + "\",");
            System.out.println("  \"total\": " + String.format("%.2f", response.totalAmount) + ",");
            System.out.println("  \"message\": \"" + response.message + "\"");
            System.out.println("}");
        } else {
            System.out.println("[Controller] Response 400: {");
            System.out.println("  \"error\": \"" + response.message + "\"");
            System.out.println("}");
        }
    }
}

// ============================================================================
// LAYER 4: FRAMEWORKS & DRIVERS
// ============================================================================
// (In real app: Spring, Database drivers, Web framework, etc.)
// Here we just simulate HTTP requests

/**
 * Demonstration of Clean Architecture Pattern
 */
public class CleanArchitecturePattern {
    public static void main(String[] args) {
        demonstrateCleanArchitecture();
    }
    
    private static void demonstrateCleanArchitecture() {
        System.out.println("=== Clean Architecture: E-Commerce System ===\n");
        
        // Layer 3: Create gateways (infrastructure)
        ProductGateway productGateway = new InMemoryProductGateway();
        CartGateway cartGateway = new InMemoryCartGateway();
        
        // Layer 2: Create use cases (business logic)
        AddToCartUseCase addToCartUseCase = new AddToCartUseCase(productGateway, cartGateway);
        CheckoutUseCase checkoutUseCase = new CheckoutUseCase(cartGateway, productGateway);
        
        // Layer 3: Create controller
        ShoppingController controller = new ShoppingController(addToCartUseCase, checkoutUseCase);
        
        // Layer 4: Simulate HTTP requests
        System.out.println("=== Scenario: Customer Shopping Session ===\n");
        
        // Add items to cart
        controller.handleAddToCart("CUST-001", "P001", 1);  // Laptop
        System.out.println();
        
        controller.handleAddToCart("CUST-001", "P002", 2);  // Mouse x2
        System.out.println();
        
        controller.handleAddToCart("CUST-001", "P003", 1);  // Keyboard
        System.out.println();
        
        // Try to add unavailable product
        controller.handleAddToCart("CUST-001", "P001", 20); // Not enough stock
        System.out.println();
        
        // Checkout
        controller.handleCheckout("CUST-001", "credit_card");
        System.out.println();
        
        // Try to checkout empty cart
        controller.handleCheckout("CUST-001", "credit_card");
        
        System.out.println("\n=== Architecture Layers ===");
        System.out.println("1. Entities: Product, ShoppingCart (pure business rules)");
        System.out.println("2. Use Cases: AddToCartUseCase, CheckoutUseCase (application logic)");
        System.out.println("3. Interface Adapters: ShoppingController, Gateways (adapt external systems)");
        System.out.println("4. Frameworks & Drivers: HTTP simulation (web framework would go here)");
        System.out.println("\nDependency Direction: 4 → 3 → 2 → 1 (always pointing inward!)");
    }
}
