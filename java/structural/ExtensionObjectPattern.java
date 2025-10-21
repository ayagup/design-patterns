package structural;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension Object Pattern
 * Adds functionality to a hierarchy without changing the hierarchy.
 */
public class ExtensionObjectPattern {
    
    // Extension interface
    interface Extension {
        String getName();
    }
    
    // Base class with extension support
    static abstract class GameObject {
        private Map<String, Extension> extensions = new HashMap<>();
        protected String name;
        
        public GameObject(String name) {
            this.name = name;
        }
        
        public void addExtension(Extension extension) {
            extensions.put(extension.getName(), extension);
            System.out.println("Added extension '" + extension.getName() + 
                             "' to " + name);
        }
        
        public Extension getExtension(String name) {
            return extensions.get(name);
        }
        
        public boolean hasExtension(String name) {
            return extensions.containsKey(name);
        }
        
        public abstract void display();
    }
    
    // Concrete game objects
    static class Player extends GameObject {
        public Player(String name) {
            super(name);
        }
        
        @Override
        public void display() {
            System.out.println("Player: " + name);
        }
    }
    
    static class Enemy extends GameObject {
        public Enemy(String name) {
            super(name);
        }
        
        @Override
        public void display() {
            System.out.println("Enemy: " + name);
        }
    }
    
    // Extension implementations
    static class PhysicsExtension implements Extension {
        private double velocity;
        private double mass;
        
        public PhysicsExtension(double velocity, double mass) {
            this.velocity = velocity;
            this.mass = mass;
        }
        
        @Override
        public String getName() {
            return "Physics";
        }
        
        public void applyForce(double force) {
            double acceleration = force / mass;
            velocity += acceleration;
            System.out.println("Applied force: " + force + 
                             ", New velocity: " + velocity);
        }
        
        public double getVelocity() {
            return velocity;
        }
    }
    
    static class RenderExtension implements Extension {
        private String texture;
        private int layer;
        
        public RenderExtension(String texture, int layer) {
            this.texture = texture;
            this.layer = layer;
        }
        
        @Override
        public String getName() {
            return "Render";
        }
        
        public void render() {
            System.out.println("Rendering with texture: " + texture + 
                             " on layer: " + layer);
        }
    }
    
    static class SoundExtension implements Extension {
        private String soundFile;
        private int volume;
        
        public SoundExtension(String soundFile, int volume) {
            this.soundFile = soundFile;
            this.volume = volume;
        }
        
        @Override
        public String getName() {
            return "Sound";
        }
        
        public void playSound() {
            System.out.println("Playing sound: " + soundFile + 
                             " at volume: " + volume);
        }
        
        public void setVolume(int volume) {
            this.volume = volume;
            System.out.println("Volume set to: " + volume);
        }
    }
    
    static class AIExtension implements Extension {
        private String behavior;
        
        public AIExtension(String behavior) {
            this.behavior = behavior;
        }
        
        @Override
        public String getName() {
            return "AI";
        }
        
        public void update() {
            System.out.println("AI executing behavior: " + behavior);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Extension Object Pattern Demo ===\n");
        
        // Create player with extensions
        System.out.println("1. Creating Player with Extensions:");
        Player player = new Player("Hero");
        player.display();
        
        // Add extensions to player
        player.addExtension(new PhysicsExtension(5.0, 75.0));
        player.addExtension(new RenderExtension("hero_texture.png", 1));
        player.addExtension(new SoundExtension("footsteps.mp3", 50));
        
        System.out.println("\nUsing Player Extensions:");
        if (player.hasExtension("Physics")) {
            PhysicsExtension physics = (PhysicsExtension) player.getExtension("Physics");
            physics.applyForce(100.0);
        }
        
        if (player.hasExtension("Render")) {
            RenderExtension render = (RenderExtension) player.getExtension("Render");
            render.render();
        }
        
        if (player.hasExtension("Sound")) {
            SoundExtension sound = (SoundExtension) player.getExtension("Sound");
            sound.playSound();
        }
        
        // Create enemy with different extensions
        System.out.println("\n\n2. Creating Enemy with Different Extensions:");
        Enemy enemy = new Enemy("Goblin");
        enemy.display();
        
        enemy.addExtension(new PhysicsExtension(3.0, 50.0));
        enemy.addExtension(new RenderExtension("goblin_texture.png", 1));
        enemy.addExtension(new AIExtension("patrol"));
        
        System.out.println("\nUsing Enemy Extensions:");
        if (enemy.hasExtension("Physics")) {
            PhysicsExtension physics = (PhysicsExtension) enemy.getExtension("Physics");
            System.out.println("Enemy velocity: " + physics.getVelocity());
        }
        
        if (enemy.hasExtension("AI")) {
            AIExtension ai = (AIExtension) enemy.getExtension("AI");
            ai.update();
        }
        
        // Demonstrate flexibility
        System.out.println("\n\n3. Adding Extension Dynamically:");
        System.out.println("Player doesn't have AI initially: " + 
                         !player.hasExtension("AI"));
        player.addExtension(new AIExtension("follow_enemy"));
        System.out.println("Player now has AI: " + player.hasExtension("AI"));
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Adds functionality without modifying base classes");
        System.out.println("✓ Supports dynamic extension addition");
        System.out.println("✓ Follows Open/Closed Principle");
        System.out.println("✓ Reduces class hierarchy complexity");
        System.out.println("✓ Allows different objects to have different extensions");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Game engines (components on game objects)");
        System.out.println("• Plugin systems");
        System.out.println("• Frameworks requiring extensibility");
        System.out.println("• Systems needing runtime feature addition");
    }
}
