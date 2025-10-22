"""
Factory Method Pattern
Purpose: Defines an interface for creating objects but lets subclasses decide which class to instantiate
Use Case: Creating UI components, document types, database connections
"""

from abc import ABC, abstractmethod
from enum import Enum
from typing import Dict, List


# Example 1: Document Creation
class Document(ABC):
    """Abstract document"""
    @abstractmethod
    def open(self) -> str:
        pass
    
    @abstractmethod
    def save(self) -> str:
        pass


class PDFDocument(Document):
    def open(self) -> str:
        return "Opening PDF document"
    
    def save(self) -> str:
        return "Saving PDF document"


class WordDocument(Document):
    def open(self) -> str:
        return "Opening Word document"
    
    def save(self) -> str:
        return "Saving Word document"


class ExcelDocument(Document):
    def open(self) -> str:
        return "Opening Excel document"
    
    def save(self) -> str:
        return "Saving Excel document"


class DocumentCreator(ABC):
    """Abstract creator"""
    @abstractmethod
    def create_document(self) -> Document:
        pass
    
    def new_document(self) -> str:
        """Template method using factory method"""
        doc = self.create_document()
        result = doc.open()
        return result


class PDFCreator(DocumentCreator):
    def create_document(self) -> Document:
        return PDFDocument()


class WordCreator(DocumentCreator):
    def create_document(self) -> Document:
        return WordDocument()


class ExcelCreator(DocumentCreator):
    def create_document(self) -> Document:
        return ExcelDocument()


# Example 2: Transportation System
class Transport(ABC):
    @abstractmethod
    def deliver(self) -> str:
        pass


class Truck(Transport):
    def deliver(self) -> str:
        return "Delivering by land in a truck"


class Ship(Transport):
    def deliver(self) -> str:
        return "Delivering by sea in a ship"


class Plane(Transport):
    def deliver(self) -> str:
        return "Delivering by air in a plane"


class Logistics(ABC):
    @abstractmethod
    def create_transport(self) -> Transport:
        pass
    
    def plan_delivery(self) -> str:
        transport = self.create_transport()
        return transport.deliver()


class RoadLogistics(Logistics):
    def create_transport(self) -> Transport:
        return Truck()


class SeaLogistics(Logistics):
    def create_transport(self) -> Transport:
        return Ship()


class AirLogistics(Logistics):
    def create_transport(self) -> Transport:
        return Plane()


# Example 3: Payment Processing
class PaymentMethod(ABC):
    @abstractmethod
    def process_payment(self, amount: float) -> str:
        pass


class CreditCardPayment(PaymentMethod):
    def process_payment(self, amount: float) -> str:
        return f"Processing ${amount} via Credit Card"


class PayPalPayment(PaymentMethod):
    def process_payment(self, amount: float) -> str:
        return f"Processing ${amount} via PayPal"


class CryptoPayment(PaymentMethod):
    def process_payment(self, amount: float) -> str:
        return f"Processing ${amount} via Cryptocurrency"


class PaymentProcessor(ABC):
    @abstractmethod
    def create_payment_method(self) -> PaymentMethod:
        pass
    
    def process(self, amount: float) -> str:
        method = self.create_payment_method()
        return method.process_payment(amount)


class CreditCardProcessor(PaymentProcessor):
    def create_payment_method(self) -> PaymentMethod:
        return CreditCardPayment()


class PayPalProcessor(PaymentProcessor):
    def create_payment_method(self) -> PaymentMethod:
        return PayPalPayment()


class CryptoProcessor(PaymentProcessor):
    def create_payment_method(self) -> PaymentMethod:
        return CryptoPayment()


# Example 4: Notification System
class Notification(ABC):
    @abstractmethod
    def send(self, message: str) -> str:
        pass


class EmailNotification(Notification):
    def send(self, message: str) -> str:
        return f"Sending email: {message}"


class SMSNotification(Notification):
    def send(self, message: str) -> str:
        return f"Sending SMS: {message}"


class PushNotification(Notification):
    def send(self, message: str) -> str:
        return f"Sending push notification: {message}"


class NotificationFactory(ABC):
    @abstractmethod
    def create_notification(self) -> Notification:
        pass
    
    def notify(self, message: str) -> str:
        notification = self.create_notification()
        return notification.send(message)


class EmailFactory(NotificationFactory):
    def create_notification(self) -> Notification:
        return EmailNotification()


class SMSFactory(NotificationFactory):
    def create_notification(self) -> Notification:
        return SMSNotification()


class PushFactory(NotificationFactory):
    def create_notification(self) -> Notification:
        return PushNotification()


# Example 5: Simple Factory (Parameterized)
class Button(ABC):
    @abstractmethod
    def render(self) -> str:
        pass


class WindowsButton(Button):
    def render(self) -> str:
        return "Rendering Windows button"


class MacButton(Button):
    def render(self) -> str:
        return "Rendering Mac button"


class LinuxButton(Button):
    def render(self) -> str:
        return "Rendering Linux button"


class ButtonFactory:
    """Parameterized factory method"""
    @staticmethod
    def create_button(platform: str) -> Button:
        if platform == "Windows":
            return WindowsButton()
        elif platform == "Mac":
            return MacButton()
        elif platform == "Linux":
            return LinuxButton()
        else:
            raise ValueError(f"Unknown platform: {platform}")


def demonstrate_factory_method():
    """Demonstrate factory method pattern"""
    print("=== Factory Method Pattern Demo ===\n")
    
    # Example 1: Document Creation
    print("--- Example 1: Document Creation ---")
    pdf_creator = PDFCreator()
    word_creator = WordCreator()
    print(pdf_creator.new_document())
    print(word_creator.new_document())
    
    # Example 2: Transportation System
    print("\n--- Example 2: Transportation System ---")
    road = RoadLogistics()
    sea = SeaLogistics()
    air = AirLogistics()
    print(road.plan_delivery())
    print(sea.plan_delivery())
    print(air.plan_delivery())
    
    # Example 3: Payment Processing
    print("\n--- Example 3: Payment Processing ---")
    credit_card = CreditCardProcessor()
    paypal = PayPalProcessor()
    crypto = CryptoProcessor()
    print(credit_card.process(100.0))
    print(paypal.process(150.0))
    print(crypto.process(200.0))
    
    # Example 4: Notification System
    print("\n--- Example 4: Notification System ---")
    email = EmailFactory()
    sms = SMSFactory()
    push = PushFactory()
    print(email.notify("Welcome to our service!"))
    print(sms.notify("Your code is 123456"))
    print(push.notify("New message received"))
    
    # Example 5: Simple Factory
    print("\n--- Example 5: Simple Factory (Parameterized) ---")
    for platform in ["Windows", "Mac", "Linux"]:
        button = ButtonFactory.create_button(platform)
        print(button.render())
    
    print("\n=== Key Concepts ===")
    print("1. Factory Method - Subclasses decide which class to instantiate")
    print("2. Creator - Abstract class with factory method")
    print("3. Concrete Creators - Implement factory method")
    print("4. Product - Interface for objects factory method creates")
    
    print("\n=== Benefits ===")
    print("+ Loose coupling between creator and products")
    print("+ Single Responsibility Principle")
    print("+ Open/Closed Principle")
    print("+ Easy to add new product types")
    
    print("\n=== When to Use ===")
    print("• Don't know exact types/dependencies beforehand")
    print("• Want to provide extension points")
    print("• Want to save system resources by reusing objects")


if __name__ == "__main__":
    demonstrate_factory_method()
