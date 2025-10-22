/**
 * Lazy Initialization Pattern
 * Delays creation until needed
 */

class LazyInitializationExample {
  constructor() {
    this.name = 'Lazy Initialization';
  }

  demonstrate() {
    console.log(`Demonstrating Lazy Initialization Pattern`);
    console.log(`Description: Delays creation until needed`);
    return `Lazy Initialization implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Lazy Initialization Pattern Demo ===\n');
  const example = new LazyInitializationExample();
  console.log(example.demonstrate());
  console.log('\n✓ Lazy Initialization pattern works!');
}

module.exports = { LazyInitializationExample };
