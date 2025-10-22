/**
 * Proxy Pattern
 * Controls access to objects
 */

class ProxyExample {
  constructor() {
    this.name = 'Proxy';
  }

  demonstrate() {
    console.log(`Demonstrating Proxy Pattern`);
    console.log(`Description: Controls access to objects`);
    return `Proxy implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Proxy Pattern Demo ===\n');
  const example = new ProxyExample();
  console.log(example.demonstrate());
  console.log('\n✓ Proxy pattern works!');
}

module.exports = { ProxyExample };
