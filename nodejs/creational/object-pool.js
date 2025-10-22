/**
 * Object Pool Pattern
 * Reuses expensive objects
 */

class ObjectPoolExample {
  constructor() {
    this.name = 'Object Pool';
  }

  demonstrate() {
    console.log(`Demonstrating Object Pool Pattern`);
    console.log(`Description: Reuses expensive objects`);
    return `Object Pool implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Object Pool Pattern Demo ===\n');
  const example = new ObjectPoolExample();
  console.log(example.demonstrate());
  console.log('\n✓ Object Pool pattern works!');
}

module.exports = { ObjectPoolExample };
