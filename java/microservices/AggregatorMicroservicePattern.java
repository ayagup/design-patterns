package microservices;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Aggregator Microservice Pattern
 * =================================
 * 
 * Intent:
 * A dedicated microservice that invokes multiple downstream services,
 * aggregates their responses, and returns a unified result to the client.
 * 
 * Also Known As:
 * - Aggregator Pattern
 * - Service Aggregator
 * - Backend for Frontend (BFF) variant
 * 
 * Motivation:
 * - Clients need data from multiple services
 * - Don't want clients to make multiple calls
 * - Need to aggregate, transform, and enrich data
 * - Want to reduce client complexity and network round trips
 * 
 * Applicability:
 * - Client needs data from multiple microservices
 * - Need to transform/enrich aggregated data
 * - Want to minimize client-side logic
 * - Multiple clients need same aggregated data
 * 
 * Structure:
 * Client -> Aggregator Service -> [Service A, Service B, Service C]
 * Aggregator is a dedicated microservice (not just a library)
 * 
 * Participants:
 * - Aggregator Service: Dedicated service that aggregates
 * - Provider Services: Individual services being aggregated
 * - Client: Consumer of aggregated data
 * 
 * Benefits:
 * + Reduces network round trips
 * + Centralizes aggregation logic
 * + Can cache aggregated results
 * + Can add business logic/transformation
 * + Scales independently
 * 
 * Drawbacks:
 * - Additional service to manage
 * - Increased latency (extra hop)
 * - Single point of failure
 * - Complexity increases
 */

// ============================================================================
// DOMAIN MODELS
// ============================================================================

class Account {
    private final String accountId;
    private final String customerId;
    private final String accountType;
    private final double balance;
    
    public Account(String accountId, String customerId, String accountType, double balance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = balance;
    }
    
    public String getAccountId() { return accountId; }
    public String getCustomerId() { return customerId; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    
    @Override
    public String toString() {
        return String.format("Account{id='%s', type='%s', balance=$%.2f}", accountId, accountType, balance);
    }
}

class Transaction {
    private final String transactionId;
    private final String accountId;
    private final String type;
    private final double amount;
    private final long timestamp;
    
    public Transaction(String transactionId, String accountId, String type, double amount) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    
    @Override
    public String toString() {
        return String.format("Transaction{id='%s', type='%s', amount=$%.2f}", transactionId, type, amount);
    }
}

class CreditScore {
    private final String customerId;
    private final int score;
    private final String rating;
    
    public CreditScore(String customerId, int score) {
        this.customerId = customerId;
        this.score = score;
        this.rating = calculateRating(score);
    }
    
    private String calculateRating(int score) {
        if (score >= 750) return "Excellent";
        if (score >= 700) return "Good";
        if (score >= 650) return "Fair";
        return "Poor";
    }
    
    public String getCustomerId() { return customerId; }
    public int getScore() { return score; }
    public String getRating() { return rating; }
    
    @Override
    public String toString() {
        return String.format("CreditScore{score=%d, rating='%s'}", score, rating);
    }
}

class LoanOffer {
    private final String offerId;
    private final String customerId;
    private final double amount;
    private final double interestRate;
    private final String status;
    
    public LoanOffer(String offerId, String customerId, double amount, double interestRate, String status) {
        this.offerId = offerId;
        this.customerId = customerId;
        this.amount = amount;
        this.interestRate = interestRate;
        this.status = status;
    }
    
    public String getOfferId() { return offerId; }
    public String getCustomerId() { return customerId; }
    public double getAmount() { return amount; }
    public double getInterestRate() { return interestRate; }
    public String getStatus() { return status; }
    
    @Override
    public String toString() {
        return String.format("LoanOffer{id='%s', amount=$%.2f, rate=%.2f%%, status='%s'}",
                           offerId, amount, interestRate, status);
    }
}

// Aggregated result
class CustomerFinancialProfile {
    private final String customerId;
    private final List<Account> accounts;
    private final List<Transaction> recentTransactions;
    private final CreditScore creditScore;
    private final List<LoanOffer> loanOffers;
    private final double totalBalance;
    private final String recommendation;
    
    public CustomerFinancialProfile(String customerId, List<Account> accounts,
                                   List<Transaction> transactions, CreditScore creditScore,
                                   List<LoanOffer> loanOffers) {
        this.customerId = customerId;
        this.accounts = accounts;
        this.recentTransactions = transactions;
        this.creditScore = creditScore;
        this.loanOffers = loanOffers;
        this.totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
        this.recommendation = generateRecommendation();
    }
    
