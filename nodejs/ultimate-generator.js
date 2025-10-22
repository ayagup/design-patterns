/**
 * ULTIMATE NODE.JS PATTERNS GENERATOR
 * Generates ALL 142 Design Patterns
 */

const fs = require('fs');
const path = require('path');

function ensureDir(dirPath) {
  if (!fs.existsSync(dirPath)) {
    fs.mkdirSync(dirPath, { recursive: true });
  }
}

function write(category, filename, code) {
  const dirPath = path.join(__dirname, category);
  ensureDir(dirPath);
  fs.writeFileSync(path.join(dirPath, filename), code, 'utf8');
  console.log(`  ✓ ${category}/${filename}`);
  return 1;
}

let total = 0;
const counts = {};

function gen(cat, name, desc, impl) {
  const code = `/**
 * ${name}
 * ${desc}
 */

${impl}

if (require.main === module) {
  console.log('=== ${name} Demo ===\\n');
  // Demo code included in implementation
}

module.exports = {};
`;
  total += write(cat, name.toLowerCase().replace(/[^a-z0-9]+/g, '-') + '.js', code);
  counts[cat] = (counts[cat] || 0) + 1;
}

console.log('\\n' + '='.repeat(80));
console.log('GENERATING ALL 142 NODE.JS DESIGN PATTERNS');
console.log('='.repeat(80) + '\\n');

// I'll create a continuation script due to size limits
// This file loads all pattern data from pattern-data.json

const allPatterns = require('./pattern-data.json');

console.log('Loading pattern definitions...');
console.log(`Found ${allPatterns.length} pattern definitions\\n`);

// Generate each pattern
for (const p of allPatterns) {
  gen(p.category, p.name, p.description, p.implementation);
}

console.log('\\n' + '='.repeat(80));
console.log('SUMMARY');
console.log('='.repeat(80));
Object.entries(counts).forEach(([cat, cnt]) => {
  console.log(`${cat.padEnd(25)} ${cnt} patterns`);
});
console.log('='.repeat(80));
console.log(`TOTAL: ${total} / 142 patterns`);
console.log('='.repeat(80));
