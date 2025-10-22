package cloud;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Valet Key Pattern
 * 
 * Intent: Use a token or key that provides clients with restricted direct access
 * to a specific resource or service, without requiring the application to act as a proxy.
 * 
 * Also Known As: Shared Access Signature (SAS), Presigned URL
 * 
 * Motivation:
 * Client applications often need to read/write to cloud storage. Instead of:
 * - Proxying all data through application servers (bottleneck, cost)
 * - Giving clients full access credentials (security risk)
 * 
 * Valet Key provides time-limited, restricted access tokens.
 * 
 * Applicability:
 * - Direct file uploads to cloud storage (S3, Azure Blob)
 * - Temporary access to private resources
 * - Reducing bandwidth costs through direct access
 * - Offloading data transfer from application servers
 * - Granting limited permissions (read-only, write-only)
 */

/**
 * Permissions that can be granted via valet key
 */
enum Permission {
    READ,
    WRITE,
    DELETE,
    LIST
}

/**
 * Valet key containing access token and permissions
 */
class ValetKey {
    private final String token;
    private final String resourceUri;
    private final Set<Permission> permissions;
    private final Instant expiresAt;
    private final Map<String, String> constraints;
    
    public ValetKey(String token, String resourceUri, Set<Permission> permissions, 
                    Instant expiresAt) {
        this.token = token;
        this.resourceUri = resourceUri;
        this.permissions = new HashSet<>(permissions);
        this.expiresAt = expiresAt;
        this.constraints = new HashMap<>();
    }
    
    public String getToken() { return token; }
    public String getResourceUri() { return resourceUri; }
    public Set<Permission> getPermissions() { return permissions; }
    public Instant getExpiresAt() { return expiresAt; }
    public Map<String, String> getConstraints() { return constraints; }
    
    public boolean isValid() {
        return Instant.now().isBefore(expiresAt);
    }
    
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
    
    public void addConstraint(String key, String value) {
        constraints.put(key, value);
    }
}

/**
 * Request to access a resource using valet key
 */
class ResourceAccessRequest {
    private final String token;
    private final Permission operation;
    private final String clientId;
    private final Map<String, String> metadata;
    
    public ResourceAccessRequest(String token, Permission operation, String clientId) {
        this.token = token;
        this.operation = operation;
        this.clientId = clientId;
        this.metadata = new HashMap<>();
    }
    
    public String getToken() { return token; }
    public Permission getOperation() { return operation; }
    public String getClientId() { return clientId; }
    public Map<String, String> getMetadata() { return metadata; }
}

/**
 * Response indicating access result
 */
class AccessResponse {
    private final boolean allowed;
    private final String message;
    private final Object data;
    
    public AccessResponse(boolean allowed, String message, Object data) {
        this.allowed = allowed;
        this.message = message;
        this.data = data;
    }
    
    public boolean isAllowed() { return allowed; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}

/**
 * Example 1: Basic Valet Key Provider
 * 
 * Generates time-limited tokens with specific permissions.
 * Validates tokens before allowing resource access.
 */
class BasicValetKeyProvider {
    private final Map<String, ValetKey> activeKeys;
    private final String resourceBaseUri;
    
    public BasicValetKeyProvider(String resourceBaseUri) {
        this.activeKeys = new ConcurrentHashMap<>();
        this.resourceBaseUri = resourceBaseUri;
    }
    
    public ValetKey generateKey(String resourcePath, Set<Permission> permissions, 
                                Duration validity) {
        String token = UUID.randomUUID().toString();
        String fullUri = resourceBaseUri + "/" + resourcePath;
        Instant expiresAt = Instant.now().plus(validity);
        
        ValetKey key = new ValetKey(token, fullUri, permissions, expiresAt);
        activeKeys.put(token, key);
        
        return key;
    }
    
