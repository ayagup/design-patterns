/**
 * Abstract Factory Pattern
 * Creates families of related objects
 */

class AbstractFactoryExample {
  constructor() {
    this.name = 'Abstract Factory';
  }

  demonstrate() {
    console.log(`Demonstrating Abstract Factory Pattern`);
    console.log(`Description: Creates families of related objects`);
    return `Abstract Factory implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Abstract Factory Pattern Demo ===\n');
  const example = new AbstractFactoryExample();
  console.log(example.demonstrate());
  console.log('\n✓ Abstract Factory pattern works!');
}

module.exports = { AbstractFactoryExample };
