"""Federated Identity - Delegate authentication"""
class IdentityProvider:
    def __init__(self, name):
        self.name = name
        self.users = {}
    
    def register_user(self, user_id, credentials):
        self.users[user_id] = credentials
    
    def authenticate(self, user_id, credentials):
        if user_id in self.users and self.users[user_id] == credentials:
            return self.issue_token(user_id)
        return None
    
    def issue_token(self, user_id):
        return f"TOKEN_{self.name}_{user_id}"
    
    def validate_token(self, token):
        if token.startswith(f"TOKEN_{self.name}_"):
            user_id = token.split('_')[-1]
            return {"user_id": user_id, "provider": self.name}
        return None

class Application:
    def __init__(self):
        self.trusted_providers = {}
    
    def trust_provider(self, provider):
        self.trusted_providers[provider.name] = provider
    
    def login_with_provider(self, provider_name, user_id, credentials):
        if provider_name in self.trusted_providers:
            provider = self.trusted_providers[provider_name]
            token = provider.authenticate(user_id, credentials)
            if token:
                print(f"User {user_id} authenticated via {provider_name}")
                return token
        print("Authentication failed")
        return None
    
    def validate_token(self, token):
        for provider in self.trusted_providers.values():
            user_info = provider.validate_token(token)
            if user_info:
                return user_info
        return None

if __name__ == "__main__":
    # Setup identity providers
    google = IdentityProvider("Google")
    google.register_user("user@gmail.com", "password123")
    
    microsoft = IdentityProvider("Microsoft")
    microsoft.register_user("user@outlook.com", "password456")
    
    # Application trusts these providers
    app = Application()
    app.trust_provider(google)
    app.trust_provider(microsoft)
    
    # User logs in via Google
    token = app.login_with_provider("Google", "user@gmail.com", "password123")
    print(f"Token: {token}")
    
    # Validate token
    user_info = app.validate_token(token)
    print(f"User info: {user_info}")
