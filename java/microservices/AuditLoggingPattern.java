package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Audit Logging Pattern
 * =======================
 * 
 * Intent:
 * Records all user actions and system events in an immutable audit trail
 * for compliance, security, and forensic analysis.
 * 
 * Also Known As:
 * - Audit Trail Pattern
 * - Event Logging
 * 
 * Motivation:
 * - Compliance requirements (SOX, GDPR, HIPAA)
 * - Security monitoring
 * - Forensic investigation
 * - User accountability
 * - Non-repudiation
 * 
 * Applicability:
 * - Systems handling sensitive data
 * - Compliance requirements
 * - Need to track user actions
 * - Financial systems
 * - Healthcare systems
 * 
 * Key Principles:
 * - Immutable (cannot be modified)
 * - Complete (all actions logged)
 * - Tamper-proof
 * - Searchable
 * 
 * Benefits:
 * + Compliance
 * + Security
 * + Accountability
 * + Forensics
 */

// ============================================================================
// AUDIT EVENT
// ============================================================================

class AuditEvent {
    private final String id;
    private final long timestamp;
    private final String userId;
    private final String action;
    private final String resource;
    private final String resourceId;
    private final Map<String, Object> before;
    private final Map<String, Object> after;
    private final String ipAddress;
    private final String userAgent;
    private final boolean success;
    
    private AuditEvent(Builder builder) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.userId = builder.userId;
        this.action = builder.action;
        this.resource = builder.resource;
        this.resourceId = builder.resourceId;
        this.before = builder.before;
        this.after = builder.after;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
        this.success = builder.success;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public String getResource() { return resource; }
    public String getResourceId() { return resourceId; }
    public boolean isSuccess() { return success; }
    
    @Override
    public String toString() {
        return String.format("[%tT] User:%s Action:%s Resource:%s/%s Success:%b IP:%s",
            timestamp, userId, action, resource, resourceId, success, ipAddress);
    }
    
    public String getDetailedLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(toString()).append("\n");
        if (before != null && !before.isEmpty()) {
            sb.append("  Before: ").append(before).append("\n");
        }
        if (after != null && !after.isEmpty()) {
            sb.append("  After: ").append(after).append("\n");
        }
        return sb.toString();
    }
    
    static class Builder {
        private String userId;
        private String action;
        private String resource;
        private String resourceId;
        private Map<String, Object> before;
        private Map<String, Object> after;
        private String ipAddress;
        private String userAgent;
        private boolean success;
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder action(String action) {
            this.action = action;
            return this;
        }
        
        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }
        
        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }
        
        public Builder before(Map<String, Object> before) {
            this.before = before;
            return this;
        }
        
        public Builder after(Map<String, Object> after) {
            this.after = after;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }
}

// ============================================================================
// AUDIT LOG STORE (Immutable, append-only)
// ============================================================================

class AuditLogStore {
    private final List<AuditEvent> auditLog = new CopyOnWriteArrayList<>();
    
    public void append(AuditEvent event) {
        auditLog.add(event);
        // In production: write to secure storage (blockchain, immutable DB, etc.)
        System.out.println("[AUDIT] " + event);
    }
    
    // Query methods
    public List<AuditEvent> findByUser(String userId) {
        List<AuditEvent> results = new ArrayList<>();
        for (AuditEvent event : auditLog) {
            if (userId.equals(event.getUserId())) {
                results.add(event);
            }
        }
        return results;
    }
    
    public List<AuditEvent> findByResource(String resource, String resourceId) {
        List<AuditEvent> results = new ArrayList<>();
        for (AuditEvent event : auditLog) {
            if (resource.equals(event.getResource()) && resourceId.equals(event.getResourceId())) {
                results.add(event);
            }
        }
        return results;
    }
    
    public List<AuditEvent> findByAction(String action) {
        List<AuditEvent> results = new ArrayList<>();
        for (AuditEvent event : auditLog) {
            if (action.equals(event.getAction())) {
                results.add(event);
            }
        }
        return results;
    }
    
    public List<AuditEvent> findByTimeRange(long startTime, long endTime) {
        List<AuditEvent> results = new ArrayList<>();
        for (AuditEvent event : auditLog) {
            if (event.getTimestamp() >= startTime && event.getTimestamp() <= endTime) {
                results.add(event);
            }
        }
        return results;
    }
    
    public int getTotalEvents() {
        return auditLog.size();
    }
}

// ============================================================================
// AUDITABLE SERVICE
// ============================================================================

class BankAccountService {
    private final AuditLogStore auditLog;
    private final Map<String, Double> accounts = new ConcurrentHashMap<>();
    
    public BankAccountService(AuditLogStore auditLog) {
        this.auditLog = auditLog;
        
        // Initialize accounts
        accounts.put("ACC-001", 1000.0);
        accounts.put("ACC-002", 2000.0);
    }
    
