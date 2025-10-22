"""Scheduler Agent Supervisor - Coordinate distributed actions"""
import time
from enum import Enum

class TaskState(Enum):
    PENDING = "pending"
    RUNNING = "running"
    COMPLETED = "completed"
    FAILED = "failed"

class Agent:
    def __init__(self, name):
        self.name = name
    
    def execute(self, task):
        print(f"[{self.name}] Executing: {task['name']}")
        time.sleep(0.1)
        # Simulate occasional failure
        if task.get('should_fail'):
            raise Exception(f"Task {task['name']} failed")
        return f"Result from {task['name']}"

class Scheduler:
    def __init__(self):
        self.tasks = []
    
    def schedule_task(self, task):
        task['state'] = TaskState.PENDING
        self.tasks.append(task)
        print(f"[Scheduler] Scheduled: {task['name']}")
    
    def get_pending_tasks(self):
        return [t for t in self.tasks if t['state'] == TaskState.PENDING]

class Supervisor:
    def __init__(self, scheduler):
        self.scheduler = scheduler
        self.agents = {}
    
    def register_agent(self, agent):
        self.agents[agent.name] = agent
    
    def run(self):
        while True:
            pending = self.scheduler.get_pending_tasks()
            if not pending:
                break
            
            for task in pending:
                task['state'] = TaskState.RUNNING
                agent_name = task.get('agent', 'agent1')
                agent = self.agents.get(agent_name)
                
                try:
                    result = agent.execute(task)
                    task['state'] = TaskState.COMPLETED
                    task['result'] = result
                    print(f"[Supervisor] Task completed: {task['name']}")
                except Exception as e:
                    task['state'] = TaskState.FAILED
                    task['error'] = str(e)
                    print(f"[Supervisor] Task failed: {task['name']} - {e}")
                    
                    # Compensate or retry
                    if task.get('retry'):
                        print(f"[Supervisor] Retrying: {task['name']}")
                        task['state'] = TaskState.PENDING
                        task['should_fail'] = False

if __name__ == "__main__":
    scheduler = Scheduler()
    supervisor = Supervisor(scheduler)
    
    # Register agents
    supervisor.register_agent(Agent("agent1"))
    supervisor.register_agent(Agent("agent2"))
    
    # Schedule tasks
    scheduler.schedule_task({"name": "Task1", "agent": "agent1"})
    scheduler.schedule_task({"name": "Task2", "agent": "agent2", "should_fail": True, "retry": True})
    scheduler.schedule_task({"name": "Task3", "agent": "agent1"})
    
    # Supervisor coordinates execution
    supervisor.run()
    
    print("\nFinal task states:")
    for task in scheduler.tasks:
        print(f"  {task['name']}: {task['state'].value}")
