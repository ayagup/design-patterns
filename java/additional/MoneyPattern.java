package additional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * MONEY PATTERN
 * 
 * Represents monetary values with proper handling of currency, rounding, and arithmetic
 * operations. Avoids floating-point precision issues and ensures consistent currency handling.
 * 
 * Benefits:
 * - Precise monetary calculations (no floating-point errors)
 * - Currency-aware operations
 * - Prevents mixing different currencies
 * - Immutable value object
 * - Type-safe money representation
 * 
 * Use Cases:
 * - Financial applications
 * - E-commerce systems
 * - Accounting software
 * - Payment processing
 * - Multi-currency systems
 */

// Money Value Object
class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency cannot be null");
        }
        // Scale to currency's default fraction digits
        this.amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
        this.currency = currency;
    }
    
    public Money(double amount, Currency currency) {
        this(BigDecimal.valueOf(amount), currency);
    }
    
    public Money(String amount, Currency currency) {
        this(new BigDecimal(amount), currency);
    }
    
    // Factory methods for common currencies
    public static Money usd(double amount) {
        return new Money(amount, Currency.getInstance("USD"));
    }
    
    public static Money eur(double amount) {
        return new Money(amount, Currency.getInstance("EUR"));
    }
    
    public static Money gbp(double amount) {
        return new Money(amount, Currency.getInstance("GBP"));
    }
    
    public static Money jpy(double amount) {
        return new Money(amount, Currency.getInstance("JPY"));
    }
    
    // Arithmetic operations
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(double multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }
    
    public Money multiply(BigDecimal multiplier) {
        return new Money(this.amount.multiply(multiplier), this.currency);
    }
    
    public Money divide(double divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return new Money(
            this.amount.divide(BigDecimal.valueOf(divisor), currency.getDefaultFractionDigits(), 
            RoundingMode.HALF_EVEN), 
            this.currency
        );
    }
    
    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }
    
    public Money abs() {
        return new Money(this.amount.abs(), this.currency);
    }
    
    // Comparison operations
    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    public boolean isLessThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    public boolean isLessThanOrEqual(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) <= 0;
    }
    
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
    
    // Allocation operations
    public List<Money> allocate(int[] ratios) {
        int totalRatio = Arrays.stream(ratios).sum();
        BigDecimal totalRatioBD = BigDecimal.valueOf(totalRatio);
        
        List<Money> results = new ArrayList<>();
        BigDecimal remainder = this.amount;
        
        for (int i = 0; i < ratios.length; i++) {
            BigDecimal ratio = BigDecimal.valueOf(ratios[i]);
            BigDecimal share = this.amount.multiply(ratio)
                .divide(totalRatioBD, currency.getDefaultFractionDigits(), RoundingMode.DOWN);
            results.add(new Money(share, this.currency));
            remainder = remainder.subtract(share);
        }
        
        // Distribute remainder to first allocation(s) to avoid rounding loss
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            Money first = results.get(0);
            results.set(0, first.add(new Money(remainder, this.currency)));
        }
        
        return results;
    }
    
    public List<Money> allocateEqually(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of parts must be positive");
        }
        
        BigDecimal share = this.amount.divide(
            BigDecimal.valueOf(n), 
            currency.getDefaultFractionDigits(), 
            RoundingMode.DOWN
        );
        
        List<Money> results = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (int i = 0; i < n; i++) {
            results.add(new Money(share, this.currency));
            total = total.add(share);
        }
        
        // Distribute remainder
        BigDecimal remainder = this.amount.subtract(total);
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            Money first = results.get(0);
            results.set(0, first.add(new Money(remainder, this.currency)));
        }
        
        return results;
    }
    
    // Getters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    // Helper methods
    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot operate on different currencies: " + 
                this.currency + " and " + other.currency
            );
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;
        Money other = (Money) obj;
        return this.amount.compareTo(other.amount) == 0 && 
               this.currency.equals(other.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return currency.getSymbol() + amount.toPlainString();
    }
    
    public String toFormattedString() {
        return String.format("%s %.2f", currency.getCurrencyCode(), amount.doubleValue());
    }
}

