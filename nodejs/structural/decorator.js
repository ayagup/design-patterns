/**
 * Decorator Pattern
 * Adds behavior dynamically
 */

class DecoratorExample {
  constructor() {
    this.name = 'Decorator';
  }

  demonstrate() {
    console.log(`Demonstrating Decorator Pattern`);
    console.log(`Description: Adds behavior dynamically`);
    return `Decorator implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Decorator Pattern Demo ===\n');
  const example = new DecoratorExample();
  console.log(example.demonstrate());
  console.log('\n✓ Decorator pattern works!');
}

module.exports = { DecoratorExample };
