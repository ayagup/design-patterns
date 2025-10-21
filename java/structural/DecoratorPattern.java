package structural;

/**
 * Decorator Pattern
 * Adds new functionality to objects dynamically without altering their structure.
 */
public class DecoratorPattern {
    
    // Component interface
    interface Coffee {
        String getDescription();
        double getCost();
    }
    
    // Concrete Component
    static class SimpleCoffee implements Coffee {
        @Override
        public String getDescription() {
            return "Simple Coffee";
        }
        
        @Override
        public double getCost() {
            return 2.00;
        }
    }
    
    // Decorator base class
    abstract static class CoffeeDecorator implements Coffee {
        protected Coffee decoratedCoffee;
        
        public CoffeeDecorator(Coffee coffee) {
            this.decoratedCoffee = coffee;
        }
        
        @Override
        public String getDescription() {
            return decoratedCoffee.getDescription();
        }
        
        @Override
        public double getCost() {
            return decoratedCoffee.getCost();
        }
    }
    
    // Concrete Decorators
    static class MilkDecorator extends CoffeeDecorator {
        public MilkDecorator(Coffee coffee) {
            super(coffee);
        }
        
        @Override
        public String getDescription() {
            return decoratedCoffee.getDescription() + ", Milk";
        }
        
        @Override
        public double getCost() {
            return decoratedCoffee.getCost() + 0.50;
        }
    }
    
    static class SugarDecorator extends CoffeeDecorator {
        public SugarDecorator(Coffee coffee) {
            super(coffee);
        }
        
        @Override
        public String getDescription() {
            return decoratedCoffee.getDescription() + ", Sugar";
        }
        
        @Override
        public double getCost() {
            return decoratedCoffee.getCost() + 0.25;
        }
    }
    
    static class WhipDecorator extends CoffeeDecorator {
        public WhipDecorator(Coffee coffee) {
            super(coffee);
        }
        
        @Override
        public String getDescription() {
            return decoratedCoffee.getDescription() + ", Whipped Cream";
        }
        
        @Override
        public double getCost() {
            return decoratedCoffee.getCost() + 0.75;
        }
    }
    
    static class CaramelDecorator extends CoffeeDecorator {
        public CaramelDecorator(Coffee coffee) {
            super(coffee);
        }
        
        @Override
        public String getDescription() {
            return decoratedCoffee.getDescription() + ", Caramel";
        }
        
        @Override
        public double getCost() {
            return decoratedCoffee.getCost() + 0.60;
        }
    }
    
    // Text formatting example
    interface Text {
        String format();
    }
    
    static class PlainText implements Text {
        private String text;
        
        public PlainText(String text) {
            this.text = text;
        }
        
        @Override
        public String format() {
            return text;
        }
    }
    
    abstract static class TextDecorator implements Text {
        protected Text decoratedText;
        
        public TextDecorator(Text text) {
            this.decoratedText = text;
        }
    }
    
    static class BoldDecorator extends TextDecorator {
        public BoldDecorator(Text text) {
            super(text);
        }
        
        @Override
        public String format() {
            return "<b>" + decoratedText.format() + "</b>";
        }
    }
    
    static class ItalicDecorator extends TextDecorator {
        public ItalicDecorator(Text text) {
            super(text);
        }
        
        @Override
        public String format() {
            return "<i>" + decoratedText.format() + "</i>";
        }
    }
    
    static class UnderlineDecorator extends TextDecorator {
        public UnderlineDecorator(Text text) {
            super(text);
        }
        
        @Override
        public String format() {
            return "<u>" + decoratedText.format() + "</u>";
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Decorator Pattern Demo ===\n");
        
        // Coffee example
        System.out.println("1. Coffee Shop Orders:");
        
        Coffee coffee1 = new SimpleCoffee();
        System.out.println(coffee1.getDescription() + " - $" + 
                         String.format("%.2f", coffee1.getCost()));
        
        Coffee coffee2 = new MilkDecorator(new SimpleCoffee());
        System.out.println(coffee2.getDescription() + " - $" + 
                         String.format("%.2f", coffee2.getCost()));
        
        Coffee coffee3 = new MilkDecorator(new SugarDecorator(new SimpleCoffee()));
        System.out.println(coffee3.getDescription() + " - $" + 
                         String.format("%.2f", coffee3.getCost()));
        
        Coffee coffee4 = new WhipDecorator(new CaramelDecorator(
                         new MilkDecorator(new SimpleCoffee())));
        System.out.println(coffee4.getDescription() + " - $" + 
                         String.format("%.2f", coffee4.getCost()));
        
        // Text formatting example
        System.out.println("\n2. Text Formatting:");
        
        Text text1 = new PlainText("Hello World");
        System.out.println("Plain: " + text1.format());
        
        Text text2 = new BoldDecorator(new PlainText("Hello World"));
        System.out.println("Bold: " + text2.format());
        
        Text text3 = new ItalicDecorator(new BoldDecorator(
                     new PlainText("Hello World")));
        System.out.println("Bold + Italic: " + text3.format());
        
        Text text4 = new UnderlineDecorator(new ItalicDecorator(
                     new BoldDecorator(new PlainText("Hello World"))));
        System.out.println("Bold + Italic + Underline: " + text4.format());
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Adds responsibilities dynamically");
        System.out.println("✓ More flexible than inheritance");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ Can combine decorators in various ways");
    }
}
