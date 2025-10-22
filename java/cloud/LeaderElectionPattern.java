package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Leader Election Pattern
 * 
 * Intent: Coordinate task execution in a distributed system by electing a single
 * coordinator (leader) instance that is responsible for managing work distribution.
 * 
 * Also Known As: Master Election, Coordinator Election
 * 
 * Motivation:
 * In distributed systems with multiple instances, certain tasks should only be
 * performed by one instance to avoid conflicts or duplicate work. Leader election
 * ensures that exactly one instance acts as the coordinator.
 * 
 * Applicability:
 * - Distributed task scheduling
 * - Coordinated writes to prevent conflicts
 * - Managing shared resources across instances
 * - Failover scenarios (new leader elected if current fails)
 * - Distributed locks and coordination
 * - Cluster management
 * 
 * Benefits:
 * - Prevents duplicate work in distributed systems
 * - Automatic failover when leader fails
 * - Coordinated task execution
 * - Scalability with multiple instances
 * - Fault tolerance
 * 
 * Implementation Considerations:
 * - Split-brain scenarios (two leaders)
 * - Election algorithm complexity
 * - Leader failure detection
 * - Network partitions
 * - Election performance overhead
 * - Consensus mechanisms (Paxos, Raft, Zab)
 */

// Leader election status
enum LeaderStatus {
    FOLLOWER,
    CANDIDATE,
    LEADER
}

// Election result
class ElectionResult {
    private final String leaderId;
    private final int term;
    private final LocalDateTime electionTime;
    
    public ElectionResult(String leaderId, int term) {
        this.leaderId = leaderId;
        this.term = term;
        this.electionTime = LocalDateTime.now();
    }
    
    public String getLeaderId() { return leaderId; }
    public int getTerm() { return term; }
    public LocalDateTime getElectionTime() { return electionTime; }
    
    @Override
    public String toString() {
        return String.format("Leader: %s, Term: %d, Time: %s", leaderId, term, electionTime);
    }
}

// Node in distributed system
interface DistributedNode {
    String getNodeId();
    LeaderStatus getStatus();
    void startElection();
    void shutdown();
    boolean isHealthy();
}

// Example 1: Simple Bully Algorithm Implementation
class BullyAlgorithmNode implements DistributedNode {
    private final String nodeId;
    private final int priority; // Higher priority = higher chance to be leader
    private final AtomicReference<LeaderStatus> status;
    private final AtomicReference<String> currentLeader;
    private final AtomicInteger currentTerm;
    private final List<BullyAlgorithmNode> peers;
    private final ScheduledExecutorService scheduler;
    private volatile boolean healthy;
    private volatile boolean running;
    
    public BullyAlgorithmNode(String nodeId, int priority) {
        this.nodeId = nodeId;
        this.priority = priority;
        this.status = new AtomicReference<>(LeaderStatus.FOLLOWER);
        this.currentLeader = new AtomicReference<>(null);
        this.currentTerm = new AtomicInteger(0);
        this.peers = new CopyOnWriteArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.healthy = true;
        this.running = true;
    }
    
    public void addPeer(BullyAlgorithmNode peer) {
        if (!peer.getNodeId().equals(this.nodeId)) {
            peers.add(peer);
        }
    }
    
    @Override
    public void startElection() {
        if (!healthy || !running) return;
        
        System.out.printf("[%s] Starting election (priority: %d)%n", nodeId, priority);
        status.set(LeaderStatus.CANDIDATE);
        
        // Check if any node with higher priority is alive
        boolean higherPriorityExists = false;
        for (BullyAlgorithmNode peer : peers) {
            if (peer.getPriority() > this.priority && peer.isHealthy()) {
                higherPriorityExists = true;
                System.out.printf("[%s] Found higher priority node: %s (priority: %d)%n",
                    nodeId, peer.getNodeId(), peer.getPriority());
                break;
            }
        }
        
        if (!higherPriorityExists) {
            // Become leader
            becomeLeader();
        } else {
            // Wait for higher priority node to become leader
            status.set(LeaderStatus.FOLLOWER);
        }
    }
    
