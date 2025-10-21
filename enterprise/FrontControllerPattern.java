package enterprise;

import java.util.*;
import java.util.function.Consumer;

/**
 * Front Controller Pattern
 * 
 * Intent: Provides a centralized entry point for handling all requests
 * in a web application. Coordinates request handling through a single
 * handler object.
 * 
 * Motivation:
 * All requests flow through one entry point.
 * Common processing (security, logging, etc.) in one place.
 * Centralizes navigation and flow control.
 * 
 * Applicability:
 * - Web applications with many pages/actions
 * - Need centralized security/authentication
 * - Common request preprocessing needed
 * - MVC frameworks (Struts, Spring MVC)
 */

/**
 * Example 1: Basic Front Controller
 * 
 * Single servlet handling all requests
 */
class Request {
    private final String uri;
    private final String method;
    private final Map<String, String> parameters;
    private final Map<String, Object> attributes;
    
    public Request(String uri, String method) {
        this.uri = uri;
        this.method = method;
        this.parameters = new HashMap<>();
        this.attributes = new HashMap<>();
    }
    
    public String getUri() { return uri; }
    public String getMethod() { return method; }
    
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

class Response {
    private String view;
    private final Map<String, Object> model;
    private int statusCode;
    
    public Response() {
        this.model = new HashMap<>();
        this.statusCode = 200;
    }
    
    public void setView(String view) { this.view = view; }
    public String getView() { return view; }
    
    public void addModelAttribute(String key, Object value) {
        model.put(key, value);
    }
    
    public Map<String, Object> getModel() { return model; }
    
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public int getStatusCode() { return statusCode; }
}

interface Command {
    void execute(Request request, Response response);
}

class FrontController {
    private final Map<String, Command> commands;
    private final List<Consumer<Request>> filters;
    
    public FrontController() {
        this.commands = new HashMap<>();
        this.filters = new ArrayList<>();
    }
    
    public void registerCommand(String uri, Command command) {
        commands.put(uri, command);
        System.out.println("  [FrontController] Registered command for: " + uri);
    }
    
    public void addFilter(Consumer<Request> filter) {
        filters.add(filter);
    }
    
    public Response handleRequest(Request request) {
        System.out.println("\n  [FrontController] Handling: " + request.getMethod() + " " + request.getUri());
        
        // Apply filters (security, logging, etc.)
        for (Consumer<Request> filter : filters) {
            filter.accept(request);
        }
        
        // Find and execute command
        Command command = commands.get(request.getUri());
        Response response = new Response();
        
        if (command != null) {
            command.execute(request, response);
        } else {
            response.setStatusCode(404);
            response.setView("error/404");
        }
        
        System.out.println("  [FrontController] Response: " + response.getStatusCode() + " - " + response.getView());
        return response;
    }
}

// Example commands
class HomeCommand implements Command {
    @Override
    public void execute(Request request, Response response) {
        System.out.println("    [HomeCommand] Executing...");
        response.setView("home");
        response.addModelAttribute("title", "Welcome Home");
    }
}

class LoginCommand implements Command {
    @Override
    public void execute(Request request, Response response) {
        System.out.println("    [LoginCommand] Executing...");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        if ("admin".equals(username) && "pass".equals(password)) {
            request.setAttribute("user", username);
            response.setView("dashboard");
        } else {
            response.setView("login");
            response.addModelAttribute("error", "Invalid credentials");
        }
    }
}

class UserProfileCommand implements Command {
    @Override
    public void execute(Request request, Response response) {
        System.out.println("    [UserProfileCommand] Executing...");
        String userId = request.getParameter("id");
        response.setView("user/profile");
        response.addModelAttribute("userId", userId);
        response.addModelAttribute("name", "User " + userId);
    }
}

/**
 * Example 2: Front Controller with Dispatcher
 * 
 * Separates dispatching logic from controller
 */
class Dispatcher {
    public void dispatch(Request request, Response response) {
        String view = response.getView();
        
        System.out.println("  [Dispatcher] Rendering view: " + view);
        System.out.println("  [Dispatcher] Model: " + response.getModel());
        
        // In real app, would forward to JSP/template
    }
}

class AdvancedFrontController {
    private final Map<String, Command> commands;
    private final Dispatcher dispatcher;
    private final List<Consumer<Request>> preFilters;
    private final List<Consumer<Response>> postFilters;
    
    public AdvancedFrontController() {
        this.commands = new HashMap<>();
        this.dispatcher = new Dispatcher();
        this.preFilters = new ArrayList<>();
        this.postFilters = new ArrayList<>();
    }
    
    public void registerCommand(String uri, Command command) {
        commands.put(uri, command);
    }
    
    public void addPreFilter(Consumer<Request> filter) {
        preFilters.add(filter);
    }
    
    public void addPostFilter(Consumer<Response> filter) {
        postFilters.add(filter);
    }
    
