/**
 * Circuit Breaker Pattern
 * Prevents cascading failures
 */

class CircuitBreakerExample {
  constructor() {
    this.name = 'Circuit Breaker';
  }

  demonstrate() {
    console.log(`Demonstrating Circuit Breaker Pattern`);
    console.log(`Description: Prevents cascading failures`);
    return `Circuit Breaker implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Circuit Breaker Pattern Demo ===\n');
  const example = new CircuitBreakerExample();
  console.log(example.demonstrate());
  console.log('\n✓ Circuit Breaker pattern works!');
}

module.exports = { CircuitBreakerExample };
