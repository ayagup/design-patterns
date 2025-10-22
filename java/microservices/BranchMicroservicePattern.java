package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Branch Microservice Pattern
 * =============================
 * 
 * Intent:
 * A service receives a request and invokes multiple services in parallel
 * (branches), then aggregates their responses.
 * 
 * Also Known As:
 * - Parallel Service Invocation
 * - Fan-Out/Fan-In Pattern
 * 
 * Motivation:
 * - Need to invoke multiple independent services
 * - Services can be called in parallel (no dependencies)
 * - Want to minimize total latency
 * - Aggregate results from all branches
 * 
 * Applicability:
 * - Multiple independent operations needed
 * - Operations can execute concurrently
 * - Need to aggregate results
 * - Want to minimize latency
 * 
 * Structure:
 * Client -> Branch Service -> [Service A || Service B || Service C] -> Aggregate
 * 
 * Benefits:
 * + Reduced latency (parallel execution)
 * + Better resource utilization
 * + Scalability
 * 
 * Drawbacks:
 * - Complexity in error handling
 * - Need to handle partial failures
 * - Synchronization overhead
 */

// ============================================================================
// DOMAIN MODELS
// ============================================================================

class InsuranceQuoteRequest {
    private final String customerId;
    private final String vehicleType;
    private final int driverAge;
    private final String location;
    
    public InsuranceQuoteRequest(String customerId, String vehicleType, int driverAge, String location) {
        this.customerId = customerId;
        this.vehicleType = vehicleType;
        this.driverAge = driverAge;
        this.location = location;
    }
    
    public String getCustomerId() { return customerId; }
    public String getVehicleType() { return vehicleType; }
    public int getDriverAge() { return driverAge; }
    public String getLocation() { return location; }
}

class Quote {
    private final String provider;
    private final double monthlyPremium;
    private final double annualPremium;
    private final String coverageLevel;
    
    public Quote(String provider, double monthlyPremium, String coverageLevel) {
        this.provider = provider;
        this.monthlyPremium = monthlyPremium;
        this.annualPremium = monthlyPremium * 12;
        this.coverageLevel = coverageLevel;
    }
    
    public String getProvider() { return provider; }
    public double getMonthlyPremium() { return monthlyPremium; }
    public double getAnnualPremium() { return annualPremium; }
    public String getCoverageLevel() { return coverageLevel; }
    
    @Override
    public String toString() {
        return String.format("Quote{provider='%s', monthly=$%.2f, annual=$%.2f, coverage='%s'}",
                           provider, monthlyPremium, annualPremium, coverageLevel);
    }
}

class AggregatedQuotes {
    private final List<Quote> quotes;
    private final Quote bestQuote;
    private final long processingTimeMs;
    
    public AggregatedQuotes(List<Quote> quotes, long processingTimeMs) {
        this.quotes = quotes;
        this.bestQuote = findBestQuote(quotes);
        this.processingTimeMs = processingTimeMs;
    }
    
