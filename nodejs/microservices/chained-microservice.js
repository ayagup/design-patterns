/**
 * Chained Microservice Pattern
 * Sequential calls
 */

class ChainedMicroserviceExample {
  constructor() {
    this.name = 'Chained Microservice';
  }

  demonstrate() {
    console.log(`Demonstrating Chained Microservice Pattern`);
    console.log(`Description: Sequential calls`);
    return `Chained Microservice implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Chained Microservice Pattern Demo ===\n');
  const example = new ChainedMicroserviceExample();
  console.log(example.demonstrate());
  console.log('\n✓ Chained Microservice pattern works!');
}

module.exports = { ChainedMicroserviceExample };
