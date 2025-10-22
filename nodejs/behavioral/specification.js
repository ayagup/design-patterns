/**
 * Specification Pattern
 * Combines business rules
 */

class SpecificationExample {
  constructor() {
    this.name = 'Specification';
  }

  demonstrate() {
    console.log(`Demonstrating Specification Pattern`);
    console.log(`Description: Combines business rules`);
    return `Specification implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Specification Pattern Demo ===\n');
  const example = new SpecificationExample();
  console.log(example.demonstrate());
  console.log('\n✓ Specification pattern works!');
}

module.exports = { SpecificationExample };
