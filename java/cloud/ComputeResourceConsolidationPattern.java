package cloud;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compute Resource Consolidation Pattern
 * 
 * Intent: Consolidate multiple tasks or services onto shared compute resources
 * to improve resource utilization, reduce costs, and simplify management.
 * 
 * Also Known As:
 * - Server Consolidation
 * - Multi-tenancy
 * - Resource Pooling
 * 
 * Motivation:
 * Running each task or service on dedicated infrastructure can lead to:
 * - Low resource utilization (servers often idle)
 * - High costs (paying for unused capacity)
 * - Management overhead (many servers to maintain)
 * - Inefficient scaling (each service scales independently)
 * 
 * Applicability:
 * - Multiple low-utilization services
 * - Variable workload patterns that complement each other
 * - Cost optimization requirements
 * - Containerized or virtualized workloads
 * - Cloud environments with pay-per-use pricing
 * 
 * Benefits:
 * - Reduced infrastructure costs
 * - Improved resource utilization
 * - Simplified management
 * - Better scalability
 * - Energy efficiency
 * 
 * Trade-offs:
 * - Resource contention between tasks
 * - Noisy neighbor problems
 * - Increased complexity in isolation
 * - Single point of failure
 */

// Service metadata and resource requirements
class ServiceDefinition {
    private final String name;
    private final int cpuUnits;      // CPU units required
    private final int memoryMB;      // Memory in MB
    private final int instanceCount;  // Number of instances
    
    public ServiceDefinition(String name, int cpuUnits, int memoryMB, int instanceCount) {
        this.name = name;
        this.cpuUnits = cpuUnits;
        this.memoryMB = memoryMB;
        this.instanceCount = instanceCount;
    }
    
    public String getName() { return name; }
    public int getCpuUnits() { return cpuUnits; }
    public int getMemoryMB() { return memoryMB; }
    public int getInstanceCount() { return instanceCount; }
    
    @Override
    public String toString() {
        return String.format("%s (CPU: %d, Memory: %dMB, Instances: %d)", 
            name, cpuUnits, memoryMB, instanceCount);
    }
}

// Compute resource (server/VM/container host)
class ComputeResource {
    private final String id;
    private final int totalCpuUnits;
    private final int totalMemoryMB;
    private int availableCpuUnits;
    private int availableMemoryMB;
    private final List<ServiceInstance> runningServices;
    
    public ComputeResource(String id, int cpuUnits, int memoryMB) {
        this.id = id;
        this.totalCpuUnits = cpuUnits;
        this.totalMemoryMB = memoryMB;
        this.availableCpuUnits = cpuUnits;
        this.availableMemoryMB = memoryMB;
        this.runningServices = new ArrayList<>();
    }
    
    public boolean canAccommodate(int cpuUnits, int memoryMB) {
        return availableCpuUnits >= cpuUnits && availableMemoryMB >= memoryMB;
    }
    
    public void allocate(ServiceInstance instance) {
        availableCpuUnits -= instance.getCpuUnits();
        availableMemoryMB -= instance.getMemoryMB();
        runningServices.add(instance);
    }
    
    public void deallocate(ServiceInstance instance) {
        availableCpuUnits += instance.getCpuUnits();
        availableMemoryMB += instance.getMemoryMB();
        runningServices.remove(instance);
    }
    
    public String getId() { return id; }
    public int getAvailableCpuUnits() { return availableCpuUnits; }
    public int getAvailableMemoryMB() { return availableMemoryMB; }
    public List<ServiceInstance> getRunningServices() { return new ArrayList<>(runningServices); }
    
    public double getCpuUtilization() {
        return ((double)(totalCpuUnits - availableCpuUnits) / totalCpuUnits) * 100;
    }
    
    public double getMemoryUtilization() {
        return ((double)(totalMemoryMB - availableMemoryMB) / totalMemoryMB) * 100;
    }
    
    @Override
    public String toString() {
        return String.format("%s [CPU: %.1f%%, Memory: %.1f%%, Services: %d]",
            id, getCpuUtilization(), getMemoryUtilization(), runningServices.size());
    }
}

