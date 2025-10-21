package structural;

/**
 * Adapter Pattern (Wrapper)
 * Allows incompatible interfaces to work together.
 */
public class AdapterPattern {
    
    // Target interface (what client expects)
    interface MediaPlayer {
        void play(String audioType, String fileName);
    }
    
    // Adaptee (incompatible interface)
    interface AdvancedMediaPlayer {
        void playVlc(String fileName);
        void playMp4(String fileName);
    }
    
    // Concrete Adaptee classes
    static class VlcPlayer implements AdvancedMediaPlayer {
        @Override
        public void playVlc(String fileName) {
            System.out.println("Playing VLC file: " + fileName);
        }
        
        @Override
        public void playMp4(String fileName) {
            // Do nothing
        }
    }
    
    static class Mp4Player implements AdvancedMediaPlayer {
        @Override
        public void playVlc(String fileName) {
            // Do nothing
        }
        
        @Override
        public void playMp4(String fileName) {
            System.out.println("Playing MP4 file: " + fileName);
        }
    }
    
    // Adapter (makes AdvancedMediaPlayer compatible with MediaPlayer)
    static class MediaAdapter implements MediaPlayer {
        private AdvancedMediaPlayer advancedPlayer;
        
        public MediaAdapter(String audioType) {
            if (audioType.equalsIgnoreCase("vlc")) {
                advancedPlayer = new VlcPlayer();
            } else if (audioType.equalsIgnoreCase("mp4")) {
                advancedPlayer = new Mp4Player();
            }
        }
        
        @Override
        public void play(String audioType, String fileName) {
            if (audioType.equalsIgnoreCase("vlc")) {
                advancedPlayer.playVlc(fileName);
            } else if (audioType.equalsIgnoreCase("mp4")) {
                advancedPlayer.playMp4(fileName);
            }
        }
    }
    
    // Client class
    static class AudioPlayer implements MediaPlayer {
        @Override
        public void play(String audioType, String fileName) {
            // Built-in support for mp3
            if (audioType.equalsIgnoreCase("mp3")) {
                System.out.println("Playing MP3 file: " + fileName);
            }
            // Use adapter for other formats
            else if (audioType.equalsIgnoreCase("vlc") || 
                     audioType.equalsIgnoreCase("mp4")) {
                MediaAdapter adapter = new MediaAdapter(audioType);
                adapter.play(audioType, fileName);
            } else {
                System.out.println("Invalid media type: " + audioType);
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Adapter Pattern Demo ===\n");
        
        AudioPlayer player = new AudioPlayer();
        
        player.play("mp3", "song.mp3");
        player.play("mp4", "video.mp4");
        player.play("vlc", "movie.vlc");
        player.play("avi", "clip.avi");
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Integrates incompatible interfaces");
        System.out.println("✓ Promotes reusability");
        System.out.println("✓ Follows Single Responsibility Principle");
    }
}
