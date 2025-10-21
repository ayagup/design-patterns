package behavioral;

import java.util.*;

/**
 * Observer Pattern (Publish-Subscribe)
 * Defines a one-to-many dependency between objects.
 */
public class ObserverPattern {
    
    // Subject interface
    interface Subject {
        void attach(Observer observer);
        void detach(Observer observer);
        void notifyObservers();
    }
    
    // Observer interface
    interface Observer {
        void update(String message);
    }
    
    // Concrete Subject - News Agency
    static class NewsAgency implements Subject {
        private List<Observer> observers = new ArrayList<>();
        private String news;
        
        @Override
        public void attach(Observer observer) {
            observers.add(observer);
            System.out.println("Observer attached");
        }
        
        @Override
        public void detach(Observer observer) {
            observers.remove(observer);
            System.out.println("Observer detached");
        }
        
        @Override
        public void notifyObservers() {
            for (Observer observer : observers) {
                observer.update(news);
            }
        }
        
        public void setNews(String news) {
            this.news = news;
            System.out.println("\n📰 Breaking News: " + news);
            notifyObservers();
        }
    }
    
    // Concrete Observers
    static class NewsChannel implements Observer {
        private String name;
        
        public NewsChannel(String name) {
            this.name = name;
        }
        
        @Override
        public void update(String message) {
            System.out.println("📺 " + name + " received: " + message);
        }
    }
    
    static class Newspaper implements Observer {
        private String name;
        
        public Newspaper(String name) {
            this.name = name;
        }
        
        @Override
        public void update(String message) {
            System.out.println("📄 " + name + " printing: " + message);
        }
    }
    
    // Stock market example
    static class Stock {
        private List<StockObserver> observers = new ArrayList<>();
        private String symbol;
        private double price;
        
        public Stock(String symbol, double price) {
            this.symbol = symbol;
            this.price = price;
        }
        
        public void addObserver(StockObserver observer) {
            observers.add(observer);
        }
        
        public void removeObserver(StockObserver observer) {
            observers.remove(observer);
        }
        
        public void setPrice(double price) {
            this.price = price;
            notifyObservers();
        }
        
        private void notifyObservers() {
            System.out.println("\n📈 " + symbol + " price changed to $" + price);
            for (StockObserver observer : observers) {
                observer.priceChanged(symbol, price);
            }
        }
    }
    
    interface StockObserver {
        void priceChanged(String symbol, double price);
    }
    
    static class Investor implements StockObserver {
        private String name;
        
        public Investor(String name) {
            this.name = name;
        }
        
        @Override
        public void priceChanged(String symbol, double price) {
            System.out.println("👤 Investor " + name + " notified: " + 
                             symbol + " = $" + price);
        }
    }
    
    static class TradingBot implements StockObserver {
        private String name;
        private double buyThreshold;
        
        public TradingBot(String name, double buyThreshold) {
            this.name = name;
            this.buyThreshold = buyThreshold;
        }
        
        @Override
        public void priceChanged(String symbol, double price) {
            System.out.println("🤖 Bot " + name + " analyzing: " + 
                             symbol + " = $" + price);
            if (price < buyThreshold) {
                System.out.println("   → BUY signal triggered!");
            }
        }
    }
    
    // Weather station example
    static class WeatherStation {
        private List<WeatherObserver> observers = new ArrayList<>();
        private float temperature;
        private float humidity;
        
        public void addObserver(WeatherObserver observer) {
            observers.add(observer);
        }
        
        public void setMeasurements(float temperature, float humidity) {
            this.temperature = temperature;
            this.humidity = humidity;
            notifyObservers();
        }
        
        private void notifyObservers() {
            for (WeatherObserver observer : observers) {
                observer.update(temperature, humidity);
            }
        }
    }
    
    interface WeatherObserver {
        void update(float temperature, float humidity);
    }
    
    static class PhoneDisplay implements WeatherObserver {
        @Override
        public void update(float temperature, float humidity) {
            System.out.println("📱 Phone Display: " + temperature + "°C, " + 
                             humidity + "% humidity");
        }
    }
    
    static class WebDisplay implements WeatherObserver {
        @Override
        public void update(float temperature, float humidity) {
            System.out.println("🌐 Web Display: " + temperature + "°C, " + 
                             humidity + "% humidity");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Observer Pattern Demo ===\n");
        
        // News agency example
        System.out.println("1. News Agency (Subject) with Multiple Channels:");
        NewsAgency agency = new NewsAgency();
        
        NewsChannel cnn = new NewsChannel("CNN");
        NewsChannel bbc = new NewsChannel("BBC");
        Newspaper nyt = new Newspaper("New York Times");
        
        agency.attach(cnn);
        agency.attach(bbc);
        agency.attach(nyt);
        
        agency.setNews("Major tech breakthrough announced!");
        agency.setNews("Economy shows signs of recovery");
        
        System.out.println("\n" + "Detaching BBC...");
        agency.detach(bbc);
        
        agency.setNews("Sports: Team wins championship!");
        
        // Stock market example
        System.out.println("\n\n2. Stock Market Observers:");
        Stock appleStock = new Stock("AAPL", 150.00);
        
        Investor investor1 = new Investor("Alice");
        Investor investor2 = new Investor("Bob");
        TradingBot bot = new TradingBot("AutoTrader", 145.00);
        
        appleStock.addObserver(investor1);
        appleStock.addObserver(investor2);
        appleStock.addObserver(bot);
        
        appleStock.setPrice(155.00);
        appleStock.setPrice(140.00);
        
        // Weather station example
        System.out.println("\n\n3. Weather Station:");
        WeatherStation station = new WeatherStation();
        
        PhoneDisplay phoneDisplay = new PhoneDisplay();
        WebDisplay webDisplay = new WebDisplay();
        
        station.addObserver(phoneDisplay);
        station.addObserver(webDisplay);
        
        System.out.println("Weather update 1:");
        station.setMeasurements(25.5f, 65.0f);
        
        System.out.println("\nWeather update 2:");
        station.setMeasurements(28.0f, 70.0f);
        
        System.out.println("\n--- Benefits ---");
        System.out.println("✓ Loose coupling between subject and observers");
        System.out.println("✓ Dynamic relationships at runtime");
        System.out.println("✓ Broadcast communication");
        System.out.println("✓ Supports Open/Closed Principle");
        
        System.out.println("\n--- Use Cases ---");
        System.out.println("• Event handling systems");
        System.out.println("• MVC architecture");
        System.out.println("• Real-time data feeds");
        System.out.println("• Notification systems");
    }
}
