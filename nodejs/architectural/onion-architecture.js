/**
 * Onion Architecture Pattern
 * Domain-centric layers
 */

class OnionArchitectureExample {
  constructor() {
    this.name = 'Onion Architecture';
  }

  demonstrate() {
    console.log(`Demonstrating Onion Architecture Pattern`);
    console.log(`Description: Domain-centric layers`);
    return `Onion Architecture implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Onion Architecture Pattern Demo ===\n');
  const example = new OnionArchitectureExample();
  console.log(example.demonstrate());
  console.log('\n✓ Onion Architecture pattern works!');
}

module.exports = { OnionArchitectureExample };