    private void becomeLeader() {
        status.set(LeaderStatus.LEADER);
        int term = currentTerm.incrementAndGet();
        currentLeader.set(nodeId);
        
        System.out.printf("[%s] ✓ Elected as LEADER (term: %d)%n", nodeId, term);
        
        // Announce leadership to all peers
        for (BullyAlgorithmNode peer : peers) {
            if (peer.isHealthy()) {
                peer.notifyNewLeader(nodeId, term);
            }
        }
        
        // Start heartbeat
        startHeartbeat();
    }
    
    public void notifyNewLeader(String leaderId, int term) {
        if (term >= currentTerm.get()) {
            currentTerm.set(term);
            currentLeader.set(leaderId);
            status.set(LeaderStatus.FOLLOWER);
            System.out.printf("[%s] Acknowledged leader: %s (term: %d)%n", 
                nodeId, leaderId, term);
        }
    }
    
    private void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (status.get() == LeaderStatus.LEADER && healthy && running) {
                System.out.printf("[%s] Heartbeat (I am leader)%n", nodeId);
            }
        }, 2, 2, TimeUnit.SECONDS);
    }
    
    public void simulateFailure() {
        System.out.printf("[%s] ❌ Node failed%n", nodeId);
        healthy = false;
        if (status.get() == LeaderStatus.LEADER) {
            System.out.printf("[%s] Leader failed! Election needed.%n", nodeId);
        }
    }
    
    public void simulateRecovery() {
        System.out.printf("[%s] ✓ Node recovered%n", nodeId);
        healthy = true;
    }
    
    @Override
    public String getNodeId() { return nodeId; }
    
    @Override
    public LeaderStatus getStatus() { return status.get(); }
    
    @Override
    public boolean isHealthy() { return healthy; }
    
    public int getPriority() { return priority; }
    
    public String getCurrentLeader() { return currentLeader.get(); }
    
    @Override
    public void shutdown() {
        running = false;
        scheduler.shutdown();
    }
}

// Example 2: Lease-Based Leader Election
class LeaseBasedNode implements DistributedNode {
    private final String nodeId;
    private final AtomicReference<LeaderStatus> status;
    private final AtomicReference<String> currentLeader;
    private final ScheduledExecutorService scheduler;
    private volatile long leaseExpiryTime;
    private final long leaseDurationMs = 5000; // 5 seconds
    private volatile boolean healthy;
    
    public LeaseBasedNode(String nodeId) {
        this.nodeId = nodeId;
        this.status = new AtomicReference<>(LeaderStatus.FOLLOWER);
        this.currentLeader = new AtomicReference<>(null);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.leaseExpiryTime = 0;
        this.healthy = true;
    }
    
    @Override
    public void startElection() {
        if (!healthy) return;
        
        System.out.printf("[%s] Attempting to acquire lease%n", nodeId);
        
        // Simulate trying to acquire distributed lease
        if (tryAcquireLease()) {
            becomeLeader();
            scheduleLeaseRenewal();
        } else {
            status.set(LeaderStatus.FOLLOWER);
            System.out.printf("[%s] Could not acquire lease, remaining follower%n", nodeId);
        }
    }
    
    private boolean tryAcquireLease() {
        // Simulate lease acquisition (in real system, this would be coordinated)
        long now = System.currentTimeMillis();
        if (now > leaseExpiryTime) {
            leaseExpiryTime = now + leaseDurationMs;
            return true;
        }
        return false;
    }
    
    private void becomeLeader() {
        status.set(LeaderStatus.LEADER);
        currentLeader.set(nodeId);
        System.out.printf("[%s] ✓ Acquired lease, now LEADER%n", nodeId);
    }
    
