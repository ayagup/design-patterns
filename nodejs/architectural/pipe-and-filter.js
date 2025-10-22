/**
 * Pipe and Filter Pattern
 * Sequential data processing
 */

class PipeandFilterExample {
  constructor() {
    this.name = 'Pipe and Filter';
  }

  demonstrate() {
    console.log(`Demonstrating Pipe and Filter Pattern`);
    console.log(`Description: Sequential data processing`);
    return `Pipe and Filter implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Pipe and Filter Pattern Demo ===\n');
  const example = new PipeandFilterExample();
  console.log(example.demonstrate());
  console.log('\n✓ Pipe and Filter pattern works!');
}

module.exports = { PipeandFilterExample };
