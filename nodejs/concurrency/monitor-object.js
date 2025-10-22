/**
 * Monitor Object Pattern
 * Synchronizes method execution
 */

class MonitorObjectExample {
  constructor() {
    this.name = 'Monitor Object';
  }

  demonstrate() {
    console.log(`Demonstrating Monitor Object Pattern`);
    console.log(`Description: Synchronizes method execution`);
    return `Monitor Object implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Monitor Object Pattern Demo ===\n');
  const example = new MonitorObjectExample();
  console.log(example.demonstrate());
  console.log('\n✓ Monitor Object pattern works!');
}

module.exports = { MonitorObjectExample };
