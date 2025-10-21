package concurrency;

import java.util.*;
import java.util.concurrent.*;

/**
 * Actor Model Pattern
 * Objects (actors) communicate via asynchronous message passing.
 */
public class ActorModelPattern {
    
    // Message interface
    interface Message {}
    
    // Actor interface
    interface Actor {
        void receive(Message message);
        void start();
        void stop();
        void send(Message message);
    }
    
    // Base Actor implementation
    static abstract class BaseActor implements Actor {
        private final BlockingQueue<Message> mailbox = new LinkedBlockingQueue<>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private volatile boolean running = false;
        protected final String name;
        
        public BaseActor(String name) {
            this.name = name;
        }
        
        @Override
        public void start() {
            running = true;
            executor.submit(() -> {
                while (running) {
                    try {
                        Message message = mailbox.poll(100, TimeUnit.MILLISECONDS);
                        if (message != null) {
                            receive(message);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        @Override
        public void stop() {
            running = false;
            executor.shutdown();
        }
        
        public void send(Message message) {
            mailbox.offer(message);
        }
        
        protected void log(String message) {
            System.out.println("[" + name + "] " + message);
        }
    }
    
    // Concrete Messages
    static class TextMessage implements Message {
        private final String content;
        private final Actor sender;
        
        public TextMessage(String content, Actor sender) {
            this.content = content;
            this.sender = sender;
        }
        
        public String getContent() { return content; }
        public Actor getSender() { return sender; }
    }
    
    static class PingMessage implements Message {
        private final Actor sender;
        
        public PingMessage(Actor sender) {
            this.sender = sender;
        }
        
        public Actor getSender() { return sender; }
    }
    
    static class PongMessage implements Message {}
    
    static class StartMessage implements Message {}
    static class StopMessage implements Message {}
    
    // Concrete Actors
    static class PrinterActor extends BaseActor {
        public PrinterActor(String name) {
            super(name);
        }
        
        @Override
        public void receive(Message message) {
            if (message instanceof TextMessage) {
                TextMessage msg = (TextMessage) message;
                log("Printing: " + msg.getContent());
            }
        }
    }
    
    static class PingPongActor extends BaseActor {
        public PingPongActor(String name) {
            super(name);
        }
        
        @Override
        public void receive(Message message) {
            if (message instanceof PingMessage) {
                log("Received PING");
                PingMessage ping = (PingMessage) message;
                ping.getSender().send(new PongMessage());
            } else if (message instanceof PongMessage) {
                log("Received PONG");
            }
        }
    }
    
    // Counter Actor with state
    static class CounterMessage implements Message {
        enum Action { INCREMENT, DECREMENT, GET }
        private final Action action;
        private final Actor replyTo;
        
        public CounterMessage(Action action, Actor replyTo) {
            this.action = action;
            this.replyTo = replyTo;
        }
        
        public Action getAction() { return action; }
        public Actor getReplyTo() { return replyTo; }
    }
    
    static class CounterResultMessage implements Message {
        private final int value;
        
        public CounterResultMessage(int value) {
            this.value = value;
        }
        
        public int getValue() { return value; }
    }
    
    static class CounterActor extends BaseActor {
        private int count = 0;
        
        public CounterActor(String name) {
            super(name);
        }
        
        @Override
        public void receive(Message message) {
            if (message instanceof CounterMessage) {
                CounterMessage msg = (CounterMessage) message;
                switch (msg.getAction()) {
                    case INCREMENT:
                        count++;
                        log("Incremented to " + count);
                        break;
                    case DECREMENT:
                        count--;
                        log("Decremented to " + count);
                        break;
                    case GET:
                        log("Returning count: " + count);
                        if (msg.getReplyTo() != null) {
                            msg.getReplyTo().send(new CounterResultMessage(count));
                        }
                        break;
                }
            }
        }
    }
    
    // Worker Actor for parallel processing
    static class WorkMessage implements Message {
        private final int taskId;
        private final int value;
        private final Actor coordinator;
        
        public WorkMessage(int taskId, int value, Actor coordinator) {
            this.taskId = taskId;
            this.value = value;
            this.coordinator = coordinator;
        }
        
        public int getTaskId() { return taskId; }
        public int getValue() { return value; }
        public Actor getCoordinator() { return coordinator; }
    }
    
    static class ResultMessage implements Message {
        private final int taskId;
        private final int result;
        
        public ResultMessage(int taskId, int result) {
            this.taskId = taskId;
            this.result = result;
        }
        
        public int getTaskId() { return taskId; }
        public int getResult() { return result; }
    }
    
    static class WorkerActor extends BaseActor {
        public WorkerActor(String name) {
            super(name);
        }
        
        @Override
        public void receive(Message message) {
            if (message instanceof WorkMessage) {
                WorkMessage work = (WorkMessage) message;
                log("Processing task " + work.getTaskId());
                
                // Simulate work
                int result = work.getValue() * work.getValue();
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                log("Completed task " + work.getTaskId() + " with result " + result);
                work.getCoordinator().send(new ResultMessage(work.getTaskId(), result));
            }
        }
    }
    
    static class CoordinatorActor extends BaseActor {
        private int completedTasks = 0;
        private final int totalTasks;
        private final Map<Integer, Integer> results = new HashMap<>();
        
        public CoordinatorActor(String name, int totalTasks) {
            super(name);
            this.totalTasks = totalTasks;
        }
        
        @Override
        public void receive(Message message) {
            if (message instanceof ResultMessage) {
                ResultMessage result = (ResultMessage) message;
                results.put(result.getTaskId(), result.getResult());
                completedTasks++;
                log("Received result for task " + result.getTaskId() + 
                    " (" + completedTasks + "/" + totalTasks + ")");
                
                if (completedTasks == totalTasks) {
                    log("All tasks completed! Results: " + results);
                }
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Actor Model Pattern Demo ===\n");
        
        // 1. Simple Printer Actor
        System.out.println("1. Simple Printer Actor:");
        PrinterActor printer = new PrinterActor("Printer");
        printer.start();
        
        printer.send(new TextMessage("Hello, Actor Model!", null));
        printer.send(new TextMessage("Actors process messages asynchronously", null));
        
        Thread.sleep(1000);
        printer.stop();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 2. Ping-Pong between actors
        System.out.println("2. Ping-Pong Actors:");
        PingPongActor actor1 = new PingPongActor("Actor1");
        PingPongActor actor2 = new PingPongActor("Actor2");
        
        actor1.start();
        actor2.start();
        
        actor2.send(new PingMessage(actor1));
        Thread.sleep(500);
        actor1.send(new PingMessage(actor2));
        
        Thread.sleep(1000);
        actor1.stop();
        actor2.stop();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 3. Counter Actor with state
        System.out.println("3. Counter Actor:");
        CounterActor counter = new CounterActor("Counter");
        counter.start();
        
        counter.send(new CounterMessage(CounterMessage.Action.INCREMENT, null));
        counter.send(new CounterMessage(CounterMessage.Action.INCREMENT, null));
        counter.send(new CounterMessage(CounterMessage.Action.INCREMENT, null));
        counter.send(new CounterMessage(CounterMessage.Action.DECREMENT, null));
        counter.send(new CounterMessage(CounterMessage.Action.GET, null));
        
        Thread.sleep(1000);
        counter.stop();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 4. Parallel processing with workers
        System.out.println("4. Parallel Processing with Worker Actors:");
        int numTasks = 5;
        CoordinatorActor coordinator = new CoordinatorActor("Coordinator", numTasks);
        coordinator.start();
        
        WorkerActor worker1 = new WorkerActor("Worker1");
        WorkerActor worker2 = new WorkerActor("Worker2");
        WorkerActor worker3 = new WorkerActor("Worker3");
        
        worker1.start();
        worker2.start();
        worker3.start();
        
        // Distribute work
        worker1.send(new WorkMessage(1, 5, coordinator));
        worker2.send(new WorkMessage(2, 10, coordinator));
        worker3.send(new WorkMessage(3, 15, coordinator));
        worker1.send(new WorkMessage(4, 20, coordinator));
        worker2.send(new WorkMessage(5, 25, coordinator));
        
        Thread.sleep(3000);
        
        worker1.stop();
        worker2.stop();
        worker3.stop();
        coordinator.stop();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ No shared mutable state");
        System.out.println("✓ Natural concurrency model");
        System.out.println("✓ Location transparency");
        System.out.println("✓ Fault tolerance through supervision");
        System.out.println("✓ Scalable architecture");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Distributed systems (Akka, Orleans)");
        System.out.println("• Real-time messaging systems");
        System.out.println("• Game servers");
        System.out.println("• IoT device communication");
        System.out.println("• Concurrent data processing");
    }
}
