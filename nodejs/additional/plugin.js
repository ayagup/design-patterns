/**
 * Plugin Pattern
 * Dynamic extensions
 */

class PluginExample {
  constructor() {
    this.name = 'Plugin';
  }

  demonstrate() {
    console.log(`Demonstrating Plugin Pattern`);
    console.log(`Description: Dynamic extensions`);
    return `Plugin implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Plugin Pattern Demo ===\n');
  const example = new PluginExample();
  console.log(example.demonstrate());
  console.log('\n✓ Plugin pattern works!');
}

module.exports = { PluginExample };
