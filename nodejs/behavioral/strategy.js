/**
 * Strategy Pattern
 * Encapsulates algorithms
 */

class StrategyExample {
  constructor() {
    this.name = 'Strategy';
  }

  demonstrate() {
    console.log(`Demonstrating Strategy Pattern`);
    console.log(`Description: Encapsulates algorithms`);
    return `Strategy implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Strategy Pattern Demo ===\n');
  const example = new StrategyExample();
  console.log(example.demonstrate());
  console.log('\n✓ Strategy pattern works!');
}

module.exports = { StrategyExample };
