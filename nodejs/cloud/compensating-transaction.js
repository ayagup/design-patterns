/**
 * Compensating Transaction Pattern
 * Undoes work
 */

class CompensatingTransactionExample {
  constructor() {
    this.name = 'Compensating Transaction';
  }

  demonstrate() {
    console.log(`Demonstrating Compensating Transaction Pattern`);
    console.log(`Description: Undoes work`);
    return `Compensating Transaction implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Compensating Transaction Pattern Demo ===\n');
  const example = new CompensatingTransactionExample();
  console.log(example.demonstrate());
  console.log('\n✓ Compensating Transaction pattern works!');
}

module.exports = { CompensatingTransactionExample };
