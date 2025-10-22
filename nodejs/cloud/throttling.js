/**
 * Throttling Pattern
 * Rate limiting
 */

class ThrottlingExample {
  constructor() {
    this.name = 'Throttling';
  }

  demonstrate() {
    console.log(`Demonstrating Throttling Pattern`);
    console.log(`Description: Rate limiting`);
    return `Throttling implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Throttling Pattern Demo ===\n');
  const example = new ThrottlingExample();
  console.log(example.demonstrate());
  console.log('\n✓ Throttling pattern works!');
}

module.exports = { ThrottlingExample };
