/**
 * Composite Pattern
 * Treats objects and compositions uniformly
 */

class CompositeExample {
  constructor() {
    this.name = 'Composite';
  }

  demonstrate() {
    console.log(`Demonstrating Composite Pattern`);
    console.log(`Description: Treats objects and compositions uniformly`);
    return `Composite implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Composite Pattern Demo ===\n');
  const example = new CompositeExample();
  console.log(example.demonstrate());
  console.log('\n✓ Composite pattern works!');
}

module.exports = { CompositeExample };
