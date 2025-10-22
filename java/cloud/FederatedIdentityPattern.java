package cloud;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Federated Identity Pattern
 * 
 * Intent: Delegate authentication to an external identity provider (IdP)
 * instead of managing user credentials directly.
 * 
 * Also Known As: Identity Federation, SSO (Single Sign-On), External Authentication
 * 
 * Motivation:
 * Managing user authentication internally has drawbacks:
 * - Security burden of storing passwords
 * - Complex password policies and resets
 * - No single sign-on across multiple applications
 * - Difficult to integrate with enterprise directories
 * 
 * Federated Identity delegates authentication to trusted providers.
 * 
 * Applicability:
 * - Enterprise applications using Active Directory/LDAP
 * - Consumer applications using social login (Google, Facebook)
 * - Multi-tenant SaaS with per-tenant identity providers
 * - Applications requiring SSO across multiple systems
 * - Compliance requirements for centralized identity management
 */

/**
 * User identity from external provider
 */
class FederatedUser {
    private final String id;
    private final String email;
    private final String name;
    private final String providerId;
    private final Map<String, Object> claims;
    private final Instant authenticatedAt;
    
    public FederatedUser(String id, String email, String name, String providerId) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.providerId = providerId;
        this.claims = new HashMap<>();
        this.authenticatedAt = Instant.now();
    }
    
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getProviderId() { return providerId; }
    public Map<String, Object> getClaims() { return claims; }
    public Instant getAuthenticatedAt() { return authenticatedAt; }
    
    public void addClaim(String key, Object value) {
        claims.put(key, value);
    }
}

/**
 * Authentication token from IdP
 */
class AuthenticationToken {
    private final String token;
    private final String providerId;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final Map<String, Object> payload;
    
    public AuthenticationToken(String token, String providerId, Instant expiresAt) {
        this.token = token;
        this.providerId = providerId;
        this.issuedAt = Instant.now();
        this.expiresAt = expiresAt;
        this.payload = new HashMap<>();
    }
    
    public String getToken() { return token; }
    public String getProviderId() { return providerId; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Map<String, Object> getPayload() { return payload; }
    
    public boolean isValid() {
        return Instant.now().isBefore(expiresAt);
    }
}

/**
 * Authentication result
 */
class AuthenticationResult {
    private final boolean success;
    private final FederatedUser user;
    private final String errorMessage;
    
    public AuthenticationResult(boolean success, FederatedUser user, String errorMessage) {
        this.success = success;
        this.user = user;
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccess() { return success; }
    public FederatedUser getUser() { return user; }
    public String getErrorMessage() { return errorMessage; }
    
    public static AuthenticationResult success(FederatedUser user) {
        return new AuthenticationResult(true, user, null);
    }
    
    public static AuthenticationResult failure(String errorMessage) {
        return new AuthenticationResult(false, null, errorMessage);
    }
}

/**
 * Example 1: Basic Identity Provider Interface
 * 
 * Defines contract for external identity providers.
 * Supports multiple IdPs (Google, Azure AD, etc.).
 */
interface IdentityProvider {
    String getProviderId();
    String getAuthorizationUrl(String redirectUri, String state);
    AuthenticationToken exchangeCodeForToken(String code);
    FederatedUser getUserInfo(AuthenticationToken token);
}

/**
 * Example 2: OAuth2 Identity Provider
 * 
 * Implements OAuth2 / OpenID Connect flow.
 * Simulates Google, GitHub, or other OAuth providers.
 */
class OAuth2IdentityProvider implements IdentityProvider {
    private final String providerId;
    private final String authEndpoint;
    private final String clientId;
    
    // Simulated storage
    private final Map<String, String> authorizationCodes;
    private final Map<String, AuthenticationToken> tokens;
    private final Map<String, FederatedUser> users;
    
    public OAuth2IdentityProvider(String providerId, String clientId, String clientSecret) {
        this.providerId = providerId;
        this.authEndpoint = "https://" + providerId + "/oauth/authorize";
        this.clientId = clientId;
        this.authorizationCodes = new ConcurrentHashMap<>();
        this.tokens = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        
        // Note: In production, would use tokenEndpoint, userInfoEndpoint, and clientSecret
        // for actual OAuth2 flows. Simplified here for demonstration.
        
        // Pre-populate with test users
        initializeTestUsers();
    }
    
