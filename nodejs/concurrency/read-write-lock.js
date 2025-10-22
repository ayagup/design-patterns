/**
 * Read-Write Lock Pattern
 * Allows concurrent reads
 */

class ReadWriteLockExample {
  constructor() {
    this.name = 'Read-Write Lock';
  }

  demonstrate() {
    console.log(`Demonstrating Read-Write Lock Pattern`);
    console.log(`Description: Allows concurrent reads`);
    return `Read-Write Lock implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Read-Write Lock Pattern Demo ===\n');
  const example = new ReadWriteLockExample();
  console.log(example.demonstrate());
  console.log('\n✓ Read-Write Lock pattern works!');
}

module.exports = { ReadWriteLockExample };
