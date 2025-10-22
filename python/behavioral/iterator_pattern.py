"""Iterator Pattern"""
class Iterator:
    def __init__(self, collection):
        self._collection = collection
        self._index = 0
    
    def __next__(self):
        if self._index < len(self._collection):
            result = self._collection[self._index]
            self._index += 1
            return result
        raise StopIteration

class BookCollection:
    def __init__(self):
        self._books = []
    
    def add_book(self, book):
        self._books.append(book)
    
    def __iter__(self):
        return Iterator(self._books)

if __name__ == "__main__":
    collection = BookCollection()
    collection.add_book("Book 1")
    collection.add_book("Book 2")
    for book in collection:
        print(book)