    private String generateRecommendation() {
        if (creditScore.getScore() >= 750 && totalBalance > 10000) {
            return "Excellent candidate for premium credit card";
        } else if (creditScore.getScore() >= 700) {
            return "Consider savings account with higher interest";
        } else {
            return "Focus on building credit score";
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "CustomerFinancialProfile{\n" +
            "  customerId='%s',\n" +
            "  accounts=%d (total=$%.2f),\n" +
            "  recentTransactions=%d,\n" +
            "  creditScore=%s,\n" +
            "  loanOffers=%d,\n" +
            "  recommendation='%s'\n" +
            "}",
            customerId, accounts.size(), totalBalance, recentTransactions.size(),
            creditScore, loanOffers.size(), recommendation
        );
    }
}

// ============================================================================
// PROVIDER MICROSERVICES
// ============================================================================

class AccountService {
    private final Map<String, List<Account>> accountsByCustomer = new ConcurrentHashMap<>();
    
    public AccountService() {
        accountsByCustomer.put("C1", Arrays.asList(
            new Account("A1", "C1", "Checking", 5000.0),
            new Account("A2", "C1", "Savings", 15000.0)
        ));
        accountsByCustomer.put("C2", Arrays.asList(
            new Account("A3", "C2", "Checking", 3000.0)
        ));
    }
    
