package additional;

import java.util.*;
import java.util.function.*;

/**
 * Callback Pattern
 * Passes executable code as a parameter to be called back later.
 */
public class CallbackPattern {
    
    // Callback interface
    interface Callback<T> {
        void onComplete(T result);
        void onError(Exception error);
    }
    
    // Simple callback interface
    interface SimpleCallback {
        void execute();
    }
    
    // Async Task with callback
    static class AsyncTask<T> {
        private final Supplier<T> task;
        
        public AsyncTask(Supplier<T> task) {
            this.task = task;
        }
        
        public void execute(Callback<T> callback) {
            new Thread(() -> {
                try {
                    System.out.println("  ⚙️  Task executing in background...");
                    Thread.sleep(1000); // Simulate work
                    T result = task.get();
                    callback.onComplete(result);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }).start();
        }
    }
    
    // File downloader with callbacks
    static class FileDownloader {
        public void download(String url, Callback<String> callback) {
            System.out.println("📥 Starting download: " + url);
            
            new Thread(() -> {
                try {
                    // Simulate download
                    Thread.sleep(1500);
                    
                    if (Math.random() > 0.8) {
                        throw new Exception("Download failed: Connection timeout");
                    }
                    
                    String content = "File content from " + url;
                    callback.onComplete(content);
                    
                } catch (Exception e) {
                    callback.onError(e);
                }
            }).start();
        }
    }
    
    // Event system with callbacks
    static class Button {
        private final String label;
        private final List<SimpleCallback> clickHandlers = new ArrayList<>();
        
        public Button(String label) {
            this.label = label;
        }
        
        public void onClick(SimpleCallback handler) {
            clickHandlers.add(handler);
        }
        
        public void click() {
            System.out.println("\n🖱️  Button clicked: " + label);
            for (SimpleCallback handler : clickHandlers) {
                handler.execute();
            }
        }
    }
    
    // HTTP Client with callbacks
    static class HttpClient {
        public void get(String url, Callback<String> callback) {
            System.out.println("📡 GET " + url);
            
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    String response = "{\"status\": \"success\", \"data\": \"Response from " + url + "\"}";
                    callback.onComplete(response);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }).start();
        }
        