// Running service instance
class ServiceInstance {
    private final String serviceId;
    private final String instanceId;
    private final int cpuUnits;
    private final int memoryMB;
    private final ComputeResource host;
    
    public ServiceInstance(String serviceId, String instanceId, int cpuUnits, int memoryMB, ComputeResource host) {
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.cpuUnits = cpuUnits;
        this.memoryMB = memoryMB;
        this.host = host;
    }
    
    public String getServiceId() { return serviceId; }
    public String getInstanceId() { return instanceId; }
    public int getCpuUnits() { return cpuUnits; }
    public int getMemoryMB() { return memoryMB; }
    public ComputeResource getHost() { return host; }
    
    @Override
    public String toString() {
        return String.format("%s/%s on %s", serviceId, instanceId, host.getId());
    }
}

// Example 1: Basic Resource Consolidation
// First-fit allocation strategy
class BasicConsolidationManager {
    private final List<ComputeResource> resources;
    private final List<ServiceInstance> instances;
    
    public BasicConsolidationManager() {
        this.resources = new ArrayList<>();
        this.instances = new ArrayList<>();
    }
    
    public void addResource(ComputeResource resource) {
        resources.add(resource);
        System.out.println("Added resource: " + resource);
    }
    
    public boolean deployService(ServiceDefinition service) {
        System.out.println("\nDeploying " + service);
        
        for (int i = 0; i < service.getInstanceCount(); i++) {
            String instanceId = "instance-" + UUID.randomUUID().toString().substring(0, 8);
            
            // First-fit: find first resource that can accommodate
            ComputeResource selectedResource = null;
            for (ComputeResource resource : resources) {
                if (resource.canAccommodate(service.getCpuUnits(), service.getMemoryMB())) {
                    selectedResource = resource;
                    break;
                }
            }
            
            if (selectedResource == null) {
                System.out.println("  ❌ Failed to deploy instance " + (i + 1) + ": No available resources");
                return false;
            }
            
            ServiceInstance instance = new ServiceInstance(
                service.getName(), instanceId, 
                service.getCpuUnits(), service.getMemoryMB(), 
                selectedResource
            );
            
            selectedResource.allocate(instance);
            instances.add(instance);
            System.out.println("  ✓ Deployed " + instance);
        }
        
        return true;
    }
    
    public void printResourceUtilization() {
        System.out.println("\nResource Utilization:");
        for (ComputeResource resource : resources) {
            System.out.println("  " + resource);
        }
    }
}

// Example 2: Best-Fit Consolidation
// Minimizes wasted resources by selecting resource with least remaining capacity
class BestFitConsolidationManager {
    private final List<ComputeResource> resources;
    
    public BestFitConsolidationManager() {
        this.resources = new ArrayList<>();
    }
    
    public void addResource(ComputeResource resource) {
        resources.add(resource);
    }
    
    public boolean deployService(ServiceDefinition service) {
        System.out.println("\nDeploying " + service + " (Best-Fit)");
        
        for (int i = 0; i < service.getInstanceCount(); i++) {
            String instanceId = "instance-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Best-fit: find resource with minimum remaining capacity that can still accommodate
            ComputeResource selectedResource = null;
            int minRemainingCapacity = Integer.MAX_VALUE;
            
            for (ComputeResource resource : resources) {
                if (resource.canAccommodate(service.getCpuUnits(), service.getMemoryMB())) {
                    int remainingCapacity = resource.getAvailableCpuUnits() + resource.getAvailableMemoryMB();
                    if (remainingCapacity < minRemainingCapacity) {
                        minRemainingCapacity = remainingCapacity;
                        selectedResource = resource;
                    }
                }
            }
            
            if (selectedResource == null) {
                System.out.println("  ❌ Failed to deploy instance " + (i + 1));
                return false;
            }
            
            ServiceInstance instance = new ServiceInstance(
                service.getName(), instanceId,
                service.getCpuUnits(), service.getMemoryMB(),
                selectedResource
            );
            
            selectedResource.allocate(instance);
            System.out.println("  ✓ Deployed " + instance);
        }
        
        return true;
    }
    
    public void printUtilization() {
        System.out.println("\nBest-Fit Resource Utilization:");
        for (ComputeResource resource : resources) {
            System.out.println("  " + resource);
        }
    }
}

