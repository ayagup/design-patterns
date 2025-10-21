package enterprise;

import java.util.*;
import java.util.regex.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Template View Pattern
 * 
 * Intent: Renders information into HTML by embedding markers in HTML page
 * that are replaced with values from domain objects.
 * 
 * Motivation:
 * Separates presentation from business logic.
 * Allows designers to work on templates.
 * Changes to presentation don't require code changes.
 * Can use different templates for different clients.
 * 
 * Applicability:
 * - Generating HTML views
 * - Email templates
 * - Report generation
 * - Multiple presentation formats
 */

/**
 * Example 1: Simple Template Engine
 * 
 * Basic placeholder replacement: {{variableName}}
 */
class SimpleTemplateEngine {
    public String render(String template, Map<String, Object> context) {
        String result = template;
        
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
}

/**
 * Example 2: Advanced Template Engine
 * 
 * Supports loops and conditionals
 */
class AdvancedTemplateEngine {
    private static final Pattern IF_PATTERN = 
        Pattern.compile("\\{%\\s*if\\s+(\\w+)\\s*%\\}(.*?)\\{%\\s*endif\\s*%\\}", Pattern.DOTALL);
    private static final Pattern FOR_PATTERN = 
        Pattern.compile("\\{%\\s*for\\s+(\\w+)\\s+in\\s+(\\w+)\\s*%\\}(.*?)\\{%\\s*endfor\\s*%\\}", Pattern.DOTALL);
    private static final Pattern VAR_PATTERN = 
        Pattern.compile("\\{\\{\\s*(\\w+(?:\\.\\w+)*)\\s*\\}\\}");
    
    public String render(String template, Map<String, Object> context) {
        String result = template;
        
        // Process conditionals
        result = processIf(result, context);
        
        // Process loops
        result = processFor(result, context);
        
        // Process variables
        result = processVariables(result, context);
        
        return result;
    }
    
