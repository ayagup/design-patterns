package additional;

import java.util.*;

/**
 * Twin Pattern
 * 
 * Intent: Allows modeling of the same entity in two different systems
 * or contexts, keeping them synchronized.
 * 
 * Motivation:
 * Separates concerns across different contexts.
 * Maintains consistency between representations.
 * Enables independent evolution of each twin.
 * Coordinates state changes.
 * 
 * Applicability:
 * - Multi-system integration
 * - GUI and business logic separation
 * - Database and cache synchronization
 * - Different domain representations
 */

/**
 * Example 1: GUI and Business Model Twins
 * 
 * Business model and UI representation
 */
class BusinessCustomer {
    private String id;
    private String name;
    private double creditLimit;
    private UICustomer twin;
    
    public BusinessCustomer(String id, String name, double creditLimit) {
        this.id = id;
        this.name = name;
        this.creditLimit = creditLimit;
    }
    
    public void setTwin(UICustomer twin) {
        this.twin = twin;
    }
    
    public void updateCreditLimit(double newLimit) {
        System.out.println("  [Business] Updating credit limit: " + newLimit);
        this.creditLimit = newLimit;
        
        // Notify twin
        if (twin != null) {
            twin.onCreditLimitChanged(newLimit);
        }
    }
    
    public void validateOrder(double amount) {
        if (amount > creditLimit) {
            System.out.println("  [Business] Order exceeds credit limit!");
            if (twin != null) {
                twin.showCreditWarning();
            }
        } else {
            System.out.println("  [Business] Order approved");
        }
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public double getCreditLimit() { return creditLimit; }
}

class UICustomer {
    private String displayName;
    private String formattedCredit;
    private BusinessCustomer twin;
    
    public UICustomer(String displayName) {
        this.displayName = displayName;
    }
    
    public void setTwin(BusinessCustomer twin) {
        this.twin = twin;
    }
    
    public void onCreditLimitChanged(double newLimit) {
        formattedCredit = "$" + String.format("%.2f", newLimit);
        System.out.println("  [UI] Display updated: Credit limit = " + formattedCredit);
        refreshDisplay();
    }
    
    public void showCreditWarning() {
        System.out.println("  [UI] ⚠ WARNING: Order exceeds credit limit!");
        System.out.println("  [UI] Showing alert dialog...");
    }
    
    private void refreshDisplay() {
        System.out.println("  [UI] Refreshing customer display: " + displayName);
    }
    
    public void requestCreditIncrease(double amount) {
        System.out.println("  [UI] User requested credit increase");
        if (twin != null) {
            twin.updateCreditLimit(twin.getCreditLimit() + amount);
        }
    }
}

/**
 * Example 2: Database and Cache Twins
 * 
 * Database record and cached representation
 */
class DatabaseRecord {
    private Long id;
    private Map<String, Object> data;
    private CacheEntry twin;
    
    public DatabaseRecord(Long id) {
        this.id = id;
        this.data = new HashMap<>();
    }
    
    public void setTwin(CacheEntry twin) {
        this.twin = twin;
    }
    
    public void save(String key, Object value) {
        System.out.println("  [Database] Writing to disk: " + key + " = " + value);
        data.put(key, value);
        
        // Update cache twin
        if (twin != null) {
            twin.updateFromDatabase(key, value);
        }
    }
    
    public Object load(String key) {
        // Check cache first
        if (twin != null && twin.has(key)) {
            System.out.println("  [Database] Cache hit, skipping disk read");
            return twin.get(key);
        }
        
        System.out.println("  [Database] Reading from disk: " + key);
        return data.get(key);
    }
    
    public void invalidate() {
        System.out.println("  [Database] Record invalidated");
        if (twin != null) {
            twin.evict();
        }
    }
}

class CacheEntry {
    private Map<String, Object> cached;
    private long timestamp;
    private DatabaseRecord twin;
    
