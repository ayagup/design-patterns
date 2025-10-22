package additional;

import java.util.*;

/**
 * Service Stub Pattern
 * 
 * Intent: Removes dependence on problematic services during testing by
 * providing a stub implementation that returns predictable responses.
 * 
 * Motivation:
 * Enables testing without real external services.
 * Provides predictable responses for testing.
 * Faster test execution.
 * No network dependencies in tests.
 * 
 * Applicability:
 * - Testing with external services
 * - Development without live services
 * - Simulating error conditions
 * - Performance testing
 */

/**
 * Example 1: Payment Gateway Stub
 * 
 * Stub for external payment service
 */
interface PaymentGateway {
    PaymentResult processPayment(String cardNumber, double amount);
    boolean refund(String transactionId);
}

class PaymentResult {
    private final boolean success;
    private final String transactionId;
    private final String message;
    
    public PaymentResult(boolean success, String transactionId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getTransactionId() { return transactionId; }
    public String getMessage() { return message; }
}

class RealPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult processPayment(String cardNumber, double amount) {
        // Would make actual API call to payment processor
        System.out.println("  [RealGateway] Connecting to payment API...");
        System.out.println("  [RealGateway] Processing $" + amount + " on card " + cardNumber);
        return new PaymentResult(true, "TXN-" + System.currentTimeMillis(), "Payment processed");
    }
    
    @Override
    public boolean refund(String transactionId) {
        System.out.println("  [RealGateway] Processing refund for " + transactionId);
        return true;
    }
}

class PaymentGatewayStub implements PaymentGateway {
    private final Map<String, PaymentResult> configuredResponses;
    private int callCount = 0;
    
    public PaymentGatewayStub() {
        this.configuredResponses = new HashMap<>();
    }
    
    public void configureResponse(String cardNumber, PaymentResult result) {
        configuredResponses.put(cardNumber, result);
        System.out.println("  [Stub] Configured response for card: " + cardNumber);
    }
    
    @Override
    public PaymentResult processPayment(String cardNumber, double amount) {
        callCount++;
        System.out.println("  [Stub] Processing payment (call #" + callCount + ")");
        
        PaymentResult configured = configuredResponses.get(cardNumber);
        if (configured != null) {
            return configured;
        }
        
        // Default success response
        return new PaymentResult(true, "STUB-TXN-" + callCount, "Stubbed payment success");
    }
    
    @Override
    public boolean refund(String transactionId) {
        System.out.println("  [Stub] Refund stubbed for: " + transactionId);
        return true;
    }
    
    public int getCallCount() { return callCount; }
}

/**
 * Example 2: Email Service Stub
 * 
 * Stub for email sending
 */
interface EmailService {
    boolean sendEmail(String to, String subject, String body);
    List<String> getSentEmails();
}

class RealEmailService implements EmailService {
    @Override
    public boolean sendEmail(String to, String subject, String body) {
        System.out.println("  [RealEmail] Connecting to SMTP server...");
        System.out.println("  [RealEmail] Sending email to: " + to);
        System.out.println("  [RealEmail] Subject: " + subject);
        return true;
    }
    
    @Override
    public List<String> getSentEmails() {
        throw new UnsupportedOperationException("Real service doesn't track sent emails");
    }
}

class EmailServiceStub implements EmailService {
    private final List<EmailRecord> sentEmails;
    private boolean shouldFail = false;
    
    public EmailServiceStub() {
        this.sentEmails = new ArrayList<>();
    }
    
    @Override
    public boolean sendEmail(String to, String subject, String body) {
        System.out.println("  [Stub] Email captured (not actually sent)");
        System.out.println("  [Stub] To: " + to + ", Subject: " + subject);
        
        if (shouldFail) {
            System.out.println("  [Stub] Simulating send failure");
            return false;
        }
        
        sentEmails.add(new EmailRecord(to, subject, body));
        return true;
    }
    
    @Override
    public List<String> getSentEmails() {
        return sentEmails.stream()
            .map(e -> e.to + ": " + e.subject)
            .toList();
    }
    
    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }
    
    public int getEmailCount() {
        return sentEmails.size();
    }
    
    private static class EmailRecord {
        String to;
        String subject;
        String body;
        
        EmailRecord(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }
    }
}

/**
 * Example 3: Weather API Stub
 * 
 * Stub for external weather service
 */
interface WeatherService {
    WeatherData getCurrentWeather(String city);
    List<WeatherData> getForecast(String city, int days);
}