    private void initializeTestUsers() {
        FederatedUser user1 = new FederatedUser(
            "google-123", "alice@example.com", "Alice Smith", providerId);
        user1.addClaim("picture", "https://example.com/alice.jpg");
        user1.addClaim("locale", "en-US");
        users.put("alice@example.com", user1);
        
        FederatedUser user2 = new FederatedUser(
            "google-456", "bob@example.com", "Bob Jones", providerId);
        user2.addClaim("picture", "https://example.com/bob.jpg");
        users.put("bob@example.com", user2);
    }
    
    @Override
    public String getProviderId() {
        return providerId;
    }
    
    @Override
    public String getAuthorizationUrl(String redirectUri, String state) {
        // Generate authorization code
        String code = UUID.randomUUID().toString();
        authorizationCodes.put(code, state);
        
        return authEndpoint + 
               "?client_id=" + clientId +
               "&redirect_uri=" + redirectUri +
               "&response_type=code" +
               "&state=" + state +
               "&scope=openid profile email" +
               "&code=" + code; // Simplified: include code in URL
    }
    
    @Override
    public AuthenticationToken exchangeCodeForToken(String code) {
        if (!authorizationCodes.containsKey(code)) {
            throw new IllegalArgumentException("Invalid authorization code");
        }
        
        String tokenValue = "access_token_" + UUID.randomUUID().toString();
        AuthenticationToken token = new AuthenticationToken(
            tokenValue, 
            providerId,
            Instant.now().plusSeconds(3600) // 1 hour
        );
        
        token.getPayload().put("code", code);
        tokens.put(tokenValue, token);
        authorizationCodes.remove(code);
        
        return token;
    }
    
    @Override
    public FederatedUser getUserInfo(AuthenticationToken token) {
        if (!token.isValid()) {
            throw new IllegalArgumentException("Token expired");
        }
        
        // In real implementation, would call userInfoEndpoint
        // For demo, return first user
        return users.values().iterator().next();
    }
}

/**
 * Example 3: SAML Identity Provider
 * 
 * Implements SAML 2.0 for enterprise SSO.
 * Common in corporate environments with Active Directory.
 */
class SAMLIdentityProvider implements IdentityProvider {
    private final String providerId;
    private final String ssoUrl;
    private final String entityId;
    private final Map<String, FederatedUser> userDirectory;
    
    public SAMLIdentityProvider(String providerId, String entityId) {
        this.providerId = providerId;
        this.entityId = entityId;
        this.ssoUrl = "https://" + providerId + "/saml/sso";
        this.userDirectory = new ConcurrentHashMap<>();
        
        // Simulate enterprise directory
        FederatedUser user1 = new FederatedUser(
            "ad-001", "john.doe@company.com", "John Doe", providerId);
        user1.addClaim("department", "Engineering");
        user1.addClaim("role", "Senior Developer");
        userDirectory.put("john.doe@company.com", user1);
        
        FederatedUser user2 = new FederatedUser(
            "ad-002", "jane.smith@company.com", "Jane Smith", providerId);
        user2.addClaim("department", "Product");
        user2.addClaim("role", "Product Manager");
        userDirectory.put("jane.smith@company.com", user2);
    }
    
    @Override
    public String getProviderId() {
        return providerId;
    }
    
    @Override
    public String getAuthorizationUrl(String redirectUri, String state) {
        // SAML uses POST binding, but simplified here
        String samlRequest = Base64.getEncoder().encodeToString(
            ("<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\">" +
             "<saml:Issuer>" + entityId + "</saml:Issuer>" +
             "</samlp:AuthnRequest>").getBytes()
        );
        
        return ssoUrl + "?SAMLRequest=" + samlRequest + "&RelayState=" + state;
    }
    