    public AccessResponse accessResource(ResourceAccessRequest request) {
        ValetKey key = activeKeys.get(request.getToken());
        
        if (key == null) {
            return new AccessResponse(false, "Invalid token", null);
        }
        
        if (!key.isValid()) {
            activeKeys.remove(request.getToken());
            return new AccessResponse(false, "Token expired", null);
        }
        
        if (!key.hasPermission(request.getOperation())) {
            return new AccessResponse(false, 
                "Permission denied: " + request.getOperation(), null);
        }
        
        // Simulate resource access
        String result = "Accessed resource: " + key.getResourceUri() + 
                       " with operation: " + request.getOperation();
        
        return new AccessResponse(true, "Access granted", result);
    }
    
    public void revokeKey(String token) {
        activeKeys.remove(token);
    }
    
    public int getActiveKeyCount() {
        // Clean up expired keys
        activeKeys.entrySet().removeIf(entry -> !entry.getValue().isValid());
        return activeKeys.size();
    }
}

/**
 * Example 2: Storage Service with Presigned URLs
 * 
 * Simulates cloud storage (S3/Azure Blob) with presigned URL generation.
 * Clients can upload/download directly without going through application server.
 */
class StorageService {
    private final Map<String, byte[]> storage;
    private final Map<String, ValetKey> presignedUrls;
    private final String serviceEndpoint;
    
    public StorageService(String serviceEndpoint) {
        this.storage = new ConcurrentHashMap<>();
        this.presignedUrls = new ConcurrentHashMap<>();
        this.serviceEndpoint = serviceEndpoint;
    }
    
    public String generatePresignedUploadUrl(String objectKey, Duration validity) {
        String token = UUID.randomUUID().toString();
        String resourceUri = serviceEndpoint + "/objects/" + objectKey;
        
        ValetKey key = new ValetKey(
            token,
            resourceUri,
            Set.of(Permission.WRITE),
            Instant.now().plus(validity)
        );
        
        presignedUrls.put(token, key);
        
        return resourceUri + "?token=" + token;
    }
    
    public String generatePresignedDownloadUrl(String objectKey, Duration validity) {
        String token = UUID.randomUUID().toString();
        String resourceUri = serviceEndpoint + "/objects/" + objectKey;
        
        ValetKey key = new ValetKey(
            token,
            resourceUri,
            Set.of(Permission.READ),
            Instant.now().plus(validity)
        );
        
        presignedUrls.put(token, key);
        
        return resourceUri + "?token=" + token;
    }
    
    public AccessResponse upload(String token, String objectKey, byte[] data) {
        ValetKey key = presignedUrls.get(token);
        
        if (key == null || !key.isValid()) {
            return new AccessResponse(false, "Invalid or expired token", null);
        }
        
        if (!key.hasPermission(Permission.WRITE)) {
            return new AccessResponse(false, "Write permission required", null);
        }
        
        storage.put(objectKey, data);
        return new AccessResponse(true, 
            "Uploaded " + data.length + " bytes to " + objectKey, objectKey);
    }
    
    public AccessResponse download(String token, String objectKey) {
        ValetKey key = presignedUrls.get(token);
        
        if (key == null || !key.isValid()) {
            return new AccessResponse(false, "Invalid or expired token", null);
        }
        
        if (!key.hasPermission(Permission.READ)) {
            return new AccessResponse(false, "Read permission required", null);
        }
        
        byte[] data = storage.get(objectKey);
        if (data == null) {
            return new AccessResponse(false, "Object not found", null);
        }
        
        return new AccessResponse(true, 
            "Downloaded " + data.length + " bytes from " + objectKey, data);
    }
    
    public int getStorageSize() {
        return storage.size();
    }
}

/**
 * Example 3: Constrained Valet Key
 * 
 * Adds additional constraints beyond permissions:
 * - IP address restrictions
 * - Content type restrictions
 * - Maximum file size
 * - Single-use tokens
 */
class ConstrainedValetKeyProvider {
    private final Map<String, ValetKey> keys;
    private final Set<String> usedTokens;
    
