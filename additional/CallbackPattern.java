package additional;

import java.util.*;
import java.util.function.*;

/**
 * Callback Pattern
 * 
 * Intent: A piece of executable code that is passed as an argument
 * to other code, which is expected to call back (execute) the argument
 * at a convenient time.
 * 
 * Motivation:
 * Enables asynchronous processing.
 * Decouples caller from callee.
 * Allows customization of behavior.
 * Supports event-driven programming.
 * 
 * Applicability:
 * - Asynchronous operations
 * - Event handling
 * - Custom behavior injection
 * - Non-blocking I/O
 */

/**
 * Example 1: Basic Callback Interface
 * 
 * Traditional callback using interface
 */
interface Callback {
    void onComplete(String result);
    void onError(String error);
}

class FileDownloader {
    public void download(String url, Callback callback) {
        System.out.println("  [Downloader] Starting download: " + url);
        
        // Simulate download
        new Thread(() -> {
            try {
                Thread.sleep(100); // Simulate network delay
                
                if (url.contains("valid")) {
                    callback.onComplete("Downloaded: " + url);
                } else {
                    callback.onError("Invalid URL: " + url);
                }
            } catch (InterruptedException e) {
                callback.onError("Download interrupted");
            }
        }).start();
    }
}

/**
 * Example 2: Functional Callback (Java 8+)
 * 
 * Using Consumer and BiConsumer
 */
class AsyncProcessor {
    public void processAsync(String data, 
                            Consumer<String> onSuccess,
                            Consumer<String> onFailure) {
        System.out.println("  [AsyncProcessor] Processing: " + data);
        
        new Thread(() -> {
            try {
                Thread.sleep(50);
                
                if (data != null && !data.isEmpty()) {
                    String result = "Processed: " + data.toUpperCase();
                    onSuccess.accept(result);
                } else {
                    onFailure.accept("Empty data provided");
                }
            } catch (Exception e) {
                onFailure.accept("Processing error: " + e.getMessage());
            }
        }).start();
    }
}

/**
 * Example 3: Callback Chain
 * 
 * Multiple callbacks in sequence
 */
class CallbackChain<T> {
    private final List<Consumer<T>> callbacks;
    
    public CallbackChain() {
        this.callbacks = new ArrayList<>();
    }
    
    public CallbackChain<T> then(Consumer<T> callback) {
        callbacks.add(callback);
        return this;
    }
    
    public void execute(T value) {
        System.out.println("  [CallbackChain] Executing " + callbacks.size() + " callbacks");
        for (Consumer<T> callback : callbacks) {
            callback.accept(value);
        }
    }
}

class DataPipeline {
    public void process(String data, CallbackChain<String> chain) {
        System.out.println("  [Pipeline] Processing: " + data);
        
        new Thread(() -> {
            try {
                Thread.sleep(50);
                String result = data.toUpperCase();
                chain.execute(result);
            } catch (InterruptedException e) {
                System.err.println("Pipeline interrupted");
            }
        }).start();
    }
}

/**
 * Example 4: Event Callback System
 * 
 * Callbacks for different event types
 */
class EventCallbackSystem {
    private final Map<String, List<Consumer<Object>>> callbacks;
    
    public EventCallbackSystem() {
        this.callbacks = new HashMap<>();
    }
    
    public void on(String event, Consumer<Object> callback) {
        callbacks.computeIfAbsent(event, k -> new ArrayList<>()).add(callback);
        System.out.println("  [EventSystem] Registered callback for: " + event);
    }
    
    public void emit(String event, Object data) {
        List<Consumer<Object>> eventCallbacks = callbacks.get(event);
        if (eventCallbacks != null) {
            System.out.println("  [EventSystem] Emitting event: " + event);
            for (Consumer<Object> callback : eventCallbacks) {
                callback.accept(data);
            }
        }
    }
    
    public void off(String event) {
        callbacks.remove(event);
        System.out.println("  [EventSystem] Removed callbacks for: " + event);
    }
}

/**
 * Example 5: Promise-like Callback
 * 
 * Future-style callback with chaining
 */
class Promise<T> {
    private T result;
    private Exception error;
    private boolean completed = false;
    private final List<Consumer<T>> successCallbacks = new ArrayList<>();
    private final List<Consumer<Exception>> errorCallbacks = new ArrayList<>();
    
    public Promise<T> then(Consumer<T> onSuccess) {
        synchronized (this) {
            if (completed && error == null) {
                onSuccess.accept(result);
            } else {
                successCallbacks.add(onSuccess);
            }
        }
        return this;
    }
    
    public Promise<T> catchError(Consumer<Exception> onError) {
        synchronized (this) {
            if (completed && error != null) {
                onError.accept(error);
            } else {
                errorCallbacks.add(onError);
            }
        }
        return this;
    }
    
