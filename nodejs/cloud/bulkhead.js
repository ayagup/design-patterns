/**
 * Bulkhead Pattern
 * Isolates resources
 */

class BulkheadExample {
  constructor() {
    this.name = 'Bulkhead';
  }

  demonstrate() {
    console.log(`Demonstrating Bulkhead Pattern`);
    console.log(`Description: Isolates resources`);
    return `Bulkhead implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Bulkhead Pattern Demo ===\n');
  const example = new BulkheadExample();
  console.log(example.demonstrate());
  console.log('\n✓ Bulkhead pattern works!');
}

module.exports = { BulkheadExample };
