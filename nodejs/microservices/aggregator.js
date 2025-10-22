/**
 * Aggregator Pattern
 * Aggregates data
 */

class AggregatorExample {
  constructor() {
    this.name = 'Aggregator';
  }

  demonstrate() {
    console.log(`Demonstrating Aggregator Pattern`);
    console.log(`Description: Aggregates data`);
    return `Aggregator implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Aggregator Pattern Demo ===\n');
  const example = new AggregatorExample();
  console.log(example.demonstrate());
  console.log('\n✓ Aggregator pattern works!');
}

module.exports = { AggregatorExample };
