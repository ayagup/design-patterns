/**
 * Money Pattern
 * Monetary values
 */

class MoneyExample {
  constructor() {
    this.name = 'Money';
  }

  demonstrate() {
    console.log(`Demonstrating Money Pattern`);
    console.log(`Description: Monetary values`);
    return `Money implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Money Pattern Demo ===\n');
  const example = new MoneyExample();
  console.log(example.demonstrate());
  console.log('\n✓ Money pattern works!');
}

module.exports = { MoneyExample };