    private Quote findBestQuote(List<Quote> quotes) {
        return quotes.stream()
            .min(Comparator.comparingDouble(Quote::getMonthlyPremium))
            .orElse(null);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AggregatedQuotes{\n");
        sb.append("  quotes received: ").append(quotes.size()).append("\n");
        for (Quote quote : quotes) {
            sb.append("    - ").append(quote).append("\n");
        }
        if (bestQuote != null) {
            sb.append("  BEST QUOTE: ").append(bestQuote).append("\n");
        }
        sb.append("  processing time: ").append(processingTimeMs).append("ms\n");
        sb.append("}");
        return sb.toString();
    }
}

// ============================================================================
// BRANCH SERVICES (Independent insurance providers)
// ============================================================================

class ProviderAService {
    public CompletableFuture<Quote> getQuote(InsuranceQuoteRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [ProviderA] Calculating quote...");
            simulateDelay(150);
            
            // Provider A's pricing logic
            double baseRate = 100.0;
            if (request.getDriverAge() < 25) baseRate *= 1.5;
            if (request.getVehicleType().equals("Sports")) baseRate *= 1.3;
            
            System.out.println("  [ProviderA] Quote ready");
            return new Quote("Provider A", baseRate, "Standard");
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ProviderBService {
    public CompletableFuture<Quote> getQuote(InsuranceQuoteRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [ProviderB] Calculating quote...");
            simulateDelay(200);
            
            // Provider B's pricing logic (more competitive)
            double baseRate = 90.0;
            if (request.getDriverAge() < 25) baseRate *= 1.4;
            if (request.getVehicleType().equals("Sports")) baseRate *= 1.2;
            
            System.out.println("  [ProviderB] Quote ready");
            return new Quote("Provider B", baseRate, "Premium");
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ProviderCService {
    public CompletableFuture<Quote> getQuote(InsuranceQuoteRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [ProviderC] Calculating quote...");
            simulateDelay(120);
            
            // Provider C's pricing logic
            double baseRate = 110.0;
            if (request.getDriverAge() < 25) baseRate *= 1.6;
            if (request.getLocation().equals("Urban")) baseRate *= 1.1;
            
            System.out.println("  [ProviderC] Quote ready");
            return new Quote("Provider C", baseRate, "Basic");
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class ProviderDService {
    public CompletableFuture<Quote> getQuote(InsuranceQuoteRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [ProviderD] Calculating quote...");
            simulateDelay(180);
            
            // Provider D's pricing logic
            double baseRate = 95.0;
            if (request.getDriverAge() >= 25 && request.getDriverAge() <= 60) {
                baseRate *= 0.9; // Discount for prime age
            }
            
            System.out.println("  [ProviderD] Quote ready");
            return new Quote("Provider D", baseRate, "Comprehensive");
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// ============================================================================
// BRANCH MICROSERVICE (Fan-Out/Fan-In)
// ============================================================================

class InsuranceQuoteAggregator {
    private final ProviderAService providerA;
    private final ProviderBService providerB;
    private final ProviderCService providerC;
    private final ProviderDService providerD;
    
    public InsuranceQuoteAggregator() {
        this.providerA = new ProviderAService();
        this.providerB = new ProviderBService();
        this.providerC = new ProviderCService();
        this.providerD = new ProviderDService();
    }
    
    public CompletableFuture<AggregatedQuotes> getQuotes(InsuranceQuoteRequest request) {
        System.out.println("[Aggregator] Requesting quotes from all providers (parallel)...");
        long startTime = System.currentTimeMillis();
        
        // Fan-Out: Invoke all providers in parallel
        CompletableFuture<Quote> quoteA = providerA.getQuote(request);
        CompletableFuture<Quote> quoteB = providerB.getQuote(request);
        CompletableFuture<Quote> quoteC = providerC.getQuote(request);
        CompletableFuture<Quote> quoteD = providerD.getQuote(request);
        
        // Fan-In: Wait for all and aggregate
        return CompletableFuture.allOf(quoteA, quoteB, quoteC, quoteD)
            .thenApply(v -> {
                List<Quote> quotes = new ArrayList<>();
                quotes.add(quoteA.join());
                quotes.add(quoteB.join());
                quotes.add(quoteC.join());
                quotes.add(quoteD.join());
                
                long endTime = System.currentTimeMillis();
                System.out.println("[Aggregator] All quotes received");
                
                return new AggregatedQuotes(quotes, endTime - startTime);
            });
    }
    
    // Variant: With timeout and partial results
    public CompletableFuture<AggregatedQuotes> getQuotesWithTimeout(InsuranceQuoteRequest request, long timeoutMs) {
        System.out.println("[Aggregator] Requesting quotes with " + timeoutMs + "ms timeout...");
        long startTime = System.currentTimeMillis();
        
        // Fan-Out
        CompletableFuture<Quote> quoteA = providerA.getQuote(request);
        CompletableFuture<Quote> quoteB = providerB.getQuote(request);
        CompletableFuture<Quote> quoteC = providerC.getQuote(request);
        CompletableFuture<Quote> quoteD = providerD.getQuote(request);
        
        // Fan-In with timeout
        return CompletableFuture.supplyAsync(() -> {
            List<Quote> quotes = new ArrayList<>();
            long deadline = System.currentTimeMillis() + timeoutMs;
            
            // Try to get each quote with remaining time
            try {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining > 0) quotes.add(quoteA.get(remaining, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                System.out.println("  [Aggregator] ProviderA timed out");
            }
            
            try {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining > 0) quotes.add(quoteB.get(remaining, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                System.out.println("  [Aggregator] ProviderB timed out");
            }
            
            try {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining > 0) quotes.add(quoteC.get(remaining, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                System.out.println("  [Aggregator] ProviderC timed out");
            }
            
            try {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining > 0) quotes.add(quoteD.get(remaining, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                System.out.println("  [Aggregator] ProviderD timed out");
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("[Aggregator] Received " + quotes.size() + " quotes within timeout");
            
            return new AggregatedQuotes(quotes, endTime - startTime);
        });
    }
}

/**
 * Demonstration of Branch Microservice Pattern
 */
public class BranchMicroservicePattern {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Branch Microservice Pattern ===\n");
        
        InsuranceQuoteAggregator aggregator = new InsuranceQuoteAggregator();
        
        System.out.println("--- Scenario 1: Standard Request (Wait for All) ---\n");
        InsuranceQuoteRequest request1 = new InsuranceQuoteRequest("C001", "Sedan", 30, "Suburban");
        AggregatedQuotes result1 = aggregator.getQuotes(request1).get();
        System.out.println("\n" + result1);
        
        System.out.println("\n\n--- Scenario 2: Request with Timeout (Partial Results OK) ---\n");
        InsuranceQuoteRequest request2 = new InsuranceQuoteRequest("C002", "Sports", 22, "Urban");
        AggregatedQuotes result2 = aggregator.getQuotesWithTimeout(request2, 160).get();
        System.out.println("\n" + result2);
        
        System.out.println("\n\n=== Key Points ===");
        System.out.println("1. Parallel invocation - all services called simultaneously");
        System.out.println("2. Reduced latency - total time = slowest service, not sum");
        System.out.println("3. Independent branches - services don't depend on each other");
        System.out.println("4. Aggregation - combines results from all branches");
        System.out.println("5. Timeout handling - can return partial results");
        
        System.out.println("\n=== Benefits ===");
        System.out.println("+ Much faster than sequential (Chained pattern)");
        System.out.println("+ Better resource utilization");
        System.out.println("+ Can handle partial failures gracefully");
        System.out.println("+ Scales horizontally");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("- Price comparison services");
        System.out.println("- Multi-vendor quote aggregation");
        System.out.println("- Parallel data enrichment");
        System.out.println("- Search across multiple sources");
    }
}
