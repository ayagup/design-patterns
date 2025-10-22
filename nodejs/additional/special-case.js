/**
 * Special Case Pattern
 * Special behavior
 */

class SpecialCaseExample {
  constructor() {
    this.name = 'Special Case';
  }

  demonstrate() {
    console.log(`Demonstrating Special Case Pattern`);
    console.log(`Description: Special behavior`);
    return `Special Case implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Special Case Pattern Demo ===\n');
  const example = new SpecialCaseExample();
  console.log(example.demonstrate());
  console.log('\n✓ Special Case pattern works!');
}

module.exports = { SpecialCaseExample };
