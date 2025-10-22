package additional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Value Object Pattern
 * 
 * Intent: A small object that represents a simple entity whose equality
 * is based on value rather than identity. Immutable and has no identity.
 * 
 * Motivation:
 * Some objects are defined by their attributes, not by identity.
 * Should be immutable to ensure consistency.
 * Can be freely shared and compared by value.
 * 
 * Applicability:
 * - Money, dates, coordinates, addresses
 * - Objects that describe properties
 * - Need value-based equality
 * - Domain-Driven Design value objects
 */

/**
 * Example 1: Money Value Object
 * 
 * Immutable money object with currency
 */
class Money {
    private final BigDecimal amount;
    private final String currency;
    
    public Money(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency cannot be null");
        }
        this.amount = amount;
        this.currency = currency;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    // Value objects define operations that return new instances
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
    
    public Money multiply(double factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }
    
    // Value-based equality
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;
        Money other = (Money) obj;
        return amount.equals(other.amount) && currency.equals(other.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return amount + " " + currency;
    }
}

/**
 * Example 2: Address Value Object
 * 
 * Immutable address with validation
 */
class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;
    
    public Address(String street, String city, String state, String zipCode, String country) {
        if (street == null || city == null || zipCode == null || country == null) {
            throw new IllegalArgumentException("Required address fields cannot be null");
        }
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getCountry() { return country; }
    
    // Create new instance with modified field
    public Address withStreet(String newStreet) {
        return new Address(newStreet, city, state, zipCode, country);
    }
    
    public Address withCity(String newCity) {
        return new Address(street, newCity, state, zipCode, country);
    }
    
    public String getFullAddress() {
        return street + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Address)) return false;
        Address other = (Address) obj;
        return Objects.equals(street, other.street) &&
               Objects.equals(city, other.city) &&
               Objects.equals(state, other.state) &&
               Objects.equals(zipCode, other.zipCode) &&
               Objects.equals(country, other.country);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, zipCode, country);
    }
    
    @Override
    public String toString() {
        return getFullAddress();
    }
}

/**
 * Example 3: DateRange Value Object
 * 
 * Immutable date range with business logic
 */
class DateRange {
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    public DateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    
    public long getDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    public boolean includes(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    public boolean overlaps(DateRange other) {
        return !this.endDate.isBefore(other.startDate) && 
               !other.endDate.isBefore(this.startDate);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DateRange)) return false;
        DateRange other = (DateRange) obj;
        return startDate.equals(other.startDate) && endDate.equals(other.endDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }
    
    @Override
    public String toString() {
        return startDate + " to " + endDate + " (" + getDays() + " days)";
    }
}

/**
 * Example 4: Coordinate Value Object
 * 
 * Immutable geographic coordinates
 */
class Coordinate {
    private final double latitude;
    private final double longitude;
    
    public Coordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    
    // Calculate distance to another coordinate (Haversine formula)
    public double distanceTo(Coordinate other) {
        final int R = 6371; // Earth radius in km
        
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinate)) return false;
        Coordinate other = (Coordinate) obj;
        return Double.compare(latitude, other.latitude) == 0 &&
               Double.compare(longitude, other.longitude) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
    
    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}

/**
 * Example 5: Email Value Object
 * 
 * Validated email address
 */
class Email {
    private final String address;
    
    public Email(String address) {
        if (address == null || !isValid(address)) {
            throw new IllegalArgumentException("Invalid email address: " + address);
        }
        this.address = address.toLowerCase();
    }
    
    private boolean isValid(String email) {
        // Simple validation
        return email != null && 
               email.contains("@") && 
               email.indexOf("@") > 0 && 
               email.indexOf("@") < email.length() - 1;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getDomain() {
        return address.substring(address.indexOf("@") + 1);
    }
    
    public String getLocalPart() {
        return address.substring(0, address.indexOf("@"));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Email)) return false;
        Email other = (Email) obj;
        return address.equals(other.address);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
    
    @Override
    public String toString() {
        return address;
    }
}

/**
 * Example 6: Using Value Objects in Entity
 * 
 * Entity uses value objects for properties
 */
class Customer {
    private final Long id; // Entity has identity
    private String name;
    private Email email; // Value object
    private Address address; // Value object
    
