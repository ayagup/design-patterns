package architectural;

import java.util.*;
import java.util.function.*;

/**
 * Pipe and Filter Architecture Pattern
 * =====================================
 * 
 * Intent:
 * Processes data through a series of independent processing steps (filters)
 * connected by pipes. Each filter transforms data and passes it to the next
 * filter, creating a processing pipeline.
 * 
 * Also Known As:
 * - Pipeline Pattern
 * - Chain of Processors
 * 
 * Motivation:
 * - Break down complex processing into simple, reusable stages
 * - Enable parallel processing of independent filters
 * - Make processing steps independently testable
 * - Allow dynamic reconfiguration of processing pipeline
 * 
 * Applicability:
 * - Data transformation and processing systems
 * - Image/video/audio processing pipelines
 * - Text processing and parsing
 * - ETL (Extract, Transform, Load) operations
 * - Compiler design (lexer -> parser -> optimizer -> code gen)
 * 
 * Structure:
 * Input -> Filter1 -> Pipe -> Filter2 -> Pipe -> Filter3 -> Output
 * 
 * Participants:
 * - Filter: Independent processing component
 * - Pipe: Connector between filters (passes data)
 * - Pipeline: Orchestrates the entire process
 * 
 * Benefits:
 * - Flexibility: Easy to add/remove/reorder filters
 * - Reusability: Filters can be reused in different pipelines
 * - Concurrency: Independent filters can run in parallel
 * - Testability: Each filter can be tested in isolation
 */

// ============================================================================
// FILTER INTERFACE
// ============================================================================

@FunctionalInterface
interface Filter<T> {
    T process(T input);
}

// ============================================================================
// PIPELINE IMPLEMENTATION
// ============================================================================

class Pipeline<T> {
    private final List<Filter<T>> filters = new ArrayList<>();
    private final String name;
    
    public Pipeline(String name) {
        this.name = name;
    }
    
    public Pipeline<T> addFilter(Filter<T> filter) {
        filters.add(filter);
        return this;
    }
    
    public T execute(T input) {
        System.out.println("[Pipeline: " + name + "] Starting execution");
        T current = input;
        
        for (int i = 0; i < filters.size(); i++) {
            System.out.println("[Pipeline: " + name + "] Stage " + (i + 1) + "/" + filters.size());
            current = filters.get(i).process(current);
        }
        
        System.out.println("[Pipeline: " + name + "] Completed");
        return current;
    }
    
    public int getFilterCount() {
        return filters.size();
    }
}

// ============================================================================
// EXAMPLE 1: TEXT PROCESSING PIPELINE
// ============================================================================

class TextProcessingFilters {
    
    // Filter: Remove extra whitespace
    static class TrimWhitespaceFilter implements Filter<String> {
        @Override
        public String process(String input) {
            System.out.println("  [TrimWhitespace] Processing...");
            String result = input.trim().replaceAll("\\s+", " ");
            System.out.println("  [TrimWhitespace] '" + input + "' -> '" + result + "'");
            return result;
        }
    }
    
    // Filter: Convert to lowercase
    static class LowercaseFilter implements Filter<String> {
        @Override
        public String process(String input) {
            System.out.println("  [Lowercase] Processing...");
            String result = input.toLowerCase();
            System.out.println("  [Lowercase] '" + input + "' -> '" + result + "'");
            return result;
        }
    }
    
    // Filter: Remove punctuation
    static class RemovePunctuationFilter implements Filter<String> {
        @Override
        public String process(String input) {
            System.out.println("  [RemovePunctuation] Processing...");
            String result = input.replaceAll("[^a-zA-Z0-9\\s]", "");
            System.out.println("  [RemovePunctuation] '" + input + "' -> '" + result + "'");
            return result;
        }
    }
    
    // Filter: Remove stop words
    static class RemoveStopWordsFilter implements Filter<String> {
        private static final Set<String> STOP_WORDS = new HashSet<>(
            Arrays.asList("the", "a", "an", "and", "or", "but", "is", "are", "was", "were")
        );
        
        @Override
        public String process(String input) {
            System.out.println("  [RemoveStopWords] Processing...");
            String[] words = input.split("\\s+");
            StringBuilder result = new StringBuilder();
            
            for (String word : words) {
                if (!STOP_WORDS.contains(word.toLowerCase())) {
                    if (result.length() > 0) result.append(" ");
                    result.append(word);
                }
            }
            
            System.out.println("  [RemoveStopWords] '" + input + "' -> '" + result + "'");
            return result.toString();
        }
    }
}

// ============================================================================
// EXAMPLE 2: IMAGE PROCESSING PIPELINE
// ============================================================================

class Image {
    private String name;
    private int width;
    private int height;
    private double brightness;
    private double contrast;
    
