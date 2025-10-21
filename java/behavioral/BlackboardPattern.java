package behavioral;

import java.util.*;

/**
 * Blackboard Pattern
 * Multiple specialized subsystems contribute to a solution.
 */
public class BlackboardPattern {
    
    // Blackboard - shared knowledge base
    static class Blackboard {
        private Map<String, Object> data = new HashMap<>();
        private List<String> log = new ArrayList<>();
        
        public void put(String key, Object value) {
            data.put(key, value);
            log.add("Added: " + key + " = " + value);
        }
        
        public Object get(String key) {
            return data.get(key);
        }
        
        public boolean contains(String key) {
            return data.containsKey(key);
        }
        
        public void showLog() {
            System.out.println("\n=== Blackboard Log ===");
            log.forEach(System.out::println);
        }
        
        public void clear() {
            data.clear();
            log.clear();
        }
    }
    
    // Knowledge Source interface
    interface KnowledgeSource {
        boolean canContribute(Blackboard blackboard);
        void contribute(Blackboard blackboard);
        String getName();
    }
    
    // Concrete Knowledge Sources for Math Problem Solving
    static class AdditionSolver implements KnowledgeSource {
        @Override
        public boolean canContribute(Blackboard blackboard) {
            return blackboard.contains("operation") && 
                   "add".equals(blackboard.get("operation")) &&
                   blackboard.contains("operand1") && 
                   blackboard.contains("operand2") &&
                   !blackboard.contains("result");
        }
        
        @Override
        public void contribute(Blackboard blackboard) {
            int a = (int) blackboard.get("operand1");
            int b = (int) blackboard.get("operand2");
            int result = a + b;
            blackboard.put("result", result);
            System.out.println("➕ AdditionSolver: " + a + " + " + b + " = " + result);
        }
        
        @Override
        public String getName() {
            return "AdditionSolver";
        }
    }
    
    static class MultiplicationSolver implements KnowledgeSource {
        @Override
        public boolean canContribute(Blackboard blackboard) {
            return blackboard.contains("operation") && 
                   "multiply".equals(blackboard.get("operation")) &&
                   blackboard.contains("operand1") && 
                   blackboard.contains("operand2") &&
                   !blackboard.contains("result");
        }
        
        @Override
        public void contribute(Blackboard blackboard) {
            int a = (int) blackboard.get("operand1");
            int b = (int) blackboard.get("operand2");
            int result = a * b;
            blackboard.put("result", result);
            System.out.println("✖️  MultiplicationSolver: " + a + " × " + b + " = " + result);
        }
        
        @Override
        public String getName() {
            return "MultiplicationSolver";
        }
    }
    
    static class ResultFormatter implements KnowledgeSource {
        @Override
        public boolean canContribute(Blackboard blackboard) {
            return blackboard.contains("result") && 
                   !blackboard.contains("formatted_result");
        }
        
        @Override
        public void contribute(Blackboard blackboard) {
            int result = (int) blackboard.get("result");
            String formatted = "The answer is: " + result;
            blackboard.put("formatted_result", formatted);
            System.out.println("📝 ResultFormatter: " + formatted);
        }
        
        @Override
        public String getName() {
            return "ResultFormatter";
        }
    }
    
    // Control component
    static class Controller {
        private Blackboard blackboard;
        private List<KnowledgeSource> sources;
        
        public Controller(Blackboard blackboard) {
            this.blackboard = blackboard;
            this.sources = new ArrayList<>();
        }
        
        public void addKnowledgeSource(KnowledgeSource source) {
            sources.add(source);
            System.out.println("Registered: " + source.getName());
        }
        
        public void solve() {
            System.out.println("\n🔄 Starting problem-solving process...\n");
            boolean progress = true;
            int iterations = 0;
            int maxIterations = 10;
            
            while (progress && iterations < maxIterations) {
                progress = false;
                iterations++;
                
                for (KnowledgeSource source : sources) {
                    if (source.canContribute(blackboard)) {
                        source.contribute(blackboard);
                        progress = true;
                    }
                }
                
                if (!progress) {
                    System.out.println("\n✅ No more contributions possible");
                }
            }
            
            if (iterations >= maxIterations) {
                System.out.println("\n⚠️  Max iterations reached");
            }
        }
    }
    
    // Image recognition example (simplified)
    static class ImageRecognitionBlackboard {
        private Map<String, Object> features = new HashMap<>();
        
        public void addFeature(String name, Object value) {
            features.put(name, value);
        }
        
        public Object getFeature(String name) {
            return features.get(name);
        }
        
        public boolean hasFeature(String name) {
            return features.containsKey(name);
        }
        
        public void showFeatures() {
            System.out.println("Extracted features:");
            features.forEach((k, v) -> System.out.println("  " + k + ": " + v));
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Blackboard Pattern Demo ===\n");
        
        // Math problem solving
        System.out.println("1. Collaborative Math Problem Solving:");
        Blackboard blackboard = new Blackboard();
        Controller controller = new Controller(blackboard);
        
        // Register knowledge sources
        controller.addKnowledgeSource(new AdditionSolver());
        controller.addKnowledgeSource(new MultiplicationSolver());
        controller.addKnowledgeSource(new ResultFormatter());
        
        // Problem 1: Addition
        System.out.println("\nProblem: 15 + 27");
        blackboard.put("operation", "add");
        blackboard.put("operand1", 15);
        blackboard.put("operand2", 27);
        controller.solve();
        System.out.println("\nFinal result: " + blackboard.get("formatted_result"));
        
        // Problem 2: Multiplication
        System.out.println("\n" + "=".repeat(50));
        blackboard.clear();
        System.out.println("\nProblem: 8 × 12");
        blackboard.put("operation", "multiply");
        blackboard.put("operand1", 8);
        blackboard.put("operand2", 12);
        controller.solve();
        System.out.println("\nFinal result: " + blackboard.get("formatted_result"));
        
        // Image recognition example (conceptual)
        System.out.println("\n\n2. Image Recognition (Conceptual):");
        ImageRecognitionBlackboard imageBoard = new ImageRecognitionBlackboard();
        
        System.out.println("Edge detector contributes...");
        imageBoard.addFeature("edges", "rectangular shape detected");
        
        System.out.println("Color analyzer contributes...");
        imageBoard.addFeature("colors", "red and white");
        
        System.out.println("Pattern recognizer contributes...");
        imageBoard.addFeature("pattern", "stop sign characteristics");
        
        System.out.println("Classifier makes final decision...");
        imageBoard.addFeature("classification", "STOP SIGN");
        
        System.out.println();
        imageBoard.showFeatures();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Multiple experts collaborate");
        System.out.println("✓ Flexible and extensible");
        System.out.println("✓ Handles complex problems");
        System.out.println("✓ Incremental solution building");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• AI and expert systems");
        System.out.println("• Speech recognition");
        System.out.println("• Image analysis");
        System.out.println("• Complex problem solving");
    }
}
