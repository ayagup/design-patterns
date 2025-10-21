package microservices;

import java.util.*;
import java.util.concurrent.*;

/**
 * Service Discovery Pattern
 * Enables automatic detection of service instances in a microservices architecture.
 */
public class ServiceDiscoveryPattern {
    
    // Service Instance
    static class ServiceInstance {
        private final String serviceId;
        private final String host;
        private final int port;
        private final Map<String, String> metadata;
        private long lastHeartbeat;
        private boolean healthy;
        
        public ServiceInstance(String serviceId, String host, int port) {
            this.serviceId = serviceId;
            this.host = host;
            this.port = port;
            this.metadata = new HashMap<>();
            this.lastHeartbeat = System.currentTimeMillis();
            this.healthy = true;
        }
        
        public String getServiceId() { return serviceId; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getUrl() { return "http://" + host + ":" + port; }
        public Map<String, String> getMetadata() { return metadata; }
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public long getLastHeartbeat() { return lastHeartbeat; }
        
        public void heartbeat() {
            this.lastHeartbeat = System.currentTimeMillis();
            this.healthy = true;
        }
        
        public void addMetadata(String key, String value) {
            metadata.put(key, value);
        }
        
        @Override
        public String toString() {
            return serviceId + " @ " + getUrl() + " (healthy=" + healthy + ")";
        }
    }
    
    // Service Registry (Central registry for service instances)
    static class ServiceRegistry {
        private final Map<String, List<ServiceInstance>> services = new ConcurrentHashMap<>();
        private final ScheduledExecutorService healthChecker = Executors.newScheduledThreadPool(1);
        private final long heartbeatTimeout = 10000; // 10 seconds
        
        public ServiceRegistry() {
            startHealthCheck();
        }
        
        public void register(ServiceInstance instance) {
            services.computeIfAbsent(instance.getServiceId(), k -> new CopyOnWriteArrayList<>())
                    .add(instance);
            System.out.println("✅ Registered: " + instance);
        }
        
        public void deregister(ServiceInstance instance) {
            List<ServiceInstance> instances = services.get(instance.getServiceId());
            if (instances != null) {
                instances.remove(instance);
                System.out.println("❌ Deregistered: " + instance);
            }
        }
        
        public List<ServiceInstance> discover(String serviceId) {
            List<ServiceInstance> instances = services.get(serviceId);
            if (instances == null) {
                return Collections.emptyList();
            }
            
            // Return only healthy instances
            List<ServiceInstance> healthyInstances = new ArrayList<>();
            for (ServiceInstance instance : instances) {
                if (instance.isHealthy()) {
                    healthyInstances.add(instance);
                }
            }
            return healthyInstances;
        }
        
        public ServiceInstance discoverOne(String serviceId) {
            List<ServiceInstance> instances = discover(serviceId);
            if (instances.isEmpty()) {
                return null;
            }
            // Simple round-robin (could use other strategies)
            return instances.get(new Random().nextInt(instances.size()));
        }
        
        public void heartbeat(String serviceId, String host, int port) {
            List<ServiceInstance> instances = services.get(serviceId);
            if (instances != null) {
                for (ServiceInstance instance : instances) {
                    if (instance.getHost().equals(host) && instance.getPort() == port) {
                        instance.heartbeat();
                        return;
                    }
                }
            }
        }
        
        private void startHealthCheck() {
            healthChecker.scheduleAtFixedRate(() -> {
                long now = System.currentTimeMillis();
                for (List<ServiceInstance> instances : services.values()) {
                    for (ServiceInstance instance : instances) {
                        if (now - instance.getLastHeartbeat() > heartbeatTimeout) {
                            if (instance.isHealthy()) {
                                instance.setHealthy(false);
                                System.out.println("⚠️  Marked unhealthy: " + instance);
                            }
                        }
                    }
                }
            }, 5, 5, TimeUnit.SECONDS);
        }
        
        public void printRegistry() {
            System.out.println("\n📋 Service Registry:");
            for (Map.Entry<String, List<ServiceInstance>> entry : services.entrySet()) {
                System.out.println("  " + entry.getKey() + ":");
                for (ServiceInstance instance : entry.getValue()) {
                    System.out.println("    - " + instance);
                }
            }
        }
        
        public void shutdown() {
            healthChecker.shutdown();
        }
    }
    
    // Load Balancer
    interface LoadBalancer {
        ServiceInstance selectInstance(List<ServiceInstance> instances);
    }
    
    static class RoundRobinLoadBalancer implements LoadBalancer {
        private int currentIndex = 0;
        
        @Override
        public ServiceInstance selectInstance(List<ServiceInstance> instances) {
            if (instances.isEmpty()) {
                return null;
            }
            ServiceInstance instance = instances.get(currentIndex % instances.size());
            currentIndex++;
            return instance;
        }
    }
    
    static class RandomLoadBalancer implements LoadBalancer {
        private final Random random = new Random();
        
        @Override
        public ServiceInstance selectInstance(List<ServiceInstance> instances) {
            if (instances.isEmpty()) {
                return null;
            }
            return instances.get(random.nextInt(instances.size()));
        }
    }
    
    // Service Client with Discovery
    static class ServiceClient {
        private final ServiceRegistry registry;
        private final LoadBalancer loadBalancer;
        
