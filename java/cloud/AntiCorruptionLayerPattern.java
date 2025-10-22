package cloud;

import java.util.*;

/**
 * ANTI-CORRUPTION LAYER PATTERN
 * 
 * Implements a facade or adapter layer between different subsystems that don't share
 * the same semantics. This pattern is especially useful when integrating with legacy
 * systems or external services with different domain models.
 * 
 * Benefits:
 * - Isolates modern code from legacy systems
 * - Translates between different domain models
 * - Prevents legacy code from polluting new codebase
 * - Enables gradual system migration
 * - Maintains clean architecture boundaries
 * 
 * Use Cases:
 * - Legacy system integration
 * - Third-party API integration
 * - Microservices with different models
 * - Gradual system modernization
 * - Cross-context communication in DDD
 */

// Modern System - Clean domain model
class Customer {
    private final String customerId;
    private final String fullName;
    private final String emailAddress;
    private final Address address;
    private final CustomerStatus status;
    
    public Customer(String customerId, String fullName, String emailAddress, 
                   Address address, CustomerStatus status) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.address = address;
        this.status = status;
    }
    
    public String getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public String getEmailAddress() { return emailAddress; }
    public Address getAddress() { return address; }
    public CustomerStatus getStatus() { return status; }
    
    @Override
    public String toString() {
        return "Customer{id='" + customerId + "', name='" + fullName + 
               "', email='" + emailAddress + "', status=" + status + "}";
    }
}

class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;
    
    public Address(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getCountry() { return country; }
    
    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
}

enum CustomerStatus {
    ACTIVE, INACTIVE, SUSPENDED, PENDING
}

// Legacy System - Poor naming, flat structure, different conventions
class LegacyCustomerRecord {
    public int cust_id;
    public String fname;
    public String lname;
    public String email_addr;
    public String addr_line1;
    public String addr_city;
    public String addr_state;
    public String addr_zip;
    public String addr_country;
    public int status_code; // 1=active, 0=inactive, 2=suspended, 3=pending
    
    public LegacyCustomerRecord(int cust_id, String fname, String lname, String email_addr,
                               String addr_line1, String addr_city, String addr_state,
                               String addr_zip, String addr_country, int status_code) {
        this.cust_id = cust_id;
        this.fname = fname;
        this.lname = lname;
        this.email_addr = email_addr;
        this.addr_line1 = addr_line1;
        this.addr_city = addr_city;
        this.addr_state = addr_state;
        this.addr_zip = addr_zip;
        this.addr_country = addr_country;
        this.status_code = status_code;
    }
    
    @Override
    public String toString() {
        return "LegacyCustomerRecord{cust_id=" + cust_id + ", fname='" + fname + 
               "', lname='" + lname + "', status_code=" + status_code + "}";
    }
}

// Legacy System Interface
class LegacyCustomerService {
    private final Map<Integer, LegacyCustomerRecord> database = new HashMap<>();
    private int nextId = 1000;
    
    public int createCustomer(String fname, String lname, String email_addr,
                             String addr_line1, String addr_city, String addr_state,
                             String addr_zip, String addr_country) {
        int id = nextId++;
        LegacyCustomerRecord record = new LegacyCustomerRecord(
            id, fname, lname, email_addr, addr_line1, addr_city, 
            addr_state, addr_zip, addr_country, 1 // active by default
        );
        database.put(id, record);
        System.out.println("  [LEGACY] Created: " + record);
        return id;
    }
    
    public LegacyCustomerRecord getCustomer(int cust_id) {
        System.out.println("  [LEGACY] Fetching customer ID: " + cust_id);
        return database.get(cust_id);
    }
    
    public void updateCustomerStatus(int cust_id, int status_code) {
        LegacyCustomerRecord record = database.get(cust_id);
        if (record != null) {
            record.status_code = status_code;
            System.out.println("  [LEGACY] Updated status: " + cust_id + " -> " + status_code);
        }
    }
    
    public List<LegacyCustomerRecord> getAllCustomers() {
        System.out.println("  [LEGACY] Fetching all customers");
        return new ArrayList<>(database.values());
    }
}

// Anti-Corruption Layer - Translates between modern and legacy models
class CustomerAntiCorruptionLayer {
    private final LegacyCustomerService legacyService;
    
    public CustomerAntiCorruptionLayer(LegacyCustomerService legacyService) {
        this.legacyService = legacyService;
    }
    
    // Translate from modern to legacy
    public String createCustomer(Customer customer) {
        System.out.println("\n[ACL] Translating modern Customer to legacy format...");
        
        // Extract name parts
        String[] nameParts = customer.getFullName().split(" ", 2);
        String fname = nameParts[0];
        String lname = nameParts.length > 1 ? nameParts[1] : "";
        
        // Extract address parts
        Address addr = customer.getAddress();
        
        // Create in legacy system
        int legacyId = legacyService.createCustomer(
            fname, lname, customer.getEmailAddress(),
            addr.getStreet(), addr.getCity(), addr.getState(),
            addr.getZipCode(), addr.getCountry()
        );
        
        // Return modern ID format
        String modernId = "CUST-" + legacyId;
        System.out.println("[ACL] Translation complete: " + customer.getCustomerId() + 
                         " -> Legacy ID " + legacyId);
        return modernId;
    }
    
