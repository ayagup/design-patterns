"""Chained Microservice - Services call each other in sequence"""
class ValidationService:
    def validate(self, data):
        print("Validating...")
        return {**data, "validated": True}

class EnrichmentService:
    def enrich(self, data):
        print("Enriching...")
        return {**data, "enriched": True}

class ProcessingService:
    def process(self, data):
        print("Processing...")
        return {**data, "processed": True}

class ChainOrchestrator:
    def __init__(self):
        self.validator = ValidationService()
        self.enricher = EnrichmentService()
        self.processor = ProcessingService()
    
    def execute(self, data):
        result = self.validator.validate(data)
        result = self.enricher.enrich(result)
        result = self.processor.process(result)
        return result

if __name__ == "__main__":
    orchestrator = ChainOrchestrator()
    result = orchestrator.execute({"input": "data"})
    print(f"Final result: {result}")
