package behavioral;

import java.util.*;

/**
 * Iterator Pattern
 * Provides a way to access elements of a collection sequentially.
 */
public class IteratorPattern {
    
    // Iterator interface
    interface Iterator<T> {
        boolean hasNext();
        T next();
    }
    
    // Aggregate interface
    interface Container<T> {
        Iterator<T> createIterator();
    }
    
    // Concrete Aggregate - Book Collection
    static class BookCollection implements Container<String> {
        private List<String> books = new ArrayList<>();
        
        public void addBook(String book) {
            books.add(book);
        }
        
        @Override
        public Iterator<String> createIterator() {
            return new BookIterator();
        }
        
        // Concrete Iterator
        private class BookIterator implements Iterator<String> {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < books.size();
            }
            
            @Override
            public String next() {
                if (hasNext()) {
                    return books.get(index++);
                }
                throw new NoSuchElementException();
            }
        }
    }
    
    // Custom collection with reverse iterator
    static class NameRepository implements Container<String> {
        private String[] names = {"Alice", "Bob", "Charlie", "David", "Eve"};
        
        @Override
        public Iterator<String> createIterator() {
            return new NameIterator();
        }
        
        public Iterator<String> createReverseIterator() {
            return new ReverseNameIterator();
        }
        
        private class NameIterator implements Iterator<String> {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < names.length;
            }
            
            @Override
            public String next() {
                if (hasNext()) {
                    return names[index++];
                }
                throw new NoSuchElementException();
            }
        }
        
        private class ReverseNameIterator implements Iterator<String> {
            private int index = names.length - 1;
            
            @Override
            public boolean hasNext() {
                return index >= 0;
            }
            
            @Override
            public String next() {
                if (hasNext()) {
                    return names[index--];
                }
                throw new NoSuchElementException();
            }
        }
    }
    
    // Generic collection with filter iterator
    static class NumberCollection implements Container<Integer> {
        private List<Integer> numbers = new ArrayList<>();
        
        public void addNumber(int number) {
            numbers.add(number);
        }
        
        @Override
        public Iterator<Integer> createIterator() {
            return new NumberIterator();
        }
        
        public Iterator<Integer> createEvenIterator() {
            return new EvenNumberIterator();
        }
        
        private class NumberIterator implements Iterator<Integer> {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                return index < numbers.size();
            }
            
            @Override
            public Integer next() {
                if (hasNext()) {
                    return numbers.get(index++);
                }
                throw new NoSuchElementException();
            }
        }
        
        private class EvenNumberIterator implements Iterator<Integer> {
            private int index = 0;
            
            @Override
            public boolean hasNext() {
                while (index < numbers.size()) {
                    if (numbers.get(index) % 2 == 0) {
                        return true;
                    }
                    index++;
                }
                return false;
            }
            
            @Override
            public Integer next() {
                if (hasNext()) {
                    return numbers.get(index++);
                }
                throw new NoSuchElementException();
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Iterator Pattern Demo ===\n");
        
        // Book collection
        System.out.println("1. Book Collection Iterator:");
        BookCollection bookCollection = new BookCollection();
        bookCollection.addBook("Design Patterns");
        bookCollection.addBook("Clean Code");
        bookCollection.addBook("Refactoring");
        bookCollection.addBook("The Pragmatic Programmer");
        
        Iterator<String> bookIterator = bookCollection.createIterator();
        while (bookIterator.hasNext()) {
            System.out.println("📚 " + bookIterator.next());
        }
        
        // Name repository with forward and reverse iterators
        System.out.println("\n2. Name Repository (Forward and Reverse):");
        NameRepository nameRepo = new NameRepository();
        
        System.out.println("Forward iteration:");
        Iterator<String> forwardIterator = nameRepo.createIterator();
        while (forwardIterator.hasNext()) {
            System.out.println("👤 " + forwardIterator.next());
        }
        
        System.out.println("\nReverse iteration:");
        Iterator<String> reverseIterator = nameRepo.createReverseIterator();
        while (reverseIterator.hasNext()) {
            System.out.println("👤 " + reverseIterator.next());
        }
        
        // Number collection with filtered iterator
        System.out.println("\n3. Number Collection (All and Even Only):");
        NumberCollection numbers = new NumberCollection();
        for (int i = 1; i <= 10; i++) {
            numbers.addNumber(i);
        }
        
        System.out.println("All numbers:");
        Iterator<Integer> allNumbers = numbers.createIterator();
        while (allNumbers.hasNext()) {
            System.out.print(allNumbers.next() + " ");
        }
        
        System.out.println("\n\nEven numbers only:");
        Iterator<Integer> evenNumbers = numbers.createEvenIterator();
        while (evenNumbers.hasNext()) {
            System.out.print(evenNumbers.next() + " ");
        }
        
        System.out.println("\n\n--- Benefits ---");
        System.out.println("✓ Provides uniform interface for traversal");
        System.out.println("✓ Supports multiple simultaneous traversals");
        System.out.println("✓ Encapsulates internal structure");
        System.out.println("✓ Simplifies aggregate interface");
        
        System.out.println("\n--- Java's Built-in Iterator ---");
        System.out.println("Java Collections Framework uses this pattern extensively");
        List<String> javaList = Arrays.asList("A", "B", "C");
        java.util.Iterator<String> javaIterator = javaList.iterator();
        System.out.print("Using Java Iterator: ");
        while (javaIterator.hasNext()) {
            System.out.print(javaIterator.next() + " ");
        }
        System.out.println();
    }
}
