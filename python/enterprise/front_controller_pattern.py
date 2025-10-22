"""Front Controller - Single handler for all requests"""
class FrontController:
    def __init__(self):
        self.handlers = {}
    
    def register_handler(self, path, handler):
        self.handlers[path] = handler
    
    def handle_request(self, request):
        path = request['path']
        if path in self.handlers:
            return self.handlers[path](request)
        return {"error": "Not found"}

def home_handler(request):
    return {"page": "home", "title": "Home Page"}

def about_handler(request):
    return {"page": "about", "title": "About Us"}

if __name__ == "__main__":
    controller = FrontController()
    controller.register_handler("/", home_handler)
    controller.register_handler("/about", about_handler)
    
    print(controller.handle_request({"path": "/"}))
    print(controller.handle_request({"path": "/about"}))
