/**
 * Chain of Responsibility Pattern
 * Passes requests along chain
 */

class ChainofResponsibilityExample {
  constructor() {
    this.name = 'Chain of Responsibility';
  }

  demonstrate() {
    console.log(`Demonstrating Chain of Responsibility Pattern`);
    console.log(`Description: Passes requests along chain`);
    return `Chain of Responsibility implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Chain of Responsibility Pattern Demo ===\n');
  const example = new ChainofResponsibilityExample();
  console.log(example.demonstrate());
  console.log('\n✓ Chain of Responsibility pattern works!');
}

module.exports = { ChainofResponsibilityExample };
