package structural;

/**
 * Private Class Data Pattern
 * Restricts accessor/mutator access to class data.
 */
public class PrivateClassDataPattern {
    
    // Data class with immutable data
    static class CircleData {
        private final double radius;
        private final String color;
        private final int x;
        private final int y;
        
        public CircleData(double radius, String color, int x, int y) {
            this.radius = radius;
            this.color = color;
            this.x = x;
            this.y = y;
        }
        
        public double getRadius() {
            return radius;
        }
        
        public String getColor() {
            return color;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
    }
    
    // Main class that uses private data
    static class Circle {
        private final CircleData data;
        
        public Circle(double radius, String color, int x, int y) {
            this.data = new CircleData(radius, color, x, y);
        }
        
        public double getRadius() {
            return data.getRadius();
        }
        
        public double getCircumference() {
            return 2 * Math.PI * data.getRadius();
        }
        
        public double getArea() {
            return Math.PI * data.getRadius() * data.getRadius();
        }
        
        public void display() {
            System.out.println("Circle: [Color=" + data.getColor() + 
                             ", Radius=" + data.getRadius() + 
                             ", Position=(" + data.getX() + "," + data.getY() + ")]");
        }
    }
    
    // Another example - Immutable Person
    static class PersonData {
        private final String name;
        private final int age;
        private final String ssn;
        
        public PersonData(String name, int age, String ssn) {
            this.name = name;
            this.age = age;
            this.ssn = ssn;
        }
        
        public String getName() {
            return name;
        }
        
        public int getAge() {
            return age;
        }
        
        String getSSN() { // Package-private access
            return ssn;
        }
    }
    
    static class Person {
        private final PersonData data;
        
        public Person(String name, int age, String ssn) {
            this.data = new PersonData(name, age, ssn);
        }
        
        public String getName() {
            return data.getName();
        }
        
        public int getAge() {
            return data.getAge();
        }
        
        // SSN is protected and not directly accessible
        public String getMaskedSSN() {
            String ssn = data.getSSN();
            return "XXX-XX-" + ssn.substring(ssn.length() - 4);
        }
        
        public void displayInfo() {
            System.out.println("Person: " + data.getName() + 
                             ", Age: " + data.getAge() + 
                             ", SSN: " + getMaskedSSN());
        }
    }
    
    // Book example with controlled access
    static class BookData {
        private final String title;
        private final String author;
        private final String isbn;
        private final double price;
        
        public BookData(String title, String author, String isbn, double price) {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
            this.price = price;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getAuthor() {
            return author;
        }
        
        public String getIsbn() {
            return isbn;
        }
        
        public double getPrice() {
            return price;
        }
    }
    
    static class Book {
        private final BookData data;
        
        public Book(String title, String author, String isbn, double price) {
            this.data = new BookData(title, author, isbn, price);
        }
        
        public String getTitle() {
            return data.getTitle();
        }
        
        public String getAuthor() {
            return data.getAuthor();
        }
        
        public double getDiscountedPrice(double discountPercent) {
            return data.getPrice() * (1 - discountPercent / 100);
        }
        
        public void displayInfo() {
            System.out.println("Book: '" + data.getTitle() + "' by " + 
                             data.getAuthor() + " - $" + data.getPrice() + 
                             " (ISBN: " + data.getIsbn() + ")");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Private Class Data Pattern Demo ===\n");
        
        // Circle example
        System.out.println("1. Circle with Protected Data:");
        Circle circle = new Circle(5.0, "Red", 10, 20);
        circle.display();
        System.out.println("Area: " + String.format("%.2f", circle.getArea()));
        System.out.println("Circumference: " + 
                         String.format("%.2f", circle.getCircumference()));
        
        // Person example
        System.out.println("\n2. Person with Sensitive Data Protection:");
        Person person = new Person("John Doe", 30, "123-45-6789");
        person.displayInfo();
        System.out.println("Masked SSN: " + person.getMaskedSSN());
        
        // Book example
        System.out.println("\n3. Book with Controlled Price Access:");
        Book book = new Book("Design Patterns", "Gang of Four", 
                            "978-0201633610", 54.99);
        book.displayInfo();
        System.out.println("Price with 10% discount: $" + 
                         String.format("%.2f", book.getDiscountedPrice(10)));
        System.out.println("Price with 25% discount: $" + 
                         String.format("%.2f", book.getDiscountedPrice(25)));
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Encapsulates class data");
        System.out.println("✓ Makes class data immutable");
        System.out.println("✓ Reduces coupling");
        System.out.println("✓ Prevents unintended modifications");
        System.out.println("✓ Enforces read-only access where needed");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• When you want to make class data immutable");
        System.out.println("• When you need fine-grained access control");
        System.out.println("• When you want to protect sensitive data");
        System.out.println("• When you need to control how data is accessed");
    }
}