    public ConstrainedValetKeyProvider() {
        this.keys = new ConcurrentHashMap<>();
        this.usedTokens = ConcurrentHashMap.newKeySet();
    }
    
    public ValetKey generateConstrainedKey(String resourcePath, 
                                           Set<Permission> permissions,
                                           Duration validity,
                                           Map<String, String> constraints) {
        String token = UUID.randomUUID().toString();
        String resourceUri = "storage://" + resourcePath;
        
        ValetKey key = new ValetKey(token, resourceUri, permissions, 
                                    Instant.now().plus(validity));
        
        constraints.forEach(key::addConstraint);
        keys.put(token, key);
        
        return key;
    }
    
    public AccessResponse accessWithConstraints(ResourceAccessRequest request) {
        ValetKey key = keys.get(request.getToken());
        
        if (key == null) {
            return new AccessResponse(false, "Invalid token", null);
        }
        
        if (!key.isValid()) {
            keys.remove(request.getToken());
            return new AccessResponse(false, "Token expired", null);
        }
        
        // Check single-use constraint
        if ("true".equals(key.getConstraints().get("singleUse"))) {
            if (usedTokens.contains(request.getToken())) {
                return new AccessResponse(false, "Token already used", null);
            }
        }
        
        // Check IP constraint
        String allowedIp = key.getConstraints().get("ipAddress");
        String clientIp = request.getMetadata().get("clientIp");
        if (allowedIp != null && !allowedIp.equals(clientIp)) {
            return new AccessResponse(false, 
                "IP address mismatch: expected " + allowedIp, null);
        }
        
        // Check content type constraint
        String allowedContentType = key.getConstraints().get("contentType");
        String actualContentType = request.getMetadata().get("contentType");
        if (allowedContentType != null && !allowedContentType.equals(actualContentType)) {
            return new AccessResponse(false, 
                "Content type not allowed: " + actualContentType, null);
        }
        
        // Check file size constraint
        String maxSizeStr = key.getConstraints().get("maxSizeBytes");
        String actualSizeStr = request.getMetadata().get("sizeBytes");
        if (maxSizeStr != null && actualSizeStr != null) {
            long maxSize = Long.parseLong(maxSizeStr);
            long actualSize = Long.parseLong(actualSizeStr);
            if (actualSize > maxSize) {
                return new AccessResponse(false, 
                    "File size exceeds limit: " + actualSize + " > " + maxSize, null);
            }
        }
        
        // Check permission
        if (!key.hasPermission(request.getOperation())) {
            return new AccessResponse(false, 
                "Permission denied: " + request.getOperation(), null);
        }
        
        // Mark as used if single-use
        if ("true".equals(key.getConstraints().get("singleUse"))) {
            usedTokens.add(request.getToken());
        }
        
        return new AccessResponse(true, "Access granted with constraints", 
            "Resource: " + key.getResourceUri());
    }
}

/**
 * Example 4: Hierarchical Valet Key
 * 
 * Supports wildcards and hierarchical resource access.
 * One key can grant access to multiple related resources.
 */
class HierarchicalValetKeyProvider {
    private final Map<String, ValetKey> keys;
    
    public HierarchicalValetKeyProvider() {
        this.keys = new ConcurrentHashMap<>();
    }
    
    public ValetKey generateHierarchicalKey(String resourcePattern, 
                                            Set<Permission> permissions,
                                            Duration validity) {
        String token = UUID.randomUUID().toString();
        
        ValetKey key = new ValetKey(token, resourcePattern, permissions, 
                                    Instant.now().plus(validity));
        keys.put(token, key);
        
        return key;
    }
    
    public AccessResponse accessResource(String token, String resourcePath, 
                                        Permission operation) {
        ValetKey key = keys.get(token);
        
        if (key == null || !key.isValid()) {
            return new AccessResponse(false, "Invalid or expired token", null);
        }
        
        if (!key.hasPermission(operation)) {
            return new AccessResponse(false, "Permission denied", null);
        }
        
        // Check if resource matches pattern
        String pattern = key.getResourceUri();
        if (!matchesPattern(resourcePath, pattern)) {
            return new AccessResponse(false, 
                "Resource does not match allowed pattern: " + pattern, null);
        }
        
        return new AccessResponse(true, "Access granted", 
            "Accessed: " + resourcePath + " with " + operation);
    }
    