    // Translate from legacy to modern
    public Customer getCustomer(String customerId) {
        System.out.println("\n[ACL] Translating customer ID to legacy format...");
        
        // Parse modern ID to legacy ID
        int legacyId = Integer.parseInt(customerId.replace("CUST-", ""));
        
        // Fetch from legacy system
        LegacyCustomerRecord legacyRecord = legacyService.getCustomer(legacyId);
        
        if (legacyRecord == null) {
            return null;
        }
        
        // Translate to modern model
        Customer customer = translateToModern(legacyRecord);
        System.out.println("[ACL] Translation complete: Legacy ID " + legacyId + 
                         " -> " + customer);
        return customer;
    }
    
    // Update status with translation
    public void updateCustomerStatus(String customerId, CustomerStatus status) {
        System.out.println("\n[ACL] Translating status update...");
        
        int legacyId = Integer.parseInt(customerId.replace("CUST-", ""));
        int legacyStatusCode = translateStatusToLegacy(status);
        
        legacyService.updateCustomerStatus(legacyId, legacyStatusCode);
        System.out.println("[ACL] Status translated: " + status + " -> code " + legacyStatusCode);
    }
    
    // Get all customers with translation
    public List<Customer> getAllCustomers() {
        System.out.println("\n[ACL] Fetching and translating all customers...");
        
        List<LegacyCustomerRecord> legacyRecords = legacyService.getAllCustomers();
        List<Customer> customers = new ArrayList<>();
        
        for (LegacyCustomerRecord record : legacyRecords) {
            customers.add(translateToModern(record));
        }
        
        System.out.println("[ACL] Translated " + customers.size() + " customers");
        return customers;
    }
    
    // Translation helper: Legacy to Modern
    private Customer translateToModern(LegacyCustomerRecord legacy) {
        String customerId = "CUST-" + legacy.cust_id;
        String fullName = legacy.fname + " " + legacy.lname;
        String emailAddress = legacy.email_addr;
        
        Address address = new Address(
            legacy.addr_line1,
            legacy.addr_city,
            legacy.addr_state,
            legacy.addr_zip,
            legacy.addr_country
        );
        
        CustomerStatus status = translateStatusToModern(legacy.status_code);
        
        return new Customer(customerId, fullName, emailAddress, address, status);
    }
    
    // Translation helper: Status code to enum
    private CustomerStatus translateStatusToModern(int statusCode) {
        switch (statusCode) {
            case 1: return CustomerStatus.ACTIVE;
            case 0: return CustomerStatus.INACTIVE;
            case 2: return CustomerStatus.SUSPENDED;
            case 3: return CustomerStatus.PENDING;
            default: return CustomerStatus.INACTIVE;
        }
    }
    
    // Translation helper: Enum to status code
    private int translateStatusToLegacy(CustomerStatus status) {
        switch (status) {
            case ACTIVE: return 1;
            case INACTIVE: return 0;
            case SUSPENDED: return 2;
            case PENDING: return 3;
            default: return 0;
        }
    }
}

// Modern Service - Uses clean domain model
class ModernCustomerService {
    private final CustomerAntiCorruptionLayer acl;
    
    public ModernCustomerService(CustomerAntiCorruptionLayer acl) {
        this.acl = acl;
    }
    
    public String registerCustomer(String fullName, String email, Address address) {
        System.out.println("\n[MODERN SERVICE] Registering new customer: " + fullName);
        
        // Create customer with modern model
        Customer customer = new Customer(
            UUID.randomUUID().toString(), // Temporary ID
            fullName,
            email,
            address,
            CustomerStatus.PENDING
        );
        
        // Use ACL to persist (delegates to legacy system)
        String customerId = acl.createCustomer(customer);
        
        System.out.println("[MODERN SERVICE] Customer registered with ID: " + customerId);
        return customerId;
    }
    
    public Customer getCustomerDetails(String customerId) {
        System.out.println("\n[MODERN SERVICE] Fetching customer: " + customerId);
        
        // Use ACL to fetch (translates from legacy)
        Customer customer = acl.getCustomer(customerId);
        
        if (customer != null) {
            System.out.println("[MODERN SERVICE] Retrieved: " + customer);
        } else {
            System.out.println("[MODERN SERVICE] Customer not found");
        }
        
        return customer;
    }
    
    public void activateCustomer(String customerId) {
        System.out.println("\n[MODERN SERVICE] Activating customer: " + customerId);
        
        // Use ACL to update status
        acl.updateCustomerStatus(customerId, CustomerStatus.ACTIVE);
        
        System.out.println("[MODERN SERVICE] Customer activated");
    }
    
