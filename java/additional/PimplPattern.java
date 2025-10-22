package additional;

import java.util.*;

/**
 * Pimpl Pattern (Pointer to Implementation)
 * ==========================================
 * 
 * Intent:
 * Hides implementation details by placing them in a separate class that is
 * referenced only through a pointer/reference. This pattern reduces compilation
 * dependencies and allows changing implementation without recompiling clients.
 * 
 * Also Known As:
 * - Opaque Pointer
 * - Compilation Firewall
 * - Cheshire Cat
 * - Bridge (in some contexts)
 * 
 * Motivation:
 * - Reduce compilation dependencies and build times
 * - Hide implementation details completely from header/interface
 * - Allow binary compatibility when implementation changes
 * - Provide better encapsulation
 * 
 * Applicability:
 * - When you want to hide implementation from client code
 * - When you need stable ABI (Application Binary Interface)
 * - When implementation changes frequently but interface is stable
 * - When reducing compilation dependencies is important
 * 
 * Structure:
 * PublicInterface → Impl (hidden implementation class)
 * 
 * Participants:
 * - PublicInterface: The public-facing class/interface
 * - Impl: Private implementation class containing actual logic
 * 
 * Implementation Considerations:
 * 1. Implementation class should be private/package-private
 * 2. Public interface delegates all operations to impl
 * 3. Impl is created in constructor, destroyed in cleanup
 * 4. Implementation can be swapped without affecting clients
 * 5. May have small performance overhead due to indirection
 */

// Example 1: Database Connection with Hidden Implementation
// Public interface exposes only essential operations
class DatabaseConnection {
    private final DatabaseConnectionImpl impl;
    
    public DatabaseConnection(String host, int port, String database) {
        this.impl = new DatabaseConnectionImpl(host, port, database);
    }
    
    public void connect() {
        impl.connect();
    }
    
    public void disconnect() {
        impl.disconnect();
    }
    
    public ResultSet executeQuery(String sql) {
        return impl.executeQuery(sql);
    }
    
    public boolean isConnected() {
        return impl.isConnected();
    }
}

// Hidden implementation with all the complex details
class DatabaseConnectionImpl {
    private String host;
    private int port;
    private String database;
    private boolean connected;
    private Socket socket; // Complex networking details hidden
    private Map<String, PreparedStatement> statementCache;
    
    DatabaseConnectionImpl(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.connected = false;
        this.socket = new Socket();
        this.statementCache = new HashMap<>();
    }
    
    void connect() {
        if (!connected) {
            socket.connect(host, port);
            authenticateWithDatabase();
            selectDatabase(database);
            connected = true;
            System.out.println("Connected to database: " + database + " at " + host + ":" + port);
        }
    }
    
    void disconnect() {
        if (connected) {
            clearStatementCache();
            socket.close();
            connected = false;
            System.out.println("Disconnected from database");
        }
    }
    
    ResultSet executeQuery(String sql) {
        if (!connected) {
            throw new IllegalStateException("Not connected to database");
        }
        
        PreparedStatement stmt = getOrCreateStatement(sql);
        return stmt.execute();
    }
    
    boolean isConnected() {
        return connected && socket.isOpen();
    }
    
    private void authenticateWithDatabase() {
        System.out.println("Authenticating with database...");
    }
    
    private void selectDatabase(String db) {
        System.out.println("Selecting database: " + db);
    }
    
    private PreparedStatement getOrCreateStatement(String sql) {
        return statementCache.computeIfAbsent(sql, PreparedStatement::new);
    }
    
    private void clearStatementCache() {
        statementCache.clear();
    }
}

// Supporting classes (simplified)
class Socket {
    private boolean open = false;
    
    void connect(String host, int port) {
        this.open = true;
    }
    
    void close() {
        this.open = false;
    }
    
    boolean isOpen() {
        return open;
    }
}

class PreparedStatement {
    private String sql;
    
    PreparedStatement(String sql) {
        this.sql = sql;
    }
    
    ResultSet execute() {
        return new ResultSet(sql);
    }
}

class ResultSet {
    private String query;
    private List<Map<String, Object>> rows;
    
    ResultSet(String query) {
        this.query = query;
        this.rows = new ArrayList<>();
        // Simulate some results
        Map<String, Object> row = new HashMap<>();
        row.put("result", "Data for: " + query);
        rows.add(row);
    }
    
    @Override
    public String toString() {
        return "ResultSet{query='" + query + "', rows=" + rows.size() + "}";
    }
}

