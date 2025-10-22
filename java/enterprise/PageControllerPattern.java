package enterprise;

import java.util.*;

/**
 * Page Controller Pattern
 * 
 * Intent: An object that handles a request for a specific page or
 * action on a website. Each page/action has its own controller.
 * 
 * Motivation:
 * Simple approach where each page has dedicated controller.
 * Contrast with Front Controller (single entry point).
 * Direct mapping between URLs and controller classes.
 * 
 * Applicability:
 * - Simple web applications
 * - Few pages with independent logic
 * - Rapid development
 * - When Front Controller is overkill
 */

/**
 * Example 1: Basic Page Controllers
 * 
 * Each page has its own controller class
 */
class HttpRequest {
    private final Map<String, String> parameters;
    private final Map<String, Object> attributes;
    
    public HttpRequest() {
        this.parameters = new HashMap<>();
        this.attributes = new HashMap<>();
    }
    
    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }
    
    public String getParameter(String key) {
        return parameters.get(key);
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}

class HttpResponse {
    private String content;
    private String redirectUrl;
    private int statusCode = 200;
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    
    public void redirect(String url) {
        this.redirectUrl = url;
        this.statusCode = 302;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setStatusCode(int code) {
        this.statusCode = code;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}

// Home Page Controller
class HomePageController {
    public void handleRequest(HttpRequest request, HttpResponse response) {
        System.out.println("  [HomePageController] Handling request");
        
        // Fetch data
        List<String> recentArticles = Arrays.asList("Article 1", "Article 2", "Article 3");
        
        // Set attributes for view
        request.setAttribute("articles", recentArticles);
        request.setAttribute("title", "Welcome Home");
        
        // Render view
        response.setContent("<html><body><h1>Home Page</h1></body></html>");
        System.out.println("  [HomePageController] Rendered home page");
    }
}

// Login Page Controller
class LoginPageController {
    public void handleGet(HttpRequest request, HttpResponse response) {
        System.out.println("  [LoginPageController] GET - Displaying login form");
        response.setContent("<html><body><form>Login Form</form></body></html>");
    }
    
    public void handlePost(HttpRequest request, HttpResponse response) {
        System.out.println("  [LoginPageController] POST - Processing login");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // Validate credentials
        if ("admin".equals(username) && "pass".equals(password)) {
            System.out.println("  [LoginPageController] Login successful");
            request.setAttribute("user", username);
            response.redirect("/dashboard");
        } else {
            System.out.println("  [LoginPageController] Login failed");
            request.setAttribute("error", "Invalid credentials");
            response.setContent("<html><body>Login Failed</body></html>");
        }
    }
}

// User Profile Page Controller
class UserProfilePageController {
    public void handleRequest(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("id");
        System.out.println("  [UserProfileController] Loading profile for user: " + userId);
        
        // Simulate fetching user data
        Map<String, String> user = new HashMap<>();
        user.put("id", userId);
        user.put("name", "User " + userId);
        user.put("email", "user" + userId + "@example.com");
        
        request.setAttribute("user", user);
        response.setContent("<html><body><h1>User Profile</h1></body></html>");
        
        System.out.println("  [UserProfileController] Rendered profile page");
    }
}

/**
 * Example 2: RESTful Page Controllers
 * 
 * Controllers for REST API endpoints
 */
class ProductApiController {
    private final Map<Long, Product> products;
    
    public ProductApiController() {
        this.products = new HashMap<>();
        products.put(1L, new Product(1L, "Laptop", 999.99));
        products.put(2L, new Product(2L, "Phone", 599.99));
    }
    
    public void handleGetAll(HttpRequest request, HttpResponse response) {
        System.out.println("  [ProductAPI] GET /api/products");
        
        String json = products.values().toString();
        response.setContent(json);
        System.out.println("  [ProductAPI] Returned " + products.size() + " products");
    }
    
    public void handleGetById(HttpRequest request, HttpResponse response) {
        String id = request.getParameter("id");
        System.out.println("  [ProductAPI] GET /api/products/" + id);
        
        Product product = products.get(Long.parseLong(id));
        if (product != null) {
            response.setContent(product.toString());
        } else {
            response.setStatusCode(404);
            response.setContent("{\"error\": \"Product not found\"}");
        }
    }
    
    public void handleCreate(HttpRequest request, HttpResponse response) {
        System.out.println("  [ProductAPI] POST /api/products");
        
        String name = request.getParameter("name");
        String price = request.getParameter("price");
        
        long newId = products.size() + 1;
        Product product = new Product(newId, name, Double.parseDouble(price));
        products.put(newId, product);
        
        response.setStatusCode(201);
        response.setContent("{\"id\": " + newId + "}");
        System.out.println("  [ProductAPI] Created product: " + newId);
    }
}

class Product {
    Long id;
    String name;
    double price;
    
    public Product(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    @Override
    public String toString() {
        return "{\"id\":" + id + ",\"name\":\"" + name + "\",\"price\":" + price + "}";
    }
}

/**
 * Example 3: CRUD Page Controllers
 * 
 * Controllers for Create, Read, Update, Delete operations
 */
class CustomerListPageController {
    public void handleRequest(HttpRequest request, HttpResponse response) {
        System.out.println("  [CustomerListController] Displaying customer list");
        
        List<String> customers = Arrays.asList("Alice", "Bob", "Charlie");
        request.setAttribute("customers", customers);
        response.setContent("<html><body>Customer List</body></html>");
    }
}

class CustomerEditPageController {
    public void handleGet(HttpRequest request, HttpResponse response) {
        String id = request.getParameter("id");
        System.out.println("  [CustomerEditController] GET - Loading edit form for: " + id);
        
        // Load customer data
        Map<String, String> customer = new HashMap<>();
        customer.put("id", id);
        customer.put("name", "Customer " + id);
        
        request.setAttribute("customer", customer);
        response.setContent("<html><body>Edit Customer Form</body></html>");
    }
    
    public void handlePost(HttpRequest request, HttpResponse response) {
        String id = request.getParameter("id");
        String name = request.getParameter("name");
        System.out.println("  [CustomerEditController] POST - Saving customer: " + id);
        
        // Save customer
        System.out.println("  [CustomerEditController] Updated name to: " + name);
        
        response.redirect("/customers");
    }
}

class CustomerDeletePageController {
    public void handleRequest(HttpRequest request, HttpResponse response) {
        String id = request.getParameter("id");
        System.out.println("  [CustomerDeleteController] Deleting customer: " + id);
        
        // Delete customer
        response.redirect("/customers");
    }
}

/**
 * Example 4: Page Controller with Template Method
 * 
 * Base class with common processing
 */
abstract class BasePageController {
    public final void processRequest(HttpRequest request, HttpResponse response) {
        System.out.println("  [BaseController] Starting request processing");
        
        // Common pre-processing
        authenticate(request);
        authorize(request);
        
        // Template method - subclass implements
        handleRequest(request, response);
        
        // Common post-processing
        logRequest(request);
        
        System.out.println("  [BaseController] Request processing complete");
    }
    
    protected void authenticate(HttpRequest request) {
        System.out.println("  [BaseController] Authenticating...");
    }
    
    protected void authorize(HttpRequest request) {
        System.out.println("  [BaseController] Authorizing...");
    }
    
    protected void logRequest(HttpRequest request) {
        System.out.println("  [BaseController] Logging request...");
    }
    
    protected abstract void handleRequest(HttpRequest request, HttpResponse response);
}

class DashboardPageController extends BasePageController {
    @Override
    protected void handleRequest(HttpRequest request, HttpResponse response) {
        System.out.println("  [DashboardController] Rendering dashboard");
        
        List<String> widgets = Arrays.asList("Sales Chart", "Analytics", "Notifications");
        request.setAttribute("widgets", widgets);
        response.setContent("<html><body>Dashboard</body></html>");
    }
}

class ReportsPageController extends BasePageController {
    @Override
    protected void handleRequest(HttpRequest request, HttpResponse response) {
        System.out.println("  [ReportsController] Generating reports");
        
        String reportType = request.getParameter("type");
        System.out.println("  [ReportsController] Report type: " + reportType);
        
        response.setContent("<html><body>Report: " + reportType + "</body></html>");
    }
}

/**
 * Demonstration of the Page Controller Pattern
 */
public class PageControllerPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Page Controller Pattern Demo ===\n");
        
        // Example 1: Basic Page Controllers
        System.out.println("1. Basic Page Controllers:");
        
        HomePageController homeCtrl = new HomePageController();
        HttpRequest req1 = new HttpRequest();
        HttpResponse resp1 = new HttpResponse();
        homeCtrl.handleRequest(req1, resp1);
        
        LoginPageController loginCtrl = new LoginPageController();
        HttpRequest req2 = new HttpRequest();
        HttpResponse resp2 = new HttpResponse();
        loginCtrl.handleGet(req2, resp2);
        
        HttpRequest req3 = new HttpRequest();
        req3.setParameter("username", "admin");
        req3.setParameter("password", "pass");
        HttpResponse resp3 = new HttpResponse();
        loginCtrl.handlePost(req3, resp3);
        System.out.println("Redirect to: " + resp3.getRedirectUrl());
        
        // Example 2: RESTful API Controllers
        System.out.println("\n2. RESTful API Controllers:");
        
        ProductApiController productApi = new ProductApiController();
        
        HttpRequest req4 = new HttpRequest();
        HttpResponse resp4 = new HttpResponse();
        productApi.handleGetAll(req4, resp4);
        
        HttpRequest req5 = new HttpRequest();
        req5.setParameter("id", "1");
        HttpResponse resp5 = new HttpResponse();
        productApi.handleGetById(req5, resp5);
        
        HttpRequest req6 = new HttpRequest();
        req6.setParameter("name", "Tablet");
        req6.setParameter("price", "399.99");
        HttpResponse resp6 = new HttpResponse();
        productApi.handleCreate(req6, resp6);
        
        // Example 3: CRUD Controllers
        System.out.println("\n3. CRUD Page Controllers:");
        
        CustomerListPageController listCtrl = new CustomerListPageController();
        listCtrl.handleRequest(new HttpRequest(), new HttpResponse());
        
        CustomerEditPageController editCtrl = new CustomerEditPageController();
        HttpRequest editReq = new HttpRequest();
        editReq.setParameter("id", "42");
        editCtrl.handleGet(editReq, new HttpResponse());
        
        // Example 4: Template Method Controllers
        System.out.println("\n4. Base Controller with Template Method:");
        
        DashboardPageController dashCtrl = new DashboardPageController();
        dashCtrl.processRequest(new HttpRequest(), new HttpResponse());
        
        ReportsPageController reportsCtrl = new ReportsPageController();
        HttpRequest reportReq = new HttpRequest();
        reportReq.setParameter("type", "Sales");
        reportsCtrl.processRequest(reportReq, new HttpResponse());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Simple and straightforward");
        System.out.println("✓ One controller per page");
        System.out.println("✓ Easy to understand and implement");
        System.out.println("✓ Good for small to medium apps");
        System.out.println("✓ Direct URL-to-controller mapping");
        
        System.out.println("\n=== vs Front Controller ===");
        System.out.println("• Page Controller: Multiple controllers");
        System.out.println("• Front Controller: Single entry point");
        System.out.println("• Use Page Controller for simpler apps");
        System.out.println("• Use Front Controller for complex routing");
    }
}
