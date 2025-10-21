package concurrency;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * Future/Promise Pattern
 * Represents a value that will be available in the future.
 */
public class FuturePromisePattern {
    
    // Simple Promise implementation
    static class Promise<T> {
        private volatile T value;
        private volatile Throwable exception;
        private volatile boolean completed = false;
        private final List<Consumer<T>> successCallbacks = new ArrayList<>();
        private final List<Consumer<Throwable>> errorCallbacks = new ArrayList<>();
        private final Object lock = new Object();
        
        public void resolve(T value) {
            synchronized (lock) {
                if (completed) {
                    throw new IllegalStateException("Promise already completed");
                }
                this.value = value;
                this.completed = true;
                for (Consumer<T> callback : successCallbacks) {
                    callback.accept(value);
                }
                lock.notifyAll();
            }
        }
        
        public void reject(Throwable exception) {
            synchronized (lock) {
                if (completed) {
                    throw new IllegalStateException("Promise already completed");
                }
                this.exception = exception;
                this.completed = true;
                for (Consumer<Throwable> callback : errorCallbacks) {
                    callback.accept(exception);
                }
                lock.notifyAll();
            }
        }
        
        public Promise<T> then(Consumer<T> onSuccess) {
            synchronized (lock) {
                if (completed && exception == null) {
                    onSuccess.accept(value);
                } else if (!completed) {
                    successCallbacks.add(onSuccess);
                }
            }
            return this;
        }
        
        public Promise<T> catchError(Consumer<Throwable> onError) {
            synchronized (lock) {
                if (completed && exception != null) {
                    onError.accept(exception);
                } else if (!completed) {
                    errorCallbacks.add(onError);
                }
            }
            return this;
        }
        
        public T get() throws InterruptedException, ExecutionException {
            synchronized (lock) {
                while (!completed) {
                    lock.wait();
                }
                if (exception != null) {
                    throw new ExecutionException(exception);
                }
                return value;
            }
        }
        
        public T get(long timeout, TimeUnit unit) 
                throws InterruptedException, ExecutionException, TimeoutException {
            synchronized (lock) {
                if (!completed) {
                    lock.wait(unit.toMillis(timeout));
                }
                if (!completed) {
                    throw new TimeoutException();
                }
                if (exception != null) {
                    throw new ExecutionException(exception);
                }
                return value;
            }
        }
    }
    
    // Using Java's CompletableFuture
    static class AsyncTaskExecutor {
        public static void demonstrateBasicFuture() {
            System.out.println("=== Basic Future ===");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            
            Future<Integer> future = executor.submit(() -> {
                System.out.println("Computing result...");
                Thread.sleep(2000);
                return 42;
            });
            
            System.out.println("Doing other work...");
            
            try {
                Integer result = future.get(); // Blocks until result is ready
                System.out.println("Result: " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            
            executor.shutdown();
        }
        
        public static void demonstrateCompletableFuture() {
            System.out.println("\n=== CompletableFuture ===");
            
            // Asynchronous computation
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                System.out.println("Fetching data...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "User Data";
            });
            
            // Chain operations
            future.thenApply(data -> {
                System.out.println("Processing: " + data);
                return data.toUpperCase();
            }).thenAccept(processed -> {
                System.out.println("Final result: " + processed);
            });
            
            // Wait for completion
            future.join();
        }
        
        public static void demonstrateCombiningFutures() {
            System.out.println("\n=== Combining Futures ===");
            
            CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
                System.out.println("Computing first value...");
                return 10;
            });
            
            CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
                System.out.println("Computing second value...");
                return 20;
            });
            
            // Combine two futures
            CompletableFuture<Integer> combined = future1.thenCombine(future2, 
                (a, b) -> {
                    int sum = a + b;
                    System.out.println(a + " + " + b + " = " + sum);
                    return sum;
                });
            
            combined.join();
        }
        
        public static void demonstrateErrorHandling() {
            System.out.println("\n=== Error Handling ===");
            
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                System.out.println("Attempting risky operation...");
                if (Math.random() > 0.5) {
                    throw new RuntimeException("Operation failed!");
                }
                return 100;
            }).exceptionally(ex -> {
                System.out.println("Error caught: " + ex.getMessage());
                return 0; // Fallback value
            }).thenApply(value -> {
                System.out.println("Processing value: " + value);
                return value * 2;
            });
            
            try {
                Integer result = future.get();
                System.out.println("Final result: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // Real-world example: Async HTTP request simulation
    static class AsyncHttpClient {
        private final ExecutorService executor = Executors.newFixedThreadPool(4);
        
        public CompletableFuture<String> fetchData(String url) {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("Fetching from " + url + "...");
                try {
                    Thread.sleep((long) (Math.random() * 2000 + 500));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "Data from " + url;
            }, executor);
        }
        
        public void shutdown() {
            executor.shutdown();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Future/Promise Pattern Demo ===\n");
        
        // 1. Custom Promise
        System.out.println("1. Custom Promise:");
        Promise<String> promise = new Promise<>();
        
        promise.then(result -> {
            System.out.println("✅ Success: " + result);
        }).catchError(error -> {
            System.out.println("❌ Error: " + error.getMessage());
        });
        
        // Resolve promise in another thread
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                promise.resolve("Hello from Promise!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        Thread.sleep(2000);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 2. Java's Future and CompletableFuture
        AsyncTaskExecutor.demonstrateBasicFuture();
        AsyncTaskExecutor.demonstrateCompletableFuture();
        AsyncTaskExecutor.demonstrateCombiningFutures();
        AsyncTaskExecutor.demonstrateErrorHandling();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 3. Real-world async HTTP example
        System.out.println("3. Async HTTP Client:");
        AsyncHttpClient client = new AsyncHttpClient();
        
        List<CompletableFuture<String>> futures = Arrays.asList(
            client.fetchData("api.example.com/users"),
            client.fetchData("api.example.com/posts"),
            client.fetchData("api.example.com/comments")
        );
        
        // Wait for all to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        allFutures.thenRun(() -> {
            System.out.println("\nAll requests completed!");
            futures.forEach(f -> {
                try {
                    System.out.println("  - " + f.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).join();
        
        client.shutdown();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Non-blocking async operations");
        System.out.println("✓ Composable async workflows");
        System.out.println("✓ Better resource utilization");
        System.out.println("✓ Elegant error handling");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• HTTP API calls");
        System.out.println("• Database queries");
        System.out.println("• File I/O operations");
        System.out.println("• Parallel computations");
    }
}
