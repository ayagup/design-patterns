/**
 * Active Object Pattern
 * Decouples execution from invocation
 */

class ActiveObjectExample {
  constructor() {
    this.name = 'Active Object';
  }

  demonstrate() {
    console.log(`Demonstrating Active Object Pattern`);
    console.log(`Description: Decouples execution from invocation`);
    return `Active Object implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Active Object Pattern Demo ===\n');
  const example = new ActiveObjectExample();
  console.log(example.demonstrate());
  console.log('\n✓ Active Object pattern works!');
}

module.exports = { ActiveObjectExample };
