package behavioral;

/**
 * State Pattern
 * Allows an object to alter its behavior when its internal state changes.
 */
public class StatePattern {
    
    // State interface
    interface State {
        void insertCoin(VendingMachine machine);
        void ejectCoin(VendingMachine machine);
        void dispense(VendingMachine machine);
    }
    
    // Context
    static class VendingMachine {
        private State noCoinState;
        private State hasCoinState;
        private State soldState;
        private State currentState;
        private int count;
        
        public VendingMachine(int count) {
            this.noCoinState = new NoCoinState();
            this.hasCoinState = new HasCoinState();
            this.soldState = new SoldState();
            this.count = count;
            this.currentState = noCoinState;
        }
        
        public void insertCoin() {
            currentState.insertCoin(this);
        }
        
        public void ejectCoin() {
            currentState.ejectCoin(this);
        }
        
        public void dispense() {
            currentState.dispense(this);
        }
        
        public void setState(State state) {
            this.currentState = state;
        }
        
        public void releaseProduct() {
            if (count > 0) {
                System.out.println("🥤 Product dispensed!");
                count--;
            }
        }
        
        public State getNoCoinState() { return noCoinState; }
        public State getHasCoinState() { return hasCoinState; }
        public State getSoldState() { return soldState; }
        public int getCount() { return count; }
    }
    
    // Concrete States
    static class NoCoinState implements State {
        @Override
        public void insertCoin(VendingMachine machine) {
            System.out.println("💰 Coin inserted");
            machine.setState(machine.getHasCoinState());
        }
        
        @Override
        public void ejectCoin(VendingMachine machine) {
            System.out.println("❌ No coin to eject");
        }
        
        @Override
        public void dispense(VendingMachine machine) {
            System.out.println("❌ Insert coin first");
        }
    }
    
    static class HasCoinState implements State {
        @Override
        public void insertCoin(VendingMachine machine) {
            System.out.println("❌ Coin already inserted");
        }
        
        @Override
        public void ejectCoin(VendingMachine machine) {
            System.out.println("💰 Coin ejected");
            machine.setState(machine.getNoCoinState());
        }
        
        @Override
        public void dispense(VendingMachine machine) {
            System.out.println("✅ Dispensing...");
            machine.setState(machine.getSoldState());
            machine.releaseProduct();
            if (machine.getCount() > 0) {
                machine.setState(machine.getNoCoinState());
            }
        }
    }
    
    static class SoldState implements State {
        @Override
        public void insertCoin(VendingMachine machine) {
            System.out.println("⏳ Please wait, dispensing product");
        }
        
        @Override
        public void ejectCoin(VendingMachine machine) {
            System.out.println("❌ Cannot eject, already dispensing");
        }
        
        @Override
        public void dispense(VendingMachine machine) {
            System.out.println("⏳ Already dispensing");
        }
    }
    
    // Document workflow example
    interface DocumentState {
        void publish(Document doc);
        void approve(Document doc);
        void reject(Document doc);
    }
    
    static class Document {
        private DocumentState draftState;
        private DocumentState moderationState;
        private DocumentState publishedState;
        private DocumentState currentState;
        private String content;
        
        public Document(String content) {
            this.content = content;
            this.draftState = new DraftState();
            this.moderationState = new ModerationState();
            this.publishedState = new PublishedState();
            this.currentState = draftState;
        }
        
        public void publish() {
            currentState.publish(this);
        }
        
        public void approve() {
            currentState.approve(this);
        }
        
        public void reject() {
            currentState.reject(this);
        }
        
        public void setState(DocumentState state) {
            this.currentState = state;
        }
        
        public DocumentState getDraftState() { return draftState; }
        public DocumentState getModerationState() { return moderationState; }
        public DocumentState getPublishedState() { return publishedState; }
        public String getContent() { return content; }
    }
    
    static class DraftState implements DocumentState {
        @Override
        public void publish(Document doc) {
            System.out.println("📝 Submitting for moderation");
            doc.setState(doc.getModerationState());
        }
        
        @Override
        public void approve(Document doc) {
            System.out.println("❌ Cannot approve draft");
        }
        
        @Override
        public void reject(Document doc) {
            System.out.println("❌ Cannot reject draft");
        }
    }
    
    static class ModerationState implements DocumentState {
        @Override
        public void publish(Document doc) {
            System.out.println("⏳ Already in moderation");
        }
        
        @Override
        public void approve(Document doc) {
            System.out.println("✅ Document approved and published");
            doc.setState(doc.getPublishedState());
        }
        
        @Override
        public void reject(Document doc) {
            System.out.println("❌ Document rejected, back to draft");
            doc.setState(doc.getDraftState());
        }
    }
    
    static class PublishedState implements DocumentState {
        @Override
        public void publish(Document doc) {
            System.out.println("ℹ️  Already published");
        }
        
        @Override
        public void approve(Document doc) {
            System.out.println("ℹ️  Already approved");
        }
        
        @Override
        public void reject(Document doc) {
            System.out.println("❌ Unpublishing document");
            doc.setState(doc.getDraftState());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== State Pattern Demo ===\n");
        
        // Vending machine example
        System.out.println("1. Vending Machine State Machine:");
        VendingMachine machine = new VendingMachine(3);
        
        System.out.println("Scenario 1: Normal purchase");
        machine.insertCoin();
        machine.dispense();
        
        System.out.println("\nScenario 2: Eject coin");
        machine.insertCoin();
        machine.ejectCoin();
        
        System.out.println("\nScenario 3: Try to dispense without coin");
        machine.dispense();
        
        System.out.println("\nScenario 4: Try to insert coin twice");
        machine.insertCoin();
        machine.insertCoin();
        machine.dispense();
        
        // Document workflow example
        System.out.println("\n\n2. Document Workflow State Machine:");
        Document doc = new Document("Important Article");
        
        System.out.println("Document created in Draft state");
        System.out.println("\nTrying to approve draft:");
        doc.approve();
        
        System.out.println("\nSubmitting for moderation:");
        doc.publish();
        
        System.out.println("\nApproving document:");
        doc.approve();
        
        System.out.println("\nTrying to publish again:");
        doc.publish();
        
        System.out.println("\n\nCreating another document:");
        Document doc2 = new Document("Another Article");
        doc2.publish();
        System.out.println("\nRejecting during moderation:");
        doc2.reject();
        System.out.println("\nResubmitting:");
        doc2.publish();
        doc2.approve();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Organizes state-specific behavior");
        System.out.println("✓ Makes state transitions explicit");
        System.out.println("✓ Eliminates large conditional statements");
        System.out.println("✓ Easy to add new states");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Vending machines");
        System.out.println("• Document workflow systems");
        System.out.println("• Network connection states");
        System.out.println("• Game character states");
    }
}
