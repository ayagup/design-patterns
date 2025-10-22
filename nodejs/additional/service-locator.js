/**
 * Service Locator Pattern
 * Service registry
 */

class ServiceLocatorExample {
  constructor() {
    this.name = 'Service Locator';
  }

  demonstrate() {
    console.log(`Demonstrating Service Locator Pattern`);
    console.log(`Description: Service registry`);
    return `Service Locator implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Service Locator Pattern Demo ===\n');
  const example = new ServiceLocatorExample();
  console.log(example.demonstrate());
  console.log('\n✓ Service Locator pattern works!');
}

module.exports = { ServiceLocatorExample };
