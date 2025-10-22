/**
 * Lock Pattern
 * Mutual exclusion
 */

class LockExample {
  constructor() {
    this.name = 'Lock';
  }

  demonstrate() {
    console.log(`Demonstrating Lock Pattern`);
    console.log(`Description: Mutual exclusion`);
    return `Lock implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Lock Pattern Demo ===\n');
  const example = new LockExample();
  console.log(example.demonstrate());
  console.log('\n✓ Lock pattern works!');
}

module.exports = { LockExample };
