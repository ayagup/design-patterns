package microservices;

import java.util.*;

/**
 * Chained Microservice Pattern
 * ==============================
 * 
 * Intent:
 * Services call each other in a chain where each service processes
 * the request, potentially enriches it, and passes it to the next service.
 * 
 * Also Known As:
 * - Chain of Microservices
 * - Sequential Service Pattern
 * 
 * Motivation:
 * - Request needs processing by multiple services in sequence
 * - Each service adds value/transformation to the data
 * - Output of one service becomes input to next
 * - Resembles Chain of Responsibility but for microservices
 * 
 * Applicability:
 * - Multi-stage processing pipelines
 * - Each stage handled by different service
 * - Services need to process in specific order
 * - Each service enriches/transforms data
 * 
 * Structure:
 * Client -> Service A -> Service B -> Service C -> Response
 * 
 * Benefits:
 * + Clear separation of concerns
 * + Each service focused on single responsibility
 * + Easy to add new stages
 * + Services can be independently developed
 * 
 * Drawbacks:
 * - High latency (sum of all services)
 * - Reduced availability (fails if any service fails)
 * - Complex debugging
 * - Request tracing becomes important
 */

// ============================================================================
// REQUEST/RESPONSE MODELS
// ============================================================================

class LoanApplicationRequest {
    private String applicantId;
    private double amount;
    private int termMonths;
    
    // Enriched by services
    private Map<String, Object> data = new HashMap<>();
    
    public LoanApplicationRequest(String applicantId, double amount, int termMonths) {
        this.applicantId = applicantId;
        this.amount = amount;
        this.termMonths = termMonths;
    }
    
    public String getApplicantId() { return applicantId; }
    public double getAmount() { return amount; }
    public int getTermMonths() { return termMonths; }
    
    public void addData(String key, Object value) {
        data.put(key, value);
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
    
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
    
    @Override
    public String toString() {
        return String.format("LoanApplication{applicantId='%s', amount=$%.2f, termMonths=%d, enrichedData=%s}",
                           applicantId, amount, termMonths, data.keySet());
    }
}

// ============================================================================
// CHAINED MICROSERVICES
// ============================================================================

// Service 1: Validates application
class ValidationService {
    private final IdentityVerificationService nextService;
    
    public ValidationService(IdentityVerificationService nextService) {
        this.nextService = nextService;
    }
    
    public LoanApplicationRequest process(LoanApplicationRequest request) {
        System.out.println("\n[1. ValidationService] Processing application...");
        System.out.println("  Validating amount: $" + request.getAmount());
        System.out.println("  Validating term: " + request.getTermMonths() + " months");
        
        // Validation logic
        if (request.getAmount() <= 0 || request.getAmount() > 1000000) {
            throw new IllegalArgumentException("Invalid loan amount");
        }
        
        if (request.getTermMonths() < 12 || request.getTermMonths() > 360) {
            throw new IllegalArgumentException("Invalid term");
        }
        
        // Enrich request
        request.addData("validationStatus", "PASSED");
        request.addData("validatedAt", System.currentTimeMillis());
        
        System.out.println("  -> Validation PASSED");
        
        // Call next service in chain
        return nextService.process(request);
    }
}

// Service 2: Verifies identity
class IdentityVerificationService {
    private final CreditCheckService nextService;
    
    public IdentityVerificationService(CreditCheckService nextService) {
        this.nextService = nextService;
    }
    
