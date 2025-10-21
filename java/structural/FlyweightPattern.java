package structural;

import java.util.HashMap;
import java.util.Map;

/**
 * Flyweight Pattern
 * Shares objects to support large numbers of fine-grained objects efficiently.
 */
public class FlyweightPattern {
    
    // Flyweight interface
    interface Shape {
        void draw(int x, int y, int radius, String color);
    }
    
    // Concrete Flyweight - intrinsic state (shared)
    static class Circle implements Shape {
        private final String type = "Circle";
        
        @Override
        public void draw(int x, int y, int radius, String color) {
            System.out.println("Drawing " + type + " [Color: " + color + 
                             ", x: " + x + ", y: " + y + ", radius: " + radius + "]");
        }
    }
    
    // Flyweight Factory
    static class ShapeFactory {
        private static final Map<String, Shape> circleMap = new HashMap<>();
        
        public static Shape getCircle(String color) {
            Circle circle = (Circle) circleMap.get(color);
            
            if (circle == null) {
                circle = new Circle();
                circleMap.put(color, circle);
                System.out.println("Creating circle of color: " + color);
            }
            return circle;
        }
        
        public static int getTotalObjects() {
            return circleMap.size();
        }
    }
    
    // Character example (like in text editors)
    static class Character {
        private final char value;
        private final String font;
        private final int size;
        
        public Character(char value, String font, int size) {
            this.value = value;
            this.font = font;
            this.size = size;
        }
        
        public void display(int row, int column) {
            System.out.println("Char '" + value + "' at [" + row + "," + column + 
                             "] - Font: " + font + ", Size: " + size);
        }
    }
    
    static class CharacterFactory {
        private static final Map<String, Character> characters = new HashMap<>();
        
        public static Character getCharacter(char value, String font, int size) {
            String key = value + "-" + font + "-" + size;
            Character character = characters.get(key);
            
            if (character == null) {
                character = new Character(value, font, size);
                characters.put(key, character);
                System.out.println("Creating new character: '" + value + "'");
            }
            return character;
        }
        
        public static int getTotalCharacters() {
            return characters.size();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Flyweight Pattern Demo ===\n");
        
        // Shape example
        System.out.println("1. Drawing Circles (Shared Colors):");
        String[] colors = {"Red", "Green", "Blue", "Red", "Green", "Red"};
        
        for (int i = 0; i < 6; i++) {
            Shape circle = ShapeFactory.getCircle(colors[i]);
            circle.draw(i * 10, i * 20, i * 5 + 10, colors[i]);
        }
        
        System.out.println("\nTotal unique circle objects created: " + 
                         ShapeFactory.getTotalObjects());
        System.out.println("(Instead of 6 objects, only 3 were created due to sharing)\n");
        
        // Text editor example
        System.out.println("2. Text Editor Characters (Shared Character Objects):");
        String text = "HELLO";
        String font = "Arial";
        int fontSize = 12;
        
        for (int i = 0; i < text.length(); i++) {
            Character character = CharacterFactory.getCharacter(
                text.charAt(i), font, fontSize);
            character.display(0, i);
        }
        
        // Add more text with same characters
        System.out.println("\nAdding more text 'HELLO WORLD':");
        text = "HELLO WORLD";
        for (int i = 0; i < text.length(); i++) {
            Character character = CharacterFactory.getCharacter(
                text.charAt(i), font, fontSize);
        }
        
        System.out.println("\nTotal unique character objects created: " + 
                         CharacterFactory.getTotalCharacters());
        System.out.println("(Only unique characters were created, duplicates were reused)");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Reduces memory usage by sharing common state");
        System.out.println("✓ Improves performance with large numbers of objects");
        System.out.println("✓ Intrinsic state is shared, extrinsic state is passed");
        
        System.out.println("\n--- Trade-offs ---");
        System.out.println("⚠ Increased complexity");
        System.out.println("⚠ Runtime costs for managing flyweights");
    }
}