// Example 3: Load-Balanced Consolidation
// Distributes load evenly across resources
class LoadBalancedConsolidationManager {
    private final List<ComputeResource> resources;
    
    public LoadBalancedConsolidationManager() {
        this.resources = new ArrayList<>();
    }
    
    public void addResource(ComputeResource resource) {
        resources.add(resource);
    }
    
    public boolean deployService(ServiceDefinition service) {
        System.out.println("\nDeploying " + service + " (Load-Balanced)");
        
        for (int i = 0; i < service.getInstanceCount(); i++) {
            String instanceId = "instance-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Load-balanced: select resource with lowest current utilization
            ComputeResource selectedResource = null;
            double minUtilization = Double.MAX_VALUE;
            
            for (ComputeResource resource : resources) {
                if (resource.canAccommodate(service.getCpuUnits(), service.getMemoryMB())) {
                    double utilization = (resource.getCpuUtilization() + resource.getMemoryUtilization()) / 2;
                    if (utilization < minUtilization) {
                        minUtilization = utilization;
                        selectedResource = resource;
                    }
                }
            }
            
            if (selectedResource == null) {
                System.out.println("  ❌ Failed to deploy instance " + (i + 1));
                return false;
            }
            
            ServiceInstance instance = new ServiceInstance(
                service.getName(), instanceId,
                service.getCpuUnits(), service.getMemoryMB(),
                selectedResource
            );
            
            selectedResource.allocate(instance);
            System.out.println("  ✓ Deployed " + instance + 
                String.format(" (Resource utilization: %.1f%%)", minUtilization));
        }
        
        return true;
    }
    
    public void printUtilization() {
        System.out.println("\nLoad-Balanced Resource Utilization:");
        for (ComputeResource resource : resources) {
            System.out.println("  " + resource);
        }
        
        double avgUtilization = resources.stream()
            .mapToDouble(r -> (r.getCpuUtilization() + r.getMemoryUtilization()) / 2)
            .average()
            .orElse(0);
        System.out.println(String.format("  Average Utilization: %.1f%%", avgUtilization));
    }
}

// Example 4: Auto-Scaling Consolidation
// Automatically adds/removes resources based on demand
class AutoScalingConsolidationManager {
    private final List<ComputeResource> resources;
    private final int maxResources;
    private int resourceCounter = 0;
    
    public AutoScalingConsolidationManager(int maxResources) {
        this.resources = new ArrayList<>();
        this.maxResources = maxResources;
    }
    
    public void addInitialResource(ComputeResource resource) {
        resources.add(resource);
        resourceCounter++;
    }
    
    public boolean deployService(ServiceDefinition service) {
        System.out.println("\nDeploying " + service + " (Auto-Scaling)");
        
        for (int i = 0; i < service.getInstanceCount(); i++) {
            String instanceId = "instance-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Try to find existing resource
            ComputeResource selectedResource = findAvailableResource(service.getCpuUnits(), service.getMemoryMB());
            
            // Scale up if needed
            if (selectedResource == null) {
                if (resources.size() < maxResources) {
                    selectedResource = scaleUp();
                    System.out.println("  📈 Scaled up: Added " + selectedResource.getId());
                } else {
                    System.out.println("  ❌ Failed to deploy: Max resources reached");
                    return false;
                }
            }
            
            ServiceInstance instance = new ServiceInstance(
                service.getName(), instanceId,
                service.getCpuUnits(), service.getMemoryMB(),
                selectedResource
            );
            
            selectedResource.allocate(instance);
            System.out.println("  ✓ Deployed " + instance);
        }
        
        return true;
    }
    
    public void removeService(String serviceName) {
        System.out.println("\nRemoving service: " + serviceName);
        
        List<ServiceInstance> toRemove = new ArrayList<>();
        for (ComputeResource resource : resources) {
            for (ServiceInstance instance : resource.getRunningServices()) {
                if (instance.getServiceId().equals(serviceName)) {
                    toRemove.add(instance);
                }
            }
        }
        
        for (ServiceInstance instance : toRemove) {
            instance.getHost().deallocate(instance);
            System.out.println("  ✓ Removed " + instance);
        }
        
        // Scale down if possible
        scaleDown();
    }
    
