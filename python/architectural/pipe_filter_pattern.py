"""Pipe and Filter Pattern"""
class Filter:
    def process(self, data):
        pass

class UpperCaseFilter(Filter):
    def process(self, data):
        return data.upper()

class ExclamationFilter(Filter):
    def process(self, data):
        return data + "!"

class Pipeline:
    def __init__(self):
        self.filters = []
    
    def add_filter(self, filter):
        self.filters.append(filter)
    
    def execute(self, data):
        for filter in self.filters:
            data = filter.process(data)
        return data

if __name__ == "__main__":
    pipeline = Pipeline()
    pipeline.add_filter(UpperCaseFilter())
    pipeline.add_filter(ExclamationFilter())
    result = pipeline.execute("hello")
    print(result)
