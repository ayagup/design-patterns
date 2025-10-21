package cloud;

import java.util.*;

/**
 * COMPENSATING TRANSACTION PATTERN
 * 
 * Undoes the work performed by a series of steps that together define an eventually
 * consistent operation if one or more steps fail. Provides a way to handle failures
 * in distributed transactions.
 * 
 * Benefits:
 * - Handles failures in distributed systems
 * - Maintains data consistency across services
 * - Enables saga pattern implementation
 * - Provides rollback mechanism for multi-step operations
 * - Supports eventual consistency
 * 
 * Use Cases:
 * - Distributed transactions across microservices
 * - E-commerce order processing
 * - Travel booking systems
 * - Multi-step workflows
 * - Saga pattern implementations
 */

// Transaction Step Interface
interface TransactionStep {
    String getName();
    boolean execute() throws Exception;
    void compensate();
}

// Compensating Transaction Coordinator
class CompensatingTransactionCoordinator {
    private final List<TransactionStep> steps = new ArrayList<>();
    private final List<TransactionStep> completedSteps = new ArrayList<>();
    
    public void addStep(TransactionStep step) {
        steps.add(step);
    }
    
    public boolean executeTransaction() {
        System.out.println("\n🔄 Starting compensating transaction with " + steps.size() + " steps");
        
        try {
            // Execute all steps
            for (TransactionStep step : steps) {
                System.out.println("\n  ▶️  Executing: " + step.getName());
                
                if (!step.execute()) {
                    System.out.println("  ❌ Step failed: " + step.getName());
                    compensate();
                    return false;
                }
                
                System.out.println("  ✅ Step completed: " + step.getName());
                completedSteps.add(step);
            }
            
            System.out.println("\n✅ Transaction completed successfully!");
            return true;
            
        } catch (Exception e) {
            System.out.println("\n❌ Exception during transaction: " + e.getMessage());
            compensate();
            return false;
        }
    }
    
    private void compensate() {
        System.out.println("\n🔙 Starting compensation...");
        
        // Compensate in reverse order
        Collections.reverse(completedSteps);
        
        for (TransactionStep step : completedSteps) {
            try {
                System.out.println("  ↩️  Compensating: " + step.getName());
                step.compensate();
                System.out.println("  ✅ Compensation completed: " + step.getName());
            } catch (Exception e) {
                System.out.println("  ⚠️  Compensation failed for " + step.getName() + 
                                 ": " + e.getMessage());
                // Log but continue compensating other steps
            }
        }
        
        System.out.println("\n✅ Compensation completed");
    }
}

// Example 1: E-commerce Order Processing
class ReserveInventoryStep implements TransactionStep {
    private final String productId;
    private final int quantity;
    private boolean reserved = false;
    
