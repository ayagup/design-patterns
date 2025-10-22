"""Two-Step View - Two-stage transformation"""
class LogicalView:
    def transform(self, data):
        return {
            "title": data['title'],
            "items": [{"label": item} for item in data['items']]
        }

class PhysicalView:
    def render_html(self, logical):
        html = f"<h1>{logical['title']}</h1><ul>"
        for item in logical['items']:
            html += f"<li>{item['label']}</li>"
        html += "</ul>"
        return html
    
    def render_json(self, logical):
        import json
        return json.dumps(logical, indent=2)

if __name__ == "__main__":
    data = {"title": "Products", "items": ["Item 1", "Item 2"]}
    
    logical = LogicalView().transform(data)
    physical = PhysicalView()
    
    print("HTML:")
    print(physical.render_html(logical))
    print("\nJSON:")
    print(physical.render_json(logical))
