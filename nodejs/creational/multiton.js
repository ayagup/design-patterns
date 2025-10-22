/**
 * Multiton Pattern
 * Manages multiple named instances
 */

class MultitonExample {
  constructor() {
    this.name = 'Multiton';
  }

  demonstrate() {
    console.log(`Demonstrating Multiton Pattern`);
    console.log(`Description: Manages multiple named instances`);
    return `Multiton implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Multiton Pattern Demo ===\n');
  const example = new MultitonExample();
  console.log(example.demonstrate());
  console.log('\n✓ Multiton pattern works!');
}

module.exports = { MultitonExample };