    @Override
    public AuthenticationToken exchangeCodeForToken(String samlResponse) {
        // Validate and parse SAML response
        String tokenValue = "saml_token_" + UUID.randomUUID().toString();
        AuthenticationToken token = new AuthenticationToken(
            tokenValue,
            providerId,
            Instant.now().plusSeconds(28800) // 8 hours
        );
        
        token.getPayload().put("samlResponse", samlResponse);
        token.getPayload().put("sessionIndex", UUID.randomUUID().toString());
        
        return token;
    }
    
    @Override
    public FederatedUser getUserInfo(AuthenticationToken token) {
        if (!token.isValid()) {
            throw new IllegalArgumentException("SAML token expired");
        }
        
        // Extract user from SAML assertions
        return userDirectory.values().iterator().next();
    }
}

/**
 * Example 4: Federated Authentication Service
 * 
 * Coordinates authentication across multiple identity providers.
 * Manages provider discovery and token validation.
 */
class FederatedAuthenticationService {
    private final Map<String, IdentityProvider> providers;
    private final Map<String, FederatedUser> activeSessions;
    
    public FederatedAuthenticationService() {
        this.providers = new ConcurrentHashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
    }
    
    public void registerProvider(IdentityProvider provider) {
        providers.put(provider.getProviderId(), provider);
        System.out.println("Registered identity provider: " + provider.getProviderId());
    }
    
    public List<String> getAvailableProviders() {
        return new ArrayList<>(providers.keySet());
    }
    
    public String initiateAuthentication(String providerId, String redirectUri) {
        IdentityProvider provider = providers.get(providerId);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider: " + providerId);
        }
        
        String state = UUID.randomUUID().toString();
        return provider.getAuthorizationUrl(redirectUri, state);
    }
    
    public AuthenticationResult completeAuthentication(String providerId, String code) {
        IdentityProvider provider = providers.get(providerId);
        if (provider == null) {
            return AuthenticationResult.failure("Unknown provider: " + providerId);
        }
        
        try {
            // Exchange code for token
            AuthenticationToken token = provider.exchangeCodeForToken(code);
            
            // Get user info from IdP
            FederatedUser user = provider.getUserInfo(token);
            
            // Create local session
            String sessionId = UUID.randomUUID().toString();
            activeSessions.put(sessionId, user);
            
            return AuthenticationResult.success(user);
            
        } catch (Exception e) {
            return AuthenticationResult.failure("Authentication failed: " + e.getMessage());
        }
    }
    
    public FederatedUser getSessionUser(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    public void logout(String sessionId) {
        activeSessions.remove(sessionId);
    }
    
    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeProviders", providers.size());
        stats.put("activeSessions", activeSessions.size());
        
        Map<String, Long> sessionsByProvider = new HashMap<>();
        activeSessions.values().forEach(user -> {
            sessionsByProvider.merge(user.getProviderId(), 1L, Long::sum);
        });
        stats.put("sessionsByProvider", sessionsByProvider);
        
        return stats;
    }
}

/**
 * Example 5: Multi-Tenant Federated Identity
 * 
 * Each tenant can configure their own identity provider.
 * Supports per-tenant SAML, OAuth, or local authentication.
 */
class MultiTenantFederatedAuth {
    private final Map<String, TenantIdentityConfig> tenantConfigs;
    private final Map<String, IdentityProvider> providerCache;
    
    static class TenantIdentityConfig {
        String tenantId;
        String providerType; // "oauth2", "saml", "local"
        Map<String, String> providerSettings;
        
        TenantIdentityConfig(String tenantId, String providerType) {
            this.tenantId = tenantId;
            this.providerType = providerType;
            this.providerSettings = new HashMap<>();
        }
    }
    
    public MultiTenantFederatedAuth() {
        this.tenantConfigs = new ConcurrentHashMap<>();
        this.providerCache = new ConcurrentHashMap<>();
    }
    
    public void configureTenant(String tenantId, String providerType, 
                                Map<String, String> settings) {
        TenantIdentityConfig config = new TenantIdentityConfig(tenantId, providerType);
        config.providerSettings.putAll(settings);
        tenantConfigs.put(tenantId, config);
        
        System.out.println("Configured tenant " + tenantId + 
                         " with " + providerType + " identity provider");
    }
    
