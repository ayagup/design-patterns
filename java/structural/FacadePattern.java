package structural;

/**
 * Facade Pattern
 * Provides a simplified interface to a complex subsystem.
 */
public class FacadePattern {
    
    // Complex subsystem classes
    static class CPU {
        public void freeze() {
            System.out.println("CPU: Freezing processor");
        }
        
        public void jump(long position) {
            System.out.println("CPU: Jumping to position " + position);
        }
        
        public void execute() {
            System.out.println("CPU: Executing instructions");
        }
    }
    
    static class Memory {
        public void load(long position, byte[] data) {
            System.out.println("Memory: Loading data at position " + position);
        }
    }
    
    static class HardDrive {
        public byte[] read(long lba, int size) {
            System.out.println("HardDrive: Reading " + size + " bytes from sector " + lba);
            return new byte[size];
        }
    }
    
    // Facade
    static class ComputerFacade {
        private CPU cpu;
        private Memory memory;
        private HardDrive hardDrive;
        
        public ComputerFacade() {
            this.cpu = new CPU();
            this.memory = new Memory();
            this.hardDrive = new HardDrive();
        }
        
        public void start() {
            System.out.println("=== Starting Computer ===");
            cpu.freeze();
            memory.load(0, hardDrive.read(0, 1024));
            cpu.jump(0);
            cpu.execute();
            System.out.println("=== Computer Started ===\n");
        }
    }
    
    // Home Theater example
    static class DVDPlayer {
        public void on() {
            System.out.println("DVD Player: ON");
        }
        
        public void play(String movie) {
            System.out.println("DVD Player: Playing '" + movie + "'");
        }
        
        public void off() {
            System.out.println("DVD Player: OFF");
        }
    }
    
    static class Amplifier {
        public void on() {
            System.out.println("Amplifier: ON");
        }
        
        public void setVolume(int level) {
            System.out.println("Amplifier: Setting volume to " + level);
        }
        
        public void off() {
            System.out.println("Amplifier: OFF");
        }
    }
    
    static class Projector {
        public void on() {
            System.out.println("Projector: ON");
        }
        
        public void wideScreenMode() {
            System.out.println("Projector: Setting widescreen mode");
        }
        
        public void off() {
            System.out.println("Projector: OFF");
        }
    }
    
    static class Lights {
        public void dim(int level) {
            System.out.println("Lights: Dimming to " + level + "%");
        }
        
        public void on() {
            System.out.println("Lights: ON (100%)");
        }
    }
    
    // Home Theater Facade
    static class HomeTheaterFacade {
        private DVDPlayer dvdPlayer;
        private Amplifier amplifier;
        private Projector projector;
        private Lights lights;
        
        public HomeTheaterFacade() {
            this.dvdPlayer = new DVDPlayer();
            this.amplifier = new Amplifier();
            this.projector = new Projector();
            this.lights = new Lights();
        }
        
        public void watchMovie(String movie) {
            System.out.println("=== Get ready to watch a movie ===");
            lights.dim(10);
            projector.on();
            projector.wideScreenMode();
            amplifier.on();
            amplifier.setVolume(5);
            dvdPlayer.on();
            dvdPlayer.play(movie);
            System.out.println("=== Enjoy your movie! ===\n");
        }
        
        public void endMovie() {
            System.out.println("=== Shutting down movie theater ===");
            dvdPlayer.off();
            amplifier.off();
            projector.off();
            lights.on();
            System.out.println("=== Theater shutdown complete ===\n");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Facade Pattern Demo ===\n");
        
        // Computer boot example
        System.out.println("1. Computer Boot Sequence:");
        ComputerFacade computer = new ComputerFacade();
        computer.start();
        
        // Home theater example
        System.out.println("2. Home Theater System:");
        HomeTheaterFacade homeTheater = new HomeTheaterFacade();
        homeTheater.watchMovie("The Matrix");
        
        System.out.println("... Movie playing ...\n");
        
        homeTheater.endMovie();
        
        System.out.println("--- Benefits ---");
        System.out.println("✓ Simplifies complex subsystems");
        System.out.println("✓ Reduces dependencies on subsystem classes");
        System.out.println("✓ Makes the subsystem easier to use");
        System.out.println("✓ Doesn't prevent access to subsystem if needed");
    }
}