    public CacheEntry() {
        this.cached = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public void setTwin(DatabaseRecord twin) {
        this.twin = twin;
    }
    
    public void updateFromDatabase(String key, Object value) {
        cached.put(key, value);
        timestamp = System.currentTimeMillis();
        System.out.println("  [Cache] Updated: " + key);
    }
    
    public boolean has(String key) {
        return cached.containsKey(key);
    }
    
    public Object get(String key) {
        System.out.println("  [Cache] Returning cached value: " + key);
        return cached.get(key);
    }
    
    public void evict() {
        System.out.println("  [Cache] Evicting all cached data");
        cached.clear();
    }
    
    public long getTimestamp() { return timestamp; }
}

/**
 * Example 3: Client and Server Twins
 * 
 * Client-side and server-side representations
 */
class ClientGameState {
    private int playerX;
    private int playerY;
    private int score;
    private ServerGameState twin;
    
    public ClientGameState(int x, int y) {
        this.playerX = x;
        this.playerY = y;
        this.score = 0;
    }
    
    public void setTwin(ServerGameState twin) {
        this.twin = twin;
    }
    
    public void movePlayer(int dx, int dy) {
        playerX += dx;
        playerY += dy;
        System.out.println("  [Client] Player moved to (" + playerX + ", " + playerY + ")");
        
        // Send to server twin for validation
        if (twin != null) {
            twin.validateMove(playerX, playerY);
        }
    }
    
    public void updateFromServer(int x, int y, int newScore) {
        playerX = x;
        playerY = y;
        score = newScore;
        System.out.println("  [Client] State synced from server: Score = " + score);
        render();
    }
    
    private void render() {
        System.out.println("  [Client] Rendering at (" + playerX + ", " + playerY + ")");
    }
}

class ServerGameState {
    private int authorativeX;
    private int authorativeY;
    private int score;
    private ClientGameState twin;
    
    public ServerGameState(int x, int y) {
        this.authorativeX = x;
        this.authorativeY = y;
        this.score = 0;
    }
    
    public void setTwin(ClientGameState twin) {
        this.twin = twin;
    }
    
    public void validateMove(int x, int y) {
        System.out.println("  [Server] Validating move to (" + x + ", " + y + ")");
        
        // Validate and update authoritative state
        if (isValidPosition(x, y)) {
            authorativeX = x;
            authorativeY = y;
            score += 10;
            System.out.println("  [Server] Move accepted, score: " + score);
            
            // Sync back to client
            if (twin != null) {
                twin.updateFromServer(authorativeX, authorativeY, score);
            }
        } else {
            System.out.println("  [Server] Invalid move, rejecting");
        }
    }
    
    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x <= 100 && y >= 0 && y <= 100;
    }
}

/**
 * Example 4: Document Edit Twins
 * 
 * Editor and preview representations
 */
class DocumentEditor {
    private StringBuilder content;
    private DocumentPreview twin;
    
    public DocumentEditor() {
        this.content = new StringBuilder();
    }
    
    public void setTwin(DocumentPreview twin) {
        this.twin = twin;
    }
    
    public void type(String text) {
        content.append(text);
        System.out.println("  [Editor] Typed: " + text);
        
        // Update preview
        if (twin != null) {
            twin.updatePreview(content.toString());
        }
    }
    
    public void delete(int count) {
        if (count > 0 && content.length() >= count) {
            content.delete(content.length() - count, content.length());
            System.out.println("  [Editor] Deleted " + count + " characters");
            
            if (twin != null) {
                twin.updatePreview(content.toString());
            }
        }
    }
    
    public String getContent() {
        return content.toString();
    }
}

class DocumentPreview {
    private String renderedHtml;
    private DocumentEditor twin;
    
    public void setTwin(DocumentEditor twin) {
        this.twin = twin;
    }
    
    public void updatePreview(String markdown) {
        // Simulate markdown to HTML conversion
        renderedHtml = convertToHtml(markdown);
        System.out.println("  [Preview] Updated HTML preview");
        render();
    }
    
    private String convertToHtml(String markdown) {
        return "<p>" + markdown + "</p>";
    }
    
    private void render() {
        System.out.println("  [Preview] Rendering: " + 
                         (renderedHtml.length() > 50 ? 
                          renderedHtml.substring(0, 50) + "..." : renderedHtml));
    }
    
