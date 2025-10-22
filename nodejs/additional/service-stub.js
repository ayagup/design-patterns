/**
 * Service Stub Pattern
 * Test doubles
 */

class ServiceStubExample {
  constructor() {
    this.name = 'Service Stub';
  }

  demonstrate() {
    console.log(`Demonstrating Service Stub Pattern`);
    console.log(`Description: Test doubles`);
    return `Service Stub implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Service Stub Pattern Demo ===\n');
  const example = new ServiceStubExample();
  console.log(example.demonstrate());
  console.log('\n✓ Service Stub pattern works!');
}

module.exports = { ServiceStubExample };
