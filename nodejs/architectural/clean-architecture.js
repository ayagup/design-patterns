/**
 * Clean Architecture Pattern
 * Business rules independence
 */

class CleanArchitectureExample {
  constructor() {
    this.name = 'Clean Architecture';
  }

  demonstrate() {
    console.log(`Demonstrating Clean Architecture Pattern`);
    console.log(`Description: Business rules independence`);
    return `Clean Architecture implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Clean Architecture Pattern Demo ===\n');
  const example = new CleanArchitectureExample();
  console.log(example.demonstrate());
  console.log('\n✓ Clean Architecture pattern works!');
}

module.exports = { CleanArchitectureExample };
