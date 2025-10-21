package cloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Pipes and Filters Pattern
 * 
 * Intent: Decompose complex processing into a sequence of discrete, reusable
 * processing steps (filters) connected by channels (pipes).
 * 
 * Also Known As:
 * - Pipeline Pattern
 * - Processing Pipeline
 * - Chain of Processors
 * 
 * Motivation:
 * Complex data processing often involves multiple transformation steps.
 * Combining all logic in one component makes it:
 * - Hard to understand and maintain
 * - Difficult to test individual steps
 * - Impossible to reuse steps in different contexts
 * - Challenging to parallelize or distribute
 * 
 * Applicability:
 * - Data transformation pipelines (ETL)
 * - Stream processing
 * - Image/video processing
 * - Text processing and parsing
 * - Request/response processing
 * 
 * Benefits:
 * - Modularity and reusability
 * - Easy to understand (single responsibility)
 * - Simple to test each filter independently
 * - Flexible composition
 * - Can parallelize filters
 * - Easy to add/remove/replace filters
 * 
 * Trade-offs:
 * - Overhead of passing data between filters
 * - Complexity in error handling across pipeline
 * - State management can be challenging
 * - Debugging can be harder
 */

// Message passed through pipeline
class Message<T> {
    private final T data;
    private final Map<String, Object> metadata;
    
    public Message(T data) {
        this.data = data;
        this.metadata = new HashMap<>();
    }
    
    public Message(T data, Map<String, Object> metadata) {
        this.data = data;
        this.metadata = new HashMap<>(metadata);
    }
    
    public T getData() { return data; }
    public Map<String, Object> getMetadata() { return metadata; }
    
    public <R> Message<R> withData(R newData) {
        return new Message<>(newData, this.metadata);
    }
    
    public Message<T> withMetadata(String key, Object value) {
        metadata.put(key, value);
        return this;
    }
}

// Filter interface
interface Filter<I, O> {
    Message<O> process(Message<I> input);
}

// Pipe connects filters
class Pipe<T> {
    private final BlockingQueue<Message<T>> queue;
    
    public Pipe() {
        this.queue = new LinkedBlockingQueue<>();
    }
    
