package creational;

/**
 * Abstract Factory Pattern
 * Provides an interface for creating families of related or dependent objects.
 */
public class AbstractFactoryPattern {
    
    // Abstract Products
    interface Button {
        void render();
    }
    
    interface Checkbox {
        void render();
    }
    
    // Windows Products
    static class WindowsButton implements Button {
        @Override
        public void render() {
            System.out.println("Rendering Windows Button 🪟");
        }
    }
    
    static class WindowsCheckbox implements Checkbox {
        @Override
        public void render() {
            System.out.println("Rendering Windows Checkbox ☑️");
        }
    }
    
    // Mac Products
    static class MacButton implements Button {
        @Override
        public void render() {
            System.out.println("Rendering Mac Button 🍎");
        }
    }
    
    static class MacCheckbox implements Checkbox {
        @Override
        public void render() {
            System.out.println("Rendering Mac Checkbox ✅");
        }
    }
    
    // Abstract Factory
    interface GUIFactory {
        Button createButton();
        Checkbox createCheckbox();
    }
    
    // Concrete Factories
    static class WindowsFactory implements GUIFactory {
        @Override
        public Button createButton() {
            return new WindowsButton();
        }
        
        @Override
        public Checkbox createCheckbox() {
            return new WindowsCheckbox();
        }
    }
    
    static class MacFactory implements GUIFactory {
        @Override
        public Button createButton() {
            return new MacButton();
        }
        
        @Override
        public Checkbox createCheckbox() {
            return new MacCheckbox();
        }
    }
    
    // Client code
    static class Application {
        private Button button;
        private Checkbox checkbox;
        
        public Application(GUIFactory factory) {
            button = factory.createButton();
            checkbox = factory.createCheckbox();
        }
        
        public void render() {
            button.render();
            checkbox.render();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Abstract Factory Pattern Demo ===\n");
        
        System.out.println("Creating Windows Application:");
        GUIFactory windowsFactory = new WindowsFactory();
        Application windowsApp = new Application(windowsFactory);
        windowsApp.render();
        
        System.out.println("\nCreating Mac Application:");
        GUIFactory macFactory = new MacFactory();
        Application macApp = new Application(macFactory);
        macApp.render();
    }
}
