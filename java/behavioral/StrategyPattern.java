package behavioral;

/**
 * Strategy Pattern
 * Defines a family of algorithms and makes them interchangeable.
 */
public class StrategyPattern {
    
    // Strategy interface
    interface PaymentStrategy {
        void pay(int amount);
    }
    
    // Concrete Strategies
    static class CreditCardStrategy implements PaymentStrategy {
        private String cardNumber;
        private String cvv;
        
        public CreditCardStrategy(String cardNumber, String cvv) {
            this.cardNumber = cardNumber;
            this.cvv = cvv;
        }
        
        @Override
        public void pay(int amount) {
            System.out.println("💳 Paid $" + amount + " using Credit Card ****" + 
                             cardNumber.substring(cardNumber.length() - 4));
        }
    }
    
    static class PayPalStrategy implements PaymentStrategy {
        private String email;
        
        public PayPalStrategy(String email) {
            this.email = email;
        }
        
        @Override
        public void pay(int amount) {
            System.out.println("💰 Paid $" + amount + " using PayPal account: " + email);
        }
    }
    
    static class BitcoinStrategy implements PaymentStrategy {
        private String walletAddress;
        
        public BitcoinStrategy(String walletAddress) {
            this.walletAddress = walletAddress;
        }
        
        @Override
        public void pay(int amount) {
            System.out.println("₿ Paid $" + amount + " using Bitcoin wallet: " + 
                             walletAddress.substring(0, 10) + "...");
        }
    }
    
    // Context
    static class ShoppingCart {
        private int totalAmount = 0;
        private PaymentStrategy paymentStrategy;
        
        public void addItem(int price) {
            totalAmount += price;
        }
        
        public void setPaymentStrategy(PaymentStrategy strategy) {
            this.paymentStrategy = strategy;
        }
        
        public void checkout() {
            if (paymentStrategy == null) {
                System.out.println("❌ Please select a payment method");
                return;
            }
            paymentStrategy.pay(totalAmount);
            totalAmount = 0;
        }
        
        public int getTotal() {
            return totalAmount;
        }
    }
    
    // Sorting strategy example
    interface SortStrategy {
        void sort(int[] array);
    }
    
    static class BubbleSort implements SortStrategy {
        @Override
        public void sort(int[] array) {
            System.out.println("Sorting using Bubble Sort");
            for (int i = 0; i < array.length - 1; i++) {
                for (int j = 0; j < array.length - i - 1; j++) {
                    if (array[j] > array[j + 1]) {
                        int temp = array[j];
                        array[j] = array[j + 1];
                        array[j + 1] = temp;
                    }
                }
            }
        }
    }
    
    static class QuickSort implements SortStrategy {
        @Override
        public void sort(int[] array) {
            System.out.println("Sorting using Quick Sort");
            quickSort(array, 0, array.length - 1);
        }
        
        private void quickSort(int[] arr, int low, int high) {
            if (low < high) {
                int pi = partition(arr, low, high);
                quickSort(arr, low, pi - 1);
                quickSort(arr, pi + 1, high);
            }
        }
        
        private int partition(int[] arr, int low, int high) {
            int pivot = arr[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                if (arr[j] < pivot) {
                    i++;
                    int temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
            int temp = arr[i + 1];
            arr[i + 1] = arr[high];
            arr[high] = temp;
            return i + 1;
        }
    }
    
    static class Sorter {
        private SortStrategy strategy;
        
        public void setStrategy(SortStrategy strategy) {
            this.strategy = strategy;
        }
        
        public void sort(int[] array) {
            strategy.sort(array);
        }
    }
    
    // Compression strategy
    interface CompressionStrategy {
        void compress(String filename);
    }
    
    static class ZipCompression implements CompressionStrategy {
        @Override
        public void compress(String filename) {
            System.out.println("🗜️  Compressing " + filename + " using ZIP");
        }
    }
    
    static class RarCompression implements CompressionStrategy {
        @Override
        public void compress(String filename) {
            System.out.println("🗜️  Compressing " + filename + " using RAR");
        }
    }
    
    static class FileCompressor {
        private CompressionStrategy strategy;
        
        public void setCompressionStrategy(CompressionStrategy strategy) {
            this.strategy = strategy;
        }
        
        public void compress(String filename) {
            strategy.compress(filename);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Strategy Pattern Demo ===\n");
        
        // Payment strategy example
        System.out.println("1. Payment Strategies:");
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(100);
        cart.addItem(50);
        cart.addItem(75);
        System.out.println("Cart total: $" + cart.getTotal());
        
        System.out.println("\nPaying with Credit Card:");
        cart.setPaymentStrategy(new CreditCardStrategy("1234567890123456", "123"));
        cart.checkout();
        
        cart.addItem(200);
        System.out.println("\nCart total: $" + cart.getTotal());
        System.out.println("Paying with PayPal:");
        cart.setPaymentStrategy(new PayPalStrategy("user@example.com"));
        cart.checkout();
        
        cart.addItem(150);
        System.out.println("\nCart total: $" + cart.getTotal());
        System.out.println("Paying with Bitcoin:");
        cart.setPaymentStrategy(new BitcoinStrategy("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"));
        cart.checkout();
        
        // Sorting strategy example
        System.out.println("\n\n2. Sorting Strategies:");
        int[] data1 = {64, 34, 25, 12, 22, 11, 90};
        int[] data2 = {64, 34, 25, 12, 22, 11, 90};
        
        Sorter sorter = new Sorter();
        
        sorter.setStrategy(new BubbleSort());
        sorter.sort(data1);
        System.out.print("Result: ");
        for (int num : data1) System.out.print(num + " ");
        
        System.out.println("\n");
        sorter.setStrategy(new QuickSort());
        sorter.sort(data2);
        System.out.print("Result: ");
        for (int num : data2) System.out.print(num + " ");
        
        // Compression strategy
        System.out.println("\n\n\n3. Compression Strategies:");
        FileCompressor compressor = new FileCompressor();
        
        compressor.setCompressionStrategy(new ZipCompression());
        compressor.compress("document.pdf");
        
        compressor.setCompressionStrategy(new RarCompression());
        compressor.compress("photos.jpg");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Family of algorithms interchangeable");
        System.out.println("✓ Encapsulates algorithm selection");
        System.out.println("✓ Eliminates conditional statements");
        System.out.println("✓ Easy to add new strategies");
        System.out.println("✓ Runtime algorithm selection");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Payment processing");
        System.out.println("• Sorting algorithms");
        System.out.println("• Compression algorithms");
        System.out.println("• Validation strategies");
    }
}
