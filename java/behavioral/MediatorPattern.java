package behavioral;

import java.util.*;

/**
 * Mediator Pattern
 * Defines an object that encapsulates how objects interact.
 */
public class MediatorPattern {
    
    // Mediator interface
    interface ChatMediator {
        void sendMessage(String message, User user);
        void addUser(User user);
    }
    
    // Concrete Mediator
    static class ChatRoom implements ChatMediator {
        private List<User> users = new ArrayList<>();
        
        @Override
        public void addUser(User user) {
            users.add(user);
        }
        
        @Override
        public void sendMessage(String message, User sender) {
            for (User user : users) {
                // Don't send message to sender
                if (user != sender) {
                    user.receive(message, sender);
                }
            }
        }
    }
    
    // Colleague
    static abstract class User {
        protected ChatMediator mediator;
        protected String name;
        
        public User(ChatMediator mediator, String name) {
            this.mediator = mediator;
            this.name = name;
        }
        
        public abstract void send(String message);
        public abstract void receive(String message, User sender);
    }
    
    // Concrete Colleague
    static class ChatUser extends User {
        public ChatUser(ChatMediator mediator, String name) {
            super(mediator, name);
        }
        
        @Override
        public void send(String message) {
            System.out.println(name + " sends: " + message);
            mediator.sendMessage(message, this);
        }
        
        @Override
        public void receive(String message, User sender) {
            System.out.println(name + " received from " + sender.name + ": " + message);
        }
    }
    
    // Air Traffic Control example
    interface ATCMediator {
        void registerFlight(Flight flight);
        void requestLanding(Flight flight);
        void requestTakeoff(Flight flight);
    }
    
    static class AirTrafficControl implements ATCMediator {
        private List<Flight> flights = new ArrayList<>();
        private boolean runwayAvailable = true;
        
        @Override
        public void registerFlight(Flight flight) {
            flights.add(flight);
            System.out.println("✈️  Flight " + flight.getName() + " registered with ATC");
        }
        
        @Override
        public void requestLanding(Flight flight) {
            if (runwayAvailable) {
                runwayAvailable = false;
                System.out.println("✅ ATC: Flight " + flight.getName() + " cleared for landing");
                flight.land();
                runwayAvailable = true;
            } else {
                System.out.println("⏳ ATC: Flight " + flight.getName() + " please hold, runway occupied");
            }
        }
        
        @Override
        public void requestTakeoff(Flight flight) {
            if (runwayAvailable) {
                runwayAvailable = false;
                System.out.println("✅ ATC: Flight " + flight.getName() + " cleared for takeoff");
                flight.takeoff();
                runwayAvailable = true;
            } else {
                System.out.println("⏳ ATC: Flight " + flight.getName() + " please hold, runway occupied");
            }
        }
    }
    
    static class Flight {
        private ATCMediator mediator;
        private String name;
        
        public Flight(ATCMediator mediator, String name) {
            this.mediator = mediator;
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void requestLanding() {
            System.out.println("Flight " + name + " requesting landing permission");
            mediator.requestLanding(this);
        }
        
        public void requestTakeoff() {
            System.out.println("Flight " + name + " requesting takeoff permission");
            mediator.requestTakeoff(this);
        }
        
        public void land() {
            System.out.println("🛬 Flight " + name + " landing...");
        }
        
        public void takeoff() {
            System.out.println("🛫 Flight " + name + " taking off...");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Mediator Pattern Demo ===\n");
        
        // Chat room example
        System.out.println("1. Chat Room (Mediator):");
        ChatMediator chatRoom = new ChatRoom();
        
        User alice = new ChatUser(chatRoom, "Alice");
        User bob = new ChatUser(chatRoom, "Bob");
        User charlie = new ChatUser(chatRoom, "Charlie");
        User david = new ChatUser(chatRoom, "David");
        
        chatRoom.addUser(alice);
        chatRoom.addUser(bob);
        chatRoom.addUser(charlie);
        chatRoom.addUser(david);
        
        alice.send("Hello everyone!");
        System.out.println();
        
        bob.send("Hi Alice!");
        System.out.println();
        
        charlie.send("Hey team!");
        
        // Air Traffic Control example
        System.out.println("\n\n2. Air Traffic Control (Mediator):");
        ATCMediator atc = new AirTrafficControl();
        
        Flight flight1 = new Flight(atc, "AA101");
        Flight flight2 = new Flight(atc, "UA202");
        Flight flight3 = new Flight(atc, "DL303");
        
        atc.registerFlight(flight1);
        atc.registerFlight(flight2);
        atc.registerFlight(flight3);
        
        System.out.println("\nFlight operations:");
        flight1.requestLanding();
        System.out.println();
        
        flight2.requestTakeoff();
        System.out.println();
        
        flight3.requestLanding();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Reduces coupling between components");
        System.out.println("✓ Centralizes control logic");
        System.out.println("✓ Simplifies object protocols");
        System.out.println("✓ Easier to understand interactions");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Chat applications");
        System.out.println("• Air traffic control systems");
        System.out.println("• UI dialogs and forms");
        System.out.println("• Event handling systems");
    }
}
