"""External Configuration Store - Centralized configuration"""
import json

class ConfigurationStore:
    def __init__(self):
        self._config = {
            "database": {
                "host": "localhost",
                "port": 5432,
                "username": "admin"
            },
            "features": {
                "feature_x_enabled": True,
                "max_connections": 100
            },
            "logging": {
                "level": "INFO",
                "format": "json"
            }
        }
    
    def get(self, key, default=None):
        keys = key.split('.')
        value = self._config
        for k in keys:
            if isinstance(value, dict) and k in value:
                value = value[k]
            else:
                return default
        return value
    
    def set(self, key, value):
        keys = key.split('.')
        config = self._config
        for k in keys[:-1]:
            if k not in config:
                config[k] = {}
            config = config[k]
        config[keys[-1]] = value
        print(f"Configuration updated: {key} = {value}")
    
    def reload(self):
        print("Reloading configuration from external store...")
        # Simulate reload
        return self._config

class Application:
    def __init__(self, config_store):
        self.config = config_store
    
    def get_database_config(self):
        return {
            "host": self.config.get("database.host"),
            "port": self.config.get("database.port")
        }
    
    def is_feature_enabled(self, feature):
        return self.config.get(f"features.{feature}", False)

if __name__ == "__main__":
    store = ConfigurationStore()
    app = Application(store)
    
    print(f"Database config: {app.get_database_config()}")
    print(f"Feature X enabled: {app.is_feature_enabled('feature_x_enabled')}")
    
    # Update configuration without redeploying
    store.set("features.feature_x_enabled", False)
    print(f"Feature X enabled now: {app.is_feature_enabled('feature_x_enabled')}")
