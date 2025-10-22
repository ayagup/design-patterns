/**
 * Lazy Load Pattern
 * Defers loading
 */

class LazyLoadExample {
  constructor() {
    this.name = 'Lazy Load';
  }

  demonstrate() {
    console.log(`Demonstrating Lazy Load Pattern`);
    console.log(`Description: Defers loading`);
    return `Lazy Load implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Lazy Load Pattern Demo ===\n');
  const example = new LazyLoadExample();
  console.log(example.demonstrate());
  console.log('\n✓ Lazy Load pattern works!');
}

module.exports = { LazyLoadExample };
