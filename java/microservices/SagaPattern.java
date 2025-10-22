package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * SAGA PATTERN
 * 
 * Manages distributed transactions across multiple services using compensating transactions.
 * Each step has a corresponding compensation action to undo the operation in case of failure.
 * 
 * Benefits:
 * - Maintains data consistency across services without distributed transactions
 * - Enables long-running business processes
 * - Improves system resilience
 * - Avoids distributed locks
 * 
 * Use Cases:
 * - Order processing across multiple services
 * - Travel booking (flight + hotel + car)
 * - Money transfer between accounts
 * - E-commerce checkout process
 */

// Saga step interface
interface SagaStep {
    String getName();
    boolean execute();
    boolean compensate();
}

// Saga coordinator
class SagaCoordinator {
    private final List<SagaStep> steps = new ArrayList<>();
    private final List<SagaStep> executedSteps = new ArrayList<>();
    
    public void addStep(SagaStep step) {
        steps.add(step);
    }
    
    public boolean executeSaga() {
        System.out.println("=== Starting Saga Execution ===");
        
        for (SagaStep step : steps) {
            System.out.println("Executing step: " + step.getName());
            
            if (!step.execute()) {
                System.out.println("❌ Step failed: " + step.getName());
                System.out.println("=== Starting Compensation ===");
                compensate();
                return false;
            }
            
            executedSteps.add(step);
            System.out.println("✅ Step completed: " + step.getName());
        }
        
        System.out.println("=== Saga Completed Successfully ===");
        return true;
    }
    
    private void compensate() {
        // Compensate in reverse order
        for (int i = executedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = executedSteps.get(i);
            System.out.println("Compensating step: " + step.getName());
            
            if (step.compensate()) {
                System.out.println("✅ Compensation successful: " + step.getName());
            } else {
                System.out.println("❌ Compensation failed: " + step.getName());
            }
        }
        
        executedSteps.clear();
    }
}

// Example 1: Order Saga
class OrderCreationStep implements SagaStep {
    private String orderId;
    private boolean shouldFail;
    
    public OrderCreationStep(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }
    
    @Override
    public String getName() {
        return "Order Creation";
    }
    
    @Override
    public boolean execute() {
        if (shouldFail) return false;
        
        orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("  Created order: " + orderId);
        return true;
    }
    
    @Override
    public boolean compensate() {
        System.out.println("  Cancelled order: " + orderId);
        orderId = null;
        return true;
    }
}

class PaymentProcessingStep implements SagaStep {
    private String transactionId;
    private boolean shouldFail;
    
    public PaymentProcessingStep(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }
    
    @Override
    public String getName() {
        return "Payment Processing";
    }
    
    @Override
    public boolean execute() {
        if (shouldFail) return false;
        
        transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("  Payment processed: " + transactionId);
        return true;
    }
    
    @Override
    public boolean compensate() {
        System.out.println("  Payment refunded: " + transactionId);
        transactionId = null;
        return true;
    }
}

class InventoryReservationStep implements SagaStep {
    private List<String> reservedItems = new ArrayList<>();
    private boolean shouldFail;
    
    public InventoryReservationStep(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }
    
    @Override
    public String getName() {
        return "Inventory Reservation";
    }
    
    @Override
    public boolean execute() {
        if (shouldFail) return false;
        
        reservedItems.add("ITEM-123");
        reservedItems.add("ITEM-456");
        System.out.println("  Reserved items: " + reservedItems);
        return true;
    }
    
    @Override
    public boolean compensate() {
        System.out.println("  Released items: " + reservedItems);
        reservedItems.clear();
        return true;
    }
}

class ShippingScheduleStep implements SagaStep {
    private String shippingId;
    private boolean shouldFail;
    
    public ShippingScheduleStep(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }
    
    @Override
    public String getName() {
        return "Shipping Schedule";
    }
    
    @Override
    public boolean execute() {
        if (shouldFail) return false;
        
        shippingId = "SHIP-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("  Scheduled shipping: " + shippingId);
        return true;
    }
    
