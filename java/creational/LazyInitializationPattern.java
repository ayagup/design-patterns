package creational;

/**
 * Lazy Initialization Pattern
 * Delays object creation until it's needed.
 */
public class LazyInitializationPattern {
    
    // Heavy object that's expensive to create
    static class HeavyObject {
        public HeavyObject() {
            System.out.println("Creating HeavyObject (expensive operation)...");
            // Simulate expensive initialization
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("HeavyObject created!");
        }
        
        public void doSomething() {
            System.out.println("HeavyObject is doing something");
        }
    }
    
    // Lazy initialization holder
    static class LazyHolder {
        private HeavyObject heavyObject;
        
        public HeavyObject getHeavyObject() {
            if (heavyObject == null) {
                System.out.println("First access - initializing HeavyObject");
                heavyObject = new HeavyObject();
            } else {
                System.out.println("Returning existing HeavyObject");
            }
            return heavyObject;
        }
    }
    
    // Thread-safe lazy initialization
    static class ThreadSafeLazyHolder {
        private volatile HeavyObject heavyObject;
        
        public HeavyObject getHeavyObject() {
            if (heavyObject == null) {
                synchronized (this) {
                    if (heavyObject == null) {
                        System.out.println("First access (thread-safe) - initializing HeavyObject");
                        heavyObject = new HeavyObject();
                    }
                }
            }
            return heavyObject;
        }
    }
    
    // Lazy initialization using holder class (Bill Pugh approach)
    static class HolderIdiom {
        private HolderIdiom() {}
        
        private static class Holder {
            private static final HeavyObject INSTANCE = new HeavyObject();
        }
        
        public static HeavyObject getHeavyObject() {
            return Holder.INSTANCE;
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Lazy Initialization Pattern Demo ===\n");
        
        System.out.println("1. Basic Lazy Initialization:");
        LazyHolder holder = new LazyHolder();
        System.out.println("Holder created (HeavyObject not created yet)");
        System.out.println("\nAccessing HeavyObject for the first time:");
        holder.getHeavyObject().doSomething();
        System.out.println("\nAccessing HeavyObject again:");
        holder.getHeavyObject().doSomething();
        
        System.out.println("\n\n2. Thread-Safe Lazy Initialization:");
        ThreadSafeLazyHolder threadSafeHolder = new ThreadSafeLazyHolder();
        System.out.println("ThreadSafeHolder created");
        System.out.println("\nAccessing in multiple threads:");
        
        Thread t1 = new Thread(() -> threadSafeHolder.getHeavyObject().doSomething());
        Thread t2 = new Thread(() -> threadSafeHolder.getHeavyObject().doSomething());
        
        t1.start();
        t2.start();
        
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n\n3. Holder Idiom (Bill Pugh):");
        System.out.println("Before accessing HeavyObject");
        HolderIdiom.getHeavyObject().doSomething();
    }
}
