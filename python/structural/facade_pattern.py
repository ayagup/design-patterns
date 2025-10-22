"""
Facade Pattern - Provides a simplified interface to complex subsystem
"""


class CPU:
    def freeze(self):
        print("CPU: Freezing...")
    
    def jump(self, position: int):
        print(f"CPU: Jumping to {position}")
    
    def execute(self):
        print("CPU: Executing...")


class Memory:
    def load(self, position: int, data: bytes):
        print(f"Memory: Loading data at {position}")


class HardDrive:
    def read(self, lba: int, size: int) -> bytes:
        print(f"HardDrive: Reading {size} bytes from {lba}")
        return b"boot data"


class ComputerFacade:
    """Simplified interface to complex computer subsystem"""
    def __init__(self):
        self.cpu = CPU()
        self.memory = Memory()
        self.hard_drive = HardDrive()
    
    def start(self):
        print("Starting computer...\n")
        self.cpu.freeze()
        self.memory.load(0, self.hard_drive.read(0, 1024))
        self.cpu.jump(0)
        self.cpu.execute()
        print("\nComputer started!")


if __name__ == "__main__":
    print("=== Facade Pattern Demo ===\n")
    
    computer = ComputerFacade()
    computer.start()
