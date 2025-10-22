"""Materialized View - Pre-generated views"""
class MaterializedView:
    def __init__(self):
        self.view = None
        self.last_updated = None
    
    def refresh(self, data_source):
        # Generate view from source data
        data = data_source.get_data()
        self.view = self._compute_view(data)
        import datetime
        self.last_updated = datetime.datetime.now()
        print(f"View refreshed at {self.last_updated}")
    
    def _compute_view(self, data):
        # Complex computation
        return {"summary": sum(data)}
    
    def query(self):
        return self.view

class DataSource:
    def get_data(self):
        return [1, 2, 3, 4, 5]

if __name__ == "__main__":
    source = DataSource()
    view = MaterializedView()
    view.refresh(source)
    print(f"View: {view.query()}")
