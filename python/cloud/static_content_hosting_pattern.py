"""Static Content Hosting - Deliver static content efficiently"""
class CDN:
    def __init__(self):
        self.cache = {}
        self.origin_requests = 0
    
    def get_content(self, url, origin):
        if url in self.cache:
            print(f"[CDN] Cache HIT: {url}")
            return self.cache[url]
        
        print(f"[CDN] Cache MISS: {url}, fetching from origin")
        content = origin.get_content(url)
        self.cache[url] = content
        self.origin_requests += 1
        return content

class OriginServer:
    def __init__(self):
        self.content = {
            "/static/css/style.css": "body { color: blue; }",
            "/static/js/app.js": "console.log('app');",
            "/static/images/logo.png": "[PNG DATA]"
        }
    
    def get_content(self, url):
        print(f"[Origin] Serving: {url}")
        return self.content.get(url, "404 Not Found")

class WebApplication:
    def __init__(self, cdn):
        self.cdn = cdn
    
    def get_static_resource(self, url):
        return self.cdn.get_content(url, OriginServer())

if __name__ == "__main__":
    cdn = CDN()
    app = WebApplication(cdn)
    
    # First request - cache miss
    content = app.get_static_resource("/static/css/style.css")
    print(f"Content: {content}\n")
    
    # Second request - cache hit
    content = app.get_static_resource("/static/css/style.css")
    print(f"Content: {content}\n")
    
    print(f"Origin requests: {cdn.origin_requests}")
