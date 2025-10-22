"""Page Controller - One controller per page"""
class PageController:
    def handle(self, request):
        pass

class HomeController(PageController):
    def handle(self, request):
        return {"page": "home", "content": "Welcome home!"}

class ProductController(PageController):
    def handle(self, request):
        product_id = request.get('id')
        return {"page": "product", "product_id": product_id}

class Router:
    def __init__(self):
        self.routes = {}
    
    def register(self, path, controller):
        self.routes[path] = controller
    
    def route(self, path, request):
        if path in self.routes:
            return self.routes[path].handle(request)
        return {"error": "Not found"}

if __name__ == "__main__":
    router = Router()
    router.register("/", HomeController())
    router.register("/product", ProductController())
    
    print(router.route("/", {}))
    print(router.route("/product", {"id": 123}))
