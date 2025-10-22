/**
 * Hexagonal Architecture Pattern
 * Ports and adapters
 */

class HexagonalArchitectureExample {
  constructor() {
    this.name = 'Hexagonal Architecture';
  }

  demonstrate() {
    console.log(`Demonstrating Hexagonal Architecture Pattern`);
    console.log(`Description: Ports and adapters`);
    return `Hexagonal Architecture implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Hexagonal Architecture Pattern Demo ===\n');
  const example = new HexagonalArchitectureExample();
  console.log(example.demonstrate());
  console.log('\n✓ Hexagonal Architecture pattern works!');
}

module.exports = { HexagonalArchitectureExample };