    public LoanApplicationRequest process(LoanApplicationRequest request) {
        System.out.println("\n[2. IdentityVerificationService] Processing application...");
        System.out.println("  Verifying identity for: " + request.getApplicantId());
        
        // Simulate identity verification
        simulateDelay(100);
        
        // Enrich request
        request.addData("identityVerified", true);
        request.addData("identityScore", 95);
        
        System.out.println("  -> Identity verified (score: 95)");
        
        // Call next service in chain
        return nextService.process(request);
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// Service 3: Checks credit
class CreditCheckService {
    private final RiskAssessmentService nextService;
    
    public CreditCheckService(RiskAssessmentService nextService) {
        this.nextService = nextService;
    }
    
    public LoanApplicationRequest process(LoanApplicationRequest request) {
        System.out.println("\n[3. CreditCheckService] Processing application...");
        System.out.println("  Checking credit for: " + request.getApplicantId());
        
        // Simulate credit check
        simulateDelay(150);
        int creditScore = 750; // Simulated
        
        // Enrich request
        request.addData("creditScore", creditScore);
        request.addData("creditRating", "GOOD");
        
        System.out.println("  -> Credit score: " + creditScore + " (GOOD)");
        
        // Call next service in chain
        return nextService.process(request);
    }
    
    private void simulateDelay(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// Service 4: Assesses risk
class RiskAssessmentService {
    private final ApprovalService nextService;
    
    public RiskAssessmentService(ApprovalService nextService) {
        this.nextService = nextService;
    }
    
    public LoanApplicationRequest process(LoanApplicationRequest request) {
        System.out.println("\n[4. RiskAssessmentService] Processing application...");
        
        // Calculate risk based on enriched data
        int creditScore = (Integer) request.getData("creditScore");
        double amount = request.getAmount();
        
        String riskLevel;
        if (creditScore >= 750 && amount <= 100000) {
            riskLevel = "LOW";
        } else if (creditScore >= 650) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "HIGH";
        }
        
        // Enrich request
        request.addData("riskLevel", riskLevel);
        request.addData("riskScore", creditScore / 10.0);
        
        System.out.println("  -> Risk level: " + riskLevel);
        
        // Call next service in chain
        return nextService.process(request);
    }
}

// Service 5: Final approval (end of chain)
class ApprovalService {
    public LoanApplicationRequest process(LoanApplicationRequest request) {
        System.out.println("\n[5. ApprovalService] Processing application...");
        
        // Final decision based on all enriched data
        String riskLevel = (String) request.getData("riskLevel");
        int creditScore = (Integer) request.getData("creditScore");
        
        boolean approved = !riskLevel.equals("HIGH") && creditScore >= 650;
        String decision = approved ? "APPROVED" : "REJECTED";
        
        // Enrich request with final decision
        request.addData("decision", decision);
        request.addData("approvedAt", System.currentTimeMillis());
        
        if (approved) {
            double interestRate = calculateInterestRate(riskLevel);
            request.addData("interestRate", interestRate);
            System.out.println("  -> " + decision + " (interest rate: " + interestRate + "%)");
        } else {
            System.out.println("  -> " + decision);
        }
        
        // End of chain - return final result
        return request;
    }
    
    private double calculateInterestRate(String riskLevel) {
        switch (riskLevel) {
            case "LOW": return 4.5;
            case "MEDIUM": return 6.5;
            case "HIGH": return 9.5;
            default: return 10.0;
        }
    }
}

// ============================================================================
// CHAIN BUILDER
// ============================================================================

class LoanProcessingChain {
    private final ValidationService chainStart;
    
    public LoanProcessingChain() {
        // Build chain from end to start
        ApprovalService approvalService = new ApprovalService();
        RiskAssessmentService riskService = new RiskAssessmentService(approvalService);
        CreditCheckService creditService = new CreditCheckService(riskService);
        IdentityVerificationService identityService = new IdentityVerificationService(creditService);
        this.chainStart = new ValidationService(identityService);
    }
    
    public LoanApplicationRequest process(LoanApplicationRequest request) {
        long startTime = System.currentTimeMillis();
        System.out.println("=== Starting Loan Processing Chain ===");
        System.out.println("Initial request: " + request);
        
        LoanApplicationRequest result = chainStart.process(request);
        
        long endTime = System.currentTimeMillis();
        System.out.println("\n=== Chain Complete ===");
        System.out.println("Total processing time: " + (endTime - startTime) + "ms");
        System.out.println("Final result: " + result);
        
        return result;
    }
}

/**
 * Demonstration of Chained Microservice Pattern
 */
public class ChainedMicroservicePattern {
    public static void main(String[] args) {
        System.out.println("=== Chained Microservice Pattern ===\n");
        
        // Create the chain
        LoanProcessingChain chain = new LoanProcessingChain();
        
        System.out.println("--- Processing Loan Application 1 ---");
        LoanApplicationRequest request1 = new LoanApplicationRequest("CUST-001", 50000, 60);
        LoanApplicationRequest result1 = chain.process(request1);
        System.out.println("\nEnriched data: " + result1.getAllData());
        
        System.out.println("\n\n--- Processing Loan Application 2 ---");
        LoanApplicationRequest request2 = new LoanApplicationRequest("CUST-002", 200000, 240);
        LoanApplicationRequest result2 = chain.process(request2);
        System.out.println("\nEnriched data: " + result2.getAllData());
        
        System.out.println("\n\n=== Key Points ===");
        System.out.println("1. Sequential processing - each service processes in order");
        System.out.println("2. Data enrichment - each service adds information");
        System.out.println("3. Accumulated latency - total time = sum of all services");
        System.out.println("4. Single failure breaks chain - need good error handling");
        
        System.out.println("\n=== Trade-offs ===");
        System.out.println("Benefits:");
        System.out.println("  + Clear separation of concerns");
        System.out.println("  + Each service has single responsibility");
        System.out.println("  + Easy to add/remove stages");
        System.out.println("\nDrawbacks:");
        System.out.println("  - High latency (sequential calls)");
        System.out.println("  - Reduced availability (chain breaks if any service fails)");
        System.out.println("  - Complex debugging across services");
        System.out.println("  - Request tracing essential");
    }
}