// Shopping Cart using Money
class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    private final Currency currency;
    
    public ShoppingCart(Currency currency) {
        this.currency = currency;
    }
    
    public void addItem(String name, Money price, int quantity) {
        System.out.println("  🛒 Adding: " + name + " x" + quantity + " @ " + price);
        items.add(new CartItem(name, price, quantity));
    }
    
    public Money getSubtotal() {
        Money subtotal = new Money(BigDecimal.ZERO, currency);
        for (CartItem item : items) {
            subtotal = subtotal.add(item.getTotal());
        }
        return subtotal;
    }
    
    public Money getTax(double taxRate) {
        return getSubtotal().multiply(taxRate);
    }
    
    public Money getTotal(double taxRate) {
        return getSubtotal().add(getTax(taxRate));
    }
    
    public void printReceipt(double taxRate) {
        System.out.println("\n  📄 RECEIPT");
        System.out.println("  " + "=".repeat(40));
        for (CartItem item : items) {
            System.out.println("  " + item.getName() + " x" + item.getQuantity() + 
                             " @ " + item.getPrice() + " = " + item.getTotal());
        }
        System.out.println("  " + "-".repeat(40));
        System.out.println("  Subtotal: " + getSubtotal());
        System.out.println("  Tax (" + (taxRate * 100) + "%): " + getTax(taxRate));
        System.out.println("  " + "=".repeat(40));
        System.out.println("  TOTAL: " + getTotal(taxRate));
    }
    
    static class CartItem {
        private final String name;
        private final Money price;
        private final int quantity;
        
        public CartItem(String name, Money price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        
        public Money getTotal() {
            return price.multiply(quantity);
        }
        
        public String getName() { return name; }
        public Money getPrice() { return price; }
        public int getQuantity() { return quantity; }
    }
}

// Bill Splitting
class BillSplitter {
    private final Money totalAmount;
    
    public BillSplitter(Money totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public void splitEqually(int people) {
        System.out.println("\n💰 Splitting " + totalAmount + " equally among " + people + " people:");
        
        List<Money> shares = totalAmount.allocateEqually(people);
        
        for (int i = 0; i < shares.size(); i++) {
            System.out.println("  Person " + (i + 1) + ": " + shares.get(i));
        }
        
        // Verify total
        Money total = shares.stream()
            .reduce(Money::add)
            .orElse(new Money(BigDecimal.ZERO, totalAmount.getCurrency()));
        
        System.out.println("  ✅ Total verification: " + total + " (original: " + totalAmount + ")");
    }
    
    public void splitByRatio(String[] names, int[] ratios) {
        System.out.println("\n💰 Splitting " + totalAmount + " by ratio:");
        
        List<Money> shares = totalAmount.allocate(ratios);
        
        int totalRatio = Arrays.stream(ratios).sum();
        for (int i = 0; i < names.length; i++) {
            double percentage = (ratios[i] * 100.0) / totalRatio;
            System.out.println("  " + names[i] + " (" + ratios[i] + "/" + totalRatio + 
                             " = " + String.format("%.1f", percentage) + "%): " + shares.get(i));
        }
    }
}

// Currency Converter
class CurrencyConverter {
    private final Map<String, Double> exchangeRates = new HashMap<>();
    
    public CurrencyConverter() {
        // Sample exchange rates (relative to USD)
        exchangeRates.put("USD_EUR", 0.92);
        exchangeRates.put("USD_GBP", 0.79);
        exchangeRates.put("USD_JPY", 149.50);
        exchangeRates.put("EUR_USD", 1.09);
        exchangeRates.put("GBP_USD", 1.27);
        exchangeRates.put("JPY_USD", 0.0067);
    }
    
    public Money convert(Money source, Currency targetCurrency) {
        if (source.getCurrency().equals(targetCurrency)) {
            return source;
        }
        
        String key = source.getCurrency().getCurrencyCode() + "_" + 
                     targetCurrency.getCurrencyCode();
        
        Double rate = exchangeRates.get(key);
        if (rate == null) {
            throw new IllegalArgumentException("No exchange rate available for " + key);
        }
        
        BigDecimal convertedAmount = source.getAmount().multiply(BigDecimal.valueOf(rate));
        Money result = new Money(convertedAmount, targetCurrency);
        
        System.out.println("  💱 Converting: " + source + " → " + result + 
                         " (rate: " + rate + ")");
        
        return result;
    }
}

// Demo
public class MoneyPattern {
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   MONEY PATTERN DEMONSTRATION        ║");
        System.out.println("╚══════════════════════════════════════╝");
        