    public Image(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.brightness = 1.0;
        this.contrast = 1.0;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getBrightness() { return brightness; }
    public double getContrast() { return contrast; }
    
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setBrightness(double brightness) { this.brightness = brightness; }
    public void setContrast(double contrast) { this.contrast = contrast; }
    
    @Override
    public String toString() {
        return String.format("Image{name='%s', %dx%d, brightness=%.2f, contrast=%.2f}",
                           name, width, height, brightness, contrast);
    }
}

class ImageProcessingFilters {
    
    // Filter: Resize image
    static class ResizeFilter implements Filter<Image> {
        private final int targetWidth;
        private final int targetHeight;
        
        public ResizeFilter(int targetWidth, int targetHeight) {
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
        }
        
        @Override
        public Image process(Image input) {
            System.out.println("  [Resize] " + input.getWidth() + "x" + input.getHeight() + 
                             " -> " + targetWidth + "x" + targetHeight);
            input.setWidth(targetWidth);
            input.setHeight(targetHeight);
            return input;
        }
    }
    
    // Filter: Adjust brightness
    static class BrightnessFilter implements Filter<Image> {
        private final double adjustment;
        
        public BrightnessFilter(double adjustment) {
            this.adjustment = adjustment;
        }
        
        @Override
        public Image process(Image input) {
            double newBrightness = input.getBrightness() * adjustment;
            System.out.println("  [Brightness] " + input.getBrightness() + " -> " + newBrightness);
            input.setBrightness(newBrightness);
            return input;
        }
    }
    
    // Filter: Adjust contrast
    static class ContrastFilter implements Filter<Image> {
        private final double adjustment;
        
        public ContrastFilter(double adjustment) {
            this.adjustment = adjustment;
        }
        
        @Override
        public Image process(Image input) {
            double newContrast = input.getContrast() * adjustment;
            System.out.println("  [Contrast] " + input.getContrast() + " -> " + newContrast);
            input.setContrast(newContrast);
            return input;
        }
    }
    
    // Filter: Add watermark
    static class WatermarkFilter implements Filter<Image> {
        private final String watermarkText;
        
        public WatermarkFilter(String watermarkText) {
            this.watermarkText = watermarkText;
        }
        
        @Override
        public Image process(Image input) {
            System.out.println("  [Watermark] Adding: '" + watermarkText + "'");
            // In real implementation, would add watermark to image data
            return input;
        }
    }
}

// ============================================================================
// EXAMPLE 3: DATA VALIDATION AND TRANSFORMATION PIPELINE
// ============================================================================

class DataRecord {
    private Map<String, String> fields = new HashMap<>();
    
    public void set(String key, String value) {
        fields.put(key, value);
    }
    
    public String get(String key) {
        return fields.get(key);
    }
    
    public boolean has(String key) {
        return fields.containsKey(key);
    }
    
    @Override
    public String toString() {
        return "DataRecord" + fields;
    }
}

class DataProcessingFilters {
    
    // Filter: Validate required fields
    static class ValidationFilter implements Filter<DataRecord> {
        private final String[] requiredFields;
        
        public ValidationFilter(String... requiredFields) {
            this.requiredFields = requiredFields;
        }
        
        @Override
        public DataRecord process(DataRecord input) {
            System.out.println("  [Validation] Checking required fields...");
            for (String field : requiredFields) {
                if (!input.has(field) || input.get(field).isEmpty()) {
                    throw new IllegalArgumentException("Missing required field: " + field);
                }
            }
            System.out.println("  [Validation] All required fields present");
            return input;
        }
    }
    
    // Filter: Normalize data
    static class NormalizationFilter implements Filter<DataRecord> {
        @Override
        public DataRecord process(DataRecord input) {
            System.out.println("  [Normalization] Normalizing data...");
            
            // Trim whitespace from all fields
            if (input.has("name")) {
                input.set("name", input.get("name").trim());
            }
            
            // Normalize email to lowercase
            if (input.has("email")) {
                input.set("email", input.get("email").toLowerCase().trim());
            }
            
            // Format phone number
            if (input.has("phone")) {
                String phone = input.get("phone").replaceAll("[^0-9]", "");
                input.set("phone", phone);
            }
            
            System.out.println("  [Normalization] Data normalized");
            return input;
        }
    }
    
    // Filter: Enrich data
    static class EnrichmentFilter implements Filter<DataRecord> {
        @Override
        public DataRecord process(DataRecord input) {
            System.out.println("  [Enrichment] Enriching data...");
            
            // Add timestamp
            input.set("processed_at", String.valueOf(System.currentTimeMillis()));
            
            // Add derived field
            if (input.has("first_name") && input.has("last_name")) {
                input.set("full_name", input.get("first_name") + " " + input.get("last_name"));
            }
            
            System.out.println("  [Enrichment] Data enriched");
            return input;
        }
    }
    
