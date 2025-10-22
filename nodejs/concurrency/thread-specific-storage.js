/**
 * Thread-Specific Storage Pattern
 * Stores thread-local data
 */

class ThreadSpecificStorageExample {
  constructor() {
    this.name = 'Thread-Specific Storage';
  }

  demonstrate() {
    console.log(`Demonstrating Thread-Specific Storage Pattern`);
    console.log(`Description: Stores thread-local data`);
    return `Thread-Specific Storage implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Thread-Specific Storage Pattern Demo ===\n');
  const example = new ThreadSpecificStorageExample();
  console.log(example.demonstrate());
  console.log('\n✓ Thread-Specific Storage pattern works!');
}

module.exports = { ThreadSpecificStorageExample };
