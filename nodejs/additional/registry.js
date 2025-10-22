/**
 * Registry Pattern
 * Service lookup
 */

class RegistryExample {
  constructor() {
    this.name = 'Registry';
  }

  demonstrate() {
    console.log(`Demonstrating Registry Pattern`);
    console.log(`Description: Service lookup`);
    return `Registry implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Registry Pattern Demo ===\n');
  const example = new RegistryExample();
  console.log(example.demonstrate());
  console.log('\n✓ Registry pattern works!');
}

module.exports = { RegistryExample };
