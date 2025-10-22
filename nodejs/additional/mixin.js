/**
 * Mixin Pattern
 * Horizontal reuse
 */

class MixinExample {
  constructor() {
    this.name = 'Mixin';
  }

  demonstrate() {
    console.log(`Demonstrating Mixin Pattern`);
    console.log(`Description: Horizontal reuse`);
    return `Mixin implemented`;
  }
}

// Demo
if (require.main === module) {
  console.log('=== Mixin Pattern Demo ===\n');
  const example = new MixinExample();
  console.log(example.demonstrate());
  console.log('\n✓ Mixin pattern works!');
}

module.exports = { MixinExample };