        // Example 1: Basic arithmetic operations
        System.out.println("\n1. BASIC ARITHMETIC OPERATIONS");
        System.out.println("───────────────────────────────");
        
        Money price1 = Money.usd(100.50);
        Money price2 = Money.usd(50.25);
        
        System.out.println("  Price 1: " + price1);
        System.out.println("  Price 2: " + price2);
        System.out.println("  Add: " + price1.add(price2));
        System.out.println("  Subtract: " + price1.subtract(price2));
        System.out.println("  Multiply by 2: " + price1.multiply(2));
        System.out.println("  Divide by 3: " + price1.divide(3));
        
        // Example 2: Comparison operations
        System.out.println("\n2. COMPARISON OPERATIONS");
        System.out.println("────────────────────────");
        
        Money amount1 = Money.usd(100);
        Money amount2 = Money.usd(150);
        
        System.out.println("  Amount 1: " + amount1);
        System.out.println("  Amount 2: " + amount2);
        System.out.println("  Amount 1 > Amount 2: " + amount1.isGreaterThan(amount2));
        System.out.println("  Amount 1 < Amount 2: " + amount1.isLessThan(amount2));
        System.out.println("  Amount 1 is positive: " + amount1.isPositive());
        
        // Example 3: Shopping cart
        System.out.println("\n3. SHOPPING CART (Money in E-commerce)");
        System.out.println("───────────────────────────────────────");
        
        ShoppingCart cart = new ShoppingCart(Currency.getInstance("USD"));
        cart.addItem("Laptop", Money.usd(1299.99), 1);
        cart.addItem("Mouse", Money.usd(29.99), 2);
        cart.addItem("Keyboard", Money.usd(89.99), 1);
        
        cart.printReceipt(0.08); // 8% tax
        
        // Example 4: Bill splitting (equal)
        System.out.println("\n4. BILL SPLITTING (Equal Shares)");
        System.out.println("─────────────────────────────────");
        
        Money billTotal = Money.usd(127.89);
        BillSplitter splitter = new BillSplitter(billTotal);
        splitter.splitEqually(3);
        
        // Example 5: Bill splitting (by ratio)
        System.out.println("\n5. BILL SPLITTING (By Ratio)");
        System.out.println("─────────────────────────────");
        
        Money investmentTotal = Money.usd(10000);
        BillSplitter investmentSplit = new BillSplitter(investmentTotal);
        investmentSplit.splitByRatio(
            new String[]{"Alice", "Bob", "Charlie"},
            new int[]{5, 3, 2} // 50%, 30%, 20%
        );
        
        // Example 6: Currency conversion
        System.out.println("\n6. CURRENCY CONVERSION");
        System.out.println("──────────────────────");
        
        CurrencyConverter converter = new CurrencyConverter();
        
        Money usdAmount = Money.usd(1000);
        System.out.println("  Original: " + usdAmount);
        
        Money eurAmount = converter.convert(usdAmount, Currency.getInstance("EUR"));
        Money gbpAmount = converter.convert(usdAmount, Currency.getInstance("GBP"));
        Money jpyAmount = converter.convert(usdAmount, Currency.getInstance("JPY"));
        
        // Example 7: Preventing currency mixing
        System.out.println("\n7. CURRENCY SAFETY (Preventing Mixing)");
        System.out.println("───────────────────────────────────────");
        
        Money usd = Money.usd(100);
        Money eur = Money.eur(100);
        
        System.out.println("  USD amount: " + usd);
        System.out.println("  EUR amount: " + eur);
        
        try {
            Money invalid = usd.add(eur); // This will throw an exception
        } catch (IllegalArgumentException e) {
            System.out.println("  ❌ Cannot mix currencies: " + e.getMessage());
        }
        
        System.out.println("\n✅ Money Pattern demonstration completed!");
        System.out.println("\n📊 Pattern Benefits:");
        System.out.println("  • Precise decimal arithmetic (no floating-point errors)");
        System.out.println("  • Currency-aware operations");
        System.out.println("  • Prevents mixing different currencies");
        System.out.println("  • Immutable value object");
        System.out.println("  • Type-safe monetary values");
        
        System.out.println("\n⚠️  Why not use double for money?");
        System.out.println("  • 0.1 + 0.2 = 0.30000000000000004 (floating-point error)");
        System.out.println("  • Use BigDecimal for precise decimal arithmetic");
        System.out.println("  • Money pattern encapsulates this complexity");
    }
}
