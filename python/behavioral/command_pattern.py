"""
Command Pattern - Encapsulates a request as an object
"""

from abc import ABC, abstractmethod


class Command(ABC):
    @abstractmethod
    def execute(self):
        pass
    
    @abstractmethod
    def undo(self):
        pass


class Light:
    def on(self):
        print("Light is ON")
    
    def off(self):
        print("Light is OFF")


class LightOnCommand(Command):
    def __init__(self, light: Light):
        self.light = light
    
    def execute(self):
        self.light.on()
    
    def undo(self):
        self.light.off()


class LightOffCommand(Command):
    def __init__(self, light: Light):
        self.light = light
    
    def execute(self):
        self.light.off()
    
    def undo(self):
        self.light.on()


class RemoteControl:
    def __init__(self):
        self._command: Optional[Command] = None
        self._history = []
    
    def set_command(self, command: Command):
        self._command = command
    
    def press_button(self):
        if self._command:
            self._command.execute()
            self._history.append(self._command)
    
    def press_undo(self):
        if self._history:
            command = self._history.pop()
            command.undo()


if __name__ == "__main__":
    print("=== Command Pattern Demo ===\n")
    
    light = Light()
    remote = RemoteControl()
    
    on_command = LightOnCommand(light)
    off_command = LightOffCommand(light)
    
    remote.set_command(on_command)
    remote.press_button()
    
    remote.set_command(off_command)
    remote.press_button()
    
    print("\nUndo last command:")
    remote.press_undo()
