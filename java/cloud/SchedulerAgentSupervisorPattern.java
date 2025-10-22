package cloud;

import java.util.*;
import java.util.concurrent.*;

/**
 * Scheduler Agent Supervisor Pattern
 * 
 * Intent: Coordinate a set of distributed actions as a single operation.
 * If any action fails, handle failures transparently and retry, or undo
 * work that was performed.
 * 
 * Also Known As:
 * - Orchestrated Saga
 * - Workflow Coordinator
 * - Task Orchestration
 * 
 * Motivation:
 * Distributed applications often need to perform operations across multiple
 * services or resources. Challenges include:
 * - Individual steps may fail
 * - Need to track overall progress
 * - Must handle retries and failures
 * - May need to compensate/rollback on failure
 * - Need coordination across services
 * 
 * Applicability:
 * - Multi-step distributed workflows
 * - Operations spanning multiple services
 * - Long-running business processes
 * - Need for automatic retry and recovery
 * - Complex orchestration requirements
 * 
 * Benefits:
 * - Centralized coordination logic
 * - Automatic retry and error handling
 * - Progress tracking and visibility
 * - Failure recovery
 * - Compensation/rollback support
 * 
 * Trade-offs:
 * - Central coordinator can be bottleneck
 * - Increased complexity
 * - Requires careful state management
 * - Potential single point of failure
 */

// Step status
enum StepStatus {
    PENDING, RUNNING, COMPLETED, FAILED, COMPENSATED
}

// Workflow step
abstract class WorkflowStep {
    private final String name;
    private StepStatus status;
    private String errorMessage;
    private int attemptCount;
    
    public WorkflowStep(String name) {
        this.name = name;
        this.status = StepStatus.PENDING;
        this.attemptCount = 0;
    }
    
    public abstract void execute() throws Exception;
    public abstract void compensate() throws Exception;
    
    public String getName() { return name; }
    public StepStatus getStatus() { return status; }
    public void setStatus(StepStatus status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String error) { this.errorMessage = error; }
    public int getAttemptCount() { return attemptCount; }
    public void incrementAttempt() { this.attemptCount++; }
    
    @Override
    public String toString() {
        return String.format("%s [%s]", name, status);
    }
}

// Example 1: Basic Scheduler Agent Supervisor
class BasicSchedulerSupervisor {
    private final List<WorkflowStep> steps;
    private final int maxRetries;
    
    public BasicSchedulerSupervisor(int maxRetries) {
        this.steps = new ArrayList<>();
        this.maxRetries = maxRetries;
    }
    
    public void addStep(WorkflowStep step) {
        steps.add(step);
    }
    
    public boolean executeWorkflow() {
        System.out.println("=== Starting Workflow ===");
        
        for (int i = 0; i < steps.size(); i++) {
            WorkflowStep step = steps.get(i);
            boolean success = executeStepWithRetry(step);
            
            if (!success) {
                System.out.println(String.format("\n✗ Workflow failed at step: %s", step.getName()));
                compensateCompletedSteps(i - 1);
                return false;
            }
        }
        
        System.out.println("\n✓ Workflow completed successfully!");
        return true;
    }
    
    private boolean executeStepWithRetry(WorkflowStep step) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            step.incrementAttempt();
            step.setStatus(StepStatus.RUNNING);
            
            System.out.println(String.format("\n[Attempt %d/%d] Executing: %s", 
                attempt, maxRetries, step.getName()));
            
