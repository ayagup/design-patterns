"""Compensating Transaction - Undo failed operations"""
class Transaction:
    def execute(self):
        pass
    
    def compensate(self):
        pass

class BookFlightTransaction(Transaction):
    def execute(self):
        print("Flight booked")
        return True
    
    def compensate(self):
        print("Flight booking cancelled")

class BookHotelTransaction(Transaction):
    def execute(self):
        print("Hotel booked")
        raise Exception("Hotel unavailable")
    
    def compensate(self):
        print("Hotel booking cancelled")

class TravelBookingService:
    def book_trip(self):
        transactions = [BookFlightTransaction(), BookHotelTransaction()]
        executed = []
        
        try:
            for txn in transactions:
                if txn.execute():
                    executed.append(txn)
        except Exception as e:
            print(f"Error: {e}, compensating...")
            for txn in reversed(executed):
                txn.compensate()
            return False
        
        return True

if __name__ == "__main__":
    service = TravelBookingService()
    service.book_trip()
