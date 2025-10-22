"""Template Method Pattern"""
from abc import ABC, abstractmethod

class AbstractClass(ABC):
    def template_method(self):
        self.step1()
        self.step2()
        self.step3()
    
    @abstractmethod
    def step1(self):
        pass
    
    @abstractmethod
    def step2(self):
        pass
    
    def step3(self):
        print("Common step3")

class ConcreteClass(AbstractClass):
    def step1(self):
        print("ConcreteClass step1")
    
    def step2(self):
        print("ConcreteClass step2")

if __name__ == "__main__":
    obj = ConcreteClass()
    obj.template_method()