// Example 2: Image Processing Library
// Public API is simple and stable
class ImageProcessor {
    private final ImageProcessorImpl impl;
    
    public ImageProcessor(int width, int height) {
        this.impl = new ImageProcessorImpl(width, height);
    }
    
    public void loadImage(String filename) {
        impl.loadImage(filename);
    }
    
    public void applyFilter(String filterName) {
        impl.applyFilter(filterName);
    }
    
    public void resize(int newWidth, int newHeight) {
        impl.resize(newWidth, newHeight);
    }
    
    public void save(String filename) {
        impl.save(filename);
    }
    
    public ImageStats getStats() {
        return impl.getStats();
    }
}

// Implementation contains complex image processing logic
class ImageProcessorImpl {
    private int width;
    private int height;
    private byte[] pixelData;
    private ColorSpace colorSpace;
    private CompressionEngine compressionEngine;
    private FilterPipeline filterPipeline;
    
    ImageProcessorImpl(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixelData = new byte[width * height * 4]; // RGBA
        this.colorSpace = new ColorSpace();
        this.compressionEngine = new CompressionEngine();
        this.filterPipeline = new FilterPipeline();
    }
    
    void loadImage(String filename) {
        System.out.println("Loading image: " + filename);
        // Complex decoding logic hidden
        decompressImageData(filename);
        convertColorSpace();
    }
    
    void applyFilter(String filterName) {
        System.out.println("Applying filter: " + filterName);
        filterPipeline.addFilter(filterName);
        filterPipeline.execute(pixelData, width, height);
    }
    
    void resize(int newWidth, int newHeight) {
        System.out.println("Resizing from " + width + "x" + height + 
                         " to " + newWidth + "x" + newHeight);
        
        byte[] newPixelData = new byte[newWidth * newHeight * 4];
        performBilinearInterpolation(pixelData, width, height, 
                                    newPixelData, newWidth, newHeight);
        
        this.width = newWidth;
        this.height = newHeight;
        this.pixelData = newPixelData;
    }
    
    void save(String filename) {
        System.out.println("Saving image to: " + filename);
        byte[] compressed = compressionEngine.compress(pixelData);
        writeToFile(filename, compressed);
    }
    
    ImageStats getStats() {
        return new ImageStats(width, height, calculateAverageBrightness(), 
                            countColors());
    }
    
    private void decompressImageData(String filename) {
        // Complex decompression logic
    }
    
    private void convertColorSpace() {
        colorSpace.convert(pixelData);
    }
    
    private void performBilinearInterpolation(byte[] src, int srcW, int srcH,
                                             byte[] dst, int dstW, int dstH) {
        // Complex interpolation algorithm
    }
    
    private void writeToFile(String filename, byte[] data) {
        // File I/O operations
    }
    
    private double calculateAverageBrightness() {
        return 128.0; // Simplified
    }
    
    private int countColors() {
        return 256; // Simplified
    }
}

// Supporting classes
class ColorSpace {
    void convert(byte[] pixels) {
        System.out.println("Converting color space");
    }
}

class CompressionEngine {
    byte[] compress(byte[] data) {
        System.out.println("Compressing image data");
        return data;
    }
}

class FilterPipeline {
    private List<String> filters = new ArrayList<>();
    
    void addFilter(String filter) {
        filters.add(filter);
    }
    
    void execute(byte[] pixels, int width, int height) {
        for (String filter : filters) {
            System.out.println("Executing filter: " + filter);
        }
    }
}

class ImageStats {
    private int width;
    private int height;
    private double avgBrightness;
    private int colorCount;
    
    ImageStats(int width, int height, double avgBrightness, int colorCount) {
        this.width = width;
        this.height = height;
        this.avgBrightness = avgBrightness;
        this.colorCount = colorCount;
    }
    
    @Override
    public String toString() {
        return String.format("ImageStats{%dx%d, brightness=%.2f, colors=%d}",
                           width, height, avgBrightness, colorCount);
    }
}

// Example 3: Network Protocol Handler
class ProtocolHandler {
    private final ProtocolHandlerImpl impl;
    
    public ProtocolHandler(String protocolVersion) {
        this.impl = new ProtocolHandlerImpl(protocolVersion);
    }
    
    public void sendMessage(String message) {
        impl.sendMessage(message);
    }
    
    public String receiveMessage() {
        return impl.receiveMessage();
    }
    
    public void setEncryption(boolean enabled) {
        impl.setEncryption(enabled);
    }
    
