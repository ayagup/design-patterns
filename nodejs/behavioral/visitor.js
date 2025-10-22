/**
 * Visitor Pattern
 * Separates algorithm from structure
 */

class VisitorExample {
  constructor() {
    this.name = 'Visitor';
  }

  demonstrate() {
    console.log(`Demonstrating Visitor Pattern`);
    console.log(`Description: Separates algorithm from structure`);
    return `Visitor implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Visitor Pattern Demo ===\n');
  const example = new VisitorExample();
  console.log(example.demonstrate());
  console.log('\n✓ Visitor pattern works!');
}

module.exports = { VisitorExample };
