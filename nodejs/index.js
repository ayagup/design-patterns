/**
 * Node.js Design Patterns - Main Index
 * Complete collection of 142 design patterns
 */

const fs = require('fs');
const path = require('path');

const patterns = {
  creational: [
    'singleton', 'factory-method', 'abstract-factory', 'builder', 'prototype',
    'object-pool', 'lazy-initialization', 'dependency-injection', 'multiton'
  ],
  structural: [
    'adapter', 'bridge', 'composite', 'decorator', 'facade', 'flyweight',
    'proxy', 'private-class-data', 'extension-object'
  ],
  behavioral: [
    'chain-of-responsibility', 'command', 'interpreter', 'iterator', 'mediator',
    'memento', 'observer', 'state', 'strategy', 'template-method', 'visitor',
    'null-object', 'specification', 'blackboard'
  ],
  concurrency: [
    'active-object', 'balking', 'barrier', 'double-checked-locking',
    'guarded-suspension', 'monitor-object', 'reactor', 'read-write-lock',
    'scheduler', 'thread-pool', 'thread-specific-storage', 'proactor',
    'lock', 'future-promise', 'actor-model'
  ],
  architectural: [
    'mvc', 'mvp', 'mvvm', 'layered-architecture', 'hexagonal-architecture',
    'clean-architecture', 'onion-architecture', 'pipe-and-filter', 'microkernel',
    'event-driven-architecture', 'soa', 'space-based-architecture', 'cqrs',
    'event-sourcing', 'broker-pattern'
  ],
  enterprise: [
    'repository', 'unit-of-work', 'data-mapper', 'active-record',
    'table-data-gateway', 'row-data-gateway', 'dto', 'service-layer',
    'domain-model', 'transaction-script', 'table-module', 'identity-map',
    'lazy-load', 'front-controller', 'application-controller', 'page-controller',
    'template-view', 'transform-view', 'two-step-view'
  ],
  cloud: [
    'ambassador', 'anti-corruption-layer', 'backends-for-frontends', 'bulkhead',
    'circuit-breaker', 'compensating-transaction', 'competing-consumers',
    'compute-resource-consolidation', 'event-sourcing-cloud',
    'external-configuration-store', 'federated-identity', 'gatekeeper',
    'gateway-aggregation', 'gateway-offloading', 'gateway-routing',
    'health-endpoint-monitoring', 'index-table', 'leader-election',
    'materialized-view', 'priority-queue', 'publisher-subscriber',
    'queue-based-load-leveling', 'retry', 'scheduler-agent-supervisor',
    'sharding', 'sidecar', 'static-content-hosting', 'strangler-fig',
    'throttling', 'valet-key'
  ],
  microservices: [
    'api-gateway', 'service-registry', 'saga', 'database-per-service',
    'shared-database', 'api-composition', 'aggregator', 'chained-microservice',
    'branch-microservice', 'asynchronous-messaging', 'transactional-outbox',
    'event-driven-microservices', 'distributed-tracing', 'log-aggregation',
    'application-metrics', 'audit-logging', 'exception-tracking',
    'service-mesh', 'bff'
  ],
  additional: [
    'registry', 'money', 'special-case', 'plugin', 'service-stub',
    'service-locator', 'module', 'revealing-module', 'mixin', 'twin',
    'marker-interface', 'interceptor'
  ]
};

function getPattern(category, name) {
  try {
    return require(`./${category}/${name}.js`);
  } catch (error) {
    console.error(`Error loading ${category}/${name}:`, error.message);
    return null;
  }
}

function listPatterns() {
  console.log('\\n' + '='.repeat(80));
  console.log('NODE.JS DESIGN PATTERNS - COMPLETE COLLECTION');
  console.log('='.repeat(80));
  
  let total = 0;
  for (const [category, patternList] of Object.entries(patterns)) {
    console.log(`\\n${category.toUpperCase()} (${patternList.length} patterns):`);
    patternList.forEach((pattern, index) => {
      console.log(`  ${(index + 1).toString().padStart(2)}. ${pattern}`);
      total++;
    });
  }
  
  console.log('\\n' + '='.repeat(80));
  console.log(`TOTAL: ${total} patterns`);
  console.log('='.repeat(80) + '\\n');
}

function verifyAll() {
  console.log('\\n' + '='.repeat(80));
  console.log('VERIFYING ALL PATTERNS');
  console.log('='.repeat(80) + '\\n');
  
  let verified = 0;
  let missing = 0;
  
  for (const [category, patternList] of Object.entries(patterns)) {
    console.log(`\\n${category.toUpperCase()}:`);
    for (const pattern of patternList) {
      const filePath = path.join(__dirname, category, `${pattern}.js`);
      if (fs.existsSync(filePath)) {
        console.log(`  ✓ ${pattern}`);
        verified++;
      } else {
        console.log(`  ✗ ${pattern} (MISSING)`);
        missing++;
      }
    }
  }
  
  console.log('\\n' + '='.repeat(80));
  console.log(`VERIFIED: ${verified} patterns`);
  if (missing > 0) {
    console.log(`MISSING: ${missing} patterns`);
  }
  console.log('='.repeat(80) + '\\n');
  
  return missing === 0;
}

if (require.main === module) {
  console.log('\\nNode.js Design Patterns Collection\\n');
  console.log('Available commands:');
  console.log('  node index.js list    - List all patterns');
  console.log('  node index.js verify  - Verify all patterns exist');
  console.log('  node index.js help    - Show this help');
  
  const command = process.argv[2];
  
  if (command === 'list') {
    listPatterns();
  } else if (command === 'verify') {
    const allOk = verifyAll();
    process.exit(allOk ? 0 : 1);
  } else {
    console.log('\\nUse one of the commands above.\\n');
  }
}

module.exports = {
  patterns,
  getPattern,
  listPatterns,
  verifyAll
};
