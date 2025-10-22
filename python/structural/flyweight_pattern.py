"""
Flyweight Pattern - Shares objects to support large numbers efficiently
"""

from typing import Dict


class TreeType:
    """Flyweight - shared state"""
    def __init__(self, name: str, color: str, texture: str):
        self.name = name
        self.color = color
        self.texture = texture
    
    def draw(self, x: int, y: int):
        print(f"Drawing {self.name} tree at ({x}, {y})")


class TreeFactory:
    """Manages flyweights"""
    _tree_types: Dict[str, TreeType] = {}
    
    @classmethod
    def get_tree_type(cls, name: str, color: str, texture: str) -> TreeType:
        key = f"{name}_{color}_{texture}"
        if key not in cls._tree_types:
            cls._tree_types[key] = TreeType(name, color, texture)
            print(f"Creating new tree type: {name}")
        return cls._tree_types[key]


class Tree:
    """Context - unique state"""
    def __init__(self, x: int, y: int, tree_type: TreeType):
        self.x = x
        self.y = y
        self.tree_type = tree_type
    
    def draw(self):
        self.tree_type.draw(self.x, self.y)


class Forest:
    def __init__(self):
        self.trees = []
    
    def plant_tree(self, x: int, y: int, name: str, color: str, texture: str):
        tree_type = TreeFactory.get_tree_type(name, color, texture)
        tree = Tree(x, y, tree_type)
        self.trees.append(tree)
    
    def draw(self):
        for tree in self.trees:
            tree.draw()


if __name__ == "__main__":
    print("=== Flyweight Pattern Demo ===\n")
    
    forest = Forest()
    forest.plant_tree(1, 2, "Oak", "Green", "Rough")
    forest.plant_tree(3, 4, "Oak", "Green", "Rough")  # Reuses flyweight
    forest.plant_tree(5, 6, "Pine", "Dark Green", "Smooth")
    
    print("\nDrawing forest:")
    forest.draw()
