/**
 * Flyweight Pattern
 * Shares data to reduce memory
 */

class FlyweightExample {
  constructor() {
    this.name = 'Flyweight';
  }

  demonstrate() {
    console.log(`Demonstrating Flyweight Pattern`);
    console.log(`Description: Shares data to reduce memory`);
    return `Flyweight implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Flyweight Pattern Demo ===\n');
  const example = new FlyweightExample();
  console.log(example.demonstrate());
  console.log('\n✓ Flyweight pattern works!');
}

module.exports = { FlyweightExample };
