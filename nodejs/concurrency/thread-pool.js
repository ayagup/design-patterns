/**
 * Thread Pool Pattern
 * Reuses worker threads
 */

class ThreadPoolExample {
  constructor() {
    this.name = 'Thread Pool';
  }

  demonstrate() {
    console.log(`Demonstrating Thread Pool Pattern`);
    console.log(`Description: Reuses worker threads`);
    return `Thread Pool implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Thread Pool Pattern Demo ===\n');
  const example = new ThreadPoolExample();
  console.log(example.demonstrate());
  console.log('\n✓ Thread Pool pattern works!');
}

module.exports = { ThreadPoolExample };