    public void send(Message<T> message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public Message<T> receive() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    public Message<T> receive(long timeout, TimeUnit unit) {
        try {
            return queue.poll(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

// Example 1: Simple Text Processing Pipeline
// Filters for text transformation
class UpperCaseFilter implements Filter<String, String> {
    @Override
    public Message<String> process(Message<String> input) {
        String result = input.getData().toUpperCase();
        System.out.println("  [UpperCase] " + input.getData() + " -> " + result);
        return input.withData(result);
    }
}

class TrimFilter implements Filter<String, String> {
    @Override
    public Message<String> process(Message<String> input) {
        String result = input.getData().trim();
        System.out.println("  [Trim] '" + input.getData() + "' -> '" + result + "'");
        return input.withData(result);
    }
}

class RemovePunctuationFilter implements Filter<String, String> {
    @Override
    public Message<String> process(Message<String> input) {
        String result = input.getData().replaceAll("[^a-zA-Z0-9\\s]", "");
        System.out.println("  [RemovePunctuation] " + input.getData() + " -> " + result);
        return input.withData(result);
    }
}

class WordCountFilter implements Filter<String, Integer> {
    @Override
    public Message<Integer> process(Message<String> input) {
        int count = input.getData().split("\\s+").length;
        System.out.println("  [WordCount] " + input.getData() + " -> " + count + " words");
        return input.withData(count);
    }
}

// Simple synchronous pipeline
class SimplePipeline {
    private final List<Filter<?, ?>> filters;
    
    public SimplePipeline() {
        this.filters = new ArrayList<>();
    }
    
    public <I, O> SimplePipeline addFilter(Filter<I, O> filter) {
        filters.add(filter);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public <I, O> O execute(I input) {
        Message<?> message = new Message<>(input);
        
        for (Filter<?, ?> filter : filters) {
            Filter<Object, Object> typedFilter = (Filter<Object, Object>) filter;
            message = typedFilter.process((Message<Object>) message);
        }
        
        return (O) message.getData();
    }
}

// Example 2: Asynchronous Pipeline
// Each filter runs in its own thread
class AsyncPipeline {
    private final List<FilterStage<?, ?>> stages;
    private final ExecutorService executor;
    
    static class FilterStage<I, O> {
        final Filter<I, O> filter;
        final Pipe<I> inputPipe;
        final Pipe<O> outputPipe;
        
        public FilterStage(Filter<I, O> filter, Pipe<I> inputPipe, Pipe<O> outputPipe) {
            this.filter = filter;
            this.inputPipe = inputPipe;
            this.outputPipe = outputPipe;
        }
    }
    
    public AsyncPipeline(ExecutorService executor) {
        this.stages = new ArrayList<>();
        this.executor = executor;
    }
    
    public <I, O> AsyncPipeline addFilter(Filter<I, O> filter, Pipe<I> inputPipe, Pipe<O> outputPipe) {
        stages.add(new FilterStage<>(filter, inputPipe, outputPipe));
        return this;
    }
    
    public void start() {
        for (FilterStage<?, ?> stage : stages) {
            executor.submit(() -> runStage(stage));
        }
    }
    
    private <I, O> void runStage(FilterStage<I, O> stage) {
        while (!Thread.currentThread().isInterrupted()) {
            Message<I> input = stage.inputPipe.receive();
            if (input == null) break;
            
            try {
                Message<O> output = stage.filter.process(input);
                if (output != null) {
                    stage.outputPipe.send(output);
                }
            } catch (Exception e) {
                System.err.println("Error in filter: " + e.getMessage());
            }
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}

// Example 3: Image Processing Pipeline
class ImageData {
    private final String name;
    private final int width;
    private final int height;
    private final String format;
    
    public ImageData(String name, int width, int height, String format) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.format = format;
    }
    
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getFormat() { return format; }
    
    @Override
    public String toString() {
        return String.format("%s (%dx%d, %s)", name, width, height, format);
    }
}

class ResizeFilter implements Filter<ImageData, ImageData> {
    private final int targetWidth;
    private final int targetHeight;
    
    public ResizeFilter(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }
    
    @Override
    public Message<ImageData> process(Message<ImageData> input) {
        ImageData original = input.getData();
        ImageData resized = new ImageData(
            original.getName(),
            targetWidth,
            targetHeight,
            original.getFormat()
        );
        
        System.out.println("  [Resize] " + original.getWidth() + "x" + original.getHeight() + 
            " -> " + targetWidth + "x" + targetHeight);
        
        return input.withData(resized)
            .withMetadata("resized", true)
            .withMetadata("originalSize", original.getWidth() + "x" + original.getHeight());
    }
}

class ConvertFormatFilter implements Filter<ImageData, ImageData> {
    private final String targetFormat;
    
    public ConvertFormatFilter(String targetFormat) {
        this.targetFormat = targetFormat;
    }
    
    @Override
    public Message<ImageData> process(Message<ImageData> input) {
        ImageData original = input.getData();
        ImageData converted = new ImageData(
            original.getName(),
            original.getWidth(),
            original.getHeight(),
            targetFormat
        );
        
        System.out.println("  [Convert] " + original.getFormat() + " -> " + targetFormat);
        
        return input.withData(converted)
            .withMetadata("converted", true)
            .withMetadata("originalFormat", original.getFormat());
    }
}

class WatermarkFilter implements Filter<ImageData, ImageData> {
    private final String watermarkText;
    
    public WatermarkFilter(String watermarkText) {
        this.watermarkText = watermarkText;
    }
    
    @Override
    public Message<ImageData> process(Message<ImageData> input) {
        System.out.println("  [Watermark] Adding: '" + watermarkText + "'");
        return input.withMetadata("watermark", watermarkText);
    }
}

// Example 4: Data Validation Pipeline
class DataRecord {
    private final Map<String, Object> fields;
    
    public DataRecord() {
        this.fields = new HashMap<>();
    }
    
    public DataRecord(Map<String, Object> fields) {
        this.fields = new HashMap<>(fields);
    }
    
    public void setField(String name, Object value) {
        fields.put(name, value);
    }
    
    public Object getField(String name) {
        return fields.get(name);
    }
    
    public Map<String, Object> getFields() {
        return new HashMap<>(fields);
    }
    
    @Override
    public String toString() {
        return fields.toString();
    }
}

class ValidationFilter implements Filter<DataRecord, DataRecord> {
    private final String fieldName;
    private final Function<Object, Boolean> validator;
    
    public ValidationFilter(String fieldName, Function<Object, Boolean> validator) {
        this.fieldName = fieldName;
        this.validator = validator;
    }
    
    @Override
    public Message<DataRecord> process(Message<DataRecord> input) {
        DataRecord record = input.getData();
        Object value = record.getField(fieldName);
        
        boolean isValid = validator.apply(value);
        
        System.out.println("  [Validate:" + fieldName + "] " + value + " -> " + 
            (isValid ? "✓ VALID" : "✗ INVALID"));
        
        input.withMetadata("valid_" + fieldName, isValid);
        
        if (!isValid) {
            input.withMetadata("validation_failed", true);
        }
        
        return input;
    }
}

class TransformationFilter implements Filter<DataRecord, DataRecord> {
    private final String fieldName;
    private final Function<Object, Object> transformer;
    
    public TransformationFilter(String fieldName, Function<Object, Object> transformer) {
        this.fieldName = fieldName;
        this.transformer = transformer;
    }
    
    @Override
    public Message<DataRecord> process(Message<DataRecord> input) {
        DataRecord record = input.getData();
        Object oldValue = record.getField(fieldName);
        Object newValue = transformer.apply(oldValue);
        
        record.setField(fieldName, newValue);
        
        System.out.println("  [Transform:" + fieldName + "] " + oldValue + " -> " + newValue);
        
        return input;
    }
}

class EnrichmentFilter implements Filter<DataRecord, DataRecord> {
    private final String newFieldName;
    private final Function<DataRecord, Object> enricher;
    
    public EnrichmentFilter(String newFieldName, Function<DataRecord, Object> enricher) {
        this.newFieldName = newFieldName;
        this.enricher = enricher;
    }
    
    @Override
    public Message<DataRecord> process(Message<DataRecord> input) {
        DataRecord record = input.getData();
        Object enrichedValue = enricher.apply(record);
        
        record.setField(newFieldName, enrichedValue);
        
        System.out.println("  [Enrich] Added field: " + newFieldName + " = " + enrichedValue);
        
        return input;
    }
}

// Example 5: ETL Pipeline
class ETLPipeline<T, R> {
    private final List<Filter<?, ?>> extractors;
    private final List<Filter<?, ?>> transformers;
    private final List<Filter<?, ?>> loaders;
    
    public ETLPipeline() {
        this.extractors = new ArrayList<>();
        this.transformers = new ArrayList<>();
        this.loaders = new ArrayList<>();
    }
    
    public <I, O> ETLPipeline<T, R> addExtractor(Filter<I, O> filter) {
        extractors.add(filter);
        return this;
    }
    
    public <I, O> ETLPipeline<T, R> addTransformer(Filter<I, O> filter) {
        transformers.add(filter);
        return this;
    }
    
    public <I, O> ETLPipeline<T, R> addLoader(Filter<I, O> filter) {
        loaders.add(filter);
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public R execute(T input) {
        System.out.println("Starting ETL Pipeline...");
        
        // Extract
        System.out.println("\n--- EXTRACT Phase ---");
        Message<?> message = new Message<>(input);
        message = processFilters(extractors, message);
        
        // Transform
        System.out.println("\n--- TRANSFORM Phase ---");
        message = processFilters(transformers, message);
        
        // Load
        System.out.println("\n--- LOAD Phase ---");
        message = processFilters(loaders, message);
        
        System.out.println("\nETL Pipeline Complete!");
        return (R) message.getData();
    }
    
    @SuppressWarnings("unchecked")
    private Message<?> processFilters(List<Filter<?, ?>> filters, Message<?> message) {
        for (Filter<?, ?> filter : filters) {
            Filter<Object, Object> typedFilter = (Filter<Object, Object>) filter;
            message = typedFilter.process((Message<Object>) message);
        }
        return message;
    }
}

// Demo
public class PipesAndFiltersPattern {
    public static void main(String[] args) throws InterruptedException {
        demonstrateSimpleTextPipeline();
        demonstrateImageProcessingPipeline();
        demonstrateDataValidationPipeline();
        demonstrateETLPipeline();
        demonstrateAsyncPipeline();
    }
    
    private static void demonstrateSimpleTextPipeline() {
        System.out.println("=== Simple Text Processing Pipeline ===\n");
        
        SimplePipeline pipeline = new SimplePipeline()
            .addFilter(new TrimFilter())
            .addFilter(new UpperCaseFilter())
            .addFilter(new RemovePunctuationFilter())
            .addFilter(new WordCountFilter());
        
        String input = "  Hello, World! How are you?  ";
        System.out.println("Input: '" + input + "'");
        
        Integer result = pipeline.execute(input);
        System.out.println("\nFinal result: " + result + " words");
    }
    
    private static void demonstrateImageProcessingPipeline() {
        System.out.println("\n\n=== Image Processing Pipeline ===\n");
        
        SimplePipeline pipeline = new SimplePipeline()
            .addFilter(new ResizeFilter(800, 600))
            .addFilter(new ConvertFormatFilter("JPEG"))
            .addFilter(new WatermarkFilter("© 2025 Company"));
        
        ImageData input = new ImageData("photo.png", 4000, 3000, "PNG");
        System.out.println("Input: " + input);
        
        ImageData result = pipeline.execute(input);
        System.out.println("\nFinal result: " + result);
    }
    
    private static void demonstrateDataValidationPipeline() {
        System.out.println("\n\n=== Data Validation Pipeline ===\n");
        
        SimplePipeline pipeline = new SimplePipeline()
            .addFilter(new ValidationFilter("email", 
                v -> v != null && v.toString().contains("@")))
            .addFilter(new ValidationFilter("age", 
                v -> v != null && (Integer)v >= 18 && (Integer)v <= 120))
            .addFilter(new TransformationFilter("name", 
                v -> v.toString().toUpperCase()))
            .addFilter(new EnrichmentFilter("status", 
                record -> "PROCESSED"));
        
        DataRecord record = new DataRecord();
        record.setField("name", "john doe");
        record.setField("email", "john@example.com");
        record.setField("age", 25);
        
        System.out.println("Input: " + record);
        
        DataRecord result = pipeline.execute(record);
        System.out.println("\nFinal result: " + result);
    }
    
    private static void demonstrateETLPipeline() {
        System.out.println("\n\n=== ETL Pipeline ===\n");
        
        // Simulating CSV to database ETL
        ETLPipeline<String, DataRecord> etl = new ETLPipeline<>();
        
        // Extract: Parse CSV
        etl.addExtractor(new Filter<String, DataRecord>() {
            @Override
            public Message<DataRecord> process(Message<String> input) {
                String[] parts = input.getData().split(",");
                DataRecord record = new DataRecord();
                record.setField("id", parts[0]);
                record.setField("name", parts[1]);
                record.setField("value", parts[2]);
                System.out.println("  [Parse CSV] " + input.getData() + " -> " + record);
                return input.withData(record);
            }
        });
        
        // Transform: Clean and validate
        etl.addTransformer(new TransformationFilter("name", v -> v.toString().trim().toUpperCase()));
        etl.addTransformer(new ValidationFilter("value", v -> {
            try {
                Double.parseDouble(v.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }));
        
        // Transform: Enrich with timestamp
        etl.addTransformer(new EnrichmentFilter("timestamp", 
            r -> System.currentTimeMillis()));
        
        // Load: Save to database (simulated)
        etl.addLoader(new Filter<DataRecord, DataRecord>() {
            @Override
            public Message<DataRecord> process(Message<DataRecord> input) {
                System.out.println("  [Save to DB] " + input.getData());
                return input;
            }
        });
        
        String csvInput = "123,john doe,45.67";
        etl.execute(csvInput);
    }
    
    private static void demonstrateAsyncPipeline() throws InterruptedException {
        System.out.println("\n\n=== Asynchronous Pipeline ===\n");
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Create pipes
        Pipe<String> inputPipe = new Pipe<>();
        Pipe<String> pipe1 = new Pipe<>();
        Pipe<String> pipe2 = new Pipe<>();
        Pipe<Integer> outputPipe = new Pipe<>();
        
        // Create async pipeline
        AsyncPipeline pipeline = new AsyncPipeline(executor)
            .addFilter(new TrimFilter(), inputPipe, pipe1)
            .addFilter(new UpperCaseFilter(), pipe1, pipe2)
            .addFilter(new WordCountFilter(), pipe2, outputPipe);
        
        // Start pipeline
        pipeline.start();
        
        // Send messages
        System.out.println("Sending messages to async pipeline...\n");
        inputPipe.send(new Message<>("  hello world  "));
        inputPipe.send(new Message<>("  pipes and filters  "));
        inputPipe.send(new Message<>("  asynchronous processing  "));
        
        // Receive results
        System.out.println("\nReceiving results...");
        for (int i = 0; i < 3; i++) {
            Message<Integer> result = outputPipe.receive(2, TimeUnit.SECONDS);
            if (result != null) {
                System.out.println("Result " + (i + 1) + ": " + result.getData() + " words");
            }
        }
        
        pipeline.shutdown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