    private ComputeResource findAvailableResource(int cpuUnits, int memoryMB) {
        for (ComputeResource resource : resources) {
            if (resource.canAccommodate(cpuUnits, memoryMB)) {
                return resource;
            }
        }
        return null;
    }
    
    private ComputeResource scaleUp() {
        String id = "resource-" + (++resourceCounter);
        ComputeResource newResource = new ComputeResource(id, 100, 2048);
        resources.add(newResource);
        return newResource;
    }
    
    private void scaleDown() {
        // Remove empty resources (except keep at least one)
        List<ComputeResource> emptyResources = resources.stream()
            .filter(r -> r.getRunningServices().isEmpty())
            .collect(Collectors.toList());
        
        if (emptyResources.size() > 0 && resources.size() > 1) {
            ComputeResource toRemove = emptyResources.get(0);
            resources.remove(toRemove);
            System.out.println("  📉 Scaled down: Removed " + toRemove.getId());
        }
    }
    
    public void printUtilization() {
        System.out.println("\nAuto-Scaling Resource Utilization:");
        System.out.println("  Total Resources: " + resources.size() + "/" + maxResources);
        for (ComputeResource resource : resources) {
            System.out.println("  " + resource);
        }
    }
}

// Example 5: Cost-Optimized Consolidation
// Minimizes cost by preferring cheaper resource types
class CostOptimizedConsolidationManager {
    private final List<ComputeResourceWithCost> resources;
    
    static class ComputeResourceWithCost {
        private final ComputeResource resource;
        private final double costPerHour;
        
        public ComputeResourceWithCost(ComputeResource resource, double costPerHour) {
            this.resource = resource;
            this.costPerHour = costPerHour;
        }
        
        public ComputeResource getResource() { return resource; }
        public double getCostPerHour() { return costPerHour; }
        
        public double getEfficiency() {
            // Cost per utilized CPU unit - calculate from available vs total
            double utilizationPercent = resource.getCpuUtilization();
            return utilizationPercent > 0 ? costPerHour / utilizationPercent : Double.MAX_VALUE;
        }
    }
    
    public CostOptimizedConsolidationManager() {
        this.resources = new ArrayList<>();
    }
    
    public void addResource(ComputeResource resource, double costPerHour) {
        resources.add(new ComputeResourceWithCost(resource, costPerHour));
        System.out.println(String.format("Added %s ($%.2f/hour)", resource.getId(), costPerHour));
    }
    
    public boolean deployService(ServiceDefinition service) {
        System.out.println("\nDeploying " + service + " (Cost-Optimized)");
        
        for (int i = 0; i < service.getInstanceCount(); i++) {
            String instanceId = "instance-" + UUID.randomUUID().toString().substring(0, 8);
            
            // Cost-optimized: prefer cheaper resources first
            ComputeResourceWithCost selectedResourceWithCost = null;
            
            for (ComputeResourceWithCost rwc : resources) {
                if (rwc.getResource().canAccommodate(service.getCpuUnits(), service.getMemoryMB())) {
                    if (selectedResourceWithCost == null || 
                        rwc.getCostPerHour() < selectedResourceWithCost.getCostPerHour()) {
                        selectedResourceWithCost = rwc;
                    }
                }
            }
            
            if (selectedResourceWithCost == null) {
                System.out.println("  ❌ Failed to deploy instance " + (i + 1));
                return false;
            }
            
            ComputeResource selectedResource = selectedResourceWithCost.getResource();
            ServiceInstance instance = new ServiceInstance(
                service.getName(), instanceId,
                service.getCpuUnits(), service.getMemoryMB(),
                selectedResource
            );
            
            selectedResource.allocate(instance);
            System.out.println(String.format("  ✓ Deployed %s ($%.2f/hour)", 
                instance, selectedResourceWithCost.getCostPerHour()));
        }
        
        return true;
    }
    
