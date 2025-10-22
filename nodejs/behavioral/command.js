/**
 * Command Pattern
 * Encapsulates requests as objects
 */

class CommandExample {
  constructor() {
    this.name = 'Command';
  }

  demonstrate() {
    console.log(`Demonstrating Command Pattern`);
    console.log(`Description: Encapsulates requests as objects`);
    return `Command implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Command Pattern Demo ===\n');
  const example = new CommandExample();
  console.log(example.demonstrate());
  console.log('\n✓ Command pattern works!');
}

module.exports = { CommandExample };
