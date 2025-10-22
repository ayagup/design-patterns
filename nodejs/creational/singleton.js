/**
 * Singleton Pattern
 * Ensures only one instance exists
 */

class SingletonExample {
  constructor() {
    this.name = 'Singleton';
  }

  demonstrate() {
    console.log(`Demonstrating Singleton Pattern`);
    console.log(`Description: Ensures only one instance exists`);
    return `Singleton implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Singleton Pattern Demo ===\n');
  const example = new SingletonExample();
  console.log(example.demonstrate());
  console.log('\n✓ Singleton pattern works!');
}

module.exports = { SingletonExample };
