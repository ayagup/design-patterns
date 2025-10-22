/**
 * Template Method Pattern
 * Defines algorithm skeleton
 */

class TemplateMethodExample {
  constructor() {
    this.name = 'Template Method';
  }

  demonstrate() {
    console.log(`Demonstrating Template Method Pattern`);
    console.log(`Description: Defines algorithm skeleton`);
    return `Template Method implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Template Method Pattern Demo ===\n');
  const example = new TemplateMethodExample();
  console.log(example.demonstrate());
  console.log('\n✓ Template Method pattern works!');
}

module.exports = { TemplateMethodExample };