    public void printCostAnalysis() {
        System.out.println("\nCost Analysis:");
        double totalCost = 0;
        
        for (ComputeResourceWithCost rwc : resources) {
            ComputeResource resource = rwc.getResource();
            System.out.println(String.format("  %s: $%.2f/hour (efficiency: $%.3f per CPU unit)",
                resource.getId(), rwc.getCostPerHour(), rwc.getEfficiency()));
            totalCost += rwc.getCostPerHour();
        }
        
        System.out.println(String.format("\nTotal Cost: $%.2f/hour ($%.2f/month)", 
            totalCost, totalCost * 730));
    }
}

// Demo
public class ComputeResourceConsolidationPattern {
    public static void main(String[] args) {
        demonstrateBasicConsolidation();
        demonstrateBestFitConsolidation();
        demonstrateLoadBalancedConsolidation();
        demonstrateAutoScaling();
        demonstrateCostOptimization();
    }
    
    private static void demonstrateBasicConsolidation() {
        System.out.println("=== Basic Consolidation (First-Fit) ===");
        
        BasicConsolidationManager manager = new BasicConsolidationManager();
        
        // Add 2 compute resources
        manager.addResource(new ComputeResource("server-1", 100, 2048));
        manager.addResource(new ComputeResource("server-2", 100, 2048));
        
        // Deploy multiple services
        manager.deployService(new ServiceDefinition("web-api", 20, 512, 3));
        manager.deployService(new ServiceDefinition("auth-service", 10, 256, 2));
        manager.deployService(new ServiceDefinition("background-worker", 15, 512, 2));
        
        manager.printResourceUtilization();
    }
    
    private static void demonstrateBestFitConsolidation() {
        System.out.println("\n\n=== Best-Fit Consolidation ===");
        
        BestFitConsolidationManager manager = new BestFitConsolidationManager();
        
        manager.addResource(new ComputeResource("server-1", 100, 2048));
        manager.addResource(new ComputeResource("server-2", 100, 2048));
        
        manager.deployService(new ServiceDefinition("web-api", 20, 512, 3));
        manager.deployService(new ServiceDefinition("auth-service", 10, 256, 2));
        
        manager.printUtilization();
    }
    
    private static void demonstrateLoadBalancedConsolidation() {
        System.out.println("\n\n=== Load-Balanced Consolidation ===");
        
        LoadBalancedConsolidationManager manager = new LoadBalancedConsolidationManager();
        
        manager.addResource(new ComputeResource("server-1", 100, 2048));
        manager.addResource(new ComputeResource("server-2", 100, 2048));
        manager.addResource(new ComputeResource("server-3", 100, 2048));
        
        manager.deployService(new ServiceDefinition("web-api", 20, 512, 6));
        
        manager.printUtilization();
    }
    
    private static void demonstrateAutoScaling() {
        System.out.println("\n\n=== Auto-Scaling Consolidation ===");
        
        AutoScalingConsolidationManager manager = new AutoScalingConsolidationManager(5);
        manager.addInitialResource(new ComputeResource("resource-1", 100, 2048));
        
        // Deploy services - should trigger scale-up
        manager.deployService(new ServiceDefinition("web-api", 40, 1024, 3));
        manager.printUtilization();
        
        // Deploy more - another scale-up
        manager.deployService(new ServiceDefinition("background-job", 30, 512, 2));
        manager.printUtilization();
        
        // Remove service - should trigger scale-down
        manager.removeService("background-job");
        manager.printUtilization();
    }
    
    private static void demonstrateCostOptimization() {
        System.out.println("\n\n=== Cost-Optimized Consolidation ===");
        
        CostOptimizedConsolidationManager manager = new CostOptimizedConsolidationManager();
        
        // Different resource types with different costs
        manager.addResource(new ComputeResource("t3.medium", 50, 1024), 0.0416);   // Cheap, small
        manager.addResource(new ComputeResource("t3.large", 100, 2048), 0.0832);   // Medium
        manager.addResource(new ComputeResource("c5.xlarge", 200, 4096), 0.17);    // Expensive, powerful
        
        // Deploy services - should prefer cheaper resources
        manager.deployService(new ServiceDefinition("web-api", 20, 512, 3));
        manager.deployService(new ServiceDefinition("auth-service", 15, 256, 2));
        
        manager.printCostAnalysis();
    }
}