    @Override
    public boolean compensate() {
        System.out.println("  Cancelled shipping: " + shippingId);
        shippingId = null;
        return true;
    }
}

// Example 2: Travel Booking Saga with Orchestration
class TravelBookingSaga {
    private String bookingId;
    
    static class FlightBookingStep implements SagaStep {
        private String flightBookingId;
        private boolean shouldFail;
        
        public FlightBookingStep(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
        
        @Override
        public String getName() {
            return "Flight Booking";
        }
        
        @Override
        public boolean execute() {
            if (shouldFail) return false;
            
            flightBookingId = "FLT-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("  Booked flight: " + flightBookingId);
            return true;
        }
        
        @Override
        public boolean compensate() {
            System.out.println("  Cancelled flight: " + flightBookingId);
            flightBookingId = null;
            return true;
        }
    }
    
    static class HotelBookingStep implements SagaStep {
        private String hotelBookingId;
        private boolean shouldFail;
        
        public HotelBookingStep(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
        
        @Override
        public String getName() {
            return "Hotel Booking";
        }
        
        @Override
        public boolean execute() {
            if (shouldFail) return false;
            
            hotelBookingId = "HTL-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("  Booked hotel: " + hotelBookingId);
            return true;
        }
        
        @Override
        public boolean compensate() {
            System.out.println("  Cancelled hotel: " + hotelBookingId);
            hotelBookingId = null;
            return true;
        }
    }
    
    static class CarRentalStep implements SagaStep {
        private String carRentalId;
        private boolean shouldFail;
        
        public CarRentalStep(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
        
        @Override
        public String getName() {
            return "Car Rental";
        }
        
        @Override
        public boolean execute() {
            if (shouldFail) return false;
            
            carRentalId = "CAR-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("  Booked car: " + carRentalId);
            return true;
        }
        
        @Override
        public boolean compensate() {
            System.out.println("  Cancelled car: " + carRentalId);
            carRentalId = null;
            return true;
        }
    }
}

// Example 3: Async Saga with Event-Driven Approach
class AsyncSagaCoordinator {
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final List<SagaStep> steps = new ArrayList<>();
    
    public void addStep(SagaStep step) {
        steps.add(step);
    }
    
    public CompletableFuture<Boolean> executeSagaAsync() {
        System.out.println("=== Starting Async Saga Execution ===");
        
        CompletableFuture<Boolean> sagaFuture = CompletableFuture.completedFuture(true);
        List<SagaStep> executedSteps = new ArrayList<>();
        
        for (SagaStep step : steps) {
            sagaFuture = sagaFuture.thenCompose(success -> {
                if (!success) {
                    return CompletableFuture.completedFuture(false);
                }
                
                return CompletableFuture.supplyAsync(() -> {
                    System.out.println("Executing async step: " + step.getName());
                    
                    if (step.execute()) {
                        executedSteps.add(step);
                        System.out.println("✅ Async step completed: " + step.getName());
                        return true;
                    } else {
                        System.out.println("❌ Async step failed: " + step.getName());
                        compensateAsync(executedSteps);
                        return false;
                    }
                }, executor);
            });
        }
        
        return sagaFuture.thenApply(result -> {
            if (result) {
                System.out.println("=== Async Saga Completed Successfully ===");
            }
            return result;
        });
    }
    