    public void scroll(int position) {
        System.out.println("  [Preview] Scrolled to position: " + position);
        // Could notify editor to sync scroll position
    }
}

/**
 * Example 5: Model-View Twins
 * 
 * Data model and visual representation
 */
class DataModel {
    private List<Integer> values;
    private ChartView twin;
    
    public DataModel() {
        this.values = new ArrayList<>();
    }
    
    public void setTwin(ChartView twin) {
        this.twin = twin;
    }
    
    public void addValue(int value) {
        values.add(value);
        System.out.println("  [Model] Added value: " + value);
        
        if (twin != null) {
            twin.redraw(values);
        }
    }
    
    public void removeValue(int index) {
        if (index >= 0 && index < values.size()) {
            values.remove(index);
            System.out.println("  [Model] Removed value at index: " + index);
            
            if (twin != null) {
                twin.redraw(values);
            }
        }
    }
    
    public List<Integer> getValues() {
        return new ArrayList<>(values);
    }
}

class ChartView {
    private String chartType;
    private DataModel twin;
    
    public ChartView(String chartType) {
        this.chartType = chartType;
    }
    
    public void setTwin(DataModel twin) {
        this.twin = twin;
    }
    
    public void redraw(List<Integer> data) {
        System.out.println("  [Chart] Redrawing " + chartType + " with " + data.size() + " points");
        System.out.println("  [Chart] Data: " + data);
    }
    
    public void changeChartType(String newType) {
        this.chartType = newType;
        System.out.println("  [Chart] Changed to " + chartType);
        
        if (twin != null) {
            redraw(twin.getValues());
        }
    }
}

/**
 * Demonstration of the Twin Pattern
 */
public class TwinPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Twin Pattern Demo ===\n");
        
        // Example 1: Business and UI Twins
        System.out.println("1. Business and UI Twins:");
        BusinessCustomer bizCustomer = new BusinessCustomer("C001", "Alice Corp", 10000.0);
        UICustomer uiCustomer = new UICustomer("Alice Corporation");
        
        // Link twins
        bizCustomer.setTwin(uiCustomer);
        uiCustomer.setTwin(bizCustomer);
        
        bizCustomer.updateCreditLimit(15000.0);
        uiCustomer.requestCreditIncrease(5000.0);
        bizCustomer.validateOrder(25000.0);
        
        // Example 2: Database and Cache Twins
        System.out.println("\n2. Database and Cache Twins:");
        DatabaseRecord dbRecord = new DatabaseRecord(123L);
        CacheEntry cache = new CacheEntry();
        
        dbRecord.setTwin(cache);
        cache.setTwin(dbRecord);
        
        dbRecord.save("username", "alice");
        dbRecord.save("email", "alice@example.com");
        dbRecord.load("username"); // Cache hit
        
        // Example 3: Client and Server Twins
        System.out.println("\n3. Client and Server Twins:");
        ClientGameState client = new ClientGameState(10, 10);
        ServerGameState server = new ServerGameState(10, 10);
        
        client.setTwin(server);
        server.setTwin(client);
        
        client.movePlayer(5, 3);
        client.movePlayer(2, 1);
        
        // Example 4: Editor and Preview Twins
        System.out.println("\n4. Editor and Preview Twins:");
        DocumentEditor editor = new DocumentEditor();
        DocumentPreview preview = new DocumentPreview();
        
        editor.setTwin(preview);
        preview.setTwin(editor);
        
        editor.type("# Hello World");
        editor.type("\nThis is markdown.");
        editor.delete(5);
        
        // Example 5: Model and View Twins
        System.out.println("\n5. Model and View Twins:");
        DataModel model = new DataModel();
        ChartView chart = new ChartView("Bar Chart");
        
        model.setTwin(chart);
        chart.setTwin(model);
        
        model.addValue(10);
        model.addValue(25);
        model.addValue(15);
        chart.changeChartType("Line Chart");
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Separates concerns across contexts");
        System.out.println("✓ Maintains synchronization");
        System.out.println("✓ Independent evolution");
        System.out.println("✓ Coordinated state changes");
        System.out.println("✓ Clear responsibilities");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• GUI and business logic");
        System.out.println("• Client-server sync");
        System.out.println("• Database and cache");
        System.out.println("• Editor and preview");
        System.out.println("• Model-View separation");
    }
}
