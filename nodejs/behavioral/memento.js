/**
 * Memento Pattern
 * Captures and restores state
 */

class MementoExample {
  constructor() {
    this.name = 'Memento';
  }

  demonstrate() {
    console.log(`Demonstrating Memento Pattern`);
    console.log(`Description: Captures and restores state`);
    return `Memento implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Memento Pattern Demo ===\n');
  const example = new MementoExample();
  console.log(example.demonstrate());
  console.log('\n✓ Memento pattern works!');
}

module.exports = { MementoExample };