    private String processIf(String template, Map<String, Object> context) {
        Matcher matcher = IF_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String variable = matcher.group(1);
            String content = matcher.group(2);
            
            Object value = context.get(variable);
            boolean condition = isTrue(value);
            
            String replacement = condition ? content : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private String processFor(String template, Map<String, Object> context) {
        Matcher matcher = FOR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String itemVar = matcher.group(1);
            String listVar = matcher.group(2);
            String content = matcher.group(3);
            
            Object listObj = context.get(listVar);
            if (listObj instanceof List) {
                List<?> list = (List<?>) listObj;
                StringBuilder loopResult = new StringBuilder();
                
                for (Object item : list) {
                    Map<String, Object> loopContext = new HashMap<>(context);
                    loopContext.put(itemVar, item);
                    
                    String itemResult = processVariables(content, loopContext);
                    loopResult.append(itemResult);
                }
                
                matcher.appendReplacement(sb, Matcher.quoteReplacement(loopResult.toString()));
            }
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private String processVariables(String template, Map<String, Object> context) {
        Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String expression = matcher.group(1);
            Object value = resolveExpression(expression, context);
            String replacement = value != null ? value.toString() : "";
            
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private Object resolveExpression(String expression, Map<String, Object> context) {
        String[] parts = expression.split("\\.");
        Object current = context.get(parts[0]);
        
        for (int i = 1; i < parts.length && current != null; i++) {
            try {
                String methodName = "get" + capitalize(parts[i]);
                current = current.getClass().getMethod(methodName).invoke(current);
            } catch (Exception e) {
                return null;
            }
        }
        
        return current;
    }
    
    private boolean isTrue(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return !((String) value).isEmpty();
        if (value instanceof Collection) return !((Collection<?>) value).isEmpty();
        return true;
    }
    
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

/**
 * Example 3: HTML Email Template
 * 
 * Domain object for email
 */
class EmailTemplate {
    private final String template;
    private final AdvancedTemplateEngine engine;
    
    public EmailTemplate(String template) {
        this.template = template;
        this.engine = new AdvancedTemplateEngine();
    }
    
    public String render(Order order, Customer customer) {
        Map<String, Object> context = new HashMap<>();
        context.put("customer", customer);
        context.put("order", order);
        context.put("currentDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        return engine.render(template, context);
    }
}

class Customer {
    private final String name;
    private final String email;
    
    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
}

class Order {
    private final String orderId;
    private final List<OrderItem> items;
    private final double total;
    
    public Order(String orderId, List<OrderItem> items, double total) {
        this.orderId = orderId;
        this.items = items;
        this.total = total;
    }
    
    public String getOrderId() { return orderId; }
    public List<OrderItem> getItems() { return items; }
    public double getTotal() { return total; }
}

class OrderItem {
    private final String name;
    private final int quantity;
    private final double price;
    
    public OrderItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }
    
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getSubtotal() { return quantity * price; }
}

/**
 * Example 4: Report Template
 * 
 * Generates formatted reports
 */
class ReportTemplate {
    private final String template;
    private final AdvancedTemplateEngine engine;
    
    public ReportTemplate(String template) {
        this.template = template;
        this.engine = new AdvancedTemplateEngine();
    }
    
    public String generate(ReportData data) {
        Map<String, Object> context = new HashMap<>();
        context.put("title", data.getTitle());
        context.put("generatedDate", data.getGeneratedDate());
        context.put("rows", data.getRows());
        context.put("total", data.getTotal());
        context.put("showTotal", data.getTotal() > 0);
        
        return engine.render(template, context);
    }
}

class ReportData {
    private final String title;
    private final String generatedDate;
    private final List<ReportRow> rows;
    private final double total;
    
    public ReportData(String title, List<ReportRow> rows) {
        this.title = title;
        this.generatedDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        this.rows = rows;
        this.total = rows.stream().mapToDouble(ReportRow::getValue).sum();
    }
    
    public String getTitle() { return title; }
    public String getGeneratedDate() { return generatedDate; }
    public List<ReportRow> getRows() { return rows; }
    public double getTotal() { return total; }
}

class ReportRow {
    private final String description;
    private final double value;
    
    public ReportRow(String description, double value) {
        this.description = description;
        this.value = value;
    }
    
    public String getDescription() { return description; }
    public double getValue() { return value; }
}

/**
 * Example 5: Multi-Format Template System
 * 
 * Different templates for different formats
 */
class TemplateRegistry {
    private final Map<String, String> templates;
    private final AdvancedTemplateEngine engine;
    
    public TemplateRegistry() {
        this.templates = new HashMap<>();
        this.engine = new AdvancedTemplateEngine();
    }
    
    public void registerTemplate(String name, String template) {
        templates.put(name, template);
        System.out.println("  [Registry] Registered template: " + name);
    }
    
    public String render(String templateName, Map<String, Object> context) {
        String template = templates.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        
        return engine.render(template, context);
    }
}

class Product {
    private final String name;
    private final String description;
    private final double price;
    private final boolean inStock;
    
    public Product(String name, String description, double price, boolean inStock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.inStock = inStock;
    }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public boolean isInStock() { return inStock; }
}

/**
 * Demonstration of the Template View Pattern
 */
public class TemplateViewPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Template View Pattern Demo ===\n");
        
        // Example 1: Simple Template
        System.out.println("1. Simple Template Engine:");
        SimpleTemplateEngine simple = new SimpleTemplateEngine();
        
        String simpleTemplate = "<h1>Hello, {{name}}!</h1>\n<p>Welcome to {{site}}.</p>";
        Map<String, Object> simpleContext = new HashMap<>();
        simpleContext.put("name", "Alice");
        simpleContext.put("site", "My Website");
        
        String simpleResult = simple.render(simpleTemplate, simpleContext);
        System.out.println(simpleResult);
        
        // Example 2: Advanced Template with Conditionals
        System.out.println("\n2. Template with Conditionals:");
        AdvancedTemplateEngine advanced = new AdvancedTemplateEngine();
        
        String conditionalTemplate = 
            "<div>\n" +
            "  <h2>{{title}}</h2>\n" +
            "  {% if premium %}\n" +
            "    <span class='badge'>Premium Member</span>\n" +
            "  {% endif %}\n" +
            "</div>";
        
        Map<String, Object> conditionalContext = new HashMap<>();
        conditionalContext.put("title", "User Dashboard");
        conditionalContext.put("premium", true);
        
        String conditionalResult = advanced.render(conditionalTemplate, conditionalContext);
        System.out.println(conditionalResult);
        
        // Example 3: Email Template
        System.out.println("\n3. Email Order Confirmation:");
        String emailTemplate = 
            "<html>\n" +
            "<body>\n" +
            "  <h1>Order Confirmation</h1>\n" +
            "  <p>Dear {{customer.name}},</p>\n" +
            "  <p>Thank you for your order #{{order.orderId}}</p>\n" +
            "  <table>\n" +
            "    <tr><th>Item</th><th>Qty</th><th>Price</th></tr>\n" +
            "    {% for item in items %}\n" +
            "    <tr><td>{{item.name}}</td><td>{{item.quantity}}</td><td>${{item.price}}</td></tr>\n" +
            "    {% endfor %}\n" +
            "  </table>\n" +
            "  <p><strong>Total: ${{order.total}}</strong></p>\n" +
            "</body>\n" +
            "</html>";
        
        Customer customer = new Customer("Bob Smith", "bob@example.com");
        List<OrderItem> items = Arrays.asList(
            new OrderItem("Laptop", 1, 999.99),
            new OrderItem("Mouse", 2, 29.99)
        );
        Order order = new Order("ORD-12345", items, 1059.97);
        
        EmailTemplate emailTpl = new EmailTemplate(emailTemplate);
        Map<String, Object> emailContext = new HashMap<>();
        emailContext.put("customer", customer);
        emailContext.put("order", order);
        emailContext.put("items", items);
        
        String emailResult = advanced.render(emailTemplate, emailContext);
        System.out.println(emailResult);
        
        // Example 4: Report Template
        System.out.println("\n4. Sales Report:");
        String reportTemplate = 
            "=== {{title}} ===\n" +
            "Generated: {{generatedDate}}\n\n" +
            "{% for row in rows %}\n" +
            "{{row.description}}: ${{row.value}}\n" +
            "{% endfor %}\n" +
            "{% if showTotal %}\n" +
            "-------------------\n" +
            "TOTAL: ${{total}}\n" +
            "{% endif %}";
        
        List<ReportRow> rows = Arrays.asList(
            new ReportRow("Q1 Sales", 50000.0),
            new ReportRow("Q2 Sales", 65000.0),
            new ReportRow("Q3 Sales", 58000.0),
            new ReportRow("Q4 Sales", 72000.0)
        );
        
        ReportData reportData = new ReportData("Annual Sales Report", rows);
        ReportTemplate reportTpl = new ReportTemplate(reportTemplate);
        
        String reportResult = reportTpl.generate(reportData);
        System.out.println(reportResult);
        
        // Example 5: Multi-Format Templates
        System.out.println("\n5. Multi-Format Template System:");
        TemplateRegistry registry = new TemplateRegistry();
        
        // Register HTML template
        registry.registerTemplate("product-html",
            "<div class='product'>\n" +
            "  <h3>{{name}}</h3>\n" +
            "  <p>{{description}}</p>\n" +
            "  <p class='price'>${{price}}</p>\n" +
            "  {% if inStock %}\n" +
            "  <button>Add to Cart</button>\n" +
            "  {% endif %}\n" +
            "</div>");
        
        // Register text template
        registry.registerTemplate("product-text",
            "{{name}}\n" +
            "{{description}}\n" +
            "Price: ${{price}}\n" +
            "{% if inStock %}\n" +
            "Status: In Stock\n" +
            "{% endif %}");
        
        Product product = new Product("Wireless Keyboard", 
            "Ergonomic wireless keyboard with backlight", 79.99, true);
        
        Map<String, Object> productContext = new HashMap<>();
        productContext.put("name", product.getName());
        productContext.put("description", product.getDescription());
        productContext.put("price", product.getPrice());
        productContext.put("inStock", product.isInStock());
        
        System.out.println("\nHTML Format:");
        String htmlProduct = registry.render("product-html", productContext);
        System.out.println(htmlProduct);
        
        System.out.println("\nText Format:");
        String textProduct = registry.render("product-text", productContext);
        System.out.println(textProduct);
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Separates presentation from logic");
        System.out.println("✓ Designers can work independently");
        System.out.println("✓ Easy to change presentation");
        System.out.println("✓ Multiple formats from same data");
        System.out.println("✓ Template reusability");
        
        System.out.println("\n=== Use Cases ===");
        System.out.println("• HTML page generation");
        System.out.println("• Email templates");
        System.out.println("• Report generation");
        System.out.println("• PDF documents");
        System.out.println("• Multi-language support");
    }
}
