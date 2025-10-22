"""Compute Resource Consolidation - Consolidate multiple tasks"""
class Task:
    def __init__(self, name, resource_requirements):
        self.name = name
        self.resource_requirements = resource_requirements
    
    def execute(self):
        print(f"Executing task: {self.name}")
        return f"Result from {self.name}"

class ComputeUnit:
    def __init__(self, id, capacity):
        self.id = id
        self.capacity = capacity
        self.tasks = []
        self.current_load = 0
    
    def can_add_task(self, task):
        return self.current_load + task.resource_requirements <= self.capacity
    
    def add_task(self, task):
        if self.can_add_task(task):
            self.tasks.append(task)
            self.current_load += task.resource_requirements
            return True
        return False
    
    def execute_tasks(self):
        results = []
        for task in self.tasks:
            results.append(task.execute())
        return results

class ResourceConsolidator:
    def __init__(self):
        self.compute_units = []
    
    def add_compute_unit(self, unit):
        self.compute_units.append(unit)
    
    def consolidate_tasks(self, tasks):
        for task in tasks:
            placed = False
            for unit in self.compute_units:
                if unit.add_task(task):
                    print(f"Placed {task.name} on unit {unit.id}")
                    placed = True
                    break
            if not placed:
                print(f"No available unit for {task.name}")

if __name__ == "__main__":
    consolidator = ResourceConsolidator()
    consolidator.add_compute_unit(ComputeUnit(1, 100))
    consolidator.add_compute_unit(ComputeUnit(2, 100))
    
    tasks = [
        Task("Task1", 30),
        Task("Task2", 40),
        Task("Task3", 50),
        Task("Task4", 20)
    ]
    
    consolidator.consolidate_tasks(tasks)