class WeatherData {
    private final String city;
    private final double temperature;
    private final String condition;
    
    public WeatherData(String city, double temperature, String condition) {
        this.city = city;
        this.temperature = temperature;
        this.condition = condition;
    }
    
    public String getCity() { return city; }
    public double getTemperature() { return temperature; }
    public String getCondition() { return condition; }
    
    @Override
    public String toString() {
        return city + ": " + temperature + "°F, " + condition;
    }
}

class WeatherServiceStub implements WeatherService {
    private final Map<String, WeatherData> stubbedWeather;
    
    public WeatherServiceStub() {
        this.stubbedWeather = new HashMap<>();
        // Pre-configure some cities
        stubbedWeather.put("New York", new WeatherData("New York", 72.0, "Sunny"));
        stubbedWeather.put("London", new WeatherData("London", 65.0, "Cloudy"));
        stubbedWeather.put("Tokyo", new WeatherData("Tokyo", 68.0, "Rainy"));
    }
    
    @Override
    public WeatherData getCurrentWeather(String city) {
        System.out.println("  [Stub] Returning stubbed weather for: " + city);
        
        WeatherData data = stubbedWeather.get(city);
        if (data != null) {
            return data;
        }
        
        // Default weather
        return new WeatherData(city, 70.0, "Clear");
    }
    
    @Override
    public List<WeatherData> getForecast(String city, int days) {
        System.out.println("  [Stub] Returning " + days + "-day forecast for: " + city);
        
        List<WeatherData> forecast = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            forecast.add(new WeatherData(city, 70.0 + i, "Day " + (i + 1)));
        }
        return forecast;
    }
    
    public void setWeather(String city, double temperature, String condition) {
        stubbedWeather.put(city, new WeatherData(city, temperature, condition));
        System.out.println("  [Stub] Configured weather for " + city);
    }
}

/**
 * Example 4: Database Service Stub
 * 
 * Stub for database operations
 */
interface DatabaseService {
    User findUserById(Long id);
    void saveUser(User user);
    List<User> findAllUsers();
}

class User {
    private Long id;
    private String username;
    private String email;
    
    public User(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    
    @Override
    public String toString() {
        return "User[" + id + "]: " + username + " (" + email + ")";
    }
}

class DatabaseServiceStub implements DatabaseService {
    private final Map<Long, User> inMemoryStore;
    private long nextId = 1L;
    
    public DatabaseServiceStub() {
        this.inMemoryStore = new HashMap<>();
        // Pre-populate with test data
        inMemoryStore.put(1L, new User(1L, "testuser", "test@example.com"));
    }
    
    @Override
    public User findUserById(Long id) {
        System.out.println("  [Stub] Finding user by ID: " + id);
        return inMemoryStore.get(id);
    }
    
    @Override
    public void saveUser(User user) {
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        inMemoryStore.put(user.getId(), user);
        System.out.println("  [Stub] Saved user: " + user.getUsername());
    }
    
    @Override
    public List<User> findAllUsers() {
        System.out.println("  [Stub] Finding all users (" + inMemoryStore.size() + " total)");
        return new ArrayList<>(inMemoryStore.values());
    }
    
    public void clear() {
        inMemoryStore.clear();
        System.out.println("  [Stub] Cleared all data");
    }
}

/**
 * Example 5: REST API Stub
 * 
 * Stub for REST API calls
 */
interface RestApiClient {
    ApiResponse get(String endpoint);
    ApiResponse post(String endpoint, String body);
}

class ApiResponse {
    private final int statusCode;
    private final String body;
    
    public ApiResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
    
    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
}

class RestApiStub implements RestApiClient {
    private final Map<String, ApiResponse> getResponses;
    private final Map<String, ApiResponse> postResponses;
    private int networkDelay = 0;
    
    public RestApiStub() {
        this.getResponses = new HashMap<>();
        this.postResponses = new HashMap<>();
    }
    
    public void stubGet(String endpoint, int statusCode, String body) {
        getResponses.put(endpoint, new ApiResponse(statusCode, body));
        System.out.println("  [Stub] Configured GET " + endpoint);
    }
    
    public void stubPost(String endpoint, int statusCode, String body) {
        postResponses.put(endpoint, new ApiResponse(statusCode, body));
        System.out.println("  [Stub] Configured POST " + endpoint);
    }
    
    public void setNetworkDelay(int millis) {
        this.networkDelay = millis;
    }
    
