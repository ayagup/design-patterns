package behavioral;

/**
 * Visitor Pattern
 * Separates an algorithm from the object structure it operates on.
 */
public class VisitorPattern {
    
    // Visitor interface
    interface ShapeVisitor {
        void visit(Circle circle);
        void visit(Rectangle rectangle);
        void visit(Triangle triangle);
    }
    
    // Element interface
    interface Shape {
        void accept(ShapeVisitor visitor);
    }
    
    // Concrete Elements
    static class Circle implements Shape {
        private double radius;
        
        public Circle(double radius) {
            this.radius = radius;
        }
        
        public double getRadius() {
            return radius;
        }
        
        @Override
        public void accept(ShapeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    static class Rectangle implements Shape {
        private double width;
        private double height;
        
        public Rectangle(double width, double height) {
            this.width = width;
            this.height = height;
        }
        
        public double getWidth() {
            return width;
        }
        
        public double getHeight() {
            return height;
        }
        
        @Override
        public void accept(ShapeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    static class Triangle implements Shape {
        private double base;
        private double height;
        
        public Triangle(double base, double height) {
            this.base = base;
            this.height = height;
        }
        
        public double getBase() {
            return base;
        }
        
        public double getHeight() {
            return height;
        }
        
        @Override
        public void accept(ShapeVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    // Concrete Visitors
    static class AreaCalculator implements ShapeVisitor {
        @Override
        public void visit(Circle circle) {
            double area = Math.PI * circle.getRadius() * circle.getRadius();
            System.out.println("Circle area: " + String.format("%.2f", area));
        }
        
        @Override
        public void visit(Rectangle rectangle) {
            double area = rectangle.getWidth() * rectangle.getHeight();
            System.out.println("Rectangle area: " + String.format("%.2f", area));
        }
        
        @Override
        public void visit(Triangle triangle) {
            double area = 0.5 * triangle.getBase() * triangle.getHeight();
            System.out.println("Triangle area: " + String.format("%.2f", area));
        }
    }
    
    static class PerimeterCalculator implements ShapeVisitor {
        @Override
        public void visit(Circle circle) {
            double perimeter = 2 * Math.PI * circle.getRadius();
            System.out.println("Circle perimeter: " + String.format("%.2f", perimeter));
        }
        
        @Override
        public void visit(Rectangle rectangle) {
            double perimeter = 2 * (rectangle.getWidth() + rectangle.getHeight());
            System.out.println("Rectangle perimeter: " + String.format("%.2f", perimeter));
        }
        
        @Override
        public void visit(Triangle triangle) {
            // Assuming equilateral for simplicity
            double perimeter = 3 * triangle.getBase();
            System.out.println("Triangle perimeter (approx): " + String.format("%.2f", perimeter));
        }
    }
    
    static class DrawVisitor implements ShapeVisitor {
        @Override
        public void visit(Circle circle) {
            System.out.println("⭕ Drawing Circle with radius " + circle.getRadius());
        }
        
        @Override
        public void visit(Rectangle rectangle) {
            System.out.println("▭ Drawing Rectangle " + rectangle.getWidth() + "x" + rectangle.getHeight());
        }
        
        @Override
        public void visit(Triangle triangle) {
            System.out.println("△ Drawing Triangle with base " + triangle.getBase());
        }
    }
    
    // File system example
    interface FileSystemVisitor {
        void visit(File file);
        void visit(Directory directory);
    }
    
    interface FileSystemElement {
        void accept(FileSystemVisitor visitor);
    }
    
    static class File implements FileSystemElement {
        private String name;
        private int size;
        
        public File(String name, int size) {
            this.name = name;
            this.size = size;
        }
        
        public String getName() {
            return name;
        }
        
        public int getSize() {
            return size;
        }
        
        @Override
        public void accept(FileSystemVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    static class Directory implements FileSystemElement {
        private String name;
        private java.util.List<FileSystemElement> children = new java.util.ArrayList<>();
        
        public Directory(String name) {
            this.name = name;
        }
        
        public void add(FileSystemElement element) {
            children.add(element);
        }
        
        public String getName() {
            return name;
        }
        
        public java.util.List<FileSystemElement> getChildren() {
            return children;
        }
        
        @Override
        public void accept(FileSystemVisitor visitor) {
            visitor.visit(this);
        }
    }
    
    static class SizeCalculatorVisitor implements FileSystemVisitor {
        private int totalSize = 0;
        
        @Override
        public void visit(File file) {
            totalSize += file.getSize();
            System.out.println("  📄 " + file.getName() + ": " + file.getSize() + " KB");
        }
        
        @Override
        public void visit(Directory directory) {
            System.out.println("📁 " + directory.getName() + "/");
            for (FileSystemElement child : directory.getChildren()) {
                child.accept(this);
            }
        }
        
        public int getTotalSize() {
            return totalSize;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Visitor Pattern Demo ===\n");
        
        // Shape example
        System.out.println("1. Shape Operations:");
        Shape[] shapes = {
            new Circle(5),
            new Rectangle(4, 6),
            new Triangle(3, 4)
        };
        
        System.out.println("Calculating areas:");
        ShapeVisitor areaCalculator = new AreaCalculator();
        for (Shape shape : shapes) {
            shape.accept(areaCalculator);
        }
        
        System.out.println("\nCalculating perimeters:");
        ShapeVisitor perimeterCalculator = new PerimeterCalculator();
        for (Shape shape : shapes) {
            shape.accept(perimeterCalculator);
        }
        
        System.out.println("\nDrawing shapes:");
        ShapeVisitor drawVisitor = new DrawVisitor();
        for (Shape shape : shapes) {
            shape.accept(drawVisitor);
        }
        
        // File system example
        System.out.println("\n\n2. File System Operations:");
        Directory root = new Directory("root");
        root.add(new File("file1.txt", 100));
        root.add(new File("file2.pdf", 500));
        
        Directory subDir = new Directory("documents");
        subDir.add(new File("doc1.docx", 200));
        subDir.add(new File("doc2.xlsx", 300));
        root.add(subDir);
        
        SizeCalculatorVisitor sizeCalc = new SizeCalculatorVisitor();
        root.accept(sizeCalc);
        System.out.println("\nTotal size: " + sizeCalc.getTotalSize() + " KB");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Separates algorithm from object structure");
        System.out.println("✓ Easy to add new operations");
        System.out.println("✓ Gathers related operations");
        System.out.println("✓ Follows Open/Closed Principle");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Compiler AST traversal");
        System.out.println("• File system operations");
        System.out.println("• Document processing");
        System.out.println("• Reporting systems");
    }
}
