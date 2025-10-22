"""Interpreter Pattern"""
class Context:
    def __init__(self):
        self.variables = {}

class Expression:
    def interpret(self, context):
        pass

class Number(Expression):
    def __init__(self, value):
        self.value = value
    
    def interpret(self, context):
        return self.value

class Plus(Expression):
    def __init__(self, left, right):
        self.left = left
        self.right = right
    
    def interpret(self, context):
        return self.left.interpret(context) + self.right.interpret(context)

if __name__ == "__main__":
    expr = Plus(Number(5), Number(3))
    print(f"Result: {expr.interpret(Context())}")
