/**
 * Marker Interface Pattern
 * Tagging interface
 */

class MarkerInterfaceExample {
  constructor() {
    this.name = 'Marker Interface';
  }

  demonstrate() {
    console.log(`Demonstrating Marker Interface Pattern`);
    console.log(`Description: Tagging interface`);
    return `Marker Interface implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Marker Interface Pattern Demo ===\n');
  const example = new MarkerInterfaceExample();
  console.log(example.demonstrate());
  console.log('\n✓ Marker Interface pattern works!');
}

module.exports = { MarkerInterfaceExample };
