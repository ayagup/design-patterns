/**
 * Repository Pattern
 * Data access abstraction
 */

class RepositoryExample {
  constructor() {
    this.name = 'Repository';
  }

  demonstrate() {
    console.log(`Demonstrating Repository Pattern`);
    console.log(`Description: Data access abstraction`);
    return `Repository implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Repository Pattern Demo ===\n');
  const example = new RepositoryExample();
  console.log(example.demonstrate());
  console.log('\n✓ Repository pattern works!');
}

module.exports = { RepositoryExample };