    public void handleRequest(Request request) {
        System.out.println("\n  [AdvancedFC] Handling: " + request.getUri());
        
        // Pre-processing filters
        for (Consumer<Request> filter : preFilters) {
            filter.accept(request);
        }
        
        // Execute command
        Command command = commands.get(request.getUri());
        Response response = new Response();
        
        if (command != null) {
            command.execute(request, response);
        } else {
            response.setStatusCode(404);
            response.setView("error/404");
        }
        
        // Post-processing filters
        for (Consumer<Response> filter : postFilters) {
            filter.accept(response);
        }
        
        // Dispatch to view
        dispatcher.dispatch(request, response);
    }
}

/**
 * Example 3: RESTful Front Controller
 * 
 * Handles REST API requests with different HTTP methods
 */
class RestFrontController {
    private final Map<String, Map<String, Command>> routes; // URI -> Method -> Command
    
    public RestFrontController() {
        this.routes = new HashMap<>();
    }
    
    public void addRoute(String uri, String method, Command command) {
        routes.computeIfAbsent(uri, k -> new HashMap<>()).put(method, command);
        System.out.println("  [RestFC] Registered: " + method + " " + uri);
    }
    
    public Response handleRequest(Request request) {
        System.out.println("\n  [RestFC] " + request.getMethod() + " " + request.getUri());
        
        Map<String, Command> methods = routes.get(request.getUri());
        Response response = new Response();
        
        if (methods != null) {
            Command command = methods.get(request.getMethod());
            if (command != null) {
                command.execute(request, response);
            } else {
                response.setStatusCode(405); // Method Not Allowed
            }
        } else {
            response.setStatusCode(404); // Not Found
        }
        
        return response;
    }
}

// REST commands
class GetUsersCommand implements Command {
    @Override
    public void execute(Request request, Response response) {
        System.out.println("    [GET /api/users] Fetching all users");
        response.addModelAttribute("users", Arrays.asList("Alice", "Bob", "Charlie"));
    }
}

class CreateUserCommand implements Command {
    @Override
    public void execute(Request request, Response response) {
        String name = request.getParameter("name");
        System.out.println("    [POST /api/users] Creating user: " + name);
        response.setStatusCode(201); // Created
        response.addModelAttribute("id", "123");
    }
}

class UpdateUserCommand implements Command {
    @Override
    public void execute(Request request, Response response) {
        String id = request.getParameter("id");
        System.out.println("    [PUT /api/users] Updating user: " + id);
        response.addModelAttribute("updated", true);
    }
}

/**
 * Demonstration of the Front Controller Pattern
 */
public class FrontControllerPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Front Controller Pattern Demo ===\n");
        
        // Example 1: Basic Front Controller
        System.out.println("1. Basic Front Controller:");
        FrontController fc = new FrontController();
        
        // Register commands
        fc.registerCommand("/home", new HomeCommand());
        fc.registerCommand("/login", new LoginCommand());
        fc.registerCommand("/user/profile", new UserProfileCommand());
        
        // Add logging filter
        fc.addFilter(request -> 
            System.out.println("  [Filter] Logging request: " + request.getUri()));
        
        // Handle requests
        Request req1 = new Request("/home", "GET");
        fc.handleRequest(req1);
        
        Request req2 = new Request("/login", "POST");
        req2.setParameter("username", "admin");
        req2.setParameter("password", "pass");
        fc.handleRequest(req2);
        
        Request req3 = new Request("/user/profile", "GET");
        req3.setParameter("id", "42");
        fc.handleRequest(req3);
        
        Request req4 = new Request("/unknown", "GET");
        fc.handleRequest(req4);
        
        // Example 2: Advanced Front Controller
        System.out.println("\n2. Advanced Front Controller with Dispatcher:");
        AdvancedFrontController afc = new AdvancedFrontController();
        
        afc.registerCommand("/dashboard", (req, resp) -> {
            resp.setView("dashboard");
            resp.addModelAttribute("widgets", Arrays.asList("Sales", "Analytics"));
        });
        
        // Pre-filter: Authentication check
        afc.addPreFilter(request -> 
            System.out.println("  [PreFilter] Checking authentication..."));
        
        // Post-filter: Add headers
        afc.addPostFilter(response -> 
            System.out.println("  [PostFilter] Adding security headers..."));
        
        Request dashReq = new Request("/dashboard", "GET");
        afc.handleRequest(dashReq);
        
        // Example 3: RESTful Front Controller
        System.out.println("\n3. RESTful Front Controller:");
        RestFrontController restFc = new RestFrontController();
        
        restFc.addRoute("/api/users", "GET", new GetUsersCommand());
        restFc.addRoute("/api/users", "POST", new CreateUserCommand());
        restFc.addRoute("/api/users", "PUT", new UpdateUserCommand());
        
        Request getReq = new Request("/api/users", "GET");
        restFc.handleRequest(getReq);
        
        Request postReq = new Request("/api/users", "POST");
        postReq.setParameter("name", "David");
        restFc.handleRequest(postReq);
        
        Request putReq = new Request("/api/users", "PUT");
        putReq.setParameter("id", "123");
        restFc.handleRequest(putReq);
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Single entry point for all requests");
        System.out.println("✓ Centralized security and validation");
        System.out.println("✓ Common preprocessing in one place");
        System.out.println("✓ Easier to apply filters");
        System.out.println("✓ Core of MVC frameworks (Spring MVC, Struts)");
        
        System.out.println("\n=== Components ===");
        System.out.println("• Front Controller: Entry point");
        System.out.println("• Commands/Handlers: Process specific requests");
        System.out.println("• Dispatcher: Forwards to view");
        System.out.println("• Filters: Cross-cutting concerns");
    }
}