    public AuthenticationResult authenticate(String tenantId, String username, 
                                            String credential) {
        TenantIdentityConfig config = tenantConfigs.get(tenantId);
        if (config == null) {
            return AuthenticationResult.failure("Tenant not configured: " + tenantId);
        }
        
        IdentityProvider provider = getOrCreateProvider(config);
        
        try {
            // Simplified authentication flow
            AuthenticationToken token = provider.exchangeCodeForToken(credential);
            FederatedUser user = provider.getUserInfo(token);
            
            // Add tenant information to user
            user.addClaim("tenantId", tenantId);
            
            return AuthenticationResult.success(user);
            
        } catch (Exception e) {
            return AuthenticationResult.failure(
                "Authentication failed for tenant " + tenantId + ": " + e.getMessage());
        }
    }
    
    private IdentityProvider getOrCreateProvider(TenantIdentityConfig config) {
        String cacheKey = config.tenantId + ":" + config.providerType;
        
        return providerCache.computeIfAbsent(cacheKey, k -> {
            switch (config.providerType) {
                case "oauth2":
                    return new OAuth2IdentityProvider(
                        config.tenantId + "-oauth",
                        config.providerSettings.get("clientId"),
                        config.providerSettings.get("clientSecret")
                    );
                case "saml":
                    return new SAMLIdentityProvider(
                        config.tenantId + "-saml",
                        config.providerSettings.get("entityId")
                    );
                default:
                    throw new IllegalArgumentException(
                        "Unknown provider type: " + config.providerType);
            }
        });
    }
    
    public Map<String, Object> getTenantStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTenants", tenantConfigs.size());
        
        Map<String, Long> tenantsByType = new HashMap<>();
        tenantConfigs.values().forEach(config -> {
            tenantsByType.merge(config.providerType, 1L, Long::sum);
        });
        stats.put("tenantsByProviderType", tenantsByType);
        
        return stats;
    }
}

/**
 * Demonstration of the Federated Identity Pattern
 */