    public CompletableFuture<List<Account>> getAccountsByCustomer(String customerId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [AccountService] Fetching accounts for customer: " + customerId);
            simulateDelay(100);
            return accountsByCustomer.getOrDefault(customerId, new ArrayList<>());
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class TransactionService {
    private final Map<String, List<Transaction>> transactionsByAccount = new ConcurrentHashMap<>();
    
    public TransactionService() {
        transactionsByAccount.put("A1", Arrays.asList(
            new Transaction("T1", "A1", "DEBIT", 50.0),
            new Transaction("T2", "A1", "CREDIT", 1000.0),
            new Transaction("T3", "A1", "DEBIT", 75.0)
        ));
        transactionsByAccount.put("A2", Arrays.asList(
            new Transaction("T4", "A2", "CREDIT", 500.0)
        ));
    }
    
    public CompletableFuture<List<Transaction>> getRecentTransactions(String accountId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [TransactionService] Fetching transactions for account: " + accountId);
            simulateDelay(150);
            List<Transaction> transactions = transactionsByAccount.getOrDefault(accountId, new ArrayList<>());
            return transactions.stream().limit(limit).collect(Collectors.toList());
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class CreditScoreService {
    private final Map<String, CreditScore> scores = new ConcurrentHashMap<>();
    
    public CreditScoreService() {
        scores.put("C1", new CreditScore("C1", 780));
        scores.put("C2", new CreditScore("C2", 650));
    }
    
    public CompletableFuture<CreditScore> getCreditScore(String customerId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [CreditScoreService] Fetching credit score for: " + customerId);
            simulateDelay(200);
            return scores.getOrDefault(customerId, new CreditScore(customerId, 500));
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

class LoanService {
    private final Map<String, List<LoanOffer>> offersByCustomer = new ConcurrentHashMap<>();
    
    public LoanService() {
        offersByCustomer.put("C1", Arrays.asList(
            new LoanOffer("L1", "C1", 50000.0, 4.5, "APPROVED"),
            new LoanOffer("L2", "C1", 100000.0, 5.0, "PENDING")
        ));
    }
    
    public CompletableFuture<List<LoanOffer>> getLoanOffers(String customerId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  [LoanService] Fetching loan offers for: " + customerId);
            simulateDelay(120);
            return offersByCustomer.getOrDefault(customerId, new ArrayList<>());
        });
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// ============================================================================
// AGGREGATOR MICROSERVICE (dedicated service, not just API composition)
// ============================================================================

class FinancialProfileAggregator {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CreditScoreService creditScoreService;
    private final LoanService loanService;
    
    // Optional: cache for aggregated profiles
    private final Map<String, CustomerFinancialProfile> cache = new ConcurrentHashMap<>();
    
    public FinancialProfileAggregator(AccountService accountService,
                                     TransactionService transactionService,
                                     CreditScoreService creditScoreService,
                                     LoanService loanService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.creditScoreService = creditScoreService;
        this.loanService = loanService;
    }
    
    public CompletableFuture<CustomerFinancialProfile> getFinancialProfile(String customerId) {
        System.out.println("[Aggregator] Fetching financial profile for: " + customerId);
        long startTime = System.currentTimeMillis();
        
        // Check cache first
        CustomerFinancialProfile cached = cache.get(customerId);
        if (cached != null) {
            System.out.println("[Aggregator] Returning cached profile");
            return CompletableFuture.completedFuture(cached);
        }
        
        // Step 1: Get accounts
        return accountService.getAccountsByCustomer(customerId)
            .thenCompose(accounts -> {
                if (accounts.isEmpty()) {
                    return CompletableFuture.completedFuture(
                        new CustomerFinancialProfile(customerId, accounts, 
                            new ArrayList<>(), new CreditScore(customerId, 500), new ArrayList<>())
                    );
                }
                
                // Step 2: Get transactions for all accounts in parallel
                List<CompletableFuture<List<Transaction>>> transactionFutures = accounts.stream()
                    .map(account -> transactionService.getRecentTransactions(account.getAccountId(), 5))
                    .collect(Collectors.toList());
                
                CompletableFuture<List<Transaction>> allTransactionsFuture = 
                    CompletableFuture.allOf(transactionFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> transactionFutures.stream()
                            .flatMap(f -> f.join().stream())
                            .collect(Collectors.toList()));
                
                // Step 3: Get credit score and loan offers in parallel
                CompletableFuture<CreditScore> creditFuture = creditScoreService.getCreditScore(customerId);
                CompletableFuture<List<LoanOffer>> loansFuture = loanService.getLoanOffers(customerId);
                
                // Wait for all and aggregate
                return CompletableFuture.allOf(allTransactionsFuture, creditFuture, loansFuture)
                    .thenApply(v -> {
                        CustomerFinancialProfile profile = new CustomerFinancialProfile(
                            customerId,
                            accounts,
                            allTransactionsFuture.join(),
                            creditFuture.join(),
                            loansFuture.join()
                        );
                        
                        // Cache the result
                        cache.put(customerId, profile);
                        
                        long endTime = System.currentTimeMillis();
                        System.out.println("[Aggregator] Profile aggregated in " + (endTime - startTime) + "ms");
                        
                        return profile;
                    });
            });
    }
    
    public void clearCache(String customerId) {
        cache.remove(customerId);
        System.out.println("[Aggregator] Cache cleared for: " + customerId);
    }
}

/**
 * Demonstration of Aggregator Microservice Pattern
 */
public class AggregatorMicroservicePattern {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Aggregator Microservice Pattern ===\n");
        
        // Individual provider services
        AccountService accountService = new AccountService();
        TransactionService transactionService = new TransactionService();
        CreditScoreService creditScoreService = new CreditScoreService();
        LoanService loanService = new LoanService();
        
        // Aggregator microservice
        FinancialProfileAggregator aggregator = new FinancialProfileAggregator(
            accountService, transactionService, creditScoreService, loanService
        );
        
        System.out.println("--- First Request (Cache Miss) ---\n");
        CustomerFinancialProfile profile1 = aggregator.getFinancialProfile("C1").get();
        System.out.println("\nAggregated Profile:\n" + profile1);
        
        System.out.println("\n--- Second Request (Cache Hit) ---\n");
        CustomerFinancialProfile profile2 = aggregator.getFinancialProfile("C1").get();
        System.out.println("\nAggregated Profile:\n" + profile2);
        
        System.out.println("\n--- Different Customer ---\n");
        CustomerFinancialProfile profile3 = aggregator.getFinancialProfile("C2").get();
        System.out.println("\nAggregated Profile:\n" + profile3);
        
        System.out.println("\n=== Key Points ===");
        System.out.println("1. Dedicated service - aggregator is its own microservice");
        System.out.println("2. Parallel calls - invokes multiple services concurrently");
        System.out.println("3. Data enrichment - adds calculated fields and recommendations");
        System.out.println("4. Caching - can cache aggregated results for performance");
        System.out.println("5. Error handling - can handle partial failures gracefully");
        
        System.out.println("\n=== Benefits vs API Composition ===");
        System.out.println("- Aggregator is separate deployable service");
        System.out.println("- Can add business logic and transformation");
        System.out.println("- Can cache aggregated results");
        System.out.println("- Scales independently");
        System.out.println("- Reusable across multiple clients");
    }
}