    private void scheduleLeaseRenewal() {
        scheduler.scheduleAtFixedRate(() -> {
            if (status.get() == LeaderStatus.LEADER && healthy) {
                renewLease();
            }
        }, leaseDurationMs / 2, leaseDurationMs / 2, TimeUnit.MILLISECONDS);
    }
    
    private void renewLease() {
        if (tryAcquireLease()) {
            System.out.printf("[%s] Lease renewed%n", nodeId);
        } else {
            System.out.printf("[%s] Failed to renew lease, stepping down%n", nodeId);
            status.set(LeaderStatus.FOLLOWER);
            currentLeader.set(null);
        }
    }
    
    @Override
    public String getNodeId() { return nodeId; }
    
    @Override
    public LeaderStatus getStatus() { return status.get(); }
    
    @Override
    public boolean isHealthy() { return healthy; }
    
    @Override
    public void shutdown() {
        scheduler.shutdown();
    }
}

// Example 3: Election Coordinator (simulates distributed coordination service)
class ElectionCoordinator {
    private final Map<String, NodeRegistration> registeredNodes;
    private final AtomicReference<String> currentLeader;
    private final AtomicInteger currentTerm;
    private final ScheduledExecutorService scheduler;
    
    public ElectionCoordinator() {
        this.registeredNodes = new ConcurrentHashMap<>();
        this.currentLeader = new AtomicReference<>(null);
        this.currentTerm = new AtomicInteger(0);
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    public void registerNode(String nodeId, int priority) {
        registeredNodes.put(nodeId, new NodeRegistration(nodeId, priority));
        System.out.printf("[Coordinator] Registered node: %s (priority: %d)%n", nodeId, priority);
    }
    
    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkLeaderHealth, 1, 2, TimeUnit.SECONDS);
    }
    
    private void checkLeaderHealth() {
        String leader = currentLeader.get();
        
        if (leader == null || !isNodeHealthy(leader)) {
            System.out.println("[Coordinator] No healthy leader detected, triggering election");
            electNewLeader();
        }
    }
    
    private void electNewLeader() {
        // Find node with highest priority that is healthy
        Optional<NodeRegistration> newLeader = registeredNodes.values().stream()
            .filter(node -> node.isHealthy())
            .max(Comparator.comparingInt(NodeRegistration::getPriority));
        
        if (newLeader.isPresent()) {
            String leaderId = newLeader.get().getNodeId();
            int term = currentTerm.incrementAndGet();
            currentLeader.set(leaderId);
            
            System.out.printf("[Coordinator] ✓ Elected new leader: %s (term: %d)%n", 
                leaderId, term);
            
            // Notify all nodes
            registeredNodes.values().forEach(node -> 
                System.out.printf("[Coordinator] Notifying %s about new leader%n", node.getNodeId()));
        } else {
            System.out.println("[Coordinator] ❌ No healthy nodes available for election");
        }
    }
    
    public void markNodeUnhealthy(String nodeId) {
        NodeRegistration node = registeredNodes.get(nodeId);
        if (node != null) {
            node.setHealthy(false);
            System.out.printf("[Coordinator] Node %s marked unhealthy%n", nodeId);
            
            if (nodeId.equals(currentLeader.get())) {
                System.out.println("[Coordinator] Current leader is unhealthy!");
                currentLeader.set(null);
            }
        }
    }
    
    public void markNodeHealthy(String nodeId) {
        NodeRegistration node = registeredNodes.get(nodeId);
        if (node != null) {
            node.setHealthy(true);
            System.out.printf("[Coordinator] Node %s marked healthy%n", nodeId);
        }
    }
    
    private boolean isNodeHealthy(String nodeId) {
        NodeRegistration node = registeredNodes.get(nodeId);
        return node != null && node.isHealthy();
    }
    
    public String getCurrentLeader() {
        return currentLeader.get();
    }
    
    public int getCurrentTerm() {
        return currentTerm.get();
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
    
    public void printStatus() {
        System.out.println("\n=== Election Status ===");
        System.out.println("Current Leader: " + (currentLeader.get() != null ? currentLeader.get() : "None"));
        System.out.println("Current Term: " + currentTerm.get());
        System.out.println("\nRegistered Nodes:");
        registeredNodes.values().forEach(node -> 
            System.out.printf("  - %s (priority: %d, healthy: %s)%n",
                node.getNodeId(), node.getPriority(), node.isHealthy()));
    }
    
    private static class NodeRegistration {
        private final String nodeId;
        private final int priority;
        private volatile boolean healthy;
        
        public NodeRegistration(String nodeId, int priority) {
            this.nodeId = nodeId;
            this.priority = priority;
            this.healthy = true;
        }
        
        public String getNodeId() { return nodeId; }
        public int getPriority() { return priority; }
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
    }
}