            try {
                step.execute();
                step.setStatus(StepStatus.COMPLETED);
                System.out.println(String.format("  ✓ %s completed", step.getName()));
                return true;
            } catch (Exception e) {
                step.setStatus(StepStatus.FAILED);
                step.setErrorMessage(e.getMessage());
                System.out.println(String.format("  ✗ %s failed: %s", step.getName(), e.getMessage()));
                
                if (attempt < maxRetries) {
                    System.out.println("  Retrying...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        return false;
    }
    
    private void compensateCompletedSteps(int lastCompletedIndex) {
        System.out.println("\n=== Compensating completed steps ===");
        
        for (int i = lastCompletedIndex; i >= 0; i--) {
            WorkflowStep step = steps.get(i);
            if (step.getStatus() == StepStatus.COMPLETED) {
                System.out.println(String.format("Compensating: %s", step.getName()));
                try {
                    step.compensate();
                    step.setStatus(StepStatus.COMPENSATED);
                    System.out.println(String.format("  ✓ %s compensated", step.getName()));
                } catch (Exception e) {
                    System.out.println(String.format("  ✗ Compensation failed for %s: %s", 
                        step.getName(), e.getMessage()));
                }
            }
        }
    }
    
    public void printStatus() {
        System.out.println("\nWorkflow Status:");
        for (WorkflowStep step : steps) {
            System.out.println(String.format("  %s", step));
        }
    }
}

// Example 2: Parallel Step Execution
class ParallelSchedulerSupervisor {
    private final List<List<WorkflowStep>> stages;
    private final ExecutorService executor;
    private final int maxRetries;
    
    public ParallelSchedulerSupervisor(int maxRetries) {
        this.stages = new ArrayList<>();
        this.executor = Executors.newCachedThreadPool();
        this.maxRetries = maxRetries;
    }
    
    public void addStage(List<WorkflowStep> steps) {
        stages.add(new ArrayList<>(steps));
    }
    
    public boolean executeWorkflow() {
        System.out.println("=== Starting Parallel Workflow ===");
        
        for (int stageNum = 0; stageNum < stages.size(); stageNum++) {
            List<WorkflowStep> stage = stages.get(stageNum);
            System.out.println(String.format("\n--- Stage %d: %d parallel steps ---", 
                stageNum + 1, stage.size()));
            
            boolean stageSuccess = executeStageInParallel(stage);
            
            if (!stageSuccess) {
                System.out.println(String.format("\n✗ Workflow failed at stage %d", stageNum + 1));
                compensateAllStages(stageNum);
                return false;
            }
        }
        
        System.out.println("\n✓ Parallel workflow completed successfully!");
        return true;
    }
    
    private boolean executeStageInParallel(List<WorkflowStep> steps) {
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (WorkflowStep step : steps) {
            Future<Boolean> future = executor.submit(() -> executeStepWithRetry(step));
            futures.add(future);
        }
        
        // Wait for all steps to complete
        boolean allSuccess = true;
        for (Future<Boolean> future : futures) {
            try {
                if (!future.get()) {
                    allSuccess = false;
                }
            } catch (Exception e) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    private boolean executeStepWithRetry(WorkflowStep step) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            step.incrementAttempt();
            step.setStatus(StepStatus.RUNNING);
            
            System.out.println(String.format("[Attempt %d] %s", attempt, step.getName()));
            
            try {
                step.execute();
                step.setStatus(StepStatus.COMPLETED);
                System.out.println(String.format("  ✓ %s completed", step.getName()));
                return true;
            } catch (Exception e) {
                step.setStatus(StepStatus.FAILED);
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return false;
    }
    
    private void compensateAllStages(int lastStageIndex) {
        System.out.println("\n=== Compensating all completed stages ===");
        
        for (int i = lastStageIndex; i >= 0; i--) {
            for (WorkflowStep step : stages.get(i)) {
                if (step.getStatus() == StepStatus.COMPLETED) {
                    try {
                        step.compensate();
                        step.setStatus(StepStatus.COMPENSATED);
                        System.out.println(String.format("  ✓ Compensated: %s", step.getName()));
                    } catch (Exception e) {
                        System.out.println(String.format("  ✗ Compensation failed: %s", step.getName()));
                    }
                }
            }
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Example 3: Stateful Workflow Manager
class StatefulWorkflowManager {
    private final Map<String, WorkflowExecution> executions;
    private final ScheduledExecutorService scheduler;
    
    static class WorkflowExecution {
        private final String workflowId;
        private final List<WorkflowStep> steps;
        private int currentStepIndex;
        private long startTime;
        private long endTime;
        private boolean completed;
        private boolean failed;
        
        public WorkflowExecution(String workflowId, List<WorkflowStep> steps) {
            this.workflowId = workflowId;
            this.steps = new ArrayList<>(steps);
            this.currentStepIndex = 0;
            this.startTime = System.currentTimeMillis();
            this.completed = false;
            this.failed = false;
        }
        
        public String getWorkflowId() { return workflowId; }
        public List<WorkflowStep> getSteps() { return steps; }
        public int getCurrentStepIndex() { return currentStepIndex; }
        public void setCurrentStepIndex(int index) { this.currentStepIndex = index; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { 
            this.completed = completed;
            this.endTime = System.currentTimeMillis();
        }
        public boolean isFailed() { return failed; }
        public void setFailed(boolean failed) { this.failed = failed; }
        
        public double getProgress() {
            return (double) currentStepIndex / steps.size() * 100;
        }
        
        public long getDuration() {
            long end = completed ? endTime : System.currentTimeMillis();
            return end - startTime;
        }
    }
    
    public StatefulWorkflowManager() {
        this.executions = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    public String startWorkflow(List<WorkflowStep> steps) {
        String workflowId = "WF-" + UUID.randomUUID().toString().substring(0, 8);
        WorkflowExecution execution = new WorkflowExecution(workflowId, steps);
        executions.put(workflowId, execution);
        
        System.out.println(String.format("Started workflow: %s (%d steps)", 
            workflowId, steps.size()));
        
        // Schedule execution
        scheduler.submit(() -> executeWorkflow(execution));
        
        return workflowId;
    }
    
    private void executeWorkflow(WorkflowExecution execution) {
        List<WorkflowStep> steps = execution.getSteps();
        
        for (int i = 0; i < steps.size(); i++) {
            execution.setCurrentStepIndex(i);
            WorkflowStep step = steps.get(i);
            
            System.out.println(String.format("[%s] Executing step %d/%d: %s", 
                execution.getWorkflowId(), i + 1, steps.size(), step.getName()));
            
            try {
                step.setStatus(StepStatus.RUNNING);
                step.execute();
                step.setStatus(StepStatus.COMPLETED);
                System.out.println(String.format("  ✓ Step completed (%.1f%%)", 
                    execution.getProgress()));
            } catch (Exception e) {
                step.setStatus(StepStatus.FAILED);
                execution.setFailed(true);
                System.out.println(String.format("  ✗ Step failed: %s", e.getMessage()));
                return;
            }
        }
        
        execution.setCurrentStepIndex(steps.size());
        execution.setCompleted(true);
        System.out.println(String.format("[%s] Workflow completed in %dms", 
            execution.getWorkflowId(), execution.getDuration()));
    }
    
    public WorkflowExecution getStatus(String workflowId) {
        return executions.get(workflowId);
    }
    
    public void printStatus(String workflowId) {
        WorkflowExecution execution = executions.get(workflowId);
        if (execution != null) {
            System.out.println(String.format("\nWorkflow %s:", workflowId));
            System.out.println(String.format("  Progress: %.1f%%", execution.getProgress()));
            System.out.println(String.format("  Completed: %s", execution.isCompleted()));
            System.out.println(String.format("  Failed: %s", execution.isFailed()));
            System.out.println(String.format("  Duration: %dms", execution.getDuration()));
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
    }
}

// Example 4: Timeout-Aware Supervisor
class TimeoutSupervisor {
    private final List<WorkflowStep> steps;
    private final long stepTimeoutMs;
    private final ExecutorService executor;
    
    public TimeoutSupervisor(long stepTimeoutMs) {
        this.steps = new ArrayList<>();
        this.stepTimeoutMs = stepTimeoutMs;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public void addStep(WorkflowStep step) {
        steps.add(step);
    }
    
    public boolean executeWorkflow() {
        System.out.println("=== Workflow with Timeouts ===");
        
        for (WorkflowStep step : steps) {
            boolean success = executeStepWithTimeout(step);
            if (!success) {
                System.out.println(String.format("\n✗ Workflow failed at: %s", step.getName()));
                return false;
            }
        }
        
        System.out.println("\n✓ Workflow completed!");
        return true;
    }
    
    private boolean executeStepWithTimeout(WorkflowStep step) {
        System.out.println(String.format("\nExecuting: %s (timeout: %dms)", 
            step.getName(), stepTimeoutMs));
        
        Future<?> future = executor.submit(() -> {
            try {
                step.setStatus(StepStatus.RUNNING);
                step.execute();
                step.setStatus(StepStatus.COMPLETED);
            } catch (Exception e) {
                step.setStatus(StepStatus.FAILED);
                throw new RuntimeException(e);
            }
        });
        
        try {
            future.get(stepTimeoutMs, TimeUnit.MILLISECONDS);
            System.out.println(String.format("  ✓ %s completed", step.getName()));
            return true;
        } catch (TimeoutException e) {
            future.cancel(true);
            step.setStatus(StepStatus.FAILED);
            step.setErrorMessage("Timeout after " + stepTimeoutMs + "ms");
            System.out.println(String.format("  ✗ %s timed out", step.getName()));
            return false;
        } catch (Exception e) {
            step.setStatus(StepStatus.FAILED);
            System.out.println(String.format("  ✗ %s failed: %s", step.getName(), e.getMessage()));
            return false;
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Example 5: Event-Driven Supervisor
class EventDrivenSupervisor {
    private final List<WorkflowStep> steps;
    private final List<WorkflowEventListener> listeners;
    
    interface WorkflowEventListener {
        void onStepStarted(WorkflowStep step);
        void onStepCompleted(WorkflowStep step);
        void onStepFailed(WorkflowStep step, Exception error);
        void onWorkflowCompleted();
        void onWorkflowFailed();
    }
    
    static class LoggingEventListener implements WorkflowEventListener {
        @Override
        public void onStepStarted(WorkflowStep step) {
            System.out.println(String.format("[EVENT] Step started: %s", step.getName()));
        }
        
        @Override
        public void onStepCompleted(WorkflowStep step) {
            System.out.println(String.format("[EVENT] Step completed: %s ✓", step.getName()));
        }
        
        @Override
        public void onStepFailed(WorkflowStep step, Exception error) {
            System.out.println(String.format("[EVENT] Step failed: %s ✗ (%s)", 
                step.getName(), error.getMessage()));
        }
        
        @Override
        public void onWorkflowCompleted() {
            System.out.println("[EVENT] Workflow completed successfully! ✓");
        }
        
        @Override
        public void onWorkflowFailed() {
            System.out.println("[EVENT] Workflow failed! ✗");
        }
    }
    
    public EventDrivenSupervisor() {
        this.steps = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }
    
    public void addStep(WorkflowStep step) {
        steps.add(step);
    }
    
    public void addEventListener(WorkflowEventListener listener) {
        listeners.add(listener);
    }
    
    public boolean executeWorkflow() {
        for (WorkflowStep step : steps) {
            notifyStepStarted(step);
            
            try {
                step.setStatus(StepStatus.RUNNING);
                step.execute();
                step.setStatus(StepStatus.COMPLETED);
                notifyStepCompleted(step);
            } catch (Exception e) {
                step.setStatus(StepStatus.FAILED);
                notifyStepFailed(step, e);
                notifyWorkflowFailed();
                return false;
            }
        }
        
        notifyWorkflowCompleted();
        return true;
    }
    
    private void notifyStepStarted(WorkflowStep step) {
        for (WorkflowEventListener listener : listeners) {
            listener.onStepStarted(step);
        }
    }
    
    private void notifyStepCompleted(WorkflowStep step) {
        for (WorkflowEventListener listener : listeners) {
            listener.onStepCompleted(step);
        }
    }
    
    private void notifyStepFailed(WorkflowStep step, Exception error) {
        for (WorkflowEventListener listener : listeners) {
            listener.onStepFailed(step, error);
        }
    }
    
    private void notifyWorkflowCompleted() {
        for (WorkflowEventListener listener : listeners) {
            listener.onWorkflowCompleted();
        }
    }
    
    private void notifyWorkflowFailed() {
        for (WorkflowEventListener listener : listeners) {
            listener.onWorkflowFailed();
        }
    }
}

// Demo
public class SchedulerAgentSupervisorPattern {
    public static void main(String[] args) throws InterruptedException {
        demonstrateBasicWorkflow();
        demonstrateParallelWorkflow();
        demonstrateStatefulWorkflow();
        demonstrateTimeoutWorkflow();
        demonstrateEventDrivenWorkflow();
    }
    
    private static void demonstrateBasicWorkflow() {
        System.out.println("=== Basic Scheduler Agent Supervisor ===\n");
        
        BasicSchedulerSupervisor supervisor = new BasicSchedulerSupervisor(3);
        
        supervisor.addStep(createStep("Reserve Inventory", false, 0));
        supervisor.addStep(createStep("Process Payment", true, 50));  // Will fail
        supervisor.addStep(createStep("Send Confirmation", false, 0));
        
        supervisor.executeWorkflow();
        supervisor.printStatus();
    }
    
    private static void demonstrateParallelWorkflow() {
        System.out.println("\n\n=== Parallel Workflow Execution ===\n");
        
        ParallelSchedulerSupervisor supervisor = new ParallelSchedulerSupervisor(2);
        
        // Stage 1: Parallel validations
        supervisor.addStage(Arrays.asList(
            createStep("Validate Address", false, 0),
            createStep("Validate Payment", false, 0),
            createStep("Check Inventory", false, 0)
        ));
        
        // Stage 2: Process order
        supervisor.addStage(Arrays.asList(
            createStep("Reserve Items", false, 0)
        ));
        
        supervisor.executeWorkflow();
        supervisor.shutdown();
    }
    
    private static void demonstrateStatefulWorkflow() throws InterruptedException {
        System.out.println("\n\n=== Stateful Workflow Manager ===\n");
        
        StatefulWorkflowManager manager = new StatefulWorkflowManager();
        
        List<WorkflowStep> steps = Arrays.asList(
            createStep("Step 1", false, 100),
            createStep("Step 2", false, 100),
            createStep("Step 3", false, 100)
        );
        
        String workflowId = manager.startWorkflow(steps);
        
        // Monitor progress
        Thread.sleep(150);
        manager.printStatus(workflowId);
        
        Thread.sleep(200);
        manager.printStatus(workflowId);
        
        manager.shutdown();
    }
    
    private static void demonstrateTimeoutWorkflow() {
        System.out.println("\n\n=== Timeout-Aware Supervisor ===\n");
        
        TimeoutSupervisor supervisor = new TimeoutSupervisor(500);
        
        supervisor.addStep(createStep("Fast Step", false, 100));
        supervisor.addStep(createStep("Slow Step", false, 1000));  // Will timeout
        
        supervisor.executeWorkflow();
        supervisor.shutdown();
    }
    
    private static void demonstrateEventDrivenWorkflow() {
        System.out.println("\n\n=== Event-Driven Supervisor ===\n");
        
        EventDrivenSupervisor supervisor = new EventDrivenSupervisor();
        supervisor.addEventListener(new EventDrivenSupervisor.LoggingEventListener());
        
        supervisor.addStep(createStep("Initialize", false, 50));
        supervisor.addStep(createStep("Process", false, 50));
        supervisor.addStep(createStep("Finalize", false, 50));
        
        supervisor.executeWorkflow();
    }
    
    private static WorkflowStep createStep(String name, boolean shouldFail, long delayMs) {
        return new WorkflowStep(name) {
            @Override
            public void execute() throws Exception {
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
                if (shouldFail) {
                    throw new Exception("Simulated failure");
                }
            }
            
            @Override
            public void compensate() throws Exception {
                System.out.println("    Undoing: " + getName());
            }
        };
    }
}
