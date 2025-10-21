package creational;

/**
 * Builder Pattern
 * Separates the construction of a complex object from its representation.
 */
public class BuilderPattern {
    
    // Product
    static class Computer {
        // Required parameters
        private final String CPU;
        private final String RAM;
        
        // Optional parameters
        private final String storage;
        private final String graphicsCard;
        private final boolean isWiFiEnabled;
        private final boolean isBluetoothEnabled;
        
        private Computer(Builder builder) {
            this.CPU = builder.CPU;
            this.RAM = builder.RAM;
            this.storage = builder.storage;
            this.graphicsCard = builder.graphicsCard;
            this.isWiFiEnabled = builder.isWiFiEnabled;
            this.isBluetoothEnabled = builder.isBluetoothEnabled;
        }
        
        @Override
        public String toString() {
            return "Computer{" +
                    "CPU='" + CPU + '\'' +
                    ", RAM='" + RAM + '\'' +
                    ", storage='" + storage + '\'' +
                    ", graphicsCard='" + graphicsCard + '\'' +
                    ", isWiFiEnabled=" + isWiFiEnabled +
                    ", isBluetoothEnabled=" + isBluetoothEnabled +
                    '}';
        }
        
        // Builder class
        static class Builder {
            // Required parameters
            private final String CPU;
            private final String RAM;
            
            // Optional parameters - initialized to default values
            private String storage = "256GB SSD";
            private String graphicsCard = "Integrated";
            private boolean isWiFiEnabled = false;
            private boolean isBluetoothEnabled = false;
            
            public Builder(String CPU, String RAM) {
                this.CPU = CPU;
                this.RAM = RAM;
            }
            
            public Builder storage(String storage) {
                this.storage = storage;
                return this;
            }
            
            public Builder graphicsCard(String graphicsCard) {
                this.graphicsCard = graphicsCard;
                return this;
            }
            
            public Builder enableWiFi() {
                this.isWiFiEnabled = true;
                return this;
            }
            
            public Builder enableBluetooth() {
                this.isBluetoothEnabled = true;
                return this;
            }
            
            public Computer build() {
                return new Computer(this);
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Builder Pattern Demo ===\n");
        
        // Building a basic computer
        Computer basicComputer = new Computer.Builder("Intel i5", "8GB")
                .build();
        System.out.println("Basic Computer:");
        System.out.println(basicComputer);
        
        // Building a gaming computer with all options
        Computer gamingComputer = new Computer.Builder("Intel i9", "32GB")
                .storage("1TB NVMe SSD")
                .graphicsCard("NVIDIA RTX 4090")
                .enableWiFi()
                .enableBluetooth()
                .build();
        System.out.println("\nGaming Computer:");
        System.out.println(gamingComputer);
        
        // Building a work computer with some options
        Computer workComputer = new Computer.Builder("AMD Ryzen 7", "16GB")
                .storage("512GB SSD")
                .enableWiFi()
                .build();
        System.out.println("\nWork Computer:");
        System.out.println(workComputer);
    }
}
