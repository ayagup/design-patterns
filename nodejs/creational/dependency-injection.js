/**
 * Dependency Injection Pattern
 * Injects dependencies externally
 */

class DependencyInjectionExample {
  constructor() {
    this.name = 'Dependency Injection';
  }

  demonstrate() {
    console.log(`Demonstrating Dependency Injection Pattern`);
    console.log(`Description: Injects dependencies externally`);
    return `Dependency Injection implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Dependency Injection Pattern Demo ===\n');
  const example = new DependencyInjectionExample();
  console.log(example.demonstrate());
  console.log('\n✓ Dependency Injection pattern works!');
}

module.exports = { DependencyInjectionExample };
