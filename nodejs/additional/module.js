/**
 * Module Pattern
 * Code organization
 */

class ModuleExample {
  constructor() {
    this.name = 'Module';
  }

  demonstrate() {
    console.log(`Demonstrating Module Pattern`);
    console.log(`Description: Code organization`);
    return `Module implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Module Pattern Demo ===\n');
  const example = new ModuleExample();
  console.log(example.demonstrate());
  console.log('\n✓ Module pattern works!');
}

module.exports = { ModuleExample };
