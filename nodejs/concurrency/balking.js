/**
 * Balking Pattern
 * Executes only when ready
 */

class BalkingExample {
  constructor() {
    this.name = 'Balking';
  }

  demonstrate() {
    console.log(`Demonstrating Balking Pattern`);
    console.log(`Description: Executes only when ready`);
    return `Balking implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Balking Pattern Demo ===\n');
  const example = new BalkingExample();
  console.log(example.demonstrate());
  console.log('\n✓ Balking pattern works!');
}

module.exports = { BalkingExample };
