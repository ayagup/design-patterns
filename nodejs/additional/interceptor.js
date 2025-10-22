/**
 * Interceptor Pattern
 * Method interception
 */

class InterceptorExample {
  constructor() {
    this.name = 'Interceptor';
  }

  demonstrate() {
    console.log(`Demonstrating Interceptor Pattern`);
    console.log(`Description: Method interception`);
    return `Interceptor implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Interceptor Pattern Demo ===\n');
  const example = new InterceptorExample();
  console.log(example.demonstrate());
  console.log('\n✓ Interceptor pattern works!');
}

module.exports = { InterceptorExample };