    public ReserveInventoryStep(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    @Override
    public String getName() {
        return "Reserve Inventory";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    📦 Reserving " + quantity + " units of " + productId);
        try {
            Thread.sleep(100);
            reserved = true;
            System.out.println("    ✅ Inventory reserved");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (reserved) {
            System.out.println("    📦 Releasing reserved inventory for " + productId);
            reserved = false;
        }
    }
}

class ChargePaymentStep implements TransactionStep {
    private final String customerId;
    private final double amount;
    private boolean charged = false;
    private final boolean shouldFail;
    
    public ChargePaymentStep(String customerId, double amount, boolean shouldFail) {
        this.customerId = customerId;
        this.amount = amount;
        this.shouldFail = shouldFail;
    }
    
    @Override
    public String getName() {
        return "Charge Payment";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    💳 Charging $" + amount + " to customer " + customerId);
        
        if (shouldFail) {
            System.out.println("    ❌ Payment declined");
            return false;
        }
        
        try {
            Thread.sleep(100);
            charged = true;
            System.out.println("    ✅ Payment charged");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (charged) {
            System.out.println("    💳 Refunding $" + amount + " to customer " + customerId);
            charged = false;
        }
    }
}

class CreateShipmentStep implements TransactionStep {
    private final String orderId;
    private final String address;
    private boolean shipmentCreated = false;
    
    public CreateShipmentStep(String orderId, String address) {
        this.orderId = orderId;
        this.address = address;
    }
    
    @Override
    public String getName() {
        return "Create Shipment";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    📮 Creating shipment for order " + orderId + " to " + address);
        try {
            Thread.sleep(100);
            shipmentCreated = true;
            System.out.println("    ✅ Shipment created");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (shipmentCreated) {
            System.out.println("    📮 Canceling shipment for order " + orderId);
            shipmentCreated = false;
        }
    }
}

class SendConfirmationEmailStep implements TransactionStep {
    private final String email;
    private final String orderId;
    private boolean emailSent = false;
    
    public SendConfirmationEmailStep(String email, String orderId) {
        this.email = email;
        this.orderId = orderId;
    }
    
    @Override
    public String getName() {
        return "Send Confirmation Email";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    📧 Sending confirmation email to " + email);
        try {
            Thread.sleep(50);
            emailSent = true;
            System.out.println("    ✅ Email sent");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (emailSent) {
            System.out.println("    📧 Sending cancellation email to " + email);
            emailSent = false;
        }
    }
}

// Example 2: Travel Booking System
class BookFlightStep implements TransactionStep {
    private final String flightNumber;
    private final String passengerName;
    private boolean booked = false;
    
    public BookFlightStep(String flightNumber, String passengerName) {
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
    }
    
    @Override
    public String getName() {
        return "Book Flight";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    ✈️  Booking flight " + flightNumber + " for " + passengerName);
        try {
            Thread.sleep(100);
            booked = true;
            System.out.println("    ✅ Flight booked");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (booked) {
            System.out.println("    ✈️  Canceling flight booking " + flightNumber);
            booked = false;
        }
    }
}

class BookHotelStep implements TransactionStep {
    private final String hotelName;
    private final String guestName;
    private final boolean shouldFail;
    private boolean booked = false;
    
    public BookHotelStep(String hotelName, String guestName, boolean shouldFail) {
        this.hotelName = hotelName;
        this.guestName = guestName;
        this.shouldFail = shouldFail;
    }
    
    @Override
    public String getName() {
        return "Book Hotel";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    🏨 Booking " + hotelName + " for " + guestName);
        
        if (shouldFail) {
            System.out.println("    ❌ Hotel fully booked");
            return false;
        }
        
        try {
            Thread.sleep(100);
            booked = true;
            System.out.println("    ✅ Hotel booked");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (booked) {
            System.out.println("    🏨 Canceling hotel reservation at " + hotelName);
            booked = false;
        }
    }
}

class BookCarRentalStep implements TransactionStep {
    private final String carType;
    private final String renterName;
    private boolean booked = false;
    
    public BookCarRentalStep(String carType, String renterName) {
        this.carType = carType;
        this.renterName = renterName;
    }
    
    @Override
    public String getName() {
        return "Book Car Rental";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    🚗 Booking " + carType + " for " + renterName);
        try {
            Thread.sleep(100);
            booked = true;
            System.out.println("    ✅ Car rental booked");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (booked) {
            System.out.println("    🚗 Canceling car rental " + carType);
            booked = false;
        }
    }
}

// Example 3: Banking Transfer
class DebitAccountStep implements TransactionStep {
    private final String accountId;
    private final double amount;
    private boolean debited = false;
    
    public DebitAccountStep(String accountId, double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }
    
    @Override
    public String getName() {
        return "Debit Account";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    💰 Debiting $" + amount + " from account " + accountId);
        try {
            Thread.sleep(100);
            debited = true;
            System.out.println("    ✅ Account debited");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (debited) {
            System.out.println("    💰 Crediting $" + amount + " back to account " + accountId);
            debited = false;
        }
    }
}

class CreditAccountStep implements TransactionStep {
    private final String accountId;
    private final double amount;
    private boolean credited = false;
    
    public CreditAccountStep(String accountId, double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }
    
    @Override
    public String getName() {
        return "Credit Account";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    💰 Crediting $" + amount + " to account " + accountId);
        try {
            Thread.sleep(100);
            credited = true;
            System.out.println("    ✅ Account credited");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (credited) {
            System.out.println("    💰 Debiting $" + amount + " from account " + accountId);
            credited = false;
        }
    }
}

class RecordTransactionStep implements TransactionStep {
    private final String transactionId;
    private boolean recorded = false;
    
    public RecordTransactionStep(String transactionId) {
        this.transactionId = transactionId;
    }
    
    @Override
    public String getName() {
        return "Record Transaction";
    }
    
    @Override
    public boolean execute() {
        System.out.println("    📝 Recording transaction " + transactionId);
        try {
            Thread.sleep(50);
            recorded = true;
            System.out.println("    ✅ Transaction recorded");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Override
    public void compensate() {
        if (recorded) {
            System.out.println("    📝 Marking transaction " + transactionId + " as failed");
            recorded = false;
        }
    }
}

// Demo
public class CompensatingTransactionPattern {
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   COMPENSATING TRANSACTION PATTERN DEMONSTRATION  ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        
        // Example 1: Successful E-commerce Order
        System.out.println("\n1. SUCCESSFUL E-COMMERCE ORDER");
        System.out.println("═══════════════════════════════════");
        
        CompensatingTransactionCoordinator orderTransaction = new CompensatingTransactionCoordinator();
        orderTransaction.addStep(new ReserveInventoryStep("LAPTOP-001", 1));
        orderTransaction.addStep(new ChargePaymentStep("CUST-123", 1299.99, false));
        orderTransaction.addStep(new CreateShipmentStep("ORD-001", "123 Main St"));
        orderTransaction.addStep(new SendConfirmationEmailStep("customer@example.com", "ORD-001"));
        
        orderTransaction.executeTransaction();
        
        // Example 2: Failed E-commerce Order (Payment Declined)
        System.out.println("\n\n2. FAILED E-COMMERCE ORDER (Payment Declined)");
        System.out.println("═══════════════════════════════════════════════");
        
        CompensatingTransactionCoordinator failedOrderTransaction = new CompensatingTransactionCoordinator();
        failedOrderTransaction.addStep(new ReserveInventoryStep("PHONE-002", 2));
        failedOrderTransaction.addStep(new ChargePaymentStep("CUST-456", 899.99, true)); // Will fail
        failedOrderTransaction.addStep(new CreateShipmentStep("ORD-002", "456 Oak Ave"));
        failedOrderTransaction.addStep(new SendConfirmationEmailStep("customer2@example.com", "ORD-002"));
        
        failedOrderTransaction.executeTransaction();
        
        // Example 3: Successful Travel Booking
        System.out.println("\n\n3. SUCCESSFUL TRAVEL BOOKING");
        System.out.println("═══════════════════════════════");
        
        CompensatingTransactionCoordinator travelBooking = new CompensatingTransactionCoordinator();
        travelBooking.addStep(new BookFlightStep("AA123", "Alice Johnson"));
        travelBooking.addStep(new BookHotelStep("Grand Hotel", "Alice Johnson", false));
        travelBooking.addStep(new BookCarRentalStep("SUV", "Alice Johnson"));
        
        travelBooking.executeTransaction();
        
        // Example 4: Failed Travel Booking (Hotel Unavailable)
        System.out.println("\n\n4. FAILED TRAVEL BOOKING (Hotel Unavailable)");
        System.out.println("═══════════════════════════════════════════════");
        
        CompensatingTransactionCoordinator failedTravelBooking = new CompensatingTransactionCoordinator();
        failedTravelBooking.addStep(new BookFlightStep("BA456", "Bob Smith"));
        failedTravelBooking.addStep(new BookHotelStep("Seaside Resort", "Bob Smith", true)); // Will fail
        failedTravelBooking.addStep(new BookCarRentalStep("Sedan", "Bob Smith"));
        
        failedTravelBooking.executeTransaction();
        
        // Example 5: Banking Transfer
        System.out.println("\n\n5. BANKING TRANSFER");
        System.out.println("═══════════════════");
        
        CompensatingTransactionCoordinator bankTransfer = new CompensatingTransactionCoordinator();
        bankTransfer.addStep(new DebitAccountStep("ACC-001", 500.00));
        bankTransfer.addStep(new CreditAccountStep("ACC-002", 500.00));
        bankTransfer.addStep(new RecordTransactionStep("TXN-12345"));
        
        bankTransfer.executeTransaction();
        
        System.out.println("\n\n✅ Compensating Transaction Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Handles failures in distributed transactions");
        System.out.println("  • Maintains eventual consistency");
        System.out.println("  • Provides automatic rollback mechanism");
        System.out.println("  • Compensates in reverse order");
        System.out.println("  • Enables saga pattern implementation");
        
        System.out.println("\n🏗️  Common Use Cases:");
        System.out.println("  • E-commerce order processing");
        System.out.println("  • Travel booking systems");
        System.out.println("  • Banking and financial transactions");
        System.out.println("  • Microservices orchestration");
        System.out.println("  • Multi-step workflows");
        
        System.out.println("\n⚠️  Important Considerations:");
        System.out.println("  • Compensation must be idempotent");
        System.out.println("  • Not all operations are compensatable");
        System.out.println("  • May result in eventual consistency (not immediate)");
        System.out.println("  • Compensation itself can fail (needs error handling)");
    }
}
