"""Transform View - Transforms domain data to presentation"""
class TransformView:
    def transform(self, data):
        # Transform domain objects to view models
        return {
            "display_name": data['name'].upper(),
            "formatted_price": f"${data['price']:.2f}"
        }

if __name__ == "__main__":
    product = {"name": "laptop", "price": 999.99}
    view = TransformView()
    view_model = view.transform(product)
    print(view_model)
