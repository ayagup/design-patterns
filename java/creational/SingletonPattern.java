package creational;

/**
 * Singleton Pattern
 * Ensures a class has only one instance and provides a global point of access to it.
 */
public class SingletonPattern {
    
    // Eager Initialization Singleton
    static class EagerSingleton {
        private static final EagerSingleton INSTANCE = new EagerSingleton();
        
        private EagerSingleton() {
            System.out.println("EagerSingleton instance created");
        }
        
        public static EagerSingleton getInstance() {
            return INSTANCE;
        }
    }
    
    // Lazy Initialization Singleton (Thread-Safe)
    static class LazySingleton {
        private static LazySingleton instance;
        
        private LazySingleton() {
            System.out.println("LazySingleton instance created");
        }
        
        public static synchronized LazySingleton getInstance() {
            if (instance == null) {
                instance = new LazySingleton();
            }
            return instance;
        }
    }
    
    // Bill Pugh Singleton (Best Practice)
    static class BillPughSingleton {
        private BillPughSingleton() {
            System.out.println("BillPughSingleton instance created");
        }
        
        private static class SingletonHelper {
            private static final BillPughSingleton INSTANCE = new BillPughSingleton();
        }
        
        public static BillPughSingleton getInstance() {
            return SingletonHelper.INSTANCE;
        }
    }
    
    // Enum Singleton (Most secure)
    enum EnumSingleton {
        INSTANCE;
        
        public void doSomething() {
            System.out.println("EnumSingleton doing something");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Singleton Pattern Demo ===\n");
        
        // Eager Singleton
        System.out.println("1. Eager Singleton:");
        EagerSingleton eager1 = EagerSingleton.getInstance();
        EagerSingleton eager2 = EagerSingleton.getInstance();
        System.out.println("Same instance? " + (eager1 == eager2));
        
        // Lazy Singleton
        System.out.println("\n2. Lazy Singleton:");
        LazySingleton lazy1 = LazySingleton.getInstance();
        LazySingleton lazy2 = LazySingleton.getInstance();
        System.out.println("Same instance? " + (lazy1 == lazy2));
        
        // Bill Pugh Singleton
        System.out.println("\n3. Bill Pugh Singleton:");
        BillPughSingleton bp1 = BillPughSingleton.getInstance();
        BillPughSingleton bp2 = BillPughSingleton.getInstance();
        System.out.println("Same instance? " + (bp1 == bp2));
        
        // Enum Singleton
        System.out.println("\n4. Enum Singleton:");
        EnumSingleton.INSTANCE.doSomething();
    }
}
