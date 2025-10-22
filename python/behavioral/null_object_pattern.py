"""Null Object Pattern"""
from abc import ABC, abstractmethod

class Animal(ABC):
    @abstractmethod
    def make_sound(self):
        pass

class Dog(Animal):
    def make_sound(self):
        return "Woof!"

class NullAnimal(Animal):
    def make_sound(self):
        return ""

if __name__ == "__main__":
    animals = [Dog(), NullAnimal()]
    for animal in animals:
        sound = animal.make_sound()
        if sound:
            print(sound)