    @Override
    public ApiResponse get(String endpoint) {
        simulateDelay();
        System.out.println("  [Stub] GET " + endpoint);
        
        ApiResponse response = getResponses.get(endpoint);
        return response != null ? response : new ApiResponse(404, "Not Found");
    }
    
    @Override
    public ApiResponse post(String endpoint, String body) {
        simulateDelay();
        System.out.println("  [Stub] POST " + endpoint);
        
        ApiResponse response = postResponses.get(endpoint);
        return response != null ? response : new ApiResponse(201, "{\"id\": 123}");
    }
    
    private void simulateDelay() {
        if (networkDelay > 0) {
            try {
                Thread.sleep(networkDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

/**
 * Demonstration of the Service Stub Pattern
 */
public class ServiceStubPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Service Stub Pattern Demo ===\n");
        
        // Example 1: Payment Gateway Stub
        System.out.println("1. Payment Gateway Stub:");
        PaymentGatewayStub paymentStub = new PaymentGatewayStub();
        
        // Configure specific responses
        paymentStub.configureResponse("1234-5678", 
            new PaymentResult(false, null, "Insufficient funds"));
        
        PaymentResult result1 = paymentStub.processPayment("1234-5678", 100.0);
        System.out.println("  Result: " + result1.getMessage());
        
        PaymentResult result2 = paymentStub.processPayment("9999-0000", 50.0);
        System.out.println("  Result: " + result2.getMessage());
        
        // Example 2: Email Service Stub
        System.out.println("\n2. Email Service Stub:");
        EmailServiceStub emailStub = new EmailServiceStub();
        
        emailStub.sendEmail("alice@example.com", "Welcome", "Hello Alice!");
        emailStub.sendEmail("bob@example.com", "Alert", "System notification");
        
        System.out.println("  Sent emails: " + emailStub.getEmailCount());
        emailStub.getSentEmails().forEach(email -> 
            System.out.println("    - " + email));
        
        // Test failure scenario
        emailStub.setShouldFail(true);
        boolean sent = emailStub.sendEmail("charlie@example.com", "Test", "Body");
        System.out.println("  Send success: " + sent);
        
        // Example 3: Weather Service Stub
        System.out.println("\n3. Weather Service Stub:");
        WeatherServiceStub weatherStub = new WeatherServiceStub();
        
        WeatherData nyWeather = weatherStub.getCurrentWeather("New York");
        System.out.println("  " + nyWeather);
        
        weatherStub.setWeather("San Francisco", 58.0, "Foggy");
        WeatherData sfWeather = weatherStub.getCurrentWeather("San Francisco");
        System.out.println("  " + sfWeather);
        
        List<WeatherData> forecast = weatherStub.getForecast("London", 3);
        System.out.println("  Forecast:");
        forecast.forEach(day -> System.out.println("    - " + day));
        
        // Example 4: Database Service Stub
        System.out.println("\n4. Database Service Stub:");
        DatabaseServiceStub dbStub = new DatabaseServiceStub();
        
        User user1 = dbStub.findUserById(1L);
        System.out.println("  Found: " + user1);
        
        User newUser = new User(null, "newuser", "new@example.com");
        dbStub.saveUser(newUser);
        
        List<User> allUsers = dbStub.findAllUsers();
        System.out.println("  All users:");
        allUsers.forEach(u -> System.out.println("    - " + u));
        
        // Example 5: REST API Stub
        System.out.println("\n5. REST API Stub:");
        RestApiStub apiStub = new RestApiStub();
        
        apiStub.stubGet("/users/123", 200, "{\"id\": 123, \"name\": \"Alice\"}");
        apiStub.stubPost("/users", 201, "{\"id\": 456}");
        
        ApiResponse getResp = apiStub.get("/users/123");
        System.out.println("  GET Response: " + getResp.getStatusCode() + " - " + getResp.getBody());
        
        ApiResponse postResp = apiStub.post("/users", "{\"name\": \"Bob\"}");
        System.out.println("  POST Response: " + postResp.getStatusCode() + " - " + postResp.getBody());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Fast test execution");
        System.out.println("✓ No external dependencies");
        System.out.println("✓ Predictable responses");
        System.out.println("✓ Simulate error conditions");
        System.out.println("✓ Offline development");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Unit testing");
        System.out.println("• Integration testing");
        System.out.println("• Development without services");
        System.out.println("• Performance testing");
        System.out.println("• Error scenario simulation");
    }
}
