package creational;

/**
 * Dependency Injection Pattern
 * Provides objects with their dependencies rather than having them construct dependencies.
 */
public class DependencyInjectionPattern {
    
    // Service interfaces
    interface MessageService {
        void sendMessage(String message, String recipient);
    }
    
    // Concrete service implementations
    static class EmailService implements MessageService {
        @Override
        public void sendMessage(String message, String recipient) {
            System.out.println("Email sent to " + recipient + ": " + message);
        }
    }
    
    static class SMSService implements MessageService {
        @Override
        public void sendMessage(String message, String recipient) {
            System.out.println("SMS sent to " + recipient + ": " + message);
        }
    }
    
    static class PushNotificationService implements MessageService {
        @Override
        public void sendMessage(String message, String recipient) {
            System.out.println("Push notification sent to " + recipient + ": " + message);
        }
    }
    
    // Client class using Constructor Injection
    static class NotificationService {
        private final MessageService messageService;
        
        // Constructor Injection
        public NotificationService(MessageService messageService) {
            this.messageService = messageService;
        }
        
        public void notifyUser(String message, String recipient) {
            messageService.sendMessage(message, recipient);
        }
    }
    
    // Client class using Setter Injection
    static class UserNotifier {
        private MessageService messageService;
        
        // Setter Injection
        public void setMessageService(MessageService messageService) {
            this.messageService = messageService;
        }
        
        public void notify(String message, String recipient) {
            if (messageService == null) {
                throw new IllegalStateException("MessageService not injected");
            }
            messageService.sendMessage(message, recipient);
        }
    }
    
    // Simple DI Container
    static class DIContainer {
        private static final java.util.Map<Class<?>, Object> services = new java.util.HashMap<>();
        
        public static <T> void register(Class<T> serviceClass, T implementation) {
            services.put(serviceClass, implementation);
        }
        
        @SuppressWarnings("unchecked")
        public static <T> T resolve(Class<T> serviceClass) {
            return (T) services.get(serviceClass);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Dependency Injection Pattern Demo ===\n");
        
        // 1. Constructor Injection
        System.out.println("1. Constructor Injection:");
        MessageService emailService = new EmailService();
        NotificationService emailNotifier = new NotificationService(emailService);
        emailNotifier.notifyUser("Hello via Email", "user@example.com");
        
        MessageService smsService = new SMSService();
        NotificationService smsNotifier = new NotificationService(smsService);
        smsNotifier.notifyUser("Hello via SMS", "+1234567890");
        
        // 2. Setter Injection
        System.out.println("\n2. Setter Injection:");
        UserNotifier userNotifier = new UserNotifier();
        userNotifier.setMessageService(new PushNotificationService());
        userNotifier.notify("Hello via Push", "user123");
        
        // Change dependency at runtime
        userNotifier.setMessageService(new EmailService());
        userNotifier.notify("Changed to Email", "user@example.com");
        
        // 3. Using DI Container
        System.out.println("\n3. Using DI Container:");
        DIContainer.register(MessageService.class, new EmailService());
        
        MessageService service = DIContainer.resolve(MessageService.class);
        NotificationService containerNotifier = new NotificationService(service);
        containerNotifier.notifyUser("Hello from DI Container", "container@example.com");
        
        // Benefits demonstration
        System.out.println("\n--- Benefits of Dependency Injection ---");
        System.out.println("✓ Loose coupling between classes");
        System.out.println("✓ Easy to test with mock implementations");
        System.out.println("✓ Easy to change implementations at runtime");
        System.out.println("✓ Supports Single Responsibility Principle");
    }
}
