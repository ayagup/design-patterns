package creational;

/**
 * Prototype Pattern
 * Creates new objects by cloning existing ones.
 */
public class PrototypePattern {
    
    // Prototype interface
    interface Prototype extends Cloneable {
        Prototype clone();
    }
    
    // Concrete Prototype
    static class Shape implements Prototype {
        private String type;
        private String color;
        private int x;
        private int y;
        
        public Shape(String type, String color, int x, int y) {
            this.type = type;
            this.color = color;
            this.x = x;
            this.y = y;
        }
        
        // Copy constructor
        public Shape(Shape source) {
            this.type = source.type;
            this.color = source.color;
            this.x = source.x;
            this.y = source.y;
        }
        
        @Override
        public Shape clone() {
            return new Shape(this);
        }
        
        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "Shape{type='" + type + "', color='" + color + 
                   "', position=(" + x + "," + y + ")}";
        }
    }
    
    // Prototype Registry
    static class ShapeRegistry {
        private java.util.Map<String, Shape> shapes = new java.util.HashMap<>();
        
        public void addShape(String key, Shape shape) {
            shapes.put(key, shape);
        }
        
        public Shape getShape(String key) {
            Shape prototype = shapes.get(key);
            return prototype != null ? prototype.clone() : null;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Prototype Pattern Demo ===\n");
        
        // Create original shapes
        Shape circle = new Shape("Circle", "Red", 10, 20);
        Shape rectangle = new Shape("Rectangle", "Blue", 30, 40);
        
        System.out.println("Original shapes:");
        System.out.println(circle);
        System.out.println(rectangle);
        
        // Clone shapes
        Shape clonedCircle = circle.clone();
        clonedCircle.setPosition(50, 60);
        
        Shape clonedRectangle = rectangle.clone();
        clonedRectangle.setPosition(70, 80);
        
        System.out.println("\nCloned shapes (with modified positions):");
        System.out.println(clonedCircle);
        System.out.println(clonedRectangle);
        
        System.out.println("\nOriginal shapes (unchanged):");
        System.out.println(circle);
        System.out.println(rectangle);
        
        // Using registry
        System.out.println("\n--- Using Prototype Registry ---");
        ShapeRegistry registry = new ShapeRegistry();
        registry.addShape("red-circle", new Shape("Circle", "Red", 0, 0));
        registry.addShape("blue-rectangle", new Shape("Rectangle", "Blue", 0, 0));
        
        Shape shape1 = registry.getShape("red-circle");
        shape1.setPosition(100, 100);
        
        Shape shape2 = registry.getShape("red-circle");
        shape2.setPosition(200, 200);
        
        System.out.println("Shape 1: " + shape1);
        System.out.println("Shape 2: " + shape2);
    }
}