    public ConnectionStats getStats() {
        return impl.getStats();
    }
}

class ProtocolHandlerImpl {
    private String protocolVersion;
    private EncryptionModule encryption;
    private CompressionModule compression;
    private PacketAssembler assembler;
    private TransmissionQueue queue;
    private StatisticsCollector stats;
    
    ProtocolHandlerImpl(String protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.encryption = new EncryptionModule();
        this.compression = new CompressionModule();
        this.assembler = new PacketAssembler();
        this.queue = new TransmissionQueue();
        this.stats = new StatisticsCollector();
    }
    
    void sendMessage(String message) {
        System.out.println("Sending message: " + message);
        
        byte[] data = message.getBytes();
        data = compression.compress(data);
        data = encryption.encrypt(data);
        
        List<Packet> packets = assembler.createPackets(data, protocolVersion);
        queue.enqueue(packets);
        transmitPackets();
        
        stats.recordSent(data.length);
    }
    
    String receiveMessage() {
        List<Packet> packets = queue.dequeue();
        if (packets.isEmpty()) {
            return null;
        }
        
        byte[] data = assembler.assemblePackets(packets);
        data = encryption.decrypt(data);
        data = compression.decompress(data);
        
        stats.recordReceived(data.length);
        return new String(data);
    }
    
    void setEncryption(boolean enabled) {
        encryption.setEnabled(enabled);
        System.out.println("Encryption " + (enabled ? "enabled" : "disabled"));
    }
    
    ConnectionStats getStats() {
        return stats.getConnectionStats();
    }
    
    private void transmitPackets() {
        System.out.println("Transmitting packets...");
    }
}

// Supporting classes
class EncryptionModule {
    private boolean enabled = true;
    
    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    byte[] encrypt(byte[] data) {
        return enabled ? data : data; // Simplified
    }
    
    byte[] decrypt(byte[] data) {
        return enabled ? data : data; // Simplified
    }
}

class CompressionModule {
    byte[] compress(byte[] data) {
        return data; // Simplified
    }
    
    byte[] decompress(byte[] data) {
        return data; // Simplified
    }
}

class PacketAssembler {
    List<Packet> createPackets(byte[] data, String version) {
        List<Packet> packets = new ArrayList<>();
        packets.add(new Packet(version, data));
        return packets;
    }
    
    byte[] assemblePackets(List<Packet> packets) {
        return packets.isEmpty() ? new byte[0] : packets.get(0).data;
    }
}

class Packet {
    String version;
    byte[] data;
    
    Packet(String version, byte[] data) {
        this.version = version;
        this.data = data;
    }
}

class TransmissionQueue {
    private Queue<List<Packet>> outgoing = new LinkedList<>();
    private Queue<List<Packet>> incoming = new LinkedList<>();
    
    void enqueue(List<Packet> packets) {
        incoming.offer(packets); // Simulate echo
    }
    
    List<Packet> dequeue() {
        return incoming.poll() != null ? incoming.poll() : new ArrayList<>();
    }
}

class StatisticsCollector {
    private long bytesSent = 0;
    private long bytesReceived = 0;
    
    void recordSent(long bytes) {
        bytesSent += bytes;
    }
    
    void recordReceived(long bytes) {
        bytesReceived += bytes;
    }
    
    ConnectionStats getConnectionStats() {
        return new ConnectionStats(bytesSent, bytesReceived);
    }
}

class ConnectionStats {
    private long bytesSent;
    private long bytesReceived;
    
    ConnectionStats(long bytesSent, long bytesReceived) {
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
    }
    
    @Override
    public String toString() {
        return String.format("ConnectionStats{sent=%d bytes, received=%d bytes}",
                           bytesSent, bytesReceived);
    }
}

// Example 4: Audio Engine
class AudioEngine {
    private final AudioEngineImpl impl;
    
    public AudioEngine(int sampleRate, int channels) {
        this.impl = new AudioEngineImpl(sampleRate, channels);
    }
    
    public void loadSound(String filename) {
        impl.loadSound(filename);
    }
    
    public void play() {
        impl.play();
    }
    
    public void pause() {
        impl.pause();
    }
    
    public void setVolume(double volume) {
        impl.setVolume(volume);
    }
    
    public void applyEffect(String effectName) {
        impl.applyEffect(effectName);
    }
}

class AudioEngineImpl {
    private int sampleRate;
    private int channels;
    private AudioBuffer buffer;
    private MixerEngine mixer;
    private EffectsChain effects;
    private PlaybackState state;
    
