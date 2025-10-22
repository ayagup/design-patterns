package concurrency;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

/**
 * BARRIER PATTERN
 * 
 * Enables multiple threads to wait for each other to reach a common barrier point
 * before continuing execution. All threads must arrive at the barrier before any
 * can proceed past it.
 * 
 * Benefits:
 * - Synchronizes multiple threads at specific points
 * - Coordinates parallel computations
 * - Enables phased parallel algorithms
 * - Simplifies complex thread coordination
 * - Built-in support via CyclicBarrier
 * 
 * Use Cases:
 * - Parallel algorithms with phases
 * - Distributed simulations
 * - Parallel testing frameworks
 * - Map-Reduce style operations
 * - Multi-stage data processing
 */

// Example 1: Matrix Computation with Barriers
class MatrixProcessor {
    private final int[][] matrix;
    private final int rows;
    private final int cols;
    private final CyclicBarrier barrier;
    
    public MatrixProcessor(int rows, int cols, int numThreads) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = new int[rows][cols];
        
        // Initialize matrix with random values
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(10);
            }
        }
        
        // Barrier action executed when all threads reach barrier
        this.barrier = new CyclicBarrier(numThreads, () -> {
            System.out.println("  🚧 All threads reached barrier - proceeding to next phase");
        });
    }
    
    public void processInParallel(int numThreads) throws InterruptedException {
        System.out.println("\n[MATRIX PROCESSOR] Processing " + rows + "x" + cols + 
                         " matrix with " + numThreads + " threads");
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int rowsPerThread = rows / numThreads;
        
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            final int startRow = t * rowsPerThread;
            final int endRow = (t == numThreads - 1) ? rows : (t + 1) * rowsPerThread;
            
            executor.submit(() -> {
                try {
                    // Phase 1: Square each element
                    System.out.println("  [Thread " + threadId + "] Phase 1: Squaring rows " + 
                                     startRow + "-" + (endRow - 1));
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < cols; j++) {
                            matrix[i][j] = matrix[i][j] * matrix[i][j];
                        }
                    }
                    
                    // Wait for all threads to complete phase 1
                    barrier.await();
                    
                    // Phase 2: Add row number
                    System.out.println("  [Thread " + threadId + "] Phase 2: Adding offset to rows " + 
                                     startRow + "-" + (endRow - 1));
                    for (int i = startRow; i < endRow; i++) {
                        for (int j = 0; j < cols; j++) {
                            matrix[i][j] += i;
                        }
                    }
                    
                    // Wait for all threads to complete phase 2
                    barrier.await();
                    
                    System.out.println("  [Thread " + threadId + "] ✅ Completed all phases");
                    
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("  [Thread " + threadId + "] ❌ Interrupted");
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
    
    public void printMatrix() {
        System.out.println("\n  Matrix result:");
        for (int i = 0; i < Math.min(3, rows); i++) {
            System.out.print("    ");
            for (int j = 0; j < Math.min(5, cols); j++) {
                System.out.printf("%4d ", matrix[i][j]);
            }
            System.out.println("...");
        }
        System.out.println("    ...");
    }
}

// Example 2: Parallel Sort with Barriers
class ParallelSorter {
    private final int[] array;
    private final CyclicBarrier barrier;
    
    public ParallelSorter(int size, int numThreads) {
        this.array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(1000);
        }
        
        this.barrier = new CyclicBarrier(numThreads, () -> {
            System.out.println("  🚧 Barrier: All segments sorted - ready for merge");
        });
    }
    
    public void parallelSort(int numThreads) throws InterruptedException {
        System.out.println("\n[PARALLEL SORTER] Sorting " + array.length + 
                         " elements with " + numThreads + " threads");
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int segmentSize = array.length / numThreads;
        
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            final int start = t * segmentSize;
            final int end = (t == numThreads - 1) ? array.length : (t + 1) * segmentSize;
            
            executor.submit(() -> {
                try {
                    // Sort local segment
                    System.out.println("  [Thread " + threadId + "] Sorting segment [" + 
                                     start + "-" + (end - 1) + "]");
                    Arrays.sort(array, start, end);
                    
                    // Wait for all threads to finish sorting
                    barrier.await();
                    
                    System.out.println("  [Thread " + threadId + "] ✅ Segment sorted");
                    
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("  ✅ All segments sorted (merge would happen here)");
    }
}

// Example 3: Simulation with Synchronized Steps
class Simulation {
    private final int numAgents;
    private final Agent[] agents;
    private final CyclicBarrier barrier;
    private volatile boolean running = true;
    
    public Simulation(int numAgents) {
        this.numAgents = numAgents;
        this.agents = new Agent[numAgents];
        
        // Barrier for synchronizing simulation steps
        this.barrier = new CyclicBarrier(numAgents, () -> {
            // This runs after all agents complete their step
            System.out.println("  🚧 Step completed - all agents synchronized");
        });
        
        // Initialize agents
        for (int i = 0; i < numAgents; i++) {
            agents[i] = new Agent(i, barrier);
        }
    }
    
    public void runSimulation(int numSteps) throws InterruptedException {
        System.out.println("\n[SIMULATION] Running " + numSteps + " steps with " + 
                         numAgents + " agents");
        
        ExecutorService executor = Executors.newFixedThreadPool(numAgents);
        
        // Start all agents
        for (Agent agent : agents) {
            executor.submit(() -> {
                for (int step = 0; step < numSteps && running; step++) {
                    agent.executeStep(step);
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        System.out.println("  ✅ Simulation completed");
    }
    
    static class Agent {
        private final int id;
        private final CyclicBarrier barrier;
        private int position = 0;
        
        public Agent(int id, CyclicBarrier barrier) {
            this.id = id;
            this.barrier = barrier;
        }
        
        public void executeStep(int stepNumber) {
            try {
                // Compute next state
                position += (stepNumber % 2 == 0) ? 1 : -1;
                
                if (stepNumber < 3) { // Only print first few steps
                    System.out.println("  [Agent " + id + "] Step " + stepNumber + 
                                     ": position=" + position);
                }
                
                // Wait for all agents to complete this step
                barrier.await();
                
            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

// Example 4: Parallel Testing Framework
class ParallelTestRunner {
    private final List<TestCase> testCases;
    private final CyclicBarrier setupBarrier;
    private final CyclicBarrier teardownBarrier;
    private final int numThreads;
    
    public ParallelTestRunner(int numThreads) {
        this.numThreads = numThreads;
        this.testCases = new ArrayList<>();
        
        // Barrier after setup phase
        this.setupBarrier = new CyclicBarrier(numThreads, () -> {
            System.out.println("  🚧 All threads completed setup");
        });
        
        // Barrier before teardown phase
        this.teardownBarrier = new CyclicBarrier(numThreads, () -> {
            System.out.println("  🚧 All threads completed execution - starting teardown");
        });
    }
    
    public void addTest(String name) {
        testCases.add(new TestCase(name));
    }
    
    public void runTests() throws InterruptedException {
        System.out.println("\n[TEST RUNNER] Running " + testCases.size() + 
                         " tests with " + numThreads + " threads");
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int testsPerThread = Math.max(1, testCases.size() / numThreads);
        
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            final int start = t * testsPerThread;
            final int end = Math.min((t + 1) * testsPerThread, testCases.size());
            
            if (start >= testCases.size()) break;
            
            executor.submit(() -> {
                try {
                    // Setup phase
                    System.out.println("  [Thread " + threadId + "] Setup");
                    Thread.sleep(50);
                    setupBarrier.await();
                    
                    // Execution phase
                    for (int i = start; i < end; i++) {
                        testCases.get(i).run(threadId);
                    }
                    
                    // Wait for all tests to complete
                    teardownBarrier.await();
                    
                    // Teardown phase
                    System.out.println("  [Thread " + threadId + "] Teardown");
                    Thread.sleep(50);
                    
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
    
    static class TestCase {
        private final String name;
        
        public TestCase(String name) {
            this.name = name;
        }
        
        public void run(int threadId) {
            System.out.println("  [Thread " + threadId + "] Running test: " + name);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

// Example 5: Custom Barrier Implementation
class CustomBarrier {
    private final int parties;
    private final Runnable barrierAction;
    private int count;
    private final Lock lock = new ReentrantLock();
    private final Condition allArrived = lock.newCondition();
    
    public CustomBarrier(int parties, Runnable barrierAction) {
        this.parties = parties;
        this.barrierAction = barrierAction;
        this.count = parties;
    }
    
    public void await() throws InterruptedException {
        lock.lock();
        try {
            count--;
            
            if (count > 0) {
                // Not everyone has arrived yet - wait
                System.out.println("  [" + Thread.currentThread().getName() + 
                                 "] Waiting at barrier (" + count + " more expected)");
                allArrived.await();
            } else {
                // Last thread to arrive - release everyone
                System.out.println("  [" + Thread.currentThread().getName() + 
                                 "] Last to arrive - releasing all threads");
                
                if (barrierAction != null) {
                    barrierAction.run();
                }
                
                count = parties; // Reset for reuse
                allArrived.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}

// Demo
public class BarrierPattern {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   BARRIER PATTERN DEMONSTRATION      ║");
        System.out.println("╚══════════════════════════════════════╝");
        
        // Example 1: Matrix computation with phases
        System.out.println("\n1. MATRIX COMPUTATION (Multi-Phase Parallel Processing)");
        System.out.println("────────────────────────────────────────────────────────");
        
        MatrixProcessor matrixProcessor = new MatrixProcessor(10, 10, 4);
        matrixProcessor.processInParallel(4);
        matrixProcessor.printMatrix();
        
        // Example 2: Parallel sorting
        System.out.println("\n\n2. PARALLEL SORTING (Synchronized Sorting Phases)");
        System.out.println("──────────────────────────────────────────────────");
        
        ParallelSorter sorter = new ParallelSorter(100, 4);
        sorter.parallelSort(4);
        
        // Example 3: Simulation with synchronized steps
        System.out.println("\n\n3. SIMULATION (Lockstep Execution)");
        System.out.println("───────────────────────────────────");
        
        Simulation simulation = new Simulation(3);
        simulation.runSimulation(5);
        
        // Example 4: Parallel testing framework
        System.out.println("\n\n4. PARALLEL TEST RUNNER (Setup-Execute-Teardown)");
        System.out.println("─────────────────────────────────────────────────");
        
        ParallelTestRunner testRunner = new ParallelTestRunner(3);
        testRunner.addTest("TestUserLogin");
        testRunner.addTest("TestUserRegistration");
        testRunner.addTest("TestPasswordReset");
        testRunner.addTest("TestProfileUpdate");
        testRunner.addTest("TestOrderPlacement");
        testRunner.addTest("TestPaymentProcessing");
        testRunner.runTests();
        
        // Example 5: Custom barrier implementation
        System.out.println("\n\n5. CUSTOM BARRIER IMPLEMENTATION");
        System.out.println("─────────────────────────────────");
        
        CustomBarrier customBarrier = new CustomBarrier(3, () -> {
            System.out.println("  ⚡ Barrier action: All threads synchronized!");
        });
        
        for (int i = 0; i < 3; i++) {
            final int threadNum = i;
            new Thread(() -> {
                try {
                    Thread.sleep(threadNum * 200); // Stagger arrival
                    customBarrier.await();
                    System.out.println("  [Thread-" + threadNum + "] Proceeding after barrier");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Thread-" + i).start();
        }
        
        Thread.sleep(1000);
        
        System.out.println("\n\n✅ Barrier Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Synchronizes multiple threads at barrier points");
        System.out.println("  • Enables phased parallel algorithms");
        System.out.println("  • Simplifies complex thread coordination");
        System.out.println("  • Supports reusable barriers (CyclicBarrier)");
        System.out.println("  • Can execute action when all threads arrive");
        
        System.out.println("\n🔧 Java Support:");
        System.out.println("  • CyclicBarrier - Reusable barrier with optional action");
        System.out.println("  • CountDownLatch - One-time countdown (not reusable)");
        System.out.println("  • Phaser - More flexible phase-based synchronization");
    }
}