    public void transfer(String userId, String fromAccount, String toAccount, double amount, String ipAddress) {
        System.out.println("\n[BankService] Processing transfer...");
        
        Map<String, Object> before = new HashMap<>();
        before.put("fromBalance", accounts.get(fromAccount));
        before.put("toBalance", accounts.get(toAccount));
        
        boolean success = false;
        try {
            // Validate
            if (accounts.get(fromAccount) < amount) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            
            // Execute transfer
            accounts.put(fromAccount, accounts.get(fromAccount) - amount);
            accounts.put(toAccount, accounts.get(toAccount) + amount);
            
            success = true;
            System.out.println("[BankService] Transfer successful");
            
        } finally {
            Map<String, Object> after = new HashMap<>();
            after.put("fromBalance", accounts.get(fromAccount));
            after.put("toBalance", accounts.get(toAccount));
            after.put("amount", amount);
            
            // Audit log (always logged, success or failure)
            AuditEvent event = AuditEvent.builder()
                .userId(userId)
                .action("TRANSFER")
                .resource("Account")
                .resourceId(fromAccount + "->" + toAccount)
                .before(before)
                .after(after)
                .ipAddress(ipAddress)
                .userAgent("Mobile App")
                .success(success)
                .build();
            
            auditLog.append(event);
        }
    }
    
    public void updateProfile(String userId, String accountId, String field, Object oldValue, Object newValue, String ipAddress) {
        System.out.println("\n[BankService] Updating profile...");
        
        Map<String, Object> before = new HashMap<>();
        before.put(field, oldValue);
        
        Map<String, Object> after = new HashMap<>();
        after.put(field, newValue);
        
        AuditEvent event = AuditEvent.builder()
            .userId(userId)
            .action("UPDATE_PROFILE")
            .resource("Account")
            .resourceId(accountId)
            .before(before)
            .after(after)
            .ipAddress(ipAddress)
            .userAgent("Web Browser")
            .success(true)
            .build();
        
        auditLog.append(event);
        System.out.println("[BankService] Profile updated");
    }
    
    public void login(String userId, String ipAddress, boolean success) {
        System.out.println("\n[BankService] Login attempt...");
        
        AuditEvent event = AuditEvent.builder()
            .userId(userId)
            .action("LOGIN")
            .resource("Session")
            .resourceId("N/A")
            .ipAddress(ipAddress)
            .userAgent("Mobile App")
            .success(success)
            .build();
        
        auditLog.append(event);
    }
}

/**
 * Demonstration of Audit Logging Pattern
 */
public class AuditLoggingPattern {
    public static void main(String[] args) {
        System.out.println("=== Audit Logging Pattern ===\n");
        
        // Create immutable audit log store
        AuditLogStore auditLog = new AuditLogStore();
        
        // Create auditable service
        BankAccountService bankService = new BankAccountService(auditLog);
        
        System.out.println("--- User Actions (All Audited) ---");
        
        // User logs in
        bankService.login("USER-123", "192.168.1.100", true);
        
        // User updates profile
        bankService.updateProfile("USER-123", "ACC-001", "email", 
            "old@example.com", "new@example.com", "192.168.1.100");
        
        // User transfers money
        bankService.transfer("USER-123", "ACC-001", "ACC-002", 150.0, "192.168.1.100");
        
        // Another user logs in
        bankService.login("USER-456", "192.168.1.101", false); // Failed login
        bankService.login("USER-456", "192.168.1.101", true); // Success
        
        // Another transfer
        bankService.transfer("USER-456", "ACC-002", "ACC-001", 50.0, "192.168.1.101");
        
        System.out.println("\n\n--- Audit Queries ---\n");
        
        System.out.println("1. All actions by USER-123:");
        List<AuditEvent> userActions = auditLog.findByUser("USER-123");
        userActions.forEach(event -> System.out.println("  " + event));
        
        System.out.println("\n2. All TRANSFER actions:");
        List<AuditEvent> transfers = auditLog.findByAction("TRANSFER");
        transfers.forEach(event -> System.out.println("  " + event.getDetailedLog()));
        
        System.out.println("\n3. All actions on ACC-001:");
        List<AuditEvent> accountHistory = auditLog.findByResource("Account", "ACC-001");
        accountHistory.forEach(event -> System.out.println("  " + event));
        
        System.out.println("\n\n=== Audit Statistics ===");
        System.out.println("Total audit events: " + auditLog.getTotalEvents());
        System.out.println("Transfers: " + auditLog.findByAction("TRANSFER").size());
        System.out.println("Logins: " + auditLog.findByAction("LOGIN").size());
        System.out.println("Profile updates: " + auditLog.findByAction("UPDATE_PROFILE").size());
        
        System.out.println("\n=== Key Principles ===");
        System.out.println("1. Immutable - audit logs cannot be modified");
        System.out.println("2. Complete - all actions are logged");
        System.out.println("3. Tamper-proof - cryptographic signing in production");
        System.out.println("4. Searchable - can query by user, action, resource, time");
        
        System.out.println("\n=== Compliance Use Cases ===");
        System.out.println("- Financial regulations (SOX, PCI-DSS)");
        System.out.println("- Healthcare (HIPAA)");
        System.out.println("- Data privacy (GDPR)");
        System.out.println("- Government systems");
        System.out.println("- Security forensics");
    }
}
