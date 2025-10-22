"""Module Pattern - Groups related code into a single unit"""
class DatabaseModule:
    """Module for database operations"""
    
    def __init__(self):
        self._connection = None
        self._connected = False
    
    def connect(self, connection_string):
        print(f"Connecting to: {connection_string}")
        self._connection = connection_string
        self._connected = True
    
    def query(self, sql):
        if not self._connected:
            raise Exception("Not connected to database")
        print(f"Executing: {sql}")
        return [{"id": 1, "name": "Result"}]
    
    def disconnect(self):
        print("Disconnecting...")
        self._connected = False

class ValidationModule:
    """Module for validation operations"""
    
    @staticmethod
    def validate_email(email):
        return "@" in email and "." in email
    
    @staticmethod
    def validate_phone(phone):
        return phone.isdigit() and len(phone) >= 10

class UtilityModule:
    """Module for utility operations"""
    
    @staticmethod
    def format_currency(amount):
        return f"${amount:.2f}"
    
    @staticmethod
    def format_date(date):
        return date.strftime("%Y-%m-%d") if hasattr(date, 'strftime') else str(date)

if __name__ == "__main__":
    # Use database module
    db = DatabaseModule()
    db.connect("postgresql://localhost/mydb")
    results = db.query("SELECT * FROM users")
    print(f"Results: {results}")
    
    # Use validation module
    print(f"\nEmail valid: {ValidationModule.validate_email('user@example.com')}")
    print(f"Phone valid: {ValidationModule.validate_phone('1234567890')}")
    
    # Use utility module
    print(f"\nFormatted: {UtilityModule.format_currency(1234.56)}")
