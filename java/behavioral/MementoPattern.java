package behavioral;

/**
 * Memento Pattern
 * Captures and restores an object's internal state.
 */
public class MementoPattern {
    
    // Originator
    static class TextEditor {
        private StringBuilder content;
        
        public TextEditor() {
            this.content = new StringBuilder();
        }
        
        public void write(String text) {
            content.append(text);
        }
        
        public void delete(int length) {
            int start = Math.max(0, content.length() - length);
            content.delete(start, content.length());
        }
        
        public String getContent() {
            return content.toString();
        }
        
        // Create memento
        public EditorMemento save() {
            return new EditorMemento(content.toString());
        }
        
        // Restore from memento
        public void restore(EditorMemento memento) {
            this.content = new StringBuilder(memento.getState());
        }
        
        // Memento
        static class EditorMemento {
            private final String state;
            private final long timestamp;
            
            private EditorMemento(String state) {
                this.state = state;
                this.timestamp = System.currentTimeMillis();
            }
            
            private String getState() {
                return state;
            }
            
            public long getTimestamp() {
                return timestamp;
            }
        }
    }
    
    // Caretaker
    static class EditorHistory {
        private java.util.Stack<TextEditor.EditorMemento> history = new java.util.Stack<>();
        
        public void save(TextEditor editor) {
            history.push(editor.save());
            System.out.println("💾 State saved");
        }
        
        public void undo(TextEditor editor) {
            if (!history.isEmpty()) {
                TextEditor.EditorMemento memento = history.pop();
                editor.restore(memento);
                System.out.println("↩️  Undo performed");
            } else {
                System.out.println("❌ Nothing to undo");
            }
        }
        
        public int getHistorySize() {
            return history.size();
        }
    }
    
    // Game state example
    static class Game {
        private int level;
        private int score;
        private int health;
        
        public Game() {
            this.level = 1;
            this.score = 0;
            this.health = 100;
        }
        
        public void play() {
            level++;
            score += 100;
            health -= 20;
        }
        
        public void displayState() {
            System.out.println("🎮 Level: " + level + ", Score: " + score + ", Health: " + health);
        }
        
        public GameMemento save() {
            return new GameMemento(level, score, health);
        }
        
        public void restore(GameMemento memento) {
            this.level = memento.level;
            this.score = memento.score;
            this.health = memento.health;
        }
        
        static class GameMemento {
            private final int level;
            private final int score;
            private final int health;
            
            private GameMemento(int level, int score, int health) {
                this.level = level;
                this.score = score;
                this.health = health;
            }
        }
    }
    
    static class GameSaveManager {
        private java.util.List<Game.GameMemento> saves = new java.util.ArrayList<>();
        
        public void saveGame(Game game, String name) {
            saves.add(game.save());
            System.out.println("💾 Game saved: " + name);
        }
        
        public void loadGame(Game game, int saveIndex) {
            if (saveIndex >= 0 && saveIndex < saves.size()) {
                game.restore(saves.get(saveIndex));
                System.out.println("📂 Game loaded from save " + (saveIndex + 1));
            } else {
                System.out.println("❌ Invalid save index");
            }
        }
        
        public int getSaveCount() {
            return saves.size();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Memento Pattern Demo ===\n");
        
        // Text editor example
        System.out.println("1. Text Editor with Undo:");
        TextEditor editor = new TextEditor();
        EditorHistory history = new EditorHistory();
        
        editor.write("Hello");
        System.out.println("Content: \"" + editor.getContent() + "\"");
        history.save(editor);
        
        editor.write(" World");
        System.out.println("Content: \"" + editor.getContent() + "\"");
        history.save(editor);
        
        editor.write("!");
        System.out.println("Content: \"" + editor.getContent() + "\"");
        history.save(editor);
        
        editor.write(" This is amazing.");
        System.out.println("Content: \"" + editor.getContent() + "\"");
        
        System.out.println("\nPerforming undo operations:");
        history.undo(editor);
        System.out.println("Content: \"" + editor.getContent() + "\"");
        
        history.undo(editor);
        System.out.println("Content: \"" + editor.getContent() + "\"");
        
        history.undo(editor);
        System.out.println("Content: \"" + editor.getContent() + "\"");
        
        history.undo(editor);
        System.out.println("Content: \"" + editor.getContent() + "\"");
        
        // Game state example
        System.out.println("\n\n2. Game Save/Load System:");
        Game game = new Game();
        GameSaveManager saveManager = new GameSaveManager();
        
        game.displayState();
        saveManager.saveGame(game, "Start");
        
        System.out.println("\nPlaying...");
        game.play();
        game.displayState();
        saveManager.saveGame(game, "Checkpoint 1");
        
        System.out.println("\nPlaying more...");
        game.play();
        game.displayState();
        saveManager.saveGame(game, "Checkpoint 2");
        
        System.out.println("\nPlaying even more...");
        game.play();
        game.displayState();
        
        System.out.println("\nLoading previous save:");
        saveManager.loadGame(game, 1);
        game.displayState();
        
        System.out.println("\nLoading initial save:");
        saveManager.loadGame(game, 0);
        game.displayState();
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Preserves encapsulation");
        System.out.println("✓ Simplifies originator");
        System.out.println("✓ Enables undo/redo functionality");
        System.out.println("✓ State can be saved externally");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Text editors (undo/redo)");
        System.out.println("• Game save systems");
        System.out.println("• Transaction rollback");
        System.out.println("• Snapshot/checkpoint mechanisms");
    }
}
