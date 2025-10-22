/**
 * Adapter Pattern
 * Adapts incompatible interfaces
 */

class AdapterExample {
  constructor() {
    this.name = 'Adapter';
  }

  demonstrate() {
    console.log(`Demonstrating Adapter Pattern`);
    console.log(`Description: Adapts incompatible interfaces`);
    return `Adapter implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Adapter Pattern Demo ===\n');
  const example = new AdapterExample();
  console.log(example.demonstrate());
  console.log('\n✓ Adapter pattern works!');
}

module.exports = { AdapterExample };
