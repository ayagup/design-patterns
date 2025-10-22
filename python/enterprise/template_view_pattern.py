"""Template View - Renders with embedded markers"""
class TemplateView:
    def __init__(self, template):
        self.template = template
    
    def render(self, context):
        result = self.template
        for key, value in context.items():
            result = result.replace(f"{{{{{key}}}}}", str(value))
        return result

if __name__ == "__main__":
    template = "<h1>{{title}}</h1><p>{{content}}</p>"
    view = TemplateView(template)
    html = view.render({"title": "Welcome", "content": "Hello World"})
    print(html)