    private void compensateAsync(List<SagaStep> executedSteps) {
        System.out.println("=== Starting Async Compensation ===");
        
        List<CompletableFuture<Void>> compensations = new ArrayList<>();
        
        for (int i = executedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = executedSteps.get(i);
            
            CompletableFuture<Void> compensation = CompletableFuture.runAsync(() -> {
                System.out.println("Compensating async step: " + step.getName());
                
                if (step.compensate()) {
                    System.out.println("✅ Async compensation successful: " + step.getName());
                } else {
                    System.out.println("❌ Async compensation failed: " + step.getName());
                }
            }, executor);
            
            compensations.add(compensation);
        }
        
        CompletableFuture.allOf(compensations.toArray(new CompletableFuture[0])).join();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Demo
public class SagaPattern {
    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       SAGA PATTERN DEMONSTRATION         ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        
        // Example 1: Successful Order Saga
        System.out.println("1. SUCCESSFUL ORDER SAGA");
        System.out.println("─────────────────────────");
        SagaCoordinator orderSaga = new SagaCoordinator();
        orderSaga.addStep(new OrderCreationStep(false));
        orderSaga.addStep(new PaymentProcessingStep(false));
        orderSaga.addStep(new InventoryReservationStep(false));
        orderSaga.addStep(new ShippingScheduleStep(false));
        orderSaga.executeSaga();
        
        System.out.println("\n2. FAILED ORDER SAGA (Payment Fails)");
        System.out.println("─────────────────────────────────────");
        SagaCoordinator failedOrderSaga = new SagaCoordinator();
        failedOrderSaga.addStep(new OrderCreationStep(false));
        failedOrderSaga.addStep(new PaymentProcessingStep(true)); // This will fail
        failedOrderSaga.addStep(new InventoryReservationStep(false));
        failedOrderSaga.addStep(new ShippingScheduleStep(false));
        failedOrderSaga.executeSaga();
        
        // Example 2: Travel Booking Saga
        System.out.println("\n3. SUCCESSFUL TRAVEL BOOKING SAGA");
        System.out.println("──────────────────────────────────");
        SagaCoordinator travelSaga = new SagaCoordinator();
        travelSaga.addStep(new TravelBookingSaga.FlightBookingStep(false));
        travelSaga.addStep(new TravelBookingSaga.HotelBookingStep(false));
        travelSaga.addStep(new TravelBookingSaga.CarRentalStep(false));
        travelSaga.executeSaga();
        
        System.out.println("\n4. FAILED TRAVEL BOOKING SAGA (Car Rental Fails)");
        System.out.println("─────────────────────────────────────────────────");
        SagaCoordinator failedTravelSaga = new SagaCoordinator();
        failedTravelSaga.addStep(new TravelBookingSaga.FlightBookingStep(false));
        failedTravelSaga.addStep(new TravelBookingSaga.HotelBookingStep(false));
        failedTravelSaga.addStep(new TravelBookingSaga.CarRentalStep(true)); // This will fail
        failedTravelSaga.executeSaga();
        
        // Example 3: Async Saga
        System.out.println("\n5. ASYNC SAGA EXECUTION");
        System.out.println("───────────────────────");
        AsyncSagaCoordinator asyncSaga = new AsyncSagaCoordinator();
        asyncSaga.addStep(new OrderCreationStep(false));
        asyncSaga.addStep(new PaymentProcessingStep(false));
        asyncSaga.addStep(new InventoryReservationStep(false));
        asyncSaga.addStep(new ShippingScheduleStep(false));
        
        CompletableFuture<Boolean> asyncResult = asyncSaga.executeSagaAsync();
        asyncResult.get(); // Wait for completion
        
        System.out.println("\n6. FAILED ASYNC SAGA");
        System.out.println("────────────────────");
        AsyncSagaCoordinator failedAsyncSaga = new AsyncSagaCoordinator();
        failedAsyncSaga.addStep(new OrderCreationStep(false));
        failedAsyncSaga.addStep(new PaymentProcessingStep(false));
        failedAsyncSaga.addStep(new InventoryReservationStep(true)); // This will fail
        failedAsyncSaga.addStep(new ShippingScheduleStep(false));
        
        CompletableFuture<Boolean> failedAsyncResult = failedAsyncSaga.executeSagaAsync();
        failedAsyncResult.get(); // Wait for completion
        
        asyncSaga.shutdown();
        failedAsyncSaga.shutdown();
        
        System.out.println("\n✅ Saga Pattern demonstration completed!");
    }
}
