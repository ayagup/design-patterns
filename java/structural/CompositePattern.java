package structural;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Pattern
 * Composes objects into tree structures to represent part-whole hierarchies.
 */
public class CompositePattern {
    
    // Component interface
    interface FileSystemComponent {
        void display(String indent);
        int getSize();
    }
    
    // Leaf
    static class File implements FileSystemComponent {
        private String name;
        private int size;
        
        public File(String name, int size) {
            this.name = name;
            this.size = size;
        }
        
        @Override
        public void display(String indent) {
            System.out.println(indent + "📄 " + name + " (" + size + " KB)");
        }
        
        @Override
        public int getSize() {
            return size;
        }
    }
    
    // Composite
    static class Directory implements FileSystemComponent {
        private String name;
        private List<FileSystemComponent> children = new ArrayList<>();
        
        public Directory(String name) {
            this.name = name;
        }
        
        public void add(FileSystemComponent component) {
            children.add(component);
        }
        
        public void remove(FileSystemComponent component) {
            children.remove(component);
        }
        
        @Override
        public void display(String indent) {
            System.out.println(indent + "📁 " + name + "/");
            for (FileSystemComponent child : children) {
                child.display(indent + "  ");
            }
        }
        
        @Override
        public int getSize() {
            int totalSize = 0;
            for (FileSystemComponent child : children) {
                totalSize += child.getSize();
            }
            return totalSize;
        }
    }
    
    // Organization structure example
    interface Employee {
        void showDetails(String indent);
        int getSalary();
    }
    
    static class Developer implements Employee {
        private String name;
        private int salary;
        
        public Developer(String name, int salary) {
            this.name = name;
            this.salary = salary;
        }
        
        @Override
        public void showDetails(String indent) {
            System.out.println(indent + "👨‍💻 Developer: " + name + " ($" + salary + ")");
        }
        
        @Override
        public int getSalary() {
            return salary;
        }
    }
    
    static class Manager implements Employee {
        private String name;
        private int salary;
        private List<Employee> subordinates = new ArrayList<>();
        
        public Manager(String name, int salary) {
            this.name = name;
            this.salary = salary;
        }
        
        public void addSubordinate(Employee employee) {
            subordinates.add(employee);
        }
        
        @Override
        public void showDetails(String indent) {
            System.out.println(indent + "👔 Manager: " + name + " ($" + salary + ")");
            for (Employee subordinate : subordinates) {
                subordinate.showDetails(indent + "  ");
            }
        }
        
        @Override
        public int getSalary() {
            int totalSalary = salary;
            for (Employee subordinate : subordinates) {
                totalSalary += subordinate.getSalary();
            }
            return totalSalary;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Composite Pattern Demo ===\n");
        
        // File System example
        System.out.println("1. File System Hierarchy:");
        Directory root = new Directory("root");
        
        File file1 = new File("file1.txt", 10);
        File file2 = new File("file2.txt", 20);
        root.add(file1);
        root.add(file2);
        
        Directory subDir1 = new Directory("documents");
        subDir1.add(new File("doc1.pdf", 50));
        subDir1.add(new File("doc2.pdf", 30));
        
        Directory subDir2 = new Directory("images");
        subDir2.add(new File("photo1.jpg", 100));
        subDir2.add(new File("photo2.jpg", 150));
        
        root.add(subDir1);
        root.add(subDir2);
        
        root.display("");
        System.out.println("\nTotal size: " + root.getSize() + " KB");
        
        // Organization example
        System.out.println("\n\n2. Organization Hierarchy:");
        
        Developer dev1 = new Developer("Alice", 80000);
        Developer dev2 = new Developer("Bob", 85000);
        Developer dev3 = new Developer("Charlie", 75000);
        Developer dev4 = new Developer("David", 90000);
        
        Manager teamLead1 = new Manager("Eve", 100000);
        teamLead1.addSubordinate(dev1);
        teamLead1.addSubordinate(dev2);
        
        Manager teamLead2 = new Manager("Frank", 105000);
        teamLead2.addSubordinate(dev3);
        teamLead2.addSubordinate(dev4);
        
        Manager cto = new Manager("Grace", 150000);
        cto.addSubordinate(teamLead1);
        cto.addSubordinate(teamLead2);
        
        cto.showDetails("");
        System.out.println("\nTotal company payroll: $" + cto.getSalary());
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Treats individual objects and compositions uniformly");
        System.out.println("✓ Makes it easy to add new component types");
        System.out.println("✓ Simplifies client code");
    }
}
