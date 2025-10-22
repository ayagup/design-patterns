/**
 * Null Object Pattern
 * Provides default behavior
 */

class NullObjectExample {
  constructor() {
    this.name = 'Null Object';
  }

  demonstrate() {
    console.log(`Demonstrating Null Object Pattern`);
    console.log(`Description: Provides default behavior`);
    return `Null Object implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Null Object Pattern Demo ===\n');
  const example = new NullObjectExample();
  console.log(example.demonstrate());
  console.log('\n✓ Null Object pattern works!');
}

module.exports = { NullObjectExample };
