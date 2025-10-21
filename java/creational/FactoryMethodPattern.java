package creational;

/**
 * Factory Method Pattern
 * Defines an interface for creating objects but lets subclasses decide which class to instantiate.
 */
public class FactoryMethodPattern {
    
    // Product interface
    interface Vehicle {
        void drive();
    }
    
    // Concrete Products
    static class Car implements Vehicle {
        @Override
        public void drive() {
            System.out.println("Driving a car 🚗");
        }
    }
    
    static class Bike implements Vehicle {
        @Override
        public void drive() {
            System.out.println("Riding a bike 🚲");
        }
    }
    
    static class Truck implements Vehicle {
        @Override
        public void drive() {
            System.out.println("Driving a truck 🚚");
        }
    }
    
    // Creator (Factory)
    abstract static class VehicleFactory {
        // Factory method
        public abstract Vehicle createVehicle();
        
        // Template method using factory method
        public void deliverVehicle() {
            Vehicle vehicle = createVehicle();
            System.out.println("Vehicle created and ready for delivery");
            vehicle.drive();
        }
    }
    
    // Concrete Creators
    static class CarFactory extends VehicleFactory {
        @Override
        public Vehicle createVehicle() {
            return new Car();
        }
    }
    
    static class BikeFactory extends VehicleFactory {
        @Override
        public Vehicle createVehicle() {
            return new Bike();
        }
    }
    
    static class TruckFactory extends VehicleFactory {
        @Override
        public Vehicle createVehicle() {
            return new Truck();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Factory Method Pattern Demo ===\n");
        
        VehicleFactory carFactory = new CarFactory();
        carFactory.deliverVehicle();
        
        System.out.println();
        VehicleFactory bikeFactory = new BikeFactory();
        bikeFactory.deliverVehicle();
        
        System.out.println();
        VehicleFactory truckFactory = new TruckFactory();
        truckFactory.deliverVehicle();
    }
}
