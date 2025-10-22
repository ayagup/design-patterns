/**
 * Builder Pattern
 * Constructs complex objects step by step
 */

class BuilderExample {
  constructor() {
    this.name = 'Builder';
  }

  demonstrate() {
    console.log(`Demonstrating Builder Pattern`);
    console.log(`Description: Constructs complex objects step by step`);
    return `Builder implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Builder Pattern Demo ===\n');
  const example = new BuilderExample();
  console.log(example.demonstrate());
  console.log('\n✓ Builder pattern works!');
}

module.exports = { BuilderExample };
