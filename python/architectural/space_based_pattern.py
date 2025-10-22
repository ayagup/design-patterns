"""Space-Based Architecture - In-memory data grid"""
class ProcessingUnit:
    def __init__(self, id):
        self.id = id
        self.cache = {}
    
    def process(self, request):
        # Process using in-memory data
        result = f"Processed by unit {self.id}: {request}"
        self.cache[request] = result
        return result

class DataGrid:
    def __init__(self):
        self.units = []
    
    def add_unit(self, unit):
        self.units.append(unit)
    
    def route_request(self, request):
        # Simple round-robin
        unit = self.units[hash(request) % len(self.units)]
        return unit.process(request)

if __name__ == "__main__":
    grid = DataGrid()
    grid.add_unit(ProcessingUnit(1))
    grid.add_unit(ProcessingUnit(2))
    
    print(grid.route_request("Request1"))
    print(grid.route_request("Request2"))