    private boolean matchesPattern(String resourcePath, String pattern) {
        // Support wildcards: /users/123/* matches /users/123/profile, /users/123/settings
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return resourcePath.startsWith(prefix);
        }
        
        // Support recursive wildcards: /users/** matches /users/123/profile/avatar
        if (pattern.contains("/**")) {
            String prefix = pattern.substring(0, pattern.indexOf("/**"));
            return resourcePath.startsWith(prefix);
        }
        
        // Exact match
        return resourcePath.equals(pattern);
    }
}

/**
 * Example 5: Delegated Valet Key System
 * 
 * Allows clients to create sub-keys with reduced permissions.
 * Useful for multi-tier applications where one service needs to grant
 * limited access to another service.
 */
class DelegatedValetKeySystem {
    private final Map<String, ValetKey> keys;
    private final Map<String, String> keyHierarchy; // child -> parent
    
    public DelegatedValetKeySystem() {
        this.keys = new ConcurrentHashMap<>();
        this.keyHierarchy = new ConcurrentHashMap<>();
    }
    
    public ValetKey generateMasterKey(String resourceUri, Set<Permission> permissions, 
                                      Duration validity) {
        String token = "master-" + UUID.randomUUID().toString();
        ValetKey key = new ValetKey(token, resourceUri, permissions, 
                                    Instant.now().plus(validity));
        key.addConstraint("canDelegate", "true");
        keys.put(token, key);
        
        return key;
    }
    
    public ValetKey delegateKey(String parentToken, Set<Permission> reducedPermissions, 
                                Duration validity) {
        ValetKey parentKey = keys.get(parentToken);
        
        if (parentKey == null || !parentKey.isValid()) {
            throw new IllegalArgumentException("Invalid parent token");
        }
        
        if (!"true".equals(parentKey.getConstraints().get("canDelegate"))) {
            throw new IllegalArgumentException("Parent key cannot delegate");
        }
        
        // Ensure child permissions are subset of parent
        if (!parentKey.getPermissions().containsAll(reducedPermissions)) {
            throw new IllegalArgumentException(
                "Child permissions must be subset of parent permissions");
        }
        
        // Child cannot live longer than parent
        Instant childExpiry = Instant.now().plus(validity);
        if (childExpiry.isAfter(parentKey.getExpiresAt())) {
            childExpiry = parentKey.getExpiresAt();
        }
        
        String childToken = "delegated-" + UUID.randomUUID().toString();
        ValetKey childKey = new ValetKey(childToken, parentKey.getResourceUri(), 
                                         reducedPermissions, childExpiry);
        childKey.addConstraint("canDelegate", "false"); // Children can't delegate
        
        keys.put(childToken, childKey);
        keyHierarchy.put(childToken, parentToken);
        
        return childKey;
    }
    
    public AccessResponse accessResource(String token, Permission operation) {
        ValetKey key = keys.get(token);
        
        if (key == null) {
            return new AccessResponse(false, "Invalid token", null);
        }
        
        // Check if parent is still valid (cascading revocation)
        if (!isKeyChainValid(token)) {
            return new AccessResponse(false, "Token chain broken", null);
        }
        
        if (!key.isValid()) {
            return new AccessResponse(false, "Token expired", null);
        }
        
        if (!key.hasPermission(operation)) {
            return new AccessResponse(false, "Permission denied", null);
        }
        
        return new AccessResponse(true, "Access granted via delegation chain", 
            key.getResourceUri());
    }
    
    private boolean isKeyChainValid(String token) {
        String current = token;
        while (current != null) {
            ValetKey key = keys.get(current);
            if (key == null || !key.isValid()) {
                return false;
            }
            current = keyHierarchy.get(current);
        }
        return true;
    }
    
