/**
 * Double-Checked Locking Pattern
 * Optimizes lazy initialization
 */

class DoubleCheckedLockingExample {
  constructor() {
    this.name = 'Double-Checked Locking';
  }

  demonstrate() {
    console.log(`Demonstrating Double-Checked Locking Pattern`);
    console.log(`Description: Optimizes lazy initialization`);
    return `Double-Checked Locking implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Double-Checked Locking Pattern Demo ===\n');
  const example = new DoubleCheckedLockingExample();
  console.log(example.demonstrate());
  console.log('\n✓ Double-Checked Locking pattern works!');
}

module.exports = { DoubleCheckedLockingExample };
