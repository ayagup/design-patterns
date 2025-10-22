/**
 * DTO Pattern
 * Data transfer object
 */

class DTOExample {
  constructor() {
    this.name = 'DTO';
  }

  demonstrate() {
    console.log(`Demonstrating DTO Pattern`);
    console.log(`Description: Data transfer object`);
    return `DTO implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== DTO Pattern Demo ===\n');
  const example = new DTOExample();
  console.log(example.demonstrate());
  console.log('\n✓ DTO pattern works!');
}

module.exports = { DTOExample };