    public void revokeKey(String token) {
        // Revoke key and all its descendants
        keys.remove(token);
        
        List<String> toRevoke = new ArrayList<>();
        for (Map.Entry<String, String> entry : keyHierarchy.entrySet()) {
            if (entry.getValue().equals(token)) {
                toRevoke.add(entry.getKey());
            }
        }
        
        toRevoke.forEach(this::revokeKey);
    }
}

/**
 * Demonstration of the Valet Key Pattern
 */
public class ValetKeyPattern {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Valet Key Pattern Demo ===\n");
        
        // Example 1: Basic Valet Key
        System.out.println("1. Basic Valet Key Provider:");
        BasicValetKeyProvider provider = new BasicValetKeyProvider("https://storage.example.com");
        
        ValetKey readKey = provider.generateKey("documents/report.pdf", 
            Set.of(Permission.READ), Duration.ofMinutes(5));
        
        System.out.println("Generated read-only key for document");
        System.out.println("Token: " + readKey.getToken().substring(0, 8) + "...");
        System.out.println("Expires: " + readKey.getExpiresAt());
        System.out.println("Permissions: " + readKey.getPermissions());
        
        ResourceAccessRequest readRequest = new ResourceAccessRequest(
            readKey.getToken(), Permission.READ, "client-1");
        AccessResponse readResponse = provider.accessResource(readRequest);
        System.out.println("Read access: " + readResponse.getMessage());
        
        ResourceAccessRequest writeRequest = new ResourceAccessRequest(
            readKey.getToken(), Permission.WRITE, "client-1");
        AccessResponse writeResponse = provider.accessResource(writeRequest);
        System.out.println("Write access: " + writeResponse.getMessage());
        
        System.out.println("Active keys: " + provider.getActiveKeyCount());
        
        // Example 2: Storage Service with Presigned URLs
        System.out.println("\n2. Storage Service with Presigned URLs:");
        StorageService storage = new StorageService("https://blob.storage.com");
        
        String uploadUrl = storage.generatePresignedUploadUrl("images/photo.jpg", 
            Duration.ofMinutes(10));
        System.out.println("Presigned upload URL: " + 
            uploadUrl.substring(0, 50) + "...");
        
        String uploadToken = uploadUrl.substring(uploadUrl.indexOf("token=") + 6);
        byte[] imageData = "IMAGE_BINARY_DATA".getBytes();
        
        AccessResponse uploadResponse = storage.upload(uploadToken, "images/photo.jpg", imageData);
        System.out.println("Upload: " + uploadResponse.getMessage());
        
        String downloadUrl = storage.generatePresignedDownloadUrl("images/photo.jpg", 
            Duration.ofMinutes(10));
        System.out.println("Presigned download URL: " + 
            downloadUrl.substring(0, 50) + "...");
        
        String downloadToken = downloadUrl.substring(downloadUrl.indexOf("token=") + 6);
        AccessResponse downloadResponse = storage.download(downloadToken, "images/photo.jpg");
        System.out.println("Download: " + downloadResponse.getMessage());
        System.out.println("Storage size: " + storage.getStorageSize() + " objects");
        
        // Example 3: Constrained Valet Key
        System.out.println("\n3. Constrained Valet Key (with restrictions):");
        ConstrainedValetKeyProvider constrained = new ConstrainedValetKeyProvider();
        
        Map<String, String> constraints = new HashMap<>();
        constraints.put("ipAddress", "192.168.1.100");
        constraints.put("contentType", "image/jpeg");
        constraints.put("maxSizeBytes", "5242880"); // 5MB
        constraints.put("singleUse", "true");
        
        ValetKey constrainedKey = constrained.generateConstrainedKey(
            "uploads/user123/avatar.jpg", 
            Set.of(Permission.WRITE),
            Duration.ofMinutes(5),
            constraints
        );
        
