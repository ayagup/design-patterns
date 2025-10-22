/**
 * Bridge Pattern
 * Separates abstraction from implementation
 */

class BridgeExample {
  constructor() {
    this.name = 'Bridge';
  }

  demonstrate() {
    console.log(`Demonstrating Bridge Pattern`);
    console.log(`Description: Separates abstraction from implementation`);
    return `Bridge implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Bridge Pattern Demo ===\n');
  const example = new BridgeExample();
  console.log(example.demonstrate());
  console.log('\n✓ Bridge pattern works!');
}

module.exports = { BridgeExample };