    AudioEngineImpl(int sampleRate, int channels) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.buffer = new AudioBuffer(sampleRate, channels);
        this.mixer = new MixerEngine(channels);
        this.effects = new EffectsChain();
        this.state = PlaybackState.STOPPED;
    }
    
    void loadSound(String filename) {
        System.out.println("Loading audio: " + filename);
        buffer.loadFromFile(filename);
        decodeAudioFormat();
        normalizeAudio();
    }
    
    void play() {
        if (state != PlaybackState.PLAYING) {
            System.out.println("Playing audio");
            state = PlaybackState.PLAYING;
            startAudioThread();
        }
    }
    
    void pause() {
        if (state == PlaybackState.PLAYING) {
            System.out.println("Pausing audio");
            state = PlaybackState.PAUSED;
        }
    }
    
    void setVolume(double volume) {
        System.out.println("Setting volume to: " + volume);
        mixer.setMasterVolume(volume);
    }
    
    void applyEffect(String effectName) {
        System.out.println("Applying effect: " + effectName);
        effects.addEffect(effectName);
    }
    
    private void decodeAudioFormat() {
        System.out.println("Decoding audio format");
    }
    
    private void normalizeAudio() {
        System.out.println("Normalizing audio levels");
    }
    
    private void startAudioThread() {
        System.out.println("Starting audio playback thread");
    }
}

enum PlaybackState {
    STOPPED, PLAYING, PAUSED
}

class AudioBuffer {
    private int sampleRate;
    private int channels;
    private float[] samples;
    
    AudioBuffer(int sampleRate, int channels) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.samples = new float[0];
    }
    
    void loadFromFile(String filename) {
        // Load audio data
    }
}

class MixerEngine {
    private int channels;
    private double masterVolume = 1.0;
    
    MixerEngine(int channels) {
        this.channels = channels;
    }
    
    void setMasterVolume(double volume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, volume));
    }
}

class EffectsChain {
    private List<String> effects = new ArrayList<>();
    
    void addEffect(String effect) {
        effects.add(effect);
    }
}

/**
 * Demonstration of Pimpl Pattern
 */
public class PimplPattern {
    public static void main(String[] args) {
        demonstrateDatabaseConnection();
        demonstrateImageProcessor();
        demonstrateProtocolHandler();
        demonstrateAudioEngine();
    }
    
    private static void demonstrateDatabaseConnection() {
        System.out.println("=== Database Connection (Pimpl) ===\n");
        
        // Client only sees simple interface, implementation is completely hidden
        DatabaseConnection db = new DatabaseConnection("localhost", 5432, "myapp");
        
        db.connect();
        ResultSet results = db.executeQuery("SELECT * FROM users");
        System.out.println("Query results: " + results);
        
        System.out.println("Is connected: " + db.isConnected());
        db.disconnect();
        System.out.println();
    }
    
    private static void demonstrateImageProcessor() {
        System.out.println("=== Image Processor (Pimpl) ===\n");
        
        // Complex image processing hidden behind simple API
        ImageProcessor processor = new ImageProcessor(1920, 1080);
        
        processor.loadImage("photo.jpg");
        processor.applyFilter("blur");
        processor.applyFilter("sharpen");
        processor.resize(800, 600);
        
        ImageStats stats = processor.getStats();
        System.out.println("Image statistics: " + stats);
        
        processor.save("output.jpg");
        System.out.println();
    }
    
    private static void demonstrateProtocolHandler() {
        System.out.println("=== Protocol Handler (Pimpl) ===\n");
        
        // Network protocol complexity hidden from client
        ProtocolHandler handler = new ProtocolHandler("HTTP/2.0");
        
        handler.setEncryption(true);
        handler.sendMessage("Hello, World!");
        
        String received = handler.receiveMessage();
        System.out.println("Received: " + received);
        
        ConnectionStats stats = handler.getStats();
        System.out.println(stats);
        System.out.println();
    }
    
    private static void demonstrateAudioEngine() {
        System.out.println("=== Audio Engine (Pimpl) ===\n");
        
        // Audio processing complexity hidden
        AudioEngine audio = new AudioEngine(44100, 2);
        
        audio.loadSound("music.mp3");
        audio.setVolume(0.8);
        audio.applyEffect("reverb");
        audio.applyEffect("equalizer");
        audio.play();
        
        // Simulate some playback
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        audio.pause();
    }
}
