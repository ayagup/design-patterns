package structural;

/**
 * Bridge Pattern
 * Separates abstraction from implementation so both can vary independently.
 */
public class BridgePattern {
    
    // Implementor interface
    interface Color {
        void applyColor();
    }
    
    // Concrete Implementors
    static class RedColor implements Color {
        @Override
        public void applyColor() {
            System.out.print("Red");
        }
    }
    
    static class BlueColor implements Color {
        @Override
        public void applyColor() {
            System.out.print("Blue");
        }
    }
    
    static class GreenColor implements Color {
        @Override
        public void applyColor() {
            System.out.print("Green");
        }
    }
    
    // Abstraction
    abstract static class Shape {
        protected Color color;
        
        public Shape(Color color) {
            this.color = color;
        }
        
        abstract public void draw();
    }
    
    // Refined Abstractions
    static class Circle extends Shape {
        public Circle(Color color) {
            super(color);
        }
        
        @Override
        public void draw() {
            System.out.print("Drawing Circle in ");
            color.applyColor();
            System.out.println(" color");
        }
    }
    
    static class Square extends Shape {
        public Square(Color color) {
            super(color);
        }
        
        @Override
        public void draw() {
            System.out.print("Drawing Square in ");
            color.applyColor();
            System.out.println(" color");
        }
    }
    
    static class Triangle extends Shape {
        public Triangle(Color color) {
            super(color);
        }
        
        @Override
        public void draw() {
            System.out.print("Drawing Triangle in ");
            color.applyColor();
            System.out.println(" color");
        }
    }
    
    // Real-world example: Device and Remote Control
    interface Device {
        void turnOn();
        void turnOff();
        void setVolume(int volume);
    }
    
    static class TV implements Device {
        private int volume = 50;
        
        @Override
        public void turnOn() {
            System.out.println("TV is ON");
        }
        
        @Override
        public void turnOff() {
            System.out.println("TV is OFF");
        }
        
        @Override
        public void setVolume(int volume) {
            this.volume = volume;
            System.out.println("TV volume set to: " + volume);
        }
    }
    
    static class Radio implements Device {
        private int volume = 30;
        
        @Override
        public void turnOn() {
            System.out.println("Radio is ON");
        }
        
        @Override
        public void turnOff() {
            System.out.println("Radio is OFF");
        }
        
        @Override
        public void setVolume(int volume) {
            this.volume = volume;
            System.out.println("Radio volume set to: " + volume);
        }
    }
    
    static class RemoteControl {
        protected Device device;
        
        public RemoteControl(Device device) {
            this.device = device;
        }
        
        public void togglePower() {
            device.turnOn();
        }
        
        public void volumeUp() {
            device.setVolume(75);
        }
    }
    
    static class AdvancedRemoteControl extends RemoteControl {
        public AdvancedRemoteControl(Device device) {
            super(device);
        }
        
        public void mute() {
            System.out.println("Muting device");
            device.setVolume(0);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Bridge Pattern Demo ===\n");
        
        System.out.println("1. Shape and Color Bridge:");
        Shape redCircle = new Circle(new RedColor());
        Shape blueSquare = new Square(new BlueColor());
        Shape greenTriangle = new Triangle(new GreenColor());
        
        redCircle.draw();
        blueSquare.draw();
        greenTriangle.draw();
        
        System.out.println("\n2. Device and Remote Control Bridge:");
        Device tv = new TV();
        RemoteControl tvRemote = new RemoteControl(tv);
        tvRemote.togglePower();
        tvRemote.volumeUp();
        
        System.out.println();
        Device radio = new Radio();
        AdvancedRemoteControl radioRemote = new AdvancedRemoteControl(radio);
        radioRemote.togglePower();
        radioRemote.mute();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Decouples abstraction from implementation");
        System.out.println("✓ Both can be extended independently");
        System.out.println("✓ Changes in implementation don't affect clients");
    }
}