        System.out.println("Generated constrained key with:");
        System.out.println("  - IP restriction: " + constraints.get("ipAddress"));
        System.out.println("  - Content type: " + constraints.get("contentType"));
        System.out.println("  - Max size: 5MB");
        System.out.println("  - Single use only");
        
        ResourceAccessRequest validRequest = new ResourceAccessRequest(
            constrainedKey.getToken(), Permission.WRITE, "client-2");
        validRequest.getMetadata().put("clientIp", "192.168.1.100");
        validRequest.getMetadata().put("contentType", "image/jpeg");
        validRequest.getMetadata().put("sizeBytes", "2097152"); // 2MB
        
        AccessResponse validResponse = constrained.accessWithConstraints(validRequest);
        System.out.println("Valid request: " + validResponse.getMessage());
        
        // Try to reuse single-use token
        AccessResponse reuseResponse = constrained.accessWithConstraints(validRequest);
        System.out.println("Reuse attempt: " + reuseResponse.getMessage());
        
        // Example 4: Hierarchical Valet Key
        System.out.println("\n4. Hierarchical Valet Key (wildcard patterns):");
        HierarchicalValetKeyProvider hierarchical = new HierarchicalValetKeyProvider();
        
        ValetKey wildcardKey = hierarchical.generateHierarchicalKey(
            "/users/123/*", 
            Set.of(Permission.READ, Permission.WRITE),
            Duration.ofHours(1)
        );
        
        System.out.println("Generated key for pattern: /users/123/*");
        
        String[] testPaths = {
            "/users/123/profile",
            "/users/123/settings",
            "/users/456/profile"
        };
        
        for (String path : testPaths) {
            AccessResponse response = hierarchical.accessResource(
                wildcardKey.getToken(), path, Permission.READ);
            System.out.println(path + ": " + 
                (response.isAllowed() ? "ALLOWED" : response.getMessage()));
        }
        
        // Example 5: Delegated Valet Key
        System.out.println("\n5. Delegated Valet Key System:");
        DelegatedValetKeySystem delegated = new DelegatedValetKeySystem();
        
        ValetKey masterKey = delegated.generateMasterKey(
            "storage://company-data/*",
            Set.of(Permission.READ, Permission.WRITE, Permission.DELETE),
            Duration.ofDays(1)
        );
        
        System.out.println("Master key created with full permissions: " + 
            masterKey.getPermissions());
        
        ValetKey readOnlyKey = delegated.delegateKey(
            masterKey.getToken(),
            Set.of(Permission.READ),
            Duration.ofHours(2)
        );
        
        System.out.println("Delegated read-only key from master: " + 
            readOnlyKey.getPermissions());
        
        AccessResponse masterRead = delegated.accessResource(
            masterKey.getToken(), Permission.READ);
        System.out.println("Master key - READ: " + masterRead.getMessage());
        
        AccessResponse delegatedRead = delegated.accessResource(
            readOnlyKey.getToken(), Permission.READ);
        System.out.println("Delegated key - READ: " + delegatedRead.getMessage());
        
        AccessResponse delegatedWrite = delegated.accessResource(
            readOnlyKey.getToken(), Permission.WRITE);
        System.out.println("Delegated key - WRITE: " + delegatedWrite.getMessage());
        
        System.out.println("\nRevoking master key (cascades to delegated keys)...");
        delegated.revokeKey(masterKey.getToken());
        
        AccessResponse afterRevoke = delegated.accessResource(
            readOnlyKey.getToken(), Permission.READ);
        System.out.println("Delegated key after master revoked: " + afterRevoke.getMessage());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Direct client-to-storage access (no proxy bottleneck)");
        System.out.println("✓ Time-limited access tokens (automatic expiration)");
        System.out.println("✓ Fine-grained permissions (read/write/delete)");
        System.out.println("✓ Additional constraints (IP, content type, size)");
        System.out.println("✓ Reduced bandwidth costs for application servers");
        System.out.println("✓ Hierarchical and delegated access support");
    }
}
