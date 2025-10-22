"""
Adapter Pattern - Allows incompatible interfaces to work together
"""

from abc import ABC, abstractmethod


class EuropeanSocket:
    def voltage(self) -> int:
        return 230
    
    def live(self) -> int:
        return 1
    
    def neutral(self) -> int:
        return -1


class USASocket:
    def voltage(self) -> int:
        return 120


class EuropeanSocketInterface(ABC):
    @abstractmethod
    def voltage(self) -> int:
        pass


class USASocketAdapter(EuropeanSocketInterface):
    """Adapts USA socket to European interface"""
    def __init__(self, usa_socket: USASocket):
        self.usa_socket = usa_socket
    
    def voltage(self) -> int:
        return 230  # Convert 120V to 230V


class Kettle:
    def __init__(self, socket: EuropeanSocketInterface):
        self.socket = socket
    
    def boil(self):
        voltage = self.socket.voltage()
        if voltage > 200:
            print(f"Kettle: Boiling with {voltage}V")
        else:
            print(f"Kettle: Voltage {voltage}V too low!")


if __name__ == "__main__":
    print("=== Adapter Pattern Demo ===\n")
    
    # European kettle with European socket
    eu_socket = EuropeanSocket()
    kettle = Kettle(eu_socket)
    kettle.boil()
    
    # European kettle with adapted USA socket
    usa_socket = USASocket()
    adapted_socket = USASocketAdapter(usa_socket)
    kettle = Kettle(adapted_socket)
    kettle.boil()
