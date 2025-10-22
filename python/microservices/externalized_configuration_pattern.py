"""Externalized Configuration - Config outside services"""
class ConfigurationService:
    def __init__(self):
        self.configs = {
            "database_url": "postgresql://localhost:5432/db",
            "max_connections": 100,
            "timeout": 30
        }
    
    def get(self, key):
        return self.configs.get(key)
    
    def set(self, key, value):
        self.configs[key] = value
        print(f"Configuration updated: {key} = {value}")

class ApplicationService:
    def __init__(self, config_service):
        self.config = config_service
    
    def get_database_url(self):
        return self.config.get("database_url")
    
    def get_timeout(self):
        return self.config.get("timeout")

if __name__ == "__main__":
    config = ConfigurationService()
    app = ApplicationService(config)
    
    print(f"Database URL: {app.get_database_url()}")
    print(f"Timeout: {app.get_timeout()}")
    
    # Update configuration without redeploying
    config.set("timeout", 60)
    print(f"New timeout: {app.get_timeout()}")
