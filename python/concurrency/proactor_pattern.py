"""Proactor Pattern - Asynchronous operation completion"""
import asyncio

async def async_operation():
    print("Starting async operation")
    await asyncio.sleep(0.1)
    print("Async operation complete")
    return "Result"

class Proactor:
    def __init__(self):
        self.loop = asyncio.new_event_loop()
    
    def run(self, operation):
        return self.loop.run_until_complete(operation())

if __name__ == "__main__":
    proactor = Proactor()
    result = proactor.run(async_operation)
    print(f"Got: {result}")
