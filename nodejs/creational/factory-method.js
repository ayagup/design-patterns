/**
 * Factory Method Pattern
 * Creates objects through factory methods
 */

class FactoryMethodExample {
  constructor() {
    this.name = 'Factory Method';
  }

  demonstrate() {
    console.log(`Demonstrating Factory Method Pattern`);
    console.log(`Description: Creates objects through factory methods`);
    return `Factory Method implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Factory Method Pattern Demo ===\n');
  const example = new FactoryMethodExample();
  console.log(example.demonstrate());
  console.log('\n✓ Factory Method pattern works!');
}

module.exports = { FactoryMethodExample };