// Example 4: Distributed Task Scheduler with Leader Election
class DistributedTaskScheduler {
    private final String nodeId;
    private final BullyAlgorithmNode electionNode;
    private final ScheduledExecutorService taskScheduler;
    private final AtomicInteger tasksExecuted;
    
    public DistributedTaskScheduler(String nodeId, int priority) {
        this.nodeId = nodeId;
        this.electionNode = new BullyAlgorithmNode(nodeId, priority);
        this.taskScheduler = Executors.newScheduledThreadPool(1);
        this.tasksExecuted = new AtomicInteger(0);
    }
    
    public void connectToPeer(DistributedTaskScheduler peer) {
        this.electionNode.addPeer(peer.electionNode);
    }
    
    public void start() {
        System.out.printf("[%s] Task scheduler starting%n", nodeId);
        
        // Start election
        electionNode.startElection();
        
        // Schedule periodic task (only leader executes)
        taskScheduler.scheduleAtFixedRate(() -> {
            if (electionNode.getStatus() == LeaderStatus.LEADER) {
                executeTask();
            }
        }, 2, 3, TimeUnit.SECONDS);
    }
    
    private void executeTask() {
        int taskNum = tasksExecuted.incrementAndGet();
        System.out.printf("[%s] LEADER executing task #%d%n", nodeId, taskNum);
    }
    
    public void simulateFailure() {
        electionNode.simulateFailure();
    }
    
    public void simulateRecovery() {
        electionNode.simulateRecovery();
        electionNode.startElection();
    }
    
    public LeaderStatus getStatus() {
        return electionNode.getStatus();
    }
    
    public void shutdown() {
        taskScheduler.shutdown();
        electionNode.shutdown();
    }
}

