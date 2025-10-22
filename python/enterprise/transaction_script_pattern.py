"""Transaction Script - Procedural business logic"""
class TransactionScript:
    def __init__(self, db):
        self.db = db
    
    def transfer_money(self, from_account, to_account, amount):
        # Get accounts
        from_acc = self.db.get_account(from_account)
        to_acc = self.db.get_account(to_account)
        
        # Validate
        if from_acc['balance'] < amount:
            raise ValueError("Insufficient funds")
        
        # Update balances
        from_acc['balance'] -= amount
        to_acc['balance'] += amount
        
        # Save
        self.db.save_account(from_acc)
        self.db.save_account(to_acc)
        
        print(f"Transferred ${amount} from {from_account} to {to_account}")

class Database:
    def __init__(self):
        self.accounts = {
            "ACC1": {"id": "ACC1", "balance": 1000},
            "ACC2": {"id": "ACC2", "balance": 500}
        }
    
    def get_account(self, id):
        return self.accounts[id]
    
    def save_account(self, account):
        self.accounts[account['id']] = account

if __name__ == "__main__":
    db = Database()
    script = TransactionScript(db)
    script.transfer_money("ACC1", "ACC2", 100)
    print(f"ACC1 balance: ${db.get_account('ACC1')['balance']}")
    print(f"ACC2 balance: ${db.get_account('ACC2')['balance']}")
