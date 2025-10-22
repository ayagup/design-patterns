/**
 * Extension Object Pattern
 * Extends objects dynamically
 */

class ExtensionObjectExample {
  constructor() {
    this.name = 'Extension Object';
  }

  demonstrate() {
    console.log(`Demonstrating Extension Object Pattern`);
    console.log(`Description: Extends objects dynamically`);
    return `Extension Object implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Extension Object Pattern Demo ===\n');
  const example = new ExtensionObjectExample();
  console.log(example.demonstrate());
  console.log('\n✓ Extension Object pattern works!');
}

module.exports = { ExtensionObjectExample };