// Demonstration
public class LeaderElectionPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Leader Election Pattern Demo ===\n");
        
        // Demo 1: Bully algorithm
        System.out.println("--- Demo 1: Bully Algorithm ---");
        demoBullyAlgorithm();
        
        Thread.sleep(2000);
        
        // Demo 2: Lease-based election
        System.out.println("\n--- Demo 2: Lease-Based Election ---");
        demoLeaseBasedElection();
        
        Thread.sleep(2000);
        
        // Demo 3: Centralized coordinator
        System.out.println("\n--- Demo 3: Centralized Election Coordinator ---");
        demoCentralizedCoordinator();
        
        Thread.sleep(2000);
        
        // Demo 4: Distributed task scheduler with leader election
        System.out.println("\n--- Demo 4: Distributed Task Scheduler ---");
        demoDistributedTaskScheduler();
    }
    
    private static void demoBullyAlgorithm() throws InterruptedException {
        BullyAlgorithmNode node1 = new BullyAlgorithmNode("Node1", 1);
        BullyAlgorithmNode node2 = new BullyAlgorithmNode("Node2", 2);
        BullyAlgorithmNode node3 = new BullyAlgorithmNode("Node3", 3);
        
        // Connect nodes as peers
        node1.addPeer(node2);
        node1.addPeer(node3);
        node2.addPeer(node1);
        node2.addPeer(node3);
        node3.addPeer(node1);
        node3.addPeer(node2);
        
        // Start elections
        node1.startElection();
        node2.startElection();
        node3.startElection();
        
        Thread.sleep(1000);
        System.out.println("\nInitial state:");
        System.out.println("Node1 status: " + node1.getStatus() + ", Leader: " + node1.getCurrentLeader());
        System.out.println("Node2 status: " + node2.getStatus() + ", Leader: " + node2.getCurrentLeader());
        System.out.println("Node3 status: " + node3.getStatus() + ", Leader: " + node3.getCurrentLeader());
        
        // Simulate leader failure
        Thread.sleep(2000);
        System.out.println("\n[SIMULATION] Leader (Node3) fails");
        node3.simulateFailure();
        
        // Trigger new election
        Thread.sleep(1000);
        node1.startElection();
        node2.startElection();
        
        Thread.sleep(1000);
        System.out.println("\nAfter failover:");
        System.out.println("Node1 status: " + node1.getStatus());
        System.out.println("Node2 status: " + node2.getStatus());
        
        node1.shutdown();
        node2.shutdown();
        node3.shutdown();
    }
    
    private static void demoLeaseBasedElection() throws InterruptedException {
        LeaseBasedNode node1 = new LeaseBasedNode("LeaseNode1");
        LeaseBasedNode node2 = new LeaseBasedNode("LeaseNode2");
        
        node1.startElection();
        Thread.sleep(500);
        
        node2.startElection(); // Should fail since node1 has lease
        Thread.sleep(1000);
        
        System.out.println("\nNode1 status: " + node1.getStatus());
        System.out.println("Node2 status: " + node2.getStatus());
        
        node1.shutdown();
        node2.shutdown();
    }
    
    private static void demoCentralizedCoordinator() throws InterruptedException {
        ElectionCoordinator coordinator = new ElectionCoordinator();
        
        coordinator.registerNode("ServiceA", 10);
        coordinator.registerNode("ServiceB", 20);
        coordinator.registerNode("ServiceC", 30);
        
        coordinator.startMonitoring();
        coordinator.printStatus();
        
        Thread.sleep(3000);
        
        // Simulate leader failure
        System.out.println("\n[SIMULATION] ServiceC (leader) fails");
        coordinator.markNodeUnhealthy("ServiceC");
        
        Thread.sleep(3000);
        coordinator.printStatus();
        
        // Recover failed node
        System.out.println("\n[SIMULATION] ServiceC recovers");
        coordinator.markNodeHealthy("ServiceC");
        
        Thread.sleep(3000);
        coordinator.printStatus();
        
        coordinator.shutdown();
    }
    
    private static void demoDistributedTaskScheduler() throws InterruptedException {
        DistributedTaskScheduler scheduler1 = new DistributedTaskScheduler("Scheduler1", 1);
        DistributedTaskScheduler scheduler2 = new DistributedTaskScheduler("Scheduler2", 2);
        DistributedTaskScheduler scheduler3 = new DistributedTaskScheduler("Scheduler3", 3);
        
        scheduler1.connectToPeer(scheduler2);
        scheduler1.connectToPeer(scheduler3);
        scheduler2.connectToPeer(scheduler1);
        scheduler2.connectToPeer(scheduler3);
        scheduler3.connectToPeer(scheduler1);
        scheduler3.connectToPeer(scheduler2);
        
        scheduler1.start();
        scheduler2.start();
        scheduler3.start();
        
        // Let it run for a while
        Thread.sleep(8000);
        
        // Simulate leader failure
        System.out.println("\n[SIMULATION] Leader fails");
        scheduler3.simulateFailure();
        
        Thread.sleep(2000);
        scheduler1.start(); // Trigger new election
        scheduler2.start();
        
        Thread.sleep(8000);
        
        scheduler1.shutdown();
        scheduler2.shutdown();
        scheduler3.shutdown();
    }
}
