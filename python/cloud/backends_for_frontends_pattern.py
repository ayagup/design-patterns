"""BFF - Separate backends for different frontends"""
class MobileAPI:
    def get_data(self):
        return {"items": ["Item1", "Item2"]}  # Simplified for mobile

class WebAPI:
    def get_data(self):
        return {
            "items": [
                {"id": 1, "name": "Item1", "description": "Full details"},
                {"id": 2, "name": "Item2", "description": "Full details"}
            ]
        }  # Detailed for web

class SharedService:
    def get_all_data(self):
        return [
            {"id": 1, "name": "Item1", "description": "Full details"},
            {"id": 2, "name": "Item2", "description": "Full details"}
        ]

if __name__ == "__main__":
    service = SharedService()
    mobile = MobileAPI()
    web = WebAPI()
    
    print("Mobile:", mobile.get_data())
    print("Web:", web.get_data())