    public void resolve(T value) {
        synchronized (this) {
            if (completed) return;
            
            this.result = value;
            this.completed = true;
            
            for (Consumer<T> callback : successCallbacks) {
                callback.accept(value);
            }
            successCallbacks.clear();
        }
    }
    
    public void reject(Exception exception) {
        synchronized (this) {
            if (completed) return;
            
            this.error = exception;
            this.completed = true;
            
            for (Consumer<Exception> callback : errorCallbacks) {
                callback.accept(exception);
            }
            errorCallbacks.clear();
        }
    }
}

class AsyncService {
    public Promise<String> fetchData(String id) {
        Promise<String> promise = new Promise<>();
        
        System.out.println("  [AsyncService] Fetching data for: " + id);
        
        new Thread(() -> {
            try {
                Thread.sleep(100);
                
                if (id != null && !id.isEmpty()) {
                    promise.resolve("Data for " + id);
                } else {
                    promise.reject(new IllegalArgumentException("Invalid ID"));
                }
            } catch (InterruptedException e) {
                promise.reject(e);
            }
        }).start();
        
        return promise;
    }
}

/**
 * Example 6: Callback with State
 * 
 * Callbacks that track progress
 */
interface ProgressCallback {
    void onProgress(int percent);
    void onComplete(String result);
}

class LongRunningTask {
    public void execute(String taskName, ProgressCallback callback) {
        System.out.println("  [Task] Starting: " + taskName);
        
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i += 25) {
                    Thread.sleep(50);
                    callback.onProgress(i);
                }
                callback.onComplete("Task completed: " + taskName);
            } catch (InterruptedException e) {
                System.err.println("Task interrupted");
            }
        }).start();
    }
}

/**
 * Demonstration of the Callback Pattern
 */
public class CallbackPattern {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Callback Pattern Demo ===\n");
        
        // Example 1: Traditional Callback Interface
        System.out.println("1. Traditional Callback Interface:");
        FileDownloader downloader = new FileDownloader();
        
        downloader.download("https://example.com/valid-file.zip", new Callback() {
            @Override
            public void onComplete(String result) {
                System.out.println("  [Main] " + result);
            }
            
            @Override
            public void onError(String error) {
                System.out.println("  [Main] Error: " + error);
            }
        });
        
        Thread.sleep(150); // Wait for async operation
        
        // Example 2: Functional Callbacks
        System.out.println("\n2. Functional Callbacks:");
        AsyncProcessor processor = new AsyncProcessor();
        
        processor.processAsync("hello world",
            result -> System.out.println("  [Main] Success: " + result),
            error -> System.out.println("  [Main] Failure: " + error));
        
        Thread.sleep(100);
        
        // Example 3: Callback Chain
        System.out.println("\n3. Callback Chain:");
        DataPipeline pipeline = new DataPipeline();
        
        CallbackChain<String> chain = new CallbackChain<>();
        chain.then(data -> System.out.println("  [Step 1] Received: " + data))
             .then(data -> System.out.println("  [Step 2] Length: " + data.length()))
             .then(data -> System.out.println("  [Step 3] First char: " + data.charAt(0)));
        
        pipeline.process("callback", chain);
        
        Thread.sleep(100);
        
        // Example 4: Event Callback System
        System.out.println("\n4. Event Callback System:");
        EventCallbackSystem eventSystem = new EventCallbackSystem();
        
        eventSystem.on("user.login", data -> {
            System.out.println("  [Callback 1] User logged in: " + data);
        });
        
        eventSystem.on("user.login", data -> {
            System.out.println("  [Callback 2] Sending welcome email to: " + data);
        });
        
        eventSystem.emit("user.login", "alice@example.com");
        
        // Example 5: Promise-like Callback
        System.out.println("\n5. Promise-like Callback:");
        AsyncService service = new AsyncService();
        
        service.fetchData("user-123")
               .then(data -> System.out.println("  [Main] Fetched: " + data))
               .then(data -> System.out.println("  [Main] Processing complete"))
               .catchError(error -> System.out.println("  [Main] Error: " + error.getMessage()));
        
        Thread.sleep(150);
        
        // Example 6: Progress Callback
        System.out.println("\n6. Progress Callback:");
        LongRunningTask task = new LongRunningTask();
        
        task.execute("Data Analysis", new ProgressCallback() {
            @Override
            public void onProgress(int percent) {
                System.out.println("  [Progress] " + percent + "% complete");
            }
            
            @Override
            public void onComplete(String result) {
                System.out.println("  [Complete] " + result);
            }
        });
        
        Thread.sleep(300);
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Asynchronous execution");
        System.out.println("✓ Decouples components");
        System.out.println("✓ Event-driven architecture");
        System.out.println("✓ Custom behavior injection");
        System.out.println("✓ Non-blocking operations");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• Asynchronous I/O");
        System.out.println("• Event handling");
        System.out.println("• GUI frameworks");
        System.out.println("• Network operations");
        System.out.println("• Database queries");
    }
}
