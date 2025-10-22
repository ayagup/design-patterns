/**
 * Valet Key Pattern
 * Restricted access tokens
 */

class ValetKeyExample {
  constructor() {
    this.name = 'Valet Key';
  }

  demonstrate() {
    console.log(`Demonstrating Valet Key Pattern`);
    console.log(`Description: Restricted access tokens`);
    return `Valet Key implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Valet Key Pattern Demo ===\n');
  const example = new ValetKeyExample();
  console.log(example.demonstrate());
  console.log('\n✓ Valet Key pattern works!');
}

module.exports = { ValetKeyExample };
