"""Service Stub - Test double for problematic services"""
from abc import ABC, abstractmethod

class PaymentService(ABC):
    @abstractmethod
    def process_payment(self, amount, card):
        pass

class RealPaymentService(PaymentService):
    def process_payment(self, amount, card):
        # Real implementation would call external payment API
        print(f"Processing real payment: ${amount}")
        return {"status": "success", "transaction_id": "TXN123"}

class PaymentServiceStub(PaymentService):
    """Stub for testing without calling real payment service"""
    def __init__(self, should_succeed=True):
        self.should_succeed = should_succeed
        self.calls = []
    
    def process_payment(self, amount, card):
        self.calls.append({"amount": amount, "card": card})
        print(f"[STUB] Simulating payment: ${amount}")
        
        if self.should_succeed:
            return {"status": "success", "transaction_id": "STUB_TXN_123"}
        else:
            return {"status": "failed", "error": "Insufficient funds"}

class OrderService:
    def __init__(self, payment_service):
        self.payment_service = payment_service
    
    def place_order(self, amount, card):
        result = self.payment_service.process_payment(amount, card)
        if result["status"] == "success":
            print(f"Order placed successfully: {result}")
            return True
        else:
            print(f"Order failed: {result}")
            return False

if __name__ == "__main__":
    # Testing with stub
    print("=== Testing with Stub ===")
    stub = PaymentServiceStub(should_succeed=True)
    order_service = OrderService(stub)
    order_service.place_order(100, "1234-5678-9012-3456")
    
    print(f"\nStub was called {len(stub.calls)} time(s)")
    print(f"Call details: {stub.calls}")
    
    # Testing failure scenario
    print("\n=== Testing Failure ===")
    failing_stub = PaymentServiceStub(should_succeed=False)
    order_service2 = OrderService(failing_stub)
    order_service2.place_order(200, "1234-5678-9012-3456")
