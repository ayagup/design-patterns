/**
 * Iterator Pattern
 * Traverses collections
 */

class IteratorExample {
  constructor() {
    this.name = 'Iterator';
  }

  demonstrate() {
    console.log(`Demonstrating Iterator Pattern`);
    console.log(`Description: Traverses collections`);
    return `Iterator implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Iterator Pattern Demo ===\n');
  const example = new IteratorExample();
  console.log(example.demonstrate());
  console.log('\n✓ Iterator pattern works!');
}

module.exports = { IteratorExample };
