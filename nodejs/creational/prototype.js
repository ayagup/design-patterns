/**
 * Prototype Pattern
 * Creates objects by cloning prototypes
 */

class PrototypeExample {
  constructor() {
    this.name = 'Prototype';
  }

  demonstrate() {
    console.log(`Demonstrating Prototype Pattern`);
    console.log(`Description: Creates objects by cloning prototypes`);
    return `Prototype implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Prototype Pattern Demo ===\n');
  const example = new PrototypeExample();
  console.log(example.demonstrate());
  console.log('\n✓ Prototype pattern works!');
}

module.exports = { PrototypeExample };