    // Filter: Transform to specific format
    static class TransformationFilter implements Filter<DataRecord> {
        @Override
        public DataRecord process(DataRecord input) {
            System.out.println("  [Transformation] Transforming data...");
            
            // Convert name to title case
            if (input.has("name")) {
                String name = input.get("name");
                String titleCase = Arrays.stream(name.split("\\s+"))
                                        .map(word -> Character.toUpperCase(word.charAt(0)) + 
                                                   word.substring(1).toLowerCase())
                                        .reduce((a, b) -> a + " " + b)
                                        .orElse(name);
                input.set("name", titleCase);
            }
            
            System.out.println("  [Transformation] Data transformed");
            return input;
        }
    }
}

// ============================================================================
// EXAMPLE 4: COMPOSITE FILTERS
// ============================================================================

class CompositeFilter<T> implements Filter<T> {
    private final List<Filter<T>> filters = new ArrayList<>();
    private final String name;
    
    public CompositeFilter(String name) {
        this.name = name;
    }
    
    public CompositeFilter<T> add(Filter<T> filter) {
        filters.add(filter);
        return this;
    }
    
    @Override
    public T process(T input) {
        System.out.println("  [Composite: " + name + "] Processing with " + filters.size() + " sub-filters");
        T current = input;
        for (Filter<T> filter : filters) {
            current = filter.process(current);
        }
        return current;
    }
}

/**
 * Demonstration of Pipe and Filter Architecture Pattern
 */
public class PipesAndFiltersPattern {
    public static void main(String[] args) {
        demonstrateTextProcessing();
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demonstrateImageProcessing();
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demonstrateDataProcessing();
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demonstrateCompositeFilters();
    }
    
    private static void demonstrateTextProcessing() {
        System.out.println("=== Pipe and Filter: Text Processing Pipeline ===\n");
        
        Pipeline<String> pipeline = new Pipeline<>("Text Cleaner");
        pipeline.addFilter(new TextProcessingFilters.TrimWhitespaceFilter())
                .addFilter(new TextProcessingFilters.LowercaseFilter())
                .addFilter(new TextProcessingFilters.RemovePunctuationFilter())
                .addFilter(new TextProcessingFilters.RemoveStopWordsFilter());
        
        String input = "   The  quick   brown fox, jumps over the lazy dog!  ";
        System.out.println("Input: '" + input + "'\n");
        
        String output = pipeline.execute(input);
        
        System.out.println("\nFinal Output: '" + output + "'");
    }
    
    private static void demonstrateImageProcessing() {
        System.out.println("=== Pipe and Filter: Image Processing Pipeline ===\n");
        
        Pipeline<Image> pipeline = new Pipeline<>("Photo Editor");
        pipeline.addFilter(new ImageProcessingFilters.ResizeFilter(800, 600))
                .addFilter(new ImageProcessingFilters.BrightnessFilter(1.2))
                .addFilter(new ImageProcessingFilters.ContrastFilter(1.1))
                .addFilter(new ImageProcessingFilters.WatermarkFilter("© 2025 MyCompany"));
        
        Image image = new Image("photo.jpg", 1920, 1080);
        System.out.println("Input: " + image + "\n");
        
        Image processed = pipeline.execute(image);
        
        System.out.println("\nFinal Output: " + processed);
    }
    
    private static void demonstrateDataProcessing() {
        System.out.println("=== Pipe and Filter: Data Processing Pipeline ===\n");
        
        Pipeline<DataRecord> pipeline = new Pipeline<>("ETL Pipeline");
        pipeline.addFilter(new DataProcessingFilters.ValidationFilter("name", "email"))
                .addFilter(new DataProcessingFilters.NormalizationFilter())
                .addFilter(new DataProcessingFilters.EnrichmentFilter())
                .addFilter(new DataProcessingFilters.TransformationFilter());
        
        DataRecord record = new DataRecord();
        record.set("name", "  john doe  ");
        record.set("email", "  JOHN.DOE@EXAMPLE.COM  ");
        record.set("phone", "(555) 123-4567");
        
        System.out.println("Input: " + record + "\n");
        
        DataRecord processed = pipeline.execute(record);
        
        System.out.println("\nFinal Output: " + processed);
    }
    
    private static void demonstrateCompositeFilters() {
        System.out.println("=== Pipe and Filter: Composite Filters ===\n");
        
        // Create a composite filter for text cleaning
        CompositeFilter<String> cleaningComposite = new CompositeFilter<>("Text Cleaning");
        cleaningComposite.add(new TextProcessingFilters.TrimWhitespaceFilter())
                        .add(new TextProcessingFilters.RemovePunctuationFilter());
        
        // Create a composite filter for text normalization
        CompositeFilter<String> normalizationComposite = new CompositeFilter<>("Text Normalization");
        normalizationComposite.add(new TextProcessingFilters.LowercaseFilter())
                             .add(new TextProcessingFilters.RemoveStopWordsFilter());
        
        // Use composites in main pipeline
        Pipeline<String> pipeline = new Pipeline<>("Composite Text Processor");
        pipeline.addFilter(cleaningComposite)
                .addFilter(normalizationComposite);
        
        String input = "  Hello, World!   The quick brown fox.  ";
        System.out.println("Input: '" + input + "'\n");
        
        String output = pipeline.execute(input);
        
        System.out.println("\nFinal Output: '" + output + "'");
    }
}