    public Customer(Long id, String name, Email email, Address address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
    }
    
    public Long getId() { return id; }
    public String getName() { return name; }
    public Email getEmail() { return email; }
    public Address getAddress() { return address; }
    
    // Update using new value object instances
    public void updateEmail(Email newEmail) {
        this.email = newEmail;
        System.out.println("  [Customer] Email updated to: " + newEmail);
    }
    
    public void updateAddress(Address newAddress) {
        this.address = newAddress;
        System.out.println("  [Customer] Address updated to: " + newAddress);
    }
    
    @Override
    public String toString() {
        return "Customer[" + id + "]: " + name + " - " + email;
    }
}

/**
 * Demonstration of the Value Object Pattern
 */
public class ValueObjectPattern {
    
    public static void main(String[] args) {
        System.out.println("=== Value Object Pattern Demo ===\n");
        
        // Example 1: Money Value Object
        System.out.println("1. Money Value Object:");
        Money price1 = new Money(new BigDecimal("100.00"), "USD");
        Money price2 = new Money(new BigDecimal("50.00"), "USD");
        Money total = price1.add(price2);
        System.out.println("  " + price1 + " + " + price2 + " = " + total);
        
        Money discounted = total.multiply(0.9);
        System.out.println("  10% discount: " + discounted);
        
        System.out.println("  Are equal? " + price1.equals(new Money(new BigDecimal("100.00"), "USD")));
        
        // Example 2: Address Value Object
        System.out.println("\n2. Address Value Object:");
        Address addr1 = new Address("123 Main St", "Springfield", "IL", "62701", "USA");
        System.out.println("  Address: " + addr1);
        
        Address addr2 = addr1.withStreet("456 Oak Ave");
        System.out.println("  New address: " + addr2);
        System.out.println("  Original unchanged: " + addr1);
        
        // Example 3: DateRange Value Object
        System.out.println("\n3. DateRange Value Object:");
        DateRange range1 = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10));
        DateRange range2 = new DateRange(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));
        
        System.out.println("  Range 1: " + range1);
        System.out.println("  Range 2: " + range2);
        System.out.println("  Overlaps? " + range1.overlaps(range2));
        System.out.println("  Includes Jan 7? " + range1.includes(LocalDate.of(2025, 1, 7)));
        
        // Example 4: Coordinate Value Object
        System.out.println("\n4. Coordinate Value Object:");
        Coordinate newYork = new Coordinate(40.7128, -74.0060);
        Coordinate london = new Coordinate(51.5074, -0.1278);
        
        System.out.println("  New York: " + newYork);
        System.out.println("  London: " + london);
        System.out.println("  Distance: " + String.format("%.2f", newYork.distanceTo(london)) + " km");
        
        // Example 5: Email Value Object
        System.out.println("\n5. Email Value Object:");
        Email email1 = new Email("user@example.com");
        Email email2 = new Email("USER@EXAMPLE.COM");
        
        System.out.println("  Email 1: " + email1);
        System.out.println("  Email 2: " + email2);
        System.out.println("  Are equal? " + email1.equals(email2));
        System.out.println("  Domain: " + email1.getDomain());
        
        // Example 6: Entity with Value Objects
        System.out.println("\n6. Entity with Value Objects:");
        Customer customer = new Customer(
            1L,
            "John Doe",
            new Email("john@example.com"),
            new Address("789 Elm St", "Boston", "MA", "02101", "USA")
        );
        
        System.out.println("  " + customer);
        System.out.println("  Address: " + customer.getAddress());
        
        customer.updateEmail(new Email("john.doe@example.com"));
        customer.updateAddress(new Address("999 Pine St", "Boston", "MA", "02102", "USA"));
        
        System.out.println("\n=== Pattern Benefits ===");
        System.out.println("✓ Immutable and thread-safe");
        System.out.println("✓ Value-based equality");
        System.out.println("✓ No identity, defined by attributes");
        System.out.println("✓ Can be freely shared");
        System.out.println("✓ Encapsulates validation logic");
        
        System.out.println("\n=== vs Entity ===");
        System.out.println("• Value Object: No identity, equality by value");
        System.out.println("• Entity: Has identity, equality by ID");
        System.out.println("• Value Objects are immutable");
        System.out.println("• Entities have lifecycle and mutable state");
    }
}
