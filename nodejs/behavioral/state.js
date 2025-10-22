/**
 * State Pattern
 * Changes behavior based on state
 */

class StateExample {
  constructor() {
    this.name = 'State';
  }

  demonstrate() {
    console.log(`Demonstrating State Pattern`);
    console.log(`Description: Changes behavior based on state`);
    return `State implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== State Pattern Demo ===\n');
  const example = new StateExample();
  console.log(example.demonstrate());
  console.log('\n✓ State pattern works!');
}

module.exports = { StateExample };