        public void post(String url, String data, Callback<String> callback) {
            System.out.println("📡 POST " + url + " with data: " + data);
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    String response = "{\"status\": \"created\", \"id\": \"12345\"}";
                    callback.onComplete(response);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }).start();
        }
    }
    
    // Data processor with progress callbacks
    interface ProgressCallback {
        void onProgress(int percentage, String message);
        void onComplete();
    }
    
    static class DataProcessor {
        public void processData(List<String> items, ProgressCallback callback) {
            new Thread(() -> {
                System.out.println("\n🔄 Processing " + items.size() + " items...");
                
                for (int i = 0; i < items.size(); i++) {
                    try {
                        Thread.sleep(500);
                        int percentage = (int) ((i + 1) * 100.0 / items.size());
                        callback.onProgress(percentage, "Processing: " + items.get(i));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                callback.onComplete();
            }).start();
        }
    }
    
    // Chained callbacks
    static class ChainedCallbackExample {
        public static void step1(Callback<String> callback) {
            new Thread(() -> {
                try {
                    System.out.println("  Step 1: Fetching user data...");
                    Thread.sleep(500);
                    callback.onComplete("user123");
                } catch (Exception e) {
                    callback.onError(e);
                }
            }).start();
        }
        
        public static void step2(String userId, Callback<String> callback) {
            new Thread(() -> {
                try {
                    System.out.println("  Step 2: Fetching orders for " + userId + "...");
                    Thread.sleep(500);
                    callback.onComplete("order456");
                } catch (Exception e) {
                    callback.onError(e);
                }
            }).start();
        }
        
        public static void step3(String orderId, Callback<String> callback) {
            new Thread(() -> {
                try {
                    System.out.println("  Step 3: Processing payment for " + orderId + "...");
                    Thread.sleep(500);
                    callback.onComplete("payment789");
                } catch (Exception e) {
                    callback.onError(e);
                }
            }).start();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Callback Pattern Demo ===\n");
        
        // 1. Basic async task with callback
        System.out.println("1. Async Task with Callback:");
        AsyncTask<Integer> task = new AsyncTask<>(() -> {
            return 42;
        });
        
        task.execute(new Callback<Integer>() {
            @Override
            public void onComplete(Integer result) {
                System.out.println("  ✅ Task completed! Result: " + result);
            }
            
            @Override
            public void onError(Exception error) {
                System.out.println("  ❌ Task failed: " + error.getMessage());
            }
        });
        
        Thread.sleep(1500);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 2. File downloader
        System.out.println("\n2. File Download with Callbacks:");
        FileDownloader downloader = new FileDownloader();
        
        downloader.download("https://example.com/file.pdf", new Callback<String>() {
            @Override
            public void onComplete(String content) {
                System.out.println("  ✅ Download complete!");
                System.out.println("  📄 Content: " + content);
            }
            
            @Override
            public void onError(Exception error) {
                System.out.println("  ❌ Download failed: " + error.getMessage());
            }
        });
        
        Thread.sleep(2000);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 3. Button click callbacks
        System.out.println("\n3. Event Callbacks:");
        Button saveButton = new Button("Save");
        
        saveButton.onClick(() -> {
            System.out.println("  💾 Handler 1: Saving data...");
        });
        
        saveButton.onClick(() -> {
            System.out.println("  📧 Handler 2: Sending notification...");
        });
        
        saveButton.onClick(() -> {
            System.out.println("  📊 Handler 3: Logging event...");
        });
        
        saveButton.click();
        
        System.out.println("\n" + "=".repeat(50));
        
        // 4. HTTP Client callbacks
        System.out.println("\n4. HTTP Client Callbacks:");
        HttpClient client = new HttpClient();
        
        client.get("https://api.example.com/users/1", new Callback<String>() {
            @Override
            public void onComplete(String response) {
                System.out.println("  ✅ GET Response: " + response);
            }
            
            @Override
            public void onError(Exception error) {
                System.out.println("  ❌ GET Error: " + error.getMessage());
            }
        });
        
        Thread.sleep(1000);
        
        client.post("https://api.example.com/users", "{\"name\": \"John\"}", new Callback<String>() {
            @Override
            public void onComplete(String response) {
                System.out.println("  ✅ POST Response: " + response);
            }
            
            @Override
            public void onError(Exception error) {
                System.out.println("  ❌ POST Error: " + error.getMessage());
            }
        });
        
        Thread.sleep(1500);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 5. Progress callbacks
        System.out.println("\n5. Progress Callbacks:");
        DataProcessor processor = new DataProcessor();
        List<String> items = Arrays.asList("Item1", "Item2", "Item3", "Item4", "Item5");
        
        processor.processData(items, new ProgressCallback() {
            @Override
            public void onProgress(int percentage, String message) {
                System.out.println("  ⏳ " + percentage + "% - " + message);
            }
            
            @Override
            public void onComplete() {
                System.out.println("  ✅ All items processed!");
            }
        });
        
        Thread.sleep(3000);
        
        System.out.println("\n" + "=".repeat(50));
        
        // 6. Chained callbacks (Callback Hell example)
        System.out.println("\n6. Chained Callbacks:");
        ChainedCallbackExample.step1(new Callback<String>() {
            @Override
            public void onComplete(String userId) {
                System.out.println("  ✅ Got user: " + userId);
                
                ChainedCallbackExample.step2(userId, new Callback<String>() {
                    @Override
                    public void onComplete(String orderId) {
                        System.out.println("  ✅ Got order: " + orderId);
                        
                        ChainedCallbackExample.step3(orderId, new Callback<String>() {
                            @Override
                            public void onComplete(String paymentId) {
                                System.out.println("  ✅ Payment processed: " + paymentId);
                                System.out.println("  🎉 All steps completed!");
                            }
                            
                            @Override
                            public void onError(Exception error) {
                                System.out.println("  ❌ Step 3 failed: " + error.getMessage());
                            }
                        });
                    }
                    
                    @Override
                    public void onError(Exception error) {
                        System.out.println("  ❌ Step 2 failed: " + error.getMessage());
                    }
                });
            }
            
            @Override
            public void onError(Exception error) {
                System.out.println("  ❌ Step 1 failed: " + error.getMessage());
            }
        });
        
        Thread.sleep(2000);
        
        System.out.println("\n--- Callback Pattern ---");
        System.out.println("Function passed as argument to be 'called back' later");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Asynchronous operation handling");
        System.out.println("✓ Event-driven programming");
        System.out.println("✓ Decoupling");
        System.out.println("✓ Flexible control flow");
        
        System.out.println("\n--- Drawbacks ---");
        System.out.println("❌ Callback hell (nested callbacks)");
        System.out.println("❌ Error handling complexity");
        System.out.println("❌ Hard to read/maintain");
        
        System.out.println("\n--- Modern Alternatives ---");
        System.out.println("• Promises/Futures");
        System.out.println("• Async/Await");
        System.out.println("• Reactive Streams (RxJava)");
        System.out.println("• CompletableFuture (Java 8+)");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Async I/O operations");
        System.out.println("• Event listeners");
        System.out.println("• GUI callbacks");
        System.out.println("• Timer/Scheduler tasks");
        System.out.println("• HTTP request/response");
    }
}
