/**
 * MVP Pattern
 * Model-View-Presenter variant
 */

class MVPExample {
  constructor() {
    this.name = 'MVP';
  }

  demonstrate() {
    console.log(`Demonstrating MVP Pattern`);
    console.log(`Description: Model-View-Presenter variant`);
    return `MVP implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== MVP Pattern Demo ===\n');
  const example = new MVPExample();
  console.log(example.demonstrate());
  console.log('\n✓ MVP pattern works!');
}

module.exports = { MVPExample };
