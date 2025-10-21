package behavioral;

/**
 * Template Method Pattern
 * Defines the skeleton of an algorithm, deferring some steps to subclasses.
 */
public class TemplateMethodPattern {
    
    // Abstract class with template method
    abstract static class DataProcessor {
        // Template method - defines the algorithm structure
        public final void process() {
            readData();
            processData();
            writeData();
            if (customerWantsHook()) {
                hook();
            }
        }
        
        protected abstract void readData();
        protected abstract void processData();
        protected abstract void writeData();
        
        // Hook method - can be overridden but has default implementation
        protected boolean customerWantsHook() {
            return true;
        }
        
        protected void hook() {
            // Default empty implementation
        }
    }
    
    // Concrete implementations
    static class CSVDataProcessor extends DataProcessor {
        @Override
        protected void readData() {
            System.out.println("📊 Reading data from CSV file");
        }
        
        @Override
        protected void processData() {
            System.out.println("⚙️  Processing CSV data");
        }
        
        @Override
        protected void writeData() {
            System.out.println("💾 Writing data to CSV file");
        }
        
        @Override
        protected void hook() {
            System.out.println("🔗 CSV-specific post-processing");
        }
    }
    
    static class XMLDataProcessor extends DataProcessor {
        @Override
        protected void readData() {
            System.out.println("📄 Reading data from XML file");
        }
        
        @Override
        protected void processData() {
            System.out.println("⚙️  Processing XML data with validation");
        }
        
        @Override
        protected void writeData() {
            System.out.println("💾 Writing data to XML file");
        }
        
        @Override
        protected boolean customerWantsHook() {
            return false; // Skip hook for XML
        }
    }
    
    // Game example
    abstract static class Game {
        // Template method
        public final void play() {
            initialize();
            startPlay();
            endPlay();
        }
        
        protected abstract void initialize();
        protected abstract void startPlay();
        protected abstract void endPlay();
    }
    
    static class Cricket extends Game {
        @Override
        protected void initialize() {
            System.out.println("🏏 Cricket Game Initialized");
        }
        
        @Override
        protected void startPlay() {
            System.out.println("🏏 Cricket Game Started");
        }
        
        @Override
        protected void endPlay() {
            System.out.println("🏏 Cricket Game Finished");
        }
    }
    
    static class Football extends Game {
        @Override
        protected void initialize() {
            System.out.println("⚽ Football Game Initialized");
        }
        
        @Override
        protected void startPlay() {
            System.out.println("⚽ Football Game Started");
        }
        
        @Override
        protected void endPlay() {
            System.out.println("⚽ Football Game Finished");
        }
    }
    
    // Beverage example
    abstract static class Beverage {
        // Template method
        public final void prepareRecipe() {
            boilWater();
            brew();
            pourInCup();
            if (customerWantsCondiments()) {
                addCondiments();
            }
        }
        
        protected void boilWater() {
            System.out.println("💧 Boiling water");
        }
        
        protected void pourInCup() {
            System.out.println("☕ Pouring into cup");
        }
        
        protected abstract void brew();
        protected abstract void addCondiments();
        
        // Hook - subclasses can override
        protected boolean customerWantsCondiments() {
            return true;
        }
    }
    
    static class Tea extends Beverage {
        @Override
        protected void brew() {
            System.out.println("🍵 Steeping the tea");
        }
        
        @Override
        protected void addCondiments() {
            System.out.println("🍋 Adding lemon");
        }
    }
    
    static class Coffee extends Beverage {
        @Override
        protected void brew() {
            System.out.println("☕ Dripping coffee through filter");
        }
        
        @Override
        protected void addCondiments() {
            System.out.println("🥛 Adding sugar and milk");
        }
        
        @Override
        protected boolean customerWantsCondiments() {
            return false; // Black coffee
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Template Method Pattern Demo ===\n");
        
        // Data processor example
        System.out.println("1. Data Processing:");
        System.out.println("Processing CSV:");
        DataProcessor csvProcessor = new CSVDataProcessor();
        csvProcessor.process();
        
        System.out.println("\nProcessing XML:");
        DataProcessor xmlProcessor = new XMLDataProcessor();
        xmlProcessor.process();
        
        // Game example
        System.out.println("\n\n2. Game Framework:");
        Game cricket = new Cricket();
        cricket.play();
        
        System.out.println();
        Game football = new Football();
        football.play();
        
        // Beverage example
        System.out.println("\n\n3. Beverage Preparation:");
        System.out.println("Making Tea:");
        Beverage tea = new Tea();
        tea.prepareRecipe();
        
        System.out.println("\nMaking Coffee (black):");
        Beverage coffee = new Coffee();
        coffee.prepareRecipe();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Code reuse through inheritance");
        System.out.println("✓ Controls algorithm structure");
        System.out.println("✓ Allows customization through hooks");
        System.out.println("✓ Implements Hollywood Principle (Don't call us, we'll call you)");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Framework design");
        System.out.println("• Data processing pipelines");
        System.out.println("• Game engines");
        System.out.println("• Workflow systems");
    }
}
