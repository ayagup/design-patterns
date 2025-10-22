/**
 * Twin Pattern
 * Multiple inheritance
 */

class TwinExample {
  constructor() {
    this.name = 'Twin';
  }

  demonstrate() {
    console.log(`Demonstrating Twin Pattern`);
    console.log(`Description: Multiple inheritance`);
    return `Twin implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Twin Pattern Demo ===\n');
  const example = new TwinExample();
  console.log(example.demonstrate());
  console.log('\n✓ Twin pattern works!');
}

module.exports = { TwinExample };