        public ServiceClient(ServiceRegistry registry, LoadBalancer loadBalancer) {
            this.registry = registry;
            this.loadBalancer = loadBalancer;
        }
        
        public String call(String serviceId, String endpoint) {
            List<ServiceInstance> instances = registry.discover(serviceId);
            
            if (instances.isEmpty()) {
                throw new RuntimeException("No instances available for service: " + serviceId);
            }
            
            ServiceInstance instance = loadBalancer.selectInstance(instances);
            System.out.println("  🔗 Calling " + instance.getUrl() + endpoint);
            
            // Simulate HTTP call
            return "Response from " + instance.getUrl() + endpoint;
        }
    }
    
    // Microservice that registers itself
    static class Microservice {
        private final ServiceInstance instance;
        private final ServiceRegistry registry;
        private final ScheduledExecutorService heartbeatExecutor;
        
        public Microservice(String serviceId, String host, int port, ServiceRegistry registry) {
            this.instance = new ServiceInstance(serviceId, host, port);
            this.registry = registry;
            this.heartbeatExecutor = Executors.newScheduledThreadPool(1);
        }
        
        public void start() {
            // Register with registry
            registry.register(instance);
            
            // Start sending heartbeats
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                registry.heartbeat(instance.getServiceId(), instance.getHost(), instance.getPort());
                System.out.println("  💓 Heartbeat from " + instance);
            }, 2, 3, TimeUnit.SECONDS);
        }
        
        public void stop() {
            heartbeatExecutor.shutdown();
            registry.deregister(instance);
        }
        
        public ServiceInstance getInstance() {
            return instance;
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Service Discovery Pattern Demo ===\n");
        
        // Create Service Registry
        ServiceRegistry registry = new ServiceRegistry();
        
        // Start multiple service instances
        System.out.println("1. Starting Microservices:");
        Microservice userService1 = new Microservice("user-service", "localhost", 8001, registry);
        Microservice userService2 = new Microservice("user-service", "localhost", 8002, registry);
        Microservice orderService1 = new Microservice("order-service", "localhost", 9001, registry);
        Microservice orderService2 = new Microservice("order-service", "localhost", 9002, registry);
        Microservice productService = new Microservice("product-service", "localhost", 7001, registry);
        
        userService1.start();
        userService2.start();
        orderService1.start();
        orderService2.start();
        productService.start();
        
        Thread.sleep(1000);
        
        registry.printRegistry();
        
        System.out.println("\n" + "=".repeat(50));
        
        // Create client with load balancer
        System.out.println("\n2. Service Discovery with Load Balancing:");
        ServiceClient client = new ServiceClient(registry, new RoundRobinLoadBalancer());
        
        // Make multiple calls (load balanced)
        for (int i = 0; i < 4; i++) {
            System.out.println("\nRequest " + (i + 1) + ":");
            String response = client.call("user-service", "/api/users");
            System.out.println("  ✅ " + response);
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // Test with different service
        System.out.println("\n3. Calling Order Service:");
        for (int i = 0; i < 2; i++) {
            String response = client.call("order-service", "/api/orders");
            System.out.println("  ✅ " + response);
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // Simulate service failure
        System.out.println("\n4. Simulating Service Failure:");
        System.out.println("Stopping user-service instance 1...");
        userService1.stop();
        
        Thread.sleep(12000); // Wait for health check to mark as unhealthy
        
        registry.printRegistry();
        
        System.out.println("\nMaking requests after failure:");
        for (int i = 0; i < 3; i++) {
            String response = client.call("user-service", "/api/users");
            System.out.println("  ✅ " + response);
        }
        
        System.out.println("\n" + "=".repeat(50));
        
        // Cleanup
        System.out.println("\n5. Shutting down:");
        userService2.stop();
        orderService1.stop();
        orderService2.stop();
        productService.stop();
        registry.shutdown();
        
        System.out.println("\n--- Service Discovery Components ---");
        System.out.println("📋 Service Registry:");
        System.out.println("   - Central registry of all service instances");
        System.out.println("   - Tracks health status");
        System.out.println("   - Handles registration/deregistration");
        System.out.println();
        System.out.println("💓 Health Checking:");
        System.out.println("   - Periodic heartbeats from services");
        System.out.println("   - Marks unhealthy instances");
        System.out.println("   - Automatic failover");
        System.out.println();
        System.out.println("⚖️  Load Balancing:");
        System.out.println("   - Round-robin, Random, Least connections");
        System.out.println("   - Distributes traffic across instances");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Dynamic service location");
        System.out.println("✓ Automatic failover");
        System.out.println("✓ Load balancing");
        System.out.println("✓ Scalability (add/remove instances)");
        System.out.println("✓ No hardcoded URLs");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Microservices architecture");
        System.out.println("• Cloud-native applications");
        System.out.println("• Container orchestration");
        System.out.println("• Dynamic scaling");
        
        System.out.println("\n--- Real-World Implementations ---");
        System.out.println("• Netflix Eureka");
        System.out.println("• Consul");
        System.out.println("• Apache Zookeeper");
        System.out.println("• Kubernetes Service Discovery");
        System.out.println("• etcd");
    }
}
