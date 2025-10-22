"""Leader Election - Coordinate by electing leader"""
import threading
import time

class LeaderElection:
    def __init__(self):
        self.leader = None
        self.nodes = []
        self._lock = threading.Lock()
    
    def register_node(self, node):
        with self._lock:
            self.nodes.append(node)
            if self.leader is None:
                self.leader = node
                print(f"Node {node} elected as leader")
    
    def is_leader(self, node):
        return self.leader == node

if __name__ == "__main__":
    election = LeaderElection()
    election.register_node("Node1")
    election.register_node("Node2")
    election.register_node("Node3")
    
    for node in ["Node1", "Node2", "Node3"]:
        print(f"{node} is leader: {election.is_leader(node)}")
