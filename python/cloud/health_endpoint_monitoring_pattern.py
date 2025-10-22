"""Health Endpoint Monitoring - Health check endpoints"""
class HealthCheck:
    def __init__(self):
        self.checks = {}
    
    def register(self, name, check_func):
        self.checks[name] = check_func
    
    def check_health(self):
        results = {}
        for name, check_func in self.checks.items():
            try:
                results[name] = {
                    "status": "healthy" if check_func() else "unhealthy"
                }
            except Exception as e:
                results[name] = {"status": "unhealthy", "error": str(e)}
        
        overall = all(r["status"] == "healthy" for r in results.values())
        return {
            "status": "healthy" if overall else "unhealthy",
            "checks": results
        }

def database_health():
    return True  # Simulate DB check

def cache_health():
    return True  # Simulate cache check

if __name__ == "__main__":
    health = HealthCheck()
    health.register("database", database_health)
    health.register("cache", cache_health)
    
    status = health.check_health()
    print(f"Health: {status}")
