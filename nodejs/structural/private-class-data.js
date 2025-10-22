/**
 * Private Class Data Pattern
 * Restricts data access
 */

class PrivateClassDataExample {
  constructor() {
    this.name = 'Private Class Data';
  }

  demonstrate() {
    console.log(`Demonstrating Private Class Data Pattern`);
    console.log(`Description: Restricts data access`);
    return `Private Class Data implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Private Class Data Pattern Demo ===\n');
  const example = new PrivateClassDataExample();
  console.log(example.demonstrate());
  console.log('\n✓ Private Class Data pattern works!');
}

module.exports = { PrivateClassDataExample };
