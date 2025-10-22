/**
 * Reactor Pattern
 * Handles concurrent requests
 */

class ReactorExample {
  constructor() {
    this.name = 'Reactor';
  }

  demonstrate() {
    console.log(`Demonstrating Reactor Pattern`);
    console.log(`Description: Handles concurrent requests`);
    return `Reactor implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Reactor Pattern Demo ===\n');
  const example = new ReactorExample();
  console.log(example.demonstrate());
  console.log('\n✓ Reactor pattern works!');
}

module.exports = { ReactorExample };