    public void suspendCustomer(String customerId) {
        System.out.println("\n[MODERN SERVICE] Suspending customer: " + customerId);
        
        acl.updateCustomerStatus(customerId, CustomerStatus.SUSPENDED);
        
        System.out.println("[MODERN SERVICE] Customer suspended");
    }
    
    public List<Customer> listAllCustomers() {
        System.out.println("\n[MODERN SERVICE] Listing all customers");
        
        List<Customer> customers = acl.getAllCustomers();
        
        System.out.println("[MODERN SERVICE] Found " + customers.size() + " customers");
        customers.forEach(c -> System.out.println("  - " + c));
        
        return customers;
    }
}

// Demo
public class AntiCorruptionLayerPattern {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   ANTI-CORRUPTION LAYER PATTERN DEMONSTRATION  ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        
        // Setup: Legacy system exists
        LegacyCustomerService legacyService = new LegacyCustomerService();
        
        System.out.println("\n📦 SCENARIO: Modern application needs to integrate with legacy system");
        System.out.println("   Legacy uses: flat structure, poor naming, integer status codes");
        System.out.println("   Modern uses: clean OOP, enums, proper naming conventions");
        
        // Example 1: Anti-Corruption Layer isolates modern code
        System.out.println("\n\n1. CREATING ANTI-CORRUPTION LAYER");
        System.out.println("──────────────────────────────────");
        
        CustomerAntiCorruptionLayer acl = new CustomerAntiCorruptionLayer(legacyService);
        ModernCustomerService modernService = new ModernCustomerService(acl);
        
        System.out.println("✅ ACL created - acts as translator between systems");
        
        // Example 2: Create customer using modern model
        System.out.println("\n\n2. CREATE CUSTOMER (Modern → Legacy Translation)");
        System.out.println("─────────────────────────────────────────────────");
        
        Address address1 = new Address(
            "123 Main St", "New York", "NY", "10001", "USA"
        );
        String customerId1 = modernService.registerCustomer("Alice Johnson", "alice@example.com", address1);
        
        // Example 3: Create another customer
        Address address2 = new Address(
            "456 Oak Ave", "Los Angeles", "CA", "90001", "USA"
        );
        String customerId2 = modernService.registerCustomer("Bob Smith", "bob@example.com", address2);
        
        // Example 4: Retrieve customer using modern model
        System.out.println("\n\n3. RETRIEVE CUSTOMER (Legacy → Modern Translation)");
        System.out.println("───────────────────────────────────────────────────");
        
        Customer customer = modernService.getCustomerDetails(customerId1);
        System.out.println("\n✅ Modern code works with clean Customer object");
        System.out.println("   No exposure to legacy naming or structure!");
        
        // Example 5: Update customer status
        System.out.println("\n\n4. UPDATE STATUS (Enum → Status Code Translation)");
        System.out.println("──────────────────────────────────────────────────");
        
        modernService.activateCustomer(customerId1);
        modernService.suspendCustomer(customerId2);
        
        // Example 6: List all customers
        System.out.println("\n\n5. LIST ALL CUSTOMERS (Batch Translation)");
        System.out.println("──────────────────────────────────────────────");
        
        List<Customer> allCustomers = modernService.listAllCustomers();
        
        // Example 7: Demonstrate isolation
        System.out.println("\n\n6. ISOLATION DEMONSTRATION");
        System.out.println("──────────────────────────");
        
        System.out.println("\n✅ Benefits of Anti-Corruption Layer:");
        System.out.println("   • Modern code NEVER sees LegacyCustomerRecord");
        System.out.println("   • Modern code uses clean Customer domain model");
        System.out.println("   • Legacy system can be replaced without changing modern code");
        System.out.println("   • Translation logic centralized in ACL");
        System.out.println("   • Different semantics properly mapped");
        
        System.out.println("\n🔄 Translation Examples:");
        System.out.println("   • Legacy: fname + lname ↔ Modern: fullName");
        System.out.println("   • Legacy: status_code (int) ↔ Modern: CustomerStatus (enum)");
        System.out.println("   • Legacy: cust_id (int) ↔ Modern: customerId (String)");
        System.out.println("   • Legacy: flat fields ↔ Modern: Address object");
        
        System.out.println("\n\n✅ Anti-Corruption Layer Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Isolates modern code from legacy systems");
        System.out.println("  • Prevents corruption of clean domain model");
        System.out.println("  • Enables gradual system migration");
        System.out.println("  • Centralizes translation logic");
        System.out.println("  • Maintains architectural boundaries");
        
        System.out.println("\n🏗️  Common Use Cases:");
        System.out.println("  • Legacy system integration");
        System.out.println("  • Third-party API integration");
        System.out.println("  • Domain-Driven Design context mapping");
        System.out.println("  • Strangler Fig pattern implementation");
    }
}