public class FederatedIdentityPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Federated Identity Pattern Demo ===\n");
        
        // Example 1: OAuth2 Provider
        System.out.println("1. OAuth2 Identity Provider (Google-style):");
        OAuth2IdentityProvider google = new OAuth2IdentityProvider(
            "google.com", "client123", "secret456");
        
        String authUrl = google.getAuthorizationUrl(
            "https://myapp.com/callback", "state-xyz");
        System.out.println("Authorization URL: " + 
            authUrl.substring(0, 80) + "...");
        
        // Extract code from URL (simplified)
        String code = authUrl.substring(authUrl.indexOf("code=") + 5);
        
        AuthenticationToken token = google.exchangeCodeForToken(code);
        System.out.println("Access token: " + token.getToken().substring(0, 20) + "...");
        System.out.println("Expires: " + token.getExpiresAt());
        
        FederatedUser user = google.getUserInfo(token);
        System.out.println("User: " + user.getName() + " (" + user.getEmail() + ")");
        System.out.println("Claims: " + user.getClaims());
        
        // Example 2: SAML Provider
        System.out.println("\n2. SAML Identity Provider (Enterprise SSO):");
        SAMLIdentityProvider adfs = new SAMLIdentityProvider(
            "adfs.company.com", "urn:myapp:entity");
        
        String samlUrl = adfs.getAuthorizationUrl(
            "https://myapp.com/saml/acs", "relay-state-123");
        System.out.println("SAML SSO URL: " + 
            samlUrl.substring(0, 60) + "...");
        
        // Simulate SAML response
        String samlResponse = "PHNhbWxwOlJlc3BvbnNl...";
        AuthenticationToken samlToken = adfs.exchangeCodeForToken(samlResponse);
        System.out.println("SAML token: " + samlToken.getToken().substring(0, 20) + "...");
        
        FederatedUser enterpriseUser = adfs.getUserInfo(samlToken);
        System.out.println("Enterprise user: " + enterpriseUser.getName() + 
            " (" + enterpriseUser.getEmail() + ")");
        System.out.println("Department: " + enterpriseUser.getClaims().get("department"));
        System.out.println("Role: " + enterpriseUser.getClaims().get("role"));
        
        // Example 3: Federated Authentication Service
        System.out.println("\n3. Federated Authentication Service (Multiple IdPs):");
        FederatedAuthenticationService authService = new FederatedAuthenticationService();
        
        authService.registerProvider(google);
        authService.registerProvider(adfs);
        
        System.out.println("Available providers: " + authService.getAvailableProviders());
        
        // User chooses Google
        System.out.println("\nUser selects Google for authentication...");
        String googleAuthUrl = authService.initiateAuthentication(
            "google.com", "https://myapp.com/callback");
        System.out.println("Redirecting to: " + 
            googleAuthUrl.substring(0, 60) + "...");
        
        String googleCode = googleAuthUrl.substring(
            googleAuthUrl.indexOf("code=") + 5);
        AuthenticationResult result = authService.completeAuthentication(
            "google.com", googleCode);
        
        if (result.isSuccess()) {
            System.out.println("Authentication successful!");
            System.out.println("Welcome: " + result.getUser().getName());
        } else {
            System.out.println("Authentication failed: " + result.getErrorMessage());
        }
        
        System.out.println("\nSession statistics: " + 
            authService.getSessionStatistics());
        
        // Example 4: User chooses SAML
        System.out.println("\n4. Enterprise User Authentication via SAML:");
        String samlAuthUrl = authService.initiateAuthentication(
            "adfs.company.com", "https://myapp.com/saml/acs");
        System.out.println("Redirecting to SAML SSO: " + 
            samlAuthUrl.substring(0, 60) + "...");
        
        AuthenticationResult samlResult = authService.completeAuthentication(
            "adfs.company.com", samlResponse);
        
        if (samlResult.isSuccess()) {
            System.out.println("SAML authentication successful!");
            System.out.println("Enterprise user: " + samlResult.getUser().getName());
        }
        
        // Example 5: Multi-Tenant Federated Identity
        System.out.println("\n5. Multi-Tenant Federated Identity:");
        MultiTenantFederatedAuth multiTenant = new MultiTenantFederatedAuth();
        
        // Tenant A uses OAuth2
        Map<String, String> tenantASettings = new HashMap<>();
        tenantASettings.put("clientId", "tenant-a-client");
        tenantASettings.put("clientSecret", "tenant-a-secret");
        multiTenant.configureTenant("tenant-a", "oauth2", tenantASettings);
        
        // Tenant B uses SAML
        Map<String, String> tenantBSettings = new HashMap<>();
        tenantBSettings.put("entityId", "urn:tenant-b:entity");
        multiTenant.configureTenant("tenant-b", "saml", tenantBSettings);
        
        System.out.println("\nAuthenticating user for Tenant A (OAuth2):");
        AuthenticationResult tenantAResult = multiTenant.authenticate(
            "tenant-a", "user@tenanta.com", "oauth-code-123");
        
        if (tenantAResult.isSuccess()) {
            System.out.println("Tenant A user authenticated: " + 
                tenantAResult.getUser().getName());
            System.out.println("Tenant ID: " + 
                tenantAResult.getUser().getClaims().get("tenantId"));
        }
        
        System.out.println("\nAuthenticating user for Tenant B (SAML):");
        AuthenticationResult tenantBResult = multiTenant.authenticate(
            "tenant-b", "user@tenantb.com", "saml-response-456");
        
        if (tenantBResult.isSuccess()) {
            System.out.println("Tenant B user authenticated: " + 
                tenantBResult.getUser().getName());
        }
        
        System.out.println("\nMulti-tenant statistics: " + 
            multiTenant.getTenantStatistics());
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Delegates authentication to trusted providers");
        System.out.println("✓ No password storage or management");
        System.out.println("✓ Single sign-on (SSO) across applications");
        System.out.println("✓ Enterprise directory integration (AD, LDAP)");
        System.out.println("✓ Social login support (Google, Facebook, GitHub)");
        System.out.println("✓ Per-tenant identity provider configuration");
        System.out.println("✓ Centralized identity and access management");
    }
}
